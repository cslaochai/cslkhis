-- 领域：22-互联网医院·随访·满意度·消息与工作台
-- 库：hn_biz_his    表数：21
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_online_consult  线上问诊
-- ----------------------------
CREATE TABLE `biz_online_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '问诊单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名（快照）',
  `dept_id` bigint DEFAULT NULL COMMENT '接诊科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '接诊科室名称（快照）',
  `doctor_id` bigint DEFAULT NULL COMMENT '接诊医生ID（员工ID）',
  `doctor_name` varchar(64) DEFAULT NULL COMMENT '接诊医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT '1' COMMENT '问诊方式（1-图文问诊 2-电话问诊 3-视频问诊）',
  `chief_complaint` varchar(1000) NOT NULL COMMENT '主诉/问题描述',
  `reply` varchar(1000) DEFAULT NULL COMMENT '医生回复',
  `advice` varchar(500) DEFAULT NULL COMMENT '处置建议',
  `need_visit` tinyint NOT NULL DEFAULT '0' COMMENT '是否建议线下就诊（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待接诊 2-接诊中 3-已完成 4-已退诊）',
  `fee` decimal(10,2) DEFAULT NULL COMMENT '问诊费用',
  `apply_time` datetime DEFAULT NULL COMMENT '发起时间',
  `accept_by` varchar(64) DEFAULT NULL COMMENT '接诊人',
  `accept_time` datetime DEFAULT NULL COMMENT '接诊时间',
  `finish_by` varchar(64) DEFAULT NULL COMMENT '完成人',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '退诊原因',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_online_no` (`consult_no`),
  KEY `idx_online_patient` (`patient_id`),
  KEY `idx_online_status` (`status`),
  KEY `idx_online_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='线上问诊';

-- ----------------------------
-- biz_tele_consult  远程会诊
-- ----------------------------
CREATE TABLE `biz_tele_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '会诊单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名（快照）',
  `admission_id` bigint DEFAULT NULL COMMENT '关联住院ID',
  `apply_dept_id` bigint DEFAULT NULL COMMENT '申请科室ID',
  `apply_dept_name` varchar(128) DEFAULT NULL COMMENT '申请科室名称（快照）',
  `apply_doctor_id` bigint DEFAULT NULL COMMENT '申请医生ID（员工ID）',
  `apply_doctor` varchar(64) DEFAULT NULL COMMENT '申请医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT '1' COMMENT '会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他）',
  `is_urgent` tinyint NOT NULL DEFAULT '0' COMMENT '是否急会诊（0-否 1-是）',
  `expert_hospital` varchar(128) DEFAULT NULL COMMENT '受邀专家所在医院',
  `expert_dept` varchar(128) DEFAULT NULL COMMENT '受邀专家科室',
  `expert_name` varchar(64) DEFAULT NULL COMMENT '受邀专家姓名',
  `expert_title` varchar(32) DEFAULT NULL COMMENT '受邀专家职称',
  `purpose` varchar(500) DEFAULT NULL COMMENT '会诊目的',
  `diagnosis` varchar(500) DEFAULT NULL COMMENT '申请方诊断/病情摘要',
  `plan_time` datetime DEFAULT NULL COMMENT '计划会诊时间',
  `duration_min` int DEFAULT NULL COMMENT '计划时长（分钟）',
  `platform` varchar(64) DEFAULT NULL COMMENT '对接平台',
  `meet_no` varchar(64) DEFAULT NULL COMMENT '接入号/会议室号（预留）',
  `opinion` varchar(1000) DEFAULT NULL COMMENT '会诊意见',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待安排 2-已安排 3-已完成 4-已取消）',
  `fee` decimal(10,2) DEFAULT NULL COMMENT '会诊费用',
  `arrange_by` varchar(64) DEFAULT NULL COMMENT '安排人',
  `arrange_time` datetime DEFAULT NULL COMMENT '安排时间',
  `complete_by` varchar(64) DEFAULT NULL COMMENT '完成人',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消原因',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tele_no` (`consult_no`),
  KEY `idx_tele_patient` (`patient_id`),
  KEY `idx_tele_status` (`status`),
  KEY `idx_tele_dept` (`apply_dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='远程会诊';

-- ----------------------------
-- biz_referral  转诊
-- ----------------------------
CREATE TABLE `biz_referral` (
  `referral_id` bigint NOT NULL COMMENT '转诊ID',
  `referral_no` varchar(32) NOT NULL COMMENT '转诊编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint DEFAULT NULL COMMENT '就诊次ID',
  `from_dept_id` bigint NOT NULL COMMENT '转出科室ID',
  `to_dept_id` bigint DEFAULT NULL COMMENT '转入科室ID',
  `to_hospital` varchar(128) DEFAULT NULL COMMENT '转入医院',
  `reason` varchar(1000) DEFAULT NULL COMMENT '转诊原因',
  `referral_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转诊时间',
  `referral_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-待确认 1-已确认 2-已完成 3-已取消）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `direction` tinyint DEFAULT '1' COMMENT '转诊方向（1-上转 2-下转）',
  `admission_id` bigint DEFAULT NULL COMMENT '入院ID',
  `diagnosis` varchar(500) DEFAULT NULL COMMENT '诊断摘要',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `audit_by` bigint DEFAULT NULL COMMENT '确认人（员工ID）',
  `audit_name` varchar(50) DEFAULT NULL COMMENT '确认人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '确认时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '确认意见',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `finish_remark` varchar(500) DEFAULT NULL COMMENT '完成备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`referral_id`),
  UNIQUE KEY `uk_referral_no` (`referral_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_referral_time` (`referral_time`),
  KEY `idx_ref_direction` (`referral_status`),
  KEY `idx_ref_admission` (`admission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转诊';

-- ----------------------------
-- biz_followup_task  随访任务
-- ----------------------------
CREATE TABLE `biz_followup_task` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `diagnosis` varchar(200) DEFAULT NULL COMMENT '诊断',
  `dept_id` bigint DEFAULT NULL COMMENT '随访所属科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称（快照）',
  `followup_type` tinyint NOT NULL COMMENT '随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访）',
  `followup_content` varchar(500) DEFAULT NULL COMMENT '随访内容',
  `followup_time` datetime NOT NULL COMMENT '计划随访时间',
  `followup_status` tinyint NOT NULL DEFAULT '1' COMMENT '随访状态（1-待随访 2-随访中 3-已完成 4-已取消）',
  `executor_id` bigint DEFAULT NULL COMMENT '执行人ID',
  `executor_name` varchar(50) DEFAULT NULL COMMENT '执行人姓名',
  `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
  `execute_result` varchar(500) DEFAULT NULL COMMENT '执行结果',
  `revisit_record_id` bigint DEFAULT NULL COMMENT '复诊引用的原病历ID',
  `revisit_appoint_id` bigint DEFAULT NULL COMMENT '由本任务生成的复诊挂号ID',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `patient_reply` text COMMENT '患者反馈内容（小程序回写）',
  `patient_reply_time` datetime DEFAULT NULL COMMENT '患者反馈时间',
  `call_channel` tinyint DEFAULT NULL COMMENT '外呼通道（1-人工 2-自动）',
  `call_status` tinyint NOT NULL DEFAULT '0' COMMENT '外呼状态（0-未外呼 1-待外呼 2-已接通 3-未接通）',
  `call_time` datetime DEFAULT NULL COMMENT '最近一次外呼登记时间',
  `call_attempts` int NOT NULL DEFAULT '0' COMMENT '累计外呼登记次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_followup_type` (`followup_type`),
  KEY `idx_followup_status` (`followup_status`),
  KEY `idx_followup_time` (`followup_time`),
  KEY `idx_followup_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='随访任务';

-- ----------------------------
-- biz_survey_template  满意度问卷模板
-- ----------------------------
CREATE TABLE `biz_survey_template` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_no` varchar(32) NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) NOT NULL COMMENT '问卷名称',
  `scene` tinyint NOT NULL COMMENT '适用场景（1-出院随访 2-门诊 3-住院在院 4-体检）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 2-停用）',
  `description` varchar(500) DEFAULT NULL COMMENT '说明（调查目的、口径、上报去向）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_template_no` (`template_no`),
  KEY `idx_survey_tpl_scene` (`scene`),
  KEY `idx_survey_tpl_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度问卷模板';

-- ----------------------------
-- biz_survey_item  满意度问卷题目
-- ----------------------------
CREATE TABLE `biz_survey_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `seq_no` int NOT NULL COMMENT '题号',
  `dimension` tinyint NOT NULL COMMENT '评价维度',
  `question_type` tinyint NOT NULL COMMENT '题型（1-量表 2-单选 3-多选 4-NPS推荐度 5-开放文本）',
  `title` varchar(255) NOT NULL COMMENT '题干',
  `required` tinyint NOT NULL DEFAULT '1' COMMENT '是否必答（0-否 1-是）',
  `weight` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '权重',
  `max_score` tinyint NOT NULL DEFAULT '5' COMMENT '满分',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_item` (`template_id`,`seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度问卷题目';

