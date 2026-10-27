-- ============================================================================
-- 300 - 科室 → 诊疗科目映射（病案首页 / 卫统上报 / 医保报盘的标准科别出口）
--
-- 【为什么要这张表】
-- 院内 sys_department 是"行政科室"，dept_code 是本院自编码（如 10000101 呼吸内科）。
-- 但病案首页上报、卫生统计直报、医保结算清单要求的"科室"是**国家卫健委
-- 《医疗机构诊疗科目名录》**里的诊疗科目代码（如 03.01 呼吸内科专业）。
-- 两者不是一回事：
--   · 一个行政科室可能覆盖多个科目（如"外科系统"下的普外科同时开展 04.01 普通外科
--     与 04.09 其他专业）；
--   · 一个科目也可能由多个科室共同承担（如"急诊内科/急诊外科"都归 20 急诊医学科）。
-- 主流 HIS 的对应能力叫「病案科室对应设置」，本表即该口径，只做映射不做替换——
-- 病案首页仍然存院内 dept_id（历史数据与数据权限都依赖它），上报时再折算标准科目码。
--
-- 【科目种子数据的可靠边界】
-- 一级科目代码与名称取自国家卫健委《医疗机构诊疗科目名录》；
-- 二级科目**只种了本院子系统实际用到的那些**（内科 03.xx、外科 04.xx、妇产科 05.xx、
-- 儿科 07.01、中医科 31.xx），其余请按本院《医疗机构执业许可证》副本核验后自行补录。
-- 科目表可增删改，不在代码里写死枚举——名录会修订，写死就得改代码。
--
-- 【删除口径】
-- 两张新表都有业务唯一键（subject_code / dept_id+subject_code），一律**物理删除**，
-- 不设 del_flag：逻辑删除会占着唯一键，"删了再建同码"会撞 Duplicate entry 报 500。
-- 这是本项目第三次踩这个坑，已写进 AGENTS 铁律。
-- ============================================================================

