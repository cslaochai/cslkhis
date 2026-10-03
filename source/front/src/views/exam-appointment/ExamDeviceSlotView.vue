<template>
  <div class="exam-device-slot-page" data-testid="exam-device-slot-view">
    <el-alert
      class="mb-3"
      type="info"
      :closable="false"
      show-icon
      title="检查设备档位与号源"
      description="设备档位 = 可被预约的资源：开放时段（上/下午 + 粒度 + 并行台数）与提前开放天数共同决定一天放出多少格子、每个格子多长。改开放时间或粒度不会自动重排历史格子 —— 号源一旦被占用就不允许被静默挪走，保存后按新口径生成后续日期并做一次对账。" />

    <!-- ============ 设备档位（两卡式：查询卡 + 表格卡，口径参照 views/system/user/UserView.vue） ============ -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="devQuery" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="devQuery.keyword" placeholder="设备编码/名称/机房" clearable style="width: 200px"
              @keyup.enter="devQuery.pageNum = 1; loadDevices()" />
          </el-form-item>
          <el-form-item label="类别">
            <el-select v-model="devQuery.deviceType" placeholder="类别" clearable style="width: 140px" :fit-input-width="false">
              <el-option v-for="d in deviceTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="开放状态">
            <el-select v-model="devQuery.status" placeholder="开放状态" clearable style="width: 130px" :fit-input-width="false">
              <el-option v-for="d in deviceStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="devQuery.pageNum = 1; loadDevices()">查询</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" v-perm="'medtech:examAppoint:add'" @click="openDevice()">新增设备档位</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="devRows" v-loading="devLoading" stripe :max-height="tableMaxHeight" data-testid="exam-device-table">
        <el-table-column prop="deviceCode" label="编码" width="110" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="类别" width="100">
          <template #default="{ row }">{{ row.deviceTypeText || deviceTypeText(row.deviceType) }}</template>
        </el-table-column>
        <el-table-column label="科室/机房" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.deptName }} {{ row.roomName || '' }}</template>
        </el-table-column>
        <el-table-column prop="openRangeText" label="开放时间" width="160" />
        <el-table-column label="粒度/并行" width="90" align="center">
          <template #default="{ row }">{{ row.slotMinutes }}′/{{ row.parallelCount }}</template>
        </el-table-column>
        <el-table-column label="日格子数" width="90" align="center">
          <template #default="{ row }">{{ row.dailySlots }}</template>
        </el-table-column>
        <el-table-column prop="aheadDays" label="提前天数" width="85" align="center" />
        <el-table-column label="可开展项目" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="!Number(row.itemCount)" type="warning" size="small">未配置</el-tag>
            <span v-else>{{ row.itemCount }} 项</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="deviceStatusTag(row.status) as any">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-perm="'medtech:examAppoint:edit'" @click="openItems(row)">项目</el-button>
            <el-button link type="primary" size="small" v-perm="'medtech:examAppoint:edit'" @click="slot.deviceId = row.id; slot.date = today; loadBoardPanel()">号源</el-button>
            <el-button link type="primary" size="small" v-perm="'medtech:examAppoint:edit'" @click="openDevice(row)">编辑</el-button>
            <el-button link type="danger" size="small" v-perm="'medtech:examAppoint:delete'" @click="removeDevice(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination background layout="total, prev, pager, next" :total="devTotal"
          v-model:current-page="devQuery.pageNum" v-model:page-size="devQuery.pageSize" @current-change="loadDevices" />
      </div>
    </el-card>

    <!-- ============ 号源台 ============ -->
    <el-card shadow="never">
      <div class="flex flex-wrap items-center gap-2 mb-3">
        <span class="text-sm font-medium text-gray-700">分时段号源台</span>
        <el-select v-model="slot.deviceId" placeholder="设备" filterable style="width: 220px" :fit-input-width="false"
          data-testid="slot-device" @change="loadBoardPanel">
          <el-option v-for="d in slotDevices" :key="d.id" :label="`${d.deviceName}（${d.deviceCode}）`" :value="d.id" />
        </el-select>
        <el-date-picker v-model="slot.date" type="date" value-format="YYYY-MM-DD" placeholder="日期" style="width: 140px"
          data-testid="slot-date" @change="loadBoardPanel" />
        <el-input-number v-model="slot.genDays" :min="1" :max="31" style="width: 110px" />
        <el-button :icon="Calendar" v-perm="'medtech:examAppoint:add'" @click="genSlots">生成/补齐号源</el-button>
        <el-button @click="loadBoardPanel">刷新看板</el-button>
        <el-date-picker v-model="slot.recalcTo" type="date" value-format="YYYY-MM-DD" placeholder="对账截止日" style="width: 140px" />
        <el-button type="warning" v-perm="'medtech:examAppoint:edit'" @click="runRecalc">号源对账</el-button>
      </div>
      <div v-if="slot.board" class="mb-2 text-sm text-gray-600" data-testid="slot-summary">
        {{ slot.board.deviceName }}　{{ slot.board.slotDate }}　粒度 {{ slot.board.slotMinutes }}′　并行 {{ slot.board.parallelCount }}
        <el-tag size="small" class="ml-2">总 {{ slot.board.totalSlots }}</el-tag>
        <el-tag size="small" type="success" class="ml-1">空闲 {{ slot.board.freeSlots }}</el-tag>
        <el-tag size="small" type="danger" class="ml-1">占用 {{ slot.board.usedSlots }}</el-tag>
        <el-tag size="small" type="info" class="ml-1">锁号 {{ slot.board.lockedSlots }}</el-tag>
      </div>
      <div v-loading="slot.loading" class="grid grid-cols-6 gap-2" data-testid="exam-slot-board">
        <div v-for="c in slotCells" :key="c.slotId" :class="cellClass(c)" @click="toggleSlot(c)">
          <div class="font-medium">{{ c.startTime }}-{{ c.endTime }}</div>
          <div class="text-xs">{{ slotCellState(c).label }}</div>
          <div v-if="c.occupyPatientNames?.length" class="text-xs opacity-75 truncate">{{ c.occupyPatientNames.join('、') }}</div>
        </div>
      </div>
      <div class="mt-2 text-xs text-gray-400">点击格子锁号/放号；已有占号的格子不允许锁号（避免把在办检查的患者凭空挪走）。</div>
    </el-card>

    <!-- ============ 设备档位弹窗 ============ -->
    <el-dialog v-model="devDlg.visible" :title="devDlg.form.id ? '编辑设备档位' : '新增设备档位'" width="720px" data-testid="exam-device-dialog">
      <el-form label-width="110px">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="设备编码" required><el-input v-model="devDlg.form.deviceCode" placeholder="如 EX-CT03" /></el-form-item>
          <el-form-item label="设备名称" required><el-input v-model="devDlg.form.deviceName" /></el-form-item>
          <el-form-item label="设备类别" required>
            <el-select v-model="devDlg.form.deviceType" style="width: 100%">
              <el-option v-for="d in deviceTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属科室">
            <el-select v-model="devDlg.form.deptId" placeholder="可空" clearable filterable style="width: 100%" :fit-input-width="false">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="Number(d.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="设备台账">
            <el-select v-model="devDlg.form.equipmentId" placeholder="可空（台账没建档也能先排号源）" clearable filterable style="width: 100%" :fit-input-width="false">
              <el-option v-for="e in equipOptions" :key="e.id" :label="`${e.equipmentName}（${e.equipmentCode}）`" :value="e.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="机房"><el-input v-model="devDlg.form.roomName" /></el-form-item>
          <el-form-item label="上午开始" required><el-time-picker v-model="devDlg.form.amStart" format="HH:mm" value-format="HH:mm" style="width: 100%" /></el-form-item>
          <el-form-item label="上午结束" required><el-time-picker v-model="devDlg.form.amEnd" format="HH:mm" value-format="HH:mm" style="width: 100%" /></el-form-item>
          <el-form-item label="下午开始"><el-time-picker v-model="devDlg.form.pmStart" format="HH:mm" value-format="HH:mm" style="width: 100%" /></el-form-item>
          <el-form-item label="下午结束"><el-time-picker v-model="devDlg.form.pmEnd" format="HH:mm" value-format="HH:mm" style="width: 100%" /></el-form-item>
          <el-form-item label="号源粒度(分)"><el-input-number v-model="devDlg.form.slotMinutes" :min="5" :max="240" :step="5" style="width: 100%" /></el-form-item>
          <el-form-item label="并行检查台数"><el-input-number v-model="devDlg.form.parallelCount" :min="1" :max="20" style="width: 100%" /></el-form-item>
          <el-form-item label="开放提前天数"><el-input-number v-model="devDlg.form.aheadDays" :min="1" :max="90" style="width: 100%" /></el-form-item>
          <el-form-item label="单次可占上限(分)"><el-input-number v-model="devDlg.form.maxSlotMinutes" :min="5" :max="1440" :step="5" style="width: 100%" /></el-form-item>
          <el-form-item label="开放状态">
            <el-select v-model="devDlg.form.status" style="width: 100%">
              <el-option v-for="d in deviceStatusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注"><el-input v-model="devDlg.form.remark" /></el-form-item>
        </div>
      </el-form>
      <div class="text-xs text-gray-400 px-2">
        改开放时间或粒度不会自动重排历史格子：号源一旦被占用就不允许被静默挪走，保存后请到号源台按新口径生成后续日期并做一次对账。
      </div>
      <template #footer>
        <el-button @click="devDlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="devDlg.saving" v-perm="['medtech:examAppoint:add', 'medtech:examAppoint:edit']" @click="saveDevice">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 可开展项目弹窗 ============ -->
    <el-dialog v-model="itemDlg.visible" :title="`可开展项目：${itemDlg.device?.deviceName || ''}`" width="760px">
      <div class="flex items-center gap-2 mb-2">
        <el-input v-model="itemDlg.keyword" placeholder="按项目编码/名称搜索" clearable style="width: 240px" @keyup.enter="searchCandidates" />
        <el-button :icon="Search" @click="searchCandidates">搜索项目</el-button>
        <span class="text-xs text-gray-400">保存为覆盖式：列表里没有的映射视为取消</span>
      </div>
      <div v-if="itemDlg.candidates.length" class="mb-3 border border-gray-100 rounded p-2 max-h-32 overflow-auto">
        <el-button v-for="c in itemDlg.candidates" :key="c.itemId" size="small" class="mb-1" v-perm="'medtech:examAppoint:add'" @click="addItem(c); searchCandidates()">
          + {{ c.itemName }}（{{ c.itemCode }}，{{ c.itemDictMinutes ?? '-' }}′）
        </el-button>
      </div>
      <el-table :data="itemDlg.rows" size="small" max-height="360">
        <el-table-column prop="itemCode" label="编码" width="120" />
        <el-table-column prop="itemName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="字典时长" width="90" align="right">
          <template #default="{ row }">{{ row.itemDictMinutes ?? '-' }}′</template>
        </el-table-column>
        <el-table-column label="本机时长覆盖" width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.examMinutes" :min="5" :max="1440" :step="5" size="small" controls-position="right" style="width: 130px" placeholder="留空取字典" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ $index }"><el-button link type="danger" size="small" v-perm="'medtech:examAppoint:delete'" @click="removeItem($index)">移除</el-button></template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="itemDlg.visible = false">取消</el-button>
        <el-button type="primary" :loading="itemDlg.saving" v-perm="['medtech:examAppoint:add', 'medtech:examAppoint:edit', 'medtech:examAppoint:delete']" @click="saveItems">保存（共 {{ itemDlg.rows.length }} 项）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 检查设备档位与号源（菜单 2925，sql/181）
 *
 * 2026-09 菜单治理：原先是「检查预约中心」（400 医技医辅 / 412）的第 3 个页签。
 * 预约中心回答的是「这个患者约到哪个设备哪个时段」（biz_exam_appoint，逐患者、每天高频）；
 * 本页回答的是「这台设备一天放出多少格子、每格多长、哪些格子锁掉」（biz_exam_device 档位 +
 * biz_exam_slot 号源，科室管理员低频配置）。实体不同、岗位不同、频率不同，故独立成菜单。
 *
 * 拆出后与预约中心的数据关系：
 *   - 预约侧占号仍自行调 getExamSlotBoard（占号弹窗里先补格子再看板），不依赖本页本地状态；
 *   - 预约台账的设备筛选原先借用的 slotDevices 已由预约侧自行加载（同一接口 getExamDeviceSelectList）。
 *
 * 三条口径：
 * 1. 改开放时间/粒度不会自动重排历史格子：占用过的格子不允许被静默挪走，
 *    改完必须按新口径「生成/补齐」后续日期，再做一次「号源对账」看有没有漂移。
 * 2. 锁号必须写原因（设备维护/消毒），原因留空后端不收。
 * 3. 有占号的格子不允许锁号 —— 否则在办检查的患者会被凭空挪走。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Calendar } from '@element-plus/icons-vue'
