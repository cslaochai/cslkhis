<script setup lang="ts">
/**
 * 日间手术准入目录（菜单 2928，sql/183；原为 316 日间手术的第二个页签）
 *
 * 目录是日间手术的**准入闸门主数据**：
 * 1. 预约登记只能选启用中的目录术式（316 的预约下拉走 getDaySurgeryItemList，只含启用，
 *    后端也校验）——目录停用后不能再新预约，存量登记单不受影响。
 * 2. 手术分级决定术者授权校验：日间手术要求术者持有不低于目录登记的分级（sql/155）。
 * 3. 最长滞留(h)是超期判定的基准，超期由服务端算（overdue），前端不重算。
 *
 * 拆分口径：目录（主数据，随术式准入政策调整，低频）与登记台账（单据，逐患者经办，高频）
 * 主键维度不同、写接口不同（daySurgeryItemUpsert/updateDaySurgeryItemStatus ↔ daySurgery* 单据流转），
 * 拆开后各自独立菜单，数据流：本页改目录 → 316 预约下拉自动跟着变（同一份目录表）。
 *
 * 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { listDaySurgeryItemPage, daySurgeryItemUpsert, updateDaySurgeryItemStatus } from '@/api/daySurgery'
import { getDepartmentSelectList, getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// 手术分级（字典 his_operation_level）：日间手术按目录上登记的分级要求术者授权（sql/155）
const operationLevelMap: Record<number, string> = { 1: '一级', 2: '二级', 3: '三级', 4: '四级' }

const anesDict = ref<any[]>([])
const anesText = (v: any) => dictLabelText(anesDict.value, v)
const deptOptions = ref<any[]>([])

// ---------------- 目录列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', deptId: null as any, enabledOnly: true, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await listDaySurgeryItemPage({
      keyword: query.keyword.trim() || undefined,
      deptId: query.deptId || undefined,
      enabledOnly: query.enabledOnly || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.deptId = null
  query.enabledOnly = true
  query.pageNum = 1
  loadList()
}

// ---------------- 新增 / 编辑 ----------------
const editVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null as string | null, itemCode: '', itemName: '', deptId: null as any,
  maxStayHours: 48 as number, operationLevel: 2 as number, anesthesiaType: null as number | null,
  standardFee: null as number | null, status: 1 as number, remark: '',
})

const openCreate = () => {
  Object.assign(form, {
    id: null, itemCode: '', itemName: '', deptId: null, maxStayHours: 48, operationLevel: 2,
    anesthesiaType: null, standardFee: null, status: 1, remark: '',
  })
  editVisible.value = true
}

const openEdit = (row: any) => {
  Object.assign(form, {
    id: row.id, itemCode: row.itemCode, itemName: row.itemName, deptId: row.deptId ?? null,
    maxStayHours: Number(row.maxStayHours ?? 48), operationLevel: Number(row.operationLevel ?? 2),
    anesthesiaType: row.anesthesiaType ?? null,
    standardFee: row.standardFee ?? null, status: Number(row.status ?? 1), remark: row.remark || '',
  })
  editVisible.value = true
}

const save = async () => {
  if (!form.itemCode.trim() || !form.itemName.trim()) { ElMessage.warning('请填写术式编码与名称'); return }
  saving.value = true
  try {
    const res: any = await daySurgeryItemUpsert({
      id: form.id || undefined,
      itemCode: form.itemCode.trim(),
      itemName: form.itemName.trim(),
      deptId: form.deptId || undefined,
      maxStayHours: form.maxStayHours,
      operationLevel: form.operationLevel,
      anesthesiaType: form.anesthesiaType ?? undefined,
      standardFee: form.standardFee ?? undefined,
      status: form.status,
      remark: form.remark.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(form.id ? '修改成功' : '术式已录入')
      editVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    console.error(e); ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const toggle = async (row: any) => {
  const next = Number(row.status) === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      next === 0 ? `停用后该术式不可新预约（存量登记单不受影响），确认停用「${row.itemName}」？` : `确认启用「${row.itemName}」？`,
      '启停确认', { type: 'warning' })
  } catch { return }
  try {
    const res: any = await updateDaySurgeryItemStatus(row.id, next)
    if (res.code === 200) { ElMessage.success(next === 1 ? '已启用' : '已停用'); loadList() }
    else ElMessage.error(res.message || '操作失败')
  } catch (e) { console.error(e); ElMessage.error('操作失败') }
}

onMounted(async () => {
  try {
    const [deptRes, dictRes]: any[] = await Promise.all([
      getDepartmentSelectList({}),
      getDictDataMapList(DICT_TYPE.DAY_SURGERY_ANESTHESIA),
    ])
    if (deptRes.code === 200) deptOptions.value = deptRes.data || []
    anesDict.value = dictRes?.data?.[DICT_TYPE.DAY_SURGERY_ANESTHESIA] || []
  } catch (e) { console.error('加载基础数据失败', e) }
  await loadList()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="编码 / 名称" clearable style="width: 200px"
              @keyup.enter="loadList" />
          </el-form-item>
          <el-form-item label="适用科室">
            <el-select v-model="query.deptId" placeholder="适用科室" clearable filterable style="width: 180px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.enabledOnly">仅启用</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:daySurgery:add'" type="primary" :icon="Plus" @click="openCreate">新增术式</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight">
        <el-table-column prop="itemCode" label="术式编码" width="120" />
        <el-table-column prop="itemName" label="术式名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="deptName" label="适用科室" width="160" show-overflow-tooltip />
        <el-table-column label="最长滞留(h)" width="110" align="right">
          <template #default="{ row }">{{ row.maxStayHours }}</template>
        </el-table-column>
        <el-table-column label="手术分级" width="90" align="center">
          <template #default="{ row }">{{ operationLevelMap[Number(row.operationLevel)] || '—' }}</template>
        </el-table-column>
        <el-table-column label="麻醉方式" width="120">
          <template #default="{ row }">{{ anesText(row.anesthesiaType) }}</template>
        </el-table-column>
        <el-table-column label="标准费用(元)" width="120" align="right">
          <template #default="{ row }">{{ row.standardFee ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="Number(row.status) === 1 ? 'success' : 'info'">
              {{ Number(row.status) === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'ipd:daySurgery:add'" link type="primary" size="small"
              @click.stop="openEdit(row)">编辑</el-button>
            <el-button v-perm="'ipd:daySurgery:add'" link :type="Number(row.status) === 1 ? 'warning' : 'success'"
              size="small" @click.stop="toggle(row)">
              {{ Number(row.status) === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无准入术式" /></template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 准入术式 -->
    <el-dialog v-model="editVisible" :title="form.id ? '修改准入术式' : '新增准入术式'" width="620px">
      <el-form label-width="120px" size="small">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="术式编码" required><el-input v-model="form.itemCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最长滞留(h)" required>
              <el-input-number v-model="form.maxStayHours" :min="1" :max="168" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="手术分级" required>
          <el-select v-model="form.operationLevel" style="width: 160px" :fit-input-width="false">
            <el-option v-for="(label, code) in operationLevelMap" :key="code" :label="label" :value="Number(code)" />
          </el-select>
          <span class="ml-2 text-xs text-slate-400">决定预约时校验的术者授权级别（日间手术要求术者持有不低于此级）</span>
        </el-form-item>
        <el-form-item label="术式名称" required><el-input v-model="form.itemName" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="适用科室">
              <el-select v-model="form.deptId" filterable clearable placeholder="请选择" style="width: 100%">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="麻醉方式">
              <el-select v-model="form.anesthesiaType" clearable placeholder="请选择" style="width: 100%">
                <el-option v-for="d in anesDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="标准费用(元)">
              <el-input-number v-model="form.standardFee" :min="0" :precision="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

