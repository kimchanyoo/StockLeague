import React from "react";
import styles from "@/app/styles/components/AdminHeader.module.css";
import UserMenu from "./UserMenu";
import { useAuth } from "@/context/AuthContext";

const AdminHeader = () => {
  // 임시 유저 (나중에 실제 로그인 정보로 대체)
  const { user } = useAuth(); // 사용자 정보를 가져옵니다.
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
            관리자 페이지
          </div>

          <div className={styles.rightHeader}>
            {isLoggedIn ? (
              <>
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