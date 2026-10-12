<template>
  <!-- 登记 / 修改筛查 -->
  <el-dialog v-model="visible" :title="form.id ? '修改筛查记录' : '登记营养风险筛查'" data-testid="screen-dialog"
             width="620px">
    <el-form label-width="130px">
      <el-form-item label="住院患者" required>
        <div data-testid="sel-admission" style="width: 100%">
          <el-select v-model="form.admissionId" :disabled="!!form.id" filterable placeholder="选择在院患者"
                     style="width: 100%">
            <el-option v-for="a in admissions" :key="a.admissionId" :label="admissionLabel(a)"
                       :value="a.admissionId"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="量表" required>
        <div data-testid="sel-screen-type">
          <el-select v-model="form.screenType" style="width: 240px" @change="onTypeChange">
            <el-option v-for="(t, code) in SCREEN_TYPE_TEXT" :key="code" :label="t" :value="Number(code)"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="筛查时机" required>
        <div data-testid="sel-screen-source">
          <el-select v-model="form.screenSource" style="width: 200px">
            <el-option v-for="(t, code) in SCREEN_SOURCE_TEXT" :key="code" :label="t" :value="Number(code)"/>
          </el-select>
        </div>
      </el-form-item>

      <template v-if="form.screenType === 1">
        <el-form-item label="营养状态受损">
          <div data-testid="sel-impair">
            <el-select v-model="form.impairScore" style="width: 120px">
              <el-option v-for="o in SCORE_OPTIONS_0_3" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </div>
          <span class="muted hint">0-无 1-肿瘤患者 2-重度消耗（卧床、胸腹部术后） 3-重症监护</span>
        </el-form-item>
        <el-form-item label="疾病严重程度">
          <div data-testid="sel-severity">
            <el-select v-model="form.severityScore" style="width: 120px">
              <el-option v-for="o in SCORE_OPTIONS_0_3" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </div>
          <span class="muted hint">1-发热等 2-腹部手术、慢病急性发作 3-ICU、重度颅脑损伤</span>
        </el-form-item>
        <el-form-item label="年龄项（≥70 岁 +1）">
          <span data-testid="age-score">{{ ageScoreHint }}</span>
        </el-form-item>
        <el-form-item label="预计总分 / 判定">
          <span data-testid="local-total">{{ localTotal }} 分</span>
          <el-tag :type="nrsLocalRisk(localTotal) ? 'danger' : 'success'" class="hint" disable-transitions>
            {{ nrsLocalRisk(localTotal) ? '达到营养风险阈值（≥3）' : '未达阈值，需 7 天后复筛' }}
          </el-tag>
          <div class="muted hint">仅作表单提示，保存后以服务端返回的总分与判定为准。</div>
        </el-form-item>
      </template>
      <el-form-item v-else label="评定总分" required>
        <el-input-number v-model="form.totalScore" :max="form.screenType === 3 ? 17 : 35" :min="0"
                         data-testid="input-total"/>
        <span class="muted hint">{{
            form.screenType === 3 ? 'MNA 满分 17，<11 判为存在营养不良' : 'PG-SGA 由营养师按 0~35 分级'
          }}</span>
      </el-form-item>

      <el-form-item label="身高 / 体重">
        <el-input-number v-model="form.heightCm" :max="250" :min="0" :precision="1" controls-position="right"
                         data-testid="input-height" style="width: 140px"/>
        <span class="muted hint">cm</span>
        <el-input-number v-model="form.weightKg" :max="400" :min="0" :precision="1" controls-position="right"
                         data-testid="input-weight" style="width: 140px"/>
        <span class="muted hint">kg（BMI 由服务端算）</span>
      </el-form-item>
      <el-form-item label="近 3 月体重下降">
        <el-input-number v-model="form.weightLossPercent" :max="100" :min="0" :precision="1" controls-position="right"
                         data-testid="input-loss" style="width: 140px"/>
        <span class="muted hint">%</span>
      </el-form-item>
      <el-form-item label="下次筛查日期">
        <div data-testid="sel-next-screen">
          <el-date-picker v-model="form.nextScreenDate" placeholder="留空：阴性自动取筛查日 +7 天" style="width: 260px"
                          type="date" value-format="YYYY-MM-DD"/>
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="500" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" data-testid="btn-save-screen" type="primary" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getInpatientListPage } from '@/api/inpatient'
import { upsertNutritionScreen } from '@/api/nutrition'
import {
  nrsLocalRisk,
  nrsLocalTotal,
  SCORE_OPTIONS_0_3,
  SCREEN_SOURCE_TEXT,
  SCREEN_TYPE_TEXT,
} from '@/lib/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 筛查行：为空是登记，非空是修改（后端同一 upsert） */
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

// 住院患者下拉只在本弹框使用，弹框打开时现取（在院住院，一次抓全量供选择）
const admissions = ref([])

const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`

async function loadAdmissions() {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200})
    admissions.value = res?.data?.records || []
  } catch (e) {
    admissions.value = []
  }
}

const saving = ref(false)
const emptyForm = () => ({
  id: null,
  admissionId: null,
  screenType: 1,
  screenSource: 1,
  impairScore: 0,
  severityScore: 0,
  totalScore: null,
  heightCm: null,
  weightKg: null,
  weightLossPercent: null,
  nextScreenDate: null,
  remark: '',
})
const form = reactive(emptyForm())

/** 年龄项只作展示：服务端按患者档案真实年龄算，前端传了也不认 */
const ageScoreHint = computed(() => {
  const a = admissions.value.find((x) => String(x.admissionId) === String(form.admissionId))
  return a?.age != null ? `${a.age} 岁 · 年龄项由服务端按档案年龄判定（≥70 岁 +1 分）` : '由服务端按患者档案年龄判定（≥70 岁 +1 分）'
})
const localTotal = computed(() => nrsLocalTotal(form.impairScore, form.severityScore, 0))

function onTypeChange() {
  if (form.screenType !== 1) {
    form.impairScore = null
    form.severityScore = null
  } else {
    form.impairScore = 0
    form.severityScore = 0
    form.totalScore = null
  }
}

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    loadAdmissions()
    Object.assign(form, emptyForm())
    const row = props.row
    if (row) {
      Object.assign(form, {
        id: row.id,
        admissionId: row.admissionId,
        screenType: row.screenType,
        screenSource: row.screenSource,
        impairScore: row.impairScore,
        severityScore: row.severityScore,
        totalScore: row.screenType === 1 ? null : row.totalScore,
        heightCm: row.heightCm,
        weightKg: row.weightKg,
        weightLossPercent: row.weightLossPercent,
        nextScreenDate: row.nextScreenDate,
        remark: row.remark || '',
      })
    }
  },
)

async function submitForm() {
  if (!form.admissionId) {
    ElMessage.warning('请选择住院患者');
    return
  }
  if (form.screenType !== 1 && form.totalScore == null) {
    ElMessage.warning('请填写评定总分');
    return
  }
  saving.value = true
  try {
    const res = await upsertNutritionScreen({...form})
    const saved = res?.data || {}
    ElMessage.success(`已保存：总分 ${saved.totalScore ?? '-'} 分 · ${saved.riskFlagText || ''}`
        + (saved.nextScreenDate ? ` · 下次筛查 ${saved.nextScreenDate}` : ''))
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
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
.hint {
  margin-left: 8px;
}
</style>
