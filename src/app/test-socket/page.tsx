"use client";

import { useEffect, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

export default function TestSocketPage() {
  const [connected, setConnected] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    setLogs((prev) => [...prev, "accessToken 발행: " + token]);
    if (!token) return;

    const socketUrl = process.env.NEXT_PUBLIC_SOCKET_URL;
    if (!socketUrl) {
      console.error("❌ 소켓 URL이 정의되지 않았습니다.");
      return;
    }

    const client = new Client({
      webSocketFactory: () => new WebSocket(socketUrl),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
        
      },
      
      debug: (str) => {
        console.log("[STOMP DEBUG]", str);
        setLogs((prev) => [...prev, "[DEBUG] " + str]);
      },
      reconnectDelay: 5000,
      // 수신 및 송신 하트비트 주기: 4초
      // 하트비트란 정기적으로 보내지는 작은 메시지로, 상대방이 연결되어 있는지 확인하는 용도로 사용된다.
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log("✅ WebSocket 연결 완료");
        setLogs((prev) => [...prev, "✅ WebSocket 연결 완료"]);
        setConnected(true);

        client.subscribe("/user/queue/notifications", (message: IMessage) => {
          console.log("📩 수신된 메시지:", message.body);
          setLogs((prev) => [...prev, "📩 수신된 메시지: " + message.body]);
        });

        // 딜레이 후 메시지 전송
        setTimeout(() => {
          client.publish({
            destination: "/pub/test",
            body: "프론트에서 보낸 메시지 아아아아아앙 ✅",
          });
          setLogs((prev) => [...prev, "📤 테스트 메시지 전송"]);
        }, 300);
      },
      onStompError: (frame) => {
        console.error("❌ STOMP 에러:", frame.headers["message"]);
        console.error("세부 정보:", frame.body);
        setLogs((prev) => [
          ...prev,
          `❌ STOMP 에러: ${frame.headers["message"]}`,
          `세부 정보: ${frame.body}`,
        ]);
      },
      onWebSocketClose: () => {
        console.warn("⚠️ WebSocket 연결 종료");
        setConnected(false);
        setLogs((prev) => [...prev, "⚠️ WebSocket 연결 종료"]);
      },
    });

    client.activate();

    return () => {
      client.deactivate().then(() => {
        setConnected(false);
        setLogs((prev) => [...prev, "🛑 연결 종료"]);
      });
    };
  }, []); // 의존성 없음

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold mb-4">📡 WebSocket 테스트</h1>
      <p>연결 상태: {connected ? "✅ 연결됨" : "❌ 끊김"}</p>
      <div className="mt-4 bg-gray-100 p-3 rounded h-200 overflow-auto text-sm">
        {logs.map((log, i) => (
          <div key={i}>{log}</div>
        ))}
      </div>
    </div>
  );
}
