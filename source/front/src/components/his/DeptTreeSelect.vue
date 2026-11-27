<template>
  <el-tree-select
      v-model="selected"
      :data="tree"
      :props="treeProps"
      node-key="id"
      :disabled="disabled"
      :clearable="clearable"
      :filterable="filterable"
      :multiple="multiple"
      :collapse-tags="multiple"
      :collapse-tags-tooltip="multiple"
      :placeholder="placeholder"
      :size="size"
      :style="{ width }"
      check-strictly
      :fit-input-width="false"
      @change="onChange"
      @clear="onClear"
  >
    <template #default="{ data }">
      <span :title="data.disabledReason || data.deptName">
        {{ data.deptName }}{{ deptNodeTypeSuffix(data.nodeType) }}
        <span v-if="stats" class="text-xs font-normal text-slate-400">（{{ data._occupied ?? 0 }}/{{ data._total ?? 0 }}）</span>
      </span>
    </template>
  </el-tree-select>
</template>

<script setup>
/**
 * 科室树形下拉（统一入口）。
 *
 * 可选节点由服务端按 mode 标注 disabled：
 *   · LEAF 仅末级业务科室；· TREE 父子均可（父节点按子树聚合）；· ORG 组织归属维护（除根外均可选）。
 * 组件只渲染出参，不自行判定片区/行政/停用等业务规则。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { loadDeptTreeOptions } from '@/lib/deptOptions'
import { deptNodeTypeSuffix } from '@/lib/deptNodeType'

const props = defineProps({
  modelValue: { type: [String, Number, Array], default: null },
  /** 多选模式：绑定值为 id 数组 */
  multiple: { type: Boolean, default: false },
  /** LEAF-仅末级业务科室 / TREE-父子均可（默认）/ ORG-组织归属维护 */
  mode: { type: String, default: 'TREE' },
  /** CURRENT-按当前人授权（默认）/ ALL-全量 */
  scope: { type: String, default: 'CURRENT' },
  /** 服务范围逗号串：1 门诊 2 医技 3 药房 4 住院 5 其他 */
  deptType: { type: String, default: null },
  /** 节点类型逗号串：1 片区 2 临床 3 医技 4 药剂 5 行政 6 辅助 7 其他 */
  nodeType: { type: String, default: null },
  placeholder: { type: String, default: '请选择科室' },
  clearable: { type: Boolean, default: true },
  filterable: { type: Boolean, default: true },
  disabled: { type: Boolean, default: false },
  size: { type: String, default: 'default' },
  width: { type: String, default: '200px' },
  /** 可选：科室统计映射 { [deptId]: { occupied, total, reserved } }，传入后节点尾部渲染「占用/总数」，父节点按子树求和 */
  stats: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'change', 'clear'])

const tree = ref([])
const selected = ref(props.modelValue)

const treeProps = { label: 'deptName', value: 'id', children: 'children', disabled: 'disabled' }

const statMap = computed(() => {
  const m = new Map()
  if (props.stats) {
    for (const [k, v] of Object.entries(props.stats)) m.set(String(k), v)
  }
  return m
})

/** 把叶子统计沿树向上汇总：父节点 occupied/total = 自身 + 全部后代之和 */
const applyStats = (list) => {
  let occupied = 0
  let total = 0
  for (const node of list || []) {
    const child = applyStats(node.children)
    const self = statMap.value.get(String(node.id))
    const o = (self?.occupied ?? 0) + child.occupied
    const t = (self?.total ?? 0) + child.total
    node._occupied = o
    node._total = t
    occupied += o
    total += t
  }
  return { occupied, total }
}

const load = async () => {
  try {
    tree.value = await loadDeptTreeOptions({
      scope: props.scope,
      deptType: props.deptType,
      nodeType: props.nodeType,
      mode: props.mode,
    })
    if (props.stats) applyStats(tree.value)
  } catch (e) {
    console.error('加载科室树失败', e)
    tree.value = []
  }
}

watch(() => props.modelValue, (v) => { selected.value = v })
watch(() => [props.scope, props.deptType, props.nodeType, props.mode], load)
watch(() => props.stats, () => { if (props.stats) applyStats(tree.value) }, { deep: true })

const onChange = (v) => {
  selected.value = v
  emit('update:modelValue', v)
  emit('change', v)
}
const onClear = () => {
  emit('update:modelValue', props.multiple ? [] : null)
  emit('clear')
}

onMounted(load)
</script>
