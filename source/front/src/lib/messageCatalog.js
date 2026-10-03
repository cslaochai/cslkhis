/**
 * 站内信 biz_type 唯一展示口径（单点定义，页面禁止各写映射）
 *
 * 后端口径：his-system BizTypeEnum（21 个码值），列注释见 sql/69-消息业务类型扩码值.sql。
 * 本文件吞掉了原 lib/messageAction.js（仅 3 类的映射副本）——类型目录、紧急度、
 * 权限锁、岗位归属、筛选下拉，全部从这里出，改一处全端生效。
 *
 * 两条口径（与后端注释一致）：
 *   · 通知是「投给人的待办」，不是「投给角色的菜单」——列表对所有角色可见
 *     （多角色账号切身份不丢消息），但「处理」按钮按当前角色权限锁，见 kind/permissions；
 *   · 未知码值渲染「未知(n)」，不回落成某个看似合法的值。
 */

/**
 * @typedef {Object} MessageCatalogEntry
 * @property {string}  label       类型名（下拉/标签共用）
 * @property {string}  tagClass    类型 chip 的 tailwind 配色
 * @property {'urgent'|'warning'|'info'} severity 紧急度：urgent 置顶红标（危急值），
 *          warning 待办型，info 普通通知
 * @property {'task'|'notify'} kind task=待办型（必须显式处置回执）；notify=通知型（已读即闭环）
 * @property {string}  owner       归属岗位（当前角色处理不了时的提示文案）
 * @property {string[]} permissions 处理所需权限码（hasAnyPermission 语义：任一命中即可），
 *          权限码复用 sys_menu.permission，不新建权限体系
 */

