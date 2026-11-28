-- ============================================================
-- 436 医保付费权限收口：壳入口 / 页签 / 分组模拟 三档分权
-- ============================================================
-- 背景（2026-11-28）：
--   907「医保付费」是聚合壳（DRG 分组 / DIP 分组 / 医保结算 三页签），但菜单只挂了一个
--   report:drg:list，于是出现两个后果：
--     · 后端 /report/drg/simulate、/simulateBatch 用 report:drg:list 鉴权 —— 查询码兼写操作，
--       谁能看这个页面谁就能跑分组模拟（写 biz_drg_sim_result，批量默认 50 条）；
--     · 400 为了给医保结算员(16) 授壳入口，授了 907 = 授了 report:drg:list = 顺带拿到
--       DRG 分组页签与模拟权（400 注释里「DRG/DIP 页签因无对应码隐藏」实际不成立）。
--   医保结算员按「批量模拟」写的是自己后面出院结算要用的分组结果，这不是只读报表。
--
-- 改动（把「能进这一页」与「能用页签里的东西」拆成两档）：
--   1) 907 让出入口码：permission report:drg:list → 置 NULL（纯壳，不再兼任 DRG 页签码）。
--      菜单可见性由 sys_role_menu 决定，置 NULL 不影响进壳；/auth/info 不再因授壳而下发 DRG 码。
--   2) 909/910 同步退役为纯记录：permission 置 NULL、is_visible=0。
--      这两条隐藏菜单原本还是 medpay:settle:list / report:dip:list 的授权载体，
--      把权限码挂在「已下线页面」上是历史包袱，本次一律改由 907 下的按钮承载。
--   3) 907 下新增 5 条 menu_type=3 权限载体（页签 3 条 + 模拟 2 条）：
--      9071 DRG分组（页签）  report:drg:list
--      9072 DRG分组模拟       report:drg:sim       ← 新增码，后端两个写接口改用它
--      9073 DIP分组（页签）   report:dip:list
--      9074 DIP分组模拟       report:dip:sim       ← 新增码
--      9075 医保结算（页签）  medpay:settle:list
--   4) 授权（按 2026-11-28 现库实测，与铺底 SQL 不一致，以现库为准）：
--      1 系统管理员    只授 9071/9073/9075 三个页签查看，**不授模拟**（9072/9074 属写操作）
--      16 医保结算员   五条全授（现库本就持 907/909/910 三码，本次只把码从退役菜单平移到按钮，
--                      她的可见范围与操作能力不变 —— 菜单就是给她用的）
--      17 审计统计员   现库无本页任何授权，本次不恢复（见 4.2 的可选段）
--      18 院领导       不授（395 已收口，保持无入口）
--
-- 后端配套（同批已改）：
--   DrgSimController#simulate / #simulateBatch → hasAuthority('report:drg:sim')
--   DipSimController#simulate / #simulateBatch → hasAuthority('report:dip:sim')
-- 前端配套（同批已改）：
--   GroupSimView 壳不再用入口码判页签；DrgSimView / DipSimView 的「模拟」「批量模拟」挂 v-perm
--
-- 幂等：907/909/910 用 id + 现值判；按钮用 id OR menu_key OR permission 三道键判；
--       授权按 menu_key 解析 menu_id 后按 (role_id, menu_id) 判，重跑无新增。
-- 落地后清缓存（否则 10 分钟 TTL 内仍按旧码放行）：
--   redis-cli --scan --pattern 'his:perm:role:*' | xargs -r redis-cli del
--   redis-cli --scan --pattern 'his:user:identity:*' | xargs -r redis-cli del
-- 回滚见文件末尾。
-- ============================================================

-- ---------------- 0. 前置核对 ----------------
SELECT id, menu_name, parent_id, menu_type, is_visible, permission
FROM sys_menu WHERE id IN (907, 909, 910) ORDER BY id;

SELECT rm.role_id, r.role_name, rm.menu_id, m.permission
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.menu_id IN (907, 909, 910)
ORDER BY rm.role_id, rm.menu_id;

-- ---------------- 1. 907 让出入口码（纯壳） ----------------
UPDATE sys_menu
SET permission  = NULL,
    remark      = '医保管理域：聚合壳（DRG / DIP 分组 + 医保结算三页签）。入口不再绑业务码，页签与模拟权限见 9071-9075；909/910 已退役为记录',
    update_by   = 'sql436',
    update_time = NOW()
