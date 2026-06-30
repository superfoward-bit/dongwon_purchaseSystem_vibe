-- =============================================================
-- 구매시스템 SEED - 38. RFP 평가입찰 + 역경매 자동연장
-- =============================================================

-- 가격점수 방식 코드
INSERT INTO CM_CODE_GRP (GRP_CD, GRP_NM, REG_ID) VALUES ('RFX_PRICE_METHOD', 'RFP 가격점수 방식', 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('RFX_PRICE_METHOD', 'MR', '최저가 비율(최저가/제시가)', 1, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('RFX_PRICE_METHOD', 'LN', '선형(최고-제시)/(최고-최저)', 2, 'SYSTEM');

COMMIT;
