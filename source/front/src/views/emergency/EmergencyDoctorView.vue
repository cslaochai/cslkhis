<template>
  <div class="flex h-full flex-col overflow-hidden bg-slate-50">
    <!-- ========== 顶栏：标题 + 队列 tab + 统计 ========== -->
    <div class="flex items-center gap-3 border-b border-slate-200 bg-white px-4 py-2.5">
      <h1 class="text-base font-bold text-slate-800">急诊医生工作站</h1>
      <el-radio-group v-model="queueTab" size="small" @change="reloadQueue">
        <el-radio-button value="mine">我的候诊</el-radio-button>
        <el-radio-button value="pool">待派单池</el-radio-button>
      </el-radio-group>
      <div class="flex items-center gap-3 text-xs text-slate-500">
        <span data-testid="emg-doc-count-mine">我的候诊 <b class="text-slate-800">{{ mineCount }}</b> 人</span>
        <span data-testid="emg-doc-count-critical">危重（I/II级）<b class="text-red-600">{{ criticalCount }}</b> 人</span>
        <span data-testid="emg-doc-count-pool">待派单池 <b class="text-slate-800">{{ poolCount }}</b> 人</span>
      </div>
      <div class="flex-1"></div>
      <el-button :icon="Refresh" size="small" data-testid="emg-doc-refresh" @click="reloadQueue">刷新</el-button>
    </div>

    <div class="flex flex-1 overflow-hidden">
      <!-- ========== 左栏：候诊队列 ========== -->
      <div class="w-[400px] shrink-0 overflow-auto border-r border-slate-200 bg-white" data-testid="emg-doc-queue">
        <div v-if="queueList.length === 0" class="p-8 text-center text-sm text-slate-400">
          {{ queueTab === 'mine' ? '暂无派给我的候诊患者' : '待派单池是空的' }}
        </div>
        <div
            v-for="row in queueList"
            :key="row.id"
            class="cursor-pointer border-b border-slate-100 px-3 py-2.5 hover:bg-blue-50/60"
            :class="current && String(current.id) === String(row.id) ? 'bg-blue-50 border-l-4 border-l-blue-500' : 'border-l-4 border-l-transparent'"
            :data-testid="'emg-doc-card-' + row.id"
            @click="selectPatient(row)"
        >
          <div class="flex items-center gap-1.5">
            <span class="text-sm font-bold text-slate-800">{{ row.patientName }}</span>
            <span class="text-xs text-slate-400">{{ genderText(row.gender) }} / {{ row.age ?? '—' }}岁</span>
            <el-tag size="small" :class="levelOf(row).color" class="!border">{{ levelOf(row).label }}</el-tag>
            <el-tag v-if="row.mewsScore != null && row.mewsScore >= 5" size="small" type="danger" effect="dark">
              MEWS {{ row.mewsScore }}
            </el-tag>
            <el-tag v-else-if="row.mewsScore != null" size="small" type="info">MEWS {{ row.mewsScore }}</el-tag>
            <el-tag v-if="row.greenChannel && row.greenChannel !== '无'" size="small" type="success" effect="dark">
              {{ row.greenChannel }}
            </el-tag>
            <span class="flex-1"></span>
            <span v-if="row.overdueLevel > 0" class="text-xs font-bold text-red-600">超时 {{ row.waitMinutes }}分</span>
          </div>
          <div class="mt-1 flex items-center gap-2 text-xs text-slate-400">
            <span class="font-mono">{{ row.emergencyNo }}</span>
            <span v-if="row.waitMinutes != null">候诊 {{ row.waitMinutes }} 分</span>
            <span v-if="row.assignTypeText" class="text-slate-500">{{ row.assignTypeText }}</span>
          </div>
          <div class="mt-0.5 truncate text-xs text-slate-500" :title="row.chiefComplaint">{{ row.chiefComplaint || '（未填主诉）' }}</div>
        </div>
      </div>

      <!-- ========== 右栏：工作区 ========== -->
      <div class="flex flex-1 flex-col overflow-hidden">
        <div v-if="!current" class="flex flex-1 flex-col items-center justify-center text-slate-400">
          <el-icon :size="48"><FirstAidKit/></el-icon>
          <p class="mt-3 text-sm">请从左侧队列选择患者开始接诊</p>
          <p class="mt-1 text-xs">「我的候诊」是派给你的；「待派单池」里的患者可以抢单接诊</p>
        </div>

        <template v-else>
          <!-- 患者条 + 急诊要素 -->
          <div class="border-b border-slate-200 bg-white px-4 py-2">
            <PatientBriefBar
                :patient="current"
                :detail="patientDetail"
                :record="recordForm"
                :arrive-text="'候诊 ' + (current.waitMinutes ?? 0) + ' 分'"
            />
            <div class="mt-2 flex flex-wrap items-center gap-1.5 text-xs">
              <el-tag size="small" :class="levelOf(current).color" class="!border">{{ levelOf(current).label }} · {{ levelOf(current).zone }}</el-tag>
              <el-tag v-if="current.greenChannel && current.greenChannel !== '无'" size="small" type="success">{{ current.greenChannel }}</el-tag>
              <el-tag size="small" type="info">急诊号 {{ current.emergencyNo }}</el-tag>
              <el-tag size="small" :type="statusTagType(current.emergencyStatus)">{{ statusText(current.emergencyStatus) }}</el-tag>
              <span v-for="v in vitalChips" :key="v.label" class="rounded bg-slate-100 px-1.5 py-0.5 text-slate-600">
                {{ v.label }} {{ v.value }}
              </span>
              <span class="text-slate-400">意识：{{ current.consciousnessText || '—' }}</span>
            </div>
          </div>

          <!-- 操作条 -->
          <div class="flex items-center gap-2 border-b border-slate-200 bg-white px-4 py-2">
            <el-button
                v-if="current.emergencyStatus === 1"
                v-perm="'opd:emergencyDoctor:edit'"
                type="primary" :icon="VideoPlay" data-testid="emg-doc-btn-accept"
                @click="acceptPatient"
            >接诊</el-button>
            <el-button
                v-if="current.emergencyStatus >= 2 && current.emergencyStatus <= 3"
                v-perm="'opd:emergencyDoctor:edit'"
                type="warning" :icon="Switch" data-testid="emg-doc-btn-transfer"
                @click="transferVisible = true"
            >转归</el-button>
            <span class="text-xs text-slate-400">
              {{ current.emergencyStatus === 1 ? '接诊后才能书写病历和开处方' : current.doctorName ? '接诊医生：' + current.doctorName : '' }}
            </span>
            <div class="flex-1"></div>
            <el-button
                v-if="current.emergencyStatus === 2"
                type="primary" :loading="saving" data-testid="emg-doc-btn-save"
                @click="saveAll"
            >保存（病历 + 处方）</el-button>
          </div>

          <!-- 病历 / 处方 -->
          <div class="flex-1 overflow-auto p-4">
            <el-alert
                v-if="current.emergencyStatus !== 2"
                :title="current.emergencyStatus === 1 ? '该患者还未接诊——点上方「接诊」后开始书写' : '该患者已转归结束，内容只读'"
                type="info" show-icon :closable="false" class="!mb-3"
            />
            <el-tabs v-model="activeTab" class="emg-doc-tabs">
              <!-- ===== 病历 ===== -->
              <el-tab-pane label="病历" name="record">
                <div class="grid grid-cols-2 gap-3">
                  <el-form label-position="top" :disabled="current.emergencyStatus !== 2">
                    <el-form-item label="主诉">
                      <el-input v-model="recordForm.chiefComplaint" type="textarea" :rows="2" data-testid="emg-doc-chief"/>
                    </el-form-item>
                    <el-form-item label="现病史">
                      <el-input v-model="recordForm.presentIllness" type="textarea" :rows="4" data-testid="emg-doc-present"/>
                    </el-form-item>
                    <el-form-item label="过敏史">
                      <el-input v-model="recordForm.allergyHistory" data-testid="emg-doc-allergy"/>
                    </el-form-item>
                    <el-form-item label="既往史">
                      <el-input v-model="recordForm.pastHistory" type="textarea" :rows="2"/>
                    </el-form-item>
                    <el-form-item label="个人史">
                      <el-input v-model="recordForm.personalHistory" type="textarea" :rows="2"/>
                    </el-form-item>
                    <el-form-item label="家族史">
                      <el-input v-model="recordForm.familyHistory" type="textarea" :rows="2"/>
                    </el-form-item>
                  </el-form>
                  <el-form label-position="top" :disabled="current.emergencyStatus !== 2">
                    <div class="mb-1 text-xs font-bold text-slate-500">生命体征</div>
                    <div class="grid grid-cols-3 gap-2">
                      <el-form-item label="体温℃"><el-input v-model="recordForm.temperature"/></el-form-item>
                      <el-form-item label="脉搏"><el-input v-model="recordForm.pulse"/></el-form-item>
                      <el-form-item label="呼吸"><el-input v-model="recordForm.respiration"/></el-form-item>
                      <el-form-item label="收缩压"><el-input v-model="recordForm.systolicPressure"/></el-form-item>
                      <el-form-item label="舒张压"><el-input v-model="recordForm.diastolicPressure"/></el-form-item>
                    </div>
                    <div class="mb-1 mt-2 text-xs font-bold text-slate-500">诊断</div>
                    <el-form-item label="诊断名称">
                      <el-input v-model="recordForm.diagnosisName" placeholder="ICD-10 诊断名称" data-testid="emg-doc-diag-name"/>
                    </el-form-item>
                    <el-form-item label="诊断编码">
                      <el-input v-model="recordForm.diagnosisCode" placeholder="ICD-10 编码" data-testid="emg-doc-diag-code"/>
                    </el-form-item>
                    <el-form-item label="处置计划">
                      <el-input v-model="recordForm.treatmentPlan" type="textarea" :rows="3"/>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>

              <!-- ===== 处方 ===== -->
              <el-tab-pane :label="'处方' + (rxDetailCount ? '（' + rxDetailCount + '）' : '')" name="rx">
                <div v-if="current.emergencyStatus !== 2" class="py-6 text-center text-sm text-slate-400">接诊后可开处方</div>
                <template v-else>
                  <div class="mb-3 flex items-center gap-2">
                    <el-radio-group v-model="currentRxType" size="small" @change="switchRxTab">
                      <el-radio-button :value="1">西药</el-radio-button>
                      <el-radio-button :value="2">中成药</el-radio-button>
                      <el-radio-button :value="3">中药饮片</el-radio-button>
                    </el-radio-group>
                    <span class="flex-1"></span>
                    <template v-if="currentRxType === 3">
                      <span class="text-xs text-slate-500">剂数</span>
                      <el-input-number v-model="rxDraft.doseCount" :min="1" :max="30" size="small" class="!w-24"/>
                      <span class="text-xs text-slate-500">煎服</span>
                      <el-select v-model="rxDraft.decoctFlag" size="small" class="!w-28" placeholder="选择">
                        <el-option :value="1" label="代煎"/>
                        <el-option :value="2" label="自煎"/>
                      </el-select>
                    </template>
                    <!-- 慢病长处方（处方级）：仅西药/中成药；患者须有已认定慢病档案（服务端强校验，此处只做 disabled 预检） -->
                    <template v-if="currentRxType !== 3">
                      <span class="text-xs text-slate-500" data-testid="emg-doc-longrx-label">慢病长处方</span>
                      <el-switch v-model="rxDraft.isLongPrescription" :disabled="!chronicQualified"
                                 data-testid="emg-doc-longrx-switch" size="small"/>
                      <template v-if="rxDraft.isLongPrescription">
                        <span class="text-xs text-slate-500">天数</span>
                        <el-input-number v-model="rxDraft.longPrescriptionDays" :min="1" :max="90" size="small"
                                         class="!w-24" data-testid="emg-doc-longrx-days"/>
                        <span class="text-[10px] text-slate-400">1~90 天</span>
                      </template>
                      <el-tooltip v-else-if="!chronicQualified" effect="dark" content="该患者无已认定的慢病档案，不能开慢病长处方">
                        <span class="text-[10px] text-amber-600" data-testid="emg-doc-longrx-nochronic">无慢病档案</span>
                      </el-tooltip>
                      <el-tooltip v-else effect="dark" :content="'已认定：' + chronicDiseaseText">
                        <span class="text-[10px] text-slate-400">{{ chronicDiseaseText }}</span>
                      </el-tooltip>
                    </template>
                    <el-button :icon="Plus" size="small" data-testid="emg-doc-rx-add-row" @click="addRxRow">加一行</el-button>
                  </div>

                  <el-table :data="rxDraft.details" size="small" empty-text="暂无明细，点「加一行」开始开方" data-testid="emg-doc-rx-table">
                    <el-table-column label="药品" min-width="180">
                      <template #default="{row}">
                        <el-select
                            v-model="row.drugId" filterable remote reserve-keyword
                            :remote-method="searchDrug" :loading="drugLoading" size="small"
                            placeholder="输入药品名搜索" @change="onDrugPicked(row)"
                        >
                          <el-option v-for="d in drugResults" :key="d.id" :value="d.drugId"
                                     :label="d.drugName + (d.specification ? '（' + d.specification + '）' : '')"/>
                        </el-select>
                      </template>
                    </el-table-column>
                    <el-table-column label="规格" min-width="110">
                      <template #default="{row}"><span class="text-xs text-slate-500">{{ row.specification || '—' }}</span></template>
                    </el-table-column>
                    <el-table-column :label="currentRxType === 3 ? '每剂克数' : '单次用量'" width="120">
                      <template #default="{row}"><el-input v-model="row.singleDosage" size="small"/></template>
                    </el-table-column>
                    <el-table-column label="频次" width="120">
                      <template #default="{row}">
                        <el-select v-model="row.frequency" size="small">
                          <el-option v-if="currentRxType === 3" value="每日一剂" label="每日一剂"/>
                          <el-option v-if="currentRxType === 3" value="每日两剂" label="每日两剂"/>
                          <el-option v-if="currentRxType !== 3" value="一日一次" label="一日一次"/>
                          <el-option v-if="currentRxType !== 3" value="一日两次" label="一日两次"/>
                          <el-option v-if="currentRxType !== 3" value="一日三次" label="一日三次"/>
                        </el-select>
                      </template>
                    </el-table-column>
                    <el-table-column label="途径/煎法" width="120">
                      <template #default="{row}">
                        <el-select v-model="row.route" size="small">
                          <el-option v-if="currentRxType === 3" value="先煎" label="先煎"/>
                          <el-option v-if="currentRxType === 3" value="后下" label="后下"/>
                          <el-option v-if="currentRxType !== 3" value="口服" label="口服"/>
                          <el-option v-if="currentRxType !== 3" value="静脉注射" label="静脉注射"/>
                          <el-option v-if="currentRxType !== 3" value="肌肉注射" label="肌肉注射"/>
                          <el-option v-if="currentRxType !== 3" value="外用" label="外用"/>
                        </el-select>
                      </template>
                    </el-table-column>
                    <el-table-column label="总量" width="100">
                      <template #default="{row}"><el-input v-model="row.quantity" size="small"/></template>
                    </el-table-column>
                    <el-table-column v-if="currentRxType !== 3" label="天数" width="100">
                      <template #default="{row}"><el-input v-model="row.duration" size="small"/></template>
                    </el-table-column>
                    <el-table-column label="备注" min-width="110">
                      <template #default="{row}"><el-input v-model="row.remark" size="small"/></template>
                    </el-table-column>
                    <el-table-column label="" width="56">
                      <template #default="{ $index }">
                        <el-button link type="danger" size="small" @click="rxDraft.details.splice($index, 1)">删</el-button>
                      </template>
                    </el-table-column>
                  </el-table>

                  <div v-if="rxErrors.length" class="mt-2 rounded border border-red-200 bg-red-50 p-2 text-xs text-red-700" data-testid="emg-doc-rx-errors">
                    <div v-for="(e, i) in rxErrors" :key="i">· {{ e }}</div>
                  </div>
                </template>
              </el-tab-pane>
            </el-tabs>
          </div>
        </template>
      </div>
    </div>

    <!-- ========== 转归面板（复用护士站同款组件）========== -->
    <EmergencyTransferEdit v-model="transferVisible" :row="current" @saved="onTransferSaved"/>

    <!-- ========== 处方签名板（复用门诊医生工作站同款组件）========== -->
    <RxSignDialog
        v-model="rxSignVisible"
        :doctor-name="current?.doctorName || ''"
        :prescription-count="pendingRxCount"
        @confirm="onRxSigned"
        @cancel="rxSignVisible = false"
    />
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {FirstAidKit, Plus, Refresh, Switch, VideoPlay} from '@element-plus/icons-vue';
import PatientBriefBar from '@/components/his/PatientBriefBar.vue';
import RxSignDialog from '@/views/doctor-workstation/RxSignDialog.vue';
import EmergencyTransferEdit from '@/views/emergency/EmergencyTransferEdit.vue';
import {getEmergencyList, updateEmergencyStatus} from '@/api/emergency';
import {getPrescriptionList, saveMedicalRecord, submitMedicalRecord} from '@/api/doctor';
import {handleEmrSubmitVeto} from '@/lib/emrSubmit';
import {getByRegistId} from '@/api/emr';
import {getPatientFullDetail} from '@/api/patient';
import {getStockList} from '@/api/pharmacy';
import {getChronicActiveByPatient} from '@/api/chronic';
import {PATIENT_GENDER_OPTIONS} from '@/lib/patientGender';
import {useCurrentPatientStore} from '@/stores/currentPatient';

