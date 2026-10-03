<template>
  <div class="operation-count-page" data-testid="operation-count-view">
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <el-select v-model="countApplyId" placeholder="选择手术申请单" filterable clearable class="!w-96"
                   data-testid="g15-count-apply" @change="loadCount">
          <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
        </el-select>
        <el-button v-perm="'ipd:operationCount:add'" type="primary" :icon="Plus" data-testid="g15-count-create"
                   @click="openCountCreate">建立清点单
        </el-button>
        <el-button
            v-if="countRow && (countRow.canCountBefore || countRow.canCountClosure || countRow.canCountFinal)"
            v-perm="'ipd:operationCount:edit'"
            type="warning"
            data-testid="g15-count-phase"
            @click="openPhase"
        >
          登记清点
        </el-button>
      </div>

      <div v-if="!countRow" class="py-8 text-center text-sm text-slate-400" data-testid="g15-count-empty">
        该手术尚未建立器械清点单（清点是可选登记的；一旦建了单，三轮对不上会锁死手术完成登记）
      </div>

      <div v-else class="space-y-4">
        <div v-if="countRow.warningText"
             class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] text-amber-700">
          <el-icon class="mr-1 align-middle">
            <Warning/>
          </el-icon>
          {{ countRow.warningText }}
        </div>
        <el-descriptions :column="4" border size="small" data-testid="g15-count-desc">
          <el-descriptions-item label="清点单号">{{ text(countRow.countNo) }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ text(countRow.patientName) }}</el-descriptions-item>
          <el-descriptions-item label="术式">{{ text(countRow.plannedOperationName) }}</el-descriptions-item>
          <el-descriptions-item label="手术间">{{ text(countRow.operationRoom) }}</el-descriptions-item>
          <el-descriptions-item label="当前阶段">{{ text(countRow.phaseText) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ text(countRow.statusText) }}</el-descriptions-item>
          <el-descriptions-item label="洗手护士">{{ text(countRow.instrumentNurseName) }}</el-descriptions-item>
          <el-descriptions-item label="巡回护士">{{ text(countRow.circulateNurseName) }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="countRow.items || []" style="width: 100%" data-testid="g15-count-item-table">
          <el-table-column prop="seqNo" label="#" width="50"/>
          <el-table-column label="类别" width="90">
            <template #default="{ row }">{{ text(row.itemCategoryText) }}</template>
          </el-table-column>
          <el-table-column label="名称 / 规格" min-width="180">
            <template #default="{ row }">
              <div class="text-slate-900">{{ text(row.itemName) }}</div>
              <div class="text-[11px] text-slate-400">{{ text(row.spec) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="术前" width="90" align="center">
            <template #default="{ row }">{{
                row.beforeQty === null || row.beforeQty === undefined ? '—' : row.beforeQty
              }}
            </template>
          </el-table-column>
          <el-table-column label="关体前" width="90" align="center">
            <template #default="{ row }">
              {{ row.closureQty === null || row.closureQty === undefined ? '—' : row.closureQty }}
            </template>
          </el-table-column>
          <el-table-column label="关体后" width="90" align="center">
            <template #default="{ row }">{{
                row.finalQty === null || row.finalQty === undefined ? '—' : row.finalQty
              }}
            </template>
          </el-table-column>
          <el-table-column label="关体后 vs 术前" min-width="150">
            <template #default="{ row }">
              <span v-if="row.consistent === null || row.consistent === undefined" class="text-[11px] text-slate-400">尚未比对</span>
              <span v-else-if="row.consistent" class="text-[11px] text-green-600">一致</span>
              <span v-else class="text-[11px] font-medium text-red-600">差异 {{
                  row.diffQty > 0 ? '+' : ''
                }}{{ row.diffQty }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="text-[12px] text-slate-500">
          合计：术前 {{ countRow.totalBefore }} / 关体前 {{ countRow.totalClosure }} / 关体后 {{ countRow.totalFinal }}
        </div>
        <div v-if="countRow.diffNote"
             class="rounded border border-red-200 bg-red-50 px-3 py-2 text-[12px] text-red-700">
          差异说明：{{ countRow.diffNote }}
        </div>
      </div>
    </div>

    <!-- 建立清点单 -->
    <el-dialog v-model="countCreateVisible" title="建立手术器械清点单" width="720px"
               data-testid="g15-count-create-dialog">
      <el-form label-width="120px">
        <el-form-item label="手术申请单" required>
          <el-select v-model="countCreateForm.applyId" placeholder="选择手术申请单" filterable class="!w-full"
                     data-testid="g15-count-create-apply">
            <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="洗手护士">
          <el-select v-model="countCreateForm.instrumentNurseId" placeholder="器械护士" filterable clearable
                     class="!w-full">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="巡回护士">
          <el-select v-model="countCreateForm.circulateNurseId" placeholder="巡回护士" filterable clearable
                     class="!w-full">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="清点清单">
          <el-input v-model="countCreateForm.itemsText" type="textarea" :rows="6"
                    placeholder="每行一条：名称,类别,数量（类别 1器械 2敷料 3缝针 4刀片 5其他）"
                    data-testid="g15-count-items"/>
        </el-form-item>
      </el-form>
      <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] text-amber-700">
        <el-icon class="mr-1 align-middle">
          <Warning/>
        </el-icon>
        清单在开始清点之前定：进入任何阶段后不能再加行（后补的基线没有比对意义）。
      </div>
      <template #footer>
        <el-button @click="countCreateVisible = false">取消</el-button>
        <el-button v-perm="'ipd:operationCount:add'" type="primary" :loading="countCreateSubmitting"
                   data-testid="g15-count-create-submit" @click="submitCountCreate">建立
        </el-button>
      </template>
    </el-dialog>

    <!-- 三阶段清点 -->
    <el-dialog v-model="phaseVisible" :title="phaseTitle" width="760px" data-testid="g15-count-phase-dialog">
      <div v-if="countRow" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
          {{ text(countRow.countNo) }} · {{ text(countRow.patientName) }} · {{ text(countRow.plannedOperationName) }}
          <div class="text-[11px] text-slate-500">当前阶段：{{ text(countRow.phaseText) }}</div>
        </div>
        <el-form label-width="110px">
          <el-form-item label="核对人" required>
            <el-select v-model="phaseForm.nurseId" placeholder="选择核对人（清点必须留名）" filterable class="!w-full"
                       data-testid="g15-count-nurse">
              <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
            </el-select>
          </el-form-item>
        </el-form>
        <el-table :data="countRow.items || []" style="width: 100%">
          <el-table-column label="名称" min-width="200">
            <template #default="{ row }">
              <span class="text-slate-900">{{ text(row.itemName) }}</span>
              <span class="ml-2 text-[11px] text-slate-400">{{ text(row.itemCategoryText) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="术前基线" width="110" align="center">
            <template #default="{ row }">{{
                row.beforeQty === null || row.beforeQty === undefined ? '—' : row.beforeQty
              }}
            </template>
          </el-table-column>
          <el-table-column label="本次实点" width="180" align="center">
            <template #default="{ row }">
              <el-input-number v-model="phaseForm.qtys[String(row.id)]" :min="0" :step="1" size="small"
                               :data-testid="`g15-count-qty-${row.seqNo}`"/>
            </template>
          </el-table-column>
        </el-table>
        <el-form label-width="110px">
          <el-form-item label="差异说明">
            <el-input v-model="phaseForm.diffNote" type="textarea" :rows="2"
                      placeholder="任一项与术前不一致时必填：差了什么、怎么处理、结论" data-testid="g15-count-diffnote"/>
          </el-form-item>
        </el-form>
        <div class="text-[12px] text-slate-500">
          判定基准是<b>术前基线</b>，不是"和上一段比" ——
          连续两段都少一块纱布时，"与上段一致"会显示通过。
        </div>
      </div>
      <template #footer>
        <el-button @click="phaseVisible = false">取消</el-button>
        <el-button v-perm="'ipd:operationCount:edit'" type="primary" :loading="phaseSubmitting"
                   data-testid="g15-count-phase-submit" @click="submitPhase">登记清点
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Plus, Warning} from '@element-plus/icons-vue'
import {countOperationPhase, createOperationCount, getOperationCountByApply,} from '@/api/operationCount'
import {getOperationApplyListPage} from '@/api/inpatientOperation'
import {getEmployeeList} from '@/api/system'
import {countPhaseText} from '@/lib/anesthesia'

