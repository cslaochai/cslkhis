<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {Search, Refresh, Money, Document, Clock, Check, View, Warning, Tickets, Plus, Delete} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getSettlementList, getInsuranceSettlementStats, getInsuranceSettlementDetailVO} from '@/api/settlement'
import {settle, uploadSettlement, cancelUploadSettlement, getReportListPage, getReportById, reconcileSettlement} from '@/api/settlement'
import {
  getSettlementCoding,
  saveSettlementCoding,
  auditSettlementCompliance,
} from '@/api/compliance'
import {patientGenderText} from '@/lib/patientGender'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const loading = ref(false)
const searchTerm = ref('')
const statusFilter = ref<number | string>('all')

// 统计数据
const stats = ref({
  todayTotal: 0,
  todayInsurancePay: 0,
  todayCount: 0,
  pendingCount: 0,
  settledCount: 0,
})

// 结算记录
const settlementRecords = ref<any[]>([])
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 与后端清单状态机对齐（字典 his_ins_settlement_status）：1-待结算 2-已结算 3-已上传(报盘回执成功) 4-已审核 5-已作废
const statusMap: Record<number, { label: string, type: string }> = {
  1: {label: '待结算', type: 'warning'},
  2: {label: '已结算', type: 'success'},
  3: {label: '已上传', type: 'primary'},
  4: {label: '已审核', type: 'info'},
  5: {label: '已作废', type: 'danger'},
}

// 结算方式只回答「走不走医保」，险种名称在 insuranceType 字段
const settlementTypeMap: Record<number, string> = {
  1: '自费',
  2: '医保',
}

const auditStatusMap: Record<number, { label: string, type: string }> = {
  0: {label: '待审核', type: 'warning'},
  1: {label: '审核通过', type: 'success'},
  2: {label: '审核驳回', type: 'danger'},
}

// 关联账单状态（字典 his_bill_status）
const billStatusMap: Record<number, { label: string, type: string }> = {
  1: {label: '待支付', type: 'warning'},
  2: {label: '部分支付', type: 'warning'},
  3: {label: '已支付', type: 'success'},
  4: {label: '已作废', type: 'info'},
  5: {label: '已退费', type: 'danger'},
}

// 付款项目类型（PaymentItemTypeEnum，1-8）
const itemTypeMap: Record<number, string> = {
  1: '挂号费',
  2: '西药',
  3: '中成药',
  4: '中药饮片',
  5: '检查',
  6: '检验',
  7: '治疗',
  8: '耗材',
}

// 结算清单详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<any>(null)

const openDetail = async (row: any) => {
  detail.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    // 走后端聚合接口，补齐患者/挂号/病历/收费明细
    const res = await getInsuranceSettlementDetailVO(row.id)
    detail.value = res.data || {}
  } catch (error) {
    console.error('加载医保结算清单失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const money = (val: any) => Number(val || 0).toFixed(2)

// ==================== 医保合规审核（防止高编高套 / 低编入组） ====================
const complianceVisible = ref(false)
const complianceLoading = ref(false)
const complianceAuditing = ref(false)
const complianceRow = ref<any>(null)
const diagnoses = ref<any[]>([])
const operations = ref<any[]>([])
const auditResult = ref<any>(null)

// 三态结果的标签样式：命中=红、通过=绿、不适用=灰。
// 文案一律用后端的 resultText，前端不自己拼 —— 否则很容易把「不适用」写成「通过」，
// 而这三态的区别正是这份审核报告值不值得信的全部。
const complianceResultMap: Record<number, string> = {1: 'danger', 2: 'success', 3: 'info'}
const riskLevelMap: Record<number, string> = {0: 'success', 1: 'warning', 2: 'warning', 3: 'danger'}
const diagTypeOptions = [{label: '主要诊断', value: 1}, {label: '其他诊断', value: 2}]
const admitConditionOptions = [
  {label: '有', value: 1}, {label: '临床未确定', value: 2}, {label: '情况不明', value: 3}, {label: '无', value: 4},
]
const ccLevelOptions = [{label: 'NONE', value: 'NONE'}, {label: 'CC', value: 'CC'}, {label: 'MCC', value: 'MCC'}]

const openCompliance = async (row: any) => {
  complianceRow.value = row
  auditResult.value = null
  diagnoses.value = []
  operations.value = []
  complianceVisible.value = true
  complianceLoading.value = true
  try {
    const res = await getSettlementCoding(row.id)
    diagnoses.value = (res.data?.diagnoses || []).map((d: any) => ({...d}))
    operations.value = (res.data?.operations || []).map((o: any) => ({...o}))
  } catch (error: any) {
    ElMessage.error(error?.message || '加载编码明细失败')
  } finally {
    complianceLoading.value = false
  }
}

const addDiagnosis = () => {
  diagnoses.value.push({diagType: diagnoses.value.length === 0 ? 1 : 2, icdCode: '', icdName: '', admitCondition: 1, ccLevel: ''})
}

const addOperation = () => {
  operations.value.push({operCode: '', operName: '', isMain: operations.value.length === 0 ? 1 : 0, operLevel: 2})
}

const saveCoding = async (silent = false) => {
  if (!complianceRow.value) return false
  try {
    await saveSettlementCoding({
      settlementId: complianceRow.value.id,
      diagnoses: diagnoses.value.map((d: any) => ({
        diagType: d.diagType, seqNo: d.seqNo, icdCode: d.icdCode, icdName: d.icdName,
        admitCondition: d.admitCondition, ccLevel: d.ccLevel,
      })),
      operations: operations.value.map((o: any) => ({
        seqNo: o.seqNo, operCode: o.operCode, operName: o.operName, isMain: o.isMain, operLevel: o.operLevel,
      })),
    })
    if (!silent) ElMessage.success('编码明细已保存')
    return true
  } catch (error: any) {
    ElMessage.error(error?.message || '保存失败')
    return false
  }
}

const runComplianceAudit = async () => {
  if (!complianceRow.value) return
  complianceAuditing.value = true
  try {
    // 先落编码明细再审核，保证审的就是屏幕上看到的这一份
    if (!(await saveCoding(true))) return
    const res = await auditSettlementCompliance(complianceRow.value.id, 1)
    auditResult.value = res.data || null
    // 回写后的三态要重新拉，否则表格里还是审核前的旧状态
    const coding = await getSettlementCoding(complianceRow.value.id)
    diagnoses.value = (coding.data?.diagnoses || []).map((d: any) => ({...d}))
    operations.value = (coding.data?.operations || []).map((o: any) => ({...o}))
    const data = res.data
    if (data?.hitCount > 0) {
      ElMessage.warning(`命中 ${data.hitCount} 条风险规则（${data.riskLevelText}）`)
    } else {
      ElMessage.success(`未发现违规，${data?.naCount || 0} 条规则因缺依据未评估`)
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '合规审核失败')
  } finally {
    complianceAuditing.value = false
  }
}

const complianceItems = computed(() => auditResult.value?.items || [])

const fmtTime = (val: any, len = 19) => (val ? String(val).replace('T', ' ').substring(0, len) : '-')

const text = (val: any) => (val === null || val === undefined || val === '' ? '-' : val)

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getInsuranceSettlementStats()
    stats.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载结算记录
const loadSettlements = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    }
    if (searchTerm.value) {
      params.patientName = searchTerm.value
    }
    if (statusFilter.value !== 'all') {
      params.settlementStatus = statusFilter.value
    }
    const res = await getSettlementList(params)
    settlementRecords.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载结算记录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadSettlements()
}