const CATALOG = {
    /* ---------- 已接通发送方（3）---------- */
    // 危急值：全站唯一 urgent。确认/处置是临床动作，走 critical 闭环
    //（receive/handle 接口，状态机 1 待接收 → 2 已接收 → 3 已处置）
    critical: {
        label: '危急值', tagClass: 'bg-red-100 text-red-700',
        severity: 'urgent', kind: 'task',
        owner: '医生 / 护士', permissions: ['medtech:criticalValue:list'],
    },
    // 检查报告：医生读报告用 emr:records:list，检查技师在自己工作站看用 inspectionWorkstation
    inspection: {
        label: '检查报告', tagClass: 'bg-purple-100 text-purple-700',
        severity: 'info', kind: 'notify',
        owner: '医生 / 检查技师', permissions: ['emr:records:list', 'medtech:inspectionWorkstation:list'],
    },
    // 检验报告：同上，技师侧码为 laboratoryWorkstation
    report: {
        label: '检验报告', tagClass: 'bg-orange-100 text-orange-700',
        severity: 'info', kind: 'notify',
        owner: '医生 / 检验技师', permissions: ['emr:records:list', 'medtech:laboratoryWorkstation:list'],
    },

    /* ---------- 枚举已有、尚未接发送方（先给目录占位，别让新消息一来就渲染成未知）---------- */
    regist: {
        label: '挂号', tagClass: 'bg-blue-100 text-blue-700',
        severity: 'info', kind: 'notify',
        owner: '—', permissions: ['portal:messages:view'],
    },
    appointment: {
        label: '预约', tagClass: 'bg-green-100 text-green-700',
        severity: 'info', kind: 'notify',
        owner: '—', permissions: ['portal:messages:view'],
    },
    prescription: {
        label: '处方', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'warning', kind: 'task',
        owner: '医生 / 药剂师', permissions: ['portal:messages:view'],
    },
    drug: {
        label: '药品', tagClass: 'bg-red-100 text-red-700',
        severity: 'info', kind: 'notify',
        owner: '药剂师', permissions: ['portal:messages:view'],
    },

    /* ---------- BizTypeEnum 扩码值（sql/69 同步列注释）---------- */
    'appt-remind': {
        label: '预约提醒', tagClass: 'bg-green-100 text-green-700',
        severity: 'info', kind: 'notify',
        owner: '医生 / 患者端', permissions: ['portal:messages:view'],
    },
    'regist-refund': {
        label: '退号退费', tagClass: 'bg-blue-100 text-blue-700',
        severity: 'info', kind: 'notify',
        owner: '收费员', permissions: ['portal:messages:view'],
    },
    // 接通发送方（EmergencyServiceImpl.escalateOverdue）：急诊候诊超过该分诊级别的应接诊时限
    // → 催办给派单医生；无人认领的发当班医生/科室主任；严重超时两路一起发。
    // 闭环动作是「接诊」（status 离开候诊即不再被扫到），不是在收件箱点已读 → 待办型，
    // 同一条急诊对同一收件人只催一次。紧急度 warning：全站唯一 urgent 留给危急值。
    'emg-wait': {
        label: '急诊候诊超时', tagClass: 'bg-red-100 text-red-700',
        severity: 'warning', kind: 'task',
        owner: '医生 / 急诊科主任', permissions: ['portal:messages:view'],
    },
    // 接通发送方（EmergencyServiceImpl.escalateObservation）：留观超过上限（默认 72h，
    // sys_config emergency.observation_max_hours）→ 催办主管/当班医生/科室主任，
    // 要求当场定去向（转住院 / 离院 / 写明继续留观理由）。48h 预警只上看板不发消息，
    // 否则同一个人在 24h 内被催两轮，阶梯也收敛不完。
    'emg-obs': {
        label: '急诊留观超时限', tagClass: 'bg-red-100 text-red-700',
        severity: 'warning', kind: 'task',
        owner: '医生 / 急诊科主任', permissions: ['portal:messages:view'],
    },
    // 接通发送方（EmergencyServiceImpl.submitHandover）：交班受理后逐条把责任医生改派到
    // 接班人名下，并给每位接续医生发一条接收待办。闭环动作是「接诊/继续处理」，
    // 点已读不算完成 → 待办型，一次交班对一人只发一条。
    'emg-ho': {
        label: '急诊交班接收', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'warning', kind: 'task',
        owner: '医生', permissions: ['portal:messages:view'],
    },
    // 接通发送方（sql/169）：发给「当日总值班」的全院协调待办，三个触发点 ——
    // ① EmergencyServiceImpl 候诊/留观升级阶梯走完仍没人接 ② BedCenterServiceImpl 跨科调配
    // ③ ReferralService 转诊登记与待确认超时。收件人是 DutyRosterService.current() 解析出的那个人，
    // 一天一换、含临时换班 —— 不是科室，也不是写死的兜底账号。
    // 跨科调配/转诊登记是 warning（通知要协调），等床与转诊超时是 urgent（已挂住必须处置）。
    'duty-coord': {
        label: '全院协调', tagClass: 'bg-purple-100 text-purple-700',
        severity: 'warning', kind: 'task',
        owner: '总值班', permissions: ['portal:messages:view'],
    },
    // admit 当前发送方语义是「患者已收治入院 → 通知开证医生」（InpatientServiceImpl.notifyAdmitted），
    // 医生知道收进哪张床即可，无需动作 → 通知型。若日后接「待收治待办」再拆码值，别复用这个。
    admit: {
        label: '入院收治通知', tagClass: 'bg-cyan-100 text-cyan-700',
        severity: 'info', kind: 'notify',
        owner: '医生', permissions: ['portal:messages:view'],
    },
    // 接通发送方（InpatientOrderServiceImpl.verify）：医嘱校对完成 → 按开嘱医生+住院分组汇总通知。
    // 校对完成不需要医生动作，已读即闭环 → 通知型。组内含急嘱时后端发 warning。
    'inpat-order': {
        label: '住院医嘱提醒', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'info', kind: 'notify',
        owner: '医生', permissions: ['portal:messages:view'],
    },
    // 接通发送方（InpatientConsultationServiceImpl.save）：新会诊申请 → 会诊方（指定医生或会诊科室全员）。
    // 待办型：接诊时后端联动 handle_status 0→1，取消时 0→2，收件箱不会永远挂「待处理」。
    consult: {
        label: '会诊邀请', tagClass: 'bg-indigo-100 text-indigo-700',
        severity: 'warning', kind: 'task',
        owner: '医生', permissions: ['portal:messages:view'],
    },
    dispense: {
        label: '待发药', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'warning', kind: 'task',
        owner: '药剂师', permissions: ['portal:messages:view'],
    },
    // 审方只有「通过」一个出口（签名锚点流程，无打回状态），医生看一眼意见即可 → 通知型
    drugaudit: {
        label: '处方审核结果', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'warning', kind: 'notify',
        owner: '医生 / 药剂师', permissions: ['portal:messages:view'],
    },
    stock: {
        label: '缺药库存预警', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'warning', kind: 'notify',
        owner: '药剂师', permissions: ['portal:messages:view'],
    },
    // 接通发送方（MedicalRecordArchiveServiceImpl.notifyOverdueArchives）：
    // 门诊病历超 3 天未归档 → 每日一条提醒给病历医生（定时 08:00 + POST /charge/archive/notifyOverdue 补跑）。
    // 整改动作是「去归档」，无消息内闭环接口 → 通知型。
    'emr-arch': {
        label: '病历归档超期', tagClass: 'bg-slate-100 text-slate-700',
        severity: 'warning', kind: 'notify',
        owner: '医生', permissions: ['portal:messages:view'],
    },
    // 接通发送方（QualityControlServiceImpl.executeQc）：质控发现问题 → 通知病历书写医生。
    // 病历整改无系统内闭环动作（改病历重新质控即可），质控单流转在质控员侧 → 通知型。
    'emr-qc': {
        label: '病历质控问题', tagClass: 'bg-slate-100 text-slate-700',
        severity: 'warning', kind: 'notify',
        owner: '医生', permissions: ['portal:messages:view'],
    },
    // 接通发送方（ArchiveBorrowServiceImpl.notifyOverdue）：借阅单应还日期已过仍未归还
    // → 每日一条提醒申请人（定时 08:30 + POST /charge/archiveBorrow/notifyOverdue 补跑）。
    // 整改动作是「去病案室归还」，无消息内闭环接口 → 通知型。
    'arch-borrow': {
        label: '病案借阅超期', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'warning', kind: 'notify',
        owner: '病案室', permissions: ['portal:messages:view'],
    },
    // 接通发送方（ArchiveCodeTaskServiceImpl.audit）：编码任务被退修 → 提醒编码员改编码重新提交，
    // 提交动作在编码任务池页面完成 → 通知型。
    'code-task': {
        label: '编码任务退修', tagClass: 'bg-slate-100 text-slate-700',
        severity: 'warning', kind: 'notify',
        owner: '病案编码员', permissions: ['portal:messages:view'],
    },
    sign: {
        label: '签名待办提醒', tagClass: 'bg-indigo-100 text-indigo-700',
        severity: 'warning', kind: 'task',
        owner: '医生 / 药剂师', permissions: ['portal:messages:view'],
    },
    settle: {
        label: '医保结算结果', tagClass: 'bg-teal-100 text-teal-700',
        severity: 'info', kind: 'notify',
        owner: '收费员', permissions: ['portal:messages:view'],
    },
    arrears: {
        label: '欠费提醒', tagClass: 'bg-amber-100 text-amber-700',
        severity: 'warning', kind: 'notify',
        owner: '收费员', permissions: ['portal:messages:view'],
    },
    notice: {
        label: '系统公告', tagClass: 'bg-slate-100 text-slate-600',
        severity: 'info', kind: 'notify',
        owner: '—', permissions: ['portal:messages:view'],
    },
    // 接通发送方（DeathCertificateServiceImpl.notifyOverdue）：已开具且逾院内上报时限仍未上报
    // → 每日一条提醒病案/防保科补报（定时 09:00 + POST /patient/death/cert/notifyOverdue 补跑）。
    // 上报动作在死亡证明工作台上报台账完成 → 通知型。
    'death-cert': {
        label: '死亡证明上报时限', tagClass: 'bg-rose-100 text-rose-700',
        severity: 'warning', kind: 'notify',
        owner: '病案室 / 防保科', permissions: ['ipd:deathCertificate:report'],
    },
}

