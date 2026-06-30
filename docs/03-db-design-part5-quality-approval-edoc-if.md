# DB 설계 (Part 5, 최종) — QL(품질) · AP(전자결재) · ED(전자계약/서명) · IF(SAP/INDIGO 인터페이스)

> 표준(Part1) 적용. 공통컬럼 생략. 표기 🔑PK · ⭐UK · 🔗FK

---

## 1. QL_ 품질 (입고검사 · SAP QM 연동)

### QL_INSPECTION (입고검사 헤더)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| ID 🔑 | NUMBER | 대리키 |
| INSP_NO ⭐ | VARCHAR2(30) | 검사번호 |
| GR_ID 🔗 | NUMBER | 입고 → GR_RECEIPT |
| PO_ID 🔗 | NUMBER | 발주 → PO_ORDER |
| VD_CD 🔗 | VARCHAR2(18) | 협력사 |
| ITEM_CD 🔗 | VARCHAR2(18) | 품목(원부자재) |
| OPER_ORG_CD 🔗 | VARCHAR2(18) | 운영조직(플랜트) |
| INSP_TYP | VARCHAR2(18) | 검사유형(입고/정기/특별) |
| QM_LOT_NO | VARCHAR2(30) | SAP QM 검사로트번호 |
| INSP_QTY | NUMBER(20,5) | 검사수량 |
| PASS_QTY / FAIL_QTY | NUMBER(20,5) | 합격/불합격 수량 |
| INSP_RESULT | VARCHAR2(18) | 결과(합격/불합격/조건부) |
| INSP_YMD | DATE | 검사일 |
| IF_STATUS | CHAR(1) | SAP QM 수신상태 |
| STS | VARCHAR2(18) | 상태 |

### QL_INSPECTION_RESULT (검사 항목 결과 — SAP QM)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| INSP_ID 🔗 | NUMBER | → QL_INSPECTION |
| ITEM_SEQ ⭐ | NUMBER | 검사항목 순번 |
| INSP_ITEM_NM | VARCHAR2(200) | 검사항목명 |
| SPEC_VAL | VARCHAR2(100) | 규격(기준)값 |
| MEASURE_VAL | VARCHAR2(100) | 측정값 |
| UNIT | VARCHAR2(18) | 단위 |
| PASS_YN | CHAR(1) | 합부 |
| QM_CODE | VARCHAR2(30) | SAP QM 특성코드 |

> SAP QM 인터페이스(`ZQM_GR_INSP_RESULTS`)로 검사결과 수신 → QL_INSPECTION(_RESULT) 적재. 불합격 시 GR 검수 `반송/재입고`(상태전이 모델 연계).

---

## 2. AP_ 전자결재

### AP_APPROVAL (결재 마스터 — 모든 문서가 연결)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| ID 🔑 | NUMBER | 대리키 |
| APRV_NO ⭐ | VARCHAR2(30) | 결재문서번호 |
| DOC_TYP | VARCHAR2(18) | 원 문서유형(PR/PO/CT/GR/IP/VENDOR…) |
| DOC_ID | NUMBER | 원 문서 ID |
| DOC_NO | VARCHAR2(30) | 원 문서번호 |
| APRV_TITLE | VARCHAR2(300) | 결재 제목 |
| TEMPLATE_ID 🔗 | NUMBER | 결재템플릿 → AP_TEMPLATE |
| DRAFT_USR_ID 🔗 | VARCHAR2(18) | 기안자 |
| DRAFT_DT | TIMESTAMP | 기안일시 |
| CUR_STEP_NO | NUMBER | 현재 결재단계 |
| APRV_STS | VARCHAR2(18) | 결재상태(↓) |
| FINAL_APRV_DT | TIMESTAMP | 최종 완료일시 |

**APRV_STS**: `DRAFT`기안→`ING`결재중→`DONE`완료 / `REJECT`반려 / `RECALL`회수 (상태전이 모델과 동일 — 반려/회수 시 원문서도 되돌림)

### AP_APPROVAL_LINE (결재선)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| APRV_ID 🔗 | NUMBER | → AP_APPROVAL |
| STEP_NO ⭐ | NUMBER | 결재 단계 순번 |
| APRV_TYP | VARCHAR2(18) | 결재/합의/협조/참조/전결 |
| APRV_USR_ID 🔗 | VARCHAR2(18) | 결재자 |
| DEPUTY_USR_ID 🔗 | VARCHAR2(18) | 대결/위임자 |
| LINE_STS | VARCHAR2(18) | 대기/진행/승인/반려 |
| APRV_DT | TIMESTAMP | 처리일시 |
| OPINION | VARCHAR2(1000) | 결재의견 |

