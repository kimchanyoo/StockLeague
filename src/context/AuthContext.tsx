import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { logout as logoutAPI, fetchUserProfile } from "@/lib/api/auth";

// User 인터페이스 정의 (필요한 필드 추가 가능)
interface User {
  nickname: string;
  role: "USER" | "ADMIN";
  // 다른 사용자 정보가 있으면 여기에 추가
}

interface AuthContextType {
  user: User | undefined; // user 상태
  accessToken: string | null; // accessToken 상태 추가
  setUser: (user: User) => void;
  setAccessToken: (token: string) => void; // setAccessToken 함수 추가
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | undefined>(undefined); // user 상태
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
    const loadUser = async () => {
      try {
        const res = await fetchUserProfile(); // API 호출

        if (res.success) {
          setUser({ nickname: res.nickname, role: res.role }); // role 설정
          const cookieToken = getCookie("accessToken");
          if (cookieToken) setAccessToken(cookieToken);
        } else {
          setUser(undefined);
          setAccessToken(null);
        }
      } catch (err) {
        console.error("유저 정보 불러오기 실패:", err);
        setUser(undefined);
        setAccessToken(null);
      }
    };

    loadUser();
  }, []);


  /// 로그아웃 처리 함수
const logout = async () => {
  try {
    const response = await logoutAPI(); // 인자 없이 호출

    if (response.success) {
      console.log("로그아웃 성공:", response.message);
    } else {
      throw new Error(response.message);
    }

    // 쿠키 삭제 (수동 삭제 필요하면)
    document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/";

    setUser(undefined);
    setAccessToken(null);
    router.push("/");
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
