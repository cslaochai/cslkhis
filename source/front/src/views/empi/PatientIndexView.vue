<script setup lang="ts">
/**
 * 患者主索引（EMPI / P5.1）
 *
 * 这个页面对应「临床数据整合」这个功能角色：把多来源建档产生的重复档案找出来，
 * 由人确认后建立主索引关系。
 *
 * 三条必须写在页面上的口径（否则使用者会误以为系统会自动帮他合并）：
 *   - 这里**不做自动合并**，只把可疑组合摆出来让人判断；
 *   - 合并**不搬业务数据**，只是让系统知道"这两份是同一个人"；
 *   - 合并**可撤销**，所以每次合并都要求写清依据。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Link, View, RefreshLeft, WarnTriangleFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import {
  getPatientIndexList,
  getDuplicateGroups,
  getPatientIndexDetail,
  mergePatientIndex,
  revertPatientMerge,
  getMergeLogList,
  getPatientIndexStats,
  getPatientIndexDict,
} from '@/api/patientIndex'

const activeTab = ref('duplicate')
const loading = ref(false)

// ---------- 字典 ----------
const levelMap = ref<Record<number, { text: string; strong: boolean; minReasonLength: number }>>({})
const profileFields = ref<Record<string, string>>({})
const loadDict = async () => {
  const res = await getPatientIndexDict()
  const map: Record<number, { text: string; strong: boolean; minReasonLength: number }> = {}
  ;(res.data?.matchLevels || []).forEach((l: any) => {
    map[l.code] = { text: l.text, strong: l.strong, minReasonLength: l.minReasonLength }
  })
  levelMap.value = map
  profileFields.value = res.data?.profileFields || {}
}

// ---------- 概览 ----------
const stats = ref<any>({})
const loadStats = async () => {
  const res = await getPatientIndexStats()
  stats.value = res.data || {}
}

// ---------- 疑似重复检测 ----------
const groups = ref<any[]>([])
const loadDuplicates = async () => {
  loading.value = true
  try {
    const res = await getDuplicateGroups({ keyword: dupKeyword.value || undefined })
    groups.value = res.data || []
  } finally {
    loading.value = false
  }
}
const dupKeyword = ref('')

const levelTagType = (level: number) =>
  level === 1 ? 'danger' : level === 2 ? 'warning' : level === 3 ? 'info' : 'info'

// ---------- 档案列表 ----------
const listQuery = reactive({ keyword: '', includeShadow: false, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const rows = ref<any[]>([])
const total = ref(0)
const loadList = async () => {
  loading.value = true
  try {
    const res = await getPatientIndexList({ ...listQuery })
    rows.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

// ---------- 合并历史 ----------
const logs = ref<any[]>([])
const logQuery = reactive({ keyword: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const logTotal = ref(0)
const loadLogs = async () => {
  loading.value = true
  try {
    const res = await getMergeLogList({ ...logQuery })
    logs.value = res.data?.records || []
    logTotal.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

// ---------- 合并弹窗 ----------
const mergeVisible = ref(false)
const mergeSubmitting = ref(false)
const mergeForm = reactive({
  masterId: '',
  mergedId: '',
  reason: '',
  fillBlank: false,
})
const currentGroup = ref<any>(null)

const memberLabel = (m: any) =>
  `${m.patientNo} ${m.patientName}（${m.genderText || '—'}${m.age != null ? ' ' + m.age + '岁' : ''}｜数据 ${m.totalDataCount} 条｜档案完整度 ${m.completeRate}%）`

const openMerge = (group: any, masterId?: string, mergedId?: string) => {
  currentGroup.value = group
  const members = group.members || []
  // 默认主档 = 数据量最大的那份（数据在谁名下，就保留谁）
  mergeForm.masterId = masterId || (members[0] ? String(members[0].id) : '')
  const others = members.filter((m: any) => String(m.id) !== mergeForm.masterId)
  mergeForm.mergedId = mergedId || (others[0] ? String(others[0].id) : '')
  mergeForm.reason = ''
  mergeForm.fillBlank = false
  mergeVisible.value = true
}

const currentLevel = computed(() => {
  const g = currentGroup.value
  if (!g) return null
  // 选定的两方之间的真实级别以服务端为准，这里只用于展示"至少要写多少字"
  return levelMap.value[g.matchLevel] || null
})

const submitMerge = async () => {
  if (!mergeForm.masterId || !mergeForm.mergedId) {
    ElMessage.warning('请选择主档与被并档案')
    return
  }
  if (mergeForm.masterId === mergeForm.mergedId) {
    ElMessage.warning('主档与被并档案不能是同一份')
    return
  }
  const minLen = currentLevel.value?.minReasonLength ?? 10
  if ((mergeForm.reason || '').trim().length < minLen) {
    ElMessage.warning(`当前匹配级别的合并理由至少 ${minLen} 个字（越弱的依据越要写清是谁、依据什么核实的）`)
    return
  }
  mergeSubmitting.value = true
  try {
    const res = await mergePatientIndex({ ...mergeForm })
    ElMessage.success(
      `合并完成：${res.data?.mergedNo} 并入 ${res.data?.masterNo}，级别「${res.data?.matchTypeText}」。已写入审计，可撤销。`
    )
    mergeVisible.value = false
    await Promise.all([loadDuplicates(), loadStats(), loadList()])
  } catch (e: any) {
    ElMessage.error(e?.message || '合并失败')
  } finally {
    mergeSubmitting.value = false
  }
}

// ---------- 撤销 ----------
const handleRevert = async (log: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `将把 ${log.mergedNo} ${log.mergedName} 从主档 ${log.masterNo} ${log.masterName} 中撤出，` +
        `该档案重新在册。请填写撤销理由：`,
      '撤销合并',
      { inputPlaceholder: '例如：核实后确认是两个人，误合并', inputValidator: (v: string) => !!v?.trim() || '撤销理由必填' }
    )
    await revertPatientMerge({ logId: log.id, revertReason: value })
    ElMessage.success('已撤销合并，档案重新在册')
    await Promise.all([loadLogs(), loadStats(), loadDuplicates()])
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '撤销失败')
  }
}

// ---------- 详情抽屉 ----------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  const res = await getPatientIndexDetail(String(row.id))
  detail.value = res.data
  detailVisible.value = true
}

// ---------- 初始化 ----------
onMounted(async () => {
  await loadDict()
  await Promise.all([loadStats(), loadDuplicates(), loadList(), loadLogs()])
})
</script>

<template>
  <div class="space-y-6">
    <!-- 标题 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">患者主索引（EMPI）</h1>
        <p class="mt-1 text-sm text-slate-500">
          把多来源建档产生的重复档案找出来，人工确认后建立主索引关系。
          <span class="font-medium text-slate-600">系统不会自动合并</span>；合并只建立"这两份是同一个人"的指向，
          <span class="font-medium text-slate-600">不搬动业务数据</span>，且可撤销。
        </p>
      </div>
      <div class="flex items-center gap-3">
        <el-button :icon="Refresh" @click="loadStats(); loadDuplicates(); loadList(); loadLogs()">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">患者档案总数</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p5-empi-stats-total">
          {{ stats.patientTotal ?? '—' }}
        </p>
        <p class="text-[11px] text-slate-400">含已并入主档的影子档案</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">强重复组（身份证相同）</p>
        <p
          class="text-lg font-bold"
          :class="Number(stats.strongDupGroups) > 0 ? 'text-red-600' : 'text-slate-900'"
          data-testid="p5-empi-stats-dup"
        >
          {{ stats.strongDupGroups ?? '—' }}
        </p>
        <p class="text-[11px] text-slate-400">手机号重复不计入（存在多人共用号码）</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">已并入主档的档案</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p5-empi-stats-shadow">
          {{ stats.mergedCount ?? '—' }}
        </p>
        <p class="text-[11px] text-slate-400">可在「合并历史」中撤销</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">唯一性 / 身份证完整率</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p5-empi-stats-unique">
          {{ stats.uniqueRate ?? '—' }}% / {{ stats.idCardCompleteRate ?? '—' }}%
        </p>
        <p class="text-[11px] text-slate-400">5 级数据质量门槛 ≥ 80%</p>
      </div>
    </div>

    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <el-tabs v-model="activeTab">
        <!-- ============ 疑似重复检测 ============ -->
        <el-tab-pane label="疑似重复检测" name="duplicate">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-input
              v-model="dupKeyword"
              data-testid="p5-empi-dup-keyword"
              placeholder="按姓名/患者号/手机号/身份证号过滤"
              clearable
              class="!w-72"
              @keyup.enter="loadDuplicates"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" :icon="Search" data-testid="p5-empi-dup-search" @click="loadDuplicates">
              检测
            </el-button>
            <span class="text-xs text-slate-400">
              共 {{ groups.length }} 组可疑档案（结果是"值得看一眼"，不是"应该合并"）
            </span>
          </div>

          <el-empty
            v-if="!loading && groups.length === 0"
            description="没有发现可疑的重复档案"
            data-testid="p5-empi-dup-empty"
          />

          <div class="space-y-4" data-testid="p5-empi-groups">
            <div
              v-for="(g, gi) in groups"
              :key="g.groupKey"
              class="rounded-lg border border-slate-200"
              :data-testid="`p5-empi-group-${gi}`"
            >
              <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 bg-slate-50 px-4 py-2">
                <el-tag :type="levelTagType(g.matchLevel)" size="small" :data-testid="`p5-empi-group-level-${gi}`">
                  {{ g.matchLevelText }}
                </el-tag>
                <span v-if="g.strong" class="text-xs font-medium text-red-600">强依据</span>
                <span class="text-xs text-slate-500">{{ g.members.length }} 份档案</span>
                <span class="text-xs text-slate-400" :data-testid="`p5-empi-group-evidence-${gi}`">
                  依据：{{ g.matchEvidence }}
                </span>
                <div class="ml-auto">
                  <el-button
                    v-perm="'patient:empi:edit'"
                    type="primary"
                    size="small"
                    :icon="Link"
                    :data-testid="`p5-empi-group-merge-${gi}`"
                    @click="openMerge(g)"
                  >
                    合并档案
                  </el-button>
                </div>
              </div>
              <div
                class="flex items-start gap-2 px-4 py-2 text-xs"
                :class="g.strong ? 'text-slate-600' : 'bg-amber-50 text-amber-700'"
                :data-testid="`p5-empi-group-tip-${gi}`"
              >
                <el-icon v-if="!g.strong" class="mt-0.5 shrink-0"><WarnTriangleFilled /></el-icon>
                <span>{{ g.tip }}</span>
              </div>
              <el-table :data="g.members" size="small" class="w-full">
                <el-table-column label="患者号" prop="patientNo" width="140" />
                <el-table-column label="姓名" prop="patientName" width="140" />
                <el-table-column label="性别/年龄" width="100">
                  <template #default="{ row }">{{ row.genderText }} / {{ row.age ?? '—' }}</template>
                </el-table-column>
                <el-table-column label="身份证号" prop="idCard" min-width="180">
                  <template #default="{ row }">
                    <span :class="row.idCard ? '' : 'text-slate-300'">{{ row.idCard || '（空）' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="手机号" prop="phone" width="130">
                  <template #default="{ row }">
                    <span :class="row.phone ? '' : 'text-slate-300'">{{ row.phone || '（空）' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="档案完整度" width="130">
                  <template #default="{ row }">
                    <span :class="row.completeRate >= 80 ? 'text-emerald-600' : 'text-amber-600'">
                      {{ row.completeRate }}%（缺 {{ row.missingFields?.length || 0 }} 项）
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="业务数据" width="110">
                  <template #default="{ row }">{{ row.totalDataCount }} 条</template>
                </el-table-column>
                <el-table-column label="操作" width="90" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- ============ 档案列表 ============ -->
        <el-tab-pane label="档案列表" name="list">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-input
              v-model="listQuery.keyword"
              data-testid="p5-empi-list-keyword"
              placeholder="按姓名/患者号/手机号/身份证号搜索"
              clearable
              class="!w-72"
              @keyup.enter="listQuery.pageNum = 1; loadList()"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-checkbox
              v-model="listQuery.includeShadow"
              data-testid="p5-empi-include-shadow"
              @change="listQuery.pageNum = 1; loadList()"
            >
              包含已并入主档的影子档案
            </el-checkbox>
            <el-button type="primary" :icon="Search" data-testid="p5-empi-list-search" @click="listQuery.pageNum = 1; loadList()">
              搜索
            </el-button>
          </div>

          <el-table :data="rows" v-loading="loading" size="small" data-testid="p5-empi-table">
            <el-table-column label="患者号" prop="patientNo" width="140" />
            <el-table-column label="姓名" prop="patientName" width="130" />
            <el-table-column label="性别/年龄" width="100">
              <template #default="{ row }">{{ row.genderText }} / {{ row.age ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="身份证号" prop="idCard" min-width="170">
              <template #default="{ row }">
                <span :class="row.idCard ? '' : 'text-slate-300'">{{ row.idCard || '（空）' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="手机号" prop="phone" width="130" />
            <el-table-column label="档案完整度" width="140">
              <template #default="{ row }">
                <span :class="row.completeRate >= 80 ? 'text-emerald-600' : 'text-amber-600'">
                  {{ row.completeRate }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="业务数据量" width="110" prop="totalDataCount" />
            <el-table-column label="主索引" width="150">
              <template #default="{ row }">
                <el-tag v-if="Number(row.mergeStatus) === 1" type="warning" size="small">
                  已并入 {{ row.masterNo }}
                </el-tag>
                <el-tag v-else-if="Number(row.shadowCount) > 0" type="success" size="small">
                  主档（含 {{ row.shadowCount }} 份影子）
                </el-tag>
                <span v-else class="text-xs text-slate-400">主档</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination
              v-model:current-page="listQuery.pageNum"
              v-model:page-size="listQuery.pageSize"
              :total="total"
              :page-sizes="PAGE_SIZES"
              layout="total, sizes, prev, pager, next"
              @current-change="loadList"
              @size-change="loadList"
            />
          </div>
        </el-tab-pane>

        <!-- ============ 合并历史 ============ -->
        <el-tab-pane label="合并历史" name="logs">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-input
              v-model="logQuery.keyword"
              data-testid="p5-empi-log-keyword"
              placeholder="按合并单号/患者号/姓名搜索"
              clearable
              class="!w-72"
              @keyup.enter="logQuery.pageNum = 1; loadLogs()"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" :icon="Search" @click="logQuery.pageNum = 1; loadLogs()">搜索</el-button>
            <span class="text-xs text-slate-400">审计只增不删：已撤销的记录仍在这里</span>
          </div>

          <el-table :data="logs" v-loading="loading" size="small" data-testid="p5-empi-log-table">
            <el-table-column label="合并单号" prop="mergeNo" width="150" />
            <el-table-column label="主档（保留）" min-width="170">
              <template #default="{ row }">{{ row.masterNo }} {{ row.masterName }}</template>
            </el-table-column>
            <el-table-column label="被并档案" min-width="170">
              <template #default="{ row }">{{ row.mergedNo }} {{ row.mergedName }}</template>
            </el-table-column>
            <el-table-column label="匹配级别" width="190">
              <template #default="{ row }">
                <el-tag :type="levelTagType(row.matchType)" size="small">{{ row.matchTypeText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="理由" prop="reason" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作人" prop="operatorName" width="100" />
            <el-table-column label="合并时间" prop="mergeTime" width="160" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="Number(row.logStatus) === 1 ? 'success' : 'info'" size="small">
                  {{ row.logStatusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row, $index }">
                <el-button
                  v-if="row.canRevert"
                  v-perm="'patient:empi:delete'"
                  link
                  type="danger"
                  size="small"
                  :icon="RefreshLeft"
                  :data-testid="`p5-empi-log-revert-${$index}`"
                  @click="handleRevert(row)"
                >
                  撤销
                </el-button>
                <span v-else class="text-xs text-slate-300">—</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination
              v-model:current-page="logQuery.pageNum"
              v-model:page-size="logQuery.pageSize"
              :total="logTotal"
              layout="total, prev, pager, next"
              @current-change="loadLogs"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ============ 合并弹窗 ============ -->
    <el-dialog v-model="mergeVisible" title="合并患者档案" width="720px" data-testid="p5-empi-merge-dialog">
      <div class="space-y-4">
        <el-alert
          v-if="currentGroup && !currentGroup.strong"
          type="warning"
          :closable="false"
          show-icon
          :title="currentGroup.matchLevelText"
        >
          <template #default>
            <span class="text-xs">{{ currentGroup.tip }}</span>
          </template>
        </el-alert>

        <div>
          <p class="mb-1 text-sm font-medium text-slate-700">主档（保留，{{
            currentGroup?.strong ? '建议选有业务数据的那份' : '必须是核实为同一人的那份'
          }}）</p>
          <el-select v-model="mergeForm.masterId" data-testid="p5-empi-merge-master" class="!w-full">
            <el-option
              v-for="m in currentGroup?.members || []"
              :key="m.id"
              :label="memberLabel(m)"
              :value="String(m.id)"
            />
          </el-select>
        </div>

        <div>
          <p class="mb-1 text-sm font-medium text-slate-700">被并档案（合并后在册状态失效）</p>
          <el-select v-model="mergeForm.mergedId" data-testid="p5-empi-merge-merged" class="!w-full">
            <el-option
              v-for="m in (currentGroup?.members || []).filter((x: any) => String(x.id) !== mergeForm.masterId)"
              :key="m.id"
              :label="memberLabel(m)"
              :value="String(m.id)"
            />
          </el-select>
        </div>

        <div>
          <p class="mb-1 text-sm font-medium text-slate-700">
            合并理由（至少 {{ currentLevel?.minReasonLength ?? 10 }} 字，将写入审计）
          </p>
          <el-input
            v-model="mergeForm.reason"
            data-testid="p5-empi-merge-reason"
            type="textarea"
            :rows="3"
            :placeholder="
              currentGroup?.strong
                ? '例如：同一患者两次建档，身份证号一致'
                : '写清是谁、依据什么材料核实的 —— 仅凭同名或同手机号不足以合并'
            "
          />
        </div>

        <el-checkbox v-model="mergeForm.fillBlank" data-testid="p5-empi-merge-fillblank">
          把被并档案"有值而主档为空"的关键字段补全到主档（默认不补，只建立归属关系）
        </el-checkbox>

        <p class="text-xs text-slate-500">
          合并后：被并档案在主档下失效，但它的挂号/病历/收费等记录
          <span class="font-medium">原地保留</span>，查询时按主档归并显示 —— 不会搬动或删除任何业务数据。
        </p>
      </div>
      <template #footer>
        <el-button @click="mergeVisible = false">取消</el-button>
        <el-button
          v-perm="'patient:empi:edit'"
          type="primary"
          :loading="mergeSubmitting"
          data-testid="p5-empi-merge-submit"
          @click="submitMerge"
        >
          确认合并
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 详情抽屉 ============ -->
    <el-drawer v-model="detailVisible" title="患者主索引详情" size="560px" data-testid="p5-empi-detail">
      <div v-if="detail" class="space-y-4">
        <div class="rounded-lg border border-slate-200 p-4">
          <p class="text-base font-semibold text-slate-900">
            {{ detail.patientNo }} {{ detail.patientName }}
            <span class="ml-2 text-sm font-normal text-slate-500">{{ detail.genderText }} / {{ detail.age ?? '—' }}岁</span>
          </p>
          <p class="mt-2 text-sm text-slate-600">身份证号：{{ detail.idCard || '（空）' }}</p>
          <p class="text-sm text-slate-600">手机号：{{ detail.phone || '（空）' }}</p>
          <p class="text-sm text-slate-600">地址：{{ detail.address || '（空）' }}</p>
          <p class="mt-2 text-sm">
            主索引状态：
            <el-tag v-if="Number(detail.mergeStatus) === 1" type="warning" size="small">
              已并入 {{ detail.masterNo }} {{ detail.masterName }}
            </el-tag>
            <el-tag v-else type="success" size="small">主档</el-tag>
          </p>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 text-sm font-medium text-slate-700">
            档案完整度 {{ detail.completeRate }}%（{{ detail.completeCount }}/{{ detail.totalFieldCount }}）
          </p>
          <div class="flex flex-wrap gap-1">
            <el-tag
              v-for="f in detail.missingFields || []"
              :key="f"
              type="warning"
              size="small"
            >
              缺 {{ f }}
            </el-tag>
            <span v-if="!(detail.missingFields || []).length" class="text-xs text-emerald-600">关键字段已填全</span>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 text-sm font-medium text-slate-700">关联业务数据（{{ detail.totalDataCount }} 条）</p>
          <div class="grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
            <div v-for="d in detail.dataCounts || []" :key="d.key" class="flex justify-between">
              <span class="text-slate-500">{{ d.label }}</span>
              <span class="font-medium text-slate-800">{{ d.count }}</span>
            </div>
          </div>
        </div>

        <div v-if="(detail.siblingPatients || []).length" class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 text-sm font-medium text-slate-700">
            同主档下的其他档案（{{ detail.siblingPatients.length }} 份）
          </p>
          <div v-for="s in detail.siblingPatients" :key="s.id" class="mb-2 text-sm text-slate-600">
            {{ s.patientNo }} {{ s.patientName }}（{{ s.genderText }}）｜
            证件 {{ s.idCard || '空' }}｜并入时间 {{ s.mergeTime || '—' }}
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>
