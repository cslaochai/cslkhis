import request from './request'

// 抗菌药物管理（sql/161，菜单 517/518/519）
// 后端控制器：/antibiotic（分级目录与处方权授权）、/antibioticMonitor（使用监测与 I 类切口点评）

// ========== 分级目录 ==========

export function listAntibioticCatalogPage(params) {
    return request.post('/antibiotic/catalogListPage', params)
}

// 维护药品分级与 DDD 值（level=0 表示移出抗菌药物目录）
export function upsertAntibioticCatalogLevel(data) {
    return request.post('/antibiotic/catalogLevelUpsert', data)
}

export function getAntibioticDrugSelectList() {
    return request.get('/antibiotic/antibioticDrugSelectList')
}

export function getAntibioticDoctorSelectList(keyword) {
    return request.get('/antibiotic/doctorSelectList', {params: {keyword}})
}

// ========== 医嘱别名（住院医嘱名 → 药品目录的精确匹配键）==========

export function listAntibioticAlias(drugId) {
    return request.get('/antibiotic/aliasList', {params: {drugId}})
}

export function upsertAntibioticAlias(data) {
    return request.post('/antibiotic/aliasUpsert', data)
}

export function deleteAntibioticAlias(id) {
    return request.delete('/antibiotic/aliasDeleteById', {params: {id}})
}

// ========== 处方权授权 ==========

export function listAntibioticAuthPage(params) {
    return request.post('/antibiotic/authListPage', params)
}

export function upsertAntibioticAuth(data) {
    return request.post('/antibiotic/authUpsert', data)
}

// 开方前越权自检（当前登录医师）：传药品 id 数组（API 层包成 DTO 入参形状，视图层零改动）
export function checkAntibioticAuthority(drugIds) {
    return request.post('/antibiotic/checkAuthority', {drugIds})
}

// ========== 使用监测 ==========

export function listAntibioticStatsPage(params) {
    return request.post('/antibioticMonitor/statsListPage', params)
}

export function previewAntibioticStats(statMonth) {
    return request.get('/antibioticMonitor/previewStats', {params: {statMonth}})
}

export function generateAntibioticStats(data) {
    return request.post('/antibioticMonitor/generateStats', data)
}

export function exportAntibioticStatsCsv(params) {
    return request.post('/antibioticMonitor/statsExportCsv', params)
}

// ========== I 类切口预防用药点评 ==========

export function getIncisionCandidates() {
    return request.get('/antibioticMonitor/incisionCandidates')
}

export function listIncisionReviewPage(params) {
    return request.post('/antibioticMonitor/incisionReviewListPage', params)
}

export function upsertIncisionReview(data) {
    return request.post('/antibioticMonitor/incisionReviewUpsert', data)
}
