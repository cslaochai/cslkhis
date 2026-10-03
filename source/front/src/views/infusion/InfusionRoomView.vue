<script setup lang="ts">
/**
 * 门诊输液室（M10，菜单 2180，挂医技医辅）
 *
 * 链路：入座（建输液单+占座）→【需皮试：打皮试 → ≥15 分钟观察窗 → 判读】→ 开始（滴速）
 * → N 次巡视 → 结束（不良反应）。结束/取消释放座位。座位全院物理资源。
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import {
  getInfusionBoard, listInfusionSeats, upsertInfusionSeat, admitInfusion,
  createSkinTest, judgeSkinTest, startInfusion, roundInfusion,
  finishInfusion, cancelInfusion, listInfusionPage, listInfusionRounds,
} from '@/api/infusionRoom'

const STATUS_TEXT: Record<number, string> = {
  1: '待皮试', 2: '待输注', 3: '输液中', 4: '已完成', 5: '已取消',
}
const STATUS_TAG: Record<number, string> = {
  1: 'warning', 2: 'primary', 3: 'danger', 4: 'success', 5: 'info',
}
const SEAT_STATUS_TEXT: Record<number, string> = { 1: '空闲', 2: '占用', 3: '停用' }

const board = ref<any>(null)
const seats = ref<any[]>([])
const pageData = ref<any>({ records: [], total: 0 })
const query = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, status: null as number | null, keyword: '' })
const loading = ref(false)
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const loadBoard = async () => {
  const res: any = await getInfusionBoard()
  board.value = res?.data || null
  seats.value = board.value?.seats || []
}
const loadPage = async () => {
  loading.value = true
  try {
    const res: any = await listInfusionPage(query.value)
    pageData.value = res?.data || { records: [], total: 0 }
  } finally { loading.value = false }
}
const reload = async () => { await Promise.all([loadBoard(), loadPage()]) }
onMounted(reload)

// ---------------- 座位维护 ----------------
const seatDlg = ref(false)
const seatForm = ref<any>({})
const openSeatDlg = (seat?: any) => {
  seatForm.value = seat
    ? { id: seat.id, seatNo: seat.seatNo, area: seat.area, seatStatus: seat.seatStatus, remark: seat.remark }
    : { id: null, seatNo: '', area: '普通区', seatStatus: 1, remark: '' }
  seatDlg.value = true
}
const saveSeat = async () => {
  const res: any = await upsertInfusionSeat(seatForm.value)
  if (res?.code === 200) { ElMessage.success('座位已保存'); seatDlg.value = false; loadBoard() }
}

// ---------------- 入座 ----------------
const admitDlg = ref(false)
const admitForm = ref<any>(null)
const openAdmit = (seat: any) => {
  if (seat.seatStatus !== 1) { ElMessage.warning('座位不是空闲状态'); return }
  admitForm.value = { seatId: seat.id, seatNo: seat.seatNo, patientId: null, patientName: '',
    treatmentRecordId: null, drugSummary: '', needSkinTest: 0, remark: '' }
  admitDlg.value = true
}
const onPatientSelect = (p: any) => {
  if (admitForm.value) admitForm.value.patientName = p?.patientName || ''
}
const saveAdmit = async () => {
  const f = admitForm.value
  if (!f.patientId) { ElMessage.warning('请先选患者'); return }
  if (!f.drugSummary.trim()) { ElMessage.warning('请填写输注内容摘要'); return }
  const res: any = await admitInfusion(f)
  if (res?.code === 200) {
    ElMessage.success(`已入座 ${f.seatNo}（单号 ${res.data?.infusionNo}）`)
    admitDlg.value = false; reload()
  }
}

// ---------------- 操作 ----------------
const opDlg = ref(false)
const opForm = ref<any>({})
const openSkinTest = (row: any) => {
  opForm.value = { infusionId: row.id, drugName: '', remark: '' }; opDlg.value = true
}
const saveSkinTest = async () => {
  const res: any = await createSkinTest(opForm.value)
  if (res?.code === 200) { ElMessage.success('皮试已打，观察 15 分钟后判读'); opDlg.value = false; reload() }
}

const openJudge = (row: any) => {
  ElMessageBox.prompt('皮试判读：1=阴性 2=阳性（阳性将自动取消输液单）', '皮试判读', {
    inputPattern: /^[12]$/, inputErrorMessage: '请输入 1 或 2',
  }).then(async ({ value }) => {
    const res: any = await judgeSkinTest({ skinTestId: row.skinTestId, result: Number(value) })
    if (res?.code === 200) {
      ElMessage.success(Number(value) === 1 ? '判读阴性，可开始输注' : '判读阳性，输液单已取消')
      reload()
    }
  }).catch(() => {})
}

const openStart = (row: any) => {
  ElMessageBox.prompt('起始滴速（滴/分）', '开始输注', {
    inputPattern: /^[1-9]\d*$/, inputErrorMessage: '请输入正整数',
  }).then(async ({ value }) => {
    const res: any = await startInfusion({ infusionId: row.id, dripRate: Number(value) })
    if (res?.code === 200) { ElMessage.success('输注已开始'); reload() }
  }).catch(() => {})
}

const openRound = (row: any) => {
  ElMessageBox.prompt('巡视：滴速（滴/分，可空）', '巡视', { inputPattern: /^[1-9]\d*$/, inputErrorMessage: '请输入正整数' })
    .then(async ({ value }) => {
      const res: any = await roundInfusion({ infusionId: row.id, dripRate: Number(value) })
      if (res?.code === 200) { ElMessage.success('巡视已记录'); reload() }
    }).catch(() => {})
}

const openFinish = (row: any) => {
  ElMessageBox.prompt('结束输注：有无不良反应？0=无 1=有（选 1 需在下一步填描述）', '结束输注', {
    inputPattern: /^[01]$/, inputErrorMessage: '请输入 0 或 1',
  }).then(async ({ value }) => {
    const adverse = Number(value)
    let adverseDesc = ''
    if (adverse === 1) {
      const { value: desc } = await ElMessageBox.prompt('不良反应描述（必填）', '不良反应', {
        inputValidator: (v: string) => (v && v.trim() ? true : '描述必填'),
      })
      adverseDesc = desc
    }
    const res: any = await finishInfusion({ infusionId: row.id, adverseFlag: adverse, adverseDesc })
    if (res?.code === 200) { ElMessage.success('输注已结束，座位已释放'); reload() }
  }).catch(() => {})
}

const openCancel = (row: any) => {
  ElMessageBox.prompt('取消原因（必填）', '取消输液单', {
    inputValidator: (v: string) => (v && v.trim() ? true : '原因必填'),
  }).then(async ({ value }) => {
    const res: any = await cancelInfusion({ infusionId: row.id, cancelReason: value })
    if (res?.code === 200) { ElMessage.success('已取消，座位已释放'); reload() }
  }).catch(() => {})
}

const roundsDlg = ref(false)
const rounds = ref<any[]>([])
const openRounds = async (row: any) => {
  const res: any = await listInfusionRounds(row.id)
  rounds.value = res?.data || []
  roundsDlg.value = true
}

const seatClass = (s: any) => ({
  'seat-free': s.seatStatus === 1,
  'seat-busy': s.seatStatus === 2,
  'seat-disabled': s.seatStatus === 3,
})
const seatTip = (s: any) => s.seatStatus === 2
  ? `${s.patientName || ''} · ${STATUS_TEXT[s.infusionStatus] || ''}`
  : SEAT_STATUS_TEXT[s.seatStatus]

const ops = (row: any) => {
  const list: Array<{ key: string; label: string; perm: string; type: string; handler: () => void }> = []
  if (row.status === 1 && !row.skinTestId) list.push({ key: 'st', label: '打皮试', perm: 'medtech:infusion:edit', type: 'warning', handler: () => openSkinTest(row) })
  if (row.status === 1 && row.skinTestId) list.push({ key: 'judge', label: '判读', perm: 'medtech:infusion:edit', type: 'warning', handler: () => openJudge(row) })
  if (row.status === 2) list.push({ key: 'start', label: '开始', perm: 'medtech:infusion:edit', type: 'success', handler: () => openStart(row) })
  if (row.status === 3) {
    list.push({ key: 'round', label: '巡视', perm: 'medtech:infusion:edit', type: 'primary', handler: () => openRound(row) })
    list.push({ key: 'fin', label: '结束', perm: 'medtech:infusion:edit', type: 'success', handler: () => openFinish(row) })
  }
  if (row.status !== 4 && row.status !== 5) list.push({ key: 'cancel', label: '取消', perm: 'medtech:infusion:edit', type: 'danger', handler: () => openCancel(row) })
  if (row.status === 3 || row.status === 4) list.push({ key: 'rounds', label: '巡视单', perm: 'medtech:infusion:list', type: 'info', handler: () => openRounds(row) })
  return list
}
</script>

<template>
  <div>
    <el-row :gutter="12" class="stat-row">
      <el-col :span="4" v-for="kv in [
        ['待皮试', board?.pendingTest], ['待输注', board?.waiting], ['输液中', board?.infusing],
        ['已完成', board?.finished], ['已取消', board?.cancelled]]" :key="kv[0]">
        <el-card shadow="never" data-testid="ir-stat-card">
          <div class="stat-label">{{ kv[0] }}</div>
          <div class="stat-value" :data-testid="'ir-stat-' + kv[0]">{{ kv[1] ?? '—' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="block">
      <template #header>
        <div class="card-head">
          <span>座位图</span>
          <el-button v-perm="'medtech:infusion:add'" size="small" data-testid="ir-seat-add" @click="openSeatDlg()">新增座位</el-button>
        </div>
      </template>
      <div class="seat-grid">
        <el-tooltip v-for="s in seats" :key="s.id" :content="seatTip(s)" placement="top">
          <div class="seat" :class="seatClass(s)" :data-testid="'ir-seat-' + s.seatNo" @click="openAdmit(s)">
            <div class="seat-no">{{ s.seatNo }}</div>
            <div class="seat-area">{{ s.area }}</div>
            <div class="seat-status">{{ seatTip(s) }}</div>
          </div>
        </el-tooltip>
        <el-empty v-if="!seats.length" description="暂无座位，请先新增" />
      </div>
    </el-card>

    <!-- 输液台账查询（两卡式：查询卡 + 表格卡，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px" data-testid="ir-status-filter">
            <el-option v-for="(t, k) in STATUS_TEXT" :key="k" :label="t" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="患者姓名 / 单号" clearable style="width:200px" data-testid="ir-keyword" />
        </el-form-item>
        <el-form-item>
          <el-button data-testid="ir-search" @click="loadPage">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="pageData.records" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="ir-table">
        <el-table-column prop="infusionNo" label="单号" width="180" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="drugSummary" label="输注内容" min-width="160" show-overflow-tooltip />
        <el-table-column prop="seatNo" label="座位" width="90" />
        <el-table-column label="皮试" width="140">
          <template #default="{ row }">
            <span v-if="row.skinTestId">{{ row.skinTestDrug }}（{{ row.skinTestResult === 1 ? '阴性' : row.skinTestResult === 2 ? '阳性' : '待判读' }}）</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="STATUS_TAG[row.status]" data-testid="ir-row-status">{{ STATUS_TEXT[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="nurseName" label="责任护士" width="100" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-for="op in ops(row)" :key="op.key" v-perm="op.perm" link :type="op.type" size="small"
              :data-testid="'ir-op-' + op.key" @click.stop="op.handler">{{ op.label }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :page-sizes="PAGE_SIZES" :total="pageData.total" layout="total, sizes, prev, pager, next"
          data-testid="ir-pagination" @change="loadPage" />
      </div>
    </el-card>

    <!-- 座位维护 -->
    <el-dialog v-model="seatDlg" title="座位维护" width="420px">
      <el-form label-width="80px">
        <el-form-item label="座位号"><el-input v-model="seatForm.seatNo" data-testid="ir-seat-no" /></el-form-item>
        <el-form-item label="区域"><el-input v-model="seatForm.area" data-testid="ir-seat-area" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="seatForm.seatStatus" data-testid="ir-seat-status">
            <el-option label="空闲" :value="1" /><el-option label="停用" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="seatForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="ir-seat-cancel" @click="seatDlg = false">取消</el-button>
        <el-button v-perm="'medtech:infusion:add'" type="primary" data-testid="ir-seat-save" @click="saveSeat">保存</el-button>
      </template>
    </el-dialog>

    <!-- 入座 -->
    <el-dialog v-model="admitDlg" :title="`入座 ${admitForm?.seatNo || ''}`" width="480px">
      <el-form label-width="100px">
        <el-form-item label="患者">
          <PatientSelect v-model="admitForm.patientId" data-testid="ir-admit-patient" @select="onPatientSelect" />
        </el-form-item>
        <el-form-item label="输注内容">
          <el-input v-model="admitForm.drugSummary" type="textarea" :rows="2" placeholder="药名/组数摘要" data-testid="ir-admit-drug" />
        </el-form-item>
        <el-form-item label="需要皮试">
          <el-switch v-model="admitForm.needSkinTest" :active-value="1" :inactive-value="0" data-testid="ir-admit-needtest" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="admitForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="ir-admit-cancel" @click="admitDlg = false">取消</el-button>
        <el-button v-perm="'medtech:infusion:edit'" type="primary" data-testid="ir-admit-save" @click="saveAdmit">确认入座</el-button>
      </template>
    </el-dialog>

    <!-- 打皮试 -->
    <el-dialog v-model="opDlg" title="打皮试" width="420px">
      <el-form label-width="90px">
        <el-form-item label="皮试药物"><el-input v-model="opForm.drugName" data-testid="ir-skin-drug" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="opForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="ir-skin-cancel" @click="opDlg = false">取消</el-button>
        <el-button v-perm="'medtech:infusion:edit'" type="primary" data-testid="ir-skin-save" @click="saveSkinTest">确认打皮试</el-button>
      </template>
    </el-dialog>

    <!-- 巡视单 -->
    <el-dialog v-model="roundsDlg" title="巡视记录" width="640px">
      <el-table :data="rounds" data-testid="ir-rounds-table">
        <el-table-column prop="roundTime" label="时间" width="160" />
        <el-table-column prop="dripRate" label="滴速" width="80" />
        <el-table-column prop="remainingVolume" label="余量(ml)" width="100" />
        <el-table-column prop="nurseName" label="护士" width="100" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped>
.stat-row { margin-bottom: 12px; }
.stat-label { font-size: 13px; color: var(--el-text-color-secondary); }
.stat-value { font-size: 24px; font-weight: 600; margin-top: 4px; }
.block { margin-bottom: 12px; }
.card-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.seat-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.seat { width: 118px; border: 1px solid var(--el-border-color); border-radius: 8px; padding: 8px 10px; cursor: pointer; transition: box-shadow .15s; }
.seat:hover { box-shadow: var(--el-box-shadow-light); }
.seat-no { font-weight: 600; }
.seat-area { font-size: 12px; color: var(--el-text-color-secondary); }
.seat-status { font-size: 12px; margin-top: 2px; }
.seat-free { background: #f0f9eb; border-color: #b3e19d; }
.seat-busy { background: #fef0f0; border-color: #fbc4c4; cursor: not-allowed; }
.seat-disabled { background: #f4f4f5; border-color: #d3d4d6; color: var(--el-text-color-disabled); cursor: not-allowed; }
</style>
