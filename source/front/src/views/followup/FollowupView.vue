<template>
  <div class="followup-page">
    <!-- 看板：今日该打多少电话、欠了多少 -->
    <el-card v-loading="statLoading" class="stat-card" shadow="never">
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-label">待随访</span>
          <span class="stat-value warn">{{ stat?.pendingCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">随访中</span>
          <span class="stat-value">{{ stat?.doingCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已完成</span>
          <span class="stat-value ok">{{ stat?.doneCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日应随访</span>
          <span class="stat-value">{{ stat?.todayDueCount ?? 0 }}</span>
        </div>
        <div class="stat-item clickable" title="点击筛出逾期未随访的任务" @click="gotoOverdue">
          <span class="stat-label">逾期未访</span>
          <span class="stat-value danger">{{ stat?.overdueCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日完成</span>
          <span class="stat-value ok">{{ stat?.doneTodayCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">完成率</span>
          <span class="stat-value">{{ stat?.completeRate ?? 0 }}%</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">随访带来的复诊号</span>
          <span class="stat-value">{{ stat?.revisitCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已取消</span>
          <span class="stat-value">{{ stat?.cancelledCount ?? 0 }}</span>
        </div>
      </div>
      <div class="stat-foot">
        <span class="text-xs text-gray-400">统计时点 {{
            fmtTime(stat?.statTime)
          }}（数字为全量口径，不随下方翻页变化）</span>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="随访任务" name="list">
          <div class="filter-bar">
            <el-input v-model="query.patientName" clearable placeholder="患者姓名" style="width: 160px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
            <el-select v-model="query.followupType" :fit-input-width="false" clearable placeholder="随访类型"
                       style="width: 130px">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="query.followupStatus" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-checkbox v-model="query.overdueOnly" @change="query.pageNum = 1; loadList()">仅逾期未访</el-checkbox>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
            <el-button v-perm="'inpatient:followup:add'" plain type="primary" @click="openGenerate">按出院记录生成
            </el-button>
            <el-button v-perm="'inpatient:followup:add'" type="primary" @click="openCreate">新建随访</el-button>
          </div>
          <el-table v-loading="loading" :data="rows" border data-testid="followup-table" size="small"
                    stripe>
            <el-table-column label="任务编号" prop="taskNo" width="200"/>
            <el-table-column label="患者" prop="patientName" width="120">
              <template #default="{ row }">{{ row.patientName }}<span
                  class="text-gray-400 text-xs ml-1">{{ row.patientNo }}</span></template>
            </el-table-column>
            <el-table-column label="联系电话" prop="phoneMasked" width="120">
              <template #default="{ row }">{{ row.phoneMasked || '—' }}</template>
            </el-table-column>
            <el-table-column label="出院科室" prop="deptName" show-overflow-tooltip width="140">
              <template #default="{ row }">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column label="类型" prop="followupType" width="100">
              <template #default="{ row }">{{ typeText(row.followupType) }}</template>
            </el-table-column>
            <el-table-column label="计划时间" prop="followupTime" width="150">
              <template #default="{ row }">
                <span :class="{ 'overdue-text': row.overdue }">{{ fmtTime(row.followupTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="诊断" min-width="140" prop="diagnosis" show-overflow-tooltip>
              <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" prop="followupStatus" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.followupStatus)">{{ statusText(row.followupStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="患者反馈" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.patientReply">
                  <span>{{ row.patientReply }}</span>
                  <span class="text-gray-400 text-xs ml-1">{{ fmtTime(row.patientReplyTime) }}</span>
                </template>
                <span v-else class="text-gray-400 text-xs">—</span>
              </template>
            </el-table-column>
            <el-table-column label="执行人" prop="executorName" width="90">
              <template #default="{ row }">{{ row.executorName || '—' }}</template>
            </el-table-column>
            <el-table-column label="复诊号" width="150">
              <template #default="{ row }">
                <span v-if="row.revisitAppointId" class="font-mono text-xs text-slate-600">{{
                    row.revisitAppointId
                  }}</span>
                <span v-else class="text-gray-400 text-xs">未预约</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="340">
              <template #default="{ row }">
                <!-- 行点击会开详情弹框，操作列每个按钮必须 .stop，否则点「编辑」同时弹两个框 -->
                <el-button link size="small" type="primary" @click.stop="openDetail(row)">详情
                </el-button>
                <el-button v-if="row.followupStatus !== 4" v-perm="'inpatient:followup:edit'"
                           link size="small" type="warning" @click.stop="openRevisit(row)">
                  {{ row.revisitAppointId ? '重约复诊号' : '生成复诊号' }}
                </el-button>
                <template v-if="row.followupStatus === 1">
                  <el-button v-perm="'inpatient:followup:edit'" link size="small" type="primary"
                             @click.stop="openEdit(row)">编辑
                  </el-button>
                  <el-button v-if="row.callStatus === 1" v-perm="'inpatient:followup:edit'" link size="small"
                             type="primary" @click.stop="openCallResume(row)">回填外呼结果
                  </el-button>
                  <el-button v-else v-perm="'inpatient:followup:edit'" link size="small"
                             type="primary" @click.stop="openCall(row)">电话外呼
                  </el-button>
                  <el-button link size="small" type="success" @click.stop="start(row)">开始</el-button>
                  <el-button v-perm="'inpatient:followup:delete'" link size="small" type="danger"
                             @click.stop="cancel(row)">取消
                  </el-button>
                </template>
                <template v-else-if="row.followupStatus === 2">
                  <el-button v-if="row.callStatus === 1" v-perm="'inpatient:followup:edit'" link size="small"
                             type="primary" @click.stop="openCallResume(row)">回填外呼结果
                  </el-button>
                  <el-button v-else v-perm="'inpatient:followup:edit'" link size="small"
                             type="primary" @click.stop="openCall(row)">电话外呼
                  </el-button>
                  <el-button link size="small" type="success"
                             @click.stop="openComplete(row)">完成随访
                  </el-button>
                </template>
                <!-- 补发问卷：完成随访时没有启用模板会被后端跳过，这里给一条人工兜底 -->
                <el-button v-else-if="row.followupStatus === 3" v-perm="'qc:survey:add'"
                           link size="small" type="primary" @click.stop="openIssue(row)">补发问卷
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                           :page-sizes="PAGE_SIZES" :total="total"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="query.pageNum = 1; loadList()" @current-change="loadList"/>
          </div>
        </el-tab-pane>

        <el-tab-pane label="科室待办与类型分布" name="byDept">
          <div class="two-col">
            <div>
              <div class="block-title">科室待办 TOP10（未完成 / 已完成）</div>
              <el-table v-loading="statLoading" :data="stat?.byDeptPending || []" border size="small" stripe>
                <el-table-column label="科室" min-width="160" prop="deptName">
                  <template #default="{ row }">{{ row.deptName || '未归科（出院记录无科室）' }}</template>
                </el-table-column>
                <el-table-column align="right" label="未完成" prop="pendingCount" width="100">
                  <template #default="{ row }"><span class="stat-value warn">{{ row.pendingCount }}</span></template>
                </el-table-column>
                <el-table-column align="right" label="已完成" prop="doneCount" width="100">
                  <template #default="{ row }">{{ row.doneCount }}</template>
                </el-table-column>
              </el-table>
              <div class="text-xs text-gray-400 mt-2">
                待办多可能是出院量大本就该多，所以同一行给出已完成 —— 一眼看出是「活多」还是「没干」。
              </div>
            </div>
            <div>
              <div class="block-title">按随访方式分布</div>
              <el-table v-loading="statLoading" :data="stat?.byType || []" border size="small" stripe>
                <el-table-column label="随访方式" min-width="140" prop="name"/>
                <el-table-column align="right" label="任务数" prop="count" width="100"/>
              </el-table>
              <div class="text-xs text-gray-400 mt-2">
                术后随访（出院时有手术记录）与复诊提醒由出院办理自动建单，慢病/用药指导多为手工新建。
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 详情（行点击，只读） -->
    <FollowupDetail v-model="detailVisible" :id="detailId" :row="detailRow"
                    :status-dict="statusDict" :type-dict="typeDict"/>

    <!-- 新建/编辑 -->
    <FollowupEdit v-model="editVisible" :row="editRow" :type-dict="typeDict" @saved="reloadAll"/>

    <!-- 按出院记录生成 -->
    <FollowupGenerate v-model="genVisible" :type-dict="typeDict" @saved="reloadAll"/>

    <!-- 完成随访 -->
    <el-dialog v-model="completeVisible" title="完成随访" width="480px">
      <el-input v-model="completeResult" :rows="3" placeholder="随访结果（接通情况/患者反馈/处理）" type="textarea"/>
      <div class="text-xs text-gray-400 mt-2">
        提交后系统会按当前启用的「出院随访」问卷自动发放一张满意度评价单，随访员在同一次通话里回收。
        没有启用中的问卷时随访照常完成，可在列表「补发问卷」人工兜底。
      </div>
      <template #footer>
        <el-button type="primary" @click="submitComplete">提交</el-button>
        <el-button @click="completeVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 补发满意度问卷 -->
    <FollowupSurveyIssue v-model="issueVisible" :row="issueRow"/>

    <!-- 电话外呼 -->
    <FollowupCallEdit v-model="callVisible" :resume="callResume" :task-id="callTaskId" @saved="reloadAll"/>

    <!-- 生成复诊号（来源 4）：与医生站「预约复诊」共用同一个弹框（选原病历 + 号源 + 当场看金额） -->
    <RevisitAppointDialog
        v-model="revisitVisible"
        :default-record-id="revisitTask?.revisitRecordId ?? null"
        :patient-id="revisitTask?.patientId ?? null"
        :patient-name="revisitTask?.patientName"
        :revisit-source="REVISIT_SOURCE.FOLLOWUP_PLAN"
        :submitting="revisitBusy"
        @submitted="submitRevisit"
    />
  </div>
</template>

<script setup>
/**
 * 出院随访与满意度回收（G20 任务流转 + sql/164 回收闭环，菜单 311）
 *
 * 三种入口：手动新建（选患者）、按出院记录一键生成（幂等）、出院办理自动建单（后端 SPI）。
 * 状态机：1 待随访 → 2 随访中 → 3 已完成；未完成可取消（4）。
 *
 * 四条口径：
 * 1. **看板数字全部来自 `/charge/followup/stat`**，前端不数当前页 —— 翻页只能看见 20 条，
 *    而「今天欠了多少电话」是全量事实。逾期按 followup_time 现算，没有定时任务翻状态。
 * 2. **手机号列表只有 `phoneMasked`**：编辑必须先 `getById` 取明文再回填。
 *    直接把列表行塞进表单会怎样？`Object.assign` 后整对象回写，星号就把真号洗进库里了（不可逆）。
 * 3. 完成率分母**不含已取消**：取消是任务作废，不是随访失败，算进分母会把率无故压低。
 * 4. 科室范围由后端按登录岗位收口，前端不给「全部科室」开关。
 *
 * followupTime 传「yyyy-MM-dd HH:mm:ss」空格分隔，与后端 @JsonFormat 宽进口径一致。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  cancelFollowup,
  completeFollowup,
  followupCreateRevisitAppoint,
  getFollowupList,
  getFollowupStat,
  startFollowup,
} from '@/api/followup';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {fmtTime} from '@/lib/followup';
import FollowupCallEdit from './FollowupCallEdit.vue';
import FollowupDetail from './FollowupDetail.vue';
import FollowupEdit from './FollowupEdit.vue';
import FollowupGenerate from './FollowupGenerate.vue';
import FollowupSurveyIssue from './FollowupSurveyIssue.vue';
import RevisitAppointDialog from '@/components/his/RevisitAppointDialog.vue';
import {REVISIT_SOURCE} from '@/lib/revisitPolicy';

const activeTab = ref('list');
// ---------------- 字典 ----------------
const typeDict = ref([]);
const statusDict = ref([]);
const typeText = (v) => dictLabelText(typeDict.value, v);
const statusText = (v) => dictLabelText(statusDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.FOLLOWUP_TYPE},${DICT_TYPE.FOLLOWUP_STATUS}`);
    typeDict.value = res?.data?.[DICT_TYPE.FOLLOWUP_TYPE] || [];
    statusDict.value = res?.data?.[DICT_TYPE.FOLLOWUP_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const statusTag = (v) => ({1: 'warning', 2: 'primary', 3: 'success', 4: 'info'}[v] || 'info');
// ---------------- 看板 ----------------
const stat = ref(null);
const statLoading = ref(false);
const loadStat = async () => {
  statLoading.value = true;
  try {
    const res = await getFollowupStat();
    if (res.code === 200)
      stat.value = res.data;
    else
      ElMessage.error(res.message || '看板加载失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('看板加载失败');
  } finally {
    statLoading.value = false;
  }
};
// ---------------- 列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  patientName: '', followupType: null, followupStatus: null,
  overdueOnly: false,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getFollowupList({
      patientName: query.patientName.trim() || undefined,
      followupType: query.followupType ?? undefined,
      followupStatus: query.followupStatus ?? undefined,
      overdueOnly: query.overdueOnly || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {patientName: '', followupType: null, followupStatus: null, overdueOnly: false, pageNum: 1});
  loadList();
};
/** 看板上点「逾期」直接筛出这批任务 */
const gotoOverdue = () => {
  Object.assign(query, {patientName: '', followupStatus: null, overdueOnly: true, pageNum: 1});
  activeTab.value = 'list';
  loadList();
};
/** 弹框保存成功后统一刷新列表与看板（原实现每处都是这两行） */
const reloadAll = () => {
  loadList();
  loadStat();
};
// ---------------- 只读详情（行点击） ----------------
const detailVisible = ref(false);
const detailId = ref(null);
// 列表行只为「打开瞬间不闪白」，详情数据由子组件按 id 自取
const detailRow = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailRow.value = row;
  detailVisible.value = true;
};
// ---------------- 新建 / 编辑 ----------------
const editVisible = ref(false);
const editRow = ref(null);
const openCreate = () => {
  editRow.value = null;
  editVisible.value = true;
};
const openEdit = (row) => {
  editRow.value = row;
  editVisible.value = true;
};
// ---------------- 执行 ----------------
const completeVisible = ref(false);
const completeId = ref(null);
const completeResult = ref('');
const openComplete = (row) => {
  completeId.value = row.id;
  completeResult.value = '';
  completeVisible.value = true;
};
const submitComplete = async () => {
  if (!completeResult.value.trim()) {
    ElMessage.warning('请填写随访结果');
    return;
  }
  try {
    const res = await completeFollowup(completeId.value, {result: completeResult.value.trim()});
    if (res.code === 200) {
      ElMessage.success('随访已完成，满意度问卷已按启用模板自动发放');
      completeVisible.value = false;
      loadList();
      loadStat();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
const start = async (row) => {
  try {
    const res = await startFollowup(row.id, {});
    if (res.code === 200) {
      ElMessage.success('已开始随访');
      loadList();
      loadStat();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
const cancel = async (row) => {
  try {
    const res = await cancelFollowup(row.id, {reason: '页面手动取消'});
    if (res.code === 200) {
      ElMessage.success('已取消');
      loadList();
      loadStat();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
// ---------------- 电话外呼（G-15，mock 通道=人工登记待呼） ----------------
const callVisible = ref(false);
const callTaskId = ref(null);
const callResume = ref(false);
// 首次外呼：登记待外呼，返回明文电话供护士拨打
const openCall = (row) => {
  callTaskId.value = row.id;
  callResume.value = false;
  callVisible.value = true;
};
// 已登记待外呼的行：不再重复登记，直接取详情（明文电话）回弹窗回填结果
const openCallResume = (row) => {
  callTaskId.value = row.id;
  callResume.value = true;
  callVisible.value = true;
};
/**
 * 手工补发满意度问卷：正常路径是「完成随访」自动发，这里只服务两种缺口 ——
 * 完成那天院内有事没配问卷（后端跳过不报错），或电话重打了一次要再收一份。
 * 同一任务 + 同一模板幂等，重复点不会多出第二张发放单。
 */
const issueVisible = ref(false);
const issueRow = ref(null);
const openIssue = (row) => {
  issueRow.value = row;
  issueVisible.value = true;
};
// ---------------- 按出院记录生成 ----------------
const genVisible = ref(false);
const openGenerate = () => {
  genVisible.value = true;
};
// ---------------- 生成复诊号（复诊来源 4，sql/121） ----------------
const revisitVisible = ref(false);
const revisitBusy = ref(false);
const revisitTask = ref(null);
/**
 * 随访打电话时约定「下周回来复查」，这一步就是把约定落成一张真实的号：
 * 占号源、按复诊收费策略收钱，并把复诊号回写到任务上（一条任务同一时间只挂一个有效复诊号，
 * 旧号退号/过号/爽约后允许重约 —— 这条判断在后端，前端只负责把按钮写成「重约复诊号」）。
 */
const openRevisit = (row) => {
  revisitTask.value = row;
  revisitVisible.value = true;
};
const submitRevisit = async (payload) => {
  const task = revisitTask.value;
  if (!task)
    return;
  revisitBusy.value = true;
  try {
    const res = await followupCreateRevisitAppoint({taskId: task.id, ...payload});
    if (res.code === 200) {
      ElMessage.success('复诊号已生成，患者按预约时段来院即可');
      revisitVisible.value = false;
      loadList();
      loadStat();
    } else
      ElMessage.error(res.message || '生成复诊号失败');
  } catch (e) {
    console.error(e);
    ElMessage.error(e?.message || '生成复诊号失败');
  } finally {
    revisitBusy.value = false;
  }
};
onMounted(() => {
  loadDicts();
  loadList();
  loadStat();
});
</script>

<style scoped>
.followup-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.stat-row {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item.clickable {
  cursor: pointer;
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

.stat-value.danger {
  color: #f56c6c;
}

.stat-foot {
  margin-top: 10px;
}

.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  align-items: center;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.overdue-text {
  color: #f56c6c;
  font-weight: 600;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.block-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
</style>
