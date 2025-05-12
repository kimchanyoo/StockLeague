"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { postOAuthLogin } from "@/lib/api/auth";

// 쿠키에서 값을 읽는 함수
const getCookie = (name: string) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(";").shift();
  return null;
};

type Provider = "KAKAO" | "NAVER"; // 추가할 소셜 로그인 제공자

export default function OAuthCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { setUser } = useAuth();

  useEffect(() => {
    const authCode = searchParams.get("code");
    const provider = (searchParams.get("provider") as Provider) || "KAKAO"; // 타입을 명시적으로 지정
    const redirectUri = `${process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI}`;

    const handleRedirect = (isFirstLogin: boolean, accessToken: string) => {
      router.push( 
        isFirstLogin ? `/auth/terms?accessToken=${accessToken}` : "/"
        
      );
    };

    if (!authCode) {
      alert("인가 코드가 없습니다.");
      router.push("/auth/login");
      return;
    }

    const loginWithSocial = async () => {
      try {
        // 서버 API 라우트를 통해 외부 API로 요청을 전달
        const response = await postOAuthLogin({
          provider,
          authCode,
          redirectUri,
        });
        // 응답 데이터 로그 출력
        console.log("로그인 응답:", response);  // 여기서 응답 전체를 출력합니다.

        // 응답 구조에 따라 accessToken, refreshToken 추출
        const { accessToken, refreshToken, isFirstLogin, nickname } = response;

        // accessToken, refreshToken을 쿠키에 저장
        if (accessToken) {
          document.cookie = `accessToken=${accessToken}; path=/; secure; HttpOnly;`;
        }
        if (refreshToken) {
          document.cookie = `refreshToken=${refreshToken}; path=/; secure; HttpOnly;`;
        }

        // 사용자 정보(닉네임 포함)를 상태 관리 또는 localStorage에 저장
        localStorage.setItem("nickname", nickname); // localStorage에 닉네임 저장
        setUser({ nickname });

        // 첫 로그인일 경우 약관 동의 페이지로 리디렉션, 아니면 홈 화면
        handleRedirect(isFirstLogin, accessToken);
      } catch (error: any) {
        console.error("OAuth 로그인 실패:", error);

        alert("로그인에 실패했습니다. 다시 시도해주세요.");
        router.push("/auth/login");
      }
    };

    loginWithSocial();
  }, [router, searchParams, setUser]);

  return <p>로그인 처리 중입니다...</p>;
}
