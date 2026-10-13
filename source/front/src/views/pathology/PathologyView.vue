<template>
  <div>
    <!-- 统计 -->
    <div class="mb-3 grid grid-cols-5 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待接收标本</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pendingReceive }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">处理中（制片阶段）</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.processing }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待审核（已初诊）</div>
        <div class="text-2xl font-semibold text-[#92400E] mt-1">{{ stats.pendingAudit }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已发布</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.published }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">今日冰冻</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.frozenToday }}</div>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="病理号">
            <el-input v-model="query.orderNo" clearable placeholder="病理号" style="width: 170px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="患者姓名">
            <el-input v-model="query.patientName" clearable placeholder="患者姓名" style="width: 150px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="检查类型">
            <el-select v-model="query.examType" :fit-input-width="false" clearable placeholder="检查类型"
                       style="width: 130px">
              <el-option v-for="d in examTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
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
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'medtech:pathology:add'" type="primary" @click="openCreate">登记病理单</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="pathology-table" stripe>
        <el-table-column label="病理号" prop="orderNo" width="160"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="检查类型" width="110">
          <template #default="{ row }">{{ examTypeText(row.examType) }}
            <el-tag v-if="Number(row.isFrozen) === 1" class="ml-1" size="small" type="danger">冰冻</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="取材部位" min-width="130" prop="specimenPart" show-overflow-tooltip/>
        <el-table-column label="临床诊断" min-width="140" prop="clinicalDiagnosis" show-overflow-tooltip/>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="蜡块数" prop="blockCount" width="80"/>
        <el-table-column label="初诊" prop="reportBy" width="90"/>
        <el-table-column label="审核" prop="auditBy" width="90"/>
        <el-table-column fixed="right" label="操作" width="290">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'medtech:pathology:edit'" link type="warning"
                       @click="receive(row)">接收标本
            </el-button>
            <el-button v-if="[2, 3].includes(Number(row.status))" v-perm="'medtech:pathology:add'" link type="primary"
                       @click="addBlock(row)">加蜡块
            </el-button>
            <el-button
                v-if="[4, 3].includes(Number(row.status)) || (Number(row.isFrozen) === 1 && [2, 3].includes(Number(row.status)))"
                v-perm="'medtech:pathology:edit'" link type="primary" @click="openReport(row)">初诊
            </el-button>
            <el-button v-if="Number(row.status) === 5" v-perm="'medtech:pathology:edit'" link type="warning"
                       @click="doAudit(row)">审核
            </el-button>
            <el-button v-if="Number(row.status) === 6" v-perm="'medtech:pathology:edit'" link type="success"
                       @click="doPublish(row)">发布
            </el-button>
            <el-button v-if="Number(row.status) !== 7 && Number(row.status) !== 8" v-perm="'medtech:pathology:delete'"
                       link type="danger" @click="cancel(row)">取消
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

    <!-- 登记 / 修改（弹框已拆到 PathologyEdit） -->
    <PathologyEdit v-model="editVisible" :exam-type-dict="examTypeDict" @saved="loadList(); loadStats()"/>

    <!-- 初诊（弹框已拆到 PathologyReportAdd） -->
    <PathologyReportAdd v-model="reportVisible" :row="reportRow" @saved="loadList(); loadStats()"/>

    <!-- 详情（弹框已拆到 PathologyDetail，组件按 id 自取） -->
    <PathologyDetail v-model="detailVisible" :id="detailId" @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * 病理工作站（G17，菜单 407）
 *
 * 流程：登记 → 标本接收 → 蜡块取材/包埋/切片（明细单点推进）→ 初诊
 * → 审核（服务端硬校验：审核人 ≠ 初诊人）→ 发布。
 * 状态/类型文案全部走字典（his_pathology_status / his_pathology_exam_type），
 * 本页不写任何映射 —— 口径单点在 sys_dict_data。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  getPathologyListPage,
  getPathologyStats,
  pathologyAudit,
  pathologyBlockUpsert,
  pathologyCancel,
  pathologyPublish,
  pathologyReceive,
} from '@/api/medicaltech';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import PathologyDetail from './PathologyDetail.vue';
import PathologyEdit from './PathologyEdit.vue';
import PathologyReportAdd from './PathologyReportAdd.vue';

