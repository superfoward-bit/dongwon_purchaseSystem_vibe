-- =============================================================
-- 구매시스템 DDL - 68. (C7a) 회계전표 복식부기 다라인 + 계정코드
--   단일 DR/CR/AMT → AC_SLIP_LINE(다중 차/대변) + 계정과목코드 + 부가세대급금 분리
-- =============================================================

-- ---------- 공통코드: 계정과목 ----------
INSERT INTO CM_CODE_GRP (GRP_CD, GRP_NM, REG_ID) VALUES ('ACCT', '계정과목', 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('ACCT', '14900', '원재료',        1, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('ACCT', '14600', '상품',          2, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('ACCT', '13500', '부가세대급금',  3, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('ACCT', '25100', '외상매입금',    4, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('ACCT', '13300', '선급금',        5, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('ACCT', '53500', '매입할인·조정', 6, 'SYSTEM');

-- ---------- 전표 헤더 보강: 회계기간/ERP전표번호 ----------
ALTER TABLE AC_SLIP ADD (
  FISCAL_YM   VARCHAR2(6 CHAR),     -- 회계기간(전기 귀속연월)
  ERP_DOC_NO  VARCHAR2(30 CHAR)     -- ERP(SAP) 전표번호 회수
);
COMMENT ON COLUMN AC_SLIP.FISCAL_YM IS '회계기간(YYYYMM)';
COMMENT ON COLUMN AC_SLIP.ERP_DOC_NO IS 'ERP 전표번호(전송 회신)';

-- ---------- 전표 명세(복식부기 다라인) ----------
CREATE TABLE AC_SLIP_LINE (
  ID        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  SLIP_ID   NUMBER NOT NULL,
  LINE_NO   NUMBER NOT NULL,
  DR_CR     CHAR(1 CHAR) NOT NULL,        -- D차변 / C대변
  ACCT_CD   VARCHAR2(18 CHAR),
  ACCT_NM   VARCHAR2(100 CHAR),
  AMT       NUMBER(20,5) DEFAULT 0,
  REMARK    VARCHAR2(500 CHAR),
  REG_ID    VARCHAR2(18 CHAR),
  REG_DT    TIMESTAMP(0) WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP,
  CONSTRAINT UX_AC_SLIP_LINE_1 UNIQUE (SLIP_ID, LINE_NO),
  CONSTRAINT FK_AC_SLIP_LINE_1 FOREIGN KEY (SLIP_ID) REFERENCES AC_SLIP (ID)
);
COMMENT ON TABLE AC_SLIP_LINE IS '회계전표 명세(차/대변)';
COMMENT ON COLUMN AC_SLIP_LINE.DR_CR IS '차대구분(D차변/C대변)';

COMMIT;
