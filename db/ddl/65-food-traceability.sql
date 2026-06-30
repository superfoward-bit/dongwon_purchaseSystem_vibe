-- =============================================================
-- 구매시스템 DDL - 65. (C2) 식품 추적성 (LOT/제조일자/유통기한)
--   입고(GR)에서 입력 → 검사(QL)·부적합(QC)·클레임(CLM)으로 전파
--   리콜·HACCP·선입선출(FEFO) 대응
-- =============================================================

-- ---------- 입고 품목: 추적성 핵심(원천) ----------
ALTER TABLE GR_RECEIPT_ITEM ADD (
  LOT_NO   VARCHAR2(40 CHAR),   -- 협력사 제품 LOT번호
  MFG_YMD  DATE,                -- 제조일자
  EXP_YMD  DATE                 -- 유통기한(소비기한)
);
COMMENT ON COLUMN GR_RECEIPT_ITEM.LOT_NO IS 'LOT번호(추적성)';
COMMENT ON COLUMN GR_RECEIPT_ITEM.MFG_YMD IS '제조일자';
COMMENT ON COLUMN GR_RECEIPT_ITEM.EXP_YMD IS '유통/소비기한';

-- ---------- 입고검사: 검사대상 LOT 추적 ----------
ALTER TABLE QL_INSPECTION ADD (
  LOT_NO   VARCHAR2(40 CHAR),
  MFG_YMD  DATE,
  EXP_YMD  DATE
);
COMMENT ON COLUMN QL_INSPECTION.LOT_NO IS 'LOT번호(입고에서 전파)';

-- ---------- 부적합: 회수 범위 산정 ----------
ALTER TABLE QC_NONCONF ADD (
  LOT_NO   VARCHAR2(40 CHAR),
  MFG_YMD  DATE,
  EXP_YMD  DATE
);
COMMENT ON COLUMN QC_NONCONF.LOT_NO IS 'LOT번호(부적합 대상 추적)';

-- ---------- 클레임: 대상 LOT 추적 ----------
ALTER TABLE CM_CLAIM ADD (
  LOT_NO   VARCHAR2(40 CHAR),
  MFG_YMD  DATE
);
COMMENT ON COLUMN CM_CLAIM.LOT_NO IS 'LOT번호(클레임 대상 추적)';

COMMIT;
