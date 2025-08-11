import { useEffect, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { StockPriceResponse, getStockPrice } from "@/lib/api/stock";

export function useMainStockPriceSocket(
  ticker: string,
  onUpdate: (data: StockPriceResponse) => void,
  accessToken?: string
) {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!ticker) return;

    let socketClient: Client | null = null;

    const connectHeaders: Record<string, string> = accessToken
      ? { Authorization: `Bearer ${accessToken}` }
      : {};

    const init = async () => {
      try {
        const initialData = await getStockPrice(ticker);
        onUpdate(initialData);

        if (!initialData.isMarketOpen) {
          //console.log(`🛑 장 종료(${ticker}) - WebSocket 연결하지 않음`);
          return; // 장 닫혀있으면 WebSocket 연결 안함
        }

        socketClient = new Client({
          webSocketFactory: () => new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
          connectHeaders,
          reconnectDelay: 10000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
          onConnect: () => {
            console.log(`✅ WebSocket 연결 성공 (${ticker})`);
            socketClient?.subscribe(`/topic/stocks/${ticker}`, (message: IMessage) => {
              try {
                const data: StockPriceResponse = JSON.parse(message.body);
                onUpdate(data);
              } catch (err) {
                console.error(`❌ JSON 파싱 오류 (${ticker}):`, err);
              }
            });
          },
          onStompError: (frame) => {
            console.error("🛑 STOMP 에러", frame.headers["message"], frame.body);
          },
        });

        socketClient.activate();
        clientRef.current = socketClient;
      } catch (err) {
        console.error(`❌ 초기 가격 조회 실패 (${ticker}):`, err);
      }
    };

    init();

    return () => {
      if (clientRef.current) {
        //console.log(`🔌 WebSocket 연결 해제 (${ticker})`);
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, [ticker, accessToken]);
}
