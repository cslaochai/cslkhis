<script setup lang="ts">
/**
 * 药品入库单（G9）
 *
 * 状态机：1待审核 → 2已审核 → 3已入库；1/2 可取消（4已取消）。
 * **只有「入库」这一步动库存**：按明细「药品+批号」建/加批次并写库存流水（来源 drugInbound+单号）；
 * 生成入库单本身不动库存。已入库的不可取消/删除 —— 冲销走退货入库。
 *
 * 入库类型/状态文案走字典（his_drug_inbound_type / his_inbound_status）；
 * 明细状态无字典，单点 lib/purchase.js detailStatusText —— 未知码值一律「未知(n)」。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import {
  getInboundList,
  getInboundDetail,
  auditInbound,
  stockInInbound,
  cancelInbound,
  deleteInbound,
} from '@/api/purchase'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText, formatMoney } from '@/lib/utils'
import {
  canAuditInbound,
  canStockIn,
  canCancelInbound,
  canDeleteInbound,
  detailStatusText,
  detailStatusTagType,
  inboundStatusTagType,
} from '@/lib/purchase'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  inboundNo: '',
  purchaseOrderNo: '',
  inboundType: null as number | null,
  inboundStatus: null as number | null,
  dateRange: null as [string, string] | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const statusDict = ref<any[]>([])
const typeDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const typeText = (v: any) => dictLabelText(typeDict.value, v)

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.INBOUND_STATUS},${DICT_TYPE.DRUG_INBOUND_TYPE}`
    )
    statusDict.value = res?.data?.[DICT_TYPE.INBOUND_STATUS] || []
    typeDict.value = res?.data?.[DICT_TYPE.DRUG_INBOUND_TYPE] || []
  } catch (e) {
    console.error('加载入库字典失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getInboundList({
      inboundNo: query.inboundNo?.trim() || undefined,
      purchaseOrderNo: query.purchaseOrderNo?.trim() || undefined,
      inboundType: query.inboundType ?? undefined,
      inboundStatus: query.inboundStatus ?? undefined,
      dateStart: query.dateRange?.[0] || undefined,
      dateEnd: query.dateRange?.[1] || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || []
      total.value = Number(res.data.total || 0)
    } else {
      ElMessage.error(res.message || '加载入库单失败')
    }
  } catch (e) {
    ElMessage.error('加载入库单失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.inboundNo = ''
  query.purchaseOrderNo = ''
  query.inboundType = null
  query.inboundStatus = null
  query.dateRange = null
  query.pageNum = 1
  loadList()
}

// ==================== 操作 ====================
const doAudit = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认审核通过入库单 ${row.inboundNo}？审核后方可入库。`, '审核确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res: any = await auditInbound(row.id)
    if (res.code === 200) {
      ElMessage.success('已审核')
      loadList()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (e) {
    console.error(e)
  }
}

const doStockIn = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `入库将按明细「药品+批号」增加库存批次并写库存流水，入库后不可取消。确认入库 ${row.inboundNo}？`,
      '入库确认',
      { type: 'warning', confirmButtonText: '确认入库' }
    )
  } catch {
    return
  }
  try {
    const res: any = await stockInInbound(row.id)
    if (res.code === 200) {
      ElMessage.success('已入库，库存批次已更新')
      loadList()
    } else {
      ElMessage.error(res.message || '入库失败')
    }
  } catch (e) {
    console.error(e)
  }
}

const doCancel = async (row: any) => {
  let reason = ''
  try {
    const r = await ElMessageBox.prompt('取消必须填写原因', `取消入库单 ${row.inboundNo}`, {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '取消原因（必填）',
      inputValidator: (v: string) => (v && v.trim() ? true : '取消原因不能为空'),
    })
    reason = r.value.trim()
  } catch {
    return
  }
  try {
    const res: any = await cancelInbound({ inboundId: row.id, cancelReason: reason })
    if (res.code === 200) {
      ElMessage.success('已取消')
      loadList()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    console.error(e)
  }
}

const doDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定删除入库单 ${row.inboundNo}？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res: any = await deleteInbound(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    console.error(e)
  }
}

// ==================== 详情抽屉 ====================
const drawerVisible = ref(false)
const detail = ref<any>(null)
const activeAmount = computed(() =>
  (detail.value?.items || [])
    .filter((d: any) => Number(d.detailStatus) !== 3)
    .reduce((s: number, d: any) => s + Number(d.amount || 0), 0)
    .toFixed(2)
)
const openDetail = async (row: any) => {
  try {
    const res: any = await getInboundDetail(row.id)
    if (res.code === 200) {
      detail.value = res.data
      drawerVisible.value = true
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadDicts()
  loadList()
})
</script>

<template>
  <div>
    <!-- 页头 -->
    <div class="mb-3 flex items-center justify-between">
      <h2 class="text-lg font-semibold text-slate-800">药品入库单</h2>
      <span class="text-sm text-slate-400">入库单由采购订单页生成；审核通过后点「入库」才会增加库存</span>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="入库单号">
          <el-input
            v-model="query.inboundNo"
            placeholder="入库单号"
            clearable
            style="width: 170px"
            data-testid="inbound-no-filter"
            @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="采购订单号">
          <el-input
            v-model="query.purchaseOrderNo"
            placeholder="采购订单号"
            clearable
            style="width: 170px"
            @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="入库类型">
          <el-select v-model="query.inboundType" placeholder="全部" clearable style="width: 130px" :fit-input-width="false">
            <el-option label="采购入库" :value="1" />
            <el-option label="退货入库" :value="2" />
            <el-option label="盘盈入库" :value="3" />
            <el-option label="其他入库" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.inboundStatus" placeholder="全部" clearable style="width: 120px" :fit-input-width="false">
            <el-option label="待审核" :value="1" />
            <el-option label="已审核" :value="2" />
            <el-option label="已入库" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="inbound-table">
        <el-table-column prop="inboundNo" label="入库单号" width="150" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">{{ typeText(row.inboundType) }}</template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" min-width="150" show-overflow-tooltip />
        <el-table-column prop="purchaseOrderNo" label="采购订单号" width="150">
          <template #default="{ row }">{{ row.purchaseOrderNo || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="数量合计" width="90" align="right">
          <template #default="{ row }">{{ row.totalQuantity }}</template>
        </el-table-column>
        <el-table-column label="金额合计" width="110" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="inboundStatusTagType(row.inboundStatus)">{{ statusText(row.inboundStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canAuditInbound(row)" v-perm="'pharmacy:inbound:edit'" link type="success" @click="doAudit(row)">审核</el-button>
            <el-button v-if="canStockIn(row)" v-perm="'pharmacy:inbound:edit'" link type="warning" data-testid="inbound-stockin-btn" @click="doStockIn(row)">入库</el-button>
            <el-button v-if="canCancelInbound(row)" v-perm="'pharmacy:inbound:delete'" link type="info" @click="doCancel(row)">取消</el-button>
            <el-button v-if="canDeleteInbound(row)" v-perm="'pharmacy:inbound:delete'" link type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          layout="total, prev, pager, next, sizes"
          :page-sizes="PAGE_SIZES"
          :total="total"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          @current-change="loadList"
          @size-change="query.pageNum = 1; loadList()"
        />
      </div>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="入库单详情" size="820px" data-testid="inbound-detail-drawer">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small" class="mb-4">
          <el-descriptions-item label="入库单号">{{ detail.inboundNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeText(detail.inboundType) }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detail.supplier || '—' }}</el-descriptions-item>
          <el-descriptions-item label="采购订单号">{{ detail.purchaseOrderNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="inboundStatusTagType(detail.inboundStatus)">{{ statusText(detail.inboundStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="金额合计">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ detail.auditBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="入库人">{{ detail.inboundBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ detail.cancelReason || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detail.items || []" border size="small" data-testid="inbound-detail-table">
          <el-table-column prop="drugCode" label="编码" width="100" />
          <el-table-column prop="drugName" label="药品" min-width="130" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格" width="100" />
          <el-table-column prop="batchNo" label="批号" width="90" />
          <el-table-column prop="expiryDate" label="效期至" width="100" />
          <el-table-column prop="quantity" label="数量" width="70" align="right" />
          <el-table-column label="成本价" width="90" align="right">
            <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="95" align="right">
            <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="明细状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="detailStatusTagType(row.detailStatus)" size="small">
                {{ detailStatusText(row.detailStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="mt-2 text-sm text-slate-500 text-right">有效明细金额合计：¥{{ activeAmount }}</div>
      </template>
    </el-drawer>
  </div>
</template>
