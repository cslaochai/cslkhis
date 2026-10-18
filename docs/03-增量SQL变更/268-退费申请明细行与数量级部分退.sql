-- 268-退费申请明细行（来源单据圈选 + 数量级部分退）
-- 背景：biz_refund_apply 只有「退费类型 + 退费金额」两个旋钮，没有"勾哪几行、各退多少数量"。
--       一张账单混了多张处方时，想退其中指定一张只能靠把金额凑到行边界（ pickRefundTargets
--       的前缀和提示）；半条明细（共 5 盒退 2 盒）更是只能整行退。而 L1 的 reversePartial
--       早就支持数量级部分红冲、支付层 patientShare 也按本次冲减额比例摊过 —— 缺的只是
--       申请单把"圈选结果"落成明细行，执行时按明细行退。
-- 表：biz_refund_apply_item 一行 = 申请单要退的一条记账行 + 退费数量（金额快照）。
--     存量申请单没有明细行，executeRefund 回退旧的「金额贪心凑整行」逻辑，行为不变。
-- 顺序：先发布后端再执行本文件。

CREATE TABLE IF NOT EXISTS `biz_refund_apply_item`
(
    `id`               bigint         NOT NULL COMMENT '主键（雪花）',
    `apply_id`         bigint         NOT NULL COMMENT '退费申请ID',
    `fee_record_id`    bigint         NOT NULL COMMENT '来源记账行ID',
    `refund_quantity`  decimal(10, 2) NOT NULL COMMENT '本次退费数量（=该行剩余量时即整行退）',
    `refund_amount`    decimal(12, 2) NOT NULL COMMENT '本次退费金额（快照：整行=剩余金额，部分=单价×数量）',
    `item_name`        varchar(200)   NOT NULL COMMENT '项目名称（快照）',
    `source_no`        varchar(64) COMMENT '来源单据号快照（处方号/申请单号；手工补记账无来源单为空）',
    `create_by`        varchar(64)    NOT NULL,
    `create_by_id`     bigint COMMENT '创建人ID',
    `create_time`      datetime       NOT NULL,
    `update_by`        varchar(64)    NOT NULL,
    `update_by_id`     bigint COMMENT '更新人ID',
    `update_time`      datetime       NOT NULL,
    `del_flag`         tinyint        NOT NULL DEFAULT '0',
    `remark`           varchar(500),
    PRIMARY KEY (`id`),
    KEY                `idx_raitem_apply` (`apply_id`),
    KEY                `idx_raitem_fee` (`fee_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退费申请明细行';
