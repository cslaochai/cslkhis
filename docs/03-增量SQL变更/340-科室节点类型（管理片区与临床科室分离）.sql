-- ============================================================
-- 340 科室节点类型：把「管理片区」与「临床科室」拆成两个维度
-- 日期：2026-11-20
-- 背景：
--   现有 sys_department 只有 dept_type（1-门诊 2-医技 3-药房 4-住院 5-其他 6-急诊），
--   它是「服务范围」，一个科室可多选。但组织树上还有另一件事实——节点角色：
--   「内科系统 / 外科系统 / 医技科室 / 行政后勤部」是管理片区或支撑部门，
--   不是能收治患者、能排医生护士班的科室。
--   两件事混在一列的直接后果（本次改造起因）：
--     · 内科系统 dept_type=4（住院），于是它出现在「收治科室」下拉；
--     · 内科系统没有病区，于是它又被护理排班当成「有护理编制的门诊科室」列出，
--       护士长可以在「内科系统」这个虚拟单元上排整周班，提交审核时找不到归属科室；
--     · 员工岗位（sys_employee_post.dept_id）同样能选到内科系统，医生岗位挂在片区上。
--   大厂口径（Epic Facility > Service Area > Revenue Location > Hospital Area > Department，
--   部门类型 Hospital Unit/Outpatient/HOD/Virtual）也是把「组织层级」与「服务类型」分开存。
--
-- 本次做法（方案 A）：
--   1) 新增 node_type 单值列（节点角色，结构性事实 → 落 Java 枚举，不做字典、不许操作员改码值）；
--   2) dept_type 保留列名与字典 his_dept_type，语义收窄为「服务范围」；
--      管理片区不再挂任何服务范围（清空），避免它再次混进门诊/住院/急诊口径。
--   3) 不做院区（Facility）层：本系统只有一个院区，根节点「长沙麓康医院」用 0-医疗机构 表示。
--
-- node_type 码值（与 DeptNodeTypeEnum 逐字对齐）：
--   0-医疗机构  1-管理片区  2-临床科室  3-医技科室  4-药剂科室  5-行政后勤  6-医疗辅助  7-其他
--
-- 前端/后端配套（已改）：
--   · 后端 DeptNodeTypeEnum（his-core-common/enums）；
--   · SysDepartment / DepartmentVO / DepartmentUpsertDTO（@NotNull + @AllowedValues）/
--     DepartmentQueryDTO / DepartmentSelectDTO 新增 nodeType，upsert 拒绝「片区 + 服务范围」；
--   · DeptBriefVO 透出 nodeType，排班单元下拉（StaffScheduleServiceImpl#unitSelectList）、
--     护理排班科室候选（BizScheduleMapper#selectDeptOptions）、小程序挂号目录（MiniDirectoryMapper）
--     按 isBusinessDept（2/3/4/6/7）收口；未回填的历史行先放行，避免升级瞬间下拉变空；
--   · 前端科室管理页增加「节点类型」列与筛选、编辑弹框增加节点类型并联动服务范围；
--     员工岗位科室下拉对片区显示「（管理片区）」标注（科护士长的岗位本来就挂片区，不能简单屏蔽）。
-- 范围边界：
--   · 只动 sys_department 一列 + 96 行数据；sys_bed.dept_id 与 biz_admission 两个 dept 字段的语义澄清
--     不在本次 DDL 里（是注释口径问题，另行处理）。
-- 落地后：后端需重启（枚举与过滤条件在代码里）；前端科室管理页需刷新。
-- 回滚：见文件末尾。
-- ============================================================

-- A 新增列（放在 dept_type 后，便于对照两列语义）。
--   默认 '2'（临床科室）：新增科室走接口时 nodeType 必填，默认值只兜「漏传的存量脚本」，
--   片区/医技/行政必须显式写自己的码值，不会被静默归成临床。
ALTER TABLE sys_department
    ADD COLUMN node_type VARCHAR(2) NOT NULL DEFAULT '2'
        COMMENT '节点类型（0-医疗机构 1-管理片区 2-临床科室 3-医技科室 4-药剂科室 5-行政后勤 6-医疗辅助 7-其他）'
        AFTER dept_type,
    ADD KEY idx_node_type (node_type);

-- B 根节点：医疗机构本身
UPDATE sys_department SET node_type = '0' WHERE id = 1958001;

-- C 14 个管理片区/系统节点
UPDATE sys_department
SET node_type = '1'
WHERE id IN (19580002, 19580012, 19580021, 19580025, 19580029, 19580034,
             19580042, 19580047, 19580052, 19580058, 19580063, 19580067,
             19580071, 19580075);

-- D 医技科室 10 个子节点
UPDATE sys_department
SET node_type = '3'
WHERE parent_id = 19580034;

-- E 药剂科 4 个子节点
UPDATE sys_department
SET node_type = '4'
WHERE parent_id = 19580042;

-- F 行政后勤部 11 个子节点 → 5-行政后勤；病案室是医疗辅助（病案管理），单独改 6
UPDATE sys_department
SET node_type = '5'
WHERE parent_id = 19580075;

UPDATE sys_department
SET node_type = '6'
WHERE dept_name IN ('病案室');

-- G 健康管理中心 3 个子节点：体检/预防保健，不属于临床病房序列 → 7-其他
UPDATE sys_department
SET node_type = '7'
WHERE parent_id = 19580071;

-- H 其余全部是临床科室（内/外/妇/儿/五官/急危重症/中医/肿瘤/精神/感染 各系统的末级科室）
UPDATE sys_department
SET node_type = '2'
WHERE node_type IS NULL;

-- I 服务范围收窄：片区与机构本身不挂门诊/住院/急诊类型（历史上它们带 1/4/5 的误导值）
UPDATE sys_department
SET dept_type = ''
WHERE node_type IN ('0', '1');

-- J 回查：每个节点都应恰好落在一个码值上，且没有片区残留服务范围
SELECT node_type, COUNT(*) AS cnt
FROM sys_department
GROUP BY node_type
ORDER BY node_type;

SELECT id, dept_name, node_type, dept_type
FROM sys_department
WHERE node_type IN ('0', '1') AND dept_type <> '';

-- ============================================================
-- 回滚段
-- ============================================================
-- 1) 恢复 14 个系统节点的 dept_type（原值与 dept_name 一一对应，按需执行）：
--    UPDATE sys_department SET dept_type = CASE id
--        WHEN 19580002 THEN '4' WHEN 19580012 THEN '4' WHEN 19580021 THEN '4'
--        WHEN 19580025 THEN '4' WHEN 19580029 THEN '1' WHEN 19580034 THEN '2'
--        WHEN 19580042 THEN '3' WHEN 19580047 THEN '1,6' WHEN 19580052 THEN '1'
--        WHEN 19580058 THEN '4' WHEN 19580063 THEN '1' WHEN 19580067 THEN '1'
--        WHEN 19580071 THEN '1' WHEN 19580075 THEN '5' END
--    WHERE id IN (19580002, 19580012, 19580021, 19580025, 19580029, 19580034,
--                 19580042, 19580047, 19580052, 19580058, 19580063, 19580067,
--                 19580071, 19580075);
--    UPDATE sys_department SET dept_type = '5' WHERE id = 1958001;
-- 2) 删列：
--    ALTER TABLE sys_department DROP COLUMN node_type;