### AP_APPROVAL_HIS (결재 이력)
- `APRV_ID`🔗, `SEQ`, `STEP_NO`, `ACTION`(상신/승인/반려/회수/위임), `ACTOR_ID`, `ACTION_DT`, `OPINION` *(상태전이 LOG_DOC_STATUS_HIS와 연동/일원화 가능)*

### AP_TEMPLATE / AP_TEMPLATE_LINE (결재 템플릿)
- AP_TEMPLATE: `TEMPLATE_CD`⭐, `TEMPLATE_NM`, `DOC_TYP`(적용 문서유형), `USE_YN`
- AP_TEMPLATE_LINE: `TEMPLATE_CD`🔗+`STEP_NO`⭐, `APRV_TYP`, `APRV_ROLE`(역할/직위 기준), `APRV_USR_ID`(고정 결재자), `COND`(금액 등 조건)

### AP_DELEGATE (결재 위임 설정) — 선택
- `DELEGATOR_ID`🔗(위임자), `DEPUTY_ID`🔗(대리자), `START_YMD/END_YMD`, `DOC_TYP`, `USE_YN`

---

## 3. ED_ 전자계약/전자서명 (KICA PKI)

### ED_CONTRACT (전자계약 문서)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| ID 🔑 | NUMBER | 대리키 |
| EDOC_NO ⭐ | VARCHAR2(30) | 전자문서번호 |
| CT_ID 🔗 | NUMBER | 원 단가계약 → CT_CONTRACT |
| VD_CD 🔗 | VARCHAR2(18) | 협력사 |
| DOC_TITLE | VARCHAR2(300) | 문서 제목 |
| TEMPLATE_ID 🔗 | NUMBER | 계약서 템플릿 → CM_TEMPLATE |
| PDF_ATTACH_GRP_ID | NUMBER | 생성 PDF |
| DOC_STS | VARCHAR2(18) | 상태(↓) |
| DISTRIBUTE_DT | TIMESTAMP | 협력사 배포일시 |
| COMPLETE_DT | TIMESTAMP | 서명완료일시 |

**DOC_STS**: `DRAFT`작성→`DISTRIBUTE`배포→`SIGNING`서명중→`COMPLETE`완료 / `REJECT`반려 / `CANCEL`취소(재수정)

### ED_SIGN (서명 정보)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| EDOC_ID 🔗 | NUMBER | → ED_CONTRACT |
| SIGN_SEQ ⭐ | NUMBER | 서명 순번 |
| SIGNER_TYP | VARCHAR2(18) | 내부/협력사 |
| SIGNER_ID | VARCHAR2(18) | 서명자 |
| SIGN_TYP | VARCHAR2(18) | 서명방식(KICA 공동인증/간편) |
| CERT_ID 🔗 | NUMBER | 인증서 → ED_CERT |
| SIGN_VALUE | CLOB | 전자서명값 |
| SIGN_DT | TIMESTAMP | 서명일시 |
| VERIFY_YN | CHAR(1) | 서명검증 결과 |

### ED_CERT (인증서)
- `CERT_ID`🔑, `SUBJECT_DN`(주체), `ISSUER_DN`(발급기관), `SERIAL_NO`, `VALID_SD/ED`, `CERT_TYP`

---

## 4. IF_ 외부 인터페이스 (SAP / INDIGO)

### 4.1 표준 스테이징 패턴
모든 인터페이스는 `IF_<업무>_RCV`(수신) / `IF_<업무>_SND`(송신) 스테이징 테이블 + 공통 IF 컬럼:
| 공통 IF 컬럼 | 타입 | 설명 |
|------|------|------|
| IF_ID ⭐ | VARCHAR2(50) | 인터페이스 메시지 ID |
| IF_SEQ | VARCHAR2(50) | 배치/시퀀스 |
| IF_STATUS | CHAR(1) | N미처리/S성공/E오류/X실패 |
| IF_MSG | VARCHAR2(200) | 처리 메시지 |
| IF_DT | TIMESTAMP | 처리일시 |
| (+ 업무 데이터 컬럼들) | | SAP 원장 필드 |

### 4.2 설정·로그 (공통)
- **CM_IF_CONFIG**(인터페이스 설정): `IF_CD`⭐, `IF_NM`, `DIRECTION`(RCV/SND), `SRC_SYS`(SAP/INDIGO/그룹웨어), `ENDPOINT`, `SCHEDULE`(배치주기), `USE_YN`, 필드매핑(`CM_IF_MAP`)
- **LOG_IF**(인터페이스 로그): `IF_CD`, `IF_ID`, `DIRECTION`, `STATUS`, `REQ_CNT/SUC_CNT/ERR_CNT`, `MSG`, `EXEC_DT`

