<template>
  <!-- ============ 按处方号补录 ============ -->
  <el-dialog v-model="visible" title="按处方号补录" width="420px">
    <el-form label-width="90px">
      <el-form-item label="处方号" required>
        <el-input v-model="addByNo" placeholder="输入处方号" data-testid="add-by-no-input" @keyup.enter="doAddByNo" />
      </el-form-item>
    </el-form>
    <div class="form-tip">处方须为已审核/已发药状态，且未被任何批次收录（一处方只点评一次）</div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="doAddByNo">补录</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addRxItemByNo } from '@/api/pharmacy'

// 补录必须落在当前选中的批次上，批次归属由页面传下来（补录表单独有，不是列表列数据）
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  batchId: { type: [String, Number], default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
const addByNo = ref('')

async function doAddByNo() {
  if (!props.batchId) {
    ElMessage.warning('请先选择点评批次')
    return
  }
  if (!addByNo.value.trim()) {
    ElMessage.warning('请输入处方号')
    return
  }
  saving.value = true
  try {
    await addRxItemByNo({ batchId: props.batchId, prescriptionNo: addByNo.value.trim() })
    ElMessage.success('补录成功')
    addByNo.value = ''
    visible.value = false
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
