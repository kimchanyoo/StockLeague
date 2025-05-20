"use client";

import React, { useEffect, useRef, useState } from "react";
import { createChart, UTCTimestamp, LineData } from "lightweight-charts";

const MainStockChart: React.FC = () => {
  const chartContainerRef = useRef<HTMLDivElement | null>(null);
  const [stockInfo, setStockInfo] = useState({ name: "종목명", currentPrice: 10000 });

  useEffect(() => {
    if (chartContainerRef.current) {
      const chart = createChart(chartContainerRef.current, {
        width: chartContainerRef.current.clientWidth,
        height: chartContainerRef.current.clientHeight,
        crosshair: { mode: 0 },
        leftPriceScale: { visible: true },
        rightPriceScale: { visible: false },
        layout: {
          background: { color : 'transparent' },
        },
      });

      const lineSeries = chart.addLineSeries({ color: "#4A90E2", lineWidth: 2, });

      // 주식 데이터 (예시)
      const stockData: LineData[] = [
        { time: 1640995200 as UTCTimestamp, value: 10000 },
        { time: 1641081600 as UTCTimestamp, value: 10200 },
        { time: 1641168000 as UTCTimestamp, value: 10150 },
        { time: 1641254400 as UTCTimestamp, value: 10300 },
        { time: 1641340800 as UTCTimestamp, value: 10450 },
        { time: 1641427200 as UTCTimestamp, value: 10500 },
      ];
      // 데이터를 차트에 추가
      lineSeries.setData(stockData);

      return () => {
        chart.remove();
      };
    }
  }, []);

  return (
    <div 
    style={{
      width: "100%", 
      height: "100%", 
      display: "flex", 
      flexDirection: "column",
      alignItems: "center", 
      justifyContent: "center",
      padding: "1rem"
    }}>
      <h1>Korea Value-up</h1>
      <div
        ref={chartContainerRef}
        style={{ 
          width: "100%", 
          height: "100%", 
        }}
      />
    </div>
    
  );
};

export default MainStockChart;