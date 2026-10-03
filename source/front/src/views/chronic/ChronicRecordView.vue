<script setup>
/**
 * 慢病建档与认定（菜单 2051 / 路由 /chronic-record，菜单化见 sql/165）
 *
 * 这张表此前只有小程序 M1 在用，桌面端无从查看 —— 结果是「长处方能不能开」这件事
 * 只能靠医生口头判断，而它本来是**唯一的判定依据**（EmrServiceImpl 开方侧校验）。
 *
 * 口径（服务端为准，前端不自判）：
 *  - 建档即认定：保存就是 confirmStatus=1，认定医生=当前登录医生、科室=当前科室。
 *  - 同患者同一慢病编码只允许一条有效档案 —— 重复建档会被服务端拒绝，前端不拦、不静默。
 *  - 作废是单向的（已认定→已取消），作废后长处方资格立即失效；要恢复只能重新建档。
 *  - 慢病字典 his_chronic_disease 的 dict_value 就是 ICD-10 编码，选中即回填 code+name。
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, CircleClose } from '@element-plus/icons-vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { getChronicRecordList, upsertChronicRecord, cancelChronicRecord } from '@/api/chronic'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')

// ---------------- 字典 ----------------
const confirmStatusDict = ref([])
const diseaseDict = ref([])
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList([DICT_TYPE.CHRONIC_CONFIRM_STATUS, DICT_TYPE.CHRONIC_DISEASE].join(','))
    const d = res?.data || {}
    confirmStatusDict.value = d[DICT_TYPE.CHRONIC_CONFIRM_STATUS] || []
    diseaseDict.value = d[DICT_TYPE.CHRONIC_DISEASE] || []
  } catch (e) { console.error('加载慢病字典失败', e) }
}
const statusText = (v) => dictLabelText(confirmStatusDict.value, v)
const statusTagType = (v) => ({ 0: 'info', 1: 'success', 2: 'info' }[v] || 'info')

// ---------------- 列表（条件下推后端，禁止前端切片） ----------------
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
  patientId: null, patientName: '', recordNo: '', diseaseKeyword: '', confirmStatus: null,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadRows = async () => {
  loading.value = true
  try {
    const res = await getChronicRecordList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      patientId: query.patientId || undefined,
      patientName: query.patientName?.trim() || undefined,
      recordNo: query.recordNo?.trim() || undefined,
      diseaseKeyword: query.diseaseKeyword?.trim() || undefined,
      confirmStatus: query.confirmStatus === null || query.confirmStatus === '' ? undefined : Number(query.confirmStatus),
    })
    if (res.code === 200) { rows.value = res.data?.records || []; total.value = res.data?.total || 0 }
    else ElMessage.error(res.message || '加载慢病档案失败')
  } catch (e) { console.error(e); ElMessage.error('加载慢病档案失败') } finally { loading.value = false }
}
const resetQuery = () => {
  Object.assign(query, { patientId: null, patientName: '', recordNo: '', diseaseKeyword: '', confirmStatus: null, pageNum: 1 })
  loadRows()
}

// ---------------- 建档 ----------------
const openVisible = ref(false)
const submitting = ref(false)
const form = reactive({ patientId: null, patientName: '', patientNo: '', diseaseCode: '', diseaseName: '', remark: '' })
const onPatientSelect = (p) => {
  form.patientName = p?.patientName || ''
  form.patientNo = p?.patientNo || p?.patientNoStr || ''
}
const onDiseaseChange = (code) => {
  const d = diseaseDict.value.find((x) => String(x.dictValue) === String(code))
  form.diseaseName = d?.dictLabel || ''
}
const onCreate = () => {
  Object.assign(form, { patientId: null, patientName: '', patientNo: '', diseaseCode: '', diseaseName: '', remark: '' })
  openVisible.value = true
}
const onSubmit = async () => {
  if (!form.patientId) return ElMessage.warning('请选择患者')
  if (!form.diseaseCode) return ElMessage.warning('请选择慢病病种')
  if (!form.diseaseName?.trim()) return ElMessage.warning('请填写慢病名称')
  submitting.value = true
  try {
    const res = await upsertChronicRecord({
      patientId: form.patientId,
      patientNo: form.patientNo,
      patientName: form.patientName,
      diseaseCode: form.diseaseCode,
      diseaseName: form.diseaseName,
      remark: form.remark?.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success('建档已认定')
      openVisible.value = false
      await loadRows()
    } else ElMessage.error(res.message || '建档失败')
  } catch (e) { console.error(e) } finally { submitting.value = false }
}

// ---------------- 作废（单向） ----------------
const doCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `作废后「${row.patientName}」的「${row.diseaseName}」不再是有效慢病档案，长处方开方资格立即失效。继续？`,
      '作废慢病档案', { type: 'warning', confirmButtonText: '确认作废', cancelButtonText: '取消' }
    )
  } catch { return }
  try {
    const res = await cancelChronicRecord(String(row.id))
    if (res.code === 200) { ElMessage.success('已作废'); await loadRows() }
    else ElMessage.error(res.message || '作废失败')
  } catch (e) { console.error(e) }
}

// 统计卡：只按当前查询结果的有效行数，不冒充全院慢病人数
const activeInPage = computed(() => rows.value.filter((r) => Number(r.confirmStatus) === 1).length)

onMounted(async () => {
  await loadDicts()
  await loadRows()
})
</script>

<template>
  <div class="chronic-record-view" data-testid="chronic-record-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="患者姓名">
            <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width:140px"
                      data-testid="chronic-filter-name" @keyup.enter="query.pageNum = 1; loadRows()" />
          </el-form-item>
          <el-form-item label="档案编号">
            <el-input v-model="query.recordNo" placeholder="档案编号" clearable style="width:150px" data-testid="chronic-filter-no" />
          </el-form-item>
          <el-form-item label="慢病名称">
            <el-input v-model="query.diseaseKeyword" placeholder="慢病名称 / ICD-10" clearable style="width:170px" data-testid="chronic-filter-disease" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.confirmStatus" placeholder="全部状态" clearable style="width:120px" data-testid="chronic-filter-status">
              <el-option v-for="d in confirmStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" data-testid="chronic-search-btn" @click="query.pageNum = 1; loadRows()">查询</el-button>
            <el-button :icon="Refresh" data-testid="chronic-reset-btn" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <span class="muted">本页有效档案 {{ activeInPage }} 条</span>
          <el-button v-perm="'opd:chronicRecord:add'" type="primary" :icon="Plus" data-testid="chronic-create-btn" @click="onCreate">慢病建档</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="chronic-table">
      <el-table-column prop="recordNo" label="档案编号" width="180" />
      <el-table-column prop="patientName" label="患者" width="100" />
      <el-table-column prop="patientNo" label="患者号" width="120" />
      <el-table-column prop="diseaseName" label="慢病名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="diseaseCode" label="ICD-10" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.confirmStatus)" size="small" :data-testid="`chronic-status-${row.recordNo}`">
            {{ statusText(row.confirmStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="doctorName" label="认定医生" width="100" />
      <el-table-column prop="deptName" label="认定科室" width="130" show-overflow-tooltip />
      <el-table-column label="认定时间" width="150"><template #default="{ row }">{{ fmtTime(row.confirmTime) }}</template></el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button v-if="Number(row.confirmStatus) === 1" v-perm="'opd:chronicRecord:cancel'" link type="danger" :icon="CircleClose"
                     :data-testid="`chronic-cancel-${row.recordNo}`" @click.stop="doCancel(row)">作废</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <template #empty><span class="muted">还没有慢病档案 —— 建档即认定，没有有效档案的患者开不出长处方</span></template>
    </el-table>
    <div ref="footerRef" class="list-footer flex items-center justify-end">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="PAGE_SIZES"
                     :total="total" layout="total, sizes, prev, pager, next"
                     data-testid="chronic-pagination" @current-change="loadRows" @size-change="query.pageNum = 1; loadRows()" />
    </div>
    </el-card>

    <el-dialog v-model="openVisible" title="慢病建档（建档即认定）" width="560px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="患者" required>
          <PatientSelect v-model="form.patientId" @select="onPatientSelect" data-testid="chronic-form-patient" />
        </el-form-item>
        <el-form-item label="慢病病种" required>
          <el-select v-model="form.diseaseCode" filterable style="width:100%" data-testid="chronic-form-disease" @change="onDiseaseChange">
            <el-option v-for="d in diseaseDict" :key="d.dictValue" :label="`${d.dictLabel}（${d.dictValue}）`" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="慢病名称" required>
          <el-input v-model="form.diseaseName" data-testid="chronic-form-disease-name" placeholder="选中病种自动回填，可手动补并发症描述" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" data-testid="chronic-form-remark" placeholder="诊断依据 / 认定材料说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" data-testid="chronic-form-save" @click="onSubmit">保存并认定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.muted { color: var(--el-text-color-secondary); font-size: 12px; }
</style>
