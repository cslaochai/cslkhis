// 状态色板（三页共用）
//
// 原来分诊台 / 医生站 / 今日就诊各写了一套 statusMap，同一个「已过号」在三个页面是三种颜色，
// 而且分诊台那套还混进了挂号语义（registStatus）。这里按「业务域 + 码值」收口，
// 页面只调函数、不再自己定义色板。
//
// 约定：颜色表达的是**紧急/异常程度**，不是装饰 ——
//   蓝 = 正常在途、紫 = 进行中、绿 = 正常终结、红 = 异常终结（退号/作废）、
//   橙 = 需要注意（过号/欠费）、灰 = 终态但不重要。

export interface StatusStyle {
    /** 文案 */
    label: string
    /** 列表徽标 class（tailwind，底色+文字色） */
    color: string
    /** Element Plus 语义色，给 el-tag / el-button 用 */
    tagType: 'primary' | 'success' | 'warning' | 'danger' | 'info'
}

const UNKNOWN: StatusStyle = {label: '未知', color: 'bg-slate-100 text-slate-500', tagType: 'info'}

/** 未知码值一律渲染成 `未知(n)`，绝不回落到某个合法值（回落会让人以为数据是对的） */
export function unknownOf(code: unknown): StatusStyle {
    if (code === null || code === undefined || code === '') return UNKNOWN
    return {label: `未知(${code})`, color: 'bg-slate-100 text-slate-500', tagType: 'info'}
}

/**
 * 队列状态：QueueStatusEnum 2 候诊中 / 3 就诊中 / 4 已就诊 / 5 已退号 / 6 已过号 / 7 已失效。
 *
 * 7 已失效 是日终结转（跨日未接诊）落的终态，和 6 已过号 不是一回事：
 * 6 = 叫了号没来，7 = 压根没被叫到。两者颜色都取「需要注意」，但文案必须分开，
 * 否则「过号率」这类统计会被 7 混进来而无人察觉。
 */
