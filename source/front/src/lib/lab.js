/**
 * 检验工作站（views/lab）共用的展示口径。
 *
 * 详情弹框（LaboratoryDetail）与页面里的录入/审核对话框都渲染检验结果明细，
 * 异常标志的文案与标签色必须同一口径，所以从 LaboratoryView 抽到此处共用。
 */

/**
 * 异常标志展示文案一律取后端的 abnormalFlagText。
 *
 * 不要自己用 abnormalFlag 写三目判断：abnormal_flag 列的默认值是 0，
 * 「未判定」（参考区间无法解析）与「正常」在库里同值，
 * 用 `flag !== 1 && flag !== 2 ? '正常'` 会把「不知道」显示成「正常」。
 * 缺失就显示 —，不要替后端猜一个结论。
 */
export const abnormalText = (row) => row.abnormalFlagText || '—';

export const abnormalTagType = (row) => {
  const text = abnormalText(row);
  if (text === '偏高' || text === '异常')
    return 'danger';
  if (text === '偏低')
    return 'warning';
  if (text === '正常')
    return 'success';
  return 'info'; // 未判定 / 未知
};
