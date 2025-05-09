import axiosInstance from "./axiosInstance";

export const getNotices = async (page: number, size: number = 10) => {
  const res = await axiosInstance.get('/api/v1/notices', {
    params: { page, size },
    headers: {
      "Content-Type": "application/json",
    },
  }); 
  return res.data.data;
};
