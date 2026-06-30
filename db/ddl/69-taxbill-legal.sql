-- =============================================================
-- 구매시스템 DDL - 69. (C7b) 전자세금계산서 법정항목 + 품목명세
--   국세청 승인번호, 작성/발행일, 공급자·공급받는자 사업자정보, 영세율, 수정세금계산서, 품목명세
-- =============================================================

ALTER TABLE TX_TAXBILL ADD (
  NTS_APPROVAL_NO VARCHAR2(24 CHAR),   -- 국세청 승인번호(법정)
  WRITE_YMD       DATE,                -- 작성일자
  ISSUE_YMD       DATE,                -- 발행일자
  SUP_BIZ_NO      VARCHAR2(20 CHAR),   -- 공급자 사업자번호
  SUP_NM          VARCHAR2(200 CHAR),  -- 공급자 상호
  SUP_CEO         VARCHAR2(100 CHAR),  -- 공급자 대표
  SUP_ADDR        VARCHAR2(300 CHAR),
  BUY_BIZ_NO      VARCHAR2(20 CHAR),   -- 공급받는자(자사) 사업자번호
  BUY_NM          VARCHAR2(200 CHAR),
  BUY_CEO         VARCHAR2(100 CHAR),
  BUY_ADDR        VARCHAR2(300 CHAR),
  BILL_KIND       VARCHAR2(18 CHAR) DEFAULT 'NORMAL',  -- NORMAL일반/MODIFY수정
  ORIG_BILL_NO    VARCHAR2(30 CHAR),   -- 수정세금계산서 원본번호
  MODIFY_RSN      VARCHAR2(200 CHAR)   -- 수정사유
);
COMMENT ON COLUMN TX_TAXBILL.NTS_APPROVAL_NO IS '국세청 승인번호';
COMMENT ON COLUMN TX_TAXBILL.BILL_TYP IS '과세구분(TAX과세/FREE면세/ZERO영세율)';
COMMENT ON COLUMN TX_TAXBILL.BILL_KIND IS '종류(NORMAL일반/MODIFY수정)';

-- ---------- 품목명세 ----------
CREATE TABLE TX_TAXBILL_ITEM (
  ID         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  BILL_ID    NUMBER NOT NULL,
  LINE_NO    NUMBER NOT NULL,
  ITEM_CD    VARCHAR2(18 CHAR),
  ITEM_NM    VARCHAR2(300 CHAR),
  SPEC       VARCHAR2(300 CHAR),
  QTY        NUMBER(20,5) DEFAULT 0,
  UNIT_PRC   NUMBER(20,5) DEFAULT 0,
  SUPPLY_AMT NUMBER(20,5) DEFAULT 0,
  VAT_AMT    NUMBER(20,5) DEFAULT 0,
  REMARK     VARCHAR2(500 CHAR),
  REG_ID     VARCHAR2(18 CHAR),
  REG_DT     TIMESTAMP(0) WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP,
  CONSTRAINT UX_TX_TAXBILL_ITEM_1 UNIQUE (BILL_ID, LINE_NO),
  CONSTRAINT FK_TX_TAXBILL_ITEM_1 FOREIGN KEY (BILL_ID) REFERENCES TX_TAXBILL (ID)
);
COMMENT ON TABLE TX_TAXBILL_ITEM IS '세금계산서 품목명세';

-- ---------- 자사 법인 사업자정보 보강(데모) ----------
UPDATE CM_COMPANY SET BIZ_NO='124-81-00998', CEO_NM='대표이사', ADDR='서울특별시 동작구 대방동'
 WHERE COMP_CD='1000';
-- 협력사 샘플 사업자정보 보강
UPDATE VD_VENDOR SET BIZ_NO=NVL(BIZ_NO,'220-88-12345'), CEO_NM=NVL(CEO_NM,'협력사대표') WHERE VD_CD='VD-00001';

COMMIT;
