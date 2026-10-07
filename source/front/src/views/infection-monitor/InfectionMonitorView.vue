<script setup>
/**
 * 院感监测（L10，菜单 614 / 路由 /infection-monitor）
 *
 * 三个 tab：病例报告卡（报卡→感控核实，漏报补报 leakFlag=1）→
 * 目标性监测（登记→每日打卡=导管日→感染确认/拔管，感染率‰ 后端算）→
 * 手卫生依从性（观察登记只增不改，依从率后端聚合）。
 * 口径：文案取后端 VO 的 *Text；漏报不设独立状态（补报建卡 + 统计口径）。
 */
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Refresh, Search} from '@element-plus/icons-vue'
import PatientSelect from '@/components/his/PatientSelect.vue'
import HandHygienePanel from '@/components/his/HandHygienePanel.vue'
import request from '@/api/request'
import {getDictDataMapList} from '@/api/system'
import {DICT_TYPE} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {
  caseAudit,
  caseUpsert,
  getCaseListPage,
  getCaseStats,
  getMonitorDailyList,
  getMonitorListPage,
  getMonitorStats,
  monitorAdd,
  monitorConfirmInfection,
  monitorPunchDaily,
  monitorRemove,
} from '@/api/infectionMonitor'
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination'

const activeTab = ref('case')
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')
const fmtDate = (d) => (d ? String(d).slice(0, 10) : '—')
const pct = (v) => (v === null || v === undefined ? '—' : Number(v).toFixed(1))

// ---------------- 字典（筛选用；列表文案取后端 *Text） ----------------
const caseStatusDict = ref([])
const caseSourceDict = ref([])
const siteDict = ref([])
const monitorTypeDict = ref([])
const monitorStatusDict = ref([])
// 手卫生观察对象字典已随 HandHygienePanel 组件自带，本页不再加载
const siteText = (v) => dictLabelText(siteDict.value, v)
const loadDicts = async () => {
  try {
    // 铁律：getDictDataMapList 一次最多 5 个 type —— 刚好 5 个，别再往上加
    const r1 = await getDictDataMapList([DICT_TYPE.INFECTION_CASE_STATUS, DICT_TYPE.INFECTION_SOURCE, DICT_TYPE.INFECTION_SITE, DICT_TYPE.INFECTION_MONITOR_TYPE, DICT_TYPE.INFECTION_MONITOR_STATUS].join(','))
    const d1 = r1?.data || {}
    caseStatusDict.value = d1[DICT_TYPE.INFECTION_CASE_STATUS] || []
    caseSourceDict.value = d1[DICT_TYPE.INFECTION_SOURCE] || []
    siteDict.value = d1[DICT_TYPE.INFECTION_SITE] || []
    monitorTypeDict.value = d1[DICT_TYPE.INFECTION_MONITOR_TYPE] || []
    monitorStatusDict.value = d1[DICT_TYPE.INFECTION_MONITOR_STATUS] || []
  } catch (e) {
    console.error('加载字典失败', e)
  }
}

// ================= 病例报告卡 =================
const caseStats = ref({})
const loadCaseStats = async () => {
  try {
    const res = await getCaseStats()
    if (res.code === 200) caseStats.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}
const caseQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  caseStatus: null,
  caseSource: null,
  leakFlag: null,
  keyword: ''
})
const caseRows = ref([])
const caseTotal = ref(0)
const caseLoading = ref(false)
const loadCases = async () => {
  caseLoading.value = true
  try {
    const res = await getCaseListPage({...caseQuery})
    if (res.code === 200) {
      caseRows.value = res.data?.records || [];
      caseTotal.value = res.data?.total || 0
    } else ElMessage.error(res.message || '加载病例失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载病例失败')
  } finally {
    caseLoading.value = false
  }
}
const resetCaseQuery = () => {
  Object.assign(caseQuery, {caseStatus: null, caseSource: null, leakFlag: null, keyword: '', pageNum: 1});
  loadCases()
}

