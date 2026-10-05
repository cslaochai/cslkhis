-- =============================================================================
-- 225 · AI 管理台（菜单与授权）
--
-- 【这一刀是什么】
-- docs/AI能力施工手册.md P1 期的「AI 管理台」一页收口（G-03 + G-04）：
--   · 调用审计签页：sys_ai_call_log 的分页浏览（能力/状态/模型/耗时/token/摘要），
--     此前 getAiAuditLogPage 有接口无页面；
--   · 知识库问答签页：/ai/knowledge/ask 前端首次接线（RAG 只科普不判定）；
--   · 知识库维护签页：录入/删除/重建索引/灌示例语料（ai:knowledge:manage）。
-- 页面 views/ai/AiAdminView.vue，路由 /ai-admin。
--
-- 【菜单挂哪】
-- 挂「系统管理」(1100) 下，与日志审计 (1108) 同域（AI 调用审计是审计账本的一种），
-- sort_order=44 排在电子签名与时间戳 (43) 之后。
--
-- 【权限码】
-- 页面 ai:admin:list；按钮 2943 知识库维护 ai:knowledge:manage ——
-- 维护类接口（AiKnowledgeController 的 ingest/listPage/getById/deleteById/rebuild/seed）
-- 同步从借用的 opd:doctorWorkstation:add 改为本码，语义不再错位。
-- ask 任何登录用户可用，不设码。
--
-- 【id 段】
-- 菜单 2942/2943（避开 sql/224 的 2940/2941）；role_menu id 派生
-- 70000+menu_id*10+role_id（99421/99431，与 sql/224 的 99400~99418 错开）。
--
-- 【执行后必做】
-- 清权限缓存 his:perm:role:*（workspace/_clear_perm_cache.mjs），再回查自检。
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 0. 前置检查：父目录 1100 必须存在
-- -----------------------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM sys_menu WHERE id = 1100 AND del_flag = 0 AND menu_type = 1) t_parent,
  (SELECT COUNT(*) FROM sys_role WHERE id = 1 AND del_flag = 0)                      t_admin;
-- 期望：t_parent=1 t_admin=1；任一项不符先别往下执行。

-- -----------------------------------------------------------------------------
-- 1. 菜单（2942）+ 按钮（2943）
--    三键守卫：id / menu_key / permission 任一已存在都跳过；跑完必须看自检。
-- -----------------------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, del_flag, remark)
SELECT x.id, x.menu_name, x.parent_id, x.sort_no, x.menu_type, x.path, x.component, x.menu_key,
       x.icon, x.permission, 0, 0, x.is_visible, 1, 'sql225', NOW(), 0, x.remark
FROM (
       SELECT 2942 id, 'AI 管理台' menu_name, 1100 parent_id, 44 sort_no, 2 menu_type, '/ai-admin' path,
              'views/ai/AiAdminView.vue' component, 'ai.admin' menu_key,
              'Monitor' icon, 'ai:admin:list' permission, 1 is_visible,
              'AI 管理台：调用审计 + 知识库问答/维护（施工手册 P1，G-03/G-04）' remark
  UNION ALL
       SELECT 2943, '知识库维护', 2942, 1, 3, '', '', 'ai.knowledge',
              '', 'ai:knowledge:manage', 1,
              '知识文档录入/删除/重建索引/灌语料（ask 不需要此码）' remark
) x
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu e
  WHERE e.id = x.id OR (e.menu_key = x.menu_key AND x.menu_key <> '')
     OR (e.permission = x.permission AND x.permission <> '')
);

-- -----------------------------------------------------------------------------
-- 2. 授权：系统管理员（role_id=1）
--    按 sql/100 规则，授页面即自动含全部按钮码；其他角色要用走角色管理界面勾选。
-- -----------------------------------------------------------------------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time)
SELECT 70000 + m.id * 10 + r.id, r.id, m.id, 'sql225', NOW()
FROM sys_menu m
JOIN sys_role r ON r.id IN (1) AND r.del_flag = 0
WHERE m.id IN (2942, 2943) AND m.del_flag = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- -----------------------------------------------------------------------------
-- 3. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
SELECT 'M1_两条菜单都在' item, COUNT(*) cnt, 2 expect
  FROM sys_menu WHERE id IN (2942, 2943) AND del_flag = 0
UNION ALL SELECT 'M2_都挂在有效目录下', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id IN (2942, 2943) AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_menu p WHERE p.id = m.parent_id AND p.del_flag = 0
                      AND p.menu_type = IF(m.id = 2942, 1, 2))
UNION ALL SELECT 'M3_页面path与component齐全', COUNT(*), 0
  FROM sys_menu WHERE id = 2942 AND del_flag = 0
    AND (path IS NULL OR path = '' OR component IS NULL OR component = '')
UNION ALL SELECT 'M4_menu_key冲突', COUNT(*), 0
  FROM (SELECT menu_key FROM sys_menu WHERE menu_key IN ('ai.admin','ai.knowledge')
          AND del_flag = 0 GROUP BY menu_key HAVING COUNT(*) > 1) x
UNION ALL SELECT 'M5_每条菜单都有角色', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id IN (2942, 2943) AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.menu_id = m.id)
UNION ALL SELECT 'M6_权限码与控制器一致', COUNT(*), 2
  FROM sys_menu WHERE id IN (2942, 2943) AND del_flag = 0
    AND permission IN ('ai:admin:list','ai:knowledge:manage')
UNION ALL SELECT 'M7_授权行2条（2菜单x1角色）', COUNT(*), 2
  FROM sys_role_menu WHERE menu_id IN (2942, 2943);
-- 期望：M1=2、M6=2、M7=2，其余全 0（cnt 列 = 异常行数）。

-- 回滚（物理删，menu_key 唯一索引不覆盖 del_flag，不能软删）：
-- DELETE FROM sys_role_menu WHERE menu_id IN (2942, 2943);
-- DELETE FROM sys_menu WHERE id IN (2942, 2943);
