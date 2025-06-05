"use client";

import React, { useState, useEffect, useRef } from "react";
import {
  createChart,
  UTCTimestamp,
  CandlestickSeriesOptions,
  LineSeriesOptions,
  Time,
  IChartApi,
  ISeriesApi,
} from "lightweight-charts";
import styles from "@/app/styles/components/StockChart.module.css";
import TimeIntervalSelector from "./TimeIntervalSelector";
import MovingAverageSelector from "./MovingAverageSelector";
import DeleteIcon from '@mui/icons-material/Delete';
import ShowChartIcon from '@mui/icons-material/ShowChart';
import { IconButton, Tooltip } from '@mui/material';

type Props = {
  activeTab: 'chart' | 'community';
  setActiveTab: (tab: 'chart' | 'community') => void;
};

type Point = { time: Time; price: number };

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
  const candlestickSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const maRefs = useRef<{ [period: number]: ISeriesApi<"Line"> }>({});
  const lineSeriesRef = useRef<ISeriesApi<"Line">[]>([]);
  const previewLineRef = useRef<ISeriesApi<"Line"> | null>(null);

  const [lines, setLines] = useState<Point[][]>([]);
  const [hoverPreviewLine, setHoverPreviewLine] = useState<Point[] | null>(null);

  const [selectedInterval, setSelectedInterval] = useState<string>("d");
  const [maVisibility, setMaVisibility] = useState<{ [key: number]: boolean }>({ 5: true, 20: false, 60: false, });
  const [isDrawingLine, setIsDrawingLine] = useState<boolean>(false);

  const toggleMA = (period: number) => {
    clearLines();
    setMaVisibility(prev => ({ ...prev, [period]: !prev[period] }));
  };
  
  // 선 그리기 / 삭제 모드 토글 함수
  const toggleDrawingLine = () => { setIsDrawingLine((prev) => !prev); };
  const clearLines = () => { 
    if (!chartRef.current) return;
    lineSeriesRef.current.forEach(series => {
      if (series) {
        try {
          chartRef.current!.removeSeries(series);
        } catch (e) {
          console.warn('선 제거 중 오류 발생:', e);
        }
      }
    });
    lineSeriesRef.current = [];
    setLines([]); 
  };

  const coordinateToPoint = (x: number, y: number): Point | null => {
    if (!chartRef.current || !candlestickSeriesRef.current) return null;
    const time = chartRef.current.timeScale().coordinateToTime(x);
    const price = candlestickSeriesRef.current.coordinateToPrice(y);
    if (time === null || price === null) return null;
    return { time, price };
  };

  const handleChartClick = (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    if (!isDrawingLine) return;
    
    if (!chartContainerRef.current) return;
    const rect = chartContainerRef.current.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const point = coordinateToPoint(x, y);

    if (point) {
      setLines(prev => {
        const last = prev[prev.length - 1] || [];
        if (last.length === 0) return [...prev, [point]];
        if (last.length === 1) return [...prev.slice(0, -1), [...last, point]];
        return [...prev, [point]];
      });
      setHoverPreviewLine(null)
    }
  };

  const handleMouseMove = (event: MouseEvent) => {
    if (!isDrawingLine || !chartContainerRef.current) return;
    const lastLine = lines[lines.length - 1];
    if (!lastLine || lastLine.length !== 1) return;

    const rect = chartContainerRef.current.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const point = coordinateToPoint(x, y);
    if (point) setHoverPreviewLine([lastLine[0], point]);
  };

  useEffect(() => {
    if (!chartContainerRef.current || !volumeContainerRef.current) return;

    const chart = createChart(chartContainerRef.current, {
      layout: { textColor: 'black' },
    });
    chartRef.current = chart;

    const volumeChart = createChart(volumeContainerRef.current, {
      layout: { textColor: 'black', background: { color: 'white' } },
      crosshair: { vertLine: { color: '#000' }, horzLine: { color: '#000' } },
      grid: { vertLines: { visible: false }, horzLines: { visible: false } },
      leftPriceScale: { visible: false },
    });

    const candlestickSeries = chart.addCandlestickSeries({
      upColor: '#CB3030',
      downColor: '#2D7CD1',
      borderVisible: false,
      wickUpColor: '#CB3030',
      wickDownColor: '#2D7CD1',
    } as CandlestickSeriesOptions);
    const sortedData = [...data].sort((a, b) => a.time - b.time);
    candlestickSeries.setData(sortedData);
    candlestickSeriesRef.current = candlestickSeries;

    const volumeSeries = volumeChart.addHistogramSeries({ color: '#26a69a', priceLineVisible: false });
    volumeSeries.setData(
      data.map((d, i) => ({
        time: d.time,
        value: d.volume,
        color: i > 0 && d.volume > data[i - 1].volume ? '#CB3030' : '#2D7CD1',
      }))
    );

    chart.timeScale().fitContent();
    volumeChart.timeScale().fitContent();

    Object.entries(maVisibility).forEach(([periodStr, visible]) => {
      const period = Number(periodStr);
      if (!chartRef.current) return;

      if (visible) {
        if (!maRefs.current[period]) {
          maRefs.current[period] = chart.addLineSeries({
            color: period === 5 ? '#FFA500' : period === 20 ? '#008000' : '#0000FF',
            lineWidth: 1,
          });
        }

        const maData = data.map((d, i, arr) => {
          if (i < period - 1) return null;
          const avg = arr.slice(i - period + 1, i + 1).reduce((sum, item) => sum + item.close, 0) / period;
          return { time: d.time, value: avg };
        }).filter(Boolean) as { time: UTCTimestamp; value: number }[];
        maData.sort((a, b) => (a.time as number) - (b.time as number));

        maRefs.current[period]?.setData(maData);
      } else {
        if (maRefs.current[period]) {
          try {
            chartRef.current?.removeSeries(maRefs.current[period]!);
          } catch (e) {
            console.warn(`MA 제거 실패 (period ${period}):`, e);
          }
          delete maRefs.current[period];
        }
      }
    });

    return () => {
      chart.remove();
      volumeChart.remove();
      candlestickSeriesRef.current = null;
      maRefs.current = {};
    };
  }, [maVisibility]);

  useEffect(() => {
    if (!chartContainerRef.current) return;
    chartContainerRef.current.addEventListener("mousemove", handleMouseMove);
    return () => chartContainerRef.current?.removeEventListener("mousemove", handleMouseMove);
  }, [lines, isDrawingLine]);
  
  useEffect(() => {
    if (!chartRef.current) return;

    lineSeriesRef.current.forEach(s => {
      if (s) {
        try {
          chartRef.current?.removeSeries(s);
        } catch (e) {
          console.warn('선 제거 실패:', e);
        }
      }
    });
    lineSeriesRef.current = [];

    if (!lines || lines.length === 0) return;

    lines.forEach(line => {
      if (line.length !== 2) return;
      const series = chartRef.current!.addLineSeries({ color: 'blue', lineWidth: 2, });

      const [p1, p2] = line;

      if (p1.time === p2.time) {
        const min = Math.min(p1.price, p2.price);
        const max = Math.max(p1.price, p2.price);

        series.setData([
          { time: p1.time as UTCTimestamp, value: min },
          { time: (p1.time as number + 1) as UTCTimestamp, value: max },
        ]);
      } else {
        const sorted = [p1, p2].sort((a, b) => (a.time as number) - (b.time as number));
        series.setData(sorted.map(p => ({
          time: p.time as UTCTimestamp,
          value: p.price,
        })));
      }

      lineSeriesRef.current.push(series);
    });
  }, [lines]);

  useEffect(() => {
    if (!chartRef.current) return;

    if (previewLineRef.current) {
      chartRef.current.removeSeries(previewLineRef.current);
      previewLineRef.current = null;
    }

    if (hoverPreviewLine && hoverPreviewLine.length === 2) {
      const previewSeries = chartRef.current.addLineSeries({ color: 'gray', lineWidth: 1, lineStyle: 1 });
      let [p1, p2] = [...hoverPreviewLine];
      if (p1.time === p2.time) {
        p2 = { ...p2, time: (p2.time as number) + 1 as UTCTimestamp };
      }
      const sorted = [p1, p2].sort((a, b) => (a.time as number) - (b.time as number));
      previewSeries.setData(sorted.map(p => ({ time: p.time as UTCTimestamp, value: p.price })));
      previewLineRef.current = previewSeries;
    }
  }, [hoverPreviewLine]);

  return (
    <div className={styles.container}>
      <div className={styles.btnSection}>
        <div className={styles.label}>시간 간격</div>
        <TimeIntervalSelector onIntervalChange={setSelectedInterval} />
        <h1>|</h1>
        <div className={styles.label}>이동평균선 주기</div>
        <MovingAverageSelector selected={maVisibility} onToggle={toggleMA} />
        <h1>|</h1>
        <div className={styles.label}>그리기</div>
        <div>
          <Tooltip title={isDrawingLine ? "그리기 끄기" : "그리기 켜기"}>
            <IconButton
              onClick={toggleDrawingLine}
              color={isDrawingLine ? "primary" : "default"}
              aria-pressed={isDrawingLine}
              size="large"
            >
              <ShowChartIcon />
            </IconButton>
          </Tooltip>

          <Tooltip title="모든 선 삭제">
            <span>
              <IconButton
                onClick={clearLines}
                disabled={lines.length === 0}
                color="error"
                size="large"
              >
                <DeleteIcon />
              </IconButton>
            </span>
          </Tooltip>
        </div>
      </div>
      <div ref={chartContainerRef} className={styles.centerSection} onClick={handleChartClick} />
      <div ref={volumeContainerRef} className={styles.bottomSection} />
    </div>
  );
};

export default StockChart;
