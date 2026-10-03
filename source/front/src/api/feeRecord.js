import request from './request'

// ==================== L1 费用记账台账（biz_fee_record） ====================
//
// 记账行是应收的唯一来源，不可修改，修正只能红冲：
//   listPage（台账）→ getDetailById（含红冲链）→ book（手工补记账，幂等）→ reverse（红冲）

// 分页查询记账行
export function getFeeRecordListPage(data) {
    return request.post('/charge/feeRecord/listPage', data)
}

// 记账行详情（含红冲链）
export function getFeeRecordDetail(id) {
    return request.get('/charge/feeRecord/getDetailById', {params: {id}})
}

// 手工补记账（错漏费用的唯一录入口，幂等）
export function bookFee(data) {
    return request.post('/charge/feeRecord/book', data)
}

// 红冲：数量为空=整行冲，给了数量=部分冲减
export function reverseFee(data) {
    return request.post('/charge/feeRecord/reverse', data)
}
