-- 领域：01-系统基础（组织·用户·岗位·权限·参数·字典）
-- 库：hn_biz_his    表数：17
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- sys_department  科室
-- ----------------------------
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_code` varchar(32) NOT NULL COMMENT '科室编码（唯一）',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `dept_type` varchar(20) NOT NULL COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父科室ID',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `dept_icon` varchar(200) DEFAULT NULL COMMENT '科室图标',
  `dept_desc` varchar(500) DEFAULT NULL COMMENT '科室描述',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `location` varchar(200) DEFAULT NULL COMMENT '科室位置',
  `dept_leader_id` bigint DEFAULT NULL COMMENT '科室负责人（sys_employee.id)',
  `is_open` tinyint DEFAULT '1' COMMENT '是否开诊（0-否 1-是）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_dept_type` (`dept_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='科室';

-- ----------------------------
-- sys_role  角色
-- ----------------------------
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码（唯一）',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_type` tinyint DEFAULT '1' COMMENT '角色类型（1-系统角色 2-自定义角色）',
  `staff_type` tinyint DEFAULT NULL COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `data_scope` tinyint DEFAULT '1' COMMENT '数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据）',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_role_type` (`role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

-- ----------------------------
-- sys_menu  菜单
-- ----------------------------
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `menu_type` tinyint DEFAULT '1' COMMENT '菜单类型（1-目录 2-菜单 3-按钮）',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `menu_key` varchar(100) DEFAULT NULL COMMENT '菜单标识（唯一）',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `permission` varchar(200) DEFAULT NULL COMMENT '权限标识',
  `is_frame` tinyint DEFAULT '0' COMMENT '是否外链（0-否 1-是）',
  `is_cache` tinyint DEFAULT '0' COMMENT '是否缓存（0-否 1-是）',
  `is_visible` tinyint DEFAULT '1' COMMENT '是否可见（0-隐藏 1-显示）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_key` (`menu_key`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单';

-- ----------------------------
-- sys_role_menu  角色菜单关联
-- ----------------------------
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联';

-- ----------------------------
-- sys_user  用户
-- ----------------------------
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_name` varchar(64) NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(200) NOT NULL COMMENT '密码',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `emp_id` bigint DEFAULT NULL COMMENT '关联员工ID',
  `user_type` tinyint DEFAULT '1' COMMENT '用户类型（1-系统用户 2-外部用户）',
  `patient_id` bigint DEFAULT NULL COMMENT '关联患者ID',
  `openid` varchar(64) DEFAULT NULL COMMENT '微信openid',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像地址',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_count` int DEFAULT '0' COMMENT '登录次数',
  `password_update_time` datetime DEFAULT NULL COMMENT '密码更新时间',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用 2-锁定）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_name`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_emp_id` (`emp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户';

-- ----------------------------
-- sys_employee  员工
-- ----------------------------
CREATE TABLE `sys_employee` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `emp_code` varchar(32) NOT NULL COMMENT '员工编号（唯一）',
  `emp_name` varchar(50) NOT NULL COMMENT '员工姓名',
  `emp_type` tinyint NOT NULL DEFAULT '1' COMMENT '员工类型（1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他）',
  `gender` tinyint DEFAULT '1' COMMENT '性别',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(200) DEFAULT NULL COMMENT '科室ID',
  `title` varchar(50) DEFAULT NULL COMMENT '职称',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `specialty` varchar(200) DEFAULT NULL COMMENT '专业特长',
  `education` varchar(50) DEFAULT NULL COMMENT '学历',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像地址',
  `is_expert` tinyint DEFAULT '0' COMMENT '是否专家（0-否 1-是）',
  `expert_price` decimal(10,2) DEFAULT '0.00' COMMENT '专家号价格',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_code` (`emp_code`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_emp_type` (`emp_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工';

-- ----------------------------
-- sys_employee_post  员工岗位（角色×科室）
-- ----------------------------
CREATE TABLE `sys_employee_post` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `is_primary` tinyint DEFAULT '0' COMMENT '是否主科室（0-否 1-是）',
  `effective_date` date DEFAULT NULL COMMENT '岗位生效日期',
  `expire_date` date DEFAULT NULL COMMENT '岗位失效日期',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_role_dept` (`employee_id`,`role_id`,`dept_id`),
  KEY `idx_user_id` (`employee_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工岗位（角色×科室）';

-- ----------------------------
-- sys_employee_qualification  员工资格证书
-- ----------------------------
CREATE TABLE `sys_employee_qualification` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `cert_type` varchar(8) NOT NULL COMMENT '证书类型（2-医师执业证 3-护士执业证 4-药师资格证 5-技术职称聘书 9-其他）',
  `cert_no` varchar(64) NOT NULL COMMENT '证书编号',
  `issue_org` varchar(8) DEFAULT NULL COMMENT '发证机关',
  `issue_date` date DEFAULT NULL COMMENT '发证日期',
  `valid_until` date DEFAULT NULL COMMENT '有效期至',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_cert_type_no` (`employee_id`,`cert_type`,`cert_no`) COMMENT '同一人同类型证书编号唯一；本表删除走物理删，软删会占键',
  KEY `idx_valid_until` (`valid_until`) COMMENT '后续资质到期扫描用（到期提醒清单按有效期筛）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工资格证书';

-- ----------------------------
-- sys_employee_tech_auth  医疗技术授权台账
-- ----------------------------
CREATE TABLE `sys_employee_tech_auth` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_name` varchar(50) NOT NULL COMMENT '员工姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '所属科室ID',
  `dept_name` varchar(200) DEFAULT NULL COMMENT '所属科室名称（快照）',
  `title` varchar(50) DEFAULT NULL COMMENT '职称',
  `auth_category` tinyint NOT NULL COMMENT '授权类别（1-手术 2-麻醉 3-内镜与介入）',
  `tech_level` tinyint NOT NULL COMMENT '可独立操作的手术级别上限',
  `item_scope` varchar(500) DEFAULT NULL COMMENT '限定术式编码白名单',
  `auth_type` tinyint NOT NULL DEFAULT '1' COMMENT '授权方式（1-独立授权 2-上级指导下 3-限制授权须上级在场）',
  `auth_basis` varchar(200) DEFAULT NULL COMMENT '授权依据（技术准入评价/培训考核/累计手术量，评审要看依据）',
  `valid_from` date NOT NULL COMMENT '授权生效日期',
  `valid_until` date DEFAULT NULL COMMENT '授权有效期至',
  `auth_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待审批 2-已授权 3-已驳回 4-已收回）',
  `apply_by` varchar(64) DEFAULT NULL COMMENT '申请（登记）',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人',
  `approver_name` varchar(50) DEFAULT NULL COMMENT '审批人姓名',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `revoke_by` varchar(64) DEFAULT NULL COMMENT '收回人',
  `revoke_time` datetime DEFAULT NULL COMMENT '收回时间',
  `revoke_reason` varchar(500) DEFAULT NULL COMMENT '收回原因',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_cat_from` (`employee_id`,`auth_category`,`valid_from`) COMMENT '同一人同类别同生效日只一条；本表删除走物理删，软删会占键',
  KEY `idx_status_valid` (`auth_status`,`valid_until`) COMMENT '闸门按「已授权 + 日期覆盖」捞人，必须走索引',
  KEY `idx_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医疗技术授权台账';

-- ----------------------------
-- biz_tech_auth_override  越权授权事后登记
-- ----------------------------
CREATE TABLE `biz_tech_auth_override` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `source_type` tinyint NOT NULL COMMENT '来源单据类型（1-手术申请 2-日间手术 3-住院医嘱 4-内镜记录）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `source_no` varchar(64) DEFAULT NULL COMMENT '来源单据号',
  `employee_id` bigint NOT NULL COMMENT '越权操作者（员工ID）',
  `employee_name` varchar(50) NOT NULL COMMENT '越权操作者姓名（快照）',
  `auth_category` tinyint NOT NULL COMMENT '涉及授权类别',
  `required_level` tinyint NOT NULL COMMENT '该操作要求的级别',
  `held_level` tinyint DEFAULT NULL COMMENT '越权者当时的授权级别上限',
  `reason` varchar(500) NOT NULL COMMENT '越权原因',
  `occur_time` datetime NOT NULL COMMENT '越权发生时间',
  `override_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-待上级确认 2-已确认）',
  `supervisor_id` bigint DEFAULT NULL COMMENT '上级确认人（员工ID）',
  `supervisor_name` varchar(50) DEFAULT NULL COMMENT '上级确认人姓名',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `confirm_opinion` varchar(500) DEFAULT NULL COMMENT '确认意见',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_source` (`source_type`,`source_id`) COMMENT '按单据捞越权记录：一张手术单有几笔越权要能一次看全',
  KEY `idx_emp` (`employee_id`,`occur_time`) COMMENT '抽查某人越权频次（反复越权=准入或排班有问题）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='越权授权事后登记';

-- ----------------------------
-- sys_config  系统参数
-- ----------------------------
CREATE TABLE `sys_config` (
  `config_id` bigint NOT NULL COMMENT '配置ID',
  `config_name` varchar(64) NOT NULL COMMENT '配置名称',
  `config_key` varchar(64) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) DEFAULT NULL COMMENT '配置值',
  `config_type` tinyint NOT NULL DEFAULT '0' COMMENT '类型（0-系统 1-业务）',
  `is_system` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统内置（0-否 1-是）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数';

-- ----------------------------
-- sys_dict_type  字典类型
-- ----------------------------
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型（唯一）',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `dict_source` tinyint DEFAULT '2' COMMENT '来源（1-系统级 2-自定义）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型';

-- ----------------------------
-- sys_dict_data  字典数据
-- ----------------------------
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
  `dict_label` varchar(100) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(100) NOT NULL COMMENT '字典值',
  `dict_sort` int DEFAULT '0' COMMENT '排序号',
  `dict_class` varchar(100) DEFAULT NULL COMMENT '样式属性',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认（0-否 1-是）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `dict_source` tinyint DEFAULT '2' COMMENT '来源（1-系统级 2-自定义）',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据';

-- ----------------------------
-- sys_attachment  附件
-- ----------------------------
CREATE TABLE `sys_attachment` (
  `attachment_id` bigint NOT NULL COMMENT '附件ID',
  `attachment_name` varchar(256) NOT NULL COMMENT '附件名称',
  `attachment_type` varchar(32) DEFAULT NULL COMMENT '附件类型',
  `file_path` varchar(512) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint DEFAULT '0' COMMENT '文件大小(字节)',
  `file_md5` varchar(32) DEFAULT NULL COMMENT '文件MD5校验值',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传人ID',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`attachment_id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='附件';

-- ----------------------------
-- sys_sign_cert  电子签名证书
-- ----------------------------
CREATE TABLE `sys_sign_cert` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `cert_no` varchar(32) NOT NULL COMMENT '证书编号',
  `emp_id` bigint NOT NULL COMMENT '签名人员工ID',
  `emp_name` varchar(64) NOT NULL COMMENT '签名人姓名（快照）',
  `dept_id` bigint DEFAULT NULL COMMENT '所属科室ID（快照）',
  `dept_name` varchar(64) DEFAULT NULL COMMENT '所属科室名称（快照）',
  `key_algo` varchar(16) NOT NULL DEFAULT 'RSA2048' COMMENT '密钥算法',
  `digest_algo` varchar(16) NOT NULL DEFAULT 'SHA256' COMMENT '摘要算法',
  `sign_algo` varchar(32) NOT NULL DEFAULT 'SHA256withRSA' COMMENT '签名算法',
  `public_key` text NOT NULL COMMENT '公钥',
  `key_fingerprint` varchar(64) NOT NULL COMMENT '公钥指纹',
  `protected_private_key` text NOT NULL COMMENT '私钥密文',
  `key_salt` varchar(64) NOT NULL COMMENT '私钥派生盐',
  `key_iterations` int NOT NULL COMMENT '私钥派生迭代次数',
  `issued_mode` tinyint NOT NULL DEFAULT '1' COMMENT '签发方式（1-人工签发 2-系统自动签发）',
  `cert_status` tinyint NOT NULL DEFAULT '1' COMMENT '证书状态（1-有效 2-已吊销）',
  `valid_from` datetime NOT NULL COMMENT '生效时间',
  `valid_to` datetime NOT NULL COMMENT '失效时间',
  `revoke_reason` varchar(200) DEFAULT NULL COMMENT '吊销原因',
  `revoke_time` datetime DEFAULT NULL COMMENT '吊销时间',
  `revoke_by` bigint DEFAULT NULL COMMENT '吊销操作人员工ID',
  `revoke_by_name` varchar(64) DEFAULT NULL COMMENT '吊销操作人姓名',
  `last_used_time` datetime DEFAULT NULL COMMENT '最近一次使用时间',
  `sign_count` int NOT NULL DEFAULT '0' COMMENT '累计签名次数',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_no` (`cert_no`),
  KEY `idx_sigcert_emp` (`emp_id`,`cert_status`),
  KEY `idx_sigcert_status` (`cert_status`,`valid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电子签名证书';

-- ----------------------------
-- sys_tsa_server  时间戳服务注册
-- ----------------------------
CREATE TABLE `sys_tsa_server` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tsa_code` varchar(32) NOT NULL COMMENT 'TSA服务编码',
  `tsa_name` varchar(64) NOT NULL COMMENT 'TSA服务名称',
  `public_pem` text NOT NULL COMMENT 'TSA公钥',
  `key_fingerprint` varchar(64) NOT NULL COMMENT '公钥指纹',
  `protected_private_key` text NOT NULL COMMENT '私钥密文',
  `key_salt` varchar(64) NOT NULL COMMENT '私钥派生盐',
  `key_iterations` int NOT NULL COMMENT '私钥派生迭代次数',
  `tsa_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tsa_code` (`tsa_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='时间戳服务注册';

-- ----------------------------
-- sys_field_change_log  字段级修改日志
-- ----------------------------
CREATE TABLE `sys_field_change_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `biz_type` varchar(32) NOT NULL COMMENT '对象类型',
  `biz_id` varchar(64) NOT NULL COMMENT '对象ID',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '对象编号快照（患者号/工号/病历号）',
  `biz_name` varchar(128) DEFAULT NULL COMMENT '对象名称快照',
  `field_name` varchar(64) NOT NULL COMMENT '字段英文名',
  `field_label` varchar(64) NOT NULL COMMENT '字段中文名',
  `old_value` varchar(500) DEFAULT NULL COMMENT '变更前值',
  `new_value` varchar(500) DEFAULT NULL COMMENT '变更后值',
  `change_type` varchar(16) NOT NULL DEFAULT 'UPDATE' COMMENT '变更类型',
  `batch_no` varchar(48) NOT NULL COMMENT '批次号',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '操作人科室ID',
  `dept_name` varchar(64) DEFAULT NULL COMMENT '操作人科室名称',
  `change_time` datetime NOT NULL COMMENT '变更时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) NOT NULL DEFAULT '',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_fc_time` (`change_time`,`biz_type`),
  KEY `idx_fc_biz` (`biz_type`,`biz_id`),
  KEY `idx_fc_batch` (`batch_no`),
  KEY `idx_fc_operator` (`operator_name`,`change_time`),
  KEY `idx_fc_field` (`biz_type`,`field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字段级修改日志';
