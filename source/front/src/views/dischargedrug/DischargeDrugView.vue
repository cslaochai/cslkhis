<script setup lang="ts">
/**
 * 出院带药（G20，菜单 310）
 *
 * 流程：开带药单（挂入院次，出院前即可开）→ 药房批量发药（单向，已发药留痕不可改删）。
 * 单条一行药品项；只有「待发药」可编辑/删除。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  dischargeDrugUpsert, dischargeDrugListPage, dischargeDrugDispense, dischargeDrugDelete,
} from '@/api/dischargeDrug'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// ---------------- 字典 ----------------
const statusDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(DICT_TYPE.DISCHARGE_DRUG_STATUS)
    statusDict.value = res?.data?.[DICT_TYPE.DISCHARGE_DRUG_STATUS] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  admissionId: '' as string | number,
  drugName: '', dispenseStatus: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadList = async () => {
  loading.value = true
  try {
    const res: any = await dischargeDrugListPage({
      admissionId: query.admissionId === '' ? undefined : query.admissionId,
      drugName: query.drugName.trim() || undefined,
      dispenseStatus: query.dispenseStatus ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { loading.value = false }
}
const reset = () => {
  Object.assign(query, { admissionId: '', drugName: '', dispenseStatus: null, pageNum: 1 })
  loadList()
}
const statusTag = (v: number) => (v === 2 ? 'success' : 'warning')

// ---------------- 开单 / 编辑 ----------------
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: null as number | null, admissionId: '', drugId: null as number | null, drugName: '',
  spec: '', dosage: '', unit: '', quantity: null as number | null, usageText: '', days: null as number | null, remark: '',
})
const openCreate = () => {
  Object.assign(editForm, {
    id: null, admissionId: query.admissionId === '' ? '' : query.admissionId, drugId: null, drugName: '',
    spec: '', dosage: '', unit: '', quantity: null, usageText: '', days: null, remark: '',
  })
  editVisible.value = true
}
const openEdit = (row: any) => {
  Object.assign(editForm, {
    id: row.id, admissionId: row.admissionId, drugId: row.drugId, drugName: row.drugName,
    spec: row.spec, dosage: row.dosage, unit: row.unit, quantity: row.quantity,
    usageText: row.usageText, days: row.days, remark: row.remark,
  })
  editVisible.value = true
}
const submitEdit = async () => {
  if (!editForm.admissionId || !editForm.drugName.trim() || !editForm.quantity) {
    ElMessage.warning('入院ID、药品名称、带药数量为必填')
    return
  }
  editLoading.value = true
  try {
    const res: any = await dischargeDrugUpsert({
      id: editForm.id ?? undefined,
      admissionId: editForm.admissionId,
      drugId: editForm.drugId ?? undefined,
      drugName: editForm.drugName.trim(),
      spec: editForm.spec || undefined,
      dosage: editForm.dosage || undefined,
      unit: editForm.unit || undefined,
      quantity: editForm.quantity,
      usageText: editForm.usageText || undefined,
      days: editForm.days ?? undefined,
      remark: editForm.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success('带药单已保存')
      editVisible.value = false
      loadList()
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { editLoading.value = false }
}

// ---------------- 发药 / 删除 ----------------
const dispense = async (row: any) => {
  try {
    const res: any = await dischargeDrugDispense([row.id])
    if (res.code === 200) { ElMessage.success('发药完成'); loadList() } else ElMessage.error(res.message || '发药失败')
  } catch (e) { console.error(e); ElMessage.error('发药失败') }
}
const remove = async (row: any) => {
  try {
    const res: any = await dischargeDrugDelete(row.id)
    if (res.code === 200) { ElMessage.success('已删除'); loadList() } else ElMessage.error(res.message || '删除失败')
  } catch (e) { console.error(e); ElMessage.error('删除失败') }
}

onMounted(() => { loadDicts(); loadList() })
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="入院ID">
            <el-input v-model="query.admissionId" placeholder="入院ID" clearable style="width: 180px"
                      @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="药品名称">
            <el-input v-model="query.drugName" placeholder="药品名称" clearable style="width: 180px"
                      @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="发药状态">
            <el-select v-model="query.dispenseStatus" placeholder="发药状态" clearable style="width: 130px"
                       :fit-input-width="false">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'inpatient:dischargeDrug:add'" type="primary" plain @click="openCreate">开带药单</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="discharge-drug-table">
        <el-table-column prop="orderNo" label="带药单号" width="200" />
        <el-table-column prop="patientName" label="患者" width="100">
          <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 ml-1">{{ row.patientNo }}</span></template>
        </el-table-column>
        <el-table-column prop="admissionId" label="入院ID" width="170" />
        <el-table-column prop="drugName" label="药品" min-width="160">
          <template #default="{ row }">{{ row.drugName }}<span v-if="row.spec" class="text-gray-400 ml-1">{{ row.spec }}</span></template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="right">
          <template #default="{ row }">{{ row.quantity }}<span v-if="row.unit" class="text-gray-400 ml-1">{{ row.unit }}</span></template>
        </el-table-column>
        <el-table-column prop="usageText" label="用法" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.usageText || row.dosage || '—' }}</template>
        </el-table-column>
        <el-table-column prop="dispenseStatusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.dispenseStatus)">{{ statusText(row.dispenseStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dispenseName" label="发药人" width="100">
          <template #default="{ row }">{{ row.dispenseName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createBy" label="开单人" width="100" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <template v-if="row.dispenseStatus === 1">
              <el-button v-perm="'inpatient:dischargeDrug:edit'" link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link type="success" size="small" @click="dispense(row)">发药</el-button>
              <el-button v-perm="'inpatient:dischargeDrug:delete'" link type="danger" size="small" @click="remove(row)">删除</el-button>
            </template>
            <span v-else class="text-gray-400">已发药留痕</span>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 开单/编辑 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑带药单' : '开带药单'" width="560px"
               :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="入院ID" required>
          <el-input v-model="editForm.admissionId" placeholder="入院记录ID" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item label="药品名称" required>
          <el-input v-model="editForm.drugName" placeholder="药品名称" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="editForm.spec" placeholder="如 0.25g*24片" />
        </el-form-item>
        <el-form-item label="用法用量">
          <div class="flex gap-2 w-full">
            <el-input v-model="editForm.dosage" placeholder="每次剂量" />
            <el-input v-model="editForm.usageText" placeholder="如 每日三次 饭后" />
          </div>
        </el-form-item>
        <el-form-item label="带药数量" required>
          <div class="flex gap-2 w-full">
            <el-input-number v-model="editForm.quantity" :min="0.01" :precision="2" class="!w-40" />
            <el-input v-model="editForm.unit" placeholder="单位（盒/瓶）" class="!w-32" />
          </div>
        </el-form-item>
        <el-form-item label="用药天数">
          <el-input-number v-model="editForm.days" :min="1" :precision="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button v-perm="['inpatient:dischargeDrug:add','inpatient:dischargeDrug:edit']" type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