// 就诊锚点选项（门诊挂号单 / 住院记录）
const loadRegists = async (patientId) => {
  anchor.registOptions = [];
  anchor.inpOptions = []
  anchor.registId = null;
  anchor.inpId = null
  if (!patientId) return
  try {
    const [r1, r2] = await Promise.all([
      request.get('/appoint/listPage', {params: {patientId, pageNum: 1, pageSize: 20}}),
      request.get('/patient/inpatient/listPage', {params: {patientId, pageNum: 1, pageSize: 20}}),
    ])
    if (r1.code === 200) anchor.registOptions = r1.data?.records || []
    if (r2.code === 200) anchor.inpOptions = r2.data?.records || []
  } catch (e) {
    console.error(e)
  }
}

const caseOpenVisible = ref(false)
const caseOpenTitle = ref('院感报卡')
const anchor = reactive({registOptions: [], inpOptions: [], registId: null, inpId: null})
const caseForm = reactive({
  id: null,
  patientId: null,
  patientName: '',
  visitType: 2,
  caseSource: 2,
  infectionSite: null,
  infectionDiag: '',
  pathogen: '',
  specimen: '',
  infectDate: '',
  leakFlag: 0,
  remark: ''
})
const onCasePatientSelect = async (p) => {
  caseForm.patientName = p?.patientName || '';
  await loadRegists(p?.id)
}
const onCaseCreate = (leak) => {
  caseOpenTitle.value = leak ? '漏报补报建卡' : '院感报卡'
  Object.assign(caseForm, {
    id: null,
    patientId: null,
    patientName: '',
    visitType: 2,
    caseSource: 2,
    infectionSite: null,
    infectionDiag: '',
    pathogen: '',
    specimen: '',
    infectDate: '',
    leakFlag: leak ? 1 : 0,
    remark: ''
  })
  anchor.registOptions = [];
  anchor.inpOptions = [];
  anchor.registId = null;
  anchor.inpId = null
  caseOpenVisible.value = true
}
const onSaveCase = async () => {
  if (!caseForm.patientId) return ElMessage.warning('请选择患者')
  if (caseForm.visitType === 1 && !anchor.registId) return ElMessage.warning('门诊病例必须选门诊就诊')
  if (caseForm.visitType === 2 && !anchor.inpId) return ElMessage.warning('住院病例必须选住院记录')
  if (!caseForm.infectionSite) return ElMessage.warning('请选择感染部位')
  if (!caseForm.infectionDiag?.trim()) return ElMessage.warning('请填写感染诊断')
  if (!caseForm.infectDate) return ElMessage.warning('请选择感染日期')
  try {
    const res = await caseUpsert({
      id: caseForm.id, patientId: caseForm.patientId, visitType: caseForm.visitType,
      registId: caseForm.visitType === 1 ? anchor.registId : null,
      inpId: caseForm.visitType === 2 ? anchor.inpId : null,
      caseSource: caseForm.caseSource, infectionSite: caseForm.infectionSite,
      infectionDiag: caseForm.infectionDiag, pathogen: caseForm.pathogen || undefined,
      specimen: caseForm.specimen || undefined, infectDate: caseForm.infectDate,
      leakFlag: caseForm.leakFlag || undefined, remark: caseForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '报卡已保存')
      caseOpenVisible.value = false
      await Promise.all([loadCases(), loadCaseStats()])
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) {
    console.error(e)
  }
}
/** 核实：确认/排除 一次性结论，订正建新卡（后端留痕不覆盖） */
const doCaseAudit = async (row, result) => {
  let remark = ''
  try {
    const {value} = await ElMessageBox.prompt(
        `核实病例 ${row.caseNo}（${row.patientName}），结论：${result === 2 ? '确认感染' : '排除'}。结论一次性，订正需建新卡。`,
        '感控核实', {inputPlaceholder: '核实意见（可选）', confirmButtonText: '确定', cancelButtonText: '取消'})
    remark = value || ''
  } catch (e) {
    return
  }
  try {
    const res = await caseAudit({id: row.id, auditResult: result, auditRemark: remark})
    if (res.code === 200) {
      ElMessage.success('核实完成');
      await Promise.all([loadCases(), loadCaseStats()])
    } else ElMessage.error(res.message || '核实失败')
  } catch (e) {
    console.error(e)
  }
}

// ================= 目标性监测 =================
const monitorStats = ref({})
const loadMonitorStats = async () => {
  try {
    const res = await getMonitorStats()
    if (res.code === 200) monitorStats.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}
const monitorQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  monitorType: null,
  status: null,
  infectionFlag: null,
  keyword: ''
})
const monitorRows = ref([])
const monitorTotal = ref(0)
const monitorLoading = ref(false)
const loadMonitors = async () => {
  monitorLoading.value = true
  try {
    const res = await getMonitorListPage({...monitorQuery})
    if (res.code === 200) {
      monitorRows.value = res.data?.records || [];
      monitorTotal.value = res.data?.total || 0
    } else ElMessage.error(res.message || '加载监测失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载监测失败')
  } finally {
    monitorLoading.value = false
  }
}
const resetMonitorQuery = () => {
  Object.assign(monitorQuery, {monitorType: null, status: null, infectionFlag: null, keyword: '', pageNum: 1});
  loadMonitors()
}

