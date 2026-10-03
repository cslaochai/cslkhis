<script setup lang="ts">
/**
 * 器械包模板（菜单 2932，sql/187）
 *
 * 原为 903「消毒供应 CSSD」的「器械包模板」页签，2026-09-29 拆出独立挂菜单。
 * 模板是**建档**（包编码/包名/默认灭菌方式/组成明细，一次性维护）；
 * 回收-清洗-打包-灭菌-发放是**每日流转动作**，岗位与频率不同，故拆开。
 * 回收登记的包名下拉照旧走 cssdTemplateSelectList（仅启用模板），数据流不断。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import {
  cssdTemplateListPage, getCssdTemplateDetail, cssdTemplateItemSelectList,
  cssdTemplateUpsert, cssdTemplateDelete,
} from '@/api/cssd'
import { getDictDataMapList } from '@/api/system'
import { getConsumableSelectList } from '@/api/supplies'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// ---------------- 字典 ----------------
const methodDict = ref<any[]>([])
const methodText = (v: any) => dictLabelText(methodDict.value, v)
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(DICT_TYPE.CSSD_STERIL_METHOD)
    methodDict.value = res?.data?.[DICT_TYPE.CSSD_STERIL_METHOD] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 组成明细名称下拉数据源：启用模板明细去重汇总 + 耗材字典 ----------------
const tplItemOptions = ref<{ name: string; spec?: string; unit?: string }[]>([])
const loadTplItemOptions = async () => {
  try {
    const [tplRes, conRes]: any[] = await Promise.all([cssdTemplateItemSelectList(), getConsumableSelectList()])
    const fromTpl = (tplRes.code === 200 ? tplRes.data || [] : [])
      .map((x: any) => ({ name: x.itemName, spec: x.spec || undefined, unit: x.unit || undefined }))
    const fromConsumable = (conRes.code === 200 ? conRes.data || [] : [])
      .map((x: any) => ({ name: x.consumableName, spec: x.specification || undefined, unit: x.unit || undefined }))
    const seen = new Set<string>()
    tplItemOptions.value = [...fromTpl, ...fromConsumable].filter((x) => x.name && !seen.has(x.name) && !!seen.add(x.name))
  } catch (e) { console.error('加载器械/耗材下拉失败', e) }
}
const onTplItemNameChange = (row: any, name: string) => {
  const c = tplItemOptions.value.find((x) => x.name === name)
  if (!c) return
  row.spec = c.spec || row.spec
  row.unit = c.unit || row.unit
}

// ---------------- 列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', status: null as number | null, pageNum: 1, pageSize: 10 })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadList = async () => {
  loading.value = true
  try {
    const res: any = await cssdTemplateListPage({
      keyword: query.keyword.trim() || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { loading.value = false }
}
const reset = () => {
  Object.assign(query, { keyword: '', status: null, pageNum: 1 })
  loadList()
}

// ---------------- 新增/编辑 ----------------
const editVisible = ref(false)
const saving = ref(false)
const form = reactive<any>({
  id: null as string | null, templateCode: '', packName: '', sterilizeMethod: 1, status: 1, remark: '',
  items: [] as any[],
})
const blankItem = () => ({ itemName: '', spec: '', unit: '件', quantity: 1 })
const openCreate = () => {
  Object.assign(form, {
    id: null, templateCode: '', packName: '', sterilizeMethod: 1, status: 1, remark: '',
    items: [blankItem()],
  })
  editVisible.value = true
}
const openEdit = async (row: any) => {
  try {
    const res: any = await getCssdTemplateDetail(row.id)
    if (res.code !== 200) { ElMessage.error(res.message || '查询失败'); return }
    const d = res.data
    Object.assign(form, {
      id: d.id, templateCode: d.templateCode, packName: d.packName,
      sterilizeMethod: Number(d.sterilizeMethod), status: Number(d.status), remark: d.remark || '',
      items: (d.items || []).map((i: any) => ({
        itemName: i.itemName, spec: i.spec || '', unit: i.unit || '件', quantity: Number(i.quantity),
      })),
    })
    editVisible.value = true
  } catch (e) { console.error(e); ElMessage.error('查询失败') }
}
const addItem = () => form.items.push(blankItem())
const removeItem = (idx: number) => form.items.splice(idx, 1)
const save = async () => {
  if (!form.templateCode.trim()) { ElMessage.warning('请填写包编码'); return }
  if (!form.packName.trim()) { ElMessage.warning('请填写器械包名称'); return }
  const items = form.items.filter((i: any) => i.itemName.trim())
  if (!items.length) { ElMessage.warning('组成明细至少一条'); return }
  saving.value = true
  try {
    const res: any = await cssdTemplateUpsert({
      id: form.id ?? undefined,
      templateCode: form.templateCode.trim(),
      packName: form.packName.trim(),
      sterilizeMethod: form.sterilizeMethod,
      status: form.status,
      remark: form.remark || undefined,
      items: items.map((i: any) => ({
        itemName: i.itemName.trim(), spec: i.spec?.trim() || undefined,
        unit: i.unit?.trim() || '件', quantity: Number(i.quantity) || 1,
      })),
    })
    if (res.code === 200) {
      ElMessage.success('模板已保存'); editVisible.value = false
      loadList()
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { saving.value = false }
}
const del = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除模板【${row.packName}】？历史追溯存的是包名快照，不受影响。`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    const res: any = await cssdTemplateDelete(row.id)
    if (res.code === 200) { ElMessage.success('已删除'); loadList() }
    else ElMessage.error(res.message || '删除失败')
  } catch (e) { console.error(e); ElMessage.error('删除失败') }
}

onMounted(() => { loadDicts(); loadList(); loadTplItemOptions() })
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="包编码/名称" clearable style="width: 200px"
                      @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" :fit-input-width="false">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" plain v-perm="'asset:cssd:edit'" @click="openCreate" data-testid="cssd-tpl-create-btn">新增模板</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="cssd-tpl-table">
        <el-table-column prop="templateCode" label="包编码" width="120" />
        <el-table-column prop="packName" label="器械包名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="默认灭菌方式" width="120">
          <template #default="{ row }">{{ row.sterilizeMethodText || methodText(row.sterilizeMethod) }}</template>
        </el-table-column>
        <el-table-column prop="itemCount" label="明细项数" width="90" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-perm="'asset:cssd:edit'" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" v-perm="'asset:cssd:edit'" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 模板编辑 -->
    <el-dialog v-model="editVisible" :title="form.id ? `编辑模板 — ${form.templateCode}` : '新增模板'"
               width="880px" :close-on-click-modal="false">
      <el-form label-width="110px">
        <div class="flex flex-wrap gap-x-6">
          <el-form-item label="包编码" required>
            <el-input v-model="form.templateCode" placeholder="如 CSSDP008" style="width: 200px" />
          </el-form-item>
          <el-form-item label="器械包名称" required>
            <el-input v-model="form.packName" style="width: 200px" />
          </el-form-item>
          <el-form-item label="默认灭菌方式" required>
            <el-select v-model="form.sterilizeMethod" style="width: 160px" :fit-input-width="false">
              <el-option v-for="d in methodDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item label="组成明细" required>
          <div class="w-full">
            <el-table :data="form.items" size="small" border max-height="320">
              <el-table-column label="器械/耗材名称" min-width="260">
                <template #default="{ row }">
                  <el-select v-model="row.itemName" filterable allow-create default-first-option
                             placeholder="从耗材字典选择或手输" class="w-full" :fit-input-width="false"
                             @change="(v: string) => onTplItemNameChange(row, v)">
                    <el-option v-for="(c, i) in tplItemOptions" :key="i" :label="c.name" :value="c.name" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="规格" width="170">
                <template #default="{ row }"><el-input v-model="row.spec" /></template>
              </el-table-column>
              <el-table-column label="单位" width="100">
                <template #default="{ row }"><el-input v-model="row.unit" /></template>
              </el-table-column>
              <el-table-column label="基数" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.quantity" :min="1" size="small" controls-position="right" style="width: 90px" />
                </template>
              </el-table-column>
              <el-table-column label="" width="60" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" :icon="Delete" :disabled="form.items.length <= 1"
                             @click="removeItem($index)" />
                </template>
              </el-table-column>
            </el-table>
            <div class="flex items-center gap-3 mt-1">
              <el-button link type="primary" :icon="Plus" @click="addItem">添加明细行</el-button>
              <span class="text-xs text-gray-500">共 {{ form.items.length }} 条明细</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" v-perm="'asset:cssd:edit'" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
