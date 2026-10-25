-- =====================================================================
-- 20261025 护士长(10)排班权限收口 + 门诊工位派岗(2960)菜单恢复
-- ---------------------------------------------------------------------
-- 背景（现场反馈）：
--   1. 护士长（role 10，dataScope=3 本部门）是护理排班维护岗，不是排班管理岗——
--      排班总览(2938)/员工排班(2935)/排班字典管理(2939) 从她手里收掉。
--   2. 门诊工位派岗(2960) 菜单此前被软删(del_flag=1)，组件/路由/子按钮都在，
--      授权行（1/10/26）还在，恢复 del_flag=0 即可重新可见（医务科要用）。
--   3. 15:57 用户追加确认：门诊工位派岗(2960) 也不给护士长——排班域最终只剩
--      护理排班(331)。菜单 + 29601/29602 按钮码一并收：只收菜单不收按钮的话
--      /system/opdStation/stationUpsert 等接口仍对她放行，权限收口不彻底。
--   4. 公共工位字典 tab 回归「排班字典管理」(2939/ShiftDictView.vue)：
--      8fe1e9d3 曾把该 tab 掏成空壳指向 2960，前端把工位字典 CRUD 搬回即可，
--      后端零改动（/system/opdStation/* 接口全部现成）。
--
-- 本次动作：
--   A. 护士长(10) DELETE：2938 排班总览 / 2935 员工排班 / 2939 排班字典管理
--   B. 护士长(10) DELETE：上述菜单的专属子按钮
--      29351/29352/29353 岗位出勤-增/改/删（staff:schedule:*，服务员工排班页）
--      29391/29392/29393 班次字典-增/改/删（shift:dict:*，服务排班字典管理页）
--   C. 恢复 2960 门诊工位派岗菜单（del_flag 1→0）
--   D. 护士长(10) DELETE：2960 菜单 + 29601/29602 按钮（15:57 追加）
--
-- 幂等：DELETE/UPDATE 带条件，重复执行零变化。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或重启后端。
-- =====================================================================

-- A. 收回三个菜单
DELETE FROM sys_role_menu WHERE role_id = 10 AND menu_id IN (2938, 2935, 2939);

-- B. 收回专属子按钮（菜单没了，按钮悬空）
DELETE FROM sys_role_menu WHERE role_id = 10 AND menu_id IN (29351, 29352, 29353);
DELETE FROM sys_role_menu WHERE role_id = 10 AND menu_id IN (29391, 29392, 29393);

-- C. 恢复门诊工位派岗菜单（授权行 1/10/26 已存在，恢复即可见）
UPDATE sys_menu SET del_flag = 0, update_time = NOW() WHERE id = 2960 AND del_flag = 1;

-- D.（15:57 追加）护士长收回门诊工位派岗：菜单 + 增删按钮码
DELETE FROM sys_role_menu WHERE role_id = 10 AND menu_id IN (2960, 29601, 29602);

-- 回查：护士长(10) 在排班值守中心(2945)范围内应只剩：
--    331 护理排班 + 2650/2651/2652 护理排班按钮（29608 床旁结算按钮属护士工作站）
-- SELECT m.id, m.menu_name, m.menu_type, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 10 AND (m.parent_id = 2945 OR m.id = 331)
--  ORDER BY m.id;

-- Redis 权限缓存（执行后清理，TTL 10min 不自愈的场景下必须清）：
--   redis-cli -a <pwd> --scan --pattern 'his:perm:role:*' | xargs redis-cli -a <pwd> del
