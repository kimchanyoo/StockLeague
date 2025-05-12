import axiosInstance from "./axiosInstance";

interface InquiryCreateRequest {
  title: string;
  category: string;
  content: string;
}

// 쿠키에서 accessToken을 추출하는 함수
const getCookie = (name: string) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift();
  return null;
};

export const createInquiry = async ({
  title,
  category,
  content,
}: InquiryCreateRequest) => {
  const token = getCookie("accessToken"); // 쿠키에서 accessToken을 가져옵니다.

  if (!token) {
    throw new Error("인증 토큰이 없습니다.");
  }

   try {
    const res = await axiosInstance.post("/api/v1/inquiries",
      {
        title,
        category,
        content,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`, 
        },
      }
    );

    if (res.data.success) {
      return {
        success: true,
        inquiryId: res.data.inquiryId,
        message: res.data.message,
      };
    } else {
      return {
        success: false,
        message: res.data.message,
      };
    }
  } catch (error) {
    console.error("서버와의 통신 중 문제가 발생: ", error);
    throw new Error("서버와의 통신 중 문제가 발생했습니다.");
  }
};
