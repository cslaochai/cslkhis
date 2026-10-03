import request from './request'

/**
 * 简化 PACS：检查/检验影像帧（sql/137）
 *
 * 影像挂在**申请单**上（bizType 1-检查 2-检验 + applyId），不是挂在报告上：
 * 技师是「先拍片、后写报告」，挂报告会让刚上传的图在出报告前看不见。
 * fileUrl 是相对路径（uploads/examImage/…），必须拼 /api/ 前缀才是浏览器可访问地址
 * （后端 context-path=/api，静态资源映射在 /uploads/**）。
 */

export function examImageListByApplyId(bizType, applyId) {
    return request.get('/medicaltech/examImage/listByApplyId', {params: {bizType, applyId}})
}

/**
 * 上传单帧。
 *
 * 文件走 FormData，其余三个定位字段也一起放 FormData：
 * axios 会把 multipart 里的普通字段发成 form-data part，后端用 DTO 绑定接住。
 */
export function examImageUpload({file, bizType, applyId, modality}) {
    const form = new FormData()
    form.append('file', file)
    form.append('bizType', bizType)
    form.append('applyId', applyId)
    if (modality !== undefined && modality !== null && modality !== '') {
        form.append('modality', modality)
    }
    // 必须显式声明 multipart：request.js 的实例默认头是 application/json，
    // 留着它 axios 不会补 boundary，后端直接「请求体格式不正确」（且看不到任何堆栈线索）
    return request.post('/medicaltech/examImage/upload', form, {
        headers: {'Content-Type': 'multipart/form-data'},
    })
}

export function examImageMockImport(data) {
    return request.post('/medicaltech/examImage/mockImport', data)
}

export function examImageDeleteById(id, reason) {
    return request.delete('/medicaltech/examImage/deleteById', {params: {id, reason}})
}

/** 浏览器可直接访问的影像地址（后端在 /api 下，静态映射是 /uploads/**） */
export function examImageSrc(fileUrl) {
    if (!fileUrl) return ''
    if (fileUrl.startsWith('http') || fileUrl.startsWith('/api/')) return fileUrl
    return `/api/${fileUrl.replace(/^\/+/, '')}`
}
