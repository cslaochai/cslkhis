<script setup lang="ts">
/**
 * 医嘱基础字典（给药途径 / 用药频次 / 剂量单位）—— 菜单 2383，sql/142
 *
 * 为什么有这一页：这三类此前是前端硬编码（lib/drugUsage.js），库里查不到字典，
 * 结果就是医生站与护士站各按各的常量渲染 —— 库里真实出现过「静脉泵入」而常量里没有，
 * 两侧看到的值从此对不上。现在统一落 sys_dict_data，医生站下拉从字典取，这里负责维护。
 *
 * 三条刻意收紧的规则（都在服务端，前端照做即可）：
 *  1. **值不可改**：存量医嘱行里存的就是这串值，改了历史医嘱会渲染成「未知(xxx)」。
 *     要换值：停用旧的 + 新增一条。所以编辑弹窗里值是只读的。
 *  2. **只认三种 dictType**：这个口子改不了别的字典（那是 system:dict:* 的领地）。
 *  3. **停用不删除**：停用项不进下拉，但历史医嘱按原值照样渲染出文案。
 *     列表上的「使用量」直接告诉维护人这个值有多少条医嘱在用。
 */
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getOrderDictListPage,
  upsertOrderDict,
  deleteOrderDictById,
} from '@/api/inpatientOrder'
import { ORDER_DICT_TYPE, loadOrderUsageOptions } from '@/lib/drugUsage'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const TABS = [
  { type: ORDER_DICT_TYPE.ROUTE, label: '给药途径', hint: '值存中文（历史医嘱 route 列就是中文）' },
  { type: ORDER_DICT_TYPE.FREQ, label: '用药频次', hint: '值存英文缩写（qd / bid / q12h…）' },
  { type: ORDER_DICT_TYPE.DOSE_UNIT, label: '剂量单位', hint: '值存单位字面量（g / mg / ml / 片…）' },
]

