import axiosInstance from "./axiosInstance"; // 인증 포함된 인스턴스

// 사용자용
export interface Inquiry {
  inquiryId: number;
  userNickname: string;
  category: string;
  title: string;
  status: "WAITING" | "ANSWERED";
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
  answeredAt: string;
}
export interface InquiryDetailResponse {
  success: boolean;
  inquiryId: number;
  userNickname: string;
  title: string;
  category: string;
  content: string;
  status: "WAITING" | "ANSWERED";
  createdAt: string;
  updatedAt: string;
  answers: InquiryAnswer;
}

// 관리자용
export interface AdminInquiryAnswer {
  answerId: number;
  userId: number; 
  content: string;
  createdAt: string; 
}
export interface AdminInquiryDetailResponse {
  success: boolean;
  inquiryId: number;
  userId: string;      
  userNickname: string;
  title: string;
  category: string;
  content: string;
  status: "WAITING" | "ANSWERED";
  createdAt: string;
  updatedAt: string;
  answers?: AdminInquiryAnswer;
  message?: string;     
  errorCode?: string;   
}
export interface AdminInquiry {
  inquiryId: number;
  userNickname: string;
  category: string;
  title: string;
  status: "WAITING" | "ANSWERED";
  createdAt: string;
  updatedAt: string;
}
export interface AdminInquiryListResponse {
  success: boolean;
  inquiries: AdminInquiry[];
  page: number;
  size: number;
  totalCount: number;
  message?: string;   
  errorCode?: string; 
}
export interface AdminAnswerRequest {
  content: string;
}
export interface AdminAnswerResponse {
  success: boolean;
  message: string;
  answerId?: number;
  status?: "ANSWERED";
  errorCode?: string;
}

// ─────────────────────────────
// 문의 API
// ─────────────────────────────

// 문의 목록
export const getInquiries = async ( page: number, size: number = 10, status?: string ): Promise<InquiryListResponse> => {
  const res = await axiosInstance.get("/api/v1/inquiries", {
    params: { page, size, status },
  });
  return res.data;
};

// 문의 작성
export const createInquiry = async ({
  title,
  category,
  content,
}: InquiryCreateRequest) => {
  try {
    const res = await axiosInstance.post("/api/v1/inquiries", {
      title,
      category,
      content,
    });

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
    //console.error("서버와의 통신 중 문제가 발생: ", error);
    throw new Error("서버와의 통신 중 문제가 발생했습니다.");
  }
};

// 문의 상세
export const getInquiryDetail = async (inquiryId: number): Promise<InquiryDetailResponse> => {
  const res = await axiosInstance.get(`/api/v1/inquiries/${inquiryId}`);

  if (res.data.success) {
    return res.data;
  } else {
    throw new Error(res.data.message || "문의사항을 불러오지 못했습니다.");
  }
};

// 문의 수정
export const updateInquiry = async (inquiryId: number, data: InquiryCreateRequest) => {
  try {
    const res = await axiosInstance.patch(`/api/v1/inquiries/${inquiryId}`, data);

    return {
      success: res.data.success,
      message: res.data.message,
      errorCode: res.data.errorCode,
    };
  } catch (error: any) {
    const res = error.response?.data;
    return {
      success: false,
      message: res?.message || "문의 수정 중 문제가 발생했습니다.",
      errorCode: res?.errorCode,
    };
  }
};

// 문의 삭제
export const deleteInquiry = async (inquiryId: number) => {
  try {
    const res = await axiosInstance.patch(`/api/v1/inquiries/${inquiryId}/delete`, {});

    return {
      success: res.data.success,
      message: res.data.message,
      errorCode: res.data.errorCode,
    };
  } catch (error: any) {
    const res = error.response?.data;
    return {
      success: false,
      message: res?.message || "문의 삭제 중 문제가 발생했습니다.",
      errorCode: res?.errorCode,
    };
  }
};

// 관리자용 문의 조회
export const getAdminInquiries = async (
  page: number = 1,
  size: number = 10,
  status?: string
): Promise<AdminInquiryListResponse> => {
  try {
    const params: Record<string, any> = { page, size };
    if (status) params.status = status;

    const res = await axiosInstance.get("/api/v1/admin/inquiries", {
      params,
    });

    if (res.data.success) {
      return res.data as AdminInquiryListResponse;
    } else {
      throw new Error(res.data.message || "문의 내역을 불러오지 못했습니다.");
    }
  } catch (error: any) {
    //console.error("관리자 문의 목록 조회 중 오류 발생:", error);
    throw new Error(error.message || "서버와의 통신 중 문제가 발생했습니다.");
  }
};

// 관리자용 문의 상세 조회
export const getAdminInquiryDetail = async (
  inquiryId: number
): Promise<AdminInquiryDetailResponse> => {
  try {
    const res = await axiosInstance.get(`/api/v1/admin/inquiries/${inquiryId}`);

    if (res.data.success) {
      return res.data as AdminInquiryDetailResponse;
    } else {
      // 실패 케이스 (권한없음, 문의없음 등)
      throw new Error(res.data.message || "문의사항을 불러오지 못했습니다.");
    }
  } catch (error: any) {
    //console.error("관리자 문의 상세 조회 중 오류 발생:", error);
    // 에러 객체에 message가 없으면 기본 메시지 반환
    throw new Error(error.message || "서버와의 통신 중 문제가 발생했습니다.");
  }
};

// 관리자용 문의 답변
export const createInquiryAnswer = async (
  inquiryId: number,
  data: AdminAnswerRequest
): Promise<AdminAnswerResponse> => {
  try {
    const res = await axiosInstance.post(`/api/v1/admin/inquiries/${inquiryId}/answers`, data);

    if (res.data.success) {
      return res.data as AdminAnswerResponse;
    } else {
      throw new Error(res.data.message || "답변 등록에 실패했습니다.");
    }
  } catch (error: any) {
    //console.error("문의 답변 등록 중 오류 발생:", error);
    throw new Error(error.message || "서버와의 통신 중 문제가 발생했습니다.");
  }
};