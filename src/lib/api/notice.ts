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

export interface NoticeDetail {
  success: boolean;
  noticeId: number;
  title: string;
  category: string;
  content: string;
  createdAt: string;
  isPinned: boolean;
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


export const getNoticeDetail = async (noticeId: number): Promise<NoticeDetail> => {
  const res = await axiosInstance.get(`/api/v1/notices/${noticeId}`);

  if (res.data.success) {
    return res.data;
  } else {
    throw new Error(res.data.message || "공지사항을 불러오지 못했습니다.");
  }
};

export const getSearchedNotices = async (
  keyword: string,
  page: number = 1,
  size: number = 10
) => {
  const res = await axiosInstance.get("/api/v1/notices/search", {
    params: { keyword, page, size },
    headers: {
      "Content-Type": "application/json",
    },
  });

  return res.data.data;
};