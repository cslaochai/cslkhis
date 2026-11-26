-- ============================================================================
-- 389 就诊总览菜单收口（role 2 医生 / role 9 分诊护士摘除 204）
-- 日期：2026-11-26
-- ============================================================================
-- 背景：
--   「就诊总览」(menu 204, perm=opd:todayVisits:list) 是跨科室回溯分析台账
--   （默认本月、多状态统计卡片 + 就诊列表），定位属门诊办/质控口径。一线医生、
--   分诊护士的今日工作由 203 医生工作站 / 202 分诊工作站 / 205 急诊护士工作站覆盖，
--   不需要再挂回溯台账。
--
--   历史已收两轮：259 收回前台导诊(role 7)、303 收回急诊医生(role 8) 的 204。
--   本次延续同一口径，收回剩余两个一线角色：
--     · role 2  10013 医生        —— 现持 204（行 id 2101955980530122758）
--     · role 9  10020 分诊护士    —— 现持 204（行 id 9007）
--   role 1 系统管理员(data_scope=1 全院) 保留，不动。
--
-- 配套后端改动（同一批提交，已在 QueueServiceImpl 落地）：
--   /queue/opdLogListPage、/queue/opdLogStats 原先未做科室数据范围收口，受限角色
--   不传科室即可拉全院流水；现已按 DeptScopeService 收口（授权科室 ∩ 所选科室，
--   空交集返回空页/空统计）。后端兜底已生效，本 SQL 是菜单层冗余收紧：即便后续
--   误授，受限角色也看不到别科数据。
--
-- 标准模型已同步：
--   106《角色岗位菜单标准模型》P01/P02 示例页已去掉「就诊总览」，归入 P15a 门诊办
--   台账包；107《纠偏清单》role 9 / role 25 行口径同步更新。
--
-- 注意：
--   · 只删 sys_role_menu 授权，不动 sys_menu 本身；其他角色不受影响。
--   · 执行后必须清权限缓存：DEL his:perm:role:10013 his:perm:role:10020
--     （或重启后端），否则旧授权仍在 Redis 里，菜单/按钮不消失。
-- ============================================================================

DELETE FROM sys_role_menu
WHERE menu_id = 204
  AND role_id IN (2, 9);

-- 回滚（如需恢复，逐条执行；id 取自删除前快照）：
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time)
-- VALUES (2101955980530122758, 2, 204, 'admin', NOW(), 'admin', NOW()),
--        (9007, 9, 204, 'admin', NOW(), 'admin', NOW());

-- 回查：
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = 204;   -- 期望仅剩 role_id=1
-- SELECT COUNT(*) FROM sys_role_menu WHERE menu_id = 204 AND role_id IN (2, 9);  -- 期望 0
