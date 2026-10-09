-- =============================================================================
-- 210-门诊护理人力标准铺底（G-13）
--
-- 【问题】sql/204 把人力标准铺到 61 条之后，按「单元 × 岗位类别」拆开看是这样的：
--     · 医生岗（org_type=1 科室 × staff_type=1）        40 条
--     · 病区护理（org_type=2 病区 × staff_type=2）      18 条
--     · **门诊护理（org_type=1 科室 × staff_type=2）     1 条**
--   也就是说：医生开诊有下限守着，病区护理有下限守着，
--   **门诊护理几乎没人管** —— 把某科室唯一的门诊护士删掉，系统一声不吭。
--
-- 【铺什么】给「有护理编制的门诊科室」铺单元级（shift_id=0）兜底标准：
--   · min_staff = 1：开诊日至少得有一名护士在岗（分诊/跟诊/治疗总要有人）
--   · max_staff = 0（不限）：上限只做提示不做拦截，与现有 61 条同口径
--   · shift_id = 0（单元级）：matchRule 的匹配顺序是先找班次级再回落单元级，
--     单元级兜底确保「这个组合至少有人管」，班次级的细分交给护理部后续配
--
-- 【为什么是 1 而不是按诊室数算】
--   按诊室数 × 午别算配比是护理部要定的业务口径（蓝图 §9 待决策），
--   在没有核定值之前，凭空填 3 或 5 等于替他们拍板 —— 那是编数据不是治理。
--   先给一条**常识底线**（开诊就得有护士），把闸门装上；
--   等需求层（P2）能把「今天这个门诊要几个人」算出来，再用真实需求校准这些数。
--
-- 【刻意不做】
--   · 不铺没有护理编制的门诊科室：没人可排的标准只会天天告警。
--   · 不铺有病区的科室：那些科室的护理走病区那一侧（org_type=2），重复铺会两边都管又都管不全。
--   · 不动已有的那 1 条：它可能是有人手工配的，只补缺口。
-- =============================================================================

SET @rn := (SELECT COALESCE(MAX(id), 896750000000200000) FROM biz_staff_plan_rule WHERE id < 896750000000300000);

INSERT INTO biz_staff_plan_rule (id, org_type, org_id, org_name, shift_id, staff_type,
                                 min_staff, max_staff, max_week_hours,
                                 max_consecutive_night_days, max_consecutive_work_days,
                                 status, create_time, update_time, del_flag, remark)
SELECT
  @rn := @rn + 1,
  1, t.id, t.dept_name, 0, 2,
  1, 0, 48.0, 2, 6,
  1, NOW(), NOW(), 0,
  CONCAT('sql/210 门诊护理兜底（开诊日至少 1 名护士在岗）；该科室护理编制 ',
         (SELECT COUNT(*) FROM sys_employee e JOIN sys_employee_post ep ON ep.employee_id = e.id
            JOIN sys_role r ON r.id = ep.role_id
           WHERE e.del_flag = 0 AND e.status = 1 AND r.staff_type = 2 AND e.dept_id = t.id),
         ' 人，具体配比待护理部核定')
FROM sys_department t
WHERE t.del_flag = 0 AND t.status = 1
  AND NOT EXISTS (SELECT 1 FROM sys_ward w WHERE w.status = 1 AND w.dept_id = t.id)
  AND EXISTS (SELECT 1 FROM sys_employee e JOIN sys_employee_post ep ON ep.employee_id = e.id
                JOIN sys_role r ON r.id = ep.role_id
               WHERE e.del_flag = 0 AND e.status = 1 AND r.staff_type = 2 AND e.dept_id = t.id)
  AND NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule r
                   WHERE r.del_flag = 0 AND r.org_type = 1 AND r.org_id = t.id
                     AND r.staff_type = 2 AND r.shift_id = 0);

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/210-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 有护理编制的门诊科室都有标准' AS item, COUNT(*) AS cnt, '0' AS expect FROM (SELECT DISTINCT e.dept_id FROM sys_employee e JOIN sys_employee_post ep ON ep.employee_id = e.id JOIN sys_role r ON r.id = ep.role_id WHERE e.del_flag = 0 AND e.status = 1 AND r.staff_type = 2 AND e.dept_id NOT IN (SELECT w.dept_id FROM sys_ward w WHERE w.status = 1)) x WHERE NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule n WHERE n.del_flag = 0 AND n.org_type = 1 AND n.org_id = x.dept_id AND n.staff_type = 2)
UNION ALL SELECT 'V2 门诊护理标准都有下限', COUNT(*), '0' FROM biz_staff_plan_rule WHERE del_flag = 0 AND status = 1 AND org_type = 1 AND staff_type = 2 AND (min_staff IS NULL OR min_staff < 1)
UNION ALL SELECT 'V3 标准挂在真实存在的科室上', COUNT(*), '0' FROM biz_staff_plan_rule r WHERE r.del_flag = 0 AND r.org_type = 1 AND r.staff_type = 2 AND NOT EXISTS (SELECT 1 FROM sys_department t WHERE t.id = r.org_id AND t.del_flag = 0)
UNION ALL SELECT 'V4 A 门诊护理标准条数', COUNT(*), '>=18' FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 1 AND staff_type = 2
UNION ALL SELECT 'V5 A 标准总条数（留痕）', COUNT(*), '>=79' FROM biz_staff_plan_rule WHERE del_flag = 0;