// ---------- 常量（口径与急诊护士工作站 EmergencyView 同源，不引变量避免跨页耦合）----------
const triageLevelMap = {
  1: {label: 'I级 濒危', color: 'bg-red-100 text-red-700 border-red-300', zone: '红区'},
  2: {label: 'II级 危重', color: 'bg-orange-100 text-orange-700 border-orange-300', zone: '红区'},
  3: {label: 'III级 急症', color: 'bg-amber-100 text-amber-700 border-amber-300', zone: '黄区'},
  4: {label: 'IV级 非急症', color: 'bg-emerald-100 text-emerald-700 border-emerald-300', zone: '绿区'},
};
const statusMap = {
  1: {label: '候诊', type: 'primary'},
  2: {label: '诊治中', type: 'warning'},
  3: {label: '留观', type: 'info'},
  4: {label: '转住院', type: 'success'},
  5: {label: '离院', type: 'info'},
  6: {label: '死亡', type: 'danger'},
};
const RX_TYPE_LABEL = {1: '西药', 2: '中成药', 3: '中药饮片'};

// ---------- 队列 ----------
const queueTab = ref('mine'); // mine=我的候诊 pool=待派单池
const queueList = ref([]);
const mineCount = ref(0);
const poolCount = ref(0);
const criticalCount = ref(0);
const current = ref(null);
let pollTimer = null;
const currentPatientStore = useCurrentPatientStore();

