# DB 설계 (Part 1) — 설계표준 · 전체 테이블 인벤토리 · 기반 도메인

> 대상 DBMS: **Oracle** · 기존 eMRO(DIPS/DFS) 데이터모델을 현대적 컨벤션으로 재설계
> 진행: Part 1(표준+인벤토리+공통기반) → Part 2~ (도메인별 컬럼 상세)

---

## 1. 설계 표준 (Conventions)

### 1.1 네이밍
| 항목 | 규칙 | 예 |
|------|------|----|
| 테이블 | `도메인접두어_의미` (대문자 스네이크, 가독형) — eMRO의 6자리 암호식(CSACFSM)에서 탈피 | `PR_REQUEST`, `PO_ORDER_ITEM` |
| 컬럼 | 대문자 스네이크. 코드 `_CD`, 명 `_NM`, 식별자 `_ID`, 여부 `_YN`(CHAR1 Y/N), 일시 `_DT`, 일자 `_YMD`(8자리), 금액 `_AMT`, 수량 `_QTY`, 단가 `_PRC` | `ITEM_CD`, `SUPL_AMT` |
| PK | 대리키(Surrogate) `ID NUMBER GENERATED ALWAYS AS IDENTITY` + **업무 유니크키(UK)** 별도 | `PR_REQUEST.ID` + UK(`COMP_CD`,`PR_NO`) |
| FK | `자식_부모ID` | `PR_ITEM.REQUEST_ID` |
| 인덱스 | `IX_테이블_n` / 유니크 `UX_테이블_n` | `UX_PR_REQUEST_1` |

> ※ MyBatis `map-underscore-to-camel-case=true` → `ITEM_CD` ↔ 자바 `itemCd` 자동 매핑.

### 1.2 공통 컬럼 (모든 업무 테이블 표준 탑재)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `ID` | NUMBER (IDENTITY) | 대리 PK |
| `COMP_CD` | VARCHAR2(18) | **회사/법인 코드** (다법인 분리. 구 SYS_ID 대체) |
| `USE_YN` | CHAR(1) DEFAULT 'Y' | 사용여부 |
| `DEL_YN` | CHAR(1) DEFAULT 'N' | 논리삭제 (물리삭제 지양) |
| `STS` | VARCHAR2(18) | 업무 상태코드(진행상태) — 필요한 트랜잭션 테이블만 |
| `REG_ID` | VARCHAR2(18) | 등록자 |
| `REG_DT` | TIMESTAMP(0) WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP | 등록일시 |
| `MOD_ID` | VARCHAR2(18) | 수정자 |
| `MOD_DT` | TIMESTAMP(0) WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP | 수정일시 |

> 멀티테넌시: 기존 eMRO는 SYS_ID로 다(多)고객 격리. 신규는 **단일 설치 + 다법인(COMP_CD)** 모델 권장(필요 시 TENANT_ID 추가 가능 — 결정 필요).

### 1.3 데이터 타입 규약
- 코드/ID: `VARCHAR2(18 CHAR)` (긴 명칭은 50/100/200)
- 금액/단가: `NUMBER(20,5)` · 수량: `NUMBER(20,5)` · 율(rate): `NUMBER(10,5)`
- 일자: `VARCHAR2(8)`(YYYYMMDD) 또는 `DATE` — 화면 호환 위해 8자리 문자 병행 가능
- 일시: `TIMESTAMP(0) WITH LOCAL TIME ZONE`
- 여부: `CHAR(1)` Y/N
- 다국어 명칭: 별도 다국어 테이블 또는 `_NM_KO`,`_NM_EN` 컬럼

### 1.4 공통 정책
- 모든 테이블/컬럼 **한글 COMMENT** 의무
- 물리삭제 대신 **논리삭제(DEL_YN)** + 변경이력(감사로그)
- 첨부파일은 공통 `CM_ATTACH`(+`CM_ATTACH_FILE`) 참조
- 상태(STS)·구분(CLS)값은 하드코딩 금지 → `CM_CODE` 공통코드 참조
- 채번은 공통 `CM_DOC_NO`(문서번호 규칙)로 일원화

---

## 2. 스키마 / 도메인 접두어 맵

