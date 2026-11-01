// 签名验签页/签名证书页与签名详情/证书详情/签发证书弹框组件共用的展示辅助（各处不再各写一份副本）

export const num = (v) => Number(v ?? 0);

export const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

// 时间来源标签：只有 TimeSource.trusted() 那一档才配绿；本机时钟必须显眼
export const timeSourceTag = (src) => (src === 3 ? 'success' : src === 2 ? 'warning' : 'info');

// CA 申请状态标签：已签发/已批准待签发是往前走，待审核要盯，提交失败与被驳回要处理
export const caApplyTagType = (status) => {
  if (status === 'ISSUED' || status === 'APPROVED') return 'success';
  if (status === 'PENDING') return 'warning';
  if (status === 'FAILED' || status === 'REJECTED') return 'danger';
  return 'info';
};
