-- 391 角色 6「医技人员」兜底岗人员：按所在科室在岗同岗角色归位
-- 日期：2026-11-26
-- 背景：角色 6 历史上混挂了一批不属于医技执行岗的人员。现按「同科室在岗人员的岗位角色」为标尺，
--       把能明确归位的人员转到对应专岗；无对应专岗的（康复治疗师、病理技师）暂留角色 6 兜底。
-- 归位依据（同科室现有在岗人员角色为参照）：
--   设备科 → 39 设备与供应室管理员（设备资产管理，与上一轮 CSSD 同岗）
--   输血科 → 11 检验技师（同科何璐/向玲/袁庆均为检验技师）
--   静脉配液中心 → 5 药剂师（同科朱语嫣/江倩/莫丽均为药剂师）
--   核医学科 → 12 检查技师（同科裘天为检查技师）
-- 保留角色 6：康复医学科杨天/袁岚（康复治疗师，暂无治疗师角色）、病理科马海艳（病理技师，暂无角色）；
--             后续若新建「康复治疗师 / 病理技师」专岗再单独迁移。

-- 1. 设备科：3 人 → 39 设备与供应室管理员
UPDATE sys_employee_post SET role_id = 39, update_by = 'sql391', update_time = NOW()
WHERE employee_id = 897001447 AND dept_id = 19580082 AND role_id = 6;
UPDATE sys_employee_post SET role_id = 39, update_by = 'sql391', update_time = NOW()
WHERE employee_id = 897001443 AND dept_id = 19580082 AND role_id = 6;
UPDATE sys_employee_post SET role_id = 39, update_by = 'sql391', update_time = NOW()
WHERE employee_id = 897001448 AND dept_id = 19580082 AND role_id = 6;

-- 2. 输血科：2 人 → 11 检验技师
UPDATE sys_employee_post SET role_id = 11, update_by = 'sql391', update_time = NOW()
WHERE employee_id = 897001355 AND dept_id = 19580040 AND role_id = 6;
UPDATE sys_employee_post SET role_id = 11, update_by = 'sql391', update_time = NOW()
WHERE employee_id = 897001329 AND dept_id = 19580040 AND role_id = 6;

-- 3. 静脉配液中心：1 人 → 5 药剂师
UPDATE sys_employee_post SET role_id = 5, update_by = 'sql391', update_time = NOW()
WHERE employee_id = 897001393 AND dept_id = 19580046 AND role_id = 6;

-- 4. 核医学科：1 人 → 12 检查技师
UPDATE sys_employee_post SET role_id = 12, update_by = 'sql391', update_time = NOW()
WHERE employee_id = 897001364 AND dept_id = 19580092 AND role_id = 6;

-- 回滚（按序执行，恢复到角色 6）：
-- UPDATE sys_employee_post SET role_id = 6 WHERE employee_id IN (897001447, 897001443, 897001448) AND dept_id = 19580082;
-- UPDATE sys_employee_post SET role_id = 6 WHERE employee_id IN (897001355, 897001329) AND dept_id = 19580040;
-- UPDATE sys_employee_post SET role_id = 6 WHERE employee_id = 897001393 AND dept_id = 19580046;
-- UPDATE sys_employee_post SET role_id = 6 WHERE employee_id = 897001364 AND dept_id = 19580092;

-- 回查：
-- SELECT CAST(p.employee_id AS CHAR) eid, e.emp_name, p.role_id, d.dept_name
-- FROM sys_employee_post p LEFT JOIN sys_employee e ON e.id = p.employee_id
-- LEFT JOIN sys_department d ON d.id = p.dept_id
-- WHERE p.role_id = 6 ORDER BY d.dept_name, e.emp_name;
