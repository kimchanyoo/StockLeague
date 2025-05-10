import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { logout as logoutAPI } from "@/lib/api/auth";

// User 인터페이스 정의 (필요한 필드 추가 가능)
interface User {
  nickname: string;
  // 다른 사용자 정보가 있으면 여기에 추가
}

interface AuthContextType {
  user: User | null; // ✅ user 상태
  setUser: (user: User) => void; // setUser 함수 추가
  logout: () => void; // 로그아웃 함수
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null); // user 상태
  const router = useRouter();
  
  // 로그아웃 처리 함수
  const logout = async () => {
    try {
      // 백엔드 API에 로그아웃 요청
      if (user) {
        await logoutAPI(user.nickname); // 로그아웃 API 호출 (refreshToken은 백엔드에서 처리)
      }
      // 쿠키에서 토큰 삭제
      document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/";
      document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/";
      // localStorage에서 사용자 정보 삭제
      localStorage.removeItem("nickname");
      
      setUser(null); // 상태에서 user 제거
      router.push("/"); // 홈 화면으로 리디렉션
    } catch (error) {
      console.error("로그아웃 중 오류가 발생했습니다:", error);
    }
  };

  // useEffect 훅을 사용하여 초기 상태에서 user 데이터를 설정할 수 있음
  useEffect(() => {
    const storedNickname = localStorage.getItem("nickname");
    if (storedNickname) {
      setUser({ nickname: storedNickname }); // localStorage에서 닉네임을 가져와서 user 상태 설정
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, setUser, logout }}>
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
