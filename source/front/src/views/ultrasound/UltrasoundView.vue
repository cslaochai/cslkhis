<template>
  <div>
    <!-- 统计 -->
    <div class="mb-3 grid grid-cols-5 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待检</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pending }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">检查中</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.examining }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待审核</div>
        <div class="text-2xl font-semibold text-[#92400E] mt-1">{{ stats.pendingAudit }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已发布</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.published }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日检查</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.todayCount }}</div>
      </div>
    </div>

    <!-- 筛选（两卡式列表页，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="检查号">
            <el-input v-model="query.recordNo" clearable placeholder="检查号" style="width: 170px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="患者姓名">
            <el-input v-model="query.patientName" clearable placeholder="患者姓名" style="width: 150px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="超声类型">
            <el-select v-model="query.usType" :fit-input-width="false" clearable placeholder="超声类型"
                       style="width: 130px">
              <el-option v-for="d in usTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 130px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'medtech:ultrasound:add'" type="primary" @click="openCreate">登记检查</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="ultrasound-table" stripe>
        <el-table-column label="检查号" prop="recordNo" width="150"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="超声类型" width="110">
          <template #default="{ row }">{{ usTypeText(row.usType) }}</template>
        </el-table-column>
        <el-table-column label="部位" min-width="120" prop="bodyPart" show-overflow-tooltip/>
        <el-table-column label="临床诊断" min-width="130" prop="clinicalDiagnosis" show-overflow-tooltip/>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="超声医师" prop="sonographer" width="100"/>
        <el-table-column fixed="right" label="操作" width="330">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'medtech:ultrasound:edit'" link type="warning"
                       @click="checkIn(row)">签到
            </el-button>
            <el-button v-if="[2, 3].includes(Number(row.status))" v-perm="'medtech:ultrasound:edit'" link type="primary"
                       @click="openExecute(row)">执行
            </el-button>
            <el-button v-if="[3].includes(Number(row.status))" v-perm="'medtech:ultrasound:edit'" link type="primary"
                       @click="openMeasures(row)">测量值
            </el-button>
            <el-button v-if="Number(row.status) === 3" v-perm="'medtech:ultrasound:edit'" link type="primary"
                       @click="openReport(row)">出报告
            </el-button>
            <el-button v-if="Number(row.status) === 4" v-perm="'medtech:ultrasound:edit'" link type="warning"
                       @click="doAudit(row)">审核
            </el-button>
            <el-button v-if="Number(row.status) === 5" v-perm="'medtech:ultrasound:edit'" link type="success"
                       @click="doPublish(row)">发布
            </el-button>
            <el-button v-if="![6, 7].includes(Number(row.status))" v-perm="'medtech:ultrasound:delete'" link
                       type="danger" @click="cancel(row)">取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="PAGE_SIZES"
                       :total="total" background
                       layout="total, prev, pager, next, sizes" @current-change="loadList"
                       @size-change="query.pageNum = 1; loadList()"/>
      </div>
    </el-card>

    <!-- 登记/修改弹框（拆至同目录组件：row 为空走登记，有值走修改） -->
    <UltrasoundEdit v-model="editVisible" :us-type-dict="usTypeDict" @saved="loadList(); loadStats()"/>

    <!-- 执行弹框（拆至同目录组件） -->
    <UltrasoundExecuteEdit v-model="execVisible" :row="execRow" @saved="loadList()"/>

    <!-- 测量值弹窗（留页面：不含 el-form-item 的整单覆盖编辑表格、<40 行纯动作弹框，豁免登记见 workspace/_view_split_exemptions.json） -->
    <el-dialog v-model="measureVisible" :title="`结构化测量值：${measureRow?.recordNo || ''}`" width="760px">
      <el-table :data="measures" size="small">
        <el-table-column label="测量项" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.measureName" placeholder="如 肝右叶斜径"/>
          </template>
        </el-table-column>
        <el-table-column label="测量值" width="130">
          <template #default="{ row }">
            <el-input v-model="row.measureValue"/>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="90">
          <template #default="{ row }">
            <el-input v-model="row.unit"/>
          </template>
        </el-table-column>
        <el-table-column label="参考范围" width="130">
          <template #default="{ row }">
            <el-input v-model="row.referenceRange"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="" width="60">
          <template #default="{ $index }">
            <el-button v-perm="'medtech:ultrasound:delete'" :icon="Delete" link type="danger"
                       @click="removeMeasure($index)"/>
          </template>
        </el-table-column>
      </el-table>
      <el-button v-perm="'medtech:ultrasound:add'" :icon="Plus" class="mt-2" size="small" @click="addMeasure">加测量项
      </el-button>
      <template #footer>
        <el-button v-perm="['medtech:ultrasound:add', 'medtech:ultrasound:edit']" :loading="actLoading" type="primary"
                   @click="saveMeasures">保存（整单覆盖）
        </el-button>
        <el-button @click="measureVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 报告弹框（拆至同目录组件：新增与修改共用一张表单，标题随 row 变） -->
    <UltrasoundReportEdit v-model="reportVisible" :row="reportRow" @saved="loadList(); loadStats()"/>

    <!-- 详情（拆至同目录组件：只传 id，组件自己取数） -->
    <UltrasoundDetail v-model="detailVisible" :id="detailId"/>
  </div>
