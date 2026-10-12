/**
 * 职称（sys_hospital_title 字典）文案与由它派生的默认值。
 *
 * titleMap 由页面加载一次后 prop 下发（列表列与新增/修改/模板三个组件同一份），
 * 所以这几个纯函数都以 titleMap 为首参，不在组件里各自再拉一遍字典。
 */

export const getTitleLabel = (titleMap, value) => titleMap?.[value] || value || '';

export const getTitleTagClass = (titleMap, value) => {
  const label = getTitleLabel(titleMap, value);
  if (!label)
    return '';
  if (label.includes('副主任'))
    return 'bg-orange-50 text-orange-600 ring-orange-200';
  if (label.startsWith('主任'))
    return 'bg-red-50 text-red-600 ring-red-200';
  if (label.includes('主治') || label.includes('主管'))
    return 'bg-blue-50 text-blue-600 ring-blue-200';
  return 'bg-slate-50 text-slate-500 ring-slate-200';
};

/** 专家号软联动：员工档案 isExpert 优先，职称≥副主任医师兜底；专家费带出档案 expertPrice。只给默认值，开关仍可手工改 */
export const expertDefaultOf = (titleMap, emp) => {
  if (!emp)
    return null;
  const label = getTitleLabel(titleMap, emp.title || '');
  const on = emp.isExpert === 1 || label.includes('副主任') || label.startsWith('主任') ? 1 : 0;
  return {isExpert: on, expertFee: on === 1 ? Number(emp.expertPrice) || 0 : 0};
};
