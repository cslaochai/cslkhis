/**
 * 患者健康档案六组 —— 前端码值唯一口径。
 *
 * 为什么在这里而不是 sys_dict_data：这七个枚举（过敏类型 / 过敏严重程度 / 手术类型 /
 * 恢复情况 / 控制情况 / 药物类型 / 给药途径 / 用药状态）都是随明细一起录入的描述性分类，
 * 库里**没有**对应的字典表（实测 sys_dict_type 里查不到 drug_type、drug_route、
 * allergy_type 这类类型；`lib/dict-cache.js` 里曾挂过几个同名常量，指向的是不存在的字典，
 * 页面拿到空下拉且不报错 —— 已删）。本工程对无字典枚举的既定做法是：
 * **以建表列注释为口径，在一处声明，页面禁写第二套映射**。后端对应
 * `com.his.patient.support.HealthProfileEnums`，两边增删值必须同步。
 *
 * ⚠ 与后端唯一的差异：`ALLERGY_SEVERITY` 里多一个 `未评估`。
 * 存量主档的过敏史是一句自由文本（如「青霉素」），迁移成结构化行时**没有任何严重程度信息**，
 * 而表列是 NOT NULL —— 与其默认编一个「中度」被当成临床信息读，不如显式写「未评估」。
 * 它只用于渲染（`severityTone` 有配色）与展示，**不进表单下拉**：
 * 表单里的严重程度应该由录入人明确选择。
 */

/** 六组的 key / 标题。key 与后端 PatientHealthProfileVO 的字段、CDR 的 profile 分组一致 */
export const HEALTH_GROUPS = [
    { key: 'allergy', label: '过敏史', tone: 'danger' },
    { key: 'pastDisease', label: '既往病史', tone: 'warning' },
    { key: 'surgery', label: '手术外伤史', tone: 'info' },
    { key: 'family', label: '家族史', tone: 'info' },
    { key: 'medication', label: '用药史', tone: 'success' },
    { key: 'contact', label: '联系人', tone: 'primary' },
]

export const ALLERGY_TYPE_OPTIONS = ['药物', '食物', '其他']

/** 表单可选的严重程度（不含「未评估」，见文件头说明） */
export const ALLERGY_SEVERITY_OPTIONS = ['轻度', '中度', '重度', '危及生命']

/** 渲染用的严重程度取值全集（含迁移生成的「未评估」） */
export const ALLERGY_SEVERITY_ALL = [...ALLERGY_SEVERITY_OPTIONS, '未评估']

export const SURGERY_TYPE_OPTIONS = ['择期', '紧急', '急诊']

export const RECOVERY_STATUS_OPTIONS = ['良好', '一般', '差', '死亡']

export const DISEASE_STATUS_OPTIONS = ['已治愈', '控制良好', '未控制', '随访中']

export const DRUG_TYPE_OPTIONS = ['处方药', '非处方药', '中药', '保健品']

export const DRUG_ROUTE_OPTIONS = ['口服', '注射', '外用', '吸入']

export const MEDICATION_STATUS_OPTIONS = ['进行中', '已停用', '已换药', '已减量']

/** 是否在世（biz_patient_family_history.is_alive：0-已故 1-在世） */
export const ALIVE_OPTIONS = [
    { label: '在世', value: 1 },
    { label: '已故', value: 0 },
]

/**
 * 过敏严重程度配色：危及生命最重，未评估用中性色（**不给它配「轻度」那种绿**，
 * 否则「不知道」会被读成「没事」）。
 */
export function severityTone(severity) {
    switch (severity) {
        case '危及生命':
            return 'danger'
        case '重度':
            return 'danger'
        case '中度':
            return 'warning'
        case '轻度':
            return 'success'
        case '未评估':
        default:
            return 'info'
    }
}

/** 是否算「高风险过敏」——医生站/档案页上的红色警示用这一个判据，别在各页面各判一套 */
export function isHighRiskAllergy(severity) {
    return severity === '危及生命' || severity === '重度'
}

/**
 * 用药状态配色。
 * `进行中` 用 primary 而不是 success：正在吃的药是**需要注意**的（相互作用、重复用药），
 * 染成绿色会让人一眼扫过去当成「已结束」。
 */
export function medicationStatusTone(status) {
    switch (status) {
        case '进行中':
            return 'primary'
        case '已停用':
            return 'info'
        case '已换药':
            return 'warning'
        case '已减量':
            return 'warning'
        default:
            return 'info'
    }
}

