"use client";

import React, { useState, useEffect, useRef } from "react";
import { createChart, UTCTimestamp, CandlestickSeriesOptions, LineSeriesOptions, Time, IChartApi, ISeriesApi, IPriceScaleApi } from "lightweight-charts";
import styles from "@/app/styles/components/StockChart.module.css";
import TimeIntervalSelector from "./TimeIntervalSelector";
import MovingAverageSelector from "./MovingAverageSelector"

type Props = {
  activeTab: 'chart' | 'community';
  setActiveTab: (tab: 'chart' | 'community') => void;
};
type Point = { time: Time; price: number };

// 예시 주식 데이터
const data: { open: number; high: number; low: number; close: number; time: UTCTimestamp; volume: number }[] = [
  { open: 10, high: 10.63, low: 9.49, close: 9.55, time: 1642427876 as UTCTimestamp, volume: 3000 },
  { open: 9.55, high: 10.30, low: 9.42, close: 9.94, time: 1642514276 as UTCTimestamp, volume: 2000 },
  { open: 9.94, high: 10.17, low: 9.92, close: 9.78, time: 1642600676 as UTCTimestamp, volume: 1500 },
  { open: 9.78, high: 10.59, low: 9.18, close: 9.51, time: 1642687076 as UTCTimestamp, volume: 2500 },
  { open: 9.51, high: 10.46, low: 9.10, close: 10.17, time: 1642773476 as UTCTimestamp, volume: 4000 },
  { open: 10.17, high: 10.96, low: 10.16, close: 10.47, time: 1642859876 as UTCTimestamp, volume: 3500 },
  { open: 10.47, high: 11.39, low: 10.40, close: 10.81, time: 1642946276 as UTCTimestamp, volume: 3000 },
  { open: 10.81, high: 11.60, low: 10.30, close: 10.75, time: 1643032676 as UTCTimestamp, volume: 2200 },
  { open: 10.75, high: 11.60, low: 10.49, close: 10.93, time: 1643119076 as UTCTimestamp, volume: 3300 },
  { open: 10.93, high: 11.53, low: 10.76, close: 10.96, time: 1643205476 as UTCTimestamp, volume: 3800 }
];

