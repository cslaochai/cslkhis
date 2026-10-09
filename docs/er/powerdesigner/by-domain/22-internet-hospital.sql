-- ============================================================
-- 领域 22 互联网医院·随访·满意度·消息与工作台（本域 21 表 + 上游参照 10 表 / 46 条关系）
-- 由 workspace/_er/refresh.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


-- biz_online_consult  线上问诊
CREATE TABLE `biz_online_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '问诊单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '接诊科室ID',
  `dept_name` varchar(128) COMMENT '接诊科室名称（快照）',
  `doctor_id` bigint COMMENT '接诊医生ID（员工ID）',
  `doctor_name` varchar(64) COMMENT '接诊医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT 1 COMMENT '问诊方式（1-图文问诊 2-电话问诊 3-视频问诊）',
  `chief_complaint` varchar(1000) NOT NULL COMMENT '主诉/问题描述',
  `reply` varchar(1000) COMMENT '医生回复',
  `advice` varchar(500) COMMENT '处置建议',
  `need_visit` tinyint NOT NULL DEFAULT 0 COMMENT '是否建议线下就诊（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待接诊 2-接诊中 3-已完成 4-已退诊）',
  `fee` decimal(10,2) COMMENT '问诊费用',
  `apply_time` datetime COMMENT '发起时间',
  `accept_by` varchar(64) COMMENT '接诊人',
  `accept_time` datetime COMMENT '接诊时间',
  `finish_by` varchar(64) COMMENT '完成人',
  `finish_time` datetime COMMENT '完成时间',
  `reject_reason` varchar(500) COMMENT '退诊原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_online_no` (`consult_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线上问诊';

-- biz_tele_consult  远程会诊
CREATE TABLE `biz_tele_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '会诊单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `admission_id` bigint COMMENT '关联住院ID',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(128) COMMENT '申请科室名称（快照）',
  `apply_doctor_id` bigint COMMENT '申请医生ID（员工ID）',
  `apply_doctor` varchar(64) COMMENT '申请医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT 1 COMMENT '会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他）',
  `is_urgent` tinyint NOT NULL DEFAULT 0 COMMENT '是否急会诊（0-否 1-是）',
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
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待安排 2-已安排 3-已完成 4-已取消）',
  `fee` decimal(10,2) COMMENT '会诊费用',
  `arrange_by` varchar(64) COMMENT '安排人',
  `arrange_time` datetime COMMENT '安排时间',
  `complete_by` varchar(64) COMMENT '完成人',
  `complete_time` datetime COMMENT '完成时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tele_no` (`consult_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='远程会诊';

-- biz_referral  转诊
CREATE TABLE `biz_referral` (
  `referral_id` bigint NOT NULL COMMENT '转诊ID',
  `referral_no` varchar(32) NOT NULL COMMENT '转诊编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `from_dept_id` bigint NOT NULL COMMENT '转出科室ID',
  `to_dept_id` bigint COMMENT '转入科室ID',
  `to_hospital` varchar(128) COMMENT '转入医院',
  `reason` varchar(1000) COMMENT '转诊原因',
  `referral_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转诊时间',
  `referral_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-待确认 1-已确认 2-已完成 3-已取消）',
  `remark` varchar(500) COMMENT '备注',
  `direction` tinyint DEFAULT 1 COMMENT '转诊方向（1-上转 2-下转）',
  `admission_id` bigint COMMENT '入院ID',
  `diagnosis` varchar(500) COMMENT '诊断摘要',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `audit_by` bigint COMMENT '确认人（员工ID）',
  `audit_name` varchar(50) COMMENT '确认人姓名',
  `audit_time` datetime COMMENT '确认时间',
  `audit_remark` varchar(500) COMMENT '确认意见',
  `finish_time` datetime COMMENT '完成时间',
  `finish_remark` varchar(500) COMMENT '完成备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`referral_id`),
  UNIQUE KEY `uk_referral_no` (`referral_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转诊';

-- biz_followup_task  随访任务
CREATE TABLE `biz_followup_task` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `phone` varchar(20) COMMENT '联系电话',
  `diagnosis` varchar(200) COMMENT '诊断',
  `dept_id` bigint COMMENT '随访所属科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `followup_type` tinyint NOT NULL COMMENT '随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访）',
  `followup_content` varchar(500) COMMENT '随访内容',
  `followup_time` datetime NOT NULL COMMENT '计划随访时间',
  `followup_status` tinyint NOT NULL DEFAULT 1 COMMENT '随访状态（1-待随访 2-随访中 3-已完成 4-已取消）',
  `executor_id` bigint COMMENT '执行人ID',
  `executor_name` varchar(50) COMMENT '执行人姓名',
  `execute_time` datetime COMMENT '执行时间',
  `execute_result` varchar(500) COMMENT '执行结果',
  `revisit_record_id` bigint COMMENT '复诊引用的原病历ID',
  `revisit_appoint_id` bigint COMMENT '由本任务生成的复诊挂号ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `patient_reply` text COMMENT '患者反馈内容（小程序回写）',
  `patient_reply_time` datetime COMMENT '患者反馈时间',
  `call_channel` tinyint COMMENT '外呼通道（1-人工 2-自动）',
  `call_status` tinyint NOT NULL DEFAULT 0 COMMENT '外呼状态（0-未外呼 1-待外呼 2-已接通 3-未接通）',
  `call_time` datetime COMMENT '最近一次外呼登记时间',
  `call_attempts` int NOT NULL DEFAULT 0 COMMENT '累计外呼登记次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访任务';

-- biz_survey_template  满意度问卷模板
CREATE TABLE `biz_survey_template` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_no` varchar(32) NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) NOT NULL COMMENT '问卷名称',
  `scene` tinyint NOT NULL COMMENT '适用场景（1-出院随访 2-门诊 3-住院在院 4-体检）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 2-停用）',
  `description` varchar(500) COMMENT '说明（调查目的、口径、上报去向）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_template_no` (`template_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度问卷模板';

-- biz_survey_item  满意度问卷题目
CREATE TABLE `biz_survey_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `seq_no` int NOT NULL COMMENT '题号',
  `dimension` tinyint NOT NULL COMMENT '评价维度',
  `question_type` tinyint NOT NULL COMMENT '题型（1-量表 2-单选 3-多选 4-NPS推荐度 5-开放文本）',
  `title` varchar(255) NOT NULL COMMENT '题干',
  `required` tinyint NOT NULL DEFAULT 1 COMMENT '是否必答（0-否 1-是）',
  `weight` decimal(5,2) NOT NULL DEFAULT 1.00 COMMENT '权重',
  `max_score` tinyint NOT NULL DEFAULT 5 COMMENT '满分',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_item` (`template_id`, `seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度问卷题目';

-- biz_survey_dispatch  满意度发放台账
CREATE TABLE `biz_survey_dispatch` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dispatch_no` varchar(32) NOT NULL COMMENT '发放单号',
  `template_id` bigint NOT NULL COMMENT '问卷模板ID',
  `template_name` varchar(128) COMMENT '模板名称',
  `scene` tinyint NOT NULL COMMENT '适用场景（快照）',
  `source_type` tinyint NOT NULL COMMENT '发放来源（1-随访任务 2-出院结算 3-人工补发）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `phone` varchar(20) COMMENT '联系手机号',
  `dept_id` bigint COMMENT '就诊科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `channel` tinyint NOT NULL DEFAULT 1 COMMENT '回收渠道（1-电话代填 2-短信 3-微信 4-现场扫码）',
  `dispatch_status` tinyint NOT NULL DEFAULT 1 COMMENT '回收状态（1-待推送 2-已推送待回收 3-已回收 4-已过期 5-已拒答）',
  `push_time` datetime COMMENT '推送/发起时间',
  `expire_time` datetime COMMENT '回收截止时间',
  `answer_id` bigint COMMENT '回收到的答卷ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_dispatch_no` (`dispatch_no`),
  UNIQUE KEY `uk_survey_dispatch_source` (`source_type`, `source_id`, `template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度发放台账';

-- biz_survey_answer  满意度答卷
CREATE TABLE `biz_survey_answer` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `answer_no` varchar(32) NOT NULL COMMENT '答卷编号',
  `dispatch_id` bigint NOT NULL COMMENT '发放单ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `scene` tinyint NOT NULL COMMENT '场景（快照）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '就诊科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `avg_score` decimal(5,2) NOT NULL COMMENT '李克特均分',
  `score_100` decimal(6,2) NOT NULL COMMENT '百分制得分',
  `nps` tinyint COMMENT 'NPS 推荐度',
  `comment_text` varchar(1000) COMMENT '开放意见',
  `fill_source` tinyint NOT NULL DEFAULT 2 COMMENT '填报方式（1-患者自填 2-随访员代填 3-现场扫码）',
  `anonymous_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否匿名（0-否 1-是）',
  `fill_employee_id` bigint COMMENT '代填人（员工ID）',
  `fill_employee_name` varchar(64) COMMENT '代填人姓名',
  `fill_time` datetime COMMENT '提交时间',
  `answer_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-有效 2-已作废）',
  `dispute_case_id` bigint COMMENT '低分自动转出的投诉单ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_answer_no` (`answer_no`),
  UNIQUE KEY `uk_survey_answer_dispatch` (`dispatch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度答卷';

-- biz_survey_answer_item  满意度逐题答案
CREATE TABLE `biz_survey_answer_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `answer_id` bigint NOT NULL COMMENT '答卷ID',
  `item_id` bigint NOT NULL COMMENT '题目ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `dimension` tinyint NOT NULL COMMENT '评价维度',
  `seq_no` int NOT NULL COMMENT '题号（快照）',
  `title` varchar(255) NOT NULL COMMENT '题干（快照）',
  `question_type` tinyint NOT NULL COMMENT '题型（快照）',
  `score` tinyint COMMENT '得分',
  `option_label` varchar(128) COMMENT '选项文本',
  `text_value` varchar(1000) COMMENT '文本题回答',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_answer_item` (`answer_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度逐题答案';

-- sys_message  消息通知
CREATE TABLE `sys_message` (
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `message_no` varchar(32) NOT NULL COMMENT '消息编号',
  `channel` varchar(32) NOT NULL COMMENT '发送渠道',
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `receiver_name` varchar(64) COMMENT '接收人姓名',
  `title` varchar(128) COMMENT '消息标题',
  `content` varchar(2000) NOT NULL COMMENT '消息内容',
  `biz_type` varchar(32) COMMENT '业务类型',
  `biz_id` bigint COMMENT '关联业务ID',
  `severity` varchar(16) NOT NULL DEFAULT 'info' COMMENT '紧急度',
  `payload` text COMMENT '结构化负载(JSON)',
  `handle_status` tinyint COMMENT '处理状态（0-待处理 1-已处理 2-已关闭）',
  `send_status` tinyint NOT NULL DEFAULT 0 COMMENT '发送状态（0-待发送 1-已发送 2-发送失败）',
  `send_time` datetime COMMENT '发送时间',
  `error_msg` varchar(500) COMMENT '渠道发送失败原因',
  `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '阅读状态（0-未读 1-已读）',
  `read_time` datetime COMMENT '阅读时间',
  PRIMARY KEY (`message_id`),
  UNIQUE KEY `uk_message_no` (`message_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

-- sys_alert_rule  预警规则
CREATE TABLE `sys_alert_rule` (
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `rule_name` varchar(64) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(32) NOT NULL COMMENT '规则类型',
  `rule_condition` varchar(256) COMMENT '规则条件表达式',
  `threshold` int COMMENT '阈值',
  `notify_channel` varchar(32) DEFAULT 'system' COMMENT '通知渠道',
  `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用（0-停用 1-启用）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则';

-- biz_alert  预警记录
CREATE TABLE `biz_alert` (
  `alert_id` bigint NOT NULL COMMENT '预警ID',
  `alert_no` varchar(32) NOT NULL COMMENT '预警编号',
  `rule_id` bigint COMMENT '规则ID',
  `alert_type` varchar(32) NOT NULL COMMENT '预警类型',
  `alert_content` varchar(1000) NOT NULL COMMENT '预警内容',
  `alert_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-未处理 1-已处理 2-已忽略）',
  `notify_user_id` bigint COMMENT '通知用户ID',
  `notify_time` datetime COMMENT '通知时间',
  `read_time` datetime COMMENT '阅读时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`alert_id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录';

-- sys_workbench_widget  工作台卡片注册表
CREATE TABLE `sys_workbench_widget` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `widget_name` varchar(64) NOT NULL COMMENT '卡片标题',
  `area` varchar(16) NOT NULL DEFAULT 'domain' COMMENT '归属区域',
  `api_key` varchar(128) NOT NULL COMMENT '取数来源标识',
  `permission` varchar(128) COMMENT '可见所需权限码',
  `default_span` int NOT NULL DEFAULT 6 COMMENT '栅格占宽',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '展示顺序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已上线可挂载 0-注册表先占位）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_widget_code` (`widget_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作台卡片注册表';

-- sys_workbench_role  角色工作台配置
CREATE TABLE `sys_workbench_role` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `widget_id` bigint NOT NULL COMMENT '卡片ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '该角色下的卡片顺序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否展示',
  `landing_scope` tinyint NOT NULL DEFAULT 0 COMMENT '登录/切角色落点（0-默认 1-一律工作台 2-一律患者工作站）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_role_widget` (`role_id`, `widget_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色工作台配置';

-- sys_workbench_layout  工作台个人布局
CREATE TABLE `sys_workbench_layout` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '个人顺序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '个人显隐',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_layout_user_widget` (`user_id`, `widget_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作台个人布局';

-- biz_service_message  患者端留言
CREATE TABLE `biz_service_message` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_no` varchar(32) NOT NULL COMMENT '留言单号',
  `user_id` bigint COMMENT '留言用户ID',
  `patient_id` bigint COMMENT '就诊人ID',
  `patient_name` varchar(64) COMMENT '就诊人姓名（快照）',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `category_code` varchar(32) COMMENT '留言分类（同 sys_faq.category_code）',
  `content` varchar(1000) NOT NULL COMMENT '留言内容',
  `status` tinyint DEFAULT 0 COMMENT '工单状态（0-待受理 1-处理中 2-已办结 3-已关闭）',
  `priority` tinyint DEFAULT 0 COMMENT '优先级（0-普通 1-紧急）',
  `accept_by` varchar(64) COMMENT '受理人账号（服务端取登录人，不由前端传）',
  `accept_by_name` varchar(64) COMMENT '受理人姓名',
  `accept_time` datetime COMMENT '受理时间',
  `close_by` varchar(64) COMMENT '关闭人账号',
  `close_time` datetime COMMENT '关闭时间',
  `close_reason` varchar(200) COMMENT '关闭原因（患者撤单/客服关闭都要写）',
  `last_reply_time` datetime COMMENT '最后一次客服回复时间',
  `reply_count` int DEFAULT 0 COMMENT '客服回复次数',
  `handle_by` varchar(64) COMMENT '处理人',
  `handle_time` datetime COMMENT '处理时间',
  `handle_result` varchar(500) COMMENT '处理结果',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_no` (`message_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者端留言';

-- biz_service_ticket_log  工单流转记录（患者端进展时间轴 + 客服端证据链）
CREATE TABLE `biz_service_ticket_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_id` bigint NOT NULL COMMENT '工单ID（biz_service_message.id）',
  `message_no` varchar(32) COMMENT '工单号（冗余，排查时不用 join）',
  `action` tinyint NOT NULL COMMENT '动作（0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开）',
  `content` varchar(1000) COMMENT '内容（回复正文 / 处理结果 / 撤单原因）',
  `visible_to_patient` tinyint DEFAULT 1 COMMENT '患者是否可见（0-内部备注 1-患者可见）',
  `operator_type` tinyint DEFAULT 1 COMMENT '操作人类型（1-患者 2-院内）',
  `operator` varchar(64) COMMENT '操作人账号',
  `operator_name` varchar(64) COMMENT '操作人姓名',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转记录（患者端进展时间轴 + 客服端证据链）';

-- sys_faq  患者端常见问题
CREATE TABLE `sys_faq` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `faq_no` varchar(32) NOT NULL COMMENT '常见问题编号',
  `category_code` varchar(32) NOT NULL COMMENT '分类编码',
  `category_name` varchar(64) NOT NULL COMMENT '分类名称',
  `question` varchar(200) NOT NULL COMMENT '问题',
  `answer` varchar(1000) NOT NULL COMMENT '答案（人工维护，涉时间/价格/比例一律引导式）',
  `keywords` varchar(500) COMMENT '检索关键词（顿号分隔，含口语同义词）',
  `hot_flag` tinyint DEFAULT 0 COMMENT '热门（0-否 1-是）',
  `view_count` int DEFAULT 0 COMMENT '查看次数',
  `helpful_count` int DEFAULT 0 COMMENT '有帮助次数',
  `useless_count` int DEFAULT 0 COMMENT '没帮助次数',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_faq_no` (`faq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者端常见问题';

-- sys_knowledge_chunk  知识库切块（向量在内存，文本在此）
CREATE TABLE `sys_knowledge_chunk` (
  `id` bigint NOT NULL COMMENT '切块ID（雪花）',
  `doc_id` bigint NOT NULL COMMENT '所属文档ID',
  `doc_title` varchar(200) DEFAULT '' COMMENT '文档标题（冗余）',
  `category` varchar(50) DEFAULT '' COMMENT '分类（冗余）',
  `chunk_index` int DEFAULT 0 COMMENT '块序号',
  `content` longtext COMMENT '切块文本',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库切块（向量在内存，文本在此）';

-- sys_knowledge_doc  知识库文档
CREATE TABLE `sys_knowledge_doc` (
  `id` bigint NOT NULL COMMENT '文档ID（雪花）',
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `category` varchar(50) DEFAULT '' COMMENT '分类',
  `source_type` tinyint DEFAULT 1 COMMENT '来源类型（1-内置示例 2-手工录入 3-文件导入）',
  `content` longtext COMMENT '原始全文',
  `chunk_count` int DEFAULT 0 COMMENT '切块数量',
  `status` tinyint DEFAULT 0 COMMENT '状态（0-正常 1-停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

-- sys_service_trace  客服页自助行为埋点
CREATE TABLE `sys_service_trace` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint COMMENT '用户ID',
  `patient_id` bigint COMMENT '就诊人ID',
  `session_id` varchar(64) COMMENT '会话标识（同一次进入客服页）',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型（visit/card/search/view/helpful/useless/transfer/message）',
  `event_key` varchar(200) COMMENT '事件对象（卡片名、搜索词、常见问题ID）',
  `faq_id` bigint COMMENT '关联常见问题ID',
  `ref_id` bigint COMMENT '关联业务ID（留言ID）',
  `hit_count` int COMMENT '搜索命中条数（event_type=search 时）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服页自助行为埋点';

-- biz_admission  入院记录
CREATE TABLE `biz_admission` (
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) NOT NULL COMMENT '入院记录号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `regist_id` bigint COMMENT '来源挂号ID',
  `regist_no` varchar(32) COMMENT '来源挂号号（快照）',
  `admission_order_id` bigint COMMENT '来源住院证ID',
  `admit_dept_id` bigint COMMENT '入院科室ID',
  `dept_id` bigint COMMENT '入院科室ID',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `nursing_level` tinyint COMMENT '护理等级（1-特级 2-一级 3-二级 4-三级，字典 his_nursing_level）',
  `nursing_level_source` tinyint NOT NULL DEFAULT 1 COMMENT '护理等级来源（1-默认兜底 2-护理记录带出 3-护士长评定）',
  `nursing_level_time` datetime COMMENT '护理等级评定时间（默认兜底时为写入时间）',
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `admit_doctor_id` bigint NOT NULL COMMENT '入院医生ID',
  `admit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入院时间',
  `admit_way` tinyint COMMENT '入院途径（1-门诊 2-急诊 3-转院 4-其他）',
  `diagnosis` varchar(500) COMMENT '入院诊断',
  `admit_diagnosis_code` varchar(32) COMMENT '入院诊断ICD编码',
  `admit_diagnosis_name` varchar(200) COMMENT '入院诊断名称',
  `discharge_time` datetime COMMENT '出院时间',
  `admit_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-已出院 1-在院）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`admission_id`),
  UNIQUE KEY `uk_admission_no` (`admission_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入院记录';

-- biz_appoint_info  挂号信息
CREATE TABLE `biz_appoint_info` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `regist_no` varchar(32) NOT NULL COMMENT '挂号单号（唯一）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL COMMENT '性别（1-男 2-女 3-未知）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '手机号码',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(100) COMMENT '诊室名称',
  `doctor_id` bigint COMMENT '医生ID',
  `doctor_name` varchar(50) COMMENT '医生姓名',
  `schedule_id` bigint COMMENT '排班ID',
  `slot_id` bigint COMMENT '排班时间片段ID',
  `slot_start` char(5) COMMENT '就诊时段开始快照（HH:mm）',
  `slot_end` char(5) COMMENT '就诊时段结束快照（HH:mm）',
  `regist_type` tinyint DEFAULT 1 COMMENT '挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）',
  `regist_source` tinyint DEFAULT 1 COMMENT '挂号来源（1-窗口挂号 2-自助机挂号 3-网上挂号 4-预约挂号）',
  `regist_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `arrive_time` datetime COMMENT '到达时间',
  `schedule_type` tinyint COMMENT '时间段（1-上午 2-下午 3-全天 4-凌晨）',
  `slot_time` varchar(8) COMMENT '就诊时段',
  `visit_type` tinyint NOT NULL COMMENT '就诊类型（号别）（1-初诊 2-复诊）',
  `revisit_source` tinyint COMMENT '复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）',
  `settlement_type` tinyint DEFAULT 1 COMMENT '结算方式（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-商业保险）',
  `medical_insurance_type` varchar(50) COMMENT '医保类型（如：在职职工、退休职工、城乡居民等）',
  `medical_insurance_no` varchar(50) COMMENT '医保卡号',
  `regist_status` tinyint DEFAULT 1 COMMENT '挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号 7-爽约 8-未就诊）',
  `revisit_type` tinyint COMMENT '【已废置】改用 visit_type',
  `revisit_record_id` bigint COMMENT '复诊关联的病历ID',
  `refund_time` datetime COMMENT '退号时间',
  `refund_reason` varchar(200) COMMENT '退号原因',
  `bill_id` bigint COMMENT '挂号费结算账单ID',
  `bill_no` varchar(32) COMMENT '账单号（快照）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_by_id` bigint COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_by_id` bigint COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_regist_no` (`regist_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号信息';

-- biz_dispute_case  医疗纠纷投诉主单
CREATE TABLE `biz_dispute_case` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `case_no` varchar(32) NOT NULL COMMENT '单据编号',
  `case_type` tinyint NOT NULL COMMENT '类型（1-服务投诉 2-医疗纠纷 3-医疗损害争议 4-其他）',
  `source_type` tinyint NOT NULL COMMENT '来源（1-来电 2-来访 3-来信 4-政务热线 5-上级交办 6-院内发现 7-其他）',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '等级（1-一般 2-较大 3-重大）',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `admission_id` bigint COMMENT '关联住院ID',
  `dept_id` bigint COMMENT '被投诉科室ID',
  `dept_name` varchar(128) COMMENT '被投诉科室名称（快照）',
  `involved_staff` varchar(255) COMMENT '涉及人员',
  `complainant` varchar(64) COMMENT '投诉人姓名（可为患者本人/家属/其他）',
  `complainant_rel` varchar(32) COMMENT '与患者关系（1-本人 2-家属 3-代理人 4-其他）',
  `complainant_tel` varchar(32) COMMENT '投诉人联系电话',
  `occur_time` datetime COMMENT '事件发生时间',
  `occur_place` varchar(128) COMMENT '事件发生地点',
  `content` varchar(1000) NOT NULL COMMENT '投诉/纠纷内容',
  `demand` varchar(500) COMMENT '投诉人诉求',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待受理 2-调查中 3-处理中 4-已结案 5-已撤销）',
  `need_seal` tinyint NOT NULL DEFAULT 0 COMMENT '是否需封存病历（0-否 1-是）',
  `seal_status` tinyint NOT NULL DEFAULT 0 COMMENT '封存状态（0-未申请 1-已封存 2-待归档后封存）',
  `archive_id` bigint COMMENT '已封存病案ID',
  `seal_time` datetime COMMENT '封存时间',
  `deal_type` tinyint COMMENT '处理途径（1-院内协商 2-医调委调解 3-行政调解 4-司法鉴定 5-诉讼 6-其他）',
  `duty_type` tinyint COMMENT '责任认定（1-无责 2-轻微责任 3-次要责任 4-主要责任 5-完全责任）',
  `compensation` decimal(12,2) COMMENT '赔偿/补偿金额',
  `conclusion` varchar(1000) COMMENT '调查结论/处理结果',
  `register_by` varchar(64) COMMENT '登记人',
  `register_time` datetime COMMENT '登记时间',
  `accept_by` varchar(64) COMMENT '受理人',
  `accept_time` datetime COMMENT '受理时间',
  `close_by` varchar(64) COMMENT '结案人',
  `close_time` datetime COMMENT '结案时间',
  `revoke_reason` varchar(500) COMMENT '撤销原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_case_no` (`case_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗纠纷投诉主单';

-- biz_medical_record  门诊病历
CREATE TABLE `biz_medical_record` (
  `id` bigint NOT NULL,
  `record_no` varchar(32) NOT NULL COMMENT '病历号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL COMMENT '性别',
  `age` int COMMENT '年龄',
  `regist_id` bigint NOT NULL COMMENT '挂号ID',
  `regist_no` varchar(32) NOT NULL COMMENT '挂号单号',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `visit_type` tinyint DEFAULT 1 COMMENT '就诊类型（1-初诊 2-复诊）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医生姓名',
  `chief_complaint` text COMMENT '主诉',
  `present_illness` text COMMENT '现病史',
  `past_history` text COMMENT '既往史',
  `personal_history` text COMMENT '个人史',
  `family_history` text COMMENT '家族史',
  `allergy_history` text COMMENT '过敏史',
  `temperature` varchar(10) COMMENT '体温（℃）',
  `pulse` varchar(10) COMMENT '脉搏（次/分）',
  `respiration` varchar(10) COMMENT '呼吸（次/分）',
  `systolic_pressure` varchar(10) COMMENT '收缩压（mmHg）',
  `diastolic_pressure` varchar(10) COMMENT '舒张压（mmHg）',
  `general_condition` text COMMENT '一般情况',
  `skin_mucosa` text COMMENT '皮肤黏膜',
  `head_neck` text COMMENT '头颈部',
  `chest_lung` text COMMENT '胸肺',
  `heart` text COMMENT '心脏',
  `abdomen` text COMMENT '腹部',
  `spine_limbs` text COMMENT '脊柱四肢',
  `nervous_system` text COMMENT '神经系统',
  `specialist_exam` text COMMENT '专科检查',
  `auxiliary_exam` text COMMENT '辅助检查',
  `diagnosis` text COMMENT '诊断',
  `diagnosis_code` varchar(100) COMMENT '诊断编码',
  `diagnosis_name` varchar(500) COMMENT '诊断名称',
  `treatment_plan` text COMMENT '处理意见',
  `record_status` tinyint DEFAULT 1 COMMENT '病历状态（1-草稿 2-已提交 3-已归档 4-已作废）',
  `review_status` tinyint DEFAULT 0 COMMENT '审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）',
  `review_by` varchar(64) COMMENT '审核人',
  `review_time` datetime COMMENT '审核时间',
  `review_remark` varchar(500) COMMENT '审核意见',
  `guide_pdf_path` varchar(512) COMMENT '患者引导单文件路径',
  `submit_time` datetime COMMENT '提交时间',
  `archive_time` datetime COMMENT '归档时间',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '签名状态（0-未签名 1-已签名 2-签名已失效）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '最近一次签名时刻',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊病历';

-- biz_patient  患者基本信息
CREATE TABLE `biz_patient` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `master_id` bigint COMMENT '主索引',
  `merge_status` tinyint NOT NULL DEFAULT 0 COMMENT '主索引状态（0-正常 1-已并入主档）',
  `merge_time` datetime COMMENT '并入主档的时间',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL COMMENT '性别（1-男 2-女 9-未知）',
  `birth_date` date COMMENT '出生日期',
  `age` int COMMENT '年龄',
  `id_card` varchar(18) COMMENT '身份证号',
  `phone` varchar(20) COMMENT '手机号码',
  `contact_name` varchar(50) COMMENT '联系人姓名',
  `contact_phone` varchar(20) COMMENT '联系人电话',
  `contact_relation` varchar(20) COMMENT '联系人关系（父母、配偶、子女等）',
  `address` varchar(200) COMMENT '家庭住址',
  `nation` varchar(20) COMMENT '民族',
  `occupation` varchar(50) COMMENT '职业',
  `marital_status` tinyint DEFAULT 0 COMMENT '婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）',
  `blood_type` varchar(10) COMMENT '血型（A/B/O/AB）',
  `allergy_history` text COMMENT '过敏史',
  `medical_history` text COMMENT '既往病史',
  `patient_type` tinyint DEFAULT 1 COMMENT '患者类型（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他）',
  `medical_insurance_no` varchar(50) COMMENT '医保卡号',
  `medical_insurance_type` varchar(50) COMMENT '医保类型',
  `card_type` tinyint DEFAULT 1 COMMENT '卡片类型（1-就诊卡 2-身份证 3-医保卡）',
  `card_no` varchar(50) COMMENT '卡片号码',
  `balance` decimal(10,2) DEFAULT 0.00 COMMENT '账户余额',
  `total_expense` decimal(10,2) DEFAULT 0.00 COMMENT '累计消费金额',
  `visit_count` int DEFAULT 0 COMMENT '就诊次数',
  `last_visit_time` datetime COMMENT '最后就诊时间',
  `last_visit_dept` bigint COMMENT '最后就诊科室',
  `last_visit_doctor` bigint COMMENT '最后就诊医生',
  `last_visit_dept_name` varchar(100) COMMENT '最近就诊科室名',
  `last_visit_doctor_name` varchar(50) COMMENT '最近接诊医生名',
  `first_visit_time` datetime COMMENT '首次就诊时间',
  `first_visit_dept_id` bigint COMMENT '首次就诊科室ID',
  `first_visit_dept_name` varchar(100) COMMENT '首次就诊科室名',
  `first_visit_doctor_id` bigint COMMENT '首次接诊医生ID',
  `first_visit_doctor_name` varchar(50) COMMENT '首次接诊医生名',
  `photo` varchar(200) COMMENT '患者照片',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_no` (`patient_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者基本信息';

-- biz_visit  就诊次
CREATE TABLE `biz_visit` (
  `visit_id` bigint NOT NULL COMMENT '就诊次ID',
  `visit_no` varchar(32) NOT NULL COMMENT '就诊次编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `start_time` datetime NOT NULL COMMENT '就诊开始时间',
  `end_time` datetime COMMENT '就诊结束时间',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '本次就诊总费用',
  `visit_status` tinyint NOT NULL DEFAULT 1 COMMENT '就诊状态（0-已取消 1-进行中 2-已完成）',
  `regist_ids` varchar(500) COMMENT '关联的挂号ID列表',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`visit_id`),
  UNIQUE KEY `uk_visit_no` (`visit_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊次';

-- sys_department  科室
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_code` varchar(32) NOT NULL COMMENT '科室编码（唯一）',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `dept_type` varchar(20) NOT NULL COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父科室ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `dept_icon` varchar(200) COMMENT '科室图标',
  `dept_desc` varchar(500) COMMENT '科室描述',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `location` varchar(200) COMMENT '科室位置',
  `dept_leader_id` bigint COMMENT '科室负责人（sys_employee.id)',
  `is_open` tinyint DEFAULT 1 COMMENT '是否开诊（0-否 1-是）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室';

-- sys_employee  员工
CREATE TABLE `sys_employee` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `emp_code` varchar(32) NOT NULL COMMENT '员工编号（唯一）',
  `emp_name` varchar(50) NOT NULL COMMENT '员工姓名',
  `emp_type` tinyint NOT NULL DEFAULT 1 COMMENT '员工类型（1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他）',
  `gender` tinyint DEFAULT 1 COMMENT '性别',
  `birth_date` date COMMENT '出生日期',
  `hire_date` date COMMENT '入职日期',
  `id_card` varchar(18) COMMENT '身份证号',
  `phone` varchar(20) COMMENT '手机号码',
  `email` varchar(100) COMMENT '电子邮箱',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(200) COMMENT '科室ID',
  `title` varchar(50) COMMENT '职称',
  `position` varchar(50) COMMENT '职位',
  `specialty` varchar(200) COMMENT '专业特长',
  `education` varchar(50) COMMENT '学历',
  `avatar` varchar(200) COMMENT '头像地址',
  `is_expert` tinyint DEFAULT 0 COMMENT '是否专家（0-否 1-是）',
  `expert_price` decimal(10,2) DEFAULT 0.00 COMMENT '专家号价格',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_code` (`emp_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工';

-- sys_role  角色
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码（唯一）',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_type` tinyint DEFAULT 1 COMMENT '角色类型（1-系统角色 2-自定义角色）',
  `staff_type` tinyint COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `data_scope` tinyint DEFAULT 1 COMMENT '数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据）',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- sys_user  用户
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_name` varchar(64) NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(200) NOT NULL COMMENT '密码',
  `real_name` varchar(64) COMMENT '真实姓名',
  `emp_id` bigint COMMENT '关联员工ID',
  `user_type` tinyint DEFAULT 1 COMMENT '用户类型（1-系统用户 2-外部用户）',
  `patient_id` bigint COMMENT '关联患者ID',
  `openid` varchar(64) COMMENT '微信openid',
  `avatar` varchar(200) COMMENT '头像地址',
  `last_login_time` datetime COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COMMENT '最后登录IP',
  `login_count` int DEFAULT 0 COMMENT '登录次数',
  `password_update_time` datetime COMMENT '密码更新时间',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用 2-锁定）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_name`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';
-- ---------------- 参照关系（E-R 连线） ----------------
ALTER TABLE `biz_alert` ADD CONSTRAINT `fk_biz_alert_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `sys_alert_rule` (`rule_id`);
ALTER TABLE `biz_alert` ADD CONSTRAINT `fk_biz_alert_notify_user_id` FOREIGN KEY (`notify_user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_executor_id` FOREIGN KEY (`executor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_revisit_record_id` FOREIGN KEY (`revisit_record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_revisit_appoint_id` FOREIGN KEY (`revisit_appoint_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_online_consult` ADD CONSTRAINT `fk_biz_online_consult_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_online_consult` ADD CONSTRAINT `fk_biz_online_consult_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_online_consult` ADD CONSTRAINT `fk_biz_online_consult_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_from_dept_id` FOREIGN KEY (`from_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_to_dept_id` FOREIGN KEY (`to_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_audit_by` FOREIGN KEY (`audit_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_service_message` ADD CONSTRAINT `fk_biz_service_message_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_service_message` ADD CONSTRAINT `fk_biz_service_message_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_service_ticket_log` ADD CONSTRAINT `fk_biz_service_ticket_log_message_id` FOREIGN KEY (`message_id`) REFERENCES `biz_service_message` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_dispatch_id` FOREIGN KEY (`dispatch_id`) REFERENCES `biz_survey_dispatch` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_fill_employee_id` FOREIGN KEY (`fill_employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_dispute_case_id` FOREIGN KEY (`dispute_case_id`) REFERENCES `biz_dispute_case` (`id`);
ALTER TABLE `biz_survey_answer_item` ADD CONSTRAINT `fk_biz_survey_answer_item_answer_id` FOREIGN KEY (`answer_id`) REFERENCES `biz_survey_answer` (`id`);
ALTER TABLE `biz_survey_answer_item` ADD CONSTRAINT `fk_biz_survey_answer_item_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_survey_item` (`id`);
ALTER TABLE `biz_survey_answer_item` ADD CONSTRAINT `fk_biz_survey_answer_item_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_answer_id` FOREIGN KEY (`answer_id`) REFERENCES `biz_survey_answer` (`id`);
ALTER TABLE `biz_survey_item` ADD CONSTRAINT `fk_biz_survey_item_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_knowledge_chunk` ADD CONSTRAINT `fk_sys_knowledge_chunk_doc_id` FOREIGN KEY (`doc_id`) REFERENCES `sys_knowledge_doc` (`id`);
ALTER TABLE `sys_message` ADD CONSTRAINT `fk_sys_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_service_trace` ADD CONSTRAINT `fk_sys_service_trace_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_service_trace` ADD CONSTRAINT `fk_sys_service_trace_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `sys_service_trace` ADD CONSTRAINT `fk_sys_service_trace_faq_id` FOREIGN KEY (`faq_id`) REFERENCES `sys_faq` (`id`);
ALTER TABLE `sys_service_trace` ADD CONSTRAINT `fk_sys_service_trace_ref_id` FOREIGN KEY (`ref_id`) REFERENCES `biz_service_message` (`id`);
ALTER TABLE `sys_workbench_layout` ADD CONSTRAINT `fk_sys_workbench_layout_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_workbench_role` ADD CONSTRAINT `fk_sys_workbench_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`);
ALTER TABLE `sys_workbench_role` ADD CONSTRAINT `fk_sys_workbench_role_widget_id` FOREIGN KEY (`widget_id`) REFERENCES `sys_workbench_widget` (`id`);
