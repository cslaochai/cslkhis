<template>
  <el-dialog v-model="visible" destroy-on-close title="我开的住院证" width="940px">
    <!-- 查询卡：状态下拉 + 关键字，查询/重置走栅格外 -->
    <div class="mb-3 flex flex-wrap items-center gap-2">
      <el-select v-model="query.orderStatus" clearable class="!w-32" placeholder="全部状态">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"/>
      </el-select>
      <el-input v-model="query.keyword" class="!w-56" clearable placeholder="患者姓名 / 住院证号"
                @clear="handleSearch" @keyup.enter="handleSearch"/>
      <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column label="住院证号" prop="orderNo" min-width="150"/>
      <el-table-column label="患者" min-width="130">
        <template #default="{ row }">
          <div class="text-slate-700">{{ row.patientName || '—' }}</div>
          <div class="text-xs text-slate-400">{{ row.genderText || '—' }} {{ row.age ?? '—' }}岁</div>
        </template>
      </el-table-column>
      <el-table-column label="拟收治科室" min-width="110">
        <template #default="{ row }">{{ row.applyDeptName || '—' }}</template>
      </el-table-column>
      <el-table-column label="拟诊" prop="diagnosisName" min-width="130" show-overflow-tooltip>
        <template #default="{ row }">{{ row.diagnosisName || '—' }}</template>
      </el-table-column>
      <el-table-column label="开证时间" prop="orderTime" min-width="150"/>
      <el-table-column label="有效期至" prop="validUntil" min-width="150">
        <template #default="{ row }">{{ row.validUntil || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" min-width="110">
        <template #default="{ row }">
          <el-tag :type="orderStatusMap[row.orderStatus]?.type || 'info'" size="small">
            {{ row.orderStatusText || orderStatusMap[row.orderStatus]?.label || `未知(${row.orderStatus})` }}
          </el-tag>
          <!-- 过期是后端现算的展示态，和「待收治」同时可见 -->
          <el-tag v-if="row.expired" class="ml-1" effect="plain" size="small" type="danger">已过期</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button
              v-if="row.orderStatus === 1"
              v-perm="'opd:doctorWorkstation:delete'"
              link
              size="small"
              type="danger"
              @click="cancelOrder(row)"
          >作废
          </el-button>
          <span v-else class="text-xs text-slate-300">—</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="mt-3 flex justify-end">
      <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="() => { query.pageNum = 1; load() }"
      />
    </div>

    <div class="mt-2 rounded bg-slate-50 p-2 text-xs text-slate-500">
      仅「待收治」可作废，作废必须写原因并留痕；已收治的证不可作废（需处理请走退院/出院流程）。
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Search} from '@element-plus/icons-vue';
import {cancelAdmissionOrder, getMyAdmissionOrderListPage} from '@/api/admissionOrder';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {orderStatusMap} from '@/lib/inpatient';

// ------------------------------------------------------------------
// 我开的住院证（门诊医生站）——开住院证的医生本人在这里查看、作废自己开的证。
// 开证医生由后端从登录态取（myListPage 不收前端参数），这里只传筛选条件。
// 作废复用住院处同一接口：仅待收治可作废、必填原因、同步取消床位排队。
// ------------------------------------------------------------------
const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// 状态筛选：1-待收治 2-已收治 3-已作废（4-已过期是查询时现算的展示态，不参与筛选）
const statusOptions = [
  {label: '待收治', value: 1},
  {label: '已收治', value: 2},
  {label: '已作废', value: 3},
];
const query = reactive({
  orderStatus: null,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const rows = ref([]);
const total = ref(0);
const loading = ref(false);

const load = async () => {
  loading.value = true;
  try {
    const res = await getMyAdmissionOrderListPage({
      orderStatus: query.orderStatus ?? undefined,
      keyword: query.keyword || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    rows.value = res.data?.records || [];
    total.value = Number(res.data?.total || 0);
  } catch (e)  /* 错误已由全局拦截器统一提示 */{
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  query.pageNum = 1;
  load();
};
const handleReset = () => {
  query.orderStatus = null;
  query.keyword = '';
  query.pageNum = 1;
  load();
};

const cancelOrder = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`作废住院证 ${row.orderNo}（${row.patientName}）。作废必须写原因，这条记录会留痕。`, '作废住院证', {
      inputPlaceholder: '如：患者放弃住院 / 电话联系不上 / 门诊诊断已修正',
      inputValidator: (v) => (!!v && v.trim().length > 0) || '作废原因不能为空',
    });
    await cancelAdmissionOrder({id: row.id, cancelReason: value});
    ElMessage.success('住院证已作废');
    load();
  } catch (e)  /* 取消弹窗或错误已由全局拦截器统一提示 */{
  }
};

// 每次打开弹框重新拉最新数据（作废入口点开即见当前状态）
watch(() => props.modelValue, (v) => {
  if (v) {
    load();
  }
});
</script>
