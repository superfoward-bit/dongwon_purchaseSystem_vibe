-- =============================================================
-- 구매시스템 DDL - 64. (C1) 과세구분 체계
--   품목/거래라인 과세구분(TAX_TYP) + VAT 계산을 과세구분 기반으로 전환
--   식품사 면세(농수산물)·영세율 처리 지원
-- =============================================================

-- ---------- 공통코드: 과세구분 ----------
INSERT INTO CM_CODE_GRP (GRP_CD, GRP_NM, REG_ID) VALUES ('TAX_TYP', '과세구분', 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('TAX_TYP', 'TAX',  '과세',   1, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('TAX_TYP', 'FREE', '면세',   2, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('TAX_TYP', 'ZERO', '영세율', 3, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('TAX_TYP', 'NOND', '불공제', 4, 'SYSTEM');

-- ---------- 거래라인 과세구분 컬럼 (IT_ITEM 은 기존 TAX_TYP 보유) ----------
ALTER TABLE PR_ITEM         ADD (TAX_TYP VARCHAR2(18 CHAR) DEFAULT 'TAX');
ALTER TABLE PO_ORDER_ITEM   ADD (TAX_TYP VARCHAR2(18 CHAR) DEFAULT 'TAX');
ALTER TABLE GR_RECEIPT_ITEM ADD (TAX_TYP VARCHAR2(18 CHAR) DEFAULT 'TAX');
ALTER TABLE PA_ADJUST       ADD (TAX_TYP VARCHAR2(18 CHAR) DEFAULT 'TAX');

COMMENT ON COLUMN PO_ORDER_ITEM.TAX_TYP IS '과세구분(TAX과세/FREE면세/ZERO영세율/NOND불공제)';
COMMENT ON COLUMN GR_RECEIPT_ITEM.TAX_TYP IS '과세구분(품목/발주에서 전파)';

-- ---------- 샘플: 면세 품목 1건 지정(검증용 - 농수산물 가정) ----------
UPDATE IT_ITEM SET TAX_TYP = 'FREE' WHERE ITEM_CD = 'IT-00002';

COMMIT;
