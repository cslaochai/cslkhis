-- 302: 质控检查记录补「触发来源」列
-- 为什么要这一列：qc_by 只回答「谁的名字落在上面」，答不了「这张单是引擎跑出来的还是质控员判出来的」。
-- 结诊自动质控与质控页人工质控此前共用同一个入口，两者的整改责任人不同，混在一张表里无法分开统计。
-- 存量行全部来自质控页人工操作（结诊自动那条链路从来没成功落过库），故默认落 1-人工。
ALTER TABLE `biz_quality_control`
  ADD COLUMN `trigger_source` tinyint NOT NULL DEFAULT '1' COMMENT '质控触发来源（1-人工 2-结诊自动 3-定时补扫）' AFTER `qc_type`;

CREATE INDEX `idx_trigger_source` ON `biz_quality_control` (`trigger_source`, `qc_time`);
