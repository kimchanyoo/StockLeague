import axiosInstance from "./axiosInstance";

export const getSearchedNotices = async (
  keyword: string,
  page: number = 1,
  size: number = 10
) => {
  const res = await axiosInstance.get("/api/v1/notices/search", {
    params: { keyword, page, size },
    headers: {
      "Content-Type": "application/json",
    },
  });

  return res.data.data;
};