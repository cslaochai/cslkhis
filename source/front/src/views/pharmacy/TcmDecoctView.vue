<template>
  <div data-testid="tcm-decoct-page">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="mb-3 flex items-center justify-between">
        <h2 class="text-lg font-semibold text-slate-800">中药代煎</h2>
        <div class="flex items-center gap-3">
          <el-button :icon="Refresh" data-testid="tcm-decoct-refresh" @click="refresh">刷新</el-button>
        </div>
      </div>

      <div class="mb-3 flex flex-wrap items-center gap-2">
        <el-button v-for="s in [1, 2, 3, 9]" :key="s" :data-testid="`tcm-decoct-tab-${s}`"
                   :type="query.decoctStatus === s ? 'primary' : 'default'"
                   size="small"
                   @click="filterByStatus(s)">
          {{ STATUS_META[s].label }}
          <span class="ml-1 text-xs opacity-70">{{
              counts[s === 1 ? 'pending' : s === 2 ? 'decocted' : s === 3 ? 'picked' : 'cancelled'] ?? 0
            }}</span>
        </el-button>
      </div>

      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable data-testid="tcm-decoct-keyword"
                    placeholder="代煎单号 / 处方号 / 患者姓名" style="width: 240px"
                    @keyup.enter="query.pageNum = 1; loadList()"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.decoctStatus" :fit-input-width="false" clearable data-testid="tcm-decoct-status"
                     placeholder="状态" style="width: 130px">
            <el-option v-for="s in [1, 2, 3, 9]" :key="s" :label="STATUS_META[s].label" :value="s"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="tcm-decoct-table" row-key="id"
                stripe @row-click="openDetail">
        <el-table-column label="代煎单号" prop="decoctNo" width="170"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column align="center" label="剂数" prop="doseCount" width="70"/>
        <el-table-column align="center" label="味数" prop="herbCount" width="70"/>
        <el-table-column align="right" label="总克数" width="100">
          <template #default="{ row }">{{ Number(row.totalGrams || 0).toFixed(1) }} g</template>
        </el-table-column>
        <el-table-column align="right" label="每剂约" width="90">
          <template #default="{ row }">{{ Number(row.gramsPerDose || 0).toFixed(1) }} g</template>
        </el-table-column>
        <el-table-column label="煎法脚注" min-width="180" prop="methodSummary" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.methodSummary" class="text-amber-700">{{ row.methodSummary }}</span>
            <span v-else class="text-slate-400">常规水煎</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row).tag as any" data-testid="tcm-decoct-status-tag" size="small">
              {{ row.decoctStatusLabel || statusMeta(row).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处方号" prop="prescriptionNo" show-overflow-tooltip width="180"/>
        <el-table-column label="开方科室" prop="deptName" show-overflow-tooltip width="120"/>
        <el-table-column label="建单时间" prop="createTime" width="160"/>
        <el-table-column fixed="right" label="操作" width="230">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="tcm-decoct-detail" link type="primary"
                       @click.stop="openDetail(row)">详情
            </el-button>
            <el-button v-if="canAdvance(row)" v-perm="'pharmacy:tcmDecoct:edit'" data-testid="tcm-decoct-advance" link
                       type="primary" @click.stop="doAdvance(row)">
              {{ advanceLabel(row) }}
            </el-button>
            <el-button v-if="canCancel(row)" v-perm="'pharmacy:tcmDecoct:cancel'" data-testid="tcm-decoct-cancel" link
                       type="danger" @click.stop="doCancel(row)">作废
            </el-button>
            <el-button v-perm="'pharmacy:tcmDecoct:print'" :icon="Printer" data-testid="tcm-decoct-print" link
                       type="primary" @click.stop="doPrint(row)">打印
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            background
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="query.pageNum = 1; loadList()"
            @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 详情：只读。行点击进来的必须是只读弹框（AGENTS §2），编辑走上面的按钮入口 -->
    <el-dialog v-model="detailVisible" data-testid="tcm-decoct-detail-dialog" title="代煎单详情" width="760px">
      <div v-loading="detailLoading">
        <el-form v-if="detail" class="!text-sm" disabled label-width="90px">
          <div class="grid grid-cols-2 gap-x-4">
            <el-form-item label="代煎单号">
              <el-input :model-value="detail.decoctNo"/>
            </el-form-item>
            <el-form-item label="状态">
              <el-input :model-value="detail.decoctStatusLabel"/>
            </el-form-item>
            <el-form-item label="患者">
              <el-input :model-value="detail.patientName"/>
            </el-form-item>
            <el-form-item label="处方号">
              <el-input :model-value="detail.prescriptionNo"/>
            </el-form-item>
            <el-form-item label="剂数">
              <el-input :model-value="`${detail.doseCount} 剂`"/>
            </el-form-item>
            <el-form-item label="味数">
              <el-input :model-value="`${detail.herbCount} 味`"/>
            </el-form-item>
            <el-form-item label="总克数">
              <el-input :model-value="`${Number(detail.totalGrams || 0).toFixed(2)} g`"/>
            </el-form-item>
            <el-form-item label="每剂约">
              <el-input :model-value="`${Number(detail.gramsPerDose || 0).toFixed(1)} g`"/>
            </el-form-item>
            <el-form-item label="开方医师">
              <el-input :model-value="detail.doctorName"/>
            </el-form-item>
            <el-form-item label="代煎药房">
              <el-input :model-value="detail.pharmacyName"/>
            </el-form-item>
            <el-form-item label="建单时间">
              <el-input :model-value="detail.createTime"/>
            </el-form-item>
            <el-form-item label="煎药时间">
              <el-input :model-value="detail.decoctTime || '—'"/>
            </el-form-item>
            <el-form-item label="取走时间">
              <el-input :model-value="detail.pickupTime || '—'"/>
            </el-form-item>
            <el-form-item label="经办人">
              <el-input :model-value="detail.operatorName || '—'"/>
            </el-form-item>
          </div>
          <el-form-item label="煎法脚注">
            <el-input :model-value="detail.methodSummary || '常规水煎'" :rows="2" type="textarea"/>
          </el-form-item>
          <el-form-item v-if="detail.decoctStatus === 9" label="作废原因">
            <el-input :model-value="detail.cancelReason" :rows="2" type="textarea"/>
          </el-form-item>
        </el-form>

        <div class="mt-2 text-xs font-medium text-slate-500">逐味明细（{{ herbs.length }} 味）</div>
        <el-table :data="herbs" border class="mt-1" data-testid="tcm-decoct-herbs" max-height="260" size="small">
          <el-table-column align="center" label="#" type="index" width="46"/>
          <el-table-column label="药名" min-width="120" prop="drugName"/>
          <el-table-column label="规格" prop="specification" width="90"/>
          <el-table-column align="right" label="每剂" width="90">
            <template #default="{ row }">{{ row.perDoseText }} g</template>
          </el-table-column>
          <el-table-column align="right" label="实发" width="100">
            <template #default="{ row }">{{ Number(row.grams || 0).toFixed(2) }} g</template>
          </el-table-column>
          <el-table-column label="煎法" prop="method" width="100"/>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 中药代煎台账（T5 / sql/139）
 *
 * 这张单子**不能在这里新建**：它由「发药完成」在后端生成（药还没调剂出去就挂一张要煎的单子，
 * 退药时没人知道该拿它怎么办）。同理这里也没有删除 —— 停止流转的唯一动作是作废（status=9）。
 *
 * 状态只进不退：1-待煎 →（煎好封装）2-已煎 →（患者取走）3-已取。
 * 状态名由后端 decoctStatusLabel 给出，前端不自己翻译码值。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Printer, Refresh, Search, View} from '@element-plus/icons-vue';
import {
  tcmDecoctAdvance,
  tcmDecoctCancel,
  tcmDecoctGetDetailById,
  tcmDecoctListPage,
  tcmDecoctPrint,
  tcmDecoctStatusCount,
} from '@/api/tcmDecoct';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const STATUS_PENDING = 1;
const STATUS_DECOCTED = 2;
const STATUS_PICKED = 3;
const STATUS_CANCELLED = 9;
/** 状态 → 标签色：待煎要显眼（那是催办的活），终态压成灰 */
const STATUS_META = {
  1: {label: '待煎', tag: 'warning'},
  2: {label: '已煎', tag: 'primary'},
  3: {label: '已取', tag: 'success'},
  9: {label: '已作废', tag: 'info'},
};
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const counts = ref({});
const query = reactive({
  keyword: '',
  decoctStatus: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadCounts = async () => {
  try {
    const res = await tcmDecoctStatusCount();
    if (res.code === 200)
      counts.value = res.data || {};
  } catch (e) {
    console.error('加载代煎计数失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await tcmDecoctListPage({
      keyword: query.keyword?.trim() || undefined,
      decoctStatus: query.decoctStatus ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    } else {
      ElMessage.error(res.message || '加载代煎台账失败');
    }
  } catch (e) {
    ElMessage.error('加载代煎台账失败');
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const refresh = () => {
  loadList();
  loadCounts();
};
const resetQuery = () => {
  query.keyword = '';
  query.decoctStatus = null;
  query.pageNum = 1;
  loadList();
};
/** 页签式状态过滤：点「待煎」只看待煎，再点一次取消过滤 */
const filterByStatus = (status) => {
  query.decoctStatus = query.decoctStatus === status ? null : status;
  query.pageNum = 1;
  loadList();
};
const statusMeta = (row) => STATUS_META[row.decoctStatus] || {label: row.decoctStatusLabel || '未知', tag: 'info'};
// ==================== 详情（只读） ====================
const detailVisible = ref(false);
const detail = ref(null);
const detailLoading = ref(false);
const openDetail = async (row) => {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    const res = await tcmDecoctGetDetailById(row.id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '加载代煎单详情失败');
  } catch (e) {
    console.error(e);
  } finally {
    detailLoading.value = false;
  }
};
const advanceLabel = (row) => (row.decoctStatus === STATUS_PENDING ? '标记已煎' : '标记已取');
const nextStatus = (row) => (row.decoctStatus === STATUS_PENDING ? STATUS_DECOCTED : STATUS_PICKED);
const canAdvance = (row) => row.decoctStatus === STATUS_PENDING || row.decoctStatus === STATUS_DECOCTED;
const canCancel = (row) => canAdvance(row);
const doAdvance = async (row) => {
  const target = nextStatus(row);
  try {
    await ElMessageBox.confirm(`${row.decoctNo}（${row.patientName} · ${row.doseCount} 剂）确认置为「${STATUS_META[target].label}」？`, '状态推进', {
      type: 'warning',
      confirmButtonText: '确认'
    });
  } catch {
    return;
  }
  const res = await tcmDecoctAdvance({id: row.id, targetStatus: target}).catch((e) => e);
  if (res && res.code === 200) {
    ElMessage.success(res.message || '状态已更新');
    refresh();
  } else {
    ElMessage.error(res?.message || '状态推进失败');
  }
};
const doCancel = async (row) => {
  let reason;
  try {
    const r = await ElMessageBox.prompt(`作废 ${row.decoctNo}（${row.patientName} · ${row.doseCount} 剂）。已取走的单子在药房台账上是不可变的实物记录，只能对「还没到手」的单子作废。`, '作废代煎单', {
      inputPlaceholder: '作废原因（必填）',
      inputValidator: (v) => (v && v.trim() ? true : '必须填写作废原因'),
      confirmButtonText: '确认作废'
    });
    reason = (r.value || '').trim();
  } catch {
    return;
  }
  const res = await tcmDecoctCancel({id: row.id, reason}).catch((e) => e);
  if (res && res.code === 200) {
    ElMessage.success(res.message || '已作废');
    refresh();
  } else {
    ElMessage.error(res?.message || '作废失败');
  }
};
const doPrint = async (row) => {
  const res = await tcmDecoctPrint(row.id).catch((e) => e);
  if (res && res.code === 200) {
    ElMessage.success(res.message || '已发送到打印出口（学习阶段为控制台打印）');
  } else {
    ElMessage.error(res?.message || '打印失败');
  }
};
const herbs = computed(() => detail.value?.herbs || []);
onMounted(refresh);
</script>
