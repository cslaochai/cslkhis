import request from './request'

// 查询医技执行列表
export function getExecutionList(params) {
  return request.post('/charge/execution/listPage', params)
}

// 开始执行
export function startExecution(data) {
  return request.post('/charge/execution/startExecution', {
    id: data.id,
    executorId: data.executorId,
    executorName: data.executorName
  })
}

// 完成执行
export function completeExecution(id) {
  return request.post('/charge/execution/completeExecution', null, { params: { id } })
}

// 审核执行
export function reviewExecution(data) {
  return request.post('/charge/execution/reviewExecution', {
    id: data.id,
    reviewerId: data.reviewerId,
    reviewerName: data.reviewerName
  })
}
