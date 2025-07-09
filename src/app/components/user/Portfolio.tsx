"use client";

import { useEffect, useMemo, useState } from "react";
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { getCashBalance } from "@/lib/api/user";
import { getPortfolio } from "@/lib/api/user";

// 각 종목에 대한 색상 지정
const COLORS = ["#FF5733", "#4CAF50", "#FF9800", "#3F51B5", "#9E9E9E"];
// 주문 가능 금액

const Portfolio: React.FC = () => {
  const [cash, setCash] = useState<number>(0);
  const [stocks, setStocks] = useState<any[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // 주문 가능 금액 불러오기
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [cashRes, portfolioRes] = await Promise.all([
          getCashBalance(),
          getPortfolio(),
        ]);

        setCash(cashRes);
        setStocks(portfolioRes.stocks);
      } catch (err: any) {
        setError(err.message || "포트폴리오 데이터를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // 총 자산 계산 (모든 종목의 평가 금액 합산 + 주문 가능 금액)
  const totalAssets = useMemo(() => {
    const stocksSum = stocks.reduce((acc, stock) => acc + stock.evaluationAmount, 0);
    return stocksSum + cash;
  }, [cash]);

   const chartData = useMemo(() => {
    if (totalAssets === 0) return [];

    const stockData = stocks.map((stock) => ({
      name: stock.name,
      value: (stock.evaluationAmount / totalAssets) * 100,
    }));

    return [
      ...stockData,
      {
        name: "주문 가능 금액",
        value: (cash / totalAssets) * 100,
      },
    ];
  }, [cash, totalAssets]);

  if (loading) return <p>포트폴리오를 불러오는 중입니다...</p>;
  if (error) return <p>❌ {error}</p>;

  return (
    <div style={{ width: "100%", height: "100%" }}>
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={chartData}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="50%"
            outerRadius={120}
            innerRadius={60}
            label={({ value }) => `${value.toFixed(2)}%`} // 퍼센트 표시
          >
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(value) => `${(+value).toFixed(2)}%`}/>
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};

export default Portfolio;