### 4.3 인터페이스 인벤토리 (원본 → 신규)
| 업무 | 방향 | SAP I/F ID(원본) | 신규 스테이징 | 내용 |
|------|------|------------------|---------------|------|
| 플랜트/저장위치 마스터 | RCV | ZMMTPU001 | IF_PLANT_RCV | 운영조직·플랜트 |
| 구매조직/구매그룹 | RCV | ZMPT0020 | IF_PURCORG_RCV | 구매조직 마스터 |
| 협력사 마스터 | RCV | CSMVDIM | IF_VENDOR_RCV | 협력사 동기화 |
| 부서 마스터 | RCV | ESMJBMA | IF_DEPT_RCV | 조직/부서 |
| 회사/사업소 주소 | RCV | ESACDDL | IF_COMPADDR_RCV | 회사정보 |
| 자재 GL | RCV | ESMMTGL | IF_ITEMGL_RCV | 자재-계정 매핑 |
| 비용센터 GL | RCV | CSMCTGL | IF_COSTGL_RCV | 비용센터/계정 |
| 협력사 품목단가 | RCV | ESPINFO | IF_ITEMPRICE_RCV | 단가 기준 |
| 인도가 | RCV | CSPTFPC | IF_TRANSPRICE_RCV | 인도가 구간 |
| 장려금율 | RCV/SND | CSMSRHD | IF_SUBSIDYRATE_RCV/_SND | 장려금 양방향 |
| 입고(GR) | RCV | CSIGRDT | IF_GR_RCV | 입고 실적 |
| 매입청구/세금계산서 | RCV | CSICLHD | IF_INVOICE_RCV | 전자세금계산서 |
| 갭(GR/IR) | RCV | CSGAPRS | IF_GAP_RCV | 매입실적 갭 |
| 결재결과(그룹웨어) | RCV | (CSGAPRS결재) | IF_APPROVAL_RCV | 그룹웨어 결재결과 |
| **품질검사 결과** ▲ | RCV | ZQM_GR_INSP_RESULTS | IF_QM_RCV | SAP QM 입고검사 |
| 단가 변경 | SND | (ITEMPRICE) | IF_ITEMPRICE_SND | 단가 SAP 반영 |
| 인도가 변경 | SND | (TRANSPRICE) | IF_TRANSPRICE_SND | 인도가 SAP 반영 |
| 입찰고정가 | SND | (BIDFIX) | IF_BIDFIX_SND | 고정가 SAP 반영 |
| 매입/입고 | SND | GR_PUR_SAP | IF_GR_SND | 매입 송신 |
| 보조금 정산 | SND | CSISSHD | IF_SUBSIDY_SND | 정산결과 송신 |
| 정산 마감정보 | SND | CLOSE_INFO_SAP | IF_CLOSE_SND | 마감 송신 |

> 연계 미들웨어: **INDIGO**(EAI). 실시간 호출 + 배치(스테이징) 병행. *현대화 시 INDIGO 계승 vs REST 재설계는 미결정 — 별도 인터페이스 설계서(05-interface.md)에서 확정.*

---

## 5. DB 설계 완료 — 전체 요약

| Part | 범위 | 문서 |
|------|------|------|
| 1 | 설계표준·전체 테이블 인벤토리(~85)·CM_/LOG_ 기반 | 03-db-design.md |
| (공통) | 문서 상태전이(전진+되돌리기) 모델 | 03-db-design-status-transition.md |
| 2 | PR/RX/AU/PO/GR 구매 트랜잭션 | 03-db-design-part2-procurement.md |
| 3 | CT/IP/TP/CL/PF 단가·정산(갭정산) | 03-db-design-part3-pricing-settlement.md |
| 4 | VD/IT 협력사·품목 마스터 | 03-db-design-part4-vendor-item.md |
| 5 | QL/AP/ED/IF 품질·결재·전자계약·인터페이스 | 03-db-design-part5-quality-approval-edoc-if.md |

## 다음 단계 (설계→구현 준비)
1. **물리 DDL 생성**: 위 논리설계 → Oracle `CREATE TABLE` 스크립트(`db/ddl/*.sql`, 도메인별), 제약/인덱스/COMMENT 포함
2. 공통코드(CM_CODE) 초기 데이터(상태·구분 코드값)
3. 인터페이스 설계서(05-interface.md): INDIGO 계승 vs REST 재설계 확정
4. 화면 목록·메뉴(04-screen-list.md)
5. 도메인 구현 착수 순서 확정
