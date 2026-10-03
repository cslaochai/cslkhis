import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
export function cn(...inputs) {
    return twMerge(clsx(inputs));
}

/**
 * 本地日期字符串 yyyy-MM-dd。
 * 禁止用 new Date().toISOString().slice(0,10) —— 那是 UTC，UTC+8 上午 8 点前会取到昨天。
 */
export function localDateStr(date = new Date()) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

/**
 * 金额展示：后端 BigDecimal 可能序列化成 number 也可能成 string，统一两位小数。
 * 空值给「—」，不要给 ¥0.00 —— 那会把"没这行数据"洗成"金额为零"。
 */
export function formatMoney(value) {
    if (value === null || value === undefined || value === '') return '—';
    const num = Number(value);
    return Number.isNaN(num) ? String(value) : num.toFixed(2);
}

/**
 * 叫号展示短号：急诊存量队列 queue_no 曾是整张业务单号（JZ2026092600035，15 字符），
 * 叫号大屏 38px 大字下会把患者姓名挤出可视区，队列行/患者条徽章里也最长。
 * 写入侧已改短号「急00035」，这里把存量长号同口径折算 —— 全量急诊号仍在
 * biz_emergency.emergency_no / 挂号单上展示，不丢信息。
 */
export function shortQueueNo(queueNo) {
    const m = /^JZ\d{8}(\d{3,5})$/.exec(queueNo || '');
    return m ? '急' + m[1] : (queueNo || '');
}

/**
 * 字典码值 → 文案（兼容 dictValue/value、dictLabel/label 两种形状）。
 * 命中不了就渲染「未知(n)」，**不回落成第一个选项或任何看似合法的值** ——
 * 回落会把"码值没录对"洗成"某个正常状态"（patient_type / emp_type 都踩过这个坑）。
 */
export function dictLabelText(options, value) {
    if (value === null || value === undefined || value === '') return '—';
    const list = Array.isArray(options) ? options : [];
    const hit = list.find(o => String(o.dictValue) === String(value) || String(o.value) === String(value));
    if (!hit) return `未知(${value})`;
    return hit.dictLabel ?? hit.label ?? `未知(${value})`;
}
