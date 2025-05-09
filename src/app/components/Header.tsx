import React from "react";
import Link from "next/link";
import styles from "@/app/styles/components/Header.module.css";
import DropdownMenu from "./DropdownMenu";
import MobileMenu from "./MobileMenu";
import UserMenu from "./UserMenu";
import NotificationMenu from "./NotificationMenu";
import { useAuth } from "@/context/AuthContext";


const Header = () => {
  const { user } = useAuth(); // 사용자 정보를 가져옵니다.
  const isLoggedIn = !!user?.nickname;

    return (
      <header className={styles.header}>
        <div className={styles.headerInner}>

          <div className={styles.leftHeader}>
              <Link href="/" className={styles.logo}>
                  <div className={styles.stock}>STOCK</div>
                  <div className={styles.league}>League</div>
              </Link>
          </div>
          
          <div className={styles.centerHeader}>
            <DropdownMenu/>
          </div>

          <div className={styles.rightHeader}>
            {isLoggedIn ? (
              <>
                <NotificationMenu/>
                <UserMenu nickname={user!.nickname} />
              </>
            ) : (
              <>
                <Link href="/auth/login" className={styles.signIn}>로그인</Link>
                <Link href="/auth/login" className={styles.signUp}>회원가입</Link>
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