</template>

<script setup>
/**
 * 超声工作站（G17，菜单 409）
 *
 * 流程：登记 → 签到 → 执行 → 结构化测量值（整单覆盖保存，异常标志服务端判定）
 * → 报告（所见 + 提示）→ 审核（服务端硬校验审核人 ≠ 报告人）→ 发布。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  getUltrasoundDetail,
  getUltrasoundListPage,
  getUltrasoundStats,
  ultrasoundAudit,
  ultrasoundCancel,
  ultrasoundCheckIn,
  ultrasoundPublish,
  ultrasoundSaveMeasures,
} from '@/api/medicaltech';
import {getDictDataMapList} from '@/api/system';
import UltrasoundDetail from './UltrasoundDetail.vue';
import UltrasoundEdit from './UltrasoundEdit.vue';
import UltrasoundExecuteEdit from './UltrasoundExecuteEdit.vue';
import UltrasoundReportEdit from './UltrasoundReportEdit.vue';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const loading = ref(true);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  recordNo: '', patientName: '', usType: null, status: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const stats = reactive({pending: 0, examining: 0, pendingAudit: 0, published: 0, todayCount: 0});
const statusDict = ref([]);
const usTypeDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const usTypeText = (v) => dictLabelText(usTypeDict.value, v);
const statusTag = (v) => {
  const n = Number(v);
  if (n === 6)
    return 'success';
  if (n === 7)
    return 'info';
  if (n === 5)
    return 'warning';
  return 'primary';
};
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.ENDOUS_STATUS},${DICT_TYPE.ULTRASOUND_TYPE}`);
    statusDict.value = res?.data?.[DICT_TYPE.ENDOUS_STATUS] || [];
    usTypeDict.value = res?.data?.[DICT_TYPE.ULTRASOUND_TYPE] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadStats = async () => {
  try {
    const res = await getUltrasoundStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getUltrasoundListPage({
      recordNo: query.recordNo.trim() || undefined,
      patientName: query.patientName.trim() || undefined,
      usType: query.usType ?? undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.recordNo = '';
  query.patientName = '';
  query.usType = null;
  query.status = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 登记 ----------------
const editVisible = ref(false);
const openCreate = () => {
  editVisible.value = true;
};
// ---------------- 动作 ----------------
const actLoading = ref(false);
const doAction = async (fn, done) => {
  actLoading.value = true;
  try {
    const res = await fn();
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      done();
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    actLoading.value = false;
  }
};
const checkIn = (row) => doAction(() => ultrasoundCheckIn(row.id), () => {
  loadList();
  loadStats();
});
const execVisible = ref(false);
const execRow = ref(null);
const openExecute = (row) => {
  execRow.value = row;
  execVisible.value = true;
};
// ---------------- 测量值 ----------------
const measureVisible = ref(false);
const measureRow = ref(null);
const measures = ref([]);
const addMeasure = () => measures.value.push({
  measureName: '',
  measureValue: '',
  unit: '',
  referenceRange: '',
  sortOrder: measures.value.length + 1
});
const removeMeasure = (i) => measures.value.splice(i, 1);
const openMeasures = async (row) => {
  try {
    const res = await getUltrasoundDetail(row.id);
    
    measureRow.value = res.data;
    measures.value = (res.data.measures || []).map((m, i) => ({
      id: m.id, measureName: m.measureName, measureValue: m.measureValue,
      unit: m.unit, referenceRange: m.referenceRange, sortOrder: m.sortOrder || i + 1,
    }));
    if (!measures.value.length)
      addMeasure();
    measureVisible.value = true;
  } catch (e) {
    console.error(e);
    
  }
};
const saveMeasures = () => {
  if (measures.value.some((m) => !m.measureName.trim())) {
    ElMessage.warning('测量项名称不能为空');
    return;
  }
  doAction(() => ultrasoundSaveMeasures({
    recordId: measureRow.value.id,
    measures: measures.value.map((m, i) => ({
      id: m.id || undefined, measureName: m.measureName.trim(), measureValue: m.measureValue || undefined,
      unit: m.unit || undefined, referenceRange: m.referenceRange || undefined, sortOrder: i + 1,
    })),
  }), () => {
    measureVisible.value = false;
    loadList();
  });
};
// ---------------- 报告 ----------------
const reportVisible = ref(false);
const reportRow = ref(null);
const openReport = (row) => {
  reportRow.value = row;
  reportVisible.value = true;
};
const doAudit = (row) => doAction(() => ultrasoundAudit({recordId: row.id}), () => {
  loadList();
  loadStats();
});
const doPublish = (row) => doAction(() => ultrasoundPublish(row.id), () => {
  loadList();
  loadStats();
});
const cancel = (row) => {
  ElMessageBox.prompt('取消原因（必填）', '取消检查', {
    confirmButtonText: '确定', cancelButtonText: '返回', inputPattern: /\S+/, inputErrorMessage: '取消原因不能为空',
  }).then(({value}) => doAction(() => ultrasoundCancel({recordId: row.id, cancelReason: value}), () => {
    loadList();
    loadStats();
  })).catch(() => {
  });
};
// ---------------- 详情 ----------------
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
onMounted(() => {
  loadDicts();
  loadStats();
  loadList();
});
</script>
