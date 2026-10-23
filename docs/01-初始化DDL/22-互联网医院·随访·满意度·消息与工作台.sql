-- 领域：22-互联网医院·随访·满意度·消息与工作台
-- 库：hn_biz_his    表数：28
-- 说明：DDL 快照（dev 库 SHOW CREATE TABLE 于 2026-10-23 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。
-- 口径：姓名/名称快照列与操作人列（create_by·update_by·*_by·*_name·name）不留 DEFAULT，漏填直接报错；其余 NOT NULL 列保留库上的 DEFAULT。

-- ----------------------------
-- biz_alert  预警记录
-- ----------------------------
CREATE TABLE `biz_alert` (
  `alert_id` bigint NOT NULL COMMENT '预警ID',
  `alert_no` varchar(32) NOT NULL COMMENT '预警编号',
  `rule_id` bigint COMMENT '规则ID',
  `alert_type` varchar(32) NOT NULL DEFAULT '' COMMENT '预警类型',
  `alert_content` varchar(1000) NOT NULL DEFAULT '' COMMENT '预警内容',
  `alert_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-未处理 1-已处理 2-已忽略）',
  `notify_user_id` bigint COMMENT '通知用户ID',
  `notify_time` datetime COMMENT '通知时间',
  `read_time` datetime COMMENT '阅读时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`alert_id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_alert_status` (`alert_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警记录';

-- ----------------------------
-- biz_followup_task  随访任务
-- ----------------------------
CREATE TABLE `biz_followup_task` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `phone` varchar(20) COMMENT '联系电话',
  `diagnosis` varchar(200) COMMENT '诊断',
  `dept_id` bigint COMMENT '随访所属科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `followup_type` tinyint NOT NULL DEFAULT '0' COMMENT '随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访）',
  `followup_content` varchar(500) COMMENT '随访内容',
  `followup_time` datetime NOT NULL DEFAULT (now()) COMMENT '计划随访时间',
  `followup_status` tinyint NOT NULL DEFAULT '1' COMMENT '随访状态（1-待随访 2-随访中 3-已完成 4-已取消）',
  `executor_id` bigint COMMENT '执行人ID',
  `executor_name` varchar(50) COMMENT '执行人姓名',
  `execute_time` datetime COMMENT '执行时间',
  `execute_result` varchar(500) COMMENT '执行结果',
  `revisit_record_id` bigint COMMENT '复诊引用的原病历ID',
  `revisit_appoint_id` bigint COMMENT '由本任务生成的复诊挂号ID',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `patient_reply` text COMMENT '患者反馈内容（小程序回写）',
  `patient_reply_time` datetime COMMENT '患者反馈时间',
  `call_channel` tinyint COMMENT '外呼通道（1-人工 2-自动）',
  `call_status` tinyint NOT NULL DEFAULT '0' COMMENT '外呼状态（0-未外呼 1-待外呼 2-已接通 3-未接通）',
  `call_time` datetime COMMENT '最近一次外呼登记时间',
  `call_attempts` int NOT NULL DEFAULT '0' COMMENT '累计外呼登记次数',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_followup_type` (`followup_type`),
  KEY `idx_followup_status` (`followup_status`),
  KEY `idx_followup_time` (`followup_time`),
  KEY `idx_followup_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='随访任务';

-- ----------------------------
-- biz_online_consult  线上问诊
-- ----------------------------
CREATE TABLE `biz_online_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '问诊单号',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '接诊科室ID',
  `dept_name` varchar(128) COMMENT '接诊科室名称（快照）',
  `doctor_id` bigint COMMENT '接诊医生ID（员工ID）',
  `doctor_name` varchar(64) COMMENT '接诊医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT '1' COMMENT '问诊方式（1-图文问诊 2-电话问诊 3-视频问诊）',
  `chief_complaint` varchar(1000) NOT NULL DEFAULT '' COMMENT '主诉/问题描述',
  `reply` varchar(1000) COMMENT '医生回复',
  `advice` varchar(500) COMMENT '处置建议',
  `need_visit` tinyint NOT NULL DEFAULT '0' COMMENT '是否建议线下就诊（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待接诊 2-接诊中 3-已完成 4-已退诊）',
  `fee` decimal(10,2) COMMENT '问诊费用',
  `apply_time` datetime COMMENT '发起时间',
  `accept_by` varchar(64) COMMENT '接诊人',
  `accept_time` datetime COMMENT '接诊时间',
  `finish_by` varchar(64) COMMENT '完成人',
  `finish_time` datetime COMMENT '完成时间',
  `reject_reason` varchar(500) COMMENT '退诊原因',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_online_no` (`consult_no`),
  KEY `idx_online_patient` (`patient_id`),
  KEY `idx_online_status` (`status`),
  KEY `idx_online_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='线上问诊';

-- ----------------------------
-- biz_referral  转诊
-- ----------------------------
CREATE TABLE `biz_referral` (
  `referral_id` bigint NOT NULL COMMENT '转诊ID',
  `referral_no` varchar(32) NOT NULL COMMENT '转诊编号',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `from_dept_id` bigint NOT NULL DEFAULT '0' COMMENT '转出科室ID',
  `to_dept_id` bigint COMMENT '转入科室ID',
  `to_hospital` varchar(128) COMMENT '转入医院',
  `reason` varchar(1000) COMMENT '转诊原因',
  `referral_time` datetime NOT NULL DEFAULT (now()) COMMENT '转诊时间',
  `referral_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-待确认 1-已确认 2-已完成 3-已取消）',
  `remark` varchar(500) COMMENT '备注',
  `direction` tinyint COMMENT '转诊方向（1-上转 2-下转）',
  `admission_id` bigint COMMENT '入院ID',
  `diagnosis` varchar(500) COMMENT '诊断摘要',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `audit_by` bigint COMMENT '确认人（员工ID）',
  `audit_name` varchar(50) COMMENT '确认人姓名',
  `audit_time` datetime COMMENT '确认时间',
  `audit_remark` varchar(500) COMMENT '确认意见',
  `finish_time` datetime COMMENT '完成时间',
  `finish_remark` varchar(500) COMMENT '完成备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`referral_id`),
  UNIQUE KEY `uk_referral_no` (`referral_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_referral_time` (`referral_time`),
  KEY `idx_ref_direction` (`referral_status`),
  KEY `idx_ref_admission` (`admission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转诊';

-- ----------------------------
-- biz_service_message  患者端留言
-- ----------------------------
CREATE TABLE `biz_service_message` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_no` varchar(32) NOT NULL COMMENT '留言单号',
  `user_id` bigint COMMENT '留言用户ID',
  `patient_id` bigint COMMENT '就诊人ID',
  `patient_name` varchar(64) COMMENT '就诊人姓名（快照）',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `category_code` varchar(32) COMMENT '留言分类（同 sys_faq.category_code）',
  `content` varchar(1000) NOT NULL DEFAULT '' COMMENT '留言内容',
  `status` tinyint COMMENT '工单状态（0-待受理 1-处理中 2-已办结 3-已关闭）',
  `priority` tinyint COMMENT '优先级（0-普通 1-紧急）',
  `accept_by` varchar(64) COMMENT '受理人账号（服务端取登录人，不由前端传）',
  `accept_by_name` varchar(64) COMMENT '受理人姓名',
  `accept_time` datetime COMMENT '受理时间',
  `close_by` varchar(64) COMMENT '关闭人账号',
  `close_time` datetime COMMENT '关闭时间',
  `close_reason` varchar(200) COMMENT '关闭原因（患者撤单/客服关闭都要写）',
  `last_reply_time` datetime COMMENT '最后一次客服回复时间',
  `reply_count` int COMMENT '客服回复次数',
  `handle_by` varchar(64) COMMENT '处理人',
  `handle_time` datetime COMMENT '处理时间',
  `handle_result` varchar(500) COMMENT '处理结果',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
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
  `message_id` bigint NOT NULL DEFAULT '0' COMMENT '工单ID（biz_service_message.id）',
  `message_no` varchar(32) COMMENT '工单号（冗余，排查时不用 join）',
  `action` tinyint NOT NULL DEFAULT '0' COMMENT '动作（0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开）',
  `content` varchar(1000) COMMENT '内容（回复正文 / 处理结果 / 撤单原因）',
  `visible_to_patient` tinyint COMMENT '患者是否可见（0-内部备注 1-患者可见）',
  `operator_type` tinyint COMMENT '操作人类型（1-患者 2-院内）',
  `operator` varchar(64) COMMENT '操作人账号',
  `operator_name` varchar(64) COMMENT '操作人姓名',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_message` (`message_id`,`create_time`),
  KEY `idx_no` (`message_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单流转记录（患者端进展时间轴 + 客服端证据链）';

-- ----------------------------
-- biz_survey_answer  满意度答卷
-- ----------------------------
CREATE TABLE `biz_survey_answer` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `answer_no` varchar(32) NOT NULL COMMENT '答卷编号',
  `dispatch_id` bigint NOT NULL COMMENT '发放单ID',
  `template_id` bigint NOT NULL DEFAULT '0' COMMENT '模板ID',
  `scene` tinyint NOT NULL DEFAULT '0' COMMENT '场景（快照）',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '就诊科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `avg_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '李克特均分',
  `score_100` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '百分制得分',
  `nps` tinyint COMMENT 'NPS 推荐度',
  `comment_text` varchar(1000) COMMENT '开放意见',
  `fill_source` tinyint NOT NULL DEFAULT '2' COMMENT '填报方式（1-患者自填 2-随访员代填 3-现场扫码）',
  `anonymous_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否匿名（0-否 1-是）',
  `fill_employee_id` bigint COMMENT '代填人（员工ID）',
  `fill_employee_name` varchar(64) COMMENT '代填人姓名',
  `fill_time` datetime COMMENT '提交时间',
  `answer_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-有效 2-已作废）',
  `dispute_case_id` bigint COMMENT '低分自动转出的投诉单ID',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
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
  `template_id` bigint NOT NULL DEFAULT '0' COMMENT '模板ID',
  `dimension` tinyint NOT NULL DEFAULT '0' COMMENT '评价维度',
  `seq_no` int NOT NULL DEFAULT '0' COMMENT '题号（快照）',
  `title` varchar(255) NOT NULL DEFAULT '' COMMENT '题干（快照）',
  `question_type` tinyint NOT NULL DEFAULT '0' COMMENT '题型（快照）',
  `score` tinyint COMMENT '得分',
  `option_label` varchar(128) COMMENT '选项文本',
  `text_value` varchar(1000) COMMENT '文本题回答',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_answer_item` (`answer_id`,`item_id`),
  KEY `idx_survey_ai_answer` (`answer_id`),
  KEY `idx_survey_ai_dimension` (`dimension`),
  KEY `idx_survey_ai_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度逐题答案';

-- ----------------------------
-- biz_survey_dispatch  满意度发放台账
-- ----------------------------
CREATE TABLE `biz_survey_dispatch` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dispatch_no` varchar(32) NOT NULL COMMENT '发放单号',
  `template_id` bigint NOT NULL COMMENT '问卷模板ID',
  `template_name` varchar(128) COMMENT '模板名称',
  `scene` tinyint NOT NULL DEFAULT '0' COMMENT '适用场景（快照）',
  `source_type` tinyint NOT NULL COMMENT '发放来源（1-随访任务 2-出院结算 3-人工补发）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `phone` varchar(20) COMMENT '联系手机号',
  `dept_id` bigint COMMENT '就诊科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `channel` tinyint NOT NULL DEFAULT '1' COMMENT '回收渠道（1-电话代填 2-短信 3-微信 4-现场扫码）',
  `dispatch_status` tinyint NOT NULL DEFAULT '1' COMMENT '回收状态（1-待推送 2-已推送待回收 3-已回收 4-已过期 5-已拒答）',
  `push_time` datetime COMMENT '推送/发起时间',
  `expire_time` datetime COMMENT '回收截止时间',
  `answer_id` bigint COMMENT '回收到的答卷ID',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_dispatch_no` (`dispatch_no`),
  UNIQUE KEY `uk_survey_dispatch_source` (`source_type`,`source_id`,`template_id`),
  KEY `idx_survey_disp_status` (`dispatch_status`),
  KEY `idx_survey_disp_dept` (`dept_id`),
  KEY `idx_survey_disp_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度发放台账';

-- ----------------------------
-- biz_survey_item  满意度问卷题目
-- ----------------------------
CREATE TABLE `biz_survey_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `seq_no` int NOT NULL COMMENT '题号',
  `dimension` tinyint NOT NULL DEFAULT '0' COMMENT '评价维度',
  `question_type` tinyint NOT NULL DEFAULT '0' COMMENT '题型（1-量表 2-单选 3-多选 4-NPS推荐度 5-开放文本）',
  `title` varchar(255) NOT NULL DEFAULT '' COMMENT '题干',
  `required` tinyint NOT NULL DEFAULT '1' COMMENT '是否必答（0-否 1-是）',
  `weight` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '权重',
  `max_score` tinyint NOT NULL DEFAULT '5' COMMENT '满分',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_item` (`template_id`,`seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度问卷题目';

-- ----------------------------
-- biz_survey_template  满意度问卷模板
-- ----------------------------
CREATE TABLE `biz_survey_template` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_no` varchar(32) NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) NOT NULL COMMENT '问卷名称',
  `scene` tinyint NOT NULL DEFAULT '0' COMMENT '适用场景（1-出院随访 2-门诊 3-住院在院 4-体检）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `description` varchar(500) COMMENT '说明（调查目的、口径、上报去向）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_template_no` (`template_no`),
  KEY `idx_survey_tpl_scene` (`scene`),
  KEY `idx_survey_tpl_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满意度问卷模板';

-- ----------------------------
-- biz_tele_consult  远程会诊
-- ----------------------------
CREATE TABLE `biz_tele_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '会诊单号',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `admission_id` bigint COMMENT '关联住院ID',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(128) COMMENT '申请科室名称（快照）',
  `apply_doctor_id` bigint COMMENT '申请医生ID（员工ID）',
  `apply_doctor` varchar(64) COMMENT '申请医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT '1' COMMENT '会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他）',
  `is_urgent` tinyint NOT NULL DEFAULT '0' COMMENT '是否急会诊（0-否 1-是）',
  `expert_hospital` varchar(128) COMMENT '受邀专家所在医院',
  `expert_dept` varchar(128) COMMENT '受邀专家科室',
  `expert_name` varchar(64) COMMENT '受邀专家姓名',
  `expert_title` varchar(32) COMMENT '受邀专家职称',
  `purpose` varchar(500) COMMENT '会诊目的',
  `diagnosis` varchar(500) COMMENT '申请方诊断/病情摘要',
  `plan_time` datetime COMMENT '计划会诊时间',
  `duration_min` int COMMENT '计划时长（分钟）',
  `platform` varchar(64) COMMENT '对接平台',
  `meet_no` varchar(64) COMMENT '接入号/会议室号（预留）',
  `opinion` varchar(1000) COMMENT '会诊意见',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待安排 2-已安排 3-已完成 4-已取消）',
  `fee` decimal(10,2) COMMENT '会诊费用',
  `arrange_by` varchar(64) COMMENT '安排人',
  `arrange_time` datetime COMMENT '安排时间',
  `complete_by` varchar(64) COMMENT '完成人',
  `complete_time` datetime COMMENT '完成时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tele_no` (`consult_no`),
  KEY `idx_tele_patient` (`patient_id`),
  KEY `idx_tele_status` (`status`),
  KEY `idx_tele_dept` (`apply_dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='远程会诊';

-- ----------------------------
-- sys_ai_prompt_template  AI提示词模板
-- ----------------------------
CREATE TABLE `sys_ai_prompt_template` (
  `id` bigint NOT NULL COMMENT '主键',
  `template_code` varchar(64) NOT NULL COMMENT '模板编码',
  `template_name` varchar(128) NOT NULL COMMENT '模板名称',
  `version` varchar(32) NOT NULL DEFAULT '1.0' COMMENT '版本号',
  `content` mediumtext NOT NULL COMMENT '模板正文',
  `is_active` tinyint NOT NULL DEFAULT '1' COMMENT '是否当前生效（1-是 0-否）',
  `remark` varchar(500) COMMENT '备注/修改说明',
  `create_by_id` bigint COMMENT '创建人ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by_id` bigint COMMENT '更新人ID',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_version` (`template_code`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI提示词模板';

-- ----------------------------
-- sys_ai_qa_column  AI运营问数数据列配置
-- ----------------------------
CREATE TABLE `sys_ai_qa_column` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `table_name` varchar(64) NOT NULL COMMENT '数据表名',
  `column_name` varchar(64) NOT NULL COMMENT '列名',
  `column_desc` varchar(200) NOT NULL DEFAULT '' COMMENT '列含义说明（来自表结构注释）',
  `column_type` varchar(64) COMMENT '列类型（来自 information_schema data_type）',
  `is_filterable` tinyint NOT NULL DEFAULT '0' COMMENT '可否用于过滤条件（1-是 0-否）',
  `is_groupable` tinyint NOT NULL DEFAULT '1' COMMENT '可否用于分组统计（1-是 0-否）',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否喂给模型（1-启用 0-停用）',
  `sensitive_type` tinyint NOT NULL DEFAULT '0' COMMENT '敏感类型（0-普通 1-脱敏 2-禁止出库）',
  `mask_type` varchar(16) COMMENT '脱敏方式（phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `create_by` varchar(64) NOT NULL,
  `create_by_id` bigint COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT (now()),
  `update_by` varchar(64) NOT NULL,
  `update_by_id` bigint COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT (now()),
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_column` (`table_name`,`column_name`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI运营问数数据列配置';

-- ----------------------------
-- sys_ai_qa_relation  AI运营问数表间关系配置（join 权威键）
-- ----------------------------
CREATE TABLE `sys_ai_qa_relation` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `source_table` varchar(64) NOT NULL COMMENT '源表名',
  `source_column` varchar(64) NOT NULL COMMENT '源表关联列',
  `target_table` varchar(64) NOT NULL COMMENT '目标表名',
  `target_column` varchar(64) NOT NULL COMMENT '目标表关联列',
  `relation_type` tinyint NOT NULL DEFAULT '1' COMMENT '关系类型（1-业务关联 2-维度关联）',
  `description` varchar(200) NOT NULL DEFAULT '' COMMENT '关系说明（喂给模型：何时用此关联）',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用 0-停用）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `create_by_id` bigint COMMENT '创建人ID',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by_id` bigint COMMENT '更新人ID',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rel_src_tgt` (`source_table`,`source_column`,`target_table`,`target_column`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI运营问数表间关系配置（join 权威键）';

-- ----------------------------
-- sys_ai_qa_schema  AI运营问数数据表配置
-- ----------------------------
CREATE TABLE `sys_ai_qa_schema` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `table_name` varchar(64) NOT NULL COMMENT '数据表名',
  `table_desc` varchar(200) NOT NULL DEFAULT '' COMMENT '表用途说明（喂给模型）',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用 0-停用）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `create_by` varchar(64) NOT NULL,
  `create_by_id` bigint COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT (now()),
  `update_by` varchar(64) NOT NULL,
  `update_by_id` bigint COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT (now()),
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_name` (`table_name`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI运营问数数据表配置';

-- ----------------------------
-- sys_ai_qa_semantic  AI运营问数语义映射配置
-- ----------------------------
CREATE TABLE `sys_ai_qa_semantic` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `term` varchar(64) NOT NULL COMMENT '语义词/短语（问题文本包含即命中）',
  `table_name` varchar(64) NOT NULL COMMENT '命中表',
  `column_name` varchar(64) COMMENT '目标列（空=表级口径）',
  `scope_desc` varchar(500) NOT NULL DEFAULT '' COMMENT '口径说明（喂给模型作为权威口径）',
  `match_mode` tinyint NOT NULL DEFAULT '1' COMMENT '匹配方式（1-包含 2-精确）',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级（大者先）',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用 0-停用）',
  `create_by` varchar(64) NOT NULL,
  `create_by_id` bigint COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT (now()),
  `update_by` varchar(64) NOT NULL,
  `update_by_id` bigint COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT (now()),
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_term` (`term`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI运营问数语义映射配置';

-- ----------------------------
-- sys_alert_rule  预警规则
-- ----------------------------
CREATE TABLE `sys_alert_rule` (
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `rule_name` varchar(64) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(32) NOT NULL DEFAULT '' COMMENT '规则类型',
  `rule_condition` varchar(256) COMMENT '规则条件表达式',
  `threshold` int COMMENT '阈值',
  `notify_channel` varchar(32) COMMENT '通知渠道',
  `is_active` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用（0-停用 1-启用）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警规则';

-- ----------------------------
-- sys_faq  患者端常见问题
-- ----------------------------
CREATE TABLE `sys_faq` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `faq_no` varchar(32) NOT NULL COMMENT '常见问题编号',
  `category_code` varchar(32) NOT NULL DEFAULT '' COMMENT '分类编码',
  `category_name` varchar(64) NOT NULL COMMENT '分类名称',
  `question` varchar(200) NOT NULL DEFAULT '' COMMENT '问题',
  `answer` varchar(1000) NOT NULL DEFAULT '' COMMENT '答案（人工维护，涉时间/价格/比例一律引导式）',
  `keywords` varchar(500) COMMENT '检索关键词（顿号分隔，含口语同义词）',
  `hot_flag` tinyint COMMENT '热门（0-否 1-是）',
  `view_count` int COMMENT '查看次数',
  `helpful_count` int COMMENT '有帮助次数',
  `useless_count` int COMMENT '没帮助次数',
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `sort_order` int COMMENT '排序号',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_faq_no` (`faq_no`),
  KEY `idx_category` (`category_code`),
  KEY `idx_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者端常见问题';

-- ----------------------------
-- sys_knowledge_doc  知识库文档
-- ----------------------------
CREATE TABLE `sys_knowledge_doc` (
  `id` bigint NOT NULL COMMENT '文档ID（雪花）',
  `title` varchar(200) NOT NULL DEFAULT '' COMMENT '文档标题',
  `category` varchar(50) COMMENT '分类',
  `source_type` tinyint COMMENT '来源类型（1-内置示例 2-手工录入 3-文件导入）',
  `version` varchar(50) COMMENT '版本号',
  `source_name` varchar(100) COMMENT '来源名称（诊疗指南/论坛/院内制度等）',
  `content` longtext COMMENT '原始全文',
  `chunk_count` int COMMENT '切块数量',
  `status` tinyint COMMENT '状态（0-正常 1-停用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文档';

-- ----------------------------
-- sys_knowledge_eval_case  知识库评测用例
-- ----------------------------
CREATE TABLE `sys_knowledge_eval_case` (
  `id` bigint NOT NULL COMMENT '主键',
  `question` varchar(500) NOT NULL DEFAULT '' COMMENT '评测问题',
  `expected_doc_ids` varchar(1000) NOT NULL DEFAULT '' COMMENT '期望命中文档ID（分号分隔）',
  `expected_doc_titles` varchar(1000) COMMENT '期望命中文档标题（分号分隔，展示用）',
  `remark` varchar(200) COMMENT '说明',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0-正常 1-删除）',
  `create_by` bigint COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint COMMENT '更新人ID',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库评测用例';

-- ----------------------------
-- sys_knowledge_eval_run  知识库评测运行汇总
-- ----------------------------
CREATE TABLE `sys_knowledge_eval_run` (
  `id` bigint NOT NULL COMMENT '主键',
  `case_count` int NOT NULL DEFAULT '0' COMMENT '评测用例数',
  `hit_case_count` int NOT NULL DEFAULT '0' COMMENT '命中期望文档的用例数',
  `recall_at_k` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '召回率（%）',
  `mrr` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '平均倒数排名（MRR，0-1）',
  `avg_latency_ms` int NOT NULL DEFAULT '0' COMMENT '平均检索耗时（毫秒）',
  `config_snapshot` varchar(200) COMMENT '评测时配置快照（切块参数/embedding）',
  `status` tinyint NOT NULL DEFAULT '2' COMMENT '状态（2-完成 3-失败）',
  `error_msg` varchar(500) COMMENT '失败原因',
  `operator_id` bigint COMMENT '操作人ID',
  `operator_name` varchar(64) COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '运行时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库评测运行汇总';

-- ----------------------------
-- sys_knowledge_eval_run_detail  知识库评测运行明细
-- ----------------------------
CREATE TABLE `sys_knowledge_eval_run_detail` (
  `id` bigint NOT NULL COMMENT '主键',
  `run_id` bigint NOT NULL DEFAULT '0' COMMENT '运行ID',
  `case_id` bigint NOT NULL DEFAULT '0' COMMENT '用例ID',
  `question` varchar(500) NOT NULL DEFAULT '' COMMENT '评测问题',
  `expected_doc_ids` varchar(1000) NOT NULL DEFAULT '' COMMENT '期望命中文档ID（分号分隔）',
  `expected_doc_titles` varchar(1000) COMMENT '期望命中文档标题（分号分隔）',
  `hit_doc_titles` varchar(1000) COMMENT '实际命中文档标题（前5个，分号分隔）',
  `hit_count` int NOT NULL DEFAULT '0' COMMENT '实际命中块数',
  `hit_rank` int NOT NULL DEFAULT '0' COMMENT '期望文档最高命中位次（1起，0=未命中）',
  `hit_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否命中期望文档（1-命中 0-未命中）',
  PRIMARY KEY (`id`),
  KEY `idx_run_id` (`run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库评测运行明细';

-- ----------------------------
-- sys_message  消息通知
-- ----------------------------
CREATE TABLE `sys_message` (
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `message_no` varchar(32) NOT NULL COMMENT '消息编号',
  `channel` varchar(32) NOT NULL DEFAULT '' COMMENT '发送渠道',
  `receiver_id` bigint NOT NULL DEFAULT '0' COMMENT '接收人ID',
  `receiver_name` varchar(64) COMMENT '接收人姓名',
  `title` varchar(128) COMMENT '消息标题',
  `content` varchar(2000) NOT NULL DEFAULT '' COMMENT '消息内容',
  `biz_type` varchar(32) COMMENT '业务类型',
  `biz_id` bigint COMMENT '关联业务ID',
  `severity` varchar(16) NOT NULL DEFAULT 'info' COMMENT '紧急度',
  `payload` json COMMENT '结构化负载(JSON)',
  `handle_status` tinyint COMMENT '处理状态（0-待处理 1-已处理 2-已关闭）',
  `send_status` tinyint NOT NULL DEFAULT '0' COMMENT '发送状态（0-待发送 1-已发送 2-发送失败）',
  `send_time` datetime COMMENT '发送时间',
  `error_msg` varchar(500) COMMENT '渠道发送失败原因',
  `read_status` tinyint NOT NULL DEFAULT '0' COMMENT '阅读状态（0-未读 1-已读）',
  `read_time` datetime COMMENT '阅读时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint COMMENT '创建人ID',
  `update_by_id` bigint COMMENT '更新人ID',
  PRIMARY KEY (`message_id`),
  UNIQUE KEY `uk_message_no` (`message_no`),
  KEY `idx_receiver` (`receiver_id`,`read_status`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知';

-- ----------------------------
-- sys_service_trace  客服页自助行为埋点
-- ----------------------------
CREATE TABLE `sys_service_trace` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint COMMENT '用户ID',
  `patient_id` bigint COMMENT '就诊人ID',
  `session_id` varchar(64) COMMENT '会话标识（同一次进入客服页）',
  `event_type` varchar(32) NOT NULL DEFAULT '' COMMENT '事件类型（visit/card/search/view/helpful/useless/transfer/message）',
  `event_key` varchar(200) COMMENT '事件对象（卡片名、搜索词、常见问题ID）',
  `faq_id` bigint COMMENT '关联常见问题ID',
  `ref_id` bigint COMMENT '关联业务ID（留言ID）',
  `hit_count` int COMMENT '搜索命中条数（event_type=search 时）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`),
  KEY `idx_event_time` (`event_type`,`create_time`),
  KEY `idx_faq` (`faq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服页自助行为埋点';

-- ----------------------------
-- sys_workbench_layout  工作台个人布局
-- ----------------------------
CREATE TABLE `sys_workbench_layout` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '个人顺序',
  `visible` tinyint NOT NULL DEFAULT '1' COMMENT '个人显隐',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_layout_user_widget` (`user_id`,`widget_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作台个人布局';

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
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_role_widget` (`role_id`,`widget_id`),
  KEY `idx_workbench_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色工作台配置';

-- ----------------------------
-- sys_workbench_widget  工作台卡片注册表
-- ----------------------------
CREATE TABLE `sys_workbench_widget` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `widget_name` varchar(64) NOT NULL COMMENT '卡片标题',
  `area` varchar(16) NOT NULL DEFAULT 'domain' COMMENT '归属区域',
  `api_key` varchar(128) NOT NULL DEFAULT '' COMMENT '取数来源标识',
  `permission` varchar(128) COMMENT '可见所需权限码',
  `default_span` int NOT NULL DEFAULT '6' COMMENT '栅格占宽',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '展示顺序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-已上线可挂载 0-注册表先占位）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_widget_code` (`widget_code`),
  KEY `idx_workbench_widget_area` (`area`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作台卡片注册表';
