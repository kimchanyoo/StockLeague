import { useEffect, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { StockPriceResponse, getStockPrice } from "@/lib/api/stock";

export const useStockPriceMultiSocket = (
  tickers: string[],
  onUpdate: (data: StockPriceResponse) => void,
  accessToken?: string
) => {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (tickers.length === 0) return;

    const connectHeaders: Record<string, string> = accessToken
      ? { Authorization: `Bearer ${accessToken}` }
      : {};

    const socketClient = new Client({
      webSocketFactory: () =>
        new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
      connectHeaders,
      reconnectDelay: 10000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        //console.log("✅ WebSocket 연결 성공");
        subscribedTickers.forEach((ticker) => {
          //console.log(`🔔 구독 시작: /topic/stocks/${ticker}`);
          socketClient.subscribe(`/topic/stocks/${ticker}`, (message: IMessage) => {
            try {
              const data: StockPriceResponse = JSON.parse(message.body);
              //console.log(`📈 실시간 수신 - ${ticker}:`, data);
              onUpdate(data);
            } catch (_err) {
              //console.error(`❌ JSON 파싱 오류 (${ticker})`, err, message.body);
            }
          });
        });
      },
      onStompError: (_frame) => {
        //console.error("🛑 STOMP 실시간 주식가격 에러", frame.headers["message"], frame.body);
      },
    });

    const subscribedTickers: string[] = [];

    const init = async () => {
      const promises = tickers.map(async (ticker) => {
        try {
          const initialData = await getStockPrice(ticker);
          //console.log(`🌅 초기 가격 수신 (${ticker}):`, initialData);
          onUpdate(initialData);

          if (initialData.isMarketOpen) {
            subscribedTickers.push(ticker);
          } else {
            //console.log(`🛑 장 종료 (${ticker}) - WebSocket 연결 안함`);
          }
        } catch (err) {
         // console.error(`❌ 초기 가격 조회 실패 (${ticker})`, err);
        }
      });

      await Promise.all(promises);

      if (subscribedTickers.length > 0) {
        socketClient.activate();
        clientRef.current = socketClient;
      }
    };

    init();

    return () => {
      console.log("🔌 WebSocket 연결 해제");
      clientRef.current?.deactivate();
    };
  }, [tickers.join(","), accessToken]);
};