const loading = ref(true);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  orderNo: '', patientName: '', examType: null, status: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const stats = reactive({pendingReceive: 0, processing: 0, pendingAudit: 0, published: 0, frozenToday: 0});
const statusDict = ref([]);
const examTypeDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const examTypeText = (v) => dictLabelText(examTypeDict.value, v);
const statusTag = (v) => {
  const n = Number(v);
  if (n === 7)
    return 'success';
  if (n === 8)
    return 'info';
  if (n === 5 || n === 6)
    return 'warning';
  return 'primary';
};
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.PATHOLOGY_STATUS},${DICT_TYPE.PATHOLOGY_EXAM_TYPE}`);
    statusDict.value = res?.data?.[DICT_TYPE.PATHOLOGY_STATUS] || [];
    examTypeDict.value = res?.data?.[DICT_TYPE.PATHOLOGY_EXAM_TYPE] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadStats = async () => {
  try {
    const res = await getPathologyStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getPathologyListPage({
      orderNo: query.orderNo.trim() || undefined,
      patientName: query.patientName.trim() || undefined,
      examType: query.examType ?? undefined,
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
  query.orderNo = '';
  query.patientName = '';
  query.examType = null;
  query.status = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 登记 / 修改（弹框已拆到 PathologyEdit，这里只管打开） ----------------
const editVisible = ref(false);
const openCreate = () => {
  editVisible.value = true;
};
// ---------------- 通用动作（接收 / 流程推进 / 初诊 / 审核 / 发布 / 取消） ----------------
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
const receive = (row) => doAction(() => pathologyReceive({orderId: row.id}), () => {
  loadList();
  loadStats();
});
const addBlock = (row) => {
  ElMessageBox.prompt('登记蜡块号（如 A1，多个用英文逗号分隔）', '登记蜡块', {
    confirmButtonText: '登记', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '蜡块号不能为空',
  }).then(({value}) => {
    const nos = String(value).split(',').map((s) => s.trim()).filter(Boolean);
    doAction(() => Promise.all(nos.map((n, i) => pathologyBlockUpsert({
      orderId: row.id,
      blockNo: n,
      partDesc: row.specimenPart,
      blockCount: 1
    })))
        .then((rs) => ({code: rs.every((r) => r.code === 200) ? 200 : 500, message: '蜡块已登记'})), () => {
      loadList();
    });
  }).catch(() => {
  });
};
const reportVisible = ref(false);
const reportRow = ref(null);
const openReport = (row) => {
  reportRow.value = row;
  reportVisible.value = true;
};
const doAudit = (row) => doAction(() => pathologyAudit({orderId: row.id}), () => {
  loadList();
  loadStats();
});
const doPublish = (row) => doAction(() => pathologyPublish(row.id), () => {
  loadList();
  loadStats();
});
const cancel = (row) => {
  ElMessageBox.prompt('取消原因（必填）', '取消病理单', {
    confirmButtonText: '确定', cancelButtonText: '返回', inputPattern: /\S+/, inputErrorMessage: '取消原因不能为空',
  }).then(({value}) => doAction(() => pathologyCancel({orderId: row.id, cancelReason: value}), () => {
    loadList();
    loadStats();
  })).catch(() => {
  });
};
// ---------------- 详情（含蜡块，弹框已拆到 PathologyDetail，组件按 id 自取） ----------------
const detailVisible = ref(false);
const detailId = ref('');
const openDetail = (row) => {
  detailId.value = String(row.id);
  detailVisible.value = true;
};
onMounted(() => {
  loadDicts();
  loadStats();
  loadList();
});
</script>
