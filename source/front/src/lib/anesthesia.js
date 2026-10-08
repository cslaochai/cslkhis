// 手术麻醉链码值文案单点
export const ALDRETE_DISCHARGE_MIN = 9

export const asaText = (v) => ({ 1: 'Ⅰ级', 2: 'Ⅱ级', 3: 'Ⅲ级', 4: 'Ⅳ级', 5: 'Ⅴ级' }[v] ?? '—')
export const asaOptions = [
  { value: 1, label: 'Ⅰ级' },
  { value: 2, label: 'Ⅱ级' },
  { value: 3, label: 'Ⅲ级' },
  { value: 4, label: 'Ⅳ级' },
  { value: 5, label: 'Ⅴ级' },
]

export const asaFullText = (grade, emergency) =>
  emergency === 1 ? `${asaText(grade)} E（急诊）` : asaText(grade)

export const mallampatiText = (v) =>
  ({
    1: 'Ⅰ级（可见软腭/悬雍垂）',
    2: 'Ⅱ级（可见软腭/咽峡弓）',
    3: 'Ⅲ级（仅见软腭）',
    4: 'Ⅳ级（仅见硬腭）',
  }[v] ?? '—')
export const mallampatiOptions = [
  { value: 1, label: 'Ⅰ级' },
  { value: 2, label: 'Ⅱ级' },
  { value: 3, label: 'Ⅲ级' },
  { value: 4, label: 'Ⅳ级' },
]

export const neckMobilityText = (v) => ({ 1: '正常', 2: '受限', 3: '强直' }[v] ?? '—')
export const neckMobilityOptions = [
  { value: 1, label: '正常' },
  { value: 2, label: '受限' },
  { value: 3, label: '强直' },
]

export const npoText = (v) =>
  ({ 0: '未禁食', 1: '已按要求禁食', 2: '急诊饱胃（返流误吸高危）' }[v] ?? '—')
export const npoOptions = [
  { value: 0, label: '未禁食' },
  { value: 1, label: '已按要求禁食' },
  { value: 2, label: '急诊饱胃' },
]

export const visitConclusionText = (v) =>
  ({ 1: '可施行麻醉', 2: '暂缓手术', 3: '需会诊/进一步评估' }[v] ?? '—')
export const visitConclusionOptions = [
  { value: 1, label: '可施行麻醉' },
  { value: 2, label: '暂缓手术' },
  { value: 3, label: '需会诊/进一步评估' },
]

export const recordStatusText = (v) => ({ 0: '记录中', 1: '已提交', 2: '已审核' }[v] ?? '—')
export const recordStatusOptions = [
  { value: 0, label: '记录中' },
  { value: 1, label: '已提交' },
  { value: 2, label: '已审核' },
]

export const airwayDeviceText = (v) =>
  ({ 0: '无（保留自主呼吸）', 1: '气管插管', 2: '喉罩', 3: '面罩', 4: '其他' }[v] ?? '—')
export const airwayDeviceOptions = [
  { value: 0, label: '无（保留自主呼吸）' },
  { value: 1, label: '气管插管' },
  { value: 2, label: '喉罩' },
  { value: 3, label: '面罩' },
  { value: 4, label: '其他' },
]

export const ventilationText = (v) => ({ 1: '自主呼吸', 2: '辅助通气', 3: '控制通气' }[v] ?? '—')

export const effectText = (v) => ({ 1: '满意', 2: '欠佳', 3: '失败改麻醉方式' }[v] ?? '—')
export const effectOptions = [
  { value: 1, label: '满意' },
  { value: 2, label: '欠佳' },
  { value: 3, label: '失败改麻醉方式' },
]

export const dispositionText = (v) => ({ 1: '回病房', 2: '入PACU', 3: '入ICU' }[v] ?? '—')
export const dispositionOptions = [
  { value: 1, label: '回病房' },
  { value: 2, label: '入PACU' },
  { value: 3, label: '入ICU' },
]

export const medPhaseText = (v) => ({ 1: '诱导', 2: '维持', 3: '苏醒' }[v] ?? '—')
export const medPhaseOptions = [
  { value: 1, label: '诱导' },
  { value: 2, label: '维持' },
  { value: 3, label: '苏醒' },
]

export const medRouteText = (v) =>
  ({
    1: '静脉推注',
    2: '静脉泵注',
    3: '静脉滴注',
    4: '吸入',
    5: '肌注',
    6: '椎管内',
    7: '局麻浸润',
    8: '其他',
  }[v] ?? '—')
export const medRouteOptions = [
  { value: 1, label: '静脉推注' },
  { value: 2, label: '静脉泵注' },
  { value: 3, label: '静脉滴注' },
  { value: 4, label: '吸入' },
  { value: 5, label: '肌注' },
  { value: 6, label: '椎管内' },
  { value: 7, label: '局麻浸润' },
  { value: 8, label: '其他' },
]

// ---------------- PACU ----------------

export const pacuStatusText = (v) => ({ 0: '在室观察', 1: '已出室' }[v] ?? '—')
export const pacuStatusOptions = [
  { value: 0, label: '在室观察' },
  { value: 1, label: '已出室' },
]

export const awarenessText = (v) => ({ 1: '完全清醒', 2: '嗜睡可唤醒', 3: '未清醒' }[v] ?? '—')
export const awarenessOptions = [
  { value: 1, label: '完全清醒' },
  { value: 2, label: '嗜睡可唤醒' },
  { value: 3, label: '未清醒' },
]

