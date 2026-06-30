# Purchase System (신규 통합 구매시스템)

DIPS(동원홈푸드 계열) + DFS(동원F&B 계열) 두 eMRO SmartSuite9 구매시스템의 **기능 합집합**을 구현하는 신규 시스템.
> 기존 두 시스템 코드는 **읽기 전용 참고용**이며 절대 수정하지 않는다.
> - DIPS 참고: `C:\smartx91\workspace\smartx9.1-dwhf2018`
> - DFS 참고: `D:\smartx91\workspace\smartx9.1-dwfb2020`

## 기술 스택 (현대화)

| 구분 | 채택 |
|------|------|
| Backend | Spring Boot 3 + Java 21 (LTS) |
| Data Access | MyBatis (대량 SQL·정산/단가 로직 이식) |
| Frontend | Vue 3 + TypeScript + Vite (Pinia) |
| DB | Oracle |
| Build | Gradle (Kotlin DSL) |
| Auth/API | Spring Security + JWT, REST |

## 디렉토리 구조

```
purchasesystem/
├── backend/     Spring Boot REST API
├── frontend/    Vue 3 SPA
└── docs/        설계 문서 (기능 대조 매트릭스, ERD, 화면목록 등)
```

## 빌드 사전 요구사항

- JDK 21
- Node.js LTS (18+)

## 기능 모듈 (계획)

| 도메인 | 내용 |
|------|------|
| pro | 구매요청(PR)·견적(RFx)·정보요청(RFI)·역경매·입찰고정가·단가계약·발주(PO)·입고검수(GR)·품목단가·인도가·정산(CLS)·시황·가격요소·실적·판매발주·채권 |
| cms | 품목분류·품목마스터·품목요청 |
| esourcing | 협력사정보·평가·의존심사·감시(audit/aeo)·규제준수(regal)·소싱설정 |
| srm | 협력사평가 생명주기(execute/summary/cmpl)·평가매트릭스(hfm)·육성(elev)·평가등급/그룹 |
| qlt | 원부자재 입고검사·SAP QM 연동 |
| edoc | 전자계약·전자서명(PKI) |
| approval | 전자결재 엔진 |
| analysis | 신성장보조금·품목분석·진행추적·협력사분석 |
| admin | 조직·공통코드·권한/메뉴·환율·다국어·메일/SMS·인터페이스설정 |

## 현재 상태

- [x] 프로젝트 골격 생성
- [ ] 기능 대조 매트릭스(DIPS vs DFS) 확정
- [ ] 도메인별 상세 기능정의 / DB 설계 / 화면 목록
- [ ] 모듈 구현
