"use client";

import React, { useEffect, useState, useRef, useCallback } from "react";
import styles from "@/app/styles/components/user/MyOrder.module.css";
import { getAllMyExecutions, getUnexecutedOrders, cancelOrder } from "@/lib/api/user";

interface MyOrderProps {
  activeTab: string;
}

const MyOrder = ({ activeTab }: MyOrderProps) => {
  const [orders, setOrders] = useState<any[]>([]);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const observerRef = useRef<HTMLDivElement | null>(null);

  const fetchOrders = useCallback(async () => {
    try {
      if (activeTab === "체결 내역") {
        const res = await getAllMyExecutions(page, 20);
        const newOrders = res?.executions;

        if (!Array.isArray(newOrders)) {
          console.error("❌ 체결 내역 응답이 배열이 아닙니다:", res);
          return;
        }

        setOrders((prev) => [...prev, ...newOrders]);
        setHasMore(page < res.totalPage);
      } else {
        const res = await getUnexecutedOrders(page, 20);
        const newOrders = res?.unexecutedOrders;

        if (!Array.isArray(newOrders)) {
          console.error("❌ 미체결 내역 응답이 배열이 아닙니다:", res);
          return;
        }

        setOrders((prev) => [...prev, ...newOrders]);
        setHasMore(page < res.totalPage);
      }
    } catch (err) {
      console.error("❌ 주문 내역 불러오기 실패:", err);
    }
  }, [page, activeTab]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  // ✅ IntersectionObserver로 스크롤 하단 감지
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

  return (
    <div className={styles.orderStatus}>
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
