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
  tempAccessToken?: string | null;
  loading: boolean; 
  setUser: (user: User) => void;
  setAccessToken: (token: string | null) => void; 
  setTempAccessToken: (token: string | null) => void; 
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | undefined>(undefined);
  const [accessToken, setAccessTokenState] = useState<string | null>(null);
  const [tempAccessToken, setTempAccessToken] = useState<string | null>(null); 
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  // accessToken 상태만 변경 (임시 토큰용, 쿠키에는 저장하지 않음)
  const setAccessToken = (token: string | null) => {
    setAccessTokenState(token);
  };

  // 초기 로드 시 유저 정보 및 쿠키에 저장된 토큰 불러오기
  useEffect(() => {
    const loadUser = async () => {
      try {
        const res = await fetchUserProfile(); // 쿠키 기반 인증
        if (res.success) {
          setUser({ nickname: res.nickname, role: res.role });
        } else {
          setUser(undefined);
        }
      } catch (error) {
        setUser(undefined);
      } finally {
        setLoading(false);
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
      // 상태 초기화
      setUser(undefined);
      setAccessTokenState(null);
      setTempAccessToken(null);

      router.push("/");
    } catch (error) {
      console.error("로그아웃 중 오류:", error);
      alert("로그아웃 실패. 다시 시도해주세요.");
    }
  };

  return (
    <AuthContext.Provider value={{ user, accessToken, tempAccessToken, loading, setTempAccessToken, setUser, setAccessToken, logout }}>
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
