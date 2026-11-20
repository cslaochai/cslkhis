-- ============================================================
-- 领域 23 运营与绩效（成本·绩效）（本域 2 表 + 上游参照 1 表 / 5 条关系）
-- 由 docs/01-初始化DDL 的结构基线反向生成，只用于建模，禁止在业务库执行。
-- ============================================================


-- biz_dept_cost_month  科室月度成本
CREATE TABLE `biz_dept_cost_month` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称（快照）',
  `cost_month` char(7) NOT NULL COMMENT '核算月份',
  `labor_cost` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '人力成本（元）',
  `drug_cost` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '药品成本（元）',
  `material_cost` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '耗材成本（元）',
  `depreciation` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '设备折旧（元）',
  `other_cost` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '其他成本（元）',
  `total_cost` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '成本合计',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_dept_cost_month_dept_id_cost_month` (`dept_id`, `cost_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室月度成本';

-- biz_perf_result  科室绩效核算结果
CREATE TABLE `biz_perf_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称（快照）',
  `cost_month` char(7) NOT NULL COMMENT '核算月份',
  `revenue` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '科室收入',
  `drug_revenue` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '药品收入',
  `drug_ratio` decimal(6,4) COMMENT '药占比',
  `total_cost` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '成本合计',
  `surplus` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '结余 = 收入 - 成本',
  `bonus_rate` decimal(6,4) NOT NULL DEFAULT '0.0600' COMMENT '提成系数',
  `perf_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '绩效金额 = max',
  `perf_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-草稿 2-已核算 3-已发布）',
  `cost_id` bigint COMMENT '成本快照来源',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_perf_result_dept_id_cost_month` (`dept_id`, `cost_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室绩效核算结果';

-- sys_department  科室
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_code` varchar(32) NOT NULL COMMENT '科室编码（唯一）',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `dept_type` varchar(20) NOT NULL DEFAULT '' COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父科室ID',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `dept_icon` varchar(200) COMMENT '科室图标',
  `dept_desc` varchar(500) COMMENT '科室描述',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `location` varchar(200) COMMENT '科室位置',
  `dept_leader_id` bigint COMMENT '科室负责人（sys_employee.id)',
  `is_open` tinyint COMMENT '是否开诊（0-否 1-是）',
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  `outpatient_auditor_id` bigint COMMENT '门诊排班审核人 sys_employee.id',
  `nursing_auditor_id` bigint COMMENT '护理排班审核人 sys_employee.id',
  `schedule_auditor_id` bigint COMMENT '全院岗位排班审核人（sys_employee.id）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_department_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室';

ALTER TABLE `biz_dept_cost_month`
    ADD CONSTRAINT `fk_biz_dept_cost_month_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_perf_result`
    ADD CONSTRAINT `fk_biz_perf_result_cost_id` FOREIGN KEY (`cost_id`) REFERENCES `biz_dept_cost_month` (`id`);
ALTER TABLE `biz_perf_result`
    ADD CONSTRAINT `fk_biz_perf_result_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_department`
    ADD CONSTRAINT `fk_sys_department_dept_leader_id` FOREIGN KEY (`dept_leader_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_department`
    ADD CONSTRAINT `fk_sys_department_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `sys_department` (`id`);
