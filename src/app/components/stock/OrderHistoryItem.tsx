"use client";

import React from "react";
import styles from "@/app/styles/components/stock/OrderHistoryItem.module.css";

type OrderHistoryItemProps = {
  code: string;
  name: string;
  orderAmount: number;
  orderPrice: number;
  orderType: "매수" | "매도";
  orderStatus: string;
  orderQuantity: number;
  executedQuantity: number;
  unexecutedQuantity: number;
  averageExecutedPrice: number;
  createdAt: string;
};

const OrderHistoryItem: React.FC<OrderHistoryItemProps> = ({
  code,
  name,
  orderAmount,
  orderPrice,
  orderType,
  orderStatus,
  orderQuantity,
  executedQuantity,
  unexecutedQuantity,
  averageExecutedPrice,
  createdAt,
}) => {
  const typeClass =
    orderType === "매수" ? styles.orderType_buy : styles.orderType_sell;

  return (
    <div className={styles.orderHistoryItem} data-code={code}>
      <div>{name}</div>
      <div>{orderAmount.toLocaleString()}</div>
      <div>{orderPrice.toLocaleString()}</div>
      <div className={typeClass}>{orderType}</div>
      <div>{orderStatus}</div>
      <div>{orderQuantity}</div>
      <div>{executedQuantity}</div>
      <div>{unexecutedQuantity}</div>
      <div>
        {averageExecutedPrice === 0
          ? "-"
          : averageExecutedPrice.toLocaleString()}
      </div>
      <div className={styles.createdAt}>{createdAt}</div>
    </div>
  );
};

export default OrderHistoryItem;