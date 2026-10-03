<script setup lang="ts">
/**
 * 门诊电子病历查询（原菜单 601）—— **已下线，仅保留直接访问能力**
 *
 * 【2026-09-23 摘除菜单】本页的正式出口已改为 602「门诊病案首页」。
 * 摘除理由（实测）：它与 602 同表同接口同检索、信息量更少，且没有任何岗位的
 * 工作流会走到它（临床岗看病历都在"已知患者"的上下文里，走 703 PatientDetailDialog；
 * 病案岗走 604/609）。见 `source/back_end/sql/74-摘除门诊电子病历查询菜单.sql`。
 *
 * ⚠ 本页**遗留未修的缺陷，别再照它抄**（已下线故不修）：
 *   1. 抽屉里 4 处面板恒空 —— 读的 `medicalRecords` / `inspections` /
 *      `laboratories` / `surgeryHistories` 都不在 `EmrRecordDetailVO` 返回体里
 *      （后端只返回 record / prescriptions / inspectionApplies / laboratoryApplies）；
 *      而 `prescriptions` 后端给了、本页没渲染。
 *   2. 「入院记录 / 首次病程 / 出院小结 / 手术记录」是**住院**文书，
 *      门诊病历表里没有这些数据 —— 那三个 tab 是同一批字段换标题重复渲染。
 *   3. 体格检查 9 列（该表最完整的部分）一列都没展示，602 全展示了。
 *   正确写法参考 602 `medical-record/MedicalRecordView.vue`。
 *
 * 以下为原职责说明（G4 收口，2026-09-23）：本页曾定位为只读的临床阅读视角 ——
 * 按患者/时间找到一份门诊病历，看它的病程、入院记录、检查报告。
 * 写入口不在这里：门诊病历的写入与审核在 602/603，住院病历在 304。
 *
 * 修掉的三处问题：
 * 1. **筛选曾是前端切片**：只取回当前页（10 条）再 `list.filter(...)`，
 *    于是「翻到第 3 页搜某个人」永远搜不到 —— 而 `total` 还被覆盖成过滤后的
 *    本页条数，分页器跟着一起失真。现在条件全部下推后端。
 * 2. **右栏「临床路径」是硬编码假数据**（入院当天/第1-2天/… 五个时间节点、
 *    「常用药物参考」三项、注意事项都是字面量），页面上看着有、其实接不上任何接口。
 *    真实的临床路径有独立菜单 607，这里直接摘掉。
 * 3. **「会诊记录」tab 写死「暂无会诊记录」**，恒空。没有后端数据源的 tab 就是假功能，摘掉。
 */
import { ref, onMounted } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getRecordDetail, getRecordListPage } from '@/api/emr'
import { patientGenderText, patientGenderSymbol } from '@/lib/patientGender'
import { recordStatusText, recordStatusTagType } from '@/lib/recordStatus'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const loading = ref(false)
const records = ref<any[]>([])
const drawerVisible = ref(false)
const selectedRecord = ref<any>(null)
const detailLoading = ref(false)
const detailData = ref<any>(null)
const recordTab = ref('progress')

const searchForm = ref({
  keyword: '',
  visitDateRange: [] as string[],
})

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})

