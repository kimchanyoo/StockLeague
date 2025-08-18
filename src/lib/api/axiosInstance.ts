// lib/api/axiosInstance.ts
import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "", // ← 필요시 설정
  withCredentials: true, // ✅ 쿠키 포함 필수
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 시 accessToken 주입
axiosInstance.interceptors.request.use(
  (config) => {
    if (typeof window !== "undefined") {
      const accessToken = localStorage.getItem("accessToken");
      if (accessToken) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${accessToken}`;
      }
    }
    return config;
  },
  (error) => {
    if (error.response?.status === 403) {
      // 아무것도 안 띄우고 무시
      return; // undefined 반환 → 호출한 쪽에서 아무 일도 안 일어남
    }
    return Promise.reject(error);
  }
);

// 응답 시 토큰 만료 처리
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // accessToken 만료 등으로 401 응답일 때 재발급 시도
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      typeof window !== "undefined"
    ) {
      originalRequest._retry = true;

      try {
        // 🔁 refreshToken은 쿠키로 자동 전송
        const refreshRes = await axios.post(
          "/api/v1/auth/token/refresh",
          {},
          { withCredentials: true }
        );

        const newAccessToken = refreshRes.data.accessToken;

        // 새 accessToken 저장 및 재시도
        localStorage.setItem("accessToken", newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        return axiosInstance(originalRequest); // 재요청
      } catch (refreshError: any) {
        console.error("🔒 refresh 실패:", refreshError.response?.data || refreshError);

        // 리프레시도 실패하면 로그아웃 처리
        localStorage.removeItem("accessToken");
        window.location.href = "/login"; // 또는 context.logout()
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
