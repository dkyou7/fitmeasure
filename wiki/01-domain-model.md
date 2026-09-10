# 도메인 모델

패키지 루트: `com.iamnot.fitmeasure`. 기능별 패키지 구조.

## 엔티티 관계 한눈에

```
Member (자연인 · 계정)
 ├─ 1:N ─ MemberCredential (인증수단: USERNAME/KAKAO/NAVER/APPLE/GOOGLE)
 └─ M:N ─ Club  ←(중간 엔티티)→  Membership (role: OWNER/STAFF/MEMBER)
                                    │
Club (헬스장 = 측정 운영 주체)        │ 1:N
 ├─ 1:N ─ MeasurementTemplate       Membership
 │            └─ 1:N ─ TemplateItem      └─ 1:N ─ MeasurementSession
 │                                              └─ 1:N ─ MeasurementValue
 └─ (type: GYM/STUDIO/CREW/EVENT/COMPANY)                 └─ N:1 ─ TemplateItem
```

## 핵심 엔티티

### Member — 자연인, 계정
- 사람 그 자체. **역할이 없다.** 역할은 Membership이 가진다.
- `phone` — 연락처이자 **사람 식별자**(공유·통합의 열쇠). unique. 트레이너가 회원 등록 시 입력.
- `platformAdmin` — 웹 운영자 여부(불리언). 향후 다층 권한 필요하면 enum→RBAC로 확장.
- 인증 정보는 Member가 아니라 **MemberCredential**에 분리(소셜 로그인 대비).

### MemberCredential — 인증수단
- 한 Member가 여러 개 가질 수 있음(휴대폰+카카오 등). Member를 **단방향 @ManyToOne 참조**(Member엔 역참조 컬렉션 없음 — @OneToMany 안 씀).
- `provider` = USERNAME(아이디+비번, 현재 유일 실동작) / KAKAO / NAVER / APPLE / GOOGLE(소셜은 목업).
- `providerId` = 아이디 문자열 또는 소셜 고유 id. `passwordHash`는 USERNAME일 때만.
- 로그인 식별자는 **아이디(USERNAME)**. 전 역할 공통.

### Club — 측정 운영 주체 ("헬스장")
- 원래 이름 후보가 Organization이었으나 **Club**으로. `type`으로 GYM/CREW/EVENT/마라톤 등 확장.
- 구독 단위이자 **데이터 격리 경계**(테넌트). `plan`(FREE/PAID), `freeMemberLimit`(기본 10), `slug`(공유 URL용).

### Membership — Member↔Club 중간 엔티티 (M:N)
- **역할은 사람이 아니라 이 관계의 속성.** 같은 사람이 클럽마다 다른 role 가능.
- `role`(OWNER/STAFF/MEMBER), `status`(ACTIVE/DORMANT), `nickname`(클럽 내 표시명).
- **측정 세션은 Membership에 귀속**된다(Member가 아니라). "이 클럽에서의 이 사람"의 기록.
- 트레이너 별도 엔티티 없음 — role=STAFF인 Membership이 트레이너.

### MeasurementTemplate — "프로그램"(측정표)
- 화면 용어는 "프로그램", 코드는 Template. 클럽당 **여러 개** 가능("3대 측정", "기초 체력" 등).
- `club`이 null이면 **시스템 표준 템플릿**(읽기전용). 클럽 생성 시 `copyForClub`으로 깊은 복사.
- `recommendedCadenceDays`(측정 주기, 기본 28) — 리텐션(다음 측정 예정일) 계산에 사용.

### TemplateItem — 측정 항목 정의
- "무엇을 어떻게 재는가": `measurementType`(NUMBER/REPS/TIME/SELECT), `unit`, `direction`(HIGHER_BETTER/LOWER_BETTER), `category`(STRENGTH/ENDURANCE/CARDIO/FLEXIBILITY/CORE/ETC), `protocol`(측정 가이드).
- **삭제하지 않고 `active=false`로 비활성**(과거 측정값이 참조하므로).
- **측정값이 있으면 유형·단위·방향은 수정 불가**(과거 해석이 깨짐). 이름·분류만 수정 가능. → `editAll`/`editSafe`.

### MeasurementSession — 1회 측정
- Membership에 귀속. `measuredBy`(측정한 STAFF/OWNER Membership), `measuredAt`(소급입력 대비 created_at과 분리).
- `shareToken`+`shareEnabled` — 공유 카드 공개 링크(기본 비공개, 트레이너가 활성화).

### MeasurementValue — 측정값 (중간 엔티티)
- **Session ↔ TemplateItem의 M:N을 푸는 중간 엔티티.** 실측값을 담음.
- `valueNumber`(NUMBER/REPS/TIME은 초 단위 숫자) / `valueText`(SELECT). `skipped`(안 잰 항목).
- 정적 팩토리 `ofNumber/ofText/skipped`로 생성.

## 왜 이렇게 설계했는가 (되돌리면 안 되는 것)

1. **Member ↔ Club = M:N (Membership 경유)** — 회원이 여러 헬스장에 다니며 기록을 쌓는 것을 지원. 초기엔 1:N이었으나 성장 엔진(회원이 우리 계정 소유) 설계하며 M:N으로. 세션이 Membership에 붙어서 클럽 격리와 회원 통합을 동시에 만족.

2. **Session ↔ TemplateItem = M:N, MeasurementValue가 중간** — 값에 세션(언제)과 항목(무엇)이 둘 다 붙어서, **읽는 방향이 곧 화면**이 된다. 항목 기준으로 값을 측정일 순 정렬 = 성장 추이 그래프(별도 계산 없음). `MeasurementValueRepository.findTrend` 참고.

3. **인증(Credential)과 사람(Member) 분리** — 소셜 로그인 다중 대응. provider 늘어도 Member 안 바뀜.

4. **역할 = Membership 속성** — 트레이너 엔티티를 따로 안 만든 이유. "한 사람이 여러 역할"이 자연스럽게 성립.

5. **항목 정의 불변(측정값 있으면)** — 데이터 정합성. "플랭크(초)→(분)" 같은 변경이 과거 값 해석을 깨뜨리는 것 방지.
