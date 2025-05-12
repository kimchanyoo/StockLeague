"use client";

import "./nickname.css";
import NextButton from "@/app/components/NextButton";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { useSocialSignup } from "@/context/SocialSignupContext";
import { useAuth } from "@/context/AuthContext"; 
import { useSearchParams } from "next/navigation"; 
import axios from "axios";

export default function Nickname() {
  const { data, setData } = useSocialSignup(); // SocialSignupContext에서 데이터와 setData 가져오기
  const { setAccessToken, setUser } = useAuth(); // AuthContext에서 setAccessToken과 setUser 가져오기
  const [nickname, setNickname] = useState("");
  const [error, setError] = useState("");
  const [isChecking, setIsChecking] = useState(false);
  const [isAvailable, setIsAvailable] = useState<boolean | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams(); 
  const accessTokenFromQuery = searchParams.get("accessToken"); 

  useEffect(() => {
    if (accessTokenFromQuery && !data.accessToken) {
      setData({
        ...data,
        accessToken: accessTokenFromQuery,  // accessToken만 업데이트
      });
    }
  }, [accessTokenFromQuery, data, setData]);
  
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

      console.log(response.data);  // 응답 데이터 확인

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

 if (!data.accessToken) {
    setError("로그인 정보가 유효하지 않습니다.");
    console.error("accessToken이 없습니다.");  // 토큰이 없을 때 확인
    return;
  }  
    try {
      if (!data.accessToken) {
        console.error("accessToken is missing.");
        return;
      }

      console.log("Sending request with data:", {
        nickname,
        agreedToTerms: data.agreedToTerms,
        isOverFifteen: data.isOverFifteen,
      });

      const response = await axios.post(
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
          withCredentials: true, // 쿠키도 함께 전송하는 경우
        }
      );
      // 응답을 받은 후 로그
      console.log("Response received:", response);
      
      // 성공적으로 회원가입 완료 후 AuthContext에 사용자 정보 및 토큰 설정
      setAccessToken(data.accessToken || ""); // SocialSignupContext에서 받은 accessToken 저장
      setUser({ nickname }); // 사용자 정보 설정

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
