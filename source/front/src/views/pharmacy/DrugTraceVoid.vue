<template>
  <el-dialog v-model="visible" data-testid="void-dialog" title="追溯码作废" width="480px">
    <el-form label-width="90px">
      <el-form-item label="作废类型">
        <el-select v-model="voidForm.voidType" data-testid="select-void-type" style="width: 100%">
          <el-option v-for="d in voidDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="voidForm.reason" :rows="3" data-testid="input-void-reason" placeholder="必填，退药/报损/召回原因"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="btn-void-submit" type="primary" @click="submitVoid">确认作废</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 追溯码作废（自 DrugTraceView.vue 原样搬出：字段、校验、接口与提示文案均未变）
 * voidDict 由父页面加载（详情弹窗的「作废类型」也用它），此处仅作下拉数据源。
 */
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { voidDrugTrace } from '@/api/drugTrace'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
  voidDict: { type: Array, default: () => [] },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const voidForm = reactive({ traceId: null, voidType: 1, reason: '' })

watch(visible, (open) => {
  if (!open) return
  const row = props.row
  if (row) {
    voidForm.traceId = row.id
    voidForm.voidType = Number(row.status) === 2 ? 1 : 2
    voidForm.reason = ''
  }
})

const submitVoid = async () => {
  if (!voidForm.reason?.trim()) {
    ElMessage.warning('请填写作废原因')
    return
  }
  try {
    const res = await voidDrugTrace({
      traceId: voidForm.traceId,
      voidType: voidForm.voidType,
      reason: voidForm.reason.trim(),
    })
    if (res.code === 200) {
      ElMessage.success('已作废，状态变更将重新上传')
      visible.value = false
      emit('saved')
    }
  } catch (e) {
    ElMessage.error(e?.message || '作废失败')
  }
}
</script>
