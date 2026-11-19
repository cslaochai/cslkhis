-- ============================================================================
-- 331 消息待办改名「消息中心」+ 统一待办授权补齐（A 方案收口）
--
-- 背景：329 新增「统一待办」(29654) 的出发点就是「同一件事两处都能处置、口径容易漂」，
--       但只做了新增、没撤 102 的「流程待办」页签 —— 流程待办同时挂在
--       消息待办页（页签）与统一待办页（聚合列表），重复没消掉。
-- 本文件把收口做到底：
--   1) 102 改名「消息中心」，职责固定为「全部消息查询与回看」（页面侧已删流程待办页签）
--   2) 给「授了 102 且持有 workflow:task:handle(29642)」的角色补授 29654
--      —— 页签撤掉后，这些角色的流程待办入口必须换到统一待办，否则功能直接丢失；
--         不持有该按钮码的角色不动（其待办处置仍从 Header 抽屉 / 消息中心进入）
--
-- 不改：path `/messages`、menu_key `portal.messages`、permission `portal:messages:view`
--       一律不动（前后端契约；路由与按钮权限都按这些判定，改名不影响）
--
-- ⚠ 执行后清角色权限缓存（TTL 10 分钟，不清会看不到菜单名变化/新授权）：
--     node workspace/_clear_perm_cache.mjs
-- 回滚：
--   UPDATE sys_menu SET menu_name='消息待办', remark=NULL, update_by='sql331-rollback',
--          update_time=NOW() WHERE id=102;
--   DELETE rm FROM sys_role_menu rm WHERE rm.menu_id=29654 AND rm.role_id <> 1;
-- ============================================================================

-- ---------- 1. 菜单改名（幂等：已是「消息中心」时不重复改） ----------
UPDATE sys_menu
SET menu_name   = '消息中心',
    remark      = '全部消息查询与回看；待办处置走门户→统一待办(29654)',
    update_by   = 'sql331',
    update_time = NOW()
WHERE id = 102
  AND menu_name = '消息待办';

-- ---------- 2. 补授 29654（角色集合是动态的，无法用固定 id） ----------
-- id = 当前 MAX(id) + 100 + 行号：INSERT...SELECT 会先物化结果再写表，MAX 取的是插入前快照，
-- 与 uk_role_menu 的 (role_id,menu_id) 守卫配合，重复执行零副作用（不会撞键）。
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu) + 100
           + ROW_NUMBER() OVER (ORDER BY r.role_id),
       r.role_id, 29654, 'sql331', NOW(), 'sql331', NOW(), 1, 1
FROM sys_role_menu r
WHERE r.menu_id = 102
  AND EXISTS (SELECT 1 FROM sys_role_menu a WHERE a.role_id = r.role_id AND a.menu_id = 29642)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = r.role_id AND b.menu_id = 29654)
GROUP BY r.role_id;

-- ---------- 3. 回查 ----------
-- 3.1 菜单名（期望 1 行：消息中心 / 消息待办已不存在）
SELECT id, parent_id, menu_name, menu_type, path, menu_key, permission, remark
FROM sys_menu
WHERE id = 102;

-- 3.2 统一待办授权范围（期望：role 1 + 上面补授的流程待办角色）
SELECT rm.role_id, r.role_name, r.role_code
FROM sys_role_menu rm
         LEFT JOIN sys_role r ON r.id = rm.role_id
WHERE rm.menu_id = 29654
ORDER BY rm.role_id;

-- 3.3 护栏：仍持有 29642 却没有任何待办页面（102 页签已撤，必须落 29654）的角色应为空
SELECT rm.role_id, r.role_name
FROM sys_role_menu rm
         LEFT JOIN sys_role r ON r.id = rm.role_id
WHERE rm.menu_id = 29642
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = 29654);

-- ---------- 4. 可选：把统一待办开给业务角色（当前不执行，属业务决策） ----------
-- 现状：29654 只有 role 1；29642（流程待办处理权）也只有 role 1 —— 所以上面第 2 段命中 0 行是正确的。
-- 要让某岗位真正用上「统一待办」作为待办工作台，按下述模板逐个授（先确认该岗位确实要看待办，别整批授）：
--
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
--                            create_by_id, update_by_id)
-- SELECT 892632810000000200 + :role_id, :role_id, 29654, 'sql331', NOW(), 'sql331', NOW(), 1, 1 FROM DUAL
-- WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = :role_id AND rm.menu_id = 29654);
--
-- ⚠ 只授 29654 不授 29642 时：页面能进，但「流程待办」来源不会被拉取（前端按 workflow:task:handle 判），
--   列表里只有消息待办。要看待流程待办必须同时授 29642。
-- ⚠ 角色 19「患者」(role_code PATIENT) 持有 102，但不应授 29654 —— 那是员工待办工作台。

