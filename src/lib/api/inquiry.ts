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

export interface InquiryCreateRequest {
  title: string;
  category: string;
  content: string;
}

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

// 문의 목록
export const getInquiries = async ( page: number, size: number = 10, status?: string ): Promise<InquiryListResponse> => {
  const token = getCookie("accessToken"); // 예: 쿠키에서 토큰 가져오기

  const res = await axiosInstance.get("/api/v1/inquiries", {
    params: { page, size, status },
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return res.data;
};

// 문의 작성
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

// 문의 상세
export const getInquiryDetail = async (inquiryId: number): Promise<InquiryDetailResponse> => {
  const token = getCookie("accessToken"); // 예: 쿠키에서 토큰 가져오기
  
  if (!token) {
    throw new Error("인증 토큰이 없습니다.");
  }

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

//문의 수정
export const updateInquiry = async (inquiryId: number, data: InquiryCreateRequest): Promise<{ success: boolean; message: string }> => {
  const token = getCookie("accessToken");

  if (!token) {
    throw new Error("인증 토큰이 없습니다.");
  }

  try {
    const res = await axiosInstance.patch(`/api/v1/inquiries/${inquiryId}`, data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    return {
      success: res.data.success,
      message: res.data.message,
    };
  } catch (error) {
    console.error("문의 수정 중 오류 발생:", error);
    throw new Error("문의 수정 중 문제가 발생했습니다.");
  }
};

//문의 삭제
export const deleteInquiry = async (inquiryId: number) => {
  const token = getCookie("accessToken");

  if (!token) {
    throw new Error("인증 토큰이 없습니다.");
  }

  try {
    const res = await axiosInstance.patch(`/api/v1/inquiries/${inquiryId}/delete`, {},
      {
        headers: {
        Authorization: `Bearer ${token}`,
        },
      }
    );

    return {
      success: res.data.success,
      message: res.data.message,
    };
  } catch (error) {
    console.error("문의 삭제 중 오류 발생:", error);
  }
};