<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {CircleClose, DataAnalysis, Printer, Promotion, Refresh, Search, View} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  generateStatReport,
  getStatReportDetail,
  getStatReportList,
  submitStatReport,
  voidStatReport,
} from '@/api/statReport'
import {getDepartmentSelectList} from '@/api/system'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const loading = ref(false)

// 口径与字典 his_stat_report_type / his_stat_period_type / his_stat_report_status 一致
const reportTypeMap: Record<number, string> = {1: '卫统年报', 2: '出院患者统计月报', 3: '手术工作量专项报表'}
const periodTypeMap: Record<number, string> = {1: '月报', 2: '年报'}
const statusMap: Record<number, { label: string; color: string }> = {
  0: {label: '草稿', color: 'bg-slate-100 text-slate-600'},
  1: {label: '已报出', color: 'bg-emerald-100 text-emerald-700'},
  2: {label: '已作废', color: 'bg-red-100 text-red-600'},
}
const reportTypeLabel = (t: number | null | undefined) => (t == null ? '未知' : reportTypeMap[t] || `未知(${t})`)
const statusInfo = (s: number | null | undefined) =>
    (s != null && statusMap[s as number]) || {label: `未知(${s})`, color: 'bg-slate-100 text-slate-500'}

// ========== 生成表单 ==========
const genForm = ref({
  reportType: 1 as number,
  periodType: 2 as number,
  periodValue: String(new Date().getFullYear()),
  deptId: null as string | null,
  remark: '',
})
const deptOptions = ref<any[]>([])
const genLoading = ref(false)

const periodPickerType = computed(() => (genForm.value.periodType === 1 ? 'month' : 'year'))
const periodFormat = computed(() => (genForm.value.periodType === 1 ? 'YYYY-MM' : 'YYYY'))

