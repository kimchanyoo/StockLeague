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
  handleFirstLoginSuccess: (token: string) => void;
  finalizeSignup: (nickname: string, role: Role) => void;
}

const SocialSignupContext = createContext<SocialSignupContextType | undefined>(undefined);

export const SocialSignupProvider = ({ children }: { children: React.ReactNode }) => {
  const [data, setState] = useState<Partial<SocialSignupData>>({});
  const { setUser, setTempAccessToken } = useAuth(); // AuthContext에서 setAccessToken과 setUser 가져오기

  const setData = (newData: Partial<SocialSignupData>) => {
    setState((prev) => ({ ...prev, ...newData }));
  };
  
  const handleFirstLoginSuccess = (token: string) => {
    setTempAccessToken(token);
  };
  
  const finalizeSignup = (nickname: string, role: Role) => {
    setData({
      nickname,
      agreedToTerms: true,
      isOverFifteen: true,
      role,
    });
    setUser({ nickname, role });
  };
  
  return (
    <SocialSignupContext.Provider value={{ data, setData, handleFirstLoginSuccess, finalizeSignup }}>
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
