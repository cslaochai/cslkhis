-- ============================================================
-- 414 批量多病区扩编：39 个住院科室按 3/2/1 分级扩病区
-- 日期：2026-11-27
-- 背景：413 只示范了心血管内科一/二病区一个实例；本次把多病区结构铺到全部住院科室。
-- 口径（对齐 SysWardServiceImpl 与 299 建表注释）：
--   · 科室与病区正交多对多；本位科室 = sys_ward.dept_id 且 sys_ward_dept.is_primary=1。
--   · level3 大科：老病区改「一病区」，新增二、三病区；level2 中科：老病区改「一病区」，新增二病区；
--     level1（ICU）为专科监护单元，保留单病区不动。
--   · 新病区床位全部空闲（bed_status=1），不造在院患者；跨科收治关系沿用 413 示范，本次不批量挂。
-- 编号段：ward_id ...041 起 / rid ...052 起 / bed_id ...1071 起，与 413 错开。
-- 幂等：病区按 ward_id NOT EXISTS 判重，关系/床位 INSERT IGNORE；老病区更名只匹配老名。
-- 回滚：见同目录 414-回滚.sql。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- ---------- 1. 老病区更名「一病区」并同步人力配置标准快照 ----------
UPDATE `sys_ward` SET `ward_name` = '呼吸内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000001 AND `ward_name` = '呼吸内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '呼吸内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000001 AND `org_name` = '呼吸内科病区';
UPDATE `sys_ward` SET `ward_name` = '消化内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000002 AND `ward_name` = '消化内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '消化内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000002 AND `org_name` = '消化内科病区';
UPDATE `sys_ward` SET `ward_name` = '心血管内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000003 AND `ward_name` = '心血管内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '心血管内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000003 AND `org_name` = '心血管内科病区';
UPDATE `sys_ward` SET `ward_name` = '神经内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000004 AND `ward_name` = '神经内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '神经内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000004 AND `org_name` = '神经内科病区';
UPDATE `sys_ward` SET `ward_name` = '内分泌科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000005 AND `ward_name` = '内分泌科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '内分泌科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000005 AND `org_name` = '内分泌科病区';
UPDATE `sys_ward` SET `ward_name` = '肾内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000006 AND `ward_name` = '肾内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肾内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000006 AND `org_name` = '肾内科病区';
UPDATE `sys_ward` SET `ward_name` = '血液内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000007 AND `ward_name` = '血液内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '血液内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000007 AND `org_name` = '血液内科病区';
UPDATE `sys_ward` SET `ward_name` = '风湿免疫科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000008 AND `ward_name` = '风湿免疫科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '风湿免疫科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000008 AND `org_name` = '风湿免疫科病区';
UPDATE `sys_ward` SET `ward_name` = '老年病科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000009 AND `ward_name` = '老年病科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '老年病科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000009 AND `org_name` = '老年病科病区';
UPDATE `sys_ward` SET `ward_name` = '全科医学科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000010 AND `ward_name` = '全科医学科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '全科医学科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000010 AND `org_name` = '全科医学科病区';
UPDATE `sys_ward` SET `ward_name` = '普通外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000011 AND `ward_name` = '普通外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '普通外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000011 AND `org_name` = '普通外科病区';
UPDATE `sys_ward` SET `ward_name` = '骨科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000012 AND `ward_name` = '骨科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '骨科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000012 AND `org_name` = '骨科病区';
UPDATE `sys_ward` SET `ward_name` = '神经外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000013 AND `ward_name` = '神经外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '神经外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000013 AND `org_name` = '神经外科病区';
UPDATE `sys_ward` SET `ward_name` = '心胸外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000014 AND `ward_name` = '心胸外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '心胸外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000014 AND `org_name` = '心胸外科病区';
UPDATE `sys_ward` SET `ward_name` = '泌尿外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000015 AND `ward_name` = '泌尿外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '泌尿外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000015 AND `org_name` = '泌尿外科病区';
UPDATE `sys_ward` SET `ward_name` = '烧伤整形外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000016 AND `ward_name` = '烧伤整形外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '烧伤整形外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000016 AND `org_name` = '烧伤整形外科病区';
UPDATE `sys_ward` SET `ward_name` = '血管外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000017 AND `ward_name` = '血管外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '血管外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000017 AND `org_name` = '血管外科病区';
UPDATE `sys_ward` SET `ward_name` = '肝胆外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000018 AND `ward_name` = '肝胆外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肝胆外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000018 AND `org_name` = '肝胆外科病区';
UPDATE `sys_ward` SET `ward_name` = '甲乳外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000019 AND `ward_name` = '甲乳外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '甲乳外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000019 AND `org_name` = '甲乳外科病区';
UPDATE `sys_ward` SET `ward_name` = '肛肠外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000020 AND `ward_name` = '肛肠外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肛肠外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000020 AND `org_name` = '肛肠外科病区';
UPDATE `sys_ward` SET `ward_name` = '妇科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000021 AND `ward_name` = '妇科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '妇科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000021 AND `org_name` = '妇科病区';
UPDATE `sys_ward` SET `ward_name` = '产科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000022 AND `ward_name` = '产科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '产科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000022 AND `org_name` = '产科病区';
UPDATE `sys_ward` SET `ward_name` = '小儿内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000023 AND `ward_name` = '小儿内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '小儿内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000023 AND `org_name` = '小儿内科病区';
UPDATE `sys_ward` SET `ward_name` = '小儿外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000024 AND `ward_name` = '小儿外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '小儿外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000024 AND `org_name` = '小儿外科病区';
UPDATE `sys_ward` SET `ward_name` = '新生儿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000025 AND `ward_name` = '新生儿科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '新生儿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000025 AND `org_name` = '新生儿科病区';
UPDATE `sys_ward` SET `ward_name` = '急诊内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000026 AND `ward_name` = '急诊内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '急诊内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000026 AND `org_name` = '急诊内科病区';
UPDATE `sys_ward` SET `ward_name` = '急诊外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000027 AND `ward_name` = '急诊外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '急诊外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000027 AND `org_name` = '急诊外科病区';
UPDATE `sys_ward` SET `ward_name` = '急诊儿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000029 AND `ward_name` = '急诊儿科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '急诊儿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000029 AND `org_name` = '急诊儿科病区';
UPDATE `sys_ward` SET `ward_name` = '中医内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000030 AND `ward_name` = '中医内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000030 AND `org_name` = '中医内科病区';
UPDATE `sys_ward` SET `ward_name` = '中医骨伤科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000031 AND `ward_name` = '中医骨伤科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医骨伤科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000031 AND `org_name` = '中医骨伤科病区';
UPDATE `sys_ward` SET `ward_name` = '针灸推拿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000032 AND `ward_name` = '针灸推拿科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '针灸推拿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000032 AND `org_name` = '针灸推拿科病区';
UPDATE `sys_ward` SET `ward_name` = '中医妇科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000033 AND `ward_name` = '中医妇科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医妇科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000033 AND `org_name` = '中医妇科病区';
UPDATE `sys_ward` SET `ward_name` = '中医儿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000034 AND `ward_name` = '中医儿科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医儿科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000034 AND `org_name` = '中医儿科病区';
UPDATE `sys_ward` SET `ward_name` = '肿瘤内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000035 AND `ward_name` = '肿瘤内科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肿瘤内科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000035 AND `org_name` = '肿瘤内科病区';
UPDATE `sys_ward` SET `ward_name` = '肿瘤外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000036 AND `ward_name` = '肿瘤外科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肿瘤外科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000036 AND `org_name` = '肿瘤外科病区';
UPDATE `sys_ward` SET `ward_name` = '安宁疗护病房一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000037 AND `ward_name` = '安宁疗护病房病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '安宁疗护病房一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000037 AND `org_name` = '安宁疗护病房病区';
UPDATE `sys_ward` SET `ward_name` = '临床心理科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000038 AND `ward_name` = '临床心理科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '临床心理科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000038 AND `org_name` = '临床心理科病区';
UPDATE `sys_ward` SET `ward_name` = '肝病科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000039 AND `ward_name` = '肝病科病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肝病科一病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000039 AND `org_name` = '肝病科病区';

