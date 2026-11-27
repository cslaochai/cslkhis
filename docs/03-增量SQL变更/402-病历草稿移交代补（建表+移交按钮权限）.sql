-- ============================================================
-- 402 病历草稿移交代补：建表 + 移交按钮权限
-- 日期：2026-11-27
-- 依据：《电子病历应用管理规范(试行)》第十六/十七条、《电子病历系统功能规范》第五十一条。
--   医生离职/长期休假/调离临床岗位后，名下未提交草稿由科室上级或质控岗承接代补；
--   病历经治医生归属按就诊事实快照固化，不因移交改变；移交/代补全程留痕。
-- 回滚：见 402-回滚.sql；执行后清 Redis：his:perm:role:*、his:user:identity:*、his:menu:*。
-- ============================================================

-- ---------- ① 移交留痕表 ----------
CREATE TABLE IF NOT EXISTS `biz_emr_draft_handover` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `handover_no` varchar(32) NOT NULL COMMENT '单号 HD+yyyyMMdd+4位',
  `record_id` bigint NOT NULL COMMENT '病历ID biz_medical_record.id',
  `record_no` varchar(64) COMMENT '病历号（快照）',
  `patient_id` bigint COMMENT '患者ID（快照）',
  `patient_name` varchar(64) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '病历所属科室ID（快照）',
  `dept_name` varchar(64) COMMENT '病历所属科室名称（快照）',
  `original_doctor_id` bigint COMMENT '原经治医生ID（快照）',
  `original_doctor_name` varchar(64) COMMENT '原经治医生姓名（快照）',
  `receiver_id` bigint NOT NULL COMMENT '承接人员工ID',
  `receiver_name` varchar(64) COMMENT '承接人姓名（快照）',
  `reason` tinyint NOT NULL COMMENT '移交原因（1-离职 2-长期休假 3-调离临床岗位 4-其他）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待代补 2-已完成 3-已撤销）',
  `operator_id` bigint COMMENT '移交发起人员工ID',
  `operator_name` varchar(64) COMMENT '移交发起人姓名（快照）',
  `complete_time` datetime COMMENT '代补完成时间',
  `complete_by_id` bigint COMMENT '代补完成人员工ID',
  `complete_by_name` varchar(64) COMMENT '代补完成人姓名（快照）',
  `remark` varchar(500) COMMENT '备注/移交说明',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_handover_no` (`handover_no`),
  KEY `idx_record` (`record_id`),
  KEY `idx_receiver_status` (`receiver_id`, `status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历草稿移交代补记录';

-- ---------- ② 移交按钮权限（挂 602 病历查阅与审核） ----------
DELETE FROM sys_role_menu WHERE menu_id = 2189;
DELETE FROM sys_menu WHERE id = 2189;

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time,
                      update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (2189, '病历草稿-移交代补', 602, 3, 3, NULL, NULL, 'emr.medicalRecord:handover', NULL,
        'emr:medicalRecord:handover', 0, 1, 0, 1, 'admin', NOW(),
        'admin', NOW(), 0,
        '离岗/调离医生名下未提交草稿移交上级医师或质控岗代补；病历经治医生不变，代补全程留痕', 1, 1);

-- 授权：admin(1)、病案质控员(15)、医务科(26)、科主任(27)
DELETE FROM sys_role_menu WHERE role_id IN (1, 15, 26, 27) AND menu_id = 2189;
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (8920610271000000001, 1, 2189, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (8920610271000000002, 15, 2189, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (8920610271000000003, 26, 2189, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (8920610271000000004, 27, 2189, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 回查 ----------
-- 1) 菜单在：SELECT id, menu_name, parent_id, permission FROM sys_menu WHERE id = 2189;
-- 2) 角色授权：SELECT role_id FROM sys_role_menu WHERE menu_id = 2189; 预期 1,15,26,27
-- 3) 表结构：SHOW COLUMNS FROM biz_emr_draft_handover;