WHERE id = 907
  AND permission = 'report:drg:list';

-- ---------------- 2. 909/910 退役为纯记录（权限码改由 907 下按钮承载） ----------------
UPDATE sys_menu
SET permission  = NULL,
    is_visible  = 0,
    remark      = '已并入 907 壳「医保结算」页签；权限码 medpay:settle:list 改由 907 下 9075 承载（sql436）',
    update_by   = 'sql436',
    update_time = NOW()
WHERE id = 909
  AND (permission = 'medpay:settle:list' OR is_visible = 1);

UPDATE sys_menu
SET permission  = NULL,
    is_visible  = 0,
    remark      = '已并入 907 壳「DIP 分组」页签；权限码 report:dip:list 改由 907 下 9073 承载（sql436）',
    update_by   = 'sql436',
    update_time = NOW()
WHERE id = 910
  AND (permission = 'report:dip:list' OR is_visible = 1);

-- ---------------- 3. 907 下新增 5 条权限载体 ----------------
-- 3.1 DRG 分组（页签）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      create_by_id, update_by_id, del_flag, remark)
SELECT 9071, 'DRG分组（页签）', 907, 1, 3, '', '', 'report.drg.view', '', 'report:drg:list',
       0, 0, 1, 1, 'sql436', NOW(), 'sql436', NOW(), 1, 1, 0,
       'DRG 分组页签的查看权；907 让出该码后由本行承载'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9071 OR menu_key = 'report.drg.view' OR permission = 'report:drg:list');

-- 3.2 DRG 分组模拟（写 biz_drg_sim_result）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      create_by_id, update_by_id, del_flag, remark)
SELECT 9072, 'DRG分组模拟', 907, 2, 3, '', '', 'report.drg.sim', '', 'report:drg:sim',
       0, 0, 1, 1, 'sql436', NOW(), 'sql436', NOW(), 1, 1, 0,
       '单条模拟 + 批量模拟（写 biz_drg_sim_result）；与页签查看权分离'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9072 OR menu_key = 'report.drg.sim' OR permission = 'report:drg:sim');

-- 3.3 DIP 分组（页签）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      create_by_id, update_by_id, del_flag, remark)
SELECT 9073, 'DIP分组（页签）', 907, 3, 3, '', '', 'report.dip.view', '', 'report:dip:list',
       0, 0, 1, 1, 'sql436', NOW(), 'sql436', NOW(), 1, 1, 0,
       'DIP 分组页签的查看权；910 退役后由本行承载'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9073 OR menu_key = 'report.dip.view' OR permission = 'report:dip:list');

-- 3.4 DIP 分组模拟（写 biz_dip_sim_result）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      create_by_id, update_by_id, del_flag, remark)
SELECT 9074, 'DIP分组模拟', 907, 4, 3, '', '', 'report.dip.sim', '', 'report:dip:sim',
       0, 0, 1, 1, 'sql436', NOW(), 'sql436', NOW(), 1, 1, 0,
       '单条模拟 + 批量模拟（写 biz_dip_sim_result）；与页签查看权分离'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9074 OR menu_key = 'report.dip.sim' OR permission = 'report:dip:sim');

-- 3.5 医保结算（页签）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      create_by_id, update_by_id, del_flag, remark)
SELECT 9075, '医保结算（页签）', 907, 5, 3, '', '', 'medpay.settle.view', '', 'medpay:settle:list',
       0, 0, 1, 1, 'sql436', NOW(), 'sql436', NOW(), 1, 1, 0,
       '医保结算页签的查看权；909 退役后由本行承载'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9075 OR menu_key = 'medpay.settle.view' OR permission = 'medpay:settle:list');

-- ---------------- 4. 精确授权（按 menu_key 解析，避免 id 漂移挂错） ----------------
-- 4.1 系统管理员(1)：壳已有；只授三个页签查看（9071/9073/9075），不授 9072/9074。
--     模拟是写操作，超管默认不该随手跑批；应急处置确需时在「角色管理→菜单权限」手工勾选，
--     不要改本脚本（脚本重跑会把 9072/9074 再收回，见 4.4）。
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009071, 1, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'report.drg.view'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009073, 1, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'report.dip.view'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009075, 1, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'medpay.settle.view'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);

