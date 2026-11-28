-- ============================================================
-- 424 回滚：撤掉「机器人并入设备台账」
-- 日期：2026-11-28
-- 说明：删 424 铺底的 10 条资产行与 8-物流设备字典值，清掉接入台账回填的 equipment_id，
--       并把 equipment_id 列注释恢复成"仅 asset 类型"。
--       不动 421 铺底的机器人接入台账（biz_iot_device）与调度菜单，车队仍按 421 的口径运行。
-- ============================================================
SET NAMES utf8mb4;

-- 1) 解绑：只清机器人行的资产外键
UPDATE biz_iot_device
SET equipment_id = NULL,
    update_by    = 'admin',
    update_time  = NOW()
WHERE device_type = 'robot'
  AND del_flag = 0;

-- 2) 删资产行
DELETE FROM sys_equipment
WHERE equipment_code LIKE 'RBT-%'
  AND category = 8;

-- 3) 删 8-物流设备字典值
DELETE FROM sys_dict_data
WHERE dict_type = 'his_equipment_category'
  AND dict_value = '8';

-- 3.1) 撤掉给「设备管理-新增」补授的角色授权
DELETE FROM sys_role_menu
WHERE menu_id = 2125
  AND role_id IN (1, 39);

-- 4) 列注释恢复
ALTER TABLE biz_iot_device
    MODIFY COLUMN equipment_id BIGINT NULL COMMENT '资产标签绑定的设备台账ID（仅 asset 类型）';
