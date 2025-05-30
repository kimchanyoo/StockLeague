"use client";

import React, { createContext, useContext, useState } from "react";
import { useAuth } from "@/context/AuthContext";

type Role = "USER" | "ADMIN";

interface SocialSignupData {
  agreedToTerms: boolean;
  isOverFifteen: boolean;
  accessToken?: string;
  nickname: string;
  role: Role;
}

interface SocialSignupContextType {
  data: Partial<SocialSignupData>;
  setData: (data: Partial<SocialSignupData>) => void;
  finalizeSignup: (nickname: string, role: Role, accessToken: string) => void;
}

const SocialSignupContext = createContext<SocialSignupContextType | undefined>(undefined);

export const SocialSignupProvider = ({ children }: { children: React.ReactNode }) => {
  const [data, setState] = useState<Partial<SocialSignupData>>({});
  const { setUser, setAccessToken } = useAuth();

  const setData = (newData: Partial<SocialSignupData>) => {
    setState((prev) => ({ ...prev, ...newData }));
  };

  const finalizeSignup = (nickname: string, role: Role, accessToken: string) => {
    setData({
      nickname,
      agreedToTerms: true,
      isOverFifteen: true,
      role,
    });
    setUser({ nickname, role });
    setAccessToken(accessToken);
  };

  return (
    <SocialSignupContext.Provider value={{ data, setData, finalizeSignup }}>
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
