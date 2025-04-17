"use client";

import InquriyDropdown from "@/app/components/InquiryDropdown";
import "./write.css";
import Link from 'next/link';

const inquiries = [
  { id: '1', type: '랭킹', title: '랭킹 점수 반영 문제', date: '2025-04-10', status: 'pending' },
  { id: '2', type: '거래소', title: '거래소 거래 문제', date: '2025-04-05', status: 'completed' },
];

export default function Write() {

  return (
    <div className="container">
      <h1 className="main-title">1:1 문의
        <span>문의 작성</span>
      </h1>
      
      <div className="write-title">
        <h1>제목</h1>
        <div className="write-category">
          <h1>문의유형</h1>
          <InquriyDropdown/>
        </div>
      </div>
      <input className="title-input" type="text" placeholder="제목을 작성해주세요."/>

      <h1 className="write-content">내용</h1>
      <textarea className="content-input" placeholder="내용을 작성해주세요."/>

      <button className="write-button">작성완료</button>
    </div>
  );
}