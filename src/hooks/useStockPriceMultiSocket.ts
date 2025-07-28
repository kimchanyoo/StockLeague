import { useEffect, useRef } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { StockPriceResponse } from "@/lib/api/stock";

export const useStockPriceMultiSocket = (
  tickers: string[],
  onUpdate: (data: StockPriceResponse) => void,
  accessToken: string
) => {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (tickers.length === 0) return;

    const client = new Client({
      webSocketFactory: () =>
        new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 10000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        tickers.forEach((ticker) => {
          client.subscribe(`/topic/stocks/${ticker}`, (message: IMessage) => {
            const data: StockPriceResponse = JSON.parse(message.body);
            onUpdate(data);
          });
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [tickers.join(","), accessToken]);
};
