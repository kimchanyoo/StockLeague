"use client";

import { useEffect, useRef } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { postOAuthLogin } from "@/lib/api/auth";

type Provider = "KAKAO" | "NAVER";

export default function OAuthCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { setUser, setAccessToken } = useAuth();

  const hasRequestedRef = useRef(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (hasRequestedRef.current) return;
    hasRequestedRef.current = true;

    const authCode = searchParams.get("code");
    const provider = (searchParams.get("provider") as Provider) || "KAKAO";
    const redirectUri = `${process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI}`;

    if (!authCode) {
      alert("인가 코드가 없습니다.");
      router.push("/auth/login");
      return;
    }

    const loginWithSocial = async () => {
      try {
        const response = await postOAuthLogin({
          provider,
          authCode,
          redirectUri,
        });

        const { accessToken, refreshToken, isFirstLogin, nickname, role } = response;

        if (!accessToken) throw new Error("엑세스 토큰이 없습니다.");

        // 쿠키 저장
        document.cookie = `accessToken=${accessToken}; path=/;`;
        if (refreshToken) {
          document.cookie = `refreshToken=${refreshToken}; path=/;`;
        }

        // 상태 저장
        setUser({ nickname, role });

        // 리다이렉트
        const redirectPath = isFirstLogin
          ? `/auth/terms?accessToken=${accessToken}`
          : "/";
        router.push(redirectPath);
      } catch (error) {
        console.error("❌ OAuth 로그인 실패:", error);
        alert("로그인에 실패했습니다. 다시 시도해주세요.");
        router.push("/auth/login");
      }
    };

    loginWithSocial();
  }, [router, searchParams, setUser, setAccessToken]);

  return <p>로그인 처리 중입니다...</p>;
}