/** 未知业务类型：只要求「能进消息页」这个码，不额外收紧（新类型不能一上线就点不动） */
const FALLBACK = {
    label: null, tagClass: 'bg-slate-100 text-slate-600',
    severity: 'info', kind: 'notify',
    owner: '—', permissions: ['portal:messages:view'],
}

/** 紧急度权重：urgent 排最前（工作台排序用），其余按时间倒序 */
const SEVERITY_RANK = {urgent: 0, warning: 1, info: 2}

/** @returns {MessageCatalogEntry} */
export function messageCatalogOf(bizType) {
    return CATALOG[bizType] || FALLBACK
}

/** 类型名；未知码值渲染「未知(n)」，不回落成看似合法的值 */
export function messageLabel(bizType) {
    const hit = CATALOG[bizType]
    return hit ? hit.label : `未知(${bizType ?? ''})`
}

export function messageTagClass(bizType) {
    return messageCatalogOf(bizType).tagClass
}

export function messageSeverity(bizType) {
    return messageCatalogOf(bizType).severity
}

export function messageSeverityRank(bizType) {
    return SEVERITY_RANK[messageCatalogOf(bizType).severity] ?? 2
}

/** 待办型消息（工作台「待办」Tab / 红点提示用） */
export function isTaskMessage(bizType) {
    return messageCatalogOf(bizType).kind === 'task'
}

