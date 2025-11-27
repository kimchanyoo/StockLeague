import React from "react";
import styles from "@/app/styles/components/admin/AdminHeader.module.css";
import UserMenu from "../utills/UserMenu";
import { useAuth } from "@/context/AuthContext";
import NotificationMenu from "../utills/NotificationMenu";
import Link from "next/link";

const AdminHeader = () => {
  const { user } = useAuth(); // 사용자 정보를 가져옵니다.
  const isLoggedIn = !!user;
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
            관리자 페이지
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
          </div>
          
        </div>
      </header>
    );
  };

  export default AdminHeader;