<template>
  <!-- ============ 新建/编辑批次 ============ -->
  <el-dialog v-model="visible" :title="batchForm.id ? '编辑批次' : '新建点评批次'" width="520px">
    <el-form :model="batchForm" label-width="110px">
      <el-form-item label="批次名称" required>
        <el-input v-model="batchForm.batchName" placeholder="如：2026年9月门诊处方专项点评" data-testid="batch-name" />
      </el-form-item>
      <el-form-item label="点评类型" required>
        <el-radio-group v-model="batchForm.reviewType" :disabled="!!batchForm.id">
          <el-radio :value="1">常规点评</el-radio>
          <el-radio :value="2">专项点评</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="batchForm.reviewType === 2" label="专项主题" required>
        <el-input v-model="batchForm.specialty" placeholder="如：抗菌药物 / 中药注射剂 / 辅助用药" />
      </el-form-item>
      <el-form-item label="处方日期范围" required>
        <div data-testid="batch-date-range" style="width: 100%">
          <el-date-picker
            v-model="batchDateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="起"
            end-placeholder="止"
            :disabled="!!batchForm.id"
            style="width: 100%" />
        </div>
      </el-form-item>
      <el-form-item v-if="!batchForm.id" label="抽样张数" required>
        <el-input-number v-model="batchForm.sampleCount" :min="1" :max="2000" data-testid="batch-sample-count" />
        <div class="form-tip">规范要求：每月抽查门急诊处方不少于 100 张（或总处方量 1‰）</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="batchForm.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="saving" @click="saveBatch">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { upsertRxBatch } from '@/api/pharmacy'

// row 为空走新建、有值走修改（后端是同一个 batchUpsert，两侧字段与校验同一份）
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
const batchDateRange = ref([])
const batchForm = reactive({ id: null, batchName: '', reviewType: 1, specialty: '', sampleCount: 100, remark: '' })

watch(visible, (open) => {
  if (!open) return
  const row = props.row
  if (row) {
    Object.assign(batchForm, { id: row.id, batchName: row.batchName, reviewType: row.reviewType, specialty: row.specialty || '', sampleCount: row.sampleCount, remark: row.remark || '' })
    batchDateRange.value = [row.dateStart, row.dateEnd]
  } else {
    Object.assign(batchForm, { id: null, batchName: '', reviewType: 1, specialty: '', sampleCount: 100, remark: '' })
    batchDateRange.value = []
  }
})

async function saveBatch() {
  if (!batchForm.batchName.trim()) {
    ElMessage.warning('请填写批次名称')
    return
  }
  const payload = {
    id: batchForm.id,
    batchName: batchForm.batchName,
    reviewType: batchForm.reviewType,
    specialty: batchForm.specialty,
    remark: batchForm.remark
  }
  if (!batchForm.id) {
    if (!batchDateRange.value || batchDateRange.value.length !== 2) {
      ElMessage.warning('请选择处方日期范围')
      return
    }
    payload.dateStart = batchDateRange.value[0]
    payload.dateEnd = batchDateRange.value[1]
    payload.sampleCount = batchForm.sampleCount
  }
  saving.value = true
  try {
    const res = await upsertRxBatch(payload)
    ElMessage.success(res?.data?.id && !batchForm.id
      ? `批次已建立，抽样 ${res.data.sampleCount} 张处方待点评`
      : '保存成功')
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
