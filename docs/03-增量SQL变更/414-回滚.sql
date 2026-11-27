-- 414 回滚：撤销批量新增病区/关系/床位，老病区与规则快照改回原名
-- 日期：2026-11-27
-- 前置：新增病区不得有在院患者（占用床），否则先出院/转科再回滚。
SET NAMES utf8mb4;
START TRANSACTION;

DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000041;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000042;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000043;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000044;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000045;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000046;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000047;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000048;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000049;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000050;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000051;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000052;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000053;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000054;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000055;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000056;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000057;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000058;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000059;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000060;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000061;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000062;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000063;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000064;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000065;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000066;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000067;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000068;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000069;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000070;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000071;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000072;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000073;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000074;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000075;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000076;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000077;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000078;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000079;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000080;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000081;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000082;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000083;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000084;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000085;
DELETE FROM `sys_bed` WHERE `ward_id` = 8600000000000000086;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000041;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000042;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000043;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000044;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000045;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000046;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000047;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000048;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000049;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000050;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000051;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000052;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000053;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000054;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000055;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000056;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000057;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000058;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000059;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000060;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000061;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000062;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000063;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000064;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000065;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000066;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000067;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000068;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000069;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000070;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000071;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000072;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000073;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000074;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000075;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000076;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000077;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000078;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000079;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000080;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000081;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000082;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000083;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000084;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000085;
DELETE FROM `sys_ward_dept` WHERE `ward_id` = 8600000000000000086;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000041;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000042;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000043;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000044;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000045;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000046;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000047;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000048;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000049;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000050;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000051;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000052;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000053;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000054;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000055;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000056;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000057;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000058;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000059;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000060;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000061;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000062;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000063;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000064;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000065;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000066;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000067;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000068;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000069;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000070;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000071;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000072;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000073;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000074;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000075;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000076;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000077;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000078;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000079;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000080;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000081;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000082;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000083;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000084;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000085;
DELETE FROM `sys_ward` WHERE `ward_id` = 8600000000000000086;

