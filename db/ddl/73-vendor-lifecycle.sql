-- =============================================================
-- 구매시스템 DDL - 73. (3b) 협력사 라이프사이클
--   거래중지 사유·일자·이력, 감시 RISK등급 마스터 환류, 평가 차기일/시정조치 기한
-- =============================================================

ALTER TABLE VD_VENDOR ADD (
  STOP_YMD      DATE,                 -- 거래중지일
  STOP_REASON   VARCHAR2(500 CHAR),   -- 거래중지 사유
  RISK_GRADE    VARCHAR2(18 CHAR),    -- 리스크등급(감시 환류: SAFE/WATCH/RISK)
  NEXT_EVAL_YMD DATE                  -- 차기 평가예정일
);
COMMENT ON COLUMN VD_VENDOR.STOP_REASON IS '거래중지 사유';
COMMENT ON COLUMN VD_VENDOR.RISK_GRADE IS '리스크등급(감시 결과 환류)';
COMMENT ON COLUMN VD_VENDOR.NEXT_EVAL_YMD IS '차기 평가예정일';

-- 거래중지/재개 이력
CREATE TABLE VD_STOP_HIS (
  ID         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  VENDOR_ID  NUMBER NOT NULL,
  VD_CD      VARCHAR2(18 CHAR),
  ACTION     VARCHAR2(18 CHAR) NOT NULL,   -- STOP중지/RESUME재개
  REASON     VARCHAR2(500 CHAR),
  ACTOR_ID   VARCHAR2(18 CHAR),
  ACTION_DT  TIMESTAMP(0) WITH LOCAL TIME ZONE DEFAULT SYSTIMESTAMP
);
COMMENT ON TABLE VD_STOP_HIS IS '협력사 거래중지/재개 이력';
CREATE INDEX IX_VD_STOP_HIS_1 ON VD_STOP_HIS (VENDOR_ID);

-- 감시: 시정조치 기한·상태
ALTER TABLE VD_AUDIT ADD (
  ACTION_DUE_YMD DATE,                       -- 시정조치 완료기한
  ACTION_STS     VARCHAR2(18 CHAR)           -- 시정조치 상태(OPEN/DONE)
);
COMMENT ON COLUMN VD_AUDIT.ACTION_DUE_YMD IS '시정조치 완료기한';

COMMIT;
