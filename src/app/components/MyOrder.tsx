"use client";

import React, { useState } from "react";
import styles from "@/app/styles/components/MyOrder.module.css";

interface MyOrderProps {
  activeTab: string;
}

export default function MyOrder({ activeTab }: MyOrderProps) {

  const filledOrders = [
    {
      id: 1,
      stock: "삼성전자",
      side: "매수",
      quantity: 10,
      price: 72000,
      datetime: "2025-04-15 10:23:45",
    },
    {
      id: 2,
      stock: "카카오",
      side: "매도",
      quantity: 5,
      price: 59000,
      datetime: "2025-04-14 15:01:12",
    },
  ];

  const [pendingOrders, setPendingOrders] = useState([
    {
      id: 1,
      stock: "NAVER",
      side: "매도",
      quantity: 8,
      price: 210000,
      datetime: "2025-04-14 16:02:10",
    },
    {
      id: 2,
      stock: "현대차",
      side: "매수",
      quantity: 3,
      price: 195000,
      datetime: "2025-04-15 09:12:03",
    },
    {
      id: 3,
      stock: "현대차",
      side: "매수",
      quantity: 3,
      price: 195000,
      datetime: "2025-04-15 09:12:03",
    },{
      id: 24,
      stock: "현대차",
      side: "매수",
      quantity: 3,
      price: 195000,
      datetime: "2025-04-15 09:12:03",
    },{
      id: 23,
      stock: "현대차",
      side: "매수",
      quantity: 3,
      price: 195000,
      datetime: "2025-04-15 09:12:03",
    },{
      id: 22,
      stock: "현대차",
      side: "매수",
      quantity: 3,
      price: 195000,
      datetime: "2025-04-15 09:12:03",
    },{
      id: 27,
      stock: "현대차",
      side: "매수",
      quantity: 3,
      price: 195000,
      datetime: "2025-04-15 09:12:03",
    },
  ]);

  const handleCancel = (id: number) => {
    setPendingOrders(prev => prev.filter(order => order.id !== id));
  };

  return (
      <div className={styles.orderStatus}>
        {activeTab === "체결 내역" && (
          filledOrders.length > 0 ? (
            <ul className={styles.orderList}>
              {filledOrders.map((order) => (
                <li key={order.id} className={styles.orderItem}>
                  <div>
                    <strong>{order.stock} </strong>
                    |
                    <strong className={order.side === "매수" ? styles.buy : styles.sell}> {order.side}</strong> 
                  </div>
                  <div>
                    수량: {order.quantity}주 / 가격: {order.price.toLocaleString()}원
                  </div>
                  <div className={styles.orderDate}>{order.datetime}</div>
                </li>
              ))}
            </ul>
          ) : (
            <p>거래 내역이 없습니다.</p>
          )
        )}

        {activeTab === "미체결 내역" && (
          pendingOrders.length > 0 ? (
            <ul className={styles.orderList}>
              {pendingOrders.map((order) => (
                <li key={order.id} className={styles.orderItem}>
                  <div>
                    <strong>{order.stock} </strong>
                    |
                    <strong className={order.side === "매수" ? styles.buy : styles.sell}> {order.side}</strong> 
                  </div>
                  <div>
                    수량: {order.quantity}주 / 주문가격: {order.price.toLocaleString()}원
                  </div>
                  <div className={styles.orderDate}>{order.datetime}</div>
                  <button
                    className={styles.cancelBtn}
                    onClick={() => handleCancel(order.id)}
                  >
                    취소하기
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p>거래 내역이 없습니다.</p>
          )
        )}
      </div>
  );
}
