-- 角色菜单权限 全量收口（合并 2026-10-10 三轮：第一轮域前缀 + 第二轮域内错配 + 第三轮体检/组织/排班/检查技师/空壳）
-- 已对库执行完毕，sys_role_menu 由 1645 → 1370（净收 275 条）；库内备份表 sys_role_menu_bak_20261010_full 可整体回退。
-- 本文件幂等：DELETE 语义可重复；补授权已改 INSERT IGNORE。重跑前建议先备份。

-- =============================================================
-- 角色菜单权限收口（合并两次变更的最终 SQL）
-- 库：hn_biz_his
-- 日期：2026-10-10
-- 规则：系统管理员(role 1) 保持全菜单 + 全院数据不变；
--       其余角色只保留本职业务域菜单，跨界越权一律收回。
-- 说明：本文件仅含权限更改(DELETE)。原备份段与回退段已删除；
--       原备份表 sys_role_menu_bak_20261010 / _20261010b 已从库删除。
-- =============================================================

-- 1) 护士长(role 10)：收回医生排班(org) + 护理跨界(checkup/medtech/opd)
--    含第一轮已收回的 org:schedule(804/2120/2121/2122/2935/2937/2938)
DELETE FROM sys_role_menu
WHERE role_id = 10
  AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'checkup:%' OR permission LIKE 'medtech:%' OR permission LIKE 'opd:%' OR permission LIKE 'org:%');

-- 2) 审计统计员(role 17)：收回系统管理目录壳/电子签名(1100/606/2088) + 住院护理/药剂跨界(ipd/nursing/pharmacy)
DELETE FROM sys_role_menu
WHERE role_id = 17
  AND (menu_id IN (1100, 606, 2088)
       OR menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'ipd:%' OR permission LIKE 'nursing:%' OR permission LIKE 'pharmacy:%'));

-- 3) 其余角色越权收口（按真实职责）
--    说明：医生(2) 保留 opd/emr/ipd/medtech/patient/sign/portal（开检查、住院医嘱、签名），收回 checkup/nursing/pharmacy；
--          护士(3) 保留 nursing/ipd/opd/patient/portal，收回 checkup/medtech；
--          收费员(4) 保留 finance/charge/opd/patient/portal，收回 report；
--          前台导诊(7) 保留 opd/patient/portal，收回 org；
--          急诊医生(8) 保留 opd/emr/medtech/patient/portal，收回 checkup/org；
--          分诊护士(9) 保留 opd/patient/portal，收回 checkup/medtech/org；
--          医保结算员(16) 保留 finance/charge/patient/portal，收回 inpatient/ipd/opd/report；
--          院领导(18) data_scope=1 看全院统计，保留 report/qc/portal/patient/ipd/nursing/pharmacy/checkup/emr，收回 org/ai。
DELETE FROM sys_role_menu
WHERE (role_id = 2  AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'checkup:%' OR permission LIKE 'nursing:%' OR permission LIKE 'pharmacy:%'))
   OR (role_id = 3  AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'checkup:%' OR permission LIKE 'medtech:%'))
   OR (role_id = 4  AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'report:%'))
   OR (role_id = 7  AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'org:%'))
   OR (role_id = 8  AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'checkup:%' OR permission LIKE 'org:%'))
   OR (role_id = 9  AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'checkup:%' OR permission LIKE 'medtech:%' OR permission LIKE 'org:%'))
   OR (role_id = 16 AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'inpatient:%' OR permission LIKE 'ipd:%' OR permission LIKE 'opd:%' OR permission LIKE 'report:%'))
   OR (role_id = 18 AND menu_id IN (SELECT id FROM sys_menu WHERE permission LIKE 'org:%' OR permission LIKE 'ai:%'));


-- ============================================================
-- 20261010 角色菜单权限收口（第二轮：域内错配收回 + 通用页补齐）
-- 背景：第一轮按 permission 业务域前缀粗收（org/ai/system 等），
--       本轮按「角色真实职责」逐页审计，收回域内错配页面（含其按钮子项），
--       并补齐全院通用页（工作台/消息待办）缺失的角色。
-- 效果验证：收回后护士长不再持有 公卫上报(618)/传染病报告卡(613)/病案质控域；
--           收费员只剩收退费本职；体检管理(905/2930) 仅体检相关角色持有。
-- 注意：本脚本幂等（DELETE 语义），可重复执行。
-- ============================================================

-- ---------- 一、收回域内错配页面（页面 + 按钮子项） ----------

-- 1) 收费员(4)：收回医保科/财务专岗页面（本职只留收退费/日结/流水）
DELETE FROM sys_role_menu
WHERE role_id = 4
  AND menu_id IN (
    SELECT id FROM sys_menu WHERE del_flag=0 AND (
      id IN (1003,1009,1010,1011,1012,2923,2320,2367,2340)
      OR parent_id IN (1003,1009,1010,1011,1012,2923,2320,2367,2340)
    )
  );
-- 收回明细：1003 医保结算清单 / 1009 医保目录对照 / 1010 医保扣款与飞检处理 /
--           1011 慢特病医保备案 / 1012 医保合规审核台账 / 2923 门诊慢特病病种目录 /
--           2320 支付渠道对账 / 2367 资金账户 / 2340 复诊收费策略(配置项)

