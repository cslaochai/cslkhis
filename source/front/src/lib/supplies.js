/** 耗材类别码值口径（1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他 6-植入介入，未知(n) 不回落） */
export const categoryMap = {
  1: '卫生材料', 2: '注射穿刺', 3: '医用敷料', 4: '防护用品', 5: '其他', 6: '植入介入',
};
export const categoryLabel = (c) => (c == null ? '未知' : categoryMap[c] || `未知(${c})`);