const handleReset = () => {
  searchTerm.value = ''
  statusFilter.value = 'all'
  loadSettlements()
}

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  loadSettlements()
}

const handleCurrentChange = (page: number) => {
  pagination.value.pageNum = page
  loadSettlements()
}

// ==================== G7 医保报盘（上传→回执→撤销→对账） ====================
// 发送位置：清单「已结算」后点『报盘上传』→ POST /charge/settlement/upload →
// 后端组 2304 报文落台账(biz_insurance_report) → InsuranceGateway.send()（当前 Mock，
// 正式环境替换为医保前置机实现）→ 回执成功清单转「已上传」。报文全文在「报文」抽屉里可查。

const acting = ref(false)

const handleSettle = async (row: any) => {
  try {
    await ElMessageBox.confirm(
        `确认对清单 ${row.settlementNo} 执行正式结算？将按统筹比例 ${row.coverageRatio ?? '-'}% 计算医保支付。`,
        '医保结算', {type: 'warning'})
  } catch { return }
  acting.value = true
  try {
    await settle(row.id)
    ElMessage.success('结算完成')
    refreshAll()
  } catch (error: any) {
    ElMessage.error(error?.message || '结算失败')
  } finally {
    acting.value = false
  }
}

const handleUpload = async (row: any) => {
  try {
    await ElMessageBox.confirm(
        `将生成 2304 结算上传报文并发送给医保（当前为 Mock 前置机，报文全文可在「报文」中查看）。确认报盘？`,
        '报盘上传', {type: 'warning'})
  } catch { return }
  acting.value = true
  try {
    await uploadSettlement(row.id)
    ElMessage.success('报盘成功，已收到医保回执')
    refreshAll()
  } catch (error: any) {
    ElMessage.error(error?.message || '报盘失败')
    refreshAll()
  } finally {
    acting.value = false
  }
}

const handleCancelUpload = async (row: any) => {
  let reason = ''
  try {
    const r = await ElMessageBox.prompt(
        `将发送 2305 撤销报文，医保回执成功后清单回到「已结算」。请填写撤销原因：`,
        `撤销报盘 ${row.settlementNo}`, {inputMaxlength: 200, type: 'warning'})
    reason = r.value || ''
  } catch { return }
  acting.value = true
  try {
    await cancelUploadSettlement({id: row.id, reason})
    ElMessage.success('撤销成功，清单已回到「已结算」')
    refreshAll()
  } catch (error: any) {
    ElMessage.error(error?.message || '撤销失败')
  } finally {
    acting.value = false
  }
}

const refreshAll = () => {
  loadStats()
  loadSettlements()
}

// —— 报文台账抽屉 ——
const reportTypeMap: Record<number, string> = {1: '上传 2304', 2: '撤销 2305'}
const reportStatusMap: Record<number, { label: string, type: string }> = {
  0: {label: '待发送', type: 'info'},
  1: {label: '回执成功', type: 'success'},
  2: {label: '回执失败', type: 'danger'},
  3: {label: '已被撤销', type: 'info'},
}
const reportVisible = ref(false)
const reportLoading = ref(false)
const reportRow = ref<any>(null)
const reports = ref<any[]>([])

const openReports = async (row: any) => {
  reportRow.value = row
  reports.value = []
  reportVisible.value = true
  reportLoading.value = true
  try {
    const res = await getReportListPage({settlementId: row.id, pageNum: 1, pageSize: 50})
    reports.value = res.data?.records || []
  } catch (error: any) {
    ElMessage.error(error?.message || '加载报文台账失败')
  } finally {
    reportLoading.value = false
  }
}

