import axiosInstance from "./axiosInstance";

// 로그인
export const postOAuthLogin = async ({
  provider,
  authCode,
  redirectUri,
}: {
  provider: "KAKAO" | "NAVER";
  authCode: string;
  redirectUri: string;
}) => {
  const res = await axiosInstance.post("/api/v1/auth/oauth/login", {
    provider,
    authCode,
    redirectUri,
  }, {
    withCredentials: true,
  });
  return res.data;
};

// 로그아웃
export const logout = async () => {
  const res = await axiosInstance.post("/api/v1/auth/logout", {
     withCredentials: true,
  });
  return res.data;
};

// 유저 정보 불러오기
export const fetchUserProfile = async () => {
  const res = await axiosInstance.get("/api/v1/user/profile", {
    withCredentials: true, // 쿠키로 accessToken 전달
  });
  return res.data; // { success, message, nickname }
};