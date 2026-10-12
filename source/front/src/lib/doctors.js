// 医生站展示工具：字典码值 → 文案翻译，列表列与详情/编辑弹窗共用一份口径。

/** 按 dictValue 查文案；空值渲染「-」，字典没有该码值时原样显示，不静默吞掉 */
export const getDictLabelByValue = (options, value) => {
  if (!value) return '-'
  const item = options.find(item => item.dictValue === value)
  return item ? item.dictLabel : value
}
