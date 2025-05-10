import React, { useEffect } from "react";
import Link from "next/link";
import styles from "@/app/styles/components/Header.module.css";
import DropdownMenu from "./DropdownMenu";
import MobileMenu from "./MobileMenu";
import UserMenu from "./UserMenu";
import NotificationMenu from "./NotificationMenu";
import { useAuth } from "@/context/AuthContext";


const Header = () => {
  const { user, setUser } = useAuth(); // 사용자 정보를 가져옵니다.

  useEffect(() => {
    // 페이지 초기화 시, localStorage에서 사용자 정보를 가져오기
    const storedNickname = localStorage.getItem("nickname");
    if (storedNickname) {
      setUser({ nickname: storedNickname }); // localStorage에서 닉네임을 가져와 상태를 설정
    }
  }, [setUser]); // setUser가 변경될 때만 실행
  
  const isLoggedIn = !!user?.nickname;

  useEffect(() => {
      // user 상태 변경 시 헤더가 리렌더링되도록 설정
      console.log("로그인 상태가 변경되었습니다.", user);
    }, [user]);

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