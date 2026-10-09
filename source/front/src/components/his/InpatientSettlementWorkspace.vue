<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Money, Wallet, Document, DataAnalysis } from '@element-plus/icons-vue'
import { getInpatientListPage } from '@/api/inpatient'
import {
  getPrepayListPage,
  getPrepayBalance,
  savePrepay,
  getDailyBill,
  getSettlementPreview,
  settleInpatient,
  getSettlementDetail,
  getAccountSummary,
} from '@/api/inpatientSettlement'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'

interface AdmissionOption {
  admissionId: string
  patientNo?: string
  patientName?: string
  bedNo?: string
  wardName?: string
  deptName?: string
}

interface PrepayRow {
  id: string
  prepayNo?: string
  prepayType?: number
  prepayTypeText?: string
  amount?: number
  balanceAfter?: number
  payMethodText?: string
  receiptNo?: string
  payTime?: string
  operatorName?: string
  remark?: string
}

interface FeeRow {
  feeNo?: string
  itemTypeName?: string
  itemName?: string
  specification?: string
  unit?: string
  quantity?: number
  price?: number
  amount?: number
  occurTime?: string
  sourceNo?: string
}

interface BillDay {
  date?: string
  dayTotal?: number
  items?: FeeRow[]
}

const money = (v?: number | string) => (v === null || v === undefined ? '—' : Number(v).toFixed(2))

// ---------------- 基础数据 ----------------

const admissions = ref<AdmissionOption[]>([])
const admissionId = ref<string>('')
const activeTab = ref('prepay')
const loading = ref(false)

const patientLabel = (a: AdmissionOption) =>
  `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`

const loadAdmissions = async () => {
  try {
    const res = await getInpatientListPage({ admitStatus: 1, pageNum: 1, pageSize: 200 })
    admissions.value = (res.data?.records || []) as AdmissionOption[]
    if (!admissionId.value && admissions.value.length > 0) {
      admissionId.value = String(admissions.value[0].admissionId)
    }
  } catch (error: any) {
    console.error('加载在院患者失败:', error)
  }
}

// ---------------- 概览 ----------------

const summary = ref<any>({})
const balance = ref<any>({})

const loadSummary = async () => {
  if (!admissionId.value) return
  try {
    const [s, b] = await Promise.all([
      getAccountSummary(admissionId.value),
      getPrepayBalance(admissionId.value),
    ])
    summary.value = s.data || {}
    balance.value = b.data || {}
  } catch (error: any) {
    ElMessage.error(error.message || '加载账务概览失败')
  }
}

const arrears = computed(() => !!summary.value?.arrears)
const settled = computed(() => !!summary.value?.settled)

// ---------------- 预交金 ----------------

const prepayRows = ref<PrepayRow[]>([])
const prepayTotal = ref(0)
const prepayPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })

const loadPrepay = async () => {
  if (!admissionId.value) return
  loading.value = true
  try {
    const res = await getPrepayListPage({
      admissionId: admissionId.value,
      pageNum: prepayPagination.value.pageNum,
      pageSize: prepayPagination.value.pageSize,
    })
    prepayRows.value = (res.data?.records || []) as PrepayRow[]
    prepayTotal.value = Number(res.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error.message || '加载预交金流水失败')
  } finally {
    loading.value = false
  }
}

const prepayDialog = ref(false)
const saving = ref(false)
const prepayForm = ref<any>({ prepayType: 1, payMethod: 1 })

const emptyPrepayForm = () => ({
  admissionId: admissionId.value,
  prepayType: 1,
  amount: undefined,
  payMethod: 1,
  receiptNo: '',
  remark: '',
})

const openPrepay = () => {
  prepayForm.value = emptyPrepayForm()
  prepayDialog.value = true
}

const submitPrepay = async () => {
  if (!prepayForm.value.amount || Number(prepayForm.value.amount) <= 0) {
    ElMessage.warning('请输入大于 0 的金额（退款也填正数，方向由流水类型决定）')
    return
  }
  saving.value = true
  try {
    // 退款按 FIFO 可能摊成多笔（微信收的退回微信、现金收的退现金），所以返回的是数组
    const res = await savePrepay({ ...prepayForm.value, admissionId: admissionId.value })
    const rows = res.data || []
    ElMessage.success(rows.length > 1 ? `已登记 ${rows.length} 笔流水` : '预交金已登记')
    prepayDialog.value = false
    await Promise.all([loadPrepay(), loadSummary(), loadBill(), loadSettlement()])
  } catch (error: any) {
    ElMessage.error(error.message || '登记失败')
  } finally {
    saving.value = false
  }
}

// ---------------- 日清单 ----------------

const bill = ref<any>({ days: [] })
const billLoading = ref(false)

