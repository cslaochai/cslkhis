<template>
  <div>
    <!-- 日志审计-签名记录：只读列表（无统计卡、无验签/作废入口，详情走 readonly 弹框） -->
    <div class="filter-bar">
      <el-select v-model="signQuery.signStatus" clearable data-testid="log-sign-f-status" placeholder="签名状态"
                 style="width: 120px" @change="searchSign">
        <el-option v-for="o in options.signStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
      </el-select>
      <el-select v-model="signQuery.verifyStatus" clearable data-testid="log-sign-f-verify" placeholder="验签结果"
                 style="width: 130px" @change="searchSign">
        <el-option v-for="o in options.verifyStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
      </el-select>
      <el-input v-model="signQuery.keyword" clearable data-testid="log-sign-f-keyword"
                placeholder="签名人 / 患者 / 对象单号"
                style="width: 220px" @keyup.enter="searchSign">
        <template #prefix>
          <el-icon>
            <Search/>
          </el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="searchSign">查询</el-button>
      <el-button @click="clearSignFilter">重置</el-button>
    </div>

    <el-table v-loading="signLoading" :data="signList" border data-testid="log-sign-table" size="small" stripe>
      <el-table-column label="签名编号" width="170">
        <template #default="{ row }">
          <span class="text-xs font-medium text-slate-700">{{ row.signNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="对象" width="190">
        <template #default="{ row }">
          <div class="text-xs">{{ row.bizTypeText }} · {{ row.bizNo || '—' }}</div>
          <div class="text-xs text-slate-400">{{ row.patientName || '—' }}<span
              v-if="row.deptName"> · {{ row.deptName }}</span></div>
        </template>
      </el-table-column>
      <el-table-column label="场景" width="80">
        <template #default="{ row }">
          <div class="text-xs">{{ row.signSceneText }}</div>
          <div class="text-xs text-slate-400">第 {{ row.chainNo }} 次</div>
        </template>
      </el-table-column>
      <el-table-column label="签名人" width="130">
        <template #default="{ row }">
          <div class="text-xs">{{ row.signerName || '—' }}</div>
          <div class="text-xs text-slate-400">{{ row.signerDeptName || '—' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="摘要" width="150">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-600">{{ row.contentDigestShort || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签名时间" width="170">
        <template #default="{ row }">
          <span class="text-xs">{{ row.signedTime || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :data-testid="`log-sign-row-status-${row.signNo}`"
                  :type="row.signStatus === 1 ? 'success' : 'info'"
                  size="small">
            {{ row.signStatusText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="验签" width="110">
        <template #default="{ row }">
          <el-tag :data-testid="`log-sign-row-verify-${row.signNo}`" :type="verifyTag(row.verifyStatus)"
                  size="small">
            {{ row.verifyStatusText }}
          </el-tag>
          <div v-if="row.verifyTime" class="mt-0.5 text-xs text-slate-400">{{ row.verifyTime }}</div>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="80">
        <template #default="{ row }">
          <el-button :data-testid="`log-sign-open-${row.signNo}`" link size="small" type="primary"
                     @click.stop="openDetail(row)">详情
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <span data-testid="log-sign-empty">暂无签名记录</span>
      </template>
    </el-table>

    <div class="pager">
      <el-pagination
          v-model:current-page="signQuery.pageNum"
          v-model:page-size="signQuery.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="signTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadSignList"
          @size-change="() => { signQuery.pageNum = 1; loadSignList() }"
      />
    </div>

    <!-- 只读详情：readonly 模式下不出现验签/作废入口 -->
    <SignatureCenterDetail v-model="drawerVisible" :id="detailId" readonly/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {Search} from '@element-plus/icons-vue';
import {getSignatureList, getSignatureQueryOptions} from '@/api/signature';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import SignatureCenterDetail from '@/views/sign/SignatureCenterDetail.vue';

// ---------------- 下拉选项（签名状态 / 验签结果） ----------------
const options = ref({});
// ---------------- 签名记录（只读） ----------------
const signLoading = ref(false);
const signList = ref([]);
const signTotal = ref(0);
const signQuery = reactive({
  signStatus: undefined,
  verifyStatus: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 详情弹框（只读）
const drawerVisible = ref(false);
const detailId = ref(null);

const loadOptions = async () => {
  try {
    const res = await getSignatureQueryOptions();
    options.value = res.data || {};
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const loadSignList = async () => {
  signLoading.value = true;
  try {
    const params = {pageNum: signQuery.pageNum, pageSize: signQuery.pageSize};
    if (signQuery.signStatus !== undefined && signQuery.signStatus !== null)
      params.signStatus = signQuery.signStatus;
    if (signQuery.verifyStatus !== undefined && signQuery.verifyStatus !== null)
      params.verifyStatus = signQuery.verifyStatus;
    if (signQuery.keyword)
      params.keyword = signQuery.keyword.trim();
    const res = await getSignatureList(params);
    signList.value = res.data?.records || [];
    signTotal.value = Number(res.data?.total ?? 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    signLoading.value = false;
  }
};
const searchSign = () => {
  signQuery.pageNum = 1;
  loadSignList();
};
const clearSignFilter = () => {
  signQuery.signStatus = undefined;
  signQuery.verifyStatus = undefined;
  signQuery.keyword = '';
  signQuery.pageNum = 1;
  loadSignList();
};
const openDetail = (row) => {
  detailId.value = row.id;
  drawerVisible.value = true;
};
// 0-未校验（灰）1-通过（绿）2-失败（红）
const verifyTag = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'info');
onMounted(async () => {
  await loadOptions();
  await loadSignList();
});
</script>
