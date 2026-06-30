-- =============================================================
-- 구매시스템 DDL - 66. (C3+C6) 결제조건 코드화 + 마스터 스냅샷
--   협력사 결제조건/지급방법 코드 → PO·정산에 거래시점 값 박제
--   정산 지급예정일(PAY_DUE_YMD) 자동계산
-- =============================================================

-- ---------- 공통코드: 결제조건 ----------
INSERT INTO CM_CODE_GRP (GRP_CD, GRP_NM, REG_ID) VALUES ('PAY_TERM', '결제조건', 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_TERM', 'IMMED',    '즉시(현금)',       1, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_TERM', 'NET15',    '마감 후 15일',     2, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_TERM', 'NET30',    '마감 후 30일',     3, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_TERM', 'NET60',    '마감 후 60일',     4, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_TERM', 'EOM_NEXT', '월말마감 익월말일', 5, 'SYSTEM');

-- ---------- 공통코드: 지급방법 ----------
INSERT INTO CM_CODE_GRP (GRP_CD, GRP_NM, REG_ID) VALUES ('PAY_METHOD', '지급방법', 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_METHOD', 'TRANSFER', '계좌이체', 1, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_METHOD', 'NOTE',     '어음',     2, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_METHOD', 'CASH',     '현금',     3, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('PAY_METHOD', 'OFFSET',   '상계',     4, 'SYSTEM');

-- ---------- 협력사: 결제조건 코드화 ----------
ALTER TABLE VD_VENDOR ADD (
  PAY_TERM_CD   VARCHAR2(18 CHAR),
  PAY_METHOD_CD VARCHAR2(18 CHAR)
);
COMMENT ON COLUMN VD_VENDOR.PAY_TERM_CD IS '결제조건(지급주기)';
COMMENT ON COLUMN VD_VENDOR.PAY_METHOD_CD IS '지급방법';

-- ---------- 발주: 결제조건 스냅샷(거래시점 박제) ----------
ALTER TABLE PO_ORDER ADD (
  PAY_TERM_CD   VARCHAR2(18 CHAR),
  PAY_METHOD_CD VARCHAR2(18 CHAR)
);

-- ---------- 정산: 결제조건 스냅샷 + 지급예정일 ----------
ALTER TABLE CL_CLOSE ADD (
  PAY_TERM_CD   VARCHAR2(18 CHAR),
  PAY_METHOD_CD VARCHAR2(18 CHAR),
  PAY_DUE_YMD   DATE
);
COMMENT ON COLUMN CL_CLOSE.PAY_DUE_YMD IS '지급예정일(결제조건 기반 자동산출)';

-- ---------- 샘플: VD-00001 결제조건 지정 ----------
UPDATE VD_VENDOR SET PAY_TERM_CD='EOM_NEXT', PAY_METHOD_CD='TRANSFER' WHERE VD_CD='VD-00001';
UPDATE VD_VENDOR SET PAY_TERM_CD='NET30', PAY_METHOD_CD='TRANSFER' WHERE VD_CD='VD-00002';

COMMIT;
