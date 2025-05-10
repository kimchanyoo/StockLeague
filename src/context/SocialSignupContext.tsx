"use client";

import React, { createContext, useContext, useState } from "react";
import { useAuth } from "@/context/AuthContext"; // AuthContext 가져오기

interface SocialSignupData {
  agreedToTerms: boolean;
  isOverFifteen: boolean;
  nickname: string;
  accessToken?: string; 
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
  
  const handleLoginSuccess = (accessToken: string) => {
    setData({
      accessToken,
      agreedToTerms: true, // 예시로 약관 동의 여부 설정
      isOverFifteen: true, // 예시로 15세 이상 여부 설정
      nickname: data.nickname || "", // 데이터에서 nickname을 가져옴
    });
    // 로그인 후 AuthContext에 데이터 설정
    setAccessToken(accessToken); // AuthContext에 accessToken 설정
    setUser({ nickname: data.nickname || "" }); // AuthContext에 사용자 정보 설정
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
