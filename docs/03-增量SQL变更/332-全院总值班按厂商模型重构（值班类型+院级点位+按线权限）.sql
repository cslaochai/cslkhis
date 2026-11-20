-- 332 全院总值班按厂商模型重构：值班类型（可维护）+ 院级点位 + 按线权限
--
-- 对标依据（见会话调研）：厂商产品里「总值班」是独立功能点，能力是
-- 「自定义添加值班类型 / 一键发布通知全院 / 值班前提醒 / 一键呼叫 / 快速排班 + 日常调班」，
-- 且与「各科室排班汇总」并列、不共用一个页面（黄山市人民医院排班采购功能清单 1.6 与 1.7）。
-- 制度侧：行政总值班归院办、医疗总值班归医务科、护理总值班归护理部，院领导带班由院办按月排。
--
-- 本次只动「全院值班」这一块：
--   ① 新增值班类型（可维护，不是写死枚举）——医疗/护理/行政/院领导带班
--   ② 新建 16 个院级点位（类型 × 白/夜 × 主/副），点位名里不再写班次/角色
--   ③ 原 4 个把班次+角色写进名字的点位「停用」（不删，历史行仍挂着它们）
--   ④ 权限按值班线分：医务科→医疗线、护理部(29)→护理线、院办(新建角色36)→行政线+审核发布
-- 排班侧一律不动：护理排班 331 / 门诊排班 804 / 急诊排班 29611 / 员工排班 2935、
-- biz_schedule 写入、234 个科室医师值班点位——全部原样。

SET NAMES utf8mb4;
START TRANSACTION;

-- ① 值班类型表
CREATE TABLE IF NOT EXISTS `biz_duty_type` (
  `id`                 bigint       NOT NULL COMMENT '主键',
  `type_code`          varchar(32)  NOT NULL COMMENT '类型编码',
  `type_name`          varchar(64)  NOT NULL COMMENT '类型名称（医疗总值班/护理总值班/行政总值班/院领导带班…）',
  `manage_dept_id`     bigint       NOT NULL COMMENT '归口科室ID（这一条值班线由哪个部门排班）',
  `manage_dept_name`   varchar(100) DEFAULT NULL COMMENT '归口科室名称（快照）',
  `required_staff_type` tinyint     DEFAULT NULL COMMENT '应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-不限）',
  `required_positions` varchar(200) DEFAULT NULL COMMENT '允许的职务码值（字典 hospital_position，逗号分隔，空-不限）',
  `leader_layer`       tinyint      DEFAULT NULL COMMENT '是否需要院领导带班层（1-是 0-否）',
  `sort_no`            int          DEFAULT NULL COMMENT '排序号',
  `status`             tinyint      DEFAULT NULL COMMENT '状态（1-启用 0-停用）',
  `remark`             varchar(200) DEFAULT NULL COMMENT '备注',
  `create_by`          varchar(64)  DEFAULT NULL COMMENT '创建人',
  `create_time`        datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`          varchar(64)  DEFAULT NULL COMMENT '更新人',
  `update_time`        datetime     DEFAULT NULL COMMENT '更新时间',
  `del_flag`           tinyint      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-已删除）',
  `create_by_id`       bigint       DEFAULT NULL COMMENT '创建人ID',
  `update_by_id`       bigint       DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班类型（值班线）';

-- ② 类型种子：归口科室 + 资质要求（职务码值取自字典 hospital_position）
INSERT INTO `biz_duty_type` (`id`,`type_code`,`type_name`,`manage_dept_id`,`manage_dept_name`,
       `required_staff_type`,`required_positions`,`leader_layer`,`sort_no`,`status`,`remark`,
       `create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`create_by_id`,`update_by_id`)
SELECT 896750000000000001,'MEDICAL','医疗总值班',19580077,'医务科',1,'1,2,16',1,1,1,
       '临床、医技科室正/副主任担任，医务科按月排班',  'admin',NOW(),'admin',NOW(),0,1,1
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_type` WHERE `type_code`='MEDICAL');
INSERT INTO `biz_duty_type` (`id`,`type_code`,`type_name`,`manage_dept_id`,`manage_dept_name`,
       `required_staff_type`,`required_positions`,`leader_layer`,`sort_no`,`status`,`remark`,
       `create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`create_by_id`,`update_by_id`)
