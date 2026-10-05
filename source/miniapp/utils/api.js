import { get, post, del } from './request'
import { getPatientId } from './auth'

/**
 * 集中管理小程序端所有后端调用。
 * 返回值为后端 Result 整体（{code,message,data}），页面自行判断 code。
 *
 * 端点约定（患者端统一走 his-miniapp 聚合模块 /miniapp/**，PATIENT 鉴权一把抓）：
 *  - 认证：/auth/login（账密）、/miniapp/auth/wxLogin（微信口子）、/auth/info、/auth/logout
 *  - 挂号：/miniapp/appoint/*、/miniapp/directory/*（科室/医生/号源）
 *  - 复诊：/miniapp/revisit/*（原病历候选 + 费用预估 + 自助复诊预约）
 *  - 报告：/miniapp/report/*（只返回已发布 report_status=4）
 *  - 缴费：/miniapp/charge/*、/miniapp/pay/*（统一支付单）
 *  - 队列：/miniapp/queue/*
 *  - 押金：/miniapp/deposit/*
 *  - 就诊人：/patient/guardian/*（患者域自有端点，保持原样）
 */

// ========== 认证 ==========
export const authApi = {
  login: (data) => post('/auth/login', data),
  info: () => get('/auth/info'),
  logout: () => post('/auth/logout')
}

// ========== 微信平台口子（his-miniapp；一键登录未真对接暂不上线，仅保留绑定供订阅消息用） ==========
export const wxApi = {
  // 登录态下绑定当前账号的 openid（订阅消息发送依赖）
  bindOpenid: (data) => post('/miniapp/auth/bindOpenid', data)
}

// ========== 患者 ==========
export const patientApi = {
  // 患者注册：创建 biz_patient + sys_user(user_type=3)
  register: (data) => post('/patient/register', data),
  // 患者档案（患者端聚合端点，只允许查绑定关系内的就诊人）
  getById: (patientId) => get('/miniapp/directory/patient', { patientId })
}

// ========== 短信验证码 ==========
export const smsApi = {
  // 注册验证码；mock 模式下后端会把验证码回显在 data.mockCode
  sendCode: (data) => post('/patient/sms/sendCode', data)
}

// ========== 医院信息（参数设置维护，主页展示） ==========
export const hospitalApi = {
  info: () => get('/system/config/hospitalInfo')
}

// ========== 科室 / 医生（患者端目录） ==========
export const deptApi = {
  selectList: () => get('/miniapp/directory/deptList')
}

export const doctorApi = {
  selectList: (params) => get('/miniapp/directory/doctorList', params)
}

// ========== 智能导诊 ==========
export const triageApi = {
  // 按主诉推荐科室（规则匹配，急症置顶）
  recommend: (data) => post('/miniapp/triage/recommend', data),
  // 常见症状快捷标签
  hotSymptoms: () => get('/miniapp/triage/hotSymptoms')
}

// ========== 号源 / 挂号 ==========
export const scheduleApi = {
  // 查询可挂号源（复用排班域，患者端原样取数）
  availableList: (data) => post('/miniapp/directory/scheduleList', data)
}

export const appointApi = {
  // 挂号（预约池扣号，registSource=4）
  upsert: (data) => post('/miniapp/appoint/upsert', data),
  // 我的预约（分页）
  listPage: (params) => get('/miniapp/appoint/listPage', params),
  // 退号（联动已支付挂号费原路退回）
  cancel: (data) => post('/miniapp/appoint/cancel', data),
  // 挂号详情
  getDetail: (params) => get('/miniapp/appoint/getDetail', params)
}

// ========== 复诊预约（患者自助，来源由服务端定死为 3） ==========
export const revisitApi = {
  // 原病历候选：只含真正看过病的门诊记录，按就诊日倒序
  recordSelectList: (patientId) => get('/miniapp/revisit/recordSelectList', { patientId }),
  // 费用预估：与实收同一个判定，所以「预览 0 元、到院要交钱」不可能发生
  feePreview: (data) => post('/miniapp/revisit/feePreview', data),
  // 提交复诊预约（占号源；不传 revisitSource，后端固定为患者自助）
  upsert: (data) => post('/miniapp/revisit/upsert', data)
}

