# 작성 규칙

## 개발 방식 (사용자 선호)

- **커밋 단위로 작게 나눠 진행.** 커밋마다 커밋 메시지도 함께 제시.
- 코드는 zip이 아니라 **채팅에 바로 붙일 코드 블록**으로 하나씩.
- **분석 먼저, 코드 나중.** 왜 이렇게 하는지 짚고 나서 구현.
- 조기 최적화 경계 — "나중에 필요할 것 같아서"는 대체로 지금 만들지 않는다.

## 패키지 구조

기능별. `com.iamnot.fitmeasure` 아래:
- `club`, `member`, `membership`, `config`
- `measurement/template`, `measurement/session`, `measurement/dashboard`
- `owner`(사장 전용), `admin`(운영자 전용)
- 각 기능의 dto는 `<기능>/dto/` 하위 record로

## 엔티티

- Lombok `@Getter` + `@NoArgsConstructor(access = PROTECTED)`. 클래스 레벨 `@Setter` 금지(격리/불변 깨짐).
- 상태 변경은 **의도 메서드**로(`deactivate`, `rename`, `claim`, `transferTo` 등). setter 노출 최소화.
- `@OneToMany` 지양(사용자 선호). 역참조가 필요하면 리포지토리로 조회.
- enum은 항상 `@Enumerated(EnumType.STRING)`.
- 공통 시각은 `BaseEntity`(createdAt/updatedAt, @PrePersist/@PreUpdate).
- 정적 팩토리로 의도 드러내기(`Member.anonymous()`, `Member.withPhone()`, `MeasurementValue.ofNumber()`).

## 서비스/격리

- **모든 클럽 스코프 조회는 `CurrentClub.clubId()` 경유.** 절대 `findById`만으로 접근 금지 — `findByIdAndClubId` 패턴.
- `CurrentClubFromSecurity`(@Primary)가 로그인 사용자의 클럽 제공.
- 목록 반환은 **엔티티가 아니라 dto(record)** — `open-in-view=false`라 뷰에서 지연 로딩 터짐. 서비스에서 필요한 값만 뽑아 record로.

## 화면 (Thymeleaf)

- **앱 내부 화면 = layout 상속.** `layout:decorate="~{layout}"`, 본문 `<div layout:fragment="content">`, 스타일 `layout:fragment="styles"`, 스크립트 `layout:fragment="scripts"`.
- **독립 페이지(layout 미적용):** 랜딩(`/`), 로그인, 회원가입, 클럽생성, 공유카드(`/s/**`), claim. 이들은 CSRF 메타태그를 각자 챙김.
- **회원 화면 = member-layout 상속**(트레이너 네비 없음).
- 클래스명 Bootstrap과 충돌 주의: `.card`→`.pcard`, `.row`→`.rrow` 등 프리픽스.
- CSS 변수(`--orange` 등)는 layout `:root`에 정의됨. 독립 페이지는 각자 정의.
- enum은 화면에서 **한글+예시로** 풀어 보여줌(값은 그대로 전송). 초보 사장 배려.

## htmx

- 부분 갱신은 `"뷰 :: fragment"` 반환.
- 개수/요약 같은 표시는 **갱신될 fragment 안에** 둬야 같이 바뀜.
- CSRF: layout이 `htmx:configRequest`로 헤더 자동 첨부. fetch는 수동으로 헤더 추가. form은 hidden 토큰.
- disabled input은 폼 전송 안 됨 → 잠근 값은 hidden으로 백업.

## 톤/문구

- 브랜드 색: 화이트 베이스 + 오렌지(#FF5A1F) 임팩트, 미니멀. 오렌지는 강조에만(넓게 X).
- 화면 문구는 친근하고 쉽게. 사장·회원이 헷갈리지 않게 예시 곁들임.
