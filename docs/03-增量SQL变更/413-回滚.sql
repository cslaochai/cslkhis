-- 413 回滚：撤销心血管内科二病区及跨科收治关系，一病区改回原名
-- 日期：2026-11-27
-- 前置：二病区不得有在院患者（占用床 / 在院 admission），否则先出院或转科再回滚。
SET NAMES utf8mb4;
START TRANSACTION;

-- 0. 前置核对：二病区占用床应为 0
-- SELECT COUNT(*) AS occupied_beds FROM sys_bed
--  WHERE ward_id = 8600000000000000040 AND bed_status = 2 AND del_flag = 0;

-- 1. 删二病区床位
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000040;

-- 2. 删二病区收治关系（本位 + 跨科）
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000040;

-- 3. 删二病区
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000040;

-- 4. 一病区改回原名
UPDATE `sys_ward`
SET `ward_name`  = '心血管内科病区',
    `update_by`  = 'admin',
    `update_time` = NOW()
WHERE `ward_id` = 8600000000000000003
  AND `ward_name` = '心血管内科一病区';

-- 5. 还原人力配置标准单元名快照
UPDATE `biz_staff_plan_rule`
SET `org_name`   = '心血管内科病区',
    `update_by`  = 'admin',
    `update_time` = NOW()
WHERE `org_type` = 2
  AND `org_id`   = 8600000000000000003
  AND `org_name` = '心血管内科一病区';

COMMIT;
