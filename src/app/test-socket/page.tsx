"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import { useAuth } from "@/context/AuthContext";

interface PongDto {
  type: string;
  echo: string;
  ts: number;
}

interface LogItem {
  type: "INFO" | "PONG" | "MESSAGE" | "ERROR";
  text: string;
}

export default function TestSocketPage() {
  const { accessToken } = useAuth();
  const [connected, setConnected] = useState(false);
  const [logs, setLogs] = useState<LogItem[]>([]);
  const [msg, setMsg] = useState("");

  const clientRef = useRef<Client | null>(null);
  const subsRef = useRef<StompSubscription[]>([]);
  const connectingRef = useRef(false);

  const pushLog = useCallback((text: string, type: LogItem["type"] = "INFO") => {
    setLogs(prev => [...prev, { type, text }]);
  }, []);

  const cleanup = useCallback(() => {
    subsRef.current.forEach(sub => sub.unsubscribe());
    subsRef.current = [];
    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
    }
    setConnected(false);
    connectingRef.current = false;
  }, []);

  useEffect(() => {
    if (!accessToken) {
      cleanup();
      return;
    }

    if (clientRef.current || connectingRef.current) return;
    connectingRef.current = true;

    pushLog("🪙 Token: " + accessToken, "INFO");

    const client = new Client({
      webSocketFactory: () =>
        new WebSocket(`${process.env.NEXT_PUBLIC_SOCKET_URL}?access_token=${accessToken}`),
      heartbeatIncoming: 10000,
      reconnectDelay: 3000,
      debug: (msg: string) => pushLog(`[DBG] ${msg}`, "INFO"),
      onConnect: () => {
        pushLog("onConnect 실행됨", "INFO");
        setConnected(true);
        pushLog("✅ CONNECTED", "INFO");

        // 1️⃣ 개인 큐 구독
        const sub1 = client.subscribe("/user/queue/notifications", (m: IMessage) => {
          console.log("[USER RAW]", m.command, m.headers, m.body);
          try {
            const parsed: PongDto = JSON.parse(m.body);
            pushLog(`📩 user MESSAGE: type=${parsed.type}, echo=${parsed.echo}, ts=${parsed.ts}`, "MESSAGE");
          } catch {
            pushLog(`📩 user MESSAGE(raw): ${m.body}`, "MESSAGE");
          }
        });
        subsRef.current.push(sub1);
        pushLog("🟢 SUBSCRIBED /user/queue/notifications", "INFO");

        // 2️⃣ 브로드캐스트 구독
        const sub2 = client.subscribe("/topic/broadcast", (m: IMessage) => {
          console.log("[BROADCAST RAW]", m.command, m.headers, m.body);
          try {
            const parsed: PongDto = JSON.parse(m.body);
            pushLog(`📡 broadcast MESSAGE: type=${parsed.type}, echo=${parsed.echo}, ts=${parsed.ts}`, "MESSAGE");
          } catch {
            pushLog(`📡 broadcast MESSAGE(raw): ${m.body}`, "MESSAGE");
          }
        });
        subsRef.current.push(sub2);
        pushLog("🟢 SUBSCRIBED /topic/broadcast", "INFO");
      },
      onStompError: (frame) => pushLog(`❌ STOMP 에러: ${frame.headers["message"]}`, "ERROR"),
      onWebSocketClose: () => {
        pushLog("⚠️ WebSocket 연결 종료", "INFO");
        cleanup();
      },
      onUnhandledMessage: (m) => {
        console.log("[UNHANDLED]", m);
        pushLog(`[UNHANDLED] ${m.body}`, "ERROR");
      },
    });

    clientRef.current = client;
    client.activate();

    return () => cleanup();
  }, [accessToken, cleanup, pushLog]);

  const sendMessage = useCallback(() => {
    if (!clientRef.current) return;

    const message = msg || "자동 발송 메시지";
    clientRef.current.publish({ destination: "/pub/test", body: message });
    clientRef.current.publish({ destination: "/pub/broadcast", body: message });

    pushLog("📤 메시지 발송: " + message, "INFO");
    setMsg("");
  }, [msg, pushLog]);

  useEffect(() => {
    if (!connected) return;
    const interval = setInterval(sendMessage, 10000);
    return () => clearInterval(interval);
  }, [connected, sendMessage]);

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold mb-4">📡 WebSocket 테스트</h1>
      <p>연결 상태: {connected ? "✅ 연결됨" : "❌ 끊김"}</p>

      <div className="mt-4 bg-gray-100 p-3 rounded h-150 overflow-auto text-sm space-y-0.5">
        {logs.map((log, idx) => (
          <div
            key={idx}
            className={
              log.type === "PONG"
                ? "text-yellow-600"
                : log.type === "MESSAGE"
                ? "text-green-600"
                : log.type === "ERROR"
                ? "text-red-600"
                : "text-gray-800"
            }
          >
            {log.text}
          </div>
        ))}
      </div>

      <div className="mt-2 flex gap-2">
        <input
          type="text"
          value={msg}
          onChange={(e) => setMsg(e.target.value)}
          placeholder="보낼 메시지"
          className="border p-1 rounded flex-1"
        />
        <button onClick={sendMessage} className="bg-blue-500 text-white px-3 rounded">
          전송
        </button>
      </div>
    </div>
  );
}
