-- =====================================================================
-- 20261011 排班中心菜单整改：语义改名 + 角色权限按业务域隔离
-- ---------------------------------------------------------------------
-- 背景（来自排班架构核对）：
--   原排班中心 7 个菜单共用 org:schedule:* 权限域，导致护士长(role10)
--   跨域持有 门诊排班(804)+增改删按钮、全院岗位排班(2935)、人力配置标准
--   (2937)、排班总览(2938)、值守点位(2936)；且菜单名按数据表命名，业务
--   语义不清，使用者分不清彼此职责。
-- 范围：
--   - 仅改 sys_menu.menu_name 与 sys_role_menu 映射；
--   - 不改 permission 字符串（避免后端接口 403）；
--   - 角色可见性 + 接口鉴权均依赖角色聚合的 perms，移除错误关联后即双重隔离。
-- 注意：
--   执行后需清 Redis 权限缓存(his:perm:role:*) 或重启后端，否则旧权限短期仍生效。
-- =====================================================================

-- 1. 菜单改名（对齐标准HIS业务语义，而非按表名）
UPDATE sys_menu SET menu_name = '门诊排班'     WHERE id = 804;   -- 原"排班管理"：医生门诊出诊 + 号源
UPDATE sys_menu SET menu_name = '岗位出勤'     WHERE id = 2935;  -- 原"全院岗位排班"：底座全员出勤事实
UPDATE sys_menu SET menu_name = '岗位人力标准' WHERE id = 2937;  -- 原"人力配置标准"：每科每岗最低人数
UPDATE sys_menu SET menu_name = '护理排班'     WHERE id = 331;   -- 原"病区护理排班"：护士长排护士(含门诊分诊护士)

-- 2. 移除跨域 / 越权授权
-- 2.1 护士长(10) 不排门诊、不录全院岗位、不配人力标准、不看总览、不配值守点位
DELETE FROM sys_role_menu WHERE role_id = 10 AND menu_id IN (804, 2120, 2121, 2122, 2935, 2937, 2938, 2936);
-- 2.2 院领导(18) 看总览即可：退出护理排班操作页、不配值守点位、无总值班改删换权
DELETE FROM sys_role_menu WHERE role_id = 18 AND menu_id IN (331, 2936, 2670, 2669, 2668);
-- 2.3 前台/急诊/分诊(7/8/9) 不配值守点位字典
DELETE FROM sys_role_menu WHERE role_id IN (7, 8, 9) AND menu_id = 2936;

-- 3. 新增合理授权
-- 3.1 院领导(18) 看排班总览（管理层视角）
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
VALUES ('2026101100000000001', '18', '2938', 'admin', 'admin', '1', '1');

-- 4. 子按钮同步改名（与父菜单语义对齐）
UPDATE sys_menu SET menu_name = '门诊排班-新增'         WHERE id = 2120;
UPDATE sys_menu SET menu_name = '门诊排班-修改'         WHERE id = 2121;
UPDATE sys_menu SET menu_name = '门诊排班-删除'         WHERE id = 2122;
UPDATE sys_menu SET menu_name = '护理排班-排班维护'     WHERE id = 2650;
UPDATE sys_menu SET menu_name = '护理排班-人力标准维护' WHERE id = 2651;
UPDATE sys_menu SET menu_name = '护理排班-删除排班行'   WHERE id = 2652;
