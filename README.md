# Cerberus
2026 년 최종 보안 프로젝트

## JSP 기반 보안 인증/세션 관리 시스템

### 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Java 17, Jakarta Servlet 6.0, JSP 3.1 |
| Frontend | JSP (JSTL), Vanilla JS |
| WAS | Apache Tomcat 10.1 |
| DB | MariaDB 10.11 (커넥션 풀링) |
| Build | Maven 3.8+ |
| OS | Rocky Linux 9 |

### 주요 보안 기능

- 인증 보안

BCrypt (cost=12) 비밀번호 해싱
세션 고정 공격 방어 — 로그인 성공 시 기존 세션 파기 후 신규 발급
HttpOnly + SameSite=Strict 세션 쿠키 설정
계정 잠금 — N회 실패 시 M분 잠금

- CSRF 방어

모든 POST 요청에 토큰 검증 (`CsrfFilter`)
토큰은 POST 처리 후 교체 (Double Submit Cookie 패턴 아님, 서버 측 세션 기반)

- 접근 제어

`AuthFilter` — `/dashboard/*`, `/admin/*`, `/api/*` 보호
관리자 전용 경로 역할 검사

- 보안 헤더

```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Security-Policy
Referrer-Policy: strict-origin-when-cross-origin
```

- 감사 로그

로그인 성공/실패, 로그아웃 전부 `access_logs` 테이블에 기록
IP 주소, 타임스탬프, 실패 사유 포함

- 로컬 실행 (개발용)

- 사전 요건

JDK 17+
Maven 3.8+
MariaDB or MySQL 8+
Tomcat 10.1+ (`CATALINA_HOME` 설정)

- DB 초기화

```bash
mysql -u root -p < src/main/resources/schema.sql
```

- 빌드 및 배포

```bash
mvn clean package
cp target/secauth.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

> 접속: `http://localhost:8080/secauth/`

- 기본 계정

아이디: admin / 비밀번호: Admin@2024
아이디: testuser / 비밀번호: User@2024

- Rocky Linux 자동 배포

```bash
chmod +x deploy.sh
sudo ./deploy.sh
```

- 보안 정책 설정 (관리자 패널에서)

`/admin/policy` 에서 런타임 조정 가능:

| 키 | 기본값 | 설명 |
|----|--------|------|
| max_failed_attempts | 5 | 계정 잠금까지 실패 횟수 |
| lockout_minutes | 30 | 잠금 시간(분) |
| session_timeout | 30 | 세션 만료(분) |
| password_min_length | 8 | 최소 비밀번호 길이 |

- API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/logs?limit=50` | 최근 접근 로그 JSON |
| GET | `/api/stats` | 통계 요약 JSON |
