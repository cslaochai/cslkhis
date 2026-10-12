/**
 * 排班日期工具：ScheduleView 面板与拆出的新增/修改排班组件共用。
 * 纯本地拼串与逐天展开，不发请求、不依赖任何响应式状态。
 */

// 本地日期格式化。别用 toISOString()：那是 UTC，北京时间每天 0~8 点会拿到「昨天」
export const fmtISO = (d) => {
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
};
/** 日期范围 → 逐天（本地时区拼串，别用 toISOString：UTC 在北京时间 0~8 点会取到昨天） */
export const batchDays = (range) => {
  const days = [];
  if (!range || range.length !== 2 || !range[0] || !range[1])
    return days;
  const start = new Date(range[0]);
  const end = new Date(range[1]);
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1))
    days.push(fmtISO(d));
  return days;
};