const loadBill = async () => {
  if (!admissionId.value) return
  billLoading.value = true
  try {
    const res = await getDailyBill({ admissionId: admissionId.value })
    bill.value = res.data || { days: [] }
  } catch (error: any) {
    ElMessage.error(error.message || '加载日清单失败')
  } finally {
    billLoading.value = false
  }
}

// ---------------- 结算 ----------------

const preview = ref<any>(null)
const settlement = ref<any>(null)
const previewLoading = ref(false)
const settling = ref(false)
// 结算方式只能由收窗口的人选：后端拿参保号也判不出"这次要不要走医保"
const settleMode = ref(1)

const loadSettlement = async () => {
  if (!admissionId.value) return
  try {
    const res = await getSettlementDetail(admissionId.value)
    settlement.value = res.data || null
  } catch (error: any) {
    settlement.value = null
  }
}

const runPreview = async () => {
  if (!admissionId.value) return
  previewLoading.value = true
  try {
    const res = await getSettlementPreview({ admissionId: admissionId.value, settleMode: settleMode.value })
    preview.value = res.data || null
  } catch (error: any) {
    preview.value = null
    ElMessage.error(error.message || '结算试算失败')
  } finally {
    previewLoading.value = false
  }
}

const runSettle = async () => {
  if (!preview.value) {
    ElMessage.warning('请先做结算试算')
    return
  }
  if (Number(preview.value.arrearsAmount || 0) > 0) {
    try {
      await ElMessageBox.confirm(
        `本次结算将产生欠费 ${money(preview.value.arrearsAmount)} 元，是否按欠费结算办理？`,
        '欠费结算确认',
        { type: 'warning' },
      )
    } catch {
      return
    }
  }
  settling.value = true
  try {
    const res = await settleInpatient({ admissionId: admissionId.value, settleMode: settleMode.value })
    ElMessage.success('住院结算已完成')
    settlement.value = res.data || null
    preview.value = null
    await Promise.all([loadSummary(), loadBill()])
  } catch (error: any) {
    ElMessage.error(error.message || '结算失败')
  } finally {
    settling.value = false
  }
}

// ---------------- 刷新 ----------------

const reloadAll = async () => {
  await Promise.all([loadSummary(), loadPrepay(), loadBill(), loadSettlement()])
}

const handleAdmissionChange = async () => {
  prepayPagination.value.pageNum = 1
  preview.value = null
  await reloadAll()
}

