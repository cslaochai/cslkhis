-- =====================================================================
-- 20261026 病区-科室多对多 + 医疗组（组织架构补齐：科室/病区双轴）
-- ---------------------------------------------------------------------
-- 背景：三甲医院的「科室」与「病区」是**两个正交维度**，不是父子关系：

-- ---------------- 1. 病区-科室多对多 ----------------
-- 一个病区可收多个科室的患者；一个科室也可有多个病区（如呼吸内科现有多个病区）。
-- bed_quota 表达「该科室在本病区的专科优先床数」，不填=不限额（全院机动床场景）。
CREATE TABLE IF NOT EXISTS sys_ward_dept
(
    id          BIGINT       NOT NULL COMMENT '主键ID',
    ward_id     BIGINT       NOT NULL COMMENT '病区ID（sys_ward.ward_id）',
    dept_id     BIGINT       NOT NULL COMMENT '科室ID（sys_department.id）',
    is_primary  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否本位科室（1-是 0-否）。每个病区有且仅有一条为 1，且必须与 sys_ward.dept_id 一致',
    bed_quota   INT          NULL COMMENT '该科室在本病区的专科优先床数（NULL=不限额，剩余为机动床）',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前，影响病区下科室展示顺序）',
    create_by   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新人',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by_id BIGINT      NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by_id BIGINT      NOT NULL DEFAULT 0 COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ward_dept (ward_id, dept_id) COMMENT '同一病区同一科室只允许一条',
    KEY idx_dept_id (dept_id) COMMENT '按科室反查可收治的病区'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '病区-科室多对多（病区收治范围）。可跨科收治，床位可分专科优先床与机动床';

-- ---------------- 2. 医疗组 / 主诊组 ----------------
-- 依据《三级医院评审标准(2022年版)实施细则》2.2.27：科主任根据科室/病区床位、工作量、
-- 医师专业资质组建若干医疗团队（主诊医师制/医疗组长制），指定负责人，实行三级查房。
-- 医疗组挂在**科室**下（不挂病区），组内医师查房时到病区，归属固定在科室。
-- 刻意不设 del_flag：group_code 上有唯一键，逻辑删除会让「删了再建同编码」撞 Duplicate entry，
-- 与 sys_ward / sys_menu 口径一致，一律物理删除（删除前已校验组内无医师）。
CREATE TABLE IF NOT EXISTS sys_med_group
(
    id          BIGINT       NOT NULL COMMENT '主键ID',
    group_code  VARCHAR(32)  NOT NULL COMMENT '医疗组编码（唯一）',
    group_name  VARCHAR(64)  NOT NULL COMMENT '医疗组名称（如「呼吸内科一组」）',
    dept_id     BIGINT       NOT NULL COMMENT '所属科室ID（sys_department.id）',
    leader_id   BIGINT       NULL COMMENT '主诊医师/医疗组长（sys_employee.id）',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    remark      VARCHAR(500) NULL COMMENT '备注',
    create_by   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新人',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by_id BIGINT      NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by_id BIGINT      NOT NULL DEFAULT 0 COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_code (group_code),
    KEY idx_dept_id (dept_id),
    KEY idx_leader_id (leader_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '医疗组/主诊组（科室下的医师分组，三级查房与主诊医师负责制的落点）';

-- ---------------- 3. 员工补病区归属与医疗组 ----------------
-- ward_id：护理人员用（护士挂病区）。医生留空——医生归属科室 + 医疗组，查房时才到病区。
-- group_id：医生用（挂医疗组）。两者互不强制，由业务侧按员工岗位角色决定填哪个。
SET @db = DATABASE();

SET @sql = (
    SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_employee' AND COLUMN_NAME = 'ward_id'),
              'SELECT 1',
              'ALTER TABLE sys_employee ADD COLUMN ward_id BIGINT NULL COMMENT ''病区ID（护理人员用；医生为空，医生归属科室+医疗组）'' AFTER dept_name')
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_employee' AND COLUMN_NAME = 'group_id'),
              'SELECT 1',
              'ALTER TABLE sys_employee ADD COLUMN group_id BIGINT NULL COMMENT ''医疗组ID（医生用；sys_med_group.id）'' AFTER ward_id')
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS
                     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_employee' AND INDEX_NAME = 'idx_ward_id'),
              'SELECT 1',
              'ALTER TABLE sys_employee ADD INDEX idx_ward_id (ward_id)')
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- 4. 回填：现有病区的本位科室进多对多表 ----------------
-- 只是把「一对一」现状平移成「多对多里的本位那条」，不新增任何收治关系，
-- 所以回填前后各科室能看到的病区完全不变（现有 6 处数据权限折算结果不变）。
INSERT INTO sys_ward_dept (id, ward_id, dept_id, is_primary, bed_quota, sort_order,
                           create_by, update_by, create_by_id, update_by_id)
SELECT 9100000000000000000 + ROW_NUMBER() OVER (ORDER BY w.ward_id),
       w.ward_id, w.dept_id, 1, NULL, 0,
       'admin', 'admin', 1, 1
FROM sys_ward w
WHERE w.dept_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_ward_dept wd WHERE wd.ward_id = w.ward_id AND wd.dept_id = w.dept_id);

