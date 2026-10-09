<script setup lang="ts">
/**
 * 放射诊断工作站 · 报告书写台（菜单 414，sql/138）
 *
 * 这个页面只干一件事：**下诊断结论**。
 *   拍片 → 检查工作站 401（技师，本次只到「拍片完成」，写不了报告）
 *   诊断 → 本页（放射诊断医师）
 *   胶片 → 胶片量方 415（技师）
 * 三岗分开的意义：谁拍的片、谁下的诊断在系统里能分出来，报告被退回重写也有痕迹。
 */
import {ref, computed, onMounted} from 'vue'
import {Search, EditPen, Check, Close, Promotion, Document} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getRadioReportListPage, getRadioReportDetailByRecordId, saveRadioReportDraft, submitRadioReport,
  auditRadioReport, rejectRadioReport, publishRadioReport, getRadioTemplateSelectList
} from '@/api/medicaltech'
import {patientGenderText} from '@/lib/patientGender'
import {hasPerm} from '@/lib/perm'
import ExamImagePanel from '@/components/his/ExamImagePanel.vue'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'

interface Row {
  recordId: string
  recordNo: string
  patientName: string
  patientNo?: string
  gender?: number
  age?: number
  itemName: string
  bodyPart?: string
  applyDeptName?: string
  applyDoctorName?: string
  recordStatus?: number
  recordStatusText?: string
  reportId?: string
  reportStatus?: number
  reportStatusText?: string
  writeBy?: string
  writeTime?: string
  positiveFlag?: number
  positiveFlagText?: string
  filmCount?: number
  reportVersion?: number
  rejectReason?: string
  createTime?: string
}

const TABS = [
  {key: 'unwritten', label: '待书写', reportStatus: null, onlyUnwritten: true},
  {key: 'draft', label: '草稿', reportStatus: 0, onlyUnwritten: null},
  {key: 'pending', label: '待审核', reportStatus: 1, onlyUnwritten: null},
  {key: 'reviewed', label: '已审核', reportStatus: 3, onlyUnwritten: null},
  {key: 'published', label: '已发布', reportStatus: 4, onlyUnwritten: null},
  {key: 'all', label: '全部', reportStatus: null, onlyUnwritten: null},
]

const activeTab = ref('pending')
const keyword = ref('')
const dateRange = ref<string[]>([])
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
/** 各栏真实条数（每栏一次 pageSize=1 的探针请求拿 total，不用前端数出来的假数字） */
const tabCounts = ref<Record<string, number>>({})

const canWrite = computed(() => hasPerm('medtech:radioDiagnosis:write'))
const canAudit = computed(() => hasPerm('medtech:radioDiagnosis:audit'))
const canPublish = computed(() => hasPerm('medtech:radioDiagnosis:publish'))

// ========== 列表 ==========

const currentTab = computed(() => TABS.find(t => t.key === activeTab.value) || TABS[5])