/** 组装后端 MedicalRecordQueryPageDTO 入参：条件必须下推，前端不再切片 */
const buildQuery = () => {
  const query: any = {
    pageNum: pagination.value.pageNum,
    pageSize: pagination.value.pageSize,
  }
  if (searchForm.value.keyword) query.keyword = searchForm.value.keyword.trim()
  const [start, end] = searchForm.value.visitDateRange || []
  if (start) query.visitDateStart = start
  if (end) query.visitDateEnd = end
  return query
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRecordListPage(buildQuery())
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载病历失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = { keyword: '', visitDateRange: [] }
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

const openDrawer = async (row: any) => {
  selectedRecord.value = row
  drawerVisible.value = true
  detailLoading.value = true
  recordTab.value = 'progress'
  try {
    const res = await getRecordDetail(row.id)
    detailData.value = res.data || {}
  } catch (error) {
    console.error('加载详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const calcAge = (birthDate: string | null): string => {
  if (!birthDate) return '-'
  const birth = new Date(birthDate)
  const now = new Date()
  let age = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) age--
  return age >= 0 ? String(age) : '-'
}

const getAgeTag = (age: number) => {
  if (age <= 14) return { label: '儿童', color: 'bg-pink-100 text-pink-700' }
  if (age >= 65) return { label: '老年', color: 'bg-amber-100 text-amber-700' }
  return null
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 搜索条件 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <el-input
            v-model="searchForm.keyword"
            data-testid="rec-search"
            placeholder="患者姓名 / 患者号 / 就诊号 / 病历号"
            :prefix-icon="Search"
            clearable
            class="!w-72"
            @keyup.enter="handleSearch"/>
        <el-date-picker
            v-model="searchForm.visitDateRange"
            data-testid="rec-date-range"
            type="daterange"
            range-separator="~"
            start-placeholder="就诊日期起"
            end-placeholder="就诊日期止"
            value-format="YYYY-MM-DD"
            clearable
            class="!w-72"
            @change="handleSearch"/>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 患者列表 -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-table :data="records" v-loading="loading" style="width: 100%" @row-click="openDrawer"
                row-class-name="cursor-pointer">
        <el-table-column label="患者" min-width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span
                  :class="['inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold text-white',
                           row.gender === 1 ? 'bg-blue-500' : row.gender === 2 ? 'bg-pink-500' : 'bg-slate-400']">
                {{ patientGenderSymbol(row.gender) }}
              </span>
              <div>
                <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
                <div class="text-xs text-slate-400">{{ row.patientNo }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别/年龄" width="100">
          <template #default="{ row }">
            <span class="text-sm text-slate-700">{{ patientGenderText(row.gender) }} / {{ row.age }}岁</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="120"/>
        <el-table-column prop="doctorName" label="医生" width="100"/>
        <el-table-column prop="registNo" label="就诊号" width="180" class-name="font-mono text-sm"/>
        <el-table-column label="就诊日期" width="120">
          <template #default="{ row }">
            <span class="text-sm text-slate-700">{{ row.visitDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="诊断" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-sm text-slate-700">{{ row.diagnosisName || row.diagnosis || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <!-- 状态文案走 lib/recordStatus 单点：原先这里是三元链，
                 没有第 4 档 → record_status=4（已作废）被渲染成「草稿」 -->
            <el-tag effect="plain" size="small" :type="recordStatusTagType(row.recordStatus)">
              {{ recordStatusText(row.recordStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="records.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无病历记录
      </div>
      <div class="flex justify-end border-t border-slate-100 px-4 py-3">
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
    </div>

    <!-- 电子病历抽屉 -->
    <el-drawer
        v-model="drawerVisible"
        direction="rtl"
        size="95%"
        :show-close="true"
        :before-close="() => { drawerVisible = false }"
    >
      <template #header>
        <div class="flex items-center gap-3">
          <span class="text-lg font-bold text-slate-900">电子病历</span>
          <span v-if="selectedRecord" class="text-sm text-slate-400">{{
              selectedRecord.patientName
            }} · {{ selectedRecord.registNo }}</span>
        </div>
      </template>

      <div v-loading="detailLoading" class="flex h-[calc(100vh-8rem)] gap-4">
        <!-- ========== 左栏：患者信息 + 历史病历 + 检查报告 ========== -->
        <div class="w-[300px] shrink-0 space-y-3 overflow-y-auto">
          <!-- 患者基本信息 -->
          <div v-if="detailData?.record" class="rounded-lg border border-slate-200 bg-white p-4">
            <div class="mb-3 flex items-center gap-3">
              <span
                  :class="['inline-flex h-12 w-12 shrink-0 items-center justify-center rounded-full text-lg font-bold text-white',
                           detailData.record.gender === 1 ? 'bg-blue-500'
                               : detailData.record.gender === 2 ? 'bg-pink-500' : 'bg-slate-400']">
                {{ patientGenderSymbol(detailData.record.gender) }}
              </span>
              <div>
                <div class="text-base font-semibold text-slate-900">{{ detailData.record.patientName }}</div>
                <div class="text-xs text-slate-400">{{ detailData.record.patientNo }}</div>
              </div>
            </div>
            <div class="space-y-2 text-sm">
              <div class="flex items-center justify-between"><span class="text-slate-400">性别</span><span
                  class="font-medium text-slate-700">{{ patientGenderText(detailData.record.gender) }}</span></div>
              <div class="flex items-center justify-between"><span class="text-slate-400">年龄</span>
                <span class="flex items-center gap-1">
                  <span class="font-medium text-slate-700">{{ detailData.record.age }}岁</span>
                  <span v-if="getAgeTag(detailData.record.age)"
                        :class="['rounded px-1 py-0.5 text-[10px] font-medium', getAgeTag(detailData.record.age)?.color]">
                    {{ getAgeTag(detailData.record.age)?.label }}
                  </span>
                </span>
              </div>
              <div class="flex items-center justify-between"><span class="text-slate-400">就诊号</span><span
                  class="font-mono text-xs text-slate-700">{{ detailData.record.registNo || '-' }}</span></div>
              <div class="flex items-center justify-between"><span class="text-slate-400">科室</span><span
                  class="font-medium text-slate-700">{{ detailData.record.deptName }}</span></div>
              <div class="flex items-center justify-between"><span class="text-slate-400">医生</span><span
                  class="font-medium text-slate-700">{{ detailData.record.doctorName }}</span></div>
              <div class="flex items-center justify-between"><span class="text-slate-400">就诊日期</span><span
                  class="font-medium text-slate-700">{{ detailData.record.visitDate }}</span></div>
              <div v-if="detailData.record.allergyHistory && detailData.record.allergyHistory !== '无'"
                   class="rounded border border-red-200 bg-red-50 p-2">
                <span class="text-xs font-bold text-red-600">⚠ 过敏：{{ detailData.record.allergyHistory }}</span>
              </div>
            </div>
          </div>

          <!-- 历史病历时间线 -->
          <div class="rounded-lg border border-slate-200 bg-white p-4">
            <h4 class="mb-3 text-xs font-bold text-slate-500">历史病历</h4>
            <div v-if="detailData?.medicalRecords?.length" class="space-y-0">
              <div v-for="(r, idx) in detailData.medicalRecords.slice(0, 10)" :key="r.id"
                   class="relative flex gap-3 pb-3">
                <div class="flex flex-col items-center">
                  <div class="h-2 w-2 shrink-0 rounded-full bg-blue-500"></div>
                  <div v-if="idx < detailData.medicalRecords.length - 1" class="w-0.5 flex-1 bg-slate-200"></div>
                </div>
                <div class="min-w-0 flex-1">
                  <div class="text-xs text-slate-400">{{ r.visitDate }}</div>
                  <div class="text-xs font-medium text-slate-700">{{ r.deptName }} · {{ r.doctorName }}</div>
                  <div class="mt-0.5 truncate text-xs text-slate-500">{{ r.diagnosisName || r.diagnosis || '-' }}</div>
                </div>
              </div>
            </div>
            <div v-else class="py-4 text-center text-xs text-slate-400">暂无历史病历</div>
          </div>

          <!-- 检查报告列表 -->
          <div class="rounded-lg border border-slate-200 bg-white p-4">
            <h4 class="mb-3 text-xs font-bold text-slate-500">检查 / 检验报告</h4>
            <div v-if="detailData?.inspections?.length || detailData?.laboratories?.length" class="space-y-2">
              <div v-for="ins in (detailData.inspections || [])" :key="ins.id"
                   class="flex items-center gap-2 rounded border border-slate-100 p-2 text-xs">
                <el-tag size="small" type="info" effect="plain">检查</el-tag>
                <span class="flex-1 font-medium text-slate-700">{{ ins.inspectionItemName }}</span>
                <span class="text-slate-400">{{ ins.bodyPart || '-' }}</span>
              </div>
              <div v-for="lab in (detailData.laboratories || [])" :key="lab.id"
                   class="flex items-center gap-2 rounded border border-slate-100 p-2 text-xs">
                <el-tag size="small" type="info" effect="plain">检验</el-tag>
                <span class="flex-1 font-medium text-slate-700">{{ lab.laboratoryItemName }}</span>
                <span class="text-slate-400">{{ lab.specimenType || '-' }}</span>
              </div>
            </div>
            <div v-else class="py-4 text-center text-xs text-slate-400">暂无检查报告</div>
          </div>
        </div>

        <!-- ========== 中栏：6个Tab ========== -->
        <div class="flex-1 overflow-hidden rounded-lg border border-slate-200 bg-white">
          <el-tabs v-model="recordTab" class="h-full">
            <!-- 病程记录 -->
            <el-tab-pane label="病程记录" name="progress">
              <div class="max-h-[calc(100vh-12rem)] overflow-y-auto p-4">
                <div v-if="detailData?.record" class="space-y-4">
                  <div class="rounded-lg border border-slate-200 p-4">
                    <div class="mb-2 flex items-center justify-between">
                      <h4 class="text-sm font-bold text-slate-700">主诉</h4>
                      <span class="text-xs text-slate-400">{{ detailData.record.visitDate }}</span>
                    </div>
                    <p class="text-sm text-slate-600">{{ detailData.record.chiefComplaint || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">现病史</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.presentIllness || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">治疗方案</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.treatmentPlan || '-' }}</p>
                  </div>
                </div>
                <div v-else class="py-12 text-center text-sm text-slate-400">暂无病程记录</div>
              </div>
            </el-tab-pane>

            <!-- 入院记录 -->
            <el-tab-pane label="入院记录" name="admission">
              <div class="max-h-[calc(100vh-12rem)] overflow-y-auto p-4">
                <div v-if="detailData?.record" class="space-y-4">
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">入院诊断</h4>
                    <p class="text-sm font-medium text-blue-700">
                      {{ detailData.record.diagnosisName || detailData.record.diagnosis || '-' }}</p>
                    <p v-if="detailData.record.diagnosisCode" class="mt-1 font-mono text-xs text-slate-400">ICD-10:
                      {{ detailData.record.diagnosisCode }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">既往史</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.pastHistory || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">个人史</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.personalHistory || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">家族史</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.familyHistory || '-' }}</p>
                  </div>
                </div>
                <div v-else class="py-12 text-center text-sm text-slate-400">暂无入院记录</div>
              </div>
            </el-tab-pane>

            <!-- 首次病程 -->
            <el-tab-pane label="首次病程" name="firstProgress">
              <div class="max-h-[calc(100vh-12rem)] overflow-y-auto p-4">
                <div v-if="detailData?.record" class="space-y-4">
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">病例特点</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.presentIllness || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">诊断依据</h4>
                    <p class="text-sm text-slate-600">
                      {{ detailData.record.diagnosisName || detailData.record.diagnosis || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">鉴别诊断</h4>
                    <p class="text-sm text-slate-600">-</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">诊疗计划</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.treatmentPlan || '-' }}</p>
                  </div>
                </div>
                <div v-else class="py-12 text-center text-sm text-slate-400">暂无首次病程记录</div>
              </div>
            </el-tab-pane>

            <!-- 出院小结 -->
            <el-tab-pane label="出院小结" name="discharge">
              <div class="max-h-[calc(100vh-12rem)] overflow-y-auto p-4">
                <div v-if="detailData?.record" class="space-y-4">
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">出院诊断</h4>
                    <p class="text-sm font-medium text-blue-700">
                      {{ detailData.record.diagnosisName || detailData.record.diagnosis || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">出院医嘱</h4>
                    <p class="text-sm text-slate-600">{{ detailData.record.treatmentPlan || '-' }}</p>
                  </div>
                  <div class="rounded-lg border border-slate-200 p-4">
                    <h4 class="mb-2 text-sm font-bold text-slate-700">注意事项</h4>
                    <p class="text-sm text-slate-600">-</p>
                  </div>
                </div>
                <div v-else class="py-12 text-center text-sm text-slate-400">暂无出院小结</div>
              </div>
            </el-tab-pane>

            <!-- 手术记录 -->
            <el-tab-pane label="手术记录" name="surgery">
              <div class="max-h-[calc(100vh-12rem)] overflow-y-auto p-4">
                <div v-if="detailData?.surgeryHistories?.length" class="space-y-3">
                  <div v-for="s in detailData.surgeryHistories" :key="s.id"
                       class="rounded-lg border border-slate-200 p-4">
                    <div class="flex items-center justify-between">
                      <h4 class="text-sm font-bold text-slate-700">{{ s.surgeryName }}</h4>
                      <span class="text-xs text-slate-400">{{ s.surgeryDate || '-' }}</span>
                    </div>
                    <p v-if="s.outcome" class="mt-1 text-xs text-slate-500">结局：{{ s.outcome }}</p>
                  </div>
                </div>
                <div v-else class="py-12 text-center text-sm text-slate-400">暂无手术记录</div>
              </div>
            </el-tab-pane>

            <!-- 会诊记录 tab 已摘除：原先写死「暂无会诊记录」恒空，没有后端数据源的 tab 就是假功能。
                 会诊若要做，应先在后端落数据表与接口，再回来加 tab。 -->
          </el-tabs>
        </div>
      </div>
    </el-drawer>
  </div>
</template>
