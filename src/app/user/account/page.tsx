"use client";

export default function Account() {
  return (
    <div className="container">
      <h1 className="title">
        이용안내
      </h1>
      <div className="leftSection">
        <div className="myMoney">
          <h2>나의 자산</h2>
        </div>
        <div className="portfolio">
          <h2>보유자산 포트폴리오</h2>
        </div>
      </div>
      <div className="rightSection">
        <div className="investStatus">
          <h2>투자 상태</h2>
        </div>
      </div>
    </div>
  );
}