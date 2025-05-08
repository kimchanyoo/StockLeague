import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "", 
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터 추가 (Authorization 헤더 자동 포함)
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken"); 

  if (token) {
    config.headers["Authorization"] = `Bearer ${token}`;
  }

  return config;
}, (error) => {
  return Promise.reject(error);
});

export default axiosInstance;
