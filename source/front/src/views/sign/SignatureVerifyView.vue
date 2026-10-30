<template>
  <div class="space-y-6">
    <!-- 概览 -->
    <div v-if="overview" v-loading="overviewLoading" class="grid grid-cols-2 gap-4 lg:grid-cols-5"
         data-testid="p5-sign-overview">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">有效签名</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-sign-stat-valid">{{
            overview.validSign
          }}</p>
        <p class="mt-1 text-xs text-slate-400">今日新增 {{ overview.todaySign }} 条 · 最近
          {{ text(overview.lastSignTime) }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">已作废</p>
        <p class="mt-1 text-2xl font-semibold text-amber-600" data-testid="p5-sign-stat-invalid">{{
            overview.invalidSign
          }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">未校验</p>
        <p class="mt-1 text-2xl font-semibold text-slate-500" data-testid="p5-sign-stat-unchecked">
          {{ overview.verifyUnchecked }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">验签失败</p>
        <p class="mt-1 text-2xl font-semibold text-red-600" data-testid="p5-sign-stat-failed">{{
            overview.verifyFailed
          }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">签名证书</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-sign-stat-cert">{{
            overview.certActive
          }}</p>
        <p class="mt-1 text-xs text-slate-400">
          共 {{ overview.certTotal }} 张 · 已吊销 {{ overview.certRevoked }}
        </p>
      </div>
    </div>

    <!-- 覆盖情况 -->
    <div v-if="overview" class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-4 py-3">
        <div>
          <h2 class="text-sm font-semibold text-slate-900">各业务对象签名覆盖情况</h2>
          <p class="mt-0.5 text-xs text-slate-500">
            已接入 {{ overview.bizTypeCount }} 类业务对象：{{ text(overview.bizTypeTexts) }}
          </p>
        </div>
        <div class="text-xs text-slate-500">
          未签名 <b class="text-amber-600" data-testid="p5-sign-pending-total">{{ pendingTotal }}</b> 份 · 签名已失效
          <b class="text-slate-600" data-testid="p5-sign-invalidated-total">{{ invalidatedTotal }}</b> 份
        </div>
      </div>
      <div class="p-4">
        <el-table :data="coverages" border data-testid="p5-sign-coverage-table" size="small">
          <el-table-column label="业务对象" width="140">
            <template #default="{ row }">
              <span class="text-xs font-medium">{{ row.bizTypeText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="对象总数" width="100">
            <template #default="{ row }">
              <span class="text-xs">{{ num(row.total) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已签名" width="90">
            <template #default="{ row }">
              <span :data-testid="`p5-sign-cov-signed-${row.bizType}`"
                    class="text-xs font-medium text-emerald-600">{{ num(row.signed) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="未签名（存量）" width="140">
            <template #default="{ row }">
              <span :data-testid="`p5-sign-cov-pending-${row.bizType}`"
                    class="text-xs text-amber-600">{{ num(row.pendingSign) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="签名已失效" width="110">
            <template #default="{ row }">
              <span :data-testid="`p5-sign-cov-invalidated-${row.bizType}`"
                    class="text-xs text-slate-500">{{ num(row.invalidated) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="覆盖率" width="100">
            <template #default="{ row }">
              <span
                  :class="isRatePlaceholder(row.signedRateText) ? 'text-slate-400' : 'text-slate-800'"
                  :data-testid="`p5-sign-cov-rate-${row.bizType}`"
                  class="text-xs font-medium"
              >{{ row.signedRateText }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ---------------- 签名记录 ---------------- -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 px-4 py-3">
        <el-select v-model="signQuery.bizType" clearable data-testid="p5-sign-f-biztype" placeholder="对象类型"
                   style="width: 130px" @change="searchSign">
          <el-option v-for="o in options.bizTypes || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
        </el-select>
        <el-select v-model="signQuery.signScene" clearable data-testid="p5-sign-f-scene" placeholder="签名场景"
                   style="width: 120px" @change="searchSign">
          <el-option v-for="o in options.scenes || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
        </el-select>
        <el-select v-model="signQuery.signStatus" clearable data-testid="p5-sign-f-status" placeholder="签名状态"
                   style="width: 120px" @change="searchSign">
          <el-option v-for="o in options.signStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
        </el-select>
        <el-select v-model="signQuery.verifyStatus" clearable data-testid="p5-sign-f-verify" placeholder="验签结果"
                   style="width: 130px" @change="searchSign">
          <el-option v-for="o in options.verifyStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
        </el-select>
        <el-date-picker
            v-model="timeRange"
            data-testid="p5-sign-f-timerange"
            end-placeholder="签名止"
            start-placeholder="签名起"
            style="width: 330px"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="searchSign"
        />
        <el-input v-model="signQuery.keyword" clearable data-testid="p5-sign-f-keyword"
                  placeholder="签名人 / 患者 / 对象单号"
                  style="width: 220px" @keyup.enter="searchSign">
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="searchSign">查询</el-button>
        <el-button @click="clearSignFilter">清除</el-button>
      </div>

      <div v-loading="signLoading" class="p-4">
        <el-table :data="signList" border data-testid="p5-sign-table" size="small">
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
              <span :data-testid="`p5-sign-row-digest-${row.signNo}`"
                    class="font-mono text-xs text-slate-600">{{ row.contentDigestShort || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="签名时间" width="170">
            <template #default="{ row }">
              <span class="text-xs">{{ row.signedTime || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :data-testid="`p5-sign-row-status-${row.signNo}`"
                      :type="row.signStatus === 1 ? 'success' : 'info'"
                      size="small">
                {{ row.signStatusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="验签" width="110">
            <template #default="{ row }">
              <el-tag :data-testid="`p5-sign-row-verify-${row.signNo}`" :type="verifyTag(row.verifyStatus)"
                      size="small">
                {{ row.verifyStatusText }}
              </el-tag>
              <div v-if="row.verifyTime" class="mt-0.5 text-xs text-slate-400">{{ row.verifyTime }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="80">
            <template #default="{ row }">
              <el-button :data-testid="`p5-sign-open-${row.signNo}`" link size="small" type="primary"
                         @click.stop="openDetail(row)">详情
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">暂无签名记录</div>
          </template>
        </el-table>

        <div class="mt-3 flex items-center justify-between">
          <p class="text-xs text-slate-500" data-testid="p5-sign-total">共 {{ signTotal }} 条签名记录（含已作废）</p>
          <el-pagination
              v-model:current-page="signQuery.pageNum"
              v-model:page-size="signQuery.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="signTotal"
              layout="sizes, prev, pager, next"
              @current-change="loadSignList"
              @size-change="() => { signQuery.pageNum = 1; loadSignList() }"
          />
        </div>
      </div>
    </div>

    <!-- ---------------- 签名详情弹框（子组件 SignatureCenterDetail） ---------------- -->
    <SignatureCenterDetail v-model="drawerVisible" :id="detailId" @saved="onDetailSaved"/>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {Search} from '@element-plus/icons-vue';
import {
  getSignatureList,
  getSignatureQueryOptions,
  getSignatureSummary,
} from '@/api/signature';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {num, text} from '@/lib/sign';
import SignatureCenterDetail from './SignatureCenterDetail.vue';
// ---------------- 概览 ----------------
const overviewLoading = ref(false);
const overview = ref(null);
// ---------------- 下拉选项 ----------------
const options = ref({});
// ---------------- 签名记录 ----------------
const signLoading = ref(false);
const signList = ref([]);
const signTotal = ref(0);
const signQuery = reactive({
  bizType: undefined,
  signScene: undefined,
  signStatus: undefined,
  verifyStatus: undefined,
  keyword: '',
  beginTime: undefined,
  endTime: undefined,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const timeRange = ref(null);
// ---------------- 详情弹框（子组件 SignatureCenterDetail） ----------------
const drawerVisible = ref(false);
const detailId = ref(null);
// ---------------- 加载 ----------------
const loadOverview = async () => {
  overviewLoading.value = true;
  try {
    const res = await getSignatureSummary();
    overview.value = res.data;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    overviewLoading.value = false;
  }
};
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
    if (signQuery.bizType !== undefined && signQuery.bizType !== null)
      params.bizType = signQuery.bizType;
    if (signQuery.signScene !== undefined && signQuery.signScene !== null)
      params.signScene = signQuery.signScene;
    if (signQuery.signStatus !== undefined && signQuery.signStatus !== null)
      params.signStatus = signQuery.signStatus;
    if (signQuery.verifyStatus !== undefined && signQuery.verifyStatus !== null)
      params.verifyStatus = signQuery.verifyStatus;
    if (signQuery.keyword)
      params.keyword = signQuery.keyword.trim();
    if (timeRange.value && timeRange.value[0])
      params.beginTime = timeRange.value[0];
    if (timeRange.value && timeRange.value[1])
      params.endTime = timeRange.value[1];
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
  signQuery.bizType = undefined;
  signQuery.signScene = undefined;
  signQuery.signStatus = undefined;
  signQuery.verifyStatus = undefined;
  signQuery.keyword = '';
  timeRange.value = null;
  signQuery.pageNum = 1;
  loadSignList();
};
// ---------------- 详情 / 验签 / 作废（子组件 SignatureCenterDetail，弹框） ----------------
const openDetail = (row) => {
  detailId.value = row.id;
  drawerVisible.value = true;
};
const onDetailSaved = async () => {
  await Promise.all([loadSignList(), loadOverview()]);
};
// ---------------- 展示辅助 ----------------
const coverages = computed(() => overview.value?.coverages || []);
const pendingTotal = computed(() => coverages.value.reduce((sum, c) => sum + num(c.pendingSign), 0));
const invalidatedTotal = computed(() => coverages.value.reduce((sum, c) => sum + num(c.invalidated), 0));
// 分母为 0 时后端给的是「—（…）」占位，不是比率
const isRatePlaceholder = (t) => !t || String(t).startsWith('—');
// 0-未校验（灰）1-通过（绿）2-失败（红）
const verifyTag = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'info');
onMounted(async () => {
  await loadOptions();
  await Promise.all([loadOverview(), loadSignList()]);
});
</script>