const levelOf = (row) => triageLevelMap[row?.triageLevel] || triageLevelMap[4];
const statusText = (s) => (statusMap[s] || {label: '—'}).label;
const statusTagType = (s) => (statusMap[s] || {type: 'info'}).type;
const genderText = (g) => (PATIENT_GENDER_OPTIONS.find((o) => o.value === g) || {}).label || '未知';

const reloadQueue = async () => {
  try {
    const [mine, pool] = await Promise.all([
      getEmergencyList({mineOnly: true, emergencyStatus: 1, pageNum: 1, pageSize: 100}),
      getEmergencyList({unassignedOnly: true, pageNum: 1, pageSize: 100}),
    ]);
    const mineRows = mine.data?.records || [];
    const poolRows = pool.data?.records || [];
    mineCount.value = mineRows.length;
    poolCount.value = poolRows.length;
    criticalCount.value = mineRows.filter((r) => r.triageLevel <= 2).length;
    queueList.value = queueTab.value === 'mine' ? mineRows : poolRows;
  } catch (e) { /* 错误已由全局拦截器提示 */ }
};

// ---------- 当前患者 ----------
const patientDetail = ref(null);
const activeTab = ref('record');
const recordForm = reactive({
  id: null, chiefComplaint: '', presentIllness: '', allergyHistory: '',
  pastHistory: '', personalHistory: '', familyHistory: '',
  temperature: '', pulse: '', respiration: '', systolicPressure: '', diastolicPressure: '',
  diagnosis: '', diagnosisCode: '', diagnosisName: '', treatmentPlan: '',
});

