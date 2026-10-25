-- =====================================================================
-- 295 急诊科室类型字典化（消灭 StaffScheduleServiceImpl 里的急诊科室魔数 ID）
-- ---------------------------------------------------------------------
-- 背景：
--   急诊值班公开视图 / 值班资质校验 / 固定医师占比三处口径原先写死
--   19580047 急危重症中心 + 19580048 急诊内科 + 19580049 急诊外科 + 19580051 急诊儿科。
--   换库、科室改名、ID 重导都会让口径静默失效（查不到 ≠ 报错），
--   且科室档案上没有任何地方能表达「这是急诊科室」。
--
--   标准 HIS 的做法：急诊是科室的一个属性，不是代码里的常量。
--   本仓库 sys_department.dept_type 已是「逗号分隔多值 + 字典驱动」的成熟模式
--   （前端科室编辑页多选取字典 his_dept_type），顺势加一个类型值即可，
--   零表结构变更、前端零改动（字典接口直接多一个选项）。
--
-- 口径：
--   dept_type 新增 6=急诊科室；四个急诊单元回填为 '1,6'（保留原「门诊科室」类型）。
--   后端 StaffScheduleServiceImpl 不再持有任何科室 ID：
--     - 值班资质校验：dept_type 含 6 的全部科室（含虚父级，防漏校验）
--     - 值班公开表 / 固定医师占比：dept_type 含 6 的科室中「非虚父级」
--       （集合内被其他急诊科室指为 parent_id 的视为父级，不单列 —— 与原口径一致）
--   急危重症中心(19580047)一并勾选：它确实承担急诊大盘值班，资质校验要兜住它；
--   公开表/占比按父级规则自动排除它，无需再为它写特例代码。
--
-- 幂等：字典行按 (dict_type, dict_value, del_flag=0) 判重；科室回填带
--       NOT FIND_IN_SET('6', dept_type) 守卫，重复执行不重复追加。
-- =====================================================================

-- 1. 字典加「6-急诊科室」（前端科室编辑页多选直接出现，无需改前端）
INSERT INTO sys_dict_data
    (id, dict_type, dict_label, dict_value, dict_sort, is_default, status,
     create_by, create_by_id, update_by, update_by_id, del_flag, dict_source)
SELECT 8910000000000371022, 'his_dept_type', '急诊科室', '6', 6, 0, 1,
       'admin', 1, 'admin', 1, 0, 1
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data
                    WHERE dict_type = 'his_dept_type' AND dict_value = '6' AND del_flag = 0);

-- 2. 四个急诊单元回填 dept_type = '1,6'（含虚父级急危重症中心）
--    ICU(19580050) 不勾：它是住院重症单元，不属急诊值班口径（与原代码一致）
UPDATE sys_department
   SET dept_type = CONCAT(dept_type, ',6'),
       update_by = 'admin', update_time = NOW()
 WHERE del_flag = 0
   AND id IN (19580047, 19580048, 19580049, 19580051)
   AND NOT FIND_IN_SET('6', dept_type);

-- 回查：字典行 + 四科室类型（应为 '1,6'）
-- SELECT id, dict_label, dict_value FROM sys_dict_data WHERE dict_type = 'his_dept_type' ORDER BY dict_sort;
-- SELECT id, dept_name, dept_type, parent_id FROM sys_department WHERE id IN (19580047,19580048,19580049,19580051);
-- 后端判据自检：SELECT id, dept_name FROM sys_department WHERE del_flag = 0 AND FIND_IN_SET('6', dept_type) > 0;

-- ⚠️ 运维提示：本脚本直插 sys_dict_data，不走服务层，Redis 字典缓存
--    （键 sys:dict:his_dept_type，TTL 24h，DictCacheServiceImpl）不会自动失效。
--    执行后必须 DEL 该键（或重启后端），否则科室编辑页多选 24 小时内看不到「急诊科室」：
--      redis-cli -h 192.168.88.132 -a 123456 DEL sys:dict:his_dept_type
--    后端侧不受影响（FIND_IN_SET 直查库，不读字典缓存），仅前端选项延迟可见。
