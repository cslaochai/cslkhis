/**
 * 数据字典前端缓存（MSS 版，抄 HIS lib/dict-cache.js 的所需部分）
 * 只缓存字典类型常量；字典数据每次都从后端获取（getDictDataMapList 已把数组还原成 { dictType: [...] }）
 */
import {getDictDataList, getDictDataMapList} from '@/api/system'

export const DICT_TYPE = {
  SYS_HOSPITAL_TITLE: 'sys_hospital_title', // 职称
  HOSPITAL_POSITION: 'hospital_position',   // 职位
  HIS_EDUCATION: 'his_education',           // 学历
  EMP_CERT_TYPE: 'his_emp_cert_type',       // 证书类型
  EMP_CERT_ORG: 'his_emp_cert_org',         // 发证机关
}

/** 获取字典数据（单个） */
export async function loadDictDataList(dictType) {
  try {
    const res = await getDictDataList(dictType)
    return res.data || []
  } catch (error) {
    console.error(`加载字典数据失败: ${dictType}`, error)
    return []
  }
}

/**
 * 获取字典数据（多个，返回 Map 结构 { dictType: [dictData], ... }）
 * 后端限制单次最多 5 个 dictType，按 5 个一批切开再合并
 */
export async function loadDictDataMap(dictTypes) {
  try {
    const types = String(dictTypes || '').split(',').map(s => s.trim()).filter(Boolean)
    const map = {}
    for (let i = 0; i < types.length; i += 5) {
      const res = await getDictDataMapList(types.slice(i, i + 5).join(','))
      Object.assign(map, res.data || {})
    }
    return map
  } catch (error) {
    console.error(`批量加载字典数据失败: ${dictTypes}`, error)
    return {}
  }
}
