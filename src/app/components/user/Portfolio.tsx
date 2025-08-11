"use client";

import { useEffect, useMemo, useState } from "react";
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { getUserAssetValuation, UserAssetValuation, StockValuationItem } from "@/lib/api/user";

const COLORS = ["#3F51B5", "#4CAF50", "#FF9800", "#ff5733", "#9E9E9E"];

const Portfolio: React.FC = () => {
  const [cash, setCash] = useState(0);
  const [stocks, setStocks] = useState<StockValuationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res: UserAssetValuation = await getUserAssetValuation();

        setCash(Number(res.cashBalance));
        setStocks(res.stocks);
      } catch (err: unknown) {
        if (err instanceof Error) setError(err.message);
        else setError("포트폴리오 데이터를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // 총 자산 (주식 평가금액 + 현금)
  const totalAssets = useMemo(() => {
    const stocksSum = stocks.reduce((acc, stock) => acc + Number(stock.valuation), 0);
    return stocksSum + cash;
  }, [stocks, cash]);

  const chartData = useMemo(() => {
    if (totalAssets === 0) return [];

    const stockData = stocks.map((stock) => ({
      name: stock.stockName,
      value: (Number(stock.valuation) / totalAssets) * 100,
    }));

    return [
      ...stockData,
      {
        name: "주문 가능 금액",
        value: (cash / totalAssets) * 100,
      },
    ];
  }, [stocks, cash, totalAssets]);

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
            outerRadius={100}
            innerRadius={40}
            label={({ value }) => `${value.toFixed(2)}%`}
          >
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(value) => `${(+value).toFixed(2)}%`} />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};

export default Portfolio;