import {
  getExamDeviceListPage, getExamDeviceSelectList, getExamEquipmentOptions, getExamItemCandidates,
  examDeviceUpsert, deleteExamDevice, getExamDeviceItems, examDeviceItemSave,
  examSlotGenerate, getExamSlotBoard, examSlotToggle, examSlotRecalc,
} from '@/api/examAppointment'
import { getDictDataMapList, getDepartmentSelectList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { DEVICE_STATUS, SLOT_STATUS, deviceStatusTag, slotCellState } from '@/lib/examAppointment'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const today = (() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})()

// ---------------- 字典 / 科室 ----------------
const deviceTypeDict = ref<any[]>([])
const deviceStatusDict = ref<any[]>([])
const deptOptions = ref<any[]>([])
const deviceTypeText = (v: any) => dictLabelText(deviceTypeDict.value, v)

const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(`${DICT_TYPE.EXAM_DEVICE_TYPE},${DICT_TYPE.EXAM_DEVICE_STATUS}`)
    deviceTypeDict.value = res?.data?.[DICT_TYPE.EXAM_DEVICE_TYPE] || []
    deviceStatusDict.value = res?.data?.[DICT_TYPE.EXAM_DEVICE_STATUS] || []
  } catch (e) { console.error('加载字典失败', e) }
  try {
    const res: any = await getDepartmentSelectList()
    deptOptions.value = res?.data || []
  } catch (e) { console.error('加载科室失败', e) }
}

