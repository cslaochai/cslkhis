-- =============================================================================
-- 201 - 排班三模块入口（菜单 + 角色授权）
--
-- 【前提】 sql/200（排班核心表）必须已经跑完 —— 本文件依赖它建的
--   biz_staff_schedule / biz_duty_post / biz_staff_plan_rule 三张表，自己不建表，只接菜单。
--
-- 【要解决的问题】
--   sql/200 之后 his-system 已有三个新控制器：
--     · /system/staffSchedule   全院岗位排班（listPage / upsert / swap / copyRange / onDuty / changeLogList）
--     · /system/dutyPost        值守点位（selectList / listPage / upsert / deleteById）
--     · /system/staffPlanRule   人力配置标准（listPage / upsert / deleteById）
--   三个都带 @PreAuthorize（G5 口径），但 sys_menu 里没有一行指向它们 ——
--   后端有接口、前端有页面（2026-10-02 新增 StaffScheduleView / DutyPostView / StaffPlanRuleView），
--   中间缺一座桥：**没有菜单 = 除了手工敲 URL 谁也进不去，也没有任何角色被授权。**
--   这是 2026-09-29 那轮「非医生排班是数据孤岛」的同一处病：后端先跑，入口没跟上。
--
-- 【本文件做什么】
--   1. 新增三条二级菜单：2935 全院岗位排班 /staff-schedule、2936 值守点位 /duty-post、
--      2937 人力配置标准 /staff-plan-rule。
--   2. parent_id **不写死父目录 id**：一律取「806 总值班排班」当前所在的目录，
--      新菜单与原有排班菜单同目录。800 这类目录 id 换个环境可能不一致，写死会插出孤儿菜单。
--   3. 授权：不写死角色清单 —— 取现有已授权 org:schedule:list / org:duty:list 的角色
--      （「谁能看排班」这件事 sys_role_menu 里已经有答案了），再补上管理员（10012）兜底。
--   4. 幂等：菜单行 ON DUPLICATE KEY UPDATE（改名称/路径/组件后可重跑对齐），
--      授权行 NOT EXISTS 去重，重复执行不产生第二份。
--
-- 【刻意不做】
--   - 不动 804 / 806 / 331 三条老排班菜单的归属（2026-09-29 盘点过，老王当时未拍板合并）。
--   - 不新建按钮菜单（menu_type=3）：三个控制器用的全是**既有**权限码 org:schedule:* / org:duty:*，
--     前端 v-perm 查的是权限集合，不需要新按钮码占位。
--   - 不铺演示数据。
--
-- 【component 口径】
--   本环境前端路由由 menu.path 直接匹配 router/index.js 的 children.path，
--   component 列留给后端/运维核对页面文件用，填 views/ 开头的相对路径。
--   若实际存量口径不同：改下面 x.component 三处后**整文件重跑**即可（ON DUPLICATE KEY UPDATE 会对齐）。
--
-- 【回滚】
--   DELETE FROM sys_role_menu WHERE menu_id IN (2935,2936,2937);
--   DELETE FROM sys_menu WHERE id IN (2935,2936,2937);
--   （sys_menu.menu_key 的唯一索引不覆盖 del_flag，删除一律物理删，不能软删）
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 0. 前置检查：sql/200 必须先跑。表不在就别往下走 —— 插了一半菜单又回滚比不插更乱
-- -----------------------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_staff_schedule')  t_data,
  (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_duty_post')        t_post,
  (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_staff_plan_rule')  t_rule,
  (SELECT COUNT(*) FROM sys_menu WHERE id = 806 AND del_flag = 0)                                                          t_anchor_806;
-- 期望：t_data=1 t_post=1 t_rule=1 t_anchor_806=1；任一项不符说明 sql/200 没跑或 806 被删，先别往下执行。

-- -----------------------------------------------------------------------------
-- 1.1 菜单放哪儿（由 806 锚定，别跨环境写死目录 id）
-- 与 806 总值班排班同目录；806 不在时回落到一级目录 800（组织与资源），再没有就是 NULL（自检会报错）
SET @parent_id := COALESCE(
  (SELECT parent_id FROM sys_menu WHERE id = 806 AND del_flag = 0),
  (SELECT id FROM sys_menu WHERE menu_type = 1 AND del_flag = 0 AND id = 800),
  NULL);

-- 组内排序：挂到目标目录现有最后一条之后，避免排序打架导致新菜单藏在首页看不到
SET @base_sort := COALESCE((SELECT MAX(sort_order) FROM sys_menu WHERE parent_id = @parent_id AND del_flag = 0), 0);

-- -----------------------------------------------------------------------------
-- 1. 三条二级菜单（menu_type=2）
-- -----------------------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, del_flag, remark)
SELECT x.id, x.menu_name, @parent_id, @base_sort + x.sort_no, 2, x.path, x.component, x.menu_key,
       x.icon, x.permission, 0, 0, 1, 1, 'sql201', NOW(), 0, x.remark
FROM (
            SELECT 2935 id, '全院岗位排班' menu_name, 1 sort_no, '/staff-schedule' path,
                   'views/schedule/StaffScheduleView.vue' component, 'org:staffSchedule' menu_key,
                   -- 不用 Calendar：与同目录里既有的「排班管理」撞图标（自检 M7 会挂）。
                   -- 这三个模块的本体是「岗位 × 日期 × 班次」的一张网格，用 Grid。
                   'Grid' icon, 'org:schedule:list' permission,
                   'sql/200 排班核心表：谁-哪天-在哪个单元-什么班-出不出勤。/onDuty 只要求登录（此刻谁在岗是打电话用的公共信息）' remark
  UNION ALL SELECT 2936, '值守点位', 2, '/duty-post',
                   'views/duty/DutyPostView.vue', 'org:dutyPost',
                   'Aim', 'org:duty:list',
                   'sql/200：值守点位定义。位先存在、人由排班进位，替代 biz_duty_roster 里人与位混排的旧口径'
  UNION ALL SELECT 2937, '人力配置标准', 3, '/staff-plan-rule',
                   'views/schedule/StaffPlanRuleView.vue', 'org:staffPlanRule',
                   'Tools', 'org:schedule:list',
                   'sql/200：单元x班次x岗位的人数上下限。低于最低在岗会拦住排班保存，不是报表'
) x
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  sort_order = VALUES(sort_order),
  path = VALUES(path),
  component = VALUES(component),
  icon = VALUES(icon),
  permission = VALUES(permission),
  remark = VALUES(remark),
  update_by = 'sql201',
  update_time = NOW(),
  del_flag = 0;

-- -----------------------------------------------------------------------------
-- 2. 角色授权
--    · org:schedule:* 菜单（2935/2937）→ 现有已授权 org:schedule:list 的角色 + 管理员 10012
--    · org:duty:* 菜单（2936）      → 现有已授权 org:duty:list 的角色 + 管理员 10012
--    不写死角色清单：换环境角色不同，写死就会「某些角色看得到某个版本」这种对不齐的锅
-- -----------------------------------------------------------------------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time)
SELECT DISTINCT
       896781000000000000 + ROW_NUMBER() OVER (ORDER BY t.role_id, t.menu_id),
       t.role_id, t.menu_id, 'sql201', NOW()
