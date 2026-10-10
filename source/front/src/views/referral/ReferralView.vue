<template>
  <div>
    <!-- 今日总值班：转诊单登记即通知他协调，待确认超时由他推进 -->
    <DutyOfficerBar/>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="方向">
            <el-select v-model="query.direction" :fit-input-width="false" clearable placeholder="方向"
                       style="width: 110px">
              <el-option v-for="d in dirDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.referralStatus" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="转入医院">
            <el-input v-model="query.toHospital" clearable placeholder="转入医院" style="width: 180px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'inpatient:referral:add'" plain type="primary" @click="openCreate">转诊登记</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="referral-table" stripe>
        <el-table-column label="转诊单号" prop="referralNo" width="200"/>
        <el-table-column label="患者" prop="patientName" width="110">
          <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 ml-1">{{ row.patientNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="方向" prop="direction" width="80">
          <template #default="{ row }">
            <el-tag :type="row.direction === 1 ? 'warning' : 'success'">{{ dirText(row.direction) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="转出科室" prop="fromDeptId" width="120">
          <template #default="{ row }">{{ deptName(row.fromDeptId) }}</template>
        </el-table-column>
        <el-table-column label="转入" min-width="160">
          <template #default="{ row }">
            <span v-if="row.toHospital">{{ row.toHospital }}</span>
            <span v-else-if="row.toDeptId">{{ deptName(row.toDeptId) }}</span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="转诊原因" min-width="180" prop="reason" show-overflow-tooltip/>
        <el-table-column label="诊断" min-width="140" prop="diagnosis" show-overflow-tooltip>
          <template #default="{ row }">{{ row.diagnosis || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="referralStatus" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.referralStatus)">{{ statusText(row.referralStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="确认人" prop="auditName" width="90">
          <template #default="{ row }">{{ row.auditName || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="190">
          <template #default="{ row }">
            <template v-if="row.referralStatus === 0">
              <el-button link size="small" type="primary" @click="openAudit(row)">确认</el-button>
              <el-button link size="small" type="danger" @click="cancel(row)">取消</el-button>
            </template>
            <el-button v-else-if="row.referralStatus === 1" link size="small" type="success"
                       @click="openFinish(row)">完成
            </el-button>
            <span v-else class="text-gray-400">{{ statusText(row.referralStatus) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 登记 -->
    <el-dialog v-model="createVisible" :close-on-click-modal="false" title="转诊登记" width="580px">
      <el-form label-width="90px">
        <el-form-item label="患者" required>
          <PatientSelect v-model="createForm.patientId"/>
        </el-form-item>
        <el-form-item label="方向" required>
          <el-radio-group v-model="createForm.direction">
            <el-radio v-for="d in dirDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="入院ID">
          <el-input v-model="createForm.admissionId" placeholder="住院患者转诊时填（可空=门诊）"/>
        </el-form-item>
        <el-form-item label="转出科室" required>
          <el-select v-model="createForm.fromDeptId" :fit-input-width="false" filterable style="width: 240px">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="转入医院">
          <el-input v-model="createForm.toHospital" placeholder="院际转诊必填（上转上级/下转基层）"/>
        </el-form-item>
        <el-form-item label="转入科室">
          <el-select v-model="createForm.toDeptId" :fit-input-width="false" clearable filterable style="width: 240px">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="转诊原因" required>
          <el-input v-model="createForm.reason" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item label="诊断摘要">
          <el-input v-model="createForm.diagnosis"/>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="createForm.contactPhone" style="width: 220px"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button v-perm="'inpatient:referral:add'" :loading="createLoading" type="primary" @click="submitCreate">
          登记
        </el-button>
      </template>
    </el-dialog>

    <!-- 确认 -->
    <el-dialog v-model="auditVisible" :close-on-click-modal="false" title="确认转诊" width="480px">
      <el-form label-width="90px">
        <el-form-item label="转入科室">
          <el-select v-model="auditForm.toDeptId" :fit-input-width="false" clearable filterable style="width: 240px">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="确认意见">
          <el-input v-model="auditForm.auditRemark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 完成 -->
    <el-dialog v-model="finishVisible" :close-on-click-modal="false" title="完成转诊" width="480px">
      <el-input v-model="finishForm.finishRemark" :rows="3" placeholder="转诊结局 / 接收医院反馈"
                type="textarea"/>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFinish">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 双向转诊（G20，菜单 312）
 *
 * 上转=转往上级医院、下转=转回基层/社区（院际转诊 toHospital 必填；本院内转科室走「转科」，别混）。
 * 状态机：0 待确认 → 1 已确认 → 2 已完成；未完成可取消（3）。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {referralAudit, referralCancel, referralCreate, referralFinish, referralListPage,} from '@/api/referral';
import {getDepartmentSelectList, getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// 今日总值班：转诊是跨院动作，联系外院与安排转运只有他能拍板（sql/169）
import DutyOfficerBar from '@/components/his/DutyOfficerBar.vue';
// ---------------- 字典 ----------------
const dirDict = ref([]);
const statusDict = ref([]);
const dirText = (v) => dictLabelText(dirDict.value, v);
const statusText = (v) => dictLabelText(statusDict.value, v);
const statusTag = (v) => ({0: 'warning', 1: 'primary', 2: 'success', 3: 'info'}[v] || 'info');
const loadDicts = async () => {
  try {
    const res1 = await getDictDataMapList(`${DICT_TYPE.REFERRAL_DIRECTION},${DICT_TYPE.REFERRAL_STATUS}`);
    dirDict.value = res1?.data?.[DICT_TYPE.REFERRAL_DIRECTION] || [];
    statusDict.value = res1?.data?.[DICT_TYPE.REFERRAL_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 科室 ----------------
const depts = ref([]);
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    depts.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
const deptName = (id) => depts.value.find(d => String(d.id) === String(id))?.deptName || '—';
// ---------------- 列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  direction: null, referralStatus: null,
  toHospital: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await referralListPage({
      direction: query.direction ?? undefined,
      referralStatus: query.referralStatus ?? undefined,
      toHospital: query.toHospital.trim() || undefined,
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
  Object.assign(query, {direction: null, referralStatus: null, toHospital: '', pageNum: 1});
  loadList();
};
// ---------------- 登记 ----------------
const createVisible = ref(false);
const createLoading = ref(false);
const createForm = reactive({
  patientId: null, direction: 1, admissionId: '',
  fromDeptId: null, toDeptId: null,
  toHospital: '', reason: '', diagnosis: '', contactPhone: '',
});
const openCreate = () => {
  Object.assign(createForm, {
    patientId: null, direction: 1, admissionId: '', fromDeptId: null, toDeptId: null,
    toHospital: '', reason: '', diagnosis: '', contactPhone: '',
  });
  createVisible.value = true;
};
const submitCreate = async () => {
  if (!createForm.patientId || !createForm.fromDeptId || !createForm.reason.trim()) {
    ElMessage.warning('患者、转出科室、转诊原因为必填');
    return;
  }
  if (!createForm.toHospital.trim() && !createForm.toDeptId) {
    ElMessage.warning('院际转诊必须填转入医院，院内转诊必须选转入科室');
    return;
  }
  createLoading.value = true;
  try {
    const res = await referralCreate({
      patientId: createForm.patientId,
      direction: createForm.direction,
      admissionId: createForm.admissionId || undefined,
      fromDeptId: createForm.fromDeptId,
      toDeptId: createForm.toDeptId ?? undefined,
      toHospital: createForm.toHospital.trim() || undefined,
      reason: createForm.reason.trim(),
      diagnosis: createForm.diagnosis || undefined,
      contactPhone: createForm.contactPhone || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('转诊单已登记');
      createVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  } finally {
    createLoading.value = false;
  }
};
// ---------------- 确认 / 完成 / 取消 ----------------
const auditVisible = ref(false);
const auditForm = reactive({referralId: null, toDeptId: null, auditRemark: ''});
const openAudit = (row) => {
  Object.assign(auditForm, {referralId: row.referralId, toDeptId: row.toDeptId, auditRemark: ''});
  auditVisible.value = true;
};
const submitAudit = async () => {
  try {
    const res = await referralAudit(auditForm.referralId, auditForm.toDeptId ?? undefined, auditForm.auditRemark || undefined);
    if (res.code === 200) {
      ElMessage.success('转诊已确认');
      auditVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
const finishVisible = ref(false);
const finishForm = reactive({referralId: null, finishRemark: ''});
const openFinish = (row) => {
  Object.assign(finishForm, {referralId: row.referralId, finishRemark: ''});
  finishVisible.value = true;
};
const submitFinish = async () => {
  try {
    const res = await referralFinish(finishForm.referralId, finishForm.finishRemark || undefined);
    if (res.code === 200) {
      ElMessage.success('转诊已完成');
      finishVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
const cancel = async (row) => {
  try {
    const res = await referralCancel(row.referralId, '页面手动取消');
    if (res.code === 200) {
      ElMessage.success('转诊已取消');
      loadList();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
onMounted(() => {
  loadDicts();
  loadDepts();
  loadList();
});
</script>
