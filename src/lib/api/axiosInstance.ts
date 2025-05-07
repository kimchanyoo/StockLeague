import axios from "axios";

const axiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL, // 환경변수로 API 주소 관리
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터 추가 (Authorization 헤더 자동 포함)
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken"); // 상태에서 가져오는 방식으로 변경 (예시로 localStorage 사용)
  
  if (token) {
    config.headers["Authorization"] = `Bearer ${token}`;
  }

  return config;
}, (error) => {
  return Promise.reject(error);
});

export default axiosInstance;
