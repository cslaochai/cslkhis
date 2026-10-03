<script setup lang="ts">
/**
 * 药房发药窗口
 *
 * G10 增量（麻精药品特殊管理，评审一票否决项）：
 * 1. **列表带管制分类标签**（`specialFlag` 由后端按药品主数据补齐）——
 *    药师在点「发药」之前就能看出这行是不是麻精，而不是发完才知道。
 * 2. **发药前预检 + 双人复核弹窗**：麻醉药品 / 第一类精神药品必须指定复核药师，
 *    且不得与发药人同一人（《医疗机构麻醉药品、第一类精神药品管理规定》第17条）。
 *    预检（`/narcotic/precheck`）会把「能不能发、为什么不能、每个管制品种限几日」一次说清，
 *    避免"点一次、被拒一次、再猜哪里错"。
 * 3. **超量理由**：第二类精神药品超 7 日须由医师注明理由后才放行（《处方管理办法》第24条），
 *    理由随发药写进专册留痕。
 *
 * ⚠ 这三条在前端都只是**提前告知**，真正的闸门在 his-emr 的 NarcoticControlService：
 * 前端算错只是提示不准，后端漏判才是合规事故。所以这里不"筛掉"任何超限行，
 * 一律把后端的结论原样摆出来。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Check, Clock, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { getDispensingList, getDispensingDetail, dispense, dispenseByPrescription, returnDrug } from '@/api/dispensing'
import { precheckNarcotic } from '@/api/narcotic'
import { getEmployeeList, getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { specialFlagText, specialFlagTagType, isControlledFlag } from '@/lib/drugSpecialFlag'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

const loading = ref(false)
const dispensingList = ref<any[]>([])
const selectedRecord = ref<any>(null)
const detailLoading = ref(false)
const detailData = ref<any>(null)

const patientName = ref('')
const prescriptionNo = ref('')
const statusFilter = ref<number | null>(null)

const pagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

// 三态口径：1-待发药 2-已发药 3-已退药（表注释为准；未知值渲染 未知(n) 不回落）
const statusMap: Record<number, { label: string; color: string }> = {
  1: { label: '待发药', color: 'bg-amber-100 text-amber-700' },
  2: { label: '已发药', color: 'bg-blue-100 text-blue-700' },
  3: { label: '已退药', color: 'bg-red-100 text-red-600' },
}

const statusCounts = ref({ pending: 0, dispensed: 0, returned: 0 })

// ---------------- 麻精相关 ----------------
/** 管制分类文案唯一来源：字典 his_drug_special_flag */
const specialDict = ref<any[]>([])
/** 复核药师候选。当前登录人的姓名在 localStorage（登录/切换时写入），
 *  用它做前端软过滤；**硬判定在后端**（按员工ID 比，见 resolveAndAssertChecker）。 */
const checkerOptions = ref<any[]>([])
const currentUserName = ref(localStorage.getItem('realName') || '')

const loadNarcoticSupport = async () => {
  try {
    // ⚠ 取值必须是 `res.data[dictType]`：request 拦截器已把响应剥成 `{code,message,data}`，
    //    而 getDictDataMapList 又包了一层 `{...res, data: {dictType: [...]}}`。
    //    少写这层 `.data` 不报错，但 `specialDict` 恒空 → 列表里的管制分类标签全渲染「未知(n)」。
    //    "麻精药品不能和维生素C长得一样"就靠这个标签，静默失效等于把管制提示抹掉了。
    const res: any = await getDictDataMapList(DICT_TYPE.DRUG_SPECIAL_FLAG)
    specialDict.value = res?.data?.[DICT_TYPE.DRUG_SPECIAL_FLAG] || []
  } catch (e) {
    // 拿不到字典 → 分类渲染「未知(n)」，不回落成"普通药品"（普通药品不受管制，回落会洗掉告警）
    console.error('加载药品特殊管理分类字典失败', e)
  }
  try {
    // empType=4（药剂师）**下推后端**，不在前端对全量员工切片 ——
    // 法条要求复核人"具有药师以上技术职称"，护士/收费员不能当麻精复核人。
    // 前端这层只是省得药师选错人；后端 resolveAndAssertChecker 仍按员工ID 再判一次。
    const res: any = await getEmployeeList({ empType: 4 })
    const list: any[] = res.data?.records || res.data || []
    // 同一人不能给自己复核：登录时把 realName 写进了 localStorage，用它做前端软过滤
    // （/auth/info 不带 employeeId，前端拿不到自己的员工ID，所以只能按姓名滤）
    checkerOptions.value = list.filter((e: any) => e.status === 1 && e.empName !== currentUserName.value)
  } catch (e) {
    console.error('加载复核药师候选失败', e)
  }
}

