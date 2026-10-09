<script setup lang="js">
import {computed} from 'vue'
import {Plus, Delete} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {type: Array, default: () => []},
  deptList: {type: Array, default: () => []},
  roleList: {type: Array, default: () => []},
  disabled: {type: Boolean, default: false},
})
const emit = defineEmits(['update:modelValue'])

const rows = computed(() => props.modelValue || [])

const roleNameOf = (code) => props.roleList.find((r) => r.roleCode === code)?.roleName || code
const deptNameOf = (id) => deptOptions.value.find((d) => d.id === id)?.label || id

const patch = (index, changes) => {
  emit('update:modelValue', rows.value.map((row, i) => (i === index ? {...row, ...changes} : row)))
}

// 科室树摊平成带父级前缀的选项：表格单元里放二级下拉（树选）点不开也看不清层级
const deptOptions = computed(() => {
  const out = []
  const walk = (list, prefix) => {
    (list || []).forEach((dept) => {
      const label = prefix ? `${prefix} / ${dept.deptName}` : dept.deptName
      out.push({id: String(dept.id), label})
      if (dept.children && dept.children.length > 0) walk(dept.children, label)
    })
  }
  walk(props.deptList, '')
  return out
})

// 主岗位 = 这个人的唯一默认落点（登录落点、号源与名册都读它），一人只有一条。
// 在列表行内用单选框勾选（全表只认一条），工具栏的 disabled 下拉只做联动展示、不可选。
const primaryIndex = computed(() => rows.value.findIndex((r) => r.isPrimary === 1))
const primaryLabel = computed(() => {
  const i = primaryIndex.value
  if (i < 0) return ''
  const row = rows.value[i]
  if (!row.deptId || !row.roleCode) return ''
  return `${deptNameOf(row.deptId)} · ${roleNameOf(row.roleCode)}`
})
const setPrimary = (index) => {
  emit('update:modelValue', rows.value.map((row, i) => ({...row, isPrimary: i === index ? 1 : 0})))
}

// 带全局下标渲染：删除/勾选/行内改都按 __index 定位
const indexed = computed(() => rows.value.map((row, index) => ({...row, __index: index})))

const addRow = () => {
  // 全表只有一条主岗位：第 1 行默认是主岗位，第 2 行起默认 0，所以「加行」不会把人自己的主科室抢走
  const isPrimary = rows.value.length === 0 ? 1 : 0
  emit('update:modelValue', [...rows.value, {roleCode: '', deptId: '', isPrimary, effectiveDate: '', expireDate: ''}])
}

const removeRow = (index) => {
  const wasPrimary = rows.value[index]?.isPrimary === 1
  const rest = rows.value.filter((_, i) => i !== index)
  if (wasPrimary && rest.length > 0 && !rest.some((r) => r.isPrimary === 1)) {
    rest[0] = {...rest[0], isPrimary: 1}
  }
  emit('update:modelValue', rest)
}
</script>

<template>
  <div class="w-full">
    <div class="mb-2 flex flex-wrap items-center gap-3">
      <el-button v-if="!disabled" :icon="Plus" type="primary" plain size="small" @click="addRow">添加岗位</el-button>
      <span v-if="rows.length > 0" class="text-sm text-slate-500">共 {{ rows.length }} 个岗位</span>
      <template v-if="rows.length > 0">
        <span class="text-sm text-slate-600"><span class="text-red-500">*</span> 主岗位</span>
        <el-select :model-value="primaryLabel || undefined" placeholder="请选择主岗位" disabled class="!w-[420px]">
          <el-option v-if="primaryLabel" :label="primaryLabel" :value="primaryLabel"/>
        </el-select>
      </template>
    </div>
    <el-table :data="indexed" size="small" border max-height="260" class="mb-2 post-table">
      <el-table-column width="320">
        <template #header><span class="text-red-500">*</span> 科室</template>
        <template #default="{ row }">
          <el-select
              :model-value="row.deptId"
              placeholder="请选择科室"
              filterable
              clearable
              :disabled="disabled"
              class="w-full"
              @update:model-value="(v) => patch(row.__index, {deptId: v ? String(v) : ''})"
          >
            <el-option v-for="dept in deptOptions" :key="dept.id" :label="dept.label" :value="dept.id"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column width="150">
        <template #header><span class="text-red-500">*</span> 角色</template>
        <template #default="{ row }">
          <el-select
              :model-value="row.roleCode"
              placeholder="请选择角色"
              filterable
              clearable
              :disabled="disabled"
              class="w-full"
              @update:model-value="(v) => patch(row.__index, {roleCode: v || ''})"
          >
            <el-option v-for="role in roleList" :key="role.roleCode" :label="role.roleName" :value="role.roleCode"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column width="150">
        <template #header>生效日期</template>
        <template #default="{ row }">
          <el-date-picker
              :model-value="row.effectiveDate || ''"
              type="date"
              placeholder="保存即生效"
              value-format="YYYY-MM-DD"
              :disabled="disabled"
              class="!w-full"
              @update:model-value="(v) => patch(row.__index, {effectiveDate: v || ''})"
          />
        </template>
      </el-table-column>
      <el-table-column width="150">
        <template #header>失效日期</template>
        <template #default="{ row }">
          <div class="flex flex-col items-start gap-1">
            <el-date-picker
                :model-value="row.expireDate || ''"
                type="date"
                placeholder="长期有效"
                value-format="YYYY-MM-DD"
                :disabled="disabled"
                class="!w-full"
                @update:model-value="(v) => patch(row.__index, {expireDate: v || ''})"
            />
            <!-- 状态取自后端派生值（sql/118 起不落列）：改日期只是预览，保存重载后才会翻成已失效 -->
            <el-tag v-if="row.postStatus === 2" type="danger" size="small" effect="light">已失效</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column width="80" align="center">
        <template #header><span class="text-red-500">*</span> 主岗位</template>
        <template #default="{ row }">
          <el-radio
              :model-value="primaryIndex"
              :value="row.__index"
              :disabled="disabled"
              class="!h-auto"
              @update:model-value="setPrimary(row.__index)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="80" align="center">
        <template #default="{ row }">
          <el-button v-if="!disabled" type="danger" link :icon="Delete" @click="removeRow(row.__index)">删除</el-button>
          <span v-else class="text-slate-400">-</span>
        </template>
      </el-table-column>
      <template #empty>
        <span class="text-sm text-slate-400">还没有配置岗位，请点上方「添加岗位」</span>
      </template>
    </el-table>
  </div>
</template>
