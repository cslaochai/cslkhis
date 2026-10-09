-- 领域：23-运营与绩效（成本·绩效）
-- 库：hn_biz_his    表数：2
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_dept_cost_month  科室月度成本
-- ----------------------------
CREATE TABLE `biz_dept_cost_month`
(
    `id`            bigint         NOT NULL COMMENT '主键ID',
    `dept_id`       bigint         NOT NULL COMMENT '科室ID',
    `dept_name`     varchar(100)   NOT NULL COMMENT '科室名称（快照）',
    `cost_month`    char(7)        NOT NULL COMMENT '核算月份',
    `labor_cost`    decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '人力成本（元）',
    `drug_cost`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '药品成本（元）',
    `material_cost` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '耗材成本（元）',
    `depreciation`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '设备折旧（元）',
    `other_cost`    decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '其他成本（元）',
    `total_cost`    decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '成本合计',
    `create_by`     varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`  bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`  bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`        varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_month` (`dept_id`,`cost_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='科室月度成本';

-- ----------------------------
-- biz_perf_result  科室绩效核算结果
-- ----------------------------
CREATE TABLE `biz_perf_result`
(
    `id`           bigint         NOT NULL COMMENT '主键ID',
    `dept_id`      bigint         NOT NULL COMMENT '科室ID',
    `dept_name`    varchar(100)   NOT NULL COMMENT '科室名称（快照）',
    `cost_month`   char(7)        NOT NULL COMMENT '核算月份',
    `revenue`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '科室收入',
    `drug_revenue` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '药品收入',
    `drug_ratio`   decimal(6, 4)           DEFAULT NULL COMMENT '药占比',
    `total_cost`   decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '成本合计',
    `surplus`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '结余 = 收入 - 成本',
    `bonus_rate`   decimal(6, 4)  NOT NULL DEFAULT '0.0600' COMMENT '提成系数',
    `perf_amount`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '绩效金额 = max',
    `perf_status`  tinyint        NOT NULL DEFAULT '1' COMMENT '状态（1-草稿 2-已核算 3-已发布）',
    `cost_id`      bigint                  DEFAULT NULL COMMENT '成本快照来源',
    `create_by`    varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id` bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id` bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_month` (`dept_id`,`cost_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='科室绩效核算结果';
