# StockLeague — 실시간 주식 모의투자 플랫폼
<span style="color: #868382">**2025.03 ~ 2025.08(6개월)**</span>

개인 투자자 학습용으로 **실시간 시세/호가**를 수집하고 **주문 → 체결 → 자산 평가 → 랭킹**으로 이어지는 흐름을 제공하는 모의투자 웹 서비스입니다. KIS OpenAPI WebSocket으로 받은 데이터를 Redis에 저장·가공해 빠른 응답과 실시간성을 확보했습니다.

## 결과물
![결과물](https://github.com/user-attachments/assets/66d79969-fa9a-4082-a172-b1582d7a918a)
![결과물](https://github.com/user-attachments/assets/0f3fdd96-f265-42a4-a442-fb0c5398f520)

## 시스템 아키텍처

![아키텍처 설계](https://github.com/user-attachments/assets/d262c872-6d7f-4e6f-86fb-250c2b1da282)

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
* **나의 역할**
  * **팀장**
  * 서버 구축
  * REST API 및 WebSocket(STOMP) 개발
  * Redis 키스페이스·랭킹·스냅샷 설계
  * PostgreSQL 기반 데이터베이스 구축
  * **기여도**: 50%

## 기술 스택

* **SpringBoot**: 백엔드 프레임워크
* **Redis**: 실시간 시세/랭킹/스냅샷 저장
* **PostgreSQL**: 데이터베이스 관리
* **KIS OpenAPI WebSocket**: 시세/호가 수집
* **Docker**: 컨테이너화 및 배포
* **Oracle Cloud**: 클라우드 인프라
* **Swagger**: API 문서화

## 문제 해결 및 고민한 점

> 운영 중 겪은 핵심 이슈와 해결 과정, 그리고 배운 점을 정리했습니다.

- [1) 주식 과체결 문제](#1-주식-과체결-문제)
- [2) DB 컨테이너 기동 불안정](#2-db-컨테이너-기동-불안정)
- [3) 개인 웹소켓 연결 오류](#3-개인-웹소켓-연결-오류)

---

### 1) 주식 과체결 문제

**문제 상황**  
k6로 100명의 이용자가 동일 호가에 체결 요청 테스트를 했을 때 **과체결이 발생**했습니다.  
<img width="592" height="169" alt="Image" src="https://github.com/user-attachments/assets/55561227-9dee-463e-a39a-2d76cafd502e" />

**원인**  
개별 주문을 **즉시 체결**하는 구조여서, Redis 내에서 **체결 가능 수량 대비 현재까지의 누적 요청(예상 체결량)** 을 추적할 수 없었습니다.

**해결 방법**
1. **즉시 체결 제거 → 주문 대기열(ZSET)로 일원화**  
   모든 주문을 Redis **주문 대기열(ZSET)** 에 저장하고, **스케줄러(300ms 주기)** 로 확인·처리하도록 변경했습니다.  
   - 초기 배치 1000ms → 호가 갱신 주기와 겹쳐 지연 → **300ms로 조정**
2. **Redis Lua 스크립트로 체결량 원자 추적**  
   - DB 락/`synchronized` 를 고려했으나, Redis 기반 호가 정보를 DB로 옮기는 추가 비용이 큼  
   - Lua 내부에서 **`호가 수량 - 요청 누적량`** 을 실시간 비교하여 **과체결 방지**  
   - 호가 정보만으로 부족해 **호가 스냅샷**을 생성·활용

**결과**  
동일 환경 재테스트에서 **과체결 0건**.  
<img width="667" height="220" alt="Image" src="https://github.com/user-attachments/assets/9820fb2e-f660-41f4-bbdc-1f60c820bcaf" />

**배운 점**
- 실시간 시스템은 **동시성 제어/락 전략**을 설계의 최우선 고려로 둬야 합니다.
- Redis **Lua**로 **비즈니스 로직을 원자 단위 트랜잭션처럼** 구현할 수 있습니다.

---

### 2) DB 컨테이너 기동 불안정

**문제 상황**  
Docker Compose로 **MySQL 컨테이너가 간헐적으로 기동 실패/재시작**했습니다.  
`docker logs`에는 **Permission denied**, **Received SHUTDOWN** 메시지가 확인되었습니다.

**원인**
- DB 데이터는 **Named Volume** 사용, 디렉터리 소유권이 `lxd:999`
- MySQL 공식 이미지가 `/var/lib/mysql`에 **`chown -R 999:999`** 수행  
  → Ubuntu **LXD/유저 네임스페이스** 정책과 충돌하여, 컨테이너 내부 root가 호스트 파일시스템 **소유권 변경 실패**
- GitHub Actions에서 `docker-compose stop/up` 시 **볼륨 소유권 재변경**이 재현되어 불안정 지속

**해결 방법**
1. **임시 대응:** `chown`으로 소유권 수동 정리 (재배포 시 재발)
2. **근본 조치:** **PostgreSQL로 전환**  
   - MySQL의 권한 정책 민감도로 인해 재발 가능성 높음  
   - 초기 단계라 **마이그레이션 비용 낮음**, PostgreSQL이 상대적으로 **권한 정책 유연**

**결과**  
전환 후 서버 재배포/테스트 **정상 동작**, 최근 **30일 재시작 0회**(기동 실패 없음).  
<img width="478" height="206" alt="Image" src="https://github.com/user-attachments/assets/61a3ce46-fb69-4dad-87b3-2f453023f67e" />

**배운 점**
- 컨테이너 내부의 `root`는 **호스트 OS의 `root`와 동일하지 않습니다.**
- 안정적 운영을 위해 **호스트 보안/격리 정책(LXD, userns 등)** 을 함께 이해해야 합니다.

---

### 3) 개인 웹소켓 연결 오류

**문제 상황**  
실시간 포트폴리오/체결 알림용 **개인 웹소켓(STOMP)** 이 **연결 실패**했습니다.

**원인**
- STOMP 로깅을 `DEBUG`로 확인한 결과, `spring-security-messaging` 기본 설정에서 **`MissingCsrfException`** 발생  
- 서비스는 **JWT 단일 인증(Stateless)** 으로 동작 → **CSRF 요구와 충돌**

**해결 방법**
1. `spring-security-messaging` **의존성 제거**  
   - API 통신은 **JWT**가 유일한 신원 검증 수단  
   - JWT 기반은 **세션 쿠키 비의존(Stateless)**, 토큰 서명으로 위변조 방지
2. **CONNECT 시 JWT 단일 검증 통일**  
   - `ChannelInterceptor#preSend()`에서 **JWT 검증**  
   - 성공 시 **`Principal`을 STOMP 세션에 주입** → 이후 `SUBSCRIBE`/`SEND`에서 사용자 식별 가능

<img width="934" height="682" alt="Image" src="https://github.com/user-attachments/assets/c0625524-bd58-473e-b2dc-fbe8e6143b8b" />

**결과**  
연결 테스트 **100회 기준 실패 0건**.  
<img width="1068" height="341" alt="Image" src="https://github.com/user-attachments/assets/b356e22f-c4c3-4d0e-b271-0827d9e091a3" />

**배운 점**
- **DEBUG 로깅**으로 프레임워크 내부 흐름을 추적해야 **숨은 원인**을 정확히 파악할 수 있습니다.
- WebSocket도 HTTP와 동일하게 **Stateless JWT**로 통일하면 충돌 지점이 줄고 운영이 단순해집니다.

