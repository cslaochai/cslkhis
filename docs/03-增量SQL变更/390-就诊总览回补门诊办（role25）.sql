-- ============================================================================
-- 390 就诊总览回补门诊办（role 25 ← menu 204 + 祖先目录 200）
-- 日期：2026-11-26
-- ============================================================================
-- 背景：
--   389 已把「就诊总览」(menu 204, perm=opd:todayVisits:list) 从一线 role 2 医生、
--   role 9 分诊护士摘除；该台账定位属门诊办 P15a。本脚本把 204 回补给门诊办 role 25。
--
--   断链处理：204 挂在一级目录 200「门（急）诊管理」下，role 25 当前未授 200 及其下
--   任何叶子。只授叶子不授祖先目录，侧栏挂不上（347 断链口径），故一并补授目录 200；
--   目录为分组容器，不随授目录放开 200 下其他叶子。
--
-- 数据范围：role 25 门诊办 data_scope=全部，全院台账视角；后端 /queue/opdLogListPage、
--   /queue/opdLogStats 已按 DeptScopeService 收口兜底，本步仅菜单层归属落地。
--
-- 注意：
--   · 只 INSERT sys_role_menu 授权，不动 sys_menu 本身。
--   · 唯一键 uk_role_menu(role_id, menu_id)，ON DUPLICATE KEY UPDATE 幂等。
--   · id 段 9210000000000001219–1220，接续 387 的 1201–1218，避开 388 占用的 8xx 段。
--   · 执行后需清权限缓存（角色 10035 对应 role_code 与受影响用户身份快照）；本次由
--     人工自清 Redis，脚本不代跑。
-- ============================================================================

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
  (9210000000000001219, 25, 200, 'menu-govern-390', NOW(), 'menu-govern-390', NOW(), 0, 0),
  (9210000000000001220, 25, 204, 'menu-govern-390', NOW(), 'menu-govern-390', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 回查：
-- SELECT role_id, menu_id FROM sys_role_menu WHERE role_id=25 AND menu_id IN (200,204);  -- 期望 2 行
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id=204 ORDER BY role_id;        -- 期望 1、25

-- 回滚：
-- DELETE FROM sys_role_menu WHERE role_id=25 AND menu_id IN (200,204) AND create_by='menu-govern-390';
