"use client";

import { useState, useEffect } from "react";
import { getDetailMyOrder, OrderItem, OrderExecution, GetDetailMyOrderResponse } from "@/lib/api/user";
import styles from "@/app/styles/components/user/OrderHistoryItem.module.css";

type OrderHistoryItemProps = OrderItem;

const translateOrderType = (type: string): string => {
  return type === "BUY" ? "매수" : "매도";
};

const translateOrderStatus = (status: string): string => {
  switch (status) {
    case "WAITING":
      return "미체결";
    case "PARTIALLY_EXECUTED":
      return "부분체결";
    case "EXECUTED":
      return "체결";
    case "CANCELED":
      return "취소";
    case "CANCELED_AFTER_PARTIAL":
      return "부분체결 후 취소";
    case "EXPIRED":
      return "만료";
    case "FAILED":
      return "실패";
    default:
      return status;
  }
};

const OrderHistoryItem: React.FC<OrderHistoryItemProps> = ({
  orderId,
  stockTicker,
  stockName,
  orderType,
  orderStatus,
  orderPrice,
  orderAmount,
  executedAmount,
  remainingAmount,
  averageExecutedPrice,
  createdAt,
}) => {
  const [open, setOpen] = useState(false);
  const [executions, setExecutions] = useState<OrderExecution[]>([]);
  const [loading, setLoading] = useState(false);

  const typeClass = orderType === "BUY" ? styles.orderType_buy : styles.orderType_sell;

  useEffect(() => {
    if (!open) return;

    const fetchDetail = async () => {
      setLoading(true);
      console.log("getDetailMyOrder 호출됨");
      try {
        const res: GetDetailMyOrderResponse = await getDetailMyOrder(orderId);
        if (res.success) {
          //console.log("세부 주문 내역 응답", res.contents);
          setExecutions(res.contents);
        }
      } catch (error) {
        //console.error("세부 주문 내역 조회 실패", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDetail();
  }, [open]);

  return (
    <div className={styles.orderHistoryItemWrapper}>
      <div
        className={styles.orderHistoryItem}
        onClick={() => setOpen((prev) => !prev)}
        data-order-id={orderId}
      >
        <div>{stockName}</div>
        <div>{orderAmount.toLocaleString()}</div>
        <div>{orderPrice.toLocaleString()}</div>
        <div className={typeClass}>{translateOrderType(orderType)}</div>
        <div>{translateOrderStatus(orderStatus)}</div>
        <div>{(executedAmount + remainingAmount).toLocaleString()}</div>
        <div>{executedAmount.toLocaleString()}</div>
        <div>{remainingAmount.toLocaleString()}</div>
        <div>
          {averageExecutedPrice === 0 ? "-" : averageExecutedPrice.toLocaleString()}
        </div>
        <div className={styles.createdAt}>{new Date(createdAt).toLocaleString()}</div>
      </div>

      {open && (
        <div className={styles.orderDetailBox}>
          {loading ? (
            <div>로딩 중...</div>
          ) : executions.length === 0 ? (
            <div>체결 내역 없음</div>
          ) : (
            executions.map((exe) => (
              <div key={exe.orderExecutionId} className={styles.detailRow}>
                <div><strong>체결 단가:</strong> {exe.executedPrice.toLocaleString()}원</div>
                <div><strong>체결 수량:</strong> {exe.executedAmount}주</div>
                <div><strong>체결 시간:</strong> {new Date(exe.executedAt).toLocaleString()}</div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default OrderHistoryItem;
