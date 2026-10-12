-- 236 DRG 配置表前缀统一为 biz_drg（已部署库 RENAME）
-- 背景：DRG 分组器独立成 his-drg 模块，模块内 5 张配置表统一使用 biz_drg_ 前缀，
--       与结果表 biz_drg_sim_result 保持一致。全新库直接跑 04 初始化 DDL 即可（04 已改为 biz_drg_*）。
-- 适用：仅「已部署」库（192.168.88.132:3306/hn_biz_his）。该库此前已跑过 233/234/235，
--       表名仍是 sys_drg_*，本文件把它们 RENAME 成 biz_drg_* 以对齐全新库结构。
--       235 已把唯一键纳入 version、已给 biz_drg_sim_result 加 version 列，这些约束在 RENAME 后保留，无需重复。
-- 切勿在全新库跑本文件（全新库已无 sys_drg_* 表，RENAME 会报 1051 表不存在）。
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

RENAME TABLE
    sys_drg_mdc   TO biz_drg_mdc,
    sys_drg_adrg  TO biz_drg_adrg,
    sys_drg_set   TO biz_drg_set,
    sys_drg_group TO biz_drg_group,
    sys_drg_ccmcc TO biz_drg_ccmcc;

SET FOREIGN_KEY_CHECKS=1;
