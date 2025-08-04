import { useEffect, useState, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { getProfitRanking, GetProfitRankingResponse, } from "@/lib/api/rank";

interface UseProfitRankingParams {
  accessToken: string | null;
  onUpdate: (data: GetProfitRankingResponse) => void;
}

export const useRankingSocket = ({ accessToken, onUpdate }: UseProfitRankingParams) => {
  const [isMarketOpen, setIsMarketOpen] = useState<boolean | null>(null);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!accessToken) return;

    // API 한번 호출해서 장중 여부 확인 + 초기 데이터 받기
    getProfitRanking()
      .then((data) => {
        console.log("📦 초기 자산 데이터:", data);
        setIsMarketOpen(data.isMarketOpen);
        onUpdate(data);
      })
      .catch(console.error);
  }, [accessToken, onUpdate]);

  // WebSocket은 isMarketOpen이 true일 때만 연결
  useEffect(() => {
    if (!isMarketOpen || !accessToken) return;

    const client = new Client({
      webSocketFactory: () => new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 10000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        client.subscribe("/topic/ranking", (message: IMessage) => {
          const data: GetProfitRankingResponse = JSON.parse(message.body);
          console.log("📡 실시간 자산 데이터 수신:", data);
          onUpdate(data);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [isMarketOpen, accessToken, onUpdate]);
};

