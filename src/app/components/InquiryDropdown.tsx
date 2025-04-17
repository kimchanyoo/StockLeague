"use client";
import React, {useState} from "react";
import styles from "@/app/styles/components/InquriyDropdown.module.css";

export default function InquriyDropdown() {

  const [category, setCategory] = useState('');

  const categories = [
    { value: "exchange", label: "거래소" },
    { value: "ranking", label: "랭킹" },
    { value: "bug", label: "버그 신고" },
    { value: "etc", label: "기타" },
  ];


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
}