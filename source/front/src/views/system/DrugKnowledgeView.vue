<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {
    doseLimitDeleteById,
    doseLimitListPage,
    doseLimitUpsert,
    interactionDeleteById,
    interactionListPage,
    interactionUpsert,
} from '@/api/drugKnowledge'
import {getDictDataMapList} from '@/api/system'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'

/**
 * 系统管理 → 合理用药知识库（sql/130）
 *
 * 两个页签两套数据：
 *  · 相互作用 = sys_drug_interaction（成对成分 + 严重度 + 后果原文）；
 *  · 剂量上限 = sys_drug_dose_limit（一个成分一条极量）。
 *
 * 为什么值得把这个页面做厚一点：审方药师点「通过」被拒时，屏幕上那句理由<b>就是</b>这里的
 * interaction_desc 原文（后端逐字引用，不前端拼装）。所以这一页写的每一个字都是发给
 * 医生的正式结论，改这里等于改规则本身 —— 也因此「命中药品数」必须显示出来：
 * 0 只说明本院字典还没进这个药（知识储备），而不是写错了，但没这个数字就没法区分这两件事。
 */

const activeTab = ref('interaction')

/** 严重度字典 his_drug_interaction_severity（1-禁忌 2-慎用），取值以字典为准不写死 */
const severityDict = ref<any[]>([])
const doseUnitDict = ref<any[]>([])

const severityLabel = (v: number) =>
    severityDict.value.find(d => String(d.dictValue) === String(v))?.dictLabel || (v === 1 ? '禁忌' : '慎用')
const severityTagType = (v: number) => (v === 1 ? 'danger' : 'warning')

async function loadDicts() {
    try {
        const res: any = await getDictDataMapList('his_drug_interaction_severity,his_dose_unit')
        severityDict.value = res.data?.his_drug_interaction_severity || []
        doseUnitDict.value = res.data?.his_dose_unit || []
    } catch (e: any) {
        ElMessage.error(e?.message || '字典加载失败')
    }
}

/* ==================== 页签一：药物相互作用 ==================== */
const intLoading = ref(false)
const intRows = ref<any[]>([])
const intQuery = reactive({
    component: '',
    keyword: '',
    severity: null as number | null,
    status: null as number | null,
})
const intPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const intDialog = ref(false)
const intTitle = ref('新增相互作用')
const intSubmitting = ref(false)
const emptyIntForm = () => ({
    id: null as string | null,
    componentA: '',
    componentB: '',
    severity: 2,
    interactionDesc: '',
    suggestion: '',
    status: 1,
    remark: '',
})
const intForm = ref(emptyIntForm())

async function loadInteractions() {
    intLoading.value = true
    try {
        const res = await interactionListPage({
            component: intQuery.component || undefined,
            keyword: intQuery.keyword || undefined,
            severity: intQuery.severity,
            status: intQuery.status,
            pageNum: intPage.pageNum,
            pageSize: intPage.pageSize,
        })
        intRows.value = res.data?.records || []
        intPage.total = Number(res.data?.total || 0)
    } catch (e: any) {
        ElMessage.error(e?.message || '相互作用列表加载失败')
    } finally {
        intLoading.value = false
    }
}

function handleIntSearch() {
    intPage.pageNum = 1
    loadInteractions()
}

function handleIntReset() {
    intQuery.component = ''
    intQuery.keyword = ''
    intQuery.severity = null
    intQuery.status = null
    handleIntSearch()
}

function openIntAdd() {
    intForm.value = emptyIntForm()
    intTitle.value = '新增相互作用'
    intDialog.value = true
}

function openIntEdit(row: any) {
    intForm.value = {
        id: row.id,
        componentA: row.componentA,
        componentB: row.componentB,
        severity: Number(row.severity),
        interactionDesc: row.interactionDesc || '',
        suggestion: row.suggestion || '',
        status: Number(row.status ?? 1),
        remark: row.remark || '',
    }
    intTitle.value = `修改相互作用：${row.componentA} × ${row.componentB}`
    intDialog.value = true
}

