-- =============================================================
-- 구매시스템 DDL - 67. (C5+C8) 발주·입고 보강
--   납품지/입고창고, 요청납기 vs 확정납기, 분할납품 일정, 거래명세서번호
-- =============================================================

-- ---------- 공통코드: 창고 ----------
INSERT INTO CM_CODE_GRP (GRP_CD, GRP_NM, REG_ID) VALUES ('WH', '창고', 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('WH', 'WH01', '본사물류창고', 1, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('WH', 'WH02', '음성공장창고', 2, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('WH', 'WH03', '원료창고',     3, 'SYSTEM');

-- ---------- 발주 헤더: 납품지/입고창고/요청납기 ----------
-- (기존 DLV_YMD = 확정납기로 의미 유지)
ALTER TABLE PO_ORDER ADD (
  DLV_PLACE    VARCHAR2(300 CHAR),   -- 납품지(주소/장소)
  WH_CD        VARCHAR2(18 CHAR),    -- 입고예정 창고
  REQ_DLV_YMD  DATE                  -- 요청납기(희망)
);
COMMENT ON COLUMN PO_ORDER.DLV_PLACE IS '납품지';
COMMENT ON COLUMN PO_ORDER.WH_CD IS '입고예정 창고';
COMMENT ON COLUMN PO_ORDER.REQ_DLV_YMD IS '요청납기(희망)';
COMMENT ON COLUMN PO_ORDER.DLV_YMD IS '확정납기';

-- ---------- 발주 품목: 입고창고/요청납기 ----------
ALTER TABLE PO_ORDER_ITEM ADD (
  WH_CD        VARCHAR2(18 CHAR),
  REQ_DLV_YMD  DATE
);

-- ---------- 분할납품 일정 ----------
CREATE TABLE PO_DLV_SCHEDULE (
  ID         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  ORDER_ID   NUMBER NOT NULL,
  PO_ITEM_ID NUMBER,
  LINE_NO    NUMBER NOT NULL,
  ITEM_CD    VARCHAR2(18 CHAR),
  ITEM_NM    VARCHAR2(300 CHAR),
  PLAN_QTY   NUMBER(20,5) DEFAULT 0,
  PLAN_YMD   DATE,
  DLV_PLACE  VARCHAR2(300 CHAR),
  STS        VARCHAR2(18 CHAR) DEFAULT 'PLAN',  -- PLAN/DONE
  REMARK     VARCHAR2(500 CHAR),
  REG_ID     VARCHAR2(18 CHAR),
  REG_DT     TIMESTAMP(0) WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP,
  CONSTRAINT UX_PO_DLV_SCHEDULE_1 UNIQUE (ORDER_ID, LINE_NO),
  CONSTRAINT FK_PO_DLV_SCHEDULE_1 FOREIGN KEY (ORDER_ID) REFERENCES PO_ORDER (ID)
);
COMMENT ON TABLE PO_DLV_SCHEDULE IS '발주 분할납품 일정';

-- ---------- 입고 헤더: 입고창고/거래명세서번호 ----------
ALTER TABLE GR_RECEIPT ADD (
  WH_CD     VARCHAR2(18 CHAR),    -- 입고창고
  DELY_NO   VARCHAR2(50 CHAR)     -- 거래명세서(납품서) 번호
);
COMMENT ON COLUMN GR_RECEIPT.WH_CD IS '입고창고';
COMMENT ON COLUMN GR_RECEIPT.DELY_NO IS '거래명세서번호';

COMMIT;