-- ---------------- 1. 诊疗科目主数据 ----------------
CREATE TABLE IF NOT EXISTS sys_med_subject
(
    id            BIGINT      NOT NULL COMMENT '主键ID',
    subject_code  VARCHAR(16) NOT NULL COMMENT '诊疗科目代码（如 03 / 03.01）',
    subject_name  VARCHAR(64) NOT NULL COMMENT '诊疗科目名称（如 内科 / 呼吸内科专业）',
    parent_code   VARCHAR(16) NULL COMMENT '上级科目代码（一级科目为空）',
    subject_level TINYINT     NOT NULL DEFAULT 1 COMMENT '科目层级：1-一级科目 2-二级科目',
    is_clinical   TINYINT     NOT NULL DEFAULT 1 COMMENT '是否临床科目：1-是 0-否（医技/药学等）',
    status        TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    sort_order    INT         NOT NULL DEFAULT 0 COMMENT '排序号',
    remark        VARCHAR(200) NULL COMMENT '备注',
    create_by     VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by     VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by_id  BIGINT      NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by_id  BIGINT      NOT NULL DEFAULT 0 COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_subject_code (subject_code),
    KEY idx_parent_code (parent_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '诊疗科目主数据（国家卫健委《医疗机构诊疗科目名录》）';

-- ---------------- 2. 科室 → 诊疗科目 映射 ----------------
CREATE TABLE IF NOT EXISTS sys_dept_subject_map
(
    id           BIGINT      NOT NULL COMMENT '主键ID',
    dept_id      BIGINT      NOT NULL COMMENT '院内科室ID（sys_department.id）',
    subject_code VARCHAR(16) NOT NULL COMMENT '诊疗科目代码（sys_med_subject.subject_code）',
    is_primary   TINYINT     NOT NULL DEFAULT 0 COMMENT '是否上报主科目：1-是（病案首页取这个）0-否',
    remark       VARCHAR(200) NULL COMMENT '备注',
    create_by    VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by_id BIGINT      NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by_id BIGINT      NOT NULL DEFAULT 0 COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_subject (dept_id, subject_code),
    KEY idx_subject_code (subject_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '科室与诊疗科目映射（病案首页/卫统/医保上报的标准科别出口）';

-- ---------------- 3. 诊疗科目种子（一级科目） ----------------
INSERT INTO sys_med_subject (id, subject_code, subject_name, parent_code, subject_level, is_clinical,
                             status, sort_order, create_by, update_by, create_by_id, update_by_id)
SELECT 9110000000000000000 + ROW_NUMBER() OVER (ORDER BY t.seq), t.code, t.name, NULL, 1, t.clinical,
       1, t.seq, 'admin', 'admin', 1, 1
FROM (SELECT 1 AS seq, '01' AS code, '预防保健科' AS name, 0 AS clinical
      UNION ALL SELECT 2, '02', '全科医疗科', 1
      UNION ALL SELECT 3, '03', '内科', 1
      UNION ALL SELECT 4, '04', '外科', 1
      UNION ALL SELECT 5, '05', '妇产科', 1
      UNION ALL SELECT 6, '06', '妇女保健科', 0
      UNION ALL SELECT 7, '07', '儿科', 1
      UNION ALL SELECT 8, '08', '小儿外科', 1
      UNION ALL SELECT 9, '09', '儿童保健科', 0
      UNION ALL SELECT 10, '10', '眼科', 1
      UNION ALL SELECT 11, '11', '耳鼻咽喉科', 1
      UNION ALL SELECT 12, '12', '口腔科', 1
      UNION ALL SELECT 13, '13', '皮肤科', 1
      UNION ALL SELECT 14, '14', '医疗美容科', 1
      UNION ALL SELECT 15, '15', '精神科', 1
      UNION ALL SELECT 16, '16', '传染科', 1
      UNION ALL SELECT 17, '17', '结核病科', 1
      UNION ALL SELECT 18, '18', '地方病科', 1
      UNION ALL SELECT 19, '19', '肿瘤科', 1
      UNION ALL SELECT 20, '20', '急诊医学科', 1
      UNION ALL SELECT 21, '21', '康复医学科', 1
      UNION ALL SELECT 22, '22', '运动医学科', 1
      UNION ALL SELECT 23, '23', '临终关怀科', 1
      UNION ALL SELECT 24, '24', '特种医学与军事医学科', 1
      UNION ALL SELECT 25, '25', '麻醉科', 1
      UNION ALL SELECT 26, '26', '疼痛科', 1
      UNION ALL SELECT 27, '27', '重症医学科', 1
      UNION ALL SELECT 28, '28', '医学检验科', 0
      UNION ALL SELECT 29, '29', '病理科', 0
      UNION ALL SELECT 30, '30', '医学影像科', 0
      UNION ALL SELECT 31, '31', '中医科', 1
      UNION ALL SELECT 32, '32', '民族医学科', 1
      UNION ALL SELECT 33, '33', '中西医结合科', 1) t
WHERE NOT EXISTS (SELECT 1 FROM sys_med_subject x WHERE x.subject_code = t.code);

-- ---------------- 4. 诊疗科目种子（本院子系统实际用到的二级科目） ----------------
INSERT INTO sys_med_subject (id, subject_code, subject_name, parent_code, subject_level, is_clinical,
                             status, sort_order, create_by, update_by, create_by_id, update_by_id)
SELECT 9120000000000000000 + ROW_NUMBER() OVER (ORDER BY t.code), t.code, t.name, t.parent, 2, 1,
       1, t.seq, 'admin', 'admin', 1, 1
FROM (SELECT '03.01' AS code, '呼吸内科专业' AS name, '03' AS parent, 1 AS seq
      UNION ALL SELECT '03.02', '消化内科专业', '03', 2
      UNION ALL SELECT '03.03', '神经内科专业', '03', 3
      UNION ALL SELECT '03.04', '心血管内科专业', '03', 4
      UNION ALL SELECT '03.05', '血液内科专业', '03', 5
      UNION ALL SELECT '03.06', '肾病学专业', '03', 6
      UNION ALL SELECT '03.07', '内分泌专业', '03', 7
      UNION ALL SELECT '03.08', '免疫学专业', '03', 8
      UNION ALL SELECT '03.09', '变态反应专业', '03', 9
      UNION ALL SELECT '03.10', '老年病专业', '03', 10
      UNION ALL SELECT '04.01', '普通外科专业', '04', 11
      UNION ALL SELECT '04.02', '神经外科专业', '04', 12
      UNION ALL SELECT '04.03', '骨科专业', '04', 13
      UNION ALL SELECT '04.04', '泌尿外科专业', '04', 14
      UNION ALL SELECT '04.05', '胸外科专业', '04', 15
      UNION ALL SELECT '04.06', '烧伤专业', '04', 16
      UNION ALL SELECT '04.07', '整形外科专业', '04', 17
      UNION ALL SELECT '04.08', '小儿外科专业', '04', 18
      UNION ALL SELECT '05.01', '妇科专业', '05', 19
      UNION ALL SELECT '05.02', '产科专业', '05', 20
      UNION ALL SELECT '05.03', '计划生育专业', '05', 21
      UNION ALL SELECT '05.04', '优生学专业', '05', 22
      UNION ALL SELECT '05.05', '生殖健康与不孕症专业', '05', 23
      UNION ALL SELECT '07.01', '新生儿专业', '07', 24
      UNION ALL SELECT '15.05', '临床心理专业', '15', 25
      UNION ALL SELECT '31.01', '中医内科专业', '31', 26
      UNION ALL SELECT '31.03', '中医妇产科专业', '31', 27
      UNION ALL SELECT '31.04', '中医儿科专业', '31', 28
      UNION ALL SELECT '31.10', '中医骨伤科专业', '31', 29
      UNION ALL SELECT '31.13', '针灸科专业', '31', 30
      UNION ALL SELECT '31.14', '推拿科专业', '31', 31) t
WHERE NOT EXISTS (SELECT 1 FROM sys_med_subject x WHERE x.subject_code = t.code);

-- ---------------- 5. 按科室名称自动回填映射（初装脚手架，人工复核后调整） ----------------
-- 只匹配"叶子临床科室"；内科系统/外科系统/妇产科系统/儿科系统/五官及皮肤科/
-- 急危重症中心/中医科系统/肿瘤科系统 这 8 个是分组目录节点，不参与上报，故意不映射。
-- 用法：上线前用页面的"未映射科室"清单核对补全，别指望这里 100% 对。
INSERT INTO sys_dept_subject_map (id, dept_id, subject_code, is_primary,
                                  create_by, update_by, create_by_id, update_by_id)
SELECT 9130000000000000000 + ROW_NUMBER() OVER (ORDER BY d.id), d.id, m.subject_code, 1,
       'admin', 'admin', 1, 1
FROM sys_department d
         JOIN (SELECT '呼吸内科' AS dept_name, '03.01' AS subject_code
               UNION ALL SELECT '消化内科', '03.02'
               UNION ALL SELECT '神经内科', '03.03'
               UNION ALL SELECT '心血管内科', '03.04'
               UNION ALL SELECT '血液内科', '03.05'
               UNION ALL SELECT '肾内科', '03.06'
               UNION ALL SELECT '内分泌科', '03.07'
               UNION ALL SELECT '风湿免疫科', '03.08'
               UNION ALL SELECT '老年病科', '03.10'
               UNION ALL SELECT '全科医学科', '02'
               UNION ALL SELECT '普通外科', '04.01'
               UNION ALL SELECT '肝胆外科', '04.01'
               UNION ALL SELECT '甲乳外科', '04.01'
               UNION ALL SELECT '肛肠外科', '04.01'
               UNION ALL SELECT '血管外科', '04.01'
               UNION ALL SELECT '神经外科', '04.02'
               UNION ALL SELECT '骨科', '04.03'
               UNION ALL SELECT '泌尿外科', '04.04'
               UNION ALL SELECT '心胸外科', '04.05'
               UNION ALL SELECT '烧伤整形外科', '04.06'
               UNION ALL SELECT '小儿外科', '04.08'
               UNION ALL SELECT '妇科', '05.01'
               UNION ALL SELECT '产科', '05.02'
               UNION ALL SELECT '产前诊断中心', '05.04'
               UNION ALL SELECT '生殖医学中心', '05.05'
               UNION ALL SELECT '小儿内科', '07'
               UNION ALL SELECT '新生儿科', '07.01'
               UNION ALL SELECT '儿童保健科', '09'
               UNION ALL SELECT '眼科', '10'
               UNION ALL SELECT '耳鼻喉科', '11'
               UNION ALL SELECT '口腔科', '12'
               UNION ALL SELECT '皮肤科', '13'
               UNION ALL SELECT '医疗美容科', '14'
               UNION ALL SELECT '精神心理科', '15'
               UNION ALL SELECT '精神科门诊', '15'
               UNION ALL SELECT '睡眠医学中心', '15'
               UNION ALL SELECT '临床心理科', '15.05'
               UNION ALL SELECT '感染疾病科', '16'
               UNION ALL SELECT '肝病科', '16'
               UNION ALL SELECT '发热门诊', '16'
               UNION ALL SELECT '肠道门诊', '16'
               UNION ALL SELECT '肿瘤内科', '19'
               UNION ALL SELECT '肿瘤外科', '19'
               UNION ALL SELECT '急诊内科', '20'
               UNION ALL SELECT '急诊外科', '20'
               UNION ALL SELECT '急诊儿科', '20'
               UNION ALL SELECT '安宁疗护病房', '23'
               UNION ALL SELECT '重症医学科(ICU)', '27'
               UNION ALL SELECT '中医内科', '31.01'
               UNION ALL SELECT '中医妇科', '31.03'
               UNION ALL SELECT '中医儿科', '31.04'
               UNION ALL SELECT '中医骨伤科', '31.10'
               UNION ALL SELECT '针灸推拿科', '31.13'
               UNION ALL SELECT '健康管理中心', '01'
               UNION ALL SELECT '普通体检科', '01'
               UNION ALL SELECT 'VIP体检中心', '01'
               UNION ALL SELECT '预防保健科', '01') m ON m.dept_name = d.dept_name
WHERE d.del_flag = 0
  AND NOT EXISTS (SELECT 1 FROM sys_dept_subject_map x
                  WHERE x.dept_id = d.id AND x.subject_code = m.subject_code);

-- ---------------- 6. 菜单与授权 ----------------
-- 挂在「组织与资源」(800) 下，排在医疗组管理(43)之后
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29626, '诊疗科目映射', 800, 44, 2, '/system/medSubject', 'system/medSubject/MedSubjectView',
       'org.medSubject', 'Collection', 'org:medSubject:list', 0, 0, 1, 1,
       'admin', 'admin', 0, '科室与国家卫健委诊疗科目对照，病案首页/卫统/医保上报的标准科别出口', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.medSubject');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29627, '诊疗科目映射-保存', 29626, 1, 3, NULL, NULL, 'org.medSubject.add', NULL,
       'org:medSubject:add', 0, 0, 1, 1, 'admin', 'admin', 0, '新增/修改科目与科室映射', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.medSubject.add');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29628, '诊疗科目映射-删除', 29626, 2, 3, NULL, NULL, 'org.medSubject.delete', NULL,
       'org:medSubject:delete', 0, 0, 1, 1, 'admin', 'admin', 0, '删除科目或科室映射', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.medSubject.delete');

-- 普通角色显式授权（admin 是显式授权制，不会自动全量）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000000000 + ROW_NUMBER() OVER (ORDER BY m.id), 1, m.id, 'admin', 'admin', 1, 1
FROM sys_menu m
WHERE m.id IN (29626, 29627, 29628)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = 1 AND x.menu_id = m.id);

-- ---------------- 7. 核对 ----------------
SELECT (SELECT COUNT(*) FROM sys_med_subject WHERE subject_level = 1)                       AS `level1_cnt`,
       (SELECT COUNT(*) FROM sys_med_subject WHERE subject_level = 2)                       AS `level2_cnt`,
       (SELECT COUNT(*) FROM sys_dept_subject_map)                                          AS `map_cnt`,
       (SELECT COUNT(*) FROM sys_dept_subject_map WHERE is_primary = 1)                     AS `primary_cnt`,
       (SELECT COUNT(*) FROM sys_department d
        WHERE d.del_flag = 0 AND (FIND_IN_SET('1', d.dept_type) > 0 OR FIND_IN_SET('4', d.dept_type) > 0)
          AND NOT EXISTS (SELECT 1 FROM sys_dept_subject_map x WHERE x.dept_id = d.id))      AS `unmapped_clinical`,
       (SELECT COUNT(*) FROM sys_menu WHERE id BETWEEN 29626 AND 29628 AND del_flag = 0)     AS `menu_cnt`;
