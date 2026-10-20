-- =====================================================================
-- 278-DIP付费-菜单908冲突修正：DIP分组模拟菜单改挂空闲id 910
-- ---------------------------------------------------------------------
-- 背景：275 脚本原用菜单 id='908'，但仓库内 908 已被「绩效与成本核算」
-- （menu_key=finance.performance、permission=report:perf:list）占用，
-- 275 的守卫 NOT EXISTS 因 id='908' 已存在而静默跳过，导致 DIP 分组模拟
-- 菜单与 report:dip:list 权限码未落库（链路上 /report/dip/* 接口 403）。
-- 本脚本将 DIP 分组模拟菜单以空闲 id='910' 补插并授权角色 1/17/18
-- （与 907/908/909 同一口径），可重复执行（守卫判重）。
-- 执行后 DIP 模拟入口：菜单「报表统计」→「DIP 分组模拟」（/dipsim）。
-- =====================================================================

-- -------------------------------------------------------------
-- 1. 菜单 910「DIP 分组模拟」（镜像 275 原 908 的列集与取值，id 换 910）
-- -------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id, create_time, update_time)
SELECT '910', 'DIP 分组模拟', '1200', 21, 2, '/dipsim', 'drg/DipSimView', 'report.dipsim', 'icon-suanfa',
       'report:dip:list', 0, 0, 1, 1,
       'admin', 'admin', 0, 'DIP 分组模拟（P2/P3）；原 908 被绩效与成本核算占用，278 修正挂 910', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = '910' OR menu_key = 'report.dipsim' OR permission = 'report:dip:list');

-- -------------------------------------------------------------
-- 2. 角色授权 1/17/18（镜像 275 的 sys_role_menu 列集）
-- -------------------------------------------------------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2780000000000000901', '1', '910', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '910');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2780000000000000902', '17', '910', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '17' AND menu_id = '910');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2780000000000000903', '18', '910', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '18' AND menu_id = '910');

COMMIT;

-- 回查（人工核对用，勿删）：
-- SELECT id, menu_name, menu_key, permission FROM sys_menu WHERE id IN ('908','909','910');
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = '910';
