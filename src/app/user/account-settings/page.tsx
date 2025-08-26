"use client";

import "./account-settings.css";
import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { withdrawUser, updateNickname } from "@/lib/api/auth";

export default function AccountSettings() {
  const { logout } = useAuth();
  const [newNickname, setNewNickname] = useState("");
  const [daysLeft, setDaysLeft] = useState(0);
  const [loading, setLoading] = useState(false);

  const handleLogout = async () => {
    await logout();
  }

  const handleNicknameChange = async () => {
    if (!newNickname.trim()) return alert("닉네임을 입력해주세요.");

    try {
      setLoading(true);
      const res = await updateNickname(newNickname.trim());

      if (!res.success) {
        setDaysLeft(res.daysLeft ?? 0);
        return alert(res.message);
      }

      alert(res.message);
      setDaysLeft(res.nextNicknameChangeAt 
        ? Math.ceil((new Date(res.nextNicknameChangeAt).getTime() - Date.now()) / (1000*60*60*24))
        : 0
      );
      setNewNickname("");
    } finally {
      setLoading(false);
    }
  };

  const handleWithdraw = async () => {
    const confirm = window.prompt("탈퇴를 진행하려면 '탈퇴합니다.'를 입력해주세요.");

    if (confirm !== "탈퇴합니다.") {
      alert("탈퇴 문구가 일치하지 않습니다.");
      return;
    }

    try {
      const res = await withdrawUser(confirm);
      alert(res.message); // ex: "회원 탈퇴가 완료되었습니다."
      await logout(); // context에 정의된 로그아웃 함수
      window.location.href = "/";
    } catch (error: any) {
      alert(error.response?.data?.message || "회원 탈퇴 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="container">
      <h1 className="title">
        계정 관리
      </h1>
      <div className="nickNameChange">
        <label>
          Stock<span className="highlight">League</span><br/>닉네임 <br/>
          <span>StockLeague 닉네임은 랭킹 및 커뮤니티에서 사용됩니다.</span>
        </label>
        <div className="btnGroup">
          <div className="nicknameInput">
            <input 
              type="text" 
              placeholder="새 닉네임" 
              value={newNickname} 
              onChange={(e) => setNewNickname(e.target.value)} 
              disabled={daysLeft > 0}
            />
            {daysLeft > 0 && (
              <p className="nicknameWarning">
                {daysLeft}일 뒤에 다시 변경할 수 있습니다.
              </p>
            )}
          </div>
          <button className="change" onClick={handleNicknameChange} disabled={loading || daysLeft > 0}>
            {loading ? "저장 중..." : "변경사항 저장"}
          </button>
        </div>
      </div>
      
      <div className="loginManagement">
        <label>
          로그인 관리<br/>
          <span>로그인 계정의 정보는 StockLeague의 필요한 서비스 외에 일절 사용되지 않습니다.</span>
        </label>
        <div className="btnGroup">
          <div className="loginInformation">
            <img src="/images/kakao.png" alt="카카오 로그인" className="logoImg"/>KaKao 계정
          </div>
          <button className="logout" onClick={handleLogout}>로그아웃</button>
        </div>
      </div>

      <div className="secession">
        <label>회원 탈퇴</label>
        <div className="btnGroup">
          <button className="secessionProgress" onClick={handleWithdraw}>탈퇴 진행</button>
        </div>
      </div>
    </div>
  );
}