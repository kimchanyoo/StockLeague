"use client";

import React from "react";
import "./login.css";

export default function Login() {
    const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID}&redirect_uri=${process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI}&response_type=code`;
    const NAVER_AUTH_URL = "https://nid.naver.com/oauth2.0/authorize?client_id=YOUR_NAVER_CLIENT_ID&redirect_uri=YOUR_REDIRECT_URI&response_type=code";

    console.log("🔗 KAKAO_AUTH_URL:", KAKAO_AUTH_URL);
    
    const handleLogin = () => {
        window.location.href = KAKAO_AUTH_URL;
    };

    return (
        <div className="container">
            <div className="loginContainer">
                <h2 className="loginTitle">소셜 로그인</h2>
                <h2 className="loginContent">
                    로그인된 정보는 오직 본 서비스 내에서만 사용되며,<br/>외부로 제공되지 않습니다.
                </h2>
                <div className="buttonContainer">
                    <a href={KAKAO_AUTH_URL} className="kakaoButton">
                        <img src="/images/kakao.png" alt="카카오 로그인" className="logoImg"/>
                        kakao 로그인
                    </a>
                    <a href={NAVER_AUTH_URL} className="naverButton">
                        <img src="/images/naver.png" alt="네이버 로그인" className="logoImg"/>
                        Naver 로그인
                    </a>
                </div>
            </div>
        </div>
    );
}
