import request from './request'

// ==================== 检验项目白话词典维护（sql/218 建表 / sql/219 菜单） ====================
// 患者端报告解读的每一句白话都来自这张表。表建完不维护就会烂：
// 检验科一加新项目，患者端就多一个只有数值、没有解释的条目。
// 权限码与患者端分开：患者端只认 PATIENT，这里只认 lab:plain:*。
// 后端 /coverage（未配白话的项目清单）仍在，但维护页不再展示这块 —— 有需要可单独取。

// 后台列表（含停用）
export function getLabPlainList(params) {
    return request.post('/ai/admin/labPlain/listPage', params || {})
}

export function getLabPlainDetail(id) {
    return request.get('/ai/admin/labPlain/getById', { params: { id } })
}

// 现有分组清单（筛选下拉，随词条增删变化）
export function getLabPlainGroupNames() {
    return request.get('/ai/admin/labPlain/groupNameSelectList')
}

export function labPlainUpsert(data) {
    return request.post('/ai/admin/labPlain/upsert', data)
}

// 物理删：uk_item_name 唯一键不含 del_flag，软删会占着键
export function labPlainDelete(id) {
    return request.post('/ai/admin/labPlain/deleteById', { id })
}
