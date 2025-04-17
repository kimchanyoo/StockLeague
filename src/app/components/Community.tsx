"use client";

import React from "react";
import styles from "@/app/styles/components/Community.module.css";
import Comment from "./Comment";

export default function Community() {

    const exampleComments = [
        {
          id: 1,
          parentId: null,
          nickname: '김개발',
          content: '이거 정말 유익한 정보네요!',
          createdAt: '2025-04-16 12:30',
        },
        {
          id: 2,
          parentId: null,
          nickname: '코딩왕',
          content: '질문이 있는데, 이거 어디서 실행하나요?',
          createdAt: '2025-04-16 13:00',
        },
        {
          id: 3,
          parentId: 1,
          nickname: '프론트짱',
          content: '222 진짜 도움됐어요!',
          createdAt: '2025-04-16 13:10',
        },
        {
          id: 4,
          parentId: 2,
          nickname: '김개발',
          content: '저는 VSCode에서 했어요!',
          createdAt: '2025-04-16 13:15',
        },
     
      ];

      
    return (
        <div className={styles.container}>
            <h1>
                종목이름 커뮤니티
            </h1>
            
            <div className={styles.searchSection}>
                <input type="text"/>
                <button className={styles.mainBtn}>의견 남기기</button>
            </div>
            <div className={styles.commentSection}>
                <Comment comments={exampleComments}/>
            </div>
        </div>
    );
}