"use client";

import { useState, useEffect } from "react";
import InquriyDropdown from "@/app/components/help/InquiryDropdown";
import { createInquiry, updateInquiry, getInquiryDetail } from "@/lib/api/inquiry"; 
import { useSearchParams, useRouter } from "next/navigation";
import "./write.css";

export default function Write() {
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState(""); // InquiryDropdown에서 선택되도록 연동 필요
  const [content, setContent] = useState("");

  const searchParams = useSearchParams();
  const router = useRouter();
  const inquiryId = searchParams.get("inquiryId"); 
  const isEdit = Boolean(inquiryId);

  
  useEffect(() => {
    if (isEdit && inquiryId) {
      getInquiryDetail(Number(inquiryId)).then((data) => {
        setTitle(data.title);
        setContent(data.content);
        setCategory(data.category);
      }).catch((err) => {
        alert("문의 내용을 불러오지 못했습니다.");
        console.error(err);
      });
    }
  }, [inquiryId, isEdit]);
  
  const handleSubmit = async () => {
    if (!title.trim() || !category.trim() || !content.trim()) {
      alert("모든 항목을 입력해주세요.");
      return;
    }

    try {
      if (isEdit && inquiryId) {
        const result = await updateInquiry(Number(inquiryId), { title, category, content });
        if (result.success) {
          alert("문의가 성공적으로 수정되었습니다.");
          router.push("/help/inquiry");
        } else {
          alert(`수정 실패: ${result.message}`);
        }
      } else {
        const result = await createInquiry({ title, category, content });
        if (result.success) {
          alert("문의가 성공적으로 등록되었습니다.");
          router.push("/help/inquiry");
        } else {
          alert(`등록 실패: ${result.message}`);
        }
      }
    } catch (error) {
      alert("오류가 발생했습니다. 다시 시도해주세요.");
      console.error(error);
    }
  };

  return (
    <div className="container">
      <h1 className="main-title">1:1 문의
        <span>{isEdit ? "문의 수정" : "문의 작성"}</span>
      </h1>
      
      <div className="write-title">
        <h1>제목</h1>
        <div className="write-category">
          <h1>문의유형</h1>
          <InquriyDropdown value={category} onSelect={setCategory}/>
        </div>
      </div>
      <input 
        className="title-input" 
        type="text" 
        value={title} 
        onChange={(e) => setTitle(e.target.value)} 
        placeholder="제목을 작성해주세요."
      />

      <h1 className="write-content">내용</h1>
      <textarea 
        className="content-input" 
        value={content} onChange={(e) => setContent(e.target.value)} 
        placeholder="내용을 작성해주세요."
      />

      <button className="write-button" onClick={handleSubmit}>
        {isEdit ? "수정 완료" : "작성 완료"}
      </button>
    </div>
  );
}