async function submitInt() {
    if (!intForm.value.componentA.trim() || !intForm.value.componentB.trim()) {
        return ElMessage.warning('两个成分关键字都要填')
    }
    if (!intForm.value.interactionDesc.trim()) {
        return ElMessage.warning('相互作用后果不能为空，审方被拦时医生看到的就是这句话')
    }
    intSubmitting.value = true
    try {
        await interactionUpsert(intForm.value)
        ElMessage.success('已保存，下一次审方立即生效')
        intDialog.value = false
        await loadInteractions()
    } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
    } finally {
        intSubmitting.value = false
    }
}

async function toggleInt(row: any, next: number) {
    try {
        // 启停与新增/修改在后端是同一个 xxxUpsert，所以整行原样回传、只改 status
        await interactionUpsert({
            id: row.id,
            componentA: row.componentA,
            componentB: row.componentB,
            severity: Number(row.severity),
            interactionDesc: row.interactionDesc,
            suggestion: row.suggestion,
            status: next,
            remark: row.remark,
        })
        ElMessage.success(next === 1 ? '已启用' : '已停用（保留条目，不再参与比对）')
        await loadInteractions()
    } catch (e: any) {
        ElMessage.error(e?.message || '操作失败')
    }
}

async function removeInt(row: any) {
    try {
        await ElMessageBox.confirm(
            `删除「${row.componentA} × ${row.componentB}」是物理删除（同成分对的唯一键会一并释放），`
            + '若只是暂时不想生效，请用「停用」。确定删除？',
            '删除确认',
            {type: 'warning'},
        )
    } catch {
        return
    }
    try {
        await interactionDeleteById(row.id)
        ElMessage.success('删除成功')
        await loadInteractions()
    } catch (e: any) {
        ElMessage.error(e?.message || '删除失败')
    }
}

/* ==================== 页签二：剂量上限 ==================== */
const doseLoading = ref(false)
const doseRows = ref<any[]>([])
const doseQuery = reactive({component: '', keyword: '', status: null as number | null})
const dosePage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const doseDialog = ref(false)
const doseTitle = ref('新增剂量上限')
const doseSubmitting = ref(false)
const emptyDoseForm = () => ({
    id: null as string | null,
    component: '',
    doseUnit: 'mg',
    maxSingleDose: null as number | null,
    maxDailyDose: null as number | null,
    note: '',
    status: 1,
    remark: '',
})
const doseForm = ref(emptyDoseForm())

async function loadDoseLimits() {
    doseLoading.value = true
    try {
        const res = await doseLimitListPage({
            component: doseQuery.component || undefined,
            keyword: doseQuery.keyword || undefined,
            status: doseQuery.status,
            pageNum: dosePage.pageNum,
            pageSize: dosePage.pageSize,
        })
        doseRows.value = res.data?.records || []
        dosePage.total = Number(res.data?.total || 0)
    } catch (e: any) {
        ElMessage.error(e?.message || '剂量上限列表加载失败')
    } finally {
        doseLoading.value = false
    }
}

function handleDoseSearch() {
    dosePage.pageNum = 1
    loadDoseLimits()
}

function handleDoseReset() {
    doseQuery.component = ''
    doseQuery.keyword = ''
    doseQuery.status = null
    handleDoseSearch()
}

function openDoseAdd() {
    doseForm.value = emptyDoseForm()
    doseTitle.value = '新增剂量上限'
    doseDialog.value = true
}

function openDoseEdit(row: any) {
    doseForm.value = {
        id: row.id,
        component: row.component,
        doseUnit: row.doseUnit || 'mg',
        maxSingleDose: row.maxSingleDose === null || row.maxSingleDose === undefined ? null : Number(row.maxSingleDose),
        maxDailyDose: row.maxDailyDose === null || row.maxDailyDose === undefined ? null : Number(row.maxDailyDose),
        note: row.note || '',
        status: Number(row.status ?? 1),
        remark: row.remark || '',
    }
    doseTitle.value = `修改剂量上限：${row.component}`
    doseDialog.value = true
}

async function submitDose() {
    if (!doseForm.value.component.trim()) {
        return ElMessage.warning('成分关键字不能为空')
    }
    if (doseForm.value.maxSingleDose == null && doseForm.value.maxDailyDose == null) {
        return ElMessage.warning('单次最大量与每日最大量至少填一项')
    }
    doseSubmitting.value = true
    try {
        await doseLimitUpsert(doseForm.value)
        ElMessage.success('已保存，下一次审方立即生效')
        doseDialog.value = false
        await loadDoseLimits()
    } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
    } finally {
        doseSubmitting.value = false
    }
}

