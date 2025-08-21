"use client";
import { createContext, useContext, useEffect, useState } from "react";
import { connectStomp, disconnectStomp } from "@/lib/socket/socket";
import { useAuth } from "@/context/AuthContext";
import { markNotificationRead, closeNotification, markAllNotificationsRead, Notification } from "@/lib/api/notification";

interface NotificationContextType {
  notifications: Notification[];
  addNotification: (notification: Notification) => void;
  removeNotification: (notificationId: number) => void;

  readNotification: (id: number) => Promise<void>;
  closeSingleNotification: (id: number) => Promise<void>;
  readAllNotifications: (target?: string) => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider = ({ children }: { children: React.ReactNode }) => {
  const { user, loading, accessToken } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    if (!loading && user && accessToken) {
      let isMounted = true;

      (async () => {
        try {
          await connectStomp(accessToken, (msg) => {
            if (!msg) {
              console.warn("⚠️ 메시지가 없습니다:", msg);
              return;
            }
            if (isMounted) {
              setNotifications((prev) => [msg, ...prev]);
            }
          });
        } catch (error: any) {
          console.error("STOMP 알림 연결 중 오류 발생:", error?.message || error, error);
        }
      })();

      return () => {
        isMounted = false;
        disconnectStomp();
      };
    }
    return () => {};
  }, [user, loading, accessToken]);

  // 상태 조작 함수
  const addNotification = (notification: Notification) => {
    setNotifications((prev) => [notification, ...prev]);
  };

  const removeNotification = (notificationId: number) => {
    setNotifications((prev) => prev.filter((n) => n.notificationId !== notificationId));
  };

   // ✅ 단건 읽음 처리
  const readNotification = async (id: number) => {
    try {
      const res = await markNotificationRead(id);
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) => (n.notificationId === id ? { ...n, isRead: true } : n))
        );
      }
    } catch (err) {
      console.error("알림 읽음 처리 실패:", err);
    }
  };

  // ✅ 단건 닫기
  const closeSingleNotification = async (id: number) => {
    try {
      const res = await closeNotification(id);
      if (res.success) {
        removeNotification(id);
      }
    } catch (err) {
      console.error("알림 닫기 실패:", err);
    }
  };

  // ✅ 전체 읽음 처리
  const readAllNotifications = async (target?: string) => {
    try {
      const res = await markAllNotificationsRead(target);
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) =>
            target ? (n.target === target ? { ...n, isRead: true } : n) : { ...n, isRead: true }
          )
        );
      }
    } catch (err) {
      console.error("전체 알림 읽음 처리 실패:", err);
    }
  };

  return (
    <NotificationContext.Provider value={{ 
        notifications,
        addNotification,
        removeNotification,
        readNotification,
        closeSingleNotification,
        readAllNotifications, 
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) throw new Error("useNotification must be used within NotificationProvider");
  return context;
};
