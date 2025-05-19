"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { logout as logoutAPI, fetchUserProfile } from "@/lib/api/auth";

type Role = "USER" | "ADMIN";

interface User {
  nickname: string;
  role: Role;
  // 필요시 추가 필드
}

interface AuthContextType {
  user: User | undefined;
  accessToken: string | null;
  setUser: (user: User) => void;
  setAccessToken: (token: string | null) => void; // null도 받을 수 있게 수정
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | undefined>(undefined);
  const [accessToken, setAccessTokenState] = useState<string | null>(null);
  const router = useRouter();

  // 쿠키에서 accessToken 읽기
  const getCookie = (name: string) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      return parts.pop()?.split(";").shift() ?? null;
    }
    return null;
  };

  // 쿠키에 accessToken 저장
  const saveAccessTokenToCookie = (token: string) => {
    setAccessTokenState(token);
    document.cookie = `accessToken=${token}; path=/;`;
  };

  // accessToken 상태만 변경 (임시 토큰용, 쿠키에는 저장하지 않음)
  const setAccessToken = (token: string | null) => {
    setAccessTokenState(token);
  };

  // 초기 로드 시 유저 정보 및 쿠키에 저장된 토큰 불러오기
  useEffect(() => {
    const loadUser = async () => {
      try {
        const cookieToken = getCookie("accessToken");
        if (!cookieToken) {
          setUser(undefined);
          setAccessTokenState(null);
          return;
        }

        // 토큰이 있을 때 유저 정보 요청
        const res = await fetchUserProfile();

        if (res.success) {
          setUser({ nickname: res.nickname, role: res.role });
          setAccessTokenState(cookieToken);
        } else {
          setUser(undefined);
          setAccessTokenState(null);
        }
      } catch (error) {
        console.error("유저 정보 불러오기 실패:", error);
        setUser(undefined);
        setAccessTokenState(null);
      }
    };

    loadUser();
  }, []);

  // 로그아웃 처리
  const logout = async () => {
    try {
      const response = await logoutAPI();

      if (!response.success) {
        throw new Error(response.message);
      }

      // 쿠키 삭제
      document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

      setUser(undefined);
      setAccessTokenState(null);

      router.push("/");
    } catch (error) {
      console.error("로그아웃 중 오류:", error);
      alert("로그아웃 실패. 다시 시도해주세요.");
    }
  };

  return (
    <AuthContext.Provider value={{ user, setUser, accessToken, setAccessToken, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
