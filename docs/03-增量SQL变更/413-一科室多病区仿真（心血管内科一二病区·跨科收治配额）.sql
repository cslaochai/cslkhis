-- ============================================================
-- 413 一科室多病区仿真：心血管内科一/二病区 + 跨科收治配额
-- 日期：2026-11-27
-- ⚠ 已被 414《批量多病区扩编》覆盖：心血管内科一/二/三病区由 414 统一铺底，
--   本脚本请勿单独执行；若已执行，请先跑 413-回滚.sql 清掉 W040 再跑 414。
-- 背景：现有 39 个病区全部是「科室 : 病区 = 1:1」，多对多模型（sys_ward_dept）
--       只平移了一对一现状，缺一个「同科两个病区 + 病区跨科收治」的演示实例。
-- 口径（对齐 SysWardServiceImpl 与 299 建表注释）：
--   1) 科室与病区是两个正交维度：科室挂医生/医疗组/诊疗科目，病区挂床位/护士/排班；
--      一个科室可有多个病区，一个病区也可收多个科室患者（跨科收治 / 全院一张床）。
--   2) 本位科室 = sys_ward.dept_id，且在 sys_ward_dept 里 is_primary=1；
--      跨科科室 is_primary=0，bed_quota = 该科室专科优先床数（NULL=不限额，剩余为机动床）。
--   3) 以心血管内科（19580005）为例：
--      · 原 W003「心血管内科病区」更名「心血管内科一病区」，床位与关系不动；
--      · 新增 W040「心血管内科二病区」，本位心血管内科，20 张空床；
--      · 二病区开放内分泌科（19580007）跨科收治，配额 4 张专科优先床。
-- 幂等：病区/关系/床位均按主键或唯一键判重，重复执行不产生重复行；改名只匹配老病区名。
-- 回滚：见同目录 413-回滚.sql。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- ---------- 0. 前置核对（应输出：W003 老名 1 行；W040 应为 0 行） ----------
-- SELECT ward_id, ward_code, ward_name, dept_id, total_beds
--   FROM sys_ward WHERE ward_id IN (8600000000000000003, 8600000000000000040);

-- ---------- 1. 原心血管内科病区更名为「一病区」 ----------
UPDATE `sys_ward`
SET `ward_name`  = '心血管内科一病区',
    `update_by`  = 'admin',
    `update_time` = NOW()
WHERE `ward_id` = 8600000000000000003
  AND `ward_name` = '心血管内科病区';

-- 1.1 同步该病区人力配置标准的单元名快照（当前生效模板，非历史班次留痕）；
--     已排班次 / 设备安装位置文本等历史快照不回改。
UPDATE `biz_staff_plan_rule`
SET `org_name`   = '心血管内科一病区',
    `update_by`  = 'admin',
    `update_time` = NOW()
WHERE `org_type` = 2
  AND `org_id`   = 8600000000000000003
  AND `org_name` = '心血管内科病区';

-- ---------- 2. 新增「心血管内科二病区」 ----------
INSERT INTO `sys_ward`
    (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
     `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`,
     `create_by_id`, `update_by_id`)
SELECT 8600000000000000040, 'W040', '心血管内科二病区', 19580005, 20, 0,
       1, '心血管内科第二个护理单元；开放内分泌科跨科收治（4 张专科优先床）',
       'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000040);

-- ---------- 3. 病区-科室多对多关系 ----------
-- 3.1 二病区本位科室：心血管内科（不限额，NULL）
INSERT IGNORE INTO `sys_ward_dept`
    (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
     `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES
    (9100000000000000050, 8600000000000000040, 19580005, 1, NULL, 0,
     'admin', NOW(), 'admin', NOW(), 1, 1);

-- 3.2 二病区跨科收治：内分泌科 4 张专科优先床，其余为机动床
INSERT IGNORE INTO `sys_ward_dept`
    (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
     `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES
    (9100000000000000051, 8600000000000000040, 19580007, 0, 4, 1,
     'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 4. 二病区床位：20 张空床（01~20） ----------
INSERT IGNORE INTO `sys_bed`
    (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
     `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES
    (8620000000000001051, '01', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001052, '02', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001053, '03', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001054, '04', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001055, '05', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001056, '06', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001057, '07', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001058, '08', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001059, '09', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001060, '10', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001061, '11', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001062, '12', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001063, '13', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001064, '14', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001065, '15', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001066, '16', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001067, '17', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001068, '18', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001069, '19', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1),
    (8620000000000001070, '20', 8600000000000000040, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);

-- ---------- 5. 自检 ----------
-- 5.1 心血管内科应挂 2 个病区（一/二）
-- SELECT w.ward_id, w.ward_code, w.ward_name, w.total_beds
--   FROM sys_ward w WHERE w.dept_id = 19580005 ORDER BY w.ward_code;
--
-- 5.2 二病区应有 2 条收治关系：本位心血管内科 1 条 + 跨科内分泌科 1 条（配额 4）
-- SELECT ward_id, dept_id, is_primary, bed_quota, sort_order
--   FROM sys_ward_dept WHERE ward_id = 8600000000000000040 ORDER BY sort_order;
--
-- 5.3 本位一致性：每个病区 is_primary=1 的科室必须等于 sys_ward.dept_id（应为 0 行）
-- SELECT w.ward_id, w.ward_name, w.dept_id AS ward_dept, wd.dept_id AS primary_dept
--   FROM sys_ward w JOIN sys_ward_dept wd ON wd.ward_id = w.ward_id AND wd.is_primary = 1
--  WHERE wd.dept_id <> w.dept_id;
--
-- 5.4 二病区床位 20 张且全空闲（bed_status=1）
-- SELECT COUNT(*) AS bed_total,
--        SUM(CASE WHEN bed_status = 1 THEN 1 ELSE 0 END) AS free_beds
--   FROM sys_bed WHERE ward_id = 8600000000000000040 AND del_flag = 0;

COMMIT;
