/**
 * 药品特殊管理分类（麻精毒放）—— 全前端唯一口径来源
 *
 * 口径判定依据（实测 2026-09-23）：
 *   1) 列注释：`sys_drug.special_flag` tinyint NOT NULL DEFAULT 0
 *      「特殊管理分类（0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品），
 *        字典 his_drug_special_flag」
 *   2) 字典表：`sys_dict_data.dict_type='his_drug_special_flag'`（G10 新建，5 档）
 *   3) 法条：限量档位与双人复核范围见 `his-emr NarcoticControlServiceImpl` 的类注释
 *
 * ★ 本文件**只管语义**（哪些档位算管制、哪些要双人复核、哪些要回收空安瓿、标签什么颜色）；
 *   **文案一律从字典取**（`specialFlagText(flag, dictList)` → `dictLabelText`）。
 *   原因：管制目录是动态的 —— 2024-07-01 起咪达唑仑原料药与注射剂由第二类升为第一类精神药品。
 *   把文案写死在前端，意味着每次目录调整都要重新发版前端，而药房改一条字典记录就能解决。
 *   这也正是 `special_flag` 落成数据而不是 Java/JS 枚举的理由。
 *
 * ⚠ 未知码值由 `dictLabelText` 渲染成「未知(n)」，**不回落成合法值** ——
 *   回落会把"分类没维护"洗成"普通药品"，而普通药品是不受任何麻精管制的。
 */

/** 分类码值 */
export const SPECIAL_FLAG = {
  NORMAL: 0,
  NARCOTIC: 1,        // 麻醉药品
  PSYCHOTROPIC_1: 2,  // 第一类精神药品
  PSYCHOTROPIC_2: 3,  // 第二类精神药品
  TOXIC: 4,           // 毒性药品
}

/**
 * 是否属管制品种（需要专册登记）
 * @param {number|string|null|undefined} flag
 */
export function isControlledFlag(flag) {
  if (flag === null || flag === undefined || flag === '') return false
  return Number(flag) !== SPECIAL_FLAG.NORMAL
}

/**
 * 是否必须双人复核
 *
 * 两条法条各管一段，缺一不可：
 *   · 麻醉药品、第一类精神药品 ——《医疗机构麻醉药品、第一类精神药品管理规定》第 17 条
 *   · 毒性药品 ——《医疗用毒性药品管理办法》第 9 条第 2 款
 *     （「由配方人员及具有药师以上技术职称的复核人员签名盖章后方可发出」）
 *
 * **二类精神药品不在此列** —— 地西泮片这类日常量大，一并要求双人复核会把药房堵死且于法无据。
 * 这条判定必须和后端 `requiresDualCheck` 完全一致，否则前端会误以为"二类也要选复核人"。
 */
export function requiresDualCheck(flag) {
  const n = Number(flag)
  return n === SPECIAL_FLAG.NARCOTIC
    || n === SPECIAL_FLAG.PSYCHOTROPIC_1
    || n === SPECIAL_FLAG.TOXIC
}

/**
 * 剂型是否注射剂（限量档位与空安瓿回收都按它分档）
 */
export function isInjection(dosageForm) {
  const s = String(dosageForm || '')
  return s.includes('注射') || s.includes('大输液') || s.includes('输液')
}

/**
 * 剂型是否控缓释制剂（限量 7 日档；肠溶制剂**不算**）
 */
export function isControlledRelease(dosageForm) {
  const s = String(dosageForm || '')
  return s.includes('缓释') || s.includes('控释')
}

/**
 * 是否须登记空安瓿回收：**仅**麻醉药品 / 第一类精神药品的注射剂。
 *
 * ⚠ 不要写成 `requiresDualCheck(flag) && isInjection(form)` —— 毒性药品也走双人复核，
 * 但法规没有「回收空安瓿」这条要求，顺带带上的话专册里会永远挂着回收不了的空安瓿。
 */
