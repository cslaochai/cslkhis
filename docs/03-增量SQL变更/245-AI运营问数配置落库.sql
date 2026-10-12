-- =============================================================
-- 245-AI运营问数配置落库
-- 说明：运营问数从「代码硬编码 schema」改为「数据库配置驱动」。
--   1) sys_ai_qa_schema   可查询表（白名单）表级配置
--   2) sys_ai_qa_column   列级配置（列含义来自真实 DDL COMMENT，铺底只启用经营统计列）
--   3) sys_ai_qa_semantic 语义映射（语义词 → 表/列 + 权威口径，喂给模型）
--   4) sys_menu 新增「问数配置」按钮权限（ai:operationQa:config），仅授管理员
-- 唯一键均含 del_flag：本表删除走软删，软删后可重建同名配置不冲突。
-- id 段：24500000000000001xx（表）/ 24500000000000002xx（列）/ 24500000000000005xx（语义），
--        与雪花 id、既有铺底文件错开。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- -------------------------------------------------------------
-- 1. 建表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_ai_qa_schema`
(
    `id`          bigint       NOT NULL COMMENT '主键（雪花）',
    `table_name`  varchar(64)  NOT NULL COMMENT '数据表名',
    `table_desc`  varchar(200) NOT NULL COMMENT '表用途说明（喂给模型）',
    `is_enabled`  tinyint      NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用 0-停用）',
    `sort_order`  int          NOT NULL DEFAULT '0' COMMENT '排序号',
    `create_by`   varchar(64)  NOT NULL,
    `create_by_id` bigint COMMENT '创建人ID',
    `create_time` datetime     NOT NULL,
    `update_by`   varchar(64)  NOT NULL,
    `update_by_id` bigint COMMENT '更新人ID',
    `update_time` datetime     NOT NULL,
    `del_flag`    tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`      varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_table_name` (`table_name`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI运营问数数据表配置';

CREATE TABLE IF NOT EXISTS `sys_ai_qa_column`
(
    `id`           bigint       NOT NULL COMMENT '主键（雪花）',
    `table_name`   varchar(64)  NOT NULL COMMENT '数据表名',
    `column_name`  varchar(64)  NOT NULL COMMENT '列名',
    `column_desc`  varchar(200) NOT NULL COMMENT '列含义说明（来自表结构注释）',
    `column_type`  varchar(64)  NULL COMMENT '列类型（来自 information_schema data_type，重写刷新写入）',
    `is_filterable` tinyint     NOT NULL DEFAULT '0' COMMENT '可否用于过滤条件（1-是 0-否）',
    `is_groupable` tinyint      NOT NULL DEFAULT '1' COMMENT '可否用于分组统计（1-是 0-否）',
    `is_enabled`   tinyint      NOT NULL DEFAULT '1' COMMENT '是否喂给模型（1-启用 0-停用）',
    `sensitive_type` tinyint   NOT NULL DEFAULT '0' COMMENT '敏感类型（0-普通 1-脱敏 2-禁止出库）',
    `mask_type`    varchar(16)  NULL COMMENT '脱敏方式（phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码）',
    `sort_order`   int          NOT NULL DEFAULT '0' COMMENT '排序号',
    `create_by`    varchar(64)  NOT NULL,
    `create_by_id` bigint COMMENT '创建人ID',
    `create_time`  datetime     NOT NULL,
    `update_by`    varchar(64)  NOT NULL,
    `update_by_id` bigint COMMENT '更新人ID',
    `update_time`  datetime     NOT NULL,
    `del_flag`     tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_table_column` (`table_name`, `column_name`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI运营问数数据列配置';

CREATE TABLE IF NOT EXISTS `sys_ai_qa_semantic`
(
    `id`          bigint       NOT NULL COMMENT '主键（雪花）',
    `term`        varchar(64)  NOT NULL COMMENT '语义词/短语（问题文本包含即命中）',
    `table_name`  varchar(64)  NOT NULL COMMENT '命中表',
    `column_name` varchar(64) COMMENT '目标列（空=表级口径）',
    `scope_desc`  varchar(500) NOT NULL COMMENT '口径说明（喂给模型作为权威口径）',
    `match_mode`  tinyint      NOT NULL DEFAULT '1' COMMENT '匹配方式（1-包含 2-精确）',
    `priority`    int          NOT NULL DEFAULT '0' COMMENT '优先级（大者先）',
    `is_enabled`  tinyint      NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用 0-停用）',
    `create_by`   varchar(64)  NOT NULL,
    `create_by_id` bigint COMMENT '创建人ID',
    `create_time` datetime     NOT NULL,
    `update_by`   varchar(64)  NOT NULL,
    `update_by_id` bigint COMMENT '更新人ID',
    `update_time` datetime     NOT NULL,
    `del_flag`    tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`      varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_term` (`term`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI运营问数语义映射配置';

-- -------------------------------------------------------------
-- 2. 表级配置铺底（11 张经营统计白名单表，与既有 OperationSchemaCatalog 一致）
-- -------------------------------------------------------------
INSERT INTO sys_ai_qa_schema (id, table_name, table_desc, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
VALUES ('2450000000000000101', 'sys_department', '科室字典：科室维度统计的维度表', 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000102', 'sys_ward', '病区与床位：床位总数/占用情况统计', 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000103', 'biz_admission', '住院记录：在院/入院/出院人数统计', 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000104', 'biz_visit', '门诊就诊：门诊就诊量与就诊费用', 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000105', 'biz_fee_record', '记账行（费用事实）：项目费用明细', 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000106', 'biz_settlement_bill', '结算账单：应收/实收/优惠/医保分账', 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000107', 'biz_payment_txn', '收退款流水：净收款/支付方式统计', 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000108', 'biz_prescription', '处方：处方量与处方金额', 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000109', 'biz_prescription_detail', '处方用药明细：药品销售明细', 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000110', 'biz_laboratory_record', '检验记录：检验量与检验费用', 1, 10, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL),
       ('2450000000000000111', 'biz_operation_apply', '手术申请：手术量与手术级别统计', 1, 11, 'admin', '1', NOW(), 'admin', '1', NOW(), 0, NULL);

-- -------------------------------------------------------------
-- 3. 列级配置铺底（column_name/column_desc 取自真实 DDL COMMENT；只启用经营统计相关列，
--    患者个体信息列与审计列一律停用不喂给模型）
-- -------------------------------------------------------------
-- sys_department
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000201', 'sys_department', 'dept_code', '科室编码（唯一）', 1, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000202', 'sys_department', 'dept_name', '科室名称', 1, 1, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000203', 'sys_department', 'dept_type', '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他，多个逗号分隔）', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000204', 'sys_department', 'is_open', '是否开诊（0-否 1-是）', 1, 0, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000205', 'sys_department', 'status', '状态（0-停用 1-启用）', 1, 0, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- sys_ward
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000211', 'sys_ward', 'ward_id', '病区ID', 1, 1, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000212', 'sys_ward', 'ward_code', '病区编码', 1, 0, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000213', 'sys_ward', 'ward_name', '病区名称', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000214', 'sys_ward', 'dept_id', '所属科室ID', 1, 0, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000215', 'sys_ward', 'total_beds', '总床位数', 0, 0, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000216', 'sys_ward', 'occupied_beds', '已占用床位数', 0, 0, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000217', 'sys_ward', 'status', '状态（0-停用 1-正常）', 1, 0, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_admission
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000221', 'biz_admission', 'admission_id', '入院ID', 1, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000222', 'biz_admission', 'admission_no', '入院记录号', 0, 0, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000223', 'biz_admission', 'admit_dept_id', '入院科室ID', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000224', 'biz_admission', 'ward_id', '病区ID', 1, 1, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000225', 'biz_admission', 'nursing_level', '护理等级（1-特级 2-一级 3-二级 4-三级）', 1, 1, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000226', 'biz_admission', 'admit_time', '入院时间', 1, 0, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000227', 'biz_admission', 'discharge_time', '出院时间', 1, 0, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000228', 'biz_admission', 'admit_status', '状态（0-已出院 1-在院）', 1, 1, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000229', 'biz_admission', 'admit_way', '入院途径（1-门诊 2-急诊 3-转院 4-其他）', 1, 1, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_visit
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000231', 'biz_visit', 'visit_id', '就诊次ID', 1, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000232', 'biz_visit', 'visit_no', '就诊次编号', 0, 0, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000233', 'biz_visit', 'start_time', '就诊开始时间', 1, 0, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000234', 'biz_visit', 'end_time', '就诊结束时间', 1, 0, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000235', 'biz_visit', 'total_amount', '本次就诊总费用', 0, 0, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000236', 'biz_visit', 'visit_status', '就诊状态（0-已取消 1-进行中 2-已完成）', 1, 1, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_fee_record
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000241', 'biz_fee_record', 'fee_no', '记账流水号', 0, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000242', 'biz_fee_record', 'encounter_type', '就诊类型（1-门诊 2-住院）', 1, 1, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000243', 'biz_fee_record', 'dept_id', '费用归属科室', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000244', 'biz_fee_record', 'dept_name', '科室名称（快照）', 1, 1, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000245', 'biz_fee_record', 'item_type', '项目类型（1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材）', 1, 1, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000246', 'biz_fee_record', 'item_name', '项目名称', 1, 1, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000247', 'biz_fee_record', 'catalog_type', '医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）', 1, 1, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000248', 'biz_fee_record', 'price', '单价', 0, 0, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000249', 'biz_fee_record', 'quantity', '数量', 0, 0, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000250', 'biz_fee_record', 'amount', '金额=单价×数量', 0, 0, 1, 10, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000251', 'biz_fee_record', 'fee_status', '记账状态（1-待结算 2-已锁定 3-已结算 4-已红冲）', 1, 1, 1, 11, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000252', 'biz_fee_record', 'book_time', '记账时间', 1, 0, 1, 12, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000253', 'biz_fee_record', 'refunded_amount', '累计已冲金额', 0, 0, 1, 13, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_settlement_bill
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000261', 'biz_settlement_bill', 'bill_no', '账单号', 0, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000262', 'biz_settlement_bill', 'encounter_type', '就诊类型（1-门诊 2-住院）', 1, 1, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000263', 'biz_settlement_bill', 'bill_type', '账单类型（1-挂号费结算 2-门诊诊间结算 3-住院中途结算 4-出院结算）', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000264', 'biz_settlement_bill', 'total_amount', '应收合计', 0, 0, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000265', 'biz_settlement_bill', 'discount_amount', '院内优惠/抹零', 0, 0, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000266', 'biz_settlement_bill', 'settlement_mode', '结算方式（1-自费 2-医保）', 1, 1, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000267', 'biz_settlement_bill', 'pool_amount', '医保统筹支付', 0, 0, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000268', 'biz_settlement_bill', 'account_amount', '医保个人账户支付', 0, 0, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000269', 'biz_settlement_bill', 'self_amount', '个人自费', 0, 0, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000270', 'biz_settlement_bill', 'payable_amount', '患者应缴 = total - discount - pool - account', 0, 0, 1, 10, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000271', 'biz_settlement_bill', 'paid_amount', '已收合计', 0, 0, 1, 11, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000272', 'biz_settlement_bill', 'refund_amount', '本账单已退合计', 0, 0, 1, 12, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000273', 'biz_settlement_bill', 'bill_status', '账单状态（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）', 1, 1, 1, 13, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000274', 'biz_settlement_bill', 'bill_date', '账务归属日', 1, 0, 1, 14, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000275', 'biz_settlement_bill', 'bill_time', '结算生成时间', 1, 0, 1, 15, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000276', 'biz_settlement_bill', 'pay_time', '收讫时间', 1, 0, 1, 16, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_payment_txn
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000281', 'biz_payment_txn', 'txn_no', '支付流水号', 0, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000282', 'biz_payment_txn', 'direction', '资金方向（1-收款 2-退款）', 1, 1, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000283', 'biz_payment_txn', 'pay_method', '支付方式（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000284', 'biz_payment_txn', 'amount', '金额（收款为正、退款为负）', 0, 0, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000285', 'biz_payment_txn', 'txn_status', '流水状态（1-成功 2-已冲正）', 1, 1, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000286', 'biz_payment_txn', 'refund_method', '退费方式（1-原路退回 2-现金退回 3-余额退回）', 1, 1, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000287', 'biz_payment_txn', 'channel_txn_no', '渠道流水号', 0, 0, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000288', 'biz_payment_txn', 'txn_time', '交易时间', 1, 0, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000289', 'biz_payment_txn', 'txn_date', '交易归属日', 1, 0, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_prescription
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000301', 'biz_prescription', 'prescription_no', '处方号', 0, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000302', 'biz_prescription', 'visit_date', '就诊日期', 1, 0, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000303', 'biz_prescription', 'dept_id', '科室ID', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000304', 'biz_prescription', 'dept_name', '科室名称', 1, 1, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000305', 'biz_prescription', 'doctor_id', '医生ID', 1, 1, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000306', 'biz_prescription', 'doctor_name', '医生姓名', 1, 1, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000307', 'biz_prescription', 'prescription_type', '处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）', 1, 1, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000308', 'biz_prescription', 'prescription_source', '处方来源（1-门诊处方 2-急诊处方 3-住院处方）', 1, 1, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000309', 'biz_prescription', 'total_amount', '总金额', 0, 0, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000310', 'biz_prescription', 'drug_count', '药品数量', 0, 0, 1, 10, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000311', 'biz_prescription', 'prescription_status', '处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）', 1, 1, 1, 11, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000312', 'biz_prescription', 'payment_status', '缴费状态（0-未缴费 1-已缴费 2-已退费）', 1, 1, 1, 12, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000313', 'biz_prescription', 'pay_amount', '实付金额', 0, 0, 1, 13, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000314', 'biz_prescription', 'pay_method', '支付方式（1-现金 2-微信 3-支付宝 4-医保卡 5-余额）', 1, 1, 1, 14, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000315', 'biz_prescription', 'pay_time', '缴费时间', 1, 0, 1, 15, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000316', 'biz_prescription', 'submit_time', '提交时间', 1, 0, 1, 16, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000317', 'biz_prescription', 'audit_time', '审核时间', 1, 0, 1, 17, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_prescription_detail
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000321', 'biz_prescription_detail', 'prescription_id', '处方ID', 1, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000322', 'biz_prescription_detail', 'prescription_no', '处方号', 1, 0, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000323', 'biz_prescription_detail', 'drug_code', '药品编码', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000324', 'biz_prescription_detail', 'drug_name', '药品名称', 1, 1, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000325', 'biz_prescription_detail', 'specification', '规格', 0, 0, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000326', 'biz_prescription_detail', 'unit', '单位', 0, 0, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000327', 'biz_prescription_detail', 'quantity', '数量', 0, 0, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000328', 'biz_prescription_detail', 'price', '单价', 0, 0, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000329', 'biz_prescription_detail', 'amount', '金额', 0, 0, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000330', 'biz_prescription_detail', 'detail_status', '明细状态（1-正常 2-已发药 3-已退药）', 1, 1, 1, 10, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000331', 'biz_prescription_detail', 'payment_status', '缴费状态（0-未缴费 1-已缴费 2-已退费）', 1, 1, 1, 11, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_laboratory_record
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000341', 'biz_laboratory_record', 'record_no', '检验记录号（唯一）', 0, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000342', 'biz_laboratory_record', 'apply_dept_id', '申请科室ID', 1, 1, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000343', 'biz_laboratory_record', 'apply_dept_name', '申请科室', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000344', 'biz_laboratory_record', 'visit_date', '就诊日期', 1, 0, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000345', 'biz_laboratory_record', 'laboratory_item_code', '检验项目编码', 1, 1, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000346', 'biz_laboratory_record', 'laboratory_item_name', '检验项目名称', 1, 1, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000347', 'biz_laboratory_record', 'laboratory_dept_id', '检验科室ID', 1, 1, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000348', 'biz_laboratory_record', 'laboratory_dept_name', '检验科室名称', 1, 1, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000349', 'biz_laboratory_record', 'specimen_type', '标本类型（血液、尿液、粪便等）', 1, 1, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000350', 'biz_laboratory_record', 'record_status', '记录状态（1-已登记 2-已采样 3-已接收 4-检测中 5-已出结果 6-已审核 7-已发布 8-已取消）', 1, 1, 1, 10, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000351', 'biz_laboratory_record', 'price', '检验费用', 0, 0, 1, 11, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000352', 'biz_laboratory_record', 'sample_time', '采样时间', 1, 0, 1, 12, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000353', 'biz_laboratory_record', 'receive_time', '接收时间', 1, 0, 1, 13, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000354', 'biz_laboratory_record', 'execute_time', '检测时间', 1, 0, 1, 14, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);
-- biz_operation_apply
INSERT INTO sys_ai_qa_column (id, table_name, column_name, column_desc, is_filterable, is_groupable, is_enabled, sort_order, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000361', 'biz_operation_apply', 'apply_no', '手术申请单号', 0, 0, 1, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000362', 'biz_operation_apply', 'apply_dept_id', '申请科室ID', 1, 1, 1, 2, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000363', 'biz_operation_apply', 'apply_dept_name', '申请科室名称（快照）', 1, 1, 1, 3, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000364', 'biz_operation_apply', 'apply_doctor_id', '申请医生ID', 1, 1, 1, 4, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000365', 'biz_operation_apply', 'apply_doctor_name', '申请医生姓名（快照）', 1, 1, 1, 5, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000366', 'biz_operation_apply', 'apply_time', '申请时间', 1, 0, 1, 6, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000367', 'biz_operation_apply', 'planned_operation_code', '拟施手术编码', 1, 1, 1, 7, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000368', 'biz_operation_apply', 'planned_operation_name', '拟施手术名称', 1, 1, 1, 8, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000369', 'biz_operation_apply', 'operation_level', '手术级别（1-一级 2-二级 3-三级 4-四级）', 1, 1, 1, 9, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000370', 'biz_operation_apply', 'incision_level', '切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）', 1, 1, 1, 10, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000371', 'biz_operation_apply', 'anesthesia_type', '麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）', 1, 1, 1, 11, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000372', 'biz_operation_apply', 'is_emergency', '是否急诊手术（0-否 1-是）', 1, 1, 1, 12, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000373', 'biz_operation_apply', 'is_main', '是否主要手术', 1, 1, 1, 13, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000374', 'biz_operation_apply', 'operation_room', '手术间', 1, 1, 1, 14, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000375', 'biz_operation_apply', 'planned_start_time', '计划开始时间', 1, 0, 1, 15, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000376', 'biz_operation_apply', 'planned_end_time', '计划结束时间', 1, 0, 1, 16, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000377', 'biz_operation_apply', 'surgeon_id', '主刀医师ID（员工ID）', 1, 1, 1, 17, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000378', 'biz_operation_apply', 'surgeon_name', '主刀医师姓名（快照）', 1, 1, 1, 18, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000379', 'biz_operation_apply', 'operation_status', '状态（0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消）', 1, 1, 1, 19, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000380', 'biz_operation_apply', 'operation_start_time', '实际开始时间（切皮）', 1, 0, 1, 20, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);

-- -------------------------------------------------------------
-- 4. 语义映射铺底（语义词 → 表/列 + 权威口径；模型生成 SQL 时优先遵循）
-- -------------------------------------------------------------
INSERT INTO sys_ai_qa_semantic (id, term, table_name, column_name, scope_desc, match_mode, priority, is_enabled, create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag)
VALUES ('2450000000000000501', '收入', 'biz_settlement_bill', NULL,
        '账单收入口径：统计已支付账单（bill_status=3）的 paid_amount（已收合计）；统筹走 pool_amount、个账走 account_amount、自付走 self_amount，三者分列不要混用。', 1, 10, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000502', '净收款', 'biz_payment_txn', NULL,
        '净收款口径：SUM(amount)，收款为正、退款为负，仅统计 txn_status=1（成功）的流水。', 1, 10, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000503', '门诊就诊量', 'biz_visit', NULL,
        '门诊就诊量口径：COUNT(*) 且 visit_status=2（已完成），按 start_time 所在日期归属。注意：biz_visit 表没有科室列，不支持按科室维度分组；科室维度统计须走挂号表（当前不在白名单），不要臆造 join 列。', 1, 9, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000504', '门诊人次', 'biz_visit', NULL,
        '门诊人次口径：COUNT(*) 且 visit_status=2（已完成），按 start_time 所在日期归属。注意：biz_visit 表没有科室列，不支持按科室维度分组；不要臆造 join 列。', 1, 9, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000505', '在院', 'biz_admission', NULL,
        '在院人数口径：COUNT(*) 且 admit_status=1（在院）。按病区分组用 ward_id JOIN sys_ward.ward_id；按科室分组用 admit_dept_id JOIN sys_department.id（字典主键是 id，dept_code 是编码，禁止用于关联）。', 1, 9, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000506', '入院', 'biz_admission', NULL,
        '入院人数口径：按 admit_time 所在日期 COUNT(*)。按科室分组用 admit_dept_id JOIN sys_department.id；按病区分组用 ward_id JOIN sys_ward.ward_id（dept_code 是编码，禁止用于关联）。', 1, 9, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000507', '出院', 'biz_admission', NULL,
        '出院人数口径：按 discharge_time 所在日期 COUNT(*)，admit_status=0（已出院）。按科室分组用 admit_dept_id JOIN sys_department.id；按病区分组用 ward_id JOIN sys_ward.ward_id（dept_code 是编码，禁止用于关联）。', 1, 9, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000508', '床位', 'sys_ward', NULL,
        '床位口径：总床位数 total_beds、已占床 occupied_beds；占用率=occupied_beds/total_beds。病区维度主键是 sys_ward.ward_id，业务表按各自 ward_id 与之关联。', 1, 8, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000509', '发药', 'biz_prescription', NULL,
        '发药处方口径：prescription_status=4（已发药）；处方金额用 total_amount，处方量 COUNT(*)。', 1, 9, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000510', '药品销售', 'biz_prescription_detail', NULL,
        '药品销售口径：SUM(amount)，明细排除 detail_status=3（已退药）。', 1, 8, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000511', '费用', 'biz_fee_record', NULL,
        '费用口径：SUM(amount)，必须排除 fee_status=4（已红冲）；门诊/住院用 encounter_type（1-门诊 2-住院）区分。', 1, 9, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000512', '统筹', 'biz_settlement_bill', NULL,
        '医保统筹口径：SUM(pool_amount)，仅医保结算（settlement_mode=2）的账单。', 1, 8, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000513', '支付方式', 'biz_payment_txn', NULL,
        '支付方式口径：按 pay_method（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）分组 SUM(amount)，仅 txn_status=1。', 1, 8, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000514', '检验', 'biz_laboratory_record', NULL,
        '检验量口径：COUNT(*)，排除 record_status=8（已取消）；检验费用 SUM(price)。', 1, 8, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000515', '手术', 'biz_operation_apply', NULL,
        '手术量口径：COUNT(*)，排除 operation_status=4（已取消）；级别按 operation_level（1-一级 2-二级 3-三级 4-四级）。', 1, 8, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000516', '科室', 'sys_department', NULL,
        '科室字典：维度表。主键 id，dept_code 为编码（唯一但禁止用于业务关联）；业务表按各自 *_dept_id（如 admit_dept_id、apply_dept_id、laboratory_dept_id）JOIN sys_department.id。dept_type（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）用于筛选科室类型。', 1, 5, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0),
       ('2450000000000000517', '处方量', 'biz_prescription', NULL,
        '处方量口径：COUNT(*)，排除 prescription_status=5（已取消）与 6（已退药）。', 1, 8, 1, 'admin', '1', NOW(), 'admin', '1', NOW(), 0);

-- -------------------------------------------------------------
-- 5. 菜单按钮权限：问数配置（挂报表统计目录下，跟随 2941 模式）
--    守卫三道键判重（id OR menu_key OR permission），跑完需回查
--    create_time/update_time 显式给值：本库这两列 NOT NULL 且无默认值（1364）
-- -------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id, create_time, update_time)
SELECT '2944', '问数配置', '1200', 2, 3, '', '', 'ai.operationQa.config', '', 'ai:operationQa:config', 0, 0, 1, 1,
       'admin', 'admin', 0, NULL, '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = '2944' OR menu_key = 'ai.operationQa.config' OR permission = 'ai:operationQa:config');

-- 授权：问数配置仅授管理员（role_id=1），与知识库维护（2943）同口径
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '99441', '1', '2944', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '2944');

COMMIT;

-- 回查（人工核对用，勿删）：
-- SELECT id, menu_name, permission FROM sys_menu WHERE id IN ('2944');
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = '2944';
-- SELECT COUNT(*) FROM sys_ai_qa_schema WHERE del_flag = 0;
-- SELECT COUNT(*) FROM sys_ai_qa_column WHERE del_flag = 0;
-- SELECT COUNT(*) FROM sys_ai_qa_semantic WHERE del_flag = 0;