const vitalChips = computed(() => {
  let v = {};
  try { v = JSON.parse(current.value?.vitalSigns || '{}') || {}; } catch (e) { v = {}; }
  const chips = [];
  if (v.temperature != null && v.temperature !== '') chips.push({label: 'T', value: v.temperature + '℃'});
  if (v.pulse != null && v.pulse !== '') chips.push({label: 'P', value: v.pulse});
  if (v.respiration != null && v.respiration !== '') chips.push({label: 'R', value: v.respiration});
  if (v.systolicPressure != null && v.diastolicPressure != null) chips.push({label: 'BP', value: v.systolicPressure + '/' + v.diastolicPressure});
  return chips;
});

const selectPatient = async (row) => {
  current.value = row;
  activeTab.value = 'record';
  resetForms();
  // 同步顶部「当前患者」条（口径同门诊医生工作站：患者条亮起后可全局带人）
  currentPatientStore.syncPatient({
    id: String(row.patientId), patientName: row.patientName, patientNo: row.patientNo,
    gender: row.gender, age: row.age,
  });
  try {
    const [detail, recordRes, rxRes, chronicRes] = await Promise.all([
      getPatientFullDetail(row.patientId),
      row.registId ? getByRegistId({patientId: row.patientId, registId: row.registId}) : Promise.resolve({data: null}),
      row.registId ? getPrescriptionList({patientId: row.patientId, registId: row.registId}) : Promise.resolve({data: []}),
      getChronicActiveByPatient(row.patientId),
    ]);
    chronicRecords.value = chronicRes.data || [];
    chronicQualified.value = chronicRecords.value.length > 0;
    patientDetail.value = detail.data || null;
    if (recordRes.data) Object.assign(recordForm, recordRes.data);
    // rxLoaded 是固定引用的数组：只改内容不改引用（rxDetailCount computed 盯着它）
    rxLoaded.length = 0;
    rxLoaded.push(...(rxRes.data || []));
    bindRxDraft();
  } catch (e) { /* 错误已由全局拦截器提示 */ }
};

