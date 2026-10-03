<script setup lang="ts">
/**
 * 供应商管理（G9）
 *
 * 供应商是采购链的**主数据**：采购订单引用它，删不掉被引用的供应商（后端拦截）。
 * 评级文案走字典 his_supplier_rating（1差/2一般/3良好/4优秀）；
 * 启停用走 his_enable_status —— 两个都命中不了渲染「未知(n)」，不回落成看似合法的值。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getSupplierList, upsertSupplier, deleteSupplier } from '@/api/purchase'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface SupplierRow {
  supplierId: string
  supplierCode?: string
  supplierName?: string
  contactPerson?: string
  contactPhone?: string
  address?: string
  businessLicense?: string
  licenseExpiry?: string
  rating?: number
  status?: number
  orderCount?: number
  totalAmount?: number
  remark?: string
  createTime?: string
}

const loading = ref(false)
const rows = ref<SupplierRow[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  status: null as number | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

// 字典：供应商评级 / 启停用
const ratingDict = ref<any[]>([])
const enableDict = ref<any[]>([])
const ratingOptions = computed(() =>
  ratingDict.value.map((d: any) => ({ label: d.dictLabel, value: Number(d.dictValue) }))
)
const ratingText = (v: any) => dictLabelText(ratingDict.value, v)
const enableText = (v: any) => dictLabelText(enableDict.value, v)

const loadDicts = async () => {
  try {
    // ⚠ 必须取 res.data[...]：getDictDataMapList 外层还包了一层 {data:{dictType:[...]}}
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.SUPPLIER_RATING},${DICT_TYPE.ENABLE_STATUS}`
    )
    ratingDict.value = res?.data?.[DICT_TYPE.SUPPLIER_RATING] || []
    enableDict.value = res?.data?.[DICT_TYPE.ENABLE_STATUS] || []
  } catch (e) {
    console.error('加载供应商字典失败', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getSupplierList({
      keyword: query.keyword?.trim() || undefined,
      rating: query.rating ?? undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || []
      total.value = Number(res.data.total || 0)
    } else {
      ElMessage.error(res.message || '加载供应商失败')
    }
  } catch (e) {
    ElMessage.error('加载供应商失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.status = null
  query.pageNum = 1
  loadList()
}

// ==================== 编辑弹窗 ====================
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  supplierId: null as string | null,
  supplierCode: '',
  supplierName: '',
  contactPerson: '',
  phone: '',
  address: '',
  licenseNo: '',
  licenseExpiry: '',
  rating: 3,
  status: 1,
  remark: '',
})

const openCreate = () => {
  form.supplierId = null
  form.supplierCode = ''
  form.supplierName = ''
  form.contactPerson = ''
  form.phone = ''
  form.address = ''
  form.licenseNo = ''
  form.licenseExpiry = ''
  form.rating = 3
  form.status = 1
  form.remark = ''
  dialogVisible.value = true
}

const openEdit = (row: SupplierRow) => {
  form.supplierId = row.supplierId
  form.supplierCode = row.supplierCode || ''
  form.supplierName = row.supplierName || ''
  form.contactPerson = row.contactPerson || ''
  form.phone = row.phone || ''
  form.address = row.address || ''
  form.licenseNo = row.licenseNo || ''
  form.licenseExpiry = row.licenseExpiry || ''
  form.rating = Number(row.rating ?? 3)
  form.status = Number(row.status ?? 1)
  form.remark = row.remark || ''
  dialogVisible.value = true
}

const save = async () => {
  if (!form.supplierName.trim()) {
    ElMessage.warning('请填写供应商名称')
    return
  }
  saving.value = true
  try {
    const res: any = await upsertSupplier({
      supplierId: form.supplierId || undefined,
      supplierCode: form.supplierCode.trim() || undefined,
      supplierName: form.supplierName.trim(),
      contactPerson: form.contactPerson.trim() || undefined,
      phone: form.phone.trim() || undefined,
      address: form.address.trim() || undefined,
      licenseNo: form.licenseNo.trim() || undefined,
      licenseExpiry: form.licenseExpiry || undefined,
      rating: form.rating,
      status: form.status,
      remark: form.remark.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(form.supplierId ? '供应商已更新' : '供应商已创建')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const remove = async (row: SupplierRow) => {
  try {
    await ElMessageBox.confirm(
      `确定删除供应商「${row.supplierName}」？被采购订单引用的供应商无法删除。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    const res: any = await deleteSupplier(row.supplierId)
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

onMounted(() => {
  loadDicts()
  loadList()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-slate-800">供应商管理</h2>
        <el-button v-perm="'pharmacy:supplier:add'" type="primary" :icon="Plus" data-testid="supplier-add-btn" @click="openCreate">
          新增供应商
        </el-button>
      </div>

      <!-- 筛选：条件全部下推后端，前端不对当前页切片 -->
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="编码 / 名称 / 联系人"
            clearable
            style="width: 220px"
            data-testid="supplier-keyword"
            @keyup.enter="query.pageNum = 1; loadList()"
          />
        </el-form-item>
        <el-form-item label="评级">
          <el-select
            v-model="query.rating"
            placeholder="评级"
            clearable
            style="width: 120px"
            :fit-input-width="false"
            data-testid="supplier-rating-filter"
          >
            <el-option v-for="o in ratingOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="query.status"
            placeholder="状态"
            clearable
            style="width: 120px"
            :fit-input-width="false"
          >
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="supplier-table">
        <el-table-column prop="supplierCode" label="编码" width="130" />
        <el-table-column prop="supplierName" label="名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="证照有效期" width="120">
          <template #default="{ row }">{{ row.licenseExpiry || '—' }}</template>
        </el-table-column>
        <el-table-column label="评级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="Number(row.rating) === 1 ? 'danger' : Number(row.rating) === 4 ? 'success' : 'info'">
              {{ ratingText(row.rating) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">{{ enableText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'pharmacy:supplier:add'" link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button v-perm="'pharmacy:supplier:delete'" link type="danger" :icon="Delete" @click="remove(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.supplierId ? '编辑供应商' : '新增供应商'"
      width="560px"
      data-testid="supplier-dialog"
    >
      <el-form label-width="100px">
        <el-form-item label="编码">
          <el-input v-model="form.supplierCode" placeholder="留空由后端生成" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.supplierName" placeholder="供应商名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="经营许可证">
          <el-input v-model="form.businessLicense" />
        </el-form-item>
        <el-form-item label="证照有效期">
          <el-date-picker v-model="form.licenseExpiry" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评级">
          <el-select v-model="form.rating" style="width: 100%" :fit-input-width="false">
            <el-option v-for="o in ratingOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'pharmacy:supplier:add'" type="primary" :loading="saving" data-testid="supplier-save-btn" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
