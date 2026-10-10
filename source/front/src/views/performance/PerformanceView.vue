<template>
  <div class="p-5 space-y-4">
    <el-tabs>
      <el-tab-pane label="绩效核算结果">
        <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="perfQuery.deptId" :fit-input-width="false" clearable filterable placeholder="科室"
                       style="width: 180px">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
            </el-select>
            <el-date-picker v-model="perfQuery.costMonth" placeholder="核算月份" style="width: 130px"
                            type="month" value-format="YYYY-MM"/>
            <el-button :icon="Search" type="primary" @click="perfQuery.pageNum = 1; loadPerfList()">查询</el-button>
            <el-button :icon="Refresh"
                       @click="perfQuery.deptId = null; perfQuery.costMonth = ''; perfQuery.pageNum = 1; loadPerfList()">
              重置
            </el-button>
            <div class="ml-auto flex items-center gap-2">
              <el-select v-model="calcForm.deptId" :fit-input-width="false" data-testid="perf-calc-dept" filterable
                         placeholder="选择科室" style="width: 160px">
                <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
              </el-select>
              <el-date-picker v-model="calcForm.costMonth" placeholder="核算月份" style="width: 130px"
                              type="month" value-format="YYYY-MM"/>
              <el-button data-testid="perf-calc-btn" plain type="primary" @click="openCalc">执行核算</el-button>
            </div>
          </div>
          <el-table v-loading="perfLoading" :data="perfRows" border data-testid="perf-table" stripe>
            <el-table-column label="月份" prop="costMonth" width="85"/>
            <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="130"/>
            <el-table-column align="right" label="收入" prop="revenue" width="110">
              <template #default="{ row }">{{ money(row.revenue) }}</template>
            </el-table-column>
            <el-table-column align="right" label="药占比" prop="drugRatio" width="80">
              <template #default="{ row }">
                {{ row.drugRatio != null ? (Number(row.drugRatio) * 100).toFixed(1) + '%' : '—' }}
              </template>
            </el-table-column>
            <el-table-column align="right" label="成本" prop="totalCost" width="110">
              <template #default="{ row }">{{ money(row.totalCost) }}</template>
            </el-table-column>
            <el-table-column align="right" label="结余" width="110">
              <template #default="{ row }">
                <span :class="surplusColor(row.surplus)">{{ money(row.surplus) }}</span>
              </template>
            </el-table-column>
            <el-table-column align="right" label="系数" prop="bonusRate" width="70">
              <template #default="{ row }">{{ Number(row.bonusRate || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column align="right" label="绩效金额" prop="perfAmount" width="120">
              <template #default="{ row }">
                <b>{{ money(row.perfAmount) }}</b>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="perfStatus" width="85">
              <template #default="{ row }">
                <el-tag :type="row.perfStatus === 3 ? 'success' : row.perfStatus === 2 ? 'primary' : 'info'"
                        size="small">
                  {{ ({1: '草稿', 2: '已核算', 3: '已发布'} as any)[row.perfStatus] || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="核算时间" prop="updateTime" width="160">
              <template #default="{ row }">{{ (row.updateTime || '').replace('T', ' ').slice(0, 19) || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="perfQuery.pageNum" :page-size="perfQuery.pageSize"
                           :total="perfTotal" layout="total, prev, pager, next" @current-change="loadPerfList"/>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="科室月度成本">
        <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="costQuery.deptId" :fit-input-width="false" clearable filterable placeholder="科室"
                       style="width: 180px">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
            </el-select>
            <el-date-picker v-model="costQuery.costMonth" placeholder="核算月份" style="width: 130px"
                            type="month" value-format="YYYY-MM"/>
            <el-button :icon="Search" type="primary" @click="costQuery.pageNum = 1; loadCostList()">查询</el-button>
            <el-button v-perm="'report:perf:add'" class="ml-auto" data-testid="perf-cost-btn" plain type="primary"
                       @click="openCost">成本录入
            </el-button>
          </div>
          <el-table v-loading="costLoading" :data="costRows" border data-testid="perf-cost-table" stripe>
            <el-table-column label="月份" prop="costMonth" width="85"/>
            <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="140"/>
            <el-table-column align="right" label="人力" prop="laborCost" width="100"/>
            <el-table-column align="right" label="药品" prop="drugCost" width="100"/>
            <el-table-column align="right" label="耗材" prop="materialCost" width="100"/>
            <el-table-column align="right" label="折旧" prop="depreciation" width="100"/>
            <el-table-column align="right" label="其他" prop="otherCost" width="100"/>
            <el-table-column align="right" label="合计" prop="totalCost" width="110">
              <template #default="{ row }"><b>{{ money(row.totalCost) }}</b></template>
            </el-table-column>
            <el-table-column label="备注" min-width="140" prop="remark" show-overflow-tooltip>
              <template #default="{ row }">{{ row.remark || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="costQuery.pageNum" :page-size="costQuery.pageSize"
                           :total="costTotal" layout="total, prev, pager, next" @current-change="loadCostList"/>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 成本录入弹窗 -->
    <el-dialog v-model="costVisible" title="科室月度成本录入" width="520px">
      <el-form label-width="100px">
        <el-form-item label="科室" required>
          <el-select v-model="costForm.deptId" :fit-input-width="false" data-testid="perf-cost-dept" filterable
                     placeholder="选择科室" style="width: 100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="核算月份" required>
          <el-date-picker v-model="costForm.costMonth" style="width: 100%" type="month" value-format="YYYY-MM"/>
        </el-form-item>
        <el-form-item label="人力成本">
          <el-input-number v-model="costForm.laborCost" :min="0" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="药品成本">
          <el-input-number v-model="costForm.drugCost" :min="0" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="耗材成本">
          <el-input-number v-model="costForm.materialCost" :min="0" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="设备折旧">
          <el-input-number v-model="costForm.depreciation" :min="0" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="其他成本">
          <el-input-number v-model="costForm.otherCost" :min="0" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="costForm.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="costVisible = false">取消</el-button>
        <el-button v-perm="'report:perf:add'" data-testid="perf-cost-submit" type="primary" @click="submitCost">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 核算确认弹窗 -->
    <el-dialog v-model="calcVisible" title="执行绩效核算" width="480px">
      <div v-if="info" class="text-sm space-y-2 mb-3 p-3 rounded bg-gray-50" data-testid="perf-calc-info">
        <p>科室：{{ info.deptName }}（{{ info.costMonth }}）</p>
        <p>当月收入：<b>{{ money(info.revenue) }}</b>｜药品收入 {{ money(info.drugRevenue) }}</p>
        <p>成本：<b>{{ info.costExists ? money(info.totalCost) : '未录入（按 0 计）' }}</b></p>
        <p>结余：<b :class="surplusColor(info.surplus)">{{ money(info.surplus) }}</b></p>
      </div>
      <el-form label-width="100px">
        <el-form-item label="提成系数">
          <el-input-number v-model="calcForm.bonusRate" :max="1" :min="0" :precision="4" :step="0.01"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="calcVisible = false">取消</el-button>
        <el-button :loading="calcLoading" data-testid="perf-calc-submit" type="primary" @click="submitCalc">确认核算
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 绩效与成本核算（G23，菜单 908，挂财务结算；壳页重写为真实接口驱动）
 *
 * 口径：收入=收费明细月度净额（收入-退款抵扣，按明细 dept_id 归属）；药占比=药费/收入；
 * 结余=收入-成本；绩效=max(0,结余)×提成系数（默认 0.06）。
 * 成本同科室同月唯一（重复录入拒绝）；核算结果重算覆盖。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {perfCalc, perfCostListPage, perfCostSave, perfResultListPage, perfRevenueInfo} from '@/api/perf';
import {getDepartmentSelectList} from '@/api/system';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
// ---------------- 科室 ----------------
const depts = ref([]);
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    depts.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
const deptName = (id) => depts.value.find(d => String(d.id) === String(id))?.deptName || '—';
// ---------------- 成本录入 ----------------
const costVisible = ref(false);
const costForm = reactive({
  deptId: null, costMonth: '', laborCost: 0, drugCost: 0,
  materialCost: 0, depreciation: 0, otherCost: 0, remark: '',
});
const openCost = () => {
  costForm.deptId = null;
  const d = new Date();
  costForm.costMonth = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  costForm.laborCost = 0;
  costForm.drugCost = 0;
  costForm.materialCost = 0;
  costForm.depreciation = 0;
  costForm.otherCost = 0;
  costForm.remark = '';
  costVisible.value = true;
};
const submitCost = async () => {
  if (!costForm.deptId)
    return ElMessage.warning('请选择科室');
  if (!costForm.costMonth)
    return ElMessage.warning('请选择核算月份');
  try {
    await perfCostSave({
      deptId: costForm.deptId, deptName: deptName(costForm.deptId), costMonth: costForm.costMonth,
      laborCost: costForm.laborCost, drugCost: costForm.drugCost, materialCost: costForm.materialCost,
      depreciation: costForm.depreciation, otherCost: costForm.otherCost, remark: costForm.remark,
    });
    ElMessage.success('成本已录入');
    costVisible.value = false;
    loadCostList();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '成本录入失败');
  }
};
// ---------------- 成本列表 ----------------
const costLoading = ref(false);
const costRows = ref([]);
const costTotal = ref(0);
const costQuery = reactive({deptId: null, costMonth: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadCostList = async () => {
  costLoading.value = true;
  try {
    const res = await perfCostListPage({
      deptId: costQuery.deptId ?? undefined,
      costMonth: costQuery.costMonth || undefined,
      pageNum: costQuery.pageNum, pageSize: costQuery.pageSize,
    });
    costRows.value = res?.data?.records || [];
    costTotal.value = Number(res?.data?.total || 0);
  } catch (e) {
    console.error('加载成本失败', e);
  } finally {
    costLoading.value = false;
  }
};
// ---------------- 执行核算 ----------------
const calcVisible = ref(false);
const calcForm = reactive({deptId: null, costMonth: '', bonusRate: 0.06});
const info = ref(null);
const calcLoading = ref(false);
const openCalc = async () => {
  if (!calcForm.deptId)
    return ElMessage.warning('请选择科室');
  if (!calcForm.costMonth)
    return ElMessage.warning('请选择核算月份');
  try {
    const res = await perfRevenueInfo(calcForm.deptId, calcForm.costMonth);
    info.value = res?.data;
    calcVisible.value = true;
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '获取收入信息失败');
  }
};
const submitCalc = async () => {
  calcLoading.value = true;
  try {
    const res = await perfCalc({
      deptId: calcForm.deptId, costMonth: calcForm.costMonth, bonusRate: calcForm.bonusRate,
    });
    ElMessage.success(`核算完成：${res?.data?.deptName} ${res?.data?.costMonth} 绩效 ¥${Number(res?.data?.perfAmount || 0).toLocaleString()}`);
    calcVisible.value = false;
    loadPerfList();
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '核算失败');
  } finally {
    calcLoading.value = false;
  }
};
// ---------------- 绩效结果 ----------------
const perfLoading = ref(false);
const perfRows = ref([]);
const perfTotal = ref(0);
const perfQuery = reactive({deptId: null, costMonth: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadPerfList = async () => {
  perfLoading.value = true;
  try {
    const res = await perfResultListPage({
      deptId: perfQuery.deptId ?? undefined,
      costMonth: perfQuery.costMonth || undefined,
      pageNum: perfQuery.pageNum, pageSize: perfQuery.pageSize,
    });
    perfRows.value = res?.data?.records || [];
    perfTotal.value = Number(res?.data?.total || 0);
  } catch (e) {
    console.error('加载绩效结果失败', e);
  } finally {
    perfLoading.value = false;
  }
};
const money = (v) => '¥' + Number(v || 0).toLocaleString('zh-CN');
const surplusColor = (v) => (Number(v || 0) >= 0 ? 'text-green-600' : 'text-red-600');
onMounted(() => {
  loadDepts();
  loadCostList();
  loadPerfList();
});
</script>
