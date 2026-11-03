-- ============================================================
-- 323 删除 DRG 方案默认标记（biz_drg_scheme 去掉 is_default）
-- 目标库：hn_biz_his  @ 192.168.88.132:3306
--
-- 口径收敛：biz_drg_scheme 只回答「有哪些版本」，不回答「哪版是默认」。
-- 默认版本只由「系统管理 → 参数设置 → 医保付费」的 sys_config 决定，运行时读 Redis 缓存
-- （his:drg:default-version）。表里再存一份 is_default 会形成第二个真相，
-- 一旦 Redis / 参数设置 / 表内标记三者不同步，事后完全无法追溯这笔结算用的是哪个口径。
--
-- 连带删除索引 idx_scheme_default（本身就是给 is_default 服务的，列没了索引无意义）。
-- 代码侧同步改动：DrgSchemeRowVO / DrgScheme.Header / DrgSchemeCache / DrgSimMapper / DrgSchemeConfig。
--
-- 执行前已核对：全库无代码读取该列（Mapper 仅 selectDefaultSchemeVersion/markDefaultScheme 使用，已删除）。
-- 本表唯一键为 uk_scheme_version(version, del_flag)，删列不影响唯一约束。
--
-- 不可重复执行：第二次跑会报「列/索引不存在」，属预期。
-- ============================================================
SET NAMES utf8mb4;

ALTER TABLE `biz_drg_scheme` DROP INDEX `idx_scheme_default`;

ALTER TABLE `biz_drg_scheme` DROP COLUMN `is_default`;
