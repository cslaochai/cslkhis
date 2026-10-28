<template>
  <div class="emg-order-panel" data-testid="emg-order-panel">
    <el-tabs v-model="orderTab" class="emg-order-tabs">
      <!-- ===================== 检查 ===================== -->
      <el-tab-pane label="检查" name="inspection">
        <div class="mb-2 flex items-center gap-2">
          <el-button :icon="Plus" size="small" type="primary" :disabled="!editable"
                     v-perm="'opd:emergencyDoctor:edit'"
                     data-testid="emg-order-ins-add" @click="openInsAdd">开检查
          </el-button>
          <span class="text-xs text-slate-400">共 {{ insList.length }} 条申请</span>
        </div>
        <el-table :data="insList" size="small" empty-text="暂无检查申请" data-testid="emg-order-ins-table"
                  :max-height="360">
          <el-table-column label="项目" min-width="160" show-overflow-tooltip>
            <template #default="{row}">{{ row.inspectionItemName || row.inspectionItemCode || '—' }}</template>
          </el-table-column>
          <el-table-column label="部位" prop="bodyPart" min-width="90" show-overflow-tooltip/>
          <el-table-column label="急诊" width="64" align="center">
            <template #default="{row}">
              <el-tag v-if="row.isEmergency" size="small" type="danger" effect="dark">急</el-tag>
              <span v-else class="text-xs text-slate-400">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="120">
            <template #default="{row}">
              <span class="text-xs">{{ row.applyStatusText || row.applyStatus || '—' }}</span>
              <span v-if="row.execStatusText" class="ml-1 text-xs text-slate-500">· {{ row.execStatusText }}</span>
              <el-tag v-if="row.critical" size="small" type="warning" effect="plain" class="ml-1">危急值</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{row}">
              <el-button link type="primary" size="small"
                         :disabled="!row.execRecordId"
                         data-testid="emg-order-ins-report" @click="viewInsReport(row)">报告
              </el-button>
              <el-button link type="success" size="small"
                         data-testid="emg-order-ins-pacs" @click="viewPacs(row.id, 1)">影像
              </el-button>
              <el-button link type="danger" size="small" :disabled="!editable || row.canDelete === false"
                         data-testid="emg-order-ins-del" @click="delIns(row)">删
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ===================== 检验 ===================== -->
      <el-tab-pane label="检验" name="laboratory">
        <div class="mb-2 flex items-center gap-2">
          <el-button :icon="Plus" size="small" type="primary" :disabled="!editable"
                     v-perm="'opd:emergencyDoctor:edit'"
                     data-testid="emg-order-lab-add" @click="openLabAdd">开检验
          </el-button>
          <span class="text-xs text-slate-400">共 {{ labList.length }} 条申请</span>
        </div>
        <el-table :data="labList" size="small" empty-text="暂无检验申请" data-testid="emg-order-lab-table"
                  :max-height="360">
          <el-table-column label="项目" min-width="160" show-overflow-tooltip>
            <template #default="{row}">{{ row.laboratoryItemName || row.laboratoryItemCode || '—' }}</template>
          </el-table-column>
          <el-table-column label="标本" prop="specimenType" min-width="90" show-overflow-tooltip/>
          <el-table-column label="急诊" width="64" align="center">
            <template #default="{row}">
              <el-tag v-if="row.isEmergency" size="small" type="danger" effect="dark">急</el-tag>
              <span v-else class="text-xs text-slate-400">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="120">
            <template #default="{row}">
              <span class="text-xs">{{ row.applyStatusText || row.applyStatus || '—' }}</span>
              <span v-if="row.execStatusText" class="ml-1 text-xs text-slate-500">· {{ row.execStatusText }}</span>
              <el-tag v-if="row.critical" size="small" type="warning" effect="plain" class="ml-1">危急值</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{row}">
              <el-button link type="primary" size="small"
                         :disabled="!row.execRecordId"
                         data-testid="emg-order-lab-report" @click="viewLabReport(row)">报告
              </el-button>
              <el-button link type="success" size="small"
                         data-testid="emg-order-lab-pacs" @click="viewPacs(row.id, 2)">影像
              </el-button>
              <el-button link type="danger" size="small" :disabled="!editable || row.canDelete === false"
                         data-testid="emg-order-lab-del" @click="delLab(row)">删
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ===================== 危急值 ===================== -->
      <el-tab-pane :label="'危急值' + (criticalList.length ? '（' + criticalList.length + '）' : '')" name="critical">
        <el-alert v-if="criticalPending > 0" type="error" show-icon :closable="false" class="!mb-2"
                  :title="`该患者有 ${criticalPending} 条危急值待处置确认，请及时处理并到危急值管理页闭环`"
                  data-testid="emg-critical-alert"/>
        <el-table :data="criticalList" size="small" empty-text="该患者暂无危急值记录"
                  data-testid="emg-critical-table" :max-height="360">
          <el-table-column label="项目" min-width="130" show-overflow-tooltip>
            <template #default="{row}">{{ row.itemName || row.itemCode || '—' }}</template>
          </el-table-column>
          <el-table-column label="结果" min-width="110">
            <template #default="{row}">
              <span class="font-bold text-red-600">{{ row.resultValue ?? '—' }}{{ row.resultUnit || '' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="参考区间" prop="referenceRange" min-width="110" show-overflow-tooltip/>
          <el-table-column label="类型" width="72" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.criticalType === 2 ? 'danger' : 'warning'">{{ row.criticalTypeText || '—' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="闭环状态" width="90" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.status === 3 ? 'success' : (row.status === 4 ? 'info' : 'danger')">{{ row.statusText || '—' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="报告时间" width="150">
            <template #default="{row}"><span class="text-xs">{{ (row.reportTime || '').replace('T', ' ') }}</span></template>
          </el-table-column>
          <el-table-column label="处置措施" min-width="120" show-overflow-tooltip>
            <template #default="{row}"><span class="text-xs">{{ row.handleMeasure || '—' }}</span></template>
          </el-table-column>
        </el-table>
        <div class="mt-1 text-[11px] text-slate-400">危急值闭环（接收/处置/升级）在「危急值管理」页操作，此处只读提醒。</div>
      </el-tab-pane>

      <!-- ===================== 用血 ===================== -->
      <el-tab-pane label="用血" name="transfusion">
        <el-alert v-if="!admissionId" type="warning" :closable="false" show-icon
                  data-testid="emg-order-trans-hint"
                  title="需先「转住院」" description="用血申请须挂在一次住院上，请先通过上方「转住院」生成住院号后再发起。"/>
        <template v-else>
          <div class="mb-2">
            <el-button :icon="Plus" size="small" type="primary" :disabled="!editable" v-perm="'opd:emergencyDoctor:edit'"
                       data-testid="emg-order-trans-btn" @click="transVisible = true">申请用血</el-button>
          </div>
        </template>
      </el-tab-pane>

      <!-- ===================== 手术 ===================== -->
      <el-tab-pane label="手术" name="surgery">
        <el-alert v-if="!admissionId" type="warning" :closable="false" show-icon
                  data-testid="emg-order-surgery-hint"
                  title="需先「转住院」" description="手术申请须挂在一次住院上，请先通过上方「转住院」生成住院号后再发起。"/>
        <template v-else>
          <div class="mb-2">
            <el-button :icon="Plus" size="small" type="primary" :disabled="!editable" v-perm="'opd:emergencyDoctor:edit'"
                       data-testid="emg-order-surgery-btn" @click="surgeryVisible = true">申请手术</el-button>
          </div>
        </template>
      </el-tab-pane>

      <!-- ===================== 会诊 ===================== -->
      <el-tab-pane label="会诊" name="consultation">
        <el-alert v-if="!admissionId" type="warning" :closable="false" show-icon
                  data-testid="emg-order-consult-hint"
                  title="需先「转住院」" description="会诊申请须挂在一次住院上，请先通过上方「转住院」生成住院号后再发起。"/>
        <template v-else>
          <div class="mb-2">
            <el-button :icon="Plus" size="small" type="primary" :disabled="!editable" v-perm="'opd:emergencyDoctor:edit'"
                       data-testid="emg-order-consult-btn" @click="consultVisible = true">申请会诊</el-button>
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- ===================== 检查 开单弹框 ===================== -->
    <el-dialog v-model="insAddVisible" title="开检查申请" width="560px" destroy-on-close
               data-testid="emg-order-ins-dialog">
      <el-form label-width="88px" label-position="right">
        <div v-if="insTplList.length" class="mb-2 flex flex-wrap items-center gap-1" data-testid="emg-order-ins-tpl">
          <span class="text-[11px] text-slate-400">检查模板：</span>
          <el-tag v-for="t in insTplList" :key="t.id" size="small" class="cursor-pointer"
                  type="primary" effect="plain" @click="applyInsTpl(t)">{{ t.templateName }}</el-tag>
        </div>
        <el-form-item label="检查项目" required>
          <el-select v-model="insForm.inspectionItemId" filterable remote reserve-keyword size="small" style="width:100%"
                     :remote-method="searchInsRemote" placeholder="选择或搜索检查项目" data-testid="emg-order-ins-item" @change="onInsItemChange">
            <el-option v-for="it in insItemOptions" :key="it.id" :value="it.id"
                       :label="(it.itemCode ? it.itemCode + ' ' : '') + (it.itemName || it.name || '')"/>
          </el-select>
        </el-form-item>
        <el-form-item label="部位">
          <el-input v-model="insForm.bodyPart" size="small" placeholder="如：胸部"/>
        </el-form-item>
        <el-form-item label="检查目的">
          <el-input v-model="insForm.inspectionPurpose" size="small" type="textarea" :rows="2"/>
        </el-form-item>
        <el-form-item label="临床诊断">
          <el-input v-model="insForm.clinicalDiagnosis" size="small" placeholder="默认带入本次诊断"/>
        </el-form-item>
        <el-form-item label="特殊要求">
          <el-input v-model="insForm.specialRequirements" size="small" type="textarea" :rows="2"/>
        </el-form-item>
        <el-form-item label="急诊">
          <el-switch v-model="insForm.isEmergency" size="small"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="insAddVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="insSaving" data-testid="emg-order-ins-save"
                   @click="saveIns">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- ===================== 检验 开单弹框 ===================== -->
    <el-dialog v-model="labAddVisible" title="开检验申请" width="560px" destroy-on-close
               data-testid="emg-order-lab-dialog">
      <el-form label-width="88px" label-position="right">
        <div v-if="labTplList.length" class="mb-2 flex flex-wrap items-center gap-1" data-testid="emg-order-lab-tpl">
          <span class="text-[11px] text-slate-400">检验模板：</span>
          <el-tag v-for="t in labTplList" :key="t.id" size="small" class="cursor-pointer"
                  type="primary" effect="plain" @click="applyLabTpl(t)">{{ t.templateName }}</el-tag>
        </div>
        <el-form-item label="检验项目" required>
          <el-select v-model="labForm.laboratoryItemId" filterable remote reserve-keyword size="small" style="width:100%"
                     :remote-method="searchLabRemote" placeholder="选择或搜索检验项目" data-testid="emg-order-lab-item" @change="onLabItemChange">
            <el-option v-for="it in labItemOptions" :key="it.id" :value="it.id"
                       :label="(it.itemCode ? it.itemCode + ' ' : '') + (it.itemName || it.name || '')"/>
          </el-select>
        </el-form-item>
        <el-form-item label="标本类型">
          <el-input v-model="labForm.specimenType" size="small" placeholder="如：静脉血"/>
        </el-form-item>
        <el-form-item label="检验目的">
          <el-input v-model="labForm.laboratoryPurpose" size="small" type="textarea" :rows="2"/>
        </el-form-item>
        <el-form-item label="临床诊断">
          <el-input v-model="labForm.clinicalDiagnosis" size="small" placeholder="默认带入本次诊断"/>
        </el-form-item>
        <el-form-item label="空腹">
          <el-switch v-model="labForm.isFasting" size="small"/>
        </el-form-item>
        <el-form-item label="急诊">
          <el-switch v-model="labForm.isEmergency" size="small"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="labAddVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="labSaving" data-testid="emg-order-lab-save"
                   @click="saveLab">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- ===================== 报告 / 影像 弹框（复用现成组件）===================== -->
    <InspectionDetail v-model="inspDetailVisible" :order="inspDetailOrder"/>
    <LaboratoryDetail v-model="labDetailVisible" :order="labDetailOrder"/>
    <el-dialog v-model="pacsVisible" :title="pacsBizType === 1 ? '检查影像 (PACS)' : '检验影像 (PACS)'"
               width="960px" data-testid="emg-order-pacs-dialog">
      <ExamImagePanel :apply-id="pacsApplyId" :biz-type="pacsBizType" readonly/>
    </el-dialog>

    <!-- ===================== 住院类申请（转住院后可用，复用现成弹框）===================== -->
    <TransfusionApplyEdit v-model="transVisible" :row="null" :admissions="admissionCandidates"
                          :admission-label="admissionLabelFn" @saved="onInpatientSaved"/>
    <SurgeryApplyEdit v-model="surgeryVisible" :row="null" :admissions="admissionCandidates"
                      @saved="onInpatientSaved"/>
    <ConsultationApply v-model="consultVisible" :depts="deptCandidates" :admission-id="admissionIdStr"
                       :admission="admissionObj" @saved="onInpatientSaved"/>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {
  getInspectionApplyList, saveInspectionApply, deleteInspectionApply,
  getLaboratoryApplyList, saveLaboratoryApply, deleteLaboratoryApply,
} from '@/api/doctor';
import {searchInspectionItem, searchLaboratoryItem} from '@/api/system';
import {getInspectionTemplates, getLaboratoryTemplates} from '@/api/doctor';
import {getInspectionDetail, getLaboratoryDetail, getCriticalValueListPage} from '@/api/medicaltech';
import InspectionDetail from '@/views/lab/InspectionDetail.vue';
import LaboratoryDetail from '@/views/lab/LaboratoryDetail.vue';
import ExamImagePanel from '@/components/his/ExamImagePanel.vue';
import TransfusionApplyEdit from '@/views/transfusion/TransfusionApplyEdit.vue';
import SurgeryApplyEdit from '@/views/surgery/SurgeryApplyEdit.vue';
import ConsultationApply from '@/views/consultation/ConsultationApply.vue';

const props = defineProps({
  patient: {type: Object, required: true},
  recordId: {type: [String, Number], default: null},
  editable: {type: Boolean, default: true},
});

const orderTab = ref('inspection');
const patientId = computed(() => props.patient?.patientId);
const registId = computed(() => props.patient?.registId);
const admissionId = computed(() => props.patient?.admissionId);
const admissionIdStr = computed(() => (admissionId.value != null ? String(admissionId.value) : ''));
const admissionObj = computed(() => (admissionId.value != null
  ? {admissionId: admissionId.value, patientId: patientId.value, patientName: props.patient?.patientName}
  : null));
const admissionCandidates = computed(() => (admissionObj.value ? [admissionObj.value] : []));
const deptCandidates = ref([]);
const admissionLabelFn = (a) => `住院号 ${a?.admissionId ?? ''}`;

// ---------- 检查 ----------
const insList = ref([]);
const insItemOptions = ref([]);
const insTplList = ref([]);
const insAddVisible = ref(false);
const insSaving = ref(false);
const insForm = reactive({
  inspectionItemId: null, inspectionItemName: '', bodyPart: '', inspectionPurpose: '',
  clinicalDiagnosis: '', specialRequirements: '', isEmergency: true,
});

// ---------- 检验 ----------
const labList = ref([]);
const labItemOptions = ref([]);
const labTplList = ref([]);
const labAddVisible = ref(false);
const labSaving = ref(false);
const labForm = reactive({
  laboratoryItemId: null, laboratoryItemName: '', specimenType: '', laboratoryPurpose: '',
  clinicalDiagnosis: '', isFasting: false, isEmergency: true,
});

// ---------- 报告 / 影像 ----------
const inspDetailVisible = ref(false);
const inspDetailOrder = ref(null);
const labDetailVisible = ref(false);
const labDetailOrder = ref(null);
const pacsVisible = ref(false);
const pacsApplyId = ref(null);
const pacsBizType = ref(1);

// ---------- 住院类 ----------
const transVisible = ref(false);
const surgeryVisible = ref(false);
const consultVisible = ref(false);

// ---------- 加载列表 ----------
const loadIns = async () => {
  if (!patientId.value || !registId.value) return;
  try {
    const res = await getInspectionApplyList({patientId: patientId.value, registId: registId.value});
    insList.value = res.data || [];
  } catch (e) { /* 全局提示 */
  }
};
const loadLab = async () => {
  if (!patientId.value || !registId.value) return;
  try {
    const res = await getLaboratoryApplyList({patientId: patientId.value, registId: registId.value});
    labList.value = res.data || [];
  } catch (e) { /* 全局提示 */
  }
};

// ---------- 检查 开单 ----------
const openInsAdd = async () => {
  Object.assign(insForm, {
    inspectionItemId: null, inspectionItemName: '', bodyPart: '', inspectionPurpose: '',
    clinicalDiagnosis: props.patient?.diagnosisName || '', specialRequirements: '', isEmergency: true,
  });
  await loadInsItems();
  insAddVisible.value = true;
};
const loadInsItems = async () => {
  try {
    // 用 selectList（isAuthenticated，全量启用项目），ListPage 有岗位权限码医生角色会 403
    const res = await searchInspectionItem('');
    insItemOptions.value = res.data || [];
  } catch (e) { insItemOptions.value = []; }
};
const searchInsRemote = async (query) => {
  if (!query) return; // 空查询保留预填列表，供首屏直接选
  try {
    const res = await searchInspectionItem(query);
    insItemOptions.value = res.data || [];
  } catch (e) { /* 全局提示 */ }
};
const onInsItemChange = (val) => {
  const it = insItemOptions.value.find((o) => o.id === val);
  insForm.inspectionItemName = it?.itemName || it?.name || '';
  if (it?.bodyPart && !insForm.bodyPart) insForm.bodyPart = it.bodyPart;
};
const saveIns = async () => {
  if (!insForm.inspectionItemId) { ElMessage.warning('请选择检查项目'); return; }
  insSaving.value = true;
  try {
    const payload = {
      registId: registId.value,
      patientId: patientId.value,
      patientName: props.patient?.patientName,
      patientNo: props.patient?.patientNo,
      inspectionItemId: insForm.inspectionItemId,
      inspectionItemName: insForm.inspectionItemName,
      bodyPart: insForm.bodyPart,
      inspectionPurpose: insForm.inspectionPurpose,
      clinicalDiagnosis: insForm.clinicalDiagnosis,
      specialRequirements: insForm.specialRequirements,
      isEmergency: insForm.isEmergency ? 1 : 0,
    };
    if (props.recordId) payload.recordId = props.recordId;
    await saveInspectionApply(payload);
    ElMessage.success('检查申请已提交');
    insAddVisible.value = false;
    await loadIns();
  } catch (e) {
    ElMessage.error(String(e?.message || '开单失败'));
  } finally {
    insSaving.value = false;
  }
};
const delIns = async (row) => {
  try {
    await deleteInspectionApply(row.id);
    ElMessage.success('已删除');
    await loadIns();
  } catch (e) { /* 全局提示 */
  }
};

// ---------- 检验 开单 ----------
const openLabAdd = async () => {
  Object.assign(labForm, {
    laboratoryItemId: null, laboratoryItemName: '', specimenType: '', laboratoryPurpose: '',
    clinicalDiagnosis: props.patient?.diagnosisName || '', isFasting: false, isEmergency: true,
  });
  await loadLabItems();
  labAddVisible.value = true;
};
const loadLabItems = async () => {
  try {
    const res = await searchLaboratoryItem('');
    labItemOptions.value = res.data || [];
  } catch (e) { labItemOptions.value = []; }
};
const searchLabRemote = async (query) => {
  if (!query) return;
  try {
    const res = await searchLaboratoryItem(query);
    labItemOptions.value = res.data || [];
  } catch (e) { /* 全局提示 */ }
};
const onLabItemChange = (val) => {
  const it = labItemOptions.value.find((o) => o.id === val);
  labForm.laboratoryItemName = it?.itemName || it?.name || '';
  if (it?.specimenType && !labForm.specimenType) labForm.specimenType = it.specimenType;
};
const saveLab = async () => {
  if (!labForm.laboratoryItemId) { ElMessage.warning('请选择检验项目'); return; }
  labSaving.value = true;
  try {
    const payload = {
      registId: registId.value,
      patientId: patientId.value,
      patientName: props.patient?.patientName,
      patientNo: props.patient?.patientNo,
      laboratoryItemId: labForm.laboratoryItemId,
      laboratoryItemName: labForm.laboratoryItemName,
      specimenType: labForm.specimenType,
      laboratoryPurpose: labForm.laboratoryPurpose,
      clinicalDiagnosis: labForm.clinicalDiagnosis,
      isFasting: labForm.isFasting ? 1 : 0,
      isEmergency: labForm.isEmergency ? 1 : 0,
    };
    if (props.recordId) payload.recordId = props.recordId;
    await saveLaboratoryApply(payload);
    ElMessage.success('检验申请已提交');
    labAddVisible.value = false;
    await loadLab();
  } catch (e) {
    ElMessage.error(String(e?.message || '开单失败'));
  } finally {
    labSaving.value = false;
  }
};
const delLab = async (row) => {
  try {
    await deleteLaboratoryApply(row.id);
    ElMessage.success('已删除');
    await loadLab();
  } catch (e) { /* 全局提示 */
  }
};

// ---------- 报告 / 影像 ----------
const viewInsReport = async (row) => {
  if (!row.execRecordId) { ElMessage.info('该检查尚未执行，暂无报告'); return; }
  try {
    const res = await getInspectionDetail(row.execRecordId);
    inspDetailOrder.value = res.data || {id: row.execRecordId};
    inspDetailVisible.value = true;
  } catch (e) { ElMessage.error(String(e?.message || '读取报告失败')); }
};
const viewLabReport = async (row) => {
  if (!row.execRecordId) { ElMessage.info('该检验尚未执行，暂无报告'); return; }
  try {
    const res = await getLaboratoryDetail(row.execRecordId);
    labDetailOrder.value = res.data || {id: row.execRecordId};
    labDetailVisible.value = true;
  } catch (e) { ElMessage.error(String(e?.message || '读取报告失败')); }
};
const viewPacs = (applyId, bizType) => {
  pacsApplyId.value = applyId;
  pacsBizType.value = bizType;
  pacsVisible.value = true;
};

const onInpatientSaved = () => {
  transVisible.value = false;
  surgeryVisible.value = false;
  consultVisible.value = false;
  ElMessage.success('住院类申请已提交');
};

// ---------- 危急值（当前患者，只读；闭环在危急值管理页） ----------
const criticalList = ref([]);
const criticalPending = computed(() => criticalList.value.filter((r) => r.status === 1 || r.status === 2).length);
const loadCritical = async () => {
  if (!patientId.value) return;
  try {
    const res = await getCriticalValueListPage({patientId: patientId.value, pageNum: 1, pageSize: 50});
    criticalList.value = res.data?.records || [];
  } catch (e) { criticalList.value = []; }
};

// ---------- 检查/检验模板 chips（复用医生个人模板，点击整单预填） ----------
const loadTpls = async () => {
  try {
    const [ins, lab] = await Promise.all([getInspectionTemplates(), getLaboratoryTemplates()]);
    insTplList.value = ins.data || [];
    labTplList.value = lab.data || [];
  } catch (e) {
    insTplList.value = [];
    labTplList.value = [];
  }
};
const applyInsTpl = (t) => {
  Object.assign(insForm, {
    inspectionItemId: t.inspectionItemId, inspectionItemName: t.inspectionItemName,
    bodyPart: t.bodyPart || '', inspectionPurpose: t.inspectionPurpose || '',
    isEmergency: t.isEmergency === 1,
  });
  // 模板项目若不在当前下拉候选里（停用/漏载），补进候选避免 select 显示裸 id
  if (t.inspectionItemId && !insItemOptions.value.some((o) => o.id === t.inspectionItemId)) {
    insItemOptions.value = [
      {id: t.inspectionItemId, itemCode: t.inspectionItemCode, itemName: t.inspectionItemName},
      ...insItemOptions.value,
    ];
  }
  ElMessage.success('已套用模板：' + t.templateName);
};
const applyLabTpl = (t) => {
  Object.assign(labForm, {
    laboratoryItemId: t.laboratoryItemId, laboratoryItemName: t.laboratoryItemName,
    specimenType: t.sampleType || '', laboratoryPurpose: t.inspectionPurpose || '',
    isEmergency: t.isEmergency === 1,
  });
  if (t.laboratoryItemId && !labItemOptions.value.some((o) => o.id === t.laboratoryItemId)) {
    labItemOptions.value = [
      {id: t.laboratoryItemId, itemCode: t.laboratoryItemCode, itemName: t.laboratoryItemName},
      ...labItemOptions.value,
    ];
  }
  ElMessage.success('已套用模板：' + t.templateName);
};

// ---------- 生命周期 ----------
const reloadAll = () => { loadIns(); loadLab(); loadCritical(); };
onMounted(() => { reloadAll(); loadTpls(); });
watch(() => props.patient?.id, () => { insList.value = []; labList.value = []; criticalList.value = []; reloadAll(); });
</script>

<style scoped>
:deep(.el-table .el-button.is-link) { vertical-align: middle; }
</style>
