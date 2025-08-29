import axiosInstance from "./axiosInstance";

export interface UserRanking {
  userId: number;
  nickname: string;
  profitRate: string;
  totalAsset: string;
  ranking: number;
}

export interface GetRankingResponse {
  rankingList: UserRanking[];
  myRanking: UserRanking | null;
  totalCount: number;
  isMarketOpen: boolean;
  generatedAt: string;
}

export type RankingMode = "profit" | "asset";

export const getRanking = async (mode: RankingMode): Promise<GetRankingResponse> => {
  const endpoint =
    mode === "profit"
      ? "/api/v1/ranking/profit-rate"
      : "/api/v1/ranking/total-asset";

  const { data } = await axiosInstance.get<GetRankingResponse>(endpoint);
  return data;
};