/* ==================== 六组字段配置（表格列 + 编辑对话框共用一个来源） ==================== */
// type: text | select | date | number | switch
// options: 静态数组，或 'PATIENT_RELATION'（用 sys_patient_relation 字典）
// 页面（表格列渲染）与 HealthRecordEdit.vue（表单字段）必须共用这份配置，禁写第二套副本
export const GROUP_FIELDS = {
  allergy: [
    {key: 'allergenName', label: '过敏原', type: 'text', required: true, width: 150},
    // 类型必填：表列 allergy_type 是 NOT NULL 且取值只有药物/食物/其他（没有"未知"档），
    // 不标必填的话用户不选就会拿到后端 500，这里提前拦住并说清缺什么
    {key: 'allergyType', label: '类型', type: 'select', options: ALLERGY_TYPE_OPTIONS, required: true, width: 100},
    {
      key: 'allergySeverity',
      label: '严重程度',
      type: 'select',
      options: ALLERGY_SEVERITY_OPTIONS,
      width: 120,
      tone: severityTone
    },
    {key: 'allergySymptoms', label: '反应表现', type: 'text', minWidth: 180, placeholder: '如：全身皮疹、呼吸困难'},
    {key: 'allergyDate', label: '首次发生', type: 'date', width: 130},
    {key: 'occurrenceCount', label: '次数', type: 'number', width: 80},
    {key: 'treatmentGiven', label: '处理措施', type: 'text', minWidth: 180},
    {key: 'confirmedBy', label: '确认医生', type: 'text', width: 120},
  ],
  pastDisease: [
    {key: 'diseaseName', label: '疾病名称', type: 'text', required: true, minWidth: 200},
    {key: 'diseaseCode', label: 'ICD-10', type: 'text', width: 110, placeholder: '如 I10'},
    {key: 'diagnosisDate', label: '诊断日期', type: 'date', width: 130},
    {key: 'diagnosisDept', label: '诊断科室', type: 'text', width: 140},
    {key: 'currentStatus', label: '控制情况', type: 'select', options: DISEASE_STATUS_OPTIONS, width: 110},
    {key: 'treatmentPlan', label: '治疗方案', type: 'text', minWidth: 200},
    {key: 'relapseCount', label: '复发次数', type: 'number', width: 100},
    {key: 'lastFollowupDate', label: '最近随访', type: 'date', width: 130},
  ],
  surgery: [
    {key: 'surgeryName', label: '手术名称', type: 'text', required: true, minWidth: 200},
    {key: 'surgeryDate', label: '手术日期', type: 'date', required: true, width: 130},
    {key: 'surgeryType', label: '手术类型', type: 'select', options: SURGERY_TYPE_OPTIONS, width: 110},
    {key: 'surgeon', label: '主刀医生', type: 'text', width: 120},
    {key: 'anesthesiaType', label: '麻醉方式', type: 'text', width: 120},
    {key: 'hospitalName', label: '手术医院', type: 'text', width: 160},
    {key: 'postopDiagnosis', label: '术后诊断', type: 'text', minWidth: 170},
    {key: 'recoveryStatus', label: '恢复情况', type: 'select', options: RECOVERY_STATUS_OPTIONS, width: 110},
    {key: 'complications', label: '术后并发症', type: 'text', minWidth: 160},
  ],
  family: [
    // relationship 在这一组存**称谓文案**（父亲/母亲/伯父），与联系人那一组的数字码值相反
    {
      key: 'relationship',
      label: '与患者关系',
      type: 'text',
      required: true,
      width: 120,
      placeholder: '如：父亲、母亲、伯父'
    },
    {key: 'name', label: '亲属姓名', type: 'text', width: 120},
    {key: 'age', label: '年龄', type: 'number', width: 80},
    {key: 'isAlive', label: '在世情况', type: 'switch', options: ALIVE_OPTIONS, width: 110},
    {key: 'causeOfDeath', label: '死亡原因', type: 'text', width: 150, placeholder: '已故必填'},
    {key: 'healthStatus', label: '健康状况', type: 'text', minWidth: 180},
    {key: 'hereditaryDisease', label: '遗传性疾病', type: 'text', width: 160},
    {key: 'infectiousDisease', label: '传染病史', type: 'text', width: 140},
  ],
  medication: [
    {key: 'drugName', label: '药物名称', type: 'text', required: true, minWidth: 180},
    {key: 'drugType', label: '药物类型', type: 'select', options: DRUG_TYPE_OPTIONS, width: 110},
    {key: 'dosage', label: '剂量', type: 'text', width: 100, placeholder: '如 0.5g'},
    {key: 'frequency', label: '频次', type: 'text', width: 90, placeholder: '如 bid'},
    {key: 'route', label: '给药途径', type: 'select', options: DRUG_ROUTE_OPTIONS, width: 110},
    {key: 'startDate', label: '开始用药', type: 'date', required: true, width: 130},
    {key: 'endDate', label: '停药日期', type: 'date', width: 130},
    {
      key: 'status',
      label: '用药状态',
      type: 'select',
      options: MEDICATION_STATUS_OPTIONS,
      width: 110,
      tone: medicationStatusTone
    },
    {key: 'indications', label: '用药指征', type: 'text', minWidth: 160},
    {key: 'prescriber', label: '处方医生', type: 'text', width: 120},
    {key: 'reasonStop', label: '停药原因', type: 'text', width: 150},
  ],
  contact: [
    {key: 'contactName', label: '联系人', type: 'text', required: true, width: 120},
    // relationship 在这一组是 sys_patient_relation 的**码值**（tinyint），不是文案
    // 表列 relationship 是 NOT NULL 的字典码值：标必填，别让用户提交后才被后端退回
    {
      key: 'relationship',
      label: '与患者关系',
      type: 'select',
      options: 'PATIENT_RELATION',
      dictLabel: 'relationshipText',
      required: true,
      width: 130
    },
    {key: 'phone', label: '联系电话', type: 'text', width: 140, placeholder: '11 位手机号'},
    {
      key: 'isPrimary',
      label: '主要联系人',
      type: 'switch',
      options: [{label: '是', value: 1}, {label: '否', value: 0}],
      width: 120
    },
    {key: 'address', label: '联系地址', type: 'text', minWidth: 200},
  ],
}
