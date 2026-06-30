-- =============================================================
-- 구매시스템 DDL - 74. (3c) 소싱 유찰(FAIL) 분리 + 재공고
--   기존 CANCEL(유찰/취소 혼합) → 취소 / FAIL(유찰) 분리, 통계·재공고 추적 가능
-- =============================================================

-- 라벨 정리: CANCEL = 취소
UPDATE CM_CODE SET CD_NM_KO='취소' WHERE GRP_CD='RFX_STS' AND CD='CANCEL';
UPDATE CM_CODE SET CD_NM_KO='취소' WHERE GRP_CD='AUCTION_STS' AND CD='CANCEL';

-- 유찰(FAIL) 상태 추가
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('RFX_STS', 'FAIL', '유찰', 8, 'SYSTEM');
INSERT INTO CM_CODE (GRP_CD, CD, CD_NM_KO, SORT_NO, REG_ID) VALUES ('AUCTION_STS', 'FAIL', '유찰', 8, 'SYSTEM');

-- 전이규칙: 공고(OPEN) → 유찰(FAIL), 사유필수
INSERT INTO CM_STATUS_FLOW (DOC_TYP, FROM_STS, ACTION, TO_STS, ACTION_NM, DIRECTION, RSN_REQ_YN, SORT_NO, REG_ID)
  VALUES ('RX', 'OPEN', 'FAIL', 'FAIL', '유찰처리', 'END', 'Y', 8, 'SYSTEM');
INSERT INTO CM_STATUS_FLOW (DOC_TYP, FROM_STS, ACTION, TO_STS, ACTION_NM, DIRECTION, RSN_REQ_YN, SORT_NO, REG_ID)
  VALUES ('AC', 'OPEN', 'FAIL', 'FAIL', '유찰처리', 'END', 'Y', 8, 'SYSTEM');

COMMIT;
