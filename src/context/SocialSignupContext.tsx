"use client";

import React, { createContext, useContext, useState } from "react";
import { useAuth } from "@/context/AuthContext"; // AuthContext 가져오기

type Role = "USER" | "ADMIN"; // 또는 enum
interface SocialSignupData {
  agreedToTerms: boolean;
  isOverFifteen: boolean;
  nickname: string;
  accessToken?: string; 
  role?: Role; // 추가
}

interface SocialSignupContextType {
  data: Partial<SocialSignupData>;
  setData: (data: Partial<SocialSignupData>) => void;
}

const SocialSignupContext = createContext<SocialSignupContextType | undefined>(undefined);

export const SocialSignupProvider = ({ children }: { children: React.ReactNode }) => {
  const [data, setState] = useState<Partial<SocialSignupData>>({});
  const { setAccessToken, setUser } = useAuth(); // AuthContext에서 setAccessToken과 setUser 가져오기

  const setData = (newData: Partial<SocialSignupData>) => {
    setState((prev) => ({ ...prev, ...newData }));
  };
  
  const handleLoginSuccess = (accessToken: string, nickname: string, role: Role = "USER") => {
    setData({
      accessToken,
      agreedToTerms: true,
      isOverFifteen: true,
      nickname,
      role,
    });
    // 로그인 후 AuthContext에 데이터 설정
    setAccessToken(accessToken); // AuthContext에 accessToken 설정
      setUser({ nickname, role });

  };
  
  return (
    <SocialSignupContext.Provider value={{ data, setData }}>
      {children}
    </SocialSignupContext.Provider>
  );
};

export const useSocialSignup = () => {
  const context = useContext(SocialSignupContext);
  if (!context) {
    throw new Error("useSocialSignup must be used within a SocialSignupProvider");
  }
  return context;
};
