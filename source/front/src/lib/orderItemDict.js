/**
 * 医嘱项目字典（药品 / 检查 / 检验）—— 行内项目选择器的共用口径。
 *
 * 为什么抽出来：住院医嘱开立弹窗（`components/his/InpatientOrderWorkspace.vue`）与
 * 「全院组套模板」页都要在同一张明细行上做「选类别 → 选项目 → 带出编码/规格/单位/单价」，
 * 这四件事写两遍必然漂移（一边带出了规格、另一边没带出 → 组套套用出来的医嘱少了计费锚点）。
 *
 * ⚠ 三条口径（与后端 InpatientOrderItemRules 对齐）：
 *  1. 只有药品(1) / 检查(2) / 检验(3) 有字典；治疗/护理/手术/输血/监护/其他/临床营养走手输。
 *  2. 编码必须一起带出 —— 计费与审方靠 itemCode 对齐，只带名称等于放弃四核对锚点。
 *  3. 单价只做默认带出、允许改（医嘱价是开立时快照，模板里存的是参考价）。
 *
 * 字典是模块级单例：一个会话里只拉一次，多页面共用，不重复打接口。
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDrugSelectList, getInspectionSelectList, getLaboratorySelectList } from '@/api/system'

/** 医嘱类别码（与后端 his_order_class 字典一致） */
export const ORDER_CLASS = {
  DRUG: 1,
  INSPECTION: 2,
  LABORATORY: 3,
  TREATMENT: 4,
  NURSING: 5,
  OPERATION: 6,
  TRANSFUSION: 7,
  MONITOR: 8,
  OTHER: 9,
  CLINICAL_NUTRITION: 10,
}

export const ORDER_CLASS_OPTIONS = [
  { value: 1, label: '药品' },
  { value: 2, label: '检查' },
  { value: 3, label: '检验' },
  { value: 4, label: '治疗' },
  { value: 5, label: '护理' },
  { value: 6, label: '手术' },
  { value: 7, label: '输血' },
  { value: 8, label: '监护' },
  { value: 9, label: '其他' },
  { value: 10, label: '临床营养' },
]

/** 类别码 → 文案（未知码渲染「未知(n)」，不回落成看似合法的值） */
export function orderClassText(v) {
  const hit = ORDER_CLASS_OPTIONS.find((o) => o.value === v)
  return hit ? hit.label : `未知(${v})`
}

const drugOptions = ref([])
const inspectionOptions = ref([])
const laboratoryOptions = ref([])
const dictLoading = ref(false)
const dictLoaded = ref(false)

/** 按类别取候选（非字典类返回空数组 → 调用方据此退回手输） */
export function optionsOfClass(orderClass) {
  if (orderClass === ORDER_CLASS.DRUG) return drugOptions.value
  if (orderClass === ORDER_CLASS.INSPECTION) return inspectionOptions.value
  if (orderClass === ORDER_CLASS.LABORATORY) return laboratoryOptions.value
  return []
}

/**
 * 加载三个项目字典（幂等：已加载或正在加载都不重复发请求）。
 * 失败时**不退路**：返回空候选，调用方退回手输并提示，不挡业务。
 */
export function loadOrderItemDicts() {
  if (dictLoaded.value || dictLoading.value) return Promise.resolve()
  dictLoading.value = true
  return Promise.all([
    getDrugSelectList({}),           // 不传 drugType = 全部类型（西药/中成药/中药饮片）
    getInspectionSelectList(),
    getLaboratorySelectList(),
  ])
    .then(([drugRes, inspRes, labRes]) => {
      drugOptions.value = drugRes?.data || []
      inspectionOptions.value = inspRes?.data || []
      laboratoryOptions.value = labRes?.data || []
      dictLoaded.value = true
    })
    .catch((e) => {
      console.error('加载医嘱项目字典失败，本次退化为手工录入', e)
      ElMessage.warning('医嘱项目字典加载失败，可手工录入项目名称')
    })
    .finally(() => {
      dictLoading.value = false
    })
}

/**
 * 选中字典项 → 回填明细行（val 是选中项的 id 字符串）。
 * 清空选择时名称与编码一起清掉，避免留下「有名称没编码」的孤儿行。
 */
export function pickDictItem(row, val, orderClass) {
  if (!row) return
  if (!val) {
    row.itemName = ''
    row.itemCode = ''
    return
  }
  const hit = optionsOfClass(orderClass).find((o) => String(o.id) === String(val))
  if (!hit) return
  row.itemName = hit.drugName || hit.itemName || ''
  row.itemCode = hit.drugCode || hit.itemCode || ''
  row.spec = hit.specification || hit.spec || ''
  row.unit = hit.unit || ''
  const price = hit.retailPrice ?? hit.price
  if (price != null) row.price = Number(price)
}

/** 换类别：清掉上一类带出的字段，避免「药品类别 + 检验项目名」的脏行 */
export function resetRowOnClassChange(row) {
  if (!row) return
  row.dictId = ''
  row.itemCode = ''
  row.itemName = ''
  row.spec = ''
  row.unit = ''
  row.price = undefined
}

/** 按编码回查字典项（编辑回显时把下拉选中态补回去，否则下拉会显示空白或原始 id） */
export function findDictByCode(orderClass, itemCode) {
  if (!itemCode) return null
  return optionsOfClass(orderClass).find((o) => (o.drugCode || o.itemCode) === itemCode) || null
}

/** 一行明细的空壳（新增行 / 重置表单用） */
export function blankOrderItem(orderClass = ORDER_CLASS.DRUG) {
  return {
    orderClass,
    dictId: '',
    itemCode: '',
    itemName: '',
    spec: '',
    unit: '',
    dosage: '',
    dosageUnit: '',
    route: '',
    frequency: '',
    quantity: 1,
    price: undefined,
  }
}
