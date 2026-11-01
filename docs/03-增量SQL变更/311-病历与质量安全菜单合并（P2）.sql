-- ============================================================
-- 311-病历与质量安全菜单合并（P2）
-- 日期：2026-10-30
-- 目的：把 6 组“同一件事”的菜单收成一个入口 + 页内 tab，
--       被合掉的菜单只隐藏（is_visible=0），del_flag 保持 0、权限码与角色授权不动，
--       因此前端 v-perm 与服务端 @PreAuthorize 都不受影响。
-- 前置：module-center 五个容器页面与路由必须先上线，否则点进去 404。
-- 依据：施工手册/病历与质量安全-模块重构施工蓝图.md 第四节 P2
-- 回滚：UPDATE sys_menu SET is_visible=1 WHERE id IN (603,605,611,612,615,2183,618)
-- ============================================================

-- B1 病历查阅与审核：602 + 603（同一张列表页，603 只多审核动作）
UPDATE sys_menu
SET menu_name   = '病历查阅与审核',
    path        = '/medical-record-center',
    component   = 'module-center/RecordReviewCenterView',
    sort_order  = 1,
    update_time = NOW()
WHERE id = 602;
UPDATE sys_menu
SET is_visible  = 0,
    update_time = NOW()
WHERE id = 603;

-- B2 病历质控：604 + 605（数据质量做成 604 的第 5 个 tab，不另建容器）
UPDATE sys_menu
SET menu_name   = '病历质控',
    sort_order  = 2,
    update_time = NOW()
WHERE id = 604;
UPDATE sys_menu
SET is_visible  = 0,
    update_time = NOW()
WHERE id = 605;

-- B3 病案管理：609 + 611 + 612（病案室一条串行岗位链）
UPDATE sys_menu
SET menu_name   = '病案管理',
    path        = '/archive-center',
    component   = 'module-center/ArchiveCenterView',
    sort_order  = 3,
    update_time = NOW()
WHERE id = 609;
UPDATE sys_menu
SET is_visible  = 0,
    update_time = NOW()
WHERE id = 611;
UPDATE sys_menu
SET is_visible  = 0,
    update_time = NOW()
WHERE id = 612;

-- B4 专项质控：607 + 2183
UPDATE sys_menu
SET menu_name   = '专项质控',
    path        = '/specialty-qc',
    component   = 'module-center/SpecialtyQcCenterView',
    sort_order  = 10,
    update_time = NOW()
WHERE id = 607;
UPDATE sys_menu
SET is_visible  = 0,
    update_time = NOW()
WHERE id = 2183;

-- B5 医疗安全：610 + 615
UPDATE sys_menu
SET menu_name   = '医疗安全',
    path        = '/medical-safety',
    component   = 'module-center/MedicalSafetyCenterView',
    sort_order  = 40,
    update_time = NOW()
WHERE id = 610;
UPDATE sys_menu
SET is_visible  = 0,
    update_time = NOW()
WHERE id = 615;

-- B6 公卫上报：613 + 618
UPDATE sys_menu
SET menu_name   = '公卫上报',
    path        = '/public-health-center',
    component   = 'module-center/PublicHealthCenterView',
    sort_order  = 20,
    update_time = NOW()
WHERE id = 613;
UPDATE sys_menu
SET is_visible  = 0,
    update_time = NOW()
WHERE id = 618;

-- B7 被隐藏的菜单加标注，避免在“角色管理-菜单权限”树里和合并后的菜单重名
UPDATE sys_menu
SET menu_name   = '病历审核（已并入院历查阅与审核）',
    update_time = NOW()
WHERE id = 603;
UPDATE sys_menu
SET menu_name   = '全院数据质量监测（已并入院历质控）',
    update_time = NOW()
WHERE id = 605;
UPDATE sys_menu
SET menu_name   = '病案借阅复印（已并入病案管理）',
    update_time = NOW()
WHERE id = 611;
UPDATE sys_menu
SET menu_name   = '编码任务池（已并入病案管理）',
    update_time = NOW()
WHERE id = 612;
UPDATE sys_menu
SET menu_name   = '单病种质控（已并入专项质控）',
    update_time = NOW()
WHERE id = 2183;
UPDATE sys_menu
SET menu_name   = '医疗纠纷与投诉（已并入医疗安全）',
    update_time = NOW()
WHERE id = 615;
UPDATE sys_menu
SET menu_name   = '公卫上报-其他上报（已并入公卫上报）',
    update_time = NOW()
WHERE id = 618;
