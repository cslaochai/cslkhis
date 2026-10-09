-- 领域：08-门诊病历与处方
-- 库：hn_biz_his    表数：19
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_medical_record  门诊病历
-- ----------------------------
CREATE TABLE `biz_medical_record`
(
    `id`                 bigint       NOT NULL,
    `record_no`          varchar(32)  NOT NULL COMMENT '病历号',
    `patient_id`         bigint       NOT NULL COMMENT '患者ID',
    `patient_no`         varchar(32)  NOT NULL COMMENT '患者号',
    `patient_name`       varchar(50)  NOT NULL COMMENT '患者姓名',
    `gender`             tinyint      NOT NULL COMMENT '性别',
    `age`                int                   DEFAULT NULL COMMENT '年龄',
    `regist_id`          bigint       NOT NULL COMMENT '挂号ID',
    `regist_no`          varchar(32)  NOT NULL COMMENT '挂号单号',
    `visit_date`         date         NOT NULL COMMENT '就诊日期',
    `visit_type`         tinyint               DEFAULT '1' COMMENT '就诊类型（1-初诊 2-复诊）',
    `dept_id`            bigint       NOT NULL COMMENT '科室ID',
    `dept_name`          varchar(100) NOT NULL COMMENT '科室名称',
    `doctor_id`          bigint       NOT NULL COMMENT '医生ID',
    `doctor_name`        varchar(50)  NOT NULL COMMENT '医生姓名',
    `chief_complaint`    text COMMENT '主诉',
    `present_illness`    text COMMENT '现病史',
    `past_history`       text COMMENT '既往史',
    `personal_history`   text COMMENT '个人史',
    `family_history`     text COMMENT '家族史',
    `allergy_history`    text COMMENT '过敏史',
    `temperature`        varchar(10)           DEFAULT NULL COMMENT '体温（℃）',
    `pulse`              varchar(10)           DEFAULT NULL COMMENT '脉搏（次/分）',
    `respiration`        varchar(10)           DEFAULT NULL COMMENT '呼吸（次/分）',
    `systolic_pressure`  varchar(10)           DEFAULT NULL COMMENT '收缩压（mmHg）',
    `diastolic_pressure` varchar(10)           DEFAULT NULL COMMENT '舒张压（mmHg）',
    `general_condition`  text COMMENT '一般情况',
    `skin_mucosa`        text COMMENT '皮肤黏膜',
    `head_neck`          text COMMENT '头颈部',
    `chest_lung`         text COMMENT '胸肺',
    `heart`              text COMMENT '心脏',
    `abdomen`            text COMMENT '腹部',
    `spine_limbs`        text COMMENT '脊柱四肢',
    `nervous_system`     text COMMENT '神经系统',
    `specialist_exam`    text COMMENT '专科检查',
    `auxiliary_exam`     text COMMENT '辅助检查',
    `diagnosis`          text COMMENT '诊断',
    `diagnosis_code`     varchar(100)          DEFAULT NULL COMMENT '诊断编码',
    `diagnosis_name`     varchar(500)          DEFAULT NULL COMMENT '诊断名称',
    `treatment_plan`     text COMMENT '处理意见',
    `record_status`      tinyint               DEFAULT '1' COMMENT '病历状态（1-草稿 2-已提交 3-已归档 4-已作废）',
    `review_status`      tinyint               DEFAULT '0' COMMENT '审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）',
    `review_by`          varchar(64)           DEFAULT NULL COMMENT '审核人',
    `review_time`        datetime              DEFAULT NULL COMMENT '审核时间',
    `review_remark`      varchar(500)          DEFAULT NULL COMMENT '审核意见',
    `guide_pdf_path`     varchar(512)          DEFAULT NULL COMMENT '患者引导单文件路径',
    `submit_time`        datetime              DEFAULT NULL COMMENT '提交时间',
    `archive_time`       datetime              DEFAULT NULL COMMENT '归档时间',
    `sign_status`        tinyint      NOT NULL DEFAULT '0' COMMENT '签名状态（0-未签名 1-已签名 2-签名已失效）',
    `sign_id`            bigint                DEFAULT NULL COMMENT '当前有效签名ID',
    `signed_time`        datetime              DEFAULT NULL COMMENT '最近一次签名时刻',
    `create_by`          varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`       bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`       bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`           tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`             varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_no` (`record_no`),
    KEY                  `idx_patient_id` (`patient_id`),
    KEY                  `idx_regist_id` (`regist_id`),
    KEY                  `idx_visit_date` (`visit_date`),
    KEY                  `idx_doctor_id` (`doctor_id`),
    KEY                  `idx_dept_id` (`dept_id`),
    KEY                  `idx_record_status` (`record_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊病历';

-- ----------------------------
-- biz_medical_record_log  门诊病历修改日志
-- ----------------------------
CREATE TABLE `biz_medical_record_log`
(
    `id`           bigint      NOT NULL COMMENT '主键ID',
    `record_id`    bigint      NOT NULL COMMENT '病历ID',
    `record_no`    varchar(50)          DEFAULT NULL COMMENT '病历号',
    `user_id`      bigint               DEFAULT NULL COMMENT '操作人ID',
    `user_name`    varchar(64)          DEFAULT NULL COMMENT '操作人姓名',
    `operation`    varchar(50)          DEFAULT NULL COMMENT '操作类型',
    `field_name`   varchar(100)         DEFAULT NULL COMMENT '修改字段',
    `old_value`    text COMMENT '修改前值',
    `new_value`    text COMMENT '修改后值',
    `create_by`    varchar(64) NOT NULL,
    `create_by_id` bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`    varchar(64) NOT NULL,
    `update_by_id` bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`     tinyint              DEFAULT '0',
    `remark`       varchar(500)         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY            `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊病历修改日志';

-- ----------------------------
-- biz_diag_template  常用诊断模板
-- ----------------------------
CREATE TABLE `biz_diag_template`
(
    `id`           bigint       NOT NULL,
    `doctor_id`    bigint       NOT NULL COMMENT '医生ID',
    `icd_code`     varchar(20)  NOT NULL COMMENT 'ICD-10编码',
    `icd_name`     varchar(200) NOT NULL COMMENT '诊断名称',
    `sort_order`   int                   DEFAULT '0' COMMENT '排序',
    `create_by`    varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id` bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id` bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY            `idx_doctor_id` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='常用诊断模板';

-- ----------------------------
-- biz_prescription  处方主表
-- ----------------------------
CREATE TABLE `biz_prescription`
(
    `id`                     bigint       NOT NULL,
    `prescription_no`        varchar(32)  NOT NULL COMMENT '处方号',
    `patient_id`             bigint       NOT NULL COMMENT '患者ID',
    `patient_no`             varchar(32)  NOT NULL COMMENT '患者号',
    `patient_name`           varchar(50)  NOT NULL COMMENT '患者姓名',
    `gender`                 tinyint               DEFAULT NULL COMMENT '性别',
    `age`                    int                   DEFAULT NULL COMMENT '年龄',
    `regist_id`              bigint       NOT NULL COMMENT '挂号ID',
    `record_id`              bigint                DEFAULT NULL COMMENT '病历ID',
    `record_no`              varchar(32)           DEFAULT NULL COMMENT '病历号',
    `visit_date`             date         NOT NULL COMMENT '就诊日期',
    `dept_id`                bigint       NOT NULL COMMENT '科室ID',
    `dept_name`              varchar(100) NOT NULL COMMENT '科室名称',
    `doctor_id`              bigint       NOT NULL COMMENT '医生ID',
    `doctor_name`            varchar(50)  NOT NULL COMMENT '医生姓名',
    `doctor_sign_id`         bigint                DEFAULT NULL COMMENT '开方医师签名ID',
    `doctor_signed_time`     datetime              DEFAULT NULL COMMENT '开方签名时刻',
    `prescription_type`      tinyint               DEFAULT '1' COMMENT '处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）',
    `prescription_source`    tinyint               DEFAULT '1' COMMENT '处方来源（1-门诊处方 2-急诊处方 3-住院处方）',
    `total_amount`           decimal(10, 2)        DEFAULT '0.00' COMMENT '总金额',
    `drug_count`             int                   DEFAULT '0' COMMENT '药品数量',
    `usage_instruction`      varchar(500)          DEFAULT NULL COMMENT '用法说明',
    `dose_count`             int                   DEFAULT NULL COMMENT '中药饮片剂数',
    `decoct_flag`            tinyint               DEFAULT NULL COMMENT '中药煎服方式（1-代煎 2-自煎）',
    `diagnosis`              varchar(500)          DEFAULT NULL COMMENT '诊断',
    `prescription_status`    tinyint               DEFAULT '1' COMMENT '处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）',
    `payment_status`         tinyint               DEFAULT '0' COMMENT '缴费状态（0-未缴费 1-已缴费 2-已退费）',
    `is_long_prescription`   tinyint      NOT NULL DEFAULT '0' COMMENT '长处方（0-否 1-是）',
    `long_prescription_days` int                   DEFAULT NULL COMMENT '长处方用药天数',
    `pay_time`               datetime              DEFAULT NULL COMMENT '缴费时间',
    `pay_amount`             decimal(10, 2)        DEFAULT '0.00' COMMENT '实付金额',
    `pay_method`             tinyint               DEFAULT NULL COMMENT '支付方式（1-现金 2-微信 3-支付宝 4-医保卡 5-余额）',
    `submit_time`            datetime              DEFAULT NULL COMMENT '提交时间',
    `audit_time`             datetime              DEFAULT NULL COMMENT '审核时间',
    `audit_result`           tinyint               DEFAULT NULL COMMENT '审核结果',
    `return_reason`          varchar(500)          DEFAULT NULL COMMENT '最近一次审方退回原因',
    `return_time`            datetime              DEFAULT NULL COMMENT '最近一次退回时间',
    `return_count`           int          NOT NULL DEFAULT '0' COMMENT '累计被退回次数',
    `audit_by`               varchar(64)           DEFAULT NULL COMMENT '审核人',
    `audit_sign_id`          bigint                DEFAULT NULL COMMENT '审方药师签名ID',
    `audit_signed_time`      datetime              DEFAULT NULL COMMENT '审方签名时刻',
    `dispense_time`          datetime              DEFAULT NULL COMMENT '发药时间',
    `dispense_by`            varchar(64)           DEFAULT NULL COMMENT '发药人',
    `cancel_time`            datetime              DEFAULT NULL COMMENT '取消时间',
    `cancel_reason`          varchar(200)          DEFAULT NULL COMMENT '取消原因',
    `refund_time`            datetime              DEFAULT NULL COMMENT '退药时间',
    `refund_by`              varchar(64)           DEFAULT NULL COMMENT '退药人',
    `refund_reason`          varchar(200)          DEFAULT NULL COMMENT '退药原因',
    `is_urgent`              tinyint               DEFAULT '0' COMMENT '是否加急（0-否 1-是）',
    `create_by`              varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`           bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`              varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`           bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`               tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`                 varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prescription_no` (`prescription_no`),
    KEY                      `idx_patient_id` (`patient_id`),
    KEY                      `idx_regist_id` (`regist_id`),
    KEY                      `idx_record_id` (`record_id`),
    KEY                      `idx_visit_date` (`visit_date`),
    KEY                      `idx_doctor_id` (`doctor_id`),
    KEY                      `idx_prescription_status` (`prescription_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方主表';

-- ----------------------------
-- biz_prescription_detail  处方明细
-- ----------------------------
CREATE TABLE `biz_prescription_detail`
(
    `id`               bigint         NOT NULL,
    `prescription_id`  bigint         NOT NULL COMMENT '处方ID',
    `prescription_no`  varchar(32)    NOT NULL COMMENT '处方号',
    `drug_id`          bigint         NOT NULL COMMENT '药品ID',
    `drug_code`        varchar(32)    NOT NULL COMMENT '药品编码',
    `drug_name`        varchar(200)   NOT NULL COMMENT '药品名称',
    `generic_name`     varchar(200)            DEFAULT NULL COMMENT '通用名',
    `specification`    varchar(100)            DEFAULT NULL COMMENT '规格',
    `dosage_form`      varchar(50)             DEFAULT NULL COMMENT '剂型',
    `manufacturer`     varchar(200)            DEFAULT NULL COMMENT '生产厂家',
    `unit`             varchar(20)    NOT NULL COMMENT '单位',
    `quantity`         decimal(10, 2) NOT NULL COMMENT '数量',
    `price`            decimal(10, 4)          DEFAULT NULL COMMENT '单价',
    `amount`           decimal(10, 2) NOT NULL COMMENT '金额',
    `usage_dosage`     varchar(100)   NOT NULL COMMENT '用法用量',
    `frequency`        varchar(50)    NOT NULL COMMENT '用药频次',
    `route`            varchar(50)    NOT NULL COMMENT '用药途径',
    `duration`         int                     DEFAULT NULL COMMENT '疗程天数',
    `single_dosage`    varchar(50)             DEFAULT NULL COMMENT '单次剂量',
    `total_dosage`     decimal(10, 2)          DEFAULT NULL COMMENT '总剂量',
    `is_skin_test`     tinyint                 DEFAULT '0' COMMENT '是否需要皮试（0-否 1-是）',
    `skin_test_result` tinyint                 DEFAULT NULL COMMENT '皮试结果（0-阴性 1-阳性）',
    `is_allergy`       tinyint                 DEFAULT '0' COMMENT '是否过敏（0-否 1-是）',
    `is_combo`         tinyint                 DEFAULT '0' COMMENT '是否组合药（0-否 1-是）',
    `combo_group`      int                     DEFAULT NULL COMMENT '组合组号',
    `is_special`       tinyint                 DEFAULT '0' COMMENT '是否特殊用药（0-否 1-是）',
    `special_reason`   varchar(200)            DEFAULT NULL COMMENT '特殊用药原因',
    `detail_status`    tinyint                 DEFAULT '1' COMMENT '明细状态（1-正常 2-已发药 3-已退药）',
    `create_by`        varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`     bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`     bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`         tinyint                 DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`           varchar(500)            DEFAULT NULL COMMENT '备注',
    `payment_status`   tinyint                 DEFAULT '0' COMMENT '缴费状态（0-未缴费 1-已缴费 2-已退费）',
    PRIMARY KEY (`id`),
    KEY                `idx_prescription_id` (`prescription_id`),
    KEY                `idx_drug_id` (`drug_id`),
    KEY                `idx_drug_code` (`drug_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方明细';

-- ----------------------------
-- biz_rx_template  处方模板
-- ----------------------------
CREATE TABLE `biz_rx_template`
(
    `id`            bigint       NOT NULL,
    `doctor_id`     bigint       NOT NULL COMMENT '医生ID',
    `template_name` varchar(100) NOT NULL COMMENT '模板名称',
    `drug_count`    int                   DEFAULT '0' COMMENT '药品数量',
    `total_amount`  decimal(10, 2)        DEFAULT '0.00' COMMENT '总金额',
    `create_by`     varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`  bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`  bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`        varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY             `idx_doctor_id` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方模板';

-- ----------------------------
-- biz_rx_template_detail  处方模板明细
-- ----------------------------
CREATE TABLE `biz_rx_template_detail`
(
    `id`            bigint         NOT NULL,
    `template_id`   bigint         NOT NULL COMMENT '模板ID',
    `drug_id`       bigint         NOT NULL COMMENT '药品ID',
    `drug_code`     varchar(32)    NOT NULL COMMENT '药品编码',
    `drug_name`     varchar(200)   NOT NULL COMMENT '药品名称',
    `generic_name`  varchar(200)            DEFAULT NULL COMMENT '通用名',
    `specification` varchar(100)            DEFAULT NULL COMMENT '规格',
    `dosage_form`   varchar(50)             DEFAULT NULL COMMENT '剂型',
    `manufacturer`  varchar(200)            DEFAULT NULL COMMENT '生产厂家',
    `unit`          varchar(20)    NOT NULL COMMENT '单位',
    `quantity`      decimal(10, 2) NOT NULL COMMENT '数量',
    `price`         decimal(10, 2) NOT NULL COMMENT '单价',
    `amount`        decimal(10, 2) NOT NULL COMMENT '金额',
    `usage_dosage`  varchar(100)   NOT NULL COMMENT '用法用量',
    `frequency`     varchar(50)    NOT NULL COMMENT '用药频次',
    `route`         varchar(50)    NOT NULL COMMENT '用药途径',
    `duration`      int                     DEFAULT NULL COMMENT '疗程天数',
    `single_dosage` varchar(50)             DEFAULT NULL COMMENT '单次剂量',
    `create_by`     varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`  bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`  bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint                 DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`        varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY             `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方模板明细';

-- ----------------------------
-- biz_prescription_audit_log  处方审方流水
-- ----------------------------
CREATE TABLE `biz_prescription_audit_log`
(
    `id`              bigint      NOT NULL,
    `prescription_id` bigint               DEFAULT NULL COMMENT '处方 id',
    `prescription_no` varchar(40) NOT NULL COMMENT '处方号',
    `record_id`       bigint               DEFAULT NULL COMMENT '病历 id',
    `regist_id`       bigint               DEFAULT NULL COMMENT '挂号 id',
    `round_no`        int         NOT NULL DEFAULT '1' COMMENT '第几轮',
    `action`          tinyint     NOT NULL COMMENT '动作',
    `auditor_id`      bigint               DEFAULT NULL COMMENT '操作人员工 id',
    `auditor_name`    varchar(64)          DEFAULT NULL COMMENT '操作人姓名',
    `opinion`         varchar(500)         DEFAULT NULL COMMENT '审方意见/退回原因',
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `del_flag`        tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志',
    `create_by`       varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`    bigint               DEFAULT NULL COMMENT '创建人ID',
    `update_by`       varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`    bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY               `idx_pal_record` (`record_id`),
    KEY               `idx_pal_rx` (`prescription_id`),
    KEY               `idx_pal_no` (`prescription_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方审方流水';

-- ----------------------------
-- biz_rx_flow  处方流转单
-- ----------------------------
CREATE TABLE `biz_rx_flow`
(
    `id`              bigint       NOT NULL COMMENT '主键（雪花）',
    `flow_no`         varchar(32)  NOT NULL COMMENT '流转单号',
    `prescription_id` bigint       NOT NULL COMMENT '处方ID',
    `prescription_no` varchar(64)  NOT NULL COMMENT '处方号',
    `patient_id`      bigint       NOT NULL COMMENT '患者ID',
    `patient_no`      varchar(32)  NOT NULL COMMENT '患者号',
    `patient_name`    varchar(50)  NOT NULL COMMENT '患者姓名',
    `org_name`        varchar(100) NOT NULL COMMENT '流向机构名称',
    `org_type`        tinyint      NOT NULL DEFAULT '1' COMMENT '机构类型（1-院外药店 2-基层医疗机构 3-线上药房）',
    `flow_status`     tinyint      NOT NULL DEFAULT '1' COMMENT '流转状态（1-已流转 2-已取药 3-已取消）',
    `flow_time`       datetime              DEFAULT NULL COMMENT '流转时间',
    `finish_time`     datetime              DEFAULT NULL COMMENT '完成/取消时间',
    `total_amount`    decimal(10, 2)        DEFAULT NULL COMMENT '处方总金额（快照）',
    `del_flag`        tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500)          DEFAULT NULL COMMENT '备注',
    `create_by`       varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`    bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`    bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_no` (`flow_no`),
    KEY               `idx_rxflow_prescription` (`prescription_id`),
    KEY               `idx_rxflow_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方流转单';

-- ----------------------------
-- biz_rx_review_batch  处方点评批次
-- ----------------------------
CREATE TABLE `biz_rx_review_batch`
(
    `id`             bigint       NOT NULL COMMENT '主键',
    `batch_no`       varchar(32)  NOT NULL COMMENT '批次号',
    `batch_name`     varchar(100) NOT NULL COMMENT '批次名称',
    `review_type`    tinyint      NOT NULL DEFAULT '1' COMMENT '点评类型（1-常规点评 2-专项点评）',
    `specialty`      varchar(100)          DEFAULT NULL COMMENT '专项主题',
    `date_start`     date         NOT NULL COMMENT '处方就诊日期起',
    `date_end`       date         NOT NULL COMMENT '处方就诊日期止',
    `sample_count`   int          NOT NULL COMMENT '抽样处方数',
    `reviewed_count` int          NOT NULL DEFAULT '0' COMMENT '已点评数',
    `status`         tinyint      NOT NULL DEFAULT '1' COMMENT '批次状态（1-进行中 2-已完成）',
    `reviewer_id`    bigint                DEFAULT NULL COMMENT '点评人员工ID',
    `reviewer_name`  varchar(50)           DEFAULT NULL COMMENT '点评人姓名',
    `create_by`      varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`   bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`         varchar(500)          DEFAULT NULL COMMENT '备注',
    `update_by`      varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`   bigint                DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_no` (`batch_no`) COMMENT '批次号唯一；本表删除走物理删，软删会占键',
    KEY              `idx_date` (`date_start`,`date_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方点评批次';

-- ----------------------------
-- biz_rx_review_item  处方点评明细
-- ----------------------------
CREATE TABLE `biz_rx_review_item`
(
    `id`                  bigint       NOT NULL COMMENT '主键',
    `batch_id`            bigint       NOT NULL COMMENT '批次ID',
    `batch_no`            varchar(32)  NOT NULL COMMENT '批次号',
    `prescription_id`     bigint       NOT NULL COMMENT '处方ID',
    `prescription_no`     varchar(32)  NOT NULL COMMENT '处方号（快照）',
    `patient_name`        varchar(50)  NOT NULL COMMENT '患者姓名（快照）',
    `dept_name`           varchar(100) NOT NULL COMMENT '开方科室（快照）',
    `doctor_id`           bigint       NOT NULL COMMENT '开方医生ID（快照）',
    `doctor_name`         varchar(50)  NOT NULL COMMENT '开方医生姓名（快照）',
    `visit_date`          date         NOT NULL COMMENT '就诊日期',
    `diagnosis`           varchar(500)          DEFAULT NULL COMMENT '诊断（快照）',
    `drug_count`          int                   DEFAULT '0' COMMENT '药品数量（快照）',
    `total_amount`        decimal(10, 2)        DEFAULT '0.00' COMMENT '处方金额（快照）',
    `prescription_type`   tinyint               DEFAULT '1' COMMENT '处方类型（1-西药 2-中成药 3-中药饮片）',
    `prescription_source` tinyint               DEFAULT '1' COMMENT '处方来源（1-门诊 2-急诊 3-住院）',
    `review_status`       tinyint      NOT NULL DEFAULT '0' COMMENT '点评状态（0-待点评 1-已点评）',
    `review_result`       tinyint               DEFAULT NULL COMMENT '点评结论（1-合理 2-不规范处方 3-用药不适宜处方 4-超常处方）',
    `problem_types`       varchar(500)          DEFAULT NULL COMMENT '问题码（11-15不规范 21-27不适宜 31-34超常）',
    `review_opinion`      varchar(500)          DEFAULT NULL COMMENT '点评意见',
    `reviewer_id`         bigint                DEFAULT NULL COMMENT '点评人员工ID',
    `reviewer_name`       varchar(50)           DEFAULT NULL COMMENT '点评人姓名',
    `review_time`         datetime              DEFAULT NULL COMMENT '点评时间',
    `publicity_status`    tinyint      NOT NULL DEFAULT '0' COMMENT '公示状态（0-未公示 1-已公示）',
    `publicity_by`        varchar(50)           DEFAULT NULL COMMENT '公示操作人',
    `publicity_time`      datetime              DEFAULT NULL COMMENT '公示时间',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`              varchar(500)          DEFAULT NULL COMMENT '备注',
    `create_by`           varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`        bigint                DEFAULT NULL COMMENT '创建人ID',
    `update_by`           varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`        bigint                DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_rx` (`batch_id`,`prescription_id`) COMMENT '一批次一处方一条；抽样/补录时排除已收录处方，本表删除走物理删',
    KEY                   `idx_batch` (`batch_id`),
    KEY                   `idx_doctor` (`doctor_id`),
    KEY                   `idx_result` (`review_result`),
    KEY                   `idx_publicity` (`publicity_status`),
    KEY                   `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方点评明细';

-- ----------------------------
-- biz_rx_doctor_talk  医师约谈记录
-- ----------------------------
CREATE TABLE `biz_rx_doctor_talk`
(
    `id`                  bigint      NOT NULL COMMENT '主键',
    `talk_no`             varchar(32) NOT NULL COMMENT '约谈编号',
    `doctor_id`           bigint               DEFAULT NULL COMMENT '被约谈医师ID',
    `doctor_name`         varchar(50) NOT NULL COMMENT '被约谈医师姓名',
    `dept_name`           varchar(100)         DEFAULT NULL COMMENT '医师所在科室（快照）',
    `talk_type`           tinyint     NOT NULL DEFAULT '1' COMMENT '约谈类型（1-首次约谈 2-警告约谈 3-限制处方权 4-取消处方权 5-恢复处方权）',
    `talk_time`           datetime    NOT NULL COMMENT '约谈时间',
    `talker_name`         varchar(50) NOT NULL COMMENT '约谈人姓名',
    `talker_org`          varchar(100)         DEFAULT NULL COMMENT '约谈部门',
    `related_count`       int         NOT NULL DEFAULT '0' COMMENT '关联不合理处方数',
    `related_review_ids`  varchar(500)         DEFAULT NULL COMMENT '关联点评明细ID',
    `problem_summary`     varchar(500)         DEFAULT NULL COMMENT '问题摘要',
    `talk_content`        varchar(1000)        DEFAULT NULL COMMENT '约谈内容',
    `rectify_require`     varchar(500)         DEFAULT NULL COMMENT '整改要求',
    `rectify_status`      tinyint     NOT NULL DEFAULT '1' COMMENT '整改状态（1-待整改 2-已整改）',
    `rectify_remark`      varchar(500)         DEFAULT NULL COMMENT '整改情况说明',
    `doctor_confirm`      tinyint     NOT NULL DEFAULT '0' COMMENT '医师确认（0-未确认 1-已确认）',
    `doctor_confirm_by`   varchar(50)          DEFAULT NULL COMMENT '医师确认人',
    `doctor_confirm_time` datetime             DEFAULT NULL COMMENT '医师确认时间',
    `create_by`           varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`        bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`              varchar(500)         DEFAULT NULL COMMENT '备注',
    `update_by`           varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`        bigint               DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_talk_no` (`talk_no`) COMMENT '约谈编号唯一；本表删除走物理删，软删会占键',
    KEY                   `idx_doctor` (`doctor_id`),
    KEY                   `idx_talk_time` (`talk_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医师约谈记录';

-- ----------------------------
-- biz_skin_test  门诊皮试记录
-- ----------------------------
CREATE TABLE `biz_skin_test`
(
    `id`                  bigint       NOT NULL COMMENT '主键（雪花）',
    `test_no`             varchar(32)  NOT NULL COMMENT '皮试单号',
    `patient_id`          bigint       NOT NULL COMMENT '患者ID',
    `patient_name`        varchar(50)  NOT NULL COMMENT '患者姓名（快照）',
    `drug_name`           varchar(200) NOT NULL COMMENT '皮试药物名称',
    `treatment_record_id` bigint                DEFAULT NULL COMMENT '来源治疗记录ID',
    `test_time`           datetime     NOT NULL COMMENT '皮试时间',
    `result`              tinyint      NOT NULL DEFAULT '0' COMMENT '判读结果（0-待判读 1-阴性 2-阳性）',
    `result_time`         datetime              DEFAULT NULL COMMENT '判读时间',
    `nurse_id`            bigint                DEFAULT NULL COMMENT '执行护士ID',
    `nurse_name`          varchar(50)           DEFAULT NULL COMMENT '执行护士姓名（快照）',
    `create_by`           varchar(64)  NOT NULL,
    `create_by_id`        bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`           varchar(64)  NOT NULL,
    `update_by_id`        bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`            tinyint      NOT NULL DEFAULT '0',
    `remark`              varchar(500)          DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_test_no` (`test_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊皮试记录';

-- ----------------------------
-- biz_tcm_decoct  中药代煎单
-- ----------------------------
CREATE TABLE `biz_tcm_decoct`
(
    `id`              bigint         NOT NULL COMMENT '主键ID（雪花）',
    `decoct_no`       varchar(32)    NOT NULL COMMENT '代煎单号',
    `prescription_id` bigint         NOT NULL COMMENT '处方ID',
    `prescription_no` varchar(32)    NOT NULL COMMENT '处方号',
    `patient_id`      bigint         NOT NULL COMMENT '患者ID（快照）',
    `patient_no`      varchar(32)             DEFAULT NULL COMMENT '患者号（快照）',
    `patient_name`    varchar(50)             DEFAULT NULL COMMENT '患者姓名（快照）',
    `dept_name`       varchar(100)            DEFAULT NULL COMMENT '开方科室',
    `doctor_name`     varchar(50)             DEFAULT NULL COMMENT '开方医师（快照）',
    `dose_count`      int            NOT NULL DEFAULT '1' COMMENT '剂数',
    `herb_count`      int            NOT NULL DEFAULT '0' COMMENT '味数',
    `total_grams`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '全方总克数',
    `method_summary`  varchar(500)            DEFAULT NULL COMMENT '煎法脚注汇总',
    `decoct_status`   tinyint        NOT NULL DEFAULT '1' COMMENT '状态（1-待煎 2-已煎 3-已取 9-已作废）',
    `pharmacy_id`     bigint                  DEFAULT NULL COMMENT '代煎药房ID',
    `pharmacy_name`   varchar(100)            DEFAULT NULL COMMENT '代煎药房名称（快照）',
    `operator_id`     bigint                  DEFAULT NULL COMMENT '最近一次状态操作人',
    `operator_name`   varchar(64)             DEFAULT NULL COMMENT '最近一次状态操作人姓名（快照）',
    `decoct_time`     datetime                DEFAULT NULL COMMENT '煎药完成时间',
    `pickup_time`     datetime                DEFAULT NULL COMMENT '患者取走时间（终态）',
    `cancel_reason`   varchar(200)            DEFAULT NULL COMMENT '作废原因',
    `remark`          varchar(500)            DEFAULT NULL COMMENT '备注',
    `create_by`       varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`    bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`    bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prescription` (`prescription_id`),
    KEY               `idx_decoct_status` (`decoct_status`,`create_time`),
    KEY               `idx_decoct_no` (`decoct_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='中药代煎单';

-- ----------------------------
-- biz_narcotic_register  麻精药品专册
-- ----------------------------
CREATE TABLE `biz_narcotic_register`
(
    `id`                bigint                                                       NOT NULL COMMENT '主键ID',
    `register_no`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '专册登记号',
    `prescription_id`   bigint                                                                DEFAULT NULL COMMENT '处方ID',
    `prescription_no`   varchar(32) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '处方号',
    `dispensing_id`     bigint                                                                DEFAULT NULL COMMENT '发药记录ID',
    `dispensing_no`     varchar(32) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '发药单号',
    `patient_id`        bigint                                                                DEFAULT NULL COMMENT '患者ID',
    `patient_no`        varchar(32) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '患者号',
    `patient_name`      varchar(50) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '患者姓名',
    `gender`            tinyint                                                               DEFAULT NULL COMMENT '性别（1-男 2-女 9-未知）',
    `age`               int                                                                   DEFAULT NULL COMMENT '年龄',
    `id_card`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '身份证号',
    `dept_id`           bigint                                                                DEFAULT NULL COMMENT '开方科室ID',
    `dept_name`         varchar(100) COLLATE utf8mb4_general_ci                               DEFAULT NULL COMMENT '开方科室名称',
    `diagnosis`         varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '临床诊断',
    `drug_id`           bigint                                                                DEFAULT NULL COMMENT '药品ID',
    `drug_code`         varchar(32) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '药品编码',
    `drug_name`         varchar(200) COLLATE utf8mb4_general_ci                               DEFAULT NULL COMMENT '药品名称',
    `specification`     varchar(100) COLLATE utf8mb4_general_ci                               DEFAULT NULL COMMENT '规格',
    `unit`              varchar(20) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '单位',
    `special_flag`      tinyint                                                      NOT NULL COMMENT '特殊管理分类（1-麻醉 2-第一类精神 3-第二类精神 4-毒性）',
    `dosage_form`       varchar(50) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '剂型（用于判定限量档位：注射剂/控缓释/其他）',
    `quantity`          decimal(10, 2)                                               NOT NULL COMMENT '发药数量',
    `batch_no`          varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '批号',
    `duration`          int                                                                   DEFAULT NULL COMMENT '核定的处方天数',
    `limit_days`        int                                                                   DEFAULT NULL COMMENT '规则允许的最大天数',
    `daily_dosage`      decimal(10, 2)                                                        DEFAULT NULL COMMENT '核定日用量',
    `doctor_id`         bigint                                                                DEFAULT NULL COMMENT '开方医师ID',
    `doctor_name`       varchar(50) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '开方医师姓名',
    `audit_by`          varchar(64) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '审方药师',
    `dispense_by_id`    bigint                                                                DEFAULT NULL COMMENT '发药人ID',
    `dispense_by`       varchar(50) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '发药人姓名',
    `dispense_time`     datetime                                                              DEFAULT NULL COMMENT '发药时间',
    `checker_id`        bigint                                                                DEFAULT NULL COMMENT '复核人ID',
    `checker_name`      varchar(50) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '复核人姓名',
    `check_time`        datetime                                                              DEFAULT NULL COMMENT '复核时间',
    `ampoule_status`    tinyint                                                      NOT NULL DEFAULT '0' COMMENT '空安瓿回收状态（0-不适用 1-待回收 2-已回收）',
    `ampoule_issued`    decimal(10, 2)                                                        DEFAULT NULL COMMENT '发出安瓿数',
    `ampoule_returned`  decimal(10, 2)                                                        DEFAULT NULL COMMENT '回收空安瓿数',
    `ampoule_destroyed` decimal(10, 2)                                                        DEFAULT NULL COMMENT '剩余液销毁量',
    `return_by`         varchar(50) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '回收登记人',
    `return_time`       datetime                                                              DEFAULT NULL COMMENT '回收登记时间',
    `return_remark`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '回收/销毁说明',
    `create_by`         varchar(64) COLLATE utf8mb4_general_ci                       NOT NULL COMMENT '创建人',
    `create_by_id`      bigint                                                                DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         varchar(64) COLLATE utf8mb4_general_ci                       NOT NULL COMMENT '更新人',
    `update_by_id`      bigint                                                                DEFAULT NULL COMMENT '更新人ID',
    `update_time`       datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`          tinyint                                                               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`            varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_narco_register_no` (`register_no`),
    KEY                 `idx_narco_drug` (`drug_id`),
    KEY                 `idx_narco_prescription` (`prescription_id`),
    KEY                 `idx_narco_dispensing` (`dispensing_id`),
    KEY                 `idx_narco_patient` (`patient_id`),
    KEY                 `idx_narco_flag_time` (`special_flag`,`dispense_time`),
    KEY                 `idx_narco_ampoule` (`ampoule_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='麻精药品专册';

-- ----------------------------
-- biz_outp_infusion  门诊输液单
-- ----------------------------
CREATE TABLE `biz_outp_infusion`
(
    `id`                  bigint      NOT NULL COMMENT '主键（雪花）',
    `infusion_no`         varchar(32) NOT NULL COMMENT '输液单号',
    `treatment_record_id` bigint               DEFAULT NULL COMMENT '来源治疗记录ID',
    `patient_id`          bigint      NOT NULL COMMENT '患者ID',
    `patient_no`          varchar(32)          DEFAULT NULL COMMENT '患者编号（快照）',
    `patient_name`        varchar(50) NOT NULL COMMENT '患者姓名（快照）',
    `gender`              tinyint              DEFAULT NULL COMMENT '性别（快照）',
    `age`                 int                  DEFAULT NULL COMMENT '年龄（快照）',
    `drug_summary`        varchar(500)         DEFAULT NULL COMMENT '输注内容摘要',
    `seat_id`             bigint               DEFAULT NULL COMMENT '座位ID',
    `seat_no`             varchar(32)          DEFAULT NULL COMMENT '座位号（快照）',
    `skin_test_id`        bigint               DEFAULT NULL COMMENT '皮试记录ID',
    `status`              tinyint     NOT NULL DEFAULT '1' COMMENT '状态（1-待皮试 2-待输注 3-输液中 4-已完成 5-已取消）',
    `start_time`          datetime             DEFAULT NULL COMMENT '开始输注时间',
    `drip_rate`           int                  DEFAULT NULL COMMENT '起始滴速（滴/分）',
    `end_time`            datetime             DEFAULT NULL COMMENT '结束时间',
    `adverse_flag`        tinyint     NOT NULL DEFAULT '0' COMMENT '不良反应（0-无 1-有）',
    `adverse_desc`        varchar(500)         DEFAULT NULL COMMENT '不良反应描述',
    `nurse_id`            bigint               DEFAULT NULL COMMENT '责任护士ID',
    `nurse_name`          varchar(50)          DEFAULT NULL COMMENT '责任护士姓名（快照）',
    `cancel_reason`       varchar(500)         DEFAULT NULL COMMENT '取消原因',
    `create_by`           varchar(64) NOT NULL,
    `create_by_id`        bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`           varchar(64) NOT NULL,
    `update_by_id`        bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`            tinyint     NOT NULL DEFAULT '0',
    `remark`              varchar(500)         DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_infusion_no` (`infusion_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊输液单';

-- ----------------------------
-- biz_outp_infusion_round  门诊输液巡视记录
-- ----------------------------
CREATE TABLE `biz_outp_infusion_round`
(
    `id`               bigint      NOT NULL COMMENT '主键（雪花）',
    `infusion_id`      bigint      NOT NULL COMMENT '输液单ID',
    `round_time`       datetime    NOT NULL COMMENT '巡视时间',
    `drip_rate`        int                  DEFAULT NULL COMMENT '滴速（滴/分）',
    `remaining_volume` int                  DEFAULT NULL COMMENT '余量（ml）',
    `nurse_id`         bigint               DEFAULT NULL COMMENT '巡视护士ID',
    `nurse_name`       varchar(50)          DEFAULT NULL COMMENT '巡视护士姓名（快照）',
    `create_by`        varchar(64) NOT NULL,
    `create_by_id`     bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`        varchar(64) NOT NULL,
    `update_by_id`     bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`         tinyint     NOT NULL DEFAULT '0',
    `remark`           varchar(500)         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                `idx_infusion_id` (`infusion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊输液巡视记录';

-- ----------------------------
-- biz_infusion_round  输液巡视记录
-- ----------------------------
CREATE TABLE `biz_infusion_round`
(
    `id`               bigint      NOT NULL COMMENT '主键ID（雪花）',
    `exec_id`          bigint      NOT NULL COMMENT '执行行ID',
    `order_id`         bigint      NOT NULL COMMENT '医嘱ID（冗余）',
    `admission_id`     bigint      NOT NULL COMMENT '入院ID（冗余）',
    `round_time`       datetime    NOT NULL COMMENT '巡视时间',
    `drip_rate`        int                  DEFAULT NULL COMMENT '滴速（滴/分）',
    `remaining_volume` int                  DEFAULT NULL COMMENT '余量（ml）',
    `round_nurse_id`   bigint               DEFAULT NULL COMMENT '巡视护士ID（员工ID）',
    `round_nurse_name` varchar(64)          DEFAULT NULL COMMENT '巡视护士姓名',
    `create_by`        varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`     bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`     bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`         tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`           varchar(512)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY                `idx_exec` (`exec_id`),
    KEY                `idx_adm` (`admission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='输液巡视记录';

-- ----------------------------
-- biz_ai_draft_diff  病历草稿AI留痕（草稿与终稿差异，SFT训练原料）
-- ----------------------------
CREATE TABLE `biz_ai_draft_diff`
(
    `id`           bigint      NOT NULL COMMENT '主键ID（雪花）',
    `record_id`    bigint      NOT NULL COMMENT '病历ID',
    `regist_id`    bigint               DEFAULT NULL COMMENT '挂号ID',
    `patient_id`   bigint               DEFAULT NULL COMMENT '患者ID',
    `patient_no`   varchar(50) COMMENT '患者号',
    `patient_name` varchar(50) COMMENT '患者姓名',
    `dept_id`      bigint               DEFAULT NULL COMMENT '接诊科室ID',
    `dept_name`    varchar(50) COMMENT '接诊科室名称',
    `doctor_id`    bigint               DEFAULT NULL COMMENT '终审医生ID',
    `doctor_name`  varchar(50) COMMENT '终审医生姓名',
    `draft_text`   text COMMENT 'AI草稿原文（截断2000字）',
    `final_text`   text COMMENT '医生终稿（截断2000字）',
    `diff_json`    mediumtext COMMENT '差异分段JSON（0-相同 1-删 2-增）',
    `changed`      tinyint              DEFAULT '1' COMMENT '是否修改（1-有修改 0-未修改）',
    `create_by`    varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id` bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id` bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint              DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY            `idx_record` (`record_id`),
    KEY            `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历草稿AI留痕（草稿与终稿差异，SFT训练原料）';
