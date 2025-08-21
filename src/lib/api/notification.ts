import axiosInstance from "./axiosInstance";

export interface Notification {
  notificationId: number;
  type: string;
  message: string;
  target: string;
  targetId: number;
  isRead: boolean;
  createdAt: string;
}

// 단건 읽음 처리
export const markNotificationRead = async (id: number) => {
  const res = await axiosInstance.patch(`/api/v1/notifications/${id}/read`);
  return res.data as { success: boolean; notificationId: number; isRead: boolean };
};

// 단건 닫기(soft delete)
export const closeNotification = async (id: number) => {
  const res = await axiosInstance.patch(`/api/v1/notifications/${id}/close`);
  return res.data as { success: boolean; notificationId: number; isClosed: boolean };
};

// 전체 읽음 처리
export const markAllNotificationsRead = async (target?: string) => {
  const res = await axiosInstance.patch(`/api/v1/notifications/read-all`, undefined, {
    params: target ? { target } : undefined,
  });
  return res.data as { success: boolean; updatedCount: number };
};

// 알림 목록 조회
export const getNotifications = async (
  status: "unread" | "read" | "all" = "unread",
  page = 1,
  size = 10
) => {
  const res = await axiosInstance.get("/api/v1/notifications", {
    params: { status, page, size },
  });
  return res.data as {
    success: boolean;
    content: Notification[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
};

// 읽지 않은 알림 개수 조회
export const getUnreadNotificationCount = async () => {
  const res = await axiosInstance.get("/api/v1/notifications/unread-count");
  return res.data as { success: boolean; unreadCount: number };
};