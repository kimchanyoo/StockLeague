"use client";

import { useEffect, useState } from "react";
import "./account.css";
import Portfolio from "@/app/components/user/Portfolio";
import { getUserAssetValuation } from "@/lib/api/user"

interface FormattedStock {
  ticker: string;
  name: string;
  quantity: number;
  averagePurchasePrice: number;
  currentPrice: number;
  evaluationAmount: number;
  profit: number;
  returnRate: number;
}

export default function Account() {
  const [cash, setCash] = useState<number>(0);
  const [stocks, setStocks] = useState<FormattedStock[]>([]);
  const [investingMoney, setInvestingMoney] = useState<number>(0);
  const [totalAssets, setTotalAssets] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await getUserAssetValuation();

        setCash(Number(res.cashBalance));
        setInvestingMoney(Number(res.stockValuation));
        setTotalAssets(Number(res.totalAsset));

        const formattedStocks = res.stocks.map((stock) => ({
          ticker: stock.ticker,
          name: stock.stockName,
          quantity: Number(stock.quantity),
          averagePurchasePrice: Number(stock.avgBuyPrice),
          currentPrice: Number(stock.currentPrice),
          evaluationAmount: Number(stock.valuation),
          profit: Number(stock.profit),
          returnRate: Number(stock.profitRate),
        }));

        setStocks(formattedStocks);
      } catch (err: any) {
        setError(err.message || "데이터를 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

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
              <Portfolio /> 
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