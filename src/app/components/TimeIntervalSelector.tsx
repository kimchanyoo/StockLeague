import React, { useState, useEffect } from "react";

type TimeIntervalProps = {
  onIntervalChange: (interval: string) => void; // 부모 컴포넌트로 선택된 주기를 전달하는 함수
};

const TimeIntervalSelector: React.FC<TimeIntervalProps> = ({ onIntervalChange }) => {
  const [timeInterval, setTimeInterval] = useState<string>("1m");

  const handleIntervalChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedInterval = event.target.value;
    setTimeInterval(selectedInterval);
    onIntervalChange(selectedInterval); // 부모 컴포넌트로 주기 전달
  };

  return (
    <div>
      <select onChange={handleIntervalChange} value={timeInterval}  
      style={{
      padding: "6px 12px",
      borderRadius: "5px",
      border: "1px solid #999",
      }}>
        <option value="1m">1분</option>
        <option value="3m">3분</option>
        <option value="5m">5분</option>
        <option value="10m">10분</option>
        <option value="15m">15분</option>
        <option value="30m">30분</option>
        <option value="60m">60분</option>
        <option value="1d">1일</option>
        <option value="1w">1주</option>
        <option value="1M">1월</option>
        <option value="1y">1년</option>
      </select>
    </div>
  );
};

export default TimeIntervalSelector;
