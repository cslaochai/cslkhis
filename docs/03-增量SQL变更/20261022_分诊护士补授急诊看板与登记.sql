-- =====================================================================
-- 20261022 分诊护士(role 9)补授急诊看板查看 + 急诊登记
-- ---------------------------------------------------------------------
-- 背景：
--   急诊到院的标准第一入口是预检分诊台：护士登记建档、测生命体征、按 I-IV 分级
--   定红黄绿区，再引导就诊；医生只负责接诊处置。原铺底把急诊工作站菜单 205 与
--   登记按钮 2010 只授给 1 系统管理员 / 8 急诊医生，专职 role 9 分诊护士（急诊内/外/
--   儿科各配 2 人）反而进不去急诊工作站，分工倒置。本文件给 role 9 补齐只读看板 +
--   登记定级权限。
--
-- 授权边界（最小集）：
--   ✓ 205 急诊工作站查看        opd:emergency:list（看板列表 /list）
--   ✓ 2010 急诊登记建档+定级     opd:emergency:add（/emergency/register）
--   ✗ 2011 接诊/状态流转         opd:emergency:edit（归急诊医生，不授护士）
--   ✗ 2601 交班清零             opd:emergency:handover（归值班医生/护士长，不授护士）
--
-- 幂等：uk_role_menu(role_id,menu_id)，INSERT IGNORE 重复执行零变化。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或退出重登；
--    role 9 当前无缓存 key，新登录会直查库，通常即时生效。
-- =====================================================================

-- ---------- 角色授权：role 9 分诊护士 ← 205 查看 / 2010 登记 ----------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
  (8920610200000000101, 9, 205,  'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610200000000102, 9, 2010, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 回查（应只见 205 / 2010，不见 2011 / 2601） ----------
-- SELECT rm.role_id, m.id, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 9 AND m.id IN (205,2010,2011,2601)
--  ORDER BY m.id;
