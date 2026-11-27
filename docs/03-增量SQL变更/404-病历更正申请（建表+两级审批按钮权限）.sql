-- ============================================================
-- 404 病历更正申请：建表 + 两级审批按钮权限
-- 日期：2026-11-27
-- 依据：《电子病历应用管理规范(试行)》第十六/十七条、《电子病历系统功能规范》第五十一条。
--   已提交/归档病历原则上不可直接修改；确需更正的，经治医生或上级发起申请，
--   科室初审、医务部门终审批准后授权回写，修改人与修改时间、前后值全程留痕。
-- 回滚：见 404-回滚.sql；执行后清 Redis：his:perm:role:*、his:user:identity:*、his:menu:*。
-- ============================================================

-- ---------- ① 更正申请留痕表 ----------
CREATE TABLE IF NOT EXISTS `biz_emr_correction_request` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `request_no` varchar(32) NOT NULL COMMENT '单号 CR+yyyyMMdd+4位',
  `record_id` bigint NOT NULL COMMENT '病历ID biz_medical_record.id',
  `record_no` varchar(64) COMMENT '病历号（快照）',
  `patient_id` bigint COMMENT '患者ID（快照）',
  `patient_name` varchar(64) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '病历所属科室ID（快照）',
  `dept_name` varchar(64) COMMENT '病历所属科室名称（快照）',
  `original_doctor_id` bigint COMMENT '原经治医生ID（快照）',
  `original_doctor_name` varchar(64) COMMENT '原经治医生姓名（快照）',
  `applicant_id` bigint NOT NULL COMMENT '申请人员工ID',
  `applicant_name` varchar(64) COMMENT '申请人姓名（快照）',
  `correction_type` tinyint NOT NULL COMMENT '更正类型（1-经治医生更正 2-上级代改）',
  `reason` varchar(500) NOT NULL COMMENT '更正原因/事由',
  `planned_content` varchar(2000) COMMENT '拟更正内容说明',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待科室审核 2-待医务审批 3-已批准待修改 4-已驳回 5-已完成 6-已撤回）',
  `dept_approver_id` bigint COMMENT '科室审核人ID',
  `dept_approver_name` varchar(64) COMMENT '科室审核人姓名（快照）',
  `dept_approve_time` datetime COMMENT '科室审核时间',
  `dept_comment` varchar(500) COMMENT '科室审核意见',
  `medical_approver_id` bigint COMMENT '医务审批人ID',
  `medical_approver_name` varchar(64) COMMENT '医务审批人姓名（快照）',
  `medical_approve_time` datetime COMMENT '医务审批时间',
  `medical_comment` varchar(500) COMMENT '医务审批意见',
  `modifier_id` bigint COMMENT '实际修改人员工ID',
  `modifier_name` varchar(64) COMMENT '实际修改人姓名（快照）',
  `modify_time` datetime COMMENT '病历回写完成时间',
  `remark` varchar(500) COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_no` (`request_no`),
  KEY `idx_record` (`record_id`),
  KEY `idx_status` (`status`),
  KEY `idx_dept_status` (`dept_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历更正申请单';

-- ---------- ② 两级审批按钮权限（挂 602 病历查阅与审核） ----------
DELETE FROM sys_role_menu WHERE menu_id IN (2190, 2191, 2192, 2193);
DELETE FROM sys_menu WHERE id IN (2190, 2191, 2192, 2193);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time,
                      update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (2190, '病历更正-台账查看', 602, 4, 3, NULL, NULL, 'emr.correction:list', NULL,
        'emr:correction:list', 0, 1, 0, 1, 'admin', NOW(),
        'admin', NOW(), 0, '查看病历更正申请台账与两级审批意见、回写留痕', 1, 1),
       (2191, '病历更正-发起申请', 602, 5, 3, NULL, NULL, 'emr.correction:apply', NULL,
        'emr:correction:apply', 0, 1, 0, 1, 'admin', NOW(),
        'admin', NOW(), 0, '经治医生/科主任对已提交或归档病历发起更正申请，可撤回在途申请', 1, 1),
       (2192, '病历更正-科室审核', 602, 6, 3, NULL, NULL, 'emr.correction:deptApprove', NULL,
        'emr:correction:deptApprove', 0, 1, 0, 1, 'admin', NOW(),
        'admin', NOW(), 0, '科主任科室初审：通过转医务审批，驳回终态', 1, 1),
       (2193, '病历更正-医务审批', 602, 7, 3, NULL, NULL, 'emr.correction:medicalApprove', NULL,
        'emr:correction:medicalApprove', 0, 1, 0, 1, 'admin', NOW(),
        'admin', NOW(), 0, '医务科/病案质控终审：通过置已批准待修改，驳回终态', 1, 1);

-- 授权：admin(1)、医生(2)、病案质控员(15)、医务科(26)、科主任(27)
-- 2190 台账：1, 2, 15, 26, 27
-- 2191 发起：1, 2, 27
-- 2192 科室审核：1, 27
-- 2193 医务审批：1, 15, 26
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (9210000000000002001, 1, 2190, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002002, 2, 2190, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002003, 15, 2190, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002004, 26, 2190, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002005, 27, 2190, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002006, 1, 2191, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002007, 2, 2191, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002008, 27, 2191, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002009, 1, 2192, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002010, 27, 2192, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002011, 1, 2193, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002012, 15, 2193, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (9210000000000002013, 26, 2193, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 回查 ----------
-- 1) 菜单在：SELECT id, menu_name, parent_id, permission FROM sys_menu WHERE id IN (2190,2191,2192,2193);
-- 2) 角色授权：SELECT menu_id, role_id FROM sys_role_menu WHERE menu_id IN (2190,2191,2192,2193) ORDER BY menu_id, role_id;
-- 3) 表结构：SHOW COLUMNS FROM biz_emr_correction_request;
