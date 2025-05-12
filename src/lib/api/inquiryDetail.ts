import axiosInstance from "./axiosInstance";

export interface InquiryAnswer {
  answerId: number;
  userId: number;
  content: string;
  createdAt: string;
}

export interface InquiryDetailResponse {
  success: boolean;
  inquiryId: number;
  userNickname: string;
  title: string;
  category: string;
  content: string;
  status: "WAITING" | "ANSWERED" | "COMPLETE";
  createdAt: string;
  updatedAt: string;
  answer?: InquiryAnswer;
}

// 쿠키에서 accessToken을 추출하는 함수
const getCookie = (name: string) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift();
  return null;
};

export const getInquiryDetail = async (inquiryId: number): Promise<InquiryDetailResponse> => {
  const token = getCookie("accessToken"); // 예: 쿠키에서 토큰 가져오기

  const res = await axiosInstance.get(`/api/v1/inquiries/${inquiryId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (res.data.success) {
    return res.data;
  } else {
    throw new Error(res.data.message || "문의사항을 불러오지 못했습니다.");
  }
};