<script setup lang="ts">
/**
 * 欠费管控（G20，菜单 313）
 *
 * 两块：在院欠费患者榜（口径与住院账务概览一致：已发生 = L1 记账行应收净额，
 * 已收 = 净预交 + 账单上直接收的钱，欠费 = max(0, 已发生 − 已收)）
 * + 管控策略（预警线/停费线/开关）。
 * 安全底线：停费只拦择期类（检查/检验/治疗），药品/手术/急救永不拦截；预警不拦截。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getArrearsPolicy, upsertArrearsPolicy, getArrearsBoard } from '@/api/inpatientSettlement'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// ---------------- 策略 ----------------
const policy = reactive({
  warnLine: null as number | null, stopLine: null as number | null,
  stopEnabled: 0, stopClasses: '2,3,4', remark: '',
})
const policyLoading = ref(false)
const policySaving = ref(false)
const loadPolicy = async () => {
  policyLoading.value = true
  try {
    const res: any = await getArrearsPolicy()
    if (res.code === 200 && res.data) Object.assign(policy, res.data)
    else ElMessage.error(res.message || '读取策略失败')
  } catch (e) { console.error(e) } finally { policyLoading.value = false }
}
const savePolicy = async () => {
  if (policy.stopEnabled === 1 && !policy.stopLine) {
    ElMessage.warning('开启停费管控必须设置停费线')
    return
  }
  policySaving.value = true
  try {
    const res: any = await upsertArrearsPolicy({
      warnLine: policy.warnLine, stopLine: policy.stopLine,
      stopEnabled: policy.stopEnabled, stopClasses: policy.stopClasses, remark: policy.remark,
    })
    if (res.code === 200) { ElMessage.success('策略已保存'); loadPolicy() }
    else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { policySaving.value = false }
}

// ---------------- 欠费榜 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadBoard = async () => {
  loading.value = true
  try {
    const res: any = await getArrearsBoard({
      keyword: query.keyword.trim() || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { loading.value = false }
}
const reset = () => {
  Object.assign(query, { keyword: '', pageNum: 1 })
  loadBoard()
}
const fmt = (v: any) => (v == null ? '0.00' : Number(v).toFixed(2))

onMounted(() => { loadPolicy(); loadBoard() })
</script>

<template>
  <div>
    <!-- 策略 -->
    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm mb-3" v-loading="policyLoading">
      <div class="flex items-center gap-2 mb-3">
        <span class="font-medium text-gray-700">管控策略</span>
        <el-tag v-if="policy.stopEnabled === 1" type="danger" size="small">停费管控已开启</el-tag>
        <el-tag v-else type="info" size="small">停费管控关闭（欠费只提示不拦截）</el-tag>
      </div>
      <el-form inline label-width="90px">
        <el-form-item label="预警线(元)">
          <el-input-number v-model="policy.warnLine" :min="0" :precision="2" class="!w-40" />
        </el-form-item>
        <el-form-item label="停费线(元)">
          <el-input-number v-model="policy.stopLine" :min="0" :precision="2" class="!w-40" />
        </el-form-item>
        <el-form-item label="停费开关">
          <el-switch v-model="policy.stopEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="拦截类别">
          <el-input v-model="policy.stopClasses" class="!w-52" placeholder="2检查,3检验,4治疗" />
          <span class="text-gray-400 text-xs ml-2">药品/手术/输血永不拦截</span>
        </el-form-item>
        <el-form-item>
          <el-button v-perm="'charge:arrearsControl:add'" type="primary" :loading="policySaving" @click="savePolicy">保存策略</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 榜单 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="患者姓名 / 患者号" clearable style="width: 200px"
                    @keyup.enter="query.pageNum = 1; loadBoard()" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadBoard()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="arrears-board-table">
        <el-table-column prop="admissionNo" label="住院号" width="170" />
        <el-table-column prop="patientName" label="患者" width="110">
          <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 ml-1">{{ row.patientNo }}</span></template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="130">
          <template #default="{ row }">{{ row.deptName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
        </el-table-column>
        <el-table-column prop="admitTime" label="入院时间" width="160">
          <template #default="{ row }">{{ (row.admitTime || '').slice(0, 16).replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column prop="prepayBalance" label="住院账户余额" width="120" align="right">
          <template #default="{ row }">¥{{ fmt(row.prepayBalance) }}</template>
        </el-table-column>
        <el-table-column prop="chargedAmount" label="已发生费用" width="120" align="right">
          <template #default="{ row }">¥{{ fmt(row.chargedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="arrearsAmount" label="欠费金额" width="130" align="right">
          <template #default="{ row }">
            <span class="font-semibold" :class="Number(row.arrearsAmount) >= (policy.stopLine || 0) ? 'text-red-600' : 'text-orange-500'">
              ¥{{ fmt(row.arrearsAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="管控状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="policy.stopEnabled === 1 && Number(row.arrearsAmount) >= (policy.stopLine || Infinity)"
                    type="danger" size="small">停费拦截中</el-tag>
            <el-tag v-else-if="policy.warnLine && Number(row.arrearsAmount) >= policy.warnLine"
                    type="warning" size="small">已达预警线</el-tag>
            <el-tag v-else type="info" size="small">欠费提示</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadBoard" />
      </div>
    </el-card>
  </div>
</template>
