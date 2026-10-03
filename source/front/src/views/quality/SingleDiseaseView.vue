<script setup lang="ts">
/**
 * 单病种质控（M4，菜单 2183，挂病历与质量安全）
 *
 * 链路：病种目录（ICD 前缀纳入）→ 病例纳入（首页快照，自动扫描/手工）→ 质控判级 → 上报打标 → 病种指标。
 * 指标口径：治愈率=治愈/纳入；死亡率=死亡/纳入；平均住院日/费用按纳入例数，服务端复算。
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {
  listDiseases, upsertDisease, deleteDisease, enrollCase, autoEnroll,
  qcCase, reportCase, listCasePage, getMetrics,
} from '@/api/singleDisease'

const CURATIVE: Record<number, string> = { 1: '治愈', 2: '好转', 3: '未愈', 4: '死亡', 5: '其他' }
const QC_STATUS_TEXT: Record<number, string> = { 0: '待质控', 1: '通过', 2: '异常' }
const QC_TAG: Record<number, string> = { 0: 'info', 1: 'success', 2: 'danger' }
const ENROLL_WAY: Record<number, string> = { 1: '自动扫描', 2: '手工纳入' }

const activeTab = ref('metrics')
const diseases = ref<any[]>([])
const metrics = ref<any[]>([])
const casePage = ref<any>({ records: [], total: 0 })
const caseQuery = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, diseaseId: null as number | null,
  qcStatus: null as number | null, reportStatus: null as number | null, keyword: '' })
const loading = ref(false)

const loadAll = async () => {
  const [d, m] = await Promise.all([listDiseases(), getMetrics()])
  diseases.value = d?.data || []
  metrics.value = m?.data || []
}
const loadCases = async () => {
  loading.value = true
  try {
    const res: any = await listCasePage(caseQuery.value)
    casePage.value = res?.data || { records: [], total: 0 }
  } finally { loading.value = false }
}
onMounted(async () => { await loadAll(); await loadCases() })

// ---------------- 目录维护 ----------------
const diseaseDlg = ref(false)
const diseaseForm = ref<any>({})
const openDiseaseDlg = (d?: any) => {
  diseaseForm.value = d ? { ...d } : { id: null, diseaseCode: '', diseaseName: '', icd10Prefix: '', remark: '' }
  diseaseDlg.value = true
}
const saveDisease = async () => {
  const res: any = await upsertDisease(diseaseForm.value)
  if (res?.code === 200) { ElMessage.success('病种已保存'); diseaseDlg.value = false; loadAll() }
}
const doDeleteDisease = (d: any) => {
  ElMessageBox.confirm(`删除病种「${d.diseaseName}」？（已纳入病例的病种会被拒绝）`, '删除', { type: 'warning' })
    .then(async () => {
      const res: any = await deleteDisease(d.id)
      if (res?.code === 200) { ElMessage.success('已删除'); loadAll() }
    }).catch(() => {})
}

// ---------------- 纳入 ----------------
const enrollDlg = ref(false)
const enrollForm = ref<any>({})
const openEnroll = () => {
  enrollForm.value = { diseaseId: null, admissionId: null }; enrollDlg.value = true
}
const saveEnroll = async () => {
  const f = enrollForm.value
  if (!f.diseaseId || !f.admissionId) { ElMessage.warning('病种与住院ID必填'); return }
  const res: any = await enrollCase(f)
  if (res?.code === 200) {
    ElMessage.success(`已纳入（病例 ${res.data?.caseNo}）`); enrollDlg.value = false; loadAll(); loadCases()
  }
}
const doAutoEnroll = (d: any) => {
  ElMessageBox.confirm(`按 ICD 前缀「${d.icd10Prefix}」扫描全部出院首页并自动纳入「${d.diseaseName}」？`, '自动扫描', { type: 'info' })
    .then(async () => {
      const res: any = await autoEnroll({ diseaseId: d.id })
      if (res?.code === 200) {
        const r = res.data || {}
        ElMessage.success(`扫描 ${r.scanned} 份，纳入 ${r.enrolled}，跳过 ${(r.skipped || []).length}`)
        loadAll(); loadCases()
      }
    }).catch(() => {})
}

// ---------------- 质控 / 上报 ----------------
const qcDlg = ref(false)
const qcForm = ref<any>({})
const openQc = (row: any) => {
  qcForm.value = { id: row.id, curativeEffect: row.deathFlag === 1 ? 4 : null, remark: '', row }
  qcDlg.value = true
}
const saveQc = async () => {
  if (!qcForm.value.curativeEffect) { ElMessage.warning('疗效判定必选'); return }
  const res: any = await qcCase(qcForm.value)
  if (res?.code === 200) {
    const issues = res.data?.qcIssues
    if (res.data?.qcStatus === 1) ElMessage.success('质控通过')
    else ElMessage.warning('质控异常：' + (issues || ''))
    qcDlg.value = false; loadCases(); loadAll()
  }
}
const doReport = (row: any) => {
  ElMessageBox.confirm(`上报病例「${row.caseNo}」？（质控通过才可上报）`, '上报', { type: 'info' })
    .then(async () => {
      const res: any = await reportCase(row.id)
      if (res?.code === 200) { ElMessage.success('已上报'); loadCases() }
    }).catch(() => {})
}

const pct = (v: any) => (Number(v || 0) * 100).toFixed(1) + '%'
const money = (v: any) => '¥' + Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
</script>

<template>
  <div class="page">
    <el-tabs v-model="activeTab">
      <!-- 指标总览 -->
      <el-tab-pane label="病种指标" name="metrics">
        <el-table :data="metrics" data-testid="sd-metrics-table">
          <el-table-column prop="diseaseCode" label="编码" width="110" />
          <el-table-column prop="diseaseName" label="病种" min-width="140" />
          <el-table-column prop="icd10Prefix" label="纳入前缀" width="140" />
          <el-table-column label="纳入例数" width="100" data-testid="sd-col-cases">
            <template #default="{ row }">{{ row.caseCount }}</template>
          </el-table-column>
          <el-table-column label="治愈率" width="100">
            <template #default="{ row }" data-testid="sd-col-cure">{{ pct(row.cureRate) }}</template>
          </el-table-column>
          <el-table-column label="死亡率" width="100">
            <template #default="{ row }">{{ pct(row.deathRate) }}</template>
          </el-table-column>
          <el-table-column label="平均住院日" width="110">
            <template #default="{ row }" data-testid="sd-col-los">{{ row.avgInpatientDays }}</template>
          </el-table-column>
          <el-table-column label="平均费用" width="130">
            <template #default="{ row }" data-testid="sd-col-fee">{{ money(row.avgTotalAmount) }}</template>
          </el-table-column>
          <el-table-column label="质控通过" width="100">
            <template #default="{ row }">{{ row.qcPassedCount }}/{{ row.caseCount }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!metrics.length" description="暂无病种目录，请先在「病种目录」页签维护" />
      </el-tab-pane>

      <!-- 病种目录 -->
      <el-tab-pane label="病种目录" name="diseases">
        <div class="toolbar">
          <el-button v-perm="'qc:singleDisease:add'" type="primary" data-testid="sd-disease-add" @click="openDiseaseDlg()">新增病种</el-button>
        </div>
        <el-table :data="diseases" data-testid="sd-disease-table">
          <el-table-column prop="diseaseCode" label="编码" width="110" />
          <el-table-column prop="diseaseName" label="病种名称" min-width="150" />
          <el-table-column prop="icd10Prefix" label="纳入 ICD 前缀" min-width="140" />
          <el-table-column prop="caseCount" label="已纳入" width="90" />
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button v-perm="'qc:singleDisease:add'" link type="primary" data-testid="sd-disease-edit" @click.stop="openDiseaseDlg(row)">编辑</el-button>
              <el-button v-perm="'qc:singleDisease:edit'" link type="success" data-testid="sd-auto-enroll" @click.stop="doAutoEnroll(row)">自动扫描</el-button>
              <el-button v-perm="'qc:singleDisease:add'" link type="danger" data-testid="sd-disease-del" @click.stop="doDeleteDisease(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 病例列表 -->
      <el-tab-pane label="病例列表" name="cases">
        <div class="toolbar">
          <el-button v-perm="'qc:singleDisease:edit'" type="primary" data-testid="sd-enroll" @click="openEnroll">手工纳入</el-button>
          <el-select v-model="caseQuery.diseaseId" placeholder="病种" clearable style="width:180px" data-testid="sd-disease-filter">
            <el-option v-for="d in diseases" :key="d.id" :label="d.diseaseName" :value="d.id" />
          </el-select>
          <el-select v-model="caseQuery.qcStatus" placeholder="质控状态" clearable style="width:130px" data-testid="sd-qc-filter">
            <el-option v-for="(t, k) in QC_STATUS_TEXT" :key="k" :label="t" :value="Number(k)" />
          </el-select>
          <el-select v-model="caseQuery.reportStatus" placeholder="上报状态" clearable style="width:130px" data-testid="sd-report-filter">
            <el-option label="未上报" :value="0" /><el-option label="已上报" :value="1" />
          </el-select>
          <el-input v-model="caseQuery.keyword" placeholder="患者姓名 / 病例编号" clearable style="width:200px" data-testid="sd-keyword" />
          <el-button data-testid="sd-search" @click="loadCases">查询</el-button>
        </div>
        <el-table :data="casePage.records" v-loading="loading" data-testid="sd-case-table">
          <el-table-column prop="caseNo" label="病例编号" width="170" />
          <el-table-column prop="diseaseName" label="病种" width="120" />
          <el-table-column prop="patientName" label="患者" width="90" />
          <el-table-column prop="mainDiagnosisCode" label="主诊断编码" width="110" />
          <el-table-column prop="mainDiagnosisName" label="主诊断" min-width="140" show-overflow-tooltip />
          <el-table-column prop="inpatientDays" label="住院日" width="80" />
          <el-table-column label="费用" width="110">
            <template #default="{ row }">{{ money(row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column label="疗效" width="70">
            <template #default="{ row }">{{ row.curativeEffect ? CURATIVE[row.curativeEffect] : '—' }}</template>
          </el-table-column>
          <el-table-column label="质控" width="80">
            <template #default="{ row }">
              <el-tooltip :content="row.qcIssues || ''" :disabled="!row.qcIssues">
                <el-tag :type="QC_TAG[row.qcStatus]" size="small" data-testid="sd-row-qc">{{ QC_STATUS_TEXT[row.qcStatus] }}</el-tag>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="上报" width="80">
            <template #default="{ row }">
              <el-tag :type="row.reportStatus === 1 ? 'success' : 'info'" size="small">{{ row.reportStatus === 1 ? '已上报' : '未上报' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.qcStatus === 0 || row.qcStatus === 2" v-perm="'qc:singleDisease:edit'"
                link type="primary" data-testid="sd-qc" @click.stop="openQc(row)">质控</el-button>
              <el-button v-if="row.qcStatus === 1 && row.reportStatus === 0" v-perm="'qc:singleDisease:edit'"
                link type="success" data-testid="sd-report" @click.stop="doReport(row)">上报</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="caseQuery.pageNum" v-model:page-size="caseQuery.pageSize"
          :page-sizes="PAGE_SIZES" :total="casePage.total" layout="total, sizes, prev, pager, next"
          data-testid="sd-pagination" @change="loadCases" />
      </el-tab-pane>
    </el-tabs>

    <!-- 病种维护 -->
    <el-dialog v-model="diseaseDlg" title="病种维护" width="460px">
      <el-form label-width="110px">
        <el-form-item label="病种编码"><el-input v-model="diseaseForm.diseaseCode" data-testid="sd-code" /></el-form-item>
        <el-form-item label="病种名称"><el-input v-model="diseaseForm.diseaseName" data-testid="sd-name" /></el-form-item>
        <el-form-item label="纳入 ICD 前缀">
          <el-input v-model="diseaseForm.icd10Prefix" placeholder="如 I21,I22" data-testid="sd-prefix" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="diseaseForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="sd-disease-cancel" @click="diseaseDlg = false">取消</el-button>
        <el-button v-perm="'qc:singleDisease:add'" type="primary" data-testid="sd-disease-save" @click="saveDisease">保存</el-button>
      </template>
    </el-dialog>

    <!-- 手工纳入 -->
    <el-dialog v-model="enrollDlg" title="手工纳入病例" width="460px">
      <el-form label-width="110px">
        <el-form-item label="病种">
          <el-select v-model="enrollForm.diseaseId" data-testid="sd-enroll-disease" style="width:100%">
            <el-option v-for="d in diseases" :key="d.id" :label="d.diseaseName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="住院ID">
          <el-input v-model.number="enrollForm.admissionId" placeholder="biz_admission.admission_id" data-testid="sd-enroll-admission" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="sd-enroll-cancel" @click="enrollDlg = false">取消</el-button>
        <el-button v-perm="'qc:singleDisease:edit'" type="primary" data-testid="sd-enroll-save" @click="saveEnroll">纳入</el-button>
      </template>
    </el-dialog>

    <!-- 质控 -->
    <el-dialog v-model="qcDlg" title="质控判级" width="460px">
      <el-form label-width="110px">
        <el-form-item label="疗效判定">
          <el-select v-model="qcForm.curativeEffect" data-testid="sd-qc-curative" style="width:100%">
            <el-option v-for="(t, k) in CURATIVE" :key="k" :label="t" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="质控意见"><el-input v-model="qcForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="sd-qc-cancel" @click="qcDlg = false">取消</el-button>
        <el-button v-perm="'qc:singleDisease:edit'" type="primary" data-testid="sd-qc-save" @click="saveQc">提交质控</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
</style>
