import request from './request'

// ==================== L3 资金账户台账（biz_fund_account / _txn） ====================
//
// 门诊余额与住院预交金统一账本：余额永远 SUM(流水)，页面不提供改账口子。
// 充值在住院账户 /charge/inpatient/account/prepay/save，抵扣在收款链里。

export const ACCOUNT_STATUS = {
    1: '正常',
    2: '冻结',
}

// 分页查询资金账户
export function getFundAccountListPage(data) {
    return request.post('/charge/fundAccount/listPage', data)
}

// 分页查询账户流水（按账户或按患者跨账户）
export function getFundTxnListPage(data) {
    return request.post('/charge/fundAccount/txnListPage', data)
}

// 某账户的全部有效流水（详情弹框一次给全）
export function listFundTxnsByAccount(accountId) {
    return request.get('/charge/fundAccount/listByAccount', {params: {accountId}})
}
