import React from "react";
import styles from "@/app/styles/components/Footer.module.css";

export default function Header() {
    return (
      <footer className={styles.footer}>
        <div className={styles.logo}>
            <div className={styles.stock}>STOCK</div>
            <div className={styles.league}>League</div>
        </div>
        <div className={styles.content}>
          <div className={styles.information}>
            <a href="/privacy">사업자정보</a>
            <a className="cursor-default">|</a>
            <a href="/privacy">개인정보처리방침</a>
            <a className="cursor-default">|</a>
            <a href="/terms">이용약관</a>
          </div>
          <p className="cursor-default">© 2025 Stock League. All rights reserved.</p>
        </div>
      </footer>
    );
  }