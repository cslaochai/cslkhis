-- ============================================================
-- 新增角色：住院结算员（role_id=25 / role_code=10035）
--
-- 业务定位：住院楼一层住院处窗口，管预交金收退、日清单查看、出院结算多退少补。
-- 与门诊「收费员（role_id=4）」完全分开，现金抽屉与班结各自独立。
--
-- 权限链路（已与后端 @PreAuthorize 对齐，不借用门诊/欠费管控的码）：
--   读（预交金流水/余额/日清单/结算预览/汇总）
--     InpatientAccountController.VIEW = hasAnyAuthority(
--         'charge:arrearsControl:list',   -- 欠费管控页
--         'ipd:inpatient:list',           -- 入出院管理（医生/护士站）
--         'ipd:settlement:list')          -- 住院结算页（本角色挂 305 菜单即得）
--   写-预交金收退  POST /charge/inpatient/account/prepay/save
--     → hasAuthority('ipd:inpatient:add')  → 挂按钮 2015
--   写-出院结算    POST /charge/inpatient/account/settlement/settle
--     → hasAuthority('ipd:inpatient:edit') → 挂按钮 2016
--
-- 注：出院结算内部由 InpatientAccountServiceImpl 直接调 PaymentService（service 间调用
-- 不过 Controller @PreAuthorize），所以本角色不需要 finance:cashier:list / finance:refund:list，
-- 也不挂 1001 收费结算窗口、1005 退费申请菜单——左侧导航只露「财务结算 > 住院结算」一个入口。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- 1) 角色本体
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT '25', '10035', '住院结算员', 1, 5, 1, 25, 1,
       'admin', 'admin', 0, '住院处窗口：预交金收退 / 日清单 / 出院结算多退少补', '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = '25' OR role_code = '10035');

-- 2) 菜单关联（id 段 89168000000003000xx，与现有 8916800000000200xxx 错开）
--    守卫按 (role_id, menu_id) 判重，重复执行不会报错也不会产生重复行
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT t.id, t.role_id, t.menu_id, 'admin', 'admin', '1', '1'
FROM (
    SELECT '891680000000030001' AS id, '25' AS role_id, '1000' AS menu_id UNION ALL  -- 财务结算（父目录，导航）
    SELECT '891680000000030002', '25', '305' UNION ALL  -- 住院结算（主菜单，自带 ipd:settlement:list）
    SELECT '891680000000030003', '25', '2015' UNION ALL  -- 入出院管理-新增（ipd:inpatient:add，预交金收/退）
    SELECT '891680000000030004', '25', '2016'           -- 入出院管理-修改（ipd:inpatient:edit，出院结算）
) t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = '25' AND rm.menu_id = t.menu_id
);

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 跑完后回查：
--   SELECT id, role_code, role_name FROM sys_role WHERE id = 25;
--   SELECT rm.id, rm.menu_id, m.menu_name, m.menu_type, m.permission
--     FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--    WHERE rm.role_id = 25 ORDER BY rm.id;
-- 预期：4 条，菜单名 = 财务结算/住院结算/入出院管理-新增/入出院管理-修改
-- 清权限缓存：his:perm:role:*（TTL 10 分钟）
-- ============================================================
