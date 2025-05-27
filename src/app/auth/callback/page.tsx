"use client";

import { useEffect, useRef } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { postOAuthLogin } from "@/lib/api/auth";

export default function OAuthCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { setUser } = useAuth();

  const hasRequestedRef = useRef(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (hasRequestedRef.current) return;
    hasRequestedRef.current = true;

    const authCode = searchParams.get("code");
    const provider = "KAKAO";
    const redirectUri = `${process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI}`;

    if (!authCode) {
      alert("인가 코드가 없습니다.");
      router.push("/auth/login");
      return;
    }

    const loginWithSocial = async () => {
    try {
      const response = await postOAuthLogin({ provider, authCode, redirectUri });
      const { isFirstLogin, tempAccessToken, nickname, role } = response;

      setUser({ nickname, role });

      if (isFirstLogin) {
        if (!tempAccessToken) {
          throw new Error("첫 로그인 시 임시 엑세스 토큰이 없습니다.");
        }
        router.push(`/auth/terms?accessToken=${tempAccessToken}`);
      } else {
        router.push("/");
      }
    } catch (error) {
      console.error("❌ OAuth 로그인 실패:", error);
      alert("로그인에 실패했습니다. 다시 시도해주세요.");
      router.push("/auth/login");
    }
  };

    loginWithSocial();
  }, [router, searchParams, setUser]);

  return <p style={{textAlign: "center"}}>로그인 처리 중입니다...</p>;
}
