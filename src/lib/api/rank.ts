import axios from "axios";

export interface ProfitRankingItem {
  userId: number;
  nickname: string;
  profitRate: string;
  totalAsset: string;
  ranking: number;
}

export interface GetProfitRankingResponse {
  rankingList: ProfitRankingItem[];
  myRanking: ProfitRankingItem;
  totalCount: number;
  isMarketOpen: boolean;
}

export const getProfitRanking = async (): Promise<GetProfitRankingResponse> => {
  const res = await axios.get<GetProfitRankingResponse>("/api/v1/ranking/profit-rate");
  return res.data;
};