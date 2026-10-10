-- 挂号员/收费员/前台导诊的数据范围改为「全部数据」
-- 为什么：这两个岗位天然是跨科室作业（挂任何门诊科室的号、结任何住院科室的账单）。
-- 原 data_scope=3（本部门）时，其所在科室（财务科/体检科/院办）不含任何门诊科室，
-- 于是门诊科室下拉恒为空、按别科室 deptId 查号源被数据范围闸门判「无权查看该科室的数据」，
-- 挂号链路一个账号也跑不通。数据范围随岗位收口的口径不变，只是把这两个「服务型岗位」的能力放开到全院。
-- 幂等：按 role_code 定位，重复执行结果一致。
UPDATE sys_role SET data_scope = 1, update_by = 'admin', update_by_id = 1, update_time = NOW()
WHERE role_code IN ('10015', '10018') AND del_flag = 0;

-- 回查：应全部为 1
SELECT role_code, role_name, data_scope FROM sys_role WHERE role_code IN ('10015', '10018') AND del_flag = 0;
