-- 领域：04-临床字典与项目目录（药品·耗材·诊疗项目·诊断编码）
-- 库：hn_biz_his    表数：21
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- sys_drug  药品字典
-- ----------------------------
CREATE TABLE `sys_drug`
(
    `id`                     bigint       NOT NULL COMMENT '主键ID',
    `drug_code`              varchar(32)  NOT NULL COMMENT '药品编码（唯一）',
    `drug_name`              varchar(200) NOT NULL COMMENT '药品名称',
    `drug_type`              tinyint      NOT NULL DEFAULT '1' COMMENT '药品类型（1-西药 2-中成药 3-中药饮片）',
    `generic_name`           varchar(200)          DEFAULT NULL COMMENT '通用名',
    `trade_name`             varchar(200)          DEFAULT NULL COMMENT '商品名',
    `specification`          varchar(100)          DEFAULT NULL COMMENT '规格',
    `dosage_form`            varchar(50)           DEFAULT NULL COMMENT '剂型（片剂、胶囊、注射剂等）',
    `unit`                   varchar(20)           DEFAULT NULL COMMENT '单位（片、粒、支等）',
    `gram_per_unit`          decimal(10, 3)        DEFAULT NULL COMMENT '每最小库存单位含多少克',
    `manufacturer`           varchar(200)          DEFAULT NULL COMMENT '生产厂家',
    `approval_number`        varchar(100)          DEFAULT NULL COMMENT '批准文号',
    `barcode`                varchar(50)           DEFAULT NULL COMMENT '条形码',
    `trace_di`               varchar(32)           DEFAULT NULL COMMENT '药品追溯码产品标识',
    `trace_code_prefix`      varchar(16)           DEFAULT NULL COMMENT '中国药品追溯码本体码',
    `is_trace_required`      tinyint      NOT NULL DEFAULT '0' COMMENT '是否要求扫码采集追溯码（0-不要求 1-必须采集）',
    `category_id`            bigint                DEFAULT NULL COMMENT '药品分类ID',
    `category_name`          varchar(100)          DEFAULT NULL COMMENT '药品分类名称',
    `price`                  decimal(10, 2)        DEFAULT '0.00' COMMENT '单价',
    `cost_price`             decimal(10, 2)        DEFAULT '0.00' COMMENT '成本价',
    `retail_price`           decimal(10, 2)        DEFAULT '0.00' COMMENT '零售价',
    `is_medical_insurance`   tinyint               DEFAULT '0' COMMENT '是否医保药品（0-否 1-是）',
    `medical_insurance_code` varchar(50)           DEFAULT NULL COMMENT '医保编码',
    `storage_condition`      varchar(200)          DEFAULT NULL COMMENT '储存条件',
    `shelf_life`             int                   DEFAULT '0' COMMENT '有效期（月）',
    `is_skin_test`           tinyint               DEFAULT '0' COMMENT '是否需要皮试（0-否 1-是）',
    `is_cold_chain`          tinyint               DEFAULT '0' COMMENT '是否冷链药品（0-否 1-是）',
    `special_flag`           tinyint      NOT NULL DEFAULT '0' COMMENT '特殊管理分类（0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品）',
    `antibiotic_level`       tinyint      NOT NULL DEFAULT '0' COMMENT '抗菌药物分级（0-非抗菌药物 1-非限制使用级 2-限制使用级 3-特殊使用级）',
    `ddd_value`              decimal(10, 4)        DEFAULT NULL COMMENT 'WHO 限定日剂量（g/日）',
    `ddd_unit_gram`          decimal(12, 4)        DEFAULT NULL COMMENT '每发药单位（盒/瓶/支）',
    `contraindication`       text COMMENT '禁忌症',
    `adverse_reaction`       text COMMENT '不良反应',
    `usage_dosage`           text COMMENT '用法用量',
    `status`                 tinyint               DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`              varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`           bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`              varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`           bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`               tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`                 varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drug_code` (`drug_code`),
    KEY                      `idx_drug_name` (`drug_name`),
    KEY                      `idx_drug_type` (`drug_type`),
    KEY                      `idx_category_id` (`category_id`),
    KEY                      `idx_barcode` (`barcode`),
    KEY                      `idx_drug_trace_di` (`trace_di`),
    KEY                      `idx_drug_trace_prefix` (`trace_code_prefix`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品字典';

-- ----------------------------
-- sys_drug_price_history  药品价格变动史
-- ----------------------------
CREATE TABLE `sys_drug_price_history`
(
    `history_id`    bigint         NOT NULL COMMENT '历史记录ID',
    `drug_id`       bigint         NOT NULL COMMENT '药品ID',
    `old_price`     decimal(10, 2) NOT NULL COMMENT '原价',
    `new_price`     decimal(10, 2) NOT NULL COMMENT '新价',
    `change_reason` varchar(256)            DEFAULT NULL COMMENT '调价原因',
    `operator_id`   bigint                  DEFAULT NULL COMMENT '操作人ID',
    `change_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调价时间',
    `create_by`     varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`  bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`  bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`history_id`),
    KEY             `idx_drug_id` (`drug_id`),
    KEY             `idx_change_time` (`change_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品价格变动史';

-- ----------------------------
-- sys_price_change_history  项目价格变更史
-- ----------------------------
CREATE TABLE `sys_price_change_history`
(
    `id`            bigint      NOT NULL COMMENT '主键ID（雪花）',
    `item_type`     varchar(20) NOT NULL COMMENT '项目类型',
    `item_id`       bigint      NOT NULL COMMENT '项目ID',
    `item_code`     varchar(50)          DEFAULT NULL COMMENT '项目编码',
    `item_name`     varchar(100)         DEFAULT NULL COMMENT '项目名称（冗余）',
    `old_price`     decimal(10, 2)       DEFAULT NULL COMMENT '原价',
    `new_price`     decimal(10, 2)       DEFAULT NULL COMMENT '新价',
    `change_reason` varchar(200)         DEFAULT NULL COMMENT '调价原因',
    `operator_id`   bigint               DEFAULT NULL COMMENT '操作人ID（员工ID）',
    `operator_name` varchar(50)          DEFAULT NULL COMMENT '操作人姓名',
    `change_time`   datetime    NOT NULL COMMENT '调价时间',
    `create_by`     varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`  bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`  bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY             `idx_type_item` (`item_type`,`item_id`),
    KEY             `idx_change_time` (`change_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目价格变更史';

-- ----------------------------
-- sys_consumable  耗材字典
-- ----------------------------
CREATE TABLE `sys_consumable`
(
    `id`              bigint                                  NOT NULL COMMENT '主键ID',
    `consumable_code` varchar(32) COLLATE utf8mb4_general_ci  NOT NULL COMMENT '耗材编码（唯一）',
    `consumable_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '耗材名称',
    `category`        tinyint                                                       DEFAULT '5' COMMENT '类别（1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他）',
    `specification`   varchar(100) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '规格',
    `unit`            varchar(20) COLLATE utf8mb4_general_ci                        DEFAULT NULL COMMENT '单位（包、支、盒、个等）',
    `manufacturer`    varchar(200) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '生产厂家',
    `retail_price`    decimal(10, 2)                                                DEFAULT '0.00' COMMENT '零售价',
    `is_high_value`   tinyint                                 NOT NULL              DEFAULT '0' COMMENT '是否高值耗材（0-普通 1-高值）',
    `udi_di`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '产品级UDI-DI',
    `reg_cert_no`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '医疗器械注册证/备案号',
    `status`          tinyint                                                       DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`       varchar(64) COLLATE utf8mb4_general_ci  NOT NULL COMMENT '创建人',
    `create_by_id`    bigint                                                        DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime                                NOT NULL              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64) COLLATE utf8mb4_general_ci  NOT NULL COMMENT '更新人',
    `update_by_id`    bigint                                                        DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime                                NOT NULL              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint                                                       DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_consumable_code` (`consumable_code`),
    KEY               `idx_consumable_name` (`consumable_name`),
    KEY               `idx_consumable_udi_di` (`udi_di`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='耗材字典';

-- ----------------------------
-- sys_inspection_item  检查项目字典
-- ----------------------------
CREATE TABLE `sys_inspection_item`
(
    `id`               bigint       NOT NULL COMMENT '主键ID',
    `item_code`        varchar(32)  NOT NULL COMMENT '项目编码（唯一）',
    `item_name`        varchar(200) NOT NULL COMMENT '项目名称',
    `item_type`        tinyint      NOT NULL DEFAULT '1' COMMENT '项目类型（1-放射检查 2-超声检查 3-心电图 4-内镜检查 5-其他）',
    `dept_id`          bigint                DEFAULT NULL COMMENT '检查科室ID',
    `body_part`        varchar(200)          DEFAULT NULL COMMENT '检查部位',
    `price`            decimal(10, 2)        DEFAULT '0.00' COMMENT '检查价格',
    `duration`         int                   DEFAULT '0' COMMENT '检查时长（分钟）',
    `preparation`      varchar(500)          DEFAULT NULL COMMENT '检查前准备',
    `contraindication` varchar(500)          DEFAULT NULL COMMENT '检查禁忌',
    `report_template`  text COMMENT '报告模板',
    `is_emergency`     tinyint               DEFAULT '0' COMMENT '是否支持急诊（0-否 1-是）',
    `is_appointment`   tinyint               DEFAULT '1' COMMENT '是否需要预约（0-否 1-是）',
    `status`           tinyint               DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`        varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`     bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`     bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`         tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`           varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_code` (`item_code`),
    KEY                `idx_item_type` (`item_type`),
    KEY                `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检查项目字典';

-- ----------------------------
-- sys_laboratory_item  检验项目字典
-- ----------------------------
CREATE TABLE `sys_laboratory_item`
(
    `id`              bigint       NOT NULL COMMENT '主键ID',
    `item_code`       varchar(32)  NOT NULL COMMENT '项目编码（唯一）',
    `item_name`       varchar(200) NOT NULL COMMENT '项目名称',
    `item_type`       tinyint      NOT NULL DEFAULT '1' COMMENT '项目类型（1-血液检验 2-尿液检验 3-生化检验 4-免疫检验 5-微生物检验 6-其他）',
    `dept_id`         bigint                DEFAULT NULL COMMENT '检验科室ID',
    `specimen_type`   varchar(50)           DEFAULT NULL COMMENT '标本类型（血液、尿液、粪便等）',
    `price`           decimal(10, 2)        DEFAULT '0.00' COMMENT '检验价格',
    `duration`        int                   DEFAULT '0' COMMENT '出报告时间（小时）',
    `reference_value` varchar(200)          DEFAULT NULL COMMENT '参考值范围',
    `unit`            varchar(50)           DEFAULT NULL COMMENT '单位',
    `is_emergency`    tinyint               DEFAULT '0' COMMENT '是否支持急诊（0-否 1-是）',
    `is_fasting`      tinyint               DEFAULT '0' COMMENT '是否需要空腹（0-否 1-是）',
    `status`          tinyint               DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`       varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`    bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`    bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_code` (`item_code`),
    KEY               `idx_item_type` (`item_type`),
    KEY               `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验项目字典';

-- ----------------------------
-- sys_laboratory_item_detail  检验项目组套明细
-- ----------------------------
CREATE TABLE `sys_laboratory_item_detail`
(
    `id`                 bigint       NOT NULL COMMENT '主键ID',
    `laboratory_item_id` bigint       NOT NULL COMMENT '检验大项目ID',
    `item_code`          varchar(32)  NOT NULL COMMENT '明细项目编码',
    `item_name`          varchar(200) NOT NULL COMMENT '明细项目名称',
    `unit`               varchar(50)           DEFAULT NULL COMMENT '单位',
    `reference_range`    varchar(100)          DEFAULT NULL COMMENT '参考范围',
    `sort_order`         int                   DEFAULT '0' COMMENT '排序号',
    `status`             tinyint               DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`          varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`       bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`       bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`           tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`             varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY                  `idx_laboratory_item_id` (`laboratory_item_id`),
    KEY                  `idx_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验项目组套明细';

-- ----------------------------
-- sys_treatment_item  治疗项目字典
-- ----------------------------
CREATE TABLE `sys_treatment_item`
(
    `id`           bigint       NOT NULL COMMENT '主键ID',
    `item_code`    varchar(32)  NOT NULL COMMENT '项目编码（唯一）',
    `item_name`    varchar(200) NOT NULL COMMENT '项目名称',
    `item_type`    tinyint      NOT NULL DEFAULT '1' COMMENT '项目类型（1-注射 2-输液 3-换药 4-拆线 5-其他）',
    `dept_id`      bigint                DEFAULT NULL COMMENT '执行科室ID',
    `price`        decimal(10, 2)        DEFAULT '0.00' COMMENT '治疗价格',
    `duration`     int                   DEFAULT '0' COMMENT '治疗时长（分钟）',
    `usage_method` varchar(200)          DEFAULT NULL COMMENT '使用方法',
    `status`       tinyint               DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`    varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id` bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id` bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_code` (`item_code`),
    KEY            `idx_item_type` (`item_type`),
    KEY            `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗项目字典';

-- ----------------------------
-- sys_diagnosis  诊断字典
-- ----------------------------
CREATE TABLE `sys_diagnosis`
(
    `id`             bigint       NOT NULL COMMENT '主键ID',
    `diagnosis_code` varchar(32)  NOT NULL COMMENT '诊断编码',
    `diagnosis_name` varchar(200) NOT NULL COMMENT '诊断名称',
    `diagnosis_type` tinyint               DEFAULT '1' COMMENT '诊断类型（1-西医诊断 2-中医诊断）',
    `category_name`  varchar(100)          DEFAULT NULL COMMENT '诊断分类名称',
    `parent_id`      bigint                DEFAULT '0' COMMENT '父诊断ID',
    `sort_order`     int                   DEFAULT '0' COMMENT '排序号',
    `is_common`      tinyint               DEFAULT '0' COMMENT '是否常用诊断（0-否 1-是）',
    `is_notifiable`  tinyint               DEFAULT '0' COMMENT '是否传染病（0-否 1-是）',
    `disease_stage`  varchar(100)          DEFAULT NULL COMMENT '疾病分期',
    `status`         tinyint               DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`      varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`   bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`   bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`       tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`         varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_diagnosis_code` (`diagnosis_code`),
    KEY              `idx_diagnosis_name` (`diagnosis_name`),
    KEY              `idx_is_common` (`is_common`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='诊断字典';

-- ----------------------------
-- sys_icd10  ICD-10 诊断编码
-- ----------------------------
CREATE TABLE `sys_icd10`
(
    `id`           bigint       NOT NULL COMMENT '主键ID',
    `icd_code`     varchar(20)  NOT NULL COMMENT 'ICD编码',
    `icd_name`     varchar(200) NOT NULL COMMENT '疾病名称',
    `icd_category` varchar(100)          DEFAULT NULL COMMENT '分类',
    `code_std`     tinyint      NOT NULL DEFAULT '0' COMMENT '编码口径（1-医保版贯标码 0-历史缩写码）',
    `sort_order`   int                   DEFAULT '0' COMMENT '排序',
    `status`       tinyint               DEFAULT '1' COMMENT '状态（0-停用 1-正常）',
    `create_by`    varchar(64)  NOT NULL,
    `create_by_id` bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`    varchar(64)  NOT NULL,
    `update_by_id` bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`     tinyint               DEFAULT '0',
    `remark`       varchar(500)          DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY            `idx_icd_code` (`icd_code`),
    KEY            `idx_icd_name` (`icd_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ICD-10 诊断编码';

-- ----------------------------
-- sys_icd9cm3  ICD-9-CM-3 手术编码
-- ----------------------------
CREATE TABLE `sys_icd9cm3`
(
    `id`           bigint       NOT NULL COMMENT '主键',
    `op_code`      varchar(32)  NOT NULL COMMENT '手术操作编码',
    `op_name`      varchar(300) NOT NULL COMMENT '手术操作名称',
    `op_category`  tinyint               DEFAULT NULL COMMENT '章节',
    `code_std`     tinyint      NOT NULL DEFAULT '0' COMMENT '编码口径（1-医保版贯标码 0-历史缩写码）',
    `sort_order`   int          NOT NULL DEFAULT '0' COMMENT '排序',
    `status`       tinyint      NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-正常）',
    `create_by`    varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id` bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id` bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_op_code` (`op_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ICD-9-CM-3 手术编码';

-- ----------------------------
-- sys_supplier  供应商
-- ----------------------------
CREATE TABLE `sys_supplier`
(
    `supplier_id`    bigint       NOT NULL COMMENT '供应商ID',
    `supplier_code`  varchar(32)  NOT NULL COMMENT '供应商编码',
    `supplier_name`  varchar(128) NOT NULL COMMENT '供应商名称',
    `contact_person` varchar(32)           DEFAULT NULL COMMENT '联系人',
    `phone`          varchar(32)           DEFAULT NULL COMMENT '联系电话',
    `address`        varchar(256)          DEFAULT NULL COMMENT '地址',
    `license_no`     varchar(64)           DEFAULT NULL COMMENT '营业执照号',
    `license_expiry` date                  DEFAULT NULL COMMENT '资质证照有效期',
    `rating`         tinyint               DEFAULT '3' COMMENT '评级（1-差 2-一般 3-良好 4-优秀）',
    `status`         tinyint      NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-正常）',
    `remark`         varchar(500)          DEFAULT NULL COMMENT '备注',
    `create_by`      varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`   bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`   bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`       tinyint               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    PRIMARY KEY (`supplier_id`),
    UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商';

-- ----------------------------
-- sys_patient_tag  患者标签
-- ----------------------------
CREATE TABLE `sys_patient_tag`
(
    `tag_id`       bigint      NOT NULL COMMENT '标签ID',
    `tag_name`     varchar(50) NOT NULL COMMENT '标签名称',
    `short_name`   varchar(2)           DEFAULT NULL COMMENT '标签缩写用于展示',
    `tag_color`    varchar(20)          DEFAULT NULL COMMENT '标签颜色',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id` bigint               DEFAULT NULL COMMENT '创建人ID',
    `update_by`    varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id` bigint               DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者标签';

-- ----------------------------
-- sys_infectious_disease  法定传染病目录
-- ----------------------------
CREATE TABLE `sys_infectious_disease`
(
    `id`               bigint      NOT NULL COMMENT '主键',
    `disease_code`     varchar(16) NOT NULL COMMENT '病种编码',
    `disease_name`     varchar(50) NOT NULL COMMENT '病种名称',
    `infectious_class` tinyint     NOT NULL COMMENT '传染病类别',
    `deadline_hours`   int         NOT NULL COMMENT '报卡时限',
    `icd10`            varchar(16)          DEFAULT NULL COMMENT '参考 ICD-10 编码',
    `status`           tinyint     NOT NULL DEFAULT '1' COMMENT '状态',
    `create_by`        varchar(64) NOT NULL,
    `create_by_id`     bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`        varchar(64) NOT NULL,
    `update_by_id`     bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`         tinyint     NOT NULL DEFAULT '0' COMMENT '删除标记',
    `remark`           varchar(255)         DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_infectious_disease_code` (`disease_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='法定传染病目录';

-- ----------------------------
-- sys_single_disease  单病种质控目录
-- ----------------------------
CREATE TABLE `sys_single_disease`
(
    `id`           bigint       NOT NULL COMMENT '主键（雪花）',
    `disease_code` varchar(32)  NOT NULL COMMENT '病种编码',
    `disease_name` varchar(100) NOT NULL COMMENT '病种名称',
    `icd10_prefix` varchar(200) NOT NULL COMMENT '纳入 ICD-10 前缀',
    `create_by`    varchar(64)  NOT NULL,
    `create_by_id` bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`    varchar(64)  NOT NULL,
    `update_by_id` bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`     tinyint      NOT NULL DEFAULT '0',
    `remark`       varchar(500)          DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_disease_code` (`disease_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单病种质控目录';

-- ----------------------------
-- sys_drg_mdc  DRG 主要诊断大类（MDC）目录与入组规则
-- ----------------------------
CREATE TABLE `sys_drg_mdc` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `mdc_code` varchar(8) NOT NULL COMMENT 'MDC 编码',
  `mdc_name` varchar(100) DEFAULT NULL COMMENT 'MDC 名称',
  `mdc_rule` varchar(2000) DEFAULT NULL COMMENT 'MDC 入组规则原文',
  `sort_no` int DEFAULT NULL COMMENT '排序',
  `version` varchar(32) DEFAULT NULL COMMENT '分组方案版本',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mdc_code` (`mdc_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 主要诊断大类目录';

-- ----------------------------
-- sys_drg_adrg  DRG 核心疾病诊断相关组（ADRG）目录与入组规则
-- ----------------------------
CREATE TABLE `sys_drg_adrg` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `adrg_code` varchar(16) NOT NULL COMMENT 'ADRG 编码',
  `adrg_name` varchar(200) DEFAULT NULL COMMENT 'ADRG 名称',
  `adrg_rule` varchar(2000) DEFAULT NULL COMMENT 'ADRG 入组规则原文',
  `mdc_code` varchar(8) DEFAULT NULL COMMENT '所属 MDC 编码',
  `sort_no` int DEFAULT NULL COMMENT 'MDC 内排序',
  `version` varchar(32) DEFAULT NULL COMMENT '分组方案版本',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_adrg_code` (`adrg_code`,`del_flag`),
  KEY `idx_adrg_mdc` (`mdc_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 核心组目录';

-- ----------------------------
-- sys_drg_set  分组规则引用的 ICD 码集合（集合编号 → 精确码）
-- ----------------------------
CREATE TABLE `sys_drg_set` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `set_code` varchar(24) NOT NULL COMMENT '集合编号',
  `icd_code` varchar(32) NOT NULL COMMENT 'ICD 编码',
  `icd_name` varchar(200) DEFAULT NULL COMMENT 'ICD 名称',
  `set_type` varchar(4) NOT NULL COMMENT '类型（OP-手术操作 DI-诊断 EX-CC/MCC 排除组）',
  `version` varchar(32) DEFAULT NULL COMMENT '分组方案版本',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_set_icd` (`set_code`,`icd_code`,`del_flag`),
  KEY `idx_set_icd_code` (`icd_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 分组码集合';
    `set_code`    varchar(24) NOT NULL COMMENT '集合编号',
    `icd_code`    varchar(32) NOT NULL COMMENT 'ICD 编码',
    `icd_name`    varchar(200)         DEFAULT NULL COMMENT 'ICD 名称',
    `set_type`    varchar(4)  NOT NULL COMMENT '类型（OP-手术操作 DI-诊断 EX-CC/MCC 排除组）',
    `version`     varchar(32)          DEFAULT NULL COMMENT '分组方案版本',
    `status`      tinyint     NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
    `create_by`   varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id` bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id` bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`      varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_set_icd` (`set_code`,`icd_code`,`del_flag`),
    KEY `idx_set_icd_code` (`icd_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 分组码集合';

-- ----------------------------
-- sys_drg_group  DRG 细分组与权重
-- ----------------------------
CREATE TABLE `sys_drg_group`
(
    `id`           bigint      NOT NULL COMMENT '主键ID',
    `drg_code`     varchar(32) NOT NULL COMMENT 'DRG 组编码',
    `drg_name`     varchar(200)         DEFAULT NULL COMMENT 'DRG 组名称',
    `mdc_code`     varchar(8)           DEFAULT NULL COMMENT 'MDC 主要诊断大类',
    `adrg_code`    varchar(16)          DEFAULT NULL COMMENT 'ADRG 编码',
    `drg_rule`     varchar(2000)        DEFAULT NULL COMMENT 'DRG 细分组规则原文',
    `sort_no`      int                  DEFAULT NULL COMMENT 'ADRG 内排序',
    `weight`       decimal(10, 4)       DEFAULT NULL COMMENT '权重',
    `pay_standard` decimal(10, 2)       DEFAULT NULL COMMENT '病组支付标准（元）',
    `source`       varchar(64)          DEFAULT NULL COMMENT '来源',
    `version`      varchar(32)          DEFAULT NULL COMMENT '版本号',
    `status`       tinyint     NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`    varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id` bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id` bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drg_code` (`drg_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 分组与权重';

-- ----------------------------
-- sys_drg_ccmcc  DRG 并发症合并症(CC/MCC)目录
-- ----------------------------
CREATE TABLE `sys_drg_ccmcc` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `icd_code` varchar(32) NOT NULL COMMENT '诊断编码（ICD-10）',
  `cc_level` varchar(8) NOT NULL COMMENT '级别（MCC-严重并发症合并症 CC-并发症合并症 NONE-无）',
  `excl_group` varchar(24) DEFAULT NULL COMMENT 'CC/MCC 排除组编号',
  `version` varchar(32) DEFAULT NULL COMMENT '分组方案版本（2.0/3.0）',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ccmcc_code_ver` (`icd_code`,`version`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 并发症合并症目录';

-- ----------------------------
-- sys_checkup_package  体检套餐
-- ----------------------------
CREATE TABLE `sys_checkup_package`
(
    `id`           bigint         NOT NULL COMMENT '主键ID',
    `package_name` varchar(100)   NOT NULL COMMENT '套餐名称',
    `package_code` varchar(32)             DEFAULT NULL COMMENT '套餐编码',
    `gender_limit` tinyint        NOT NULL DEFAULT '0' COMMENT '适用性别（0-不限 1-男 2-女）',
    `price`        decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '套餐价格（元）',
    `description`  varchar(500)            DEFAULT NULL COMMENT '套餐说明',
    `status`       tinyint        NOT NULL DEFAULT '1' COMMENT '启用状态',
    `create_by`    varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id` bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id` bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_package_name` (`package_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体检套餐';

-- ----------------------------
-- sys_checkup_package_item  体检套餐项目
-- ----------------------------
CREATE TABLE `sys_checkup_package_item`
(
    `id`           bigint         NOT NULL COMMENT '主键ID',
    `package_id`   bigint         NOT NULL COMMENT '套餐ID',
    `item_name`    varchar(100)   NOT NULL COMMENT '项目名称',
    `item_type`    tinyint        NOT NULL DEFAULT '1' COMMENT '项目类别（问诊/体格）（1-检验 2-检查 3-一般）',
    `ref_standard` varchar(200)            DEFAULT NULL COMMENT '参考范围/标准',
    `amount`       decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '单项金额（元）',
    `sort_order`   int            NOT NULL DEFAULT '0' COMMENT '排序',
    `create_by`    varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id` bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id` bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY            `idx_pkg` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体检套餐项目';

-- ----------------------------
-- biz_shift  班次字典
-- ----------------------------
CREATE TABLE `biz_shift`
(
    `id`                 bigint                                                       NOT NULL COMMENT '主键ID',
    `shift_name`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次名称',
    `start_time`         varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '开始时间（HH:mm）',
    `end_time`           varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '结束时间（HH:mm）',
    `cross_day`          tinyint                                                      NOT NULL DEFAULT '0' COMMENT '是否跨零点（0-不跨 1-次日收）',
    `is_night`           tinyint                                                      NOT NULL DEFAULT '0' COMMENT '是否夜班（1-夜班 0-白班）：夜班流入判定与连续夜班上限的唯一依据',
    `need_rest_hours`    decimal(4, 1)                                                NOT NULL DEFAULT '0.0' COMMENT '下此班后最短休息小时数（0-不限制；夜班通例取16）',
    `late_grace_minutes` int                                                          NOT NULL DEFAULT '15' COMMENT '迟到宽限（分钟）：签到晚于班次开始超过这个数才算迟到',
    `duration_minutes`   int                                                          NOT NULL DEFAULT '0' COMMENT '时长（分钟）',
    `dept_id`            bigint                                                                DEFAULT NULL COMMENT '适用科室ID',
    `schedule_type`      tinyint                                                               DEFAULT NULL COMMENT '班次类型（1-上午 2-下午 3-全天 4-凌晨）',
    `use_scope`          tinyint                                                      NOT NULL DEFAULT '1' COMMENT '班次适用域（1-门诊 2-病区护理排班）',
    `apply_staff_type`   tinyint                                                               DEFAULT NULL COMMENT '适用岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-全部岗位通用）',
    `status`             tinyint                                                               DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
    `create_by`          varchar(64)                                                  NOT NULL COMMENT '创建人',
    `create_by_id`       bigint                                                                DEFAULT NULL COMMENT '创建人ID',
    `create_time`        datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          varchar(64)                                                  NOT NULL COMMENT '更新人',
    `update_by_id`       bigint                                                                DEFAULT NULL COMMENT '更新人ID',
    `update_time`        datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`           tinyint                                                               DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`             varchar(500)                                                          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY                  `idx_shift_dept` (`dept_id`),
    KEY                  `idx_shift_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班次字典';

-- ----------------------------
-- sys_drug_interaction  药物相互作用知识库
-- ----------------------------
CREATE TABLE `sys_drug_interaction`
(
    `id`               bigint       NOT NULL COMMENT '主键',
    `component_a`      varchar(50)  NOT NULL COMMENT '成分关键字A',
    `component_b`      varchar(50)  NOT NULL COMMENT '成分关键字B',
    `pair_key`         varchar(120) NOT NULL COMMENT '成分对归一化键',
    `severity`         tinyint      NOT NULL COMMENT '严重度（1-禁忌 2-慎用）',
    `interaction_desc` varchar(500) NOT NULL COMMENT '相互作用后果',
    `suggestion`       varchar(500)          DEFAULT NULL COMMENT '处理建议（换药/减量/监测什么指标）',
    `status`           tinyint      NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
    `create_by`        varchar(64)  NOT NULL COMMENT '创建人',
    `create_by_id`     bigint                DEFAULT NULL COMMENT '创建人ID',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        varchar(64)  NOT NULL COMMENT '更新人',
    `update_by_id`     bigint                DEFAULT NULL COMMENT '更新人ID',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`         tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`           varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pair_key` (`pair_key`),
    KEY                `idx_interaction_severity` (`severity`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药物相互作用知识库';

-- ----------------------------
-- sys_drug_dose_limit  药品剂量上限知识库
-- ----------------------------
CREATE TABLE `sys_drug_dose_limit`
(
    `id`              bigint      NOT NULL COMMENT '主键',
    `component`       varchar(50) NOT NULL COMMENT '成分关键字',
    `dose_unit`       varchar(10) NOT NULL COMMENT '剂量单位（，只有 g/mg/ug 三值可比）',
    `max_single_dose` decimal(12, 4)       DEFAULT NULL COMMENT '单次最大量',
    `max_daily_dose`  decimal(12, 4)       DEFAULT NULL COMMENT '每日最大量',
    `note`            varchar(200)         DEFAULT NULL COMMENT '口径说明',
    `status`          tinyint     NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
    `create_by`       varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`    bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`    bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint     NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dose_component` (`component`),
    KEY               `idx_dose_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品剂量上限知识库';
