-- =====================================================================
-- 424 机器人并入设备台账（设备类别 · 资产行 · 接入绑定）
--
-- 背景：421 只做了机器人调度，车只登记在物联网接入台账 biz_iot_device，设备管理（sys_equipment）
--       里看不到一台物流机器人，于是没人给车建档、没人登记维保、也没法新增/停用一台车。
--       本脚本把"车"当固定资产补进设备台账，并与接入台账按「设备编码 = MQTT 设备号」对上：
--         1) 设备类别补 8-物流设备（字典 his_equipment_category）；
--         2) 10 台 RBT-01..10 建资产行（设备科持有、不进共享池、90 天维保周期）；
--         3) 接入台账回填 equipment_id，两页可互查。
--
-- 口径（对标设备科固定资产台账）：
--   - 设备编码就是资产编号，也是接入编号（MQTT 设备号），一车一档，不另造内码；
--   - 机器人是"闲置也不外借"的生产工具，rentable 固定 0，由派单驱动，不进共享池；
--   - 派单算法只认 biz_iot_device 里 device_type='robot' 且 enabled=1 的车，
--     资产行的状态（在用/停用/维修中/报废）由设备管理页变更时同步启停接入行。
--
-- 回滚：见 424-回滚.sql
-- =====================================================================
SET NAMES utf8mb4;

-- ----------------------------
-- 1) 设备类别：补 8-物流设备
--    文案与 Java 枚举 EquipmentCategoryEnum.LOGISTICS 逐字一致。
-- ----------------------------
INSERT INTO sys_dict_data
    (id, dict_type, dict_label, dict_value, dict_sort, is_default, status,
     create_by, create_time, update_by, update_time, del_flag, remark,
     dict_source, create_by_id, update_by_id)
SELECT 4240000000000000301, 'his_equipment_category', '物流设备', '8', 8, 0, 1,
       'admin', NOW(), 'admin', NOW(), 0, '院内物流机器人（送药/送标本/送器械包）',
       1, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data d
                  WHERE d.dict_type = 'his_equipment_category' AND d.dict_value = '8' AND d.del_flag = 0);

-- ----------------------------
-- 2) 10 台送药机器人的资产行
--    设备编码 RBT-01..10 与 421 的接入编号逐字一致；归属设备科（全院共享车队）。
--    按设备编码判重，可重复执行，已有档案不覆盖。
-- ----------------------------
INSERT INTO sys_equipment
    (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
     purchase_date, purchase_price, status, rentable, maintain_cycle_days, last_maintain_date,
     create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT seed.id, seed.code, seed.name, 8, 19580082, '设备科', '擎朗智能', 'T5',
       '2025-03-01', 128000.00, 1, 0, 90, '2026-10-15',
       'admin', NOW(), 'admin', NOW(), 0, '全院共享物流车队（送药/送标本），由机器人调度派单驱动',
       0, 0
FROM (
    SELECT 4240000000000000201 AS id, 'RBT-01' AS code, '送药机器人 01' AS name UNION ALL
    SELECT 4240000000000000202, 'RBT-02', '送药机器人 02' UNION ALL
    SELECT 4240000000000000203, 'RBT-03', '送药机器人 03' UNION ALL
    SELECT 4240000000000000204, 'RBT-04', '送药机器人 04' UNION ALL
    SELECT 4240000000000000205, 'RBT-05', '送药机器人 05' UNION ALL
    SELECT 4240000000000000206, 'RBT-06', '送药机器人 06' UNION ALL
    SELECT 4240000000000000207, 'RBT-07', '送药机器人 07' UNION ALL
    SELECT 4240000000000000208, 'RBT-08', '送药机器人 08' UNION ALL
    SELECT 4240000000000000209, 'RBT-09', '送药机器人 09' UNION ALL
    SELECT 4240000000000000210, 'RBT-10', '送药机器人 10'
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_equipment e
                  WHERE e.equipment_code = seed.code AND e.del_flag = 0);

-- ----------------------------
-- 3) 接入台账回填资产外键：按「设备编码 = 接入编号」对上，只认物流设备档案
--    bind_type/bind_id 是历史遗留列（新代码不读不写），这里只回填 equipment_id。
-- ----------------------------
UPDATE biz_iot_device d
    JOIN sys_equipment e ON e.equipment_code = d.device_no AND e.del_flag = 0
SET d.equipment_id = e.id,
    d.update_by    = 'admin',
    d.update_time  = NOW()
WHERE d.device_type = 'robot'
  AND d.del_flag = 0
  AND e.category = 8;

-- 3.1) 列注释认下机器人这类绑定（原注释写"仅 asset 类型"）
ALTER TABLE biz_iot_device
    MODIFY COLUMN equipment_id BIGINT NULL COMMENT '资产台账绑定的设备台账ID（asset 标签类与 robot 物流机器人共用）';

-- ----------------------------
-- 4) 「设备管理-新增」按钮码授权：2125 建了码但没授给任何角色，页面按钮与后端 403 同时生效，
--    结果是没人能新增/编辑/停用设备。这里授给持有「设备管理」页(902)的角色：1 系统管理员 / 39 设备与供应室管理员。
--    停用/启用复用同一 add 码（前后端是同一个 upsert/状态变更入口）。
-- ----------------------------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                                  create_by_id, update_by_id)
SELECT seed.id, seed.role_id, 2125, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 4240000000000000401 AS id, 1 AS role_id UNION ALL
    SELECT 4240000000000000402, 39
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = 2125);

-- 回查：
-- SELECT equipment_code, equipment_name, category, dept_name, status, rentable, maintain_cycle_days FROM sys_equipment WHERE category = 8 ORDER BY equipment_code;
-- SELECT device_no, equipment_id, enabled FROM biz_iot_device WHERE device_type = 'robot' ORDER BY device_no;
-- SELECT dict_value, dict_label FROM sys_dict_data WHERE dict_type = 'his_equipment_category' ORDER BY dict_sort;
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = 2125;