-- 4.2 审计统计员(17)：现库无本页任何授权（铺底曾持 907/910，387 只摘过 2160-2162，来源不明），
--     本次不恢复。若确认要由审计岗承担 DRG/DIP 分组测算，取消下面注释并按顺序执行
--     （必须先授壳 907，否则页签父级不在菜单树里，页面出不来）：
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- SELECT 9210000000000009082, 17, 907, 'sql436-manual', NOW(), 'sql436-manual', NOW(), 1, 1
-- WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 17 AND rm.menu_id = 907);
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- SELECT 9210000000000009083, 17, m.id, 'sql436-manual', NOW(), 'sql436-manual', NOW(), 1, 1 FROM sys_menu m
-- WHERE m.menu_key = 'report.drg.view' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 17 AND rm.menu_id = m.id);
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- SELECT 9210000000000009084, 17, m.id, 'sql436-manual', NOW(), 'sql436-manual', NOW(), 1, 1 FROM sys_menu m
-- WHERE m.menu_key = 'report.drg.sim' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 17 AND rm.menu_id = m.id);
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- SELECT 9210000000000009085, 17, m.id, 'sql436-manual', NOW(), 'sql436-manual', NOW(), 1, 1 FROM sys_menu m
-- WHERE m.menu_key = 'report.dip.view' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 17 AND rm.menu_id = m.id);
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- SELECT 9210000000000009086, 17, m.id, 'sql436-manual', NOW(), 'sql436-manual', NOW(), 1, 1 FROM sys_menu m
-- WHERE m.menu_key = 'report.dip.sim' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 17 AND rm.menu_id = m.id);

-- 4.3 医保结算员(16)：现库就持 907/909/910 三码（本次只是把码从退役菜单平移到按钮，能力不变）。
--     五条全授：三个页签 + 两个模拟。
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009081, 16, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'report.drg.view'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 16 AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009087, 16, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'report.drg.sim'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 16 AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009088, 16, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'report.dip.view'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 16 AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009089, 16, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'report.dip.sim'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 16 AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000009090, 16, m.id, 'sql436', NOW(), 'sql436', NOW(), 1, 1
FROM sys_menu m
WHERE m.menu_key = 'medpay.settle.view'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 16 AND rm.menu_id = m.id);

-- 4.4 反向兜底：收回系统管理员(1)名下已授的模拟码（若历史上手工授过），
--     保证「超管默认不能跑批」这一条在重跑脚本后依然成立。
DELETE rm FROM sys_role_menu rm
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.role_id = 1
  AND m.menu_key IN ('report.drg.sim', 'report.dip.sim');

-- ---------------- 5. 回查 ----------------
-- 期望：907 permission 为 NULL；9071-9075 各一行；
--       role 1 只挂 9071/9073/9075（9072/9074 为空）；role 16 挂满五条；role 17/18 无新增
SELECT id, menu_name, parent_id, menu_type, is_visible, permission
FROM sys_menu WHERE parent_id = 907 ORDER BY sort_order, id;

SELECT r.id AS role_id, r.role_name, m.id AS menu_id, m.menu_name, m.permission
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE m.parent_id = 907 OR m.id = 907
ORDER BY r.id, m.sort_order;

-- ============================================================
-- 回滚
-- ============================================================
-- 1) 恢复 907 权限码
-- UPDATE sys_menu SET permission='report:drg:list', remark='聚合壳：DRG / DIP 分组 + 医保结算页签；909 已并入'
-- WHERE id=907;
-- 2) 恢复 909/910 权限码与可见性
-- UPDATE sys_menu SET permission='medpay:settle:list', is_visible=1 WHERE id=909;
-- UPDATE sys_menu SET permission='report:dip:list',    is_visible=1 WHERE id=910;
-- 3) 删授权与按钮记录
-- DELETE rm FROM sys_role_menu rm JOIN sys_menu m ON m.id=rm.menu_id WHERE m.parent_id=907 AND m.menu_type=3;
-- DELETE FROM sys_menu WHERE id IN (9071,9072,9073,9074,9075);
-- 4) 清缓存（同落地步骤）
-- ============================================================
