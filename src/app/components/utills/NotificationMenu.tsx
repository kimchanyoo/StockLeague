"use client";
import { useState, useRef, useEffect } from "react";
import styles from "@/app/styles/components/utills/Notification.module.css";
import CloseIcon from '@mui/icons-material/Close';
import DoneIcon from '@mui/icons-material/Done';
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useNotification } from "@/context/NotificationContext";

const NotificationMenu = () => {
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const { 
    notifications, 
    unreadCount,  
    readNotification, 
    readAllNotifications,
    closeSingleNotification
  } = useNotification();
  
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className={styles.notificationMenu} ref={menuRef}>
      {/* 알림 아이콘 */}
      <div onClick={() => setOpen(!open)} className={styles.iconWrapper}>
        <NotificationsIcon fontSize="medium" />
        {unreadCount > 0 && (
          <span className={styles.badge}>{unreadCount}</span>
        )}
      </div>

      {open && (
        <div className={styles.dropdown}>
          {/* 전체 읽음 버튼 */}
          {notifications.length > 0 && (
            <div className={styles.readAllButton}>
              <button onClick={() => readAllNotifications()}>전체 읽음</button>
            </div>
          )}

          {/* 알림 목록 */}
          {notifications.length > 0 ? (
            notifications.map((n) => (
              <div key={n.notificationId} className={styles.item}>
                <span>{n.message}</span>

                {/* 읽음 버튼 */}
                {!n.isRead && (
                  <DoneIcon
                    className={styles.readIcon}
                    onClick={() => readNotification(n.notificationId)}
                    fontSize="small"
                  />
                )}

                {/* 닫기 버튼 */}
                <CloseIcon
                  className={styles.closeIcon}
                  onClick={() => closeSingleNotification(n.notificationId)}
                  fontSize="small"
                />
              </div>
            ))
          ) : (
            <div className={styles.empty}>알림이 없습니다.</div>
          )}
        </div>
      )}
    </div>
  );
};

export default NotificationMenu;