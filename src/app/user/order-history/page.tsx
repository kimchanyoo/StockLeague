"use client";

import { useState } from "react";
import TabMenu from "@/app/components/TabMenu";
import OrderHistoryItem from "@/app/components/OrderHistoryItem";
import "./order-history.css";

// 더미 데이터 생성
const dummyStockData = Array.from({ length: 100 }, (_, i) => ({
  code: `STK${i + 1}`,
  name: `종목 ${i + 1}`,
  orderAmount: 100000 + i * 1000,
  orderPrice: 10000 + i * 10,
  orderType: i % 2 === 0 ? "매수" : "매도",
  orderStatus: i % 3 === 0 ? "체결" : "미체결",
  orderQuantity: 10 + i,
  executedQuantity: i % 3 === 0 ? 10 + i : 0,
  unexecutedQuantity: i % 3 === 0 ? 0 : 10 + i,
  averageExecutedPrice: i % 3 === 0 ? 10000 + i * 10 : 0,
  createdAt: `2024-04-${(i % 30 + 1).toString().padStart(2, "0")} 14:00`,
}));

export default function OrderHistory() {
  const [activeTab, setActiveTab] = useState("전체");
  const [currentPage, setCurrentPage] = useState(1);
  const tabList = ["전체", "체결", "미체결"];

  const itemsPerPage = 20;
  const maxPageButtons = 10;

  // 필터링된 데이터
  const filteredData = dummyStockData.filter((item) => {
    if (activeTab === "전체") return true;
    return item.orderStatus === activeTab;
  });

  const totalPages = Math.ceil(filteredData.length / itemsPerPage);

  // 현재 페이지 데이터
  const currentPageData = filteredData.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  // 페이지 번호 그룹 계산
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
          {currentPageData.map((stock) => (
            <OrderHistoryItem key={stock.code} {...stock} />
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
