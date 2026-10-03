import request from './request'

// ========== 麻精药品专册（G10；表 biz_narcotic_register，发药时后端自动登记） ==========
//
// 后端刻意**不提供**修改/删除专册的接口：专册是证据，只有「空安瓿回收登记」这一个受控写入口。
// 所以这个文件里不会有 updateNarcoticRegister / deleteNarcoticRegister —— 不是漏了。

// 麻精专册分页查询（keyword 模糊患者姓名/患者号/处方号/登记号/药品名/批号；按分类与回收状态过滤）
export function getNarcoticRegisterList(params) {
  return request.post('/narcotic/listPage', params)
}

// 专册计数（total 总登记 / pendingAmpoule 待回收空安瓿 / returnedAmpoule 已回收）
export function getNarcoticStatusCount() {
  return request.get('/narcotic/statusCount')
}

// 空安瓿回收 / 剩余液销毁登记（只补记回收字段，不改业务字段）
export function ampouleReturn(data) {
  return request.post('/narcotic/ampouleReturn', data)
}

// 处方麻精限量预检：返回违规清单，空数组 = 可以发。
// 让发药窗口在点「发药」之前就把「限几日/超几日」摆出来，而不是点完才收到一句报错。
export function checkNarcoticQuota(prescriptionId, overLimitReason) {
  return request.get('/narcotic/checkPrescription', {
    params: { prescriptionId, overLimitReason: overLimitReason || undefined },
  })
}

// 处方发药前预检（发药窗口主用）：一次拿到
//   canDispense（有无 BLOCK 级违规）
//   requiresDualCheck（要不要选复核药师）—— 麻醉药品/第一类精神药品
//   requiresAmpouleTracking（发完要不要登记空安瓿回收）
//   controlledDrugs（管制明细清单，含每档限量与处方实际天数）
//   overLimitReasonRequired（是否只差一个二类精神超量理由）
// 与 checkNarcoticQuota 的区别：后者在"全部合规"时返回空数组，窗口无法据此判断要不要选复核人。
export function precheckNarcotic(prescriptionId, overLimitReason) {
  return request.get('/narcotic/precheck', {
    params: { prescriptionId, overLimitReason: overLimitReason || undefined },
  })
}
