import { useEffect, useState, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { getRanking, GetRankingResponse, RankingMode } from "@/lib/api/rank";
import { useAuth } from "@/context/AuthContext";

interface UseRankingSocketParams {
  mode: RankingMode;
  onUpdateGlobal: (data: GetRankingResponse) => void;
  onUpdateMe: (data: GetRankingResponse) => void;
}

export const useRankingSocket = ({ mode, onUpdateGlobal, onUpdateMe }: UseRankingSocketParams) => {
  const { accessToken } = useAuth();
  const [isMarketOpen, setIsMarketOpen] = useState<boolean | null>(null);
  const clientRef = useRef<Client | null>(null);

  // 초기 API
  useEffect(() => {
    if (!accessToken) return;

    getRanking(mode)
      .then((data) => {
        setIsMarketOpen(data.isMarketOpen);
        onUpdateGlobal(data);
        onUpdateMe(data);
      })
      .catch((error) => {
        console.error(`❌ ${mode} 랭킹 API 실패:`, error);
      });
  }, [accessToken, mode, onUpdateGlobal, onUpdateMe]);

  // WebSocket
  useEffect(() => {
    if (!isMarketOpen || !accessToken) return;

    const topic = mode === "profit" ? "/topic/ranking/profit" : "/topic/ranking/asset";
    const queue = mode === "profit" ? "/user/queue/ranking/me/profit" : "/user/queue/ranking/me/asset";

    const client = new Client({
      webSocketFactory: () => new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 10000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        console.log(`✅ ${mode} WebSocket 연결 성공`);

        client.subscribe(topic, (message: IMessage) => {
          const data: GetRankingResponse = JSON.parse(message.body);
          onUpdateGlobal(data);
        });

        client.subscribe(queue, (message: IMessage) => {
          const data: GetRankingResponse = JSON.parse(message.body);
          onUpdateMe(data);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      console.log("🔌 WebSocket 해제");
      client.deactivate();
      clientRef.current = null;
    };
  }, [isMarketOpen, accessToken, mode, onUpdateGlobal, onUpdateMe]);
};
