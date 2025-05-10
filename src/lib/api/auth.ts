import axiosInstance from "./axiosInstance";

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

export const logout = async (refreshToken: string) => {
  const res = await axiosInstance.post("/api/v1/auth/logout", {
    refreshToken,
  });
  return res.data;
};