async function toggleDose(row: any, next: number) {
    try {
        await doseLimitUpsert({
            id: row.id,
            component: row.component,
            doseUnit: row.doseUnit,
            maxSingleDose: row.maxSingleDose,
            maxDailyDose: row.maxDailyDose,
            note: row.note,
            status: next,
            remark: row.remark,
        })
        ElMessage.success(next === 1 ? '已启用' : '已停用')
        await loadDoseLimits()
    } catch (e: any) {
        ElMessage.error(e?.message || '操作失败')
    }
}

async function removeDose(row: any) {
    try {
        await ElMessageBox.confirm(
            `删除「${row.component}」的剂量上限是物理删除，确定继续？`,
            '删除确认',
            {type: 'warning'},
        )
    } catch {
        return
    }
    try {
        await doseLimitDeleteById(row.id)
        ElMessage.success('删除成功')
        await loadDoseLimits()
    } catch (e: any) {
        ElMessage.error(e?.message || '删除失败')
    }
}

/** 命中药品数显示：0 单独标灰，提醒「这条知识现在打不到任何药」 */
const hitsClass = (n: number) => (Number(n) > 0 ? 'text-slate-700' : 'text-slate-400')

onMounted(() => {
    loadDicts()
    loadInteractions()
    loadDoseLimits()
})
</script>