SELECT 896750000000000002,'NURSING','护理总值班',19580078,'护理部',2,'7,8,9',1,2,1,
       '全院护士长轮流承担，护理部按月排班','admin',NOW(),'admin',NOW(),0,1,1
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_type` WHERE `type_code`='NURSING');
INSERT INTO `biz_duty_type` (`id`,`type_code`,`type_name`,`manage_dept_id`,`manage_dept_name`,
       `required_staff_type`,`required_positions`,`leader_layer`,`sort_no`,`status`,`remark`,
       `create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`create_by_id`,`update_by_id`)
SELECT 896750000000000003,'ADMIN','行政总值班',19580076,'院办',6,'21,22,23,24',1,3,1,
       '职能科室中层与行政干事担任，院办按月排班并全院公开','admin',NOW(),'admin',NOW(),0,1,1
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_type` WHERE `type_code`='ADMIN');
INSERT INTO `biz_duty_type` (`id`,`type_code`,`type_name`,`manage_dept_id`,`manage_dept_name`,
       `required_staff_type`,`required_positions`,`leader_layer`,`sort_no`,`status`,`remark`,
       `create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`create_by_id`,`update_by_id`)
SELECT 896750000000000004,'LEADER','院领导带班',19580076,'院办',6,'21',0,4,1,
       '院级领导二线带班，院办随同月度值班表一并排','admin',NOW(),'admin',NOW(),0,1,1
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_type` WHERE `type_code`='LEADER');

-- ③ 加点位与值守行上的类型外键
ALTER TABLE `biz_duty_post`   ADD COLUMN `duty_type_id` bigint DEFAULT NULL COMMENT '值班类型ID（院级点位挂类型，科室点位为空）';
ALTER TABLE `biz_duty_roster` ADD COLUMN `duty_type_id` bigint DEFAULT NULL COMMENT '值班类型ID（院级值守行冗余一列，列表按线筛）';

-- ④ 新建 16 个院级点位：类型 × 白/夜 × 主/副（班次与角色回归各自字段，不再写进点位名）
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000201,'DUTY_MED_DAY_MAIN','医疗总值班-白班-主班',1,3,0,896750000000000001,1,1,3,890000000000000021,1, 1,1,'admin',NOW(),'admin',NOW(),0,1,1,'医务科排班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000201);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000202,'DUTY_MED_DAY_VICE','医疗总值班-白班-副班',1,3,0,896750000000000001,2,2,3,890000000000000021,1, 2,1,'admin',NOW(),'admin',NOW(),0,1,1,'备班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000202);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000203,'DUTY_MED_NIGHT_MAIN','医疗总值班-夜班-主班',1,3,0,896750000000000001,1,1,3,890000000000000022,1, 3,1,'admin',NOW(),'admin',NOW(),0,1,1,'医务科排班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000203);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000204,'DUTY_MED_NIGHT_VICE','医疗总值班-夜班-副班',1,3,0,896750000000000001,2,2,3,890000000000000022,1, 4,1,'admin',NOW(),'admin',NOW(),0,1,1,'备班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000204);

INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000211,'DUTY_NUR_DAY_MAIN','护理总值班-白班-主班',1,3,0,896750000000000002,1,1,3,890000000000000021,2, 11,1,'admin',NOW(),'admin',NOW(),0,1,1,'护理部排班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000211);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000212,'DUTY_NUR_DAY_VICE','护理总值班-白班-副班',1,3,0,896750000000000002,2,2,3,890000000000000021,2, 12,1,'admin',NOW(),'admin',NOW(),0,1,1,'备班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000212);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000213,'DUTY_NUR_NIGHT_MAIN','护理总值班-夜班-主班',1,3,0,896750000000000002,1,1,3,890000000000000022,2, 13,1,'admin',NOW(),'admin',NOW(),0,1,1,'护理部排班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000213);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000214,'DUTY_NUR_NIGHT_VICE','护理总值班-夜班-副班',1,3,0,896750000000000002,2,2,3,890000000000000022,2, 14,1,'admin',NOW(),'admin',NOW(),0,1,1,'备班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000214);

INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000221,'DUTY_ADM_DAY_MAIN','行政总值班-白班-主班',1,3,0,896750000000000003,1,1,3,890000000000000021,6, 21,1,'admin',NOW(),'admin',NOW(),0,1,1,'院办排班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000221);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000222,'DUTY_ADM_DAY_VICE','行政总值班-白班-副班',1,3,0,896750000000000003,2,2,3,890000000000000021,6, 22,1,'admin',NOW(),'admin',NOW(),0,1,1,'备班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000222);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000223,'DUTY_ADM_NIGHT_MAIN','行政总值班-夜班-主班',1,3,0,896750000000000003,1,1,3,890000000000000022,6, 23,1,'admin',NOW(),'admin',NOW(),0,1,1,'院办排班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000223);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000224,'DUTY_ADM_NIGHT_VICE','行政总值班-夜班-副班',1,3,0,896750000000000003,2,2,3,890000000000000022,6, 24,1,'admin',NOW(),'admin',NOW(),0,1,1,'备班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000224);

INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000231,'DUTY_LEAD_DAY_MAIN','院领导带班-白班',1,3,0,896750000000000004,1,2,3,890000000000000021,6, 31,1,'admin',NOW(),'admin',NOW(),0,1,1,'二线带班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000231);
INSERT INTO `biz_duty_post` (`id`,`post_code`,`post_name`,`duty_scope`,`org_type`,`org_id`,
       `duty_type_id`,`role_type`,`duty_level`,`attend_mode`,`shift_id`,`required_staff_type`,
       `sort_no`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `create_by_id`,`update_by_id`, `remark`)
SELECT 896740000000000232,'DUTY_LEAD_NIGHT_MAIN','院领导带班-夜班',1,3,0,896750000000000004,1,2,3,890000000000000022,6, 32,1,'admin',NOW(),'admin',NOW(),0,1,1,'二线带班'
 WHERE NOT EXISTS (SELECT 1 FROM `biz_duty_post` WHERE `id`=896740000000000232);

-- ⑤ 原 4 个把班次/角色写进名字的点位停用（不删：30 行历史值守行仍挂着它们）
UPDATE `biz_duty_post` SET `status`=0,
       `remark`=CONCAT(IFNULL(`remark`,''),'｜2026-11-16 停用：班次与角色已回归独立字段，改用 DUTY_*_DAY/NIGHT_MAIN/VICE 系列点位')
 WHERE `id` IN (896740000000000101,896740000000000102,896740000000000103,896740000000000104)
   AND `status`=1;

-- ⑥ 值班类型维护页（2945 排班值守中心 → 值班类型）
INSERT INTO `sys_menu` (`id`,`menu_name`,`parent_id`,`sort_order`,`menu_type`,`path`,`component`,
       `menu_key`,`icon`,`permission`,`is_frame`,`is_cache`,`is_visible`,`status`,
       `create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`create_by_id`,`update_by_id`)
