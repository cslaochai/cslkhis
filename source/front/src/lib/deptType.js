/**
 * 科室类型（字典 his_dept_type）的展示口径 —— 列表列与详情/编辑弹框共用一份，
 * 避免「列表显示 3 个类型、详情只显示 1 个」这类多值渲染漂移。
 */
import {dictLabelText} from './utils';

/** 兼容单值与逗号分隔多值；命中不到字典出「未知(n)」，不回落成「其他」 */
export function deptTypeLabel(options, type) {
    const list = Array.isArray(options) ? options : [];
    if (typeof type === 'string' && type.includes(',')) {
        return type.split(',').map(t => dictLabelText(list, Number(t))).join('、');
    }
    return dictLabelText(list, typeof type === 'string' ? Number(type) : type);
}

/** 后端 deptType 列存逗号分隔字符串，表单的多选控件要数组 */
export function deptTypeToArray(value) {
    if (typeof value === 'string') {
        return value.split(',').map(Number).filter(n => !isNaN(n));
    }
    return value == null || value === '' ? [] : [Number(value)];
}