const onPeriodTypeChange = () => {
  const now = new Date()
  genForm.value.periodValue = genForm.value.periodType === 1
      ? `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      : String(now.getFullYear())
}

const handleGenerate = async () => {
  if (!genForm.value.periodValue) return ElMessage.warning('请选择上报期间')
  genLoading.value = true
  try {
    const res = await generateStatReport({
      reportType: genForm.value.reportType,
      periodType: genForm.value.periodType,
      periodValue: genForm.value.periodValue,
      deptId: genForm.value.deptId ? String(genForm.value.deptId) : null,
      remark: genForm.value.remark.trim(),
    })
    ElMessage.success(res.message || '上报报文已生成')
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '生成失败')
  } finally {
    genLoading.value = false
  }
}

// ========== 台账列表 ==========
const list = ref<any[]>([])
const keyword = ref('')
const typeFilter = ref<number | null>(null)
const statusFilter = ref<number | null>(null)
const periodFilter = ref('')
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const loadList = async () => {
  loading.value = true
  try {
    const params: any = {pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize}
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (typeFilter.value != null) params.reportType = typeFilter.value
    if (statusFilter.value != null) params.status = statusFilter.value
    if (periodFilter.value.trim()) params.periodValue = periodFilter.value.trim()
    const res = await getStatReportList(params)
    list.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (e: any) {
    console.error('加载上报台账失败:', e)
    ElMessage.error(e?.message || '加载上报台账失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  keyword.value = ''
  typeFilter.value = null
  statusFilter.value = null
  periodFilter.value = ''
  pagination.value.pageNum = 1
  loadList()
}

// ========== 报出 / 作废 ==========
const handleSubmit = async (row: any) => {
  try {
    await ElMessageBox.confirm(
        `确认报出「${row.title}」？报出后报文冻结留痕（打印预留：当前不对接外部平台，真实对接时此处即 http 上报埋点）。`,
        '报出确认', {confirmButtonText: '确认报出', cancelButtonText: '取消', type: 'warning'},
    )
    const res = await submitStatReport(row.id)
    ElMessage.success(res.message || '已报出')
    loadList()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message || '报出失败')
  }
}

const handleVoid = async (row: any) => {
  try {
    const {value} = await ElMessageBox.prompt(`作废「${row.title}」后同期间可重新生成；报文留痕不删。`, '作废上报台账', {
      confirmButtonText: '确认作废', cancelButtonText: '取消',
      inputPattern: /^.{2,}$/, inputErrorMessage: '作废原因至少 2 个字', inputPlaceholder: '作废原因',
    })
    await voidStatReport(row.id, value)
    ElMessage.success('台账已作废')
    if (previewVisible.value && previewRow.value?.id === row.id) closePreview()
    loadList()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message || '作废失败')
  }
}

// ========== 报文预览 / 打印 ==========
const previewVisible = ref(false)
const previewRow = ref<any>(null)
const previewPayload = ref<any>(null)
const showRaw = ref(false)
const rawPayload = ref('')

const openPreview = async (row: any) => {
  try {
    const res = await getStatReportDetail(row.id)
    previewRow.value = res.data
    rawPayload.value = res.data?.payload || ''
    previewPayload.value = JSON.parse(res.data?.payload || '{}')
    showRaw.value = false
    previewVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '加载报文失败')
  }
}

const closePreview = () => {
  previewVisible.value = false
  previewRow.value = null
  previewPayload.value = null
}

const esc = (s: any) => String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

const handlePrint = () => {
  const p = previewPayload.value
  const row = previewRow.value
  if (!p || !row) return
  const ind = p.indicators || {}
  const levels: string = (p.operationLevels || [])
      .map((x: any) => `<tr><td>${esc(x.levelLabel)}</td><td>${esc(x.count)}</td></tr>`).join('')
  const ins: string = (p.insuranceTypes || [])
      .map((x: any) => `<tr><td>${esc(x.type)}</td><td>${esc(x.count)}</td><td>${esc(x.amount)}</td></tr>`).join('')
  const dx: string = (p.topDiagnoses || [])
      .map((x: any, i: number) => `<tr><td>${i + 1}</td><td>${esc(x.code)}</td><td>${esc(x.name)}</td><td>${esc(x.count)}</td></tr>`).join('')
  const cases: string = (p.cases || [])
      .map((x: any) => `<tr><td>${esc(x.admissionNo)}</td><td>${esc(x.patientName)}</td><td>${esc(x.deptName)}</td>`
          + `<td>${esc(x.admitTime)}</td><td>${esc(x.dischargeTime)}</td><td>${esc(x.losDays)}</td>`
          + `<td>${esc(x.diagnosisCode)}</td><td>${esc(x.diagnosisName)}</td><td>${esc(x.mainOperation)}</td><td>${esc(x.settleAmount)}</td></tr>`).join('')
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>${esc(row.reportNo)}</title><style>
      body { font-family: "Microsoft YaHei", sans-serif; padding: 24px; color: #111; }
      h1 { font-size: 18px; text-align: center; margin-bottom: 4px; }
      h2 { font-size: 13px; margin-top: 22px; border-bottom: 1px solid #000; padding-bottom: 4px; }
      .meta { text-align: center; font-size: 12px; color: #444; margin-bottom: 14px; }
      table { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
      th, td { border: 1px solid #000; padding: 4px 8px; font-size: 12px; text-align: left; }
      th { background: #f2f2f2; }
      .sign { margin-top: 36px; display: flex; justify-content: space-between; font-size: 12px; }
    </style></head><body>
    <h1>${esc(row.title)}</h1>
    <div class="meta">上报单号：${esc(row.reportNo)}　|　报文码：${esc(p.reportKind)}　|　生成时间：${esc(p.generatedAt)}</div>
    <h2>机构信息（打印预留）</h2>
    <table><tr><th>机构名称</th><td>${esc(p.org?.orgName)}</td><th>机构代码</th><td>${esc(p.org?.orgCode)}</td></tr>
    <tr><th>统计范围</th><td>${esc(p.scope?.deptName)}</td><th>期间</th><td>${esc(p.period?.value)}（${esc(p.period?.start)} ~ ${esc(p.period?.end)}）</td></tr></table>
    <h2>主要指标</h2>
    <table><tr><th>出院例数</th><td>${esc(ind.dischargeCount)}</td><th>死亡例数</th><td>${esc(ind.deathCount)}</td><th>平均住院日</th><td>${esc(ind.avgLosDays)} 天</td></tr>
    <tr><th>手术台次</th><td>${esc(ind.operationCount)}</td><th>三级及以上</th><td>${esc(ind.level3upCount)}</td><th>结算总额</th><td>${esc(ind.totalAmount)} 元</td></tr>
    <tr><th>医保支付</th><td>${esc(ind.insuranceAmount)} 元</td><th>个人支付</th><td>${esc(ind.patientPayAmount)} 元</td><th>欠费</th><td>${esc(ind.arrearsAmount)} 元</td></tr></table>
    <h2>手术级别构成</h2><table><tr><th>级别</th><th>台次</th></tr>${levels || '<tr><td colspan="2">无</td></tr>'}</table>
    <h2>险种构成</h2><table><tr><th>险种</th><th>结算笔数</th><th>金额（元）</th></tr>${ins || '<tr><td colspan="3">无</td></tr>'}</table>
    <h2>主要诊断顺位（前 10）</h2><table><tr><th>#</th><th>ICD 编码</th><th>诊断名称</th><th>例数</th></tr>${dx || '<tr><td colspan="4">无</td></tr>'}</table>
    <h2>病例明细（${(p.cases || []).length} 条）</h2>
    <table><tr><th>住院号</th><th>姓名</th><th>科室</th><th>入院时间</th><th>出院时间</th><th>住院日</th><th>诊断编码</th><th>诊断</th><th>主要手术</th><th>费用</th></tr>
    ${cases || '<tr><td colspan="10">无</td></tr>'}</table>
    <div class="sign"><span>填表人（生成人）：${esc(row.operatorName)}</span><span>报出人：${esc(row.submitByName || '')}</span><span>（加盖机构公章）</span></div>
    </body></html>`
  const win = window.open('', '_blank')
  if (win) {
    win.document.write(html)
    win.document.close()
    win.onload = () => win.print()
  }
}

