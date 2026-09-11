# 주요 설계 결정 (v3 — 소셜로그인·연결코드·온보딩 반영)

코드만 보면 "왜 이렇게 했지?" 싶은 판단들의 이유. **함부로 되돌리면 안 되는 것들.**
v2에서 인증·회원 연결 구조가 크게 바뀌었으니 이 문서를 우선한다.

## 브랜드

- 브랜드명 **1RepPlus** (원랩플러스). 태그라인 "측정하고, 기록하고, 더 나아지다".
- 로고: 잉크색 카드 실루엣 + 오렌지 상승선 + 숫자 "1".

## 포지셔닝

- **"측정 도구 제공자"** — 측정 기준·정확도·가격은 헬스장 책임. 표준 템플릿은 예시일 뿐 강제 아님. 업종 확장(크루/이벤트/마라톤) 가능.

## 인증 — 3경로 (v3에서 크게 바뀜)

- **폼 로그인**: 아이디+비번+비번확인(signup) → 계정 자격만. 이름·연락처는 온보딩에서.
- **소셜 로그인(카카오)**: OAuth2. 첫 로그인 시 Member 생성(KAKAO credential).
- **공통 온보딩**: 두 경로 모두 가입 후 `/onboarding`에서 닉네임·전화번호 입력. `Member.onboardedAt`으로 완료 판정.
- **온보딩 강제**: `OnboardingInterceptor`가 미완 사용자를 `/onboarding`으로. 세션값 아닌 **DB로 매 요청 확인**(완료 즉시 반영, 루프 방지). 제외: 로그인·가입·소셜콜백(`/oauth2/**`,`/login/oauth2/**`)·공개페이지·정적.
- **principal 통합**: `AppPrincipal` 인터페이스(memberId/clubId/nickname/isPlatformAdmin/isOnboarded)를 `LoginMember`(폼)와 `OAuth2LoginMember`(소셜)가 구현. 컨트롤러는 `@AuthenticationPrincipal AppPrincipal`로 둘 다 받음. **이거 안 하면 소셜 사용자가 LoginMember 받는 화면에서 다 튕김.**
- **아이디는 폼 로그인 전용**. 소셜/회원에겐 아이디 안 받음(용도 없음 — 로그인은 소셜, 회원검색은 연결코드로 대체됨).
- **프록시 뒤 https**: `server.forward-headers-strategy=framework` (카카오 redirect_uri가 http로 나가 KOE006 나던 것 해결).

## 회원 생성·연결 — 근본 전환 (v3 핵심)

**"사람(Member)은 본인만 만든다. 트레이너는 연결만 한다."**

- **폐기됨**: 트레이너가 전화번호로 회원(Member) 생성 → 중복 Member 문제(가입 계정과 별개)를 낳아서 제거.
- **폐기됨**: 전화번호 자동 통합 → 트레이너 손입력이라 오타·도용 위험. "번호 일치 자동통합"은 보안 구멍.

**연결 코드 방식**:
- 회원이 먼저 **본인 가입**(소셜/폼) → 계정 존재
- 회원이 `/me/connect`에서 **6자리 코드+QR 발급**(5분 유효, 재발급 시 기존 무효화)
- 트레이너가 `/members/connect` 코드 입력 or `/connect?code=` QR 스캔 → 자기 클럽에 Membership(MEMBER) 추가
- 동의 = 회원의 코드 제공(대면). 이미 등록 시 차단.
- 측정 → 그 Membership에 세션. 회원은 로그인하면 바로 피드에 보임(흡수 불필요).

**미가입 측정**:
- 트레이너가 미가입자 측정 = 무제한, **저장 안 함, 히스토리 없음**. Member 안 만듦.
- "히스토리 원하면 가입"이 자연스러운 가입 유도. 미가입은 그 자리 결과만(공유·누적 없음).

## 전화번호 — 성격 정리 (v3)

- **Membership.phone**: 트레이너가 입력하는 클럽 내 회원 연락처. 흡수/이전해도 유지. 목록 표시·클럽내 중복체크용.
- **Member.phone**: 회원 본인이 프로필에서 관리하는 연락처. 순수 표시용, 자동연결 없음.
- 전화번호는 더 이상 식별자·통합 열쇠가 아님. unique 아님(같은 번호 여러 Member 가능).

## 이름 동기화 (완전 동기화)

- 익명 닉네임("이름 없는 곰")은 연결/온보딩 시 회원 이름으로 갱신.
- 이름 변경 시 그 회원의 모든 MEMBER Membership 닉네임 동기화(syncNickname). "내 이름 = 어디서나 내 이름".

## 무료 한도

- 기준 = **claim/가입한 회원 수**(등록 전체 아님). 미가입 측정 무제한.
- 무료 한도가 "사장이 회원 억제" 유인이 될 수 있음(모순). 미가입을 "아쉽게"(히스토리·공유 없음) 해서 가입 압력 유지. 과금 방식(회원수 vs 정액)은 영업 검증 후 재고.

## 권한 층위

- ROLE_ADMIN(운영자) / OWNER(사장) / STAFF(트레이너) / MEMBER(회원) / USER(떠도는).
- 운영자 = `Member.platformAdmin` 불리언. 다층 권한(영업직 등)은 실제 필요 시 enum→RBAC. 지금 RBAC는 과설계.
- 권한 부여 단일 지점: `getAuthorities()`.
- 클럽 생성 = **운영자 전용**(계약 기반). 셀프 개설 없음. `/admin`에서 아이디 검색 → 그 계정을 OWNER로 클럽 생성. 방문자·회원은 "도입 문의"(/contact)로.

## 화면·레이아웃

- **회원 = member-layout**(모바일 앱 스타일: 상단바 + 하단 4탭 홈/기록/연결/정보, fade-up 애니메이션).
- **사장/트레이너 = layout**(상단 네비, 일상작업 3개 + 설정 드롭다운, 모바일 햄버거).
- **독립 페이지**(layout 미상속): 랜딩·로그인·회원가입·온보딩·공유카드(/s/**)·클럽생성.
- 사장 홈(index) = 운영 대시보드(지표4 + 지금 챙길 회원 명단 + 빠른진입). HomeService.ownerHome.
- Bootstrap CSS+JS(bundle, body 끝 로드). primary 오렌지 오버라이드. navbar는 min-height(고정 X — 모바일 collapse 겹침 방지).

## 회원 탐색 (양면 플랫폼 씨앗)

- 회원 홈탭 = 가치 소개 + 헬스장 찾기 + 최근 기록.
- `Club.listed`(공개 동의) 클럽만 `/me/gyms` 목록에 노출. `/gyms/{id}` 상세 = 소개(intro) + 측정 프로그램·항목 공개.
- 사장이 `/owner/club`에서 소개·주소·공개여부 편집.
- 지도·검색은 헬스장 늘면. 지금은 구조만(데모 1곳).

## 공유 카드

- 결과지에서 shareToken 발급 → `/s/{token}`(결과 카드), `/s/{token}/flex`(자랑 카드).
- 흡수 없음. 자랑·유입용("나도 시작하기" → 랜딩). 측정 트레이너 명시(공신력).

## 배포

- NAS + Jenkins CI/CD, HTTPS(fitmeasure.dkyou7.synology.me). MySQL(공유 iamnotmeeting-mysql, iamnot-net).
- 시크릿(DB비번·카카오키)은 Jenkins credential(System-Global) → docker -e 주입.
- 실데이터 없을 때(계약 전)는 DB DROP/CREATE로 스키마 리셋 가능. 실데이터 생기면 Flyway 등 마이그레이션 필요(ddl-auto validate로).