import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { logout as logoutAPI } from "@/lib/api/auth";

// User 인터페이스 정의 (필요한 필드 추가 가능)
interface User {
  nickname: string;
  // 다른 사용자 정보가 있으면 여기에 추가
}

interface AuthContextType {
  user: User | null; // user 상태
  accessToken: string | null; // accessToken 상태 추가
  setUser: (user: User) => void;
  setAccessToken: (token: string) => void; // setAccessToken 함수 추가
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null); // user 상태
  const [accessToken, setAccessToken] = useState<string | null>(null); // accessToken 상태 추가
  const router = useRouter();
  
  // 쿠키에서 accessToken을 읽는 함수
  const getCookie = (name: string) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    const cookieValue = parts.pop()?.split(";").shift();
    console.log("Retrieved cookie value: ", cookieValue);  // 쿠키 값 확인
    return cookieValue;
  }
  return null;
  };

  // 로그인 시 accessToken 저장
  const setAccessTokenHandler = (token: string) => {
    setAccessToken(token);
    document.cookie = `accessToken=${token}; path=/;`;
  };

  // 컴포넌트가 처음 렌더링될 때 쿠키에서 accessToken을 읽어오기
  useEffect(() => {
    const storedNickname = localStorage.getItem("nickname");

    if (!storedNickname || storedNickname === "null") {
      setUser(null);
      setAccessToken(null); // 액세스 토큰 초기화
      return;
    }

    setUser({ nickname: storedNickname });

    const cookieToken = getCookie("accessToken");

    if (cookieToken) {
      setAccessToken(cookieToken); // 쿠키에서 가져온 accessToken을 상태에 설정
    }
  }, []);



  // 로그아웃 처리 함수
  const logout = async () => {
    try {
      // 백엔드 API에 로그아웃 요청
      if (user) {
        const response = await logoutAPI(user.nickname);  // 로그아웃 API 호출 (nickname 사용)

        // 응답 결과 확인
        if (response.success) {
          console.log("로그아웃 성공:", response.message);
        }
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
      alert("로그아웃 실패. 다시 시도해주세요.");
    }
  };

  return (
    <AuthContext.Provider value={{ user, setUser, accessToken, setAccessToken: setAccessTokenHandler, logout }}>
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
