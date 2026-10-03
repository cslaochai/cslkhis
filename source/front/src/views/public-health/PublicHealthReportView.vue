<script setup>
/**
 * 公卫上报（菜单 618 / 路由 /public-health，菜单化见 sql/165）
 *
 * 这张表此前是**空表**——不是没人报，是压根没有录入口。法定公共卫生事件（传染病/死因监测/慢病）
 * 报不出去是等级评审与否决项，数据却只能留在纸上，所以这里要补齐「填报 → 审核」的完整闭环。
 *
 * 状态口径以**列注释**为准：1-待审核 2-审核通过 3-审核驳回（曾经 VO 注释写成「待上报/已上报/已审核」，
 * sql/165 已按列注释纠正，前端一律走字典 his_ph_report_status 翻译）。
 *
 * 服务端兜底的三件事，前端不要自己拼：
 *  - 上报单号 PH+时间戳+4位；
 *  - 上报人取当前登录人（前端留空即可）；
 *  - 只有「待审核」能审，审核通过后不可再审 —— 判在服务端，前端只是把按钮藏起来。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, View, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { getPublicHealthList, submitPublicHealth, auditPublicHealth } from '@/api/publicHealth'

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')

// ---------------- 字典 ----------------
const typeDict = ref([])
const statusDict = ref([])
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList([DICT_TYPE.PH_REPORT_TYPE, DICT_TYPE.PH_REPORT_STATUS].join(','))
    const d = res?.data || {}
    typeDict.value = d[DICT_TYPE.PH_REPORT_TYPE] || []
    statusDict.value = d[DICT_TYPE.PH_REPORT_STATUS] || []
  } catch (e) { console.error('加载公卫字典失败', e) }
}
const typeText = (v) => dictLabelText(typeDict.value, v)
const statusText = (v) => dictLabelText(statusDict.value, v)
const statusTagType = (v) => ({ 1: 'warning', 2: 'success', 3: 'danger' }[v] || 'info')

// ---------------- 列表 ----------------
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, reportType: null, reportStatus: null, patientName: '', keyword: '' })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadRows = async () => {
  loading.value = true
  try {
    const res = await getPublicHealthList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      reportType: query.reportType === null || query.reportType === '' ? undefined : Number(query.reportType),
      reportStatus: query.reportStatus === null || query.reportStatus === '' ? undefined : Number(query.reportStatus),
      patientName: query.patientName?.trim() || undefined,
      keyword: query.keyword?.trim() || undefined,
    })
    if (res.code === 200) { rows.value = res.data?.records || []; total.value = res.data?.total || 0 }
    else ElMessage.error(res.message || '加载公卫上报失败')
  } catch (e) { console.error(e); ElMessage.error('加载公卫上报失败') } finally { loading.value = false }
}
const resetQuery = () => {
  Object.assign(query, { reportType: null, reportStatus: null, patientName: '', keyword: '', pageNum: 1 })
  loadRows()
}

// ---------------- 详情（只读） ----------------
const detailVisible = ref(false)
const detail = ref({})
const openDetail = (row) => { detail.value = row; detailVisible.value = true }

// ---------------- 填报 ----------------
const openVisible = ref(false)
const submitting = ref(false)
const form = reactive({ patientId: null, patientNo: '', patientName: '', reportType: null, diagnosis: '', diagnosisCode: '', reportContent: '' })
const onPatientSelect = (p) => {
  form.patientName = p?.patientName || ''
  form.patientNo = p?.patientNo || p?.patientNoStr || ''
}
const onCreate = () => {
  Object.assign(form, { patientId: null, patientNo: '', patientName: '', reportType: null, diagnosis: '', diagnosisCode: '', reportContent: '' })
  openVisible.value = true
}
const onSubmit = async () => {
  if (!form.patientId) return ElMessage.warning('请选择患者')
  if (!form.reportType) return ElMessage.warning('请选择上报类型')
  if (!form.diagnosis?.trim()) return ElMessage.warning('请填写诊断')
  if (!form.reportContent?.trim()) return ElMessage.warning('请填写上报内容')
  submitting.value = true
  try {
    const res = await submitPublicHealth({
      patientId: form.patientId,
      patientNo: form.patientNo,
      patientName: form.patientName,
      reportType: Number(form.reportType),
      diagnosis: form.diagnosis.trim(),
      diagnosisCode: form.diagnosisCode?.trim() || undefined,
      reportContent: form.reportContent.trim(),
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '上报已提交，进入待审核')
      openVisible.value = false
      await loadRows()
    } else ElMessage.error(res.message || '提交失败')
  } catch (e) { console.error(e) } finally { submitting.value = false }
}

// ---------------- 审核（通过 / 驳回都要写意见） ----------------
const doAudit = async (row, approved) => {
  let remark = ''
  try {
    const r = await ElMessageBox.prompt(
      approved ? '审核意见' : '驳回原因（驳回后可由填报人修改再提）',
      approved ? `通过 ${row.reportNo}` : `驳回 ${row.reportNo}`,
      { confirmButtonText: approved ? '通过' : '驳回', cancelButtonText: '取消', inputPlaceholder: '必填，会写进上报记录' }
    )
    remark = String(r?.value || '').trim()
  } catch { return }
  if (!remark) return ElMessage.warning('审核意见不能为空')
  try {
    const res = await auditPublicHealth(String(row.id), { approved, remark })
    if (res.code === 200) { ElMessage.success(res.message || '审核完成'); await loadRows() }
    else ElMessage.error(res.message || '审核失败')
  } catch (e) { console.error(e) }
}

onMounted(async () => {
  await loadDicts()
  await loadRows()
})
</script>

<template>
  <div class="public-health-view" data-testid="public-health-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="上报类型">
            <el-select v-model="query.reportType" placeholder="全部类型" clearable style="width:130px" data-testid="ph-filter-type">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.reportStatus" placeholder="全部状态" clearable style="width:130px" data-testid="ph-filter-status">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="患者姓名">
            <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width:130px" data-testid="ph-filter-name"
                      @keyup.enter="query.pageNum = 1; loadRows()" />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="单号 / 诊断 / 编码" clearable style="width:180px" data-testid="ph-filter-keyword" />
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" data-testid="ph-search-btn" @click="query.pageNum = 1; loadRows()">查询</el-button>
            <el-button :icon="Refresh" data-testid="ph-reset-btn" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'emr:publicHealth:add'" type="primary" :icon="Plus" data-testid="ph-create-btn" @click="onCreate">新建上报</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="ph-table">
        <el-table-column prop="reportNo" label="上报单号" width="180" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="patientNo" label="患者号" width="120" />
        <el-table-column label="上报类型" width="110">
          <template #default="{ row }">{{ typeText(row.reportType) }}</template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断" min-width="160" show-overflow-tooltip />
        <el-table-column prop="diagnosisCode" label="ICD-10" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.reportStatus)" size="small" :data-testid="`ph-status-${row.reportNo}`">
              {{ statusText(row.reportStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportBy" label="上报人" width="100" />
        <el-table-column label="上报时间" width="150"><template #default="{ row }">{{ fmtTime(row.reportTime) }}</template></el-table-column>
        <el-table-column prop="auditBy" label="审核人" width="100" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" :data-testid="`ph-view-${row.reportNo}`" @click.stop="openDetail(row)">详情</el-button>
            <template v-if="Number(row.reportStatus) === 1">
              <el-button v-perm="'emr:publicHealth:edit'" link type="success" :icon="CircleCheck"
                         :data-testid="`ph-pass-${row.reportNo}`" @click.stop="doAudit(row, true)">通过</el-button>
              <el-button v-perm="'emr:publicHealth:edit'" link type="danger" :icon="CircleClose"
                         :data-testid="`ph-reject-${row.reportNo}`" @click.stop="doAudit(row, false)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
        <template #empty><span class="muted">没有上报记录 —— 法定公共卫生事件报不出去是评审否决项，这里现在是空的说明从来没录过</span></template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="PAGE_SIZES"
                       :total="total" layout="total, sizes, prev, pager, next"
                       data-testid="ph-pagination" @current-change="loadRows" @size-change="query.pageNum = 1; loadRows()" />
      </div>
    </el-card>

    <!-- 详情：只读弹框，修改走列表里的「通过/驳回」等动作 -->
    <el-dialog v-model="detailVisible" :title="`上报详情 ${detail.reportNo || ''}`" width="620px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="患者">{{ detail.patientName }}（{{ detail.patientNo }}）</el-descriptions-item>
        <el-descriptions-item label="上报类型">{{ typeText(detail.reportType) }}</el-descriptions-item>
        <el-descriptions-item label="诊断">{{ detail.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="ICD-10">{{ detail.diagnosisCode || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.reportStatus) }}</el-descriptions-item>
        <el-descriptions-item label="上报时间">{{ fmtTime(detail.reportTime) }}</el-descriptions-item>
        <el-descriptions-item label="上报人">{{ detail.reportBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.auditBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="上报内容" :span="2">{{ detail.reportContent || '—' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detail.auditRemark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 填报 -->
    <el-dialog v-model="openVisible" title="新建公卫上报" width="620px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="患者" required>
          <PatientSelect v-model="form.patientId" @select="onPatientSelect" data-testid="ph-form-patient" />
        </el-form-item>
        <el-form-item label="上报类型" required>
          <el-select v-model="form.reportType" style="width:100%" data-testid="ph-form-type">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="诊断" required>
          <el-input v-model="form.diagnosis" data-testid="ph-form-diagnosis" placeholder="如 手足口病 / 新型冠状病毒感染" />
        </el-form-item>
        <el-form-item label="ICD-10">
          <el-input v-model="form.diagnosisCode" data-testid="ph-form-dxcode" placeholder="选填，如 B08.4" />
        </el-form-item>
        <el-form-item label="上报内容" required>
          <el-input v-model="form.reportContent" type="textarea" :rows="3" data-testid="ph-form-content"
                    placeholder="流行病学史 / 临床表现 / 实验室依据 / 已采取措施" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" data-testid="ph-form-save" @click="onSubmit">提交上报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.muted { color: var(--el-text-color-secondary); }
</style>
