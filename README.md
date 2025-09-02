# StockLeague

## 📌 프로젝트 소개
**StockLeague**는 실시간 주식 데이터를 활용한 **모의투자 및 랭킹 서비스**입니다.  
주식 투자 경험이 없는 사람도 가상의 자산으로 투자 경험을 쌓을 수 있으며, 다른 사용자와 수익률을 비교하며 경쟁할 수 있습니다.  
단순한 종목 조회를 넘어 **실시간 호가창, 다양한 차트, 소셜 기능**까지 제공하여 실제 주식 시장과 유사한 환경을 구현했습니다.  

---

## 🚀 주요 기능
- **실시간 주식 데이터 반영**
  - WebSocket 기반 실시간 가격 및 호가 데이터 갱신  
  - 분봉(1분 ~ 60분), 일봉, 월봉, 연봉 차트 제공  

- **모의 투자 시스템**
  - 가상 자산으로 매수·매도 가능  
  - 보유 종목, 평가 금액, 수익, 수익률 자동 계산  

- **랭킹 시스템**
  - 수익률 기반 사용자 랭킹 제공  
  - 실시간 전체 랭킹 및 개인 순위 확인 가능  

- **소셜 기능**
  - 종목별 댓글 및 대댓글 작성  
  - 신고 및 관리 기능 제공  

- **관리자 페이지**
  - 공지사항 관리  
  - 문의/신고/댓글/사용자 관리  

---

## 🛠 기술 스택
- **Frontend**: Next.js (App Router), TypeScript, React Query, Recoil/Redux, TailwindCSS, Recharts, lightweight-charts  
- **Backend**: Spring Boot, JPA, Redis, WebSocket (STOMP)  
- **Database**: MySQL  
- **Infra**: AWS, Docker  

---

## 📂 프로젝트 구조
```bash
public/
  ├─ icons/                # 아이콘 리소스
  └─ images/               # 이미지 리소스

src/
  ├─ app/                  # Next.js App Router
  │   ├─ admin/            # 관리자 페이지
  │   │   ├─ inquiries/    # 문의 관리
  │   │   ├─ notices/      # 공지사항 관리
  │   │   └─ reports/      # 신고 관리
  │   ├─ auth/             # 인증 관련
  │   │   ├─ callback/
  │   │   ├─ login/
  │   │   ├─ nickname/
  │   │   ├─ success/
  │   │   └─ terms/
  │   ├─ components/       # UI 컴포넌트
  │   │   ├─ admin/
  │   │   ├─ help/
  │   │   ├─ stock/
  │   │   ├─ user/
  │   │   └─ utills/
  │   ├─ help/             # 고객센터/가이드
  │   │   ├─ guide/
  │   │   ├─ inquiry/
  │   │   │   ├─ write/
  │   │   │   └─ [id]/
  │   │   └─ notice/
  │   │       └─ [id]/
  │   ├─ rank/             # 랭킹 페이지
  │   ├─ stocks/           # 주식 관련
  │   │   └─ trade/
  │   ├─ styles/           # 컴포넌트 스타일
  │   │   └─ components/
  │   │       ├─ admin/
  │   │       ├─ help/
  │   │       ├─ stock/
  │   │       ├─ user/
  │   │       └─ utills/
  │   ├─ test-socket/      # 소켓 테스트
  │   └─ user/             # 사용자 관련
  │       ├─ account/
  │       ├─ account-settings/
  │       └─ order-history/
  │
  ├─ context/              # React Context (AuthContext 등)
  ├─ hooks/                # 커스텀 훅
  ├─ lib/                  # API & 소켓 관련 유틸
  │   ├─ api/              # API 요청 함수
  │   └─ socket/           # 소켓 클라이언트
  └─ socketHooks/          # WebSocket 관련 커스텀 훅
