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

// ─────────────────────────────
// 종목 API
// ─────────────────────────────

export const getTopStocks = async (): Promise<GetTopStocksResponse> => {
  const res = await axiosInstance.get('/api/v1/stocks', {
    headers: {
      "Content-Type": "application/json",
    },
  });
  return res.data;
};