// ========== 报告（只返回已发布） ==========
export const reportApi = {
  // 报告列表（reportType: 1-检查 2-检验，空=全部；服务端固定只查已发布）
  list: (data) => post('/miniapp/report/list', data),
  getById: (reportId) => get('/miniapp/report/getById', { reportId })
}

// ========== 门诊缴费（患者端聚合） ==========
export const chargeApi = {
  // 待缴列表（四层结算账单口径，取代旧 /miniapp/charge/pendingPage）
  pendingPage: (data) => post('/miniapp/pay/pendingBills', data)
}

// ========== 统一支付单（微信支付口子） ==========
export const payApi = {
  // 下单支付：桩模式直接返回 payStatus=1；真收银台模式返回 payParams 给 wx.requestPayment
  createOrder: (data) => post('/miniapp/pay/createOrder', data),
  // 我的支付单
  myOrders: () => get('/miniapp/pay/myOrders')
}

// ========== 排队叫号（患者视角） ==========
export const queueApi = {
  // 我的排队：近3日挂号 + 位次 + 前方等待人数（服务端校验就诊人绑定关系）
  myQueue: (patientId) => get('/miniapp/queue/myQueue', { patientId }),
  // 到院签到（只能签自己绑定的就诊人）
  myCheckIn: (registId) => post('/miniapp/queue/myCheckIn', { registId })
}

// ========== 处方 / 取药（患者视角） ==========
export const prescriptionApi = {
  myList: (patientId) => get('/miniapp/prescription/myList', { patientId })
}

// ========== 门诊病历（患者视角） ==========
export const emrApi = {
  myRecords: (patientId) => get('/miniapp/emr/myRecords', { patientId }),
  myRecordDetail: (patientId, recordId) => get('/miniapp/emr/myRecordDetail', { patientId, recordId })
}

// ========== 慢病档案（M1，患者端只读） ==========
export const chronicApi = {
  // 我的慢病档案（含长处方资格）
  myRecords: () => get('/miniapp/chronic/myRecords')
}

// ========== 消息中心（患者端，sys_message 患者侧入口） ==========
export const messageApi = {
  // 我的消息（分页；站内信 + 微信场景留痕均可见）
  listPage: (data) => post('/miniapp/message/listPage', data),
  // 未读数（角标）
  unreadCount: () => get('/miniapp/message/unreadCount'),
  // 标记已读（只允许标自己的消息）
  markRead: (messageIds) => post('/miniapp/message/markRead', { messageIds })
}

// ========== 客服台（常见问题 / 留言 / 埋点） ==========
export const faqApi = {
  // 常见问题分类（带条数）
  categories: () => get('/miniapp/faq/categories'),
  // 检索（关键词切词匹配 + 分类过滤，分页）
  listPage: (data) => post('/miniapp/faq/listPage', data),
  // 详情（累计查看次数）；faqId 全程字符串，按 JSON 数字传会丢精度
  getById: (faqId) => get('/miniapp/faq/getById', { faqId }),
  // 热门问题（客服页首屏）
  hotList: (limit) => get('/miniapp/faq/hotList', { limit }),
  // 有用反馈：helpful 1-有帮助 0-没帮助
  feedback: (data) => post('/miniapp/faq/feedback', data)
}

export const serviceApi = {
  // 提交工单（归属由登录态决定，不传 patientId）
  messageUpsert: (data) => post('/miniapp/service/messageUpsert', data),
  // 我的工单（含处理状态、受理人、回复次数、可执行动作）
  myMessages: (data) => post('/miniapp/service/myMessages', data),
  // 工单详情（含流转时间轴；内部备注后端已过滤）
  ticketDetail: (id) => get('/miniapp/service/ticketDetail', { id }),
  // 补充留言（已办结的补充会自动重开）
  ticketAppend: (data) => post('/miniapp/service/ticketAppend', data),
  // 患者动作：cancel 撤单 / confirm 确认解决 / reopen 重开
  ticketAction: (data) => post('/miniapp/service/ticketAction', data),
  // 客服页行为埋点（失败不影响业务）
  trace: (data) => post('/miniapp/service/trace', data)
}