-- ---------- 2. 新增病区 ----------
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000041, 'W041', '呼吸内科二病区', 19580003, 24, 0,
       1, '呼吸内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000041);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000042, 'W042', '呼吸内科三病区', 19580003, 24, 0,
       1, '呼吸内科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000042);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000043, 'W043', '消化内科二病区', 19580004, 24, 0,
       1, '消化内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000043);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000044, 'W044', '消化内科三病区', 19580004, 24, 0,
       1, '消化内科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000044);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000045, 'W045', '心血管内科二病区', 19580005, 24, 0,
       1, '心血管内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000045);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000046, 'W046', '心血管内科三病区', 19580005, 24, 0,
       1, '心血管内科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000046);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000047, 'W047', '神经内科二病区', 19580006, 20, 0,
       1, '神经内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000047);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000048, 'W048', '内分泌科二病区', 19580007, 20, 0,
       1, '内分泌科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000048);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000049, 'W049', '肾内科二病区', 19580008, 20, 0,
       1, '肾内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000049);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000050, 'W050', '血液内科二病区', 19580009, 20, 0,
       1, '血液内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000050);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000051, 'W051', '风湿免疫科二病区', 19580010, 20, 0,
       1, '风湿免疫科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000051);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000052, 'W052', '老年病科二病区', 19580011, 20, 0,
       1, '老年病科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000052);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000053, 'W053', '全科医学科二病区', 19580086, 20, 0,
       1, '全科医学科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000053);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000054, 'W054', '普通外科二病区', 19580013, 24, 0,
       1, '普通外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000054);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000055, 'W055', '普通外科三病区', 19580013, 24, 0,
       1, '普通外科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000055);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000056, 'W056', '骨科二病区', 19580014, 24, 0,
       1, '骨科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000056);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000057, 'W057', '骨科三病区', 19580014, 24, 0,
       1, '骨科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000057);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000058, 'W058', '神经外科二病区', 19580015, 20, 0,
       1, '神经外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000058);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000059, 'W059', '心胸外科二病区', 19580016, 20, 0,
       1, '心胸外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000059);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000060, 'W060', '泌尿外科二病区', 19580017, 20, 0,
       1, '泌尿外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000060);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000061, 'W061', '烧伤整形外科二病区', 19580018, 20, 0,
       1, '烧伤整形外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000061);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000062, 'W062', '血管外科二病区', 19580019, 20, 0,
       1, '血管外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000062);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000063, 'W063', '肝胆外科二病区', 19580020, 20, 0,
       1, '肝胆外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000063);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000064, 'W064', '甲乳外科二病区', 19580087, 20, 0,
       1, '甲乳外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000064);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000065, 'W065', '肛肠外科二病区', 19580088, 20, 0,
       1, '肛肠外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000065);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000066, 'W066', '妇科二病区', 19580022, 20, 0,
       1, '妇科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000066);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000067, 'W067', '产科二病区', 19580023, 24, 0,
       1, '产科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000067);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000068, 'W068', '产科三病区', 19580023, 24, 0,
       1, '产科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000068);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000069, 'W069', '小儿内科二病区', 19580026, 16, 0,
       1, '小儿内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000069);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000070, 'W070', '小儿内科三病区', 19580026, 16, 0,
       1, '小儿内科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000070);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000071, 'W071', '小儿外科二病区', 19580027, 16, 0,
       1, '小儿外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000071);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000072, 'W072', '新生儿科二病区', 19580028, 16, 0,
       1, '新生儿科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000072);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000073, 'W073', '急诊内科二病区', 19580048, 20, 0,
       1, '急诊内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000073);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000074, 'W074', '急诊外科二病区', 19580049, 20, 0,
       1, '急诊外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000074);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000075, 'W075', '急诊儿科二病区', 19580051, 16, 0,
       1, '急诊儿科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000075);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000076, 'W076', '中医内科二病区', 19580053, 16, 0,
       1, '中医内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000076);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000077, 'W077', '中医骨伤科二病区', 19580054, 16, 0,
       1, '中医骨伤科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000077);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000078, 'W078', '针灸推拿科二病区', 19580055, 16, 0,
       1, '针灸推拿科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000078);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000079, 'W079', '中医妇科二病区', 19580056, 16, 0,
       1, '中医妇科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000079);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000080, 'W080', '中医儿科二病区', 19580057, 16, 0,
       1, '中医儿科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000080);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000081, 'W081', '肿瘤内科二病区', 19580059, 24, 0,
       1, '肿瘤内科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000081);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000082, 'W082', '肿瘤内科三病区', 19580059, 24, 0,
       1, '肿瘤内科三病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000082);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000083, 'W083', '肿瘤外科二病区', 19580060, 20, 0,
       1, '肿瘤外科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000083);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000084, 'W084', '安宁疗护病房二病区', 19580062, 16, 0,
       1, '安宁疗护病房二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000084);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000085, 'W085', '临床心理科二病区', 19580065, 16, 0,
       1, '临床心理科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000085);
