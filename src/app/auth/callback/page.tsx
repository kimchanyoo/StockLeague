"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { postOAuthLogin } from "@/lib/api/auth";

type Provider = "KAKAO" | "NAVER"; // 추가할 소셜 로그인 제공자


export default function OAuthCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { setUser } = useAuth(); // setTokens는 이제 필요 없으므로 삭제

  useEffect(() => {
    const authCode = searchParams.get("code");
    const provider = (searchParams.get("provider") as Provider) || "KAKAO"; // 타입을 명시적으로 지정
    const redirectUri = `${process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI}`
    console.log("AuthCode:", authCode);
    console.log("Provider:", provider);
    console.log("Redirect URI:", redirectUri);
    if (!authCode) {
      alert("인가 코드가 없습니다.");
      router.push("/auth/login");
      return;
    }

    const loginWithSocial = async () => {
      try {
        // 서버 API 라우트를 통해 외부 API로 요청을 전달
        const response = await postOAuthLogin({ provider, authCode, redirectUri });

        const { accessToken, refreshToken, isFirstLogin, user } = response.data;

        // accessToken, refreshToken을 쿠키에 저장
        if (accessToken) {
          document.cookie = `accessToken=${accessToken}; path=/; secure;`;
        }

        if (refreshToken) {
          document.cookie = `refreshToken=${refreshToken}; path=/; secure;`;
        }

        // 사용자 정보(닉네임 포함) 저장
        setUser(user);

        // 첫 로그인일 경우 약관 동의 페이지로 리디렉션, 아니면 홈 화면
        router.push(isFirstLogin ? "/auth/terms" : "/");
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