-- 自愈：如果某个病区的 sys_ward.dept_id 被改过，而旧科室还留着 is_primary=1，纠正回本位那条
UPDATE sys_ward_dept wd
    JOIN sys_ward w ON w.ward_id = wd.ward_id
SET wd.is_primary = IF(wd.dept_id = w.dept_id, 1, 0)
WHERE wd.is_primary <> IF(wd.dept_id = w.dept_id, 1, 0);

-- ---------------- 5. 修正 dept_type 失真 ----------------
-- 临床科室同时具备门诊与住院能力，dept_type 是逗号分隔多值（前端用 FIND_IN_SET 判定）。
-- 现库里 36 个已设病区的科室只标了 1(门诊)，导致「住院科室」口径查不到它们。
UPDATE sys_department d
SET d.dept_type = CONCAT(d.dept_type, ',4')
WHERE d.del_flag = 0
  AND FIND_IN_SET('4', d.dept_type) = 0
  AND EXISTS (SELECT 1 FROM sys_ward w WHERE w.dept_id = d.id);

-- ---------------- 6. 修正假根节点 ----------------
-- 原值：dept_code=1001 / dept_name='医院信息系统' / dept_type='4'(住院科室)
-- 病症：根节点被标成住院科室，会被「住院科室」下拉与统计当成可收治科室；
--       名字是造数残留。改成医院名 + 类型 5(其他)。
UPDATE sys_department
SET dept_name = '长沙麓康医院',
    dept_type = '5'
WHERE id = 1958001
  AND dept_name = '医院信息系统';

-- ---------------- 7. 菜单：病区管理 / 医疗组管理 ----------------
-- 挂在 800「组织与资源」下，与科室管理(801)/诊室管理(802)/员工管理(803) 同级。
-- icon 用 HomeFilled / Avatar（已核实全场未占用，避免撞图）。
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29620, '病区管理', 800, 42, 2, '/system/ward', 'system/ward/WardView', 'org.ward',
                      'HomeFilled', 'org:ward:list', 0, 0, 1, 1,
                      'admin', 'admin', 0, '病区主数据：病区收治科室多对多、专科优先床数', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.ward');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29621, '病区管理-新增', 29620, 1, 3, NULL, NULL, 'org.ward.add',
                      '', 'org:ward:add', 0, 0, 1, 1,
                      'admin', 'admin', 0, NULL, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.ward.add');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29622, '病区管理-删除', 29620, 3, 3, NULL, NULL, 'org.ward.delete',
                      '', 'org:ward:delete', 0, 0, 1, 1,
                      'admin', 'admin', 0, NULL, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.ward.delete');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29623, '医疗组管理', 800, 43, 2, '/system/medGroup', 'system/medGroup/MedGroupView', 'org.medGroup',
                      'Avatar', 'org:medGroup:list', 0, 0, 1, 1,
                      'admin', 'admin', 0, '医疗组/主诊组：科室下的医师分组，主诊医师负责制落点', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.medGroup');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29624, '医疗组管理-新增', 29623, 1, 3, NULL, NULL, 'org.medGroup.add',
                      '', 'org:medGroup:add', 0, 0, 1, 1,
                      'admin', 'admin', 0, NULL, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.medGroup.add');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key,
                      icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29625, '医疗组管理-删除', 29623, 3, 3, NULL, NULL, 'org.medGroup.delete',
                      '', 'org:medGroup:delete', 0, 0, 1, 1,
                      'admin', 'admin', 0, NULL, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.medGroup.delete');