INSERT INTO `sys_ward`
  (`ward_id`, `ward_code`, `ward_name`, `dept_id`, `total_beds`, `occupied_beds`,
   `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8600000000000000086, 'W086', '肝病科二病区', 19580070, 20, 0,
       1, '肝病科二病区（批量扩编新增护理单元）', 'admin', NOW(), 'admin', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_ward` WHERE `ward_id` = 8600000000000000086);

-- ---------- 3. 病区-科室本位关系 ----------
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000052, 8600000000000000041, 19580003, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000053, 8600000000000000042, 19580003, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000054, 8600000000000000043, 19580004, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000055, 8600000000000000044, 19580004, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000056, 8600000000000000045, 19580005, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000057, 8600000000000000046, 19580005, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000058, 8600000000000000047, 19580006, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000059, 8600000000000000048, 19580007, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000060, 8600000000000000049, 19580008, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000061, 8600000000000000050, 19580009, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000062, 8600000000000000051, 19580010, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000063, 8600000000000000052, 19580011, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000064, 8600000000000000053, 19580086, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000065, 8600000000000000054, 19580013, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000066, 8600000000000000055, 19580013, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000067, 8600000000000000056, 19580014, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000068, 8600000000000000057, 19580014, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000069, 8600000000000000058, 19580015, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000070, 8600000000000000059, 19580016, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000071, 8600000000000000060, 19580017, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000072, 8600000000000000061, 19580018, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000073, 8600000000000000062, 19580019, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000074, 8600000000000000063, 19580020, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000075, 8600000000000000064, 19580087, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000076, 8600000000000000065, 19580088, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000077, 8600000000000000066, 19580022, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000078, 8600000000000000067, 19580023, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000079, 8600000000000000068, 19580023, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000080, 8600000000000000069, 19580026, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000081, 8600000000000000070, 19580026, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000082, 8600000000000000071, 19580027, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000083, 8600000000000000072, 19580028, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000084, 8600000000000000073, 19580048, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000085, 8600000000000000074, 19580049, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000086, 8600000000000000075, 19580051, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000087, 8600000000000000076, 19580053, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000088, 8600000000000000077, 19580054, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000089, 8600000000000000078, 19580055, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000090, 8600000000000000079, 19580056, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000091, 8600000000000000080, 19580057, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000092, 8600000000000000081, 19580059, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000093, 8600000000000000082, 19580059, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000094, 8600000000000000083, 19580060, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000095, 8600000000000000084, 19580062, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000096, 8600000000000000085, 19580065, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);
INSERT IGNORE INTO `sys_ward_dept`
  (`id`, `ward_id`, `dept_id`, `is_primary`, `bed_quota`, `sort_order`,
   `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES (9100000000000000097, 8600000000000000086, 19580070, 1, NULL, 0, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 4. 新病区床位（全部空闲） ----------
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001071, '01', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001072, '02', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001073, '03', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001074, '04', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001075, '05', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001076, '06', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001077, '07', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001078, '08', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001079, '09', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001080, '10', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001081, '11', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001082, '12', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001083, '13', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001084, '14', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001085, '15', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001086, '16', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001087, '17', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001088, '18', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001089, '19', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001090, '20', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001091, '21', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001092, '22', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001093, '23', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001094, '24', 8600000000000000041, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001095, '01', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001096, '02', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001097, '03', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001098, '04', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001099, '05', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001100, '06', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001101, '07', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001102, '08', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001103, '09', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001104, '10', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001105, '11', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001106, '12', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001107, '13', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001108, '14', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001109, '15', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001110, '16', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001111, '17', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001112, '18', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001113, '19', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001114, '20', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001115, '21', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001116, '22', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001117, '23', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001118, '24', 8600000000000000042, 19580003, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001119, '01', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001120, '02', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001121, '03', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001122, '04', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001123, '05', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001124, '06', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001125, '07', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001126, '08', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001127, '09', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001128, '10', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001129, '11', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001130, '12', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001131, '13', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001132, '14', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001133, '15', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001134, '16', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001135, '17', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001136, '18', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001137, '19', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001138, '20', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001139, '21', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001140, '22', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001141, '23', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001142, '24', 8600000000000000043, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001143, '01', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001144, '02', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001145, '03', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001146, '04', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001147, '05', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001148, '06', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001149, '07', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001150, '08', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001151, '09', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001152, '10', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001153, '11', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001154, '12', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001155, '13', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001156, '14', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001157, '15', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001158, '16', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001159, '17', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001160, '18', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001161, '19', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001162, '20', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001163, '21', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001164, '22', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001165, '23', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001166, '24', 8600000000000000044, 19580004, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001167, '01', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001168, '02', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001169, '03', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001170, '04', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001171, '05', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001172, '06', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001173, '07', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001174, '08', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001175, '09', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001176, '10', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001177, '11', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001178, '12', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001179, '13', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001180, '14', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001181, '15', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001182, '16', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001183, '17', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001184, '18', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001185, '19', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001186, '20', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001187, '21', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001188, '22', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001189, '23', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001190, '24', 8600000000000000045, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001191, '01', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001192, '02', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001193, '03', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001194, '04', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001195, '05', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001196, '06', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001197, '07', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001198, '08', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001199, '09', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001200, '10', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001201, '11', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001202, '12', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001203, '13', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001204, '14', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001205, '15', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001206, '16', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001207, '17', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001208, '18', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001209, '19', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001210, '20', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001211, '21', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001212, '22', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001213, '23', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001214, '24', 8600000000000000046, 19580005, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001215, '01', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001216, '02', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001217, '03', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001218, '04', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001219, '05', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001220, '06', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001221, '07', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001222, '08', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001223, '09', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001224, '10', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001225, '11', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001226, '12', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001227, '13', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001228, '14', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001229, '15', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001230, '16', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001231, '17', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001232, '18', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001233, '19', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001234, '20', 8600000000000000047, 19580006, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001235, '01', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001236, '02', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001237, '03', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001238, '04', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001239, '05', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001240, '06', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001241, '07', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001242, '08', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001243, '09', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001244, '10', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001245, '11', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001246, '12', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001247, '13', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001248, '14', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001249, '15', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001250, '16', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001251, '17', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001252, '18', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001253, '19', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001254, '20', 8600000000000000048, 19580007, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001255, '01', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001256, '02', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001257, '03', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001258, '04', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001259, '05', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001260, '06', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001261, '07', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001262, '08', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001263, '09', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001264, '10', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001265, '11', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001266, '12', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001267, '13', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001268, '14', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001269, '15', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001270, '16', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001271, '17', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001272, '18', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001273, '19', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001274, '20', 8600000000000000049, 19580008, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001275, '01', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001276, '02', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001277, '03', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001278, '04', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001279, '05', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001280, '06', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001281, '07', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001282, '08', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001283, '09', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001284, '10', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001285, '11', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001286, '12', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001287, '13', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001288, '14', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001289, '15', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001290, '16', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001291, '17', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001292, '18', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001293, '19', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001294, '20', 8600000000000000050, 19580009, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001295, '01', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001296, '02', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001297, '03', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001298, '04', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001299, '05', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001300, '06', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001301, '07', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001302, '08', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001303, '09', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001304, '10', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001305, '11', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001306, '12', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001307, '13', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001308, '14', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001309, '15', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001310, '16', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001311, '17', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001312, '18', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001313, '19', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001314, '20', 8600000000000000051, 19580010, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001315, '01', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001316, '02', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001317, '03', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001318, '04', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001319, '05', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001320, '06', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001321, '07', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001322, '08', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001323, '09', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001324, '10', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001325, '11', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001326, '12', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001327, '13', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001328, '14', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001329, '15', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001330, '16', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001331, '17', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001332, '18', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001333, '19', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001334, '20', 8600000000000000052, 19580011, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001335, '01', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001336, '02', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001337, '03', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001338, '04', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001339, '05', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001340, '06', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001341, '07', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001342, '08', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001343, '09', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001344, '10', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001345, '11', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001346, '12', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001347, '13', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001348, '14', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001349, '15', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001350, '16', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001351, '17', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001352, '18', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001353, '19', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001354, '20', 8600000000000000053, 19580086, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001355, '01', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001356, '02', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001357, '03', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001358, '04', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001359, '05', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001360, '06', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001361, '07', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001362, '08', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001363, '09', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001364, '10', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001365, '11', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001366, '12', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001367, '13', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001368, '14', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001369, '15', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001370, '16', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001371, '17', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001372, '18', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001373, '19', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001374, '20', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001375, '21', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001376, '22', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001377, '23', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001378, '24', 8600000000000000054, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001379, '01', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001380, '02', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001381, '03', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001382, '04', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001383, '05', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001384, '06', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001385, '07', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001386, '08', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001387, '09', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001388, '10', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001389, '11', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001390, '12', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001391, '13', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001392, '14', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001393, '15', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001394, '16', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001395, '17', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001396, '18', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001397, '19', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001398, '20', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001399, '21', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001400, '22', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001401, '23', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001402, '24', 8600000000000000055, 19580013, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001403, '01', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001404, '02', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001405, '03', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001406, '04', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001407, '05', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001408, '06', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001409, '07', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001410, '08', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001411, '09', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001412, '10', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001413, '11', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001414, '12', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001415, '13', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001416, '14', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001417, '15', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001418, '16', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001419, '17', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001420, '18', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001421, '19', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001422, '20', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001423, '21', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001424, '22', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001425, '23', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001426, '24', 8600000000000000056, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001427, '01', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001428, '02', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001429, '03', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001430, '04', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001431, '05', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001432, '06', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001433, '07', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001434, '08', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001435, '09', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001436, '10', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001437, '11', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001438, '12', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001439, '13', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001440, '14', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001441, '15', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001442, '16', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001443, '17', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001444, '18', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001445, '19', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001446, '20', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001447, '21', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001448, '22', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001449, '23', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001450, '24', 8600000000000000057, 19580014, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001451, '01', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001452, '02', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001453, '03', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001454, '04', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001455, '05', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001456, '06', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001457, '07', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001458, '08', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001459, '09', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001460, '10', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001461, '11', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001462, '12', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001463, '13', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001464, '14', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001465, '15', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001466, '16', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001467, '17', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001468, '18', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001469, '19', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001470, '20', 8600000000000000058, 19580015, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001471, '01', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001472, '02', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001473, '03', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001474, '04', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001475, '05', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001476, '06', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001477, '07', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001478, '08', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001479, '09', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001480, '10', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001481, '11', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001482, '12', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001483, '13', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001484, '14', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001485, '15', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001486, '16', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001487, '17', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001488, '18', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001489, '19', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001490, '20', 8600000000000000059, 19580016, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001491, '01', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001492, '02', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001493, '03', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001494, '04', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001495, '05', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001496, '06', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001497, '07', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001498, '08', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001499, '09', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001500, '10', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001501, '11', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001502, '12', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001503, '13', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001504, '14', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001505, '15', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001506, '16', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001507, '17', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001508, '18', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001509, '19', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001510, '20', 8600000000000000060, 19580017, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001511, '01', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001512, '02', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001513, '03', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001514, '04', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001515, '05', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001516, '06', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001517, '07', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001518, '08', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001519, '09', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001520, '10', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001521, '11', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001522, '12', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001523, '13', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001524, '14', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001525, '15', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001526, '16', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001527, '17', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001528, '18', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001529, '19', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001530, '20', 8600000000000000061, 19580018, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001531, '01', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001532, '02', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001533, '03', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001534, '04', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001535, '05', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001536, '06', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001537, '07', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001538, '08', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001539, '09', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001540, '10', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001541, '11', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001542, '12', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001543, '13', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001544, '14', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001545, '15', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001546, '16', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001547, '17', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001548, '18', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001549, '19', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001550, '20', 8600000000000000062, 19580019, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001551, '01', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001552, '02', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001553, '03', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001554, '04', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001555, '05', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001556, '06', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001557, '07', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001558, '08', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001559, '09', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001560, '10', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001561, '11', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001562, '12', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001563, '13', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001564, '14', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001565, '15', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001566, '16', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001567, '17', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001568, '18', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001569, '19', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001570, '20', 8600000000000000063, 19580020, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001571, '01', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001572, '02', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001573, '03', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001574, '04', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001575, '05', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001576, '06', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001577, '07', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001578, '08', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001579, '09', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001580, '10', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001581, '11', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001582, '12', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001583, '13', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001584, '14', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001585, '15', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001586, '16', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001587, '17', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001588, '18', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001589, '19', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001590, '20', 8600000000000000064, 19580087, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001591, '01', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001592, '02', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001593, '03', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001594, '04', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001595, '05', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001596, '06', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001597, '07', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001598, '08', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001599, '09', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001600, '10', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001601, '11', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001602, '12', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001603, '13', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001604, '14', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001605, '15', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001606, '16', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001607, '17', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001608, '18', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001609, '19', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001610, '20', 8600000000000000065, 19580088, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001611, '01', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001612, '02', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001613, '03', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001614, '04', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001615, '05', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001616, '06', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001617, '07', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001618, '08', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001619, '09', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001620, '10', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001621, '11', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001622, '12', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001623, '13', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001624, '14', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001625, '15', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001626, '16', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001627, '17', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001628, '18', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001629, '19', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001630, '20', 8600000000000000066, 19580022, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001631, '01', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001632, '02', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001633, '03', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001634, '04', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001635, '05', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001636, '06', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001637, '07', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001638, '08', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001639, '09', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001640, '10', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001641, '11', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001642, '12', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001643, '13', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001644, '14', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001645, '15', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001646, '16', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001647, '17', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001648, '18', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001649, '19', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001650, '20', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001651, '21', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001652, '22', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001653, '23', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001654, '24', 8600000000000000067, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001655, '01', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001656, '02', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001657, '03', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001658, '04', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001659, '05', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001660, '06', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001661, '07', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001662, '08', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001663, '09', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001664, '10', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001665, '11', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001666, '12', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001667, '13', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001668, '14', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001669, '15', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001670, '16', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001671, '17', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001672, '18', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001673, '19', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001674, '20', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001675, '21', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001676, '22', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001677, '23', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001678, '24', 8600000000000000068, 19580023, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001679, '01', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001680, '02', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001681, '03', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001682, '04', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001683, '05', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001684, '06', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001685, '07', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001686, '08', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001687, '09', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001688, '10', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001689, '11', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001690, '12', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001691, '13', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001692, '14', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001693, '15', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001694, '16', 8600000000000000069, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001695, '01', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001696, '02', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001697, '03', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001698, '04', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001699, '05', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001700, '06', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001701, '07', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001702, '08', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001703, '09', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001704, '10', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001705, '11', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001706, '12', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001707, '13', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001708, '14', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001709, '15', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001710, '16', 8600000000000000070, 19580026, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001711, '01', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001712, '02', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001713, '03', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001714, '04', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001715, '05', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001716, '06', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001717, '07', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001718, '08', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001719, '09', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001720, '10', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001721, '11', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001722, '12', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001723, '13', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001724, '14', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001725, '15', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001726, '16', 8600000000000000071, 19580027, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001727, '01', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001728, '02', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001729, '03', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001730, '04', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001731, '05', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001732, '06', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001733, '07', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001734, '08', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001735, '09', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001736, '10', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001737, '11', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001738, '12', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001739, '13', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001740, '14', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001741, '15', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001742, '16', 8600000000000000072, 19580028, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001743, '01', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001744, '02', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001745, '03', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001746, '04', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001747, '05', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001748, '06', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001749, '07', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001750, '08', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001751, '09', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001752, '10', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001753, '11', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001754, '12', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001755, '13', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001756, '14', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001757, '15', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001758, '16', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001759, '17', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001760, '18', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001761, '19', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001762, '20', 8600000000000000073, 19580048, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001763, '01', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001764, '02', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001765, '03', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001766, '04', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001767, '05', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001768, '06', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001769, '07', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001770, '08', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001771, '09', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001772, '10', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001773, '11', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001774, '12', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001775, '13', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001776, '14', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001777, '15', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001778, '16', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001779, '17', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001780, '18', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001781, '19', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001782, '20', 8600000000000000074, 19580049, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001783, '01', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001784, '02', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001785, '03', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001786, '04', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001787, '05', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001788, '06', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001789, '07', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001790, '08', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001791, '09', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001792, '10', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001793, '11', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001794, '12', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001795, '13', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001796, '14', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001797, '15', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001798, '16', 8600000000000000075, 19580051, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001799, '01', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001800, '02', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001801, '03', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001802, '04', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001803, '05', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001804, '06', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001805, '07', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001806, '08', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001807, '09', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001808, '10', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001809, '11', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001810, '12', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001811, '13', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001812, '14', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001813, '15', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001814, '16', 8600000000000000076, 19580053, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001815, '01', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001816, '02', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001817, '03', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001818, '04', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001819, '05', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001820, '06', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001821, '07', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001822, '08', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001823, '09', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001824, '10', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001825, '11', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001826, '12', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001827, '13', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001828, '14', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001829, '15', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001830, '16', 8600000000000000077, 19580054, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001831, '01', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001832, '02', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001833, '03', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001834, '04', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001835, '05', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001836, '06', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001837, '07', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001838, '08', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001839, '09', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001840, '10', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001841, '11', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001842, '12', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001843, '13', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001844, '14', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001845, '15', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001846, '16', 8600000000000000078, 19580055, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001847, '01', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001848, '02', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001849, '03', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001850, '04', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001851, '05', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001852, '06', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001853, '07', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001854, '08', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001855, '09', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001856, '10', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001857, '11', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001858, '12', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001859, '13', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001860, '14', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001861, '15', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001862, '16', 8600000000000000079, 19580056, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001863, '01', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001864, '02', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001865, '03', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001866, '04', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001867, '05', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001868, '06', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001869, '07', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001870, '08', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001871, '09', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001872, '10', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001873, '11', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001874, '12', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001875, '13', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001876, '14', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001877, '15', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001878, '16', 8600000000000000080, 19580057, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001879, '01', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001880, '02', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001881, '03', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001882, '04', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001883, '05', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001884, '06', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001885, '07', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001886, '08', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001887, '09', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001888, '10', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001889, '11', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001890, '12', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001891, '13', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001892, '14', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001893, '15', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001894, '16', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001895, '17', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001896, '18', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001897, '19', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001898, '20', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001899, '21', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001900, '22', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001901, '23', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001902, '24', 8600000000000000081, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001903, '01', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001904, '02', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001905, '03', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001906, '04', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001907, '05', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001908, '06', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001909, '07', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001910, '08', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001911, '09', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001912, '10', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001913, '11', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001914, '12', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001915, '13', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001916, '14', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001917, '15', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001918, '16', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001919, '17', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001920, '18', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001921, '19', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001922, '20', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001923, '21', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001924, '22', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001925, '23', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001926, '24', 8600000000000000082, 19580059, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001927, '01', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001928, '02', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001929, '03', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001930, '04', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001931, '05', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001932, '06', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001933, '07', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001934, '08', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001935, '09', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001936, '10', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001937, '11', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001938, '12', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001939, '13', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001940, '14', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001941, '15', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001942, '16', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001943, '17', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001944, '18', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001945, '19', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001946, '20', 8600000000000000083, 19580060, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001947, '01', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001948, '02', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001949, '03', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001950, '04', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001951, '05', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001952, '06', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001953, '07', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001954, '08', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001955, '09', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001956, '10', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001957, '11', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001958, '12', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001959, '13', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001960, '14', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001961, '15', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001962, '16', 8600000000000000084, 19580062, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001963, '01', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001964, '02', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001965, '03', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001966, '04', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001967, '05', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001968, '06', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001969, '07', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001970, '08', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001971, '09', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001972, '10', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001973, '11', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001974, '12', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001975, '13', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001976, '14', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001977, '15', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001978, '16', 8600000000000000085, 19580065, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001979, '01', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001980, '02', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001981, '03', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001982, '04', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001983, '05', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001984, '06', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001985, '07', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001986, '08', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001987, '09', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001988, '10', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001989, '11', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001990, '12', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001991, '13', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001992, '14', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001993, '15', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001994, '16', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001995, '17', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001996, '18', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001997, '19', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);
INSERT IGNORE INTO `sys_bed`
  (`bed_id`, `bed_no`, `ward_id`, `dept_id`, `bed_type`, `bed_status`, `patient_id`, `remark`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES (8620000000000001998, '20', 8600000000000000086, 19580070, 'normal', 1, NULL, NULL, 'admin', NOW(), 'admin', NOW(), 0, 1, 1);

-- ---------- 5. 自检（执行后放开核对） ----------
-- 5.1 各科室病区数（应 level3=3 / level2=2 / ICU=1）
-- SELECT dept_id, COUNT(*) ward_cnt FROM sys_ward GROUP BY dept_id ORDER BY dept_id;
-- 5.2 本位一致性：is_primary=1 科室必须等于 sys_ward.dept_id（应 0 行）
-- SELECT w.ward_id, w.ward_name, w.dept_id, wd.dept_id primary_dept FROM sys_ward w
--   JOIN sys_ward_dept wd ON wd.ward_id=w.ward_id AND wd.is_primary=1 WHERE wd.dept_id<>w.dept_id;
-- 5.3 新病区床位与空闲数
-- SELECT COUNT(*) bed_total, SUM(bed_status=1) free_beds FROM sys_bed WHERE bed_id >= 8620000000000001071;

COMMIT;