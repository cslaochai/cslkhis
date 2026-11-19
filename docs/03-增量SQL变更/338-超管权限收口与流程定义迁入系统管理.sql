-- ============================================================
-- 338 超管（role_id=1 / 10012 系统管理员）业务菜单只读收口 + 流程定义/演练控制台迁入系统管理
-- 日期：2026-11-17
-- 目的（最终口径）：
--   ① 超级管理员对业务域菜单【可见但只读】：业务目录、页面照常出现在侧栏、可进入浏览查询，
--      但收回全部业务写操作按钮（新增/修改/删除/审核/提交/作废等）；
--   ② 系统管理域目录、页面、写按钮全部保留，超管可正常增删改；
--   ③ 工作流「流程定义」、故障演练「演练控制台」两个页面迁入系统管理；其原顶级空壳停用。
-- 鉴权模型（已核对后端 Controller 实证）：
--   · 页面菜单(menu_type=2)自带查询码（xxx:list / xxx:view），列表/详情/统计接口挂该码；
--   · 按钮菜单(menu_type=3)是写操作码（xxx:add / :edit / :delete / :audit …），v-perm 显隐 + 后端 @PreAuthorize 兜底；
--   · 因此保留目录+页面授权即「可浏览」，收回按钮授权即「不能操作」，后端 Java 代码无需改动。
-- 白名单按钮（17 个，role_id=1 唯一保留的写操作码，均在系统管理/流程定义/演练控制台子树）：
--   用户 2148 add / 2149 edit / 2150 delete；角色 2151 add / 2152 delete；
--   菜单 2153 add / 2154 delete；字典 2155 add / 2156 edit / 2157 delete；
--   参数 2158 add；日志 2510 export / 29624 签名记录；部门 2933 add / 2934 delete；
--   AI 知识库 2943；流程定义配置 29641。
-- 范围边界：
--   · 只动 role_id=1，其他 35 个角色授权一律不碰；
--   · 临床医嘱字典/组套族（2383/2384/2385/2386/2387/2388/2389/2390）按定稿归住院临床
--     配置域，目录/页面/按钮均不授超管，菜单物理迁移属后续归位批次；
--   · 业务目录/页面授权以改库前备份 555 条为基准恢复（目录 34 + 页面 176），不在本脚本内逐行展开；
--   · 29651 工作流、29910 故障演练两个原顶级迁空后停用（status=0 / is_visible=0），不物理删除。
-- 避坑：
--   · sys_role_menu 唯一键 uk_role_menu(role_id, menu_id)，补授用 NOT EXISTS 判重；
--   · 按钮收口 DELETE 带 role_id=1 守卫 + 白名单 NOT IN，绝不波及其他角色；
--   · 菜单迁移只调 parent_id / sort_order，不改 path/component/permission/menu_key。
-- 前置：无前端改动，纯菜单树 + 授权数据调整。
-- 落地后：务必清角色权限/身份缓存（Redis TTL 10min，SQL 改库不自动失效）：
--         清 his:perm:role:10012 与 his:user:identity:*，或超管重新登录（前端重拉 /auth/info）。
-- 回滚：见文件末尾「回滚段」；改库前 555 条 role1 授权已备份至
--         workspace/_backup_role1_menu_20261117_123215.csv 与 _rollback_role1_menu_20261117_123215.sql。
-- ============================================================

-- A 菜单迁移（不改路由/组件/权限码）
-- A1 流程定义(29652)：工作流(29651) → 系统管理(1100)，排配置段；按钮 29641 父级跟随
UPDATE sys_menu
SET parent_id = 1100, sort_order = 47, update_time = NOW()
WHERE id = 29652 AND parent_id = 29651;

-- A2 演练控制台(29911)：故障演练(29910) → 系统管理(1100)，紧随流程定义
UPDATE sys_menu
SET parent_id = 1100, sort_order = 48, update_time = NOW()
WHERE id = 29911 AND parent_id = 29910;

