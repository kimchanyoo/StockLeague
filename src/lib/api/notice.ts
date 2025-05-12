import axiosInstance from "./axiosInstance";

export interface Notice {
  noticeId: number;
  title: string;
  category: string;
  isPinned: boolean;
  createdAt: string;
}

export interface NoticeListResponse {
  success: boolean;
  notices: Notice[];
  page: number;
  size: number;
  totalCount: number;
}

export const getNotices = async (
  page: number, 
  size: number = 10,
  keyword?: string
): Promise<NoticeListResponse> => {
  const res = await axiosInstance.get('/api/v1/notices', {
    params: { page, size, keyword },
    headers: {
      "Content-Type": "application/json",
    },
  }); 
  return res.data;
};