const payloadVisible = ref(false)
const payloadLoading = ref(false)
const payloadRow = ref<any>(null)

const viewPayload = async (r: any) => {
  payloadRow.value = null
  payloadVisible.value = true
  payloadLoading.value = true
  try {
    const res = await getReportById(r.id)
    payloadRow.value = res.data || null
  } catch (error: any) {
    ElMessage.error(error?.message || '加载报文全文失败')
  } finally {
    payloadLoading.value = false
  }
}

// —— 日对账 ——
const reconcileVisible = ref(false)
const reconcileLoading = ref(false)
const reconcileDate = ref(new Date().toISOString().slice(0, 10))
const reconcileResult = ref<any>(null)

const openReconcile = () => {
  reconcileResult.value = null
  reconcileVisible.value = true
  runReconcile()
}

const runReconcile = async () => {
  reconcileLoading.value = true
  try {
    const res = await reconcileSettlement({billDate: reconcileDate.value})
    reconcileResult.value = res.data || null
  } catch (error: any) {
    ElMessage.error(error?.message || '对账失败')
  } finally {
    reconcileLoading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadSettlements()
})
</script>

<template>
  <div class="space-y-4">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 gap-4 sm:grid-cols-5">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-blue-50 p-2.5">
            <Money class="h-5 w-5 text-blue-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-blue-600">¥{{ (stats.todayTotal || 0).toLocaleString() }}</p>
            <p class="text-xs text-slate-500">今日总费用</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-emerald-50 p-2.5">
            <Money class="h-5 w-5 text-emerald-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-emerald-600">¥{{ (stats.todayInsurancePay || 0).toLocaleString() }}</p>
            <p class="text-xs text-slate-500">今日医保支付</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-purple-50 p-2.5">
            <Document class="h-5 w-5 text-purple-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-purple-600">{{ stats.todayCount || 0 }}</p>
            <p class="text-xs text-slate-500">今日结算笔数</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5">
            <Clock class="h-5 w-5 text-amber-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-amber-600">{{ stats.pendingCount || 0 }}</p>
            <p class="text-xs text-slate-500">待结算</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-green-50 p-2.5">
            <Check class="h-5 w-5 text-green-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-green-600">{{ stats.settledCount || 0 }}</p>
            <p class="text-xs text-slate-500">已结算</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never">
      <div class="flex items-center gap-4">
        <el-input v-model="searchTerm" placeholder="搜索患者姓名..." :prefix-icon="Search" class="w-64" clearable
                  @keyup.enter="handleSearch" style="width: 200px"/>
        <el-select v-model="statusFilter" style="width: 200px" placeholder="结算状态" @change="handleSearch">
          <el-option label="全部状态" value="all"/>
          <el-option label="待结算" :value="1"/>
          <el-option label="已结算" :value="2"/>
          <el-option label="已上传" :value="3"/>
          <el-option label="已审核" :value="4"/>
          <el-option label="已作废" :value="5"/>
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button class="ml-auto" v-perm="'finance:insurance:edit'" type="warning" plain :icon="Tickets" data-testid="reconcile-btn"
                   @click="openReconcile">报盘日对账</el-button>
      </div>
    </el-card>

    <!-- 结算记录表格 -->
    <el-card shadow="never">
      <el-table :data="settlementRecords" v-loading="loading" stripe>
        <el-table-column prop="settlementNo" label="结算单号" width="250"/>
        <el-table-column prop="patientName" label="患者" width="100"/>
        <el-table-column prop="patientNo" label="患者号" width="200"/>
        <el-table-column prop="billNo" label="关联账单" width="200">
          <template #default="{ row }">
            <span class="font-mono">{{ text(row.billNo) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="insuranceType" label="医保类型" width="130">
          <template #default="{ row }">
            {{ row.settlementType === 1 ? '自费' : (row.insuranceType || '医保') }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总费用" width="110" align="right">
          <template #default="{ row }">
            <span class="font-medium">¥{{ (row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="insurancePay" label="医保支付" width="110" align="right">
          <template #default="{ row }">
            <span class="font-medium text-emerald-600">¥{{ (row.insurancePay || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="personalPay" label="个人自付" width="110" align="right">
          <template #default="{ row }">
            <span class="font-medium text-red-600">¥{{ (row.personalPay || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="settlementStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.settlementStatus]?.type || 'info'" size="small">
              {{ statusMap[row.settlementStatus]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="结算时间" width="160">
          <template #default="{ row }">
            {{ row.createTime?.replace('T', ' ').substring(0, 16) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">
              <el-icon class="mr-0.5">
                <View/>
              </el-icon>
              详情
            </el-button>
            <el-button v-if="row.settlementStatus === 1" v-perm="'finance:insurance:edit'" type="success" link size="small" :disabled="acting"
                       data-testid="settle-btn" @click="handleSettle(row)">结算</el-button>
            <el-button v-if="row.settlementStatus === 2" v-perm="'finance:insurance:add'" type="warning" link size="small" :disabled="acting"
                       data-testid="upload-btn" @click="handleUpload(row)">报盘上传</el-button>
            <el-button v-if="row.settlementStatus === 3" v-perm="'finance:insurance:delete'" type="danger" link size="small" :disabled="acting"
                       data-testid="cancel-btn" @click="handleCancelUpload(row)">撤销报盘</el-button>
            <el-button v-if="row.settlementStatus >= 2" link size="small" type="primary" data-testid="report-btn"
                       @click="openReports(row)">报文</el-button>
            <el-button type="danger" link size="small" @click="openCompliance(row)">
              <el-icon class="mr-0.5">
                <Warning/>
              </el-icon>
              合规审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 医保结算清单详情 -->
    <el-drawer v-model="detailVisible" direction="rtl" size="55%">
      <template #header>
        <div class="flex items-center gap-3">
          <span class="text-base font-semibold text-slate-900">医保结算清单</span>
          <span class="font-mono text-sm text-slate-400">{{ detail?.settlementNo }}</span>
          <el-tag v-if="detail" :type="statusMap[detail.settlementStatus]?.type || 'info'" size="small">
            {{ statusMap[detail.settlementStatus]?.label || '未知' }}
          </el-tag>
        </div>
      </template>

      <div v-loading="detailLoading">
        <div v-if="detail" class="space-y-4">
          <!-- 一、清单信息 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">一、清单信息</h3>
            <div class="grid grid-cols-3 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">结算清单号：</span><span class="font-mono font-medium">{{ text(detail.settlementNo) }}</span></div>
              <div><span class="text-slate-400">关联账单号：</span><span class="font-mono font-medium">{{ text(detail.billNo) }}</span></div>
              <div><span class="text-slate-400">挂号单号：</span><span class="font-mono font-medium">{{ text(detail.registNo) }}</span></div>
              <div><span class="text-slate-400">结算方式：</span><span class="font-medium">{{ settlementTypeMap[detail.settlementType] || '-' }}</span></div>
              <div><span class="text-slate-400">医保类型：</span><span class="font-medium">{{ text(detail.insuranceType) }}</span></div>
              <div><span class="text-slate-400">结算时间：</span><span class="font-medium">{{ fmtTime(detail.createTime) }}</span></div>
              <div><span class="text-slate-400">清单状态：</span>
                <el-tag :type="statusMap[detail.settlementStatus]?.type || 'info'" size="small">
                  {{ statusMap[detail.settlementStatus]?.label || '未知' }}
                </el-tag>
              </div>
              <div v-if="detail.uploadTime"><span class="text-slate-400">上传时间：</span><span class="font-medium">{{ fmtTime(detail.uploadTime) }}</span></div>
            </div>
          </div>

          <!-- 二、患者信息 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">二、患者信息</h3>
            <div class="grid grid-cols-3 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">姓名：</span><span class="font-medium">{{ text(detail.patientName) }}</span></div>
              <div><span class="text-slate-400">性别：</span><span class="font-medium">{{ patientGenderText(detail.gender) }}</span></div>
              <div><span class="text-slate-400">年龄：</span><span class="font-medium">{{ detail.age != null ? detail.age + '岁' : '-' }}</span></div>
              <div><span class="text-slate-400">患者号：</span><span class="font-mono font-medium">{{ text(detail.patientNo) }}</span></div>
              <div><span class="text-slate-400">身份证号：</span><span class="font-mono font-medium">{{ text(detail.idCardMasked) }}</span></div>
              <div><span class="text-slate-400">医保卡号：</span><span class="font-mono font-medium">{{ text(detail.medicalInsuranceNoMasked) }}</span></div>
              <div><span class="text-slate-400">联系电话：</span><span class="font-mono font-medium">{{ text(detail.phoneMasked) }}</span></div>
            </div>
          </div>

          <!-- 三、就诊信息 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">三、就诊信息</h3>
            <div class="grid grid-cols-3 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">就诊日期：</span><span class="font-medium">{{ text(detail.visitDate) }}</span></div>
              <div><span class="text-slate-400">科室：</span><span class="font-medium">{{ text(detail.deptName) }}</span></div>
              <div><span class="text-slate-400">医生：</span><span class="font-medium">{{ text(detail.doctorName) }}</span></div>
              <div><span class="text-slate-400">就诊类型：</span><span class="font-medium">{{ text(detail.visitType) }}</span></div>
              <div class="col-span-2">
                <span class="text-slate-400">诊断：</span>
                <span class="font-medium">{{ detail.diagnosisName || detail.diagnosis || '-' }}</span>
                <span v-if="detail.diagnosisCode" class="ml-2 font-mono text-xs text-slate-400">（{{ detail.diagnosisCode }}）</span>
              </div>
            </div>
          </div>

          <!-- 四、费用明细 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">四、费用明细</h3>
            <div class="rounded border border-slate-200 p-3 text-sm">
              <div class="mb-2 flex items-center justify-between border-b border-slate-100 pb-2">
                <span class="text-slate-500">医疗总费用</span>
                <span class="text-lg font-bold text-blue-600">¥{{ money(detail.totalAmount) }}</span>
              </div>
              <div class="grid grid-cols-3 gap-3">
                <div><span class="text-slate-400">药品费：</span><span class="font-medium">¥{{ money(detail.drugAmount) }}</span></div>
                <div><span class="text-slate-400">检查费：</span><span class="font-medium">¥{{ money(detail.inspectionAmount) }}</span></div>
                <div><span class="text-slate-400">检验费：</span><span class="font-medium">¥{{ money(detail.laboratoryAmount) }}</span></div>
                <div><span class="text-slate-400">治疗费：</span><span class="font-medium">¥{{ money(detail.treatmentAmount) }}</span></div>
                <div><span class="text-slate-400">材料费：</span><span class="font-medium">¥{{ money(detail.materialAmount) }}</span></div>
                <div><span class="text-slate-400">其他费用：</span><span class="font-medium">¥{{ money(detail.otherAmount) }}</span></div>
              </div>

              <!-- 账单行：2304 报盘与合规审核共用的同一份明细口径，逐行带医保 split -->
              <el-table v-if="detail.items?.length" :data="detail.items" size="small" class="mt-3"
                        border>
                <el-table-column label="类型" width="90">
                  <template #default="{ row }">
                    {{ itemTypeMap[row.itemType] || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="itemName" label="项目名称" min-width="140"/>
                <el-table-column prop="specification" label="规格" width="110">
                  <template #default="{ row }">{{ text(row.specification) }}</template>
                </el-table-column>
                <el-table-column label="数量" width="80" align="right">
                  <template #default="{ row }">{{ Number(row.quantity || 0) }}</template>
                </el-table-column>
                <el-table-column label="单价" width="90" align="right">
                  <template #default="{ row }">¥{{ money(row.price) }}</template>
                </el-table-column>
                <el-table-column label="金额" width="100" align="right">
                  <template #default="{ row }">
                    <span class="font-medium">¥{{ money(row.amount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="优惠" width="90" align="right">
                  <template #default="{ row }">¥{{ money(row.discountAmount) }}</template>
                </el-table-column>
                <el-table-column label="统筹" width="95" align="right">
                  <template #default="{ row }">
                    <span class="font-medium text-emerald-600">¥{{ money(row.poolAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="自付" width="95" align="right">
                  <template #default="{ row }">¥{{ money(row.selfAmount) }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- 五、账单信息 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">五、账单信息</h3>
            <div class="grid grid-cols-3 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">结算账单号：</span><span class="font-mono font-medium">{{ text(detail.billNo) }}</span></div>
              <div class="flex items-center gap-2">
                <span class="text-slate-400">账单状态：</span>
                <el-tag v-if="detail.billStatus != null"
                        :type="billStatusMap[detail.billStatus]?.type || 'info'" size="small">
                  {{ billStatusMap[detail.billStatus]?.label || '未知' }}
                </el-tag>
                <span v-else class="font-medium">-</span>
              </div>
              <div><span class="text-slate-400">账务归属日：</span><span class="font-medium">{{ text(detail.billDate) }}</span></div>
              <div><span class="text-slate-400">应收合计：</span><span class="font-medium">¥{{ money(detail.billTotalAmount) }}</span></div>
              <div><span class="text-slate-400">院内优惠：</span><span class="font-medium">¥{{ money(detail.discountAmount) }}</span></div>
              <div><span class="text-slate-400">应缴：</span><span class="font-medium">¥{{ money(detail.payableAmount) }}</span></div>
              <div><span class="text-slate-400">已收：</span><span class="font-medium text-emerald-600">¥{{ money(detail.paidAmount) }}</span></div>
              <div><span class="text-slate-400">已退：</span><span class="font-medium text-red-600">¥{{ money(detail.refundAmount) }}</span></div>
              <div><span class="text-slate-400">出账人：</span><span class="font-medium">{{ text(detail.billByName) }}</span></div>
              <div><span class="text-slate-400">收讫时间：</span><span class="font-medium">{{ fmtTime(detail.payTime) }}</span></div>
              <div><span class="text-slate-400">发票号：</span><span class="font-mono font-medium">{{ text(detail.invoiceNo) }}</span></div>
              <div v-if="detail.admissionNo"><span class="text-slate-400">入院单号：</span><span class="font-mono font-medium">{{ text(detail.admissionNo) }}</span></div>
            </div>
          </div>

          <!-- 六、医保结算 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">六、医保结算</h3>
            <div class="grid grid-cols-2 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">统筹报销比例：</span><span class="font-medium">{{ detail.coverageRatio != null ? detail.coverageRatio + '%' : '-' }}</span></div>
              <div><span class="text-slate-400">医保统筹支付：</span><span class="font-medium text-emerald-600">¥{{ money(detail.insurancePay) }}</span></div>
              <div><span class="text-slate-400">个人账户支付：</span><span class="font-medium">¥{{ money(detail.personalPay) }}</span></div>
              <div><span class="text-slate-400">自费金额：</span><span class="font-medium text-red-600">¥{{ money(detail.selfPay) }}</span></div>
            </div>
          </div>

          <!-- 七、DRG（无数据时不展示） -->
          <div v-if="detail.drgCode || detail.drgWeight != null || detail.estimatedCost != null">
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">七、DRG 分组</h3>
            <div class="grid grid-cols-3 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">DRG 分组编码：</span><span class="font-mono font-medium">{{ text(detail.drgCode) }}</span></div>
              <div><span class="text-slate-400">DRG 权重：</span><span class="font-medium">{{ detail.drgWeight != null ? detail.drgWeight : '-' }}</span></div>
              <div><span class="text-slate-400">预估费用：</span><span class="font-medium">¥{{ money(detail.estimatedCost) }}</span></div>
            </div>
          </div>

          <!-- 八、审核信息 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">
              {{ detail.drgCode || detail.drgWeight != null || detail.estimatedCost != null ? '八' : '七' }}、审核信息
            </h3>
            <div class="rounded border border-slate-200 p-3 text-sm">
              <div class="grid grid-cols-2 gap-3">
                <div class="flex items-center gap-2">
                  <span class="text-slate-400">审核状态：</span>
                  <el-tag v-if="detail.auditStatus != null"
                          :type="auditStatusMap[detail.auditStatus]?.type || 'info'" size="small">
                    {{ auditStatusMap[detail.auditStatus]?.label || '未知' }}
                  </el-tag>
                  <span v-else class="font-medium">未审核</span>
                </div>
                <div><span class="text-slate-400">审核时间：</span><span class="font-medium">{{ fmtTime(detail.auditTime) }}</span></div>
              </div>
              <div class="mt-2"><span class="text-slate-400">审核意见：</span><span class="text-slate-700">{{ text(detail.auditRemark) }}</span></div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 医保合规审核（防止高编高套 / 低编入组） -->
    <el-drawer v-model="complianceVisible" direction="rtl" size="80%">
      <template #header>
        <div class="flex items-center gap-3">
          <span class="text-base font-semibold text-slate-900">医保合规审核</span>
          <span class="font-mono text-sm text-slate-400">{{ complianceRow?.settlementNo }}</span>
          <el-tag type="danger" size="small" effect="plain">高编高套 / 低编入组</el-tag>
        </div>
      </template>

      <div v-loading="complianceLoading" class="space-y-4">
        <!-- 一、编码明细（审核的事实基础） -->
        <div>
          <div class="mb-2 flex items-center justify-between">
            <h3 class="border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">
              一、清单编码明细（诊断 + 手术操作）
            </h3>
            <span class="text-xs text-slate-400">
              清单上的编码是审核的唯一事实来源；没有编码明细，任何规则都无从核对
            </span>
          </div>

          <!-- 诊断 -->
          <div class="rounded border border-slate-200 p-3">
            <div class="mb-2 flex items-center justify-between">
              <span class="text-sm font-semibold text-slate-600">诊断明细</span>
              <el-button type="primary" link size="small" :icon="Plus" @click="addDiagnosis">添加诊断</el-button>
            </div>
            <el-table :data="diagnoses" size="small" border empty-text="暂无诊断明细，请先添加">
              <el-table-column label="类型" width="120">
                <template #default="{ row }">
                  <el-select v-model="row.diagType" size="small" style="width: 100%">
                    <el-option v-for="o in diagTypeOptions" :key="o.value" :label="o.label" :value="o.value"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="ICD-10 编码" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.icdCode" size="small" placeholder="如 E11.9"/>
                </template>
              </el-table-column>
              <el-table-column label="诊断名称" min-width="170">
                <template #default="{ row }">
                  <el-input v-model="row.icdName" size="small" placeholder="如 2型糖尿病"/>
                </template>
              </el-table-column>
              <el-table-column label="入院病情" width="130">
                <template #default="{ row }">
                  <el-select v-model="row.admitCondition" size="small" style="width: 100%">
                    <el-option v-for="o in admitConditionOptions" :key="o.value" :label="o.label" :value="o.value"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="CC/MCC" width="110">
                <template #default="{ row }">
                  <el-select v-model="row.ccLevel" size="small" clearable style="width: 100%">
                    <el-option v-for="o in ccLevelOptions" :key="o.value" :label="o.label" :value="o.value"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="依据核对" width="96" align="center">
                <template #default="{ row }">
                  <el-tooltip v-if="row.evidenceStatusText" :content="row.evidenceNote || '无说明'" placement="top">
                    <el-tag :type="complianceResultMap[row.evidenceStatus] || 'info'" size="small">
                      {{ row.evidenceStatusText }}
                    </el-tag>
                  </el-tooltip>
                  <span v-else class="text-xs text-slate-400">未审核</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" :icon="Delete"
                             @click="diagnoses.splice($index, 1)"/>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 手术操作 -->
          <div class="mt-3 rounded border border-slate-200 p-3">
            <div class="mb-2 flex items-center justify-between">
              <span class="text-sm font-semibold text-slate-600">手术操作明细</span>
              <el-button type="primary" link size="small" :icon="Plus" @click="addOperation">添加手术操作</el-button>
            </div>
            <el-table :data="operations" size="small" border empty-text="暂无手术操作明细">
              <el-table-column label="ICD-9-CM-3 编码" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.operCode" size="small" placeholder="如 47.0900"/>
                </template>
              </el-table-column>
              <el-table-column label="手术操作名称" min-width="200">
                <template #default="{ row }">
                  <el-input v-model="row.operName" size="small" placeholder="如 阑尾切除术"/>
                </template>
              </el-table-column>
              <el-table-column label="主要手术" width="110">
                <template #default="{ row }">
                  <el-select v-model="row.isMain" size="small" style="width: 100%">
                    <el-option label="是" :value="1"/>
                    <el-option label="否" :value="0"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="级别" width="90">
                <template #default="{ row }">
                  <el-select v-model="row.operLevel" size="small" style="width: 100%">
                    <el-option v-for="n in [1, 2, 3, 4]" :key="n" :label="n + ' 级'" :value="n"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="依据核对" width="96" align="center">
                <template #default="{ row }">
                  <el-tooltip v-if="row.evidenceStatusText" :content="row.evidenceNote || '无说明'" placement="top">
                    <el-tag :type="complianceResultMap[row.evidenceStatus] || 'info'" size="small">
                      {{ row.evidenceStatusText }}
                    </el-tag>
                  </el-tooltip>
                  <span v-else class="text-xs text-slate-400">未审核</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" :icon="Delete"
                             @click="operations.splice($index, 1)"/>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mt-3 flex gap-2">
            <el-button v-perm="'finance:insurance:edit'" type="primary" plain size="small" @click="saveCoding(false)">保存编码明细</el-button>
            <el-button v-perm="'finance:insurance:edit'" type="danger" size="small" :icon="Tickets" :loading="complianceAuditing"
                       @click="runComplianceAudit">
              执行合规审核
            </el-button>
          </div>
        </div>

        <!-- 二、审核结论 -->
        <div v-if="auditResult">
          <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">二、审核结论</h3>

          <!-- 三态统计：不适用单独计数，不和通过混在一起 -->
          <div class="grid grid-cols-5 gap-3 text-sm">
            <div class="rounded border border-slate-200 p-3">
              <p class="text-xs text-slate-400">风险等级</p>
              <el-tag :type="riskLevelMap[auditResult.riskLevel] || 'info'" size="small">
                {{ auditResult.riskLevelText || '未评估' }}
              </el-tag>
              <p class="mt-1 text-xs text-slate-400">风险分 {{ auditResult.riskScore }}</p>
            </div>
            <div class="rounded border border-slate-200 p-3">
              <p class="text-xs text-slate-400">命中</p>
              <p class="text-lg font-bold text-red-600">{{ auditResult.hitCount }}</p>
            </div>
            <div class="rounded border border-slate-200 p-3">
              <p class="text-xs text-slate-400">通过</p>
              <p class="text-lg font-bold text-emerald-600">{{ auditResult.passCount }}</p>
            </div>
            <div class="rounded border border-slate-200 p-3">
              <p class="text-xs text-slate-400">不适用（未评估）</p>
              <p class="text-lg font-bold text-slate-400">{{ auditResult.naCount }}</p>
            </div>
            <div class="rounded border border-slate-200 p-3">
              <p class="text-xs text-slate-400">实际费用 / 支付标准</p>
              <p class="font-medium">¥{{ money(auditResult.actualCost) }}</p>
              <p class="text-xs text-slate-400">
                {{ auditResult.payStandard != null
                  ? '¥' + money(auditResult.payStandard) + '（倍率 ' + (auditResult.costRatio ?? '-') + '）'
                  : '未接入 DRG 分组方案' }}
              </p>
            </div>
          </div>

          <el-alert
              class="mt-3"
              :type="auditResult.hitCount > 0 ? 'error' : (auditResult.naCount > 0 ? 'warning' : 'success')"
              :closable="false"
              show-icon
              :title="auditResult.conclusion || '审核完成'"
              :description="auditResult.naCount > 0
                ? '「不适用」表示缺少评估所需依据、规则并未评估，不能当作通过。当前结论不完整。'
                : ''"/>

          <!-- 规则判定明细 -->
          <el-table :data="complianceItems" size="small" border class="mt-3">
            <el-table-column prop="ruleCode" label="规则" width="70"/>
            <el-table-column label="分组" width="120">
              <template #default="{ row }">{{ row.ruleGroupText || row.ruleGroup }}</template>
            </el-table-column>
            <el-table-column prop="ruleName" label="规则名称" min-width="180"/>
            <el-table-column label="结果" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="complianceResultMap[row.result] || 'info'" size="small">
                  {{ row.resultText || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="风险" width="80" align="center">
              <template #default="{ row }">
                <span v-if="row.result === 1" class="text-xs font-medium text-red-600">{{ row.riskLevelText }}</span>
                <span v-else class="text-xs text-slate-300">-</span>
              </template>
            </el-table-column>
            <el-table-column label="对象" width="150">
              <template #default="{ row }">
                <span v-if="row.targetCode" class="font-mono text-xs">{{ row.targetCode }}</span>
                <span v-if="row.targetName" class="ml-1 text-xs text-slate-500">{{ row.targetName }}</span>
                <span v-if="!row.targetCode && !row.targetName" class="text-xs text-slate-300">清单级</span>
              </template>
            </el-table-column>
            <el-table-column prop="evidence" label="判定依据" min-width="280">
              <template #default="{ row }">
                <span :class="row.result === 3 ? 'text-slate-500' : ''">{{ row.evidence || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="suggestion" label="整改建议" min-width="220">
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ row.suggestion || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>

          <p class="mt-2 text-xs text-slate-400">
            审核单号 {{ auditResult.auditNo }} · 审核人 {{ auditResult.auditBy || '-' }} ·
            {{ fmtTime(auditResult.auditTime) }}
          </p>
        </div>

        <el-empty v-else-if="!complianceLoading" description="尚未审核，请先维护编码明细后点「执行合规审核」"/>
      </div>
    </el-drawer>

    <!-- G7 报文台账：每一次发给医保的数据都在这里留痕 -->
    <el-drawer v-model="reportVisible" direction="rtl" size="70%">
      <template #header>
        <div class="flex items-center gap-3">
          <span class="text-base font-semibold text-slate-900">报盘报文台账</span>
          <span class="font-mono text-sm text-slate-400">{{ reportRow?.settlementNo }}</span>
          <el-tag v-if="reportRow" :type="statusMap[reportRow.settlementStatus]?.type || 'info'" size="small">
            {{ statusMap[reportRow.settlementStatus]?.label }}
          </el-tag>
        </div>
      </template>
      <div v-loading="reportLoading" class="space-y-3">
        <el-alert type="info" :closable="false" show-icon
                  title="发送给谁、从哪发送？"
                  description="点『报盘上传』后：后端把结算清单+收费明细组装成 2304 报文 → 写入报文台账 → 经 InsuranceGateway 发往医保前置机（当前无联调环境，走 Mock 实现，回执直接回显）→ 回执成功后清单转『已上传』。拿到医保局规范后只需替换网关实现，报文在此处逐条可查。"/>
        <el-table :data="reports" size="small" border empty-text="该清单还没有发出过报文">
          <el-table-column label="类型" width="100">
            <template #default="{ row }">{{ reportTypeMap[row.reportType] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="tradeNo" label="HIS 流水号" width="200">
            <template #default="{ row }"><span class="font-mono text-xs">{{ row.tradeNo }}</span></template>
          </el-table-column>
          <el-table-column prop="receiptNo" label="医保回执号" width="180">
            <template #default="{ row }"><span class="font-mono text-xs">{{ row.receiptNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="reportStatusMap[row.status]?.type || 'info'" size="small">
                {{ reportStatusMap[row.status]?.label || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发出/回执时间" width="150">
            <template #default="{ row }">
              <p class="text-xs">{{ fmtTime(row.sendTime, 16) }}</p>
              <p class="text-xs text-slate-400">{{ row.replyTime ? fmtTime(row.replyTime, 16) : '未回执' }}</p>
            </template>
          </el-table-column>
          <el-table-column prop="errMsg" label="失败原因" min-width="140">
            <template #default="{ row }">
              <span v-if="row.errMsg" class="text-xs text-red-600">{{ row.errMsg }}</span>
              <span v-else class="text-xs text-slate-300">-</span>
            </template>
          </el-table-column>
          <el-table-column label="报文" width="80" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" data-testid="payload-btn"
                         @click="viewPayload(row)">原文</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 报文原文：出参 payload + 回执 replyPayload -->
    <el-dialog v-model="payloadVisible" width="760px" destroy-on-close>
      <template #header>
        <span class="text-base font-semibold text-slate-900">报文原文</span>
        <span class="ml-2 font-mono text-xs text-slate-400">{{ payloadRow?.tradeNo }}</span>
      </template>
      <div v-loading="payloadLoading">
        <template v-if="payloadRow">
          <h3 class="mb-1 text-sm font-bold text-slate-700">出参报文（{{ reportTypeMap[payloadRow.reportType] }}，发往医保前置机）</h3>
          <pre class="mb-4 max-h-72 overflow-auto rounded bg-slate-50 p-3 text-xs leading-5 text-slate-800">{{ payloadRow.payload }}</pre>
          <h3 class="mb-1 text-sm font-bold text-slate-700">回执报文（医保侧返回）</h3>
          <pre class="max-h-56 overflow-auto rounded bg-slate-50 p-3 text-xs leading-5 text-slate-800">{{ payloadRow.replyPayload || '尚未收到回执' }}</pre>
        </template>
      </div>
    </el-dialog>

    <!-- 日对账：本地清单 vs 医保侧账单 -->
    <el-dialog v-model="reconcileVisible" width="860px" destroy-on-close>
      <template #header>
        <span class="text-base font-semibold text-slate-900">医保报盘日对账</span>
      </template>
      <div class="mb-3 flex items-center gap-3">
        <span class="text-sm font-medium text-slate-700">账期日</span>
        <el-date-picker v-model="reconcileDate" type="date" value-format="YYYY-MM-DD" :clearable="false"
                        data-testid="reconcile-date" style="width: 160px"/>
        <el-button v-perm="'finance:insurance:edit'" type="primary" :loading="reconcileLoading" data-testid="reconcile-run" @click="runReconcile">对账</el-button>
        <span class="text-xs text-slate-400">口径：报文发出日（bill_date）</span>
      </div>
      <div v-loading="reconcileLoading">
        <template v-if="reconcileResult">
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div class="rounded border border-slate-200 p-3">
              <p class="mb-1 text-xs text-slate-400">本地口径（当日已上传/已审核清单）</p>
              <p class="font-medium">{{ reconcileResult.localCount }} 笔 ·
                总费用 ¥{{ money(reconcileResult.localTotal) }} ·
                统筹支付 ¥{{ money(reconcileResult.localInsurancePay) }}</p>
            </div>
            <div class="rounded border border-slate-200 p-3">
              <p class="mb-1 text-xs text-slate-400">医保侧口径（网关当日账单）</p>
              <p class="font-medium">{{ reconcileResult.remoteCount }} 笔 ·
                总费用 ¥{{ money(reconcileResult.remoteTotal) }} ·
                统筹支付 ¥{{ money(reconcileResult.remoteInsurancePay) }}</p>
            </div>
          </div>
          <div class="mt-3 flex items-center gap-3 text-xs text-slate-500">
            <el-tag :type="reconcileResult.matched ? 'success' : 'danger'" size="small" data-testid="reconcile-matched">
              {{ reconcileResult.matched ? '账平' : '存在差异' }}
            </el-tag>
            <span>当日报文：上传成功 {{ reconcileResult.uploadSuccess }} · 失败 {{ reconcileResult.uploadFail }}
              · 被撤销 {{ reconcileResult.uploadCancelled }} · 撤销报文 {{ reconcileResult.cancelSent }}</span>
          </div>
          <el-table v-if="reconcileResult.diffs?.length" :data="reconcileResult.diffs" size="small" border class="mt-3">
            <el-table-column prop="settlementNo" label="结算清单号" min-width="200">
              <template #default="{ row }"><span class="font-mono text-xs">{{ row.settlementNo || '-' }}</span></template>
            </el-table-column>
            <el-table-column prop="issue" label="差异" min-width="220"/>
            <el-table-column label="本地金额" width="110" align="right">
              <template #default="{ row }">{{ row.localAmount != null ? money(row.localAmount) : '-' }}</template>
            </el-table-column>
            <el-table-column label="医保侧金额" width="110" align="right">
              <template #default="{ row }">{{ row.remoteAmount != null ? money(row.remoteAmount) : '-' }}</template>
            </el-table-column>
          </el-table>
          <p v-else class="mt-3 text-xs text-slate-400">无差异明细。</p>
        </template>
      </div>
    </el-dialog>
  </div>
</template>
