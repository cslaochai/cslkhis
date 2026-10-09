-- =============================================================
-- 237 基础资质与授权铺底（用户/员工/岗位/角色/菜单已足量，仅补两张薄表）
-- 1) sys_employee_qualification 员工资格证书：按员工类型全覆盖
--    医师执业证(2)/护士执业证(3)/药师资格证(4)/技术职称聘书(5)
-- 2) sys_employee_tech_auth 医疗技术授权：外科系手术/麻醉科/内镜介入
-- 元数据统一：create_by=admin create_time=2026-05-08
-- 幂等：固定 ID 段 + NOT EXISTS 防重；冲突行由执行器跳过
-- =============================================================

-- ---------- 段1 医师执业证（emp_type=1 且无 type2 证书） ----------
INSERT INTO sys_employee_qualification (id, employee_id, cert_type, cert_no, issue_org, issue_date, valid_until, create_by, create_time, update_by, update_time, remark, create_by_id, update_by_id)
SELECT 2370000000000001000 + ROW_NUMBER() OVER (ORDER BY e.id), e.id, '2',
       CONCAT(DATE_FORMAT(e.hire_date,'%Y'),'11',LPAD(ROW_NUMBER() OVER (ORDER BY e.id)%9000+1000,4,'0')),
       '1', e.hire_date, LEAST(DATE_ADD(e.hire_date, INTERVAL 15 YEAR), '2045-12-31'),
       'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00','基础资质铺底（sql/237）',1,1
FROM sys_employee e
WHERE e.del_flag=0 AND e.emp_type=1 AND e.id<>1 AND e.hire_date IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_employee_qualification q WHERE q.employee_id=e.id AND q.cert_type='2');

-- ---------- 段2 护士执业证（emp_type=2 且无 type3 证书） ----------
INSERT INTO sys_employee_qualification (id, employee_id, cert_type, cert_no, issue_org, issue_date, valid_until, create_by, create_time, update_by, update_time, remark, create_by_id, update_by_id)
SELECT 2370000000000001400 + ROW_NUMBER() OVER (ORDER BY e.id), e.id, '3',
       CONCAT(DATE_FORMAT(e.hire_date,'%Y'),'22',LPAD(ROW_NUMBER() OVER (ORDER BY e.id)%9000+1000,4,'0')),
       '1', e.hire_date, LEAST(DATE_ADD(e.hire_date, INTERVAL 15 YEAR), '2045-12-31'),
       'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00','基础资质铺底（sql/237）',1,1
FROM sys_employee e
WHERE e.del_flag=0 AND e.emp_type=2 AND e.id<>1 AND e.hire_date IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_employee_qualification q WHERE q.employee_id=e.id AND q.cert_type='3');

-- ---------- 段3 药师资格证（emp_type=4 且无 type4 证书） ----------
INSERT INTO sys_employee_qualification (id, employee_id, cert_type, cert_no, issue_org, issue_date, valid_until, create_by, create_time, update_by, update_time, remark, create_by_id, update_by_id)
SELECT 2370000000000001800 + ROW_NUMBER() OVER (ORDER BY e.id), e.id, '4',
       CONCAT(DATE_FORMAT(e.hire_date,'%Y'),'33',LPAD(ROW_NUMBER() OVER (ORDER BY e.id)%9000+1000,4,'0')),
       '1', e.hire_date, LEAST(DATE_ADD(e.hire_date, INTERVAL 15 YEAR), '2045-12-31'),
       'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00','基础资质铺底（sql/237）',1,1
FROM sys_employee e
WHERE e.del_flag=0 AND e.emp_type=4 AND e.id<>1 AND e.hire_date IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_employee_qualification q WHERE q.employee_id=e.id AND q.cert_type='4');

-- ---------- 段4 技术职称聘书（有高级职称的医护药，年度聘书） ----------
INSERT INTO sys_employee_qualification (id, employee_id, cert_type, cert_no, issue_org, issue_date, valid_until, create_by, create_time, update_by, update_time, remark, create_by_id, update_by_id)
SELECT 2370000000000002000 + ROW_NUMBER() OVER (ORDER BY e.id), e.id, '5',
       CONCAT('HNPS', DATE_FORMAT(e.hire_date,'%Y'), LPAD(ROW_NUMBER() OVER (ORDER BY e.id)%9000+1000,4,'0')),
       '1', '2026-01-01', '2027-12-31',
       'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00','基础资质铺底（sql/237）',1,1
FROM sys_employee e
WHERE e.del_flag=0 AND e.emp_type IN (1,2,4) AND e.id<>1
  AND e.title IN ('301','302','303','304','401','402','403','404')
  AND NOT EXISTS (SELECT 1 FROM sys_employee_qualification q WHERE q.employee_id=e.id AND q.cert_type='5');

