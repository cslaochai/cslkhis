-- 255-渠道回调留痕表
-- 目的：渠道异步通知（支付宝/微信）逐条留痕 —— 通知原文、验签结果、处理结论、应答内容。
-- 缺了它，「渠道重试风暴」看不出来、争议时拿不出「几点收到过什么」的凭证、
-- 排查跨系统串单时只有一行 warn 日志可看（日志会滚掉，表不会）。
-- 口径：本表只追加不修改；无登录态的通知（验签失败/查无此单）操作人列为空，不塞假名字（填了就把「没人能归因」这件事藏进库里）。

CREATE TABLE IF NOT EXISTS `biz_pay_notify_log`
(
    `id`            bigint         NOT NULL COMMENT '主键（雪花）',
    `pay_no`        varchar(32) COMMENT '支付单号（通知报文里的商户订单号）',
    `channel`       tinyint        NOT NULL COMMENT '支付渠道（1-微信 2-支付宝 3-云闪付）',
    `notify_scene`  tinyint        NOT NULL COMMENT '通知入口（1-收费台出码 2-患者端小程序）',
    `trade_no`      varchar(64) COMMENT '渠道交易号',
    `trade_status`  varchar(32) COMMENT '渠道交易状态原文',
    `total_amount`  decimal(10, 2) COMMENT '渠道报文金额（元）',
    `app_id`        varchar(32) COMMENT '渠道应用ID',
    `sign_verified` tinyint        NOT NULL COMMENT '验签结果（1-通过 0-未通过）',
    `notify_result` tinyint        NOT NULL COMMENT '处理结果（1-已入账 2-重复通知 3-未付款通知 4-验签失败 5-支付单不存在 6-渠道不符 7-金额非法 8-通知不受理 9-入账异常）',
    `ack_body`      varchar(16)    NOT NULL COMMENT '应答内容（success-已受理 failure-要求重试）',
    `raw_body`      text           NOT NULL COMMENT '通知原文（表单参数序列化）',
    `error_msg`     varchar(500) COMMENT '拒收/异常原因',
    `create_by`     varchar(64) COMMENT '创建人',
    `create_by_id`  bigint COMMENT '创建人ID',
    `create_time`   datetime       NOT NULL COMMENT '创建时间',
    `update_by`     varchar(64) COMMENT '更新人',
    `update_by_id`  bigint COMMENT '更新人ID',
    `update_time`   datetime       NOT NULL COMMENT '更新时间',
    `del_flag`      tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    PRIMARY KEY (`id`),
    KEY            `idx_pay_no` (`pay_no`),
    KEY            `idx_result_time` (`notify_result`, `create_time`),
    KEY            `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渠道异步通知留痕';

-- 字典段与 Java 枚举同码同义（码值权威在 his-pay/enums/PayNotifySceneEnum、PayNotifyResultEnum）
-- 本表两列由后端拿码值做分支（决定是否应答重试、是否入账），属封闭集合，按口径走枚举、不落操作员维护的字典。
