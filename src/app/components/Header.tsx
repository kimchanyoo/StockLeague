import React from "react";
import styles from "@/app/styles/components/Header.module.css";
import DropdownMenu from "./DropdownMenu";
import MobileMenu from "./MobileMenu";
import UserMenu from "./UserMenu";
import NotificationMenu from "./NotificationMenu";

const Header = () => {
  // 임시 유저 (나중에 실제 로그인 정보로 대체)
  const user = {
    nickname: "닉네임",
  };
  const isLoggedIn = !!user;
    return (
      <header className={styles.header}>
        <div className={styles.headerInner}>

          <div className={styles.leftHeader}>
              <a href="/" className={styles.logo}>
                  <div className={styles.stock}>STOCK</div>
                  <div className={styles.league}>League</div>
              </a>
          </div>
          
          <div className={styles.centerHeader}>
            <DropdownMenu/>
          </div>

          <div className={styles.rightHeader}>
            {isLoggedIn ? (
              <>
                <NotificationMenu/>
                <UserMenu nickname={user.nickname} />
              </>
            ) : (
              <>
                <a href="/auth/login" className={styles.signIn}>로그인</a>
                <a href="/auth/login" className={styles.signUp}>회원가입</a>
              </>
            )}
              <div className={styles.menuToggle}>
                  <MobileMenu />
              </div>
          </div>
          
        </div>
      </header>
    );
  };

  export default Header;