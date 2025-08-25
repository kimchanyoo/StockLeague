"use client";

import { useEffect, useState, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { useAuth } from "@/context/AuthContext";

export const useWebSocketHealth = (socketUrl: string) => {
  const { accessToken } = useAuth();
  const [isHealthy, setIsHealthy] = useState(true);
  const clientRef = useRef<Client | null>(null);
  const pongTimeout = useRef<number | null>(null);
  const pingInterval = useRef<number | null>(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new WebSocket(`${process.env.NEXT_PUBLIC_SOCKET_URL}?access_token=${accessToken}`),
      reconnectDelay: 5000,
      onConnect: () => {
        // pong 구독
        client.subscribe("/user/queue/health", (message: IMessage) => {
          try {
            const body = JSON.parse(message.body);
            if (body.type === "PONG") {
              setIsHealthy(true);
              if (pongTimeout.current) clearTimeout(pongTimeout.current);
              pongTimeout.current = window.setTimeout(() => setIsHealthy(false), 5000);
            }
          } catch {
            setIsHealthy(false);
          }
        });

        // ✅ ping interval은 연결이 완료된 onConnect 안에서 시작
        pingInterval.current = window.setInterval(() => {
          try {
            client.publish({
              destination: "/pub/health.ping",
              body: JSON.stringify({ type: "PING" }),
            });
          } catch (err) {
            console.warn("ping 전송 실패:", err);
            setIsHealthy(false);
          }
        }, 3000);
      },
      onStompError: () => setIsHealthy(false),
      onWebSocketClose: () => setIsHealthy(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (pongTimeout.current) clearTimeout(pongTimeout.current);
      if (pingInterval.current) clearInterval(pingInterval.current);
      client.deactivate();
    };
  }, [socketUrl]);

  return isHealthy;
};
