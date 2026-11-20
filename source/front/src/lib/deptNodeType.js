/**
 * 科室节点类型（sys_department.node_type）的前端选项与判定口径。
 *
 * 节点类型回答「这个节点在组织树上是什么角色」，与「科室类型 dept_type（门诊/住院/急诊…服务范围）」
 * 是两个正交维度，禁止互相替代：
 *   · 列表/详情的展示文案以后端 `nodeTypeText` 为准（枚举 DeptNodeTypeEnum 翻译）；
 *   · 本文件的 LABEL 只供下拉控件渲染选项（表单必须能选，不能只显示）。
 *
 * 码值与后端 DeptNodeTypeEnum 逐字对齐，改一处必须同步另一处。
 */
export const DEPT_NODE_TYPE_OPTIONS = [
  {value: '0', label: '医疗机构'},
  {value: '1', label: '管理片区'},
  {value: '2', label: '临床科室'},
  {value: '3', label: '医技科室'},
  {value: '4', label: '药剂科室'},
  {value: '5', label: '行政后勤'},
  {value: '6', label: '医疗辅助'},
  {value: '7', label: '其他'},
]

/** 可作为业务科室（排班/开诊/收治）的节点类型：片区与行政后勤不算，见后端 isBusinessDept */
export const BUSINESS_NODE_TYPES = ['2', '3', '4', '6', '7']

/** 管理片区：只做组织归集与审核权落点，不能出现在排班单元/开诊/收治下拉 */
export const isAreaNode = (code) => String(code ?? '') === '1'

/** 是否是业务科室（未回填的历史行按业务科室放行，避免升级瞬间全空） */
export const isBusinessNode = (code) => {
  const c = String(code ?? '')
  return c === '' || BUSINESS_NODE_TYPES.includes(c)
}

/** 有服务范围（科室类型）的节点：片区/机构/行政后勤 不挂门诊/住院/急诊类型 */
export const hasServiceScope = (code) => BUSINESS_NODE_TYPES.includes(String(code ?? ''))

export function deptNodeTypeLabel(code) {
  const hit = DEPT_NODE_TYPE_OPTIONS.find(o => o.value === String(code ?? ''))
  return hit ? hit.label : ''
}

/** 下拉选项里的层级标注：片区必须显式标出来，否则它就是「一个叫内科系统的科室」 */
export function deptNodeTypeSuffix(code) {
  return isAreaNode(code) ? '（管理片区）' : ''
}
