"use client";

import React, { createContext, useContext, useState } from "react";

interface SocialSignupData {
  agreedToTerms: boolean;
  isOverFifteen: boolean;
  nickname: string;
}

interface SocialSignupContextType {
  data: Partial<SocialSignupData>;
  setData: (data: Partial<SocialSignupData>) => void;
}

const SocialSignupContext = createContext<SocialSignupContextType | undefined>(undefined);

export const SocialSignupProvider = ({ children }: { children: React.ReactNode }) => {
  const [data, setState] = useState<Partial<SocialSignupData>>({});

  const setData = (newData: Partial<SocialSignupData>) => {
    setState((prev) => ({ ...prev, ...newData }));
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