const resetForms = () => {
  Object.assign(recordForm, {
    id: null, chiefComplaint: '', presentIllness: '', allergyHistory: '',
    pastHistory: '', personalHistory: '', familyHistory: '',
    temperature: '', pulse: '', respiration: '', systolicPressure: '', diastolicPressure: '',
    diagnosis: '', diagnosisCode: '', diagnosisName: '', treatmentPlan: '',
  });
  rxLoaded.length = 0;
  bindRxDraft();
};

// ---------- 接诊 ----------
const acceptPatient = async () => {
  if (!current.value) return;
  try {
    await updateEmergencyStatus(current.value.id, 2);
    ElMessage.success(`已接诊：${current.value.patientName}`);
    // 本地即时翻态，让按钮/门控马上正确，再拉队列（该患者会从候诊列表消失）
    current.value = {...current.value, emergencyStatus: 2, doctorName: current.value.doctorName || '我'};
    await reloadQueue();
  } catch (e) { /* 错误已由全局拦截器提示 */ }
};

// ---------- 转归 ----------
const transferVisible = ref(false);
const onTransferSaved = () => {
  transferVisible.value = false;
  current.value = null;
  reloadQueue();
};

// ---------- 慢病长处方资格（患者切换即查；服务端才是最终权威，此处只为开关 disabled 预检） ----------
const chronicQualified = ref(false);
const chronicRecords = ref([]);
const chronicDiseaseText = computed(() => chronicRecords.value.map((r) => r.diseaseName).filter(Boolean).join('、'));
const loadChronicQualification = async (patientId) => {
  chronicQualified.value = false;
  chronicRecords.value = [];
  if (!patientId) return;
  try {
    const res = await getChronicActiveByPatient(patientId);
    chronicRecords.value = res.data || [];
    chronicQualified.value = chronicRecords.value.length > 0;
  } catch (e) {
    // 资格查询失败不当有资格（开关禁用）；真漏判由服务端保存时拦，不假装能开
    chronicQualified.value = false;
  }
};

