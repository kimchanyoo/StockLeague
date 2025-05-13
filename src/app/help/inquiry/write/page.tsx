"use client";

import { useState } from "react";
import InquriyDropdown from "@/app/components/InquiryDropdown";
import { createInquiry } from "@/lib/api/inquiryCreate"; // 경로는 프로젝트에 맞게 수정
import { useRouter } from "next/navigation";
import "./write.css";

export default function Write() {
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState(""); // InquiryDropdown에서 선택되도록 연동 필요
  const [content, setContent] = useState("");
  const router = useRouter();

  const handleSubmit = async () => {
    if (!title || !category || !content) {
      alert("모든 항목을 입력해주세요.");
      return;
    }
     try {
      const result = await createInquiry({ title, category, content });
      if (result.success) {
        alert("문의가 성공적으로 등록되었습니다.");
        router.push("/help/inquiry");
      } else {
        alert(`실패: ${result.message}`);
      }
    } catch (error) {
      alert("오류가 발생했습니다. 다시 시도해주세요.");
      console.error(error);
    }
  };

  return (
    <div className="container">
      <h1 className="main-title">1:1 문의
        <span>문의 작성</span>
      </h1>
      
      <div className="write-title">
        <h1>제목</h1>
        <div className="write-category">
          <h1>문의유형</h1>
          <InquriyDropdown onSelect={setCategory}/>
        </div>
      </div>
      <input className="title-input" type="text" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="제목을 작성해주세요."/>

      <h1 className="write-content">내용</h1>
      <textarea className="content-input" value={content} onChange={(e) => setContent(e.target.value)} placeholder="내용을 작성해주세요."/>

      <button className="write-button" onClick={handleSubmit}>작성완료</button>
    </div>
  );
}