export const QUEUE_STATUS: Record<number, StatusStyle> = {
    2: {label: '候诊中', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    3: {label: '就诊中', color: 'bg-purple-100 text-purple-700', tagType: 'primary'},
    4: {label: '已就诊', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    5: {label: '已退号', color: 'bg-red-100 text-red-600', tagType: 'danger'},
    6: {label: '已过号', color: 'bg-amber-100 text-amber-700', tagType: 'warning'},
    7: {label: '已失效', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
}

/**
 * 挂号状态：AppointStatusEnum 0 未知状态 / 1 已挂号 / 2 已签到 / 3 已接诊 / 4 已就诊
 * / 5 已退号 / 6 已过号 / 7 爽约 / 8 未就诊。
 *
 * 7 爽约 = 挂了号当天没到院；8 未就诊 = 到院签到了但没被接诊（日终结转落的）。
 * 这两个是**终态**，页面上不要再给「签到 / 改约 / 退号」按钮 ——
 * 号源在昨天，退了也还不了池（见 DayEndSettleMapper 注释）。
 */
export const REGIST_STATUS: Record<number, StatusStyle> = {
    0: {label: '未知状态', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
    1: {label: '已挂号', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '已签到', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    3: {label: '已接诊', color: 'bg-purple-100 text-purple-700', tagType: 'primary'},
    4: {label: '已就诊', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    5: {label: '已退号', color: 'bg-red-100 text-red-600', tagType: 'danger'},
    6: {label: '已过号', color: 'bg-amber-100 text-amber-700', tagType: 'warning'},
    7: {label: '爽约', color: 'bg-rose-100 text-rose-700', tagType: 'danger'},
    8: {label: '未就诊', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
}

/** 门诊分诊 4 级（越小越优先）：1 危重 / 2 急症 / 3 亚急 / 4 非急（默认） */
export const TRIAGE_LEVEL: Record<number, StatusStyle> = {
    1: {label: '1级·危重', color: 'bg-red-100 text-red-700', tagType: 'danger'},
    2: {label: '2级·急症', color: 'bg-amber-100 text-amber-700', tagType: 'warning'},
    3: {label: '3级·亚急', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    4: {label: '4级·非急', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
}

/**
 * 申请单状态：ApplyStatusEnum 全 7 态，**照抄后端枚举，不要自己收口**。
 * 检验申请会真的走到「已采样/检验中/已出报告」，只写 1/2/6 会把出报告的单子显示成「未知(5)」。
 * （后端若以后真的要收口成三态，两边必须同一次改完。）
 */
export const APPLY_STATUS: Record<number, StatusStyle> = {
    0: {label: '未知状态', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
    1: {label: '已提交', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '已缴费', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    3: {label: '已采样', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    4: {label: '检验中', color: 'bg-purple-100 text-purple-700', tagType: 'primary'},
    5: {label: '已出报告', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    6: {label: '已取消', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
}

/**
 * 门诊日志的「就诊状态」：OpdLogStatusEnum，由「挂号状态 + 队列状态」在后端推导。
 * 2~6 与 QUEUE_STATUS 同码同文案，所以这里复用 QUEUE_STATUS 的样式，只额外补 0/1/7/8。
 *
 * 7 在**就诊状态**域里读作「未就诊」（队列域 7=已失效，同一个事实：签到了没看上），
 * 8 是「爽约」（只存在于挂号侧，没有队列对应值）。别把 7/8 拿去 QUEUE_STATUS 里查。
 *
 * 注意：推导不出来时后端给 logStatus=null，页面要用 `queueStatus` 渲染成「未知(n)」，
 * 不能因为它没有 logStatus 就当成「待签到」。
 */
export const OPD_LOG_STATUS: Record<number, StatusStyle> = {
    0: {label: '未缴费', color: 'bg-amber-100 text-amber-700', tagType: 'warning'},
    1: {label: '待签到', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: QUEUE_STATUS[2],
    3: QUEUE_STATUS[3],
    4: QUEUE_STATUS[4],
    5: QUEUE_STATUS[5],
    6: QUEUE_STATUS[6],
    7: {label: '未就诊', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
    8: {label: '爽约', color: 'bg-rose-100 text-rose-700', tagType: 'danger'},
}

/** 门诊日志状态筛选项（顺序即业务顺序） */
export const OPD_LOG_STATUS_OPTIONS = [0, 1, 2, 3, 4, 5, 6, 7, 8].map((code) => ({
    value: code,
    label: OPD_LOG_STATUS[code].label,
}))

/** 挂号类型（号别）：biz_appoint_info.regist_type 1 普通号 2 专家号 3 急诊号 4 免费号 */
export const REGIST_TYPE: Record<number, StatusStyle> = {
    1: {label: '普通号', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '专家号', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    3: {label: '急诊号', color: 'bg-red-100 text-red-600', tagType: 'danger'},
    4: {label: '免费号', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
}

/** 挂号来源：biz_appoint_info.regist_source 1 窗口 2 自助机 3 网上 4 预约 */
export const REGIST_SOURCE: Record<number, StatusStyle> = {
    1: {label: '窗口', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '自助机', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    3: {label: '网上', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    4: {label: '预约', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
}

/**
 * 就诊类型：biz_appoint_info.revisit_type 1 初诊 / 2 复诊。
 * 库里这一列目前全为 null（写入口在批次 E 才会落），所以 `revisitTypeOf` 会给出「未标注」——
 * 这是事实，不要回落到「初诊」。
 */
export const REVISIT_TYPE: Record<number, StatusStyle> = {
    1: {label: '初诊', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '复诊', color: 'bg-amber-100 text-amber-700', tagType: 'warning'},
}

/** 就诊类型专用：null 有明确业务含义（还没标注），单独给文案而不是「未知」 */
export function revisitTypeOf(code: unknown): StatusStyle {
    if (code === null || code === undefined || code === '') {
        return {label: '未标注', color: 'bg-slate-50 text-slate-400', tagType: 'info'}
    }
    return statusOf(REVISIT_TYPE, code)
}

/**
 * 队列行/患者条的「号别」徽章：急诊号（regist_type=3）优先显「急诊号」。
 * 急诊直录写侧把 visit_type 一律置 1-初诊，而初诊/复诊是给门诊排班用的口径
 * （复诊要回原病历），挂在急诊患者身上既是噪音又误导 —— 真实 HIS 的号别徽章
 * 本来就是「急诊 > 初诊/复诊」按号类优先。
 */
export function queueVisitBadgeOf(row: any): StatusStyle {
    if (row?.registType === 3) return REGIST_TYPE[3]
    return revisitTypeOf(row?.visitType)
}

/**
 * 收费状态：biz_charge_info.charge_status 1 待收费 / 2 已收费 / 3 已退费 / 4 部分退费 / 5 已取消。
 *
 * 用途是「这笔挂号的钱收了没有」—— 看板上决定能不能改号源的正是它：
 * 已收费(2)/部分退费(4) 表示钱已经动过，挂号台不能直接换号源（详见预约看板 canReschedule）。
 */
export const CHARGE_STATUS: Record<number, StatusStyle> = {
    1: {label: '待收费', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
    2: {label: '已收费', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    3: {label: '已退费', color: 'bg-red-100 text-red-600', tagType: 'danger'},
    4: {label: '部分退费', color: 'bg-amber-100 text-amber-700', tagType: 'warning'},
    5: {label: '已取消', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
}

/** 结算方式结算：biz_appoint_info.settlement_type 1 自费 2 城镇职工医保 3 城乡居民医保 4 公费 5 商业保险 */
export const SETTLEMENT_TYPE: Record<number, StatusStyle> = {
    1: {label: '自费', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '职工医保', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    3: {label: '居民医保', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    4: {label: '公费', color: 'bg-purple-100 text-purple-700', tagType: 'primary'},
    5: {label: '商业保险', color: 'bg-amber-100 text-amber-700', tagType: 'warning'},
}

/** 检查执行状态 InsRecordStatusEnum 1 已登记 … 7 已取消 */
export const INSPECTION_RECORD_STATUS: Record<number, StatusStyle> = {
    1: {label: '已登记', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '已签到', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    3: {label: '检查中', color: 'bg-purple-100 text-purple-700', tagType: 'primary'},
    4: {label: '已出结果', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    5: {label: '已审核', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    6: {label: '已发布', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    7: {label: '已取消', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
}

/** 检验执行状态 LabRecordStatusEnum 1 已登记 … 8 已取消 */
export const LAB_RECORD_STATUS: Record<number, StatusStyle> = {
    1: {label: '已登记', color: 'bg-slate-100 text-slate-600', tagType: 'info'},
    2: {label: '已采样', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    3: {label: '已接收', color: 'bg-blue-100 text-blue-700', tagType: 'primary'},
    4: {label: '检测中', color: 'bg-purple-100 text-purple-700', tagType: 'primary'},
    5: {label: '已出结果', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    6: {label: '已审核', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    7: {label: '已发布', color: 'bg-emerald-100 text-emerald-700', tagType: 'success'},
    8: {label: '已取消', color: 'bg-slate-100 text-slate-500', tagType: 'info'},
}

/** 取样式；码值为空或枚举里没有 → 未知(n)，不回落 */
export function statusOf(table: Record<number, StatusStyle>, code: unknown): StatusStyle {
    if (code === null || code === undefined || code === '') return UNKNOWN
    const hit = table[Number(code)]
    return hit || unknownOf(code)
}