-- ---------- 段5 手术授权（外科系科室医师，级别按职称基准） ----------
INSERT INTO sys_employee_tech_auth (id, employee_id, employee_name, dept_id, dept_name, title, auth_category, tech_level, item_scope, auth_type, auth_basis, valid_from, valid_until, auth_status, apply_by, apply_time, approver_id, approver_name, approve_time, approve_opinion, create_by, create_time, update_by, update_time, remark, create_by_id, update_by_id)
SELECT 2370000000000003000 + ROW_NUMBER() OVER (ORDER BY e.id), e.id, e.emp_name, e.dept_id, e.dept_name, e.title, 1,
       CASE WHEN e.title LIKE '4%' THEN 4 WHEN e.title LIKE '3%' THEN 3 ELSE 2 END,
       NULL, 1, '2026 年度医疗技术临床应用准入评价（职称基准）', '2026-01-01', '2027-12-31', 2,
       'seed-237', '2026-05-08 09:00:00', 1, '医务科（铺底）', '2026-05-08 09:00:00', '按职称基准铺底授权，实际准入评价结果由技术授权台账页调整',
       'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00','基础授权铺底（sql/237）',1,1
FROM sys_employee e
WHERE e.del_flag=0 AND e.emp_type=1 AND e.id<>1 AND e.status=1
  AND e.dept_name REGEXP '外科|骨科|妇产|眼科|耳鼻喉|口腔|肛肠|泌尿|烧伤|整形'
  AND NOT EXISTS (SELECT 1 FROM sys_employee_tech_auth t WHERE t.employee_id=e.id AND t.auth_category=1);

-- ---------- 段6 麻醉授权（麻醉科医师） ----------
INSERT INTO sys_employee_tech_auth (id, employee_id, employee_name, dept_id, dept_name, title, auth_category, tech_level, item_scope, auth_type, auth_basis, valid_from, valid_until, auth_status, apply_by, apply_time, approver_id, approver_name, approve_time, approve_opinion, create_by, create_time, update_by, update_time, remark, create_by_id, update_by_id)
SELECT 2370000000000003400 + ROW_NUMBER() OVER (ORDER BY e.id), e.id, e.emp_name, e.dept_id, e.dept_name, e.title, 2,
       CASE WHEN e.title LIKE '4%' THEN 4 WHEN e.title LIKE '3%' THEN 3 ELSE 2 END,
       NULL, 1, '2026 年度医疗技术临床应用准入评价（职称基准）', '2026-01-01', '2027-12-31', 2,
       'seed-237', '2026-05-08 09:00:00', 1, '医务科（铺底）', '2026-05-08 09:00:00', '按职称基准铺底授权，实际准入评价结果由技术授权台账页调整',
       'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00','基础授权铺底（sql/237）',1,1
FROM sys_employee e
WHERE e.del_flag=0 AND e.emp_type=1 AND e.id<>1 AND e.status=1
  AND e.dept_name LIKE '%麻醉%'
  AND NOT EXISTS (SELECT 1 FROM sys_employee_tech_auth t WHERE t.employee_id=e.id AND t.auth_category=2);

-- ---------- 段7 内镜与介入授权（消化/内镜/介入/心内/呼吸） ----------
INSERT INTO sys_employee_tech_auth (id, employee_id, employee_name, dept_id, dept_name, title, auth_category, tech_level, item_scope, auth_type, auth_basis, valid_from, valid_until, auth_status, apply_by, apply_time, approver_id, approver_name, approve_time, approve_opinion, create_by, create_time, update_by, update_time, remark, create_by_id, update_by_id)
SELECT 2370000000000003800 + ROW_NUMBER() OVER (ORDER BY e.id), e.id, e.emp_name, e.dept_id, e.dept_name, e.title, 3,
       CASE WHEN e.title LIKE '4%' THEN 4 WHEN e.title LIKE '3%' THEN 3 ELSE 2 END,
       NULL, 1, '2026 年度医疗技术临床应用准入评价（职称基准）', '2026-01-01', '2027-12-31', 2,
       'seed-237', '2026-05-08 09:00:00', 1, '医务科（铺底）', '2026-05-08 09:00:00', '按职称基准铺底授权，实际准入评价结果由技术授权台账页调整',
       'admin','2026-05-08 00:00:00','admin','2026-05-08 00:00:00','基础授权铺底（sql/237）',1,1
FROM sys_employee e
WHERE e.del_flag=0 AND e.emp_type=1 AND e.id<>1 AND e.status=1
  AND e.dept_name REGEXP '消化|内镜|介入|心内|呼吸'
  AND NOT EXISTS (SELECT 1 FROM sys_employee_tech_auth t WHERE t.employee_id=e.id AND t.auth_category=3);

-- ---------- 自检 ----------
SELECT COUNT(*) AS chk_qual_new FROM sys_employee_qualification WHERE id >= 2370000000000001000;
SELECT cert_type, COUNT(*) AS c FROM sys_employee_qualification GROUP BY cert_type ORDER BY cert_type;
SELECT COUNT(*) AS chk_tech_new FROM sys_employee_tech_auth WHERE id >= 2370000000000003000;
SELECT auth_category, COUNT(*) AS c FROM sys_employee_tech_auth GROUP BY auth_category ORDER BY auth_category;
SELECT COUNT(*) AS chk_qual_no_name FROM sys_employee_qualification q LEFT JOIN sys_employee e ON e.id=q.employee_id WHERE e.id IS NULL;
SELECT COUNT(*) AS chk_tech_no_name FROM sys_employee_tech_auth t LEFT JOIN sys_employee e ON e.id=t.employee_id WHERE e.id IS NULL;