| 접두어 | 도메인 | 구 eMRO 대응 |
|--------|--------|-------------|
| `CM_` | 공통·기준정보(코드/조직/사용자/권한/메뉴/첨부/메일/SMS/채번) | ES*, common |
| `LOG_` | **감사로그·변경이력(RUD 추적)** (신규) | (신규) |
| `VD_` | 협력사·소싱·SRM·평가·감시·규제(esourcing/srm) | SRM, VS |
| `IT_` | 품목·분류·속성(cms) | CMS, EC |
| `PR_` | 구매요청 | pro/pr |
| `RX_` | 견적/정보요청(RFx/RFI) | pro/rfx, rfi |
| `AU_` | 역경매·입찰고정가 | pro/auction, bidfix |
| `CT_` | 단가계약 | pro/contract |
| `PO_` | 발주 | pro/po |
| `GR_` | 입고/검수 | pro/gr |
| `IP_` | 품목단가 | pro/itemprice |
| `TP_` | 인도가/운반비 | pro/transprice |
| `CL_` | 정산/마감 | CLS, CMS(수수료) |
| `PF_` | 가격요소/시황/실적/판매/채권 | pricefactor, garak, performres, salespo, ar |
| `QL_` | 품질 | qlt |
| `AP_` | 전자결재 | approval |
| `ED_` | 전자계약/전자서명 | edoc |
| `IF_` | 외부 인터페이스(SAP/INDIGO 송수신) | IF_* |

---

## 3. 전체 테이블 인벤토리 (계획)

> 각 도메인의 핵심 테이블만 표기(상세 컬럼은 Part 2~). H=헤더, D=상세(라인), M=마스터, L=로그/이력.

### CM_ 공통·기준정보
| 테이블 | 구분 | 설명 |
|--------|------|------|
| CM_COMPANY | M | 회사/법인 |
| CM_CODE_GRP / CM_CODE | M | 공통코드 그룹/상세(다국어) |
| CM_DOC_NO | M | 문서번호 채번 규칙 |
| CM_EXCH_RATE | M | 환율 |
| CM_MESSAGE | M | 다국어 메시지 |
| CM_ORG / CM_DEPT | M | 조직(회사)/부서 |
| CM_OPER_ORG / CM_OPER_UNIT | M | 운영조직(플랜트)/운영단위 |
| CM_USER | M | 사용자(임직원) |
| CM_VENDOR_USER | M | 협력사 사용자 |
| CM_MENU | M | 메뉴 |
| CM_ROLE / CM_ROLE_FUNC / CM_USER_ROLE | M | 역할/역할기능권한/사용자역할 |
| CM_PURC_GRP | M | 구매그룹(직무) |
| CM_ATTACH / CM_ATTACH_FILE | M | 첨부 그룹/파일 |
| CM_BOARD / CM_BOARD_POST | M | 게시판/게시물 |
| CM_MAIL_SET / CM_MAIL_LOG | M/L | 메일설정/발송로그 |
| CM_SMS_SET / CM_SMS_LOG | M/L | SMS설정/발송로그 |
| CM_TEMPLATE | M | 문서/메일 템플릿 |
| CM_MANUAL | M | 매뉴얼(시스템/업무/품목) |
| CM_IF_CONFIG | M | 인터페이스(INDIGO/SAP) 설정·매핑 |
| CM_PRICE_SITE | M | 가격조사 사이트 링크 |
| CM_STATUS_FLOW | M | **문서 상태전이 규칙(전진/반려/회수/취소…)** ★ |

### LOG_ 감사로그 (신규)
| 테이블 | 구분 | 설명 |
|--------|------|------|
| LOG_CHANGE | L | **변경이력(RUD)**: 테이블/PK/컬럼/변경전·후값/구분(I/U/D)/사용자/일시/사유 |
| LOG_DOC_STATUS_HIS | L | **문서 상태이력**: 정/역방향 전이 기록(반려·회수·취소 사유 포함) ★ |
| LOG_ACCESS | L | 접근/로그인·주요조회 로그(옵션) |
| LOG_BATCH | L | 배치(Quartz) 실행 로그 |
| LOG_IF | L | 인터페이스 송수신 로그 |

