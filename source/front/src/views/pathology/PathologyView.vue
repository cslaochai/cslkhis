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
            <el-tag :type="statusTag(row.status) as any">{{ statusText(row.status) }}</el-tag>
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

    <!-- 登记弹窗 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '修改病理单' : '登记病理单'" width="640px">
      <el-form label-width="90px">
        <el-form-item label="患者" required>
          <patient-select v-model="editForm.patientId" @select="onPatientSelect"/>
        </el-form-item>
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="检查类型">
            <el-select v-model="editForm.examType" style="width: 100%">
              <el-option v-for="d in examTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="是否冰冻">
            <el-radio-group v-model="editForm.isFrozen">
              <el-radio :value="0">否</el-radio>
              <el-radio :value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="标本类型">
            <el-input v-model="editForm.specimenType" placeholder="活检/切除/穿刺等"/>
          </el-form-item>
          <el-form-item label="取材部位">
            <el-input v-model="editForm.specimenPart"/>
          </el-form-item>
        </div>
        <el-form-item label="临床诊断">
          <el-input v-model="editForm.clinicalDiagnosis" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button v-perm="['medtech:pathology:add', 'medtech:pathology:edit']" :loading="editLoading" type="primary"
                   @click="saveOrder">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 初诊弹窗 -->
    <el-dialog v-model="reportVisible" :title="`病理初诊：${reportRow?.orderNo || ''}`" width="680px">
      <el-form label-width="100px">
        <el-form-item label="肉眼所见">
          <el-input v-model="reportForm.grossFindings" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item label="镜下所见">
          <el-input v-model="reportForm.microscopyFindings" :rows="3" type="textarea"/>
        </el-form-item>
        <el-form-item label="免疫组化/特染">
          <el-input v-model="reportForm.ihcResult" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item v-if="reportRow && Number(reportRow.isFrozen) === 1" label="冰冻结果" required>
          <el-input v-model="reportForm.frozenResult" :rows="2" placeholder="术中冰冻快速诊断结果（冰冻单必填）"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="病理诊断" required>
          <el-input v-model="reportForm.diagnosis" :rows="3" type="textarea"/>
        </el-form-item>
        <el-form-item label="建议">
          <el-input v-model="reportForm.suggestion" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button v-perm="'medtech:pathology:edit'" :loading="actLoading" type="primary" @click="saveReport">提交初诊
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="病理单详情" width="760px">
      <template v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="病理号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item label="检查类型">{{ detail.examTypeText }}</el-descriptions-item>
          <el-descriptions-item label="标本">{{ detail.specimenType || '-' }} / {{
              detail.specimenPart || '-'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="冰冻结果">{{ detail.frozenResult || '-' }}</el-descriptions-item>
          <el-descriptions-item :span="3" label="诊断">{{ detail.diagnosis || '未出' }}</el-descriptions-item>
          <el-descriptions-item :span="3" label="建议">{{ detail.suggestion || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt-3 font-medium text-sm text-gray-600">蜡块明细</div>
        <el-table :data="detail.blocks || []" class="mt-1" size="small">
          <el-table-column label="蜡块号" prop="blockNo" width="90"/>
          <el-table-column label="部位" min-width="120" prop="partDesc" show-overflow-tooltip/>
          <el-table-column label="状态" width="90">
            <template #default="{ row: b }">
              <el-tag size="small">{{ b.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="切片号" prop="sliceNo" width="90"/>
          <el-table-column label="流转" width="220">
            <template #default="{ row: b }">
              <el-button v-if="Number(b.status) === 1" v-perm="'medtech:pathology:edit'" link size="small"
                         type="primary" @click="blockAction(detail, b, 1)">取材
              </el-button>
              <el-button v-if="Number(b.status) === 2" v-perm="'medtech:pathology:edit'" link size="small"
                         type="primary" @click="blockAction(detail, b, 2)">包埋
              </el-button>
              <el-button v-if="Number(b.status) === 3" v-perm="'medtech:pathology:edit'" link size="small"
                         type="primary" @click="blockAction(detail, b, 3)">切片
              </el-button>
              <span v-if="Number(b.status) === 4" class="text-gray-400 text-xs">已完成</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
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
  getPathologyDetail,
  getPathologyListPage,
  getPathologyStats,
  pathologyAudit,
  pathologyBlockAction,
  pathologyBlockUpsert,
  pathologyCancel,
  pathologyPublish,
  pathologyReceive,
  pathologyReport,
  pathologyUpsert,
} from '@/api/medicaltech';
import {getDictDataMapList} from '@/api/system';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

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
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
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
// ---------------- 登记 / 修改 ----------------
const editVisible = ref(false);
const editForm = reactive({
  id: null, patientId: null, patientNo: '', patientName: '', gender: null,
  age: null, visitDate: '', clinicalDiagnosis: '', examType: 1, specimenType: '', specimenPart: '', isFrozen: 0,
});
const editLoading = ref(false);
const openCreate = () => {
  Object.assign(editForm, {
    id: null, patientId: null, patientNo: '', patientName: '', gender: null, age: null,
    visitDate: '', clinicalDiagnosis: '', examType: 1, specimenType: '', specimenPart: '', isFrozen: 0,
  });
  editVisible.value = true;
};
const onPatientSelect = (p) => {
  editForm.patientId = p?.id;
  editForm.patientName = p?.name || p?.patientName || '';
  editForm.patientNo = p?.patientNo || '';
  editForm.gender = p?.gender ?? null;
  editForm.age = p?.age ?? null;
};
const saveOrder = async () => {
  if (!editForm.patientId || !editForm.patientName.trim()) {
    ElMessage.warning('请选择患者');
    return;
  }
  editLoading.value = true;
  try {
    const res = await pathologyUpsert({
      id: editForm.id || undefined,
      patientId: editForm.patientId, patientNo: editForm.patientNo || undefined,
      patientName: editForm.patientName.trim(), gender: editForm.gender, age: editForm.age,
      visitDate: editForm.visitDate || undefined, clinicalDiagnosis: editForm.clinicalDiagnosis || undefined,
      examType: editForm.examType, specimenType: editForm.specimenType || undefined,
      specimenPart: editForm.specimenPart || undefined, isFrozen: editForm.isFrozen,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存');
      editVisible.value = false;
      loadList();
      loadStats();
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    editLoading.value = false;
  }
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
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
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
      loadDetail(row.id);
    });
  }).catch(() => {
  });
};
const blockAction = (row, block, action) => doAction(() => pathologyBlockAction({blockId: block.id, action}), () => {
  loadDetail(row.id);
  loadList();
});
const reportVisible = ref(false);
const reportRow = ref(null);
const reportForm = reactive({
  grossFindings: '',
  microscopyFindings: '',
  ihcResult: '',
  diagnosis: '',
  suggestion: '',
  frozenResult: ''
});
const openReport = async (row) => {
  await loadDetail(row.id);
  reportRow.value = detail.value;
  Object.assign(reportForm, {
    grossFindings: detail.value?.grossFindings || '', microscopyFindings: detail.value?.microscopyFindings || '',
    ihcResult: detail.value?.ihcResult || '', diagnosis: detail.value?.diagnosis || '',
    suggestion: detail.value?.suggestion || '', frozenResult: detail.value?.frozenResult || '',
  });
  reportVisible.value = true;
};
const saveReport = () => doAction(() => pathologyReport({orderId: reportRow.value.id, ...reportForm}), () => {
  reportVisible.value = false;
  loadList();
  loadStats();
});
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
// ---------------- 详情（含蜡块） ----------------
const detailVisible = ref(false);
const detail = ref(null);
const loadDetail = async (orderId) => {
  try {
    const res = await getPathologyDetail(orderId);
    if (res.code === 200) {
      detail.value = res.data;
      return res.data;
    }
    ElMessage.error(res.message || '查询详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  }
  return null;
};
const openDetail = async (row) => {
  await loadDetail(row.id);
  detailVisible.value = true;
};
onMounted(() => {
  loadDicts();
  loadStats();
  loadList();
});
</script>
