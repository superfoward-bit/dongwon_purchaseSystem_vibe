-- =============================================================
-- 구매시스템 DDL - 71. 품질 규격 정량화
--   검사 규격 상·하한 + 측정값 수치화 + 시험분류 → 자동 합부판정
-- =============================================================

INSERT INTO CM_CODE_GRP (GRP_CD, GRP_NM, REG_ID) VALUES ('INSP_ITEM_TYP', '시험분류', 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('INSP_ITEM_TYP', 'PHYS',      '이화학',   1, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('INSP_ITEM_TYP', 'MICRO',     '미생물',   2, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('INSP_ITEM_TYP', 'SENSORY',   '관능',     3, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('INSP_ITEM_TYP', 'FOREIGN',   '이물',     4, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('INSP_ITEM_TYP', 'PESTICIDE', '잔류농약', 5, 'SYSTEM');

ALTER TABLE QL_INSPECTION_RESULT ADD (
  ITEM_TYP    VARCHAR2(18 CHAR),       -- 시험분류
  SPEC_LOWER  NUMBER(20,5),            -- 규격 하한
  SPEC_UPPER  NUMBER(20,5),            -- 규격 상한
  MEASURE_NUM NUMBER(20,5),            -- 측정값(수치)
  TEST_METHOD VARCHAR2(100 CHAR)       -- 시험방법
);
COMMENT ON COLUMN QL_INSPECTION_RESULT.SPEC_LOWER IS '규격 하한';
COMMENT ON COLUMN QL_INSPECTION_RESULT.SPEC_UPPER IS '규격 상한';
COMMENT ON COLUMN QL_INSPECTION_RESULT.MEASURE_NUM IS '측정값(수치, 자동판정용)';

COMMIT;
