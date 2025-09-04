# StockLeague — 실시간 주식 모의투자 플랫폼

개인 투자자 학습용으로 **실시간 시세/호가**를 수집하고 **주문 → 체결 → 자산 평가 → 랭킹**으로 이어지는 흐름을 제공하는 모의투자 웹 서비스입니다. KIS OpenAPI WebSocket으로 받은 데이터를 Redis에 저장·가공해 빠른 응답과 실시간성을 확보했습니다.

![결과물](https://github.com/user-attachments/assets/66d79969-fa9a-4082-a172-b1582d7a918a)
![결과물](https://github.com/user-attachments/assets/0f3fdd96-f265-42a4-a442-fb0c5398f520)

## 구조도

![아키텍처 설계](https://github.com/user-attachments/assets/d262c872-6d7f-4e6f-86fb-250c2b1da282)

## 개발 기간

* **2025년 3월 - 2025년 8월**

## 개발 목표

* 실시간 시세/호가 수집 및 차트·호가판 제공
* 주문 생성 → 매칭/체결(부분/전체) → 자산·평단가 반영
* 실시간 수익률/총자산 **랭킹** 및 **장마감 스냅샷** 유지

## 요약

학생/개인 투자자가 실제 투자 흐름을 안전하게 체험할 수 있도록 설계했습니다. KIS OpenAPI WebSocket으로 수집한 데이터를 Redis에 저장하고, 주문/체결 이벤트를 통해 사용자 잔고와 보유 주식을 갱신합니다. 장중에는 실시간 랭킹을, 장 마감 후에는 스냅샷 기준으로 고정된 랭킹을 제공합니다.

## 역할 및 기여도

* **프로젝트 팀 구성**: 2명

  * **프론트엔드 개발자**: 1명
  * **백엔드 개발자**: 1명
* **나의 역할**:

  * **팀장**
  * 서버 구축
  * REST API 및 WebSocket(STOMP) 개발
  * Redis 키스페이스·랭킹·스냅샷 설계
  * PostgreSQL 기반 데이터베이스 구축
  * **기여도**: 50%

## 사용한 툴 및 라이브러리

* **SpringBoot**: 백엔드 프레임워크
* **Redis**: 실시간 시세/랭킹/스냅샷 저장
* **PostgreSQL**: 데이터베이스 관리
* **KIS OpenAPI WebSocket**: 시세/호가 수집
* **Docker**: 컨테이너화 및 배포
* **Oracle Cloud**: 클라우드 인프라
* **JPA (Java Persistence API)**: ORM
* **Swagger/OpenAPI**: API 문서화
