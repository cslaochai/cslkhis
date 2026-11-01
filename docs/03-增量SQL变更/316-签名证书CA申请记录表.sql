-- 316 签名证书 CA 申请记录表
-- 背景：CA 申请的状态原本覆盖记在 sys_sign_cert 的 ca_* 列上，一证一行只留最后一次，
--       重提的历史、驳回原因、各阶段时间全部丢失。本表改为「一次申请一行」，
--       证书表只保留「当前挂在哪张 CA 申请单上」。
-- 幂等：建表 IF NOT EXISTS；迁移按 external_request_id 唯一键判重，重复执行 0 行。
-- 回滚：DROP TABLE sys_sign_ca_apply;（本表不删证书表任何列，回滚不影响发证）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

CREATE TABLE IF NOT EXISTS `sys_sign_ca_apply`
(
    `id`                  bigint       NOT NULL COMMENT '主键ID',
    `cert_id`             bigint       NOT NULL COMMENT '证书ID',
    `cert_no`             varchar(32)  NOT NULL COMMENT '证书编号',
    `apply_seq`           int         NOT NULL DEFAULT '1' COMMENT '第几次申请',
    `external_request_id` varchar(64)  NOT NULL COMMENT 'HIS侧申请请求号',
    `apply_no`            varchar(32) COMMENT 'CA申请单号',
    `template_code`       varchar(64) COMMENT 'CA证书模板编码',
    `subject_dn`          varchar(255) COMMENT '证书主体DN',
    `apply_status`        varchar(16)  NOT NULL COMMENT 'CA申请状态（PENDING-待审核 APPROVED-审核通过待签发 ISSUED-已签发 REJECTED-已驳回 CANCELLED-已撤销 FAILED-提交失败）',
    `ca_message`          varchar(500) COMMENT 'CA回执说明',
    `ca_issuer`           varchar(128) COMMENT '签发CA名称',
    `submitted_at`        datetime     NOT NULL DEFAULT (now()) COMMENT '提交CA时间',
    `reviewed_at`         datetime COMMENT 'CA审核时间',
    `issued_at`           datetime COMMENT 'CA签发时间',
    `bound_at`            datetime COMMENT '证书回填生效时间',
    `create_by`           varchar(64)  NOT NULL COMMENT '创建人',
    `create_time`         datetime     NOT NULL DEFAULT (now()) COMMENT '创建时间',
    `update_by`           varchar(64)  NOT NULL COMMENT '更新人',
    `update_time`         datetime     NOT NULL DEFAULT (now()) COMMENT '更新时间',
    `del_flag`            tinyint      NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-已删除）',
    `create_by_id`        bigint       NOT NULL COMMENT '创建人ID',
    `update_by_id`        bigint       NOT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ext_request_id` (`external_request_id`),
    KEY                   `idx_caapply_cert` (`cert_id`,`submitted_at`),
    KEY                   `idx_caapply_apply_no` (`apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='签名证书CA申请记录';

-- 存量迁移：把证书表上「最后一次申请」的现状各造一条流水行。
-- 审核/签发/回填三个时间旧库未留存，留空；submitted_at 取证书行更新时间（只精确到「最后一次改动」）。
INSERT INTO `sys_sign_ca_apply`
(`id`, `cert_id`, `cert_no`, `apply_seq`, `external_request_id`, `apply_no`, `apply_status`, `ca_message`,
 `submitted_at`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
SELECT c.`id`,
       c.`id`,
       c.`cert_no`,
       1,
       CONCAT(c.`cert_no`, '-MIGRATED'),
       c.`ca_apply_no`,
       IFNULL(NULLIF(c.`ca_apply_status`, ''), 'PENDING'),
       c.`ca_last_message`,
       c.`update_time`,
       c.`update_by`,
       c.`update_time`,
       c.`update_by`,
       c.`update_time`,
       0,
       c.`update_by_id`,
       c.`update_by_id`
FROM `sys_sign_cert` c
WHERE (c.`ca_apply_no` IS NOT NULL OR c.`ca_apply_status` IS NOT NULL)
  AND NOT EXISTS (SELECT 1
                  FROM `sys_sign_ca_apply` a
                  WHERE a.`external_request_id` = CONCAT(c.`cert_no`, '-MIGRATED'));

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- 回查
SELECT COUNT(*) AS 申请记录行数 FROM `sys_sign_ca_apply`;
SELECT `cert_no`, `apply_seq`, `apply_no`, `apply_status`, `submitted_at`, `ca_message`
FROM `sys_sign_ca_apply`
ORDER BY `submitted_at` DESC
LIMIT 10;
