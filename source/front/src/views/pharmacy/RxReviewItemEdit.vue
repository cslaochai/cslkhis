<template>
  <!-- ============ 提交点评 ============ -->
  <el-dialog v-model="visible" title="处方点评" width="680px">
    <template v-if="row">
      <el-descriptions :column="2" border size="small" class="review-desc">
        <el-descriptions-item label="处方号">{{ row.prescriptionNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ row.patientName }}</el-descriptions-item>
        <el-descriptions-item label="医生 / 科室">{{ row.doctorName }} / {{ row.deptName }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ row.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ row.diagnosis || '—' }}</el-descriptions-item>
      </el-descriptions>
      <div class="drug-list">
        <div class="drug-list-title">处方药品（{{ (row.drugDetails || []).length }} 种）</div>
        <div v-for="(line, i) in row.drugDetails || []" :key="i" class="drug-line">{{ line }}</div>
      </div>
      <el-form label-width="90px" class="review-form">
        <el-form-item label="点评结论" required>
          <el-radio-group v-model="reviewForm.reviewResult">
            <el-radio :value="1">合理处方</el-radio>
            <el-radio :value="2">不规范处方</el-radio>
            <el-radio :value="3">用药不适宜</el-radio>
            <el-radio :value="4">超常处方</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="reviewForm.reviewResult && reviewForm.reviewResult !== 1" label="问题项" required>
          <el-checkbox-group v-model="reviewForm.problemCodes">
            <el-checkbox v-for="p in (PROBLEM_GROUPS[reviewForm.reviewResult] || [])" :key="p.code" :value="p.code">
              {{ p.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item
          v-if="reviewForm.reviewResult && reviewForm.reviewResult !== 1"
          label="点评意见"
          required>
          <el-input
            v-model="reviewForm.reviewOpinion"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="说明具体问题与依据" />
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveReview">提交点评</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { PROBLEM_GROUPS } from '@/lib/rxReview'
import { upsertRxItem } from '@/api/pharmacy'

// 待点评/已点评的明细行（后端是同一个 itemUpsert，row 为空即不打开）
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
const reviewForm = reactive({ id: null, reviewResult: null, problemCodes: [], reviewOpinion: '' })

watch(visible, (open) => {
  if (!open) return
  const row = props.row
  if (!row) return
  reviewForm.id = row.id
  reviewForm.reviewResult = row.reviewResult || null
  reviewForm.problemCodes = row.problemTypes ? row.problemTypes.split(',') : []
  reviewForm.reviewOpinion = row.reviewOpinion || ''
})

async function saveReview() {
  if (!reviewForm.reviewResult) {
    ElMessage.warning('请选择点评结论')
    return
  }
  const unreasonable = reviewForm.reviewResult !== 1
  if (unreasonable && !reviewForm.problemCodes.length) {
    ElMessage.warning('不合理处方必须勾选问题项')
    return
  }
  if (unreasonable && !reviewForm.reviewOpinion.trim()) {
    ElMessage.warning('不合理处方必须填写点评意见')
    return
  }
  saving.value = true
  try {
    await upsertRxItem({
      id: reviewForm.id,
      reviewResult: reviewForm.reviewResult,
      problemTypes: unreasonable ? reviewForm.problemCodes : [],
      reviewOpinion: unreasonable ? reviewForm.reviewOpinion : ''
    })
    ElMessage.success('点评已提交')
    visible.value = false
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.review-desc {
  margin-bottom: 12px;
}

.drug-list {
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 12px;
  max-height: 200px;
  overflow: auto;
}

.drug-list-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.drug-line {
  font-size: 13px;
  line-height: 1.8;
}

.review-form {
  margin-top: 4px;
}
</style>