export const pacuDispositionText = (v) => ({ 1: '回病房', 2: '转ICU', 3: '继续留观' }[v] ?? '—')
export const pacuDispositionOptions = [
  { value: 1, label: '回病房' },
  { value: 2, label: '转ICU' },
  { value: 3, label: '继续留观' },
]

/** Aldrete 五项每项 0~2 分，逐项含义（评分弹窗的悬停说明） */
export const ALDRETE_ITEMS = [
  {
    key: 'scoreActivity',
    label: '肌力/活动',
    options: [
      { value: 0, label: '0 · 无肢体活动' },
      { value: 1, label: '1 · 两肢可动' },
      { value: 2, label: '2 · 四肢可动' },
    ],
  },
  {
    key: 'scoreRespiration',
    label: '呼吸',
    options: [
      { value: 0, label: '0 · 需辅助通气' },
      { value: 1, label: '1 · 呼吸浅/受限' },
      { value: 2, label: '2 · 深呼吸可咳嗽' },
    ],
  },
  {
    key: 'scoreCirculation',
    label: '血压',
    options: [
      { value: 0, label: '0 · 波动 ±50mmHg 以上' },
      { value: 1, label: '1 · 波动 ±20~50mmHg' },
      { value: 2, label: '2 · 波动 ±20mmHg 以内' },
    ],
  },
  {
    key: 'scoreConsciousness',
    label: '意识',
    options: [
      { value: 0, label: '0 · 无反应' },
      { value: 1, label: '1 · 可唤醒' },
      { value: 2, label: '2 · 完全清醒' },
    ],
  },
  {
    key: 'scoreSpo2',
    label: '氧合',
    options: [
      { value: 0, label: '0 · 吸氧下 < 90%' },
      { value: 1, label: '1 · 吸氧下 > 90%' },
      { value: 2, label: '2 · 空气下 > 92%' },
    ],
  },
]

// ---------------- 器械清点 ----------------

export const countPhaseText = (v) =>
  ({ 0: '未开始', 1: '术前清点完成', 2: '关体前清点完成', 3: '关体后清点完成' }[v] ?? '—')
export const countStatusText = (v) =>
  ({ 0: '清点中', 1: '三轮一致，清点完成', 2: '存在清点差异', 3: '异常终止' }[v] ?? '—')
export const countResultText = (v) => ({ 1: '一致', 2: '不一致' }[v] ?? '—')
export const countCategoryText = (v) =>
  ({ 1: '器械', 2: '敷料', 3: '缝针', 4: '刀片', 5: '其他' }[v] ?? '—')
export const countCategoryOptions = [
  { value: 1, label: '器械' },
  { value: 2, label: '敷料' },
  { value: 3, label: '缝针' },
  { value: 4, label: '刀片' },
  { value: 5, label: '其他' },
]

// ---------------- 麻醉术后随访（P134.3） ----------------
// 同一条铁律：码值没有建 sys_dict，后端 FollowupAdverseItems 与本文件各一份单点；
// 未知码一律渲染「—」/不回落。

export const followupRecoveryText = (v) => ({ 1: '良好', 2: '一般', 3: '差' }[v] ?? '—')
export const followupRecoveryOptions = [
  { value: 1, label: '良好' },
  { value: 2, label: '一般' },
  { value: 3, label: '差' },
]

export const followupStatusText = (v) => ({ 0: '草稿', 1: '已完成' }[v] ?? '—')

export const followupAdverseText = (v) =>
  ({
    1: '恶心呕吐',
    2: '咽痛',
    3: '尿潴留',
    4: '头痛',
    5: '头晕',
    6: '神经症状',
    7: '呼吸并发症',
    8: '低血压/心律失常',
    9: '其他',
  }[v] ?? '—')
export const followupAdverseOptions = [
  { value: 1, label: '恶心呕吐' },
  { value: 2, label: '咽痛' },
  { value: 3, label: '尿潴留' },
  { value: 4, label: '头痛' },
  { value: 5, label: '头晕' },
  { value: 6, label: '神经症状' },
  { value: 7, label: '呼吸并发症' },
  { value: 8, label: '低血压/心律失常' },
  { value: 9, label: '其他' },
]

/** 随访轮次文案与后端 FollowupAdverseItems.roundText 同口径 */
export const followupRoundText = (roundNo) => {
  if (roundNo === null || roundNo === undefined) return '—'
  if (roundNo === 1) return '第1轮·术后即刻'
  if (roundNo === 2) return '第2轮·术后24h'
  if (roundNo === 3) return '第3轮·术后48h'
  return `第${roundNo}轮·追加随访`
}

// ---------------- 计费 ----------------

export const chargeStatusText = (v) => ({ 0: '未计费', 1: '已计费', 2: '计费失败' }[v] ?? '—')
export const chargeSourceText = (v) => ({ 1: '麻醉记录', 2: 'PACU复苏', 3: '手术' }[v] ?? '—')

export const yesNoText = (v) => ({ 0: '否', 1: '是' }[v] ?? '—')

/** Aldrete 总分展示（服务端算好的为准，这里只在缺项时给出提示） */
export const aldreteHint = (total) => {
  if (total === null || total === undefined) return '尚未评分'
  const ok = total >= ALDRETE_DISCHARGE_MIN
  return `${total} 分 / 10 分 · ${ok ? '已达出室标准' : `未达出室标准（≥${ALDRETE_DISCHARGE_MIN}）`}`
}
