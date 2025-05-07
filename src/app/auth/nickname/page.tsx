"use client";

import "./nickname.css";
import NextButton from "@/app/components/NextButton";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useSocialSignup } from "@/context/SocialSignupContext";
import { useAuth } from "@/context/AuthContext";
import axios from "axios";

export default function Nickname() {
  const { accessToken } = useAuth();
  const { data } = useSocialSignup();
  const [nickname, setNickname] = useState("");
  const [error, setError] = useState("");
  const [isChecking, setIsChecking] = useState(false);
  const [isAvailable, setIsAvailable] = useState<boolean | null>(null);
  const router = useRouter();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;

    if (value.length > 10) {
      setError("닉네임은 최대 10자까지 가능합니다.");
    } else {
      setError("");
      setNickname(value);
      setIsAvailable(null); // 닉네임이 바뀌면 중복 검사 결과 초기화
    }
  };

  const handleCheckDuplicate = async () => {
    if (!nickname) {
      setError("닉네임을 입력해주세요.");
      return;
    }

    try {
      setIsChecking(true);
      const response = await axios.get(`/api/v1/auth/check-nickname`, {
        params: { nickname },
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (response.data.available) {
        setIsAvailable(true);
        setError("");
      } else {
        setIsAvailable(false);
        setError("이미 사용 중인 닉네임입니다.");
      }
    } catch (err: any) {
      setIsAvailable(false);
      setError(err.response?.data?.message || "중복 검사 중 오류가 발생했습니다.");
    } finally {
      setIsChecking(false);
    }
  };

  const handleNext = async () => {
    if (!nickname) {
      setError("닉네임을 입력해주세요.");
      return;
    }

    if (nickname.length > 10) {
      setError("닉네임은 최대 10자까지 가능합니다.");
      return;
    }

    if (!isAvailable) {
      setError("중복 검사를 통과한 닉네임만 사용할 수 있습니다.");
      return;
    }

    try {
      await axios.post(
        "/api/v1/auth/oauth/complete",
        {
          nickname,
          agreedToTerms: data.agreedToTerms,
          isOverFifteen: data.isOverFifteen,
        },
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
        }
      );
      router.push("/auth/success");
    } catch (error) {
      console.error("회원가입 실패:", error);
      setError("회원가입 중 문제가 발생했습니다.");
    }
  };

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
            disabled={isChecking}
          >
            {isChecking ? "검사 중..." : "중복검사"}
          </button>
        </div>
        {isAvailable && <p className="success">사용 가능한 닉네임입니다.</p>}
        {error && <p className="error">{error}</p>}
        <NextButton
          text="회원가입"
          onClick={handleNext}
          disabled={!nickname || !!error || isAvailable !== true}
        />
      </div>
    </div>
  );
}
