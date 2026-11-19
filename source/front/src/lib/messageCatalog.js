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
    // 接通发送方（EmergencyServiceImpl.escalateReeval）：候诊超过应接诊时限仍未接诊
    // → 催分诊台复评（受众：当班护理；级别只升不降）。闭环是「复评并调整级别」，落点在分诊工作站。
    'emg-reeval': {
        label: '急诊分诊复评到期', tagClass: 'bg-red-100 text-red-700',
        severity: 'warning', kind: 'task',
        owner: '急诊护士 / 分诊台', permissions: ['portal:messages:view'],
    },
    // 复评时级别上调 → 通知负责医生 / 当班医生。与 emg-reeval 分开计数，
    // 否则同一条急诊先催过护士就会把医生的升级通知吞掉。落点在急诊医生工作站。
    'emg-reeval-up': {
        label: '急诊分诊级别上调', tagClass: 'bg-red-100 text-red-700',
        severity: 'urgent', kind: 'task',
        owner: '医生 / 急诊科主任', permissions: ['portal:messages:view'],
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
    // 接通发送方（DutyCallServiceImpl.call，sql/297）：临床二/三线听班呼叫。
    // 收件人是当班听班医生（排班上、点位 attend_mode=2），不是岗位、不是全科室 ——
    // 听班是「叫这一班的那个人」。待办型：闭环动作是「到岗应答」，点已读不算到岗。
    'duty-call': {
        label: '听班呼叫', tagClass: 'bg-orange-100 text-orange-700',
        severity: 'warning', kind: 'task',
        owner: '听班值班医生', permissions: ['portal:messages:view'],
    },
    // 接通发送方（DutyCallServiceImpl.escalateOne，sql/297）：听班呼叫到岗超时 →
    // 升级给上一层级（三线 / 全院总值班）。与 duty-call 分码：原被叫人仍要看到
    // 自己那条呼叫，混成一条码会让原被叫人的待办被升级通知顶掉。
    'duty-call-esc': {
        label: '听班呼叫超时升级', tagClass: 'bg-red-100 text-red-700',
        severity: 'urgent', kind: 'task',
        owner: '三线值班医生 / 总值班', permissions: ['portal:messages:view'],
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
    // 传染病报卡时限预警：限期未填报 → 按天提醒填报医师（公卫上报）。填报动作在「我的未报」完成
    'infectious-report': {
        label: '传染病报卡时限预警', tagClass: 'bg-rose-100 text-rose-700',
        severity: 'warning', kind: 'notify',
        owner: '医生 / 公卫科', permissions: ['portal:messages:view'],
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
    // 收费/支付回执（库内历史码值，his-ms-charge 发出）：已收款结果通知，收费员查阅即可
    pay: {
        label: '支付结果', tagClass: 'bg-teal-100 text-teal-700',
        severity: 'info', kind: 'notify',
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

/**
 * 落地页面：收件箱只做提醒，真正的处置在业务页面完成（急诊接诊 / 分诊复评 / 值班日志签收 / 发药…）。
 * 一类一条，与 CATALOG 的 bizType 一一对应 —— 新增码值时这里必须同步，否则弹框又退回「只显示一段正文」。
 *
 * bizNoKey：payload 里代表「业务单号」的字段。跳转时一并带上，目标页用它预填搜索/定位，
 * 只跳路径不带单据 = 落到模块首页，用户还得自己翻（等于没落地）。
 *
 * 刻意为空的几类：
 *   critical            危急值在弹框内闭环（确认接收 → 填处置措施）
 *   duty-call / -esc    听班呼叫在弹框内闭环（到岗应答）
 *   consult             组件内已有「前往会诊管理」按钮
 *   notice              系统公告没有可去的页面
 */
const GOTO_TARGETS = {
    regist: {path: '/appointments', label: '挂号预约', bizNoKey: 'registNo'},
    appointment: {path: '/appointments', label: '挂号预约', bizNoKey: 'apptNo'},
    'appt-remind': {path: '/appointments', label: '挂号预约', bizNoKey: 'apptNo'},
    'regist-refund': {path: '/refund', label: '退费申请', bizNoKey: 'refundNo'},
    prescription: {path: '/prescription-audit', label: '处方审核', bizNoKey: 'prescriptionNo'},
    inspection: {path: '/inspection-workstation', label: '检查工作站', bizNoKey: 'examNo'},
    report: {path: '/laboratory-workstation', label: '检验工作站', bizNoKey: 'reportNo'},
    drug: {path: '/pharmacy', label: '药品库存', bizNoKey: 'drugCode'},
    stock: {path: '/pharmacy', label: '药品库存', bizNoKey: 'drugCode'},
    dispense: {path: '/pharmacy-window', label: '药房发药', bizNoKey: 'prescriptionNo'},
    drugaudit: {path: '/prescription-audit', label: '处方审核', bizNoKey: 'prescriptionNo'},
    'emg-wait': {path: '/emergency-doctor', label: '急诊医生工作站', bizNoKey: 'emergencyNo'},
    'emg-obs': {path: '/emergency-doctor', label: '急诊医生工作站', bizNoKey: 'emergencyNo'},
    'emg-ho': {path: '/emergency-doctor', label: '急诊医生工作站', bizNoKey: 'emergencyNo'},
    'emg-reeval': {path: '/triage', label: '分诊工作站', bizNoKey: 'emergencyNo'},
    'emg-reeval-up': {path: '/emergency-doctor', label: '急诊医生工作站', bizNoKey: 'emergencyNo'},
    'duty-coord': {path: '/duty-log', label: '总值班日志', bizNoKey: 'referralNo'},
    admit: {path: '/inpatient', label: '入出院管理', bizNoKey: 'admissionNo'},
    'inpat-order': {path: '/inpatient-order', label: '住院医生站', bizNoKey: 'orderNo'},
    'emr-arch': {path: '/archive-center', label: '病案管理', bizNoKey: 'recordNo'},
    'emr-qc': {path: '/medical-record-qc', label: '病历质控', bizNoKey: 'qcNo'},
    'arch-borrow': {path: '/archive-center', label: '病案管理', bizNoKey: 'borrowNo'},
    'code-task': {path: '/archive-center', label: '病案管理', bizNoKey: 'taskNo'},
    'infectious-report': {path: '/my-unreported', label: '我的未报', bizNoKey: 'registNo'},
    sign: {path: '/sign-cert', label: '签名证书'},
    settle: {path: '/insurance', label: '医保结算清单', bizNoKey: 'settleNo'},
    arrears: {path: '/arrears-control', label: '欠费管控', bizNoKey: 'admissionNo'},
    pay: {path: '/cashier', label: '收费结算窗口', bizNoKey: 'billNo'},
    'death-cert': {path: '/death-certificate', label: '死亡证明登记', bizNoKey: 'certNo'},
}

/** payload 是 JSON 字符串（SysMessageVO.payload 为 String），脏数据不能把跳转打断 */
const payloadOf = (message) => {
    const raw = message?.payload
    if (!raw) return null
    if (typeof raw === 'object') return raw
    try {
        return JSON.parse(raw)
    } catch (e) {
        console.error('消息 payload 解析失败', e)
        return null
    }
}

/**
 * 待办深链：目标路由 + 查询参数（from/bizType/bizId/bizNo）。
 * 目标页凭 bizNo（界面上认得）或 bizId 定位到具体单据。
 */
/**
 * 待办深链：目标路由 + 查询参数（from/bizType/bizId/bizNo）。
 * 目标页凭 bizNo（界面上认得）或 bizId 定位到具体单据。
 */
export function messageGotoQuery(message) {
    const target = messageGotoTarget(message?.bizType)
    if (!target)
        return null
    const payload = payloadOf(message)
    const bizNo = target.bizNoKey ? String(payload?.[target.bizNoKey] ?? '') : ''
    return {
        path: target.path,
        query: {
            from: 'msg',
            bizType: String(message?.bizType ?? ''),
            bizId: message?.bizId == null ? '' : String(message.bizId),
            ...(bizNo ? {bizNo} : {}),
        },
    }
}

/**
 * 截止处理时间：payload 里的绝对时间优先（deadlineTime/deadline…），
 * 其次用「时限分钟 + 发送时间」推算（急诊候诊 targetSeeMinutes、危急值 deadlineMinutes）；
 * 都没有返回空串，列表显示「—」表示这类消息本身没有办理时限。
 */
export function messageDeadline(message) {
    const payload = payloadOf(message)
    if (!payload) return ''
    for (const key of ['deadlineTime', 'deadline', 'expectReturnDate', 'dueTime', 'expireTime', 'handleDeadline']) {
        if (payload[key]) return String(payload[key]).replace('T', ' ').slice(0, 16)
    }
    // 只有危急值的 deadlineMinutes 是「从本条消息发出起算」的处置时限，可以推算；
    // 急诊候诊的 targetSeeMinutes 起算点是挂号而非发消息，从 sendTime 加会算出一个还没到的假截止时间，故不推算。
    const minutes = payload.deadlineMinutes
    const base = message?.sendTime ? new Date(String(message.sendTime).replace(' ', 'T')) : null
    if (minutes != null && base && !isNaN(base.getTime())) {
        const due = new Date(base.getTime() + Number(minutes) * 60000)
        const pad = (n) => String(n).padStart(2, '0')
        return `${due.getFullYear()}-${pad(due.getMonth() + 1)}-${pad(due.getDate())} ${pad(due.getHours())}:${pad(due.getMinutes())}`
    }
    return ''
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

/** 这类站内信的落地页面（无则 undefined）：弹框据此渲染「前往…处理」，避免只给一段正文 */
export function messageGotoTarget(bizType) {
    return GOTO_TARGETS[bizType]
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
    ['callNo', '呼叫单号'],
    ['targetLevelText', '被叫层级'],
    ['deptName', '科室'],
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
