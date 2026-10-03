-- =============================================================================
-- 221 · 转人工工单化（留言升级为可受理工单 + 流转留痕）
--
-- 【为什么现在才做】
-- sql/216 的 biz_service_message 只是"患者能留一句话"，没有受理端：
-- 患者提交后不知道有没有人看、看到哪一步、什么时候解决，客服也没有任何入口认领。
-- 那是**半截闭环** —— 留言落库了，但没有人对它负责。
-- 这一刀把留言升级成工单：患者提单 → 客服受理 → 回复 → 办结 → 患者确认/重开，
-- 每一步都留痕（biz_service_ticket_log），患者端能看到进展时间轴。
--
-- 【为什么不新建工单表】
-- 新建表会让"患者看到的留言"和"客服处理的工单"变成两张皮：
-- 患者查状态要跨表、客服看到的单号和患者手上的单号对不上。
-- 留言和工单是同一件事的两个阶段，就在一张表上加受理字段。
--
-- 【状态口径（全仓唯一，改这里必须同步 ServiceTicketStatus 注释 / 前端文案）】
--   0 待受理（患者已提交，无人认领）
--   1 处理中（客服已受理）
--   2 已办结（客服给了处理结果，等患者确认）
--   3 已关闭（患者确认解决 / 患者撤单 / 客服关闭，终态）
-- 历史数据全是 status=0（语义与"待受理"一致），无 1/2 存量，改口径安全。
--
-- 【流转动作 action 口径】
--   0提交 1受理 2客服回复 3办结 4患者补充 5关闭 6患者撤单 7患者重开
-- visible_to_patient=1 才会出现在患者端时间轴（内部备注不暴露给患者）
--
-- 【执行方式】
-- 加列用动态 SQL 包一层（可重复执行）；中文 COMMENT 一律放 MODIFY（幂等，直接跑）。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. biz_service_message 加工单字段
-- -----------------------------------------------------------------------------
SET @db := DATABASE();

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'priority');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN priority tinyint DEFAULT 0 COMMENT ''priority 0-normal 1-urgent'' AFTER status',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'accept_by');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN accept_by varchar(64) DEFAULT NULL AFTER priority',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'accept_by_name');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN accept_by_name varchar(64) DEFAULT NULL AFTER accept_by',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'accept_time');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN accept_time datetime DEFAULT NULL AFTER accept_by_name',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'close_by');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN close_by varchar(64) DEFAULT NULL AFTER accept_time',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'close_time');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN close_time datetime DEFAULT NULL AFTER close_by',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'close_reason');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN close_reason varchar(200) DEFAULT NULL AFTER close_time',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'last_reply_time');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN last_reply_time datetime DEFAULT NULL AFTER close_reason',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND COLUMN_NAME = 'reply_count');
SET @sql := IF(@exist = 0,
  'ALTER TABLE biz_service_message ADD COLUMN reply_count int DEFAULT 0 AFTER last_reply_time',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 中文 COMMENT 用 MODIFY 补（幂等，可重复执行）
ALTER TABLE `biz_service_message`
  MODIFY COLUMN `status` tinyint DEFAULT '0' COMMENT '工单状态（0-待受理 1-处理中 2-已办结 3-已关闭）',
  MODIFY COLUMN `priority` tinyint DEFAULT '0' COMMENT '优先级（0-普通 1-紧急）',
  MODIFY COLUMN `accept_by` varchar(64) DEFAULT NULL COMMENT '受理人账号（服务端取登录人，不由前端传）',
  MODIFY COLUMN `accept_by_name` varchar(64) DEFAULT NULL COMMENT '受理人姓名',
  MODIFY COLUMN `accept_time` datetime DEFAULT NULL COMMENT '受理时间',
  MODIFY COLUMN `close_by` varchar(64) DEFAULT NULL COMMENT '关闭人账号',
  MODIFY COLUMN `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  MODIFY COLUMN `close_reason` varchar(200) DEFAULT NULL COMMENT '关闭原因（患者撤单/客服关闭都要写）',
  MODIFY COLUMN `last_reply_time` datetime DEFAULT NULL COMMENT '最后一次客服回复时间',
  MODIFY COLUMN `reply_count` int DEFAULT '0' COMMENT '客服回复次数';

-- 受理/关闭后会按这些条件筛，补索引
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_service_message' AND INDEX_NAME = 'idx_accept_by');
SET @sql := IF(@exist = 0,
  'CREATE INDEX idx_accept_by ON biz_service_message (accept_by, status)',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- -----------------------------------------------------------------------------
-- 2. 工单流转记录：患者端"进展"和客服端"证据链"都是这张表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_service_ticket_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_id` bigint NOT NULL COMMENT '工单ID（biz_service_message.id）',
  `message_no` varchar(32) DEFAULT NULL COMMENT '工单号（冗余，排查时不用 join）',
  `action` tinyint NOT NULL COMMENT '动作（0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开）',
  `content` varchar(1000) DEFAULT NULL COMMENT '内容（回复正文 / 处理结果 / 撤单原因）',
  `visible_to_patient` tinyint DEFAULT '1' COMMENT '患者是否可见（0-内部备注 1-患者可见）',
  `operator_type` tinyint DEFAULT '1' COMMENT '操作人类型（1-患者 2-院内）',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人账号',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_message` (`message_id`, `create_time`),
  KEY `idx_no` (`message_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单流转记录（患者端进展时间轴 + 客服端证据链）';
