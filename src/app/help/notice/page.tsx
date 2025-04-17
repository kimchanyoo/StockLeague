"use client";

import "./notice.css";
import SearchIcon from "@mui/icons-material/Search";
import Link from 'next/link';


const mockNotices = [
  { id: '1', type: '공지', title: '4월 서버 점검 안내', date: '2025-04-10' },
  { id: '2', type: '업데이트', title: '신규 기능 출시 안내', date: '2025-04-05' },
];

export default function Notice() {
  
  return (
    <div className="container">
      <h1 className="title">공지사항</h1>
      <div className="search">
        <input type="text"/>
        <button><SearchIcon/></button>
      </div>
      <table className="notice-table">
        <thead>
          <tr>
            <th>번호</th>
            <th>구분</th>
            <th>제목</th>
            <th>등록일</th>
          </tr>
        </thead>

        <tbody>
          {mockNotices.map((notice, index) => (
            <tr key={notice.id}>
              <td>{mockNotices.length - index}</td>
              <td>{notice.type}</td>
              <td>
                <Link href={`/notice/${notice.id}`}>{notice.title}</Link>
              </td>
              <td>{notice.date}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}