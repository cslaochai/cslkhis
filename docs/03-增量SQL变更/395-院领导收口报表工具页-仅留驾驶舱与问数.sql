-- ============================================================
-- 395 院领导(18) 报表统计域收口：摘除专业工具/明细报表页，仅留汇总看板
-- 日期：2026-11-27
-- 口径（对照厂商院长驾驶舱/院长查询实践）：
--   院领导菜单 = 全局汇总看板 + 核心指标可视化；DRG/DIP 分组模拟、绩效成本核算、
--   运营明细报表、病案统计上报属医保办/绩效办/统计岗的专业工具与业务页，不进领导菜单。
-- 摘除（17 审计统计员 / 41 统计上报员 仍持有，不产生孤儿）：
--   907 分组模拟 report:drg:list（DRG 分组器测算工具页）
--   908 绩效与成本核算 report:perf:list（绩效办/财务核算明细页）
--   910 DIP 分组模拟 report:dip:list（DIP 分组器测算工具页）
--   1201 运营统计报表 report:stats:list（明细报表清单，归统计岗）
--   2159 病案统计上报 report:statReport:list（上报业务页，归 41/17）
-- 保留：906 BI 驾驶舱（全院汇总看板）、2940 AI 运营问数（领导问答式看数入口）。
-- 回滚：见 395-回滚.sql；执行前备份 backup/sys_role_menu_backup_20261127_090432.csv。
-- ============================================================

DELETE FROM sys_role_menu WHERE role_id = 18 AND menu_id IN (907, 908, 910, 1201, 2159);

-- 回查：
-- SELECT m.id, m.menu_name FROM sys_role_menu rm JOIN sys_menu m ON m.id=rm.menu_id
--  WHERE rm.role_id=18 ORDER BY m.id;   -- 期望 8 条：100/101/102/29654/2001/1200/906/2940
-- 清缓存：Redis 删除 his:perm:role:* 与 his:user:identity:*