const StockChart: React.FC<Props> = ({ activeTab, setActiveTab }) => {
  const chartContainerRef = useRef<HTMLDivElement | null>(null);
  const volumeContainerRef = useRef<HTMLDivElement | null>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const maRefs = useRef<{ [period: number]: ISeriesApi<"Line"> }>({});
  const candlestickSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const [lines, setLines] = useState<Point[][]>([]);
  const [selectedInterval, setSelectedInterval] = useState<string>("d"); 
  const [maVisibility, setMaVisibility] = useState<{ [key: number]: boolean }>({
    5: true,
    20: false,
    60: false,
  });

  // 선 그리기용 점 저장 state
  const lineSeriesRef = useRef<ISeriesApi<"Line">[]>([]);

  const toggleMA = (period: number) => {
    setMaVisibility(prev => ({ ...prev, [period]: !prev[period] }));
  };

  useEffect(() => {
  if (!chartContainerRef.current || !volumeContainerRef.current) return;

    // 차트 옵션
    const chartOptions = {
      layout: {
        textColor: 'black',
      },
    };

    const chart = createChart(chartContainerRef.current, chartOptions);
    chartRef.current = chart;
    
    // 캔들스틱 시리즈 추가
    const candlestickSeries = chart.addCandlestickSeries({
      upColor: '#CB3030',
      downColor: '#2D7CD1',
      borderVisible: false,
      wickUpColor: '#CB3030',
      wickDownColor: '#2D7CD1',
    } as CandlestickSeriesOptions);

    candlestickSeries.setData(data);
    candlestickSeriesRef.current = candlestickSeries;

    // 거래량 차트 영역을 위한 또 다른 차트
    const volumeChart = createChart(volumeContainerRef.current, {
      layout: {
        textColor: 'black',
        background: { color: 'white' },
      },
      crosshair: {
        vertLine: { color: '#000', width: 1 },
        horzLine: { color: '#000', width: 1 },
      },
      grid: {
        vertLines: { visible: false },
        horzLines: { visible: false },
      },
      leftPriceScale: {
        visible: false, // 거래량 차트에는 가격 축을 숨깁니다
      },
    });

    // 거래량 시리즈 추가
    const volumeSeries = volumeChart.addHistogramSeries({
      color: '#26a69a',
      priceLineVisible: false,
    });

    // 데이터 설정
    candlestickSeries.setData(data);
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

    chart.timeScale().fitContent();
    volumeChart.timeScale().fitContent();
    
    Object.entries(maVisibility).forEach(([periodStr, visible]) => {
      const period = Number(periodStr);
      if (visible) {
        if (!maRefs.current[period]) {
          maRefs.current[period] = chart.addLineSeries({
            color: period === 5 ? '#FFA500' : period === 20 ? '#008000' : '#0000FF',
            lineWidth: 1,
          });
        }
        // 평균이동선
        const maData = data.map((d, i, arr) => {
          if (i < period - 1) return null;
          const slice = arr.slice(i - period + 1, i + 1);
          const avg = slice.reduce((sum, item) => sum + item.close, 0) / period;
          return { time: d.time, value: avg };
        }).filter(Boolean) as { time: UTCTimestamp; value: number }[];

        // 중복된 time 제거
        const filteredMAData = maData.reduce<{ time: UTCTimestamp; value: number }[]>((acc, cur) => {
          if (acc.length === 0 || acc[acc.length - 1].time !== cur.time) {
            acc.push(cur);
          }
          return acc;
        }, []);

        maRefs.current[period].setData(filteredMAData);
      } else {

        if (maRefs.current[period]) {
          chart.removeSeries(maRefs.current[period]);
          delete maRefs.current[period];
        }
      }
    });

    // 차트 클린업
    return () => {
      chart.remove();
      volumeChart.remove();
      candlestickSeriesRef.current = null;
    };
  }, [maVisibility]);

  // 클릭 좌표를 시계열 데이터 좌표로 변환하는 함수
  const coordinateToPoint = (x: number, y: number): Point | null => {
    if (!chartRef.current) return null;
    if (!candlestickSeriesRef.current) return null;

    const time = chartRef.current.timeScale().coordinateToTime(x);
    if (time === null) return null;

    // 시리즈에서 coordinateToPrice 호출
    const price = candlestickSeriesRef.current.coordinateToPrice(y);
    if (price === null) return null;

    return { time, price };
  };

  // 차트 클릭 이벤트 핸들러
  const handleChartClick = (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    if (!chartContainerRef.current) return;

    const rect = chartContainerRef.current.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const point = coordinateToPoint(x, y);
    if (point) {
    setLines(prev => {
      const lastLine = prev[prev.length - 1] || [];
      if (lastLine.length === 0) {
        // 새 선 시작
        return [...prev, [point]];
      } else if (lastLine.length === 1) {
        // 선 완성(2점)
        const updatedLastLine = [...lastLine, point];
        return [...prev.slice(0, -1), updatedLastLine];
      } else {
        // 이미 2점이라면 새 선 추가
        return [...prev, [point]];
      }
    });
  }
};

  // 선 그리기 업데이트
  useEffect(() => {
    if (!lineSeriesRef.current) return;
    if (!chartRef.current) return;

    lineSeriesRef.current.forEach(series => {
      if (series) {
        chartRef.current!.removeSeries(series);
      }
    });
    lineSeriesRef.current.length = 0;
    lineSeriesRef.current = [];

    if (!lines || lines.length === 0) return;
    
    // 새로 만든 선마다 LineSeries 생성 후 데이터 세팅
    lines.forEach(linePoints => {
      if (linePoints.length === 2) {
        const series = chartRef.current!.addLineSeries({
          color: 'blue',
          lineWidth: 2,
        });
        if (series) {
          series.setData(
            [linePoints[0], linePoints[1]]
              .sort((a, b) => (a.time as number) - (b.time as number))
              .map(p => ({ time: p.time, value: p.price }))
          );
          lineSeriesRef.current.push(series);
        }
      }
    });
  }, [lines]);

  return (
    <div className={styles.container}>
      <div className={styles.btnSection}>
        <div className={styles.label}>시간 간격</div>
        <TimeIntervalSelector onIntervalChange={setSelectedInterval}/>
        <h1>|</h1>
        <div className={styles.label}>이동평균선 주기</div>
        <MovingAverageSelector selected={maVisibility} onToggle={toggleMA} />
      </div>
      {/* 차트 영역 */}
      <div ref={chartContainerRef} className={styles.centerSection} onClick={handleChartClick}/>
      <div ref={volumeContainerRef} className={styles.bottomSection}/>
    </div>
  );
};

export default StockChart;