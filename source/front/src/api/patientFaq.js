import request from './request'

// ==================== 患者端常见问题维护（sql/216） ====================
// 语料由人工维护：患者每点一次「没帮助」都是在说这条答案该改了。
// 维护端改动即时生效（患者端读同一张表），所以改完不需要清任何缓存。
// 权限码与患者端分开：患者端只认 PATIENT，这里只认 patient:faq:*。

// 后台列表（含停用）
export function getFaqAdminList(params) {
    return request.post('/miniapp/faq/admin/listPage', params)
}

export function getFaqAdminDetail(faqId) {
    return request.get('/miniapp/faq/admin/getById', { params: { faqId } })
}

export function faqUpsert(data) {
    return request.post('/miniapp/faq/admin/upsert', data)
}

// 物理删：sys_faq.faq_no 的唯一键不含 del_flag，软删会占着键
export function faqDelete(id) {
    return request.post('/miniapp/faq/admin/deleteById', { id })
}
