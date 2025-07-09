"use client";

import React, { useState, useEffect, useRef } from "react";
import styles from "@/app/styles/components/utills/MobileMenu.module.css";
import MenuIcon from "@mui/icons-material/Menu";
import CloseIcon from "@mui/icons-material/Close";

const MobileMenu = () => {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const toggleMenu = () => {
    setIsOpen((prev) => !prev);
  };

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden"; // 배경 스크롤 막기
    } else {
      document.body.style.overflow = "auto"; // 배경 스크롤 해제
    }

    return () => {
      document.body.style.overflow = "auto"; // 컴포넌트 언마운트 시 초기화
    };
  }, [isOpen]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        isOpen &&
        menuRef.current &&
        !menuRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isOpen]);

  return (
    <div className={styles.menuContainer}>
      <button onClick={toggleMenu} className={styles.menuButton}>
        <MenuIcon />
      </button>

      {/* 전체 메뉴 (모달) */}
      {isOpen && (
        <div className={styles.menuOverlay}>
          <div className={styles.menuContent} ref={menuRef}>
            <h1>전체 메뉴</h1>
            <button onClick={toggleMenu} className={styles.closeButton}>
              <CloseIcon />
            </button>
            <div className={styles.menu}>
              <div className={styles.menuTitle}>
                <h1>종목/거래</h1>
                <nav className={styles.menuLinks}>
                  <a href="/stocks/trade">거래소</a>
                  <a href="/stocks/stockkList">주식목록</a>
                </nav>
              </div>
              <div className={styles.menuTitle}>
                <h1>고객지원/이용안내</h1>
                <nav className={styles.menuLinks}>
                  <a href="/help/guide">이용안내</a>
                  <a href="/help/notice">공지사항</a>
                  <a href="/help/inquiry">1:1문의하기</a>
                  <a href="#">FAQ</a>
                </nav>
              </div>
              <div className={styles.menuTitle}>
                <h1>랭킹</h1>
                <nav className={styles.menuLinks}>
                  <a href="/rank">일일랭킹</a>
                </nav>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MobileMenu;