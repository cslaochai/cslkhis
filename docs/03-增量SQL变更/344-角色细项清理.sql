-- 344 角色细项清理
-- 日期：2026-11-20
--   18 院领导 摘 医保付费结算(909)      —— 业务操作页，领导只看报表
--   5  药剂师 摘 抗菌药物处方权管理(2922) —— 处方权授予属医务科
--   26 医务科 补 抗菌药物处方权管理(2922)
--   12 检查技师 摘 标本管理按钮(403)     —— 其无检验工作站页面，按钮无落点
--   7  前台导诊 摘 结算账单查询(2363)    —— 保留收费结算窗口，结算明细归收费/医保
DELETE FROM sys_role_menu WHERE (role_id, menu_id) IN ((18,909),(5,2922),(12,403),(7,2363));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES (9210000000000000696, 26, 2922, 'menu-govern-344', NOW(), 'menu-govern-344', NOW(), 0, 0);

-- 344 回滚
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
 (9210000000000000697, 18, 909, 'rollback-344', NOW(), 'rollback-344', NOW(), 0, 0),
 (9210000000000000698, 5, 2922, 'rollback-344', NOW(), 'rollback-344', NOW(), 0, 0),
 (9210000000000000700, 12, 403, 'rollback-344', NOW(), 'rollback-344', NOW(), 0, 0),
 (9210000000000000701, 7, 2363, 'rollback-344', NOW(), 'rollback-344', NOW(), 0, 0);
DELETE FROM sys_role_menu WHERE create_by='menu-govern-344';
