/**
 * 病危/病重通知（菜单 319）共享口径 —— 台账页与其拆出的弹框组件共用，
 * 状态枚举/着色/时间格式化只在这里写一份，避免页面与组件漂移。
 */
import {ElMessage} from 'element-plus'

export const NS = {DRAFT: 1, ISSUED: 2, ACKED: 3, VOID: 4}

export const fmt = (t) => (t ? String(t).slice(0, 19) : '—')

export const statusTag = (s) => (s === NS.DRAFT ? 'warning' : s === NS.ISSUED ? 'primary' : s === NS.ACKED ? 'success' : 'info')

export const signTagText = (s) => (Number(s) === 1 ? '已签名' : Number(s) === 2 ? '签名已作废' : '未签名')

/** 后端统一 code/message 包装：非 200 与异常都在这里弹错，调用方只拿成功结果 */
export const gate = async (p, msg) => {
  try {
    const res = await p
    if (res.code === 200) return res
    ElMessage.error(res.message || msg)
    return null
  } catch (e) {
    
    return null
  }
}