const buildQuery = (extra = {}) => ({
  pageNum: pagination.value.pageNum,
  pageSize: pagination.value.pageSize,
  keyword: keyword.value || undefined,
  reportStatus: currentTab.value.reportStatus ?? undefined,
  onlyUnwritten: currentTab.value.onlyUnwritten ?? undefined,
  startDate: dateRange.value?.[0] || undefined,
  endDate: dateRange.value?.[1] || undefined,
  ...extra,
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRadioReportListPage(buildQuery())
    rows.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

/** 各栏条数：只要 total，所以 pageSize 传 1（这不是分页查询，不受用户翻页控制） */
const loadTabCounts = async () => {
  const jobs = TABS.filter(t => t.key !== 'all').map(async (t) => {
    try {
      const res = await getRadioReportListPage({
        pageNum: 1, pageSize: 1,
        reportStatus: t.reportStatus ?? undefined,
        onlyUnwritten: t.onlyUnwritten ?? undefined,
        keyword: keyword.value || undefined,
        startDate: dateRange.value?.[0] || undefined,
        endDate: dateRange.value?.[1] || undefined,
      })
      tabCounts.value[t.key] = res.data?.total || 0
    } catch {
      tabCounts.value[t.key] = 0
    }
  })
  await Promise.all(jobs)
}

const refresh = async () => {
  pagination.value.pageNum = 1
  await Promise.all([loadData(), loadTabCounts()])
}

const handleTabChange = async () => {
  pagination.value.pageNum = 1
  await loadData()
}

const handleSizeChange = (v: number) => {
  pagination.value.pageSize = v
  pagination.value.pageNum = 1
  loadData()
}
const handleCurrentChange = (v: number) => {
  pagination.value.pageNum = v
  loadData()
}

// ========== 报告书写 ==========

const showWrite = ref(false)
const showAudit = ref(false)
const submitting = ref(false)
const detail = ref<any>(null)
const templates = ref<any[]>([])
const form = ref({
  reportId: undefined as string | undefined,
  recordId: undefined as string | undefined,
  templateId: undefined as string | undefined,
  examMethod: '',
  reportContent: '',
  conclusion: '',
  suggestions: '',
  positiveFlag: 0,
  isCritical: 0,
})

const POSITIVE_OPTIONS = [
  {value: 0, label: '未判定'},
  {value: 1, label: '阴性'},
  {value: 2, label: '阳性'},
  {value: 3, label: '未见异常'},
]

const readonlyMode = computed(() => {
  const st = detail.value?.reportStatus
  return st === 4 || st === 5
})

const openWrite = async (row: Row) => {
  try {
    const res = await getRadioReportDetailByRecordId(row.recordId)
    detail.value = res.data
    form.value = {
      reportId: res.data?.reportId,
      recordId: res.data?.recordId,
      templateId: res.data?.templateId,
      examMethod: res.data?.examMethod || '',
      reportContent: res.data?.reportContent || '',
      conclusion: res.data?.conclusion || '',
      suggestions: res.data?.suggestions || '',
      positiveFlag: res.data?.positiveFlag ?? 0,
      isCritical: res.data?.isCritical ?? 0,
    }
    if (!templates.value.length) {
      const t = await getRadioTemplateSelectList(undefined)
      templates.value = t.data || []
    }
    showWrite.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '打开失败')
  }
}

/** 套用模板：把三段模板文本填进表单（会覆盖已写内容，所以先问一句） */
const applyTemplate = async (id: string) => {
  const t = templates.value.find(x => x.id === id)
  if (!t) return
  if (form.value.reportContent?.trim()) {
    try {
      await ElMessageBox.confirm('套用模板会覆盖当前已填写的检查所见 / 印象 / 建议，继续？', '套用模板', {
        confirmButtonText: '覆盖', cancelButtonText: '取消', type: 'warning',
      })
    } catch {
      return
    }
  }
  form.value.examMethod = t.examMethod || form.value.examMethod
  form.value.reportContent = t.findingTpl || ''
  form.value.conclusion = t.impressionTpl || ''
  form.value.suggestions = t.suggestionTpl || ''
}

const payload = () => ({
  reportId: form.value.reportId,
  recordId: form.value.recordId,
  templateId: form.value.templateId,
  examMethod: form.value.examMethod,
  reportContent: form.value.reportContent,
  conclusion: form.value.conclusion,
  suggestions: form.value.suggestions,
  positiveFlag: form.value.positiveFlag,
  isCritical: form.value.isCritical,
})

const doSaveDraft = async () => {
  submitting.value = true
  try {
    const res = await saveRadioReportDraft(payload())
    detail.value = res.data
    form.value.reportId = res.data?.reportId
    ElMessage.success('草稿已保存')
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const doSubmit = async () => {
  if (!form.value.reportContent?.trim()) return ElMessage.warning('请先填写影像所见')
  if (!form.value.conclusion?.trim()) return ElMessage.warning('请先填写影像诊断/印象')
  try {
    await ElMessageBox.confirm('提交后报告进入待审核，并由你完成报告医师签名。确认提交？', '提交审核', {
      confirmButtonText: '提交', cancelButtonText: '再改改', type: 'warning',
    })
  } catch {
    return
  }
  submitting.value = true
  try {
    await submitRadioReport(payload())
    ElMessage.success('已提交审核（报告医师签名已完成）')
    showWrite.value = false
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// ========== 审核 / 退回 ==========

const auditRow = ref<Row | null>(null)
const auditOpinion = ref('')
const auditDetail = ref<any>(null)

const openAudit = async (row: Row) => {
  if (!row.reportId) return ElMessage.warning('该检查还没有报告')
  auditRow.value = row
  auditOpinion.value = ''
  try {
    const res = await getRadioReportDetailByRecordId(row.recordId)
    auditDetail.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载详情失败')
  }
  showAudit.value = true
}

const doAudit = async () => {
  submitting.value = true
  try {
    await auditRadioReport({reportId: auditRow.value?.reportId, reason: auditOpinion.value})
    ElMessage.success('审核通过（审核医师签名已完成）')
    showAudit.value = false
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    // 服务端会拒绝「自己审自己」与「跳过审核」，这两条必须原样弹出来，不能吞掉
    ElMessage.error(e?.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

const doReject = async () => {
  if (!auditOpinion.value?.trim()) return ElMessage.warning('退回必须写明原因')
  submitting.value = true
  try {
    await rejectRadioReport({reportId: auditRow.value?.reportId, reason: auditOpinion.value})
    ElMessage.success('已退回，报告回到草稿')
    showAudit.value = false
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '退回失败')
  } finally {
    submitting.value = false
  }
}

const doPublish = async (row: Row) => {
  if (!row.reportId) return
  try {
    await ElMessageBox.confirm('发布后临床医生与患者端可见该报告，确认发布？', '发布报告', {
      confirmButtonText: '发布', cancelButtonText: '取消', type: 'warning',
    })
  } catch {
    return
  }
  try {
    await publishRadioReport(row.reportId)
    ElMessage.success('报告已发布')
    await Promise.all([loadData(), loadTabCounts()])
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  }
}

const statusTagType = (st?: number) => {
  if (st == null) return 'info'
  if (st === 4) return 'success'
  if (st === 3) return 'success'
  if (st === 1) return 'warning'
  if (st === 5) return 'danger'
  return 'info'
}

onMounted(refresh)
</script>

<template>
  <div v-loading="loading" class="space-y-4">
    <!-- 筛选 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <el-input v-model="keyword" placeholder="患者姓名 / 患者号 / 记录号 / 项目" :prefix-icon="Search"
                  class="!w-72" clearable @keyup.enter="refresh"/>
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
                        start-placeholder="开始日期" end-placeholder="截止日期" class="!w-64" @change="refresh"/>
        <el-button type="primary" @click="refresh">查询</el-button>
        <el-button @click="keyword = ''; dateRange = []; refresh()">重置</el-button>
      </div>
    </div>

    <!-- 分栏 -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="px-4 pt-2">
        <el-tab-pane v-for="t in TABS" :key="t.key" :name="t.key">
          <template #label>
            <span>{{ t.label }}</span>
            <span v-if="t.key !== 'all'" class="ml-1 text-xs text-slate-400">({{ tabCounts[t.key] ?? 0 }})</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-table :data="rows" style="width: 100%" size="small">
        <el-table-column prop="recordNo" label="记录号" width="180"/>
        <el-table-column label="患者" width="140">
          <template #default="{ row }">
            <span>{{ row.patientName }}</span>
            <span class="ml-1 text-xs text-slate-400">{{ patientGenderText(row.gender) }} {{ row.age }}岁</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="检查项目" min-width="140"/>
        <el-table-column prop="bodyPart" label="部位" width="110"/>
        <el-table-column prop="applyDeptName" label="申请科室" width="120"/>
        <el-table-column prop="applyDoctorName" label="申请医生" width="100"/>
        <el-table-column label="报告状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="statusTagType(row.reportStatus)">
              {{ row.reportStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报告医师" width="110">
          <template #default="{ row }">
            <span class="text-xs">{{ row.writeBy || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="阴阳性" width="100">
          <template #default="{ row }">
            <span class="text-xs">{{ row.positiveFlagText || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="胶片" width="80" align="center">
          <template #default="{ row }">
            <span class="text-xs">{{ row.filmCount ?? 0 }} 张</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'medtech:radioDiagnosis:write'" type="primary" link size="small"
                       @click="openWrite(row)">
              <el-icon class="mr-0.5">
                <EditPen/>
              </el-icon>
              {{ row.reportId ? '查看/修改' : '写报告' }}
            </el-button>
            <el-button v-if="row.reportStatus === 1" v-perm="'medtech:radioDiagnosis:audit'" type="warning" link
                       size="small" @click="openAudit(row)">
              <el-icon class="mr-0.5">
                <Check/>
              </el-icon>
              审核
            </el-button>
            <el-button v-if="row.reportStatus === 3" v-perm="'medtech:radioDiagnosis:publish'" type="success" link
                       size="small" @click="doPublish(row)">
              <el-icon class="mr-0.5">
                <Promotion/>
              </el-icon>
              发布
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-3 flex justify-end border-t border-slate-100 px-4 py-3">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="pagination.total"
                       layout="total, sizes, prev, pager, next, jumper"
                       @size-change="handleSizeChange" @current-change="handleCurrentChange"/>
      </div>
    </div>

    <!-- 报告书写 -->
    <el-dialog v-model="showWrite" width="1100px" destroy-on-close
               :title="`放射报告书写 - ${detail?.patientName || ''} ${detail?.itemName || ''}`">
      <template v-if="detail">
        <div class="grid grid-cols-5 gap-4">
          <!-- 左：阅片 -->
          <div class="col-span-2 space-y-3">
            <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <div><span class="text-slate-400">患者：</span>{{ detail.patientName }}
                {{ patientGenderText(detail.gender) }} {{ detail.age }}岁</div>
              <div><span class="text-slate-400">记录号：</span>{{ detail.recordNo }}</div>
              <div><span class="text-slate-400">部位：</span>{{ detail.bodyPart || '—' }}</div>
              <div><span class="text-slate-400">申请：</span>{{ detail.applyDeptName || '—' }}
                {{ detail.applyDoctorName || '' }}</div>
              <div><span class="text-slate-400">临床诊断：</span>{{ detail.clinicalDiagnosis || '—' }}</div>
              <div><span class="text-slate-400">胶片：</span>{{ detail.filmCount ?? 0 }} 张</div>
            </div>
            <ExamImagePanel :biz-type="1" :apply-id="detail.applyId" readonly/>
          </div>
          <!-- 右：书写 -->
          <div class="col-span-3 space-y-3">
            <div class="flex items-center gap-2">
              <span class="text-sm text-slate-600">报告模板</span>
              <el-select v-model="form.templateId" placeholder="选择模板（可选）" class="!w-64" clearable
                         @change="applyTemplate">
                <el-option v-for="t in templates" :key="t.id" :label="t.templateName" :value="t.id"/>
              </el-select>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">检查方法</label>
              <el-input v-model="form.examMethod" :disabled="readonlyMode"
                        placeholder="如：胸部CT平扫，层厚5mm"/>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">影像所见
                <span class="text-red-500">*</span></label>
              <el-input v-model="form.reportContent" type="textarea" :disabled="readonlyMode"
                        :autosize="{ minRows: 6, maxRows: 12 }" placeholder="描述影像所见…"/>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">影像诊断/印象
                <span class="text-red-500">*</span></label>
              <el-input v-model="form.conclusion" type="textarea" :disabled="readonlyMode"
                        :autosize="{ minRows: 3, maxRows: 6 }" placeholder="给出诊断意见…"/>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">建议</label>
              <el-input v-model="form.suggestions" type="textarea" :disabled="readonlyMode"
                        :autosize="{ minRows: 2, maxRows: 4 }"/>
            </div>
            <div class="flex items-center gap-6">
              <div class="flex items-center gap-2">
                <span class="text-sm text-slate-600">阴阳性</span>
                <el-select v-model="form.positiveFlag" class="!w-32" :disabled="readonlyMode">
                  <el-option v-for="o in POSITIVE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
                </el-select>
              </div>
              <el-checkbox v-model="form.isCritical" :true-label="1" :false-label="0"
                           :disabled="readonlyMode">危急</el-checkbox>
            </div>
            <div v-if="detail.rejectReason" class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs">
              <span class="font-medium text-amber-700">上次退回原因：</span>{{ detail.rejectReason }}
              <span class="ml-2 text-slate-400">（第 {{ detail.reportVersion }} 版）</span>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showWrite = false">关闭</el-button>
        <el-button v-if="!readonlyMode" v-perm="'medtech:radioDiagnosis:write'" :loading="submitting"
                   @click="doSaveDraft">
          <el-icon class="mr-0.5">
            <Document/>
          </el-icon>
          保存草稿
        </el-button>
        <el-button v-if="!readonlyMode" v-perm="'medtech:radioDiagnosis:write'" type="primary" :loading="submitting"
                   @click="doSubmit">
          <el-icon class="mr-0.5">
            <Check/>
          </el-icon>
          提交审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核 -->
    <el-dialog v-model="showAudit" width="900px" destroy-on-close title="放射报告审核">
      <template v-if="auditDetail">
        <div class="space-y-3">
          <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
            <span class="text-slate-400">患者：</span>{{ auditDetail.patientName }}
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">项目：</span>{{ auditDetail.itemName }}
            <span class="mx-2 text-slate-300">|</span>
            <span class="text-slate-400">报告医师：</span>{{ auditDetail.writeBy || '—' }}
          </div>
          <div class="rounded-lg border border-slate-200 p-3">
            <p class="mb-1 text-sm font-medium text-slate-700">影像所见</p>
            <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.reportContent || '—' }}</p>
          </div>
          <div class="rounded-lg border border-slate-200 p-3">
            <p class="mb-1 text-sm font-medium text-slate-700">影像诊断/印象</p>
            <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.conclusion || '—' }}</p>
          </div>
          <div v-if="auditDetail.suggestions" class="rounded-lg border border-slate-200 p-3">
            <p class="mb-1 text-sm font-medium text-slate-700">建议</p>
            <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.suggestions }}</p>
          </div>
          <ExamImagePanel :biz-type="1" :apply-id="auditDetail.applyId" readonly/>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">
              审核意见<span class="text-red-500" v-if="false">*</span>
            </label>
            <el-input v-model="auditOpinion" type="textarea" :rows="3"
                      placeholder="审核意见（选填）；点「退回」时这里是必填的退回原因"/>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showAudit = false">取消</el-button>
        <el-button v-perm="'medtech:radioDiagnosis:audit'" type="danger" :loading="submitting" @click="doReject">
          <el-icon class="mr-0.5">
            <Close/>
          </el-icon>
          退回重写
        </el-button>
        <el-button v-perm="'medtech:radioDiagnosis:audit'" type="primary" :loading="submitting" @click="doAudit">
          <el-icon class="mr-0.5">
            <Check/>
          </el-icon>
          审核通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
