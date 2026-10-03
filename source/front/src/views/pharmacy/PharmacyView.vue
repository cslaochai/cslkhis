<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search, Warning, Box, Tickets, Money, Plus, Refresh, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStockList, getStockDetail, createStock, inboundStock, outboundStock, getStockWarningList } from '@/api/pharmacy'
import { getSupplierSelectList } from '@/api/purchase'
import { getDrugSelectList, getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const medicines = ref<any[]>([])
const selectedMed = ref<any>(null)
const searchTerm = ref('')
const statusFilter = ref<number | string>('all')
/** 库位筛选（1-药库 2-药房，sql/154）；'all' = 两层都看，不传给后端 */
const roomFilter = ref<number | string>('all')
const activeTab = ref('stock')

const roomDict = ref<any[]>([])
const suppliers = ref<any[]>([])
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(DICT_TYPE.STOCK_ROOM)
    roomDict.value = res?.data?.[DICT_TYPE.STOCK_ROOM] || []
  } catch (e) {
    console.error('加载库位字典失败', e)
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

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

// 新增库存（从药品字典选药 + 批次入库）弹窗
const showAddDialog = ref(false)
const drugOptions = ref<any[]>([])
const selectedDrug = ref<any>(null)
const addForm = ref({
  drugId: null as number | null,
  batchNo: '',
  productionDate: '',
  expiryDate: '',
  quantity: 0,
  costPrice: 0,
  location: '',
  /** 库位：手工建批默认落在药房（与后端默认一致）；要进药库就选 1，之后走调拨下拨 */
  stockRoom: 2 as number,
  supplierId: null as string | null,
  supplier: '',
})

/** 选供货商：同时把名字快照进 supplier 文本列（历史页面读的是这一列，不能只存 ID） */
const onSupplierPick = (supplierId: string) => {
  const hit = suppliers.value.find((s) => s.supplierId === supplierId)
  addForm.value.supplier = hit ? hit.supplierName : ''
}

// 加载药品字典下拉（库存的新增以 sys_drug 为唯一药品来源）
const loadDrugOptions = async () => {
  try {
    const res = await getDrugSelectList()
    drugOptions.value = res.data || []
  } catch (error) {
    console.error('加载药品字典失败:', error)
  }
}

const handleDrugChange = (drugId: number) => {
  selectedDrug.value = drugOptions.value.find((d) => d.id === drugId) || null
}

// 入库/出库弹窗
const showInOutDialog = ref(false)
const inOutType = ref<'in' | 'out'>('in')
const inOutForm = ref({
  stockId: null as number | null,
  drugName: '',
  currentQty: 0,
  quantity: 1,
})

const statusColors: Record<number, string> = {
  1: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  2: 'bg-amber-100 text-amber-700 border-amber-200',
  3: 'bg-red-100 text-red-600 border-red-200',
  4: 'bg-slate-100 text-slate-500 border-slate-200',
}

const statusMap: Record<number, string> = {
  1: '正常',
  2: '预警',
  3: '缺货',
  4: '过期',
}

const totalStock = computed(() => medicines.value.reduce((sum, m) => sum + (m.quantity || 0), 0))
const totalValue = computed(() => medicines.value.reduce((sum, m) => sum + (m.totalAmount || 0), 0))
const lowStockCount = computed(() => medicines.value.filter((m) => m.stockStatus === 2).length)
const outOfStockCount = computed(() => medicines.value.filter((m) => m.stockStatus === 3).length)

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    }
    if (searchTerm.value) params.drugName = searchTerm.value
    if (statusFilter.value !== 'all') params.stockStatus = statusFilter.value
    if (roomFilter.value !== 'all') params.stockRoom = roomFilter.value
    const res = await getStockList(params)
    medicines.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载药品库存失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchTerm.value = ''
  statusFilter.value = 'all'
  roomFilter.value = 'all'
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

// 新增库存
const handleAdd = () => {
  addForm.value = {
    drugId: null, batchNo: '', productionDate: '', expiryDate: '',
    quantity: 0, costPrice: 0, location: '', stockRoom: 2, supplierId: null, supplier: '',
  }
  selectedDrug.value = null
  if (!drugOptions.value.length) loadDrugOptions()
  if (!suppliers.value.length) loadSuppliers()
  showAddDialog.value = true
}

const handleAddSubmit = async () => {
  if (!addForm.value.drugId) {
    ElMessage.warning('请从药品字典选择药品')
    return
  }
  if (!addForm.value.batchNo || !addForm.value.expiryDate) {
    ElMessage.warning('请填写批号和有效期')
    return
  }
  if (addForm.value.quantity <= 0) {
    ElMessage.warning('库存数量必须大于0')
    return
  }
  try {
    await createStock(addForm.value)
    ElMessage.success('新增成功')
    showAddDialog.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '新增失败')
  }
}

