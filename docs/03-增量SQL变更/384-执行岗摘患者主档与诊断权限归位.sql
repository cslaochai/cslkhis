-- ============================================================
-- 384 执行岗摘除患者主档授权 + 诊断权限归位 + CSSD 岗位改挂
-- 日期：2026-11-26
-- 背景：消毒供应中心岗位挂在「医技人员(6)」，侧栏出现「患者中心→患者管理」，
--       可翻全院患者主档；角色 6 同时混挂了诊断岗的报告写/审/发权限。
-- 内容：
--   A 医技人员(6)/检验技师(11)/检查技师(12) 摘除患者管理(701)及新增(2105)/修改(2106)按钮
--   B 回收上述三角色在「患者中心」目录(700)的祖先授权（已无叶子页引用）
--   C 角色 6 摘除放射诊断：工作站列表(414)+报告书写/审核/发布/模板(2373/2374/2375/2376)，
--     角色 20 放射诊断医师已持有，不重复补
--   D 角色 6 摘除心电诊断：报告书写/审核/发布/模板(2396/2397/2398/2399)；
--     心电列表(417)与签到采集(2395)属执行动作，保留在角色 6
--   E 角色 20 补心电诊断权限：417 列表 + 2396/2397/2398/2399 报告写审发模板
--   F 消毒供应中心(19580094) 4 名员工岗位由角色 6 改挂角色 39（设备与供应室管理员）
-- 回滚：见 384-回滚.sql
-- ============================================================

-- A 摘执行岗患者主档页面与写按钮
DELETE FROM sys_role_menu WHERE (role_id, menu_id) IN
  ((6, 701),  (6, 2105),  (6, 2106),
   (11, 701), (11, 2105), (11, 2106),
   (12, 701), (12, 2105), (12, 2106));

-- B 回收患者中心目录祖先授权（三角色在 700 下已无其他叶子页）
DELETE FROM sys_role_menu WHERE role_id IN (6, 11, 12) AND menu_id = 700;

-- C 角色 6 摘放射诊断权限（角色 20 已持有）
DELETE FROM sys_role_menu WHERE role_id = 6 AND menu_id IN (414, 2373, 2374, 2375, 2376);

-- D 角色 6 摘心电诊断权限（采集 2395、列表 417 保留给执行岗）
DELETE FROM sys_role_menu WHERE role_id = 6 AND menu_id IN (2396, 2397, 2398, 2399);

-- E 角色 20 补心电诊断权限
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
  (9210000000000001011, 20, 417,  'menu-govern-384', NOW(), 'menu-govern-384', NOW(), 0, 0),
  (9210000000000001021, 20, 2396, 'menu-govern-384', NOW(), 'menu-govern-384', NOW(), 0, 0),
  (9210000000000001022, 20, 2397, 'menu-govern-384', NOW(), 'menu-govern-384', NOW(), 0, 0),
  (9210000000000001023, 20, 2398, 'menu-govern-384', NOW(), 'menu-govern-384', NOW(), 0, 0),
  (9210000000000001024, 20, 2399, 'menu-govern-384', NOW(), 'menu-govern-384', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- F CSSD 岗位改挂角色 39（限消毒供应中心科室、当前仍是角色 6 的主/副岗位行）
UPDATE sys_employee_post
SET role_id = 39, update_by = 'menu-govern-384', update_time = NOW()
WHERE dept_id = 19580094 AND role_id = 6
  AND employee_id IN (897001369, 897001370, 897001371, 897001372);

-- G 回查（执行后人工核对）
-- SELECT rm.role_id, r.role_name, m.menu_id, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_role r ON r.id=rm.role_id JOIN sys_menu m ON m.id=rm.menu_id
--  WHERE rm.role_id IN (6,11,12) AND rm.menu_id IN (700,701,2105,2106);            -- 期望空
-- SELECT rm.role_id, m.menu_id, m.permission FROM sys_role_menu rm JOIN sys_menu m ON m.id=rm.menu_id
--  WHERE rm.role_id=6 AND (m.permission LIKE 'medtech:radio%' OR m.permission LIKE 'medtech:ecg:%');
-- SELECT CAST(employee_id AS CHAR) eid, role_id, dept_id FROM sys_employee_post
--  WHERE employee_id IN (897001369,897001370,897001371,897001372);                    -- 期望 role_id=39

-- H 清缓存（改库后必须）：Redis 删除 his:perm:role:* 与 his:user:identity:*
