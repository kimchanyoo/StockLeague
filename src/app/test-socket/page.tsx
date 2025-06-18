"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import { useAuth } from "@/context/AuthContext"; // ✔️ 프로젝트에서 사용하는 실제 경로
import { toast } from "react-hot-toast"; // (npm i react-hot-toast)

/**
 * STOMP WebSocket 테스트 페이지 – 실전 환경에 맞춘 수정본
 *
 * 변경 내용
 * 1) `useAuth()` 훅을 사용해 accessToken을 받아 "토큰이 나중에 생기는" 상황에서도 자동 재시도
 * 2) 하트비트(10s)·재연결 주기(20s) 튜닝 & cleanup 함수로 구독 중복 제거
 * 3) alert → toast, Tailwind `h-[150px]`, `crypto.randomUUID()` 키 등 UI 개선
 */
export default function TestSocketPage() {
  const { accessToken } = useAuth(); // null 가능성이 있으면 optional 체크

  const [connected, setConnected] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  const clientRef = useRef<Client | null>(null);
  const subsRef = useRef<StompSubscription[]>([]);

  /** 안전하게 로그 추가 */
  const pushLog = useCallback((msg: string) => {
    setLogs((prev) => [...prev, msg]);
  }, []);

  /** 구독·세션 정리 */
  const cleanup = useCallback(() => {
    subsRef.current.forEach((sub) => sub.unsubscribe());
    subsRef.current = [];
    clientRef.current?.deactivate();
    clientRef.current = null;
    setConnected(false);
  }, []);

  useEffect(() => {
    // 토큰이 없으면 연결을 시도하지 않고, 기존 연결이 있으면 끊습니다.
    if (!accessToken) {
      cleanup();
      return;
    }
    if (clientRef.current) return; // 이미 연결 중이면 무시

    const socketUrl = process.env.NEXT_PUBLIC_SOCKET_URL!; 
    pushLog("🪙 Token: " + accessToken);
    const client = new Client({
      webSocketFactory: () => new WebSocket(socketUrl),
      connectHeaders: { Authorization: `Bearer ${accessToken}` }, 
      debug: (s) => pushLog("[DBG] " + s),
      reconnectDelay: 20_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,

      onConnect: () => {
        setConnected(true);
        pushLog("✅ CONNECTED");

        // 혹시 남아 있던 구독 제거 후 재구독
        subsRef.current.forEach((s) => s.unsubscribe());
        subsRef.current = [];

        // 개인 알림 큐 – /user/queue/**
        subsRef.current.push(
          client.subscribe(
            "/user/queue/notifications",
            (m: IMessage) => {
              pushLog("📩 개인: " + m.body);
              toast.success("📩 개인 메시지: " + m.body);
            },
            { id: "noti-sub" }
          )
        );

        // ping 토픽
        subsRef.current.push(
          client.subscribe(
            "/topic/ping",
            (m: IMessage) => pushLog("🌐 ping: " + m.body),
            { id: "ping-sub" }
          )
        );

        // broadcast 토픽
        subsRef.current.push(
          client.subscribe(
            "/topic/broadcast",
            (m: IMessage) => pushLog("📢 broadcast: " + m.body),
            { id: "broadcast-sub" }
          )
        );

        /** 테스트 퍼블리시 */
        setTimeout(() => {
          client.publish({
            destination: "/pub/test",
            body: "테스트 메시지이다임마 제발 ✅",
          });
          pushLog("📤 개인 테스트 발행");
        }, 4_000);

        setTimeout(() => {
          client.publish({ destination: "/pub/ping", body: "ping" });
          pushLog("📤 /pub/ping 발행");
        }, 800);

        setTimeout(() => {
          client.publish({
            destination: "/pub/broadcast",
            body: "모든 유저에게 보내는 테스트 브로드캐스트 ✅",
          });
          pushLog("📤 /pub/broadcast 발행");
        }, 1_200);
      },

      onStompError: (frame) => {
        pushLog(`❌ STOMP 에러: ${frame.headers["message"]}`);
      },

      onWebSocketClose: () => {
        pushLog("⚠️ WebSocket 연결 종료");
        cleanup();
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      cleanup();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessToken]); // accessToken 변화 시 자동 재시도

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold mb-4">📡 WebSocket 테스트</h1>
      <p>연결 상태: {connected ? "✅ 연결됨" : "❌ 끊김"}</p>
      <div className="mt-4 bg-gray-100 p-3 rounded h-150 overflow-auto text-sm space-y-0.5">
        {logs.map((log) => (
          <div key={crypto.randomUUID()}>{log}</div>
        ))}
      </div>
    </div>
  );
}
