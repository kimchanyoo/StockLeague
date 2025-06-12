"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import {
  createChart,
  UTCTimestamp,
  Time,
  IChartApi,
  ISeriesApi,
  LogicalRange,
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
  const volumeChartRef = useRef<IChartApi | null>(null);
  const candlestickSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const maRefs = useRef<{ [period: number]: ISeriesApi<"Line"> }>({});
  const lineSeriesRef = useRef<ISeriesApi<"Line">[]>([]);
  const previewLineRef = useRef<ISeriesApi<"Line"> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<"Histogram"> | null>(null);
  const prevScrollRangeRef = useRef<LogicalRange | null>(null);
  const pendingScrollAdjust = useRef<number>(0);
  const requestedOffsets = useRef<Set<number>>(new Set());
  const lastRangeFrom = useRef<number | null>(null);

  const [candles, setCandles] = useState<CandleData[]>([]);
  const [lines, setLines] = useState<Point[][]>([]);
  const [hoverPreviewLine, setHoverPreviewLine] = useState<Point[] | null>(null);

  const [selectedInterval, setSelectedInterval] = useState<Interval>("d");
  const [maVisibility, setMaVisibility] = useState<{ [key: number]: boolean }>({ 5: true, 20: false, 60: false, });
  const [isDrawingLine, setIsDrawingLine] = useState<boolean>(false);
  
  const [offset, setOffset] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const noMoreDataRef = useRef(false);

  // 봉 데이터 불러오기 (interval, ticker 변경 시)
  useEffect(() => {
    if (!ticker) return;
    const initialLoadCount = INITIAL_LOAD_COUNTS[selectedInterval] || 100;
    let isMounted = true;

    getCandleData(ticker, selectedInterval, 0, initialLoadCount)
      .then((data) => {
        if (!isMounted) return;
        const sortedData = data.sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime());
        setCandles(sortedData);
        setOffset(sortedData.length);
      })
      .catch((err) => {
        if (!isMounted) return;
        console.error("캔들 데이터 로드 실패:", err);
        setCandles([]);
        setOffset(0);
      });

    return () => { isMounted = false; };
  }, [ticker, selectedInterval]);

  useEffect(() => {
    requestedOffsets.current.clear();
    noMoreDataRef.current = false;
  }, [ticker, selectedInterval]);

  const loadMoreCandles = useCallback(() => {
    if (isLoading || !chartRef.current || requestedOffsets.current.has(offset)) return;
    
    requestedOffsets.current.add(offset);
    setIsLoading(true);

    const chart = chartRef.current;
    const timeScale = chart.timeScale();
    const prevRange = timeScale.getVisibleLogicalRange();
    const addCount = ADDITIONAL_LOAD_COUNTS[selectedInterval] || 50;

    getCandleData(ticker, selectedInterval, offset, addCount)
      .then((data) => {
        if (data.length === 0) {
          noMoreDataRef.current = true; // ✅ 더 이상 불러올 데이터 없음
          return;
        }

        const sortedData = data.sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime());

        let newDataCount = 0;
        setCandles((prev) => {
          const existingTimestamps = new Set(prev.map((c) => c.dateTime));
          const newData = sortedData.filter((c) => !existingTimestamps.has(c.dateTime));
          newDataCount = newData.length;

          prevScrollRangeRef.current = prevRange;
          pendingScrollAdjust.current = newDataCount;

          return [...newData, ...prev]; // prepend
        });

        setOffset((prevOffset) => prevOffset + addCount);
      })
      .catch((err) => {
        console.error("추가 캔들 로드 실패:", err);
      })
      .finally(() => setIsLoading(false));
  }, [isLoading, offset, selectedInterval, ticker]);

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

  // 차트와 시리즈는 처음 마운트 시에만 생성
  useEffect(() => {
    let isMounted = true;
    if (!chartContainerRef.current || !volumeContainerRef.current) return;

    const chart = createChart(chartContainerRef.current, { layout: { textColor: 'black' } });
    chartRef.current = chart;

    const volumeChart = createChart(volumeContainerRef.current, {
      layout: { textColor: 'black', background: { color: 'white' } },
      crosshair: { vertLine: { color: '#000' }, horzLine: { color: '#000' } },
      grid: { vertLines: { visible: false }, horzLines: { visible: false } },
      leftPriceScale: { visible: false },
    });
    volumeChartRef.current = volumeChart;

    const candlestickSeries = chart.addCandlestickSeries({
      upColor: '#CB3030',
      downColor: '#2D7CD1',
      borderVisible: false,
      wickUpColor: '#CB3030',
      wickDownColor: '#2D7CD1',
    });
    candlestickSeriesRef.current = candlestickSeries;

    const volumeSeries = volumeChart.addHistogramSeries({
      color: "#26a69a",
      priceLineVisible: false,
    });
    volumeSeriesRef.current = volumeSeries;

    // visible range 이벤트 처리
    const timeScale = chart.timeScale();
    const volumeTimeScale = volumeChart.timeScale();

    const visibleRangeHandler = (range: LogicalRange | null) => {
      if (!range || !isMounted) return;
      if (lastRangeFrom.current === range.from) return;

      lastRangeFrom.current = range.from;
      
      if (range.from < 100) {
        loadMoreCandles();
      }
    };
    timeScale.subscribeVisibleLogicalRangeChange(visibleRangeHandler);

    const syncVolumeTimeScale = (range: LogicalRange | null) => {
      if (!range) return;
      volumeTimeScale.setVisibleLogicalRange(range);
    };
    timeScale.subscribeVisibleLogicalRangeChange(syncVolumeTimeScale);

    const syncChartTimeScale = (range: LogicalRange | null) => {
      if (!range) return;
      timeScale.setVisibleLogicalRange(range);
    };
    volumeTimeScale.subscribeVisibleLogicalRangeChange(syncChartTimeScale);

    return () => {
      isMounted = false;
      timeScale.unsubscribeVisibleLogicalRangeChange(visibleRangeHandler);
      timeScale.unsubscribeVisibleLogicalRangeChange(syncVolumeTimeScale);
      volumeTimeScale.unsubscribeVisibleLogicalRangeChange(syncChartTimeScale);
      chart.remove();
      volumeChart.remove();
      chartRef.current = null;
      candlestickSeriesRef.current = null;
      maRefs.current = {};
    };
  }, [loadMoreCandles]);

  // candles 데이터가 바뀔 때마다 데이터만 갱신
  useEffect(() => {
    if (!chartRef.current || !candlestickSeriesRef.current) return;

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

    candlestickSeriesRef.current.setData(candleChartData);

    // 거래량 갱신
    if (volumeSeriesRef.current) {
      volumeSeriesRef.current.setData(
        sortedCandles.map((c, i) => ({
          time: Math.floor(new Date(c.dateTime).getTime() / 1000) as UTCTimestamp,
          value: c.volume,
          color:
            i > 0 && c.volume > sortedCandles[i - 1].volume
              ? "#CB3030"
              : "#2D7CD1",
        }))
      );
    }

    // 이동평균선 갱신 (toggleMA 함수 내 계산 재사용 가능)
    Object.entries(maVisibility).forEach(([periodStr, visible]) => {
      const period = Number(periodStr);
      if (!chartRef.current) return;

      if (visible) {
        if (!maRefs.current[period]) {
          maRefs.current[period] = chartRef.current.addLineSeries({
            color:
              period === 5
                ? "#FFA500"
                : period === 20
                ? "#008000"
                : "#0000FF",
            lineWidth: 1,
          });
        }
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
            chartRef.current.removeSeries(maRefs.current[period]!);
          } catch (e) {
            console.warn(`MA 제거 실패 (period ${period}):`, e);
          }
          delete maRefs.current[period];
        }
      }
    });
  }, [candles, maVisibility]);

  useEffect(() => {
    if ( chartRef.current && prevScrollRangeRef.current && pendingScrollAdjust.current > 0 ) {
      const timeScale = chartRef.current.timeScale();
      const currentRange = timeScale.getVisibleLogicalRange();

      if (!currentRange) return;

      const newRange = {
        from: prevScrollRangeRef.current.from + pendingScrollAdjust.current,
        to: prevScrollRangeRef.current.to + pendingScrollAdjust.current,
      };

      if (newRange.from < 0 || newRange.from < currentRange.from) {
        prevScrollRangeRef.current = null;
        pendingScrollAdjust.current = 0;
        return;
      }

      timeScale.setVisibleLogicalRange(newRange);

      // 초기화
      prevScrollRangeRef.current = null;
      pendingScrollAdjust.current = 0;
    }
  }, [candles]);

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
