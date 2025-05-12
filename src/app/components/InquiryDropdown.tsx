"use client";
import React, { useState, useEffect } from "react";
import styles from "@/app/styles/components/InquriyDropdown.module.css";

interface Props {
  onSelect: (value: string) => void;
}

const InquriyDropdown = ({ onSelect }: Props) => {
  const [category, setCategory] = useState('');

  const categories = [
    { value: "", label: "문의 유형 선택" }, // 기본값
    { value: "exchange", label: "거래소" },
    { value: "ranking", label: "랭킹" },
    { value: "bug", label: "버그 신고" },
    { value: "etc", label: "기타" },
  ];

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
          {categories.map((c) => (
            <option key={c.value} value={c.value}>
              {c.label}
            </option>
          ))}
        </select>
    </div>
  );
};

export default InquriyDropdown;