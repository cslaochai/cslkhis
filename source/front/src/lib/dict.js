/**
 * 数据字典 - 前端静态配置
 * 用于常量数据，如状态、性别、类型等
 *
 * 注意：**带颜色的状态码值表统一放 `lib/statusColor.ts`**（QUEUE_STATUS / REGIST_STATUS /
 * APPLY_STATUS / OPD_LOG_STATUS…），页面直接 `statusOf(...)` 取。
 * 这里曾经也放了 QUEUE_STATUS / APPOINT_STATUS 两份手抄清单，抄成了挂号视角
 * （2已签到/3已接诊…）与后端枚举整套错位 —— 已删除，别再往这里加第二份。
 */

// 用户状态
export const USER_STATUS = [
    {label: '启用', value: 1},
    {label: '禁用', value: 0},
]

// 性别统一字典见 `lib/dict-cache.js` 的 SYS_GENDER（sys_gender，1男/2女/9未知），
// 文案函数在 `lib/patientGender.js`。这里原有一份 GENDER 死常量（未知写成 0）已删 —— 别再加回来。

// 员工类型
export const EMP_TYPE = [
    {label: '医生', value: 1},
    {label: '护士', value: 2},
    {label: '药剂师', value: 3},
    {label: '收费员', value: 4},
    {label: '管理员', value: 5},
]

// 是否
export const YES_NO = [
    {label: '是', value: 1},
    {label: '否', value: 0},
]

// 菜单类型
export const MENU_TYPE = [
    {label: '目录', value: 1},
    {label: '菜单', value: 2},
    {label: '按钮', value: 3},
]
/**
 * 就诊类型（= 字典 his_visit_type_enum：0-未知 / 1-初诊 / 2-复诊）。
 * 表单下拉不列「未知」，所以这里只有 1/2。
 *
 * 别把「急诊」加进来：急诊是**号别**（见下面 REGIST_TYPE 的 3-急诊号），
 * 与「初诊/复诊」是两个正交维度 —— 挂急诊号的患者一样分初诊和复诊。
 *
 * 也别从字典读：his_visit_type_enum 含 0-未知，直接铺进下拉会多一个不该选的选项。
 * 曾在这里多写过一个 {label:'体检', value:3}：字典里没有、库里 0 行、列注释也没提，
 * 真选了只会落一个别处一律渲染成「未知(3)」的值，已删。
 */
export const VISIT_TYPE = [
    {label: '初诊', value: 1},
    {label: '复诊', value: 2},
]
// 收费状态
export const CHARGE_STATUS = [
    {label: '待收费', value: 1},
    {label: '已收费', value: 2},
    {label: '已退费', value: 3},
    {label: '部分退费', value: 4},
    {label: '已取消', value: 5},
]

export const REGIST_TYPE = [
    {label: '普通号', value: 1},
    {label: '专家号', value: 2},
    {label: '急诊号', value: 3},
    {label: '免费号', value: 4},
]

export const CHECK_APPLY_STATUS = [
    {label: '待提交', value: 0},
    {label: '已提交', value: 1},
    {label: '已缴费', value: 2},
    {label: '已采样', value: 3},
    {label: '检验中', value: 4},
    {label: '已出报告', value: 5},
    {label: '已取消', value: 6},
]

// 收费项目类型
export const PAYMENT_ITEM_TYPE = [
    {label: '挂号费', value: 1},
    {label: '西药', value: 2},
    {label: '中成药', value: 3},
    {label: '中药饮片', value: 4},
    {label: '检查', value: 5},
    {label: '检验', value: 6},
    {label: '治疗', value: 7},
]

// 收费类型
export const CHARGE_TYPE_LIST = [
    {label: '挂号费', value: 1},
    {label: '药品费', value: 2},
    {label: '检查费', value: 3},
    {label: '检验费', value: 4},
    {label: '治疗费', value: 5},
    {label: '综合收费', value: 6},
]

// 支付方式
export const PAYMENT_METHOD_LIST = [
    {label: '现金', value: 1},
    {label: '微信', value: 2},
    {label: '支付宝', value: 3},
    {label: '医保卡', value: 4},
    {label: '余额', value: 5},
]

// 结算方式
export const SETTLEMENT_MODE_LIST = [
    {label: '自费', value: 1},
    {label: '医保', value: 2},
]

// 医保类型
export const MEDICAL_INSURANCE_TYPE_LIST = [
    {label: '城镇职工医保', value: '城镇职工医保'},
    {label: '城乡居民医保', value: '城乡居民医保'},
    {label: '公费医疗', value: '公费医疗'},
]

/**
 * 根据值获取标签
 * @param {Array} dictList - 字典列表
 * @param {*} value - 值
 * @returns {string} 标签文本
 */
export function getDictLabel(dictList, value) {
    const item = dictList.find(item => item.value == value)
    return item ? item.label : ''
}

/**
 * 根据值获取标签（带颜色）
 * @param {Array} dictList - 字典列表
 * @param {*} value - 值
 * @returns {object} { label, type } type 用于 el-tag
 */
export function getDictTag(dictList, value) {
    const item = dictList.find(item => item.value == value)
    if (!item) return {label: '', type: 'info'}

    // 根据状态返回对应颜色
    if (value == 1 || value === 1) return {label: item.label, type: 'success'}
    if (value == 0 || value === 0) return {label: item.label, type: 'danger'}
    return {label: item.label, type: 'info'}
}