<template>
  <div class="space-y-4">
    <div>
      <h1 class="text-xl font-bold text-slate-900">合理用药知识库</h1>
      <p class="mt-1 text-sm text-slate-500">
        按<b>成分关键字</b>维护（药品名称或通用名包含即命中，所以「华法林」能命中「华法林钠片」）。
        <span class="font-medium text-rose-600">禁忌</span>级命中会直接拒绝审方通过，
        <span class="font-medium text-amber-600">慎用</span>级与剂量超量只标注不拦。改完立即生效，无需重启。
      </p>
    </div>

    <el-tabs v-model="activeTab" class="bg-white rounded-lg border border-slate-200 px-4 pt-2 shadow-sm">
      <el-tab-pane label="药物相互作用" name="interaction">
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <el-input
              v-model="intQuery.component"
              data-testid="kb-int-component"
              placeholder="成分关键字（任一成分）"
              clearable
              style="width: 200px"
              @keyup.enter="handleIntSearch"
          />
          <el-input
              v-model="intQuery.keyword"
              placeholder="后果 / 建议正文"
              clearable
              style="width: 200px"
              @keyup.enter="handleIntSearch"
          />
          <el-select v-model="intQuery.severity" placeholder="严重度" clearable style="width: 130px" :fit-input-width="false">
            <el-option v-for="d in severityDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="intQuery.status" placeholder="状态" clearable style="width: 120px" :fit-input-width="false">
            <el-option label="启用" :value="1"/>
            <el-option label="停用" :value="0"/>
          </el-select>
          <el-button data-testid="kb-int-search" type="primary" :icon="Search" @click="handleIntSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleIntReset">重置</el-button>
          <el-button v-perm="'system:drugKnowledge:add'" class="ml-auto" type="primary" :icon="Plus" data-testid="kb-int-add" @click="openIntAdd">
            新增
          </el-button>
        </div>

        <el-table v-loading="intLoading" :data="intRows" border row-key="id" data-testid="kb-int-table">
          <el-table-column label="成分对" min-width="160">
            <template #default="{ row }">
              <p class="text-[15px] font-medium text-slate-800">{{ row.componentA }} × {{ row.componentB }}</p>
              <p class="text-[13px] text-slate-400">{{ row.pairKey }}</p>
            </template>
          </el-table-column>
          <el-table-column label="严重度" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="severityTagType(Number(row.severity))" size="small">{{ severityLabel(Number(row.severity)) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="相互作用后果（审方提示原文）" min-width="320">
            <template #default="{ row }">
              <p class="text-[15px] text-slate-800 leading-relaxed">{{ row.interactionDesc }}</p>
              <p v-if="row.suggestion" class="text-[14px] text-slate-500 leading-relaxed">处理：{{ row.suggestion }}</p>
            </template>
          </el-table-column>
          <el-table-column label="命中药品" width="110" align="center">
            <template #default="{ row }">
              <span :class="hitsClass(row.drugHitsA)">{{ row.drugHitsA }}</span>
              <span class="text-slate-300"> / </span>
              <span :class="hitsClass(row.drugHitsB)">{{ row.drugHitsB }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="Number(row.status) === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="info" size="small">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="更新" width="150">
            <template #default="{ row }">
              <p class="text-[14px] text-slate-600">{{ row.updateBy || row.createBy || '铺底' }}</p>
              <p class="text-[13px] text-slate-400">{{ row.updateTime || row.createTime || '' }}</p>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="210" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-perm="'system:drugKnowledge:add'" link type="primary" :icon="Edit" @click="openIntEdit(row)">编辑</el-button>
              <el-button
                  v-perm="'system:drugKnowledge:add'"
                  link
                  :type="Number(row.status) === 1 ? 'warning' : 'success'"
                  @click="toggleInt(row, Number(row.status) === 1 ? 0 : 1)"
              >
                {{ Number(row.status) === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button v-perm="'system:drugKnowledge:delete'" link type="danger" :icon="Delete" @click="removeInt(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="intPage.pageNum"
              v-model:page-size="intPage.pageSize"
              :total="intPage.total"
              :page-sizes="PAGE_SIZES"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="handleIntSearch"
              @current-change="loadInteractions"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="剂量上限" name="dose">
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <el-input
              v-model="doseQuery.component"
              data-testid="kb-dose-component"
              placeholder="成分关键字"
              clearable
              style="width: 200px"
              @keyup.enter="handleDoseSearch"
          />
          <el-input
              v-model="doseQuery.keyword"
              placeholder="口径说明正文"
              clearable
              style="width: 200px"
              @keyup.enter="handleDoseSearch"
          />
          <el-select v-model="doseQuery.status" placeholder="状态" clearable style="width: 120px" :fit-input-width="false">
            <el-option label="启用" :value="1"/>
            <el-option label="停用" :value="0"/>
          </el-select>
          <el-button data-testid="kb-dose-search" type="primary" :icon="Search" @click="handleDoseSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleDoseReset">重置</el-button>
          <el-button v-perm="'system:drugKnowledge:add'" class="ml-auto" type="primary" :icon="Plus" data-testid="kb-dose-add" @click="openDoseAdd">
            新增
          </el-button>
        </div>

        <el-table v-loading="doseLoading" :data="doseRows" border row-key="id" data-testid="kb-dose-table">
          <el-table-column label="成分" min-width="140">
            <template #default="{ row }">
              <p class="text-[15px] font-medium text-slate-800">{{ row.component }}</p>
              <p class="text-[13px]" :class="hitsClass(row.drugHits)">命中 {{ row.drugHits }} 种在用药</p>
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="doseUnit" width="80" align="center"/>
          <el-table-column label="单次最大量" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.maxSingleDose !== null" class="text-[15px] text-slate-800">{{ row.maxSingleDose }}{{ row.doseUnit }}</span>
              <span v-else class="text-slate-400">不判</span>
            </template>
          </el-table-column>
          <el-table-column label="每日最大量" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.maxDailyDose !== null" class="text-[15px] text-slate-800">{{ row.maxDailyDose }}{{ row.doseUnit }}</span>
              <span v-else class="text-slate-400">不判</span>
            </template>
          </el-table-column>
          <el-table-column label="口径说明（超量提示会带上）" min-width="300" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="text-[15px] text-slate-700">{{ row.note || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="Number(row.status) === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="info" size="small">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="210" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-perm="'system:drugKnowledge:add'" link type="primary" :icon="Edit" @click="openDoseEdit(row)">编辑</el-button>
              <el-button
                  v-perm="'system:drugKnowledge:add'"
                  link
                  :type="Number(row.status) === 1 ? 'warning' : 'success'"
                  @click="toggleDose(row, Number(row.status) === 1 ? 0 : 1)"
              >
                {{ Number(row.status) === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button v-perm="'system:drugKnowledge:delete'" link type="danger" :icon="Delete" @click="removeDose(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="dosePage.pageNum"
              v-model:page-size="dosePage.pageSize"
              :total="dosePage.total"
              :page-sizes="PAGE_SIZES"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="handleDoseSearch"
              @current-change="loadDoseLimits"
          />
        </div>

        <p class="mt-3 text-[14px] leading-relaxed text-slate-500">
          单位只有 <span class="font-medium text-slate-700">g / mg / ug</span> 三种可比：
          注射剂的「支」、胰岛素笔的「IU」与包装量都不等于一次给药量，所以这类规格解不出来时
          <span class="font-medium text-slate-700">系统选择不判</span>，宁漏报也不给药师一条假超量。
          复方制剂、一个规格里写两个含量的、按周给药的药，同样不在本表口径内。
        </p>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="intDialog" :title="intTitle" width="720px" data-testid="kb-int-dialog">
      <el-form :model="intForm" label-width="120px">
        <el-form-item label="成分关键字A" required>
          <el-input v-model="intForm.componentA" data-testid="kb-form-componentA" placeholder="取成分主干，如「华法林」（命中华法林钠片/华法林抗凝注射液）"/>
        </el-form-item>
        <el-form-item label="成分关键字B" required>
          <el-input v-model="intForm.componentB" data-testid="kb-form-componentB" placeholder="如「胺碘酮」；两个成分书写顺序不影响唯一性"/>
        </el-form-item>
        <el-form-item label="严重度" required>
          <el-radio-group v-model="intForm.severity">
            <el-radio v-for="d in severityDict" :key="d.dictValue" :value="Number(d.dictValue)" :label="Number(d.dictValue)">
              {{ d.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="相互作用后果" required>
          <el-input
              v-model="intForm.interactionDesc"
              data-testid="kb-form-desc"
              type="textarea"
              :rows="3"
              placeholder="审方被拦时医生看到的正文，逐字引用，请写成可执行的结论"
          />
        </el-form-item>
        <el-form-item label="处理建议">
          <el-input v-model="intForm.suggestion" type="textarea" :rows="2" placeholder="换什么药 / 减多少量 / 监测什么指标"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="intForm.status">
            <el-radio :value="1" :label="1">启用</el-radio>
            <el-radio :value="0" :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="intForm.remark" type="textarea" :rows="2" placeholder="依据来源（说明书版本 / 指南条目），便于以后复核"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="intDialog = false">取消</el-button>
        <el-button v-perm="'system:drugKnowledge:add'" type="primary" :loading="intSubmitting" data-testid="kb-int-save" @click="submitInt">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="doseDialog" :title="doseTitle" width="680px" data-testid="kb-dose-dialog">
      <el-form :model="doseForm" label-width="120px">
        <el-form-item label="成分关键字" required>
          <el-input v-model="doseForm.component" data-testid="kb-form-doseComponent" placeholder="如「对乙酰氨基酚」"/>
        </el-form-item>
        <el-form-item label="剂量单位" required>
          <el-radio-group v-model="doseForm.doseUnit">
            <el-radio v-for="d in doseUnitDict" :key="d.dictValue" :value="d.dictValue" :label="d.dictValue">
              {{ d.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="单次最大量">
          <el-input-number v-model="doseForm.maxSingleDose" :min="0" :step="1" :precision="4" controls-position="right"/>
          <span class="ml-2 text-[14px] text-slate-400">留空=单次不判</span>
        </el-form-item>
        <el-form-item label="每日最大量">
          <el-input-number v-model="doseForm.maxDailyDose" :min="0" :step="1" :precision="4" controls-position="right"/>
          <span class="ml-2 text-[14px] text-slate-400">留空=日累计不判；频次写法不认识时这项也自动跳过</span>
        </el-form-item>
        <el-form-item label="口径说明">
          <el-input
              v-model="doseForm.note"
              data-testid="kb-form-note"
              type="textarea"
              :rows="2"
              placeholder="按什么人群/剂型定的极量。同一成分不同适应证极量差一个量级，必须写清"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="doseForm.status">
            <el-radio :value="1" :label="1">启用</el-radio>
            <el-radio :value="0" :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="doseForm.remark" type="textarea" :rows="2"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doseDialog = false">取消</el-button>
        <el-button v-perm="'system:drugKnowledge:add'" type="primary" :loading="doseSubmitting" data-testid="kb-dose-save" @click="submitDose">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
