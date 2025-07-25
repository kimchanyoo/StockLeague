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
  isMarketOpen: boolean;
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
    try {
      if (activeTab === "체결 내역") {
        const res = await getAllMyExecutions(page, 20);
        const newOrders = res?.executions;

        if (!Array.isArray(newOrders)) return;

        setOrders((prev) => [...prev, ...newOrders]);
        setHasMore(page < res.totalPage);
      } else {
        const res = await getUnexecutedOrders(page, 20);
        const newOrders = res?.unexecutedOrders;

        if (!Array.isArray(newOrders)) return;

        setOrders((prev) => [...prev, ...newOrders]);
        setHasMore(page < res.totalPage);
      }
    } catch (err) {
      console.error("❌ 주문 내역 불러오기 실패:", err);
    }
  }, [page, activeTab]);

  const fetchAsset = useCallback(async () => {
    try {
      const res = await getUserAssetValuation();
      setAsset(res); // 전체 객체 저장
      isMarketOpenRef.current = res.isMarketOpen;

      if (res.isMarketOpen && accessToken) {
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

  const filteredOrders = orders.filter((o) =>
    activeTab === "체결 내역"
      ? o.orderStatus === "EXECUTED" || o.orderStatus === "CANCELED_AFTER_PARTIAL"
      : o.orderStatus === "WAITING" || o.orderStatus === "PARTIALLY_EXECUTED"
  );

  useEffect(() => {
    return () => {
      // 언마운트 시 WebSocket 정리
      clientRef.current?.deactivate();
    };
  }, []);

  return (
    <div className={styles.orderStatus}>
      {/* 자산 정보 표시 */}
      <div className={styles.assetInfo}>
        <strong>총 자산: </strong>
        {asset ? `${asset.totalAsset.toLocaleString()}원` : "로딩 중..."} /{" "}
        <strong>현금: </strong>
        {asset ? `${asset.cashBalance.toLocaleString()}원` : "로딩 중..."}
      </div>

      {/* 주문 리스트 */}
      {filteredOrders.length > 0 ? (
        <ul className={styles.orderList}>
          {filteredOrders.map((order) => (
            <li key={order.orderId} className={styles.orderItem}>
              <div>
                <strong>{order.stockName} </strong>|
                <strong
                  className={
                    order.orderType === "BUY" ? styles.buy : styles.sell
                  }
                >
                  {" "}
                  {order.orderType === "BUY" ? "매수" : "매도"}
                </strong>
              </div>
              <div>
                수량: {order.orderAmount}주 / 주문가격:{" "}
                {order.orderPrice.toLocaleString()}원
              </div>
              <div className={styles.orderDate}>
                {order.createdAt.replace("T", " ").slice(0, 19)}
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

      {/* 스크롤 하단 감지용 div */}
      {hasMore && <div ref={observerRef} style={{ height: 1 }} />}
    </div>
  );
};

export default MyOrder;
