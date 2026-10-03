<script setup lang="ts">
/**
 * 血库储血台账（G17，菜单 411）
 *
 * 四个 tab：库存台账（入库/预留/发血/报废/退回 + 效期预警）、交叉配血
 * （开单 → 执行 → 复核双人，相合自动预留血袋）、出入库流水（只增不改）、分血型统计。
 * 发血前置：配血复核相合 → 血袋已预留 → 带 apply_no 发血。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  bloodInbound, getBloodInventoryPage, getBloodInventoryStats, bloodReserve,
  bloodCancelReserve, bloodIssue, bloodScrap, bloodReturn,
  crossmatchCreate, getCrossmatchPage, crossmatchExecute, crossmatchVerify, crossmatchVoid,
  getBloodLogPage,
} from '@/api/medicaltech'
import { getDictDataMapList } from '@/api/system'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const activeTab = ref('inventory')

// ---------------- 字典 ----------------
const bloodTypeDict = ref<any[]>([])
const rhDict = ref<any[]>([])
const componentDict = ref<any[]>([])
const invStatusDict = ref<any[]>([])
const sourceDict = ref<any[]>([])
const cmStatusDict = ref<any[]>([])
const cmResultDict = ref<any[]>([])
const cmMethodDict = ref<any[]>([])
const bloodTypeText = (v: any) => dictLabelText(bloodTypeDict.value, v)
const rhText = (v: any) => dictLabelText(rhDict.value, v)
const componentText = (v: any) => dictLabelText(componentDict.value, v)
const invStatusText = (v: any) => dictLabelText(invStatusDict.value, v)
const sourceText = (v: any) => dictLabelText(sourceDict.value, v)
const cmStatusText = (v: any) => dictLabelText(cmStatusDict.value, v)
const cmResultText = (v: any) => dictLabelText(cmResultDict.value, v)
const cmMethodText = (v: any) => dictLabelText(cmMethodDict.value, v)

const loadDicts = async () => {
  try {
    // ⚠ 后端 getDictDataMapList 一次最多 5 个 type，超了直接报错 —— 分两批
    const res1: any = await getDictDataMapList(
      `${DICT_TYPE.BLOOD_TYPE},${DICT_TYPE.BLOOD_RH},${DICT_TYPE.BLOOD_COMPONENT},${DICT_TYPE.BLOOD_INVENTORY_STATUS},${DICT_TYPE.BLOOD_SOURCE_TYPE}`)
    const res2: any = await getDictDataMapList(
      `${DICT_TYPE.CROSSMATCH_STATUS},${DICT_TYPE.CROSSMATCH_RESULT},${DICT_TYPE.CROSSMATCH_METHOD}`)
    bloodTypeDict.value = res1?.data?.[DICT_TYPE.BLOOD_TYPE] || []
    rhDict.value = res1?.data?.[DICT_TYPE.BLOOD_RH] || []
    componentDict.value = res1?.data?.[DICT_TYPE.BLOOD_COMPONENT] || []
    invStatusDict.value = res1?.data?.[DICT_TYPE.BLOOD_INVENTORY_STATUS] || []
    sourceDict.value = res1?.data?.[DICT_TYPE.BLOOD_SOURCE_TYPE] || []
    cmStatusDict.value = res2?.data?.[DICT_TYPE.CROSSMATCH_STATUS] || []
    cmResultDict.value = res2?.data?.[DICT_TYPE.CROSSMATCH_RESULT] || []
    cmMethodDict.value = res2?.data?.[DICT_TYPE.CROSSMATCH_METHOD] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 统计 ----------------
const stats = reactive({
  inStock: 0, reserved: 0, issued: 0, expireSoon: 0, todayIn: 0, todayOut: 0, pendingMatch: 0,
  byBloodType: [] as any[],
})
const loadStats = async () => {
  try {
    const res: any = await getBloodInventoryStats()
    if (res.code === 200 && res.data) Object.assign(stats, res.data)
  } catch (e) { console.error(e) }
}

// ---------------- 库存 ----------------
const invLoading = ref(false)
const invRows = ref<any[]>([])
const invTotal = ref(0)
const invQuery = reactive({
  bagNo: '', bloodType: null as number | null, componentType: null as number | null,
  status: null as number | null, expireWithinDays: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})
const loadInventory = async () => {
  invLoading.value = true
  try {
    const res: any = await getBloodInventoryPage({
      bagNo: invQuery.bagNo.trim() || undefined,
      bloodType: invQuery.bloodType ?? undefined,
      componentType: invQuery.componentType ?? undefined,
      status: invQuery.status ?? undefined,
      expireWithinDays: invQuery.expireWithinDays ?? undefined,
      pageNum: invQuery.pageNum, pageSize: invQuery.pageSize,
    })
    if (res.code === 200) {
      invRows.value = res.data?.records || []
      invTotal.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { invLoading.value = false }
}
const resetInv = () => {
  Object.assign(invQuery, { bagNo: '', bloodType: null, componentType: null, status: null, expireWithinDays: null, pageNum: 1 })
  loadInventory()
}
const expireTag = (row: any) => {
  const d = Number(row.expireDays)
  if (d < 0) return 'danger'
  if (d <= 7) return 'warning'
  return 'success'
}

// 入库
const inVisible = ref(false)
const inForm = reactive({
  bagNo: '', bloodType: null as number | null, rhType: 1, componentType: null as number | null,
  volume: null as number | null, unitAmount: null as number | null, collectDate: '', expireDate: '',
  sourceType: 1, sourceName: '', donorNo: '', aboVerify: 0, storageLoc: '',
})
const inLoading = ref(false)
const openInbound = () => {
  Object.assign(inForm, {
    bagNo: '', bloodType: null, rhType: 1, componentType: null, volume: null, unitAmount: null,
    collectDate: '', expireDate: '', sourceType: 1, sourceName: '', donorNo: '', aboVerify: 0, storageLoc: '',
  })
  inVisible.value = true
}
const saveInbound = async () => {
  if (!inForm.bagNo.trim()) { ElMessage.warning('血袋号必填'); return }
  if (inForm.bloodType == null || inForm.componentType == null) { ElMessage.warning('血型与血液成分必填'); return }
  inLoading.value = true
  try {
    const res: any = await bloodInbound({
      ...inForm, bagNo: inForm.bagNo.trim(),
      volume: inForm.volume ?? undefined, unitAmount: inForm.unitAmount ?? undefined,
      collectDate: inForm.collectDate || undefined, expireDate: inForm.expireDate || undefined,
      sourceName: inForm.sourceName || undefined, donorNo: inForm.donorNo || undefined,
      storageLoc: inForm.storageLoc || undefined,
    })
    if (res.code === 200) { ElMessage.success('血袋已入库'); inVisible.value = false; loadInventory(); loadStats() }
    else ElMessage.error(res.message || '入库失败')
  } catch (e) { console.error(e); ElMessage.error('入库失败') } finally { inLoading.value = false }
}

// 袋动作
const doBagAction = (fn: (row: any) => Promise<any>, row: any, done: () => void) => {
  fn(row).then((res: any) => {
    if (res.code === 200) { ElMessage.success(res.message || '操作成功'); done() }
    else ElMessage.error(res.message || '操作失败')
  }).catch((e: any) => { console.error(e); ElMessage.error('操作失败') })
}
const reserve = (row: any) => doBagAction((r) => bloodReserve({ bagId: r.id }), row, () => { loadInventory(); loadStats() })
const cancelReserve = (row: any) => doBagAction((r) => bloodCancelReserve({ bagId: r.id }), row, () => { loadInventory(); loadStats() })

const issue = (row: any) => {
  ElMessageBox.prompt('用血申请单号（必填）', `发血：${row.bagNo}`, {
    confirmButtonText: '发血', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '用血申请单号不能为空',
  }).then(({ value }) => doBagAction(
    () => bloodIssue({ bagId: row.id, applyNo: value.trim() }), row, () => { loadInventory(); loadStats() })).catch(() => {})
}
const scrap = (row: any) => {
  ElMessageBox.prompt('报废原因（必填）', `报废：${row.bagNo}`, {
    confirmButtonText: '确定', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '报废原因不能为空',
  }).then(({ value }) => doBagAction(
    () => bloodScrap({ bagId: row.id, reason: value.trim() }), row, () => { loadInventory(); loadStats() })).catch(() => {})
}
const returnBag = (row: any) => {
  ElMessageBox.prompt('退回原因（必填）', `退回：${row.bagNo}`, {
    confirmButtonText: '确定', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '退回原因不能为空',
  }).then(({ value }) => doBagAction(
    () => bloodReturn({ bagId: row.id, reason: value.trim() }), row, () => { loadInventory(); loadStats() })).catch(() => {})
}

// ---------------- 交叉配血 ----------------
const cmLoading = ref(false)
const cmRows = ref<any[]>([])
const cmTotal = ref(0)
const cmQuery = reactive({ matchNo: '', bagNo: '', patientName: '', status: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const loadCrossmatch = async () => {
  cmLoading.value = true
  try {
    const res: any = await getCrossmatchPage({
      matchNo: cmQuery.matchNo.trim() || undefined,
      bagNo: cmQuery.bagNo.trim() || undefined,
      patientName: cmQuery.patientName.trim() || undefined,
      status: cmQuery.status ?? undefined,
      pageNum: cmQuery.pageNum, pageSize: cmQuery.pageSize,
    })
    if (res.code === 200) {
      cmRows.value = res.data?.records || []
      cmTotal.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { cmLoading.value = false }
}
const resetCm = () => {
  Object.assign(cmQuery, { matchNo: '', bagNo: '', patientName: '', status: null, pageNum: 1 })
  loadCrossmatch()
}

const cmVisible = ref(false)
const cmForm = reactive({
  applyNo: '', patientId: null as any, patientNo: '', patientName: '',
  patientBloodType: null as number | null, patientRhType: 1, bagNo: '',
})
const cmLoadingBtn = ref(false)
const openCmCreate = () => {
  Object.assign(cmForm, { applyNo: '', patientId: null, patientNo: '', patientName: '', patientBloodType: null, patientRhType: 1, bagNo: '' })
  cmVisible.value = true
}
const onCmPatient = (p: any) => {
  cmForm.patientId = p?.id
  cmForm.patientName = p?.name || p?.patientName || ''
  cmForm.patientNo = p?.patientNo || ''
}
const saveCm = async () => {
  if (!cmForm.patientId || !cmForm.patientName.trim()) { ElMessage.warning('请选择患者'); return }
  if (cmForm.patientBloodType == null) { ElMessage.warning('患者血型必填'); return }
  if (!cmForm.bagNo.trim()) { ElMessage.warning('血袋号必填'); return }
  cmLoadingBtn.value = true
  try {
    const res: any = await crossmatchCreate({
      applyNo: cmForm.applyNo.trim() || undefined,
      patientId: cmForm.patientId, patientNo: cmForm.patientNo || undefined,
      patientName: cmForm.patientName.trim(),
      patientBloodType: cmForm.patientBloodType, patientRhType: cmForm.patientRhType,
      bagNo: cmForm.bagNo.trim(),
    })
    if (res.code === 200) { ElMessage.success(`配血单 ${res.data?.matchNo || ''} 已创建`); cmVisible.value = false; loadCrossmatch(); loadStats() }
    else ElMessage.error(res.message || '创建失败')
  } catch (e) { console.error(e); ElMessage.error('创建失败') } finally { cmLoadingBtn.value = false }
}

const cmExecVisible = ref(false)
const cmExecRow = ref<any>(null)
const cmExecForm = reactive({ result: 1, method: 2, conclusion: '' })
const openCmExecute = (row: any) => {
  cmExecRow.value = row
  Object.assign(cmExecForm, { result: 1, method: Number(row.method) || 2, conclusion: '' })
  cmExecVisible.value = true
}
const saveCmExecute = () => {
  crossmatchExecute({
    matchId: cmExecRow.value.id, result: cmExecForm.result,
    method: cmExecForm.method, conclusion: cmExecForm.conclusion.trim() || undefined,
  }).then((res: any) => {
    if (res.code === 200) { ElMessage.success('配血已执行，待第二人复核'); cmExecVisible.value = false; loadCrossmatch(); loadStats() }
    else ElMessage.error(res.message || '执行失败')
  }).catch((e: any) => { console.error(e); ElMessage.error('执行失败') })
}
const doCmVerify = (row: any) => {
  crossmatchVerify(row.id).then((res: any) => {
    if (res.code === 200) { ElMessage.success('复核通过' + (Number(row.result) === 1 ? '，血袋已自动预留' : '')); loadCrossmatch(); loadStats() }
    else ElMessage.error(res.message || '复核失败')
  }).catch((e: any) => { console.error(e); ElMessage.error('复核失败') })
}
const doCmVoid = (row: any) => {
  crossmatchVoid(row.id).then((res: any) => {
    if (res.code === 200) { ElMessage.success('已作废'); loadCrossmatch(); loadStats() }
    else ElMessage.error(res.message || '作废失败')
  }).catch((e: any) => { console.error(e); ElMessage.error('作废失败') })
}

// ---------------- 流水 ----------------
const logLoading = ref(false)
const logRows = ref<any[]>([])
const logTotal = ref(0)
const logQuery = reactive({ bagNo: '', bizType: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const BIZ_TYPES = [
  { value: 1, label: '入库' }, { value: 2, label: '发血' }, { value: 3, label: '退回' },
  { value: 4, label: '报废' }, { value: 5, label: '预留' }, { value: 6, label: '取消预留' },
]
const loadLogs = async () => {
  logLoading.value = true
  try {
    const res: any = await getBloodLogPage({
      bagNo: logQuery.bagNo.trim() || undefined,
      bizType: logQuery.bizType ?? undefined,
      pageNum: logQuery.pageNum, pageSize: logQuery.pageSize,
    })
    if (res.code === 200) {
      logRows.value = res.data?.records || []
      logTotal.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { logLoading.value = false }
}
const fmtTime = (t: any) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

const refreshAll = () => { loadStats(); if (activeTab.value === 'inventory') loadInventory() }

onMounted(() => { loadDicts(); loadStats(); loadInventory(); loadCrossmatch(); loadLogs() })
</script>

<template>
  <div class="p-5 space-y-4">
    <!-- 统计 -->
    <div class="space-y-3">
      <div class="grid grid-cols-7 gap-3">
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">在库</div>
          <div class="text-xl font-semibold text-[#0E9488] mt-1">{{ stats.inStock }} <span class="text-xs font-normal text-gray-400">袋</span></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">已预留</div>
          <div class="text-xl font-semibold text-[#1269B5] mt-1">{{ stats.reserved }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">已发血</div>
          <div class="text-xl font-semibold text-[#6B7280] mt-1">{{ stats.issued }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">7 天内到期</div>
          <div class="text-xl font-semibold text-[#B91C1C] mt-1">{{ stats.expireSoon }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">今日入库</div>
          <div class="text-xl font-semibold text-[#1269B5] mt-1">{{ stats.todayIn }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">今日发血</div>
          <div class="text-xl font-semibold text-[#B45309] mt-1">{{ stats.todayOut }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">待配血</div>
          <div class="text-xl font-semibold text-[#B45309] mt-1">{{ stats.pendingMatch }}</div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm flex items-center gap-6">
        <span class="text-sm text-gray-500">分血型在库：</span>
        <span v-for="t in stats.byBloodType" :key="t.bloodType" class="text-sm">
          <span class="font-medium">{{ t.bloodTypeText }}</span>
          <span class="ml-1 text-[#1269B5] font-semibold">{{ t.bagCount }}</span> 袋 /
          <span>{{ t.volumeTotal }}</span> ml
        </span>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <el-tabs v-model="activeTab" @tab-change="refreshAll">
        <!-- 库存台账 -->
        <el-tab-pane label="库存台账" name="inventory">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="invQuery.bagNo" placeholder="血袋号" clearable style="width: 160px" @keyup.enter="invQuery.pageNum = 1; loadInventory()" />
            <el-select v-model="invQuery.bloodType" placeholder="血型" clearable style="width: 100px" :fit-input-width="false">
              <el-option v-for="d in bloodTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="invQuery.componentType" placeholder="成分" clearable style="width: 120px" :fit-input-width="false">
              <el-option v-for="d in componentDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="invQuery.status" placeholder="状态" clearable style="width: 110px" :fit-input-width="false">
              <el-option v-for="d in invStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-select v-model="invQuery.expireWithinDays" placeholder="效期预警" clearable style="width: 130px" :fit-input-width="false">
              <el-option label="7 天内到期" :value="7" />
              <el-option label="3 天内到期" :value="3" />
              <el-option label="已过期" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="invQuery.pageNum = 1; loadInventory()">查询</el-button>
            <el-button :icon="Refresh" @click="resetInv">重置</el-button>
            <div class="flex-1" />
            <el-button v-perm="'medtech:bloodBank:add'" type="primary" @click="openInbound">血袋入库</el-button>
          </div>
          <el-table :data="invRows" v-loading="invLoading" size="small" data-testid="blood-inventory-table">
            <el-table-column prop="bagNo" label="血袋号" width="140" />
            <el-table-column label="血型" width="100">
              <template #default="{ row }">{{ bloodTypeText(row.bloodType) }} / {{ rhText(row.rhType) }}</template>
            </el-table-column>
            <el-table-column label="成分" width="110"><template #default="{ row }">{{ componentText(row.componentType) }}</template></el-table-column>
            <el-table-column prop="volume" label="血量(ml)" width="90" align="right" />
            <el-table-column prop="expireDate" label="失效日期" width="105" />
            <el-table-column label="效期" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.expireDays != null" size="small" :type="expireTag(row) as any">
                  {{ Number(row.expireDays) < 0 ? '已过期' : `${row.expireDays}天` }}
                </el-tag>
                <span v-else class="text-gray-300">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="storageLoc" label="存放位置" width="110" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ invStatusText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="applyNo" label="申请单号" width="110" show-overflow-tooltip />
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button v-if="Number(row.status) === 1" v-perm="'medtech:bloodBank:edit'" link type="primary" size="small" @click="reserve(row)">预留</el-button>
                <template v-if="Number(row.status) === 2">
                  <el-button v-perm="'medtech:bloodBank:edit'" link type="warning" size="small" @click="issue(row)">发血</el-button>
                  <el-button v-perm="'medtech:bloodBank:edit'" link type="primary" size="small" @click="cancelReserve(row)">取消预留</el-button>
                </template>
                <el-button v-if="[1, 2].includes(Number(row.status))" v-perm="'medtech:bloodBank:delete'" link type="danger" size="small" @click="scrap(row)">报废</el-button>
                <el-button v-if="[1, 2].includes(Number(row.status))" v-perm="'medtech:bloodBank:delete'" link type="info" size="small" @click="returnBag(row)">退回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination background layout="total, prev, pager, next" :total="invTotal"
              v-model:current-page="invQuery.pageNum" v-model:page-size="invQuery.pageSize" @current-change="loadInventory" />
          </div>
        </el-tab-pane>

        <!-- 交叉配血 -->
        <el-tab-pane label="交叉配血" name="crossmatch">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="cmQuery.matchNo" placeholder="配血编号" clearable style="width: 160px" @keyup.enter="cmQuery.pageNum = 1; loadCrossmatch()" />
            <el-input v-model="cmQuery.bagNo" placeholder="血袋号" clearable style="width: 140px" @keyup.enter="cmQuery.pageNum = 1; loadCrossmatch()" />
            <el-input v-model="cmQuery.patientName" placeholder="患者姓名" clearable style="width: 130px" @keyup.enter="cmQuery.pageNum = 1; loadCrossmatch()" />
            <el-select v-model="cmQuery.status" placeholder="状态" clearable style="width: 110px" :fit-input-width="false">
              <el-option v-for="d in cmStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="cmQuery.pageNum = 1; loadCrossmatch()">查询</el-button>
            <el-button :icon="Refresh" @click="resetCm">重置</el-button>
            <div class="flex-1" />
            <el-button v-perm="'medtech:bloodBank:add'" type="primary" @click="openCmCreate">新建配血单</el-button>
          </div>
          <el-table :data="cmRows" v-loading="cmLoading" size="small" data-testid="crossmatch-table">
            <el-table-column prop="matchNo" label="配血编号" width="150" />
            <el-table-column prop="patientName" label="患者" width="90" />
            <el-table-column label="患者血型" width="110">
              <template #default="{ row }">{{ bloodTypeText(row.patientBloodType) }} / {{ rhText(row.patientRhType) }}</template>
            </el-table-column>
            <el-table-column prop="bagNo" label="血袋号" width="130" />
            <el-table-column label="血袋血型" width="110">
              <template #default="{ row }">{{ bloodTypeText(row.bagBloodType) }} / {{ rhText(row.bagRhType) }}</template>
            </el-table-column>
            <el-table-column label="方法" width="110"><template #default="{ row }">{{ cmMethodText(row.method) }}</template></el-table-column>
            <el-table-column label="结果" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.result != null" size="small" :type="Number(row.result) === 1 ? 'success' : 'danger'">{{ cmResultText(row.result) }}</el-tag>
                <span v-else class="text-gray-300">-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag size="small">{{ cmStatusText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="operator" label="配血人" width="90" />
            <el-table-column prop="verifier" label="复核人" width="90" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button v-if="Number(row.status) === 1" v-perm="'medtech:bloodBank:edit'" link type="primary" size="small" @click="openCmExecute(row)">执行配血</el-button>
                <el-button v-if="Number(row.status) === 2" v-perm="'medtech:bloodBank:edit'" link type="success" size="small" @click="doCmVerify(row)">复核</el-button>
                <el-button v-if="[1, 2].includes(Number(row.status))" v-perm="'medtech:bloodBank:delete'" link type="danger" size="small" @click="doCmVoid(row)">作废</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination background layout="total, prev, pager, next" :total="cmTotal"
              v-model:current-page="cmQuery.pageNum" v-model:page-size="cmQuery.pageSize" @current-change="loadCrossmatch" />
          </div>
        </el-tab-pane>

        <!-- 流水 -->
        <el-tab-pane label="出入库流水" name="log">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="logQuery.bagNo" placeholder="血袋号" clearable style="width: 160px" @keyup.enter="logQuery.pageNum = 1; loadLogs()" />
            <el-select v-model="logQuery.bizType" placeholder="业务类型" clearable style="width: 120px" :fit-input-width="false">
              <el-option v-for="t in BIZ_TYPES" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="logQuery.pageNum = 1; loadLogs()">查询</el-button>
            <el-button :icon="Refresh" @click="logQuery.bagNo = ''; logQuery.bizType = null; logQuery.pageNum = 1; loadLogs()">重置</el-button>
          </div>
          <el-table :data="logRows" v-loading="logLoading" size="small" data-testid="blood-log-table">
            <el-table-column prop="bagNo" label="血袋号" width="140" />
            <el-table-column label="业务" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="[2, 4].includes(Number(row.bizType)) ? 'warning' : 'primary'">{{ row.bizTypeText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyNo" label="申请单号" width="120" show-overflow-tooltip />
            <el-table-column prop="reason" label="原因" min-width="160" show-overflow-tooltip />
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column label="操作时间" width="140"><template #default="{ row }">{{ fmtTime(row.operateTime) }}</template></el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination background layout="total, prev, pager, next" :total="logTotal"
              v-model:current-page="logQuery.pageNum" v-model:page-size="logQuery.pageSize" @current-change="loadLogs" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 入库弹窗 -->
    <el-dialog v-model="inVisible" title="血袋入库登记" width="680px">
      <el-form label-width="100px">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="血袋号" required><el-input v-model="inForm.bagNo" placeholder="全局唯一" /></el-form-item>
          <el-form-item label="献血码"><el-input v-model="inForm.donorNo" /></el-form-item>
          <el-form-item label="血型" required>
            <el-select v-model="inForm.bloodType" style="width: 100%">
              <el-option v-for="d in bloodTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="Rh">
            <el-radio-group v-model="inForm.rhType">
              <el-radio v-for="d in rhDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="血液成分" required>
            <el-select v-model="inForm.componentType" style="width: 100%">
              <el-option v-for="d in componentDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="血量(ml)"><el-input-number v-model="inForm.volume" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="单位(U)"><el-input-number v-model="inForm.unitAmount" :precision="2" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="存放位置"><el-input v-model="inForm.storageLoc" placeholder="储血冰箱A/2层" /></el-form-item>
          <el-form-item label="采集日期"><el-date-picker v-model="inForm.collectDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
          <el-form-item label="失效日期"><el-date-picker v-model="inForm.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
          <el-form-item label="血液来源">
            <el-select v-model="inForm.sourceType" style="width: 100%">
              <el-option v-for="d in sourceDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="来源单位/人"><el-input v-model="inForm.sourceName" /></el-form-item>
        </div>
        <el-form-item label="血型复核">
          <el-radio-group v-model="inForm.aboVerify">
            <el-radio :value="0">未复核</el-radio>
            <el-radio :value="1">已复核</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inVisible = false">取消</el-button>
        <el-button v-perm="'medtech:bloodBank:add'" type="primary" :loading="inLoading" @click="saveInbound">入库</el-button>
      </template>
    </el-dialog>

    <!-- 配血单弹窗 -->
    <el-dialog v-model="cmVisible" title="新建配血单" width="560px">
      <el-form label-width="100px">
        <el-form-item label="患者" required>
          <patient-select v-model="cmForm.patientId" @select="onCmPatient" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="患者血型" required>
            <el-select v-model="cmForm.patientBloodType" style="width: 100%">
              <el-option v-for="d in bloodTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="患者 Rh">
            <el-radio-group v-model="cmForm.patientRhType">
              <el-radio v-for="d in rhDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item label="血袋号" required><el-input v-model="cmForm.bagNo" placeholder="从库存台账复制在库血袋号" /></el-form-item>
        <el-form-item label="用血申请单号"><el-input v-model="cmForm.applyNo" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cmVisible = false">取消</el-button>
        <el-button v-perm="'medtech:bloodBank:add'" type="primary" :loading="cmLoadingBtn" @click="saveCm">创建</el-button>
      </template>
    </el-dialog>

    <!-- 执行配血弹窗 -->
    <el-dialog v-model="cmExecVisible" :title="`执行配血：${cmExecRow?.matchNo || ''}`" width="520px">
      <el-form label-width="90px">
        <el-form-item label="配血方法">
          <el-select v-model="cmExecForm.method" style="width: 100%">
            <el-option v-for="d in cmMethodDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="配血结果">
          <el-radio-group v-model="cmExecForm.result">
            <el-radio v-for="d in cmResultDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="补充结论"><el-input v-model="cmExecForm.conclusion" type="textarea" :rows="2" placeholder="ABO/Rh 核对说明由服务端生成，这里补充观察记录" /></el-form-item>
      </el-form>
      <div class="text-xs text-gray-400 px-6">相合的配血单经第二人复核后，血袋自动置「已预留」；发血时凭预留状态 + 申请单号执行。</div>
      <template #footer>
        <el-button @click="cmExecVisible = false">取消</el-button>
        <el-button v-perm="'medtech:bloodBank:edit'" type="primary" @click="saveCmExecute">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
