/**
 * 护理评估量表口径（G14 + sql/159 专项评估）—— 页面**唯一**从这里取量表定义与风险等级文案，
 * 禁止在页面里写映射副本。
 *
 * ⚠ 风险等级的**落库值由后端按同一分数段算**（InpatientNursingServiceImpl.riskLevelOf），
 * 本文件的分段只是渲染用；若两边不一致，以后端为准。
 *
 * 量表模式（mode）：
 * - 'radio'（默认）：items[].options 单选，每项计一次分 —— Braden / Morse / 管路风险项。
 * - 'nrs'：疼痛专项，0~10 大按钮 + 部位/性质/措施（非计分元数据进 itemsJson 的 value 项）。
 * - 'check'：Caprini 危险因素勾选累加（groups[].items 独立勾选，勾中计 items[].score）。
 * - 'tube'：管路滑脱 = 风险项 radio 计分 + 当前留置管路清单勾选（清单不计分，value 项透视用）。
 */

/** 评估类型（字典 his_assess_type） */
export const ASSESS_TYPE = {
  PRESSURE: 1,
  FALL: 2,
  PAIN: 3,
  VTE: 4,
  TUBE: 5,
}

export const ASSESS_TYPE_TEXT = {
  1: '压疮评估（Braden）',
  2: '跌倒评估（Morse）',
  3: '疼痛评估（NRS）',
  4: 'VTE血栓评估（Caprini）',
  5: '管路滑脱评估',
}

/** 风险等级（字典 his_assess_risk_level）：1-低 2-中 3-高 4-极高 */
export const RISK_LEVEL_TEXT = {
  1: '低风险',
  2: '中风险',
  3: '高风险',
  4: '极高风险',
}

/** 风险等级 → tag 色（低=info、中=warning、高=danger、极高=danger 加粗用深度色） */
export const RISK_LEVEL_TAG = {
  1: 'info',
  2: 'warning',
  3: 'danger',
  4: 'danger',
}

/**
 * 量表定义。计分项 score 是该档得分（后端按 score 求和复算总分）。
 * 非计分项以 { key, label, value } 混排进 itemsJson（后端 sumItems 跳过无 score 的项）。
 */
