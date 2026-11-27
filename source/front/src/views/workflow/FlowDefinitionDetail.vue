<template>
  <el-dialog
      :model-value="modelValue"
      title="流程详情"
      width="1000px"
      top="8vh"
      @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading">
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="流程编码">{{ detail.flowCode }}</el-descriptions-item>
        <el-descriptions-item label="流程名称">{{ detail.flowName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusLabel }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="说明">{{ detail.flowDesc }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">节点</el-divider>
      <el-table :data="detail.nodes" border size="small">
        <el-table-column label="编码" prop="nodeCode" width="150"/>
        <el-table-column label="名称" prop="nodeName" width="160"/>
        <el-table-column label="类型" prop="nodeTypeLabel" width="100"/>
        <el-table-column label="审批人/比例" width="180">
          <template #default="{ row }">
            <span class="text-xs text-gray-600">{{ row.permissionFlag || '' }} / {{ row.nodeRatio || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="AI 配置" min-width="220">
          <template #default="{ row }">
            <span class="text-xs text-gray-600">
              {{ row.aiAction || '' }} · {{ row.aiTemplateName || '' }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">流转线</el-divider>
      <el-table :data="detail.skips" border size="small">
        <el-table-column label="起点" prop="nowNodeCode" width="150"/>
        <el-table-column label="目标" prop="nextNodeCode" width="150"/>
        <el-table-column label="类型" prop="skipType" width="110"/>
        <el-table-column label="条件" prop="conditionExpr" min-width="240" show-overflow-tooltip/>
        <el-table-column label="顺序" prop="sortNo" width="80"/>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {reactive, ref, watch} from 'vue'
import {flowDefinitionDetail} from '@/api/workflow'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: String, default: null},
})
const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const detail = reactive({nodes: [], skips: []})

watch(() => props.modelValue, async (visible) => {
  if (!visible || !props.id) return
  loading.value = true
  try {
    const res = await flowDefinitionDetail(props.id)
    if (res.code === 200 && res.data) {
      Object.assign(detail, res.data)
    }
  } catch (e) {
    console.error('加载流程详情失败', e)
  } finally {
    loading.value = false
  }
})
</script>
