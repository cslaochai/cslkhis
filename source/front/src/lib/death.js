/**
 * 死亡证明 / 死亡登记（views/death）共用工具：
 * 页面与拆出去的详情组件（DeathCertificateDetail / DeathCertificateRegisterDetail）共用这份，
 * 避免两侧各自抄一遍、口径漂移。
 */
import {ElMessage} from 'element-plus'

/**
 * request.js 对业务失败（code!==200）走的是 reject，而不是把 res 返回回来，
 * 所以 `if (res.code === 200) … else ElMessage.error(…)` 的 else 分支永远跑不到 ——
 * 现象是「点了按钮没反应」。这一页的卖点全是闸门（未办死亡离院不许签发、非疾病死亡未报公安不许确认、
 * 已上报冻结不许重报），被拦时必须把后端原文弹给用户，故所有调用一律走这里兜住。
 */
export const gate = async (promise, fallback) => {
  try {
    const res = await promise
    if (res && res.code === 200) return res
    ElMessage.error(String((res && res.message) || fallback))
    return null
  } catch (e) {
    
    return null
  }
}

export const fmt = (t) => (t ? String(t).slice(0, 19) : '—')
export const fmtDay = (t) => (t ? String(t).slice(0, 10) : '—')
