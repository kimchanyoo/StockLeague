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

// 주문 상태 Enum 타입
export type OrderStatus =
  | "WAITING"                 // 대기 중
  | "PARTIALLY_EXECUTED"      // 일부 체결
  | "EXECUTED"                // 전체 체결
  | "CANCELED"                // 전체 취소
  | "CANCELED_AFTER_PARTIAL"  // 일부 체결 후 나머지 취소
  | "EXPIRED"                 // 유효 시간 만료
  | "FAILED";                 // 주문 실패

// 주문 타입
export type OrderType = "BUY" | "SELL";

// 단일 주문 항목
export interface OrderItem {
  orderId: number;
  stockTicker: string;
  stockName: string;
  orderType: OrderType;
  orderStatus: OrderStatus;
  orderPrice: number;
  orderAmount: number;
  executedAmount: number;
  remainingAmount: number;
  averageExecutedPrice: number;
  createdAt: string; // ISO 8601 string (e.g., "2025-07-15T10:24:00")
}

// 전체 응답 타입
export interface OrderListResponse {
  success: boolean;
  contents: OrderItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
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

// 주문 내역 조회
export const getMyOrder = async ( page: number = 1, size: number = 10 ): Promise<OrderListResponse> => {
  const res = await axiosInstance.get<OrderListResponse>("/api/v1/order", {
    params: { page, size },
  });
  return res.data;
};

// 주문 취소
export const cancelOrder = async (orderId: number) => {
  const res = await axiosInstance.patch(`/api/v1/order/${orderId}/cancel`);
  return res.data;
};

// 개별 주문 상세 조회