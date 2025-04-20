"use client";

import { useMemo } from "react";
import "./account.css";

const dummyStocks = [
  {
    ticker: "005930",
    name: "삼성전자",
    quantity: 10,
    averagePurchasePrice: 60000,
    currentPrice: 65000,
    evaluationAmount: 650000,
    returnRate: 8.3,
  },
  {
    ticker: "035420",
    name: "NAVER",
    quantity: 5,
    averagePurchasePrice: 180000,
    currentPrice: 175000,
    evaluationAmount: 875000,
    returnRate: -2.8,
  },
];

export default function Account() {

  // 예시 데이터 (이후 API로 대체 가능)
  const orderableMoney = 5000000;  // 주문 가능 금액
  const investingMoney = 7000000;  // 투자 중인 금액
  // 총 보유자산 계산 (memoization으로 최적화)
  const totalAssets = useMemo(() => {
    return orderableMoney + investingMoney;
  }, [orderableMoney, investingMoney]);

  return (
    <div className="container">
      <h1 className="title">
        내 계좌
      </h1>
      <div className="myContents">

        <div className="leftSection">
          <div className="myMoney">
            <h2>나의 자산</h2>
            <div className="AM">
              <h3>총 보유자산 💰</h3>
              <h4><span>{totalAssets.toLocaleString()}</span> 원</h4>
            </div>
            <div className="OM">
              <h3>주문 가능 금액 💵</h3>
              <h4><span>{orderableMoney.toLocaleString()}</span> 원</h4>
            </div>
            <div className="IM">
              <h3>투자 중인 금액 💸</h3>
              <h4><span>{investingMoney.toLocaleString()}</span> 원</h4>
            </div>
          </div>

          <div className="portfolio">
            <h2>보유자산 포트폴리오</h2>
            <div className="graph">

            </div>
          </div>
        </div>


        <div className="rightSection">
          <h2>투자 상태</h2>
          <div className="investStatus">
            {dummyStocks.length === 0 ? (
              <div className="noStocksMessage">투자 종목이 없습니다.</div>
            ) : (
              <div className="stockCardList">
                {dummyStocks.map((stock) => (
                  <div key={stock.ticker} className="stockCard">
                    <div className="stockTitle">{stock.name}</div>
                    <div className="stockInfo"><strong>보유 수량:</strong> {stock.quantity}주</div>
                    <div className="stockInfo"><strong>평균 매입가:</strong> {stock.averagePurchasePrice.toLocaleString()}원</div>
                    <div className="stockInfo"><strong>현재가:</strong> {stock.currentPrice.toLocaleString()}원</div>
                    <div className="stockInfo"><strong>평가 금액:</strong> {stock.evaluationAmount.toLocaleString()}원</div>
                    <div className="stockInfo">
                      <strong>수익률:</strong>{" "}
                      <span style={{ color: stock.returnRate >= 0 ? "red" : "blue" }}>
                        {stock.returnRate > 0 ? "+" : ""}
                        {stock.returnRate}%
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}