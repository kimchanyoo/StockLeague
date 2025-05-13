"use client";

import "./terms.css";
import NextButton from "@/app/components/NextButton";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { useSocialSignup } from "@/context/SocialSignupContext";

export default function Terms() {

  const router = useRouter();
  const [isAgreed, setIsAgreed] = useState(false);
  const [isOverFifteen, setIsOverFifteen] = useState(false);
  const { setData } = useSocialSignup();
  const searchParams = useSearchParams();

  const accessToken = searchParams.get("accessToken");

  const handleNextClick = () => {
     if (isAgreed && isOverFifteen) {
      if (!accessToken) {
        alert("임시 토큰이 누락되었습니다. 다시 로그인해주세요.");
        router.replace("/auth/login");
        return;
      }

      setData({
        accessToken,
        agreedToTerms: true,
        isOverFifteen: true,
      });

      // 💾 Optional: localStorage 백업
      localStorage.setItem("tempAccessToken", accessToken);

      router.push(`/auth/nickname?accessToken=${accessToken}`);
    } else {
      alert("모든 필수 항목에 동의해주세요.");
      
    }
  };

  return (
    <div className="container">
        <div className="termsContainer">
            <h2 className="termsTitle">약관동의</h2>
             <div className="termsContent">
              1. 서비스 이용약관
              <br />- 본 서비스는 회원에게 투자 시뮬레이션 및 관련 정보를 제공합니다.
              <br />- 회원은 타인의 정보를 도용하거나, 서비스를 부정한 목적으로 이용해서는 안 됩니다.
              <br />- 회사는 서비스 제공을 위해 필요한 경우 이용자의 일부 정보를 수집·이용할 수 있으며, 자세한 사항은 개인정보처리방침을 따릅니다.
              <br />- 회사는 사전 공지 없이 서비스의 일부를 수정, 중단할 수 있습니다.
              <br /><br />
              2. 개인정보 수집 및 이용
              <br />- 수집 항목: 닉네임, 이메일 주소, 투자 기록 등
              <br />- 수집 목적: 서비스 제공, 사용자 식별, 통계 분석
              <br />- 보유 기간: 회원 탈퇴 시 즉시 파기
            </div>
            <div className="agreeBox">
              <input
                type="checkbox"
                id="agree"
                checked={isAgreed}
                onChange={(e) => setIsAgreed(e.target.checked)}
                className="mr-2"
              />
              <label htmlFor="agree">[필수] 약관에 동의합니다.</label>
            </div>
            <div className="agreeBox">
              <input
                type="checkbox"
                id="agree"
                checked={isOverFifteen}
                onChange={(e) => setIsOverFifteen(e.target.checked)}
                className="mr-2"
              />
              <label htmlFor="agree">[필수] 만 14세 이상입니다.</label>
            </div>
            <NextButton text="다음" onClick={handleNextClick} disabled={!isAgreed && isOverFifteen}/>
        </div>
    </div>
  );
}