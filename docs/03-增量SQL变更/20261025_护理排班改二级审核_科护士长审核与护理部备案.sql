-- =====================================================================
-- 20261025 护理排班改二级审核：病区护士长编制 → 片区科护士长审核 → 护理部备案
-- ---------------------------------------------------------------------
-- 背景（标准口径）：
--   三级综合医院护理排班的通用作法是「病区护士长编制提交 → 科护士长（大科/片区）审核 → 护理部备案」，
--   护理部的价值是抽查与退回，不逐格审、不阻塞生效。改造前本系统只有一级：
--   审核人配在临床科室自己那行（sys_department.nursing_auditor_id），且全库只配了 4 个科室、
--   全指向护理部的同一个人（陈慧兰），其余 101 个护理单元无人可审。
-- 本次动作：
--   A. 新增角色 30 / 10040 科护士长（staff_type=2 护理，data_scope=4 本部门及以下：挂片区节点即可覆盖片区内全部病区）
--   B. 新增按钮码 29610 nursing:schedule:file（护理排班-备案，挂页面 331 下）
--   C. 授权收口：科护士长(30) 持护理排班查看 + 审核发布(2981) + 排班总览；
--      护理部排班审核(29) 收回审核发布(2981)，改持备案(29610)，查看与总览保留，并更名「护理部排班备案」
--   D. 审核人语义上移：nursing_auditor_id 只在护理片区/大科节点配置，临床 leaf 科室清空
--      （片区节点与人员由 workspace/_seed_chief_nurses.mjs 铺，本文件不写死员工ID）
--   E. 新增周级备案表 biz_nurse_schedule_filing（不阻塞在岗：审核发布即生效，备案是正交维度）
--   F. 备案态与留痕码值补齐（字典 his_nurse_schedule_change_type 增 12-备案 13-备案退回，11 归排班模板的「套用模板」）
--   G. 存量已发布/已变更的护理周次回填为「已备案」，避免上线当天护理部面对一堆待备案
--
-- 状态机口径（本文件不改）：
--   行发布态 publish_status 0-草稿 1-待审核 2-已发布 3-调班中 4-已变更，在岗聚合仍是 publish_status IN (2,4)；
--   周备案态 filing_status 0-待备案 1-已备案 2-已退回，挂在 (unit_type, ward_id, week_start) 上。
--   护理部退回 = 该周单元内已发布/已变更行整批打回 0-草稿（不计在岗），由护士长重排后重新整周提交。
--
-- 幂等：三道键判重（id / 业务唯一键），重复执行零变化。
-- ⚠ 本表唯一键不含 del_flag，删除只走物理删（Mapper purgeXxx）；业务上只 upsert 不删。
-- ⚠ 执行后必须清 Redis 权限缓存 his:perm:role:*（TTL 10min）与 his:user:identity:*，或退出重登，
--   否则科护士长拿不到 2981、护理部仍留着审核码。
-- =====================================================================

-- ---------- A. 角色：科护士长（片区级审核人） ----------
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                      create_by, create_time, update_by, update_time, create_by_id, update_by_id, remark)
SELECT 30, '10040', '科护士长', 1, 2, 4, 39, 1,
       'sql295', NOW(), 'sql295', NOW(), 1, 1, '护理片区（大科）级护理排班审核人，编制与审核分离'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role r WHERE r.id = 30 OR r.role_code = '10040' OR r.role_name = '科护士长');

-- ---------- B. 按钮权限：护理排班-备案 ----------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, create_by_id, update_by_id, remark)
SELECT 29610, '护理排班-备案', 331, 5, 3, '', '', 'nursing.schedule:file', '',
       'nursing:schedule:file', 0, 1, 1, 1,
       'sql295', NOW(), 'sql295', NOW(), 1, 1, '护理部对护理片区已发布周次的备案与退回（不阻塞在岗生效）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.id = 29610 OR m.menu_key = 'nursing.schedule:file'
                                                  OR m.permission = 'nursing:schedule:file');

