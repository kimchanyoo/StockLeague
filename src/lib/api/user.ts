import axiosInstance from "./axiosInstance";

export interface UserAssetValuation {
  cashBalance: string;         // 현금 잔고
  totalAsset: string;          // 총 자산 (주식 평가 + 현금)
  totalProfit: string;         // 총 손익
  totalProfitRate: string;     // 총 수익률 (퍼센트, 소수점)
  stockValuation: string;      // 주식 평가금액
  stocks: StockValuationItem[]; // 보유 종목 리스트
}

export interface StockValuationItem {
  ticker: string;
  stockName: string;        
  quantity: string;
  avgBuyPrice: string;
  currentPrice: string;
  valuation: string;
  profit: string;
  profitRate: string;
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
  orders: OrderItem[];
  page: number;
  size: number;
  totalCount: number;
  totalPages: number;
}

// 세부 주문 데이터
export interface OrderExecution {
  orderExecutionId: number;
  stockName: string;
  orderType: OrderType;
  executedPrice: number;
  executedAmount: number;
  executedAt: string;
}
export interface GetDetailMyOrderResponse {
  success: boolean;
  executions: OrderExecution[];
}

// 체결 데이터
export interface OrderExecution {
  orderExecutionId: number;
  stockName: string;
  orderType: OrderType;
  executedPrice: number;
  executedAmount: number;
  executedAt: string;
}
export interface GetAllMyExecutionsResponse {
  success: boolean;
  executions: OrderExecution[];
  page: number;
  size: number;
  totalCount: number;
  totalPage: number;
}

// 미체결 데이터
export interface UnexecutedOrder {
  orderId: number;
  stockName: string;
  stockTicker: string;
  orderType: OrderType;
  orderPrice: number;
  orderAmount: number;
  remainingAmount: number;
  status: OrderStatus;
  createdAt: string;
}
export interface GetUnexecutedOrdersResponse {
  success: boolean;
  unexecutedOrders: UnexecutedOrder[];
  page: number;
  size: number;
  totalCount: number;
  totalPage: number;
}

// ─────────────────────────────
// 유저 API
// ─────────────────────────────

// 유저 전체 자산 + 포트폴리오 평가
export const getUserAssetValuation = async (): Promise<UserAssetValuation> => {
  const res = await axiosInstance.get<UserAssetValuation>(`/api/v1/asset`, {
  });
  return res.data;
};

// 주문 내역 조회
export const getMyOrder = async ( page: number = 1, size: number = 10 ): Promise<OrderListResponse> => {
  const res = await axiosInstance.get<OrderListResponse>(`/api/v1/order/`, {
    params: { page, size },
  });
  return res.data;
};

// 세부 주문 내역 조회
export const getDetailMyOrder = async (orderId: number): Promise<GetDetailMyOrderResponse> => {
  const res = await axiosInstance.get<GetDetailMyOrderResponse>(`/api/v1/executions/${orderId}/executions`);
  return res.data;
};

// 체결 내역 조회
export async function getAllMyExecutions( page: number = 1, size: number = 10 ): Promise<GetAllMyExecutionsResponse> {
  const response = await axiosInstance.get<GetAllMyExecutionsResponse>(`/api/v1/executions`, { 
    params: { page, size },
  });
  return response.data;
}

// 미체결 내역 조회
export async function getUnexecutedOrders( page: number = 1, size: number = 10 ): Promise<GetUnexecutedOrdersResponse> {
  const response = await axiosInstance.get<GetUnexecutedOrdersResponse>(`/api/v1/executions/unexecuted`, {
    params: { page, size },
  });
  return response.data;
}

// 주문 취소
export const cancelOrder = async (orderId: number) => {
  const res = await axiosInstance.patch(`/api/v1/order/${orderId}/cancel`);
  return res.data;
};

