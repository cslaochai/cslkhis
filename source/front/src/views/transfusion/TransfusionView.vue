<template>
  <div class="space-y-6">
    <!-- 标题 + 在院患者过滤 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <el-select
            v-model="filters.admissionId"
            class="!w-72"
            clearable
            data-testid="p4-tf-admission"
            filterable
            placeholder="按在院患者过滤"
            @change="handleSearch"
        >
          <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
          />
        </el-select>
        <el-button v-perm="'medtech:transfusion:add'" :icon="Plus" data-testid="p4-tf-apply" type="primary"
                   @click="openApply()">
          发起用血申请
        </el-button>
        <el-button :icon="Refresh" @click="handleSearch">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-3">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">未完成用血</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-tf-unfinished">{{ unfinishedCount }}</p>
        <p class="text-[11px] text-slate-400">待配血 + 已配血 + 已发血 + 输注中</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">当前筛选结果</p>
        <p class="text-lg font-bold text-slate-900" data-testid="p4-tf-total">{{ total }}</p>
        <p class="text-[11px] text-slate-400">共 {{ total }} 条用血申请</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">本页配血不合 / 回写链不完整</p>
        <p
            :class="
            rows.filter((r) => hasIncompatible(r) || chainBroken(r)).length > 0
              ? 'text-red-600'
              : 'text-slate-900'
          "
            class="text-lg font-bold"
            data-testid="p4-tf-abnormal"
        >
          {{ rows.filter((r) => hasIncompatible(r) || chainBroken(r)).length }}
        </p>
        <p class="text-[11px] text-slate-400">配血不合不可发血；已完成必须有病历号</p>
      </div>
    </div>

    <!-- 筛选 + 表格 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <el-select
            v-model="filters.transfusionStatus"
            class="!w-36"
            clearable
            data-testid="p4-tf-filter-status"
            placeholder="流程状态"
            @change="handleSearch"
        >
          <el-option :value="0" label="待配血"/>
          <el-option :value="1" label="已配血"/>
          <el-option :value="2" label="已发血"/>
          <el-option :value="3" label="输注中"/>
          <el-option :value="4" label="已完成"/>
          <el-option :value="5" label="已取消"/>
        </el-select>
        <el-select
            v-model="filters.crossmatchStatus"
            class="!w-40"
            clearable
            data-testid="p4-tf-filter-crossmatch"
            placeholder="配血状态"
            @change="handleSearch"
        >
          <el-option :value="0" label="待配血"/>
          <el-option :value="1" label="配血中（未配齐）"/>
          <el-option :value="2" label="全部相合"/>
          <el-option :value="3" label="存在配血不合"/>
        </el-select>
        <el-select
            v-model="filters.bloodComponent"
            class="!w-36"
            clearable
            data-testid="p4-tf-filter-component"
            placeholder="血液品种"
            @change="handleSearch"
        >
          <el-option v-for="c in components" :key="c.code" :label="c.label" :value="c.code"/>
        </el-select>
        <el-select
            v-model="filters.patientAbo"
            class="!w-32"
            clearable
            data-testid="p4-tf-filter-abo"
            placeholder="受血者血型"
            @change="handleSearch"
        >
          <el-option v-for="t in ABO_TYPES" :key="t" :label="`${t} 型`" :value="t"/>
        </el-select>
        <el-select
            v-model="filters.hasReaction"
            class="!w-36"
            clearable
            data-testid="p4-tf-filter-reaction"
            placeholder="输血反应"
            @change="handleSearch"
        >
          <el-option :value="0" label="未上报反应"/>
          <el-option :value="1" label="已上报有反应"/>
        </el-select>
        <el-input
            v-model="filters.keyword"
            :prefix-icon="Search"
            class="!w-72"
            clearable
            placeholder="输血单号 / 入院号 / 患者 / 目的 / 指征"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
        />
        <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" data-testid="p4-tf-table" style="width: 100%">
        <el-table-column label="用血单号" prop="applyNo" width="150"/>
        <el-table-column label="患者" min-width="140">
          <template #default="{ row }">
            <div class="text-slate-900">{{ text(row.patientName) }}</div>
            <div class="text-[11px] text-slate-400">{{ text(row.admissionNo) }}</div>
            <div class="text-[11px] text-slate-400">
              {{ text(row.applyBedNo) }}床 · {{ text(row.applyDeptName) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="血型 / 品种" min-width="180">
          <template #default="{ row }">
            <div class="font-medium text-slate-900">{{ text(row.bloodTypeText) }}</div>
            <div class="text-[11px] text-slate-600">
              {{ text(row.bloodComponentText) }}
              <span v-if="row.componentSpec" class="text-slate-400">（{{ row.componentSpec }}）</span>
            </div>
            <div class="text-[11px] text-slate-400">
              申请 {{ text(row.bagCount) }} 袋
              <span v-if="row.plannedAmount">
                / {{ row.plannedAmount }}{{ row.amountUnit || '' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用血审批" min-width="150">
          <template #default="{ row }">
            <el-tag
                :type="approveTagType(row.approveStatus)"
                data-testid="p4-tf-approve-status"
                effect="plain"
                size="small"
            >
              {{ text(row.approveStatusText) }}
            </el-tag>
            <div class="mt-1 text-[11px] text-slate-500">
              {{ text(row.approveLevelText) }}
              <span v-if="row.amountMl != null">· {{ row.amountMl }}ml</span>
            </div>
            <div v-if="row.approveMakeup === 1" class="text-[11px] text-amber-600">急诊后补</div>
            <div v-if="row.approveRejectReason" class="text-[11px] text-red-600">
              驳回：{{ row.approveRejectReason }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="配血" min-width="170">
          <template #default="{ row }">
            <el-tag :type="crossmatchTagType(row.crossmatchStatus)" effect="plain" size="small">
              {{ text(row.crossmatchStatusText) }}
            </el-tag>
            <div class="mt-1 text-[11px] text-slate-500">{{ text(row.bagProgressText) }}</div>
            <div v-if="row.crossmatchDoctorName" class="text-[11px] text-slate-400">
              由 {{ row.crossmatchDoctorName }} 于 {{ fmt(row.crossmatchTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态 / 进度" min-width="190">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.transfusionStatus)" effect="plain" size="small">
              {{ text(row.transfusionStatusText) }}
            </el-tag>
            <div class="mt-1 text-[11px] text-slate-500">{{ stageText(row) }}</div>
            <div v-if="row.waitText && row.transfusionStatus !== 4" class="text-[11px] text-slate-400">
              {{ row.waitText }}
            </div>
            <div v-if="row.stalled" class="mt-1 text-[11px] font-medium text-red-600">
              <el-icon class="mr-1 align-middle">
                <Warning/>
              </el-icon>
              {{ text(row.stalledText) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="双人核对 / 输注" min-width="180">
          <template #default="{ row }">
            <div v-if="!row.checkNurseName" class="text-[11px] text-slate-400">—（尚未核对）</div>
            <template v-else>
              <div class="text-[11px] text-slate-700">
                {{ row.checkNurseName }} / {{ text(row.checkNurse2Name) }}
              </div>
              <div class="text-[11px] text-slate-400">{{ fmt(row.checkTime) }}</div>
              <div v-if="row.actualAmount" class="text-[11px] text-slate-500">
                实输 {{ row.actualAmount }}{{ row.amountUnit || '' }}
                <span v-if="row.durationText">（{{ row.durationText }}）</span>
              </div>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="反应 / 回写病历" min-width="170">
          <template #default="{ row }">
            <div v-if="row.hasReaction === 1" class="text-[11px] font-medium text-red-600">
              <el-icon class="mr-1 align-middle">
                <Warning/>
              </el-icon>
              {{ text(row.reactionType) }}
            </div>
            <div v-else class="text-[11px] text-slate-400">{{ text(row.hasReactionText) }}</div>
            <div v-if="row.recordNo" class="mt-1 text-[11px] text-slate-700">
              病历号 {{ row.recordNo }}
            </div>
            <div v-else-if="row.transfusionStatus === 4" class="mt-1 text-[11px] text-red-600">
              回写链不完整（无病历号）
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="250">
          <template #default="{ row }">
            <el-button
                v-if="row.canApprove"
                v-perm="'medtech:transfusion:edit'"
                data-testid="p4-tf-approve"
                link
                type="success"
                @click="openApprove(row)"
            >
              审批
            </el-button>
            <el-button
                v-if="row.canCrossmatch"
                v-perm="'medtech:transfusion:edit'"
                data-testid="p4-tf-crossmatch"
                link
                type="warning"
                @click="openCrossmatch(row)"
            >
              配血
            </el-button>
            <el-button
                v-if="row.canIssue"
                v-perm="'medtech:transfusion:edit'"
                data-testid="p4-tf-issue"
                link
                type="primary"
                @click="openIssue(row)"
            >
              发血
            </el-button>
            <el-button
                v-if="row.canStart"
                v-perm="'medtech:transfusion:edit'"
                data-testid="p4-tf-start"
                link
                type="primary"
                @click="openStart(row)"
            >
              开始输注
            </el-button>
            <el-button
                v-if="row.canFinish"
                v-perm="'medtech:transfusion:edit'"
                data-testid="p4-tf-finish"
                link
                type="success"
                @click="openFinish(row)"
            >
              登记完成
            </el-button>
            <el-button
                v-if="row.canReportReaction"
                v-perm="'medtech:transfusion:add'"
                data-testid="p4-tf-reaction"
                link
                type="danger"
                @click="openReaction(row)"
            >
              上报反应
            </el-button>
            <el-button
                v-if="row.canEdit"
                v-perm="'medtech:transfusion:edit'"
                data-testid="p4-tf-edit"
                link
                type="primary"
                @click="openApply(row)"
            >
              修改
            </el-button>
            <el-button
                v-if="row.canCancel"
                v-perm="'medtech:transfusion:delete'"
                data-testid="p4-tf-cancel"
                link
                type="danger"
                @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button data-testid="p4-tf-detail" link type="info" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400" data-testid="p4-tf-empty">
            暂无用血申请（点击右上角「发起用血申请」开始）
          </div>
        </template>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @current-change="loadList"
            @size-change="handleSearch"
        />
      </div>
    </div>

    <!-- 发起 / 修改用血申请 -->
    <TransfusionApplyEdit
        v-model="applyVisible"
        :abo-types="ABO_TYPES"
        :admission-label="admissionLabel"
        :admissions="admissions"
        :components="components"
        :default-admission-id="filters.admissionId"
        :rh-types="RH_TYPES"
        :row="applyRow"
        @saved="reloadAfterSave"
    />

    <!-- 用血分级审批 -->
    <TransfusionApprove
        v-model="approveVisible"
        :row="approveTarget"
        @saved="reloadAfterSave"
    />

    <!-- 配血 -->
    <TransfusionCrossMatch
        v-model="crossVisible"
        :abo-types="ABO_TYPES"
        :rh-types="RH_TYPES"
        :row="crossTarget"
        @saved="reloadAfterSave"
    />

    <!-- 发血 -->
    <TransfusionIssue
        v-model="issueVisible"
        :row="issueTarget"
        @saved="reloadAfterSave"
    />

    <!-- 开始输注（双人核对） -->
    <TransfusionStart
        v-model="startVisible"
        :row="startTarget"
        @saved="reloadAfterSave"
    />

    <!-- 登记完成 -->
    <TransfusionFinish
        v-model="finishVisible"
        :row="finishTarget"
        @saved="reloadAfterSave"
    />

    <!-- 输血反应上报 -->
    <TransfusionReaction
        v-model="reactionVisible"
        :row="reactionTarget"
        @saved="loadList"
    />

    <!-- 详情 -->
    <TransfusionDetail
        v-model="detailVisible"
        :id="detailId"
    />
  </div>
</template>

<script setup>
/**
 * 住院输血闭环（P4.4：申请 → 配血 → 发血 → 双人核对输注 → 完成 → 反应上报）
 *
 * 这个页面替代了原来的演示壳：它写死患者「马建军」「孙美玲」、输血单号「SX-2026-2201」、
 * 库存 126U、状态「输血中」—— 整页没有一个字来自数据库，而全库当时**一张输血表都没有**。
 * 按项目规范（AGENTS.md §2），这种页面必须重写为真实接口驱动。
 *
 * 九条口径：
 * 1. 状态机：0-待配血 → 1-已配血 → 2-已发血 → 3-输注中 → 4-已完成；0/1/2 → 5-已取消。
 * 2. **按钮可用性由后端给**（canCrossmatch / canIssue / canStart / canFinish / canCancel /
 *    canReportReaction），不按 transfusionStatus 码值 switch。
 * 3. **两个状态都要显示**：流程状态（走到哪一步）与配血状态（血配好没、合不合）。
 *    配血不合时流程会停在「待配血」，只显示流程状态就把"配了、但不合"看成"还没配"。
 * 4. **申请 ≠ 配血 ≠ 发血**：申请只登记"给谁、什么品种、多少袋、为什么"；
 *    血袋号与交叉配血结果是输血科的动作；双人核对与输注是护理岗的动作。
 * 5. **ABO / Rh 不相容会被后端整批拒绝** —— 直接展示后端文案，不要改写成"配血失败"。
 *    但「交叉配血结论不合」后端仍返回成功、数据会落库（消息里写明 N 袋不合不能发血），
 *    两者处理方式不同。
 * 6. **输注前必须双人核对**：两个护士不能是同一个人，1~6 项必核项一项不能缺。
 * 7. **完成才回写**：一次事务写 record_type=11 输血记录病历 + 病案首页 is_transfusion=1。
 *    列表的「病历号」就是这条链的证据，缺了就是链断了（后端会标红）。
 * 8. 有反应不改历史：完成之后用「上报反应」补登记，不要在输注中/未完成状态报。
 * 9. 所有 ID 都是字符串（雪花ID），不要 Number()。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, Warning} from '@element-plus/icons-vue';
import {
  cancelTransfusion,
  getBloodComponentList,
  getTransfusionApplyListPage,
  getTransfusionUnfinishedCount,
} from '@/api/inpatientTransfusion';
import {getInpatientListPage} from '@/api/inpatient';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import TransfusionApplyEdit from './TransfusionApplyEdit.vue';
import TransfusionApprove from './TransfusionApprove.vue';
import TransfusionCrossMatch from './TransfusionCrossMatch.vue';
import TransfusionDetail from './TransfusionDetail.vue';
import TransfusionFinish from './TransfusionFinish.vue';
import TransfusionIssue from './TransfusionIssue.vue';
import TransfusionReaction from './TransfusionReaction.vue';
import TransfusionStart from './TransfusionStart.vue';

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
const ABO_TYPES = ['A', 'B', 'O', 'AB'];
const RH_TYPES = ['阳', '阴'];
// ---------------- 基础数据 ----------------
const admissions = ref([]);
const components = ref([]);
const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.deptName || a.wardName || '—'}）`;
const loadBaseData = async () => {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200});
    admissions.value = (res.data?.records || []);
  } catch (error) {
    console.error('加载在院患者失败:', error);
  }
  try {
    const res = await getBloodComponentList();
    components.value = (res.data || []);
  } catch (error) {
    console.error('加载血液品种字典失败:', error);
  }
};
// ---------------- 列表 ----------------
const rows = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(DEFAULT_PAGE_SIZE);
const loading = ref(false);
const unfinishedCount = ref(0);
const filters = reactive({
  admissionId: '',
  transfusionStatus: '',
  crossmatchStatus: '',
  bloodComponent: '',
  patientAbo: '',
  hasReaction: '',
  keyword: '',
});
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getTransfusionApplyListPage({
      admissionId: filters.admissionId || undefined,
      transfusionStatus: filters.transfusionStatus === '' ? undefined : filters.transfusionStatus,
      crossmatchStatus: filters.crossmatchStatus === '' ? undefined : filters.crossmatchStatus,
      bloodComponent: filters.bloodComponent === '' ? undefined : filters.bloodComponent,
      patientAbo: filters.patientAbo || undefined,
      hasReaction: filters.hasReaction === '' ? undefined : filters.hasReaction,
      keyword: filters.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total || 0);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};
const loadUnfinishedCount = async () => {
  try {
    const res = await getTransfusionUnfinishedCount({});
    unfinishedCount.value = Number(res.data || 0);
  } catch (error) {
    console.error('加载未完成用血数失败:', error);
  }
};
/** 每单的进行位置（列表里一眼看出卡在哪一步）——纯后端文案拼装，前端不加业务判断 */
const stageText = (row) => {
  if (row.transfusionStatus === 0) {
    return row.crossmatchStatus === 3 ? '① 配血不合，等待换血源重配' : '① 等待输血科配血';
  }
  if (row.transfusionStatus === 1)
    return '② 已配血，等待发血';
  if (row.transfusionStatus === 2)
    return '③ 已发血，等待双人核对后输注';
  if (row.transfusionStatus === 3)
    return '④ 输注中，等待登记完成';
  if (row.transfusionStatus === 4)
    return '⑤ 已完成并回写';
  return '已取消';
};
/** 配血不合：必须能一眼看见（流程状态会停在"待配血"，单看状态会把不合看成没配） */
const hasIncompatible = (row) => row.crossmatchStatus === 3;
/** 回写链断了：已完成却没有病历锚点 */
const chainBroken = (row) => row.transfusionStatus === 4 && !row.recordId;
const statusTagType = (status) => {
  if (status === 4)
    return 'success';
  if (status === 5)
    return 'info';
  if (status === 3)
    return 'warning';
  if (status === 0)
    return 'danger';
  return 'primary';
};
const crossmatchTagType = (status) => {
  if (status === 2)
    return 'success';
  if (status === 3)
    return 'danger';
  if (status === 1)
    return 'warning';
  return 'info';
};
/** 审批状态 tag：0待审批- danger 1已通过- success 2已驳回- info 3急诊待补审- warning */
const approveTagType = (status) => {
  if (status === 1)
    return 'success';
  if (status === 2)
    return 'info';
  if (status === 3)
    return 'warning';
  if (status === 0)
    return 'danger';
  return 'info';
};
const handleSearch = () => {
  pageNum.value = 1;
  loadList();
};
const resetFilters = () => {
  filters.admissionId = '';
  filters.transfusionStatus = '';
  filters.crossmatchStatus = '';
  filters.bloodComponent = '';
  filters.patientAbo = '';
  filters.hasReaction = '';
  filters.keyword = '';
  pageNum.value = 1;
  loadList();
};
const reloadAfterSave = () => Promise.all([loadList(), loadUnfinishedCount()]);
// ---------------- 一、发起 / 修改用血申请 ----------------
const applyVisible = ref(false);
const applyRow = ref(null);
const openApply = (row) => {
  applyRow.value = row || null;
  applyVisible.value = true;
};
// ---------------- 一·五、用血分级审批（sql/93） ----------------
const approveVisible = ref(false);
const approveTarget = ref(null);
const openApprove = (row) => {
  approveTarget.value = row;
  approveVisible.value = true;
};
// ---------------- 二、配血（逐袋） ----------------
const crossVisible = ref(false);
const crossTarget = ref(null);
const openCrossmatch = (row) => {
  crossTarget.value = row;
  crossVisible.value = true;
};
// ---------------- 三、发血 ----------------
const issueVisible = ref(false);
const issueTarget = ref(null);
const openIssue = (row) => {
  issueTarget.value = row;
  issueVisible.value = true;
};
// ---------------- 四、开始输注（双人核对） ----------------
const startVisible = ref(false);
const startTarget = ref(null);
const openStart = (row) => {
  startTarget.value = row;
  startVisible.value = true;
};
// ---------------- 五、完成（回写病历） ----------------
const finishVisible = ref(false);
const finishTarget = ref(null);
const openFinish = (row) => {
  finishTarget.value = row;
  finishVisible.value = true;
};
// ---------------- 六、输血反应上报 ----------------
const reactionVisible = ref(false);
const reactionTarget = ref(null);
const openReaction = (row) => {
  reactionTarget.value = row;
  reactionVisible.value = true;
};
// ---------------- 取消 ----------------
const handleCancel = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`取消用血单 ${row.applyNo}（${text(row.patientName)} ${text(row.bloodComponentText)}）请填写原因。`, '取消用血申请', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '如：患者转院 / 输血指征复查后不需要 / 血源不足改期',
      inputValidator: (v) => (v && v.trim() ? true : '取消原因不能为空'),
    });
    await cancelTransfusion({applyId: row.id, cancelReason: String(value).trim()});
    ElMessage.success('用血申请已取消');
    await Promise.all([loadList(), loadUnfinishedCount()]);
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel')
      return;
    
  }
};
// ---------------- 详情 ----------------
const detailVisible = ref(false);
const detailId = ref('');
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
onMounted(() => {
  loadBaseData();
  loadList();
  loadUnfinishedCount();
});
</script>
