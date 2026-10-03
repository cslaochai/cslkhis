<script setup lang="ts">
/**
 * 耗材字典（菜单 2921 / 路由 /consumable-dict，sql/177）
 *
 * 为什么从「物资耗材」里拆出来单独挂菜单：
 * 字典是**建档**动作 —— 新耗材进院时录一次编码/类别/规格/零售价/是否高值，
 * 一年到头动不了几回；而批次库存、出入库、领用是每天发生的库房动作。
 * 合在一页时，库管每次点开「物资耗材」看到的第一个页签是库存，要建档得先切页签，
 * 而*建档该由谁做*（耗材管理员/设备科）与*发料该由谁做*（库管）也常不是同一个人。
 * 内容与拆之前的「耗材字典」页签同源，接口与权限码均未变。
 */
import { ref, onMounted } from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getConsumableList, consumableUpsert } from '@/api/supplies'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)

// 类别口径：1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他 6-植入介入（未知(n) 不回落）
const categoryMap: Record<number, string> = {
  1: '卫生材料', 2: '注射穿刺', 3: '医用敷料', 4: '防护用品', 5: '其他', 6: '植入介入',
}
const categoryLabel = (c: number | null | undefined) => (c == null ? '未知' : categoryMap[c] || `未知(${c})`)

const dictList = ref<any[]>([])
const dictKeyword = ref('')
const dictPagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const dictDialogVisible = ref(false)
const dictForm = ref({ id: null as number | null, consumableCode: '', consumableName: '', category: 5, specification: '', unit: '', manufacturer: '', retailPrice: null as number | null, status: 1, isHighValue: 0, udiDi: '', regCertNo: '' })

const loadDict = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: dictPagination.value.pageNum, pageSize: dictPagination.value.pageSize }
    if (dictKeyword.value.trim()) params.keyword = dictKeyword.value.trim()
    const res = await getConsumableList(params)
    dictList.value = res.data?.records || []
    dictPagination.value.total = res.data?.total || 0
  } catch (e: any) {
    console.error('加载耗材字典失败:', e)
  } finally {
    loading.value = false
  }
}

const openDictDialog = (row?: any) => {
  dictForm.value = row
    ? { id: row.id, consumableCode: row.consumableCode, consumableName: row.consumableName, category: row.category ?? 5, specification: row.specification || '', unit: row.unit || '', manufacturer: row.manufacturer || '', retailPrice: row.retailPrice ?? null, status: row.status ?? 1, isHighValue: row.isHighValue ?? 0, udiDi: row.udiDi || '', regCertNo: row.regCertNo || '' }
    : { id: null, consumableCode: '', consumableName: '', category: 5, specification: '', unit: '', manufacturer: '', retailPrice: null, status: 1, isHighValue: 0, udiDi: '', regCertNo: '' }
  dictDialogVisible.value = true
}

const handleDictSubmit = async () => {
  const f = dictForm.value
  if (!f.consumableCode.trim()) return ElMessage.warning('耗材编码不能为空')
  if (!f.consumableName.trim()) return ElMessage.warning('耗材名称不能为空')
  try {
    await consumableUpsert({ ...f, consumableCode: f.consumableCode.trim(), consumableName: f.consumableName.trim() })
    ElMessage.success(f.id ? '字典修改成功' : '字典新增成功')
    dictDialogVisible.value = false
    loadDict()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
}

onMounted(() => {
  loadDict()
})
</script>

<template>
  <div data-testid="consumable-dict-view">
    <!-- 页头操作 -->
    <div class="mb-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-base font-semibold text-slate-700">耗材字典</h2>
          <p class="mt-0.5 text-xs text-slate-400">耗材基础档案：编码/类别/规格/单位/零售价/是否高值（高值须维护 UDI-DI 与注册证号才能扫码溯源）</p>
        </div>
      </div>
    </div>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="dictKeyword" placeholder="耗材名称/编码" :prefix-icon="Search" class="!w-60" clearable @keyup.enter="loadDict" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadDict">查询</el-button>
            <el-button :icon="Refresh" @click="dictKeyword = ''; loadDict()">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" plain :icon="Plus" v-perm="'asset:supplies:add'" @click="openDictDialog()">新增耗材</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="dictList" v-loading="loading" stripe :max-height="tableMaxHeight">
        <el-table-column prop="consumableCode" label="编码" width="110" class-name="font-mono" />
        <el-table-column prop="consumableName" label="耗材名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="类别" width="100" align="center">
          <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
        </el-table-column>
        <el-table-column label="高值" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.isHighValue === 1" class="inline-block rounded bg-violet-100 px-2 py-0.5 font-medium text-violet-700">高值</span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" min-width="140" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="manufacturer" label="生产厂家" min-width="130" show-overflow-tooltip />
        <el-table-column label="零售价" width="90" align="right">
          <template #default="{ row }">¥{{ row.retailPrice ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 font-medium', row.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500']">
              {{ row.status === 1 ? '启用' : row.status === 0 ? '停用' : `未知(${row.status})` }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link v-perm="'asset:supplies:edit'" @click="openDictDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="dictList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">暂无耗材字典</div>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="dictPagination.pageNum" v-model:page-size="dictPagination.pageSize"
          :page-sizes="PAGE_SIZES" :total="dictPagination.total" layout="total, sizes, prev, pager, next"
          @size-change="loadDict" @current-change="loadDict" />
      </div>
    </el-card>

    <!-- 字典编辑弹窗 -->
    <el-dialog v-model="dictDialogVisible" :title="dictForm.id ? '编辑耗材' : '新增耗材'" width="520px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="耗材编码" required>
          <el-input v-model="dictForm.consumableCode" placeholder="如 HC-HC-006" />
        </el-form-item>
        <el-form-item label="耗材名称" required>
          <el-input v-model="dictForm.consumableName" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="dictForm.category" style="width: 100%" :fit-input-width="false">
            <el-option v-for="(label, key) in categoryMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="dictForm.specification" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="dictForm.unit" placeholder="包/支/盒/个" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="dictForm.manufacturer" />
        </el-form-item>
        <el-form-item label="零售价">
          <el-input-number v-model="dictForm.retailPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="高值耗材">
          <el-switch v-model="dictForm.isHighValue" :active-value="1" :inactive-value="0" active-text="扫码溯源登记" inactive-text="普通领用" />
        </el-form-item>
        <template v-if="dictForm.isHighValue === 1">
          <el-form-item label="UDI-DI">
            <el-input v-model="dictForm.udiDi" placeholder="GS1 数据载体 (01) 段 14 位，扫码按此命中字典" />
          </el-form-item>
          <el-form-item label="注册证号">
            <el-input v-model="dictForm.regCertNo" placeholder="如 国械注准20173660001" />
          </el-form-item>
        </template>
        <el-form-item label="状态">
          <el-select v-model="dictForm.status" style="width: 100%" :fit-input-width="false">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" v-perm="['asset:supplies:add','asset:supplies:edit']" @click="handleDictSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
