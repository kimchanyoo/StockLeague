"use client";

import React, { useState, useEffect, useRef } from "react";
import { createChart, UTCTimestamp } from "lightweight-charts";
import styles from "@/app/styles/components/StockChart.module.css";
import TimeIntervalSelector from "./TimeIntervalSelector";
import MovingAverageSelector from "./MovingAverageSelector"

type Props = {
  activeTab: 'chart' | 'community';
  setActiveTab: (tab: 'chart' | 'community') => void;
};

// 예시 주식 데이터
const data: { time: UTCTimestamp; open: number; high: number; low: number; close: number; volume: number }[] = [
  { open: 10, high: 10.63, low: 9.49, close: 9.55, time: 1642427876, volume: 3000 },
  { open: 9.55, high: 10.30, low: 9.42, close: 9.94, time: 1642514276, volume: 2000 },
  { open: 9.94, high: 10.17, low: 9.92, close: 9.78, time: 1642600676, volume: 1500 },
  { open: 9.78, high: 10.59, low: 9.18, close: 9.51, time: 1642687076, volume: 2500 },
  { open: 9.51, high: 10.46, low: 9.10, close: 10.17, time: 1642773476, volume: 4000 },
  { open: 10.17, high: 10.96, low: 10.16, close: 10.47, time: 1642859876, volume: 3500 },
  { open: 10.47, high: 11.39, low: 10.40, close: 10.81, time: 1642946276, volume: 3000 },
  { open: 10.81, high: 11.60, low: 10.30, close: 10.75, time: 1643032676, volume: 2200 },
  { open: 10.75, high: 11.60, low: 10.49, close: 10.93, time: 1643119076, volume: 3300 },
  { open: 10.93, high: 11.53, low: 10.76, close: 10.96, time: 1643205476, volume: 3800 }
];

const StockChart: React.FC<Props> = ({ activeTab, setActiveTab }) => {
  const chartContainerRef = useRef<HTMLDivElement | null>(null);
  const volumeContainerRef = useRef<HTMLDivElement | null>(null);
  const [maVisibility, setMaVisibility] = useState({
    short: true,
    mid: false,
    long: false,
  });
  const toggleMA = (type: 'short' | 'mid' | 'long') => {
    setMaVisibility(prev => ({ ...prev, [type]: !prev[type] }));
  };

  useEffect(() => {
    if (!chartContainerRef.current) return;

    // 차트 옵션
    const chartOptions = {
      layout: { 
        textColor: 'black', 
        background: { type: 'solid', color: 'white' } 
      },
      
    };

    const chart = createChart(chartContainerRef.current, chartOptions);

    // 캔들스틱 시리즈 추가
    const candlestickSeries = chart.addCandlestickSeries({
      upColor: '#CB3030',
      downColor: '#2D7CD1',
      borderVisible: false,
      wickUpColor: '#CB3030',
      wickDownColor: '#2D7CD1',
    });

    // 거래량 차트 영역을 위한 또 다른 차트
    const volumeChart = createChart(volumeContainerRef.current, {
      layout: {
        textColor: 'black',
        background: { type: 'solid', color: 'white' },
      },
      crosshair: {
        vertLine: { color: '#000', width: 1 },
        horzLine: { color: '#000', width: 1 },
      },
      grid: {
        vertLines: { visible: false },
        horzLines: { visible: false },
      },
      priceScale: {
        visible: false, // 거래량 차트에는 가격 축을 숨깁니다
      },
    });

    // 거래량 시리즈 추가
    const volumeSeries = volumeChart.addHistogramSeries({
      color: '#26a69a',
      priceLineVisible: false,
    });

    // 평균이동선 추가
    const addMovingAverage = (period: number, color: string) => {
      const maSeries = chart.addLineSeries({ color, lineWidth: 1 });
      const maData = data.map((d, i, arr) => {
        if (i < period - 1) return null;
        const slice = arr.slice(i - period + 1, i + 1);
        const avg = slice.reduce((sum, item) => sum + item.close, 0) / period;
        return { time: d.time, value: avg };
      }).filter(Boolean) as { time: UTCTimestamp; value: number }[];
      maSeries.setData(maData);
    };

    if (maVisibility.short) addMovingAverage(5, '#FFA500');  // 주황 단기
    if (maVisibility.mid) addMovingAverage(20, '#008000');   // 초록 중기
    if (maVisibility.long) addMovingAverage(60, '#0000FF');  // 파랑 장기

    // 데이터 설정
    candlestickSeries.setData(data);
     // 데이터 설정 (거래량 차트)
     volumeSeries.setData(
      data.map((d, i) => {
        const prev = data[i - 1];
        const isHigherVolume = prev ? d.volume > prev.volume : true;
    
        return {
          time: d.time,
          value: d.volume,
          color: isHigherVolume ? "#CB3030" : "#2D7CD1",
        };
      })
    );
    

    // 화면에 맞게 시간 범위 조정
    chart.timeScale().fitContent();
    volumeChart.timeScale().fitContent();

    // 차트 클린업
    return () => chart.remove();
  }, []);

  return (
    <div className={styles.container}>
      <div className={styles.btnSection}>
        <TimeIntervalSelector onIntervalChange={setInterval}/>
        <MovingAverageSelector selected={maVisibility} onToggle={toggleMA} />
      </div>
      {/* 차트 영역 */}
      <div ref={chartContainerRef} className={styles.centerSection} />
      <div ref={volumeContainerRef} className={styles.bottomSection}/>
    </div>
  );
};

export default StockChart;