-- ---------- C1. 授权：科护士长(30) 门户 + 护理排班 + 审核发布 + 排班总览 ----------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000011, 30, 100, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000011 OR (rm.role_id = 30 AND rm.menu_id = 100));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000012, 30, 101, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000012 OR (rm.role_id = 30 AND rm.menu_id = 101));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000013, 30, 102, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000013 OR (rm.role_id = 30 AND rm.menu_id = 102));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000014, 30, 2001, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000014 OR (rm.role_id = 30 AND rm.menu_id = 2001));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000015, 30, 2945, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000015 OR (rm.role_id = 30 AND rm.menu_id = 2945));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000016, 30, 331, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000016 OR (rm.role_id = 30 AND rm.menu_id = 331));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000017, 30, 2981, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000017 OR (rm.role_id = 30 AND rm.menu_id = 2981));
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000018, 30, 2938, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000018 OR (rm.role_id = 30 AND rm.menu_id = 2938));

-- ---------- C2. 授权：护理部排班审核(29) 持备案按钮 ----------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920610251000000021, 29, 29610, 'sql295', NOW(), 'sql295', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920610251000000021 OR (rm.role_id = 29 AND rm.menu_id = 29610));

-- ---------- C3. 收口：护理部(29) 不再自己审核，审核权交片区 ----------
DELETE FROM sys_role_menu WHERE role_id = 29 AND menu_id = 2981;

-- ---------- C4. 角色更名：该角色只剩备案与退回，名字跟着口径走（role_code 10039 不变，鉴权不受影响） ----------
UPDATE sys_role
   SET role_name = '护理部排班备案', update_by = 'sql295', update_time = NOW()
 WHERE id = 29 AND role_name <> '护理部排班备案';

-- ---------- D. 审核人语义上移：临床 leaf 科室清空，改由片区节点配置 ----------
-- ⚠ 只清没有下级的 leaf 科室；片区/大科节点（有下级）才是审核人配置位。
UPDATE sys_department d
   SET d.nursing_auditor_id = NULL, d.update_by = 'sql295', d.update_time = NOW()
 WHERE d.nursing_auditor_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM (SELECT DISTINCT parent_id FROM sys_department WHERE del_flag = 0 AND parent_id > 0) p
                    WHERE p.parent_id = d.id);

ALTER TABLE sys_department
    MODIFY COLUMN nursing_auditor_id BIGINT NULL COMMENT '护理排班审核人 sys_employee.id';