// ---------------- 设备档位台账 ----------------
const devLoading = ref(false)
const devRows = ref<any[]>([])
const devTotal = ref(0)
const devQuery = reactive({ keyword: '', deviceType: null as number | null, status: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const loadDevices = async () => {
  devLoading.value = true
  try {
    const res: any = await getExamDeviceListPage({
      keyword: devQuery.keyword.trim() || undefined,
      deviceType: devQuery.deviceType ?? undefined,
      status: devQuery.status ?? undefined,
      pageNum: devQuery.pageNum, pageSize: devQuery.pageSize,
    })
    if (res.code === 200) {
      devRows.value = res.data?.records || []
      devTotal.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { devLoading.value = false }
}

const equipOptions = ref<any[]>([])
const devDlg = reactive({
  visible: false, saving: false,
  form: {
    id: null as any, deviceCode: '', deviceName: '', deviceType: 1, equipmentId: null as any,
    deptId: null as any, roomName: '', amStart: '08:00', amEnd: '12:00', pmStart: '14:30', pmEnd: '17:30',
    slotMinutes: 30, parallelCount: 1, aheadDays: 7, maxSlotMinutes: 240, status: DEVICE_STATUS.OPEN, remark: '',
  },
})
const openDevice = async (row?: any) => {
  if (!equipOptions.value.length) {
    try {
      const res: any = await getExamEquipmentOptions()
      equipOptions.value = res?.data || []
    } catch (e) { console.error(e) }
  }
  Object.assign(devDlg.form, {
    id: row?.id || null, deviceCode: row?.deviceCode || '', deviceName: row?.deviceName || '',
    deviceType: row?.deviceType || 1, equipmentId: row?.equipmentId || null, deptId: row?.deptId || null,
    roomName: row?.roomName || '', amStart: row?.amStart || '08:00', amEnd: row?.amEnd || '12:00',
    pmStart: row?.pmStart || '14:30', pmEnd: row?.pmEnd || '17:30',
    slotMinutes: row?.slotMinutes ?? 30, parallelCount: row?.parallelCount ?? 1,
    aheadDays: row?.aheadDays ?? 7, maxSlotMinutes: row?.maxSlotMinutes ?? 240,
    status: row?.status ?? DEVICE_STATUS.OPEN, remark: row?.remark || '',
  })
  devDlg.visible = true
}
const saveDevice = async () => {
  const f = devDlg.form
  if (!f.deviceCode.trim() || !f.deviceName.trim()) { ElMessage.warning('设备编码与名称必填'); return }
  if (!f.deviceType) { ElMessage.warning('请选择设备类别'); return }
  devDlg.saving = true
  try {
    const deptName = f.deptId ? (deptOptions.value.find((d) => String(d.id) === String(f.deptId))?.deptName || '') : ''
    const res: any = await examDeviceUpsert({
      id: f.id || undefined, deviceCode: f.deviceCode.trim(), deviceName: f.deviceName.trim(),
      deviceType: f.deviceType, equipmentId: f.equipmentId || undefined,
      deptId: f.deptId || undefined, deptName: deptName || undefined, roomName: f.roomName.trim() || undefined,
      amStart: f.amStart, amEnd: f.amEnd, pmStart: f.pmStart || undefined, pmEnd: f.pmEnd || undefined,
      slotMinutes: f.slotMinutes, parallelCount: f.parallelCount, aheadDays: f.aheadDays,
      maxSlotMinutes: f.maxSlotMinutes, status: f.status, remark: f.remark.trim() || undefined,
    })
    if (res.code === 200) {
      ElMessage({ type: res.data?.warning ? 'warning' : 'success', message: res.message || '已保存', duration: 6000 })
      devDlg.visible = false
      await Promise.all([loadDevices(), loadSlotDevices()])
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { devDlg.saving = false }
}
const removeDevice = async (row: any) => {
  try {
    await ElMessageBox.confirm(`删除设备档位「${row.deviceName}」？历史号源格子会一并失效。`, '删除设备', { type: 'warning' })
    const res: any = await deleteExamDevice(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '已删除')
      await Promise.all([loadDevices(), loadSlotDevices()])
    } else ElMessage.error(res.message || '删除失败')
  } catch (e) { /* 用户放弃 */ }
}

// ---------------- 可开展项目 ----------------
const itemDlg = reactive({
  visible: false, saving: false, device: null as any,
  rows: [] as any[],            // 已配置：{itemId,itemCode,itemName,itemDictMinutes,examMinutes}
  keyword: '', candidates: [] as any[],
})
const openItems = async (row: any) => {
  itemDlg.device = row
  itemDlg.rows = []; itemDlg.candidates = []; itemDlg.keyword = ''
  itemDlg.visible = true
  try {
    const res: any = await getExamDeviceItems(row.id)
    if (res.code === 200) itemDlg.rows = (res.data || []).map((i: any) => ({ ...i }))
    else ElMessage.error(res.message || '加载项目失败')
  } catch (e) { console.error(e); ElMessage.error('加载项目失败') }
}
const searchCandidates = async () => {
  try {
    const res: any = await getExamItemCandidates({ keyword: itemDlg.keyword.trim() || undefined, limit: 20 })
    if (res.code === 200) {
      const owned = new Set(itemDlg.rows.map((r) => String(r.itemId)))
      itemDlg.candidates = (res.data || []).filter((c: any) => !owned.has(String(c.itemId)))
    } else ElMessage.error(res.message || '搜索失败')
  } catch (e) { console.error(e); ElMessage.error('搜索失败') }
}
const addItem = (c: any) => {
  itemDlg.rows.push({ itemId: c.itemId, itemCode: c.itemCode, itemName: c.itemName, itemDictMinutes: c.itemDictMinutes, examMinutes: null })
}
const removeItem = (idx: number) => { itemDlg.rows.splice(idx, 1) }
const saveItems = async () => {
  itemDlg.saving = true
  try {
    const res: any = await examDeviceItemSave({
      deviceId: itemDlg.device.id,
      items: itemDlg.rows.map((r) => ({ itemId: r.itemId, examMinutes: r.examMinutes ?? undefined })),
    })
    if (res.code === 200) { ElMessage.success(res.message || '已保存'); itemDlg.visible = false; loadDevices() }
    else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e); ElMessage.error('保存失败') } finally { itemDlg.saving = false }
}

// ---------------- 号源台 ----------------
const slot = reactive({
  deviceId: null as any, date: today, loading: false, board: null as any,
  genDays: 1, recalcTo: '',
})
const slotDevices = ref<any[]>([])
const slotCells = computed<any[]>(() => slot.board?.slots || [])

const loadSlotDevices = async () => {
  try {
    const res: any = await getExamDeviceSelectList({})
    if (res.code === 200) {
      slotDevices.value = res.data || []
      if (!slot.deviceId && slotDevices.value.length) {
        slot.deviceId = slotDevices.value[0].id
        loadBoardPanel()
      }
    }
  } catch (e) { console.error(e) }
}
const loadBoardPanel = async () => {
  if (!slot.deviceId || !slot.date) { slot.board = null; return }
  slot.loading = true
  try {
    const res: any = await getExamSlotBoard({ deviceId: slot.deviceId, slotDate: slot.date })
    if (res.code === 200) slot.board = res.data
    else ElMessage.error(res.message || '加载号源失败')
  } catch (e) { console.error(e); ElMessage.error('加载号源失败') } finally { slot.loading = false }
}
const genSlots = async () => {
  if (!slot.deviceId) { ElMessage.warning('请选择设备'); return }
  try {
    const res: any = await examSlotGenerate({ deviceId: slot.deviceId, startDate: slot.date, days: slot.genDays })
    if (res.code === 200) { ElMessage.success(res.data?.message || res.message || '号源已生成'); loadBoardPanel() }
    else ElMessage.error(res.message || '生成失败')
  } catch (e) { console.error(e); ElMessage.error('生成失败') }
}
const toggleSlot = async (cell: any) => {
  const to = Number(cell.status) === SLOT_STATUS.LOCKED ? SLOT_STATUS.NORMAL : SLOT_STATUS.LOCKED
  let reason = ''
  if (to === SLOT_STATUS.LOCKED) {
    try {
      const { value } = await ElMessageBox.prompt(`锁定 ${cell.startTime}-${cell.endTime}：设备维护/消毒等原因（必填）`, '锁号', {
        inputValidator: (v: string) => (v && v.trim() ? true : '锁号原因不能为空'),
      })
      reason = value.trim()
    } catch { return }
  }
  const res: any = await examSlotToggle({ slotId: cell.slotId, status: to, reason: reason || undefined }).catch((e: any) => ({ message: e?.message }))
  if (res.code === 200) ElMessage.success(res.message || '已变更')
  else ElMessage.error(res.message || '操作失败')
  loadBoardPanel()
}
const runRecalc = async () => {
  const from = slot.date
  const to = slot.recalcTo || from
  try {
    const res: any = await examSlotRecalc({ deviceId: slot.deviceId || undefined, dateFrom: from, dateTo: to })
    if (res.code === 200) {
      const d = res.data
      ElMessage({ type: Number(d?.drifted || 0) ? 'warning' : 'success', message: d?.message || `对账完成：检查 ${d?.checked} 格，漂移 ${d?.drifted} 格`, duration: 6000 })
      loadBoardPanel()
    } else ElMessage.error(res.message || '对账失败')
  } catch (e) { console.error(e); ElMessage.error('对账失败') }
}

/** 号源台没有"被选中待占号"的格子（那是预约侧的事），只按格子自身状态着色 */
const cellClass = (cell: any) => {
  const st = slotCellState(cell)
  if (st.key === 'free') return 'cell cell-free'
  if (st.key === 'full') return 'cell cell-full'
  if (st.key === 'locked') return 'cell cell-locked'
  return 'cell cell-past'
}

onMounted(() => {
  loadDicts(); loadDevices(); loadSlotDevices()
})
</script>

<style scoped>
.cell {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 6px 8px;
  cursor: default;
  background: #fff;
  line-height: 1.4;
}
.cell-free { cursor: pointer; border-color: #bbe3de; background: #f2fbfa; }
.cell-free:hover { border-color: #0e9488; }
.cell-full { background: #fdf1f1; border-color: #f2c3c3; color: #b91c1c; }
.cell-locked { background: #f3f4f6; border-color: #d1d5db; color: #6b7280; }
.cell-past { background: #fafafa; border-color: #ececec; color: #9ca3af; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
