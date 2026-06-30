-- =============================================================
-- 구매시스템 SEED - 53. 실적/분석 메뉴
-- =============================================================
INSERT INTO CM_MENU (MENU_ID, MENU_NM, UP_MENU_ID, MENU_LVL, URL, ICON, SORT_NO, REG_ID) VALUES ('ANALYSIS', '분석/실적', NULL, 1, NULL, 'bar-chart', 8, 'SYSTEM');
INSERT INTO CM_MENU (MENU_ID, MENU_NM, UP_MENU_ID, MENU_LVL, URL, ICON, SORT_NO, REG_ID) VALUES ('AN_REPORT', '실적 리포트', 'ANALYSIS', 2, '/report', 'bar-chart', 1, 'SYSTEM');
INSERT INTO CM_ROLE_FUNC (ROLE_ID, MENU_ID, AUTH_CD, REG_ID) VALUES ('ADMIN', 'AN_REPORT', 'MANAGEMENT', 'SYSTEM');
INSERT INTO CM_ROLE_FUNC (ROLE_ID, MENU_ID, AUTH_CD, REG_ID) VALUES ('USER', 'AN_REPORT', 'SAVE', 'SYSTEM');
COMMIT;