SELECT 2983,'值班类型',2945,9,2,'/duty-type','duty/DutyTypeView','dutyType','Calendar',
       'org:dutyType:list',0,0,1,1,'admin',NOW(),'admin',NOW(),0,
       '值班线维护：医疗总值/护理总值/行政总值/院领导带班，含归口科室与资质要求',1,1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id`=2983 OR `menu_key`='dutyType');
INSERT INTO `sys_menu` (`id`,`menu_name`,`parent_id`,`sort_order`,`menu_type`,`path`,`component`,
       `menu_key`,`icon`,`permission`,`is_frame`,`is_cache`,`is_visible`,`status`,
       `create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`create_by_id`,`update_by_id`)
SELECT 2984,'值班类型-维护',2983,1,3,'','','dutyTypeEdit','','org:dutyType:edit',0,0,1,1,
       'admin',NOW(),'admin',NOW(),0,'新增与修改共用（ORG 规范：编辑也走 add 同名码）',1,1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id`=2984 OR `permission`='org:dutyType:edit');
INSERT INTO `sys_menu` (`id`,`menu_name`,`parent_id`,`sort_order`,`menu_type`,`path`,`component`,
       `menu_key`,`icon`,`permission`,`is_frame`,`is_cache`,`is_visible`,`status`,
       `create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,`remark`,`create_by_id`,`update_by_id`)