// ---------- 处方 ----------
const currentRxType = ref(1);
const rxLoaded = []; // 已从服务端加载的处方列表（三个类型各一张）
const rxDraft = reactive({doseCount: 7, decoctFlag: null, details: [],
  // 慢病长处方（处方级）：患者有已认定慢病档案才允许开；天数 1~90，服务端强校验
  isLongPrescription: false, longPrescriptionDays: 28});
const drugResults = ref([]);
const drugLoading = ref(false);
const rxErrors = ref([]);

const rxDetailCount = computed(() => rxLoaded.reduce((s, p) => s + (p.details?.length || 0), 0));

const bindRxDraft = () => {
  const found = rxLoaded.find((p) => p.prescriptionType === currentRxType.value);
  rxDraft.details = found?.details ? JSON.parse(JSON.stringify(found.details)) : [];
  rxDraft.doseCount = found?.doseCount || 7;
  rxDraft.decoctFlag = found?.decoctFlag ?? null;
  rxDraft.isLongPrescription = found?.isLongPrescription === 1;
  rxDraft.longPrescriptionDays = found?.longPrescriptionDays > 0 ? found.longPrescriptionDays : 28;
};

const switchRxTab = () => {
  // 切走前把当前编辑态回写到 rxLoaded，避免丢
  flushRxDraft();
  bindRxDraft();
};

