<template>
  <div data-testid="yb-chronic-page">
    <!-- 台账汇总：数字全部来自后端 SQL 聚合 -->
    <el-row :gutter="12" class="mb-3">
      <el-col :span="6">
        <el-card class="!rounded-lg" data-testid="chronic-sum-valid" shadow="never">
          <div class="text-xs text-slate-500">在有效期的备案</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.validCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="!rounded-lg cursor-pointer" data-testid="chronic-sum-expired" shadow="never"
                 @click="filterExpiredOnly">
          <div class="text-xs text-slate-500">已过期待续备</div>
          <div :class="(summary.expiredCount ?? 0) > 0 ? 'text-red-600' : ''" class="text-2xl font-semibold mt-1">
            {{ summary.expiredCount ?? 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="!rounded-lg" shadow="never">
          <div class="text-xs text-slate-500">已注销</div>
          <div class="text-2xl font-semibold mt-1 text-slate-500">{{ summary.cancelledCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="!rounded-lg" shadow="never">
          <div class="text-xs text-slate-500">已驳回</div>
          <div class="text-2xl font-semibold mt-1 text-amber-600">{{ summary.rejectedCount ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <!-- ==================== 备案台账筛选 ==================== -->
      <div class="flex items-start justify-between gap-4">
        <el-form :model="regQuery" inline @submit.prevent>
          <el-form-item label="状态">
            <el-select v-model="regQuery.regStatus" clearable data-testid="chronic-filter-status" placeholder="全部状态"
                       style="width: 140px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_CHRONIC_STATUS)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="类别">
            <el-select v-model="regQuery.diseaseType" clearable placeholder="全部类别" style="width: 130px">
              <el-option v-for="d in dictOptions(DICT_TYPE.CHRONIC_DISEASE_TYPE)" :key="d.dictValue"
                         :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="病种">
            <el-select v-model="regQuery.catalogId" clearable data-testid="chronic-filter-catalog" filterable placeholder="全部病种"
                       style="width: 210px">
              <el-option v-for="c in catFilterOptions" :key="c.id" :label="`${c.diseaseCode} ${c.diseaseName}`"
                         :value="String(c.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="regQuery.onlyExpired" data-testid="chronic-only-expired">只看已过期待续备
            </el-checkbox>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="regQuery.keyword" clearable data-testid="chronic-keyword" placeholder="单号/患者/病种/经办人"
                      style="width: 220px"
                      @keyup.enter="regPage.pageNum = 1; loadRegs()"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="chronic-search-btn" type="primary"
                       @click="regPage.pageNum = 1; loadRegs()">查询
            </el-button>
            <el-button :icon="Refresh" @click="resetRegQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span v-if="expiringSoonCount > 0" class="text-xs text-amber-600 mr-2" data-testid="chronic-expiring-hint">
            本页 {{ expiringSoonCount }} 条 30 天内到期
          </span>
          <el-button v-perm="'finance:insuranceChronic:add'" :icon="Plus" data-testid="chronic-create-btn"
                     type="primary" @click="openRegCreate">登记备案
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <!-- ==================== 备案台账 ==================== -->
      <el-table v-loading="regLoading" :data="regRows" :max-height="tableMaxHeight" data-testid="chronic-table"
                stripe style="width: 100%" @row-click="openRegDetail">
        <el-table-column class-name="font-mono" label="备案单号" prop="regNo" width="150"/>
        <el-table-column label="患者" show-overflow-tooltip width="150">
          <template #default="{row}">
            <div>{{ row.patientName }}</div>
            <div class="text-xs text-slate-400">{{ row.patientNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="病种" min-width="200" show-overflow-tooltip>
          <template #default="{row}">
            <el-tag :type="row.diseaseType === 2 ? 'danger' : 'info'" class="mr-1" size="small">
              {{ dictText(DICT_TYPE.CHRONIC_DISEASE_TYPE, row.diseaseType) }}
            </el-tag>
            <span>{{ row.diseaseName }}</span>
            <div class="text-xs text-slate-400 font-mono">{{ row.diseaseCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="诊断" show-overflow-tooltip width="160">
          <template #default="{row}">
            <div class="text-xs">{{ row.certifyDeptName || '—' }} / {{ row.certifyDoctorName || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.certifyDate }}</div>
          </template>
        </el-table-column>
        <el-table-column label="备案经办人" show-overflow-tooltip width="130">
          <template #default="{row}">
            <div data-testid="chronic-emp-name">{{ row.registerEmpName }}</div>
            <div class="text-xs text-slate-400">{{ row.registerDeptName || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="待遇有效期" width="180">
          <template #default="{row}">
            <div class="text-xs">{{ validPeriod(row) }}</div>
            <el-tag v-if="row.displayStatus === ST_EXPIRED" data-testid="chronic-expired-tag" size="small"
                    type="danger">
              已过期 {{ Math.abs(row.remainDays) }} 天
            </el-tag>
            <el-tag v-else-if="row.longTerm" class="!ml-0" size="small" type="success">长期</el-tag>
            <span v-else-if="row.remainDays != null" :class="row.remainDays <= 30 ? 'text-amber-600' : 'text-slate-400'"
                  class="text-xs">剩 {{ row.remainDays }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :data-testid="`chronic-status-${row.regNo}`" :type="statusTagType(row.displayStatus)" size="small">
              {{ statusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="230">
          <template #default="{row}">
            <el-button :data-testid="`chronic-view-${row.regNo}`" :icon="View" plain size="small"
                       @click.stop="openRegDetail(row)">详情
            </el-button>
            <el-button v-if="canEditReg(row)" v-perm="'finance:insuranceChronic:add'" :data-testid="`chronic-edit-${row.regNo}`" :icon="Edit" plain
                       size="small"
                       type="primary" @click.stop="openRegEdit(row)">编辑
            </el-button>
            <el-button v-if="canRejectReg(row)" v-perm="'finance:insuranceChronic:cancel'" :data-testid="`chronic-reject-${row.regNo}`" plain
                       size="small"
                       type="warning" @click.stop="terminalReg(row, 'reject')">驳回
            </el-button>
            <el-button v-if="canCancelReg(row)" v-perm="'finance:insuranceChronic:cancel'" :data-testid="`chronic-cancel-${row.regNo}`" plain
                       size="small"
                       type="danger" @click.stop="terminalReg(row, 'cancel')">注销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="regPage.pageNum" v-model:page-size="regPage.pageSize"
                       :page-sizes="PAGE_SIZES" :total="regPage.total" data-testid="chronic-pagination"
                       layout="total, sizes, prev, pager, next, jumper" @current-change="loadRegs"
                       @size-change="regPage.pageNum = 1; loadRegs()"/>
      </div>
    </el-card>

    <!-- ==================== 备案 新建/编辑（拆到 InsuranceChronicEdit） ==================== -->
    <InsuranceChronicEdit v-model="regFormVisible" :row="regEditRow" @saved="refreshAfterSave"/>

    <!-- ==================== 备案详情（只读）（拆到 InsuranceChronicDetail） ==================== -->
    <InsuranceChronicDetail v-model="regDetailVisible" :id="regDetailId" :dicts="dicts"
                            :status-tag-type="statusTagType" :status-text="statusText" :valid-period="validPeriod"/>

  </div>
</template>

<script setup>
/**
 * 慢特病医保备案（菜单 1011，sql/163；sql/188 由「慢特病人员备案」更名 —— 加「医保」二字与
 * 门诊的「慢病建档与认定」（菜单 208，临床口径）区分：本页只管待遇资格，不管临床建档）
 *
 * 这张台账只回答一个问题：「这个人的门特资格是谁办的、还在不在有效期内」。
 * 经办人留空由服务端回填当前登录人（谁点保存就是谁办的）；改成外部机构经办人必须在备注写明原因。
 * 过期是展示态（服务端按当天现算，库里只有 1有效/2已注销/3已驳回），页面据此提示续备；
 * 注销/驳回是终态不可逆，要改内容只能另起新单——所以这两个动作只做状态流转，不提供编辑。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {useRoute} from 'vue-router';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Edit, Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {
  cancelChronicReg,
  getChronicCatalogList,
  getChronicRegList,
  getChronicRegSummary,
  rejectChronicReg
} from '@/api/insuranceChronic';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import InsuranceChronicEdit from './InsuranceChronicEdit.vue';
import InsuranceChronicDetail from './InsuranceChronicDetail.vue';

const route = useRoute();
// 库里只有三态，4-已过期 是服务端现算的展示态，只用于标签配色与筛选提示
const ST_VALID = 1, ST_CANCELLED = 2, ST_REJECTED = 3, ST_EXPIRED = 4;
const CHRONIC_DICT_TYPES = [DICT_TYPE.CHRONIC_DISEASE_TYPE, DICT_TYPE.YB_CHRONIC_STATUS];
const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];
const dictText = (type, value) => dictLabelText(dicts.value[type], value);
// ==================== 汇总（后端 SQL 聚合，禁止前端自算） ====================
const summary = ref({});
const loadSummary = async () => {
  try {
    const res = await getChronicRegSummary();
    summary.value = res.data || {};
  } catch (error) {
    ElMessage.error(error?.message || '备案汇总加载失败');
  }
};
// ==================== 备案台账 ====================
const regLoading = ref(false);
const regRows = ref([]);
const regQuery = reactive({
  regStatus: undefined,
  diseaseType: undefined,
  catalogId: undefined,
  onlyExpired: false,
  keyword: ''
});
const regPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadRegs = async () => {
  regLoading.value = true;
  try {
    const res = await getChronicRegList({
      ...regQuery, onlyExpired: regQuery.onlyExpired || undefined,
      pageNum: regPage.pageNum, pageSize: regPage.pageSize
    });
    regRows.value = res.data?.records || [];
    regPage.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error(error?.message || '备案台账加载失败');
  } finally {
    regLoading.value = false;
  }
};
const resetRegQuery = () => {
  regQuery.regStatus = undefined;
  regQuery.diseaseType = undefined;
  regQuery.catalogId = undefined;
  regQuery.onlyExpired = false;
  regQuery.keyword = '';
  regPage.pageNum = 1;
  loadRegs();
};
const statusTagType = (s) => s === ST_VALID ? 'success' : s === ST_EXPIRED ? 'danger' : s === ST_REJECTED ? 'warning' : 'info';
const statusText = (row) => {
  if (!row)
    return '';
  return row.displayStatus === ST_EXPIRED ? '已过期' : dictText(DICT_TYPE.YB_CHRONIC_STATUS, row.regStatus);
};
const validPeriod = (row) => `${row.validStart} ~ ${row.longTerm ? '长期' : row.validEnd || ''}`;
// 终态不可逆：只有「有效」的单能改、能注销、能驳回
const canEditReg = (row) => row.regStatus === ST_VALID;
const canCancelReg = (row) => row.regStatus === ST_VALID;
const canRejectReg = (row) => row.regStatus === ST_VALID;
// ==================== 备案 新建/编辑（表单与候选加载已拆到 InsuranceChronicEdit，row 为空＝登记） ====================
const regFormVisible = ref(false);
const regEditRow = ref(null);
const refreshAfterSave = () => Promise.all([loadRegs(), loadSummary()]);
const openRegCreate = () => {
  regEditRow.value = null;
  regFormVisible.value = true;
};
const openRegEdit = (row) => {
  regEditRow.value = row;
  regFormVisible.value = true;
};
// ==================== 备案详情（只读，取数在 InsuranceChronicDetail 内） ====================
const regDetailVisible = ref(false);
const regDetailId = ref('');
const openRegDetail = (row) => {
  regDetailId.value = row.id;
  regDetailVisible.value = true;
};
// ==================== 注销 / 驳回 ====================
const terminalReg = (row, kind) => {
  const label = kind === 'cancel' ? '注销' : '驳回';
  const hint = kind === 'cancel'
      ? `注销备案「${row.regNo}」？注销后该患者此病种不能再走门特，终态不可逆，需重新备案。`
      : `驳回备案「${row.regNo}」？驳回是认定这次备案不成立，终态不可逆，补材料后另起新单。`;
  ElMessageBox.prompt(hint, `${label}确认`, {
    inputPlaceholder: `${label}原因（必填）`,
    inputValidator: (v) => (v && v.trim() ? true : `${label}原因不能为空`),
    type: 'warning'
  }).then(async ({value}) => {
    const payload = {id: row.id, reason: value.trim()};
    try {
      await (kind === 'cancel' ? cancelChronicReg(payload) : rejectChronicReg(payload));
      ElMessage.success(`已${label}`);
      await Promise.all([loadRegs(), loadSummary()]);
    } catch (error) {
      ElMessage.error(error?.message || `${label}失败`);
    }
  }).catch(() => {
  });
};
// ==================== 病种目录 ====================
// 台账筛选里按病种过滤要带停用病种的历史单，候选这里单独捞全量（一次性，不是分页查询）
const catFilterOptions = ref([]);
const loadCatFilterOptions = async () => {
  try {
    const res = await getChronicCatalogList({pageNum: 1, pageSize: 200});
    catFilterOptions.value = res.data?.records || [];
  } catch (error) {
    ElMessage.error(error?.message || '病种筛选候选加载失败');
  }
};
/** 待续备名单：点汇总卡片直接把台账筛成「有效但已过期」 */
const filterExpiredOnly = () => {
  regQuery.regStatus = undefined;
  regQuery.onlyExpired = true;
  regPage.pageNum = 1;
  loadRegs();
};
const expiringSoonCount = computed(() => regRows.value.filter((r) => r.regStatus === ST_VALID && r.remainDays != null && r.remainDays >= 0 && r.remainDays <= 30).length);
onMounted(async () => {
  dicts.value = await loadDictDataMap(CHRONIC_DICT_TYPES.join(','));
  // 「门诊慢特病病种目录」页的「看备案」跳过来时带 catalogId（sql/179 拆页后由路由参数承接，
  // 替代原先的页签内切 tab）
  const q = route.query.catalogId;
  if (q != null && q !== '')
    regQuery.catalogId = String(q);
  await Promise.all([loadRegs(), loadSummary(), loadCatFilterOptions()]);
});
</script>
