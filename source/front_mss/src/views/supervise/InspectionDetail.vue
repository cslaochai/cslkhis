<template>
  <el-dialog
    :model-value="modelValue"
    title="飞检任务详情"
    width="760px"
    append-to-body
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <div v-if="detail" class="space-y-3">
      <div class="grid grid-cols-2 gap-x-6 gap-y-2 text-[13px]">
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">任务单号</span><span>{{ detail.task.inspectionNo }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">任务状态</span><el-tag size="small" :type="statusType(detail.task.status)">{{ detail.task.status }}</el-tag></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">任务名称</span><span>{{ detail.task.title }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">检查类型</span><span>{{ detail.task.inspectType }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">被查机构</span><span>{{ detail.task.targetOrg }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">计划时间</span><span>{{ detail.task.planStart }} ~ {{ detail.task.planEnd }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">带队人</span><span>{{ detail.task.leader }}</span></div>
        <div class="flex"><span class="text-gray-500 w-20 shrink-0">检查范围</span><span>{{ detail.task.scopeDesc }}</span></div>
      </div>

      <div v-if="detail.task.resultSummary" class="text-[13px]">
        <div class="text-gray-500 mb-1">检查结果小结</div>
        <div class="bg-gray-50 rounded p-2">{{ detail.task.resultSummary }}</div>
      </div>

      <div>
        <div class="flex items-center justify-between mb-2">
          <div class="font-semibold text-[14px] text-[#0B3B66]">
            发现问题明细
            <el-tag size="small" type="danger" class="ml-2">涉及金额合计 {{ fmt(detail.totalAmount) }} 元</el-tag>
          </div>
          <el-button v-if="detail.task.status === '检查中'" size="small" type="primary" @click="openAdd">录入问题</el-button>
        </div>
        <el-table :data="detail.items" size="small">
          <el-table-column prop="problemType" label="问题类型" width="120" />
          <el-table-column prop="problemDesc" label="问题描述" min-width="160" show-overflow-tooltip />
          <el-table-column prop="evidence" label="证据说明" min-width="140" show-overflow-tooltip />
          <el-table-column label="涉及金额" align="right" width="110">
            <template #default="{ row }">{{ fmt(row.involvedAmount) }}</template>
          </el-table-column>
          <el-table-column prop="dispose" label="处置方式" width="90" align="center" />
          <el-table-column v-if="detail.task.status === '检查中'" label="操作" width="70" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" @click="onDeleteItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 录入问题 -->
    <el-dialog v-model="addVisible" title="录入发现问题" width="560px" append-to-body>
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="90px">
        <el-form-item label="问题类型" prop="problemType">
          <el-select v-model="itemForm.problemType" style="width:100%">
            <el-option label="重复收费" value="重复收费" />
            <el-option label="超限价收费" value="超限价收费" />
            <el-option label="分解住院" value="分解住院" />
            <el-option label="挂床住院" value="挂床住院" />
            <el-option label="虚记费用" value="虚记费用" />
            <el-option label="串换项目" value="串换项目" />
            <el-option label="丙类项目入统筹" value="丙类项目入统筹" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="problemDesc">
          <el-input v-model="itemForm.problemDesc" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="证据说明">
          <el-input v-model="itemForm.evidence" type="textarea" :rows="2" placeholder="票据、病程记录、明细比对等" />
        </el-form-item>
        <el-form-item label="涉及金额">
          <el-input-number v-model="itemForm.involvedAmount" :min="0" :precision="2" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="处置方式" prop="dispose">
          <el-radio-group v-model="itemForm.dispose">
            <el-radio value="追回">追回</el-radio>
            <el-radio value="整改">整改</el-radio>
            <el-radio value="移交稽核">移交稽核</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onAddItem">保存</el-button>
      </template>
    </el-dialog>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInspection, addInspectionItem, deleteInspectionItem } from '@/api/supervise'

const props = defineProps({
  modelValue: Boolean,
  id: [String, Number],
})
const emit = defineEmits(['update:modelValue', 'saved'])

const detail = ref(null)
const saving = ref(false)

const addVisible = ref(false)
const itemFormRef = ref()
const itemForm = reactive({
  inspectionId: null, problemType: '', problemDesc: '', evidence: '', involvedAmount: 0, dispose: '追回',
})
const itemRules = {
  problemType: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  problemDesc: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
  dispose: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
}

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function statusType(s) {
  return { 草稿: 'info', 待开展: '', 检查中: 'warning', 已完成: 'success' }[s] || 'info'
}

watch(
  () => [props.modelValue, props.id],
  async () => {
    if (!props.modelValue || !props.id) return
    const res = await getInspection(props.id)
    detail.value = res.data
    itemForm.inspectionId = props.id
    itemForm.problemType = ''
    itemForm.problemDesc = ''
    itemForm.evidence = ''
    itemForm.involvedAmount = 0
    itemForm.dispose = '追回'
  }
)

function openAdd() {
  addVisible.value = true
  itemFormRef.value?.clearValidate()
}

async function onAddItem() {
  await itemFormRef.value.validate()
  saving.value = true
  try {
    await addInspectionItem({ ...itemForm })
    ElMessage.success('问题已录入')
    addVisible.value = false
    const res = await getInspection(props.id)
    detail.value = res.data
  } finally {
    saving.value = false
  }
}

async function onDeleteItem(row) {
  await ElMessageBox.confirm('删除该问题记录？', '提示', { type: 'warning' })
  await deleteInspectionItem(row.id)
  ElMessage.success('已删除')
  const res = await getInspection(props.id)
  detail.value = res.data
  emit('saved')
}
</script>