### VD_ 협력사·소싱·SRM
| 테이블 | 구분 | 설명 |
|--------|------|------|
| VD_VENDOR / VD_VENDOR_HIS | M/L | 협력사 마스터/이력 |
| VD_VENDOR_FI | M | 협력사 재무정보 |
| VD_VENDOR_OPER | M | 협력사 운영조직 매핑 |
| VD_EVAL_SHEET / VD_EVAL_ITEM | M | 평가시트/평가항목(의존심사 ingp) |
| VD_EVAL / VD_EVAL_RESULT / VD_EVAL_STEP | H/D | 평가 실행/결과/단계(execute·summary·cmpl) |
| VD_EVAL_GRADE / VD_EVAL_GRP | M | 평가등급(efes)/평가그룹(egsg) |
| VD_EVAL_MATRIX | M | 평가매트릭스(hfm) |
| VD_ELEV | H | 협력사 육성(elev/DIFF) |
| VD_AUDIT / VD_AUDIT_RESULT | H/D | 협력사 감시(audit/auditnew) |
| VD_AEO | H | AEO 안전관리 평가 |
| VD_REGAL | M | 규제/법령 정보 |

### IT_ 품목·분류
| 테이블 | 구분 | 설명 |
|--------|------|------|
| IT_CATEGORY | M | 품목분류(다층) |
| IT_ATTR_POOL / IT_ATTR_GRP / IT_ATTR_CODE | M | 속성풀/속성그룹/속성코드 |
| IT_ITEM | M | 품목마스터 |
| IT_ITEM_ATTR | D | 품목 속성값 |
| IT_ITEM_OPER | D | 품목 운영조직(플랜트) |
| IT_ITEM_REQ / IT_ITEM_REQ_D | H/D | 품목요청/심사 |

### PR_ 구매요청
| 테이블 | 구분 | 설명 |
|--------|------|------|
| PR_REQUEST | H | 구매요청 헤더 |
| PR_ITEM | D | 구매요청 품목(물품/용역) |
| PR_ATTACH | D | 요청 첨부 |

### RX_ 견적/정보요청
| 테이블 | 구분 | 설명 |
|--------|------|------|
| RX_RFX | H | 견적(RFQ/RFP) 헤더 |
| RX_RFX_ITEM | D | 견적 품목 |
| RX_RFX_VENDOR | D | 초대 협력사 |
| RX_QUOTE / RX_QUOTE_ITEM | H/D | 협력사 견적 응답 |
| RX_EVAL / RX_EVAL_SCORE | H/D | 견적 평가/점수 |
| RX_RFI / RX_RFI_ITEM / RX_RFI_ANS | H/D | 정보요청/항목/응답 |

### AU_ 역경매·입찰고정가
| 테이블 | 구분 | 설명 |
|--------|------|------|
| AU_AUCTION | H | 역경매 헤더 |
| AU_AUCTION_ITEM | D | 역경매 품목 |
| AU_AUCTION_VENDOR | D | 참여 협력사 |
| AU_BID | L | 입찰(응찰) 내역 |
| AU_RESULT | D | 낙찰 결과 |
| AU_BIDFIX / AU_BIDFIX_RSV | H/D | 입찰고정가/변경예약 |

### CT_ 단가계약
| 테이블 | 구분 | 설명 |
|--------|------|------|
| CT_CONTRACT | H | 단가계약 헤더 |
| CT_CONTRACT_ITEM | D | 계약 품목·단가 |
| CT_CONTRACT_PLT | D | 계약 플랜트 확대 |
| CT_ATTACH | D | 계약 첨부 |

### PO_ 발주
| 테이블 | 구분 | 설명 |
|--------|------|------|
| PO_ORDER | H | 발주 헤더 |
| PO_ORDER_ITEM | D | 발주 품목 |
| PO_ORDER_HIS | L | 발주 수정판수 이력 |

### GR_ 입고/검수
| 테이블 | 구분 | 설명 |
|--------|------|------|
| GR_RECEIPT | H | 입고/검수 헤더 |
| GR_RECEIPT_ITEM | D | 입고 품목 |
| GR_CHANGE | H | 입고변경(GrChg) |
| GR_PAYMENT_BILL | H | 선급금/기성 청구 |
| GR_GRT | D | 보증보험료 |

