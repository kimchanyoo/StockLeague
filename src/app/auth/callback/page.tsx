"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { postOAuthLogin } from "@/lib/api/auth";

export default function OAuthCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { setTokens, setUser } = useAuth(); // 토큰을 관리하는 함수

  useEffect(() => {
    const authCode = searchParams.get("code");
    const provider = "KAKAO"; // 또는 naver

    if (!authCode) {
      alert("인가 코드가 없습니다.");
      router.push("/auth/login");
      return;
    }

    const loginWithSocial = async () => {
      try {
        // 서버 API 라우트를 통해 외부 API로 요청을 전달
        const response = await postOAuthLogin({ provider, authCode });

        const { accessToken, refreshToken, isFirstLogin, user } = response.data;

        // 받은 토큰을 상태에 저장
        setTokens(accessToken, refreshToken);

        // 사용자 정보(닉네임 포함)를 상태에 저장
        setUser(user);

        router.push(isFirstLogin ? "/auth/terms" : "/");
      } catch (error: any) {
        console.log("provider:", provider);
        console.log("authCode:", authCode);
        alert("로그인에 실패했습니다.");
        router.push("/auth/login");
      }
    };

    loginWithSocial();
  }, [router, searchParams, setTokens, setUser]);

  return <p>로그인 처리 중입니다...</p>;
}
