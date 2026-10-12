// 今日就诊（views/today-visits/）页面与其拆出子组件共用的展示辅助
// —— 主列表列与就诊详情抽屉读同一种格式，副本各写一份必然漂移，收口到这里。
import {OPD_LOG_STATUS, statusOf, unknownOf} from '@/lib/statusColor';

export const fmt = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-');
export const duration = (m) => (m == null ? '-' : m + ' 分');
/** 就诊状态：优先后端推导的 logStatus；推导不出来时用 queueStatus 渲染「未知(n)」，绝不回落 */
export const logStatusStyle = (row) => row.logStatus != null ? statusOf(OPD_LOG_STATUS, row.logStatus) : unknownOf(row.queueStatus);
