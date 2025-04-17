"use client";

import "./success.css";
import NextButton from "@/app/components/NextButton";
import { useRouter } from "next/navigation";

export default function Success() {
    const router = useRouter();
    return (
        <div className="container">
            <div className="successContainer">
                <h1 className="successTitle">회원가입 완료</h1>
                <h1 className="successSubTitle">회원가입을 진심으로 환영합니다.<br/>StockLeague의 모든 서비스를 정상적으로 이용하실 수 있습니다.  </h1>
                <NextButton text="로그인 하러 가기" onClick={() => router.push("/auth/login")}/>
            </div>
        </div>
    );
}