-- ---------- F1. 备案表（周级，正交于行发布态） ----------
CREATE TABLE IF NOT EXISTS biz_nurse_schedule_filing (
    id            BIGINT       NOT NULL COMMENT '主键',
    unit_type     TINYINT      NOT NULL COMMENT '排班单元类型（1-病区 2-门诊科室）',
    ward_id       BIGINT       NOT NULL COMMENT '护理单元ID',
    ward_name     VARCHAR(128) NULL     COMMENT '护理单元名称（快照）',
    dept_id       BIGINT       NULL     COMMENT '所属科室ID',
    dept_name     VARCHAR(128) NULL     COMMENT '所属科室名称（快照）',
    week_start    DATE         NOT NULL COMMENT '周起始日（周一）',
    week_end      DATE         NOT NULL COMMENT '周结束日（周日）',
    filing_status TINYINT      NOT NULL DEFAULT 0 COMMENT '备案态（0-待备案 1-已备案 2-已退回）',
    filer_id      BIGINT       NULL     COMMENT '备案人ID',
    filer_name    VARCHAR(64)  NULL     COMMENT '备案人姓名（快照）',
    filing_time   DATETIME     NULL     COMMENT '备案时间',
    opinion       VARCHAR(200) NULL     COMMENT '备案意见/退回原因',
    create_by     VARCHAR(64)  NULL     COMMENT '创建人',
    create_time   DATETIME     NULL     COMMENT '创建时间',
    update_by     VARCHAR(64)  NULL     COMMENT '更新人',
    update_time   DATETIME     NULL     COMMENT '更新时间',
    create_by_id  BIGINT       NULL     COMMENT '创建人ID',
    update_by_id  BIGINT       NULL     COMMENT '更新人ID',
    del_flag      TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark        VARCHAR(500) NULL     COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_unit_week (unit_type, ward_id, week_start),
    KEY idx_week_start (week_start),
    KEY idx_filing_status (filing_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '护理排班周备案';

-- 本文件首版把备案人列注释写成跨表引用，按「列注释只写是什么」洗回（对已执行过首版的库自愈）
ALTER TABLE biz_nurse_schedule_filing
    MODIFY COLUMN filer_id BIGINT NULL COMMENT '备案人ID';

-- ---------- F2. 留痕动作字典：套用模板 / 备案 / 备案退回 ----------
-- 码值以 Java 枚举为唯一真相：APPLY_TEMPLATE=11、FILING=12、FILING_REJECT=13。
-- 本文件首版把备案写成 11、备案退回写成 12，与同期上线的排班模板（11-套用模板）撞码，下面按枚举洗回（只改文案，不改值）。
UPDATE sys_dict_data
   SET dict_label = '套用模板', dict_sort = 11, update_by = 'sql295', update_time = NOW()
 WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '11' AND dict_label <> '套用模板';
UPDATE sys_dict_data
   SET dict_label = '备案', dict_sort = 12, update_by = 'sql295', update_time = NOW()
 WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '12' AND dict_label <> '备案';
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status,
                           create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021113, 'his_nurse_schedule_change_type', '备案退回', '13', 13, 0, 1,
       'sql295', NOW(), 'sql295', NOW(), 1, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data d WHERE d.id = 896710000000021113
                                                   OR (d.dict_type = 'his_nurse_schedule_change_type' AND d.dict_value = '13'));

-- ---------- G. 存量已发布/已变更周次回填为已备案 ----------
INSERT INTO biz_nurse_schedule_filing (id, unit_type, ward_id, ward_name, dept_id, dept_name,
                                       week_start, week_end, filing_status, filing_time,
                                       create_by, create_time, update_by, update_time, create_by_id, update_by_id, remark)
SELECT 8967200000000000000 + ROW_NUMBER() OVER (ORDER BY w.unit_type, w.ward_id, w.week_start),
       w.unit_type, w.ward_id, w.ward_name, w.dept_id, w.dept_name,
       w.week_start, DATE_ADD(w.week_start, INTERVAL 6 DAY), 1, NOW(),
       'sql295', NOW(), 'sql295', NOW(), 1, 1, '改造上线前已发布周次，视为已备案'
FROM (SELECT IF(s.org_type = 2, 1, 2)                                                    AS unit_type,
              s.org_id                                                                   AS ward_id,
              MAX(s.org_name)                                                            AS ward_name,
              MAX(s.dept_id)                                                             AS dept_id,
              MAX(s.dept_name)                                                           AS dept_name,
              DATE_SUB(s.schedule_date, INTERVAL WEEKDAY(s.schedule_date) DAY)            AS week_start
        FROM biz_schedule s
        WHERE s.del_flag = 0
          AND s.staff_type IN (2, 7)
          AND s.publish_status IN (2, 4)
        GROUP BY s.org_id, DATE_SUB(s.schedule_date, INTERVAL WEEKDAY(s.schedule_date) DAY), IF(s.org_type = 2, 1, 2)) w
WHERE NOT EXISTS (SELECT 1 FROM biz_nurse_schedule_filing f
                   WHERE f.unit_type = w.unit_type AND f.ward_id = w.ward_id AND f.week_start = w.week_start);

-- ---------- 回查（执行后必跑） ----------
-- SELECT id, role_code, role_name, staff_type, data_scope FROM sys_role WHERE id = 30;
-- SELECT id, menu_name, parent_id, permission, is_visible, status FROM sys_menu WHERE id IN (29610, 2981);
-- SELECT rm.role_id, r.role_name, rm.menu_id, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id JOIN sys_role r ON r.id = rm.role_id
--  WHERE rm.role_id IN (10, 29, 30) AND m.id IN (331, 2981, 29610, 2938) ORDER BY rm.role_id, m.id;
-- SELECT id, dept_name, nursing_auditor_id FROM sys_department
--  WHERE id IN (19580002,19580012,19580021,19580025,19580047,19580052,19580058,19580063,19580067)
--     OR nursing_auditor_id IS NOT NULL;
-- SELECT COUNT(*) FROM biz_nurse_schedule_filing;
