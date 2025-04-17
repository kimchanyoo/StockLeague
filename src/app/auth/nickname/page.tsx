"use client";

import "./nickname.css";
import NextButton from "@/app/components/NextButton";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function Nickname() {

  const [nickname, setNickname] = useState("");
  const [error, setError] = useState("");
  const router = useRouter();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;

    if (value.length > 10) {
      setError("닉네임은 최대 10자까지 가능합니다.");
    } else {
      setError("");
      setNickname(value);
    }
  };

  const handleNext = () => {
    if (!nickname) {
      setError("닉네임을 입력해주세요.");
      return;
    }

    if (nickname.length > 10) {
      setError("닉네임은 최대 10자까지 가능합니다.");
      return;
    }

    // TODO: 중복 검사 API 통과했는지도 체크

    router.push("/auth/success");
  };

  return (
    <div className="container">
        <div className="nicknameContainer">
            <h1 className="nicknameTitle">
                StockLeague에 오신 것을 환영합니다.
                <span>서비스 내에서 사용하실 닉네임을 적어주세요.</span>
                </h1>
            <div className="nicknameContent">
                <input type="text" id="nickname" placeholder="닉네임을 입력해주세요" onChange={handleChange} maxLength={10}/>
                <button className="duplicateBtn">중복검사</button>
            </div>
            {error && <p className="error">{error}</p>}
            <NextButton text="회원가입" onClick={handleNext} disabled={!nickname || !!error}/>
        </div>
    </div>
  );
}