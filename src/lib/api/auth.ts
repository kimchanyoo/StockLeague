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
    const res = await axiosInstance.patch("/api/v1/user/profile", {
      nickname,
    });
    return res.data; 
};

// 탈퇴
export const withdrawUser = async (confirmMessage: string) => {
  const res = await axiosInstance.delete("/api/v1/user/withdraw", {
    data: { confirmMessage },
  });
  return res.data;
};