-- 235 DRG 多版本隔离迁移
-- 背景：2.0/3.0 共用同一套 CHS-DRG 编码体系（3.0 是在 2.0 上增组，码值同名），
--       唯一键必须纳入 version 才能两版共存且不互相撞键；模拟结果也要按版本隔离，
--       否则同一首页在 3.0 与 2.0 下模拟会互相覆盖。
-- 适用：仅「已部署」库（192.168.88.132:3306/hn_biz_his）。全新库直接跑 04 初始化 DDL 即可，
--       04 已同步本变更，切勿在全新库重复跑本文件（唯一键已含 version、version 列已存在会报错）。
-- 配置表（sys_drg_*）为重灌型，本文件不触碰数据，只改约束。
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- 2) 模拟结果表增加 version 列（默认 3.0，覆盖存量行），唯一键改为 (summary_id, version)
ALTER TABLE biz_drg_sim_result
  ADD COLUMN version varchar(32) NOT NULL COMMENT '分组方案版本（3.0/2.0）' AFTER drg_name;
ALTER TABLE biz_drg_sim_result DROP INDEX uk_summary, ADD UNIQUE KEY uk_summary (summary_id, version);
