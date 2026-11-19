-- ============================================================================
-- 333 消息中心退出侧栏（全量消息并入统一待办）
--
-- 背景：331/332 之后消息中心只查不办，但门户下仍并排挂着「消息中心」与「统一待办」，
--       界面观感依旧是两个同类入口。既然办理只剩统一待办一处，消息中心的列表并入它的页签即可。
--
-- 做法（沿用 sql/191、311 的合并先例）：被合掉的菜单只置 is_visible = 0
--   —— 退出侧栏，但 menu 行、path、menu_key、权限码、角色授权一律不动：
--   · guard 按「path 在不在自己菜单树里」判权，行还在树里 ⇒ /messages 老书签仍可达
--     （页面已降级为兼容壳，内容与统一待办页签同源）
--   · 按钮 2001（portal:messages:edit，挂在 102 下）继续下发 ⇒ 统一待办 / Header 抽屉的「处理」不受影响
--   · 33 个角色的 102 授权不动 ⇒ 回滚只需把 is_visible 改回 1
--
-- 页面：全量消息列表并进统一待办的「数据范围 = 全部消息」一档（334 进一步把 29654 铺到全部角色）；
--       /messages 改成路由重定向到 /todos?scope=ALL，页面文件不再单独维护（一份实现，不漂移）
--
-- ⚠ 执行后清角色权限缓存（菜单需重新下发）：node workspace/_clear_perm_cache.mjs
-- 回滚：
--   UPDATE sys_menu SET is_visible = 1, remark = NULL, update_by = 'sql333-rollback', update_time = NOW()
--    WHERE id = 102;
-- ============================================================================

-- ---------- 1. 消息中心退出侧栏 ----------
UPDATE sys_menu
SET is_visible  = 0,
    remark      = '已并入统一待办(29654)的全部消息视图；侧栏入口关闭，2001 按钮码不动',
    update_by   = 'sql333',
    update_time = NOW()
WHERE id = 102
  AND is_visible = 1;

-- ---------- 2. 回查 ----------
-- 2.1 102 应为 is_visible=0；2001 仍是它的子按钮且 desc 不变
SELECT id, parent_id, menu_name, menu_type, path, permission, is_visible, remark
FROM sys_menu
WHERE id IN (102, 2001)
ORDER BY id;

-- 2.2 角色授权没被动过（期望与执行前一致，当前 33）
SELECT COUNT(*) AS role_grants_102
FROM sys_role_menu
WHERE menu_id = 102;

-- 2.3 门户下可见菜单（期望只剩 工作台 / 统一待办 / 急诊值班）
SELECT id, menu_name, path, sort_order
FROM sys_menu
WHERE parent_id = 100
  AND is_visible = 1
  AND status = 1
ORDER BY sort_order, id;
