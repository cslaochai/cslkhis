// 绩效核算展示工具：金额格式化与结余配色，页面列表与核算弹窗共用一份口径。

/** 金额 → 「¥1,234」中文千分位（空值按 0 计） */
export const money = (v) => '¥' + Number(v || 0).toLocaleString('zh-CN');

/** 结余 → 文字颜色类（正结余绿 / 负结余红） */
export const surplusColor = (v) => (Number(v || 0) >= 0 ? 'text-green-600' : 'text-red-600');