/** 管制分类标签（空值/未维护 → 不显示标签，而不是显示「普通」） */
const rowFlagText = (flag: number | null | undefined) => specialFlagText(flag, specialDict.value)
const rowFlagTag = (flag: number | null | undefined) => specialFlagTagType(flag) || 'info'

const loadStatusCount = async () => {
  try {
    const res = await request.get('/charge/dispensing/statusCount')
    if (res.data) statusCounts.value = res.data
  } catch (error: any) {
    console.error('加载发药统计失败:', error)
  }
}

// ---------------- 发药复核弹窗（单行 / 整单共用） ----------------
const reviewVisible = ref(false)
const reviewLoading = ref(false)
const reviewSubmitting = ref(false)
/** 待执行的发药动作：整单会带 row.prescriptionId，单行会用 row.id */
const pendingAction = ref<{ type: 'one' | 'whole'; row: any } | null>(null)
const reviewForm = reactive({
  checkerId: null as any,
  overLimitReason: '',
  precheck: null as any,
})

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    }
    if (patientName.value.trim()) params.patientName = patientName.value.trim()
    if (prescriptionNo.value.trim()) params.prescriptionNo = prescriptionNo.value.trim()
    if (statusFilter.value != null) params.dispensingStatus = statusFilter.value
    const res = await getDispensingList(params)
    dispensingList.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error: any) {
    console.error('加载发药列表失败:', error)
    ElMessage.error(error?.message || '加载发药列表失败')
  } finally {
    loading.value = false
  }
}

