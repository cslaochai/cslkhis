<template>
  <div>
    <!-- 统计 -->
    <div class="grid grid-cols-5 gap-4 mb-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待检（已登记/已签到）</div>
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
        <div class="text-sm text-gray-500">活检（累计）</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.biopsyCount }}</div>
      </div>
    </div>

    <!-- 筛选 -->
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
          <el-form-item label="内镜类型">
            <el-select v-model="query.endoType" :fit-input-width="false" clearable placeholder="内镜类型"
                       style="width: 130px">
              <el-option v-for="d in endoTypeDict" :key="d.dictValue" :label="d.dictLabel"
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
          <el-button v-perm="'medtech:endoscopy:add'" type="primary" @click="openCreate">登记检查</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="endoscopy-table" stripe>
        <el-table-column label="检查号" prop="recordNo" width="150"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="类型/麻醉" width="130">
          <template #default="{ row }">{{ endoTypeText(row.endoType) }} / {{
              anesthesiaText(row.anesthesiaMethod)
            }}
          </template>
        </el-table-column>
        <el-table-column label="部位/范围" min-width="120" prop="bodyPart" show-overflow-tooltip/>
        <el-table-column label="活检" width="150">
          <template #default="{ row }">
            <template v-if="Number(row.biopsyFlag) === 1">
              <el-tag size="small" type="warning">{{ row.biopsyCount }} 块</el-tag>
              <div v-if="row.pathologyOrderNo" class="text-gray-400">病理号 {{ row.pathologyOrderNo }}</div>
            </template>
            <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status) as any">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内镜医师" prop="endoscopist" width="100"/>
        <el-table-column fixed="right" label="操作" width="330">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'medtech:endoscopy:edit'" link type="warning"
                       @click="checkIn(row)">签到
            </el-button>
            <el-button v-if="[2, 3].includes(Number(row.status))" v-perm="'medtech:endoscopy:edit'" link type="primary"
                       @click="openExecute(row)">执行
            </el-button>
            <el-button v-if="[2, 3].includes(Number(row.status)) && !row.pathologyOrderNo"
                       v-perm="'medtech:endoscopy:add'" link type="warning" @click="sendBiopsy(row)">送病理
            </el-button>
            <el-button v-if="Number(row.status) === 3" v-perm="'medtech:endoscopy:edit'" link type="primary"
                       @click="openReport(row)">出报告
            </el-button>
            <el-button v-if="Number(row.status) === 4" v-perm="'medtech:endoscopy:edit'" link type="warning"
                       @click="doAudit(row)">审核
            </el-button>
            <el-button v-if="Number(row.status) === 5" v-perm="'medtech:endoscopy:edit'" link type="success"
                       @click="doPublish(row)">发布
            </el-button>
            <el-button v-if="![6, 7].includes(Number(row.status))" v-perm="'medtech:endoscopy:delete'" link
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

    <!-- 登记弹窗 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '修改检查' : '登记内镜检查'" width="640px">
      <el-form label-width="100px">
        <el-form-item label="患者" required>
          <patient-select v-model="editForm.patientId" @select="onPatientSelect"/>
        </el-form-item>
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="内镜类型">
            <el-select v-model="editForm.endoType" style="width: 100%">
              <el-option v-for="d in endoTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="麻醉方式">
            <el-select v-model="editForm.anesthesiaMethod" style="width: 100%">
              <el-option v-for="d in anesthesiaDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="部位/范围">
          <el-input v-model="editForm.bodyPart"/>
        </el-form-item>
        <el-form-item label="检查目的">
          <el-input v-model="editForm.examPurpose"/>
        </el-form-item>
        <el-form-item label="临床诊断">
          <el-input v-model="editForm.clinicalDiagnosis" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button v-perm="['medtech:endoscopy:add', 'medtech:endoscopy:edit']" :loading="editLoading" type="primary"
                   @click="saveRecord">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 执行弹窗 -->
    <el-dialog v-model="execVisible" :title="`执行检查：${execRow?.recordNo || ''}`" width="520px">
      <el-form label-width="130px">
        <el-form-item label="内镜医师">
          <el-input v-model="execForm.endoscopist" placeholder="留空默认当前登录人"/>
        </el-form-item>
        <el-form-item label="部位/到达范围">
          <el-input v-model="execForm.bodyPart"/>
        </el-form-item>
        <el-form-item v-if="Number(execRow?.endoType) === 2" label="Boston 评分(0~9)">
          <el-input-number v-model="execForm.bowelPrepScore" :max="9" :min="0"/>
        </el-form-item>
        <el-form-item v-if="Number(execRow?.endoType) === 1" label="幽门螺杆菌">
          <el-radio-group v-model="execForm.hpResult">
            <el-radio :value="0">未查</el-radio>
            <el-radio :value="1">阴性</el-radio>
            <el-radio :value="2">阳性</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="execVisible = false">取消</el-button>
        <el-button v-perm="'medtech:endoscopy:edit'" :loading="actLoading" type="primary" @click="saveExecute">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 报告弹窗 -->
    <el-dialog v-model="reportVisible" :title="`内镜报告：${reportRow?.recordNo || ''}`" width="680px">
      <el-form label-width="90px">
        <el-form-item label="内镜所见" required>
          <el-input v-model="reportForm.findings" :rows="4" type="textarea"/>
        </el-form-item>
        <el-form-item label="内镜诊断" required>
          <el-input v-model="reportForm.diagnosis" :rows="3" type="textarea"/>
        </el-form-item>
        <el-form-item label="建议">
          <el-input v-model="reportForm.suggestion" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button v-perm="'medtech:endoscopy:edit'" :loading="actLoading" type="primary" @click="saveReport">提交报告
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="内镜检查详情" width="680px">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="检查号">{{ detail.recordNo }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.endoTypeText }}</el-descriptions-item>
        <el-descriptions-item label="麻醉">{{ detail.anesthesiaMethodText }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
        <el-descriptions-item label="Hp">{{ detail.hpResultText }}</el-descriptions-item>
        <el-descriptions-item label="Boston 评分">{{ detail.bowelPrepScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="活检">
          {{ Number(detail.biopsyFlag) === 1 ? `${detail.biopsyPart}（${detail.biopsyCount} 块）` : '无' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.pathologyOrderNo" :span="2" label="关联病理号">{{
            detail.pathologyOrderNo
          }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="内镜所见">{{ detail.findings || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="内镜诊断">{{ detail.diagnosis || '未出' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="建议">{{ detail.suggestion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告医师">{{ detail.reportBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核医师">{{ detail.auditBy || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 内镜工作站（G17，菜单 408）
 *
 * 流程：登记 → 签到 → 执行（所见/肠镜 Boston 评分/胃镜 Hp）→ 活检送病理（联动生成
 * 病理单，重复送检服务端拒绝）→ 出报告 → 审核（服务端硬校验审核人 ≠ 报告人）→ 发布。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  endoscopyAudit,
  endoscopyCancel,
  endoscopyCheckIn,
  endoscopyExecute,
  endoscopyPublish,
  endoscopyReport,
  endoscopySendBiopsy,
  endoscopyUpsert,
  getEndoscopyDetail,
  getEndoscopyListPage,
  getEndoscopyStats,
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
  recordNo: '', patientName: '', endoType: null, status: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const stats = reactive({pending: 0, examining: 0, pendingAudit: 0, published: 0, biopsyCount: 0});
const statusDict = ref([]);
const endoTypeDict = ref([]);
const anesthesiaDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const endoTypeText = (v) => dictLabelText(endoTypeDict.value, v);
const anesthesiaText = (v) => dictLabelText(anesthesiaDict.value, v);
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
    const res = await getDictDataMapList(`${DICT_TYPE.ENDOUS_STATUS},${DICT_TYPE.ENDOSCOPY_TYPE},${DICT_TYPE.ENDOSCOPY_ANESTHESIA}`);
    statusDict.value = res?.data?.[DICT_TYPE.ENDOUS_STATUS] || [];
    endoTypeDict.value = res?.data?.[DICT_TYPE.ENDOSCOPY_TYPE] || [];
    anesthesiaDict.value = res?.data?.[DICT_TYPE.ENDOSCOPY_ANESTHESIA] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadStats = async () => {
  try {
    const res = await getEndoscopyStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getEndoscopyListPage({
      recordNo: query.recordNo.trim() || undefined,
      patientName: query.patientName.trim() || undefined,
      endoType: query.endoType ?? undefined,
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
  query.recordNo = '';
  query.patientName = '';
  query.endoType = null;
  query.status = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 登记 ----------------
const editVisible = ref(false);
const editForm = reactive({
  id: null, patientId: null, patientNo: '', patientName: '', gender: null,
  age: null, visitDate: '', clinicalDiagnosis: '', endoType: 1, anesthesiaMethod: 1,
  bodyPart: '', examPurpose: '',
});
const editLoading = ref(false);
const openCreate = () => {
  Object.assign(editForm, {
    id: null, patientId: null, patientNo: '', patientName: '', gender: null, age: null, visitDate: '',
    clinicalDiagnosis: '', endoType: 1, anesthesiaMethod: 1, bodyPart: '', examPurpose: '',
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
const saveRecord = async () => {
  if (!editForm.patientId || !editForm.patientName.trim()) {
    ElMessage.warning('请选择患者');
    return;
  }
  editLoading.value = true;
  try {
    const res = await endoscopyUpsert({...editForm, visitDate: editForm.visitDate || undefined});
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
// ---------------- 动作 ----------------
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
const checkIn = (row) => doAction(() => endoscopyCheckIn(row.id), () => {
  loadList();
  loadStats();
});
// 执行
const execVisible = ref(false);
const execRow = ref(null);
const execForm = reactive({endoscopist: '', bodyPart: '', bowelPrepScore: null, hpResult: null});
const openExecute = (row) => {
  execRow.value = row;
  Object.assign(execForm, {endoscopist: '', bodyPart: row.bodyPart || '', bowelPrepScore: null, hpResult: null});
  execVisible.value = true;
};
const saveExecute = () => doAction(() => endoscopyExecute({
  recordId: execRow.value.id,
  endoscopist: execForm.endoscopist.trim() || undefined,
  bodyPart: execForm.bodyPart.trim() || undefined,
  bowelPrepScore: Number(execRow.value.endoType) === 2 ? execForm.bowelPrepScore : undefined,
  hpResult: Number(execRow.value.endoType) === 1 ? execForm.hpResult : undefined,
}), () => {
  execVisible.value = false;
  loadList();
});
// 送病理
const sendBiopsy = (row) => {
  ElMessageBox.prompt('活检部位（必填）与块数（默认 1），格式：部位|块数', '活检送病理', {
    confirmButtonText: '送检', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '活检部位不能为空',
  }).then(({value}) => {
    const [part, cnt] = String(value).split('|');
    doAction(() => endoscopySendBiopsy({
      recordId: row.id,
      biopsyPart: part.trim(),
      biopsyCount: Number(cnt) || 1
    }), () => {
      loadList();
      loadStats();
    });
  }).catch(() => {
  });
};
// 报告
const reportVisible = ref(false);
const reportRow = ref(null);
const reportForm = reactive({findings: '', diagnosis: '', suggestion: ''});
const openReport = (row) => {
  reportRow.value = row;
  Object.assign(reportForm, {
    findings: row.findings || '',
    diagnosis: row.diagnosis || '',
    suggestion: row.suggestion || ''
  });
  reportVisible.value = true;
};
const saveReport = () => doAction(() => endoscopyReport({recordId: reportRow.value.id, ...reportForm}), () => {
  reportVisible.value = false;
  loadList();
  loadStats();
});
const doAudit = (row) => doAction(() => endoscopyAudit({recordId: row.id}), () => {
  loadList();
  loadStats();
});
const doPublish = (row) => doAction(() => endoscopyPublish(row.id), () => {
  loadList();
  loadStats();
});
const cancel = (row) => {
  ElMessageBox.prompt('取消原因（必填）', '取消检查', {
    confirmButtonText: '确定', cancelButtonText: '返回', inputPattern: /\S+/, inputErrorMessage: '取消原因不能为空',
  }).then(({value}) => doAction(() => endoscopyCancel({recordId: row.id, cancelReason: value}), () => {
    loadList();
    loadStats();
  })).catch(() => {
  });
};
// ---------------- 详情 ----------------
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  try {
    const res = await getEndoscopyDetail(row.id);
    if (res.code === 200) {
      detail.value = res.data;
      detailVisible.value = true;
    } else
      ElMessage.error(res.message || '查询详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  }
};
onMounted(() => {
  loadDicts();
  loadStats();
  loadList();
});
</script>
