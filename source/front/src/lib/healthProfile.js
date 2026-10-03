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
