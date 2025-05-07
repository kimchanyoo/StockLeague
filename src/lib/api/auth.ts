import axiosInstance from "./axiosInstance";

export const postOAuthLogin = async ({
  provider,
  authCode,
}: {
  provider: "KAKAO" | "NAVER";
  authCode: string;
}) => {
  const res = await axiosInstance.post("/api/v1/auth/oauth/login", {
    provider,
    authCode,
  });

  return res.data;
};
