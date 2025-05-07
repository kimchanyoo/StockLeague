import axios from "axios";

// 리프레시 토큰을 사용하여 액세스 토큰을 갱신하는 함수
export const refreshAccessToken = async (refreshToken: string | null) => {
  try {
    const res = await axios.post(
      `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/v1/auth/refresh-token`,
      { refreshToken },
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    return res.data.accessToken; // 새로 받은 액세스 토큰 반환
  } catch (error) {
    console.error("리프레시 토큰 갱신 실패:", error);
    return null; // 실패 시 null 반환
  }
};
