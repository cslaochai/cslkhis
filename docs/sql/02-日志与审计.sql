-- 领域：02-日志与审计
-- 库：hn_biz_his    表数：5
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- sys_oper_log  操作日志
-- ----------------------------
CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(100) DEFAULT NULL COMMENT '操作模块',
  `business_type` tinyint DEFAULT '0' COMMENT '业务类型（0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空）',
  `method` varchar(200) DEFAULT NULL COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方式（GET/POST/PUT/DELETE）',
  `oper_name` varchar(50) DEFAULT NULL COMMENT '操作人员',
  `oper_id` bigint DEFAULT NULL COMMENT '操作人员ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '部门名称',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `oper_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `oper_ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `oper_location` varchar(200) DEFAULT NULL COMMENT '操作地点',
  `oper_param` text COMMENT '请求参数',
  `json_result` text COMMENT '返回参数',
  `status` tinyint DEFAULT '0' COMMENT '操作状态（0-正常 1-异常）',
  `error_msg` text COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间（毫秒）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_oper_time` (`oper_time`),
  KEY `idx_oper_id` (`oper_id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_status` (`status`),
  KEY `idx_oper_time_status` (`oper_time`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';

-- ----------------------------
-- sys_login_log  登录日志
-- ----------------------------
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `login_location` varchar(200) DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `login_status` tinyint DEFAULT '0' COMMENT '登录状态（0-成功 1-失败）',
  `msg` varchar(200) DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_name` (`user_name`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_login_status` (`login_status`),
  KEY `idx_login_time_status` (`login_time`,`login_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志';

-- ----------------------------
-- sys_audit_log  审计日志
-- ----------------------------
CREATE TABLE `sys_audit_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `module` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `operation` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `target_id` varchar(50) DEFAULT NULL COMMENT '操作对象ID',
  `target_type` varchar(50) DEFAULT NULL COMMENT '操作对象类型',
  `content` text COMMENT '操作内容',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `status` tinyint DEFAULT '1' COMMENT '状态（1-成功 0-失败）',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_audit_time_status` (`create_time`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审计日志';

-- ----------------------------
-- sys_ai_call_log  AI 调用日志
-- ----------------------------
CREATE TABLE `sys_ai_call_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `capability_key` varchar(64) NOT NULL COMMENT '能力标识',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '业务ID',
  `provider` varchar(32) DEFAULT NULL COMMENT '提供方标识',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名',
  `prompt_version` varchar(32) DEFAULT NULL COMMENT '提示词版本号',
  `input_digest` varchar(512) DEFAULT NULL COMMENT '输入摘要',
  `output_digest` varchar(512) DEFAULT NULL COMMENT '输出摘要',
  `prompt_tokens` int DEFAULT NULL COMMENT '输入 token 数',
  `completion_tokens` int DEFAULT NULL COMMENT '输出 token 数',
  `latency_ms` int DEFAULT NULL COMMENT '调用耗时（毫秒）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1-成功 2-失败 3-超时 4-降级 5-熔断（1-成功 2-失败 3-超时 4-降级 5-熔断）',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `operator` varchar(64) DEFAULT NULL COMMENT '调用人',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '调用时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_capability_time` (`capability_key`,`create_time`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 调用日志';

-- ----------------------------
-- biz_tsa_token  时间戳令牌台账
-- ----------------------------
CREATE TABLE `biz_tsa_token` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `serial` varchar(64) NOT NULL COMMENT '令牌序列号',
  `digest_hex` char(64) NOT NULL COMMENT '被盖时间戳的内容摘要',
  `tsa_time` datetime NOT NULL COMMENT 'TSA 授时时刻',
  `token_value` text NOT NULL COMMENT '令牌值',
  `algo` varchar(32) NOT NULL DEFAULT 'SHA256withRSA' COMMENT '令牌签名算法',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tsa_serial` (`serial`),
  KEY `idx_tsa_token_time` (`tsa_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='时间戳令牌台账';