const flushRxDraft = () => {
  let found = rxLoaded.find((p) => p.prescriptionType === currentRxType.value);
  if (!found) {
    if (!rxDraft.details.length) return;
    found = {prescriptionType: currentRxType.value, diagnosis: '', usageInstruction: '', details: [],
      isLongPrescription: 0, longPrescriptionDays: null};
    rxLoaded.push(found);
  }
  found.details = rxDraft.details;
  found.doseCount = rxDraft.doseCount;
  found.decoctFlag = rxDraft.decoctFlag;
  found.isLongPrescription = rxDraft.isLongPrescription ? 1 : 0;
  found.longPrescriptionDays = rxDraft.isLongPrescription ? rxDraft.longPrescriptionDays : null;
};

const addRxRow = () => {
  rxDraft.details.push({drugId: null, drugName: '', specification: '', unit: '', singleDosage: '', frequency: currentRxType.value === 3 ? '每日一剂' : '一日三次', route: currentRxType.value === 3 ? '' : '口服', quantity: '', duration: '', remark: ''});
};

const searchDrug = async (query) => {
  if (!query) { drugResults.value = []; return; }
  drugLoading.value = true;
  try {
    const res = await getStockList({drugName: query, pageNum: 1, pageSize: 20});
    drugResults.value = res.data?.records || [];
  } catch (e) { /* 全局提示 */ } finally { drugLoading.value = false; }
};

const onDrugPicked = (row) => {
  const drug = drugResults.value.find((d) => d.drugId === row.drugId);
  if (drug) {
    row.drugName = drug.drugName;
    row.specification = drug.specification || '';
    row.unit = drug.unit || '';
  }
};

const validateRx = () => {
  const errors = [];
  flushRxDraft();
  for (const p of rxLoaded) {
    if (!p.details?.length) continue;
    const label = RX_TYPE_LABEL[p.prescriptionType] || '西药';
    if (p.prescriptionType === 3) {
      if (!p.doseCount || p.doseCount < 1 || p.doseCount > 30) errors.push(`${label}处方：请填写剂数（1~30 剂）`);
      if (p.decoctFlag !== 1 && p.decoctFlag !== 2) errors.push(`${label}处方：请选择煎服方式（代煎/自煎）`);
    }
    p.details.forEach((item, idx) => {
      const prefix = `${label}处方第${idx + 1}项`;
      if (!item.drugId) errors.push(`${prefix}：请选择药品`);
      if (!item.singleDosage) errors.push(`${prefix}：请填写${p.prescriptionType === 3 ? '每剂克数' : '单次用量'}`);
      if (!item.frequency) errors.push(`${prefix}：请填写频次/用法`);
      if (!item.route) errors.push(`${prefix}：请填写给药途径/煎法`);
      if (!item.quantity || Number(item.quantity) <= 0) errors.push(`${prefix}：请填写正确的总量/剂数`);
      if (p.prescriptionType !== 3 && (!item.duration || Number(item.duration) <= 0)) errors.push(`${prefix}：请填写天数`);
    });
  }
  return errors;
};

const buildRxPayload = () => {
  flushRxDraft();
  return rxLoaded
      .filter((p) => p.details && p.details.length > 0)
      .map((p) => ({
        prescriptionType: p.prescriptionType || 1,
        doseCount: p.doseCount,
        decoctFlag: p.decoctFlag,
        // 长处方（处方级）：关时天数置空由后端落 NO；饮片不开长处方（天数撞剂数上限）
        isLongPrescription: p.isLongPrescription === 1,
        longPrescriptionDays: p.isLongPrescription === 1 ? p.longPrescriptionDays : null,
        details: p.details.map((d) => ({
          drugId: d.drugId, drugName: d.drugName, specification: d.specification, unit: d.unit,
          singleDosage: d.singleDosage, frequency: d.frequency, route: d.route,
          quantity: d.quantity, duration: d.duration, remark: d.remark,
        })),
      }));
};

