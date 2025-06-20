"use client";

import React, { useMemo } from "react";
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from "recharts";

// 예시 데이터
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

// 주문 가능 금액
const orderableMoney = 5000000;

const Portfolio: React.FC = () => {
  // 총 자산 계산 (모든 종목의 평가 금액 합산 + 주문 가능 금액)
  const totalAssets = useMemo(() => {
    return (
      orderableMoney +
      dummyStocks.reduce((acc, stock) => acc + stock.evaluationAmount, 0)
    );
  }, [orderableMoney, dummyStocks]);

  // PieChart에 사용할 데이터 (각 종목의 평가 금액 비율 + 주문 가능 금액 비율)
  const data = [
    ...dummyStocks.map((stock) => ({
      name: stock.name,
      value: (stock.evaluationAmount / totalAssets) * 100,  // 각 종목의 자산 비율 (%)
    })),
    {
      name: "주문 가능 금액",
      value: (orderableMoney / totalAssets) * 100,  // 주문 가능 금액 비율 (%)
    },
  ];

  // 각 종목에 대한 색상 지정 (다양한 색상 사용)
  const COLORS = ["#FF5733", "#4CAF50", "#FF9800", "#3F51B5", "#9E9E9E"]; // 주문 가능 금액에 대한 색상도 추가

  return (
    <div style={{ width: "100%", height: "100%" }}>
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="50%"
            outerRadius={120}
            innerRadius={60}
            label={({ value }) => `${value.toFixed(2)}%`} // 퍼센트 표시
          >
            {data.map((entry, index) => (
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
