import { IMessage } from "@stomp/stompjs";
import { stompClient } from "@/lib/socket/socket"; // 연결된 STOMP 클라이언트
import { StockPriceResponse } from "@/lib/api/stock";

const stockPriceSubscriptions: Record<string, () => void> = {};

/**
 * 주식 종목별 실시간 시세 구독
 */
export const subscribeToStockPrice = (
  ticker: string,
  onMessage: (data: StockPriceResponse) => void
): (() => void) => {
  if (!stompClient || !stompClient.connected) {
    console.warn("STOMP 클라이언트가 아직 연결되지 않았습니다.");
    return () => {};
  }

  const destination = `/topic/stocks/${ticker}`;

  const subscription = stompClient.subscribe(destination, (message: IMessage) => {
    try {
      const parsed: StockPriceResponse = JSON.parse(message.body);
      onMessage(parsed);
    } catch (e) {
      console.error(`[실시간 시세 파싱 에러 - ${ticker}]:`, e);
    }
  });

  stockPriceSubscriptions[ticker] = () => subscription.unsubscribe();

  return () => {
    subscription.unsubscribe();
    delete stockPriceSubscriptions[ticker];
  };
};

/**
 * 특정 종목의 실시간 시세 구독 해제
 */
export const unsubscribeFromStockPrice = (ticker: string) => {
  const unsubscribe = stockPriceSubscriptions[ticker];
  if (unsubscribe) {
    unsubscribe();
    delete stockPriceSubscriptions[ticker];
    console.log(`🛑 실시간 시세 구독 해제됨: ${ticker}`);
  }
};