const refreshAll = () => {
  loadData()
  loadStatusCount()
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  patientName.value = ''
  prescriptionNo.value = ''
  statusFilter.value = null
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

const viewDetail = async (row: any) => {
  selectedRecord.value = row
  detailLoading.value = true
  try {
    const res = await getDispensingDetail(row.id)
    detailData.value = res.data
  } catch (error: any) {
    console.error('加载详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

/**
 * 发药统一入口（单行 / 整单）：
 *   预检 → 摆结论 → （麻精）选复核药师 /（二类精神超限）填超量理由 → 提交。
 *
 * 单行与整单共用一套弹窗，因为两者的判定完全一致（都按处方整体判限量），
 * 差别只在最后调哪个接口。分成两套弹窗只会让口径跑偏。
 */
const startDispense = async (row: any, whole: boolean) => {
  if (!row.prescriptionId) {
    ElMessage.warning('该发药记录未关联处方，无法校验')
    return
  }
  pendingAction.value = { type: whole ? 'whole' : 'one', row }
  reviewForm.precheck = null
  reviewForm.checkerId = null
  reviewForm.overLimitReason = ''
  reviewVisible.value = true
  reviewLoading.value = true
  try {
    const res: any = await precheckNarcotic(row.prescriptionId)
    reviewForm.precheck = res.data
  } catch (e: any) {
    // 预检接口本身失败（网络/权限）不能当成"可以发"——直接关掉弹窗，让药师重试
    ElMessage.error(e?.message || '麻精预检失败，请稍后重试')
    reviewVisible.value = false
  } finally {
    reviewLoading.value = false
  }
}

/** 弹窗内结论 */
const pc = computed(() => reviewForm.precheck || null)
const blocks = computed(() => (pc.value?.violations || []).filter((v: any) => v.level === 'BLOCK'))
const warns = computed(() => (pc.value?.violations || []).filter((v: any) => v.level !== 'BLOCK'))

/** 提交条件：无阻断、需复核则必须选人、只差超量理由则必须填理由 */
const canSubmitReview = computed(() => {
  const p = pc.value
  if (!p) return false
  if (!p.canDispense && !p.overLimitReasonRequired) return false
  if (p.requiresDualCheck && !reviewForm.checkerId) return false
  if (p.overLimitReasonRequired && !reviewForm.overLimitReason.trim()) return false
  return true
})

const reviewMissingHint = computed(() => {
  const p = pc.value
  if (!p) return ''
  if (!p.canDispense && !p.overLimitReasonRequired) return '存在禁止发药的问题，请先修正处方'
  if (p.requiresDualCheck && !reviewForm.checkerId) return '本处方含麻醉药品 / 第一类精神药品，必须选择复核药师'
  if (p.overLimitReasonRequired && !reviewForm.overLimitReason.trim()) return '第二类精神药品超 7 日，须填写医师注明理由'
  return ''
})

const submitDispense = async () => {
  const p = pc.value
  const action = pendingAction.value
  if (!p || !action) return
  const reason = reviewForm.overLimitReason.trim()
  reviewSubmitting.value = true
  try {
    // 填了理由就带理由重检一次：由后端确认"加了理由确实可发"。
    // 不在前端推断而直接发 —— 前端推断错的话，专册里会留下一笔没有依据的超量发药。
    if (reason) {
      const again: any = await precheckNarcotic(p.prescriptionId, reason)
      const pc2 = again.data
      if (!pc2?.canDispense) {
        reviewForm.precheck = pc2
        ElMessage.error('填写理由后仍存在不可放行的限量问题，请先修正处方')
        return
      }
    }
    const checkerId = reviewForm.checkerId || undefined
    if (action.type === 'whole') {
      await dispenseByPrescription(action.row.prescriptionId, checkerId, reason || undefined)
      ElMessage.success('整单发药成功')
    } else {
      await dispense({ id: action.row.id, checkerId, overLimitReason: reason || undefined })
      ElMessage.success('发药成功，库存已扣减')
    }
    reviewVisible.value = false
    refreshAll()
  } catch (e: any) {
    // 后端闸门的原始结论直接摆出来（含"哪个药、限几日、超几日"），不替换成笼统措辞
    ElMessage.error(e?.message || '发药失败')
  } finally {
    reviewSubmitting.value = false
  }
}

// 退药（回库存 + 处方状态联动）
const handleReturn = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入退药原因', `退药 - ${row.drugName}`, {
      confirmButtonText: '确认退药',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：患者过敏 / 开错药',
      inputValidator: (v: string) => (v && v.trim() ? true : '退药原因不能为空'),
    })
    await returnDrug(row.id, value.trim())
    ElMessage.success('退药成功，库存已回加')
    refreshAll()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '退药失败')
    }
  }
}

const statusLabel = (s: number | null | undefined) => {
  if (s == null) return '未知'
  return statusMap[s]?.label || `未知(${s})`
}
const statusColor = (s: number | null | undefined) => statusMap[s as number]?.color || 'bg-slate-100 text-slate-500'

onMounted(() => {
  loadNarcoticSupport()
  refreshAll()
})
</script>

