-- ============================================================
-- 418 打印台账：标签打印留痕表（不建模板表，版式写死在渲染器里）
-- 日期：2026-11-27
-- 背景：腕带 / 检验标本管 / 血袋 / 静配输液贴 / 口服药袋 / CSSD 器械包 / 病理标本
--       这些标签都要打到实物上贴住。版式属业务规则（定下来基本不动），
--       不配模板引擎、不做可视化设计器；必须落库的只有「谁在什么时候打了
--       哪个对象的第几次、补打为什么、当时纸上是什么内容」——即本表。
--       标签文件（PDF / 图片 / ZPL）不存：码值 + 字段快照 + 业务表数据
--       任何时候都能重打还原出同一张纸。
-- 口径：
--   1) 一个对象打一次一行；print_no 从 1 递增，>=2 即补打，补打原因必填（应用层校验）；
--   2) 唯一键 uk_print_no(biz_type,biz_id,print_no) 兜并发：两个人同时点打印不会抢到同一序号，
--      第二条直接 Duplicate entry，应用层转「已打印过，请走补打」；
--   3) biz_type 码值见列注释；代码落地时在 his-core-common/common/enums 建 PrintBizTypeEnum，
--      改码值须同步本列注释。当前尚无 Java 枚举，故不铺 sys_dict_data，避免两份真相；
--   4) 条码 / 二维码图形不入库、不落文件系统，由渲染端按 code_value 现场生成；
--   5) 标签作废与补打后旧码失效由各业务表自管（如 specimen_no 换号/置空），本表只留痕。
-- 幂等：建表带 IF NOT EXISTS；无菜单、无权限、无字典行，重放不产生副作用。
-- 回滚：见同目录 418-回滚.sql。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- ---------- 0. 前置核对 ----------
-- SELECT COUNT(*) AS table_should_be_0 FROM information_schema.TABLES
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_print_record';

-- ---------- 1. 打印台账 ----------
CREATE TABLE IF NOT EXISTS `sys_print_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `biz_type` tinyint NOT NULL COMMENT '业务类型（1-腕带 2-检验标本管 3-血袋 4-静配输液贴 5-口服药袋 6-CSSD器械包 7-病理标本 8-检查影像袋）',
  `biz_id` bigint NOT NULL DEFAULT 0 COMMENT '业务对象ID（住院记录/标本管/血袋/静配主单/器械包等业务表主键）',
  `code_value` varchar(64) NOT NULL DEFAULT '' COMMENT '标签码值（腕带住院号/标本号/血袋号/器械包条码）',
  `patient_id` bigint NOT NULL DEFAULT 0 COMMENT '患者ID（按患者聚合查询用，非患者对象填0）',
  `print_no` int NOT NULL DEFAULT 1 COMMENT '第几次打印（1-首次，2起为补打）',
  `print_reason` varchar(200) NOT NULL DEFAULT '' COMMENT '补打原因（print_no>=2 必填）',
  `render_code` varchar(32) NOT NULL DEFAULT '' COMMENT '渲染器标识（代码内写死的版式ID，审计对版用）',
  `paper_spec` varchar(32) NOT NULL DEFAULT '' COMMENT '纸张规格（如 50x25、80x60，取自 sys_config）',
  `printer_name` varchar(64) NOT NULL DEFAULT '' COMMENT '打印机名称（终端绑定）',
  `terminal_ip` varchar(50) NOT NULL DEFAULT '' COMMENT '打印终端IP',
  `snapshot_json` json DEFAULT NULL COMMENT '打印时字段快照（当时纸上内容的字段值与版式版本）',
  `operator_id` bigint NOT NULL DEFAULT 0 COMMENT '打印人ID',
  `operator_name` varchar(64) NOT NULL DEFAULT '' COMMENT '打印人姓名',
  `print_time` datetime NOT NULL DEFAULT (now()) COMMENT '打印时间',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) NOT NULL DEFAULT '' COMMENT '备注',
  `create_by_id` bigint NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL DEFAULT 0 COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_print_no` (`biz_type`,`biz_id`,`print_no`),
  KEY `idx_code_value` (`code_value`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_print_time` (`print_time`),
  KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='打印台账（标签打印留痕）';

COMMIT;

-- ---------- 2. 执行后回查 ----------
-- SHOW CREATE TABLE sys_print_record;
-- 期望：uk_print_no(biz_type,biz_id,print_no) 存在；snapshot_json 为 json；无外键。
