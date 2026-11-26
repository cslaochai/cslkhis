-- 383 信息科管理员摘除患者主索引
-- 日期：2026-11-26
-- 背景：348「流程共享页多岗位授权」批量授权时，把「患者中心(700) + 患者主索引(702)」一起带给了
-- role33 信息科管理员。信息科是平台运维岗，不接触患者诊疗数据；且 702 的权限码 patient:empi:list
-- 是 PatientIndexController 类级 @PreAuthorize 的准入码，等于把患者档案查询接口一并开了。
-- 处置：删除 role33 对 700/702 的授权（患者中心目录随之不再出现在其菜单里）。
-- 其他角色不动：role1 超管、role14 病案编码员按各自业务需要保留。

DELETE FROM sys_role_menu WHERE role_id = 33 AND menu_id IN (700, 702);

-- 回滚（348 原始行，如需恢复逐条执行）：
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- VALUES (9210000000000000775, 33, 702, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
--        (9210000000000000776, 33, 700, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0);

-- 回查：
-- SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 33 AND menu_id IN (700, 702);   -- 期望 0
-- SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 33;                            -- 期望 22
