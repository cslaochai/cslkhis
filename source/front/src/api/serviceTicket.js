import request from './request'

// ==================== 工单受理端（sql/221 建表升级 / sql/222 菜单） ====================
// 患者提单后要有人接：没有这一端，工单永远停在「待受理」。
// 每一步流转都写 biz_service_ticket_log —— 患者端时间轴与客服端证据链是同一份数据。
// 权限码与患者端分开：患者端只认 PATIENT，这里只认 service:ticket:*。

// 工单列表（待受理优先排序）
export function getTicketList(params) {
    return request.post('/miniapp/service/admin/listPage', params || {})
}

// 工作台统计（待受理/处理中/已办结/已关闭/超时未受理）
export function getTicketStats() {
    return request.post('/miniapp/service/admin/stats', {})
}

// 详情（含全量流转记录，含内部备注）
export function getTicketDetail(id) {
    return request.get('/miniapp/service/admin/getById', { params: { id } })
}

// 受理 / 回复 / 办结 / 关闭 / 内部备注（操作人服务端取登录人）
export function ticketHandle(data) {
    return request.post('/miniapp/service/admin/handle', data)
}
