"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuth } from "@/context/AuthContext";
import { toast } from "react-hot-toast";

export default function TestSocketPage() {
  const { accessToken } = useAuth();
  const [connected, setConnected] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);
  const [msg, setMsg] = useState("");

  const clientRef = useRef<Client | null>(null);
  const subsRef = useRef<StompSubscription[]>([]);
  const connectingRef = useRef(false);

  const pushLog = useCallback((msg: string) => setLogs(prev => [...prev, msg]), []);

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

    // 이미 연결 중이거나 Client가 있으면 실행하지 않음
    if (clientRef.current || connectingRef.current) return;
    connectingRef.current = true;

    pushLog("🪙 Token: " + accessToken);

    const client = new Client({
      webSocketFactory: () => new SockJS(`http://130.162.145.59:8080/ws-sockjs?access_token=${accessToken}`),
      heartbeatIncoming: 10000,
      reconnectDelay: 3000,
      debug: s => pushLog("[DBG] " + s),
      onConnect: () => {
        setConnected(true);
        pushLog("✅ CONNECTED");

        // 구독이 이미 있으면 새로 구독하지 않음
        if (subsRef.current.length === 0) {
          const sub = client.subscribe("/user/queue/notifications", (m: IMessage) => {
            try {
              const data = JSON.parse(m.body);
              pushLog("📩 받은 메시지: " + JSON.stringify(data));
              toast.success("📩 받은 메시지: " + data.message);
            } catch {
              pushLog("📩 받은 메시지: " + m.body);
              toast.success("📩 받은 메시지: " + m.body);
            }
          });
          subsRef.current.push(sub);
          pushLog("🟢 SUBSCRIBED /user/queue/notifications");
        }
      },
      onStompError: frame => pushLog(`❌ STOMP 에러: ${frame.headers["message"]}`),
      onWebSocketClose: () => {
        pushLog("⚠️ WebSocket 연결 종료");
        cleanup();
      },
    });

    clientRef.current = client;
    client.activate();

    // StrictMode 중복 마운트 방지
    return () => {
      cleanup();
    };
  }, [accessToken, cleanup, pushLog]);

  const sendMessage = () => {
    if (!clientRef.current || !msg) return;

    clientRef.current.publish({
      destination: "/pub/test",
      body: msg,
    });

    pushLog("📤 메시지 발송: " + msg);
    setMsg("");
  };

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold mb-4">📡 WebSocket 테스트</h1>
      <p>연결 상태: {connected ? "✅ 연결됨" : "❌ 끊김"}</p>

      <div className="mt-4 bg-gray-100 p-3 rounded h-150 overflow-auto text-sm space-y-0.5">
        {logs.map(log => <div key={crypto.randomUUID()}>{log}</div>)}
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
