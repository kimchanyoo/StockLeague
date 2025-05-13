"use client";
import React, { useState, useEffect } from "react";
import styles from "@/app/styles/components/InquriyDropdown.module.css";

interface Props {
  onSelect: (value: string) => void;
}

export const categories: Record<string, string> = {
  exchange: "거래소",
  ranking: "랭킹",
  bug: "버그 신고",
  etc: "기타",
};

const InquriyDropdown = ({ onSelect }: Props) => {
  const [category, setCategory] = useState('');

  useEffect(() => {
    onSelect(category); // 선택 변경 시 상위로 전달
  }, [category, onSelect]);

  return (
    <div className={styles.form_group}>
        <select
          id="category"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
        >
          <option value="">문의 유형 선택</option>
          {Object.entries(categories).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
    </div>
  );
};

export default InquriyDropdown;