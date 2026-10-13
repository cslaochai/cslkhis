<template>
  <!-- 发起 / 修改申请 -->
  <el-dialog v-model="visible" :title="form.id ? '修改营养会诊申请' : '发起营养会诊'" width="560px"
             data-testid="consult-form-dialog">
    <el-form label-width="120px">
      <el-form-item label="住院患者" required>
        <div data-testid="sel-admission" style="width: 100%">
          <el-select v-model="form.admissionId" filterable :disabled="!!form.id" placeholder="选择在院患者"
                     style="width: 100%">
            <el-option v-for="a in admissions" :key="a.admissionId" :label="admissionLabel(a)"
                       :value="a.admissionId"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="会诊科室" required>
        <div data-testid="sel-to-dept" style="width: 100%">
          <el-select v-model="form.toDeptId" filterable placeholder="选择营养科" style="width: 100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="会诊范围" required>
        <el-radio-group v-model="form.consultType" data-testid="sel-consult-type">
          <el-radio :value="2">科间会诊</el-radio>
          <el-radio :value="3">全院会诊</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="紧急度">
        <el-radio-group v-model="form.isUrgent" data-testid="sel-urgent">
          <el-radio :value="0">普通（24 小时内应答）</el-radio>
          <el-radio :value="1">急会诊（10 分钟内应答）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="会诊理由" required>
        <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="500" data-testid="input-reason"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500"/>
      </el-form-item>
      <div class="form-tip">
        申请科室、申请医生、患者身份一律由服务端从入院记录与登录态推导，页面传了也不认。会诊类别固定为「营养会诊」。
      </div>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="saving" data-testid="btn-save-consult" @click="submitForm">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getInpatientListPage } from '@/api/inpatient'
import { applyNutritionConsult } from '@/api/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 会诊行：为空是发起，非空是修改（后端同一 upsert） */
  row: { type: Object, default: null },
  /** 科室下拉：页面查询卡同款数据，按 prop 传入 */
  depts: { type: Array, default: () => [] },
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
  toDeptId: null,
  consultType: 2,
  isUrgent: 0,
  reason: '',
  remark: ''
})
const form = reactive(emptyForm())

// 住院患者下拉只在表单里出现，弹框打开时现取
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

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    loadAdmissions()
    Object.assign(form, emptyForm())
    const row = props.row
    if (row) {
      Object.assign(form, {
        id: row.consultationId,
        admissionId: row.admissionId,
        toDeptId: row.toDeptId,
        consultType: row.consultType,
        isUrgent: row.isUrgent,
        reason: row.reason || '',
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
  if (!form.toDeptId) {
    ElMessage.warning('请选择会诊科室');
    return
  }
  if (!form.reason.trim()) {
    ElMessage.warning('会诊理由不能为空');
    return
  }
  saving.value = true
  try {
    await applyNutritionConsult({...form})
    ElMessage.success(form.id ? '会诊申请已修改' : '营养会诊已申请')
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
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
