"use client";

import { useRouter } from 'next/navigation';
import "./inquiry.css";
import Link from 'next/link';

const inquiries = [
  { id: '1', type: '랭킹', title: '랭킹 점수 반영 문제', date: '2025-04-10', status: 'pending' },
  { id: '2', type: '거래소', title: '거래소 거래 문제', date: '2025-04-05', status: 'completed' },
];

export default function Inquiry() {
  const router = useRouter();

  return (
    <div className="container">
      <h1 className="title">1:1 문의
        <span>StockLeague 서비스 이용 중 불편한 점이 있으신가요?</span>
      </h1>
      
      <div className="inquiry-list">
        {inquiries.map((item) => (
          <div className="inquiry-item" key={item.id} onClick={() => router.push(`/help/inquiry/${item.id}`)}>
            <div className="inquiry-info">
              <h2 className="inquiry-title">{item.title}</h2>
              <span className="inquiry-date">{item.date}</span>
            </div>
            <div className={`inquiry-status ${item.status === 'pending' ? 'status-pending' : 'status-completed'}`}>
              {item.status === 'pending' ? '답변전' : '답변완'}
            </div>
          </div>
        ))}
      </div>

      <Link href="/help/inquiry/write">
        <button className="inquiry-button">문의하기</button>
      </Link>
    </div>
  );
}