// 入库/出库
const handleInbound = (row: any) => {
  inOutType.value = 'in'
  inOutForm.value = { stockId: row.id, drugName: row.drugName, currentQty: row.quantity, quantity: 1 }
  showInOutDialog.value = true
}

const handleOutbound = (row: any) => {
  inOutType.value = 'out'
  inOutForm.value = { stockId: row.id, drugName: row.drugName, currentQty: row.quantity, quantity: 1 }
  showInOutDialog.value = true
}

const handleInOutSubmit = async () => {
  if (!inOutForm.value.stockId || inOutForm.value.quantity <= 0) {
    ElMessage.warning('请输入有效数量')
    return
  }
  try {
    if (inOutType.value === 'in') {
      await inboundStock(inOutForm.value.stockId, inOutForm.value.quantity)
      ElMessage.success('入库成功')
    } else {
      if (inOutForm.value.quantity > inOutForm.value.currentQty) {
        ElMessage.warning('出库数量不能大于当前库存')
        return
      }
      await outboundStock(inOutForm.value.stockId, inOutForm.value.quantity)
      ElMessage.success('出库成功')
    }
    showInOutDialog.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  loadDicts()
  loadData()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-3">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">药品库存</h1>
        <p class="mt-1 text-sm text-slate-500">管理药品库存、出入库和效期监控</p>
      </div>
      <el-button v-perm="'pharmacy:stock:add'" type="primary" :icon="Plus" @click="handleAdd">新增药品</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="mb-3 grid grid-cols-1 gap-4 sm:grid-cols-4">
      <div v-for="item in [
        { label: '库存总量', value: totalStock, icon: Box, color: 'text-blue-600', bg: 'bg-blue-50' },
        { label: '库存总值', value: `¥${totalValue.toLocaleString()}`, icon: Money, color: 'text-emerald-600', bg: 'bg-emerald-50' },
        { label: '库存预警', value: lowStockCount, icon: Warning, color: 'text-amber-600', bg: 'bg-amber-50' },
        { label: '缺货', value: outOfStockCount, icon: Tickets, color: 'text-red-600', bg: 'bg-red-50' },
      ]" :key="item.label" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div :class="['rounded-lg p-2.5', item.bg]">
            <component :is="item.icon" :class="['h-5 w-5', item.color]" />
          </div>
          <div>
            <p :class="['text-lg font-bold', item.color]">{{ item.value }}</p>
            <p class="text-xs text-slate-500">{{ item.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="searchTerm" placeholder="搜索药品名称、编码..." :prefix-icon="Search" style="width: 220px" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="statusFilter" class="!w-36" placeholder="库存状态" @change="handleSearch">
            <el-option label="全部状态" value="all" />
            <el-option label="正常" :value="1" />
            <el-option label="预警" :value="2" />
            <el-option label="缺货" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存地点">
          <el-select v-model="roomFilter" class="!w-32" placeholder="库存地点" data-testid="stock-room-filter" @change="handleSearch">
            <el-option label="两层都看" value="all" />
            <el-option v-for="d in roomDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 药品表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="medicines" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column label="药品名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">{{ row.drugName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="drugCode" label="编码" width="100" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column label="地点" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stockRoom === 1 ? 'primary' : 'success'" size="small" effect="plain">
              {{ row.stockRoomText || (Number(row.stockRoom) === 1 ? '药库' : '药房') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进价" width="90" align="right">
          <template #default="{ row }">¥{{ row.costPrice }}</template>
        </el-table-column>
        <el-table-column label="售价" width="90" align="right">
          <template #default="{ row }">¥{{ row.retailPrice }}</template>
        </el-table-column>
        <el-table-column label="库存" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.stockStatus === 2 ? 'text-amber-600 font-bold' : row.stockStatus === 3 ? 'text-red-600 font-bold' : 'text-slate-900'">
              {{ row.quantity }}
            </span>
            <span class="text-xs text-slate-400 ml-1">{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="效期" width="110" />
        <el-table-column label="锁定/可用" width="105" align="right">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.lockedQuantity }}</span>
            <span class="text-slate-400"> / </span>
            <span class="text-slate-900 font-medium">{{ row.availableQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供货商" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="row.supplierId ? '' : 'text-amber-600'">
              {{ row.supplier || '—' }}
              <span v-if="!row.supplierId" class="text-xs">（未挂档案，不能退供应商）</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium border', statusColors[row.stockStatus] || '']">
              {{ statusMap[row.stockStatus] || '未知' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'pharmacy:stock:edit'" type="success" link :icon="ArrowDown" @click="handleInbound(row)">入库</el-button>
            <el-button v-perm="'pharmacy:stock:edit'" type="warning" link :icon="ArrowUp" @click="handleOutbound(row)">出库</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增库存弹窗（从药品字典选药 + 批次入库） -->
    <el-dialog v-model="showAddDialog" title="新增库存（选择药品批次入库）" width="640px" destroy-on-close>
      <el-form label-position="top" :model="addForm">
        <el-form-item label="药品（来自药品字典）" required>
          <el-select
            v-model="addForm.drugId"
            placeholder="搜索或选择药品"
            class="w-full"
            filterable
            @change="handleDrugChange"
          >
            <el-option
              v-for="d in drugOptions"
              :key="d.id"
              :label="`${d.drugName}（${d.specification || '—'}）`"
              :value="d.id"
            >
              <span>{{ d.drugName }}</span>
              <span class="text-xs text-slate-400 ml-2">{{ d.drugCode }} / {{ d.specification || '—' }} / ¥{{ d.retailPrice }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <div v-if="selectedDrug" class="grid grid-cols-3 gap-4 mb-4">
          <div class="rounded bg-slate-50 p-2.5">
            <p class="text-xs text-slate-500">规格</p>
            <p class="text-sm font-medium text-slate-900">{{ selectedDrug.specification || '—' }}</p>
          </div>
          <div class="rounded bg-slate-50 p-2.5">
            <p class="text-xs text-slate-500">单位</p>
            <p class="text-sm font-medium text-slate-900">{{ selectedDrug.unit || '—' }}</p>
          </div>
          <div class="rounded bg-slate-50 p-2.5">
            <p class="text-xs text-slate-500">字典零售价</p>
            <p class="text-sm font-medium text-slate-900">¥{{ selectedDrug.retailPrice ?? '—' }}</p>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="批号" required>
            <el-input v-model="addForm.batchNo" placeholder="生产批号" />
          </el-form-item>
          <el-form-item label="有效期" required>
            <el-date-picker v-model="addForm.expiryDate" type="date" placeholder="有效期至" value-format="YYYY-MM-DD" class="w-full" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="生产日期">
            <el-date-picker v-model="addForm.productionDate" type="date" placeholder="生产日期" value-format="YYYY-MM-DD" class="w-full" />
          </el-form-item>
          <el-form-item label="入库数量" required>
            <el-input-number v-model="addForm.quantity" :min="1" class="w-full" />
          </el-form-item>
          <el-form-item label="成本价(元)" required>
            <el-input-number v-model="addForm.costPrice" :min="0" :precision="2" class="w-full" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="存放位置">
            <el-input v-model="addForm.location" placeholder="货位，如 A-01-01" />
          </el-form-item>
          <el-form-item label="库存地点" required>
            <el-select v-model="addForm.stockRoom" :fit-input-width="false" data-testid="stock-room-select">
              <el-option v-for="d in roomDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="供货商">
            <el-select
              v-model="addForm.supplierId"
              placeholder="挂上档案才能被供应商退货选中"
              clearable
              filterable
              :fit-input-width="false"
              data-testid="stock-supplier-select"
              @change="onSupplierPick"
            >
              <el-option v-for="s in suppliers" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" />
            </el-select>
          </el-form-item>
        </div>
        <el-alert type="info" :closable="false" show-icon class="!mt-1">
          <template #title>
            「药房」的批次才能被发药/锁库；落在「药库」的批次要先在「药品调拨」下拨到药房。
            供货商留空 = 这一批退不了供应商（历史数据里那列文本多是生产厂家名，不能当退货对象）。
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button v-perm="'pharmacy:stock:add'" type="primary" @click="handleAddSubmit">确认新增</el-button>
      </template>
    </el-dialog>

    <!-- 入库/出库弹窗 -->
    <el-dialog v-model="showInOutDialog" :title="inOutType === 'in' ? '入库' : '出库'" width="400px" destroy-on-close>
      <div class="space-y-4 py-2">
        <div class="rounded-lg bg-slate-50 p-3">
          <p class="text-sm text-slate-500">药品</p>
          <p class="font-medium text-slate-900">{{ inOutForm.drugName }}</p>
        </div>
        <div class="rounded-lg bg-slate-50 p-3">
          <p class="text-sm text-slate-500">当前库存</p>
          <p class="font-medium text-slate-900">{{ inOutForm.currentQty }}</p>
        </div>
        <el-form-item :label="inOutType === 'in' ? '入库数量' : '出库数量'" class="!mb-0">
          <el-input-number v-model="inOutForm.quantity" :min="1" :max="inOutType === 'out' ? inOutForm.currentQty : 9999" class="w-full" />
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="showInOutDialog = false">取消</el-button>
        <el-button v-perm="'pharmacy:stock:edit'" :type="inOutType === 'in' ? 'success' : 'warning'" @click="handleInOutSubmit">
          {{ inOutType === 'in' ? '确认入库' : '确认出库' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
