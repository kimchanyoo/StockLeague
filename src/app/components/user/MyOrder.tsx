"use client";

import React, { useEffect, useState, useRef, useCallback } from "react";
import styles from "@/app/styles/components/user/MyOrder.module.css";
import { getAllMyExecutions, getUnexecutedOrders, cancelOrder } from "@/lib/api/user";
import { getUserAssetValuation } from "@/lib/api/user";
import { Client, IMessage } from "@stomp/stompjs";

interface MyOrderProps {
  activeTab: string;
  accessToken: string;
}

interface AssetData {
  totalAsset: string; // API 응답에선 string 타입으로 옵니다
  cashBalance: string;
  marketOpen: boolean;
}

const MyOrder = ({ activeTab, accessToken }: MyOrderProps) => {
  const [orders, setOrders] = useState<any[]>([]);
  const [asset, setAsset] = useState<AssetData | null>(null);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const observerRef = useRef<HTMLDivElement | null>(null);
  const clientRef = useRef<Client | null>(null);
  const isMarketOpenRef = useRef(false); // 유지용

  const fetchOrders = useCallback(async () => {
    if (!accessToken) return;

    try {
      if (activeTab === "체결 내역") {
        const res = await getAllMyExecutions(page, 20);
        const newOrders = res?.contents;
        if (!Array.isArray(newOrders)) return;
        setOrders((prev) => (page === 1 ? newOrders : [...prev, ...newOrders]));
        setHasMore(page < res.totalPage);
      } else {
        const res = await getUnexecutedOrders(page, 20);
        const newOrders = res?.contents;
        if (!Array.isArray(newOrders)) return;
        setOrders((prev) => (page === 1 ? newOrders : [...prev, ...newOrders]));
        setHasMore(page < res.totalPage);
      }
    } catch (err) {
      console.error("❌ 주문 내역 불러오기 실패:", err);
    }
  }, [page, activeTab, accessToken]);

  const fetchAsset = useCallback(async () => {
    if (!accessToken) {
      // 로그인 안 된 상태라면 API 호출 안 함
      return;
    }

    try {
      const res = await getUserAssetValuation();
      setAsset(res); // 전체 객체 저장
      isMarketOpenRef.current = res.marketOpen;

      if (res.marketOpen && accessToken) {
        if (clientRef.current) {
          clientRef.current.deactivate();
          clientRef.current = null;
        }

        const client = new Client({
          webSocketFactory: () => new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
          connectHeaders: { Authorization: `Bearer ${accessToken}` },
          reconnectDelay: 15_000,
          heartbeatIncoming: 10_000,
          heartbeatOutgoing: 10_000,
          onConnect: () => {
            client.subscribe("/user/queue/asset", (message: IMessage) => {
              try {
                const data = JSON.parse(message.body) as AssetData;
                if (data) {
                  setAsset(data);
                }
              } catch (err) {
                console.error("❌ WebSocket 데이터 파싱 오류:", err);
              }
            });
          },
          onStompError: (frame) => {
            console.error("❌ STOMP 오류:", frame);
          },
        });

        client.activate();
        clientRef.current = client;
      }
    } catch (err) {
      console.error("❌ 자산 정보 조회 실패:", err);
    }
  }, [accessToken]);

  useEffect(() => {
    fetchAsset();
  }, [fetchAsset]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  useEffect(() => {
    if (!hasMore) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          setPage((prev) => prev + 1);
        }
      },
      { threshold: 1 }
    );

    if (observerRef.current) {
      observer.observe(observerRef.current);
    }

    return () => {
      if (observerRef.current) observer.unobserve(observerRef.current);
    };
  }, [hasMore]);

  const handleCancel = async (orderId: number) => {
    try {
      await cancelOrder(orderId);
      setOrders((prev) => prev.filter((o) => o.orderId !== orderId));
      alert("주문이 취소되었습니다.");
    } catch (err) {
      alert("주문 취소 실패");
    }
  };

  const filteredOrders = (activeTab === "체결 내역")
  ? orders
  : activeTab === "미체결 내역"
  ? orders.filter(o => 
      o.orderStatus === "WAITING" || 
      o.orderStatus === "PARTIALLY_EXECUTED" || 
      o.orderStatus === "EXECUTED" ||
      o.orderStatus === "CANCELED_AFTER_PARTIAL"
    )
  : [];
      
  useEffect(() => {
    return () => {
      // 언마운트 시 WebSocket 정리
      clientRef.current?.deactivate();
    };
  }, []);
  

  return (
    <div className={styles.orderStatus}>
      {filteredOrders.length > 0 ? (
        <ul className={styles.orderList}>
          {filteredOrders.map((order) => (
            <li
              key={
                activeTab === "체결 내역"
                  ? order.orderExecutionId
                  : order.orderId
              }
              className={styles.orderItem}
            >
              <div>
                <strong>{order.stockName} </strong>|
                <strong className={order.orderType === "BUY" ? styles.buy : styles.sell}>
                  {order.orderType === "BUY" ? " 매수" : " 매도"}
                </strong>
              </div>
              <div>
                수량:{" "}
                {activeTab === "체결 내역"
                  ? order.executedAmount
                  : order.orderAmount}
                주 / 주문가격:{" "}
                {activeTab === "체결 내역"
                  ? order.executedPrice.toLocaleString()
                  : order.orderPrice.toLocaleString()}
                원
              </div>
              <div className={styles.orderDate}>
                {activeTab === "체결 내역"
                  ? order.executedAt.replace("T", " ").slice(0, 19)
                  : order.createdAt.replace("T", " ").slice(0, 19)}
              </div>
              {activeTab === "미체결 내역" && (
                <button
                  className={styles.cancelBtn}
                  onClick={() => handleCancel(order.orderId)}
                >
                  취소하기
                </button>
              )}
            </li>
          ))}
        </ul>
      ) : (
        <p>거래 내역이 없습니다.</p>
      )}

      {hasMore && <div ref={observerRef} style={{ height: 1 }} />}
    </div>
  );
};

export default MyOrder;
