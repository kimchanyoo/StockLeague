import { useEffect, useState, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { getProfitRanking, GetProfitRankingResponse } from "@/lib/api/rank";
import { useAuth } from "@/context/AuthContext";

interface UseProfitRankingParams {
  onUpdateGlobal: (data: GetProfitRankingResponse) => void;
  onUpdateMe: (data: GetProfitRankingResponse) => void;
}

export const useRankingSocket = ({ onUpdateGlobal, onUpdateMe }: UseProfitRankingParams) => {
  const { accessToken } = useAuth();
  const [isMarketOpen, setIsMarketOpen] = useState<boolean | null>(null);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!accessToken) return;

    console.log("🔑 accessToken 확인됨. 장중 여부 및 초기 데이터 요청 중...");
    getProfitRanking()
      .then((data) => {
        console.log("📦 초기 자산 데이터 수신:", data);
        setIsMarketOpen(data.isMarketOpen);
        onUpdateGlobal(data);
        onUpdateMe(data);
      })
      .catch((error) => {
        //console.error("❌ 초기 자산 데이터 요청 실패:", error);
      });
  }, [accessToken, onUpdateGlobal, onUpdateMe]);

  useEffect(() => {
    if (!isMarketOpen || !accessToken) {
      console.log("⏸️ WebSocket 연결 조건 불충족. 연결하지 않음.");
      return;
    }

    console.log("🔌 WebSocket 클라이언트 생성 및 연결 시도 중...");

    const client = new Client({
      webSocketFactory: () => new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 10000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        console.log("✅ WebSocket 연결 성공. /topic/ranking 구독 요청 중...");

        // 전체 랭킹 구독
        client.subscribe("/topic/ranking", (message: IMessage) => {
          const data: GetProfitRankingResponse = JSON.parse(message.body);
          console.log("📡 실시간 자산 데이터 수신:", data);
          onUpdateGlobal(data);
        });

        // 개인 랭킹 구독
        client.subscribe("/user/queue/ranking/me", (message: IMessage) => {
          const data: GetProfitRankingResponse = JSON.parse(message.body);
          console.log("📡 개인 랭킹 데이터 수신:", data);
          onUpdateMe(data);
        });

        console.log("📬 /topic/ranking 구독 완료.");
      },
      onStompError: (frame) => {
        //console.error("❗ STOMP 에러 발생:", frame);
      },
      onWebSocketError: (event) => {
        //console.error("❗ WebSocket 에러 발생:", event);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      console.log("🔌 WebSocket 연결 해제 중...");
      client.deactivate();
      clientRef.current = null;
    };
  }, [isMarketOpen, accessToken, onUpdateGlobal, onUpdateMe]);
};
