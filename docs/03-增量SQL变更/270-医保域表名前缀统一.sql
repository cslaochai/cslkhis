-- 270-医保域表名前缀统一为 biz_yb_
-- 背景：医保域 15 张表混了 6 种前缀（biz_yb_ ×7、biz_insurance_ ×3、sys_insurance_policy ×1、
--       biz_settlement_ ×2、biz_compliance_audit ×2），是全仓唯一没收敛的域（其他域均
--       biz_<域>_<实体>，如 biz_patient_* / biz_laboratory_* / biz_operation_*）。
--       biz_yb_ 是存量多数派：7 张表 + 全列名体系（yb_code/yb_name）+ 7 个 BizYb* 类名都已是
--       yb 口径，故向它收敛；sys_insurance_policy 顺带从 sys_ 系统域搬回 biz_ 业务域。
--       改名后 biz_yb_settlement（医保结算清单）与 biz_settlement_bill（收费账单）用前缀区分，
--       两个 settlement 不再撞词。
-- 表：8 张重命名（数据/索引/自增随 RENAME TABLE 原样带走，秒级原子）。
-- 库内依赖：已核实 0 外键、0 视图、0 触发器、0 存储过程/函数引用本域表；CDR 时间线的
--       srcTable 字段存有旧表名（仅展示消费、无逻辑反查），历史事件行按快照语义保留旧值，
--       新事件用新表名，不做数据 UPDATE。
-- 顺序：须在 269（门诊诊查费对接湘医保58号）之后执行——269 引用旧表名插入，数据随本文件跟改。
-- 停机：停服窗变更。先停后端 → 执行本文件 → 发布新代码（8 个 @TableName + 3 处 SQL 字面量
--       已随本次提交改完）→ 起服验证。旧 jar 连新表名 / 新 jar 连旧表名都会报错，中间不留窗口。
-- 回滚：反向执行下方 RENAME TABLE（新名→旧名）+ 代码回滚，秒级。
-- 历史文件不动：docs/01-初始化DDL 快照（滞后于增量的环境导出，如未含 241 的 DEFAULT 0）、
--       docs/02-铺底基础数据 与 docs/03 中 258/241/239 等历史增量均引用旧表名；按
--       01→02→03 序号执行时它们跑在旧表名上（本文件排在最后），自洽，故不改写历史，
--       01 快照的下次导出会自然带上新表名。docs/10-ER关系图 为现状设计文档，已同步新名。

SET NAMES utf8mb4;

RENAME TABLE
  `biz_insurance_catalog_rule`   TO `biz_yb_catalog_rule`,
  `biz_insurance_settlement`     TO `biz_yb_settlement`,
  `biz_insurance_report`         TO `biz_yb_report`,
  `sys_insurance_policy`         TO `biz_yb_policy`,
  `biz_settlement_diagnosis`     TO `biz_yb_settlement_diagnosis`,
  `biz_settlement_operation`     TO `biz_yb_settlement_operation`,
  `biz_compliance_audit`         TO `biz_yb_compliance_audit`,
  `biz_compliance_audit_item`    TO `biz_yb_compliance_audit_item`;
