/**
 * 叫车表单的候选取数入口（调度台与叫车浮窗共用一份）。
 *
 * 站点名称、品类、单箱基准重量、优先级全部来自后端 /iot/robotStation/options，
 * 前端不再硬编码 —— 站点改名或新增病区，改的是站点主数据，不是这里。
 */
import {robotStationOptions} from '@/api/robotStation'

/** 优先级只有三档，查询条件下拉这类纯展示场景可直接用；表单仍以后端 options 为准 */
export const PRIORITY_OPTIONS = [
  {value: 1, label: '常规'},
  {value: 2, label: '批量补货'},
  {value: 3, label: '急送'},
]

let cache = null

/** 取叫车候选（带 Promise 缓存）；传 force 强制刷新 */
export function loadRobotOptions(force = false) {
  if (!cache || force) {
    cache = robotStationOptions()
        .then((res) => res?.data || {})
        .catch((e) => {
          cache = null
          throw e
        })
  }
  return cache
}
