-- ============================================================
-- 310-病历与质量安全目录重组（P1）
-- 日期：2026-10-30
-- 目的：把 600「病历与质量安全」下四个部门的业务拆到各自目录，
--       只调整归属与授权，不改页面、不改路径、不改权限码。
-- 依据：施工手册/病历与质量安全-模块重构施工蓝图.md 第四节 P1
-- 回滚：见蓝图第七节
-- ============================================================

-- A1 新增一级目录：医疗质量与安全
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29700, '医疗质量与安全', 0, 72, 1, '/quality-safety', NULL, 'qc.safetyCenter', 'FirstAidKit', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, 'P1 目录重组', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29700);

-- A2 新增一级目录：院感与公卫
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29701, '院感与公卫', 0, 74, 1, '/infection-public', NULL, 'infection.publicCenter', 'Umbrella', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, 'P1 目录重组', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29701);

-- A3 目录改名：只保留病历与病案
UPDATE sys_menu SET menu_name = '病历与病案', remark = 'P1 由「病历与质量安全」改名', update_time = NOW()
WHERE id = 600;

-- A4 医疗质量类迁入 29700（医务科口径）
UPDATE sys_menu SET parent_id = 29700, sort_order = 10, update_time = NOW() WHERE id = 607;
UPDATE sys_menu SET parent_id = 29700, sort_order = 20, update_time = NOW() WHERE id = 2183;
UPDATE sys_menu SET parent_id = 29700, sort_order = 30, update_time = NOW() WHERE id = 608;
UPDATE sys_menu SET parent_id = 29700, sort_order = 40, update_time = NOW() WHERE id = 610;
UPDATE sys_menu SET parent_id = 29700, sort_order = 50, update_time = NOW() WHERE id = 615;

-- A4 院感公卫类迁入 29701
UPDATE sys_menu SET parent_id = 29701, sort_order = 10, update_time = NOW() WHERE id = 614;
UPDATE sys_menu SET parent_id = 29701, sort_order = 20, update_time = NOW() WHERE id = 613;
UPDATE sys_menu SET parent_id = 29701, sort_order = 30, update_time = NOW() WHERE id = 618;

-- A5 迁出本目录：满意度 → 患者中心 700，签名验签 → 组织与资源 800
UPDATE sys_menu SET parent_id = 700, sort_order = 80, update_time = NOW() WHERE id = 616;
UPDATE sys_menu SET parent_id = 800, sort_order = 90, update_time = NOW() WHERE id = 29620;

-- A6 授权补齐（已存在的不重复插）
SET @rm_id := (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    -- 系统管理员：两个新目录
    SELECT 1 AS role_id, 29700 AS menu_id
    UNION ALL SELECT 1, 29701
    -- 医务科：目录 + 专项质控 + 危急值 + 医疗安全 + 公卫审核
    UNION ALL SELECT 26, 29700
    UNION ALL SELECT 26, 607
    UNION ALL SELECT 26, 2183
    UNION ALL SELECT 26, 608
    UNION ALL SELECT 26, 610
    UNION ALL SELECT 26, 615
    UNION ALL SELECT 26, 29701
    UNION ALL SELECT 26, 618
    -- 科主任：目录 + 专项质控 + 危急值 + 医疗安全
    UNION ALL SELECT 27, 29700
    UNION ALL SELECT 27, 607
    UNION ALL SELECT 27, 2183
    UNION ALL SELECT 27, 608
    UNION ALL SELECT 27, 610
    UNION ALL SELECT 27, 615
    -- 护理部主任：目录 + 不良事件
    UNION ALL SELECT 29, 29700
    UNION ALL SELECT 29, 610
    -- 客服专员 / 门诊办：满意度
    UNION ALL SELECT 24, 616
    UNION ALL SELECT 25, 616
    -- 病案编码员：归档 / 借阅 / 编码任务池
    UNION ALL SELECT 14, 609
    UNION ALL SELECT 14, 611
    UNION ALL SELECT 14, 612
    -- 病案质控员：归档（若未授）
    UNION ALL SELECT 15, 609
    -- 公卫医师：院感公卫目录
    UNION ALL SELECT 22, 29701
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);
