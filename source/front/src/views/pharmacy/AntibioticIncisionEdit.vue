<template>
  <el-dialog v-model="visible" :title="reviewForm.id ? '重评 I 类切口预防用药' : 'I 类切口预防用药点评'" width="680px">
    <el-form :model="reviewForm" label-width="150px">
      <el-form-item label="手术">
        <span>{{ reviewMeta.operationName || '—' }}</span>
        <span class="muted">（{{ reviewMeta.patientName || '—' }} / {{ reviewMeta.deptName || '—' }} / 主刀 {{ reviewMeta.surgeonName || '—' }} / 切皮 {{ reviewMeta.operationTime || '—' }}）</span>
      </el-form-item>
      <el-form-item label="预防用药">
        <div data-testid="review-drug-select" style="width: 100%">
          <el-select v-model="reviewForm.drugId" filterable clearable placeholder="未用药则留空" style="width: 100%">
            <el-option
              v-for="d in drugOptions"
              :key="d.id"
              :label="`${d.drugName}（${ANTIBIOTIC_LEVEL[d.antibioticLevel]}）`"
              :value="d.id" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="预防用药指征">
        <el-radio-group v-model="reviewForm.indicationFlag">
          <el-radio :value="1">有（植入物 / 高危因素 / 手术 >3h 等）</el-radio>
          <el-radio :value="0">无</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="给药时机">
        <div data-testid="review-timing-select" style="width: 100%">
          <el-select v-model="reviewForm.timingType" clearable placeholder="选择给药时机" style="width: 100%">
            <el-option v-for="(text, code) in ANTIBIOTIC_TIMING" :key="code" :label="text" :value="Number(code)" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="预防用药总时长（h）">
        <el-input-number v-model="reviewForm.courseHours" :min="0" :max="720" />
        <span class="muted" style="margin-left: 8px">超过 24 小时即"疗程过长"</span>
      </el-form-item>
      <el-form-item label="联合用药">
        <el-radio-group v-model="reviewForm.comboFlag">
          <el-radio :value="0">否</el-radio>
          <el-radio :value="1">是</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="reviewForm.comboFlag === 1" label="联合用药理由" required>
        <el-input v-model="reviewForm.comboReason" type="textarea" :rows="2" maxlength="500" />
      </el-form-item>
      <el-form-item label="特殊使用级会诊">
        <el-radio-group v-model="reviewForm.consultFlag">
          <el-radio :value="1">有会诊同意</el-radio>
          <el-radio :value="0">无</el-radio>
        </el-radio-group>
        <div v-if="isSpecialLevel && reviewForm.consultFlag === 0" class="form-tip warn">
          用了特殊使用级却没有会诊同意 —— 结论为不合理时，问题码必须勾选「47 特殊使用级无会诊」
        </div>
      </el-form-item>
      <el-form-item label="点评结论" required>
        <el-radio-group v-model="reviewForm.reviewResult">
          <el-radio :value="1">合理</el-radio>
          <el-radio :value="2">不合理</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="reviewForm.reviewResult === 2" label="问题码" required>
        <el-checkbox-group v-model="problemCodes">
          <el-checkbox v-for="(text, code) in INCISION_PROBLEM" :key="code" :value="String(code)">
            {{ text }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item v-if="reviewForm.reviewResult === 2" label="点评意见" required>
        <el-input v-model="reviewForm.reviewOpinion" type="textarea" :rows="3" maxlength="500" placeholder="说明具体问题与依据" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" data-testid="btn-save-review" @click="saveReview">提交点评</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * I 类切口预防用药点评（新增+重评合并：row 为空=点评待点评行 candidate，row.id 存在=重评）
 * 逻辑自 AntibioticIncisionView.vue 原样搬入，接口、字段、校验与提示文案均未变。
 * drugOptions 只用于本弹框（下拉 + 特殊级判断），故随表单移到子组件按需加载。
 */
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ANTIBIOTIC_LEVEL, ANTIBIOTIC_TIMING, INCISION_PROBLEM } from '@/lib/antibiotic'
import { getAntibioticDrugSelectList, upsertIncisionReview } from '@/api/antibiotic'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  candidate: { type: Object, default: null },
  row: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const saving = ref(false)
const drugOptions = ref([])
const drugLoaded = ref(false)
const reviewMeta = ref({})
const problemCodes = ref([])
const reviewForm = reactive({
  id: null,
  operationApplyId: null,
  drugId: null,
  indicationFlag: 0,
  timingType: null,
  courseHours: null,
  comboFlag: 0,
  comboReason: '',
  consultFlag: 0,
  reviewResult: 1,
  reviewOpinion: '',
  remark: ''
})

const isSpecialLevel = computed(() => {
  const d = drugOptions.value.find(x => String(x.id) === String(reviewForm.drugId))
  return d && d.antibioticLevel === 3
})

async function ensureDrugOptions() {
  if (drugLoaded.value) return
  const res = await getAntibioticDrugSelectList()
  drugOptions.value = res?.data || []
  drugLoaded.value = true
}

watch(visible, async (open) => {
  if (!open) return
  ensureDrugOptions()
  const row = props.row
  const candidate = props.candidate
  problemCodes.value = []
  if (row) {
    Object.assign(reviewForm, {
      id: row.id,
      operationApplyId: row.operationApplyId,
      drugId: row.drugId,
      indicationFlag: row.indicationFlag ?? 0,
      timingType: row.timingType,
      courseHours: row.courseHours,
      comboFlag: row.comboFlag ?? 0,
      comboReason: row.comboReason || '',
      consultFlag: row.consultFlag ?? 0,
      reviewResult: row.reviewResult ?? 1,
      reviewOpinion: row.reviewOpinion || '',
      remark: row.remark || ''
    })
    problemCodes.value = row.problemTypes ? String(row.problemTypes).split(',').map(s => s.trim()) : []
    reviewMeta.value = {
      operationName: row.operationName, patientName: row.patientName,
      deptName: row.deptName, surgeonName: row.surgeonName, operationTime: row.operationTime
    }
  } else if (candidate) {
    Object.assign(reviewForm, {
      id: null,
      operationApplyId: candidate.operationApplyId,
      drugId: (candidate.drugCandidates || [])[0]?.drugId || null,
      indicationFlag: 0,
      timingType: null,
      courseHours: null,
      comboFlag: 0,
      comboReason: '',
      consultFlag: 0,
      reviewResult: 1,
      reviewOpinion: '',
      remark: ''
    })
    reviewMeta.value = {
      operationName: candidate.operationName, patientName: candidate.patientName,
      deptName: candidate.deptName, surgeonName: candidate.surgeonName, operationTime: candidate.operationTime
    }
  }
})

async function saveReview() {
  if (reviewForm.reviewResult === 2) {
    if (!problemCodes.value.length) { ElMessage.warning('结论为不合理时必须选择问题码'); return }
    if (!reviewForm.reviewOpinion.trim()) { ElMessage.warning('结论为不合理时必须填写点评意见'); return }
  }
  if (reviewForm.comboFlag === 1 && !reviewForm.comboReason.trim()) { ElMessage.warning('联合用药必须填写联合理由'); return }
  saving.value = true
  try {
    await upsertIncisionReview({
      ...reviewForm,
      problemTypes: reviewForm.reviewResult === 2 ? problemCodes.value.join(',') : null
    })
    ElMessage.success('点评已提交')
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.muted {
  color: #909399;
  font-size: 12px;
}
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
.form-tip.warn {
  color: #e6a23c;
}
</style>
