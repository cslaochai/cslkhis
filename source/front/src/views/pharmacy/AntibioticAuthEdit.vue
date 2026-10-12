<template>
  <el-dialog v-model="visible" :title="authForm.id ? '修改授权' : '授予抗菌药物处方权'" width="600px">
    <el-form :model="authForm" label-width="130px">
      <el-form-item label="医师" required>
        <div data-testid="auth-doctor-select" style="width: 100%">
          <el-select
            v-model="authForm.doctorId"
            filterable
            remote
            :remote-method="searchDoctors"
            :loading="doctorLoading"
            :disabled="!!authForm.id"
            placeholder="输入姓名搜索（在职医师）"
            style="width: 100%">
            <el-option v-for="d in doctorOptions" :key="d.id" :label="`${d.doctorName}（${d.deptName || '—'} / ${d.title || '—'}）`" :value="d.id" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="授权级别" required>
        <div data-testid="auth-level-select" style="width: 100%">
          <el-select v-model="authForm.authLevel" :disabled="!!authForm.id" style="width: 100%">
            <el-option v-for="code in [1, 2, 3]" :key="code" :label="ANTIBIOTIC_LEVEL[code]" :value="code" />
          </el-select>
        </div>
        <div v-if="authForm.id" class="form-tip">换级别请另立一条授权记录（覆盖掉就查不到中间那次取消）</div>
      </el-form-item>
      <el-form-item label="授权依据">
        <el-input v-model="authForm.authBasis" placeholder="如：高级专业技术职务任职资格，经抗菌药物培训考核合格" />
      </el-form-item>
      <el-form-item label="授权日期" required>
        <div data-testid="auth-date" style="width: 100%">
          <el-date-picker
            v-model="authForm.authDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="授权日期"
            style="width: 100%" />
        </div>
      </el-form-item>
      <el-form-item label="有效期至" required>
        <div data-testid="auth-expire" style="width: 100%">
          <el-date-picker
            v-model="authForm.expireDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="有效期至"
            style="width: 100%" />
        </div>
      </el-form-item>
      <el-form-item label="状态" required>
        <el-radio-group v-model="authForm.status">
          <el-radio :value="1">有效</el-radio>
          <el-radio :value="2">暂停</el-radio>
          <el-radio :value="3">取消</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="authForm.status !== 1" label="暂停/取消原因" required>
        <el-input v-model="authForm.revokeReason" type="textarea" :rows="2" maxlength="500" placeholder="如：考核不合格 / 连续超常处方" />
      </el-form-item>
      <el-form-item label="授权人 / 部门">
        <div class="inline-pair">
          <el-input v-model="authForm.authorizer" placeholder="授权人" />
          <el-input v-model="authForm.authorizeOrg" placeholder="授权部门（医务科）" />
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" data-testid="btn-save-auth" @click="saveAuth">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 授予/修改抗菌药物处方权（新增+修改合并：row 为空=授予，row.id 存在=修改）
 * 逻辑自 AntibioticAuthView.vue 原样搬入，接口与权限码未变。
 */
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ANTIBIOTIC_LEVEL } from '@/lib/antibiotic'
import { getAntibioticDoctorSelectList, upsertAntibioticAuth } from '@/api/antibiotic'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const saving = ref(false)
const doctorOptions = ref([])
const doctorLoading = ref(false)
const authForm = reactive({
  id: null, doctorId: null, authLevel: 1, authBasis: '', authDate: '', expireDate: '',
  status: 1, authorizer: '', authorizeOrg: '', revokeReason: ''
})

async function searchDoctors(keyword) {
  doctorLoading.value = true
  try {
    const res = await getAntibioticDoctorSelectList(keyword || '')
    doctorOptions.value = res?.data || []
  } finally {
    doctorLoading.value = false
  }
}

function defaultDates() {
  const d = (x) => `${x.getFullYear()}-${String(x.getMonth() + 1).padStart(2, '0')}-${String(x.getDate()).padStart(2, '0')}`
  const now = new Date()
  return [d(now), d(new Date(now.getFullYear() + 1, now.getMonth(), now.getDate()))]
}

watch(visible, (open) => {
  if (!open) return
  const row = props.row
  const [d1, d2] = defaultDates()
  if (row && row.id) {
    Object.assign(authForm, {
      id: row.id, doctorId: row.doctorId, authLevel: row.authLevel, authBasis: row.authBasis || '',
      authDate: row.authDate, expireDate: row.expireDate, status: row.status || 1,
      authorizer: row.authorizer || '', authorizeOrg: row.authorizeOrg || '', revokeReason: row.revokeReason || ''
    })
    doctorOptions.value = [{ id: row.doctorId, doctorName: row.doctorName, deptName: row.deptName, title: row.title }]
  } else {
    Object.assign(authForm, {
      id: null, doctorId: null, authLevel: 1, authBasis: '', authDate: d1, expireDate: d2,
      status: 1, authorizer: '', authorizeOrg: '医务科', revokeReason: ''
    })
  }
})

async function saveAuth() {
  if (!authForm.doctorId) { ElMessage.warning('请选择医师'); return }
  if (authForm.status !== 1 && !authForm.revokeReason.trim()) { ElMessage.warning('暂停或取消必须填写原因'); return }
  saving.value = true
  try {
    await upsertAntibioticAuth({ ...authForm })
    ElMessage.success('已保存')
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
  margin-bottom: 10px;
}
.inline-pair {
  display: flex;
  gap: 8px;
  width: 100%;
}
</style>
