"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "axios";
import { useAuth } from "@/context/AuthContext";

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
        const response = await axios.post(
          `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/v1/auth/oauth/login`,
          {
            provider,
            authCode,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );

        const { accessToken, refreshToken, isFirstLogin, user } = response.data;

        // 받은 토큰을 상태에 저장
        setTokens(accessToken, refreshToken);

        // 사용자 정보(닉네임 포함)를 상태에 저장
        setUser(user);

        if (isFirstLogin) {
          router.push("/auth/terms"); // 최초 로그인
        } else {
          router.push("/"); // 일반 로그인
        }
      } catch (error: any) {
        alert("로그인에 실패했습니다.");
        router.push("/auth/login");
      }
    };

    loginWithSocial();
  }, [router, searchParams, setTokens, setUser]);

  return <p>로그인 처리 중입니다...</p>;
}
