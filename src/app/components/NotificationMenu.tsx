"use client";
import { useState, useRef, useEffect } from "react";
import styles from "@/app/styles/components/Notification.module.css";
import CloseIcon from '@mui/icons-material/Close';
import { useNotification } from "@/context/NotificationContext";

const NotificationMenu = () => {
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const { notifications, removeNotification } = useNotification();
  
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
      <div onClick={() => setOpen(!open)} className={styles.iconWrapper}>
        알림
        {notifications.length > 0 && (
          <span className={styles.badge}>{notifications.length}</span>
        )}
      </div>
      {open && (
        <div className={styles.dropdown}>
          {notifications.length > 0 ? (
            notifications.map((n) => (
              <div key={n.id} className={styles.item}>
                <span>{n.content}</span>
                <CloseIcon
                  className={styles.closeIcon}
                  onClick={() => removeNotification(n.id)}
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