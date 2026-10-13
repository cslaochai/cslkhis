<template>
  <div>
    <el-card class="stat-card mb-3" shadow="never">
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-label">登记总数</span>
          <span class="stat-value">{{ stats.total }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">未结案</span>
          <span class="stat-value warn">{{ stats.openCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已结案</span>
          <span class="stat-value ok">{{ stats.closedCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已撤销</span>
          <span class="stat-value">{{ stats.revokedCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">赔偿合计（元）</span>
          <span class="stat-value money">{{ stats.compensationTotal }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">平均结案天数</span>
          <span class="stat-value">{{ stats.avgCloseDays }}</span>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="单号 / 患者 / 投诉人" style="width: 200px"
                      @keyup.enter="loadList"/>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.caseType" clearable placeholder="类型" style="width: 130px">
              <el-option v-for="d in dicts.caseType" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="状态" style="width: 130px">
              <el-option v-for="d in dicts.status" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="等级">
            <el-select v-model="query.level" clearable placeholder="等级" style="width: 120px">
              <el-option v-for="d in dicts.level" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="被投诉科室">
            <el-select v-model="query.deptId" clearable filterable placeholder="被投诉科室" style="width: 180px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="登记日期">
            <el-date-picker v-model="query.dateRange" end-placeholder="登记截止" start-placeholder="登记起始"
                            style="width: 240px" type="daterange" value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.openOnly">仅未结案</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'qc:dispute:add'" :icon="Plus" type="primary" @click="openCreate">登记</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="单号" prop="caseNo" width="150"/>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.caseType) === 1 ? 'info' : 'danger'" size="small">{{
                caseTypeText(row.caseType)
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ levelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="被投诉科室" prop="deptName" show-overflow-tooltip width="150"/>
        <el-table-column label="投诉人" prop="complainant" width="100"/>
        <el-table-column label="联系电话" prop="complainantTel" width="130"/>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="病历封存" width="140">
          <template #default="{ row }">
            <el-tag v-if="Number(row.needSeal) === 1" :type="sealTagType(row.sealStatus)" size="small">
              {{ sealText(row.sealStatus) }}
            </el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="处理途径" width="120">
          <template #default="{ row }">{{ row.dealType ? dealTypeText(row.dealType) : '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="赔偿(元)" width="100">
          <template #default="{ row }">{{ row.compensation ?? '—' }}</template>
        </el-table-column>
        <el-table-column align="right" label="受理天数" width="90">
          <template #default="{ row }">{{ row.openDays ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="登记时间" prop="createTime" width="160"/>
        <el-table-column fixed="right" label="操作" width="370">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="openDetail(row)">详情
            </el-button>
            <el-button v-if="row.canAccept" v-perm="'qc:dispute:edit'" link size="small" type="primary"
                       @click.stop="openAct(row, 'accept')">受理
            </el-button>
            <el-button v-if="row.canFollow" v-perm="'qc:dispute:edit'" link size="small" type="primary"
                       @click.stop="openAct(row, 'follow')">处理跟踪
            </el-button>
            <el-button v-if="row.canClose" v-perm="'qc:dispute:edit'" link size="small" type="success"
                       @click.stop="openClose(row)">结案
            </el-button>
            <el-button v-if="row.canSeal" v-perm="'qc:dispute:edit'" link size="small" type="warning"
                       @click.stop="doSeal(row)">补封存
            </el-button>
            <el-button v-if="row.canRevoke" v-perm="'qc:dispute:edit'" link size="small" type="info"
                       @click.stop="openAct(row, 'revoke')">撤销
            </el-button>
            <el-button v-if="row.canEdit" v-perm="'qc:dispute:add'" link size="small" type="primary"
                       @click.stop="openEdit(row)">编辑
            </el-button>
            <el-button v-if="row.canDelete" v-perm="'qc:dispute:add'" link size="small" type="danger"
                       @click.stop="remove(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无纠纷/投诉登记"/>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <DisputeEdit v-model="editVisible" :row="editRow" :dicts="dicts" :dept-options="deptOptions" @saved="onSaved"/>

    <DisputeDetail v-model="detailVisible" :id="detailId" :dicts="dicts"/>

    <DisputeAct v-model="actVisible" :row="actRow" :act-key="actKey" @saved="onSaved"/>

    <DisputeClose v-model="closeVisible" :row="closeRow" :dicts="dicts" @saved="onSaved"/>
  </div>
</template>

<script setup>
/**
 * 医疗纠纷 / 投诉登记（L17）
 *
 * 闭环：登记（待受理）→ 受理（调查中，按需封存病历）→ 调查/处理跟踪（处理中）
 *      → 结案（终态，收口途径/责任/赔偿）/ 撤销（终态，原因必填）
 *
 * 五条口径：
 * 1. **按钮可用性一律读后端 can* 字段**（canEdit/canAccept/canFollow/canClose/canRevoke/canSeal/canDelete），
 *    不按 status 码值 switch —— 后端加一档状态，switch 会静默渲染成"看着正常"的错按钮。
 * 2. **受理联动封存病历**：needSeal=1 时后端自动封存该患者已归档病案，回写 sealStatus=1；
 *    暂无已归档病案 → sealStatus=2（待归档后封存）。这一列必须让用户看见，
 *    否则会出现"以为封了其实没封"——纠纷里这是致命的。
 * 3. 结案四项必填（处理途径 + 责任认定 + 赔偿金额 + 结论）：无赔偿填 0，不允许留空蒙混。
 * 4. 投诉人电话后端已脱敏，前端原样展示，不再裁一遍。
 * 5. 「受理天数」openDays 服务端算，前端不重算时间差。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {deleteDispute, getDisputeStat, listDisputePage, sealDisputeNow} from '@/api/dispute';
import {getDepartmentSelectList} from '@/api/system';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {fetchDisputeDicts, levelTagType, sealTagType, statusTagType} from '@/lib/dispute';
import DisputeEdit from './DisputeEdit.vue';
import DisputeDetail from './DisputeDetail.vue';
import DisputeAct from './DisputeAct.vue';
import DisputeClose from './DisputeClose.vue';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '',
  caseType: null,
  status: null,
  level: null,
  deptId: null,
  openOnly: false,
  dateRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const stats = reactive({
  total: 0, openCount: 0, closedCount: 0, revokedCount: 0,
  compensationTotal: '0', avgCloseDays: '0',
});
// 字典：页面一次拉取（8 张，lib 内分两批），以 prop 传给各弹框；标签颜色口径（lib）列表与详情共用
const dicts = ref({caseType: [], source: [], status: [], level: [], dealType: [], duty: [], relation: [], seal: []});
const caseTypeText = (v) => dictLabelText(dicts.value.caseType, v);
const statusText = (v) => dictLabelText(dicts.value.status, v);
const levelText = (v) => dictLabelText(dicts.value.level, v);
const dealTypeText = (v) => dictLabelText(dicts.value.dealType, v);
const sealText = (v) => dictLabelText(dicts.value.seal, v);
const deptOptions = ref([]);
const loadDicts = async () => {
  try {
    dicts.value = await fetchDisputeDicts();
  } catch (e) {
    console.error('加载纠纷字典失败', e);
  }
};
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    if (res.code === 200)
      deptOptions.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
const loadStats = async () => {
  try {
    const res = await getDisputeStat();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listDisputePage({
      keyword: query.keyword.trim() || undefined,
      caseType: query.caseType ?? undefined,
      status: query.status ?? undefined,
      level: query.level ?? undefined,
      deptId: query.deptId || undefined,
      openOnly: query.openOnly || undefined,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1],
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.keyword = '';
  query.caseType = null;
  query.status = null;
  query.level = null;
  query.deptId = null;
  query.openOnly = false;
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// ------------------------------------------------------------------
// 弹框编排：表单状态 / 校验 / 提交均在子组件，页面只管开关与刷新
// ------------------------------------------------------------------
const editVisible = ref(false);
const editRow = ref(null);
const detailVisible = ref(false);
const detailId = ref('');
const actVisible = ref(false);
const actRow = ref(null);
const actKey = ref('accept');
const closeVisible = ref(false);
const closeRow = ref(null);
const onSaved = () => {
  loadList();
  loadStats();
};
const openCreate = () => {
  editRow.value = null;
  editVisible.value = true;
};
const openEdit = (row) => {
  editRow.value = row;
  editVisible.value = true;
};
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
const openAct = (row, key) => {
  actRow.value = row;
  actKey.value = key;
  actVisible.value = true;
};
const openClose = (row) => {
  closeRow.value = row;
  closeVisible.value = true;
};
// ------------------------------------------------------------------
// 补封存 / 删除
// ------------------------------------------------------------------
const doSeal = async (row) => {
  try {
    await ElMessageBox.confirm('将封存该患者已归档病历，确认继续？', '封存确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await sealDisputeNow({id: row.id});
    if (res.code === 200) {
      ElMessage.success('病历已封存');
      loadList();
    } else {
      ElMessage.error(res.message || '封存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('封存失败');
  }
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除单据 ${row.caseNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteDispute(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
onMounted(async () => {
  await Promise.all([loadDicts(), loadDepts()]);
  await loadList();
  await loadStats();
});
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.stat-row {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-value.warn {
  color: #e6a23c;
}

.stat-value.ok {
  color: #67c23a;
}

.stat-value.money {
  color: #f56c6c;
}
</style>
