<template>
  <el-drawer v-model="visible" :title="`日结单详情 ${detailSettlement?.settlementNo || ''}`" size="72%">
    <div v-loading="dayDetailLoading" class="space-y-5 text-sm" data-testid="fin-detail-drawer">
      <div
          v-if="isTrial"
          class="rounded-lg border border-amber-200 bg-amber-50 p-3 text-amber-800"
          data-testid="fin-detail-trial"
      >
        该日尚未日结，以下为「试算结果」（不落库）；确认无误请点右上「执行日结」。
      </div>

      <!-- 单头 -->
      <div v-if="detailSettlement" class="rounded-lg border border-slate-200 p-4">
        <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <div><span class="text-slate-400">日结单号：</span>
            <span class="font-mono text-slate-700">{{ detailSettlement.settlementNo || '—' }}</span>
          </div>
          <div><span class="text-slate-400">日期：</span>
            <span class="text-slate-700">{{ detailSettlement.settleDate || '—' }}</span>
          </div>
          <div><span class="text-slate-400">状态：</span>
            <el-tag :class="dayStatusTagClass(detailSettlement.settleStatus)" class="border" effect="plain"
                    size="small">
              {{ dayStatusText(detailSettlement.settleStatus) }}
            </el-tag>
          </div>
          <div><span class="text-slate-400">对账结论：</span>
            <el-tag
                :class="reconcileTagClass(detailSettlement.reconcileStatus)"
                class="border"
                effect="plain"
                size="small"
            >
              {{ reconcileText(detailSettlement.reconcileStatus) }}
            </el-tag>
          </div>
          <div><span class="text-slate-400">纳入班结单：</span>
            <span class="text-slate-700">{{ detailSettlement.shiftCount ?? 0 }} 张</span>
          </div>
          <div><span class="text-slate-400">当日实收：</span>
            <span class="text-slate-700">
              ¥{{ formatMoney(detailSettlement.chargeAmount) }}（{{ detailSettlement.chargeCount ?? 0 }} 笔流水
              / {{ detailSettlement.billCount ?? 0 }} 张账单）
            </span>
          </div>
          <div><span class="text-slate-400">Σ交班单：</span>
            <span class="text-slate-700">¥{{ formatMoney(detailSettlement.detailAmount) }}</span>
          </div>
          <div><span class="text-slate-400">统筹记账：</span>
            <span class="text-slate-700">¥{{ formatMoney(detailSettlement.poolAmount) }}</span>
          </div>
          <div><span class="text-slate-400">退费：</span>
            <span class="text-slate-700">
              ¥{{ formatMoney(detailSettlement.refundAmount) }}（{{ detailSettlement.refundCount ?? 0 }} 笔）
            </span>
          </div>
          <div><span class="text-slate-400">净额：</span>
            <span class="font-semibold text-slate-800">¥{{ formatMoney(detailSettlement.netAmount) }}</span>
          </div>
          <div><span class="text-slate-400">日结人：</span>
            <span class="text-slate-700">{{ detailSettlement.settleBy || '—' }}</span>
          </div>
          <div><span class="text-slate-400">日结时间：</span>
            <span class="text-slate-700">{{ detailSettlement.settleTime || '—' }}</span>
          </div>
          <div><span class="text-slate-400">审核人：</span>
            <span class="text-slate-700">{{ detailSettlement.auditBy || '—' }}</span>
          </div>
          <div><span class="text-slate-400">审核时间：</span>
            <span class="text-slate-700">{{ detailSettlement.auditTime || '—' }}</span>
          </div>
        </div>
        <div class="mt-3 grid grid-cols-2 gap-3 lg:grid-cols-5">
          <div><span class="text-slate-400">现金：</span>¥{{ formatMoney(detailSettlement.cashAmount) }}</div>
          <div><span class="text-slate-400">微信：</span>¥{{ formatMoney(detailSettlement.wechatAmount) }}</div>
          <div><span class="text-slate-400">支付宝：</span>¥{{ formatMoney(detailSettlement.alipayAmount) }}</div>
          <div><span class="text-slate-400">医保个账：</span>¥{{ formatMoney(detailSettlement.insuranceAmount) }}</div>
          <div><span class="text-slate-400">余额：</span>¥{{ formatMoney(detailSettlement.balanceAmount) }}</div>
        </div>
        <div
            v-if="Number(detailSettlement.unknownPayAmount || 0) !== 0"
            class="mt-2 text-xs text-rose-600"
        >
          支付方式未记录金额 ¥{{ formatMoney(detailSettlement.unknownPayAmount) }}
          —— 未并入任何渠道，避免点钞/渠道对账出现假数
        </div>
        <div v-if="detailSettlement.diffDetail"
             class="mt-3 rounded border border-slate-200 p-2 text-xs text-slate-600">
          差异说明：{{ detailSettlement.diffDetail }}
        </div>
      </div>

      <!-- 三级对账 -->
      <div v-if="detailReconcile">
        <div class="mb-2 flex items-center justify-between">
          <h3 class="font-semibold text-slate-800">三级对账</h3>
          <span
              :class="detailReconcile.passed ? 'text-emerald-600' : 'text-rose-600'"
              class="text-xs"
          >{{ detailReconcile.summary }}</span>
        </div>
        <table class="w-full border-collapse text-sm" data-testid="fin-recon-table">
          <thead>
          <tr class="bg-slate-50 text-left text-slate-500">
            <th class="border border-slate-200 px-3 py-2">级次</th>
            <th class="border border-slate-200 px-3 py-2">左口径</th>
            <th class="border border-slate-200 px-3 py-2 text-right">左金额</th>
            <th class="border border-slate-200 px-3 py-2">右口径</th>
            <th class="border border-slate-200 px-3 py-2 text-right">右金额</th>
            <th class="border border-slate-200 px-3 py-2 text-right">差额</th>
            <th class="border border-slate-200 px-3 py-2">结论</th>
          </tr>
          </thead>
          <tbody>
          <tr
              v-for="it in detailReconcile.items || []"
              :key="it.level"
              :data-testid="`fin-recon-${it.level}`"
          >
            <td class="border border-slate-200 px-3 py-2">{{ it.levelName }}</td>
            <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.leftLabel }}</td>
            <td class="border border-slate-200 px-3 py-2 text-right font-mono">¥{{ formatMoney(it.leftAmount) }}</td>
            <td class="border border-slate-200 px-3 py-2 text-slate-500">{{ it.rightLabel }}</td>
            <td class="border border-slate-200 px-3 py-2 text-right font-mono">¥{{ formatMoney(it.rightAmount) }}</td>
            <td
                :class="it.passed ? 'text-emerald-600' : 'text-rose-600'"
                class="border border-slate-200 px-3 py-2 text-right font-mono"
            >
              {{ formatMoney(it.diffAmount) }}
            </td>
            <td class="border border-slate-200 px-3 py-2">
              <el-tag :class="reconcileTagClass(it.passed ? 1 : 2)" class="border" effect="plain" size="small">
                {{ it.passed ? '平' : '差异' }}
              </el-tag>
            </td>
          </tr>
          </tbody>
        </table>
        <div
            v-for="it in detailReconcile.items || []"
            v-show="!it.passed"
            :key="`dmsg-${it.level}`"
            class="mt-2 rounded border border-rose-200 bg-rose-50 p-2 text-xs text-rose-700"
        >
          {{ it.levelName }}：{{ it.message }}
        </div>
      </div>

      <!-- 当日交班单 -->
      <div>
        <h3 class="mb-2 font-semibold text-slate-800">
          当日交班单（{{ detailShifts.length }} 张）
        </h3>
        <el-table :data="detailShifts" data-testid="fin-detail-shift-table" size="small" style="width: 100%">
          <el-table-column class-name="font-mono text-xs" label="交班单号" min-width="150" prop="settlementNo"/>
          <el-table-column label="收费员" width="90">
            <template #default="{ row }">{{ row.cashierName || '—' }}</template>
          </el-table-column>
          <el-table-column label="班次" width="76">
            <template #default="{ row }">{{ shiftTypeText(row.shiftType) }}</template>
          </el-table-column>
          <el-table-column label="统计区间" min-width="280">
            <template #default="{ row }">
              <span class="font-mono text-xs">{{ row.periodBegin }} ~ {{ row.periodEnd }}</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="收费金额" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.chargeAmount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="退费金额" width="120">
            <template #default="{ row }">¥{{ formatMoney(row.refundAmount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="现金差异" width="110">
            <template #default="{ row }">
              <span :class="Number(row.cashDiff || 0) === 0 ? 'text-emerald-600' : 'text-rose-600'">
                {{ formatMoney(row.cashDiff) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="86">
            <template #default="{ row }">
              <el-tag :class="cashierStatusTagClass(row.settleStatus)" class="border" effect="plain" size="small">
                {{ cashierStatusText(row.settleStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="detailShifts.length === 0" class="py-6 text-center text-xs text-slate-400">
          该日没有交班单 —— 二级对账会把当日全部收费都算成「未纳班结」
        </div>
      </div>

      <!-- 科室收入 -->
      <div>
        <h3 class="mb-2 font-semibold text-slate-800">科室收入</h3>
        <el-table :data="detailDepts" data-testid="fin-dept-table" size="small" style="width: 100%">
          <el-table-column label="科室" min-width="180">
            <template #default="{ row }">
              <span :class="row.unattributed ? 'text-rose-600' : 'text-slate-700'">
                {{ row.deptName || '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="明细笔数" width="110">
            <template #default="{ row }">{{ row.itemCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column align="right" label="金额" width="140">
            <template #default="{ row }">
              <span :class="row.unattributed ? 'text-rose-600' : 'text-slate-800'" class="font-semibold">
                ¥{{ formatMoney(row.amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="占比" width="100">
            <template #default="{ row }">{{ formatMoney(row.ratio) }}%</template>
          </el-table-column>
        </el-table>
        <div class="mt-2 text-xs text-slate-400">
          占比分母为全部明细金额（含无归属），故各行合计不超过 100%。
          无科室归属行单列、不并入科室统计。
        </div>
      </div>
    </div>

    <template #footer>
      <el-button
          v-if="detailSettlement && !audited"
          v-perm="'finance:settlement:edit'"
          :icon="Select"
          data-testid="fin-detail-audit-btn"
          type="primary"
          @click="handleAudit"
      >
        审核日结单
      </el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-drawer>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {Select} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {dayAudit, getDayDetailById, getDayDetailByDate} from '@/api/financeSettlement';
import {
  cashierStatusTagClass,
  cashierStatusText,
  dayStatusTagClass,
  dayStatusText,
  reconcileTagClass,
  reconcileText,
  shiftTypeText,
} from '@/lib/financeSettlement';
import {formatMoney} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 打开来源：已有日结单传 {id}，未日结（试算）传 {settleDate}
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dayDetailLoading = ref(false);
const dayDetail = ref(null);
const detailReconcile = computed(() => dayDetail.value?.reconcile || null);
const detailShifts = computed(() => dayDetail.value?.shifts || []);
const detailDepts = computed(() => dayDetail.value?.deptIncomes || []);
const detailSettlement = computed(() => dayDetail.value?.settlement || null);
/** 尚未日结时 settlement 为 null，此时下面几块是试算结果，必须在界面上说清楚 */
const isTrial = computed(() => !!dayDetail.value && !dayDetail.value.settlement);

async function loadDetail() {
  dayDetailLoading.value = true;
  dayDetail.value = null;
  try {
    const row = props.row;
    const res = row?.id ? await getDayDetailById(row.id) : await getDayDetailByDate(row?.settleDate);
    dayDetail.value = res.data || null;
  } catch (e) {
    ElMessage.error(e?.message || '加载日结详情失败');
  } finally {
    dayDetailLoading.value = false;
  }
}

watch(visible, (open) => {
  if (open) loadDetail();
});

const audited = computed(() => detailSettlement.value?.settleStatus === 2);

async function handleAudit() {
  const s = detailSettlement.value;
  if (!s)
    return;
  let remark = '';
  try {
    const r = await ElMessageBox.prompt(`确认审核日结单 ${s.settlementNo}（当日实收 ¥${formatMoney(s.chargeAmount)}）？审核后不可重算。`, '审核日结单', {
      type: 'warning',
      confirmButtonText: '确认审核',
      cancelButtonText: '取消',
      inputPlaceholder: '审核意见（选填）',
      inputValue: '',
    });
    remark = r.value || '';
  } catch {
    return;
  }
  try {
    await dayAudit({id: s.id, auditRemark: remark || undefined});
    ElMessage.success('日结单已审核');
    await loadDetail();
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '审核失败');
  }
}
</script>
