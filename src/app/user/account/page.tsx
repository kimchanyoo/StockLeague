"use client";

import { useEffect, useMemo, useState } from "react";
import "./account.css";
import Portfolio from "@/app/components/user/Portfolio";
import { getCashBalance, getPortfolio } from "@/lib/api/user"

export default function Account() {
  const [cash, setCash] = useState<number>(0);
  const [stocks, setStocks] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // API 데이터 불러오기
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [cashResult, portfolioResult] = await Promise.all([
          getCashBalance(),
          getPortfolio(),
        ]);

        setCash(cashResult);
        setStocks(portfolioResult.stocks);
      } catch (err: any) {
        setError(err.message || "데이터를 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // 투자 중인 금액: 모든 종목의 평가금액 합
  const investingMoney = useMemo(() => {
    return stocks.reduce((acc, stock) => acc + stock.evaluationAmount, 0);
  }, [stocks]);

  // 총 보유 자산 = 투자 중 + 주문 가능
  const totalAssets = useMemo(() => {
    return cash + investingMoney;
  }, [cash, investingMoney]);

  if (loading) return <p>불러오는 중입니다...</p>;
  if (error) return <p>❌ {error}</p>;

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
              <h4><span>{cash.toLocaleString()}</span> 원</h4>
            </div>
            <div className="IM">
              <h3>투자 중인 금액 💸</h3>
              <h4><span>{investingMoney.toLocaleString()}</span> 원</h4>
            </div>
          </div>

          <div className="portfolio">
            <h2>보유자산 포트폴리오</h2>
            <div className="graph">
              <Portfolio/>
            </div>
          </div>
        </div>


        <div className="rightSection">
          <h2>투자 상태</h2>
          <div className="investStatus">
            {stocks.length === 0 ? (
              <div className="noStocksMessage">투자 종목이 없습니다.</div>
            ) : (
              <div className="stockCardList">
                {stocks.map((stock) => (
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