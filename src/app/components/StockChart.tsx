"use client";

import React from "react";
import {
  ComposedChart,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Bar,
  Line,
  Cell,
  ResponsiveContainer,
  Customized,
  Brush
} from "recharts";
import styles from "@/app/styles/components/StockChart.module.css";

// 예시 주식 데이터
const stockData = [
  { time: '09:00', open: 120, high: 122, low: 119, close: 121.5 },
  { time: '09:03', open: 121.5, high: 122, low: 120.5, close: 120.7 },
  { time: '09:06', open: 120.7, high: 121.5, low: 120, close: 121.2 },
  { time: '09:09', open: 121.2, high: 122, low: 120.8, close: 120.5 },
  { time: '09:12', open: 120.5, high: 121.7, low: 120, close: 121.8 },
  { time: '09:15', open: 121.8, high: 122.2, low: 121, close: 120.9 },
  { time: '09:18', open: 120.9, high: 122, low: 120.5, close: 121.6 },
  { time: '09:21', open: 121.6, high: 121.9, low: 120.9, close: 120.7 },
  { time: '09:24', open: 120.7, high: 122, low: 120.4, close: 121.3 },
  { time: '09:27', open: 121.3, high: 122.1, low: 120.8, close: 120.5 },
  { time: '09:30', open: 120.5, high: 121.8, low: 120.3, close: 121.4 },
  { time: '09:33', open: 121.4, high: 122, low: 120.7, close: 120.6 },
  { time: '09:36', open: 120.6, high: 121.5, low: 120.4, close: 121.7 },
  { time: '09:39', open: 121.7, high: 122.1, low: 121.1, close: 121 },
  { time: '09:42', open: 121, high: 122.3, low: 120.8, close: 122 },
  { time: '09:45', open: 122, high: 122.5, low: 121.3, close: 121.5 },
  { time: '09:48', open: 121.5, high: 122.6, low: 121, close: 122.4 },
  { time: '09:51', open: 122.4, high: 123, low: 121.8, close: 121.9 },
  { time: '09:54', open: 121.9, high: 123.2, low: 121.5, close: 122.6 },
  { time: '09:57', open: 122.6, high: 123, low: 121.9, close: 122 },
];

// 이동평균 계산 함수
function calculateMovingAverage(data: any[], windowSize: number) {
  return data.map((_, index) => {
    if (index < windowSize - 1) return null;
    const windowSlice = data.slice(index - windowSize + 1, index + 1);
    const avg = windowSlice.reduce((sum, item) => sum + item.close, 0) / windowSize;
    return Number(avg.toFixed(2));
  });
}

// MA 데이터 추가
const shortMA = calculateMovingAverage(stockData, 5);
const longMA = calculateMovingAverage(stockData, 10);
const stockDataWithMA = stockData.map((item, idx) => ({
  ...item,
  shortMA: shortMA[idx],
  longMA: longMA[idx],
}));

// 캔들 차트
const CandleShape = (props: any) => {
  const { xAxisMap, yAxisMap, data } = props;
  const xAxis = xAxisMap["0"];
  const yAxis = yAxisMap["right"];
  const xBand = xAxis.scale.bandwidth();

  return data.map((entry: any, index: number) => {
    const x = xAxis.scale(entry.time) ?? 0;
    const centerX = x + xBand / 2;
    const openY = yAxis.scale(entry.open);
    const closeY = yAxis.scale(entry.close);
    const highY = yAxis.scale(entry.high);
    const lowY = yAxis.scale(entry.low);

    const isRise = entry.close >= entry.open;
    const color = isRise ? "#f44336" : "#2196f3";

    return (
      <g key={`candle-${index}`}>
        <line x1={centerX} x2={centerX} y1={highY} y2={lowY} stroke={color} strokeWidth={1} />
        <rect
          x={x + xBand * 0.25}
          width={xBand * 0.5}
          y={Math.min(openY, closeY)}
          height={Math.max(Math.abs(closeY - openY), 1)}
          fill={color}
        />
      </g>
    );
  });
};

const StockChart = ({
  activeTab,
  setActiveTab,
}: {
  activeTab: 'chart' | 'community';
  setActiveTab: (tab: 'chart' | 'community') => void;
}) => {
  return (
    <div className={styles.container}>
      <div className={styles.centerSection}>
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={stockDataWithMA}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="time" />
            <YAxis yAxisId="right" orientation="right" domain={["auto", "auto"]} />
            <Tooltip />

            {/* 캔들 */}
            <Customized component={<CandleShape />} />

            {/* 이동평균선 */}
            <Line
              type="monotone"
              dataKey="shortMA"
              stroke="#ff9800"
              dot={false}
              strokeWidth={2}
              yAxisId="right"
              name="5일선"
            />
            <Line
              type="monotone"
              dataKey="longMA"
              stroke="#4caf50"
              dot={false}
              strokeWidth={2}
              yAxisId="right"
              name="10일선"
            />
            <Brush dataKey="time" height={20} stroke="#8884d8" />
          </ComposedChart>
        </ResponsiveContainer>
      </div>

      <div className={styles.bottomSection}>
        <ResponsiveContainer width="100%" height={100}>
          <ComposedChart data={stockDataWithMA}>
            <XAxis dataKey="time"  />
            <YAxis hide />
            <Tooltip />

            <Bar
              dataKey={(data) => data.high - data.low}
              barSize={10}
              fill="#999"
              opacity={0.4}
            >
              {stockData.map((entry, index) => (
                <Cell
                  key={`volume-cell-${index}`}
                  fill={entry.close >= entry.open ? "#f44336" : "#2196f3"}
                />
              ))}
            </Bar>
          </ComposedChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default StockChart;