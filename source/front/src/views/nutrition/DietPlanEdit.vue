<template>
  <!-- 手工登记 / 修改方案 -->
  <el-dialog v-model="visible" :title="form.id ? '修改膳食方案' : '手工登记膳食方案'" width="620px" data-testid="plan-dialog">
    <el-form label-width="120px">
      <el-form-item label="住院患者" required>
        <div data-testid="sel-admission" style="width: 100%">
          <el-select v-model="form.admissionId" filterable :disabled="!!form.id" placeholder="选择在院患者" style="width: 100%">
            <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="a.admissionId" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="饮食类型" required>
        <div data-testid="sel-diet-code" style="width: 100%">
          <el-select v-model="form.dietCode" filterable placeholder="从饮食目录中选择" style="width: 100%" @change="onDietChange">
            <el-option
              v-for="d in dietOptions"
              :key="d.code"
              :label="`${d.name}（${d.categoryText} · ${d.routeText}）`"
              :value="d.code" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="目录口径">
        <div class="muted" data-testid="diet-hint">
          {{ currentDiet ? `${currentDiet.routeText} · ${currentDiet.needsMeal ? '进食堂订餐' : '不进食堂订餐'} · 默认 ${currentDiet.mealTypesText || '不订餐'}` : '选择饮食类型后由服务端带出类别、途径与默认餐次' }}
        </div>
      </el-form-item>
      <el-form-item label="饮食名称">
        <el-input v-model="form.dietName" maxlength="100" placeholder="留空取目录名，可写个体化名称" data-testid="input-diet-name" />
      </el-form-item>
      <el-form-item v-if="currentDiet && Number(currentDiet.route) !== 1" label="管饲/输注方式">
        <el-input v-model="form.feedWay" maxlength="100" placeholder="如：鼻胃管 间歇滴注 40ml/h" data-testid="input-feed-way" />
      </el-form-item>
      <el-form-item label="供应餐次">
        <el-checkbox-group v-model="mealTypeList" data-testid="chk-meal-types">
          <el-checkbox v-for="(t, code) in MEAL_TYPE_TEXT" :key="code" :value="Number(code)">{{ t }}</el-checkbox>
        </el-checkbox-group>
        <div class="muted">为空取目录默认餐次；勾了但途径不是口服，食堂侧不会生成餐单。</div>
      </el-form-item>
      <el-form-item label="目标量">
        <div class="target-row">
          <el-input-number v-model="form.calorieTarget" :min="0" :max="6000" :step="50" controls-position="right" data-testid="input-calorie" />
          <span class="muted">kcal</span>
          <el-input-number v-model="form.proteinTarget" :min="0" :max="300" :step="5" controls-position="right" data-testid="input-protein" />
          <span class="muted">g</span>
          <el-input-number v-model="form.fluidTarget" :min="0" :max="5000" :step="100" controls-position="right" data-testid="input-fluid" />
          <span class="muted">ml</span>
        </div>
        <div class="muted">同一种饮食按体重分级是临床常态，这里可以覆盖目录默认值。</div>
      </el-form-item>
      <el-form-item label="开始时间">
        <div data-testid="sel-start-time" style="width: 100%">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空取当前时间" style="width: 100%" />
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" data-testid="input-remark" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="saving" data-testid="btn-save-plan" @click="submitForm">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getInpatientListPage } from '@/api/inpatient'
import { getDietTypeOptions, upsertDietPlan } from '@/api/diet'
import { MEAL_TYPE_TEXT } from '@/lib/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 为空走手工登记，有值走修改（主页面只传当前行）
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
const emptyForm = () => ({
  id: null,
  admissionId: null,
  dietCode: null,
  dietName: '',
  feedWay: '',
  calorieTarget: null,
  proteinTarget: null,
  fluidTarget: null,
  startTime: null,
  remark: '',
})
const form = reactive(emptyForm())
const mealTypeList = ref([])

// ---------------- 下拉数据（只有本表单用，弹框打开时懒加载） ----------------
const admissions = ref([])
const dietOptions = ref([])

const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`

async function loadOptions() {
  try {
    const res = await getDietTypeOptions()
    dietOptions.value = res?.data || []
  } catch (e) {
    dietOptions.value = []
  }
  try {
    const res = await getInpatientListPage({ admitStatus: 1, pageNum: 1, pageSize: 200 })
    admissions.value = res?.data?.records || []
  } catch (e) {
    admissions.value = []
  }
}

const currentDiet = computed(() => dietOptions.value.find((d) => d.code === form.dietCode) || null)

/** 选完饮食类型把目录建议值填进表单（落库仍以服务端按 code 重算为准） */
function onDietChange() {
  const d = currentDiet.value
  if (!d) return
  if (!form.dietName) form.dietName = d.name
  form.calorieTarget = d.calorieTarget ?? null
  form.proteinTarget = d.proteinTarget ?? null
  mealTypeList.value = (d.mealTypes || '').split(',').filter(Boolean).map(Number)
}

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    Object.assign(form, emptyForm())
    mealTypeList.value = []
    const row = props.row
    if (row) {
      Object.assign(form, {
        id: row.id,
        admissionId: row.admissionId,
        dietCode: row.dietCode,
        dietName: row.dietName || '',
        feedWay: row.feedWay || '',
        calorieTarget: row.calorieTarget,
        proteinTarget: row.proteinTarget,
        fluidTarget: row.fluidTarget,
        remark: row.remark || '',
      })
      mealTypeList.value = (row.mealTypes || '').split(',').filter(Boolean).map(Number)
    }
    loadOptions()
  }
)

async function submitForm() {
  if (!form.admissionId) { ElMessage.warning('请选择住院患者'); return }
  if (!form.dietCode) { ElMessage.warning('请选择饮食类型'); return }
  saving.value = true
  try {
    await upsertDietPlan({
      ...form,
      mealTypes: mealTypeList.value.length ? mealTypeList.value.join(',') : null,
    })
    ElMessage.success('膳食方案已保存')
    visible.value = false
    emit('saved')
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
.target-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
