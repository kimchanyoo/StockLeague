"use client";

import "./nickname.css";
import NextButton from "@/app/components/utills/NextButton";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, useEffect } from "react";
import { useSocialSignup } from "@/context/SocialSignupContext";
import { useMutation } from "@tanstack/react-query";
import axios from "axios";

export default function Nickname() {
  const { data, setData, finalizeSignup } = useSocialSignup();
  const [nickname, setNickname] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isAvailable, setIsAvailable] = useState<boolean | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const accessTokenFromQuery = searchParams.get("accessToken");
  
  // accessToken 쿼리로부터 초기 설정
  useEffect(() => {
    if (accessTokenFromQuery && !data.accessToken) {
      setData({ accessToken: accessTokenFromQuery });
    }
  }, [accessTokenFromQuery, data.accessToken, setData]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value.length > 10) {
      setError("닉네임은 최대 10자까지 가능합니다.");
      setIsAvailable(null);
    } else {
      setError(null);
      setNickname(value);
      setIsAvailable(null); // 중복 검사 결과 초기화
    }
  };

  // 닉네임 중복 검사
  const duplicateMutation = useMutation<
    { available: boolean }, // 성공 반환 타입
    any,                    // 에러 타입
    string                  // mutate 파라미터
  >(
    async (nickname: string) => {
      const res = await axios.get("/api/v1/auth/check-nickname", {
        params: { nickname },
        headers: { "Content-Type": "application/json" },
      });
      return res.data;
    },
    {
      onSuccess: (res) => {
        if (res.available) {
          setIsAvailable(true);
          setError(null);
        } else {
          setIsAvailable(false);
          setError("이미 사용 중인 닉네임입니다.");
        }
      },
      onError: (err) => {
        setIsAvailable(false);
        setError(err.response?.data?.message || "중복 검사 중 오류가 발생했습니다.");
      },
    }
  );

  const handleCheckDuplicate = () => {
    if (!nickname) {
      setError("닉네임을 입력해주세요.");
      return;
    }
    duplicateMutation.mutate(nickname);
  };

  // 회원가입 완료
  const signupMutation = useMutation<
    any, // 성공 반환 타입
    any, // 에러 타입
    void // mutate 파라미터
  >(
    async () => {
      const res = await axios.post(
        "/api/v1/auth/oauth/complete",
        {
          nickname,
          agreedToTerms: data.agreedToTerms,
          isOverFifteen: data.isOverFifteen,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${data.accessToken}`,
          },
          withCredentials: true,
        }
      );
      return res.data;
    },
    {
      onSuccess: (res) => {
        finalizeSignup(nickname, "USER", data.accessToken!);
        router.push("/auth/success");
      },
      onError: () => setError("회원가입 중 문제가 발생했습니다."),
    }
  );

  const handleNext = () => {
    if (!nickname) return setError("닉네임을 입력해주세요.");
    if (nickname.length > 10) return setError("닉네임은 최대 10자까지 가능합니다.");
    if (isAvailable !== true) return setError("중복 검사를 통과한 닉네임만 사용할 수 있습니다.");
    if (!data.accessToken) return setError("로그인 정보가 유효하지 않습니다.");

    signupMutation.mutate();
  };

  const isLoading = duplicateMutation.isLoading || signupMutation.isLoading;

  return (
    <div className="container">
      <div className="nicknameContainer">
        <h1 className="nicknameTitle">
          StockLeague에 오신 것을 환영합니다.
          <span>서비스 내에서 사용하실 닉네임을 적어주세요.</span>
        </h1>
        <div className="nicknameContent">
          <input
            type="text"
            id="nickname"
            placeholder="닉네임을 입력해주세요"
            onChange={handleChange}
            maxLength={10}
            value={nickname}
          />
          <button
            className="duplicateBtn"
            onClick={handleCheckDuplicate}
            disabled={duplicateMutation.isLoading || signupMutation.isLoading}
          >
            {duplicateMutation.isLoading ? "검사 중..." : "중복검사"}
          </button>
        </div>
        {isAvailable && <p className="success">사용 가능한 닉네임입니다.</p>}
        {error && <p className="error">{error}</p>}
        <NextButton
          text="회원가입"
          onClick={handleNext}
          disabled={!nickname || !!error || isAvailable !== true || isLoading}
        />
      </div>
    </div>
  );
}
