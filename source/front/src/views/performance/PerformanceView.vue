<script setup lang="ts">
/**
 * 绩效与成本核算（G23，菜单 908，挂财务结算；壳页重写为真实接口驱动）
 *
 * 口径：收入=收费明细月度净额（收入-退款抵扣，按明细 dept_id 归属）；药占比=药费/收入；
 * 结余=收入-成本；绩效=max(0,结余)×提成系数（默认 0.06）。
 * 成本同科室同月唯一（重复录入拒绝）；核算结果重算覆盖。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { perfCostSave, perfCostListPage, perfRevenueInfo, perfCalc, perfResultListPage } from '@/api/perf'
import { getDepartmentSelectList } from '@/api/system'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'

// ---------------- 科室 ----------------
const depts = ref<any[]>([])
const loadDepts = async () => {
  try {
    const res: any = await getDepartmentSelectList({})
    depts.value = res.data || []
  } catch (e) { console.error('加载科室失败', e) }
}
const deptName = (id: any) => depts.value.find(d => String(d.id) === String(id))?.deptName || '—'

// ---------------- 成本录入 ----------------
const costVisible = ref(false)
const costForm = reactive({
  deptId: null as any, costMonth: '', laborCost: 0, drugCost: 0,
  materialCost: 0, depreciation: 0, otherCost: 0, remark: '',
})
const openCost = () => {
  costForm.deptId = null
  const d = new Date()
  costForm.costMonth = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  costForm.laborCost = 0; costForm.drugCost = 0; costForm.materialCost = 0
  costForm.depreciation = 0; costForm.otherCost = 0; costForm.remark = ''
  costVisible.value = true
}
const submitCost = async () => {
  if (!costForm.deptId) return ElMessage.warning('请选择科室')
  if (!costForm.costMonth) return ElMessage.warning('请选择核算月份')
  try {
    await perfCostSave({
      deptId: costForm.deptId, deptName: deptName(costForm.deptId), costMonth: costForm.costMonth,
      laborCost: costForm.laborCost, drugCost: costForm.drugCost, materialCost: costForm.materialCost,
      depreciation: costForm.depreciation, otherCost: costForm.otherCost, remark: costForm.remark,
    })
    ElMessage.success('成本已录入')
    costVisible.value = false
    loadCostList()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '成本录入失败') }
}

// ---------------- 成本列表 ----------------
const costLoading = ref(false)
const costRows = ref<any[]>([])
const costTotal = ref(0)
const costQuery = reactive({ deptId: null as any, costMonth: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const loadCostList = async () => {
  costLoading.value = true
  try {
    const res: any = await perfCostListPage({
      deptId: costQuery.deptId ?? undefined,
      costMonth: costQuery.costMonth || undefined,
      pageNum: costQuery.pageNum, pageSize: costQuery.pageSize,
    })
    costRows.value = res?.data?.records || []
    costTotal.value = Number(res?.data?.total || 0)
  } catch (e) { console.error('加载成本失败', e) } finally { costLoading.value = false }
}

// ---------------- 执行核算 ----------------
const calcVisible = ref(false)
const calcForm = reactive({ deptId: null as any, costMonth: '', bonusRate: 0.06 })
const info = ref<any>(null)
const calcLoading = ref(false)
const openCalc = async () => {
  if (!calcForm.deptId) return ElMessage.warning('请选择科室')
  if (!calcForm.costMonth) return ElMessage.warning('请选择核算月份')
  try {
    const res: any = await perfRevenueInfo(calcForm.deptId, calcForm.costMonth)
    info.value = res?.data
    calcVisible.value = true
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '获取收入信息失败') }
}
const submitCalc = async () => {
  calcLoading.value = true
  try {
    const res: any = await perfCalc({
      deptId: calcForm.deptId, costMonth: calcForm.costMonth, bonusRate: calcForm.bonusRate,
    })
    ElMessage.success(`核算完成：${res?.data?.deptName} ${res?.data?.costMonth} 绩效 ¥${Number(res?.data?.perfAmount || 0).toLocaleString()}`)
    calcVisible.value = false
    loadPerfList()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '核算失败') } finally { calcLoading.value = false }
}

// ---------------- 绩效结果 ----------------
const perfLoading = ref(false)
const perfRows = ref<any[]>([])
const perfTotal = ref(0)
const perfQuery = reactive({ deptId: null as any, costMonth: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const loadPerfList = async () => {
  perfLoading.value = true
  try {
    const res: any = await perfResultListPage({
      deptId: perfQuery.deptId ?? undefined,
      costMonth: perfQuery.costMonth || undefined,
      pageNum: perfQuery.pageNum, pageSize: perfQuery.pageSize,
    })
    perfRows.value = res?.data?.records || []
    perfTotal.value = Number(res?.data?.total || 0)
  } catch (e) { console.error('加载绩效结果失败', e) } finally { perfLoading.value = false }
}
const money = (v: any) => '¥' + Number(v || 0).toLocaleString('zh-CN')
const surplusColor = (v: any) => (Number(v || 0) >= 0 ? 'text-green-600' : 'text-red-600')

onMounted(() => { loadDepts(); loadCostList(); loadPerfList() })
</script>

<template>
  <div class="p-5 space-y-4">
    <el-tabs>
      <el-tab-pane label="绩效核算结果">
        <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="perfQuery.deptId" placeholder="科室" clearable filterable style="width: 180px"
                       :fit-input-width="false">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)" />
            </el-select>
            <el-date-picker v-model="perfQuery.costMonth" type="month" value-format="YYYY-MM"
                            placeholder="核算月份" style="width: 130px" />
            <el-button type="primary" :icon="Search" @click="perfQuery.pageNum = 1; loadPerfList()">查询</el-button>
            <el-button :icon="Refresh" @click="perfQuery.deptId = null; perfQuery.costMonth = ''; perfQuery.pageNum = 1; loadPerfList()">重置</el-button>
            <div class="ml-auto flex items-center gap-2">
              <el-select v-model="calcForm.deptId" placeholder="选择科室" filterable style="width: 160px"
                         :fit-input-width="false" data-testid="perf-calc-dept">
                <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)" />
              </el-select>
              <el-date-picker v-model="calcForm.costMonth" type="month" value-format="YYYY-MM"
                              placeholder="核算月份" style="width: 130px" />
              <el-button type="primary" plain data-testid="perf-calc-btn" @click="openCalc">执行核算</el-button>
            </div>
          </div>
          <el-table :data="perfRows" v-loading="perfLoading" border stripe data-testid="perf-table">
            <el-table-column prop="costMonth" label="月份" width="85" />
            <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
            <el-table-column prop="revenue" label="收入" width="110" align="right">
              <template #default="{ row }">{{ money(row.revenue) }}</template>
            </el-table-column>
            <el-table-column prop="drugRatio" label="药占比" width="80" align="right">
              <template #default="{ row }">{{ row.drugRatio != null ? (Number(row.drugRatio) * 100).toFixed(1) + '%' : '—' }}</template>
            </el-table-column>
            <el-table-column prop="totalCost" label="成本" width="110" align="right">
              <template #default="{ row }">{{ money(row.totalCost) }}</template>
            </el-table-column>
            <el-table-column label="结余" width="110" align="right">
              <template #default="{ row }">
                <span :class="surplusColor(row.surplus)">{{ money(row.surplus) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bonusRate" label="系数" width="70" align="right">
              <template #default="{ row }">{{ Number(row.bonusRate || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="perfAmount" label="绩效金额" width="120" align="right">
              <template #default="{ row }">
                <b>{{ money(row.perfAmount) }}</b>
              </template>
            </el-table-column>
            <el-table-column prop="perfStatus" label="状态" width="85">
              <template #default="{ row }">
                <el-tag :type="row.perfStatus === 3 ? 'success' : row.perfStatus === 2 ? 'primary' : 'info'" size="small">
                  {{ ({ 1: '草稿', 2: '已核算', 3: '已发布' } as any)[row.perfStatus] || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="核算时间" width="160">
              <template #default="{ row }">{{ (row.updateTime || '').replace('T', ' ').slice(0, 19) || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="perfQuery.pageNum" :page-size="perfQuery.pageSize"
                           :total="perfTotal" layout="total, prev, pager, next" @current-change="loadPerfList" />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="科室月度成本">
        <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="costQuery.deptId" placeholder="科室" clearable filterable style="width: 180px"
                       :fit-input-width="false">
              <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)" />
            </el-select>
            <el-date-picker v-model="costQuery.costMonth" type="month" value-format="YYYY-MM"
                            placeholder="核算月份" style="width: 130px" />
            <el-button type="primary" :icon="Search" @click="costQuery.pageNum = 1; loadCostList()">查询</el-button>
            <el-button type="primary" plain class="ml-auto" v-perm="'report:perf:add'" data-testid="perf-cost-btn"
                       @click="openCost">成本录入</el-button>
          </div>
          <el-table :data="costRows" v-loading="costLoading" border stripe data-testid="perf-cost-table">
            <el-table-column prop="costMonth" label="月份" width="85" />
            <el-table-column prop="deptName" label="科室" width="140" show-overflow-tooltip />
            <el-table-column prop="laborCost" label="人力" width="100" align="right" />
            <el-table-column prop="drugCost" label="药品" width="100" align="right" />
            <el-table-column prop="materialCost" label="耗材" width="100" align="right" />
            <el-table-column prop="depreciation" label="折旧" width="100" align="right" />
            <el-table-column prop="otherCost" label="其他" width="100" align="right" />
            <el-table-column prop="totalCost" label="合计" width="110" align="right">
              <template #default="{ row }"><b>{{ money(row.totalCost) }}</b></template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.remark || '—' }}</template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="costQuery.pageNum" :page-size="costQuery.pageSize"
                           :total="costTotal" layout="total, prev, pager, next" @current-change="loadCostList" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 成本录入弹窗 -->
    <el-dialog v-model="costVisible" title="科室月度成本录入" width="520px">
      <el-form label-width="100px">
        <el-form-item label="科室" required>
          <el-select v-model="costForm.deptId" filterable placeholder="选择科室" style="width: 100%"
                     :fit-input-width="false" data-testid="perf-cost-dept">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="核算月份" required>
          <el-date-picker v-model="costForm.costMonth" type="month" value-format="YYYY-MM" style="width: 100%" />
        </el-form-item>
        <el-form-item label="人力成本"><el-input-number v-model="costForm.laborCost" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="药品成本"><el-input-number v-model="costForm.drugCost" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="耗材成本"><el-input-number v-model="costForm.materialCost" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="设备折旧"><el-input-number v-model="costForm.depreciation" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="其他成本"><el-input-number v-model="costForm.otherCost" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="costForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="costVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'report:perf:add'" data-testid="perf-cost-submit" @click="submitCost">保存</el-button>
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
          <el-input-number v-model="calcForm.bonusRate" :min="0" :max="1" :step="0.01" :precision="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="calcVisible = false">取消</el-button>
        <el-button type="primary" :loading="calcLoading" data-testid="perf-calc-submit" @click="submitCalc">确认核算</el-button>
      </template>
    </el-dialog>
  </div>
</template>