const activeType = ref<string>(ORDER_DICT_TYPE.ROUTE)
const currentTab = () => TABS.find((t) => t.type === activeType.value) || TABS[0]

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  status: null as number | null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getOrderDictListPage({
      dictType: activeType.value,
      keyword: query.keyword || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    rows.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载字典失败')
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  query.pageNum = 1
  loadList()
}
const onReset = () => {
  query.keyword = ''
  query.status = null
  query.pageNum = 1
  loadList()
}

watch(activeType, () => {
  query.pageNum = 1
  loadList()
})

// ---------------- 新增 / 编辑 ----------------
const dialog = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const form = reactive({
  id: null as any,
  dictType: ORDER_DICT_TYPE.ROUTE,
  dictValue: '',
  dictLabel: '',
  dictSort: null as number | null,
  status: 1,
  remark: '',
})

const openCreate = () => {
  editing.value = null
  form.id = null
  form.dictType = activeType.value
  form.dictValue = ''
  form.dictLabel = ''
  form.dictSort = null
  form.status = 1
  form.remark = ''
  dialog.value = true
}

const openEdit = (row: any) => {
  editing.value = row
  form.id = row.id
  form.dictType = row.dictType
  form.dictValue = row.dictValue
  form.dictLabel = row.dictLabel
  form.dictSort = row.dictSort
  form.status = row.status ?? 1
  form.remark = row.remark || ''
  dialog.value = true
}

const submit = async () => {
  if (!form.dictLabel) {
    ElMessage.warning('请填写显示名')
    return
  }
  if (!editing.value && !form.dictValue) {
    ElMessage.warning('请填写字典值')
    return
  }
  saving.value = true
  try {
    await upsertOrderDict({
      id: form.id || undefined,
      dictType: form.dictType,
      dictValue: editing.value ? undefined : form.dictValue,
      dictLabel: form.dictLabel,
      dictSort: form.dictSort ?? undefined,
      status: form.status,
      remark: form.remark || undefined,
    })
    ElMessage.success('已保存')
    dialog.value = false
    // 字典页改完要顺手把前端缓存的选项刷一遍，否则当前会话的下拉还是旧值
    await loadOrderUsageOptions()
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const remove = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `删除后新开医嘱的下拉里不再有「${row.dictLabel}」${row.usageCount ? `（当前有 ${row.usageCount} 条医嘱在用，历史医嘱不受影响）` : ''}。`,
      '删除字典项',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteOrderDictById(row.id, row.dictType)
    ElMessage.success('已删除')
    await loadOrderUsageOptions()
    loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(() => {
  loadList()
  loadOrderUsageOptions()
})
</script>

<template>
  <div class="p-4">
    <el-card shadow="never">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-2">
          <span class="text-base font-medium">医嘱基础字典</span>
        </div>
      </template>

      <el-tabs v-model="activeType" data-testid="orderdict-tabs">
        <el-tab-pane v-for="t in TABS" :key="t.type" :label="t.label" :name="t.type" />
      </el-tabs>

      <div class="mb-3 flex flex-wrap items-center gap-3">
        <el-input
          v-model="query.keyword"
          data-testid="orderdict-search"
          size="small"
          class="!w-64"
          :prefix-icon="Search"
          clearable
          placeholder="值 / 显示名"
          @keyup.enter="onSearch"
        />
        <el-select v-model="query.status" size="small" class="!w-32" clearable placeholder="状态">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button size="small" type="primary" :icon="Search" data-testid="orderdict-query" @click="onSearch">查询</el-button>
        <el-button size="small" :icon="Refresh" @click="onReset">重置</el-button>
        <el-button size="small" type="primary" plain :icon="Plus" v-perm="'ipd:orderDict:add'" data-testid="orderdict-add" @click="openCreate">
          新增{{ currentTab().label }}
        </el-button>
        <span class="text-xs text-slate-400">{{ currentTab().hint }}</span>
      </div>

      <el-table
        v-loading="loading"
        :data="rows"
        data-testid="orderdict-table"
        row-key="id"
        border
        size="small"
        style="width: 100%"
      >
        <el-table-column prop="dictValue" label="值" width="140" />
        <el-table-column prop="dictLabel" label="显示名" min-width="160" />
        <el-table-column prop="dictSort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageCount" label="医嘱使用量" width="110" align="center" />
        <el-table-column label="来源" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.builtIn" type="primary" size="small" effect="plain">内置</el-tag>
            <span v-else class="text-xs text-slate-500">自定义</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-perm="'ipd:orderDict:add'" data-testid="orderdict-edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" v-perm="'ipd:orderDict:delete'" data-testid="orderdict-delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">该类型下还没有字典项</div>
        </template>
      </el-table>

      <div class="mt-3 flex justify-end">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="total"
          layout="total, sizes, prev, pager, next"
          size="small"
          @current-change="loadList"
          @size-change="onSearch"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog" :title="editing ? '编辑字典项' : `新增${currentTab().label}`" width="520px">
      <el-form label-width="90px">
        <el-form-item label="字典类型">
          <el-input :model-value="currentTab().label" disabled />
        </el-form-item>
        <el-form-item label="值" required>
          <el-input
            v-model="form.dictValue"
            data-testid="orderdict-form-value"
            :disabled="!!editing"
            :placeholder="editing ? '值不可修改（存量医嘱在用它）' : '如 静滴 / qd / mg'"
          />
        </el-form-item>
        <el-form-item label="显示名" required>
          <el-input v-model="form.dictLabel" data-testid="orderdict-form-label" placeholder="下拉里显示的文字" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.dictSort" :min="1" controls-position="right" class="!w-full" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="选填" />
        </el-form-item>
      </el-form>
      <el-alert
        v-if="editing"
        type="info"
        :closable="false"
        show-icon
        class="mt-2"
        title="值不允许修改：存量医嘱行里存的就是它，改了历史医嘱会显示成「未知」。要换值请停用这一条、再新增一条。"
      />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="orderdict-submit" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
