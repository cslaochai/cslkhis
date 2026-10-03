-- ============================================================
-- 领域 02 日志与审计（本域 5 表 + 上游参照 2 表 / 4 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


-- sys_oper_log  操作日志
CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(100) COMMENT '操作模块',
  `business_type` tinyint DEFAULT 0 COMMENT '业务类型（0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空）',
  `method` varchar(200) COMMENT '方法名称',
  `request_method` varchar(10) COMMENT '请求方式（GET/POST/PUT/DELETE）',
  `oper_name` varchar(50) COMMENT '操作人员',
  `oper_id` bigint COMMENT '操作人员ID',
  `dept_name` varchar(100) COMMENT '部门名称',
  `dept_id` bigint COMMENT '部门ID',
  `oper_url` varchar(500) COMMENT '请求URL',
  `oper_ip` varchar(50) COMMENT '操作IP',
  `oper_location` varchar(200) COMMENT '操作地点',
  `oper_param` text COMMENT '请求参数',
  `json_result` text COMMENT '返回参数',
  `status` tinyint DEFAULT 0 COMMENT '操作状态（0-正常 1-异常）',
  `error_msg` text COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` bigint DEFAULT 0 COMMENT '消耗时间（毫秒）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- sys_login_log  登录日志
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_name` varchar(64) COMMENT '用户名',
  `user_id` bigint COMMENT '用户ID',
  `real_name` varchar(64) COMMENT '真实姓名',
  `login_ip` varchar(50) COMMENT '登录IP',
  `login_location` varchar(200) COMMENT '登录地点',
  `browser` varchar(100) COMMENT '浏览器类型',
  `os` varchar(100) COMMENT '操作系统',
  `user_agent` varchar(500) COMMENT '用户代理',
  `login_status` tinyint DEFAULT 0 COMMENT '登录状态（0-成功 1-失败）',
  `msg` varchar(200) COMMENT '提示消息',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- sys_audit_log  审计日志
CREATE TABLE `sys_audit_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint COMMENT '操作人ID',
  `user_name` varchar(64) COMMENT '操作人姓名',
  `module` varchar(50) COMMENT '模块名称',
  `operation` varchar(50) COMMENT '操作类型',
  `target_id` varchar(50) COMMENT '操作对象ID',
  `target_type` varchar(50) COMMENT '操作对象类型',
  `content` text COMMENT '操作内容',
  `ip` varchar(50) COMMENT 'IP地址',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-成功 0-失败）',
  `error_msg` varchar(500) COMMENT '错误信息',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

-- sys_ai_call_log  AI 调用日志
CREATE TABLE `sys_ai_call_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `capability_key` varchar(64) NOT NULL COMMENT '能力标识',
  `biz_type` varchar(32) COMMENT '业务类型',
  `biz_id` bigint COMMENT '业务ID',
  `provider` varchar(32) COMMENT '提供方标识',
  `model` varchar(64) COMMENT '模型名',
  `prompt_version` varchar(32) COMMENT '提示词版本号',
  `input_digest` varchar(512) COMMENT '输入摘要',
  `output_digest` varchar(512) COMMENT '输出摘要',
  `prompt_tokens` int COMMENT '输入 token 数',
  `completion_tokens` int COMMENT '输出 token 数',
  `latency_ms` int COMMENT '调用耗时（毫秒）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-成功 2-失败 3-超时 4-降级 5-熔断（1-成功 2-失败 3-超时 4-降级 5-熔断）',
  `error_msg` varchar(500) COMMENT '失败原因',
  `operator` varchar(64) COMMENT '调用人',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '调用时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志';

-- biz_tsa_token  时间戳令牌台账
CREATE TABLE `biz_tsa_token` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `serial` varchar(64) NOT NULL COMMENT '令牌序列号',
  `digest_hex` char(64) NOT NULL COMMENT '被盖时间戳的内容摘要',
  `tsa_time` datetime NOT NULL COMMENT 'TSA 授时时刻',
  `token_value` text NOT NULL COMMENT '令牌值',
  `algo` varchar(32) NOT NULL DEFAULT 'SHA256withRSA' COMMENT '令牌签名算法',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tsa_serial` (`serial`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间戳令牌台账';

-- sys_department  科室
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_code` varchar(32) NOT NULL COMMENT '科室编码（唯一）',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `dept_type` tinyint NOT NULL DEFAULT 1 COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父科室ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `dept_icon` varchar(200) COMMENT '科室图标',
  `dept_desc` varchar(500) COMMENT '科室描述',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `location` varchar(200) COMMENT '科室位置',
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
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ---------------- 参照关系（E-R 连线） ----------------
ALTER TABLE `sys_audit_log` ADD CONSTRAINT `fk_sys_audit_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_login_log` ADD CONSTRAINT `fk_sys_login_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_oper_log` ADD CONSTRAINT `fk_sys_oper_log_oper_id` FOREIGN KEY (`oper_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_oper_log` ADD CONSTRAINT `fk_sys_oper_log_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
