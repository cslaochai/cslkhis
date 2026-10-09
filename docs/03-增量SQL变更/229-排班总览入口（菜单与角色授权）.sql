-- =============================================================================
-- 229 - 排班总览入口（菜单 + 角色授权）
--
-- 【前提】 sql/200（排班核心表）与 sql/201（排班三模块菜单）必须已跑完：
--   本文件只挂菜单，不建表；父目录锚定沿用 sql/201 的 806 口径。
--
-- 【要解决的问题】
--   排班域已有 6 个入口（804 门诊排班 / 806 总值班排班 / 331 护理排班 /
--   2935 全院岗位排班 / 2936 值守点位 / 2937 人力配置标准），各自都是编辑工作台，
--   但没有一屏能回答「全院这一周排成什么样了：谁在岗、哪个单元缺人、总值班漏没漏」。
--   本文件接入「排班总览」页（/schedule-overview，只读驾驶舱）：数据全部来自
--   事实层聚合（biz_staff_schedule / biz_schedule / biz_duty_roster / biz_staff_plan_rule），
--   不放任何写入口 —— 编辑仍走各自工作台。
--
-- 【本文件做什么】
--   1. 新增一条二级菜单 2938 排班总览 /schedule-overview，与 2935~2937 同目录。
--   2. 授权口径与 sql/201 一致：取现有已授权 org:schedule:list 的角色 + 管理员 10012。
--   3. 幂等：菜单行 ON DUPLICATE KEY UPDATE，授权行 NOT EXISTS 去重。
--
-- 【刻意不做】
--   - 不动 804 / 806 / 331 三条老排班菜单的归属（2026-09-29 盘点待拍板；
--     若日后收口成「排班中心」父目录，单独出脚本，不在总览入口里夹带）。
--   - 不新建按钮菜单（menu_type=3）：总览是只读页，权限沿用既有 org:schedule:list。
--
-- 【回滚】
--   DELETE FROM sys_role_menu WHERE menu_id = 2938;
--   DELETE FROM sys_menu WHERE id = 2938;
--   （sys_menu.menu_key 的唯一索引不覆盖 del_flag，删除一律物理删，不能软删）
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 0. 前置检查：sql/200 的表与 806 锚点必须在
-- -----------------------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_staff_schedule') t_data,
  (SELECT COUNT(*) FROM sys_menu WHERE id = 806 AND del_flag = 0) t_anchor_806;
-- 期望：t_data=1 t_anchor_806=1；任一项为 0 先别往下执行。

-- -----------------------------------------------------------------------------
-- 1. 菜单放哪儿（与 sql/201 同口径：由 806 锚定，不跨环境写死目录 id）
-- -----------------------------------------------------------------------------
SET @parent_id := COALESCE(
  (SELECT parent_id FROM sys_menu WHERE id = 806 AND del_flag = 0),
  (SELECT id FROM sys_menu WHERE menu_type = 1 AND del_flag = 0 AND id = 800),
  NULL);

SET @base_sort := COALESCE((SELECT MAX(sort_order) FROM sys_menu WHERE parent_id = @parent_id AND del_flag = 0), 0);

-- -----------------------------------------------------------------------------
-- 2. 菜单行（menu_type=2）
-- -----------------------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, del_flag, remark)
SELECT 2938, '排班总览', @parent_id, @base_sort + 1, 2, '/schedule-overview',
       'views/schedule/ScheduleOverview.vue', 'org:scheduleOverview',
       'TrendCharts', 'org:schedule:list', 0, 0, 1, 1, 'sql229', NOW(), 0,
       '排班域只读驾驶舱：门诊号源/在岗人次/人力缺口/总值班按周一屏聚合，编辑仍走各自工作台'
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  sort_order = VALUES(sort_order),
  path = VALUES(path),
  component = VALUES(component),
  icon = VALUES(icon),
  permission = VALUES(permission),
  remark = VALUES(remark),
  update_by = 'sql229',
  update_time = NOW(),
  del_flag = 0;

-- -----------------------------------------------------------------------------
-- 3. 角色授权（与 2935/2937 同码 org:schedule:list）
-- -----------------------------------------------------------------------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time)
SELECT DISTINCT
       896791000000000000 + ROW_NUMBER() OVER (ORDER BY t.role_id),
       t.role_id, 2938, 'sql229', NOW()
FROM (
  SELECT rm.role_id
  FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
  WHERE m.permission = 'org:schedule:list' AND m.del_flag = 0
  UNION
  SELECT r.id FROM sys_role r WHERE r.id = 10012 AND r.del_flag = 0
) t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu x WHERE x.role_id = t.role_id AND x.menu_id = 2938
);

-- -----------------------------------------------------------------------------
-- 4. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
SELECT 'M1_菜单存在' item, COUNT(*) cnt, 1 expect
  FROM sys_menu WHERE id = 2938 AND del_flag = 0
UNION ALL SELECT 'M2_挂在有效目录下', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id = 2938 AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_menu p WHERE p.id = m.parent_id AND p.del_flag = 0 AND p.menu_type = 1)
UNION ALL SELECT 'M3_path与component齐全', COUNT(*), 0
  FROM sys_menu WHERE id = 2938 AND del_flag = 0
    AND (path IS NULL OR path = '' OR component IS NULL OR component = '')
UNION ALL SELECT 'M4_授权行存在', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id = 2938 AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.menu_id = m.id)
UNION ALL SELECT 'M5_同目录icon重复', COUNT(*), 0
  FROM (SELECT icon FROM sys_menu WHERE parent_id = @parent_id AND del_flag = 0
          AND icon = 'TrendCharts' GROUP BY icon HAVING COUNT(*) > 1) y
UNION ALL SELECT 'M6_同目录排序不打架', COUNT(*), 0
  FROM (SELECT sort_order FROM sys_menu WHERE parent_id = @parent_id AND del_flag = 0
          GROUP BY sort_order HAVING COUNT(*) > 1) z;

-- 期望：M1=1 且 M2~M6 全 0。
-- 跑完清权限缓存：DEL his:perm:role:*（TTL 10 分钟，不清就是旧集合）。
