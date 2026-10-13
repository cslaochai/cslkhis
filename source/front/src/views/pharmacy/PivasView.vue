<template>
  <div>
    <!-- 统计卡 -->
    <div class="mb-3 grid grid-cols-7 gap-3">
      <div v-for="c in statCards" :key="c.label" class="bg-white rounded-lg border border-slate-200 px-4 py-3">
        <div class="text-xs text-slate-500">{{ c.label }}</div>
        <div :class="c.tone" class="text-2xl font-semibold mt-1">{{ c.value }}</div>
      </div>
    </div>

    <!-- 生成静配单 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white p-4 space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="font-semibold text-slate-800">生成静配单（静脉用药医嘱）</h3>
        <span v-if="unmatchedNote" class="text-xs text-amber-600">{{ unmatchedNote }}</span>
      </div>
      <div class="flex items-center gap-3 flex-wrap">
        <el-select v-model="gen.wardId" :fit-input-width="false" class="w-64" data-testid="pivas-ward-select"
                   placeholder="选择病区">
          <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
        </el-select>
        <el-date-picker v-model="gen.admixDate" :clearable="false" class="w-44"
                        placeholder="调配日期" type="date" value-format="YYYY-MM-DD"/>
        <el-button :icon="Search" :loading="candidatesLoading" @click="loadCandidates">查询静脉用药医嘱</el-button>
        <el-button v-perm="'pharmacy:pivas:add'" :disabled="!candidates.length" :icon="Plus" :loading="generating"
                   data-testid="pivas-generate-btn" type="primary"
                   @click="doGenerate">生成静配单（{{ candidates.length }} 条）
        </el-button>
      </div>
      <el-table v-if="candidates.length" :data="candidates" border max-height="240">
        <el-table-column label="患者" min-width="100" prop="patientName"/>
        <el-table-column label="医嘱号" min-width="150" prop="orderNo"/>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.orderType) === 1 ? 'warning' : 'info'" size="small">
              {{ Number(row.orderType) === 1 ? '长期' : '临时' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="药品" min-width="160" prop="drugName"/>
        <el-table-column label="规格" prop="spec" width="110"/>
        <el-table-column label="途径" prop="route" width="90"/>
        <el-table-column label="频次" prop="frequency" width="80"/>
        <el-table-column align="right" label="数量" prop="quantity" width="80"/>
        <el-table-column label="单位" prop="unit" width="70"/>
        <el-table-column align="right" label="金额" width="100">
          <template #default="{ row }">{{ formatMoney(Number(row.quantity || 0) * Number(row.price || 0)) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 静配单列表 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <h3 class="mb-3 font-semibold text-slate-800">静配台账</h3>
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="病区">
          <el-select v-model="query.wardId" :fit-input-width="false" class="w-44" clearable placeholder="病区">
            <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="调配日期">
          <el-date-picker v-model="query.admixDate" class="w-44" placeholder="调配日期"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="query.patientName" class="w-40" clearable data-testid="pivas-patient-search"
                    placeholder="患者姓名"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" :fit-input-width="false" class="w-32" clearable placeholder="状态">
            <el-option v-for="d in batchStatusDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" @click="() => { query.pageNum = 1; loadList(); loadStats() }">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="pivas-table" stripe>
        <el-table-column label="静配单号" min-width="150" prop="pivasNo"/>
        <el-table-column label="调配日期" prop="admixDate" width="110"/>
        <el-table-column label="患者" min-width="110" prop="patientName">
          <template #default="{ row }">
            <span>{{ row.patientName || '—' }}</span>
            <span class="text-xs text-slate-400 ml-1">{{ row.patientNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="病区" min-width="130" prop="wardName">
          <template #default="{ row }">{{ row.wardName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="pivasBatchStatusTag(row.status)" size="small">{{ batchStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="right" label="明细数" prop="itemCount" width="80"/>
        <el-table-column label="生成人" prop="generateBy" width="100">
          <template #default="{ row }">{{ row.generateBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="排队操作" prop="labelBy" width="120">
          <template #default="{ row }">{{ row.labelBy || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="90">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="openDetail(row.id)">明细</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">当前筛选条件下没有静配单</div>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :total="total" layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 明细弹窗（审方→排队→调配→核对 工作区） -->
    <PivasDetail v-model="detailVisible" :id="detailId"
                 :batch-status-dict="batchStatusDict" :item-status-dict="itemStatusDict"
                 @changed="() => { loadList(); loadStats() }"/>
  </div>
</template>

<script setup>
/**
 * 静配中心（PIVAS）工作台
 *
 * 链路：按病区/日期查询静脉用药医嘱 → 生成静配单（同入院同日复用主单、明细追加，幂等）
 * → 药师审方（通过/退回，退回原因必填、医嘱可重新入单）→ 打标签排队取号（标签打印预留）
 * → 调配 → 成品核对发放（终态）。
 * 规则（服务端收口，前端只做显隐）：
 *  - 候选口径 = 住院摆药 ∩ route 静脉关键词（静滴/静注/静推/静脉/泵入）；
 *  - 未来日期不可生成；仍有待审方明细时不可打标签；
 *  - 状态机 1待审方→2已审方→3已排队→4已调配→5已核对发放 顺序推进不可跳级，1 可退回 0；
 *  - 计费不在本链（仍走住院摆药），本页面零库存/费用动作。
 * 状态文案走字典（his_pivas_*），tag 色与动作显隐单点 lib/pivas.js。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  generatePivas,
  getPivasStats,
  listPivasCandidates,
  listPivasPage,
} from '@/api/pivas';
import {getInpatientWardList} from '@/api/inpatient';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {pivasBatchStatusTag} from '@/lib/pivas';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import PivasDetail from './PivasDetail.vue';

const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};
const loading = ref(true);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  wardId: null,
  admixDate: '',
  patientName: '',
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const stats = reactive({
  pendingAudit: 0, audited: 0, queued: 0, compounded: 0, verified: 0, rejected: 0, batchCount: 0,
});
const wards = ref([]);
const batchStatusDict = ref([]);
const itemStatusDict = ref([]);
const batchStatusText = (v) => dictLabelText(batchStatusDict.value, v);
// ---------------- 生成区 ----------------
const gen = reactive({wardId: null, admixDate: today()});
const candidates = ref([]);
const candidatesLoading = ref(false);
const generating = ref(false);
const unmatchedNote = ref('');
const loadCandidates = async () => {
  if (!gen.wardId) {
    ElMessage.warning('请先选择病区');
    return;
  }
  candidatesLoading.value = true;
  candidates.value = [];
  unmatchedNote.value = '';
  try {
    const res = await listPivasCandidates({
      wardId: gen.wardId,
      admixDate: gen.admixDate || undefined,
    });
    if (res.code === 200) {
      candidates.value = res.data || [];
      if (!candidates.value.length)
        ElMessage.info('该病区当天没有可入静配单的静脉用药医嘱');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    candidatesLoading.value = false;
  }
};
const doGenerate = async () => {
  if (!gen.wardId) {
    ElMessage.warning('请先选择病区');
    return;
  }
  generating.value = true;
  try {
    const res = await generatePivas({
      wardId: gen.wardId,
      admixDate: gen.admixDate || undefined,
    });
    if (res.code === 200) {
      const items = res.data?.items || [];
      ElMessage.success(`静配单 ${res.data?.pivasNo} 已生成（${items.length} 条明细待审方）${res.data?.remark ? '；' + res.data.remark : ''}`);
      unmatchedNote.value = res.data?.remark || '';
      candidates.value = [];
      await Promise.all([loadList(), loadStats()]);
      openDetail(res.data?.id);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    generating.value = false;
  }
};
// ---------------- 列表 ----------------
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.PIVAS_BATCH_STATUS},${DICT_TYPE.PIVAS_ITEM_STATUS}`);
    batchStatusDict.value = res?.data?.[DICT_TYPE.PIVAS_BATCH_STATUS] || [];
    itemStatusDict.value = res?.data?.[DICT_TYPE.PIVAS_ITEM_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadWards = async () => {
  try {
    const res = await getInpatientWardList();
    if (res.code === 200)
      wards.value = res.data || [];
  } catch (e) {
    console.error('加载病区失败', e);
  }
};
const loadStats = async () => {
  try {
    const res = await getPivasStats({
      admixDate: query.admixDate || undefined,
      wardId: query.wardId ?? undefined,
    });
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listPivasPage({
      wardId: query.wardId ?? undefined,
      admixDate: query.admixDate || undefined,
      patientName: query.patientName.trim() || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.wardId = null;
  query.admixDate = '';
  query.patientName = '';
  query.status = null;
  query.pageNum = 1;
  loadList();
  loadStats();
};
// ---------------- 明细弹窗 ----------------
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (id) => {
  if (!id) return;
  detailId.value = id;
  detailVisible.value = true;
};
const statCards = computed(() => [
  {label: '待审方', value: stats.pendingAudit, tone: 'text-amber-600'},
  {label: '已审方待排队', value: stats.audited, tone: 'text-sky-700'},
  {label: '已排队待调配', value: stats.queued, tone: 'text-sky-700'},
  {label: '已调配待核对', value: stats.compounded, tone: 'text-amber-600'},
  {label: '已核对发放', value: stats.verified, tone: 'text-emerald-700'},
  {label: '已拒配', value: stats.rejected, tone: 'text-slate-500'},
  {label: '静配单数', value: stats.batchCount, tone: 'text-[#1269B5]'},
]);
onMounted(async () => {
  await Promise.all([loadDicts(), loadWards()]);
  await loadList();
  await loadStats();
});
</script>
