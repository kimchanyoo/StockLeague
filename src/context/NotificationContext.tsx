"use client";
import { createContext, useContext, useEffect, useState } from "react";
import { connectStomp, disconnectStomp } from "@/lib/socket/socket";
import { useAuth } from "@/context/AuthContext";
import { 
  markNotificationRead, 
  closeNotification, 
  markAllNotificationsRead, 
  getNotifications, 
  getUnreadNotificationCount,
  Notification 
} from "@/lib/api/notification";

interface NotificationContextType {
  notifications: Notification[];
  unreadCount: number;  
  addNotification: (notification: Notification) => void;
  removeNotification: (notificationId: number) => void;
  readNotification: (id: number) => Promise<void>;
  closeSingleNotification: (id: number) => Promise<void>;
  readAllNotifications: (target?: string) => Promise<void>;
  refreshNotifications: () => Promise<void>;
  refreshUnreadCount: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider = ({ children }: { children: React.ReactNode }) => {
  const { user, loading, accessToken } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // 🔥 초기 알림 불러오기
  const refreshNotifications = async () => {
    try {
      const res = await getNotifications("all", 1, 20); 
      if (res.success) {
        setNotifications(res.content);
      }
    } catch (_err) {
      //console.error("알림 목록 조회 실패:", err);
    }
  };

  useEffect(() => {
    if (!loading && user && accessToken) {
      let isMounted = true;

      (async () => {
        try {
          // DB에서 기존 알림 가져오기
          await refreshNotifications();
          await refreshUnreadCount(); 
          // STOMP 연결
          await connectStomp(accessToken, (msg) => {
            if (!msg) {
              console.warn("⚠️ 메시지가 없습니다:", msg);
              return;
            }
            if (isMounted) {
              setNotifications((prev) => [msg, ...prev]);
              refreshUnreadCount();
            }
          });
        } catch (_error) {
          //console.error("STOMP 알림 연결 중 오류 발생:", error?.message || error, error);
        }
      })();

      return () => {
        isMounted = false;
        disconnectStomp();
      };
    }
    return () => {};
  }, [user, loading, accessToken]);

  // 상태 조작 함수들
  const addNotification = (notification: Notification) => {
    setNotifications((prev) => [notification, ...prev]);
  };

  const removeNotification = (notificationId: number) => {
    setNotifications((prev) => prev.filter((n) => n.notificationId !== notificationId));
  };

  const readNotification = async (id: number) => {
    try {
      const res = await markNotificationRead(id);
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) => (n.notificationId === id ? { ...n, isRead: true } : n))
        );
        await refreshUnreadCount();
      }
    } catch (_err) {
      //console.error("알림 읽음 처리 실패:", err);
    }
  };

  const closeSingleNotification = async (id: number) => {
    try {
      const res = await closeNotification(id);
      if (res.success) {
        removeNotification(id);
      }
    } catch (err) {
      //console.error("알림 닫기 실패:", err);
    }
  };

  const readAllNotifications = async (target?: string) => {
    try {
      const res = await markAllNotificationsRead(target);
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) =>
            target ? (n.target === target ? { ...n, isRead: true } : n) : { ...n, isRead: true }
          )
        );
        await refreshUnreadCount();
      }
    } catch (_err) {
      //console.error("전체 알림 읽음 처리 실패:", err);
    }
  };

  const refreshUnreadCount = async () => {
    try {
      const res = await getUnreadNotificationCount();
      if (res.success) {
        setUnreadCount(res.unreadCount);
      }
    } catch (_err) {
      //console.error("읽지 않은 알림 개수 조회 실패:", err);
    }
  };

  return (
    <NotificationContext.Provider value={{ 
        notifications,
        unreadCount,  
        addNotification,
        removeNotification,
        readNotification,
        closeSingleNotification,
        readAllNotifications,
        refreshNotifications,
        refreshUnreadCount,
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
