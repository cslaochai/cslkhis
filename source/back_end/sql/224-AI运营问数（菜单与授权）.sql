-- =============================================================================
-- 224 · AI 运营问数（菜单与授权）
--
-- 【这一刀是什么】
-- docs/AI能力施工手册.md P0 期的 operation_qa 能力：管理者的自然语言问题
-- 翻译成受控 SELECT（白名单 11 张经营表、只读、闸门十道），页面
-- views/ai/OperationQaView.vue。没有这刀菜单，接口在但没人进得来。
--
-- 【菜单挂哪】
-- 挂「报表统计」(1200) 下，与 BI 驾驶舱 (906)、运营统计报表 (1201) 同级，sort_order=46。
-- 它回答的是经营统计问题，读者是院领导与管理员，挂报表目录是对的。
--
-- 【权限码为什么是 ai:operationQa:*】
-- ask 是本页独有的业务能力（模型调用有成本），list 是页面本体码。
-- 按钮 2941 只发 ask —— 看得到页面的人不一定用得起问数（授权时可以单独摘）。
--
-- 【id 段】
-- 菜单 2940/2941（避开 sql/201 的 2935~2937）；role_menu id 派生
-- 70000+menu_id*10+role_id（落在 99400~99499 空闲段；注意「基数+menu_id」式派生
-- 在多角色下同一菜单两行同 id，必撞主键）。
--
-- 【执行后必做】
-- 权限集合走 Redis：his:perm:role:{roleCode} TTL 10min。SQL 旁路插菜单后必须清缓存
-- （G5b 已踩：不清缓存就是旧集合，新菜单点了被守卫弹回）。
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 0. 前置检查：父目录 1200 必须存在
-- -----------------------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM sys_menu WHERE id = 1200 AND del_flag = 0 AND menu_type = 1) t_parent,
  (SELECT COUNT(*) FROM sys_role WHERE id = 18 AND del_flag = 0)                     t_leader;
-- 期望：t_parent=1 t_leader=1；任一项不符先别往下执行。

-- -----------------------------------------------------------------------------
-- 1. 菜单（2940）+ 按钮（2941）
--    三键守卫：id / menu_key / permission 任一已存在都跳过（dev 库多会话共用，
--    静默 0 行比报错更危险，跑完必须看自检）。menu_key 唯一索引不覆盖 del_flag，
--    删菜单一律物理删。
-- -----------------------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, del_flag, remark)
SELECT x.id, x.menu_name, 1200, x.sort_no, x.menu_type, x.path, x.component, x.menu_key,
       x.icon, x.permission, 0, 0, x.is_visible, 1, 'sql224', NOW(), 0, x.remark
FROM (
       SELECT 2940 id, 'AI 运营问数' menu_name, 46 sort_no, 2 menu_type, '/ai-operation-qa' path,
              'views/ai/OperationQaView.vue' component, 'ai.operationQa' menu_key,
              'DataAnalysis' icon, 'ai:operationQa:list' permission, 1 is_visible,
              'AI 运营问数：自然语言翻译成白名单经营表的受控 SELECT，只读' remark
  UNION ALL
       SELECT 2941, '问数查询', 1, 3, '', '', 'ai.operationQa.ask',
              '', 'ai:operationQa:ask', 1,
              '发起问数查询（模型调用有成本，授权时可单独摘）' remark
) x
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu e
  WHERE e.id = x.id OR (e.menu_key = x.menu_key AND x.menu_key <> '')
     OR (e.permission = x.permission AND x.permission <> '')
);

-- -----------------------------------------------------------------------------
-- 2. 授权：系统管理员（role_id=1）+ 院领导（role_id=18）
--    id 派生 70000+menu_id*10+role_id：同一菜单两个角色的行 id 各不相同，
--    「基数+menu_id」式派生在多角色下必撞主键（9940/9941 就是这么炸的）
-- -----------------------------------------------------------------------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time)
SELECT 70000 + m.id * 10 + r.id, r.id, m.id, 'sql224', NOW()
FROM sys_menu m
JOIN sys_role r ON r.id IN (1, 18) AND r.del_flag = 0
WHERE m.id IN (2940, 2941) AND m.del_flag = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- -----------------------------------------------------------------------------
-- 3. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
SELECT 'M1_两条菜单都在' item, COUNT(*) cnt, 2 expect
  FROM sys_menu WHERE id IN (2940, 2941) AND del_flag = 0
UNION ALL SELECT 'M2_都挂在有效目录下', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id IN (2940, 2941) AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_menu p WHERE p.id = m.parent_id AND p.del_flag = 0 AND p.menu_type = 1)
UNION ALL SELECT 'M3_页面path与component齐全', COUNT(*), 0
  FROM sys_menu WHERE id = 2940 AND del_flag = 0
    AND (path IS NULL OR path = '' OR component IS NULL OR component = '')
UNION ALL SELECT 'M4_menu_key冲突', COUNT(*), 0
  FROM (SELECT menu_key FROM sys_menu WHERE menu_key IN ('ai.operationQa','ai.operationQa.ask')
          AND del_flag = 0 GROUP BY menu_key HAVING COUNT(*) > 1) x
UNION ALL SELECT 'M5_每条菜单都有角色', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id IN (2940, 2941) AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.menu_id = m.id)
UNION ALL SELECT 'M6_权限码与控制器一致', COUNT(*), 2
  FROM sys_menu WHERE id IN (2940, 2941) AND del_flag = 0
    AND permission IN ('ai:operationQa:list','ai:operationQa:ask')
UNION ALL SELECT 'M7_授权行4条（2菜单x2角色）', COUNT(*), 4
  FROM sys_role_menu WHERE menu_id IN (2940, 2941);
-- 期望：M1=2、M6=2、M7=4，其余全 0（cnt 列 = 异常行数）。
-- M4=2 的读法：cnt 是重复组数，两条 menu_key 各应唯一 → 0。

-- 回滚（物理删，menu_key 唯一索引不覆盖 del_flag，不能软删）：
-- DELETE FROM sys_role_menu WHERE menu_id IN (2940, 2941);
-- DELETE FROM sys_menu WHERE id IN (2940, 2941);
