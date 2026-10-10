<template>
  <div>
    <!-- 策略 -->
    <div v-loading="policyLoading" class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm mb-3">
      <div class="flex items-center gap-2 mb-3">
        <span class="font-medium text-gray-700">管控策略</span>
        <el-tag v-if="policy.stopEnabled === 1" size="small" type="danger">停费管控已开启</el-tag>
        <el-tag v-else size="small" type="info">停费管控关闭（欠费只提示不拦截）</el-tag>
      </div>
      <el-form inline label-width="90px">
        <el-form-item label="预警线(元)">
          <el-input-number v-model="policy.warnLine" :min="0" :precision="2" class="!w-40"/>
        </el-form-item>
        <el-form-item label="停费线(元)">
          <el-input-number v-model="policy.stopLine" :min="0" :precision="2" class="!w-40"/>
        </el-form-item>
        <el-form-item label="停费开关">
          <el-switch v-model="policy.stopEnabled" :active-value="1" :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="拦截类别">
          <el-input v-model="policy.stopClasses" class="!w-52" placeholder="2检查,3检验,4治疗"/>
          <span class="text-gray-400 text-xs ml-2">药品/手术/输血永不拦截</span>
        </el-form-item>
        <el-form-item>
          <el-button v-perm="'charge:arrearsControl:add'" :loading="policySaving" type="primary" @click="savePolicy">
            保存策略
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 榜单 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="患者姓名 / 患者号" style="width: 200px"
                    @keyup.enter="query.pageNum = 1; loadBoard()"/>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadBoard()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="arrears-board-table" stripe>
        <el-table-column label="住院号" prop="admissionNo" width="170"/>
        <el-table-column label="患者" prop="patientName" width="200">
          <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 ml-1">{{ row.patientNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="科室" prop="deptName" width="130">
          <template #default="{ row }">{{ row.deptName || '—' }}</template>
        </el-table-column>
        <el-table-column label="诊断" min-width="160" prop="diagnosis" show-overflow-tooltip>
          <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
        </el-table-column>
        <el-table-column label="入院时间" prop="admitTime" width="160">
          <template #default="{ row }">{{ (row.admitTime || '').slice(0, 16).replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column align="right" label="住院账户余额" prop="prepayBalance" width="120">
          <template #default="{ row }">¥{{ fmt(row.prepayBalance) }}</template>
        </el-table-column>
        <el-table-column align="right" label="已发生费用" prop="chargedAmount" width="120">
          <template #default="{ row }">¥{{ fmt(row.chargedAmount) }}</template>
        </el-table-column>
        <el-table-column align="right" label="欠费金额" prop="arrearsAmount" width="130">
          <template #default="{ row }">
            <span :class="Number(row.arrearsAmount) >= (policy.stopLine || 0) ? 'text-red-600' : 'text-orange-500'"
                  class="font-semibold">
              ¥{{ fmt(row.arrearsAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="管控状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="policy.stopEnabled === 1 && Number(row.arrearsAmount) >= (policy.stopLine || Infinity)"
                    size="small" type="danger">停费拦截中
            </el-tag>
            <el-tag v-else-if="policy.warnLine && Number(row.arrearsAmount) >= policy.warnLine"
                    size="small" type="warning">已达预警线
            </el-tag>
            <el-tag v-else size="small" type="info">欠费提示</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadBoard"/>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {getArrearsBoard, getArrearsPolicy, upsertArrearsPolicy} from '@/api/inpatientSettlement';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---------------- 策略 ----------------
const policy = reactive({
  warnLine: null, stopLine: null,
  stopEnabled: 0, stopClasses: '2,3,4', remark: '',
});
const policyLoading = ref(false);
const policySaving = ref(false);
const loadPolicy = async () => {
  policyLoading.value = true;
  try {
    const res = await getArrearsPolicy();
    if (res.code === 200 && res.data)
      Object.assign(policy, res.data);
    else
      ElMessage.error(res.message || '读取策略失败');
  } catch (e) {
    console.error(e);
  } finally {
    policyLoading.value = false;
  }
};
const savePolicy = async () => {
  if (policy.stopEnabled === 1 && !policy.stopLine) {
    ElMessage.warning('开启停费管控必须设置停费线');
    return;
  }
  policySaving.value = true;
  try {
    const res = await upsertArrearsPolicy({
      warnLine: policy.warnLine, stopLine: policy.stopLine,
      stopEnabled: policy.stopEnabled, stopClasses: policy.stopClasses, remark: policy.remark,
    });
    if (res.code === 200) {
      ElMessage.success('策略已保存');
      loadPolicy();
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    policySaving.value = false;
  }
};
// ---------------- 欠费榜 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadBoard = async () => {
  loading.value = true;
  try {
    const res = await getArrearsBoard({
      keyword: query.keyword.trim() || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {keyword: '', pageNum: 1});
  loadBoard();
};
const fmt = (v) => (v == null ? '0.00' : Number(v).toFixed(2));
onMounted(() => {
  loadPolicy();
  loadBoard();
});
</script>