SELECT 2985,'值班类型-删除',2983,2,3,'','','dutyTypeDelete','','org:dutyType:delete',0,0,1,1,
       'admin',NOW(),'admin',NOW(),0,NULL,1,1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id`=2985 OR `permission`='org:dutyType:delete');

-- ⑦ 角色 36 院办值班管理员（行政线 + 全院发布审核）
INSERT INTO `sys_role` (`id`,`role_code`,`role_name`,`role_type`,`staff_type`,`data_scope`,
       `sort_order`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`del_flag`,
       `remark`,`create_by_id`,`update_by_id`)
SELECT 36,'10046','院办值班管理员',1,6,1,43,1,'admin',NOW(),'admin',NOW(),0,
       '院办岗：行政总值班与院领导带班的排班、全流程发布审核、值班类型维护',1,1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `id`=36 OR `role_code`='10046');

-- ⑧ 按值班线授按钮权限
--    医务科(26) 原本只有「看 + 审」，补登记/删除/换班 → 医疗线；
--    护理部(29) 原本一条没有，补页面 + 登记/删除/换班 → 护理线；
--    院办(36) 拿页面 + 登记/删除/换班 + 审核发布（全条线）+ 值班类型维护。
INSERT INTO `sys_role_menu` (`id`,`role_id`,`menu_id`,`create_by`,`create_time`,
       `update_by`,`update_time`,`create_by_id`,`update_by_id`)
SELECT x.seq, x.role_id, x.menu_id, 'admin', NOW(), 'admin', NOW(), 1, 1
  FROM (SELECT 8920610360000000001 AS seq, 36 AS role_id, 100 AS menu_id
        UNION ALL SELECT 8920610360000000002, 36, 101
        UNION ALL SELECT 8920610360000000003, 36, 2945
        UNION ALL SELECT 8920610360000000004, 36, 806
        UNION ALL SELECT 8920610360000000005, 36, 2668
        UNION ALL SELECT 8920610360000000006, 36, 2669
        UNION ALL SELECT 8920610360000000007, 36, 2670
        UNION ALL SELECT 8920610360000000008, 36, 2982
        UNION ALL SELECT 8920610360000000009, 36, 2983
        UNION ALL SELECT 8920610360000000010, 36, 2984
        UNION ALL SELECT 8920610360000000011, 36, 2985
        UNION ALL SELECT 8920610360000000012, 26, 2668
        UNION ALL SELECT 8920610360000000013, 26, 2669
        UNION ALL SELECT 8920610360000000014, 26, 2670
        UNION ALL SELECT 8920610360000000015, 29, 806
        UNION ALL SELECT 8920610360000000016, 29, 2668
        UNION ALL SELECT 8920610360000000017, 29, 2669
        UNION ALL SELECT 8920610360000000018, 29, 2670) x
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id=x.role_id AND rm.menu_id=x.menu_id);

-- ⑨ 院办演示岗位：蔡璐（行政干事 E528001）× 院办值班管理员 × 院办（非主岗位，主岗位保留）
INSERT INTO `sys_employee_post` (`id`,`employee_id`,`role_id`,`dept_id`,`is_primary`,
       `effective_date`,`expire_date`,`create_by`,`create_time`,`update_by`,`update_time`,
       `create_by_id`,`update_by_id`)
SELECT 8999000000000092105, 897001425, 36, 19580076, 0, '2026-11-16', NULL,
       'admin', NOW(), 'admin', NOW(), 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_employee_post` WHERE `id`=8999000000000092105
        OR (`employee_id`=897001425 AND `role_id`=36));

-- ⑩ 审核链第二人：余璐（职能部门主任 E527001）× 同角色 × 院办
--    自审闸要求「登记人 ≠ 审核人」，只有一个人时这条闸永远走不到分支，所以补第二个院办账号。
INSERT INTO `sys_employee_post` (`id`,`employee_id`,`role_id`,`dept_id`,`is_primary`,
       `effective_date`,`expire_date`,`create_by`,`create_time`,`update_by`,`update_time`,
       `create_by_id`,`update_by_id`)
SELECT 8999000000000092106, 897001424, 36, 19580076, 0, '2026-11-16', NULL,
       'admin', NOW(), 'admin', NOW(), 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_employee_post` WHERE `id`=8999000000000092106
        OR (`employee_id`=897001424 AND `role_id`=36));

COMMIT;

-- 回查
SELECT '值班类型' AS what, id, type_code, type_name, manage_dept_name, required_positions, leader_layer
  FROM `biz_duty_type` ORDER BY sort_no;
SELECT '院级点位' AS what, COUNT(*) AS cnt FROM `biz_duty_post` WHERE `duty_type_id` IS NOT NULL;
SELECT '角色36菜单' AS what, COUNT(*) AS cnt FROM `sys_role_menu` WHERE `role_id`=36;
SELECT '院办岗位' AS what, p.id, e.emp_name, p.role_id, p.dept_id, p.is_primary
  FROM `sys_employee_post` p JOIN `sys_employee` e ON e.id=p.employee_id WHERE p.role_id=36;
