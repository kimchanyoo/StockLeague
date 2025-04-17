"use client";

import "./terms.css";
import NextButton from "@/app/components/NextButton";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function Terms() {

  const router = useRouter();
  const [isAgreed, setIsAgreed] = useState(false);

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setIsAgreed(e.target.checked);
  };

  const handleNextClick = () => {
    if (isAgreed) {
      // 이동 또는 로직 수행
      router.push("/auth/nickname")
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
                onChange={handleCheckboxChange}
                className="mr-2"
              />
              <label htmlFor="agree">[필수] 약관에 동의합니다</label>
            </div>
            <NextButton text="다음" onClick={handleNextClick} disabled={!isAgreed}/>
        </div>
    </div>
  );
}