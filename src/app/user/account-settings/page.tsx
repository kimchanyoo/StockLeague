"use client";

import "./account-settings.css";

export default function AccountSettings() {
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
          <input type="text" placeholder="새 닉네임" />
          <button className="change">변경사항 저장</button>
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
          <button className="logout">로그아웃</button>
        </div>
      </div>

      <div className="secession">
        <label>회원 탈퇴</label>
        <div className="btnGroup">
          <button className="secessionProgress">탈퇴 진행</button>
        </div>
      </div>
    </div>
  );
}