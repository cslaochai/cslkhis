-- ============================================================
-- 248-AI运营问数表间关系.sql
-- 前置：245/246/247 已执行
-- 说明：知识图谱第三层 —— 表间 join 关系（权威关联键），问数生成段按需注入，
--       模型写 JOIN 不再靠猜（历史上曾臆造 dept 列被闸门拦下）。
--   relation_type 1-业务关联（同域单据链） 2-维度关联（取维度名称，如科室）
-- 幂等：建表 IF NOT EXISTS；铺底按唯一键（表+列×2）UPDATE 兜底
-- ============================================================

CREATE TABLE IF NOT EXISTS `sys_ai_qa_relation` (
    `id`            bigint       NOT NULL COMMENT '主键（雪花）',
    `source_table`  varchar(64)  NOT NULL COMMENT '源表名',
    `source_column` varchar(64)  NOT NULL COMMENT '源表关联列',
    `target_table`  varchar(64)  NOT NULL COMMENT '目标表名',
    `target_column` varchar(64)  NOT NULL COMMENT '目标表关联列',
    `relation_type` tinyint      NOT NULL DEFAULT '1' COMMENT '关系类型（1-业务关联 2-维度关联）',
    `description`   varchar(200) NOT NULL COMMENT '关系说明（喂给模型：何时用此关联）',
    `is_enabled`    tinyint      NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用 0-停用）',
    `sort_order`    int          NOT NULL DEFAULT '0' COMMENT '排序号',
    `create_by_id`  bigint COMMENT '创建人ID',
    `create_by`     varchar(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by_id`  bigint COMMENT '更新人ID',
    `update_by`     varchar(64)  NOT NULL DEFAULT '' COMMENT '更新人',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`        varchar(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rel_src_tgt` (`source_table`, `source_column`, `target_table`, `target_column`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI运营问数表间关系配置（join 权威键）';

-- 兼容旧版建表缺列（CREATE TABLE IF NOT EXISTS 跳过场景）：幂等补 create_by/update_by
SET @has_cb := (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'sys_ai_qa_relation' AND column_name = 'create_by');
SET @sql_cb := IF(@has_cb = 0,
    'ALTER TABLE sys_ai_qa_relation ADD COLUMN create_by varchar(64) NOT NULL DEFAULT \'\' COMMENT \'创建人\' AFTER create_by_id',
    'SELECT 1');
PREPARE s_cb FROM @sql_cb;
EXECUTE s_cb;
DEALLOCATE PREPARE s_cb;
SET @has_ub := (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'sys_ai_qa_relation' AND column_name = 'update_by');
SET @sql_ub := IF(@has_ub = 0,
    'ALTER TABLE sys_ai_qa_relation ADD COLUMN update_by varchar(64) NOT NULL DEFAULT \'\' COMMENT \'更新人\' AFTER update_by_id',
    'SELECT 1');
PREPARE s_ub FROM @sql_ub;
EXECUTE s_ub;
DEALLOCATE PREPARE s_ub;

-- 铺底：白名单 11 表权威关联（与语义口径对齐：按科室 join sys_department.id；dept_code 禁止关联）
INSERT INTO `sys_ai_qa_relation`
    (`id`, `source_table`, `source_column`, `target_table`, `target_column`, `relation_type`, `description`, `is_enabled`, `sort_order`)
VALUES
    (2480000000000000501, 'biz_admission', 'admit_dept_id', 'sys_department', 'id', 2, '入院科室→科室维度表：按科室统计入院人数/入院患者时 join 此键取科室名', 1, 1),
    (2480000000000000502, 'biz_fee_record', 'dept_id', 'sys_department', 'id', 2, '费用归属科室→科室维度表：费用/收入按科室统计时 join 此键', 1, 2),
    (2480000000000000503, 'biz_prescription', 'dept_id', 'sys_department', 'id', 2, '处方开立科室→科室维度表：处方量/药品按科室统计时 join 此键', 1, 3),
    (2480000000000000504, 'biz_laboratory_record', 'apply_dept_id', 'sys_department', 'id', 2, '检验申请科室→科室维度表：检验量按申请科室统计时 join 此键', 1, 4),
    (2480000000000000505, 'biz_laboratory_record', 'laboratory_dept_id', 'sys_department', 'id', 2, '检验执行科室→科室维度表：检验量按执行科室统计时 join 此键', 1, 5),
    (2480000000000000506, 'biz_operation_apply', 'apply_dept_id', 'sys_department', 'id', 2, '手术申请科室→科室维度表：手术量按科室统计时 join 此键', 1, 6),
    (2480000000000000507, 'biz_fee_record', 'bill_id', 'biz_settlement_bill', 'id', 1, '记账行→结算账单：费用归账口径，按账单统计费用时 join 此键', 1, 7),
    (2480000000000000508, 'biz_payment_txn', 'bill_id', 'biz_settlement_bill', 'id', 1, '支付流水→结算账单：收款/净收款按账单归集时 join 此键', 1, 8),
    (2480000000000000509, 'biz_prescription_detail', 'prescription_id', 'biz_prescription', 'id', 1, '处方明细→处方主表：药品明细归处方时 join 此键', 1, 9),
    (2480000000000000510, 'biz_admission', 'visit_id', 'biz_visit', 'visit_id', 1, '入院单→就诊次：入院与就诊次关联时 join 此键', 1, 10),
    (2480000000000000511, 'biz_operation_apply', 'admission_id', 'biz_admission', 'admission_id', 1, '手术申请→入院单：住院手术按入院归集时 join 此键', 1, 11),
    (2480000000000000512, 'biz_admission', 'ward_id', 'sys_ward', 'ward_id', 2, '住院→病区维度表：按病区统计床位/在院人数时 join 此键', 1, 12),
    (2480000000000000513, 'sys_ward', 'dept_id', 'sys_department', 'id', 2, '病区→所属科室维度表：病区维度转科室维度时 join 此键', 1, 13)
ON DUPLICATE KEY UPDATE
    `source_column` = VALUES(`source_column`),
    `target_column` = VALUES(`target_column`),
    `description` = VALUES(`description`),
    `relation_type` = VALUES(`relation_type`),
    `is_enabled` = 1,
    `update_time` = NOW();
