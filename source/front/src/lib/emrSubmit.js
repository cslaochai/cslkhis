import {ElMessageBox} from 'element-plus'

/**
 * 结诊被「定稿质控」的否决项拦下时后端给的响应码（与 com.his.clinical.constant.EmrSubmitConst 对齐）。
 * 它不是错误，是「等医生决定」：message 里已经带了逐条缺陷清单。
 */
export const EMR_SUBMIT_VETO_CODE = 409;

/**
 * 否决项拦截的弹框：「去修改」回去改病历（本次不结诊），「仍要结诊」由调用方带 forceSubmit 重发。
 *
 * @param error 结诊请求 reject 出来的错误
 * @returns {'force'|'cancel'|null} force=知情放行；cancel=去修改（不再提示）；null=不是否决项拦截，按普通失败处理
 */
export async function handleEmrSubmitVeto(error) {
  if (error?.code !== EMR_SUBMIT_VETO_CODE) {
    return null;
  }
  try {
    await ElMessageBox.confirm(error.message || '病历存在否决项缺陷', '病历存在否决项缺陷', {
      confirmButtonText: '仍要结诊',
      cancelButtonText: '去修改',
      type: 'warning',
    });
  } catch {
    return 'cancel';
  }
  return 'force';
}