// ---------- 保存（病历 + 处方）----------
const saving = ref(false);
const rxSignVisible = ref(false);
const pendingRxCount = ref(0);
let pendingSubmitData = null;

const saveAll = async () => {
  if (!current.value) return;
  rxErrors.value = validateRx();
  if (rxErrors.value.length) {
    ElMessage.warning('处方存在必填项缺失，请修正后再保存');
    return;
  }
  const data = {
    patientId: current.value.patientId,
    patientNo: current.value.patientNo,
    patientName: current.value.patientName,
    registId: current.value.registId,
    // queueId 病历/处方 DTO 强校验（VO 由 fillRegistIds 按 regist_id 关联补出），漏传结诊必崩
    queueId: current.value.queueId,
    deptId: current.value.deptId,
    deptName: current.value.deptName,
    doctorId: current.value.doctorId,
    doctorName: current.value.doctorName,
    recordId: recordForm.id || null,
    chiefComplaint: recordForm.chiefComplaint,
    presentIllness: recordForm.presentIllness,
    allergyHistory: recordForm.allergyHistory,
    pastHistory: recordForm.pastHistory,
    personalHistory: recordForm.personalHistory,
    familyHistory: recordForm.familyHistory,
    temperature: recordForm.temperature,
    pulse: recordForm.pulse,
    respiration: recordForm.respiration,
    systolicPressure: recordForm.systolicPressure,
    diastolicPressure: recordForm.diastolicPressure,
    // 结诊强校验 diagnosis（DTO 主字段）：本页诊断区只有名称/编码两个输入框，
    // 与门诊医生工作站的 ICD select 口径对齐——名称即诊断主值，别让 diagnosis 永远空串
    diagnosis: recordForm.diagnosis || recordForm.diagnosisName,
    diagnosisCode: recordForm.diagnosisCode,
    diagnosisName: recordForm.diagnosisName,
    treatmentPlan: recordForm.treatmentPlan,
    prescriptions: buildRxPayload(),
  };
  saving.value = true;
  try {
    if (data.prescriptions.length > 0) {
      // 有处方：先签名板（笔迹随处方落库，电子证书签名后端自动完成），再走结诊提交
      pendingSubmitData = data;
      pendingRxCount.value = data.prescriptions.length;
      rxSignVisible.value = true;
    } else {
      await saveMedicalRecord(data);
      ElMessage.success('病历已保存');
    }
  } catch (e) { /* 全局提示 */ } finally {
    saving.value = false;
  }
};

const onRxSigned = async (dataUrl) => {
  rxSignVisible.value = false;
  const data = pendingSubmitData;
  pendingSubmitData = null;
  if (!data) return;
  data.prescriptions = data.prescriptions.map((rx) => ({...rx, doctorSignature: dataUrl || ''}));
  saving.value = true;
  // 结诊带 silent：否决项拦截要弹框让医生选，不能让全局红条先弹一遍；其余失败提示归本函数
  try {
    let res;
    try {
      res = await submitMedicalRecord(data, {silent: true});
    } catch (e) {
      const action = await handleEmrSubmitVeto(e);
      if (action === 'cancel') return;
      if (action !== 'force') throw e;
      res = await submitMedicalRecord({...data, forceSubmit: true}, {silent: true});
    }
    if (res.data) recordForm.id = res.data;
    ElMessage.success('已保存并提交（处方已签名）');
    if (current.value?.registId) {
      const rxRes = await getPrescriptionList({patientId: current.value.patientId, registId: current.value.registId});
      rxLoaded.length = 0;
      rxLoaded.push(...(rxRes.data || []));
      bindRxDraft();
    }
  } catch (e) {
    ElMessage.error(String(e?.message || '结诊失败'));
  } finally { saving.value = false; }
};

// ---------- 轮询 ----------
onMounted(() => {
  reloadQueue();
  pollTimer = setInterval(reloadQueue, 30000);
});
onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer);
});
</script>

<style scoped>
/* 表格内紧凑编辑 */
:deep(.el-table .el-select),
:deep(.el-table .el-input) {
  width: 100%;
}
</style>
