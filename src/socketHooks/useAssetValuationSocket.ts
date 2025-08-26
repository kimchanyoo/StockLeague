// hooks/useAssetSocket.ts
import { useEffect, useRef, useState } from "react";;
import { Client, IMessage } from "@stomp/stompjs";
import { getUserAssetValuation, UserAssetValuation } from "@/lib/api/user";

interface UseAssetValuationSocketParams {
  accessToken: string | null;
  onUpdate: (data: UserAssetValuation) => void;
}

export const useAssetValuationSocket = ({
  accessToken,
  onUpdate,
}: UseAssetValuationSocketParams) => {
  const [isMarketOpen, setIsMarketOpen] = useState<boolean | null>(null);
  const clientRef = useRef<Client | null>(null);

  // 1. 초기 API 호출 (장 여부 + 초기 자산 상태)
  useEffect(() => {
    if (!accessToken) return;
    
    getUserAssetValuation()
      .then((data) => {
        //console.log("📦 초기 자산 데이터:", data);
        setIsMarketOpen(data.marketOpen);
        onUpdate(data);
      })
      .catch((err) => {
        //console.error("초기 자산 불러오기 실패:", err);
      });
  }, [accessToken, onUpdate]);

  // 2. 장 열려 있을 경우에만 WebSocket 연결
  useEffect(() => {
    //console.log("▶ useEffect 실행 - isMarketOpen:", isMarketOpen, "accessToken:", accessToken);

    if (!isMarketOpen) {
      //console.log("장 미개장 상태라서 WebSocket 연결 안함");
      return;
    }
    if (!accessToken) {
      //console.log("accessToken 없음 - WebSocket 연결 안함");
      return;
    }
    if (!process.env.NEXT_PUBLIC_SOCKET_URL) {
      //console.error("NEXT_PUBLIC_SOCKET_URL 환경변수 미설정");
      return;
    }

    console.log("WebSocket URL:", process.env.NEXT_PUBLIC_SOCKET_URL);

    const client = new Client({
      webSocketFactory: () => {
        console.log("WebSocket 생성 시도");
        return  new WebSocket(`${process.env.NEXT_PUBLIC_SOCKET_URL}?access_token=${accessToken}`)
      },
      reconnectDelay: 10000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        //console.log("✅ WebSocket 연결 성공");
        client.subscribe("/user/queue/asset", (message: IMessage) => {
          try {
            const data: UserAssetValuation = JSON.parse(message.body);
            console.log("📡 실시간 자산 데이터 수신:", data);
            if (data.marketOpen) {
              onUpdate(data);
            }
          } catch (err) {
            //console.error("실시간 자산 데이터 파싱 실패:", err);
          }
        });
      },
      onDisconnect: () => {
        console.log("⛔ WebSocket 연결 끊김");
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      console.log("⛔ WebSocket 연결 해제");
      client.deactivate();
      clientRef.current = null;
    };
  }, [isMarketOpen, accessToken, onUpdate]);
};