export const SCALES = {
  1: {
    name: 'Braden 压疮风险评估',
    hint: '总分 6~23，分数越低压疮风险越高',
    min: 6,
    max: 23,
    mode: 'radio',
    items: [
      { key: 'perception', label: '感知（对压力相关不适的感受能力）', options: [{ label: '完全受损', score: 1 }, { label: '重度受损', score: 2 }, { label: '轻度受损', score: 3 }, { label: '无受损', score: 4 }] },
      { key: 'moisture', label: '潮湿（皮肤暴露于潮湿的程度）', options: [{ label: '持续潮湿', score: 1 }, { label: '经常潮湿', score: 2 }, { label: '偶尔潮湿', score: 3 }, { label: '很少潮湿', score: 4 }] },
      { key: 'activity', label: '活动能力（身体活动程度）', options: [{ label: '卧床', score: 1 }, { label: '限于轮椅', score: 2 }, { label: '偶尔行走', score: 3 }, { label: '经常行走', score: 4 }] },
      { key: 'mobility', label: '移动能力（改变/控制体位的能力）', options: [{ label: '完全受限', score: 1 }, { label: '重度受限', score: 2 }, { label: '轻度受限', score: 3 }, { label: '不受限', score: 4 }] },
      { key: 'nutrition', label: '营养（通常的进食状况）', options: [{ label: '重度缺乏', score: 1 }, { label: '可能不足', score: 2 }, { label: '充足', score: 3 }, { label: '丰富', score: 4 }] },
      { key: 'friction', label: '摩擦力与剪切力', options: [{ label: '已成为问题', score: 1 }, { label: '有潜在问题', score: 2 }, { label: '无明显问题', score: 3 }] },
    ],
  },
  2: {
    name: 'Morse 跌倒风险评估',
    hint: '总分 0~125；≥45 为高风险（无极高档）',
    min: 0,
    max: 125,
    mode: 'radio',
    items: [
      { key: 'fallHistory', label: '跌倒史（3 个月内）', options: [{ label: '无', score: 0 }, { label: '有', score: 25 }] },
      { key: 'diagnosis', label: '超过 1 个医学诊断', options: [{ label: '否', score: 0 }, { label: '是', score: 15 }] },
      { key: 'ambulatoryAid', label: '行走辅助', options: [{ label: '不需要/卧床/轮椅', score: 0 }, { label: '拐杖手杖助行器', score: 15 }, { label: '扶靠家具行走', score: 30 }] },
      { key: 'ivTherapy', label: '静脉输液/使用药物', options: [{ label: '否', score: 0 }, { label: '是', score: 20 }] },
      { key: 'gait', label: '步态', options: [{ label: '正常/卧床/轮椅', score: 0 }, { label: '虚弱', score: 10 }, { label: '受损', score: 20 }] },
      { key: 'cognition', label: '认知状态', options: [{ label: '清醒', score: 0 }, { label: '意识模糊', score: 15 }] },
    ],
  },
  3: {
    name: 'NRS 疼痛数字评分',
    hint: '0 分无疼痛，10 分最剧烈；1~3 轻度 / 4~6 中度 / 7~10 重度',
    min: 0,
    max: 10,
    mode: 'nrs',
    items: [
      { key: 'pain', label: '疼痛强度（请患者自评 0~10）', options: Array.from({ length: 11 }, (_, i) => ({ label: String(i), score: i })) },
    ],
    /** 疼痛性质候选（value 项，不计分） */
    natures: ['胀痛', '刺痛', '灼痛', '绞痛', '酸痛', '钝痛', '刀割样', '其他'],
  },
  4: {
    name: 'Caprini 血栓风险评估',
    hint: '勾选患者存在的危险因素，得分累加：0~2 低 / 3~4 中 / 5~6 高 / ≥7 极高（年龄三档临床互斥）',
    min: 0,
    max: 58,
    mode: 'check',
    groups: [
      {
        label: '基本因素',
        items: [
          { key: 'age41', label: '年龄 41~60 岁', score: 1 },
          { key: 'age61', label: '年龄 61~74 岁', score: 2 },
          { key: 'age75', label: '年龄 ≥ 75 岁', score: 5 },
          { key: 'obesity', label: '肥胖（BMI ≥ 25）', score: 1 },
        ],
      },
      {
        label: '手术 / 创伤（1 个月内）',
        items: [
          { key: 'minorSurgery', label: '小手术（预计 < 45min）', score: 1 },
          { key: 'majorSurgery', label: '大手术（预计 > 45min）', score: 5 },
          { key: 'arthroplasty', label: '髋 / 膝关节置换术', score: 5 },
          { key: 'fracture', label: '髋 / 骨盆 / 下肢骨折', score: 5 },
          { key: 'multipleTrauma', label: '多发创伤', score: 5 },
          { key: 'cast', label: '下肢石膏固定', score: 2 },
        ],
      },
      {
        label: '内科因素',
        items: [
          { key: 'sepsis', label: '脓毒症', score: 1 },
          { key: 'ibd', label: '炎症性肠病', score: 1 },
          { key: 'mi', label: '心肌梗死', score: 1 },
          { key: 'chf', label: '充血性心力衰竭', score: 1 },
          { key: 'stroke', label: '脑卒中', score: 5 },
          { key: 'malignancy', label: '恶性肿瘤（现存或既往）', score: 2 },
          { key: 'bedridden', label: '需卧床 > 72 小时', score: 2 },
          { key: 'cvc', label: '中心静脉置管', score: 2 },
        ],
      },
      {
        label: '血栓相关',
        items: [
          { key: 'vteHistory', label: 'VTE 病史', score: 5 },
          { key: 'familyHistory', label: 'VTE 家族史', score: 3 },
          { key: 'thrombophilia', label: '血栓形成倾向（实验室指标异常）', score: 3 },
        ],
      },
    ],
  },
  5: {
    name: '管路滑脱（非计划拔管）风险评估',
    hint: '风险项单选计分 + 勾选当前留置管路（清单不计分，作管路透视）；0~3 低 / 4~7 中 / 8~11 高 / ≥12 极高',
    min: 0,
    max: 24,
    mode: 'tube',
    items: [
      { key: 'tubeCount', label: '留置管路数量', options: [{ label: '无管路', score: 0 }, { label: '1 条', score: 1 }, { label: '2~3 条', score: 3 }, { label: '≥4 条', score: 5 }] },
      { key: 'highRisk', label: '高危管路（气管插管/气切/深静脉/≥2 条引流管）', options: [{ label: '无', score: 0 }, { label: '1 条', score: 2 }, { label: '≥2 条', score: 4 }] },
      { key: 'consciousness', label: '意识状态', options: [{ label: '清醒', score: 0 }, { label: '嗜睡', score: 1 }, { label: '昏迷', score: 2 }, { label: '模糊或躁动', score: 3 }] },
      { key: 'behavior', label: '精神行为', options: [{ label: '正常', score: 0 }, { label: '焦虑不安 / 试图拔管', score: 4 }] },
      { key: 'cooperation', label: '配合程度', options: [{ label: '完全配合', score: 0 }, { label: '部分配合', score: 1 }, { label: '不配合', score: 3 }] },
      { key: 'mobility', label: '活动能力', options: [{ label: '制动 / 活动受限', score: 0 }, { label: '可自主活动', score: 2 }] },
      { key: 'extubationHistory', label: '既往非计划拔管史', options: [{ label: '无', score: 0 }, { label: '有', score: 3 }] },
    ],
    /** 当前留置管路清单候选（勾选后以 value 项进 itemsJson，不计分） */
    tubes: [
      { key: 'ett', label: '气管插管 / 气管切开' },
      { key: 'cvc', label: '中心静脉导管（CVC/PICC）' },
      { key: 'ngt', label: '留置胃管' },
      { key: 'urine', label: '留置尿管' },
      { key: 'drain', label: '引流管（胸腔/腹腔/伤口等）' },
      { key: 'oxygen', label: '氧气管' },
      { key: 'peripheralIv', label: '外周静脉通路' },
      { key: 'other', label: '其他管路' },
    ],
  },
}

