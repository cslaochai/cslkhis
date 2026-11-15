-- ============================================================================
-- 329 统一待办（门户目录下新增页面「统一待办」）
--
-- 背景：消息待办（sys_message，handle_status=0）与流程待办（biz_flow_task，task_status=PENDING）
--       原本分散在「门户→消息待办」的两个页签里，同一件事两处都能处置，口径容易漂。
--       这里只做「查询层聚合」：新开一张列表页把两类待办合并展示、按来源列区分、点行走各自处置；
--       **底层两张表一张不动**（不建聚合表、不搬数据、不合并状态机）。
--
-- 落地方式（前端聚合，非后端聚合）：
--   两个来源各自调既有接口（/message/listPage?handleStatus=0、/workflow/runtime/listMyTasks），
--   各取满 200 条后在页面内存里按时间倒序合并 + 前端分页。
--   待办是「当下要办的事」，量级几十条；超过 200 条应当加工商/加催办，而不是靠假分页掩盖。
--   后续若要真分页/跨模块聚合，再在 his-core-spi 加一个取数端口（workflow 侧实现），由平台模块聚合。
--
-- 权限：
--   页面行 permission 留空（权限码只落 menu_type=3 按钮行，避免三处各存一份）。
--   页面自身的可见性 = 「角色有没有被授这条菜单」；行内动作复用既有两个按钮码：
--     workflow:task:handle（29642，父级 102）→ 流程待办「通过/驳回」
--     portal:messages:edit（2001，父级 102）→ 消息待办「处理」
--   本文件只授超管（role 1）；其余角色在「角色管理→菜单权限」里勾。
--
-- ⚠ 执行后清角色权限缓存（TTL 10 分钟，不清会看不到新菜单）：
--     node workspace/_clear_perm_cache.mjs
-- 回滚：
--   DELETE FROM sys_role_menu WHERE menu_id = 29654;
--   DELETE FROM sys_menu WHERE id = 29654;
-- ============================================================================

-- 1. 页面「统一待办」（门户目录 100，紧挨消息待办；sort_order 与 102 相同，靠 id 排在其后）
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag, remark)
SELECT 29654, 100, '统一待办', 2, 2, '/todos', 'todos/UnifiedTodoView', 'portal.todo', 'List',
       NULL, 0, 1, 1, 1, 'sql329', NOW(), 0, '消息待办+流程待办聚合列表（查询层聚合，底层分表不动）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29654)
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/todos');

-- 2. 授超管
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892632810000000007, 1, 29654, 'sql329', NOW(), 'sql329', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29654);

-- 3. 回查：期望 1 行菜单 + role 1 的 1 行授权
SELECT m.id, m.parent_id, m.menu_name, m.menu_type, m.path, m.component, m.sort_order
FROM sys_menu m
WHERE m.id = 29654;

SELECT rm.role_id, rm.menu_id FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 29654;
