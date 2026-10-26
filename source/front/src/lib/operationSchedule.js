/**
 * 手术排台「选人候选」的角色编码常量。
 *
 * 与 `docs/03-增量SQL变更/298-手术麻醉排台岗位角色.sql` 一一对应，改角色编码必须两边同步：
 *  10041 麻醉医师   —— 排台弹框「麻醉医师」下拉的候选口径（原来出全院 535 人）
 *  10042 手术室护士 —— 排台弹框「巡回护士」「器械/洗手护士」下拉的候选口径
 *
 * 传的是 role_code 而不是角色自增 id：id 随库重建会变，role_code 是人定的稳定标识。
 * 后端口径见 `SysEmployeeServiceImpl.applyRoleFilter`（走 sys_employee_post 岗位行 + 岗位有效期）。
 */
export const OPERATION_ANESTHETIST_ROLE_CODE = '10041'

export const OPERATION_THEATRE_NURSE_ROLE_CODE = '10042'

/**
 * 主刀医师候选的岗位类别：1-医生。
 * 主刀不限定具体科室（外科各科主任都能上刀），只排除护理/医技/药学/收费/行政。
 */
export const OPERATION_SURGEON_STAFF_TYPE = 1
