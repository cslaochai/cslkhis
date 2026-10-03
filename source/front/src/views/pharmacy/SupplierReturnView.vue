<script setup lang="ts">
/**
 * 药品供应商退货（sql/154 ③级：把库存批次退给供应商）
 *
 * 状态机：1待退货 →（确认退货）2已退货；只有「待退货」可改明细/作废/删除。
 * **只有「确认退货」这一步动库存**：逐批次扣减并落 9-退货出库流水（来源 supplierReturn+退货单号），
 * 金额按批次成本价 × 数量服务端算 —— 这是向供应商主张退款的依据，让前端传金额等于让它编。
 *
 * ⚠ 批次必须挂了 supplier_id 才能退：历史数据里 `supplier` 那一列多是**生产厂家名**而不是供货商，
 * 拿它当退货对象会把钱要错人，所以候选列表直接只出有结构化供应商的批次（药库侧）。
 * 药房的批次应先开「药房退回药库」调拨单搬回药库，再从这里退供应商 —— 两级不能跳。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Plus, Delete } from '@element-plus/icons-vue'
import {
  listSupplierReturnPage,
  getSupplierReturnDetail,
  upsertSupplierReturn,
  confirmSupplierReturn,
  cancelSupplierReturn,
  deleteSupplierReturnById,
  getStockBatchCandidates,
} from '@/api/pharmacy'
import { getSupplierSelectList } from '@/api/purchase'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText, formatMoney } from '@/lib/utils'
import {
  canConfirmReturn,
  canEditReturn,
  canCancelReturn,
  canDeleteReturn,
  returnStatusTagType,
} from '@/lib/supplierReturn'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  returnNo: '',
  supplierId: null as string | null,
  status: null as number | null,
  keyword: '',
  dateRange: null as [string, string] | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const statusDict = ref<any[]>([])
const roomDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const roomText = (v: any) => dictLabelText(roomDict.value, v)

const suppliers = ref<any[]>([])
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.SUPPLIER_RETURN_STATUS},${DICT_TYPE.STOCK_ROOM}`)
    statusDict.value = res?.data?.[DICT_TYPE.SUPPLIER_RETURN_STATUS] || []
    roomDict.value = res?.data?.[DICT_TYPE.STOCK_ROOM] || []
  } catch (e) {
    console.error('加载退货字典失败', e)
  }
}
const loadSuppliers = async () => {
  try {
    const res: any = await getSupplierSelectList()
    suppliers.value = res?.data || []
  } catch (e) {
    console.error('加载供应商下拉失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await listSupplierReturnPage({
      returnNo: query.returnNo?.trim() || undefined,
      supplierId: query.supplierId || undefined,
      status: query.status ?? undefined,
      keyword: query.keyword?.trim() || undefined,
      dateStart: query.dateRange?.[0] || undefined,
      dateEnd: query.dateRange?.[1] || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || []
      total.value = Number(res.data.total || 0)
    } else {
      ElMessage.error(res.message || '加载退货单失败')
    }
  } catch (e) {
    ElMessage.error('加载退货单失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.returnNo = ''
  query.supplierId = null
  query.status = null
  query.keyword = ''
  query.dateRange = null
  query.pageNum = 1
  loadList()
}

// ==================== 建单 / 改明细 ====================
const formVisible = ref(false)
const formSaving = ref(false)
const form = reactive({
  id: null as string | null,
  supplierId: null as string | null,
  returnReason: '',
  srcRefNo: '',
  remark: '',
})
/** 已选明细：一条 = 一个库存批次（stockId 保持字符串，雪花 ID 转 Number 会丢精度） */
const formItems = ref<any[]>([])
const candidateLoading = ref(false)
const candidates = ref<any[]>([])
const candidateKeyword = ref('')
const candidateRoom = ref<number | null>(1)
const formAmount = computed(() =>
  formItems.value.reduce((s, it) => s + Number(it.costPrice || 0) * Number(it.quantity || 0), 0)
)
/**
 * 候选按「本单供应商」再筛一次：后端只保证批次挂了某个供应商，
 * 不知道你要退给谁 —— 混进别家的批次会在保存时被当场拒掉，提前筛掉少一次返工。
 */
const visibleCandidates = computed(() =>
  candidates.value.filter((b: any) => !form.supplierId || b.supplierId === form.supplierId)
)

