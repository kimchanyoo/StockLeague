import axiosInstance from "./axiosInstance";
import axios from "axios";

// 사용자용 목록
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

// 사용자용 상세
export interface NoticeDetail {
  success: boolean;
  noticeId: number;
  title: string;
  category: string;
  content: string;
  createdAt: string;
  isPinned: boolean;
}

// 관리자용 목록
export interface AdminNotice {
  noticeId: number;
  title: string;
  category: string;
  isPinned: boolean;
  isDeleted: boolean;
  createdAt: string;
}
export interface AdminNoticeListResponse {
  success: boolean;
  notices: AdminNotice[];
  page: number;
  size: number;
  totalCount: number;
}

// 공통 성공 응답
export interface ApiResponse {
  success: boolean;
  message: string;
  errorCode?: string;
}

// 관리자용 작성
export interface AdminNoticeCreate {
  title: string;
  category: string;
  content: string;
}

// 관리자용 수정
export interface AdminNoticeUpdate {
  title: string;
  category: string;
  content: string;
  isPinned: boolean;
}

// 관리자용 삭제
export interface AdminNoticeDeleteResponse {
  success: boolean;
  message: string;
  deletedAt: string; // ISO 형식의 삭제 시간
}

// 관리자용 복원
export interface AdminNoticeRestoreResponse {
  success: boolean;
  message: string;
  isDeleted: boolean; // 복원된 후 false로 변경됨
}

// ─────────────────────────────
// 공지사항 API
// ─────────────────────────────

// 공지 목록
export const getNotices = async (
  page: number, 
  size: number = 10,
  keyword?: string
): Promise<NoticeListResponse> => {
  const res = await axios.get('/api/v1/notices', {
    params: { page, size, keyword },
    headers: {
      "Content-Type": "application/json",
    },
  }); 
  return res.data;
};

// 공지 상세
export const getNoticeDetail = async (noticeId: number): Promise<NoticeDetail> => {
  const res = await axios.get(`/api/v1/notices/${noticeId}`);

  if (res.data.success) {
    return res.data;
  } else {
    throw new Error(res.data.message || "공지사항을 불러오지 못했습니다.");
  }
};

// 공지 검색
export const getSearchedNotices = async (
  keyword: string,
  page: number = 1,
  size: number = 10
) => {
  const res = await axios.get("/api/v1/notices/search", {
    params: { keyword, page, size },
    headers: {
      "Content-Type": "application/json",
    },
  });

  return res.data;
};

// 관리자용 공지 목록
export const getAdminNotices = async ({
  page = 1,
  size = 10,
  isDeleted,
}: {
  page?: number;
  size?: number;
  isDeleted?: boolean;
}): Promise<AdminNoticeListResponse> => {
  const params: any = { page, size };
  if (isDeleted !== undefined) params.isDeleted = isDeleted;

  const res = await axiosInstance.get("/api/v1/admin/notices", { params });
  return res.data;
};

// 관리자용 공지 작성
export const createAdminNotice = async (
  newNotice: AdminNoticeCreate
): Promise<{ success: boolean; message: string; noticeId: number }> => {
  const res = await axiosInstance.post("/api/v1/admin/notices", newNotice, {
    headers: {
      "Content-Type": "application/json",
    },
  });
  return res.data;
};

// 관리자용 공지 수정
export const updateAdminNotice = async (
  noticeId: number,
  updatedNotice: AdminNoticeUpdate
): Promise<ApiResponse> => {
  const res = await axiosInstance.patch(
    `/api/v1/admin/notices/${noticeId}`,
    updatedNotice,
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
  return res.data;
};

// 관리자용 공지 삭제
export const deleteAdminNotice = async (
  noticeId: number
): Promise<AdminNoticeDeleteResponse> => {
  const res = await axiosInstance.patch(`/api/v1/admin/notices/${noticeId}/delete`);
  return res.data;
};

// 관리자용 공지 복원
export const restoreAdminNotice = async (
  noticeId: number
): Promise<AdminNoticeRestoreResponse> => {
  const res = await axiosInstance.patch(`/api/v1/admin/notices/${noticeId}/restore`);
  return res.data;
};