const monitorOpenVisible = ref(false)
const monitorAnchor = reactive({inpOptions: [], inpId: null})
const monitorForm = reactive({patientId: null, patientName: '', monitorType: null, insertDate: '', remark: ''})
const onMonitorPatientSelect = async (p) => {
  monitorForm.patientName = p?.patientName || ''
  monitorAnchor.inpOptions = [];
  monitorAnchor.inpId = null
  if (!p?.id) return
  try {
    const res = await request.get('/patient/inpatient/listPage', {params: {patientId: p.id, pageNum: 1, pageSize: 20}})
    if (res.code === 200) monitorAnchor.inpOptions = res.data?.records || []
  } catch (e) {
    console.error(e)
  }
}
const onMonitorCreate = () => {
  Object.assign(monitorForm, {patientId: null, patientName: '', monitorType: null, insertDate: '', remark: ''})
  monitorAnchor.inpOptions = [];
  monitorAnchor.inpId = null
  monitorOpenVisible.value = true
}
const onSaveMonitor = async () => {
  if (!monitorForm.patientId) return ElMessage.warning('请选择患者')
  if (!monitorAnchor.inpId) return ElMessage.warning('导管相关监测必须关联住院记录')
  if (!monitorForm.monitorType) return ElMessage.warning('请选择监测类型')
  if (!monitorForm.insertDate) return ElMessage.warning('请选择置入日期')
  try {
    const res = await monitorAdd({
      patientId: monitorForm.patientId, monitorType: monitorForm.monitorType,
      inpId: monitorAnchor.inpId, insertDate: monitorForm.insertDate, remark: monitorForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '监测登记已保存')
      monitorOpenVisible.value = false
      await Promise.all([loadMonitors(), loadMonitorStats()])
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) {
    console.error(e)
  }
}

const onPunch = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`为 ${row.patientName} 的 ${row.monitorTypeText} 打卡（导管日留痕，同日勿重）`, '每日打卡', {
      inputPlaceholder: '默认今天；格式 2026-09-24', inputValue: '', confirmButtonText: '打卡', cancelButtonText: '取消',
    })
    const body = {monitorId: row.id}
    if (value && value.trim()) body.monitorDate = value.trim()
    const res = await monitorPunchDaily(body)
    if (res.code === 200) {
      ElMessage.success('打卡成功');
      await Promise.all([loadMonitors(), loadMonitorStats()])
    } else ElMessage.error(res.message || '打卡失败')
  } catch (e) {
    if (e !== 'cancel' && e?.message) console.error(e)
  }
}
const onRemove = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`拔管登记：${row.patientName} 的 ${row.monitorTypeText}`, '拔管', {
      inputPlaceholder: '拔除日期，格式 2026-09-24（必填）',
      inputValue: row.insertDate,
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    if (!value || !/^\d{4}-\d{2}-\d{2}$/.test(value.trim())) return ElMessage.warning('请输入拔除日期（yyyy-MM-dd）')
    const res = await monitorRemove({monitorId: row.id, removeDate: value.trim()})
    if (res.code === 200) {
      ElMessage.success('拔管登记完成');
      await Promise.all([loadMonitors(), loadMonitorStats()])
    } else ElMessage.error(res.message || '拔管失败')
  } catch (e) { /* 取消 */
  }
}
const onConfirmInfection = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`感染确认：${row.patientName} 的 ${row.monitorTypeText}。确认后不改在管状态（导管日统计才完整）。`, '感染确认', {
      inputPlaceholder: '感染日期，格式 2026-09-24（必填）',
      inputValue: '',
      confirmButtonText: '下一步',
      cancelButtonText: '取消',
    })
    if (!value || !/^\d{4}-\d{2}-\d{2}$/.test(value.trim())) return ElMessage.warning('请输入感染日期（yyyy-MM-dd）')
    const {value: diag} = await ElMessageBox.prompt('感染诊断（必填）与部位码（可选：1下呼吸道/2泌尿道/3胃肠道/4手术切口/5血流/6皮肤软组织/7腹腔/9其他）', '感染信息', {
      inputPlaceholder: '感染诊断 | 部位码（如：导管相关血流感染 | 5）',
      inputValue: '',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    if (!diag || !diag.split('|')[0].trim()) return ElMessage.warning('请填写感染诊断')
    const [d, site] = diag.split('|')
    const res = await monitorConfirmInfection({
      monitorId: row.id, infectionDate: value.trim(), infectionDiag: d.trim(),
      infectionSite: site && site.trim() ? site.trim() : '9',
    })
    if (res.code === 200) {
      ElMessage.success('感染确认完成');
      await Promise.all([loadMonitors(), loadMonitorStats()])
    } else ElMessage.error(res.message || '感染确认失败')
  } catch (e) { /* 取消 */
  }
}
const dailyVisible = ref(false)
const dailyRows = ref([])
const dailyMonitor = ref(null)
const onShowDaily = async (row) => {
  dailyMonitor.value = row
  try {
    const res = await getMonitorDailyList(row.id)
    if (res.code === 200) dailyRows.value = res.data || []
    dailyVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// ================= 手卫生依从性 =================
// 这块已抽成共用组件 components/his/HandHygienePanel.vue ——
// 因为它同时是「院感监测」的一个页签和「手卫生依从性」独立菜单页（617）的全部内容，
// 拆开维护必然出现两边口径漂移。data-testid 全部保留在原组件里，旧验证脚本照常命中。
onMounted(async () => {
  await loadDicts()
  await Promise.all([loadCaseStats(), loadCases(), loadMonitorStats(), loadMonitors()])
})
</script>

<template>
  <div class="infection-monitor" data-testid="infection-monitor-view">
    <el-tabs v-model="activeTab">
      <!-- ================= 病例报告卡 ================= -->
      <el-tab-pane label="院感病例" name="case">
        <el-row :gutter="8" class="stat-row" data-testid="case-stats">
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-num">{{ caseStats.pendingAudit || 0 }}</div>
              <div class="stat-label">待核实</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-num">{{ caseStats.confirmed || 0 }}</div>
              <div class="stat-label">已确认</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-num">{{ caseStats.excluded || 0 }}</div>
              <div class="stat-label">已排除</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card warn">
              <div class="stat-num">{{ caseStats.leakResubmit || 0 }}</div>
              <div class="stat-label">漏报补报</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card warn">
              <div class="stat-num">{{ caseStats.hospitalInfection || 0 }}</div>
              <div class="stat-label">院内感染（确认）</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-num">{{ caseStats.todayNew || 0 }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </el-col>
        </el-row>

        <div class="toolbar">
          <el-select v-model="caseQuery.caseStatus" clearable placeholder="状态" style="width:120px"
                     @change="caseQuery.pageNum = 1; loadCases()">
            <el-option v-for="d in caseStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
          </el-select>
          <el-select v-model="caseQuery.caseSource" clearable placeholder="感染来源" style="width:130px"
                     @change="caseQuery.pageNum = 1; loadCases()">
            <el-option v-for="d in caseSourceDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
          </el-select>
          <el-select v-model="caseQuery.leakFlag" clearable data-testid="case-leak-filter" placeholder="漏报口径"
                     style="width:130px" @change="caseQuery.pageNum = 1; loadCases()">
            <el-option :value="0" label="正常报卡"/>
            <el-option :value="1" label="漏报补报"/>
          </el-select>
          <el-input v-model="caseQuery.keyword" clearable data-testid="case-keyword" placeholder="病例编号/患者/诊断"
                    style="width:220px" @keyup.enter="caseQuery.pageNum = 1; loadCases()"/>
          <el-button :icon="Search" data-testid="case-search" type="primary"
                     @click="caseQuery.pageNum = 1; loadCases()">查询
          </el-button>
          <el-button :icon="Refresh" @click="resetCaseQuery">重置</el-button>
          <div class="spacer"/>
          <el-button v-perm="'emr:infectionMonitor:add'" data-testid="case-create-leak" plain type="warning"
                     @click="onCaseCreate(true)">漏报补报建卡
          </el-button>
          <el-button v-perm="'emr:infectionMonitor:add'" data-testid="case-create" type="primary"
                     @click="onCaseCreate(false)">院感报卡
          </el-button>
        </div>

        <el-table v-loading="caseLoading" :data="caseRows" border size="small" stripe>
          <el-table-column label="病例编号" prop="caseNo" width="170"/>
          <el-table-column label="患者" prop="patientName" width="90"/>
          <el-table-column label="就诊" width="70">
            <template #default="{ row }">{{ row.visitTypeText }}</template>
          </el-table-column>
          <el-table-column label="发现科室" min-width="110" prop="deptName" show-overflow-tooltip/>
          <el-table-column label="来源" width="90">
            <template #default="{ row }">{{ row.caseSourceText }}</template>
          </el-table-column>
          <el-table-column label="部位" width="90">
            <template #default="{ row }">{{ row.infectionSiteText || siteText(row.infectionSite) }}</template>
          </el-table-column>
          <el-table-column label="感染诊断" min-width="130" prop="infectionDiag" show-overflow-tooltip/>
          <el-table-column label="病原菌" prop="pathogen" show-overflow-tooltip width="100"/>
          <el-table-column label="感染日期" width="100">
            <template #default="{ row }">{{ fmtDate(row.infectDate) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag
                  :type="Number(row.caseStatus) === 2 ? 'success' : Number(row.caseStatus) === 3 ? 'info' : 'warning'"
                  size="small">{{ row.caseStatusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="漏报" width="70">
            <template #default="{ row }">
              <el-tag v-if="Number(row.leakFlag) === 1" size="small" type="danger">漏报补</el-tag>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="上报人" prop="reportName" width="80"/>
          <el-table-column fixed="right" label="操作" width="130">
            <template #default="{ row }">
              <template v-if="Number(row.caseStatus) === 1">
                <el-button data-testid="case-confirm" link size="small" type="success" @click="doCaseAudit(row, 2)">
                  确认
                </el-button>
                <el-button data-testid="case-exclude" link size="small" type="danger" @click="doCaseAudit(row, 3)">
                  排除
                </el-button>
              </template>
              <span v-else class="muted">已核实</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="caseQuery.pageNum" :page-size="caseQuery.pageSize" :total="caseTotal"
                       layout="total, prev, pager, next" style="margin-top:8px" @current-change="loadCases"/>
      </el-tab-pane>

      <!-- ================= 目标性监测 ================= -->
      <el-tab-pane label="目标性监测" name="monitor">
        <el-row :gutter="8" class="stat-row" data-testid="monitor-stats">
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-num">{{ monitorStats.inCatheter || 0 }}</div>
              <div class="stat-label">在管</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-num">{{ monitorStats.removed || 0 }}</div>
              <div class="stat-label">已拔管</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card warn">
              <div class="stat-num">{{ monitorStats.infectionConfirmed || 0 }}</div>
              <div class="stat-label">感染例次</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-num">{{ monitorStats.catheterDays || 0 }}</div>
              <div class="stat-label">导管日（打卡）</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card warn">
              <div class="stat-num">{{ pct(monitorStats.infectionRate) }}‰</div>
              <div class="stat-label">导管相关感染率</div>
            </div>
          </el-col>
        </el-row>

        <div class="toolbar">
          <el-select v-model="monitorQuery.monitorType" clearable placeholder="监测类型" style="width:180px"
                     @change="monitorQuery.pageNum = 1; loadMonitors()">
            <el-option v-for="d in monitorTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
          </el-select>
          <el-select v-model="monitorQuery.status" clearable placeholder="状态" style="width:110px"
                     @change="monitorQuery.pageNum = 1; loadMonitors()">
            <el-option v-for="d in monitorStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
          </el-select>
          <el-select v-model="monitorQuery.infectionFlag" clearable placeholder="感染" style="width:110px"
                     @change="monitorQuery.pageNum = 1; loadMonitors()">
            <el-option :value="0" label="未感染"/>
            <el-option :value="1" label="已确认感染"/>
          </el-select>
          <el-input v-model="monitorQuery.keyword" clearable data-testid="monitor-keyword" placeholder="监测编号/患者"
                    style="width:180px" @keyup.enter="monitorQuery.pageNum = 1; loadMonitors()"/>
          <el-button :icon="Search" data-testid="monitor-search" type="primary"
                     @click="monitorQuery.pageNum = 1; loadMonitors()">查询
          </el-button>
          <el-button :icon="Refresh" @click="resetMonitorQuery">重置</el-button>
          <div class="spacer"/>
          <el-button v-perm="'emr:infectionMonitor:add'" data-testid="monitor-create" type="primary"
                     @click="onMonitorCreate">监测登记
          </el-button>
        </div>

        <el-table v-loading="monitorLoading" :data="monitorRows" border size="small" stripe>
          <el-table-column label="监测编号" prop="monitorNo" width="170"/>
          <el-table-column label="患者" prop="patientName" width="90"/>
          <el-table-column label="类型" width="150">
            <template #default="{ row }">{{ row.monitorTypeText }}</template>
          </el-table-column>
          <el-table-column label="科室" min-width="100" prop="deptName" show-overflow-tooltip/>
          <el-table-column label="置入" width="95">
            <template #default="{ row }">{{ fmtDate(row.insertDate) }}</template>
          </el-table-column>
          <el-table-column label="导管日" width="75">
            <template #default="{ row }"><b>{{ row.catheterDays }}</b></template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="Number(row.status) === 1 ? 'primary' : 'info'" size="small">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="感染" width="80">
            <template #default="{ row }">
              <el-tag v-if="Number(row.infectionFlag) === 1" size="small" type="danger">已确认</el-tag>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="200">
            <template #default="{ row }">
              <el-button data-testid="monitor-punch" link size="small" type="primary" @click="onPunch(row)">打卡
              </el-button>
              <el-button data-testid="monitor-daily" link size="small" type="warning" @click="onShowDaily(row)">导管日
              </el-button>
              <el-button v-if="Number(row.infectionFlag) !== 1" data-testid="monitor-infect" link size="small"
                         type="danger" @click="onConfirmInfection(row)">感染确认
              </el-button>
              <el-button v-if="Number(row.status) === 1" data-testid="monitor-remove" link size="small"
                         @click="onRemove(row)">拔管
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="monitorQuery.pageNum" :page-size="monitorQuery.pageSize"
                       :total="monitorTotal"
                       layout="total, prev, pager, next" style="margin-top:8px" @current-change="loadMonitors"/>
      </el-tab-pane>

      <!-- ================= 手卫生依从性（共用组件，与菜单 617 独立页同源） ================= -->
      <el-tab-pane label="手卫生依从性" name="hand">
        <HandHygienePanel/>
      </el-tab-pane>
    </el-tabs>

    <!-- 病例报卡弹窗 -->
    <el-dialog v-model="caseOpenVisible" :title="caseOpenTitle" destroy-on-close width="560px">
      <el-form label-width="90px">
        <el-form-item v-if="Number(caseForm.leakFlag) === 1" label="漏报口径">
          <el-tag type="danger">漏报调查发现后补报（统计单列）</el-tag>
        </el-form-item>
        <el-form-item label="患者">
          <PatientSelect v-model="caseForm.patientId" data-testid="case-patient"
                         @clear="anchor.registOptions = []; anchor.inpOptions = []"
                         @select="onCasePatientSelect"/>
        </el-form-item>
        <el-form-item label="就诊类型">
          <el-radio-group v-model="caseForm.visitType">
            <el-radio :value="2">住院</el-radio>
            <el-radio :value="1">门诊</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="caseForm.visitType === 1" label="门诊就诊">
          <el-select v-model="anchor.registId" data-testid="case-regist" filterable placeholder="选择门诊就诊"
                     style="width:100%">
            <el-option v-for="r in anchor.registOptions" :key="r.id"
                       :label="`${String(r.id).slice(-6)} ${r.deptName || ''} ${r.visitDate || ''}`" :value="r.id"/>
          </el-select>
        </el-form-item>
        <el-form-item v-else label="住院记录">
          <el-select v-model="anchor.inpId" data-testid="case-inp" filterable placeholder="选择住院记录"
                     style="width:100%">
            <el-option v-for="a in anchor.inpOptions" :key="a.admissionId"
                       :label="`${String(a.admissionId).slice(-6)} ${a.deptName || ''}`" :value="a.admissionId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="感染来源">
          <el-select v-model="caseForm.caseSource" style="width:100%">
            <el-option v-for="d in caseSourceDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
          </el-select>
        </el-form-item>
        <el-form-item label="感染部位">
          <el-select v-model="caseForm.infectionSite" data-testid="case-site" filterable placeholder="选择部位"
                     style="width:100%">
            <el-option v-for="d in siteDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
          </el-select>
        </el-form-item>
        <el-form-item label="感染诊断">
          <el-input v-model="caseForm.infectionDiag" data-testid="case-diag"/>
        </el-form-item>
        <el-form-item label="病原菌">
          <el-input v-model="caseForm.pathogen" placeholder="可选"/>
        </el-form-item>
        <el-form-item label="标本来源">
          <el-input v-model="caseForm.specimen" placeholder="可选"/>
        </el-form-item>
        <el-form-item label="感染日期">
          <el-date-picker v-model="caseForm.infectDate" data-testid="case-infect-date" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="caseForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="caseOpenVisible = false">取消</el-button>
        <el-button v-perm="'emr:infectionMonitor:add'" data-testid="case-save" type="primary" @click="onSaveCase">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 监测登记弹窗 -->
    <el-dialog v-model="monitorOpenVisible" destroy-on-close title="目标性监测登记" width="520px">
      <el-form label-width="90px">
        <el-form-item label="患者">
          <PatientSelect v-model="monitorForm.patientId" data-testid="monitor-patient"
                         @clear="monitorAnchor.inpOptions = []"
                         @select="onMonitorPatientSelect"/>
        </el-form-item>
        <el-form-item label="住院记录">
          <el-select v-model="monitorAnchor.inpId" data-testid="monitor-inp" filterable placeholder="选择住院记录（必选）"
                     style="width:100%">
            <el-option v-for="a in monitorAnchor.inpOptions" :key="a.admissionId"
                       :label="`${String(a.admissionId).slice(-6)} ${a.deptName || ''}`" :value="a.admissionId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="监测类型">
          <el-select v-model="monitorForm.monitorType" data-testid="monitor-type" filterable style="width:100%">
            <el-option v-for="d in monitorTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
          </el-select>
        </el-form-item>
        <el-form-item label="置入日期">
          <el-date-picker v-model="monitorForm.insertDate" data-testid="monitor-insert-date" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="monitorForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="monitorOpenVisible = false">取消</el-button>
        <el-button v-perm="'emr:infectionMonitor:add'" data-testid="monitor-save" type="primary" @click="onSaveMonitor">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 手卫生登记弹窗已随共用组件 HandHygienePanel 搬走（含 data-testid：hand-obs-date / hand-dept / hand-object / hand-opportunity / hand-comply / hand-save） -->

    <!-- 导管日明细 -->
    <el-dialog v-model="dailyVisible"
               :title="`导管日明细 — ${dailyMonitor?.patientName || ''}（${dailyMonitor?.monitorTypeText || ''}）`"
               width="480px">
      <el-table :data="dailyRows" border data-testid="daily-table" max-height="400" size="small">
        <el-table-column label="监测日期" width="120">
          <template #default="{ row }">{{ fmtDate(row.monitorDate) }}</template>
        </el-table-column>
        <el-table-column label="记录人" prop="recorderName" width="100"/>
        <el-table-column label="打卡时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.recordTime) }}</template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" show-overflow-tooltip/>
      </el-table>
      <div style="margin-top:8px">合计导管日：<b data-testid="daily-total">{{ dailyRows.length }}</b> 天</div>
    </el-dialog>
  </div>
</template>

<style scoped>
.stat-row {
  margin-bottom: 10px;
}

.stat-card {
  background: var(--el-fill-color-light);
  border-radius: 6px;
  padding: 10px 14px;
  text-align: center;
}

.stat-card.warn {
  background: var(--el-color-warning-light-9);
}

.stat-card.ok {
  background: var(--el-color-success-light-9);
}

.stat-card.mini {
  padding: 6px 10px;
  font-size: 13px;
  text-align: center;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.toolbar .spacer {
  flex: 1;
}

.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
