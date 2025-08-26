import axiosInstance from "./axiosInstance";
import axios from "axios";
// 로그인
export const postOAuthLogin = async ({
  provider,
  authCode,
  redirectUri,
}: {
  provider: "KAKAO";
  authCode: string;
  redirectUri: string;
}) => {
  const res = await axios.post("/api/v1/auth/oauth/login", {
    provider,
    authCode,
    redirectUri,
  },
  
   {
    withCredentials: true,
  });
  return res.data;
};

// 로그아웃
export const logout = async () => {
  const res = await axiosInstance.post("/api/v1/auth/logout", {}, {
     withCredentials: true,
  });
  return res.data;
};

// 유저 정보 불러오기
export const fetchUserProfile = async () => {
  const res = await axiosInstance.get("/api/v1/user/profile", {
    withCredentials: true, 
  });
  return res.data; 
};

// 닉네임 수정 요청
export async function updateNickname(nickname: string) {
  try {
    const res = await axiosInstance.patch("/api/v1/user/profile", { nickname });
    return {
      success: true,
      message: res.data.message,
      nickname: res.data.nickname,
      nextNicknameChangeAt: res.data.nextNicknameChangeAt
    };
  } catch (err: any) {
    const res = err.response?.data;
    if (!res) throw err;

    switch (res.errorCode) {
      case "NICKNAME_CHANGE_NOT_ALLOWED":
        return {
          success: false,
          message: res.message,
          daysLeft: res.details.daysLeft
        };
      case "DUPLICATED_NICKNAME":
      case "NICKNAME_FORMAT_INVALID":
        return {
          success: false,
          message: res.message
        };
      default:
        throw err;
    }
  }
}

// 탈퇴
export const withdrawUser = async (confirmMessage: string) => {
  const res = await axiosInstance.delete("/api/v1/user/withdraw", {
    data: { confirmMessage },
  });
  return res.data;
};