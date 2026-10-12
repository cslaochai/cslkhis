/**
 * 员工档案（views/system/employee/**）共用的字典行取值口径。
 *
 * 字典数据行的形状是 { dictValue, dictLabel }（后端 sys_dict_data），
 * 与 lib/dict.js 里那份 { label, value } 的静态常量表不是一个形状，所以单独一个函数。
 * 列表列与编辑表单都要用，留在页面里会让「新增/编辑」子组件各抄一份。
 */

/** 按 dictValue 取 dictLabel；空值显示「-」，字典里查不到时原样回显（脏码值看得见） */
export function getDictLabelByValue(options, value) {
    if (!value) return '-'
    const item = options.find(item => item.dictValue === value)
    return item ? item.dictLabel : value
}