interface ApplyOption {
  id: string
  applyNo?: string
  patientName?: string
  admissionNo?: string
  plannedOperationName?: string
  isEmergency?: number
}

interface EmployeeOption {
  id: string
  empName?: string
  deptName?: string
}

interface CountItemRow {
  id: string
  seqNo?: number
  itemCategoryText?: string
  itemName?: string
  spec?: string
  beforeQty?: number
  closureQty?: number
  finalQty?: number
  consistent?: boolean
  diffQty?: number
}

interface CountRow {
  id: string
  countNo?: string
  applyId?: string
  patientName?: string
  plannedOperationName?: string
  operationRoom?: string
  phase?: number
  phaseText?: string
  status?: number
  statusText?: string
  discrepancyFlag?: number
  diffNote?: string
  instrumentNurseName?: string
  circulateNurseName?: string
  items?: CountItemRow[]
  totalBefore?: number
  totalClosure?: number
  totalFinal?: number
  warningText?: string
  canCountBefore?: boolean
  canCountClosure?: boolean
  canCountFinal?: boolean
}

const text = (v?: string | number) => (v === null || v === undefined || v === '' ? '—' : String(v))

// ==================== 手术申请单 / 员工候选 ====================
const applies = ref<ApplyOption[]>([])
const employees = ref<EmployeeOption[]>([])
const applyLabel = (a: ApplyOption) =>
    `${a.applyNo || '—'} · ${a.patientName || '—'} · ${a.plannedOperationName || '—'}${
        a.isEmergency === 1 ? '（急诊）' : ''
    }`

