"use client";

import { useEffect, useState } from "react";
import TabMenu from "@/app/components/utills/TabMenu";
import OrderHistoryItem from "@/app/components/stock/OrderHistoryItem";
import { getMyOrder } from "@/lib/api/user";
import "./order-history.css";

export default function OrderHistory() {
  const [activeTab, setActiveTab] = useState("전체");
  const [currentPage, setCurrentPage] = useState(1);
  const [orderData, setOrderData] = useState<any[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const itemsPerPage = 20;
  const maxPageButtons = 10;

  const tabList = ["전체", "체결", "미체결"];

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await getMyOrder(currentPage, itemsPerPage);
        setOrderData(res.orders || []);
        setTotalPages(res.totalPages || 1);
      } catch (err) {
        console.error("주문 내역 조회 실패", err);
      }
    };
    fetchData();
  }, [currentPage]);

  const filteredData = orderData.filter((item) => {
    if (activeTab === "전체") return true;
    if (activeTab === "체결")
      return item.orderStatus === "EXECUTED" || item.orderStatus === "CANCELED_AFTER_PARTIAL";
    if (activeTab === "미체결")
      return item.orderStatus === "WAITING" || item.orderStatus === "PARTIALLY_EXECUTED";
    return true;
  });

  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);

  return (
    <div className="orderHistoryContainer">
      <h1 className="title">주문내역</h1>
      <div className="orderHistoryBox">
        <TabMenu
          tabs={tabList}
          activeTab={activeTab}
          onTabChange={(tab) => {
            setActiveTab(tab);
            setCurrentPage(1); // 탭 변경 시 1페이지로 초기화
          }}
          tabTextSize="2rem"
        />

        <div className="orderHistoryCategory">
          <h1>종목명</h1>
          <h1>주문금액</h1>
          <h1>주문가격</h1>
          <h1>주문타입</h1>
          <h1>주문상태</h1>
          <h1>주문수량</h1>
          <h1>체결수량</h1>
          <h1>미체결수량</h1>
          <h1>평균체결단가</h1>
          <h1>주문생성일</h1>
        </div>

        <div className="orderHistoryList">
          {filteredData.map((stock) => (
            <OrderHistoryItem key={stock.orderId} {...stock} />
          ))}
        </div>
      </div>

      <div className="pagination">
        <button onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))} disabled={currentPage === 1}>
          이전
        </button>

        {pageNumbers.map((num) => (
          <button
            key={num}
            className={num === currentPage ? "active" : ""}
            onClick={() => setCurrentPage(num)}
          >
            {num}
          </button>
        ))}

        <button onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))} disabled={currentPage === totalPages}>
          다음
        </button>
      </div>
    </div>
  );
}

