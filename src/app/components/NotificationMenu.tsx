"use client";
import { useState, useRef, useEffect } from "react";
import styles from "@/app/styles/components/Notification.module.css";
import CloseIcon from '@mui/icons-material/Close';

export default function NotificationMenu() {
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // 더미 알림 데이터
  const [notifications, setNotifications] = useState([
    "삼성전자 주문 체결 완료!",
    "LG화학 주가가 5% 상승했어요",
    "오늘의 주식 뉴스가 도착했어요!",
  ]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);
  
  // 개별 알림 삭제
  const handleRemove = (index: number) => {
    setNotifications(prev => prev.filter((_, i) => i !== index));
  };

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
            notifications.map((msg, i) => (
              <div key={i} className={styles.item}>
                <span>{msg}</span>
                <CloseIcon
                  className={styles.closeIcon}
                  onClick={() => handleRemove(i)}
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
}