const loadCandidates = async () => {
  candidateLoading.value = true
  try {
    const res: any = await getStockBatchCandidates({
      stockRoom: candidateRoom.value ?? undefined,
      drugName: candidateKeyword.value?.trim() || undefined,
      onlyWithSupplier: true,
    })
    if (res.code === 200) {
      candidates.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载批次候选失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    candidateLoading.value = false
  }
}

const openCreate = () => {
  form.id = null
  form.supplierId = query.supplierId || null
  lastSupplierId.value = form.supplierId
  form.returnReason = ''
  form.srcRefNo = ''
  form.remark = ''
  formItems.value = []
  candidateKeyword.value = ''
  formVisible.value = true
  loadCandidates()
}

/** 记住上一次确认过的供应商：取消换供应商时要有东西可退回去 */
const lastSupplierId = ref<string | null>(null)

/** 换供应商 = 能退的批次换了一批，已选行留着只会撞「批次不属于本单供应商」 */
const onSupplierChange = async () => {
  if (formItems.value.length) {
    try {
      await ElMessageBox.confirm(
        `换供应商后已选的 ${formItems.value.length} 个批次要重新选（批次必须属于本单供应商才能退）。确认换？`,
        '换供应商确认',
        { type: 'warning' }
      )
    } catch {
      form.supplierId = lastSupplierId.value
      return
    }
  }
  lastSupplierId.value = form.supplierId
  formItems.value = []
}

const pickBatch = (batch: any) => {
  if (formItems.value.some((it) => it.stockId === batch.id)) {
    ElMessage.warning('这个批次已经在明细里了（同一批次一单只能出现一次）')
    return
  }
  formItems.value.push({
    stockId: batch.id,
    supplierId: batch.supplierId,
    drugCode: batch.drugCode,
    drugName: batch.drugName,
    specification: batch.specification,
    unit: batch.unit,
    batchNo: batch.batchNo,
    expiryDate: batch.expiryDate,
    costPrice: Number(batch.costPrice || 0),
    availableQuantity: Number(batch.availableQuantity || 0),
    quantity: Number(batch.availableQuantity || 0),
    remark: '',
  })
}

const dropItem = (stockId: string) => {
  formItems.value = formItems.value.filter((it) => it.stockId !== stockId)
}

const saveForm = async () => {
  if (!form.supplierId) {
    ElMessage.warning('请选择供应商')
    return
  }
  if (!form.returnReason.trim()) {
    ElMessage.warning('请填写退货原因')
    return
  }
  if (!formItems.value.length) {
    ElMessage.warning('请至少从候选里点一个批次')
    return
  }
  const bad = formItems.value.find((it) => !(Number(it.quantity) > 0))
  if (bad) {
    ElMessage.warning(`批次「${bad.drugName} / ${bad.batchNo}」的退货数量必须大于 0`)
    return
  }
  const over = formItems.value.find((it) => Number(it.quantity) > it.availableQuantity)
  if (over) {
    ElMessage.warning(
      `批次「${over.drugName} / ${over.batchNo}」可用量只有 ${over.availableQuantity}，不够退 ${over.quantity}`
    )
    return
  }
  formSaving.value = true
  try {
    const res: any = await upsertSupplierReturn({
      id: form.id || undefined,
      supplierId: form.supplierId,
      returnReason: form.returnReason.trim(),
      srcRefNo: form.srcRefNo?.trim() || undefined,
      remark: form.remark?.trim() || undefined,
      items: formItems.value.map((it) => ({
        stockId: it.stockId,
        quantity: Number(it.quantity),
        remark: it.remark?.trim() || undefined,
      })),
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存')
      formVisible.value = false
      loadList()
      openDetail(res.data)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    formSaving.value = false
  }
}

// ==================== 详情 ====================
const drawerVisible = ref(false)
const detail = ref<any>(null)

const openDetail = async (rowOrId: any) => {
  const id = typeof rowOrId === 'object' ? rowOrId.id : rowOrId
  try {
    const res: any = await getSupplierReturnDetail(id)
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

const openModify = async (doc: any) => {
  // 列表行不带明细（主单分页只返回汇总），改明细前先把详情捞回来
  let full = doc
  if (!full.items) {
    const res: any = await getSupplierReturnDetail(full.id)
    if (res.code !== 200 || !res.data) {
      ElMessage.error(res.message || '加载明细失败，请重试')
      return
    }
    full = res.data
  }
  form.supplierId = full.supplierId
  lastSupplierId.value = full.supplierId
  candidateKeyword.value = ''
  await loadCandidates()
  const availMap = (id: string) => {
    const hit = candidates.value.find((b: any) => b.id === id)
    return hit ? Number(hit.availableQuantity || 0) : 0
  }
  form.id = full.id
  form.returnReason = full.returnReason || ''
  form.srcRefNo = full.srcRefNo || ''
  form.remark = full.remark || ''
  formItems.value = (full.items || []).map((it: any) => ({
    stockId: it.stockId,
    supplierId: it.supplierId,
    drugCode: it.drugCode,
    drugName: it.drugName,
    specification: it.specification,
    unit: it.unit,
    batchNo: it.batchNo,
    expiryDate: it.expiryDate,
    costPrice: Number(it.costPrice || 0),
    // 明细上的 currentQuantity 是「数量」不含锁定量，上限一律以候选的可用量为准
    availableQuantity: availMap(it.stockId) || Number(it.currentQuantity || 0),
    quantity: Number(it.quantity || 0),
    remark: it.remark || '',
  }))
  formVisible.value = true
}

// ==================== 状态机动作 ====================
const doConfirm = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认退货？将按 ${row.totalItems ?? 0} 个批次扣减库存并写 9-退货出库流水（金额 ¥${row.totalAmount ?? 0}），` +
        '货一旦出库就离院，退货单不可再作废或删除。',
      '确认退货',
      { type: 'warning', confirmButtonText: '确认退货' }
    )
  } catch {
    return
  }
  await runAction(() => confirmSupplierReturn({ id: row.id }), '已退货出库')
}

const doCancel = async (row: any) => {
  let reason = ''
  try {
    const r = await ElMessageBox.prompt(
      '作废后这张单不再动库存，可以删除。请填写作废原因。',
      `作废退货单 ${row.returnNo}`,
      {
        confirmButtonText: '确认作废',
        inputPlaceholder: '作废原因（必填）',
        inputValidator: (v: string) => (v && v.trim() ? true : '作废必须填写原因'),
      }
    )
    reason = r.value.trim()
  } catch {
    return
  }
  await runAction(() => cancelSupplierReturn({ id: row.id, reason }), '退货单已作废')
}

const doDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定删除退货单 ${row.returnNo}？只有「待退货 / 已作废」可删，动过库存的单据一律留档。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    const res: any = await deleteSupplierReturnById(String(row.id))
    if (res.code === 200) {
      ElMessage.success('已删除')
      drawerVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    console.error(e)
  }
}

const runAction = async (call: () => Promise<any>, okText: string) => {
  try {
    const res: any = await call()
    if (res.code === 200) {
      ElMessage.success(res.message || okText)
      loadList()
      if (drawerVisible.value) await openDetail(res.data)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadDicts()
  loadSuppliers()
  loadList()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">药品供应商退货</h2>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-400">只退挂了供应商档案的批次；药房的货先调拨回药库再退</span>
          <el-button v-perm="'pharmacy:supplierReturn:add'" type="primary" :icon="Plus" data-testid="sreturn-new-btn" @click="openCreate">
            新建退货单
          </el-button>
        </div>
      </div>

      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="退货单号">
          <el-input
            v-model="query.returnNo"
            placeholder="退货单号"
            clearable
            style="width: 170px"
            data-testid="sreturn-no-filter"
            @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="原因/原单据号关键字"
            clearable
            style="width: 200px"
            @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="query.supplierId" placeholder="供应商" clearable filterable style="width: 210px" :fit-input-width="false">
            <el-option v-for="s in suppliers" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" :fit-input-width="false">
            <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
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
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="sreturn-table">
        <el-table-column prop="returnNo" label="退货单号" width="150" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" show-overflow-tooltip />
        <el-table-column prop="returnReason" label="退货原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="原单据号" width="140">
          <template #default="{ row }">{{ row.srcRefNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="批次" width="70" align="right">
          <template #default="{ row }">{{ row.totalItems }}</template>
        </el-table-column>
        <el-table-column label="数量合计" width="90" align="right">
          <template #default="{ row }">{{ row.totalQuantity }}</template>
        </el-table-column>
        <el-table-column label="退款金额" width="105" align="right">
          <template #default="{ row }">
            <span class="text-rose-600">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="returnStatusTagType(row.status)">{{ row.statusText || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="制单人" width="90" />
        <el-table-column prop="createTime" label="制单时间" width="150" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" data-testid="sreturn-detail-btn" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canConfirmReturn(row)" v-perm="'pharmacy:supplierReturn:edit'" link type="warning" data-testid="sreturn-confirm-btn" @click="doConfirm(row)">退货</el-button>
            <el-button v-if="canEditReturn(row)" v-perm="'pharmacy:supplierReturn:add'" link @click="openModify(row)">改明细</el-button>
            <el-button v-if="canCancelReturn(row)" v-perm="'pharmacy:supplierReturn:edit'" link type="info" @click="doCancel(row)">作废</el-button>
            <el-button v-if="canDeleteReturn(row)" v-perm="'pharmacy:supplierReturn:delete'" link type="danger" @click="doDelete(row)">删除</el-button>
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

    <!-- 建单 / 改明细 -->
    <el-dialog
      v-model="formVisible"
      :title="form.id ? '修改退货明细' : '新建退货单'"
      width="1040px"
      data-testid="sreturn-form-dialog"
    >
      <el-form label-width="92px">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="供应商" required>
            <el-select
              v-model="form.supplierId"
              placeholder="只列出启用中的供应商"
              filterable
              :fit-input-width="false"
              data-testid="sreturn-supplier-select"
              @change="onSupplierChange"
            >
              <el-option v-for="s in suppliers" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" />
            </el-select>
          </el-form-item>
          <el-form-item label="原入库单号">
            <el-input v-model="form.srcRefNo" maxlength="50" clearable placeholder="采购/入库单号，人工填的溯源线索" />
          </el-form-item>
        </div>
        <el-form-item label="退货原因" required>
          <el-input
            v-model="form.returnReason"
            maxlength="200"
            show-word-limit
            placeholder="如：效期不足 6 个月拒收 / 外包装破损 / 召回批次"
            data-testid="sreturn-reason-input"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="承运、发票红冲等安排" />
        </el-form-item>
      </el-form>

      <div class="mb-2 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">可选批次（已挂供应商档案、可用量 &gt; 0）</h3>
        <div class="flex items-center gap-2">
          <el-select v-model="candidateRoom" placeholder="库位" clearable style="width: 110px" :fit-input-width="false" @change="loadCandidates">
            <el-option v-for="d in roomDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
          <el-input
            v-model="candidateKeyword"
            placeholder="药品名/编码/批号"
            clearable
            style="width: 190px"
            data-testid="sreturn-candidate-search"
            @keyup.enter="loadCandidates"
          />
          <el-button :icon="Search" size="small" @click="loadCandidates">搜索</el-button>
        </div>
      </div>
      <el-table :data="visibleCandidates" v-loading="candidateLoading" border size="small" max-height="260" data-testid="sreturn-candidate-table">
        <el-table-column prop="drugCode" label="编码" width="95" />
        <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格" width="100" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批号" width="95" />
        <el-table-column prop="expiryDate" label="效期至" width="100" />
        <el-table-column label="库位" width="70" align="center">
          <template #default="{ row }">{{ row.stockRoomText || roomText(row.stockRoom) }}</template>
        </el-table-column>
        <el-table-column label="库存/锁定/可用" width="120" align="right">
          <template #default="{ row }">
            {{ row.quantity }} / {{ row.lockedQuantity }} /
            <span class="text-emerald-700 font-medium">{{ row.availableQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成本价" width="80" align="right">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" data-testid="sreturn-pick-btn" @click="pickBatch(row)">加入</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="form.supplierId && !visibleCandidates.length && !candidateLoading" class="mt-2 text-sm text-amber-600">
        该供应商名下没有可退批次 —— 批次上的供应商必须是「供货商」而不是生产厂家，未挂档案的批次请先在库存里补 supplier。
      </div>

      <div class="mt-4 mb-2 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">退货明细（共 {{ formItems.length }} 个批次）</h3>
        <span class="text-sm text-slate-500">向供应商主张退款（按成本价）：{{ formatMoney(formAmount) }}</span>
      </div>
      <el-table :data="formItems" border size="small" max-height="260" data-testid="sreturn-item-table">
        <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批号" width="95" />
        <el-table-column prop="expiryDate" label="效期至" width="100" />
        <el-table-column label="成本价" width="80" align="right">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column label="可用量" width="80" align="right">
          <template #default="{ row }">{{ row.availableQuantity }}</template>
        </el-table-column>
        <el-table-column label="退货数量" width="150" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="0" :max="row.availableQuantity" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="行备注" min-width="130">
          <template #default="{ row }">
            <el-input v-model="row.remark" maxlength="200" placeholder="可空" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ row }">
            <el-button link type="danger" size="small" :icon="Delete" @click="dropItem(row.stockId)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-alert type="info" :closable="false" show-icon class="!mt-3">
        <template #title>保存只抓快照、不动库存；点列表上的「退货」才真正扣减批次并留下 9-退货出库流水。</template>
      </el-alert>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSaving" data-testid="sreturn-form-save" @click="saveForm">
          {{ form.id ? '保存明细' : '建单' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="drawerVisible" title="退货单详情" size="1000px" data-testid="sreturn-detail-drawer">
      <template v-if="detail">
        <div class="mb-3 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <el-tag :type="returnStatusTagType(detail.status)">{{ detail.statusText }}</el-tag>
            <span class="text-sm text-slate-500">{{ detail.supplierName }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-button v-if="canConfirmReturn(detail)" v-perm="'pharmacy:supplierReturn:edit'" size="small" type="warning" @click="doConfirm(detail)">确认退货</el-button>
            <el-button v-if="canEditReturn(detail)" v-perm="'pharmacy:supplierReturn:add'" size="small" @click="openModify(detail)">改明细</el-button>
            <el-button v-if="canDeleteReturn(detail)" v-perm="'pharmacy:supplierReturn:delete'" size="small" type="danger" @click="doDelete(detail)">删除</el-button>
          </div>
        </div>

        <el-descriptions :column="3" border size="small" class="mb-4">
          <el-descriptions-item label="退货单号">{{ detail.returnNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detail.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="原单据号">{{ detail.srcRefNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="退货原因" :span="2">{{ detail.returnReason }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="批次数">{{ detail.totalItems }}</el-descriptions-item>
          <el-descriptions-item label="数量合计">{{ detail.totalQuantity }}</el-descriptions-item>
          <el-descriptions-item label="制单人 / 时间">{{ detail.createBy }} {{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="退货人">{{ detail.returnBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="退货时间">{{ detail.returnTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="作废人 / 原因" :span="3">
            {{ detail.cancelBy || '—' }}{{ detail.cancelReason ? `（${detail.cancelReason}）` : '' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detail.items || []" border size="small" data-testid="sreturn-item-detail-table">
          <el-table-column prop="drugCode" label="编码" width="95" />
          <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格" width="100" show-overflow-tooltip />
          <el-table-column prop="batchNo" label="批号" width="95" />
          <el-table-column prop="expiryDate" label="效期至" width="100" />
          <el-table-column label="库位" width="70" align="center">
            <template #default="{ row }">{{ row.stockRoomText || roomText(row.stockRoom) }}</template>
          </el-table-column>
          <el-table-column label="成本价" width="80" align="right">
            <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
          </el-table-column>
          <el-table-column label="退货数量" width="85" align="right">
            <template #default="{ row }">{{ row.quantity }}</template>
          </el-table-column>
          <el-table-column label="金额" width="90" align="right">
            <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="批次剩余" width="90" align="right">
            <template #default="{ row }">{{ row.currentQuantity }}</template>
          </el-table-column>
        </el-table>

        <h3 class="mt-5 mb-2 text-sm font-semibold text-slate-700">本单落下的库存流水（退出去多少，账上就得少多少）</h3>
        <el-table :data="detail.logs || []" border size="small" data-testid="sreturn-log-table">
          <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
          <el-table-column prop="batchNo" label="批号" width="95" />
          <el-table-column label="库位" width="70" align="center">
            <template #default="{ row }">{{ row.stockRoomText }}</template>
          </el-table-column>
          <el-table-column label="变动类型" width="100" align="center">
            <template #default="{ row }">{{ row.changeTypeText }}</template>
          </el-table-column>
          <el-table-column label="变动数量" width="90" align="right">
            <template #default="{ row }">
              <span class="text-rose-600">{{ row.changeQuantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变动前 → 变动后" width="120" align="right">
            <template #default="{ row }">{{ row.quantityBefore }} → {{ row.quantityAfter }}</template>
          </el-table-column>
          <el-table-column prop="sourceNo" label="来源单号" width="150" />
          <el-table-column prop="operatorName" label="操作人" width="90" />
          <el-table-column prop="createTime" label="时间" width="150" />
        </el-table>
        <div v-if="!detail.logs?.length" class="mt-2 text-sm text-slate-400">
          还没有流水 —— 这张单仍在「待退货」，建单本身不动库存。
        </div>
      </template>
    </el-drawer>
  </div>
</template>
