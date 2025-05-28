"use client";
import React, { createContext, useContext, useEffect, useState } from "react";
import { connectStomp, disconnectStomp } from "@/lib/socket";
import { useAuth } from "@/context/AuthContext";

interface Notification {
  id: string;
  content: string;
}

interface NotificationContextType {
  notifications: Notification[];
  removeNotification: (id: string) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider = ({ children }: { children: React.ReactNode }) => {
  const { accessToken } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    if (!accessToken) return;

    disconnectStomp(); // 🔁 기존 연결 종료 후 재연결
    
    // WebSocket 연결
    connectStomp((msg) => {
      const notification = {
        id: crypto.randomUUID(), // 서버에서 id를 주면 msg.id
        content: msg.content || "새 알림이 도착했습니다!",
      };
      setNotifications((prev) => [notification, ...prev]);
    }, accessToken) // ✅ accessToken 전달
      .then(() => console.log("알림 WebSocket 연결 완료"))
      .catch((err) => console.error("알림 WebSocket 연결 실패:", err));

    return () => {
      disconnectStomp();
    };
  }, [accessToken]);

  const removeNotification = (id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  return (
    <NotificationContext.Provider value={{ notifications, removeNotification }}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) throw new Error("useNotification must be used within NotificationProvider");
  return context;
};
