-- =============================================================
-- 구매시스템 DDL - 25. 협력사 포털 (사용자-협력사 연결 + 포털 메뉴/역할)
-- =============================================================

-- 사용자에 협력사 코드 연결 (협력사 사용자용)
ALTER TABLE CM_USER ADD (VD_CD VARCHAR2(18 CHAR));
COMMENT ON COLUMN CM_USER.VD_CD IS '협력사코드(협력사 사용자)';

-- 협력사포털 메뉴
INSERT INTO CM_MENU (MENU_ID, MENU_NM, UP_MENU_ID, MENU_LVL, URL, ICON, SORT_NO, REG_ID) VALUES ('PORTAL', '협력사포털', NULL, 1, NULL, 'globe', 1, 'SYSTEM');
INSERT INTO CM_MENU (MENU_ID, MENU_NM, UP_MENU_ID, MENU_LVL, URL, ICON, SORT_NO, REG_ID) VALUES ('PORTAL_RFX', '견적 응찰', 'PORTAL', 2, '/portal/rfx', 'tag', 1, 'SYSTEM');
INSERT INTO CM_MENU (MENU_ID, MENU_NM, UP_MENU_ID, MENU_LVL, URL, ICON, SORT_NO, REG_ID) VALUES ('PORTAL_AU', '역경매 입찰', 'PORTAL', 2, '/portal/au', 'gavel', 2, 'SYSTEM');

-- 협력사 역할 (포털 메뉴만)
INSERT INTO CM_ROLE (ROLE_ID, ROLE_NM, DESCRIPTION, REG_ID) VALUES ('VENDOR', '협력사', '협력사 포털 사용자', 'SYSTEM');
INSERT INTO CM_ROLE_FUNC (ROLE_ID, MENU_ID, AUTH_CD, REG_ID) VALUES ('VENDOR', 'PORTAL_RFX', 'SAVE', 'SYSTEM');
INSERT INTO CM_ROLE_FUNC (ROLE_ID, MENU_ID, AUTH_CD, REG_ID) VALUES ('VENDOR', 'PORTAL_AU', 'SAVE', 'SYSTEM');

COMMIT;