const loadBaseData = async () => {
  try {
    const res = await getOperationApplyListPage({pageNum: 1, pageSize: 200})
    applies.value = (res.data?.records || []) as ApplyOption[]
  } catch (e: any) {
    console.error('加载手术申请失败:', e)
  }
  try {
    const res = await getEmployeeList({})
    employees.value = (res.data || []) as EmployeeOption[]
  } catch (e: any) {
    console.error('加载员工失败:', e)
  }
}

// ==================== 清点单 ====================
const countApplyId = ref('')
const countRow = ref<CountRow | null>(null)
const countLoading = ref(false)

const loadCount = async () => {
  if (!countApplyId.value) {
    countRow.value = null
    return
  }
  countLoading.value = true
  try {
    const res = await getOperationCountByApply(countApplyId.value)
    countRow.value = (res.data || null) as CountRow | null
  } catch (e: any) {
    ElMessage.error(e.message || '加载清点单失败')
  } finally {
    countLoading.value = false
  }
}

const countCreateVisible = ref(false)
const countCreateSubmitting = ref(false)
const countCreateForm = reactive({
  applyId: '',
  instrumentNurseId: '',
  circulateNurseId: '',
  itemsText: '止血钳,1,10\n纱布块,2,20\n圆针,3,5\n11号刀片,4,2',
})

const openCountCreate = () => {
  countCreateForm.applyId = countApplyId.value || ''
  countCreateForm.instrumentNurseId = ''
  countCreateForm.circulateNurseId = ''
  countCreateVisible.value = true
}

/** 文本 → items：每行「名称,类别,数量」，类别 1器械 2敷料 3缝针 4刀片 5其他 */
const parseItems = (raw: string) => {
  const items: { itemName: string; itemCategory: number; beforeQty: number }[] = []
  raw
      .split('\n')
      .map((l) => l.trim())
      .filter(Boolean)
      .forEach((line) => {
        const parts = line.split(/[,，]/).map((p) => p.trim())
        if (!parts[0]) return
        items.push({
          itemName: parts[0],
          itemCategory: Number(parts[1] || 1),
          beforeQty: Number(parts[2] || 0),
        })
      })
  return items
}

const submitCountCreate = async () => {
  if (!countCreateForm.applyId) {
    ElMessage.warning('请选择手术申请单')
    return
  }
  countCreateSubmitting.value = true
  try {
    const res = await createOperationCount({
      applyId: countCreateForm.applyId,
      instrumentNurseId: countCreateForm.instrumentNurseId || undefined,
      circulateNurseId: countCreateForm.circulateNurseId || undefined,
      items: parseItems(countCreateForm.itemsText),
    })
    ElMessage.success(`清点单已建立：${res.data || ''}（请依次完成三次核对）`)
    countCreateVisible.value = false
    countApplyId.value = countCreateForm.applyId
    await loadCount()
  } catch (e: any) {
    ElMessage.error(e.message || '建立清点单失败')
  } finally {
    countCreateSubmitting.value = false
  }
}

const phaseVisible = ref(false)
const phaseSubmitting = ref(false)
const phaseForm = reactive({phase: 1 as number, nurseId: '', diffNote: '', qtys: {} as Record<string, number>})

const openPhase = () => {
  const c = countRow.value
  if (!c) return
  phaseForm.phase = (c.phase || 0) + 1
  phaseForm.nurseId = ''
  phaseForm.diffNote = ''
  phaseForm.qtys = {}
  ;(c.items || []).forEach((it) => {
    phaseForm.qtys[String(it.id)] = Number(it.beforeQty ?? 0)
  })
  phaseVisible.value = true
}

const phaseTitle = computed(() => '登记' + countPhaseText(phaseForm.phase))

const submitPhase = async () => {
  const c = countRow.value
  if (!c) return
  if (!phaseForm.nurseId) {
    ElMessage.warning('请选择核对人（清点必须留名）')
    return
  }
  const quantities = (c.items || []).map((it) => ({
    itemId: String(it.id),
    qty: Number(phaseForm.qtys[String(it.id)] ?? 0),
  }))
  phaseSubmitting.value = true
  try {
    await countOperationPhase({
      countId: c.id,
      phase: phaseForm.phase,
      nurseId: phaseForm.nurseId,
      quantities,
      diffNote: phaseForm.diffNote.trim() || undefined,
    })
    ElMessage.success(`${countPhaseText(phaseForm.phase)}已登记`)
    phaseVisible.value = false
    await loadCount()
  } catch (e: any) {
    ElMessage.error(e.message || '清点登记失败')
  } finally {
    phaseSubmitting.value = false
  }
}

onMounted(() => {
  loadBaseData()
})
</script>
