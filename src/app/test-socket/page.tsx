"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import { useAuth } from "@/context/AuthContext";
import { toast } from "react-hot-toast";

export default function TestSocketPage() {
  const { accessToken } = useAuth();
  const [connected, setConnected] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);
  const [msg, setMsg] = useState("");

  const clientRef = useRef<Client | null>(null);
  const subsRef = useRef<StompSubscription[]>([]);

  const pushLog = useCallback((msg: string) => setLogs(prev => [...prev, msg]), []);

  const cleanup = useCallback(() => {
    subsRef.current.forEach(sub => sub.unsubscribe());
    subsRef.current = [];
    clientRef.current?.deactivate();
    clientRef.current = null;
    setConnected(false);
  }, []);

  useEffect(() => {
    if (!accessToken) { cleanup(); return; }
    if (clientRef.current) return;

    const socketUrl = process.env.NEXT_PUBLIC_SOCKET_URL!;
    pushLog("🪙 Token: " + accessToken);

    // brokerURL에 쿼리스트링으로 토큰 전달
    const client = new Client({
      brokerURL: `${socketUrl}?access_token=${encodeURIComponent(accessToken)}`,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: s => pushLog("[DBG] " + s),
      onConnect: () => {
        setConnected(true);
        pushLog("✅ CONNECTED");

        // 기존 구독 초기화
        subsRef.current.forEach(s => s.unsubscribe());
        subsRef.current = [];

        // 🔔 개인 메시지 구독
        subsRef.current.push(
          client.subscribe("/user/queue/notifications", (m: IMessage) => {
            pushLog("📩 받은 메시지: " + m.body);
            toast.success("📩 받은 메시지: " + m.body);
          }, { id: "noti-sub" })
        );
      },
      onStompError: frame => pushLog(`❌ STOMP 에러: ${frame.headers["message"]}`),
      onWebSocketClose: () => {
        pushLog("⚠️ WebSocket 연결 종료");
        cleanup();
      },
    });

    clientRef.current = client;
    client.activate();

    return () => cleanup();
  }, [accessToken]);

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
