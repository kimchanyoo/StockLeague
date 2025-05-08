import React, { createContext, useContext, useState, useEffect } from "react";
import { refreshAccessToken } from "@/lib/api/authService"; // 리프레시 토큰을 갱신하는 함수
import { useRouter } from "next/navigation";

// User 인터페이스 정의 (필요한 필드 추가 가능)
interface User {
  nickname: string;
  // 다른 사용자 정보가 있으면 여기에 추가
}

interface AuthContextType {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null; // ✅ 추가
  setTokens: (accessToken: string, refreshToken: string | null) => void;
  setUser: (user: User) => void; // setUser 함수 추가
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null); // user 상태
  const router = useRouter();

  const setTokens = (accessToken: string, refreshToken: string | null) => {
    setAccessToken(accessToken);
    setRefreshToken(refreshToken);
  };

  const logout = () => {
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
    router.push("/");
  };

  useEffect(() => {
    // 새로고침 시 리프레시 토큰으로 액세스 토큰을 갱신
    const getAccessTokenFromRefresh = async () => {
      if (refreshToken) {
        const newAccessToken = await refreshAccessToken(refreshToken);
        if (newAccessToken) {
          setAccessToken(newAccessToken);
        }
      }
    };
    
    getAccessTokenFromRefresh();
  }, [refreshToken]);

  return (
    <AuthContext.Provider value={{ accessToken, refreshToken, user, setTokens, setUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// Context 사용을 위한 커스텀 훅
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
