import { useEffect, useState } from "react";
import { Notification } from "@/lib/api/notification";
import { connectStomp, disconnectStomp } from "@/lib/socket/socket";
import { markNotificationRead, closeNotification, getNotifications } from "@/lib/api/notification";

export const useNotification = (accessToken: string | null) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  // 초기 알림 조회
  const fetchNotifications = async () => {
    if (!accessToken) return;
    try {
      const res = await getNotifications("unread", 1, 20);
      setNotifications(res.content);
    } catch (err) {
      console.error("알림 조회 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!accessToken) return;

    fetchNotifications();

    connectStomp(accessToken, (payload: any) => {
      try {
        const notification: Notification = payload;
        setNotifications((prev) => [notification, ...prev]);
      } catch (err) {
        console.error("실시간 알림 파싱 실패:", err, payload);
      }
    });

    return () => {
      disconnectStomp();
    };
  }, [accessToken]);

  // 단건 읽음 처리
  const readNotification = async (id: number) => {
    try {
      const res = await markNotificationRead(id);
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) => (n.notificationId === id ? { ...n, isRead: true } : n))
        );
      }
    } catch (err) {
      console.error("읽음 처리 실패:", err);
    }
  };

  // 단건 닫기
  const closeSingleNotification = async (id: number) => {
    try {
      const res = await closeNotification(id);
      if (res.success) {
        setNotifications((prev) => prev.filter((n) => n.notificationId !== id));
      }
    } catch (err) {
      console.error("알림 닫기 실패:", err);
    }
  };

  return {
    notifications,
    loading,
    readNotification,
    closeSingleNotification,
  };
};
