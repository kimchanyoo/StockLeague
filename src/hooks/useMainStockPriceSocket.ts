import { useEffect, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { StockPriceResponse, getStockPrice } from "@/lib/api/stock";

export function useMainStockPriceSocket(
  ticker: string,
  onUpdate: (data: StockPriceResponse) => void,
  accessToken?: string
) {
  const clientRef = useRef<Client | null>(null);
  const activatedRef = useRef(false);
  const unmountingRef = useRef(false);

  useEffect(() => {
    if (!ticker) return;
    // 토큰 필수 정책이면 가드 켜기
    // if (!accessToken) return;

    unmountingRef.current = false;

    const url = process.env.NEXT_PUBLIC_SOCKET_URL;
    if (!url) {
      console.error("NEXT_PUBLIC_SOCKET_URL is not set");
      return;
    }

    const connectHeaders: Record<string, string> = accessToken
      ? { Authorization: `Bearer ${accessToken}` }
      : {};

    const init = async () => {
      try {
        const initialData = await getStockPrice(ticker);
        onUpdate(initialData);

        if (!initialData.isMarketOpen) return;
        if (activatedRef.current) return;

        const client = new Client({
          webSocketFactory: () => new WebSocket(url),
          connectHeaders,
          reconnectDelay: 10000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
          onConnect: () => {
            activatedRef.current = true;
            client.subscribe(`/topic/stocks/${ticker}`, (msg: IMessage) => {
              try {
                onUpdate(JSON.parse(msg.body) as StockPriceResponse);
              } catch (e) {
                console.error("JSON 파싱 오류:", e);
              }
            });
          },
          onDisconnect: () => { activatedRef.current = false; },
          onWebSocketClose: (evt) => {
            activatedRef.current = false;
            console.log("WS closed", evt?.code, evt?.reason);
          },
          onWebSocketError: (evt) => {
            console.error("WS error", evt);
          },
          onStompError: (frame) => {
            console.error("STOMP 에러", frame.headers["message"], frame.body);
          },
        });

        client.activate();
        clientRef.current = client;
      } catch (e) {
        console.error("초기 가격 조회 실패:", e);
      }
    };

    init();

    return () => {
      unmountingRef.current = true;
      setTimeout(() => {
        if (unmountingRef.current && clientRef.current?.active) {
          clientRef.current.deactivate();
          clientRef.current = null;
          activatedRef.current = false;
        }
      }, 0);
    };
  }, [ticker /*, accessToken*/]); // 토큰 변화로 재연결 원치 않으면 deps에서 제외
}

