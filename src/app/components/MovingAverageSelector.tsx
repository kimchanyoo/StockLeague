"use client";

import React from "react";

type Props = {
  selected: { [key: number]: boolean };
  onToggle: (period: number) => void;
};

const MovingAverageSelector: React.FC<Props> = ({ selected, onToggle }) => {
  const maOptions = [
    { label: "단기(5)", period: 5 },
    { label: "중기(20)", period: 20 },
    { label: "장기(60)", period: 60 },
  ];

  return (
    <div style={{ display: "flex", gap: "10px", marginBottom: "10px" }}>
      {maOptions.map(({ label, period }) => (
        <button
          key={period}
          onClick={() => onToggle(period)}
          style={{
            padding: "6px 12px",
            backgroundColor: selected[period] ? "#006ADD" : "#eee",
            color: selected[period] ? "white" : "black",
            border: "1px solid #ccc",
            borderRadius: "4px",
            cursor: "pointer",
          }}
        >
          {label}
        </button>
      ))}
    </div>
  );
};

export default MovingAverageSelector;
