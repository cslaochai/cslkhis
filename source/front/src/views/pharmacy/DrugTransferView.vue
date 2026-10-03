<script setup lang="ts">
/**
 * 药品调拨（sql/154 ②级：药库 ↔ 药房）
 *
 * 状态机：1待发出 →（确认发出）2待接收 →（确认接收）3已完成；只有「待发出」可改明细/作废/删除。
 * **发出与接收各动一次库存**：确认发出扣发出库位并落 7-调拨出库（负），确认接收加接收库位并落
 * 8-调拨入库（正），两行流水合计 0 就是「搬出去又搬进来」的账实闭环证据（详情抽屉直接算给你看）。
 * 在途（待接收）不许作废 —— 只有一行 7 没有 8，作废等于把库存挂在空中，只能再开一张反向调拨搬回去。
 *
 * 明细的药品名/规格/批号/成本价是建单那一刻从批次上抓的**快照**，事后字典改名不改历史记载；
 * 数量前端只传「哪个批次 + 调多少」，成本价一律服务端算（让前端传金额等于让它编）。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Plus, Delete } from '@element-plus/icons-vue'
import {
  listDrugTransferPage,
  getDrugTransferDetail,
  upsertDrugTransfer,
  confirmDrugTransferOut,
  confirmDrugTransferIn,
  cancelDrugTransfer,
  deleteDrugTransferById,
  getStockBatchCandidates,
} from '@/api/pharmacy'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText, formatMoney } from '@/lib/utils'
import {
  STOCK_ROOM,
  fromRoomOf,
  canTransferOut,
  canTransferIn,
  canCancelTransfer,
  canDeleteTransfer,
  canEditTransfer,
  transferStatusTagType,
  isTransferBalanced,
} from '@/lib/drugTransfer'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  transferNo: '',
  transferType: null as number | null,
  status: null as number | null,
  keyword: '',
  dateRange: null as [string, string] | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const typeDict = ref<any[]>([])
const statusDict = ref<any[]>([])
const typeText = (v: any) => dictLabelText(typeDict.value, v)
const statusText = (v: any) => dictLabelText(statusDict.value, v)

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.DRUG_TRANSFER_TYPE},${DICT_TYPE.DRUG_TRANSFER_STATUS}`)
    typeDict.value = res?.data?.[DICT_TYPE.DRUG_TRANSFER_TYPE] || []
    statusDict.value = res?.data?.[DICT_TYPE.DRUG_TRANSFER_STATUS] || []
  } catch (e) {
    console.error('加载调拨字典失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await listDrugTransferPage({
      transferNo: query.transferNo?.trim() || undefined,
      transferType: query.transferType ?? undefined,
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
      ElMessage.error(res.message || '加载调拨单失败')
    }
  } catch (e) {
    ElMessage.error('加载调拨单失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.transferNo = ''
  query.transferType = null
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
  transferType: 1 as number,
  reason: '',
  remark: '',
})
/** 已选明细：一条 = 一个发出方批次（stockId 保持字符串，雪花 ID 转 Number 会丢精度） */
const formItems = ref<any[]>([])
const candidateLoading = ref(false)
const candidates = ref<any[]>([])
const candidateKeyword = ref('')
/** 记住上一次确认过的方向：取消换方向时要有东西可退回去 */
const lastType = ref(1)
const fromRoom = computed(() => fromRoomOf(form.transferType))
const fromRoomLabel = computed(() =>
  fromRoom.value === STOCK_ROOM.PHARMACY ? '药房' : '药库'
)
const toRoomLabel = computed(() =>
  fromRoom.value === STOCK_ROOM.PHARMACY ? '药库' : '药房'
)
const formAmount = computed(() =>
  formItems.value.reduce((s, it) => s + Number(it.costPrice || 0) * Number(it.applyQuantity || 0), 0)
)

