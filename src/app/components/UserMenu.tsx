"use client";

import React, { useEffect, useRef, useState } from "react";
import styles from "@/app/styles/components/UserMenu.module.css";

export default function UserMenu({ nickname }: { nickname: string }) {

  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className={styles.userMenu} ref={menuRef}>
      <div className={styles.nickname} onClick={() => setOpen(!open)}>

        {nickname}<span> 님 어서오세요 ▾</span>
      </div>
      {open && (
        <div className={styles.dropdown}>
          <a href="/user/account" className={styles.item}>내 계좌</a>
          <a href="/user/order-history" className={styles.item}>주문내역</a>
          <a href="/user/account-settings" className={styles.item}>계정관리</a>
          <a href="/logout" className={styles.item}>로그아웃</a>
        </div>
      )}
    </div>
  );
}
