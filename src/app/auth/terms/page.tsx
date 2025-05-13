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
            <NextButton 
              text="다음" 
              onClick={handleNextClick} 
              disabled={!isAgreed && isOverFifteen}
            />
        </div>
    </div>
  );
}