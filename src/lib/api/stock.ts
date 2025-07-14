import axiosInstance from "./axiosInstance";

export interface Stock {
  stockId: number;
  stockTicker: string;
  stockName: string;
  marketType: string;
  prevPrice?: number; // 임시
  currentPrice?: number; // 임시 
  priceChange?: number; // 임시 
};

export interface GetTopStocksResponse {
  success: boolean;
  message: string;
  stocks: Stock[];
};

// 관심 종목 등록 성공 응답 타입
export interface AddWatchlistSuccessResponse {
  success: true;
  message: string;
  ticker: string;
}

// 관심 종목 목록 조회
export interface GetWatchlistResponse {
  success: boolean;
  watchlists: WatchlistItem[];
  page: number;
  size: number;
  totalCount: number;
}

export interface WatchlistItem {
  watchlistId: number;
  stockId: number;
  StockTicker: string;
  StockName: string;
}

export interface DeleteWatchlistResponse {
  success: boolean;
  message: string;
}

// 봉 데이터 타입
export interface CandleData {
  ticker: string;
  dateTime: string; // ISO string or yyyy-MM-dd
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  closePrice: number;
  volume: number;
}
export type Interval = 
  | "1m" | "3m" | "5m" | "10m" | "15m" | "30m" | "60m"
  | "d" | "w" | "m" | "y";

// 호가 데이터 타입
export interface OrderbookData {
  ticker: string;
  askPrices: number[];
  askVolumes: number[];
  bidPrices: number[];
  bidVolumes: number[];
  timestamp: string;
}

// 매수 주문 데이터 타입
export interface BuyOrderRequest {
  ticker: string;
  orderPrice: number;
  orderAmount: number;
}
export interface BuyOrderResponse {
  success: boolean;
  message: string;
  errorCode?: string;
}

// 매도 주문 데이터 타입
export interface SellOrderRequest {
  ticker: string;
  orderPrice: number;
  orderAmount: number;
}
export interface SellOrderResponse {
  success: boolean;
  message: string;
  errorCode?: string;
}

// ─────────────────────────────
// 종목 API
// ─────────────────────────────

// 종목
export const getTopStocks = async (): Promise<GetTopStocksResponse> => {
  const res = await axiosInstance.get('/api/v1/stocks', {
  });
  return res.data;
};

// 관심
export const addWatchlist = async ( ticker: string ): Promise<AddWatchlistSuccessResponse> => {
  const res = await axiosInstance.post('/api/v1/stock/watchlist', {
    ticker,
  });
  return res.data;
};

// 관심 조회
export const getWatchlist = async ( page = 1, size = 10 ): Promise<GetWatchlistResponse> => {
  const res = await axiosInstance.get("/api/v1/stock/watchlist", {
    params: { page, size },
  });
  return res.data;
};

// 관심 삭제
export const deleteWatchlist = async (watchlistId: number): Promise<DeleteWatchlistResponse> => {
  const res = await axiosInstance.delete(`/api/v1/stock/watchlist/${watchlistId}`);
  return res.data;
};

// utils
const toApiInterval = (interval: Interval): string => {
  const minuteMatch = interval.match(/^(\d+)m$/); // 1m ~ 60m
  if (minuteMatch) return minuteMatch[1];         // "5m" → "5"
  return interval;                                // d / w / m / y는 그대로
};

// 봉 데이터 조회 함수
export const getCandleData = async (
  ticker: string,
  interval: Interval,
  offset: number,
  limit: number
): Promise<CandleData[]> => {
  const apiInterval = toApiInterval(interval); // 🔹 여기에 적용해야 함
  const res = await axiosInstance.get(`/api/v1/stocks/${ticker}/candles`, {
    params: { interval: apiInterval, offset, limit },
  });
  return res.data;
};

// 매수 주문
export const postBuyOrder = async ( data: BuyOrderRequest ): Promise<BuyOrderResponse> => {
  const res = await axiosInstance.post("/api/v1/order/buy", data);
  return res.data;
};

// 매도 주문
export const postSellOrder = async ( data: SellOrderRequest ): Promise<SellOrderResponse> => {
  const res = await axiosInstance.post("/api/v1/order/sell", data);
  return res.data;
};