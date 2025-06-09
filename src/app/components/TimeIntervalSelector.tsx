import React, { useState } from "react";
import { Interval } from '@/lib/api/stock';

type TimeIntervalProps = {
  onIntervalChange: (interval: Interval) => void;
};

const TimeIntervalSelector: React.FC<TimeIntervalProps> = ({ onIntervalChange }) => {
  const [timeInterval, setTimeInterval] = useState<Interval>("d");
  const minuteIntervals: Interval[] = ["1m", "3m", "5m", "10m", "15m", "30m", "60m"];
  const otherIntervals: { label: string; value: Interval }[] = [
    { label: "일", value: "d" },
    { label: "주", value: "w" },
    { label: "월", value: "m" },
    { label: "년", value: "y" },
  ];

  const handleChange = (val: Interval) => {
    setTimeInterval(val);
    onIntervalChange(val);
  };

  return (
    <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
      <select
        value={minuteIntervals.includes(timeInterval) ? timeInterval : ""}
        onChange={(e) => handleChange(e.target.value as Interval)}
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
          onClick={() => handleChange(value)}
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
