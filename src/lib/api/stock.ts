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
export const getWatchlist = async (): Promise<WatchlistItem[]> => {
  const res = await axiosInstance.get("/api/v1/stock/watchlist");
  return res.data.watchlists;
};

// 관심 삭제
export const deleteWatchlist = async (watchlistId: number): Promise<DeleteWatchlistResponse> => {
  const res = await axiosInstance.delete(`/api/v1/stock/watchlist/${watchlistId}`);
  return res.data;
};