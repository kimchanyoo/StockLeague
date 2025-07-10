import axiosInstance from "./axiosInstance";

export interface StockItem {
  ticker: string;
  name: string;
  quantity: number;
  averagePurchasePrice: number;
  purchaseAmount: number;
  currentPrice: number;
  evaluationAmount: number;
  profit: number;
  returnRate: number;
  holdingRatio: number;
}

export interface PortfolioResponse {
  totalInvestment: number;
  totalEvaluation: number;
  totalReturnRate: number;
  stocks: StockItem[];
}

// ─────────────────────────────
// 유저 API
// ─────────────────────────────

// 보유자산
export const getCashBalance = async () => {
  const res = await axiosInstance.get('/api/v1/asset');
  return Number(res.data.cashBalance);
};

// 포트폴리오
export const getPortfolio = async (): Promise<PortfolioResponse> => {
  const res = await axiosInstance.get('/api/v1/portfolio', {
  });
  return res.data;
};