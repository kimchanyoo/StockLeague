import axiosInstance from "./axiosInstance"; // 인증 포함된 인스턴스

export interface Inquiry {
  inquiryId: number;
  userNickname: string;
  category: string;
  title: string;
  status: "WAITING" | "COMPLETE";
  createdAt: string;
  updatedAt: string;
}

export interface InquiryListResponse {
  success: boolean;
  inquiries: Inquiry[];
  page: number;
  size: number;
  totalCount: number;
}

// 쿠키에서 accessToken을 추출하는 함수
const getCookie = (name: string) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift();
  return null;
};

export const getInquiries = async ( page: number, size: number = 10, status?: string ): Promise<InquiryListResponse> => {
  const token = getCookie("accessToken"); // 예: 쿠키에서 토큰 가져오기

  const res = await axiosInstance.get("/api/v1/inquiries", {
    params: { page, size, status },
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return res.data;
}