/** 这类站内信「处理」所需权限码（hasAnyPermission 语义） */
export function messageActionPermissions(bizType) {
    return messageCatalogOf(bizType).permissions
}

/** 这类站内信归属岗位（当前角色处理不了时的提示文案） */
export function messageActionOwner(bizType) {
    return messageCatalogOf(bizType).owner
}

/** 筛选下拉选项（目录驱动，不会漏 critical） */
export const MESSAGE_TYPE_OPTIONS = Object.entries(CATALOG)
    .map(([value, e]) => ({value, label: e.label}))
    .sort((a, b) => messageSeverityRank(a.value) - messageSeverityRank(b.value))

/**
 * 处理状态展示口径（sys_message.handle_status，见 sql/70）。
 * NULL = 通知型（用 read_status 闭环），不渲染处理状态标签。
 */
export function messageHandleStatusMeta(handleStatus) {
    if (handleStatus === 0) return {label: '待处理', tagClass: 'bg-amber-100 text-amber-700'}
    if (handleStatus === 1) return {label: '已处理', tagClass: 'bg-emerald-100 text-emerald-700'}
    if (handleStatus === 2) return {label: '已关闭', tagClass: 'bg-slate-100 text-slate-500'}
    return null
}

/** payload 里值得渲染成摘要 chip 的已知键（后端新增键时在这里登记，未知键一律忽略） */
const PAYLOAD_KEYS = [
    ['patientName', '患者'],
    ['itemName', '项目'],
    ['prescriptionNo', '处方'],
    ['criticalNo', '危急值号'],
    ['wardName', '病区'],
    ['bedNo', '床号'],
    ['admissionNo', '住院号'],
    ['opinion', '审方意见'],
    ['consultationNo', '会诊号'],
    ['toDeptName', '会诊科室'],
    ['qcNo', '质控单'],
    ['recordNo', '病历号'],
    ['orderNo', '医嘱号'],
    ['count', '条数'],
    ['issueCount', '问题数'],
    ['grade', '等级'],
    ['visitDate', '就诊日'],
    ['overdueDays', '超期天数'],
    ['verifyNurse', '校对护士'],
    ['arrears', '欠费'],
]

/**
 * payload（JSON 字符串或已解析对象）→ [{label, text}] 摘要 chips。
 * 解析失败返回空数组——不抛错、不把原始 JSON 甩在页面上。
 */
export function messagePayloadChips(payload) {
    let obj = payload
    if (typeof payload === 'string') {
        try {
            obj = JSON.parse(payload)
        } catch {
            return []
        }
    }
    if (!obj || typeof obj !== 'object') return []
    return PAYLOAD_KEYS
        .filter(([k]) => obj[k] !== undefined && obj[k] !== null && obj[k] !== '')
        .map(([k, label]) => ({label, text: String(obj[k])}))
}