-- 2) 护士长(10)：收回病案/公卫/营养域（截图越权来源；本职留护理三件套+不良事件+营养执行）
DELETE FROM sys_role_menu
WHERE role_id = 10
  AND menu_id IN (
    SELECT id FROM sys_menu WHERE del_flag=0 AND (
      id IN (604,607,609,611,612,615,616,2183,613,614,618,428)
      OR parent_id IN (604,607,609,611,612,615,616,2183,613,614,618,428)
    )
  );
-- 收回明细：604 病案质控工作台 / 607 临床路径 / 609 病案归档 / 611 病案借阅复印 /
--           612 编码任务池 / 615 医疗纠纷与投诉 / 616 满意度评价 / 2183 单病种质控 /
--           613 传染病报告卡 / 614 院感监测 / 618 公卫上报(截图页面) / 428 营养指标监测

-- 3) 临床药师(13)：收回体检管理（药事域职责无关）
DELETE FROM sys_role_menu
WHERE role_id = 13
  AND menu_id IN (
    SELECT id FROM sys_menu WHERE del_flag=0 AND (
      id IN (905,2930) OR parent_id IN (905,2930)
    )
  );

-- 4) 病案编码员(14)：收回体检管理
DELETE FROM sys_role_menu
WHERE role_id = 14
  AND menu_id IN (
    SELECT id FROM sys_menu WHERE del_flag=0 AND (
      id IN (905,2930) OR parent_id IN (905,2930)
    )
  );

-- 5) 病案质控员(15)：收回体检/护理/药事/公卫错配（上一轮漏收，本轮补上）
DELETE FROM sys_role_menu
WHERE role_id = 15
  AND menu_id IN (
    SELECT id FROM sys_menu WHERE del_flag=0 AND (
      id IN (905,2930,332,333,428,515,516,519,613,618)
      OR parent_id IN (905,2930,332,333,428,515,516,519,613,618)
    )
  );
-- 收回明细：905/2930 体检管理+套餐 / 332/333 VTE 防控与监测 / 428 营养指标监测 /
--           515 处方点评 / 516 处方公示 / 519 I类切口预防用药点评 / 613 传染病报告卡 / 618 公卫上报

-- 6) 医保结算员(16)：收回价格管理（物价职能，非医保结算）
DELETE FROM sys_role_menu
WHERE role_id = 16
  AND menu_id IN (
    SELECT id FROM sys_menu WHERE del_flag=0 AND (
      id IN (1007) OR parent_id IN (1007)
    )
  );

-- ---------- 二、补齐全院通用页（工作台/消息待办，登录落点必需） ----------
-- 注意：sys_role_menu.id 为 bigint 主键、无自增，必须显式给 id（沿用 8916800000000210xxx 号段）。
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
(8916800000000210101, 20, 101, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8916800000000210102, 20, 102, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8916800000000210103, 21, 101, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8916800000000210104, 21, 102, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8916800000000210105, 24, 101, 'admin', NOW(), 'admin', NOW(), 1, 1);
-- 放射诊断医师(20) / 营养师(21)：补 101 工作台 + 102 消息待办
-- 客服专员(24)：补 101 工作台（102 已有）
-- 幂等提示：重跑本段前先确认上述 id 不存在（NOT EXISTS 由人工保证）。


-- 角色菜单权限 全量收口（第三轮）：所有非管理员角色按真实职责收回越权菜单
-- 生成时间 2026-10-10；库内已先备份 sys_role_menu_bak_20261010_full

DELETE FROM sys_role_menu WHERE (role_id, menu_id) IN (
  (2, 1400),
  (2, 330),
  (2, 500),
  (2, 2051),
  (3, 1400),
  (4, 1200),
  (7, 2945),
  (7, 800),
  (8, 1400),
  (8, 2945),
  (8, 800),
  (8, 2051),
  (9, 1400),
  (9, 2945),
  (9, 800),
  (10, 1400),
  (10, 800),
  (10, 200),
  (15, 1400),
  (15, 330),
  (15, 500),
  (15, 300),
  (16, 300),
  (16, 1200),
  (17, 300),
  (17, 330),
  (17, 500),
  (12, 2052),
  (12, 2053),
  (12, 2054),
  (12, 2055),
  (12, 2056),
  (12, 2057),
  (12, 2058),
  (12, 2059),
  (12, 2060),
  (12, 2061),
  (12, 2062),
  (12, 2063),
  (12, 2064),
  (12, 2065),
  (12, 407),
  (12, 408),
  (12, 409),
  (12, 410),
  (12, 411),
  (12, 413),
  (12, 415),
  (12, 416),
  (12, 417),
  (12, 2377),
  (12, 2378),
  (12, 2379),
  (12, 2901),
  (12, 2902),
  (12, 2903),
  (12, 2393),
  (12, 2395),
  (12, 2396),
  (12, 2397),
  (12, 2398),
  (12, 2399),
  (12, 2931),
  (12, 2167),
  (12, 2168)
);

-- 补收：临床药师(13)/病案编码员(14) 漏网的 健康管理（体检）(1400) 整组
DELETE FROM sys_role_menu WHERE (role_id, menu_id) IN (
  (13, 1400),
  (14, 1400));
