-- =============================================================================
-- 230 - 排班中心（目录新建 + 排班域菜单收口）
--
-- 【要解决的问题】
--   排班域 7 个入口散在两处：804/806/2935/2936/2937/2938 平铺在 800 组织与资源，
--   331 病区护理排班孤在 330 护理管理。用户视角「没有一个统一排班的地方」——
--   本文件新建顶级目录 2945 排班中心，把 7 个页面全部挂进去；目录内按
--   「总览 → 门诊 → 全院 → 护理 → 人力标准 → 值班」排布。
--
-- 【收口边界】
--   - 2920 总值班日志（交班本）不搬：它是运行记录不是排班配置，留在 800。
--   - 805 技术授权不属排班域，不动。
--   - 按钮菜单（menu_type=3）挂在各自页面下，随页面整体迁挂，不单独动。
--
-- 【授权口径】行为中性：凡已授权 7 页中任一页的角色，自动获得 2945 目录授权
--   （菜单树父目录不可见则页面不可达，必须补父链）。
--
-- 【幂等】目录行 ON DUPLICATE KEY UPDATE；迁挂 UPDATE 天然幂等；授权行 NOT EXISTS 去重。
--
-- 【回滚】
--   UPDATE sys_menu SET parent_id=800, sort_order=1  WHERE id=804;
--   UPDATE sys_menu SET parent_id=800, sort_order=2  WHERE id=806;
--   UPDATE sys_menu SET parent_id=800, sort_order=63 WHERE id=2935;
--   UPDATE sys_menu SET parent_id=800, sort_order=64 WHERE id=2936;
--   UPDATE sys_menu SET parent_id=800, sort_order=65 WHERE id=2937;
--   UPDATE sys_menu SET parent_id=800, sort_order=66 WHERE id=2938;
--   UPDATE sys_menu SET parent_id=330, sort_order=1  WHERE id=331;
--   DELETE FROM sys_role_menu WHERE menu_id = 2945;
--   DELETE FROM sys_menu WHERE id = 2945;
--   （sys_menu/menu_key 唯一索引不含 del_flag，删除一律物理删）
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 0. 前置检查：7 页必须在、2945 与 menu_key 必须空闲
-- -----------------------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM sys_menu WHERE id IN (804,806,331,2935,2936,2937,2938) AND del_flag = 0) t_pages,
  (SELECT COUNT(*) FROM sys_menu WHERE id = 2945) t_id_free,
  (SELECT COUNT(*) FROM sys_menu WHERE menu_key = 'org:scheduleCenter') t_key_free;
-- 期望：t_pages=7 t_id_free=0 t_key_free=0；不符先别往下执行。

-- -----------------------------------------------------------------------------
-- 1. 顶级目录 2945 排班中心
-- -----------------------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, del_flag, remark)
SELECT 2945, '排班中心', 0, 45, 1, '/schedule-center', NULL, 'org:scheduleCenter',
       'Calendar', NULL, 0, 0, 1, 1, 'sql230', NOW(), 0,
       '排班域统一入口：门诊排班/全院岗位/护理排班/人力标准/总值班/值守点位/排班总览'
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  sort_order = VALUES(sort_order),
  path = VALUES(path),
  menu_key = VALUES(menu_key),
  icon = VALUES(icon),
  remark = VALUES(remark),
  update_by = 'sql230',
  update_time = NOW(),
  del_flag = 0;

-- -----------------------------------------------------------------------------
-- 2. 七页迁挂（目录内定序：总览在前，编辑工作台在后）
-- -----------------------------------------------------------------------------
UPDATE sys_menu SET parent_id = 2945, sort_order = 1, update_by = 'sql230', update_time = NOW()
 WHERE id = 2938 AND del_flag = 0;  -- 排班总览（只读驾驶舱）
UPDATE sys_menu SET parent_id = 2945, sort_order = 2, update_by = 'sql230', update_time = NOW()
 WHERE id = 804  AND del_flag = 0;  -- 排班管理（门诊出诊）
UPDATE sys_menu SET parent_id = 2945, sort_order = 3, update_by = 'sql230', update_time = NOW()
 WHERE id = 2935 AND del_flag = 0;  -- 全院岗位排班
UPDATE sys_menu SET parent_id = 2945, sort_order = 4, update_by = 'sql230', update_time = NOW()
 WHERE id = 331  AND del_flag = 0;  -- 病区护理排班（自 330 护理管理迁出）
UPDATE sys_menu SET parent_id = 2945, sort_order = 5, update_by = 'sql230', update_time = NOW()
 WHERE id = 2937 AND del_flag = 0;  -- 人力配置标准
UPDATE sys_menu SET parent_id = 2945, sort_order = 6, update_by = 'sql230', update_time = NOW()
 WHERE id = 806  AND del_flag = 0;  -- 总值班排班
UPDATE sys_menu SET parent_id = 2945, sort_order = 7, update_by = 'sql230', update_time = NOW()
 WHERE id = 2936 AND del_flag = 0;  -- 值守点位

-- -----------------------------------------------------------------------------
-- 3. 角色补授权：凡已授权七页中任一页的角色 → 授 2945（父链必须通，页面才可达）
-- -----------------------------------------------------------------------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time)
SELECT DISTINCT
       896792000000000000 + ROW_NUMBER() OVER (ORDER BY t.role_id),
       t.role_id, 2945, 'sql230', NOW()
FROM (
  SELECT DISTINCT rm.role_id
  FROM sys_role_menu rm
  WHERE rm.menu_id IN (804,806,331,2935,2936,2937,2938)
) t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu x WHERE x.role_id = t.role_id AND x.menu_id = 2945
);

-- -----------------------------------------------------------------------------
-- 4. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
SELECT 'M1_目录存在且顶级' item, COUNT(*) cnt, 1 expect
  FROM sys_menu WHERE id = 2945 AND parent_id = 0 AND menu_type = 1 AND del_flag = 0
UNION ALL SELECT 'M2_七页全部挂在2945', COUNT(*), 0
  FROM sys_menu
  WHERE id IN (804,806,331,2935,2936,2937,2938) AND del_flag = 0
    AND parent_id <> 2945
UNION ALL SELECT 'M3_目录下页面数', COUNT(*), 7
  FROM sys_menu WHERE parent_id = 2945 AND menu_type = 2 AND del_flag = 0
UNION ALL SELECT 'M4_父链授权缺口', COUNT(*), 0
  FROM sys_role_menu rm
  WHERE rm.menu_id IN (804,806,331,2935,2936,2937,2938)
    AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = 2945)
UNION ALL SELECT 'M5_同目录排序不打架', COUNT(*), 0
  FROM (SELECT sort_order FROM sys_menu WHERE parent_id = 2945 AND menu_type = 2 AND del_flag = 0
          GROUP BY sort_order HAVING COUNT(*) > 1) z
UNION ALL SELECT 'M6_800残留排班页', COUNT(*), 0
  FROM sys_menu WHERE parent_id = 800 AND del_flag = 0
    AND id IN (804,806,2935,2936,2937,2938)
UNION ALL SELECT 'M7_330残留护理排班', COUNT(*), 0
  FROM sys_menu WHERE parent_id = 330 AND del_flag = 0 AND id = 331;

-- 期望：M1=1 M3=7 且 M2/M4/M5/M6/M7 全 0。
-- 跑完清权限缓存：DEL his:perm:role:*（TTL 10 分钟，不清就是旧集合）。