-- ----------------------------
-- biz_survey_dispatch  满意度发放台账
-- ----------------------------
CREATE TABLE `biz_survey_dispatch` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dispatch_no` varchar(32) NOT NULL COMMENT '发放单号',
  `template_id` bigint NOT NULL COMMENT '问卷模板ID',
  `template_name` varchar(128) DEFAULT NULL COMMENT '模板名称',
  `scene` tinyint NOT NULL COMMENT '适用场景（快照）',
  `source_type` tinyint NOT NULL COMMENT '发放来源（1-随访任务 2-出院结算 3-人工补发）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名（快照）',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系手机号',
  `dept_id` bigint DEFAULT NULL COMMENT '就诊科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称（快照）',
  `channel` tinyint NOT NULL DEFAULT '1' COMMENT '回收渠道（1-电话代填 2-短信 3-微信 4-现场扫码）',
  `dispatch_status` tinyint NOT NULL DEFAULT '1' COMMENT '回收状态（1-待推送 2-已推送待回收 3-已回收 4-已过期 5-已拒答）',
  `push_time` datetime DEFAULT NULL COMMENT '推送/发起时间',
  `expire_time` datetime DEFAULT NULL COMMENT '回收截止时间',
  `answer_id` bigint DEFAULT NULL COMMENT '回收到的答卷ID',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_dispatch_no` (`dispatch_no`),
  UNIQUE KEY `uk_survey_dispatch_source` (`source_type`,`source_id`,`template_id`),
  KEY `idx_survey_disp_status` (`dispatch_status`),
  KEY `idx_survey_disp_dept` (`dept_id`),
  KEY `idx_survey_disp_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度发放台账';

-- ----------------------------
-- biz_survey_answer  满意度答卷
-- ----------------------------
CREATE TABLE `biz_survey_answer` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `answer_no` varchar(32) NOT NULL COMMENT '答卷编号',
  `dispatch_id` bigint NOT NULL COMMENT '发放单ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `scene` tinyint NOT NULL COMMENT '场景（快照）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '就诊科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称（快照）',
  `avg_score` decimal(5,2) NOT NULL COMMENT '李克特均分',
  `score_100` decimal(6,2) NOT NULL COMMENT '百分制得分',
  `nps` tinyint DEFAULT NULL COMMENT 'NPS 推荐度',
  `comment_text` varchar(1000) DEFAULT NULL COMMENT '开放意见',
  `fill_source` tinyint NOT NULL DEFAULT '2' COMMENT '填报方式（1-患者自填 2-随访员代填 3-现场扫码）',
  `anonymous_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否匿名（0-否 1-是）',
  `fill_employee_id` bigint DEFAULT NULL COMMENT '代填人（员工ID）',
  `fill_employee_name` varchar(64) DEFAULT NULL COMMENT '代填人姓名',
  `fill_time` datetime DEFAULT NULL COMMENT '提交时间',
  `answer_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-有效 2-已作废）',
  `dispute_case_id` bigint DEFAULT NULL COMMENT '低分自动转出的投诉单ID',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_answer_no` (`answer_no`),
  UNIQUE KEY `uk_survey_answer_dispatch` (`dispatch_id`),
  KEY `idx_survey_answer_dept` (`dept_id`),
  KEY `idx_survey_answer_time` (`fill_time`),
  KEY `idx_survey_answer_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度答卷';

-- ----------------------------
-- biz_survey_answer_item  满意度逐题答案
-- ----------------------------
CREATE TABLE `biz_survey_answer_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `answer_id` bigint NOT NULL COMMENT '答卷ID',
  `item_id` bigint NOT NULL COMMENT '题目ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `dimension` tinyint NOT NULL COMMENT '评价维度',
  `seq_no` int NOT NULL COMMENT '题号（快照）',
  `title` varchar(255) NOT NULL COMMENT '题干（快照）',
  `question_type` tinyint NOT NULL COMMENT '题型（快照）',
  `score` tinyint DEFAULT NULL COMMENT '得分',
  `option_label` varchar(128) DEFAULT NULL COMMENT '选项文本',
  `text_value` varchar(1000) DEFAULT NULL COMMENT '文本题回答',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_answer_item` (`answer_id`,`item_id`),
  KEY `idx_survey_ai_answer` (`answer_id`),
  KEY `idx_survey_ai_dimension` (`dimension`),
  KEY `idx_survey_ai_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度逐题答案';

