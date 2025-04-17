"use client";

import React from "react";
import styles from "@/app/styles/components/NextButton.module.css";

interface NextButtonProps {
  text: string;  // 버튼에 표시될 텍스트
  onClick?: () => void;  // 클릭 이벤트 핸들러 (필수 아님)
  className?: string;  // 추가 스타일 (선택)
  disabled?: boolean; // 비활성화 여부 추가
}

export default function NextButton({ text, onClick, className, disabled=false }: NextButtonProps) {
  return (
    <button 
    className={`${styles.button} ${className || ""}`} 
    onClick={onClick}
    disabled={disabled}
    >
      {text}
    </button>
  );
}