const loadCandidates = async () => {
  candidateLoading.value = true
  try {
    const res: any = await getStockBatchCandidates({
      stockRoom: fromRoom.value,
      drugName: candidateKeyword.value?.trim() || undefined,
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

/** 批次当前可用量以候选为准：明细上的 fromQuantity 是「数量」不含锁定量，拿它当上限会把患者的药调走 */
const availableOf = (stockId: string, fallback: any) => {
  const hit = candidates.value.find((b: any) => b.id === stockId)
  return hit ? Number(hit.availableQuantity || 0) : Number(fallback || 0)
}

const openCreate = () => {
  form.id = null
  form.transferType = 1
  lastType.value = 1
  form.reason = ''
  form.remark = ''
  formItems.value = []
  candidateKeyword.value = ''
  formVisible.value = true
  loadCandidates()
}

/** 换方向 = 发出库位变了，已选批次全部失效；留着只会让后端按库位校验当场拒掉 */
const onTypeChange = async () => {
  if (formItems.value.length) {
    try {
      await ElMessageBox.confirm(
        `换方向后发出库位变成「${fromRoomLabel.value}」，已选的 ${formItems.value.length} 个批次要重新选。确认换？`,
        '换方向确认',
        { type: 'warning' }
      )
    } catch {
      // 取消则把选择退回原方向：不能留下「方向已换、明细还是老库位」的半态
      form.transferType = lastType.value
      return
    }
  }
  lastType.value = form.transferType
  formItems.value = []
  loadCandidates()
}

const pickBatch = (batch: any) => {
  if (formItems.value.some((it) => it.stockId === batch.id)) {
    ElMessage.warning('这个批次已经在明细里了（同一批次一单只能出现一次）')
    return
  }
  formItems.value.push({
    stockId: batch.id,
    drugCode: batch.drugCode,
    drugName: batch.drugName,
    specification: batch.specification,
    unit: batch.unit,
    batchNo: batch.batchNo,
    expiryDate: batch.expiryDate,
    costPrice: Number(batch.costPrice || 0),
    availableQuantity: Number(batch.availableQuantity || 0),
    applyQuantity: Number(batch.availableQuantity || 0),
    remark: '',
  })
}

const dropItem = (stockId: string) => {
  formItems.value = formItems.value.filter((it) => it.stockId !== stockId)
}

const saveForm = async () => {
  if (!form.reason.trim()) {
    ElMessage.warning('请填写调拨事由')
    return
  }
  if (!formItems.value.length) {
    ElMessage.warning('请至少从候选里点一个批次')
    return
  }
  const bad = formItems.value.find((it) => !(Number(it.applyQuantity) > 0))
  if (bad) {
    ElMessage.warning(`批次「${bad.drugName} / ${bad.batchNo}」的调拨数量必须大于 0`)
    return
  }
  const over = formItems.value.find((it) => Number(it.applyQuantity) > it.availableQuantity)
  if (over) {
    ElMessage.warning(
      `批次「${over.drugName} / ${over.batchNo}」可用量只有 ${over.availableQuantity}，不够调 ${over.applyQuantity}`
    )
    return
  }
  formSaving.value = true
  try {
    const res: any = await upsertDrugTransfer({
      id: form.id || undefined,
      transferType: form.transferType,
      reason: form.reason.trim(),
      remark: form.remark?.trim() || undefined,
      items: formItems.value.map((it) => ({
        stockId: it.stockId,
        applyQuantity: Number(it.applyQuantity),
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
const balanced = computed(() => isTransferBalanced(detail.value?.logs))

const openDetail = async (rowOrId: any) => {
  const id = typeof rowOrId === 'object' ? rowOrId.id : rowOrId
  try {
    const res: any = await getDrugTransferDetail(id)
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
    const res: any = await getDrugTransferDetail(full.id)
    if (res.code !== 200 || !res.data) {
      ElMessage.error(res.message || '加载明细失败，请重试')
      return
    }
    full = res.data
  }
  candidateKeyword.value = ''
  await loadCandidates()
  form.id = full.id
  form.transferType = Number(full.transferType)
  lastType.value = form.transferType
  form.reason = full.reason || ''
  form.remark = full.remark || ''
  formItems.value = (full.items || []).map((it: any) => ({
    stockId: it.stockId,
    drugCode: it.drugCode,
    drugName: it.drugName,
    specification: it.specification,
    unit: it.unit,
    batchNo: it.batchNo,
    expiryDate: it.expiryDate,
    costPrice: Number(it.costPrice || 0),
    availableQuantity: availableOf(it.stockId, it.fromQuantity),
    applyQuantity: Number(it.applyQuantity || 0),
    remark: it.remark || '',
  }))
  candidateKeyword.value = ''
  formVisible.value = true
}

// ==================== 状态机动作 ====================
const doOut = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认发出？将按 ${row.totalItems ?? 0} 个批次从「${row.fromRoomText}」扣减库存并写 7-调拨出库流水，` +
        `发出后单据进入「待接收」，不能再改明细。`,
      '确认发出',
      { type: 'warning', confirmButtonText: '确认发出' }
    )
  } catch {
    return
  }
  await runAction(() => confirmDrugTransferOut({ id: row.id }), '已发出，等待对方库位接收')
}

const doIn = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认接收？将按明细把 ${row.outQuantity ?? 0} 个单位落到「${row.toRoomText}」并写 8-调拨入库流水；` +
        '同批号在该库位已有批次就叠加，没有则按快照新建批次。接收后本单完成、不可再改。',
      '确认接收',
      { type: 'warning', confirmButtonText: '确认接收' }
    )
  } catch {
    return
  }
  await runAction(() => confirmDrugTransferIn({ id: row.id }), '已接收，本单调拨完成')
}

const doCancel = async (row: any) => {
  let reason = ''
  try {
    const r = await ElMessageBox.prompt(
      '作废后这张单不再动库存，可以删除。请填写作废原因。',
      `作废调拨单 ${row.transferNo}`,
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
  await runAction(() => cancelDrugTransfer({ id: row.id, reason }), '调拨单已作废')
}

const doDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定删除调拨单 ${row.transferNo}？只有「待发出 / 已作废」可删，动过库存的单据一律留档。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    const res: any = await deleteDrugTransferById(String(row.id))
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

const signedQuantity = (v: any) => {
  if (v === null || v === undefined) return '—'
  const n = Number(v)
  return (n > 0 ? '+' : '') + n
}

onMounted(() => {
  loadDicts()
  loadList()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">药品调拨（药库 ↔ 药房）</h2>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-400">发出扣一个库位、接收加另一个库位，两行流水合计 0 才算搬完</span>
          <el-button v-perm="'pharmacy:drugTransfer:add'" type="primary" :icon="Plus" data-testid="transfer-new-btn" @click="openCreate">
            新建调拨单
          </el-button>
        </div>
      </div>

      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="调拨单号">
          <el-input
            v-model="query.transferNo"
            placeholder="调拨单号"
            clearable
            style="width: 170px"
            data-testid="transfer-no-filter"
            @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="事由/单号关键字"
            clearable
            style="width: 190px"
            @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="query.transferType" placeholder="方向" clearable style="width: 150px" :fit-input-width="false">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
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
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="transfer-table">
        <el-table-column prop="transferNo" label="调拨单号" width="150" />
        <el-table-column label="方向" width="140" align="center">
          <template #default="{ row }">
            <span class="text-slate-700">{{ row.transferTypeText }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库位" width="120" align="center">
          <template #default="{ row }">{{ row.fromRoomText }} → {{ row.toRoomText }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="事由" min-width="150" show-overflow-tooltip />
        <el-table-column label="批次" width="70" align="right">
          <template #default="{ row }">{{ row.totalItems }}</template>
        </el-table-column>
        <el-table-column label="申请/发出/接收" width="130" align="right">
          <template #default="{ row }">
            <span>{{ row.totalQuantity }}</span>
            <span class="text-slate-400"> / {{ row.outQuantity }} / {{ row.inQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="transferStatusTagType(row.status)">{{ row.statusText || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="制单人" width="90" />
        <el-table-column prop="createTime" label="制单时间" width="150" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" data-testid="transfer-detail-btn" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canTransferOut(row)" v-perm="'pharmacy:drugTransfer:edit'" link type="warning" data-testid="transfer-out-btn" @click="doOut(row)">发出</el-button>
            <el-button v-if="canTransferIn(row)" v-perm="'pharmacy:drugTransfer:edit'" link type="success" data-testid="transfer-in-btn" @click="doIn(row)">接收</el-button>
            <el-button v-if="canEditTransfer(row)" v-perm="'pharmacy:drugTransfer:add'" link @click="openModify(row)">改明细</el-button>
            <el-button v-if="canCancelTransfer(row)" v-perm="'pharmacy:drugTransfer:edit'" link type="info" @click="doCancel(row)">作废</el-button>
            <el-button v-if="canDeleteTransfer(row)" v-perm="'pharmacy:drugTransfer:delete'" link type="danger" @click="doDelete(row)">删除</el-button>
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
      :title="form.id ? '修改调拨明细' : '新建调拨单'"
      width="1040px"
      data-testid="transfer-form-dialog"
    >
      <el-form label-width="80px">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="调拨方向" required>
            <el-select v-model="form.transferType" :fit-input-width="false" data-testid="transfer-type-select" @change="onTypeChange">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="库位走向">
            <span class="text-sm text-slate-600">{{ fromRoomLabel }} → {{ toRoomLabel }}（按方向定死，不能自选）</span>
          </el-form-item>
        </div>
        <el-form-item label="调拨事由" required>
          <el-input
            v-model="form.reason"
            maxlength="200"
            show-word-limit
            placeholder="如：药房本周拆零用量增大，从药库下拨 20 批"
            data-testid="transfer-reason-input"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="参与人、运输条件等" />
        </el-form-item>
      </el-form>

      <div class="mb-2 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">
          可选批次（{{ fromRoomLabel }}可用量 &gt; 0，按效期先到排）
        </h3>
        <div class="flex items-center gap-2">
          <el-input
            v-model="candidateKeyword"
            placeholder="药品名/编码/批号"
            clearable
            style="width: 200px"
            data-testid="transfer-candidate-search"
            @keyup.enter="loadCandidates"
          />
          <el-button :icon="Search" size="small" @click="loadCandidates">搜索</el-button>
        </div>
      </div>
      <el-table :data="candidates" v-loading="candidateLoading" border size="small" max-height="260" data-testid="transfer-candidate-table">
        <el-table-column prop="drugCode" label="编码" width="95" />
        <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格" width="100" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批号" width="95" />
        <el-table-column prop="expiryDate" label="效期至" width="100" />
        <el-table-column label="库存/锁定/可用" width="125" align="right">
          <template #default="{ row }">
            {{ row.quantity }} / {{ row.lockedQuantity }} /
            <span class="text-emerald-700 font-medium">{{ row.availableQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成本价" width="80" align="right">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" data-testid="transfer-pick-btn" @click="pickBatch(row)">加入</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 mb-2 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-700">调拨明细（共 {{ formItems.length }} 个批次）</h3>
        <span class="text-sm text-slate-500">金额按批次成本价现算：{{ formatMoney(formAmount) }}</span>
      </div>
      <el-table :data="formItems" border size="small" max-height="260" data-testid="transfer-item-table">
        <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批号" width="95" />
        <el-table-column prop="expiryDate" label="效期至" width="100" />
        <el-table-column label="成本价" width="80" align="right">
          <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
        </el-table-column>
        <el-table-column label="可用量" width="80" align="right">
          <template #default="{ row }">{{ row.availableQuantity }}</template>
        </el-table-column>
        <el-table-column label="调拨数量" width="150" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.applyQuantity" :min="0" :max="row.availableQuantity" size="small" />
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
        <template #title>保存只抓快照、不动库存；点列表上的「发出」才开始扣减，「接收」才算搬完。</template>
      </el-alert>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSaving" data-testid="transfer-form-save" @click="saveForm">
          {{ form.id ? '保存明细' : '建单' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="drawerVisible" title="调拨单详情" size="1040px" data-testid="transfer-detail-drawer">
      <template v-if="detail">
        <div class="mb-3 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <el-tag :type="transferStatusTagType(detail.status)">{{ detail.statusText }}</el-tag>
            <span class="text-sm text-slate-500">{{ detail.transferTypeText }}：{{ detail.fromRoomText }} → {{ detail.toRoomText }}</span>
          </div>
          <div class="flex items-center gap-2">
            <el-button v-if="canTransferOut(detail)" v-perm="'pharmacy:drugTransfer:edit'" size="small" type="warning" @click="doOut(detail)">确认发出</el-button>
            <el-button v-if="canTransferIn(detail)" v-perm="'pharmacy:drugTransfer:edit'" size="small" type="success" @click="doIn(detail)">确认接收</el-button>
            <el-button v-if="canEditTransfer(detail)" v-perm="'pharmacy:drugTransfer:add'" size="small" @click="openModify(detail)">改明细</el-button>
            <el-button v-if="canDeleteTransfer(detail)" v-perm="'pharmacy:drugTransfer:delete'" size="small" type="danger" @click="doDelete(detail)">删除</el-button>
          </div>
        </div>

        <el-descriptions :column="3" border size="small" class="mb-4">
          <el-descriptions-item label="调拨单号">{{ detail.transferNo }}</el-descriptions-item>
          <el-descriptions-item label="事由" :span="2">{{ detail.reason }}</el-descriptions-item>
          <el-descriptions-item label="批次数">{{ detail.totalItems }}</el-descriptions-item>
          <el-descriptions-item label="申请合计">{{ detail.totalQuantity }}</el-descriptions-item>
          <el-descriptions-item label="已发出 / 已接收">
            {{ detail.outQuantity }} / {{ detail.inQuantity }}
          </el-descriptions-item>
          <el-descriptions-item label="金额合计">{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="制单人">{{ detail.createBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="制单时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="发出人">{{ detail.outBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="发出时间">{{ detail.outTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="接收人">{{ detail.inBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="接收时间">{{ detail.inTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="作废人 / 原因" :span="3">
            {{ detail.cancelBy || '—' }}{{ detail.cancelReason ? `（${detail.cancelReason}）` : '' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detail.items || []" border size="small" data-testid="transfer-item-detail-table">
          <el-table-column prop="drugCode" label="编码" width="95" />
          <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格" width="100" show-overflow-tooltip />
          <el-table-column prop="batchNo" label="批号" width="95" />
          <el-table-column prop="expiryDate" label="效期至" width="100" />
          <el-table-column label="成本价" width="80" align="right">
            <template #default="{ row }">{{ formatMoney(row.costPrice) }}</template>
          </el-table-column>
          <el-table-column label="调拨数量" width="85" align="right">
            <template #default="{ row }">{{ row.applyQuantity }}</template>
          </el-table-column>
          <el-table-column label="金额" width="90" align="right">
            <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="发出方余额" width="95" align="right">
            <template #default="{ row }">{{ row.fromQuantity }}</template>
          </el-table-column>
          <el-table-column label="接收方余额" width="95" align="right">
            <template #default="{ row }">{{ row.toQuantity === null ? '—' : row.toQuantity }}</template>
          </el-table-column>
          <el-table-column label="进度" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="Number(row.inFlag) === 1 ? 'success' : Number(row.outFlag) === 1 ? 'warning' : 'info'" size="small">
                {{ Number(row.inFlag) === 1 ? '已接收' : Number(row.outFlag) === 1 ? '待接收' : '未发出' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <h3 class="mt-5 mb-2 flex items-center gap-2 text-sm font-semibold text-slate-700">
          本单落下的库存流水
          <el-tag v-if="detail.logs?.length" :type="balanced ? 'success' : 'warning'" size="small">
            {{ balanced ? '出=入 已闭环' : '只在途（尚未接收）' }}
          </el-tag>
        </h3>
        <el-table :data="detail.logs || []" border size="small" data-testid="transfer-log-table">
          <el-table-column prop="drugName" label="药品" min-width="120" show-overflow-tooltip />
          <el-table-column prop="batchNo" label="批号" width="95" />
          <el-table-column label="库位" width="80" align="center">
            <template #default="{ row }">{{ row.stockRoomText }}</template>
          </el-table-column>
          <el-table-column label="变动类型" width="100" align="center">
            <template #default="{ row }">{{ row.changeTypeText }}</template>
          </el-table-column>
          <el-table-column label="变动数量" width="90" align="right">
            <template #default="{ row }">
              <span :class="Number(row.changeQuantity) < 0 ? 'text-rose-600' : 'text-emerald-700'">
                {{ signedQuantity(row.changeQuantity) }}
              </span>
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
          还没有流水 —— 这张单仍在「待发出」，保存明细本身不动库存。
        </div>
      </template>
    </el-drawer>
  </div>
</template>
