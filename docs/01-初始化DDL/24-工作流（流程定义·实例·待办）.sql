-- 领域：24-工作流（流程定义·实例·待办）
-- 库：hn_biz_his    表数：6
-- 说明：DDL 快照（dev 库 SHOW CREATE TABLE 于 2026-11-20 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。
-- 口径：姓名/名称快照列与操作人列（create_by·update_by·*_by·*_name·name）不留 DEFAULT，漏填直接报错；其余 NOT NULL 列保留库上的 DEFAULT。

-- ----------------------------
-- biz_flow_his_task  流程流转轨迹
-- ----------------------------
CREATE TABLE `biz_flow_his_task` (
  `id` bigint NOT NULL COMMENT '主键',
  `definition_id` bigint NOT NULL COMMENT '流程定义ID',
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `task_id` bigint COMMENT '待办ID',
  `business_id` varchar(64) COMMENT '业务单据ID',
  `flow_code` varchar(64) COMMENT '流程编码',
  `node_code` varchar(64) COMMENT '节点编码',
  `node_name` varchar(128) COMMENT '节点名称',
  `node_type` tinyint COMMENT '节点类型',
  `action` varchar(16) NOT NULL COMMENT '动作（START/PASS/REJECT/CANCELED/TASK/AUTO/APPROVED/REJECTED）',
  `approver_id` bigint COMMENT '操作人ID',
  `approver_name` varchar(64) COMMENT '操作人姓名',
  `message` varchar(255) COMMENT '意见/摘要',
  `create_time` datetime COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_instance` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程流转轨迹';

-- ----------------------------
-- biz_flow_instance  流程实例
-- ----------------------------
CREATE TABLE `biz_flow_instance` (
  `id` bigint NOT NULL COMMENT '主键',
  `definition_id` bigint NOT NULL COMMENT '流程定义ID',
  `flow_code` varchar(64) NOT NULL COMMENT '流程编码',
  `flow_name` varchar(128) COMMENT '流程名称',
  `business_id` varchar(64) COMMENT '业务单据ID',
  `biz_type` varchar(64) COMMENT '业务类型',
  `started_by_user_id` bigint COMMENT '发起人ID',
  `started_by_name` varchar(64) COMMENT '发起人姓名',
  `node_code` varchar(64) COMMENT '当前节点编码',
  `node_name` varchar(128) COMMENT '当前节点名称',
  `node_type` tinyint COMMENT '当前节点类型',
  `flow_status` varchar(16) NOT NULL COMMENT '实例状态（RUNNING/APPROVED/REJECTED/CANCELED）',
  `activity_status` tinyint NOT NULL DEFAULT '1' COMMENT '活动标记（1-活动 0-已结束）',
  `variable_json` mediumtext COMMENT '流程变量JSON',
  `create_time` datetime COMMENT '创建时间',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business_id`),
  KEY `idx_status` (`flow_status`,`activity_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程实例';

-- ----------------------------
-- biz_flow_task  流程待办
-- ----------------------------
CREATE TABLE `biz_flow_task` (
  `id` bigint NOT NULL COMMENT '主键',
  `definition_id` bigint NOT NULL COMMENT '流程定义ID',
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `flow_code` varchar(64) COMMENT '流程编码',
  `flow_name` varchar(128) COMMENT '流程名称',
  `business_id` varchar(64) COMMENT '业务单据ID',
  `node_code` varchar(64) NOT NULL COMMENT '节点编码',
  `node_name` varchar(128) COMMENT '节点名称',
  `node_type` tinyint COMMENT '节点类型',
  `task_status` varchar(16) NOT NULL COMMENT '待办状态（PENDING/PASS/REJECT/CANCELED）',
  `assignee_expr` varchar(255) COMMENT '候选人表达式快照',
  `assignee_user_id` bigint COMMENT '实际处理人ID',
  `assignee_name` varchar(64) COMMENT '实际处理人姓名',
  `create_time` datetime COMMENT '创建时间',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_instance_node` (`instance_id`,`node_code`),
  KEY `idx_status` (`task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程待办';

-- ----------------------------
-- sys_flow_definition  流程定义
-- ----------------------------
CREATE TABLE `sys_flow_definition` (
  `id` bigint NOT NULL COMMENT '主键',
  `flow_code` varchar(64) NOT NULL COMMENT '流程编码',
  `flow_name` varchar(128) NOT NULL COMMENT '流程名称',
  `flow_desc` varchar(500) COMMENT '流程说明（意图路由给模型看的边界描述）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-草稿 1-已发布 2-已停用）',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号（每次发布递增）',
  `model_json` text COMMENT '整图快照 JSON（仅回看，不参与执行）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_by_id` bigint COMMENT '创建人ID',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_by_id` bigint COMMENT '更新人ID',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_code` (`flow_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程定义';

-- ----------------------------
-- sys_flow_node  流程节点
-- ----------------------------
CREATE TABLE `sys_flow_node` (
  `id` bigint NOT NULL COMMENT '主键',
  `definition_id` bigint NOT NULL COMMENT '流程定义ID',
  `node_code` varchar(64) NOT NULL COMMENT '节点编码',
  `node_name` varchar(128) NOT NULL COMMENT '节点名称',
  `node_type` tinyint NOT NULL COMMENT '节点类型（0-开始 1-审批 2-结束 3-自动 4-网关）',
  `permission_flag` varchar(255) COMMENT '审批人表达式（role:XXX@@user:1001）',
  `node_ratio` varchar(16) COMMENT '签核比例（0-或签 100-会签 n-票签n%）',
  `ai_action` varchar(32) COMMENT 'AI动作（CAPABILITY-能力调用 ROUTE-意图路由）',
  `ai_capability_key` varchar(64) COMMENT '能力标识（空则用 flow_ai_node）',
  `ai_template_name` varchar(128) COMMENT '提示词模板编码',
  `ai_input_json` text COMMENT '节点固定输入 JSON',
  `coordinate` varchar(64) COMMENT '画布坐标',
  `sort_no` int COMMENT '展示顺序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_def_node` (`definition_id`,`node_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程节点';

-- ----------------------------
-- sys_flow_skip  流程流转线
-- ----------------------------
CREATE TABLE `sys_flow_skip` (
  `id` bigint NOT NULL COMMENT '主键',
  `definition_id` bigint NOT NULL COMMENT '流程定义ID',
  `now_node_code` varchar(64) NOT NULL COMMENT '起点节点编码',
  `next_node_code` varchar(64) NOT NULL COMMENT '目标节点编码',
  `skip_type` varchar(16) NOT NULL COMMENT '流转线类型（PASS-正常 COND-条件 REJECT-驳回）',
  `condition_expr` varchar(500) COMMENT '条件表达式（仅COND；DEFAULT=兜底）',
  `sort_no` int COMMENT '同层匹配顺序',
  PRIMARY KEY (`id`),
  KEY `idx_def_now` (`definition_id`,`now_node_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程流转线';