UPDATE `sys_ward` SET `ward_name` = '呼吸内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000001 AND `ward_name` = '呼吸内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '呼吸内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000001 AND `org_name` = '呼吸内科一病区';
UPDATE `sys_ward` SET `ward_name` = '消化内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000002 AND `ward_name` = '消化内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '消化内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000002 AND `org_name` = '消化内科一病区';
UPDATE `sys_ward` SET `ward_name` = '心血管内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000003 AND `ward_name` = '心血管内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '心血管内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000003 AND `org_name` = '心血管内科一病区';
UPDATE `sys_ward` SET `ward_name` = '神经内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000004 AND `ward_name` = '神经内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '神经内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000004 AND `org_name` = '神经内科一病区';
UPDATE `sys_ward` SET `ward_name` = '内分泌科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000005 AND `ward_name` = '内分泌科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '内分泌科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000005 AND `org_name` = '内分泌科一病区';
UPDATE `sys_ward` SET `ward_name` = '肾内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000006 AND `ward_name` = '肾内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肾内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000006 AND `org_name` = '肾内科一病区';
UPDATE `sys_ward` SET `ward_name` = '血液内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000007 AND `ward_name` = '血液内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '血液内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000007 AND `org_name` = '血液内科一病区';
UPDATE `sys_ward` SET `ward_name` = '风湿免疫科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000008 AND `ward_name` = '风湿免疫科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '风湿免疫科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000008 AND `org_name` = '风湿免疫科一病区';
UPDATE `sys_ward` SET `ward_name` = '老年病科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000009 AND `ward_name` = '老年病科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '老年病科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000009 AND `org_name` = '老年病科一病区';
UPDATE `sys_ward` SET `ward_name` = '全科医学科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000010 AND `ward_name` = '全科医学科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '全科医学科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000010 AND `org_name` = '全科医学科一病区';
UPDATE `sys_ward` SET `ward_name` = '普通外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000011 AND `ward_name` = '普通外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '普通外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000011 AND `org_name` = '普通外科一病区';
UPDATE `sys_ward` SET `ward_name` = '骨科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000012 AND `ward_name` = '骨科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '骨科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000012 AND `org_name` = '骨科一病区';
UPDATE `sys_ward` SET `ward_name` = '神经外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000013 AND `ward_name` = '神经外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '神经外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000013 AND `org_name` = '神经外科一病区';
UPDATE `sys_ward` SET `ward_name` = '心胸外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000014 AND `ward_name` = '心胸外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '心胸外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000014 AND `org_name` = '心胸外科一病区';
UPDATE `sys_ward` SET `ward_name` = '泌尿外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000015 AND `ward_name` = '泌尿外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '泌尿外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000015 AND `org_name` = '泌尿外科一病区';
UPDATE `sys_ward` SET `ward_name` = '烧伤整形外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000016 AND `ward_name` = '烧伤整形外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '烧伤整形外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000016 AND `org_name` = '烧伤整形外科一病区';
UPDATE `sys_ward` SET `ward_name` = '血管外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000017 AND `ward_name` = '血管外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '血管外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000017 AND `org_name` = '血管外科一病区';
UPDATE `sys_ward` SET `ward_name` = '肝胆外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000018 AND `ward_name` = '肝胆外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肝胆外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000018 AND `org_name` = '肝胆外科一病区';
UPDATE `sys_ward` SET `ward_name` = '甲乳外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000019 AND `ward_name` = '甲乳外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '甲乳外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000019 AND `org_name` = '甲乳外科一病区';
UPDATE `sys_ward` SET `ward_name` = '肛肠外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000020 AND `ward_name` = '肛肠外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肛肠外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000020 AND `org_name` = '肛肠外科一病区';
UPDATE `sys_ward` SET `ward_name` = '妇科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000021 AND `ward_name` = '妇科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '妇科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000021 AND `org_name` = '妇科一病区';
UPDATE `sys_ward` SET `ward_name` = '产科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000022 AND `ward_name` = '产科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '产科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000022 AND `org_name` = '产科一病区';
UPDATE `sys_ward` SET `ward_name` = '小儿内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000023 AND `ward_name` = '小儿内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '小儿内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000023 AND `org_name` = '小儿内科一病区';
UPDATE `sys_ward` SET `ward_name` = '小儿外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000024 AND `ward_name` = '小儿外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '小儿外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000024 AND `org_name` = '小儿外科一病区';
UPDATE `sys_ward` SET `ward_name` = '新生儿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000025 AND `ward_name` = '新生儿科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '新生儿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000025 AND `org_name` = '新生儿科一病区';
UPDATE `sys_ward` SET `ward_name` = '急诊内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000026 AND `ward_name` = '急诊内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '急诊内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000026 AND `org_name` = '急诊内科一病区';
UPDATE `sys_ward` SET `ward_name` = '急诊外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000027 AND `ward_name` = '急诊外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '急诊外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000027 AND `org_name` = '急诊外科一病区';
UPDATE `sys_ward` SET `ward_name` = '急诊儿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000029 AND `ward_name` = '急诊儿科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '急诊儿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000029 AND `org_name` = '急诊儿科一病区';
UPDATE `sys_ward` SET `ward_name` = '中医内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000030 AND `ward_name` = '中医内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000030 AND `org_name` = '中医内科一病区';
UPDATE `sys_ward` SET `ward_name` = '中医骨伤科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000031 AND `ward_name` = '中医骨伤科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医骨伤科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000031 AND `org_name` = '中医骨伤科一病区';
UPDATE `sys_ward` SET `ward_name` = '针灸推拿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000032 AND `ward_name` = '针灸推拿科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '针灸推拿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000032 AND `org_name` = '针灸推拿科一病区';
UPDATE `sys_ward` SET `ward_name` = '中医妇科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000033 AND `ward_name` = '中医妇科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医妇科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000033 AND `org_name` = '中医妇科一病区';
UPDATE `sys_ward` SET `ward_name` = '中医儿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000034 AND `ward_name` = '中医儿科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '中医儿科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000034 AND `org_name` = '中医儿科一病区';
UPDATE `sys_ward` SET `ward_name` = '肿瘤内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000035 AND `ward_name` = '肿瘤内科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肿瘤内科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000035 AND `org_name` = '肿瘤内科一病区';
UPDATE `sys_ward` SET `ward_name` = '肿瘤外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000036 AND `ward_name` = '肿瘤外科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肿瘤外科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000036 AND `org_name` = '肿瘤外科一病区';
UPDATE `sys_ward` SET `ward_name` = '安宁疗护病房病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000037 AND `ward_name` = '安宁疗护病房一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '安宁疗护病房病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000037 AND `org_name` = '安宁疗护病房一病区';
UPDATE `sys_ward` SET `ward_name` = '临床心理科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000038 AND `ward_name` = '临床心理科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '临床心理科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000038 AND `org_name` = '临床心理科一病区';
UPDATE `sys_ward` SET `ward_name` = '肝病科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `ward_id` = 8600000000000000039 AND `ward_name` = '肝病科一病区';
UPDATE `biz_staff_plan_rule` SET `org_name` = '肝病科病区', `update_by` = 'admin', `update_time` = NOW()
 WHERE `org_type` = 2 AND `org_id` = 8600000000000000039 AND `org_name` = '肝病科一病区';

COMMIT;