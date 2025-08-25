"use client";

import { useEffect, useRef } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { postOAuthLogin } from "@/lib/api/auth";

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
        if (!response.success) throw new Error(response.message);

        const { isFirstLogin, accessToken, nickname, role } = response;

        if (!accessToken) {
          throw new Error("서버에서 accessToken을 받지 못했습니다.");
        }

        setAccessToken(accessToken);
        setUser({ nickname, role });

        if (isFirstLogin) {
          // 첫 로그인 시 추가 정보 입력 페이지로 이동
          router.push("/auth/terms");
        } else {
          router.push("/");
        }
      } catch (error) {
        //console.error("❌ OAuth 로그인 실패:", error);
        alert("로그인에 실패했습니다. 다시 시도해주세요.");
        router.push("/auth/login");
      }
    };

    loginWithSocial();
  }, [router, searchParams, setUser, setAccessToken]);

  return <p style={{ textAlign: "center" }}>로그인 처리 중입니다...</p>;
}