### IP_/TP_ 단가·인도가
| 테이블 | 구분 | 설명 |
|--------|------|------|
| IP_ITEM_PRICE / IP_ITEM_PRICE_HIS | M/L | 품목단가/이력 |
| IP_PRICE_RSV | H | 단가 변경예약(번들) |
| TP_TRANS_PRICE / TP_TRANS_PRICE_RSV | M/H | 인도가/변경예약 |

### CL_ 정산/마감
| 테이블 | 구분 | 설명 |
|--------|------|------|
| CL_CLOSE | H | 정산마감 헤더 |
| CL_CLOSE_GR | D | 마감 입고 |
| CL_BILL / CL_INVOICE | H/D | 청구서/인보이스 마감 |
| CL_SUBSIDY / CL_SUBSIDY_D | H/D | 보조금/장려금 정산 |
| CL_GAP | D | 갭(매입/분배/금액/매출) |
| CL_ADVPMT | H | 선급금 마감 |

### PF_ 가격요소·시황·실적·판매·채권
| 테이블 | 구분 | 설명 |
|--------|------|------|
| PF_PRICE_FACTOR / PF_PRICE_GRP | M | 가격요소/가격군 |
| PF_MARKET_PRICE | M | 농산물 시황 |
| PF_PERFORM | H | 마감/납기 실적 |
| PF_SALES_PO | H | 판매발주 |
| PF_AR | H | 미회수채권 |

### QL_ 품질
| 테이블 | 구분 | 설명 |
|--------|------|------|
| QL_INSPECTION | H | 입고검사 헤더 |
| QL_INSPECTION_RESULT | D | 검사 결과(SAP QM) |

### AP_ 전자결재
| 테이블 | 구분 | 설명 |
|--------|------|------|
| AP_APPROVAL | H | 결재 마스터(문서연결) |
| AP_APPROVAL_LINE | D | 결재선 |
| AP_APPROVAL_HIS | L | 결재 이력 |
| AP_TEMPLATE / AP_TEMPLATE_LINE | M | 결재 템플릿/결재선 템플릿 |

### ED_ 전자계약/서명
| 테이블 | 구분 | 설명 |
|--------|------|------|
| ED_CONTRACT | H | 전자계약 문서 |
| ED_SIGN | D | 서명 정보·검증 |
| ED_CERT | M | 인증서 |

### IF_ 인터페이스
| 테이블 | 구분 | 설명 |
|--------|------|------|
| IF_*_RCV / IF_*_SND | — | SAP/INDIGO 송수신 스테이징(인터페이스 ID별) |

---

## 4. 기반(공통) 도메인 상세 — CM_ / LOG_

> 표준 공통컬럼(§1.2)은 지면상 생략하고 **업무 컬럼만** 기재.

### CM_COMPANY (회사/법인)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| COMP_CD (UK) | VARCHAR2(18) | 회사코드 |
| COMP_NM | VARCHAR2(200) | 회사명 |
| BIZ_NO | VARCHAR2(20) | 사업자번호 |
| CEO_NM, ADDR, TEL | … | 대표/주소/연락처 |

### CM_CODE_GRP (공통코드 그룹) / CM_CODE (상세)
- CM_CODE_GRP: `GRP_CD`(UK), `GRP_NM`, `DESC`
- CM_CODE: `GRP_CD`+`CD`(UK), `CD_NM_KO`, `CD_NM_EN`, `SORT_NO`, `ATTR1~4`(부가속성), `USE_YN`

### CM_DOC_NO (문서번호 채번)
| 컬럼 | 설명 |
|------|------|
| DOC_TYP (UK) | 문서유형(PR/PO/RFX/AUCTION…) |
| PREFIX | 접두 |
| DATE_FMT | 일자포맷(YYYYMM 등) |
| SEQ_LEN | 일련번호 자리수 |
| LAST_SEQ / LAST_YMD | 마지막 채번값/일자 |

### CM_ORG / CM_DEPT / CM_OPER_ORG / CM_OPER_UNIT
- CM_DEPT: `DEPT_CD`(UK), `DEPT_NM`, `UP_DEPT_CD`(상위), `COMP_CD`
- CM_OPER_ORG(플랜트): `OPER_ORG_CD`(UK), `OPER_ORG_NM`, `PLT_CD`, `PURC_ORG_CD`
- CM_OPER_UNIT: `OPER_UNIT_CD`(UK), `OPER_UNIT_NM`

