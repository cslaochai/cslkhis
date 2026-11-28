<template>
  <!-- LEAF：医护侧选业务科室，平铺可搜索，条目展示「大科室 / 小科室」路径消歧 -->
  <el-select
      v-if="isFlat"
      v-model="selected"
      :multiple="multiple"
      :disabled="disabled"
      :clearable="clearable"
      :filterable="filterable"
      :collapse-tags="multiple"
      :collapse-tags-tooltip="multiple"
      :placeholder="placeholder"
      :size="size"
      :style="{ width }"
      :fit-input-width="false"
      @change="onChange"
      @clear="onClear"
  >
    <el-option
        v-for="d in flatList"
        :key="d.id"
        :label="d.fullPath || d.deptName"
        :value="d.id"
    />
  </el-select>

  <!-- TREE / ORG：床位图子树聚合、组织归属维护等需要层级钻取的场景，保留树形 -->
  <el-tree-select
      v-else
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
 * 科室下拉（统一入口）。
 *
 * 形态按场景分角色：
 *   · LEAF（默认医护侧）— 平铺可搜索列表，条目带祖先路径「内科系统 / 神经内科」，同名科室靠路径消歧；
 *   · TREE — 床位图等按子树聚合的场景，父子均可选，父节点按子树汇总；
 *   · ORG — 组织归属维护，除根节点外均可选。
 *
 * 可选节点由服务端按 mode 标注 disabled，组件只渲染出参，不自行判定片区/行政/停用等业务规则。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { loadDeptTreeOptions, flattenDeptTree } from '@/lib/deptOptions'
import { deptNodeTypeSuffix } from '@/lib/deptNodeType'

const props = defineProps({
  modelValue: { type: [String, Number, Array], default: null },
  /** 多选模式：绑定值为 id 数组 */
  multiple: { type: Boolean, default: false },
  /** LEAF-平铺仅末级业务科室 / TREE-父子均可（默认）/ ORG-组织归属维护 */
  mode: { type: String, default: 'TREE' },
  /** CURRENT-按当前人授权（默认）/ ALL-全量 */
  scope: { type: String, default: 'CURRENT' },
  /** 服务范围逗号串：1 门诊 2 医技 3 药房 4 住院 5 其他 */
  deptType: { type: String, default: null },
  /** 排除类型逗号串，命中即剔除（如普通排班排除急诊 6） */
  excludeDeptType: { type: String, default: null },
  /** true 只出对外开放挂号科室 */
  openOnly: { type: Boolean, default: null },
  /** 节点类型逗号串：1 片区 2 临床 3 医技 4 药剂 5 行政 6 辅助 7 其他 */
  nodeType: { type: String, default: null },
  placeholder: { type: String, default: '请选择科室' },
  clearable: { type: Boolean, default: true },
  filterable: { type: Boolean, default: true },
  disabled: { type: Boolean, default: false },
  size: { type: String, default: 'default' },
  width: { type: String, default: '200px' },
  /** 可选：科室统计映射 { [deptId]: { occupied, total, reserved } }，仅树形形态渲染「占用/总数」后缀 */
  stats: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'change', 'clear'])

const isFlat = computed(() => props.mode === 'LEAF')

const tree = ref([])
const flatList = ref([])
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
    const data = await loadDeptTreeOptions({
      scope: props.scope,
      deptType: props.deptType,
      excludeDeptType: props.excludeDeptType,
      nodeType: props.nodeType,
      openOnly: props.openOnly,
      mode: props.mode,
    })
    tree.value = data
    flatList.value = flattenDeptTree(data)
    if (props.stats) applyStats(tree.value)
  } catch (e) {
    console.error('加载科室树失败', e)
    tree.value = []
    flatList.value = []
  }
}

watch(() => props.modelValue, (v) => { selected.value = v })
watch(() => [props.scope, props.deptType, props.excludeDeptType, props.nodeType, props.openOnly, props.mode], load)
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
