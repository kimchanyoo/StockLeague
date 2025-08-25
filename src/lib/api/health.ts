import axiosInstance from "./axiosInstance";

export const checkApiHealth = async () => {
  try {
    const res = await axiosInstance.get("/api/v1/health/api");
    return res.data.healthy === true; 
  } catch {
    return false;
  }
};

export const checkDbHealth = async () => {
  try {
    const res = await axiosInstance.get("/api/v1/health/db");
    return res.data.healthy === true; 
  } catch {
    return false;
  }
};