onMounted(async () => {
  await loadAdmissions()
  await reloadAll()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">住院账务（预交金 / 日清单 / 结算）</h1>
        <p class="mt-1 text-sm text-slate-500">
          余额是住院资金账户的净额（充值进账、抵扣与退差出账）；日清单按记账行净额（含红冲）汇总；结算按四层出账：应收 → 账单 → 账户抵扣 → 退差/欠费。欠费只提示、不阻断。
        </p>
      </div>
      <div class="flex items-center gap-3">
        <el-select
          v-model="admissionId"
          data-testid="p3-admission-select"
          placeholder="选择在院患者"
          filterable
          class="!w-80"
          @change="handleAdmissionChange"
        >
          <el-option v-for="a in admissions" :key="a.admissionId" :label="patientLabel(a)" :value="String(a.admissionId)" />
        </el-select>
        <el-button :icon="Refresh" @click="reloadAll">刷新</el-button>
      </div>
    </div>

    <!-- 概览卡片 -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">住院账户余额</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p3-balance-value">{{ money(balance.balance) }}</p>
        <p class="text-[11px] text-slate-400">
          充值 {{ money(balance.rechargeTotal) }} / 柜面退 {{ money(balance.refundTotal) }} / {{ balance.flowCount || 0 }} 笔
        </p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">已发生费用</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p3-total-value">{{ money(summary.totalAmount) }}</p>
        <p class="text-[11px] text-slate-400">{{ summary.admissionNo || '—' }} {{ summary.patientName || '' }}</p>
      </div>
      <div class="rounded-lg border p-4 shadow-sm" :class="arrears ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-white'">
        <p class="text-xs text-slate-500">欠费状态</p>
        <p class="text-lg font-bold" :class="arrears ? 'text-red-600' : 'text-slate-900'" data-testid="p3-arrears-value">
          {{ arrears ? money(summary.arrearsAmount) : '无欠费' }}
        </p>
        <p class="text-[11px] text-slate-400">{{ arrears ? '仅提示，不阻断诊疗' : '—' }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">结算状态</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p3-settle-status">
          {{ settled ? summary.settleStatusText : '未结算' }}
        </p>
        <p class="truncate text-[11px] text-slate-400">{{ summary.settlementNo || '未办理结算' }}</p>
      </div>
    </div>

    <div v-if="arrears" class="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700" data-testid="p3-arrears-tip">
      {{ summary.hintText }}
    </div>

    <el-tabs v-model="activeTab" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <!-- ============== 预交金 ============== -->
      <el-tab-pane label="预交金" name="prepay">
        <div class="mb-3 flex items-center justify-between">
          <span class="text-sm font-medium text-slate-700">流水（充值正、退款负）</span>
          <el-button type="primary" :icon="Wallet" data-testid="p3-open-prepay" @click="openPrepay">收/退预交金</el-button>
        </div>
        <el-table v-loading="loading" :data="prepayRows" style="width: 100%" data-testid="p3-prepay-table">
          <el-table-column prop="prepayNo" label="流水号" width="170" />
          <el-table-column label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.prepayType === 1 ? 'success' : 'warning'" size="small">{{ row.prepayTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="110" align="right">
            <template #default="{ row }">
              <span :class="row.amount < 0 ? 'text-green-600' : 'text-slate-900'">{{ money(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="余额（本笔后）" width="130" align="right">
            <template #default="{ row }">{{ money(row.balanceAfter) }}</template>
          </el-table-column>
          <el-table-column prop="payMethodText" label="支付方式" width="100" />
          <el-table-column prop="receiptNo" label="票据号" width="120" show-overflow-tooltip />
          <el-table-column label="时间" width="160">
            <template #default="{ row }">{{ (row.payTime || '').replace('T', ' ') }}</template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
            v-model:current-page="prepayPagination.pageNum"
            v-model:page-size="prepayPagination.pageSize"
            :total="prepayTotal"
            layout="total, prev, pager, next"
            @current-change="loadPrepay"
            @size-change="loadPrepay"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 日清单 ============== -->
      <el-tab-pane label="日清单" name="daily">
        <div class="mb-3 flex items-center justify-between">
          <span class="text-sm font-medium text-slate-700">按天汇总（当日小计与合计均由后端给出）</span>
          <span class="text-sm text-slate-500">
            合计 <b class="text-slate-900" data-testid="p3-daily-total">{{ money(bill.totalAmount) }}</b> 元 / {{ bill.itemCount || 0 }} 条明细
          </span>
        </div>
        <div v-loading="billLoading" class="space-y-3">
          <div v-if="!(bill.days || []).length" class="py-10 text-center text-sm text-slate-400">暂无费用明细</div>
          <div v-for="d in bill.days || []" :key="d.date" class="rounded border border-slate-200">
            <div class="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-3 py-2 text-sm">
              <span class="font-medium text-slate-700" :data-testid="`p3-day-${d.date}`">{{ d.date }}</span>
              <span class="text-slate-600">小计 <b>{{ money(d.dayTotal) }}</b> 元</span>
            </div>
            <el-table :data="d.items" size="small" style="width: 100%">
              <el-table-column prop="itemTypeName" label="类别" width="90" />
              <el-table-column prop="itemName" label="项目名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="specification" label="规格" width="110" />
              <el-table-column label="数量" width="80" align="right">
                <template #default="{ row }">{{ row.quantity }}</template>
              </el-table-column>
              <el-table-column label="单价" width="90" align="right">
                <template #default="{ row }">{{ money(row.price) }}</template>
              </el-table-column>
              <el-table-column label="金额" width="100" align="right">
                <template #default="{ row }">{{ money(row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="sourceNo" label="来源单号" width="150" />
              <el-table-column label="时间" width="150">
                <template #default="{ row }">{{ row.occurTime }}</template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============== 结算 ============== -->
      <el-tab-pane label="住院结算" name="settlement">
        <div class="mb-3 flex items-center gap-3">
          <el-select v-model="settleMode" data-testid="p3-settle-mode" class="!w-32">
            <el-option :value="1" label="自费" />
            <el-option :value="2" label="医保" />
          </el-select>
          <el-button :icon="DataAnalysis" data-testid="p3-preview-btn" :loading="previewLoading" @click="runPreview">结算试算</el-button>
          <el-button
            type="primary"
            :icon="Money"
            data-testid="p3-settle-btn"
            :loading="settling"
            :disabled="!preview"
            @click="runSettle"
          >办理结算</el-button>
          <span class="text-xs text-slate-400">一次住院一张有效结算单；欠费也留单，不允许没结算就出院</span>
        </div>

        <div v-if="settlement" class="mb-4 rounded border border-green-200 bg-green-50 p-4">
          <div class="mb-2 text-sm font-medium text-green-800">
            已结算：{{ settlement.settlementNo }}
            <el-tag size="small" :type="settlement.settleStatus === 1 ? 'success' : 'warning'">{{ settlement.settleStatusText }}</el-tag>
          </div>
          <div class="grid grid-cols-2 gap-2 text-sm text-slate-700 sm:grid-cols-4">
            <div>总费用：<b>{{ money(settlement.totalAmount) }}</b></div>
            <div>优惠：<b>{{ money(settlement.discountAmount) }}</b></div>
            <div>统筹：<b>{{ money(settlement.poolAmount) }}</b></div>
            <div>个账：<b>{{ money(settlement.accountAmount) }}</b></div>
            <div>自付：<b>{{ money(settlement.selfAmount) }}</b></div>
            <div>应缴：<b>{{ money(settlement.payableAmount) }}</b></div>
            <div>已收：<b>{{ money(settlement.paidAmount) }}</b>（账户抵扣 {{ money(settlement.balanceUsed) }}）</div>
            <div>应退：<b>{{ money(settlement.refundAmount) }}</b></div>
            <div>欠费：<b :class="Number(settlement.arrearsAmount) > 0 ? 'text-red-600' : ''">{{ money(settlement.arrearsAmount) }}</b></div>
            <div>结算方式：{{ settlement.settleModeText || '—' }}</div>
            <div>结算人：{{ settlement.settleByName || '—' }}</div>
            <div>时间：{{ (settlement.settleTime || '').replace('T', ' ') }}</div>
          </div>
        </div>

        <div v-if="preview" class="space-y-3">
          <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700" data-testid="p3-conclusion">
            {{ preview.conclusionText }}
          </div>
          <el-descriptions :column="4" border size="small">
            <el-descriptions-item label="费用行数">{{ preview.feeCount }}</el-descriptions-item>
            <el-descriptions-item label="总费用">{{ money(preview.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="优惠">{{ money(preview.discountAmount) }}</el-descriptions-item>
            <el-descriptions-item label="统筹">{{ money(preview.poolAmount) }}</el-descriptions-item>
            <el-descriptions-item label="个账">{{ money(preview.accountAmount) }}</el-descriptions-item>
            <el-descriptions-item label="自付">{{ money(preview.selfAmount) }}</el-descriptions-item>
            <el-descriptions-item label="应缴">{{ money(preview.payableAmount) }}</el-descriptions-item>
            <el-descriptions-item label="账户余额">{{ money(preview.prepayBalance) }}</el-descriptions-item>
            <el-descriptions-item label="本次抵扣">{{ money(preview.balanceUsed) }}</el-descriptions-item>
            <el-descriptions-item label="应退">{{ money(preview.refundAmount) }}</el-descriptions-item>
            <el-descriptions-item label="欠费">{{ money(preview.arrearsAmount) }}</el-descriptions-item>
            <el-descriptions-item label="结算方式">{{ preview.settleModeText }}</el-descriptions-item>
          </el-descriptions>
          <el-table :data="preview.feeRows || []" size="small" style="width: 100%" data-testid="p3-fee-rows">
            <el-table-column prop="feeNo" label="记账单号" width="170" />
            <el-table-column prop="itemTypeName" label="类别" width="90" />
            <el-table-column prop="itemName" label="项目名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="数量" width="80" align="right">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column label="单价" width="90" align="right">
              <template #default="{ row }">{{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column label="金额（净额）" width="120" align="right">
              <template #default="{ row }">
                <span :class="Number(row.amount) < 0 ? 'text-green-600' : ''">{{ money(row.amount) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else-if="!settlement" class="py-10 text-center text-sm text-slate-400">点「结算试算」查看本次住院的费用汇总</div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============== 收/退预交金 ============== -->
    <el-dialog v-model="prepayDialog" title="收/退预交金" width="480px">
      <el-form label-width="100px">
        <el-form-item label="流水类型" required>
          <el-select v-model="prepayForm.prepayType" data-testid="p3-prepay-type" class="!w-full">
            <el-option :value="1" label="充值" />
            <el-option :value="2" label="退款" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" required>
          <el-input-number
            v-model="prepayForm.amount"
            data-testid="p3-prepay-amount"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            class="!w-full"
          />
          <div class="mt-1 text-xs text-slate-400">退款也填正数（方向由流水类型决定），不能超过当前余额</div>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="prepayForm.payMethod" data-testid="p3-prepay-method" class="!w-full">
            <!-- 字典 his_pay_method：4-医保个账 / 5-院内余额 不在这里给，后端会拒 -->
            <el-option :value="1" label="现金" />
            <el-option :value="2" label="微信" />
            <el-option :value="3" label="支付宝" />
            <el-option :value="6" label="银行卡" />
            <el-option :value="7" label="转账" />
          </el-select>
        </el-form-item>
        <el-form-item label="票据号">
          <el-input v-model="prepayForm.receiptNo" placeholder="可留空" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="prepayForm.remark" type="textarea" :rows="2" placeholder="退款建议写清原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="prepayDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="p3-submit-prepay" @click="submitPrepay">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