### CM_USER / CM_VENDOR_USER
- CM_USER: `USR_ID`(UK), `USR_NM`, `DEPT_CD`, `POS_CD`(직위), `EMAIL`, `MOBILE`, `PURC_GRP_CD`, `LOGIN_ID`, `PWD_HASH`, `LANG_CD`
- CM_VENDOR_USER: `VD_CD`+`USR_ID`(UK), `USR_NM`, `EMAIL`, `MOBILE`, `REP_YN`(대표)

### CM_MENU / CM_ROLE / CM_ROLE_FUNC / CM_USER_ROLE
- CM_MENU: `MENU_ID`(UK), `MENU_NM`, `UP_MENU_ID`, `URL`, `SORT_NO`, `MENU_LVL`, `ICON`
- CM_ROLE: `ROLE_ID`(UK), `ROLE_NM`
- CM_ROLE_FUNC: `ROLE_ID`+`MENU_ID`(UK), `AUTH_CD`(READ/SAVE/APPROVAL/MANAGEMENT)
- CM_USER_ROLE: `USR_ID`+`ROLE_ID`(UK)

### CM_ATTACH / CM_ATTACH_FILE
- CM_ATTACH: `ATTACH_GRP_ID`(UK)
- CM_ATTACH_FILE: `ATTACH_GRP_ID`+`FILE_SEQ`(UK), `ORG_FILE_NM`, `SAVE_FILE_NM`, `FILE_PATH`, `FILE_SIZE`, `FILE_EXT`

### CM_SMS_SET / CM_SMS_LOG / CM_MAIL_SET / CM_MAIL_LOG
- (구 CSACFSM/CSAMSMS 대응) SMS설정: `SMS_SET_ID`(UK), `TMP_ID`, `SND_CLS`
- SMS로그: `SMS_ID`(UK), `SMS_TIT`, `SMS_CONT`, `TO_PHONE_NO`, `SND_YN`, `SND_DT`

### LOG_CHANGE (변경이력 / RUD 추적) ★신규
| 컬럼 | 타입 | 설명 |
|------|------|------|
| ID | NUMBER(IDENTITY) | PK |
| TABLE_NM | VARCHAR2(50) | 대상 테이블 |
| PK_VALUE | VARCHAR2(200) | 대상 레코드 PK(복합은 구분자 결합) |
| CHG_TYP | CHAR(1) | I(생성)/U(수정)/D(삭제) |
| COL_NM | VARCHAR2(50) | 변경 컬럼(행단위 기록 시 NULL 가능) |
| OLD_VAL | VARCHAR2(4000) | 변경 전 값 |
| NEW_VAL | VARCHAR2(4000) | 변경 후 값 |
| CHG_RSN | VARCHAR2(500) | 변경 사유 |
| CHG_ID | VARCHAR2(18) | 변경자 |
| CHG_DT | TIMESTAMP | 변경일시 |
| MENU_ID / IP_ADDR | … | 발생 메뉴/IP |

> 구현방식 옵션: ① 애플리케이션(Spring AOP/MyBatis Interceptor)에서 기록, ② Oracle 트리거. 권장은 ①(테이블 단위 on/off 설정 + 변경사유 입력 연계 용이).

---

## 5. 다음 단계 (Part 2~)
- Part 2: PR/RX/AU/PO/GR (구매 트랜잭션) 컬럼 상세 + ERD 관계
- Part 3: CT/IP/TP/CL/PF (계약·단가·정산) 상세
- Part 4: VD/IT (협력사·품목 마스터) 상세
- Part 5: QL/AP/ED/IF (품질·결재·전자계약·인터페이스) 상세
- 이후: 물리 DDL 스크립트(`db/ddl/*.sql`) 생성

## 6. 결정 필요
- 멀티테넌시: 단일설치+다법인(COMP_CD) vs 멀티테넌트(TENANT_ID) — **권장: 다법인**
- PK 전략: 대리키(IDENTITY) vs 업무복합키 — **권장: 대리키 + 업무UK**
- 일자 컬럼: VARCHAR2(8) vs DATE — **권장: DATE(+표시 변환)**, 단 SAP 연계 항목은 문자 병행
