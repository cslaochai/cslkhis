// 文案唯一来源仍是后端 PublishStatusEnum（出参 publishStatusText）与 NurseScheduleFilingStatusEnum
// （出参 filingStatusText），这里只补「能不能走这一步」的判定，码值常量与 isPublishEffective 一律
// 从 publishStatus.js / 本文件取。
import {PUBLISH_DRAFT, PUBLISH_PENDING, isPublishEffective} from './publishStatus'
import {getWardSelectList} from '@/api/system'
import {loadDeptTreeOptions, flattenDeptTree} from './deptOptions'
import {getNurseUnitNurseCounts} from '@/api/nurseSchedule'

/** 护理排班单元类型：1-病区 2-门诊科室（与后端 biz_schedule.org_type 口径对齐） */
export const NURSE_UNIT_WARD = 1
export const NURSE_UNIT_DEPT = 2

/**
 * 护理排班候选单元：病区走病区宿主下拉、科室走科室宿主 selectTree，护理在册人数由业务接口叠加。
 *
 * <p>候选清单归属各自宿主模块，护理模块只补「在册 N 人」统计后缀；已设病区的科室由该病区代表，
 * 不再重复作为科室单元。返回结构与旧混合接口一致：
 * {@code {unitType, wardId, wardName, deptId, deptName, nurseCount}}。
 */
export async function loadNurseUnitOptions() {
  const [wardRes, deptTree, countRes] = await Promise.all([
    getWardSelectList().catch(() => ({data: []})),
    loadDeptTreeOptions({mode: 'LEAF'}).catch(() => []),
    getNurseUnitNurseCounts().catch(() => ({data: []})),
  ])
  const countMap = new Map()
  ;(countRes?.data || []).forEach((c) => countMap.set(String(c.deptId), Number(c.nurseCount) || 0))

  const wardList = (wardRes?.data || []).map((w) => ({
    unitType: NURSE_UNIT_WARD,
    wardId: w.wardId,
    wardName: w.wardName,
    deptId: w.deptId,
    deptName: w.deptName,
    nurseCount: countMap.get(String(w.deptId)) || 0,
  }))
  const wardDeptIds = new Set(wardList.map((w) => String(w.deptId)))
  const deptList = flattenDeptTree(deptTree)
    .filter((d) => !wardDeptIds.has(String(d.id)))
    .map((d) => ({
      unitType: NURSE_UNIT_DEPT,
      wardId: d.id,
      wardName: d.fullPath || d.deptName,
      deptId: d.id,
      deptName: d.deptName,
      nurseCount: countMap.get(String(d.id)) || 0,
    }))
  return [...wardList, ...deptList]
}

/**
 * 护理排班（菜单 32x）按钮权限码与发布态流转判定 —— 页面与其拆出的弹框组件共用同一份，
 * 避免权限串与「哪一步能点」的门槛在多份文件里漂移。
 */
export const PERM_ADD = 'nursing:schedule:add';
export const PERM_EDIT = 'nursing:schedule:edit';
export const PERM_DELETE = 'nursing:schedule:delete';
/** 审核发布（G-15）：独立于排班权限，排班人不能自己把自己排的班发出去 */
export const PERM_AUDIT = 'nursing:schedule:audit';
/** 整周备案（护理部）：片区审核发布后由护理部留痕，不阻塞在岗生效 */
export const PERM_FILE = 'nursing:schedule:file';

/**
 * 发布态流转的可操作性判定（G-15）—— 矩阵格弹框与台账表格共用这一份，
 * 两边各写一套 if 一定会漂成「台账能点、弹框不能点」。
 * 一律要求有 id：没落库的新格子没有任何态可流转。
 * @param {{id?: string, publishStatus?: number}} row 矩阵 Cell 或台账 Row
 */
export const canSubmitPublish = (row) => !!row?.id && Number(row.publishStatus) === PUBLISH_DRAFT;
export const canAuditPublish = (row) => !!row?.id && Number(row.publishStatus) === PUBLISH_PENDING;
export const canAdjustPublish = (row) => !!row?.id && isPublishEffective(Number(row.publishStatus));
/** 撤回：任意态都能回草稿（含草稿本身，与后端 recall 的状态机同口径） */
export const canRecallPublish = (row) => !!row?.id;

/**
 * 周备案态（护理部）—— 与行发布态正交：审核发布即计在岗，备案只是护理部的留痕与抽查退回。
 * 码值取后端 NurseScheduleFilingStatusEnum，文案一律吃出参 filingStatusText，前端不再抄第二份。
 */
export const FILE_PENDING = 0;
export const FILE_DONE = 1;
export const FILE_REJECTED = 2;

/** 本周是否已进入备案环节：filingStatus 为 null 表示该周还没审核发布，压根没有备案行 */
export const hasFilingRow = (filingStatus) => filingStatus !== null && filingStatus !== undefined && filingStatus !== '';

/** 整周备案：只有「待备案」的周能备（已备案不重复留痕，已退回要先由护士长重排并重新审核发布） */
export const canFileWeek = (filingStatus) => hasFilingRow(filingStatus) && Number(filingStatus) === FILE_PENDING;
/** 整周退回：待备案/已备案都能退（护理部事后抽查发现要改的周，已备案同样能打回） */
export const canRejectFileWeek = (filingStatus) =>
    hasFilingRow(filingStatus) && (Number(filingStatus) === FILE_PENDING || Number(filingStatus) === FILE_DONE);
/** 已退回：矩阵要出醒目提示（本周已回草稿、不再计在岗） */
export const isWeekFileRejected = (filingStatus) =>
    hasFilingRow(filingStatus) && Number(filingStatus) === FILE_REJECTED;

/** 备案态标签配色：只按码值上色，文案仍由后端 filingStatusText 提供 */
const FILE_TAG_TYPES = {[FILE_PENDING]: 'warning', [FILE_DONE]: 'success', [FILE_REJECTED]: 'danger'};
export const filingStatusTagType = (filingStatus) => (hasFilingRow(filingStatus) ? FILE_TAG_TYPES[Number(filingStatus)] || 'info' : 'info');