-- ----------------------------
-- sys_message  消息通知
-- ----------------------------
CREATE TABLE `sys_message` (
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `message_no` varchar(32) NOT NULL COMMENT '消息编号',
  `channel` varchar(32) NOT NULL COMMENT '发送渠道',
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `receiver_name` varchar(64) DEFAULT NULL COMMENT '接收人姓名',
  `title` varchar(128) DEFAULT NULL COMMENT '消息标题',
  `content` varchar(2000) NOT NULL COMMENT '消息内容',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `severity` varchar(16) NOT NULL DEFAULT 'info' COMMENT '紧急度',
  `payload` json DEFAULT NULL COMMENT '结构化负载(JSON)',
  `handle_status` tinyint DEFAULT NULL COMMENT '处理状态（0-待处理 1-已处理 2-已关闭）',
  `send_status` tinyint NOT NULL DEFAULT '0' COMMENT '发送状态（0-待发送 1-已发送 2-发送失败）',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '渠道发送失败原因',
  `read_status` tinyint NOT NULL DEFAULT '0' COMMENT '阅读状态（0-未读 1-已读）',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`message_id`),
  UNIQUE KEY `uk_message_no` (`message_no`),
  KEY `idx_receiver` (`receiver_id`,`read_status`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知';

-- ----------------------------
-- sys_alert_rule  预警规则
-- ----------------------------
CREATE TABLE `sys_alert_rule` (
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `rule_name` varchar(64) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(32) NOT NULL COMMENT '规则类型',
  `rule_condition` varchar(256) DEFAULT NULL COMMENT '规则条件表达式',
  `threshold` int DEFAULT NULL COMMENT '阈值',
  `notify_channel` varchar(32) DEFAULT 'system' COMMENT '通知渠道',
  `is_active` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用（0-停用 1-启用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警规则';

-- ----------------------------
-- biz_alert  预警记录
-- ----------------------------
CREATE TABLE `biz_alert` (
  `alert_id` bigint NOT NULL COMMENT '预警ID',
  `alert_no` varchar(32) NOT NULL COMMENT '预警编号',
  `rule_id` bigint DEFAULT NULL COMMENT '规则ID',
  `alert_type` varchar(32) NOT NULL COMMENT '预警类型',
  `alert_content` varchar(1000) NOT NULL COMMENT '预警内容',
  `alert_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-未处理 1-已处理 2-已忽略）',
  `notify_user_id` bigint DEFAULT NULL COMMENT '通知用户ID',
  `notify_time` datetime DEFAULT NULL COMMENT '通知时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`alert_id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_alert_status` (`alert_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警记录';

-- ----------------------------
-- sys_workbench_widget  工作台卡片注册表
-- ----------------------------
CREATE TABLE `sys_workbench_widget` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `widget_name` varchar(64) NOT NULL COMMENT '卡片标题',
  `area` varchar(16) NOT NULL DEFAULT 'domain' COMMENT '归属区域',
  `api_key` varchar(128) NOT NULL COMMENT '取数来源标识',
  `permission` varchar(128) DEFAULT NULL COMMENT '可见所需权限码',
  `default_span` int NOT NULL DEFAULT '6' COMMENT '栅格占宽',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '展示顺序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-已上线可挂载 0-注册表先占位）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_widget_code` (`widget_code`),
  KEY `idx_workbench_widget_area` (`area`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作台卡片注册表';

-- ----------------------------
-- sys_workbench_role  角色工作台配置
-- ----------------------------
CREATE TABLE `sys_workbench_role` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `widget_id` bigint NOT NULL COMMENT '卡片ID',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '该角色下的卡片顺序',
  `visible` tinyint NOT NULL DEFAULT '1' COMMENT '是否展示',
  `landing_scope` tinyint NOT NULL DEFAULT '0' COMMENT '登录/切角色落点（0-默认 1-一律工作台 2-一律患者工作站）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_role_widget` (`role_id`,`widget_id`),
  KEY `idx_workbench_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色工作台配置';

-- ----------------------------
-- sys_workbench_layout  工作台个人布局
-- ----------------------------
CREATE TABLE `sys_workbench_layout` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '个人顺序',
  `visible` tinyint NOT NULL DEFAULT '1' COMMENT '个人显隐',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_layout_user_widget` (`user_id`,`widget_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作台个人布局';

-- ----------------------------
-- biz_service_message  患者端留言
-- ----------------------------
CREATE TABLE `biz_service_message` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_no` varchar(32) NOT NULL COMMENT '留言单号',
  `user_id` bigint DEFAULT NULL COMMENT '留言用户ID',
  `patient_id` bigint DEFAULT NULL COMMENT '就诊人ID',
  `patient_name` varchar(64) DEFAULT NULL COMMENT '就诊人姓名（快照）',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `category_code` varchar(32) DEFAULT NULL COMMENT '留言分类（同 sys_faq.category_code）',
  `content` varchar(1000) NOT NULL COMMENT '留言内容',
  `status` tinyint DEFAULT '0' COMMENT '工单状态（0-待受理 1-处理中 2-已办结 3-已关闭）',
  `priority` tinyint DEFAULT '0' COMMENT '优先级（0-普通 1-紧急）',
  `accept_by` varchar(64) DEFAULT NULL COMMENT '受理人账号（服务端取登录人，不由前端传）',
  `accept_by_name` varchar(64) DEFAULT NULL COMMENT '受理人姓名',
  `accept_time` datetime DEFAULT NULL COMMENT '受理时间',
  `close_by` varchar(64) DEFAULT NULL COMMENT '关闭人账号',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `close_reason` varchar(200) DEFAULT NULL COMMENT '关闭原因（患者撤单/客服关闭都要写）',
  `last_reply_time` datetime DEFAULT NULL COMMENT '最后一次客服回复时间',
  `reply_count` int DEFAULT '0' COMMENT '客服回复次数',
  `handle_by` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_no` (`message_no`),
  KEY `idx_patient` (`patient_id`),
  KEY `idx_status_time` (`status`,`create_time`),
  KEY `idx_accept_by` (`accept_by`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者端留言';

-- ----------------------------
-- biz_service_ticket_log  工单流转记录（患者端进展时间轴 + 客服端证据链）
-- ----------------------------
CREATE TABLE `biz_service_ticket_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_id` bigint NOT NULL COMMENT '工单ID（biz_service_message.id）',
  `message_no` varchar(32) DEFAULT NULL COMMENT '工单号（冗余，排查时不用 join）',
  `action` tinyint NOT NULL COMMENT '动作（0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开）',
  `content` varchar(1000) DEFAULT NULL COMMENT '内容（回复正文 / 处理结果 / 撤单原因）',
  `visible_to_patient` tinyint DEFAULT '1' COMMENT '患者是否可见（0-内部备注 1-患者可见）',
  `operator_type` tinyint DEFAULT '1' COMMENT '操作人类型（1-患者 2-院内）',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人账号',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_message` (`message_id`,`create_time`),
  KEY `idx_no` (`message_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单流转记录（患者端进展时间轴 + 客服端证据链）';

-- ----------------------------
-- sys_faq  患者端常见问题
-- ----------------------------
CREATE TABLE `sys_faq` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `faq_no` varchar(32) NOT NULL COMMENT '常见问题编号',
  `category_code` varchar(32) NOT NULL COMMENT '分类编码',
  `category_name` varchar(64) NOT NULL COMMENT '分类名称',
  `question` varchar(200) NOT NULL COMMENT '问题',
  `answer` varchar(1000) NOT NULL COMMENT '答案（人工维护，涉时间/价格/比例一律引导式）',
  `keywords` varchar(500) DEFAULT NULL COMMENT '检索关键词（顿号分隔，含口语同义词）',
  `hot_flag` tinyint DEFAULT '0' COMMENT '热门（0-否 1-是）',
  `view_count` int DEFAULT '0' COMMENT '查看次数',
  `helpful_count` int DEFAULT '0' COMMENT '有帮助次数',
  `useless_count` int DEFAULT '0' COMMENT '没帮助次数',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_faq_no` (`faq_no`),
  KEY `idx_category` (`category_code`),
  KEY `idx_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者端常见问题';

-- ----------------------------
-- sys_knowledge_chunk  知识库切块（向量在内存，文本在此）
-- ----------------------------
CREATE TABLE `sys_knowledge_chunk` (
  `id` bigint NOT NULL COMMENT '切块ID（雪花）',
  `doc_id` bigint NOT NULL COMMENT '所属文档ID',
  `doc_title` varchar(200) DEFAULT '' COMMENT '文档标题（冗余）',
  `category` varchar(50) DEFAULT '' COMMENT '分类（冗余）',
  `chunk_index` int DEFAULT '0' COMMENT '块序号',
  `content` longtext COMMENT '切块文本',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_doc_id` (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库切块（向量在内存，文本在此）';

-- ----------------------------
-- sys_knowledge_doc  知识库文档
-- ----------------------------
CREATE TABLE `sys_knowledge_doc` (
  `id` bigint NOT NULL COMMENT '文档ID（雪花）',
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `category` varchar(50) DEFAULT '' COMMENT '分类',
  `source_type` tinyint DEFAULT '1' COMMENT '来源类型（1-内置示例 2-手工录入 3-文件导入）',
  `content` longtext COMMENT '原始全文',
  `chunk_count` int DEFAULT '0' COMMENT '切块数量',
  `status` tinyint DEFAULT '0' COMMENT '状态（0-正常 1-停用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文档';

-- ----------------------------
-- sys_service_trace  客服页自助行为埋点
-- ----------------------------
CREATE TABLE `sys_service_trace` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `patient_id` bigint DEFAULT NULL COMMENT '就诊人ID',
  `session_id` varchar(64) DEFAULT NULL COMMENT '会话标识（同一次进入客服页）',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型（visit/card/search/view/helpful/useless/transfer/message）',
  `event_key` varchar(200) DEFAULT NULL COMMENT '事件对象（卡片名、搜索词、常见问题ID）',
  `faq_id` bigint DEFAULT NULL COMMENT '关联常见问题ID',
  `ref_id` bigint DEFAULT NULL COMMENT '关联业务ID（留言ID）',
  `hit_count` int DEFAULT NULL COMMENT '搜索命中条数（event_type=search 时）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`),
  KEY `idx_event_time` (`event_type`,`create_time`),
  KEY `idx_faq` (`faq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服页自助行为埋点';
