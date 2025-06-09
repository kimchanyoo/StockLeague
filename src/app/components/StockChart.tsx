"use client";

import React, { useState, useEffect, useRef } from "react";
import {
  createChart,
  UTCTimestamp,
  CandlestickSeriesOptions,
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
import { getCandleData, CandleData, Interval } from '@/lib/api/stock';

// 초기 로드 및 추가 로드 개수 정의
const INITIAL_LOAD_COUNTS: Record<Interval, number> = {
  "1m": 500, "3m": 600, "5m": 700, "10m": 700,
  "15m": 700, "30m": 700, "60m": 800,
  d: 250, w: 150, m: 80, y: 40,
};

const ADDITIONAL_LOAD_COUNTS: Record<Interval, number> = {
  "1m": 200, "3m": 200, "5m": 200, "10m": 200,
  "15m": 200, "30m": 200, "60m": 200,
  d: 100, w: 50, m: 25, y: 10,
};

type Props = {
  activeTab: 'chart' | 'community';
  setActiveTab: (tab: 'chart' | 'community') => void;
  ticker: string;
};

type Point = { time: Time; price: number };


const StockChart: React.FC<Props> = ({ activeTab, setActiveTab, ticker }) => {
  const chartContainerRef = useRef<HTMLDivElement | null>(null);
  const volumeContainerRef = useRef<HTMLDivElement | null>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const candlestickSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const maRefs = useRef<{ [period: number]: ISeriesApi<"Line"> }>({});
  const lineSeriesRef = useRef<ISeriesApi<"Line">[]>([]);
  const previewLineRef = useRef<ISeriesApi<"Line"> | null>(null);

  const [candles, setCandles] = useState<CandleData[]>([]);
  const [lines, setLines] = useState<Point[][]>([]);
  const [hoverPreviewLine, setHoverPreviewLine] = useState<Point[] | null>(null);

  const [selectedInterval, setSelectedInterval] = useState<Interval>("d");
  const [maVisibility, setMaVisibility] = useState<{ [key: number]: boolean }>({ 5: true, 20: false, 60: false, });
  const [isDrawingLine, setIsDrawingLine] = useState<boolean>(false);
  
  const [offset, setOffset] = useState(0);

  // 봉 데이터 불러오기 (interval, ticker 변경 시)
  useEffect(() => {
    if (!ticker) return;
    // 초기 로드 개수 가져오기
    const initialLoadCount = INITIAL_LOAD_COUNTS[selectedInterval] || 100;

    getCandleData(ticker, selectedInterval, offset, initialLoadCount)
      .then((data) => {
        console.log("받은 캔들 데이터:", data);
        const sortedData = data.sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime());
        if (offset === 0) {
          // 처음 로딩
          setCandles(sortedData);
        } else {
          // 추가 로딩 시 기존 데이터 뒤에 붙임
          setCandles(prev => [...sortedData, ...prev]); // 오래된 데이터가 앞에 오도록
        }
      })
      .catch((err) => {
        console.error("캔들 데이터 로드 실패:", err);
        if (offset === 0) setCandles([]);
      });
  }, [ticker, selectedInterval, offset]);;

  // 추가 데이터 더 불러오기 (예: 스크롤 하단에 도달했을 때 호출 가능)
  const loadMoreCandles = () => {
    const addCount = ADDITIONAL_LOAD_COUNTS[selectedInterval] || 50;
    setOffset(prev => prev + addCount);
  };
  // interval 또는 ticker 변경 시 offset 초기화
  useEffect(() => {
    setOffset(0);
  }, [selectedInterval, ticker]);

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

    const chart = createChart(chartContainerRef.current, { layout: { textColor: 'black' }, });
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

    const sortedCandles = [...candles].sort(
      (a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime()
    );

    const candleChartData = sortedCandles.map((c) => ({
      time: Math.floor(new Date(c.dateTime).getTime() / 1000) as UTCTimestamp,
      open: c.openPrice,
      high: c.highPrice,
      low: c.lowPrice,
      close: c.closePrice,
    }));

    candlestickSeries.setData(candleChartData);

    // 거래량 히스토그램 시리즈
    const volumeSeries = volumeChart.addHistogramSeries({
      color: "#26a69a",
      priceLineVisible: false,
    });
    volumeSeries.setData(
      candles.map((c, i) => ({
        time: Math.floor(new Date(c.dateTime).getTime() / 1000) as UTCTimestamp,
        value: c.volume,
        color:
          i > 0 && c.volume > candles[i - 1].volume
            ? "#CB3030"
            : "#2D7CD1",
      }))
    );

    chart.timeScale().fitContent();
    volumeChart.timeScale().fitContent();

    // 이동평균선 처리
    Object.entries(maVisibility).forEach(([periodStr, visible]) => {
      const period = Number(periodStr);
      if (!chartRef.current) return;

      if (visible) {
        if (!maRefs.current[period]) {
          maRefs.current[period] = chart.addLineSeries({
            color:
              period === 5
                ? "#FFA500"
                : period === 20
                ? "#008000"
                : "#0000FF",
            lineWidth: 1,
          });
        }

        // 이동평균 데이터 계산
        const maData = candleChartData
          .map((d, i, arr) => {
            if (i < period - 1) return null;
            const avg =
              arr
                .slice(i - period + 1, i + 1)
                .reduce((sum, item) => sum + item.close, 0) / period;
            return { time: d.time, value: avg };
          })
          .filter(Boolean) as { time: UTCTimestamp; value: number }[];

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
  }, [maVisibility, candles]);

  useEffect(() => {
    if (!chartContainerRef.current) return;
    chartContainerRef.current.style.cursor = isDrawingLine ? "crosshair" : "crosshair";
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