onMounted(async () => {
  loadList()
  try {
    const res = await getDepartmentSelectList()
    deptOptions.value = res.data || []
  } catch (e) {
    console.error('加载科室下拉失败:', e)
  }
})
</script>

<template>
  <div>
    <!-- 生成报文 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="mb-3 text-sm font-medium text-slate-600">生成上报报文</div>
      <div class="flex flex-wrap items-center gap-3">
        <el-select v-model="genForm.reportType" class="!w-44" :fit-input-width="false">
          <el-option v-for="(label, key) in reportTypeMap" :key="key" :label="label" :value="Number(key)"/>
        </el-select>
        <el-radio-group v-model="genForm.periodType" @change="onPeriodTypeChange">
          <el-radio-button :value="1">月报</el-radio-button>
          <el-radio-button :value="2">年报</el-radio-button>
        </el-radio-group>
        <el-date-picker v-model="genForm.periodValue" :type="periodPickerType" :value-format="periodFormat"
                        placeholder="上报期间" class="!w-40" :clearable="false"/>
        <el-select v-model="genForm.deptId" placeholder="科室（默认全院）" clearable filterable class="!w-44"
                   :fit-input-width="false">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
        </el-select>
        <el-input v-model="genForm.remark" placeholder="备注（可选）" class="!w-52" clearable/>
        <el-button type="primary" :icon="DataAnalysis" :loading="genLoading" v-perm="'report:statReport:add'"
                   @click="handleGenerate">生成报文
        </el-button>
      </div>
    </div>

    <!-- 台账 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="keyword" placeholder="上报单号/标题" :prefix-icon="Search" class="!w-52" clearable
                    @keyup.enter="loadList"/>
        </el-form-item>
        <el-form-item label="报表类型">
          <el-select v-model="typeFilter" placeholder="报表类型" clearable class="!w-40" :fit-input-width="false"
                     @change="loadList">
            <el-option v-for="(label, key) in reportTypeMap" :key="key" :label="label" :value="Number(key)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="状态" clearable class="!w-28" @change="loadList">
            <el-option v-for="(v, key) in statusMap" :key="key" :label="v.label" :value="Number(key)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="期间">
          <el-input v-model="periodFilter" placeholder="期间（2026-09 / 2026）" class="!w-40" clearable
                    @keyup.enter="loadList"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="list" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="reportNo" label="上报单号" width="170" class-name="font-mono"/>
        <el-table-column prop="title" label="报表标题" min-width="200" show-overflow-tooltip/>
        <el-table-column label="类型" width="140" align="center">
          <template #default="{ row }">{{ reportTypeLabel(row.reportType) }}</template>
        </el-table-column>
        <el-table-column label="期间" width="110" align="center">
          <template #default="{ row }">{{ row.periodValue }}（{{ periodTypeMap[row.periodType] || '?' }}）</template>
        </el-table-column>
        <el-table-column label="范围" width="90" align="center">
          <template #default="{ row }">{{ row.deptName || '全院' }}</template>
        </el-table-column>
        <el-table-column label="摘要（出院/手术/死亡/费用）" width="190" align="center">
          <template #default="{ row }">
            <span class="font-mono text-slate-600">
              {{ row.dischargeCount }} / {{ row.operationCount }} / {{ row.deathCount }} / ¥{{ row.totalAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="86" align="center">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 font-medium', statusInfo(row.status).color]">
              {{ statusInfo(row.status).label }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="生成人" width="90"/>
        <el-table-column prop="generateTime" label="生成时间" width="150"/>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openPreview(row)">报文预览</el-button>
            <el-button v-if="row.status === 0" v-perm="'report:statReport:submit'" type="success" link :icon="Promotion"
                       @click="handleSubmit(row)">报出
            </el-button>
            <el-button v-if="row.status !== 2" v-perm="'report:statReport:void'" type="danger" link :icon="CircleClose"
                       @click="handleVoid(row)">作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无上报台账：上方选类型和期间点「生成报文」
      </div>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="pagination.total" layout="total, sizes, prev, pager, next"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 报文预览 -->
    <el-dialog v-model="previewVisible" :title="`上报报文预览 - ${previewRow?.reportNo || ''}`" width="900px" top="4vh"
               @closed="closePreview">
      <div v-if="previewPayload" class="space-y-4">
        <div class="flex items-center justify-between">
          <div class="text-sm font-medium text-slate-700">{{ previewPayload.reportName }}</div>
          <div class="flex items-center gap-3">
            <el-switch v-model="showRaw" active-text="报文原文"/>
            <el-button type="primary" :icon="Printer" @click="handlePrint">打印报文</el-button>
          </div>
        </div>
        <el-alert type="info" :closable="false" show-icon
                  :title="`报文码 ${previewPayload.reportKind}　|　期间 ${previewPayload.period?.start} ~ ${previewPayload.period?.end}　|　范围 ${previewPayload.scope?.deptName}`"/>
        <template v-if="!showRaw">
          <div class="grid grid-cols-5 gap-2">
            <div v-for="(label, key) in {
              dischargeCount: '出院例数', deathCount: '死亡例数', avgLosDays: '平均住院日',
              operationCount: '手术台次', level3upCount: '三级及以上手术',
            }" :key="key" class="rounded border border-slate-200 bg-slate-50 p-2 text-center">
              <div class="text-lg font-semibold text-slate-700">{{ previewPayload.indicators?.[key] }}</div>
              <div class="text-xs text-slate-400">{{ label }}</div>
            </div>
          </div>
          <div class="grid grid-cols-5 gap-2">
            <div v-for="(label, key) in {
              totalAmount: '结算总额', insuranceAmount: '医保支付',
              patientPayAmount: '个人支付', arrearsAmount: '欠费', settleCount: '结算笔数',
            }" :key="key" class="rounded border border-slate-200 bg-slate-50 p-2 text-center">
              <div class="text-sm font-medium text-slate-700">{{ previewPayload.indicators?.[key] }}</div>
              <div class="text-xs text-slate-400">{{ label }}（元）</div>
            </div>
          </div>
          <div>
            <div class="mb-1 text-xs font-medium text-slate-500">手术级别构成</div>
            <el-table :data="previewPayload.operationLevels || []" size="small" border>
              <el-table-column prop="levelLabel" label="级别" width="120"/>
              <el-table-column prop="count" label="台次" width="100"/>
            </el-table>
          </div>
          <div>
            <div class="mb-1 text-xs font-medium text-slate-500">主要诊断顺位</div>
            <el-table :data="previewPayload.topDiagnoses || []" size="small" border>
              <el-table-column prop="code" label="ICD 编码" width="140" class-name="font-mono text-xs"/>
              <el-table-column prop="name" label="诊断" min-width="200" show-overflow-tooltip/>
              <el-table-column prop="count" label="例数" width="80" align="center"/>
            </el-table>
          </div>
          <div>
            <div class="mb-1 text-xs font-medium text-slate-500">病例明细（{{ (previewPayload.cases || []).length }}
              条）
            </div>
            <div class="max-h-56 overflow-y-auto">
              <el-table :data="previewPayload.cases || []" size="small" border>
                <el-table-column prop="admissionNo" label="住院号" width="140" class-name="font-mono text-xs"/>
                <el-table-column prop="patientName" label="姓名" width="100"/>
                <el-table-column prop="deptName" label="科室" width="100"/>
                <el-table-column prop="dischargeTime" label="出院时间" width="130"/>
                <el-table-column prop="losDays" label="住院日" width="70" align="center"/>
                <el-table-column prop="diagnosisName" label="诊断" min-width="140" show-overflow-tooltip/>
                <el-table-column prop="mainOperation" label="主要手术" min-width="160" show-overflow-tooltip/>
                <el-table-column prop="settleAmount" label="费用" width="90" align="right"/>
              </el-table>
            </div>
          </div>
        </template>
        <pre v-else class="max-h-[55vh] overflow-auto rounded bg-slate-900 p-3 text-xs leading-5 text-slate-100">{{
            rawPayload
          }}</pre>
        <div class="rounded border border-amber-200 bg-amber-50 p-2 text-xs text-amber-700">
          {{ previewPayload.reserved?.sendChannel }}
        </div>
      </div>
      <template #footer>
        <el-button @click="closePreview">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
