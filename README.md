# SPRING PLUS


---

## 주요 고민 & 해결

### ① QueryDSL 검색 성능

* **카디널리티 폭발**: `Todo × Manager × Comment` → 댓글은 서브쿼리로 분리
* **동적 JOIN**: 닉네임 파라미터 없으면 `manager.user` JOIN 생략
* **QDto Projection**: 컴파일-타임 타입 안전 + 불필요 필드 차단

### ② JOIN 전략

* 초기엔 `INNER JOIN` → **담당자 탈퇴**·**로직 변경** 대비 **LEFT JOIN** 전환

### ③ 로그 트랜잭션

* 매니저 등록 실패 여부와 무관하게 **로그는 반드시 저장**해야 하므로
  `@Transactional(REQUIRES_NEW)` + `try-catch`로 **비즈니스-로그 분리**

---


## 예외-처리 & 트랜잭션 분리

| 시나리오              | HTTP      | 처리 위치                                |
| ----------------- | --------- | ------------------------------------ |
| 인증 토큰 없음          | 401       | `JwtAuthenticationFilter`            |
| 토큰 변조·만료          | 401       | `JwtAuthenticationFilter`            |
| 권한 부족             | 403       | `SecurityConfig.accessDeniedHandler` |
| 매니저 등록 실패해도 로그 저장 | 200 / 4xx | `LogService` (`REQUIRES_NEW`)        |

---

## 보안 (Spring Security + JWT)

1. **필터 체인**: `JwtAuthenticationFilter` → `SecurityContext`
2. **권한**: `/admin/**` → `ROLE_ADMIN`
3. **JWT 페이로드**: `userId`, `email`, `nickname`, `userRole`
4. **커스텀 핸들러**: 401/403 JSON 응답

---

## 테스트

* `BulkInsertTest` – 1 M 더미 데이터 생성 속도 측정
* `UserSearchTest` – 인덱스별 검색 시간 10회 평균
* `TodoControllerTest` – 존재하지 않는 Todo 예외 시나리오 통과
* `AdminAccessLoggingAspectTest` – `changeUserRole()` 메서드 실행 전 AOP 동작 확인

---


### 닉네임 검색 속도 
| 테이블                   | p50 (ms) | 평균 (ms) | 최대 (ms) |
| --------------------- | -------- | ------- | ------- |
| `users_noidx` (인덱스 X) | 600      | 800     | 1,000   |
| `users_idx` (인덱스)     | 70       | 120     | 200     |
| `users_idx` + 캐시 HIT  | 10       | 15      | 20      |


> *측정 쿼리:* `SELECT SQL_NO_CACHE id FROM … WHERE nickname = ?`

---

## 실행 방법

```bash
./gradlew clean build
docker compose up -d          # MySQL
./gradlew bootRun
```

* `application-test.yml`에 **`allowLoadLocalInfile=true`** 세팅 필요
* JWT Secret Key는 `application.yml` `jwt.secret.key` 에 Base64 문자열로 입력

