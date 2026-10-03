/**
 * 检查预约中心（G21）语义常量
 *
 * 分工与项目惯例一致：**码值文案在字典**（`his_exam_device_type` /
 * `his_exam_appoint_status` / `his_exam_device_status` / `his_exam_slot_status`，
 * 由后端 `SubDictText` 解析成 xxxText 随 VO 下发，前端下拉也从字典取），
 * 本文件只放「拿到码值之后怎么对待它」的判定 —— 哪些状态还能改约、
 * 哪种格子不能点、标签用什么颜色。这些判定和后端 Service 的守卫是同一套规则，
 * 改一处必须同步另一处，所以集中定义、不在页面里散写魔法数。
 */

// 预约单状态（biz_exam_appointment.status）
export const APPT_STATUS = {
  BOOKED: 1,    // 已预约 —— 唯一可改约/取消的档位
  ARRIVED: 2,   // 已到检 —— 只能走「完成检查」，取消要到检查工作站
  FINISHED: 3,  // 已完成 —— 终态，号源已释放
  CANCELLED: 4, // 已取消 —— 终态（改约产生的旧单也落在这里，靠 remark 区分）
  NOSHOW: 5,    // 爽约 —— 定时扫描判定，同样终态
}

// 设备开放状态（biz_exam_device.status）
export const DEVICE_STATUS = {
  OPEN: 1,    // 开放预约
  PAUSED: 2,  // 暂停预约：不能新建，历史占号不受影响
}

// 号源段状态（biz_exam_slot.status）
export const SLOT_STATUS = {
  LOCKED: 0,  // 锁号（设备维护/消毒/临时停用）
  NORMAL: 1,  // 正常
}

// 预约单状态 → el-tag 类型
export function apptStatusTag(status) {
  const n = Number(status)
  if (n === APPT_STATUS.BOOKED) return 'primary'
  if (n === APPT_STATUS.ARRIVED) return 'warning'
  if (n === APPT_STATUS.FINISHED) return 'success'
  return 'info' // 4 已取消 / 5 爽约 —— 都是没做成的检查，不标红以免和「异常」混淆
}

// 设备状态 → el-tag 类型
export function deviceStatusTag(status) {
  return Number(status) === DEVICE_STATUS.OPEN ? 'success' : 'warning'
}

/**
 * 号源格子的可操作判定（返回 {key,label,tag,clickable}）。
 * 判定顺序是有意为之：锁号要先于满号 —— 一格「锁了且计数为满」多半是对账漂移，
 * 显示成「已约满」会把人去导向「换个时段」，而真正该做的是对账。
 */
export function slotCellState(cell) {
  const used = Number(cell?.usedSource ?? 0)
  const total = Number(cell?.totalSource ?? 0)
  if (Number(cell?.status) === SLOT_STATUS.LOCKED) {
    return { key: 'locked', label: '锁号', tag: 'info', clickable: false }
  }
  if (used >= total) {
    return { key: 'full', label: `已约满 ${used}/${total}`, tag: 'danger', clickable: false }
  }
  if (cell?.past) {
    return { key: 'past', label: `已过时 ${used}/${total}`, tag: 'info', clickable: false }
  }
  return { key: 'free', label: `可约 ${used}/${total}`, tag: 'success', clickable: true }
}

// 该预约单还能不能改约/取消
export function isMutable(status) {
  return Number(status) === APPT_STATUS.BOOKED
}

// 该预约单是否需要到检签到（工作台默认只铺这两档）
export function isOpenAppt(status) {
  const n = Number(status)
  return n === APPT_STATUS.BOOKED || n === APPT_STATUS.ARRIVED
}