FROM (
  SELECT rm.role_id, 2935 menu_id
  FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
  WHERE m.permission = 'org:schedule:list' AND m.del_flag = 0
  UNION
  SELECT rm.role_id, 2937
  FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
  WHERE m.permission = 'org:schedule:list' AND m.del_flag = 0
  UNION
  SELECT rm.role_id, 2936
  FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
  WHERE m.permission = 'org:duty:list' AND m.del_flag = 0
  UNION
  SELECT r.id, 2935 FROM sys_role r WHERE r.id = 10012 AND r.del_flag = 0
  UNION
  SELECT r.id, 2936 FROM sys_role r WHERE r.id = 10012 AND r.del_flag = 0
  UNION
  SELECT r.id, 2937 FROM sys_role r WHERE r.id = 10012 AND r.del_flag = 0
) t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu x WHERE x.role_id = t.role_id AND x.menu_id = t.menu_id
);

-- -----------------------------------------------------------------------------
-- 3. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
SELECT 'M1_三条菜单都在' item, COUNT(*) cnt, 3 expect
  FROM sys_menu WHERE id IN (2935, 2936, 2937) AND del_flag = 0
UNION ALL SELECT 'M2_都挂在有效目录下', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id IN (2935, 2936, 2937) AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_menu p WHERE p.id = m.parent_id AND p.del_flag = 0 AND p.menu_type = 1)
UNION ALL SELECT 'M3_path与component齐全', COUNT(*), 0
  FROM sys_menu WHERE id IN (2935, 2936, 2937) AND del_flag = 0
    AND (path IS NULL OR path = '' OR component IS NULL OR component = '')
UNION ALL SELECT 'M4_menu_key唯一性冲突', COUNT(*), 0
  FROM (SELECT menu_key FROM sys_menu WHERE menu_key IN ('org:staffSchedule','org:dutyPost','org:staffPlanRule')
          AND del_flag = 0 GROUP BY menu_key HAVING COUNT(*) > 1) x
UNION ALL SELECT 'M5_每条菜单都有角色', COUNT(*), 0
  FROM sys_menu m
  WHERE m.id IN (2935, 2936, 2937) AND m.del_flag = 0
    AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.menu_id = m.id)
UNION ALL SELECT 'M6_孤儿授权', COUNT(*), 0
  FROM sys_role_menu rm LEFT JOIN sys_menu m ON m.id = rm.menu_id
  WHERE rm.menu_id IN (2935, 2936, 2937) AND m.id IS NULL
UNION ALL SELECT 'M7_同目录icon重复', COUNT(*), 0
  FROM (SELECT icon FROM sys_menu WHERE parent_id = @parent_id AND del_flag = 0
          AND icon IN ('Grid','Aim','Tools') GROUP BY icon HAVING COUNT(*) > 1) y
UNION ALL SELECT 'M8_权限码与控制器一致', COUNT(*), 3
  FROM sys_menu WHERE id IN (2935, 2936, 2937) AND del_flag = 0
    AND permission IN ('org:schedule:list','org:duty:list')
UNION ALL SELECT 'M9_排序号不打架', COUNT(*), 0
  FROM (SELECT sort_order FROM sys_menu WHERE parent_id = @parent_id AND del_flag = 0
          GROUP BY sort_order HAVING COUNT(*) > 1) z;

-- 期望：M1=3 且 M2~M7、M9 全 0（cnt 列 = 异常行数），M8=3。