export function requiresAmpouleTracking(flag, dosageForm) {
  const n = Number(flag)
  return (n === SPECIAL_FLAG.NARCOTIC || n === SPECIAL_FLAG.PSYCHOTROPIC_1) && isInjection(dosageForm)
}

/**
 * 标签颜色：管制品种一律醒目（danger），普通药品不标
 * @returns {''|'info'|'warning'|'danger'}
 */
export function specialFlagTagType(flag) {
  const n = Number(flag)
  if (n === SPECIAL_FLAG.NARCOTIC) return 'danger'
  if (n === SPECIAL_FLAG.PSYCHOTROPIC_1) return 'warning'
  if (n === SPECIAL_FLAG.PSYCHOTROPIC_2) return 'warning'
  if (n === SPECIAL_FLAG.TOXIC) return 'danger'
  return ''
}

/**
 * 分类码值 → 文案（唯一来源：字典 his_drug_special_flag）
 *
 * @param {number|string|null|undefined} flag
 * @param {Array} dictList `loadDictDataList(DICT_TYPE.DRUG_SPECIAL_FLAG)` 的结果
 * @returns {string} 命中字典取 dictLabel；未命中渲染「未知(n)」；空值渲染「—」
 */
export function specialFlagText(flag, dictList) {
  if (flag === null || flag === undefined || flag === '') return '—'
  const list = Array.isArray(dictList) ? dictList : []
  const hit = list.find(o => String(o.dictValue) === String(flag))
  if (!hit) return `未知(${flag})`
  return hit.dictLabel || `未知(${flag})`
}

/**
 * 空安瓿回收状态 —— 无字典枚举，口径按 `biz_narcotic_register.ampoule_status` 列注释单点定义
 * （0-不适用 1-待回收 2-已回收）。库中没有对应 dict_type，所以按项目约定落在 lib 单一文件里，
 * 页面禁止再写一份映射。
 */
export const AMPOULE_STATUS_OPTIONS = [
  { label: '不适用', value: 0 },
  { label: '待回收', value: 1 },
  { label: '已回收', value: 2 },
]

/**
 * 空安瓿回收状态 → 文案（命中不了渲染「未知(n)」，不回落成"不适用"）
 */
export function ampouleStatusText(status) {
  if (status === null || status === undefined || status === '') return '—'
  const hit = AMPOULE_STATUS_OPTIONS.find(o => o.value === Number(status))
  return hit ? hit.label : `未知(${status})`
}

/** 空安瓿回收状态 → 标签色 */
export function ampouleStatusTagType(status) {
  const n = Number(status)
  if (n === 1) return 'danger'
  if (n === 2) return 'success'
  return 'info'
}

/**
 * 限量档位文案（给发药窗口提示"这个药最多能开几天"）
 *
 * ⚠ 只用于**提示**展示。真正的判定在后端 `NarcoticControlService.checkPrescription`，
 * 前端这份是同一口径的镜像 —— 两处不一致时以后端为准（前端算错只是提示不准，
 * 后端算错才是合规事故）。改动此函数务必同步后端。
 *
 * @returns {string} 如「麻醉药品·注射剂限 1 日常用量」；普通药品返回 ''
 */
export function limitHint(flag, dosageForm) {
  const n = Number(flag)
  if (!isControlledFlag(n)) return ''
  if (n === SPECIAL_FLAG.TOXIC) return '毒性药品·每次处方不超过二日极量'
  if (n === SPECIAL_FLAG.PSYCHOTROPIC_2) return '第二类精神药品·每张处方限 7 日常用量'
  // 麻醉药品 / 第一类精神药品
  if (isInjection(dosageForm)) return '麻醉及第一类精神药品·注射剂限 1 日常用量'
  if (isControlledRelease(dosageForm)) return '麻醉及第一类精神药品·控缓释制剂限 7 日常用量'
  return '麻醉及第一类精神药品·其他剂型限 3 日常用量'
}
