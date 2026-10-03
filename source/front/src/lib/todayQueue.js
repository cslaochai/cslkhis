/**
 * 「这个患者是不是今天排给我的人」——顶栏搜索跳转工作台前的准入判定。
 *
 * ## 为什么需要这一层
 *
 * 顶栏搜索选中患者后会把人带去工作台（路由 meta.patientWorkspace，目前只有医生工作站）。
 * 但工作台只能接「今天排给自己的人」：医生站首次加载会**自动选中队列里正在就诊的患者**，
 * 而开医嘱 / 开处方 / 写病历挂的都是「当前患者」。
 * 所以「不校验就带过去」的后果不是多点一下，而是：医生搜 A → 页面选中了队列里的 C →
 * 医生没细看名字就接着开单 → **开给 C 了**。
 *
 * ## 为什么用这个接口，而不是自己写一套判断
 *
 * 队列的准入口径（哪些算「我的今日队列」）只有医生站自己知道，且以后会改。
 * 这里直接复用医生站加载队列用的同一个接口 `getTodayQueueList()`
 * （后端口径：`dept_id = 我的科室` AND `doctor_id = 我的员工号` AND `arrive_time` 在今天）。
 *
 * 注意页面上另有一个「今日就诊」标注（his-patient 的 PatientTodayVisitProvider SPI），
 * 那个的口径**与队列不同**（按 `visit_date` 过滤、状态排除退号/过号、`mine` 是「我是医生 **或** 我在这个科」），
 * 它是给「患者搜索结果分组/排序」用的，**不能拿来当接诊准入**：
 * 用它会出现「顶栏说能接、医生站队列里没这个人」的打架。
 *
 * 口径只有一处：后端 QueueServiceImpl#getTodayQueueList。后端改了这里自动跟着变。
 */
import { getTodayQueueList } from '@/api/appoint'

/**
 * 判断某患者是否在当前登录医生的今日候诊队列中
 *
 * @param {string|number} patientId 患者主档 ID
 * @returns {Promise<boolean>} true = 在队列里，可以被带去医生站接诊
 * @throws 队列接口失败时抛出，由调用方决定降级方式（**不要在此吞掉**：
 *         吞掉会让「查不到」和「不在队列」变成同一个结果，医生搜自己的患者却只看到档案，且不知道原因）
 */
export async function isInMyTodayQueue(patientId) {
    if (!patientId) return false
    const res = await getTodayQueueList({})
    const list = res?.data || []
    return list.some((q) => String(q.patientId) === String(patientId))
}
