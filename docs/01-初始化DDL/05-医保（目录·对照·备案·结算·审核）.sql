-- 领域：05-医保（目录·对照·备案·结算·审核）
-- 库：hn_biz_his    表数：15
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_yb_catalog  国家医保目录
-- ----------------------------
CREATE TABLE `biz_yb_catalog`
(
    `id`              bigint       NOT NULL COMMENT '主键（雪花）',
    `catalog_type`    tinyint      NOT NULL COMMENT '目录类型（1-西药 2-中药饮片 3-医疗服务项目 4-医用耗材）',
    `yb_code`         varchar(64)  NOT NULL COMMENT '国家医保编码',
    `yb_name`         varchar(200) NOT NULL COMMENT '目录名称',
    `spec`            varchar(100) COMMENT '规格',
    `unit`            varchar(20) COMMENT '单位',
    `dosage_form`     varchar(50) COMMENT '剂型',
    `insurance_level` tinyint COMMENT '甲乙类（1-甲类 2-乙类 3-丙类）',
    `pay_ratio`       decimal(5, 2) COMMENT '支付比例%',
    `effective_date`  date COMMENT '生效日期',
    `expire_date`     date COMMENT '失效日期',
    `status`          tinyint      NOT NULL COMMENT '状态（0-停用 1-启用）',
    `create_by`       varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`    bigint COMMENT '创建人ID',
    `create_time`     datetime     NOT NULL COMMENT '创建时间',
    `update_by`       varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`    bigint COMMENT '更新人ID',
    `update_time`     datetime     NOT NULL COMMENT '更新时间',
    `del_flag`        tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_yb_code` (`yb_code`),
    KEY               `idx_type_name` (`catalog_type`,`yb_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='国家医保目录';

-- ----------------------------
-- biz_yb_mapping  医保目录对照
-- ----------------------------
CREATE TABLE `biz_yb_mapping`
(
    `id`           bigint       NOT NULL COMMENT '主键（雪花）',
    `item_type`    tinyint      NOT NULL COMMENT '院内项目类型（1-药品 2-诊疗项目 3-检验项目 4-耗材）',
    `item_id`      bigint       NOT NULL COMMENT '院内项目ID',
    `item_code`    varchar(32)  NOT NULL COMMENT '院内项目编码',
    `item_name`    varchar(200) NOT NULL COMMENT '院内项目名称',
    `catalog_id`   bigint       NOT NULL COMMENT '医保目录ID',
    `yb_code`      varchar(64)  NOT NULL COMMENT '国家医保编码',
    `yb_name`      varchar(200) NOT NULL COMMENT '目录名称',
    `match_type`   tinyint      NOT NULL COMMENT '对照方式（1-自动名称精确 2-人工 3-导入）',
    `mapped_by`    varchar(64) COMMENT '对照人',
    `mapped_time`  datetime COMMENT '对照时间',
    `create_by`    varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id` bigint COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL COMMENT '创建时间',
    `update_by`    varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id` bigint COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL COMMENT '更新时间',
    `del_flag`     tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志',
    `remark`       varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item` (`item_type`,`item_id`),
    KEY            `idx_catalog` (`catalog_id`),
    KEY            `idx_yb_code` (`yb_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保目录对照';

-- ----------------------------
-- biz_yb_chronic_catalog  门诊慢特病病种目录
-- ----------------------------
CREATE TABLE `biz_yb_chronic_catalog`
(
    `id`                   bigint       NOT NULL COMMENT '主键',
    `disease_code`         varchar(32)  NOT NULL COMMENT '病种编码',
    `disease_name`         varchar(200) NOT NULL COMMENT '病种名称',
    `disease_type`         tinyint      NOT NULL COMMENT '类别（1-慢性病 2-特殊病）',
    `icd_code`             varchar(32) COMMENT '对应 ICD-10 主码',
    `default_valid_months` int COMMENT '默认有效期月数',
    `status`               tinyint      NOT NULL COMMENT '启用状态（1-启用 0-停用）',
    `create_by`            varchar(64)  NOT NULL,
    `create_by_id`         bigint COMMENT '创建人ID',
    `create_time`          datetime     NOT NULL,
    `update_by`            varchar(64)  NOT NULL,
    `update_by_id`         bigint COMMENT '更新人ID',
    `update_time`          datetime     NOT NULL,
    `del_flag`             tinyint      NOT NULL DEFAULT '0',
    `remark`               varchar(500),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chronic_catalog_code` (`disease_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊慢特病病种目录';

-- ----------------------------
-- biz_yb_chronic_reg  门诊慢特病备案
-- ----------------------------
CREATE TABLE `biz_yb_chronic_reg`
(
    `id`                   bigint       NOT NULL COMMENT '主键',
    `reg_no`               varchar(32)  NOT NULL COMMENT '备案单号',
    `patient_id`           bigint       NOT NULL COMMENT '患者ID',
    `patient_name`         varchar(50)  NOT NULL COMMENT '患者姓名快照',
    `patient_no`           varchar(32) COMMENT '患者编号快照',
    `medical_insurance_no` varchar(32) COMMENT '医保卡号快照',
    `catalog_id`           bigint       NOT NULL COMMENT '病种目录ID',
    `disease_code`         varchar(32)  NOT NULL COMMENT '病种编码快照',
    `disease_name`         varchar(200) NOT NULL COMMENT '病种名称快照',
    `disease_type`         tinyint      NOT NULL COMMENT '病种类别快照（1-慢性 2-特殊）',
    `certify_dept_id`      bigint COMMENT '诊断科室ID',
    `certify_dept_name`    varchar(100) COMMENT '诊断科室名称',
    `certify_doctor_name`  varchar(64) COMMENT '诊断医师姓名',
    `certify_date`         date         NOT NULL COMMENT '诊断日期',
    `certify_basis`        varchar(500) NOT NULL COMMENT '诊断依据（病历摘要/检验结果/出院小结，必填）',
    `register_dept_id`     bigint COMMENT '备案经办机构ID',
    `register_dept_name`   varchar(100) COMMENT '备案经办机构名称',
    `register_emp_id`      bigint COMMENT '备案经办人ID',
    `register_emp_name`    varchar(64)  NOT NULL COMMENT '备案经办人姓名',
    `register_date`        date         NOT NULL COMMENT '备案日期',
    `valid_start`          date         NOT NULL COMMENT '待遇生效日',
    `valid_end`            date COMMENT '待遇终止日',
    `valid_end_key`        date         NOT NULL COMMENT '唯一键辅助列 = COALESCE',
    `reg_status`           tinyint      NOT NULL COMMENT '状态（1-有效 2-已注销 3-已驳回）',
    `cancel_reason`        varchar(500) COMMENT '注销原因',
    `cancel_by`            varchar(64) COMMENT '注销经办人',
    `cancel_time`          datetime COMMENT '注销时间',
    `reject_reason`        varchar(500) COMMENT '驳回原因',
    `reject_by`            varchar(64) COMMENT '驳回经办人',
    `reject_time`          datetime COMMENT '驳回时间',
    `create_by`            varchar(64)  NOT NULL,
    `create_by_id`         bigint COMMENT '创建人ID',
    `create_time`          datetime     NOT NULL,
    `update_by`            varchar(64)  NOT NULL,
    `update_by_id`         bigint COMMENT '更新人ID',
    `update_time`          datetime     NOT NULL,
    `del_flag`             tinyint      NOT NULL DEFAULT '0',
    `remark`               varchar(500),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chronic_reg_no` (`reg_no`),
    UNIQUE KEY `uk_chronic_active` (`patient_id`,`disease_code`,`reg_status`,`valid_end_key`) COMMENT '同患者同病种在有效窗口内只许一条 status=1；valid_end_key 由服务端填 COALESCE(valid_end,''9999-12-31'')',
    KEY                    `idx_chronic_patient` (`patient_id`,`reg_status`),
    KEY                    `idx_chronic_catalog` (`catalog_id`,`reg_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊慢特病备案';

-- ----------------------------
-- biz_yb_deduct_notice  医保扣款通知单
-- ----------------------------
CREATE TABLE `biz_yb_deduct_notice`
(
    `id`                   bigint         NOT NULL COMMENT '主键',
    `deduct_no`            varchar(32)    NOT NULL COMMENT '扣款单号',
    `source_type`          tinyint        NOT NULL COMMENT '来源（1-飞检现场发现 2-智能审核）',
    `inspection_id`        bigint COMMENT '关联飞检批次ID',
    `inspection_no`        varchar(32) COMMENT '飞检批次号快照',
    `settlement_id`        bigint COMMENT '关联医保结算清单ID（可选）',
    `settlement_no`        varchar(32) COMMENT '结算清单号快照',
    `encounter_type`       tinyint COMMENT '就诊类型（1-门诊 2-住院）',
    `encounter_id`         bigint COMMENT '就诊标识',
    `patient_id`           bigint COMMENT '患者ID',
    `patient_name`         varchar(50) COMMENT '患者姓名快照',
    `patient_no`           varchar(32) COMMENT '患者编号快照',
    `dept_id`              bigint COMMENT '被审科室ID',
    `dept_name`            varchar(100) COMMENT '被审科室名称快照',
    `doctor_name`          varchar(50) COMMENT '责任医师姓名快照',
    `violation_type`       tinyint        NOT NULL COMMENT '违规类型',
    `violation_desc`       varchar(500)   NOT NULL COMMENT '违规事实描述',
    `deduct_amount`        decimal(10, 2) NOT NULL COMMENT '扣款金额',
    `notice_date`          date           NOT NULL COMMENT '通知日期',
    `handle_deadline`      date           NOT NULL COMMENT '处理期限',
    `deduct_status`        tinyint        NOT NULL COMMENT '状态（1-待确认 2-申诉中 3-申诉成功 4-维持扣款待缴 5-已缴回 6-已作废）',
    `appeal_reason`        varchar(500) COMMENT '申诉理由',
    `appeal_material`      varchar(500) COMMENT '申诉材料说明',
    `appeal_by`            varchar(64) COMMENT '申诉发起人',
    `appeal_time`          datetime COMMENT '申诉时间',
    `appeal_result`        tinyint COMMENT '申诉结果（1-成功 2-驳回）',
    `appeal_result_remark` varchar(500) COMMENT '申诉结果说明',
    `appeal_result_by`     varchar(64) COMMENT '申诉结果录入人',
    `appeal_result_time`   datetime COMMENT '申诉结果录入时间',
    `liable_dept_id`       bigint COMMENT '责任科室ID',
    `liable_dept_name`     varchar(100) COMMENT '责任科室名称',
    `liable_emp_name`      varchar(64) COMMENT '责任人姓名',
    `loss_bear_type`       tinyint COMMENT '损失承担方式（1-院方承担 2-科室承担 3-个人承担 4-科室+个人共担）',
    `bear_dept_amount`     decimal(10, 2) COMMENT '科室承担金额',
    `bear_emp_amount`      decimal(10, 2) COMMENT '个人承担金额',
    `confirm_by`           varchar(64) COMMENT '确认经办人',
    `confirm_time`         datetime COMMENT '确认时间',
    `paid_amount`          decimal(10, 2) COMMENT '实际缴回金额',
    `payback_date`         date COMMENT '缴回日期',
    `payback_voucher`      varchar(100) COMMENT '缴回凭证号/转账流水',
    `payback_by`           varchar(64) COMMENT '缴回经办人',
    `payback_time`         datetime COMMENT '缴回录入时间',
    `cancel_reason`        varchar(500) COMMENT '作废原因（必填）',
    `cancel_by`            varchar(64),
    `cancel_time`          datetime,
    `create_by`            varchar(64)    NOT NULL,
    `create_by_id`         bigint COMMENT '创建人ID',
    `create_time`          datetime       NOT NULL,
    `update_by`            varchar(64)    NOT NULL,
    `update_by_id`         bigint COMMENT '更新人ID',
    `update_time`          datetime       NOT NULL,
    `del_flag`             tinyint        NOT NULL DEFAULT '0',
    `remark`               varchar(500),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_deduct_no` (`deduct_no`),
    KEY                    `idx_deduct_status_deadline` (`deduct_status`,`handle_deadline`),
    KEY                    `idx_deduct_inspection` (`inspection_id`),
    KEY                    `idx_deduct_settlement` (`settlement_id`),
    KEY                    `idx_deduct_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保扣款通知单';

-- ----------------------------
-- biz_yb_deduct_log  医保扣款处理留痕
-- ----------------------------
CREATE TABLE `biz_yb_deduct_log`
(
    `id`           bigint      NOT NULL COMMENT '自增主键',
    `notice_id`    bigint      NOT NULL COMMENT '扣款通知ID',
    `action`       tinyint     NOT NULL COMMENT '动作（1-新建草稿 2-发起申诉 3-录入申诉结果 4-确认扣款并追责 5-录入缴回 6-作废）',
    `detail`       varchar(1000) COMMENT '动作详情/备注',
    `amount`       decimal(10, 2) COMMENT '涉及金额',
    `operator`     varchar(64) NOT NULL COMMENT '操作人',
    `operate_time` datetime    NOT NULL COMMENT '操作时间',
    `create_by`    varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id` bigint COMMENT '创建人ID',
    `create_time`  datetime    NOT NULL COMMENT '创建时间',
    `update_by`    varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id` bigint COMMENT '更新人ID',
    `update_time`  datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_log_notice` (`notice_id`,`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保扣款处理留痕';

-- ----------------------------
-- biz_yb_inspection  医保飞检批次
-- ----------------------------
CREATE TABLE `biz_yb_inspection`
(
    `id`                 bigint       NOT NULL COMMENT '主键（雪花ID）',
    `inspect_no`         varchar(32)  NOT NULL COMMENT '批次号',
    `inspect_type`       tinyint      NOT NULL COMMENT '检查类型（1-国家飞检 2-省级飞检 3-智能审核转来 4-日常驻点审核）',
    `fund_org`           varchar(100) NOT NULL COMMENT '统筹区/医保局名称',
    `inspect_start_date` date         NOT NULL COMMENT '审核目标期间起',
    `inspect_end_date`   date         NOT NULL COMMENT '审核目标期间止',
    `inspect_date`       date         NOT NULL COMMENT '检查组进驻/通知日期',
    `inspect_team`       varchar(200) COMMENT '检查组/审核团队名称',
    `our_receiver`       varchar(64) COMMENT '本院接待负责人',
    `status`             tinyint      NOT NULL COMMENT '状态（1-进行中 2-已结项 3-已作废）',
    `conclusion`         varchar(1000) COMMENT '结项结论',
    `conclude_time`      datetime COMMENT '结项时间',
    `conclude_by`        varchar(64) COMMENT '结项经办人',
    `cancel_reason`      varchar(500) COMMENT '作废原因（必填）',
    `cancel_by`          varchar(64) COMMENT '作废经办人',
    `cancel_time`        datetime COMMENT '作废时间',
    `create_by`          varchar(64)  NOT NULL,
    `create_by_id`       bigint COMMENT '创建人ID',
    `create_time`        datetime     NOT NULL,
    `update_by`          varchar(64)  NOT NULL,
    `update_by_id`       bigint COMMENT '更新人ID',
    `update_time`        datetime     NOT NULL,
    `del_flag`           tinyint      NOT NULL DEFAULT '0',
    `remark`             varchar(500),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inspect_no` (`inspect_no`),
    KEY                  `idx_inspect_status_date` (`status`,`inspect_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保飞检批次';

-- ----------------------------
-- sys_insurance_policy  医保政策配置
-- ----------------------------
CREATE TABLE `sys_insurance_policy`
(
    `id`              bigint        NOT NULL COMMENT '主键ID',
    `policy_name`     varchar(100)  NOT NULL COMMENT '政策名称',
    `insurance_type`  varchar(50)   NOT NULL COMMENT '医保类型',
    `settlement_type` tinyint COMMENT '结算方式（2-城镇职工医保 3-城乡居民医保 4-公费医疗）',
    `coverage_ratio`  decimal(5, 2) NOT NULL COMMENT '统筹比例',
    `self_pay_ratio`  decimal(5, 2)          DEFAULT '10.00' COMMENT '乙类药品自付比例',
    `status`          tinyint                DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_time`     datetime      NOT NULL COMMENT '创建时间',
    `update_time`     datetime      NOT NULL COMMENT '更新时间',
    `remark`          varchar(500) COMMENT '备注',
    `create_by`       varchar(64)   NOT NULL COMMENT '创建人',
    `create_by_id`    bigint COMMENT '创建人ID',
    `update_by`       varchar(64)   NOT NULL COMMENT '更新人',
    `update_by_id`    bigint COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY               `idx_settlement_type` (`settlement_type`),
    KEY               `idx_insurance_type` (`insurance_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保政策配置';

-- ----------------------------
-- biz_insurance_catalog_rule  医保目录报销规则
-- ----------------------------
CREATE TABLE `biz_insurance_catalog_rule`
(
    `id`             bigint         NOT NULL COMMENT '主键（雪花）',
    `rule_no`        varchar(32)    NOT NULL COMMENT '规则编号',
    `item_code`      varchar(32)    NOT NULL COMMENT '项目编码',
    `item_name`      varchar(200) COMMENT '项目名称',
    `catalog_type`   tinyint        NOT NULL COMMENT '医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）',
    `encounter_type` tinyint        NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
    `insurance_type` varchar(32) COMMENT '医保类型（职工/居民/公费等；NULL=通用规则，所有医保类型共用）',
    `self_pay_ratio` decimal(5, 2)  NOT NULL COMMENT '自付比例（%）',
    `deductible`     decimal(10, 2) NOT NULL COMMENT '起付线（元）',
    `ceiling`        decimal(10, 2) NOT NULL COMMENT '封顶线（元）',
    `pool_ratio`     decimal(5, 2)  NOT NULL COMMENT '统筹报销比例（%）',
    `limit_flags`    int            NOT NULL COMMENT '限制标志位掩码',
    `effective_date` date           NOT NULL COMMENT '生效日期（含）',
    `expire_date`    date COMMENT '失效日期',
    `priority`       int            NOT NULL COMMENT '优先级',
    `status`         tinyint        NOT NULL COMMENT '状态（1-启用 0-停用）',
    `create_by`      varchar(64)    NOT NULL,
    `create_by_id`   bigint COMMENT '创建人ID',
    `create_time`    datetime       NOT NULL,
    `update_by`      varchar(64)    NOT NULL,
    `update_by_id`   bigint COMMENT '更新人ID',
    `update_time`    datetime       NOT NULL,
    `del_flag`       tinyint        NOT NULL DEFAULT '0',
    `remark`         varchar(500),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_no` (`rule_no`),
    UNIQUE KEY `uk_item_catalog_encounter` (`item_code`,`catalog_type`,`encounter_type`,`insurance_type`,`effective_date`),
    KEY              `idx_rule_item` (`item_code`),
    KEY              `idx_rule_catalog` (`catalog_type`),
    KEY              `idx_rule_encounter` (`encounter_type`),
    KEY              `idx_rule_effective` (`effective_date`,`expire_date`),
    KEY              `idx_rule_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保目录报销规则';

-- ----------------------------
-- biz_insurance_settlement  医保结算清单
-- ----------------------------
CREATE TABLE `biz_insurance_settlement`
(
    `id`                   bigint         NOT NULL COMMENT '主键ID',
    `settlement_no`        varchar(32)    NOT NULL COMMENT '结算清单号',
    `bill_id`              bigint         NOT NULL COMMENT '结算账单ID',
    `bill_no`              varchar(32) COMMENT '结算账单号',
    `encounter_type`       tinyint COMMENT '就诊类型（1-门诊 2-住院）',
    `encounter_id`         bigint COMMENT '就诊标识',
    `patient_id`           bigint         NOT NULL COMMENT '患者ID',
    `patient_no`           varchar(32) COMMENT '患者号',
    `patient_name`         varchar(50) COMMENT '患者姓名',
    `gender`               tinyint COMMENT '性别（1-男 2-女 9-未知）',
    `age`                  int COMMENT '年龄',
    `id_card`              varchar(18) COMMENT '身份证号',
    `medical_insurance_no` varchar(32) COMMENT '医保卡号',
    `regist_id`            bigint COMMENT '挂号ID快照',
    `visit_type`           varchar(20) COMMENT '就诊类型（初诊/复诊）',
    `dept_id`              bigint COMMENT '科室ID',
    `dept_name`            varchar(50) COMMENT '科室名称',
    `doctor_id`            bigint COMMENT '医生ID',
    `doctor_name`          varchar(50) COMMENT '医生姓名',
    `diagnosis`            varchar(500) COMMENT '诊断',
    `diagnosis_code`       varchar(32) COMMENT '诊断编码',
    `diagnosis_name`       varchar(200) COMMENT '诊断名称',
    `total_amount`         decimal(10, 2) NOT NULL COMMENT '医疗总费用 = 账单应收合计 - 院内优惠',
    `drug_amount`          decimal(10, 2)          DEFAULT '0.00' COMMENT '药品费',
    `inspection_amount`    decimal(10, 2)          DEFAULT '0.00' COMMENT '检查费',
    `laboratory_amount`    decimal(10, 2)          DEFAULT '0.00' COMMENT '检验费',
    `treatment_amount`     decimal(10, 2)          DEFAULT '0.00' COMMENT '治疗费',
    `material_amount`      decimal(10, 2)          DEFAULT '0.00' COMMENT '材料费',
    `other_amount`         decimal(10, 2)          DEFAULT '0.00' COMMENT '其他费用',
    `settlement_type`      tinyint COMMENT '结算方式（1-自费 2-医保）',
    `insurance_type`       varchar(32) COMMENT '医保类型（城镇职工/城镇居民/新农合等）',
    `coverage_ratio`       decimal(5, 2) COMMENT '统筹报销比例（%）',
    `insurance_pay`        decimal(10, 2)          DEFAULT '0.00' COMMENT '医保统筹支付 = 账单 pool_amount',
    `personal_pay`         decimal(10, 2)          DEFAULT '0.00' COMMENT '个人账户支付 = 本账单 pay_method=4 的成功收款流水合计',
    `self_pay`             decimal(10, 2)          DEFAULT '0.00' COMMENT '患者自付 = 现金',
    `settlement_status`    tinyint        NOT NULL COMMENT '清单状态（1-待结算 2-已结算 3-已上传 4-已审核 5-已作废）',
    `upload_time`          datetime COMMENT '上传时间',
    `audit_status`         tinyint COMMENT '审核状态（0-待审核 1-审核通过 2-审核驳回）',
    `audit_time`           datetime COMMENT '审核时间',
    `audit_remark`         varchar(500) COMMENT '审核意见',
    `drg_code`             varchar(32) COMMENT 'DRG分组编码',
    `drg_weight`           decimal(10, 4) COMMENT 'DRG权重',
    `estimated_cost`       decimal(10, 2) COMMENT '预估费用',
    `create_by`            varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`         bigint COMMENT '创建人ID',
    `create_time`          datetime       NOT NULL COMMENT '创建时间',
    `update_by`            varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`         bigint COMMENT '更新人ID',
    `update_time`          datetime       NOT NULL COMMENT '更新时间',
    `del_flag`             tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`               varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settlement_no` (`settlement_no`),
    UNIQUE KEY `uk_isb_bill` (`bill_id`),
    KEY                    `idx_patient_id` (`patient_id`),
    KEY                    `idx_regist_id` (`regist_id`),
    KEY                    `idx_settlement_status` (`settlement_status`),
    KEY                    `idx_isb_encounter` (`encounter_type`,`encounter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保结算清单';

-- ----------------------------
-- biz_settlement_diagnosis  结算清单诊断明细
-- ----------------------------
CREATE TABLE `biz_settlement_diagnosis`
(
    `id`              bigint      NOT NULL COMMENT '主键ID',
    `settlement_id`   bigint      NOT NULL COMMENT '结算清单ID',
    `seq_no`          int         NOT NULL COMMENT '序号',
    `diag_type`       tinyint     NOT NULL COMMENT '诊断类型（1-主要诊断 2-其他诊断）',
    `icd_code`        varchar(32) COMMENT 'ICD-10 编码',
    `icd_name`        varchar(200) COMMENT '诊断名称',
    `admit_condition` tinyint COMMENT '入院病情（1-有 2-临床未确定 3-情况不明 4-无）',
    `cc_level`        varchar(8) COMMENT '并发症合并症级别',
    `evidence_status` tinyint COMMENT '依据核对结果（1-命中 2-通过 3-不适用）',
    `evidence_note`   varchar(500) COMMENT '依据核对说明',
    `create_by`       varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`    bigint COMMENT '创建人ID',
    `create_time`     datetime    NOT NULL COMMENT '创建时间',
    `update_by`       varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`    bigint COMMENT '更新人ID',
    `update_time`     datetime    NOT NULL COMMENT '更新时间',
    `del_flag`        tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY               `idx_sd_settlement` (`settlement_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结算清单诊断明细';

-- ----------------------------
-- biz_settlement_operation  结算清单手术明细
-- ----------------------------
CREATE TABLE `biz_settlement_operation`
(
    `id`              bigint      NOT NULL COMMENT '主键ID',
    `settlement_id`   bigint      NOT NULL COMMENT '结算清单ID',
    `seq_no`          int         NOT NULL COMMENT '序号',
    `oper_code`       varchar(32) COMMENT '手术操作编码',
    `oper_name`       varchar(200) COMMENT '手术操作名称',
    `oper_date`       date COMMENT '手术操作日期',
    `oper_level`      tinyint COMMENT '手术级别',
    `is_main`         tinyint     NOT NULL COMMENT '是否主要手术操作（0-否 1-是）',
    `evidence_status` tinyint COMMENT '依据核对结果（1-命中 2-通过 3-不适用）',
    `evidence_note`   varchar(500) COMMENT '依据核对说明',
    `create_by`       varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`    bigint COMMENT '创建人ID',
    `create_time`     datetime    NOT NULL COMMENT '创建时间',
    `update_by`       varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`    bigint COMMENT '更新人ID',
    `update_time`     datetime    NOT NULL COMMENT '更新时间',
    `del_flag`        tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY               `idx_so_settlement` (`settlement_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结算清单手术明细';

-- ----------------------------
-- biz_insurance_report  医保报盘报文台账
-- ----------------------------
CREATE TABLE `biz_insurance_report`
(
    `id`            bigint      NOT NULL COMMENT '主键（雪花）',
    `settlement_id` bigint      NOT NULL COMMENT '医保结算清单ID',
    `settlement_no` varchar(64) COMMENT '结算清单号',
    `report_type`   tinyint     NOT NULL COMMENT '报文类型（1-上传 2-撤销）',
    `msg_type`      varchar(8)  NOT NULL COMMENT '医保接口编号',
    `trade_no`      varchar(64) NOT NULL COMMENT 'HIS 侧流水号',
    `orig_trade_no` varchar(64) COMMENT '撤销报文回指的原上传 trade_no',
    `receipt_no`    varchar(64) COMMENT '医保端回执编号',
    `payload`       longtext    NOT NULL COMMENT '出参报文全文',
    `reply_payload` text COMMENT '回执报文全文（JSON）',
    `status`        tinyint     NOT NULL COMMENT '报文状态（0-待发送 1-回执成功 2-回执失败 3-已被撤销）',
    `err_msg`       varchar(500) COMMENT '失败原因',
    `send_time`     datetime COMMENT '发出时间',
    `reply_time`    datetime COMMENT '回执时间',
    `bill_date`     date COMMENT '账期日',
    `total_amount`  decimal(12, 2) COMMENT '冗余',
    `insurance_pay` decimal(12, 2) COMMENT '冗余',
    `personal_pay`  decimal(12, 2) COMMENT '冗余',
    `self_pay`      decimal(12, 2) COMMENT '冗余',
    `create_by`     varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`  bigint COMMENT '创建人ID',
    `create_time`   datetime    NOT NULL COMMENT '创建时间',
    `update_by`     varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`  bigint COMMENT '更新人ID',
    `update_time`   datetime    NOT NULL COMMENT '更新时间',
    `del_flag`      tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`        varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_no` (`trade_no`),
    KEY             `idx_settlement` (`settlement_id`),
    KEY             `idx_bill_date_type` (`bill_date`,`report_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保报盘报文台账';

-- ----------------------------
-- biz_compliance_audit  医保合规审核单
-- ----------------------------
CREATE TABLE `biz_compliance_audit`
(
    `id`            bigint      NOT NULL COMMENT '主键ID',
    `audit_no`      varchar(32) NOT NULL COMMENT '审核单号',
    `settlement_id` bigint      NOT NULL COMMENT '结算清单ID',
    `regist_id`     bigint COMMENT '就诊锚点',
    `audit_type`    tinyint     NOT NULL COMMENT '审核类型（1-结算前自查 2-批量筛查 3-医保反馈复核）',
    `risk_level`    tinyint     NOT NULL COMMENT '风险等级（0-未发现 1-提示 2-关注 3-高危）',
    `risk_score`    int         NOT NULL COMMENT '风险分',
    `hit_count`     int         NOT NULL COMMENT '命中规则数',
    `pass_count`    int         NOT NULL COMMENT '通过规则数',
    `na_count`      int         NOT NULL COMMENT '不适用规则数',
    `drg_code`      varchar(32) COMMENT 'DRG分组编码',
    `drg_weight`    decimal(10, 4) COMMENT 'DRG权重',
    `pay_standard`  decimal(10, 2) COMMENT '病组支付标准（元）',
    `actual_cost`   decimal(10, 2) COMMENT '实际总费用（元）',
    `cost_ratio`    decimal(10, 4) COMMENT '费用倍率=实际/支付标准',
    `conclusion`    varchar(500) COMMENT '审核结论',
    `audit_by`      varchar(64) COMMENT '审核人',
    `audit_time`    datetime COMMENT '审核时间',
    `create_by`     varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`  bigint COMMENT '创建人ID',
    `create_time`   datetime    NOT NULL COMMENT '创建时间',
    `update_by`     varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`  bigint COMMENT '更新人ID',
    `update_time`   datetime    NOT NULL COMMENT '更新时间',
    `del_flag`      tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`        varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ca_audit_no` (`audit_no`),
    KEY             `idx_ca_settlement` (`settlement_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保合规审核单';

-- ----------------------------
-- biz_compliance_audit_item  医保合规审核明细
-- ----------------------------
CREATE TABLE `biz_compliance_audit_item`
(
    `id`           bigint       NOT NULL COMMENT '主键ID',
    `audit_id`     bigint       NOT NULL COMMENT '审核ID',
    `rule_code`    varchar(16)  NOT NULL COMMENT '规则编码',
    `rule_name`    varchar(100) NOT NULL COMMENT '规则名称',
    `rule_group`   char(1)      NOT NULL COMMENT '规则分组',
    `result`       tinyint      NOT NULL COMMENT '结果（1-命中 2-通过 3-不适用）',
    `risk_level`   tinyint      NOT NULL COMMENT '风险等级（1-提示 2-关注 3-高危）',
    `target_type`  tinyint      NOT NULL COMMENT '对象（0-清单级 1-诊断 2-手术操作）',
    `target_id`    bigint COMMENT '对象ID',
    `target_code`  varchar(32) COMMENT '对象编码',
    `target_name`  varchar(200) COMMENT '对象名称',
    `evidence`     varchar(1000) COMMENT '判定依据',
    `suggestion`   varchar(500) COMMENT '整改建议',
    `create_by`    varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id` bigint COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL COMMENT '创建时间',
    `update_by`    varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id` bigint COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL COMMENT '更新时间',
    `del_flag`     tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY            `idx_cai_audit` (`audit_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保合规审核明细';