// ========== 患者端 AI 能力 ==========
// 报告解读的事实层（哪些项异常）由后端规则算，模型只润色串话；
// 模型不可用时接口照常返回，只是 source=rule、degraded=true。
// 所以前端**不要**因为 degraded=true 就弹「服务不可用」，那会劝退本来就焦虑的患者。
// 费用解释 P1 起转纯计算（不经过模型，无 degraded/source 语义）；
// 用药说明是规则型能力（剂量逐字来自医嘱，模型不参与）；导诊归一失败时用原话查即可。
export const aiApi = {
  // 报告解读（患者版大白话）；reportId 全程字符串
  reportExplain: (data) => post('/ai/patient/reportExplain', data),
  // 费用解释（这笔钱怎么算的）
  feeExplain: (data) => post('/ai/patient/feeExplain', data),
  // 用药说明（这盒药怎么吃）；prescriptionId 全程字符串
  medicationGuide: (data) => post('/ai/patient/medicationGuide', data),
  // 导诊口语归一：口语 → 症状词 + 追问 + 检索文本。不推荐科室，科室仍由规则表给
  triageNormalize: (data) => post('/ai/patient/triageNormalize', data)
}

// ========== 住院押金（复用院内预交金口径） ==========
export const depositApi = {
  // 我的住院记录（押金页选择入院单）
  myAdmissions: (patientId) => get('/miniapp/deposit/myAdmissions', { patientId }),
  // 押金余额（充值合计/退款合计/余额）
  balance: (admissionId) => get('/miniapp/deposit/balance', { admissionId }),
  // 押金流水分页
  listPage: (data) => post('/miniapp/deposit/listPage', data)
}

// ========== 就诊人绑定（多就诊人基座，患者域自有端点） ==========
export const guardianApi = {
  myPatients: () => get('/patient/guardian/myPatients'),
  // 绑定发码：码只发往建档预留手机号
  sendBindCode: (data) => post('/patient/guardian/sendBindCode', data),
  bindPatient: (data) => post('/patient/guardian/bindPatient', data),
  // 新增建档发码：码发往操作人手机
  sendAddCode: (data) => post('/patient/guardian/sendAddCode', data),
  addPatient: (data) => post('/patient/guardian/addPatient', data),
  unbindPatient: (data) => post('/patient/guardian/unbindPatient', data),
  setDefault: (data) => post('/patient/guardian/setDefault', data),
  bindOpenid: (data) => post('/patient/guardian/bindOpenid', data),
  testNotify: () => post('/patient/guardian/testNotify')
}

// ========== 预问诊（G-05，患者端，/miniapp/previsit PATIENT 鉴权） ==========
export const previsitApi = {
  // 预问诊量表（主症状 + 通用问 + 主症状追问组，题目结构由后端下发）
  questionnaire: () => get('/miniapp/previsit/questionnaire'),
  // 提交问卷（按挂号 upsert，重复提交覆盖更新）
  submit: (data) => post('/miniapp/previsit/submit', data),
  // 按挂号查已提交的问卷（回显）
  getByRegist: (registId) => get('/miniapp/previsit/getByRegist', { registId })
}

// ========== 我的随访（G-06，患者端，/miniapp/followup PATIENT 鉴权） ==========
export const followupApi = {
  // 我的随访任务（只含本人名下，按计划时间倒序）
  myList: (patientId) => get('/miniapp/followup/myList', { patientId }),
  // 提交随访反馈（写回任务的患者反馈列）
  reply: (data) => post('/miniapp/followup/reply', data)
}

// ========== 通用：带患者ID 的便捷封装 ==========
export function withPatientId() {
  return getPatientId()
}
