"use client";
import { createContext, useContext, useEffect, useState } from "react";
import { connectStomp, disconnectStomp } from "@/lib/socket/socket";
import { useAuth } from "@/context/AuthContext";

export interface Notification {
  notificationId: number;
  type: string;
  message: string;
  target: string;
  targetId: number;
  isRead: boolean;
  createdAt: string;
}

interface NotificationContextType {
  notifications: Notification[];
  addNotification: (notification: Notification) => void;
  removeNotification: (notificationId: number) => void;
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

  const addNotification = (notification: Notification) => {
    setNotifications((prev) => [notification, ...prev]);
  };

  const removeNotification = (notificationId: number) => {
    setNotifications((prev) => prev.filter((n) => n.notificationId !== notificationId));
  };

  return (
    <NotificationContext.Provider value={{ notifications, addNotification, removeNotification }}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) throw new Error("useNotification must be used within NotificationProvider");
  return context;
};
