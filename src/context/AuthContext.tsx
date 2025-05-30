"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { logout as logoutAPI, fetchUserProfile } from "@/lib/api/auth";
import { connectStomp, disconnectStomp } from "@/lib/socket";

type Role = "USER" | "ADMIN";

interface User {
  nickname: string;
  role: Role;
}

interface AuthContextType {
  user: User | undefined;
  accessToken: string | null;
  loading: boolean;
  setUser: (user: User) => void;
  setAccessToken: (token: string | null) => void;
  logout: () => void;
  stompConnected: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | undefined>(undefined);
  const [accessToken, setAccessTokenState] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const [stompConnected, setStompConnected] = useState(false);

  // accessToken 상태와 localStorage 동기화 함수
  const setAccessToken = (token: string | null) => {
    if (token) {
      localStorage.setItem("accessToken", token);
    } else {
      localStorage.removeItem("accessToken");
    }
    setAccessTokenState(token);
  };

  // 초기 로드 시 localStorage에서 토큰 불러와 상태에 반영하고 프로필 요청
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem("accessToken");
      if (storedToken) {
        setAccessTokenState(storedToken);
        try {
          const profile = await fetchUserProfile();
          setUser(profile);
          // STOMP 연결
          await connectStomp(storedToken, (message) => {
            console.log("STOMP 메시지 수신:", message);
            // 메시지 처리 로직 추가
          });
          setStompConnected(true);
        } catch (error) {
          setAccessToken(null);
          setUser(undefined);
          setStompConnected(false);
        }
      } else {
        setStompConnected(false);
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  // 로그아웃 처리
  const logout = async () => {
    try {
      const response = await logoutAPI();

      if (!response.success) {
        throw new Error(response.message);
      }
      setUser(undefined);
      setAccessToken(null);
      await disconnectStomp();
      setStompConnected(false);

      router.push("/");
    } catch (error) {
      console.error("로그아웃 중 오류:", error);
      alert("로그아웃 실패. 다시 시도해주세요.");
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        loading,
        setUser,
        setAccessToken,
        logout,
        stompConnected,
      }}
    >
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
