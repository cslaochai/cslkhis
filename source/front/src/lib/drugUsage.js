/**
 * 给药途径 / 用药频次 / 剂量单位 —— 口径单点（sql/142 起改为「字典 + 兜底」）
 *
 * 历史：这三类此前是纯前端硬编码（本文件里的常量），库里查不到字典，于是医生站与护士站
 * 各按各的常量渲染 —— 库里真实出现过「静脉泵入」而常量里没有，两侧看到的值从此对不上。
 * 2026-09-26（sql/142）把它们落进 sys_dict_data（his_order_route / his_order_freq / his_dose_unit），
 * 由「住院业务 → 医嘱基础字典」维护；本文件改成从字典读，**读不到才用兜底常量**。
 *
 * ⚠ 三处不要各写一份：本文件、门诊 `lib/drugFrequency.js`（历史遗留，值域与此处一致）、
 *    任何页面内的 `const ROUTES = [...]`。新增码值请走字典页，不要再往这里加。
 *
 * 频次码值取临床通用的英文缩写（医嘱单上就是这么写的），不是数字码：
 * 后端 `biz_inpatient_order.frequency` 是 VARCHAR，存的是这串缩写本身。
 * 给药途径存中文（历史医嘱 route 列就是中文，改成码会让存量数据渲染成「未知」）。
 */
import { ref } from 'vue'
import { getDictDataMapList } from '@/api/system'

/** 医嘱基础字典类型（与后端 OrderDictTypes 一一对应，改一处必须改另一处） */
export const ORDER_DICT_TYPE = {
  ROUTE: 'his_order_route',
  FREQ: 'his_order_freq',
  DOSE_UNIT: 'his_dose_unit',
}

/**
 * 兜底常量：字典接口挂了 / 还没铺数据时，下拉不能变成空 ——
 * 医生照样要开医嘱，只是候选少了。值与 sql/142 预置数据一致。
 */
const FALLBACK_ROUTE = [
  '口服', '静滴', '静推', '肌注', '皮下注射', '皮内注射', '静脉泵入',
  '外用', '舌下含服', '雾化吸入', '直肠给药', '滴眼', '滴鼻', '鼻饲', '其他',
]

const FALLBACK_FREQ = [
  { value: 'qd', label: 'qd 每日一次' },
  { value: 'bid', label: 'bid 每日两次' },
  { value: 'tid', label: 'tid 每日三次' },
  { value: 'qid', label: 'qid 每日四次' },
  { value: 'q8h', label: 'q8h 每8小时' },
  { value: 'q12h', label: 'q12h 每12小时' },
  { value: 'q6h', label: 'q6h 每6小时' },
  { value: 'qod', label: 'qod 隔日一次' },
  { value: 'qw', label: 'qw 每周一次' },
  { value: 'prn', label: 'prn 必要时' },
  { value: 'st', label: 'st 立即一次' },
  { value: 'hs', label: 'hs 睡前' },
  { value: 'am', label: 'am 上午' },
  { value: 'pm', label: 'pm 下午' },
]

const FALLBACK_DOSE_UNIT = [
  'g', 'mg', 'μg', 'ml', 'L', 'IU', 'U', '片', '粒', '支', '袋', '瓶', '滴', '喷', '单位',
]

/** 下拉候选（响应式：字典到位后自动更新，模板里直接用名字即可，ref 会自动解包） */
export const routeOptions = ref([...FALLBACK_ROUTE])
export const frequencyOptions = ref([...FALLBACK_FREQ])
export const dosageUnitOptions = ref([...FALLBACK_DOSE_UNIT])

/** 字典是否已就位（想提示「当前是内置候选」的页面可读它） */
export const usageDictLoaded = ref(false)

let loadingPromise = null

/**
 * 用字典覆盖兜底常量（幂等：并发只发一次请求）。
 *
 * 用到这三个下拉的页面（医生站开立弹窗、组套模板页）各自调一次即可；
 * 失败时**保留兜底常量**并静默降级 —— 字典拉不到不是医生的错，不能挡开立。
 */
export function loadOrderUsageOptions() {
  if (loadingPromise) return loadingPromise
  loadingPromise = (async () => {
    try {
      const res = await getDictDataMapList(
        `${ORDER_DICT_TYPE.ROUTE},${ORDER_DICT_TYPE.FREQ},${ORDER_DICT_TYPE.DOSE_UNIT}`
      )
      const map = res?.data || {}
      // 只取启用项：停用项不进下拉，但历史医嘱里的值仍要能渲染出文案
      const enabled = (type) =>
        (map[type] || []).filter((d) => d.status !== 0 && d.dictValue != null && d.dictValue !== '')

      const routes = enabled(ORDER_DICT_TYPE.ROUTE).map((d) => String(d.dictValue))
      const freqs = enabled(ORDER_DICT_TYPE.FREQ).map((d) => ({
        value: String(d.dictValue),
        label: d.dictLabel || String(d.dictValue),
      }))
      const units = enabled(ORDER_DICT_TYPE.DOSE_UNIT).map((d) => String(d.dictValue))
      // 空结果不覆盖兜底：字典还没铺数据时宁可给内置候选，也不给一个空下拉
      if (routes.length) routeOptions.value = routes
      if (freqs.length) frequencyOptions.value = freqs
      if (units.length) dosageUnitOptions.value = units
      usageDictLoaded.value = true
    } catch (e) {
      console.error('加载医嘱基础字典失败，本次沿用内置候选值', e)
    } finally {
      loadingPromise = null
    }
  })()
  return loadingPromise
}

/** 频次缩写 → 展示文案。命中不到渲染「未知(code)」，不回落成看似合法的值 */
export function frequencyText(v) {
  if (!v) return '—'
  const hit = (frequencyOptions.value || []).find((o) => (typeof o === 'string' ? o === v : o.value === v))
  return hit ? v : `未知(${v})`
}

/** 途径展示文案（值本身就是中文，兜底防空） */
export function routeText(v) {
  return v || '—'
}

/** 剂量单位展示文案 */
export function dosageUnitText(v) {
  return v || '—'
}