-- A3 两个迁空的原顶级停用（先回查确认无子节点，期望 0；MySQL 1093：UPDATE 同表不能在 WHERE 子查询再查同表）：
-- SELECT COUNT(*) FROM sys_menu WHERE parent_id=29651 AND del_flag=0;  -- 期望 0
-- SELECT COUNT(*) FROM sys_menu WHERE parent_id=29910 AND del_flag=0;  -- 期望 0
UPDATE sys_menu SET status = 0, is_visible = 0, update_time = NOW() WHERE id = 29651;
UPDATE sys_menu SET status = 0, is_visible = 0, update_time = NOW() WHERE id = 29910;

-- B 按钮收口：role_id=1 仅保留 17 个白名单按钮，其余业务写按钮全部收回
--   （目录 type=1 / 页面 type=2 不在本步删除，业务菜单保持可见只读）
DELETE FROM sys_role_menu
WHERE role_id = 1
  AND menu_id IN (
      SELECT id FROM sys_menu WHERE menu_type = 3
  )
  AND menu_id NOT IN (
      2148,2149,2150,2151,2152,2153,2154,
      2155,2156,2157,2158,2510,2933,2934,
      2943,29624,29641
  );

-- C 补齐白名单按钮授权（含备份漏授的 2150 用户管理-删除）；NOT EXISTS 防重复，幂等可重跑
SET @rm_id := (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), 1, b.menu_id, 'sql338', NOW(), 'sql338', NOW(), 0, 0
FROM (
    SELECT 2148 AS menu_id UNION SELECT 2149 UNION SELECT 2150 UNION SELECT 2151 UNION SELECT 2152
    UNION SELECT 2153 UNION SELECT 2154 UNION SELECT 2155 UNION SELECT 2156 UNION SELECT 2157
    UNION SELECT 2158 UNION SELECT 2510 UNION SELECT 2933 UNION SELECT 2934
    UNION SELECT 2943 UNION SELECT 29624 UNION SELECT 29641
) b
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = 1 AND x.menu_id = b.menu_id);

-- D 回查（可单独执行）
-- D1 role1 授权类型分布（期望 目录 34 / 页面 176 / 按钮 17，共 227）
-- SELECT m.menu_type, COUNT(*) c FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id WHERE rm.role_id = 1 GROUP BY m.menu_type;

-- D2 保留按钮清单（期望 17，全部落在系统管理/流程定义/演练控制台子树）
-- SELECT rm.menu_id, m.menu_name, m.permission FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id WHERE rm.role_id = 1 AND m.menu_type = 3 ORDER BY rm.menu_id;

-- D3 业务按钮残留（期望 0 行）：role1 持有的按钮不在白名单内
-- SELECT rm.menu_id, m.menu_name, m.permission FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id WHERE rm.role_id = 1 AND m.menu_type = 3
-- AND rm.menu_id NOT IN (
--   2148,2149,2150,2151,2152,2153,2154,2155,2156,2157,2158,
--   2510,2933,2934,2943,29624,29641);

-- D4 迁入菜单与停用顶级状态
-- SELECT id, menu_name, parent_id, sort_order, status, is_visible FROM sys_menu
-- WHERE id IN (29652,29641,29911,29651,29910);

-- ============================================================
-- 回滚段
--   1) 授权整体恢复：执行备份回滚脚本
--      workspace/_rollback_role1_menu_20261117_123215.sql
--      （先 DELETE role_id=1 全部，再按备份逐行 INSERT 回原 id，目录/页面/按钮全量复原）。
--   2) 菜单退回原父级、恢复显示：
-- UPDATE sys_menu SET parent_id = 29651, sort_order = 1, update_time = NOW() WHERE id = 29652;
-- UPDATE sys_menu SET parent_id = 29910, sort_order = 1, update_time = NOW() WHERE id = 29911;
-- UPDATE sys_menu SET status = 1, is_visible = 1, update_time = NOW() WHERE id = 29651;
-- UPDATE sys_menu SET status = 1, is_visible = 1, update_time = NOW() WHERE id = 29910;
-- ============================================================
