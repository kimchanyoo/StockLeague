import axiosInstance from "./axiosInstance";

export interface NoticeDetail {
  success: boolean;
  noticeId: number;
  title: string;
  category: string;
  content: string;
  createdAt: string;
  isPinned: boolean;
}

export const getNoticeDetail = async (noticeId: number): Promise<NoticeDetail> => {
  const res = await axiosInstance.get(`/api/v1/notices/${noticeId}`);

  if (res.data.success) {
    return res.data;
  } else {
    throw new Error(res.data.message || "공지사항을 불러오지 못했습니다.");
  }
};