<template>
  <div>
    <!-- 统计卡片（真实全库计数） -->
    <div class="mb-3 grid grid-cols-3 gap-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5"><Clock class="h-5 w-5 text-amber-600" /></div>
          <div>
            <p class="text-lg font-bold text-amber-600">{{ statusCounts.pending }}</p>
            <p class="text-xs text-slate-500">待发药（全部）</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-blue-50 p-2.5"><Check class="h-5 w-5 text-blue-600" /></div>
          <div>
            <p class="text-lg font-bold text-blue-600">{{ statusCounts.dispensed }}</p>
            <p class="text-xs text-slate-500">已发药（全部）</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-red-50 p-2.5"><RefreshLeft class="h-5 w-5 text-red-500" /></div>
          <div>
            <p class="text-lg font-bold text-red-500">{{ statusCounts.returned }}</p>
            <p class="text-xs text-slate-500">已退药（全部）</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 查询卡（真实传参） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="{ patientName, prescriptionNo, statusFilter }" inline @submit.prevent>
        <el-form-item label="患者姓名">
          <el-input v-model="patientName" placeholder="患者姓名" :prefix-icon="Search" class="!w-52" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="处方号">
          <el-input v-model="prescriptionNo" placeholder="处方号" :prefix-icon="Search" class="!w-56" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="发药状态">
          <el-select v-model="statusFilter" placeholder="全部" clearable class="!w-36" @change="handleSearch">
            <el-option label="待发药" :value="1" />
            <el-option label="已发药" :value="2" />
            <el-option label="已退药" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡：发药明细列表（每行 = 药品 × 处方） -->
    <el-card class="table-card" shadow="never">
      <el-table :data="dispensingList" v-loading="loading" stripe :max-height="tableMaxHeight" style="width: 100%" @row-click="viewDetail">
        <el-table-column prop="patientName" label="患者" width="110" />
        <el-table-column prop="prescriptionNo" label="处方号" width="180" class-name="font-mono" />
        <el-table-column prop="drugName" label="药品" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
              v-if="isControlledFlag(row.specialFlag)"
              :type="rowFlagTag(row.specialFlag)"
              size="small"
              effect="dark"
              class="mr-1"
              data-testid="pharm-flag-tag"
            >{{ rowFlagText(row.specialFlag) }}</el-tag>
            <span>{{ row.drugName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" width="110" show-overflow-tooltip />
        <el-table-column label="数量" width="90" align="center">
          <template #default="{ row }">{{ row.quantity }}{{ row.unit || '' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="90" align="right">
          <template #default="{ row }">
            <span class="font-medium text-red-600">¥{{ row.amount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 font-medium', statusColor(row.dispensingStatus)]">
              {{ statusLabel(row.dispensingStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="pharmacistName" label="发药药师" width="100" />
        <el-table-column prop="dispensingTime" label="发药时间" width="160" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <template v-if="row.dispensingStatus === 1">
              <el-button v-perm="'pharmacy:dispensing:edit'" type="primary" link :icon="Check" data-testid="pharm-dispense-btn" @click.stop="startDispense(row, false)">发药</el-button>
              <el-button v-perm="'pharmacy:dispensing:edit'" type="primary" plain link data-testid="pharm-dispense-all-btn" @click.stop="startDispense(row, true)">整单发药</el-button>
            </template>
            <el-button v-else-if="row.dispensingStatus === 2" v-perm="'pharmacy:dispensing:edit'" type="warning" link @click.stop="handleReturn(row)">退药</el-button>
            <span v-else class="text-slate-400">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="dispensingList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无发药记录
      </div>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 发药复核弹窗（单行 / 整单共用；麻精限量 + 双人复核 + 超量理由） -->
    <el-dialog
      v-model="reviewVisible"
      :title="pendingAction?.type === 'whole' ? '整单发药复核' : '发药复核'"
      width="720px"
      destroy-on-close
      data-testid="pharm-review-dialog"
    >
      <div v-loading="reviewLoading" class="min-h-[120px] space-y-4 text-sm">
        <template v-if="pc">
          <!-- 待发信息 -->
          <div class="rounded-lg bg-slate-50 p-3">
            <div>患者：<span class="font-medium">{{ pendingAction?.row?.patientName }}</span></div>
            <div>处方号：<span class="font-mono">{{ pc.prescriptionNo || pendingAction?.row?.prescriptionNo }}</span></div>
            <div v-if="pendingAction?.type === 'one'">
              本次发出：{{ pendingAction?.row?.drugName }} × {{ pendingAction?.row?.quantity }}{{ pendingAction?.row?.unit || '' }}
            </div>
            <div v-else>整单发出该处方全部待发药品（任一药品库存不足则整单不执行）</div>
          </div>

          <!-- 违规结论：BLOCK 红、WARN 黄。文案来自后端，不在这里重写措辞 -->
          <div v-if="blocks.length" class="rounded-lg border border-rose-200 bg-rose-50 p-3" data-testid="pharm-block-list">
            <div class="mb-1 font-medium text-rose-700">禁止发药（{{ blocks.length }} 条）</div>
            <ul class="space-y-1">
              <li v-for="(v, i) in blocks" :key="'b' + i" class="flex gap-2 text-rose-700">
                <el-tag type="danger" size="small" effect="dark">禁止</el-tag>
                <span>{{ v.message }}</span>
              </li>
            </ul>
          </div>
          <div v-if="warns.length" class="rounded-lg border border-amber-200 bg-amber-50 p-3" data-testid="pharm-warn-list">
            <div class="mb-1 font-medium text-amber-700">提示（{{ warns.length }} 条）</div>
            <ul class="space-y-1">
              <li v-for="(v, i) in warns" :key="'w' + i" class="flex gap-2 text-amber-700">
                <el-tag type="warning" size="small">提示</el-tag>
                <span>{{ v.message }}</span>
              </li>
            </ul>
          </div>
          <div
            v-if="!pc.violations || pc.violations.length === 0"
            class="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-emerald-700"
            data-testid="pharm-pass"
          >麻精限量校验通过</div>

          <!-- 管制明细清单：合规的也列出来，药师才知道这单里的管制品种 -->
          <div v-if="pc.controlledDrugs?.length" class="rounded-lg border border-slate-200 p-3" data-testid="pharm-controlled-list">
            <div class="mb-2 font-medium text-slate-700">本处方管制品种（{{ pc.controlledDrugs.length }}）</div>
            <el-table :data="pc.controlledDrugs" size="small" border>
              <el-table-column prop="drugName" label="药品" min-width="150" show-overflow-tooltip />
              <el-table-column prop="dosageForm" label="剂型" width="100" />
              <el-table-column label="分类" width="120">
                <template #default="{ row }">
                  <el-tag :type="rowFlagTag(row.specialFlag)" size="small" effect="dark">{{ rowFlagText(row.specialFlag) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="限量/实际" width="110" align="center">
                <template #default="{ row }">
                  <span :class="row.actualDays != null && row.limitDays != null && row.actualDays > row.limitDays ? 'font-medium text-rose-600' : ''">
                    {{ row.limitDays ?? '—' }} / {{ row.actualDays ?? '核不出' }} 日
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="发药手续" min-width="170">
                <template #default="{ row }">
                  <el-tag v-if="row.requiresDualCheck" type="danger" size="small" class="mr-1">双人复核</el-tag>
                  <el-tag v-if="row.requiresAmpouleTracking" type="warning" size="small">空安瓿回收</el-tag>
                  <span v-if="!row.requiresDualCheck && !row.requiresAmpouleTracking" class="text-xs text-slate-400">常规</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-form label-width="120px">
            <!-- 双人复核：麻醉药品 / 第一类精神药品 -->
            <el-form-item v-if="pc.requiresDualCheck" label="复核药师" required>
              <el-select
                v-model="reviewForm.checkerId"
                data-testid="pharm-checker-select"
                placeholder="请选择在场复核药师"
                filterable
                clearable
                class="!w-72"
                :fit-input-width="false"
              >
                <el-option v-for="e in checkerOptions" :key="e.id" :label="e.empName" :value="e.id" />
              </el-select>
              <div class="mt-1 text-xs text-rose-600" data-testid="pharm-dual-hint">
                本处方含麻醉药品 / 第一类精神药品 / 毒性药品，调配须双人复核
                （《医疗机构麻醉药品、第一类精神药品管理规定》第 17 条、《医疗用毒性药品管理办法》第 9 条）。
                复核人须为在职、具有药师以上技术职称者，且不得与发药人同一人；
                名单已下推后端按药剂师岗位过滤并排除当前登录人，后端会再按员工 ID 校验一次。
              </div>
            </el-form-item>

            <!-- 二类精神药品超 7 日：须医师注明理由 -->
            <el-form-item v-if="pc.overLimitReasonRequired" label="超量理由" required>
              <el-input
                v-model="reviewForm.overLimitReason"
                data-testid="pharm-reason-input"
                type="textarea"
                :rows="2"
                placeholder="第二类精神药品超 7 日常用量，须由医师注明理由（《处方管理办法》第 24 条）"
              />
            </el-form-item>

            <el-form-item v-if="pc.requiresAmpouleTracking" label="空安瓿">
              <span class="text-xs text-slate-500">
                本处方含麻醉/第一类精神药品注射剂 —— 发药后会自动进麻精专册并置为「待回收」，
                请在专册页登记空安瓿回收与剩余液销毁。
              </span>
            </el-form-item>
          </el-form>
        </template>
      </div>
      <template #footer>
        <span v-if="reviewMissingHint" class="mr-auto align-middle text-xs text-rose-600" data-testid="pharm-review-hint">{{ reviewMissingHint }}</span>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button
          v-perm="'pharmacy:dispensing:edit'"
          type="primary"
          :loading="reviewSubmitting"
          :disabled="!canSubmitReview || reviewLoading"
          data-testid="pharm-review-submit"
          @click="submitDispense"
        >{{ pendingAction?.type === 'whole' ? '确认整单发药' : '确认发药' }}</el-button>
      </template>
    </el-dialog>

    <!-- 发药详情弹窗 -->
    <el-dialog v-model="selectedRecord" title="发药明细详情" width="620px" destroy-on-close>
      <template v-if="detailData">
        <div v-loading="detailLoading" class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
            <div><p class="text-xs text-slate-400">患者</p><p class="text-sm font-medium text-slate-700">{{ detailData.patientName }}（{{ detailData.patientNo }}）</p></div>
            <div><p class="text-xs text-slate-400">处方号</p><p class="font-mono text-sm font-medium text-slate-700">{{ detailData.prescriptionNo }}</p></div>
            <div><p class="text-xs text-slate-400">药品</p><p class="text-sm font-medium text-slate-700">{{ detailData.drugName }}</p></div>
            <div><p class="text-xs text-slate-400">管制分类</p>
              <p class="text-sm font-medium">
                <el-tag
                  v-if="isControlledFlag(detailData.specialFlag)"
                  :type="rowFlagTag(detailData.specialFlag)"
                  size="small"
                  effect="dark"
                >{{ rowFlagText(detailData.specialFlag) }}</el-tag>
                <span v-else class="text-slate-500">普通药品</span>
              </p>
            </div>
            <div><p class="text-xs text-slate-400">规格</p><p class="text-sm font-medium text-slate-700">{{ detailData.specification || '—' }}</p></div>
            <div><p class="text-xs text-slate-400">数量</p><p class="text-sm font-medium text-slate-700">{{ detailData.quantity }}{{ detailData.unit || '' }}</p></div>
            <div><p class="text-xs text-slate-400">金额</p><p class="text-sm font-bold text-red-600">¥{{ detailData.amount ?? 0 }}</p></div>
            <div><p class="text-xs text-slate-400">状态</p>
              <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', statusColor(detailData.dispensingStatus)]">
                {{ statusLabel(detailData.dispensingStatus) }}
              </span>
            </div>
            <div><p class="text-xs text-slate-400">发药单号</p><p class="font-mono text-sm font-medium text-slate-700">{{ detailData.dispensingNo }}</p></div>
            <div><p class="text-xs text-slate-400">发药药师</p><p class="text-sm font-medium text-slate-700">{{ detailData.pharmacistName || '—' }}</p></div>
            <div><p class="text-xs text-slate-400">发药时间</p><p class="text-sm font-medium text-slate-700">{{ detailData.dispensingTime || '—' }}</p></div>
            <div><p class="text-xs text-slate-400">发药前库存（该药合计）</p><p class="text-sm font-medium text-slate-700">{{ detailData.stockBefore ?? '—' }}</p></div>
            <div><p class="text-xs text-slate-400">发药后库存（该药合计）</p><p class="text-sm font-medium text-slate-700">{{ detailData.stockAfter ?? '—' }}</p></div>
            <div v-if="detailData.remark" class="col-span-2"><p class="text-xs text-slate-400">备注/退药原因</p><p class="text-sm text-slate-700">{{ detailData.remark }}</p></div>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
