import request from './request'

// ==================== 医保目录（国家编码模拟目录库） ====================

// 目录分页（对照候选弹窗复用本接口）
export function getYbCatalogList(params) {
    return request.get('/charge/ybCatalog/listPage', {params})
}

// 目录新增/修改（id 空=新增）
export function upsertYbCatalog(data) {
    return request.post('/charge/ybCatalog/upsert', data)
}

// 目录批量导入（按 yb_code 幂等，存在即更新；API 层包成 DTO 入参形状，视图层零改动）
export function importYbCatalog(items) {
    return request.post('/charge/ybCatalog/importBatch', {items})
}

// 目录启停
export function changeYbCatalogStatus(id, status) {
    return request.post('/charge/ybCatalog/changeStatus', null, {params: {id, status}})
}

// ==================== 对照关系 ====================

// 对照工作台分页（itemType 必填）
export function getYbMappingList(params) {
    return request.get('/charge/ybMapping/listPage', {params})
}

// 各类型对照率统计
export function getYbMappingStats() {
    return request.get('/charge/ybMapping/stats')
}

// 人工对照（已存在旧对照=换对照覆盖）
export function mapYbItem(data) {
    return request.post('/charge/ybMapping/map', data)
}

// 解对照（物理删）
export function unmapYbItem(itemType, itemId) {
    return request.post('/charge/ybMapping/unmap', null, {params: {itemType, itemId}})
}

// 自动对照（名称精确匹配且唯一命中才落）
export function autoMatchYb(itemType) {
    return request.post('/charge/ybMapping/autoMatch', itemType == null ? {} : {itemType})
}
