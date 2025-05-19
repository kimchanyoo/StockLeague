import React, { useState, useEffect } from "react";

type TimeIntervalProps = {
  onIntervalChange: (interval: string) => void; // 부모 컴포넌트로 선택된 주기를 전달하는 함수
};

const TimeIntervalSelector: React.FC<TimeIntervalProps> = ({ onIntervalChange }) => {
  const [timeInterval, setTimeInterval] = useState<string>("1m");

  const minuteIntervals = ["1m", "3m", "5m", "10m", "15m", "30m", "60m"];
  const otherIntervals = [
    { label: "일", value: "d" },
    { label: "주", value: "w" },
    { label: "월", value: "M" },
    { label: "년", value: "y" },
  ];

  const handleIntervalChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const val = e.target.value;
    setTimeInterval(val);
    onIntervalChange(val); // 부모 컴포넌트로 주기 전달
  };

  const handleButtonClick = (val: string) => {
    setTimeInterval(val);
    onIntervalChange(val);
  };


  return (
    <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
      <select
        onChange={handleIntervalChange}
        value={minuteIntervals.includes(timeInterval) ? timeInterval : ""}
        style={{
          padding: "6px 12px",
          borderRadius: "5px",
          border: "1px solid #999",
        }}
      >
        <option value="">분 단위</option>
        {minuteIntervals.map((interval) => (
          <option key={interval} value={interval}>
            {interval.replace("m", "분")}
          </option>
        ))}
      </select>

      {otherIntervals.map(({ label, value }) => (
        <button
          key={value}
          onClick={() => handleButtonClick(value)}
          style={{
            padding: "6px 12px",
            borderRadius: "5px",
            border: timeInterval === value ? "2px solid #333" : "1px solid #ccc",
            backgroundColor: timeInterval === value ? "#f0f0f0" : "#fff",
            cursor: "pointer",
          }}
        >
          {label}
        </button>
      ))}
    </div>
  );
};

export default TimeIntervalSelector;
