// hooks/useOrderbook.ts
"use client";

import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import { getOrderbook, OrderbookData } from "@/lib/api/stock";

export const useOrderbook = ({
  ticker,
  accessToken,
  loading
}: {
  ticker: string;
  accessToken: string | null;
  loading: boolean;
}) => {
  const [orderbook, setOrderbook] = useState<OrderbookData | null>(null);
  const [isMarketOpen, setIsMarketOpen] = useState(false);

  useEffect(() => {
    if (!ticker || ticker.trim() === "") return;
    if (loading) return;

    const fetchInitialOrderbook = async () => {
      try {
        //console.log("📡 API로 최신 호가 조회 시작:", ticker);
        const data = await getOrderbook(ticker);
        //console.log("✅ API 응답:", data);
        setOrderbook(data);
        setIsMarketOpen(data.isMarketOpen);
      } catch (err) {
        //console.error("❌ 호가 조회 실패:", err);
      }
    };

    fetchInitialOrderbook();
  }, [ticker, loading]);

  useEffect(() => {
    if (!ticker) {
      //console.warn("⏩ ticker 없음 - WebSocket 연결 건너뜀");
      return;
    }
    if (loading) {
      //console.warn("⏩ 로딩 중 - WebSocket 연결 대기");
      return;
    }
    if (!accessToken) {
      //console.warn("⚠️ accessToken 없음 - WebSocket 연결 건너뜀(호가)");
      return;
    }
    if (!isMarketOpen) {
      //console.warn("🛑 장 마감 상태 - WebSocket 연결 안 함");
      return;
    }

    console.log("📡 WebSocket Client 생성 시작");

    const client = new Client({
      webSocketFactory: () => {
        console.log("🌐 WebSocketFactory 호출 - URL:", process.env.NEXT_PUBLIC_SOCKET_URL);
        return new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!);
      },
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`, 
      },
      reconnectDelay: 15_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,

      onConnect: () => {
        //console.log("✅ WebSocket 연결 성공 - ticker:", ticker);

        const destination = `/topic/orderbook/${ticker}`;
        console.log("📌 구독 요청 - destination:", destination);

        client.subscribe(destination, (message) => {
          //console.log("📥 수신된 메시지:", message.body);
          try {
            const data = JSON.parse(message.body) as OrderbookData;
            setOrderbook(data);
          } catch (err) {
            //console.error("❌ 호가 데이터 처리 오류:", err);
          }
        });
      },

      onStompError: (frame) => {
        //console.error("❌ WebSocket STOMP 오류:", frame.headers["message"], frame.body);
      },
      onWebSocketClose: (event) => {
        console.warn("🔌 WebSocket 연결 종료됨",event);
        console.log("close code:", event.code, "reason:", event.reason);
      },
      onWebSocketError: (event) => {
        //console.error("🚨 WebSocket 오류 발생:", event);
      }
    });

    console.log("🚀 WebSocket Client 활성화");
    client.activate();

    return () => {
      console.log("🛑 WebSocket Client 비활성화");
      client.deactivate();
    };
  }, [ticker, loading, accessToken, isMarketOpen]);

  return { orderbook, isMarketOpen };
};