-- ---------------- 8. 授权 ----------------
-- 与科室管理(801)/诊室管理(802)/员工管理(803) 同口径：只授系统管理员(role_id=1)。
-- 祖先目录 800 该角色已有，无需重复。
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9200000000000000000 + ROW_NUMBER() OVER (ORDER BY m.id), 1, m.id, 'admin', 'admin', 1, 1
FROM sys_menu m
WHERE m.id BETWEEN 29620 AND 29625
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);

-- ---------------- 9. 自检 ----------------
SELECT '① 病区-科室关系' AS 项,
       (SELECT COUNT(*) FROM sys_ward) AS 病区数,
       (SELECT COUNT(*) FROM sys_ward_dept) AS 关系条数,
       (SELECT COUNT(*) FROM sys_ward_dept WHERE is_primary = 1) AS 本位条数,
       (SELECT COUNT(*) FROM sys_ward w WHERE NOT EXISTS(
           SELECT 1 FROM sys_ward_dept wd WHERE wd.ward_id = w.ward_id AND wd.is_primary = 1)) AS 缺本位科室的病区
UNION ALL
SELECT '② 本位一致性（应为 0）', NULL, NULL, NULL,
       (SELECT COUNT(*) FROM sys_ward w JOIN sys_ward_dept wd
        ON wd.ward_id = w.ward_id AND wd.is_primary = 1 WHERE wd.dept_id <> w.dept_id)
UNION ALL
SELECT '③ 临床科室补 4 后', NULL,
       (SELECT COUNT(*) FROM sys_department d WHERE d.del_flag = 0 AND FIND_IN_SET('4', d.dept_type) > 0),
       (SELECT COUNT(*) FROM sys_department d WHERE d.del_flag = 0 AND FIND_IN_SET('1', d.dept_type) > 0),
       (SELECT COUNT(*) FROM sys_department d WHERE d.del_flag = 0
                                             AND FIND_IN_SET('1', d.dept_type) > 0
                                             AND FIND_IN_SET('4', d.dept_type) > 0)
UNION ALL
SELECT '④ 根节点', NULL, (SELECT id FROM sys_department WHERE id = 1958001), NULL,
       (SELECT COUNT(*) FROM sys_department WHERE id = 1958001 AND dept_name = '长沙麓康医院' AND dept_type = '5')
UNION ALL
SELECT '⑤ 员工新列', NULL,
       (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db
                                                         AND TABLE_NAME = 'sys_employee'
                                                         AND COLUMN_NAME IN ('ward_id', 'group_id')),
       (SELECT COUNT(*) FROM sys_med_group), NULL
UNION ALL
SELECT '⑥ 菜单与授权', NULL,
       (SELECT COUNT(*) FROM sys_menu WHERE id BETWEEN 29620 AND 29625),
       (SELECT COUNT(*) FROM sys_role_menu WHERE menu_id BETWEEN 29620 AND 29625), NULL;