/** 前端渲染用的分段（与后端 riskLevelOf 一致） */
export function riskLevelOf(assessType, totalScore) {
  if (assessType === 1) return totalScore <= 9 ? 4 : totalScore <= 12 ? 3 : totalScore <= 14 ? 2 : 1
  if (assessType === 2) return totalScore >= 45 ? 3 : totalScore >= 25 ? 2 : 1
  if (assessType === 3) return totalScore >= 7 ? 3 : totalScore >= 4 ? 2 : 1
  if (assessType === 4) return totalScore >= 7 ? 4 : totalScore >= 5 ? 3 : totalScore >= 3 ? 2 : 1
  if (assessType === 5) return totalScore >= 12 ? 4 : totalScore >= 8 ? 3 : totalScore >= 4 ? 2 : 1
  return 1
}

/** 专项评估 tab 的透视卡顺序与文案 */
export const SPECIAL_TYPES = [
  { type: 1, short: '压疮 Braden', desc: '6~23 分，越低越危险' },
  { type: 2, short: '跌倒 Morse', desc: '≥45 分高风险' },
  { type: 3, short: '疼痛 NRS', desc: '0~10 自评' },
  { type: 4, short: 'VTE Caprini', desc: '≥7 分极高危' },
  { type: 5, short: '管路滑脱', desc: '含留置清单' },
]
