<template>
  <el-dialog v-model="visible" :title="dialogTitle" destroy-on-close width="60%">
    <div v-loading="loading" class="min-h-[120px]">
      <!-- 危急值：结构化详情 + 闭环动作 -->
      <template v-if="isCritical && criticalDetail">
        <div class="space-y-3 text-sm">
          <div class="flex flex-wrap items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-3">
            <span class="font-semibold text-red-700">{{ criticalDetail.itemName || '-' }}</span>
            <span class="text-slate-600">{{ criticalDetail.patientName || '-' }}
              {{ criticalDetail.genderText || '' }} {{
                criticalDetail.age != null ? criticalDetail.age + '岁' : ''
              }}</span>
            <el-tag
                :type="criticalDetail.status === 1 ? 'danger' : criticalDetail.status === 2 ? 'warning' : criticalDetail.status === 4 ? 'info' : 'success'"
                size="small">
              {{ criticalDetail.statusText || `未知(${criticalDetail.status})` }}
            </el-tag>
            <el-tag v-if="criticalDetail.overdue && criticalDetail.status !== 3 && criticalDetail.status !== 4"
                    effect="dark" size="small" type="danger">已超时
            </el-tag>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <p><strong>危急值号：</strong>{{ criticalDetail.criticalNo || '-' }}</p>
            <p><strong>结果：</strong>
              <span class="font-semibold text-red-600">{{ criticalDetail.resultValue || '-' }}</span>
              {{ criticalDetail.resultUnit || '' }}
              <span class="text-slate-400">（参考 {{ criticalDetail.referenceRange || '-' }}）</span>
            </p>
            <p><strong>异常类型：</strong>{{ criticalDetail.criticalTypeText || '-' }}</p>
            <p><strong>危急描述：</strong>{{ criticalDetail.criticalDesc || '-' }}</p>
            <p><strong>报告科室：</strong>{{ criticalDetail.reportDeptName || '-' }}</p>
            <p><strong>报告人：</strong>{{ criticalDetail.reportBy || '-' }}（{{ fmtTime(criticalDetail.reportTime) }}）
            </p>
            <p><strong>处置时限：</strong>{{ fmtTime(criticalDetail.deadlineTime) }}</p>
            <p v-if="criticalDetail.receiveBy"><strong>接收：</strong>{{
                criticalDetail.receiveBy
              }}（{{ fmtTime(criticalDetail.receiveTime) }}）</p>
            <p v-if="criticalDetail.handleBy"><strong>处置：</strong>{{
                criticalDetail.handleBy
              }}（{{ fmtTime(criticalDetail.handleTime) }}）</p>
          </div>

          <div v-if="criticalDetail.handleMeasure" class="border-t pt-3">
            <p class="mb-1 font-bold text-slate-700">处置措施：</p>
            <p class="whitespace-pre-wrap rounded bg-slate-50 p-2">{{ criticalDetail.handleMeasure }}</p>
          </div>

          <!-- 闭环动作：待接收 → 确认接收；已接收 → 填处置措施 -->
          <div v-if="canReceive" class="border-t pt-3">
            <el-button v-perm="'portal:messages:edit'" :loading="receiving" type="danger" @click="submitReceive">
              确认接收
            </el-button>
            <p class="mt-2 text-xs text-slate-400">确认接收后须在处置时限内填写处置措施完成闭环。</p>
          </div>
          <div v-else-if="canHandle" class="space-y-2 border-t pt-3">
            <el-input v-model="handleMeasure" :rows="3" maxlength="500" placeholder="请填写处置措施（用药/复查/通知家属等临床动作，必填）" show-word-limit
                      type="textarea"/>
            <el-button v-perm="'portal:messages:edit'" :loading="handling" type="primary" @click="submitHandle">
              提交处置
            </el-button>
          </div>
        </div>
      </template>

      <!-- 听班呼叫：呼叫单详情 + 到岗应答闭环 -->
      <template v-else-if="isDutyCall && dutyCallDetail">
        <div class="space-y-3 text-sm">
          <div class="flex flex-wrap items-center gap-2 rounded-lg border border-orange-200 bg-orange-50 p-3">
            <span class="font-semibold text-orange-700">{{ dutyCallDetail.callNo || '-' }}</span>
            <el-tag :type="dutyCallStatusTagType(dutyCallDetail.status)" size="small">
              {{ dutyCallDetail.statusText || '未知状态' }}
            </el-tag>
            <el-tag v-if="dutyCallDetail.targetLevelText" type="info" size="small">
              {{ dutyCallDetail.targetLevelText }}听班
            </el-tag>
            <el-tag v-if="dutyCallDetail.patientName" size="small" type="warning">
              患者：{{ dutyCallDetail.patientName }}
              <span v-if="dutyCallDetail.patientLocation">（{{ dutyCallDetail.patientLocation }}）</span>
            </el-tag>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <p><strong>值班：</strong>{{ dutyCallDetail.dutyDate || '-' }} {{ dutyCallDetail.shiftTypeText || '' }}</p>
            <p><strong>呼叫科室：</strong>{{ dutyCallDetail.deptName || '-' }}</p>
            <p><strong>呼叫时间：</strong>{{ fmtTime(dutyCallDetail.callTime) }}</p>
            <p><strong>到岗时间：</strong>{{ dutyCallDetail.arriveTime ? fmtTime(dutyCallDetail.arriveTime) : '未到岗' }}</p>
            <p v-if="dutyCallDetail.escalateTime" class="col-span-2 text-red-600">
              <strong>升级记录：</strong>{{ fmtTime(dutyCallDetail.escalateTime) }}
              （原 {{ dutyCallDetail.escalateFromLevelText || '-' }} 未到岗，已升级）
            </p>
          </div>
          <p><strong>呼叫事由：</strong>{{ dutyCallDetail.callReason || '-' }}</p>
          <p v-if="dutyCallDetail.targetPhone"><strong>值班电话：</strong>{{ dutyCallDetail.targetPhone }}</p>
        </div>
        <div v-if="dutyCallDetail.canArrive" class="space-y-2 border-t pt-3">
          <el-input v-model="arriveNote" :rows="2" maxlength="200" placeholder="到岗说明（选填，如 已到床旁）" show-word-limit
                    type="textarea"/>
          <el-button v-perm="'org:dutyCall:arrive'" :loading="handling" type="primary" @click="submitArrive">
            到岗应答
          </el-button>
        </div>
        <div v-else-if="dutyCallDetail.status === 0" class="border-t pt-3 text-sm text-gray-500">
          当前登录人不是本次被叫值班人，无法应答到岗。请切换到被叫值班人的账号，或在「听班呼叫」页处理。
        </div>
      </template>

      <!-- 检查/检验报告：结构化 HTML -->
      <template v-else-if="htmlContent">
        <div v-html="htmlContent"></div>
      </template>

      <!-- 兜底：未接通处理链路的类型，展示消息正文 -->
      <template v-else-if="!loading && message">
        <p class="whitespace-pre-wrap text-sm text-slate-600">{{ message.content }}</p>
      </template>
    </div>
    <template #footer>
      <el-button v-if="isConsult" data-testid="msg-goto-consult" type="primary" @click="goConsult">
        前往会诊管理
      </el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {useRouter} from 'vue-router';
import {
  getCriticalValueDetail,
  getInspectionDetail,
  getLaboratoryDetail,
  handleCriticalValue,
  receiveCriticalValue
} from '@/api/medicaltech';
import {dutyCallArrive, getDutyCallDetail} from '@/api/dutyCall';
import {messageLabel} from '@/lib/messageCatalog';

const props = defineProps({
  modelValue: {type: Boolean, required: true},
  message: {type: null, required: true}
});
const emit = defineEmits();
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const bizType = computed(() => props.message?.bizType || '');
const isCritical = computed(() => bizType.value === 'critical');
// 听班呼叫：待办型消息必须在这里闭环「到岗应答」—— 只标已读等于没应答，
// 呼叫单还是「待到岗」，收件箱却看着像处理过了。
const isDutyCall = computed(() => bizType.value === 'duty-call' || bizType.value === 'duty-call-esc');
// 会诊邀请与会诊进展（接诊/完成）共用 bizType=consult，正文即全部信息，另给一个跳转到会诊工作台的入口
const isConsult = computed(() => bizType.value === 'consult');
const router = useRouter();
const goConsult = () => {
  visible.value = false;
  router.push({name: 'Consultation'});
};
const loading = ref(false);
// inspection / report 走结构化 HTML（与报告详情同口径）；critical 走 Vue 模板（带动作）
const htmlContent = ref('');
const criticalDetail = ref(null);
// 听班呼叫单详情（bizId → 呼叫单）
const dutyCallDetail = ref(null);
const arriveNote = ref('');
const dialogTitle = computed(() => {
  if (!props.message)
    return '消息详情';
  const label = messageLabel(bizType.value);
  const prefix = CATALOG_PREFIX[bizType.value] || '';
  return prefix ? `${prefix} · ${label}` : props.message.title || label;
});
const CATALOG_PREFIX = {
  inspection: '检查报告详情',
  report: '检验报告详情',
  critical: '危急值处置',
  'duty-call': '听班呼叫',
  'duty-call-esc': '听班呼叫超时升级',
};
const fmtTime = (v) => (v ? String(v).replace('T', ' ').slice(0, 16) : '—');
const loadDetail = async () => {
  if (!props.message)
    return;
  loading.value = true;
  htmlContent.value = '';
  criticalDetail.value = null;
  dutyCallDetail.value = null;
  try {
    const id = props.message.bizId;
    if (bizType.value === 'inspection') {
      htmlContent.value = await renderInspection(id);
    } else if (bizType.value === 'report') {
      htmlContent.value = await renderLaboratory(id);
    } else if (isCritical.value) {
      await loadCritical(id);
    } else if (isDutyCall.value) {
      await loadDutyCall(id);
    }
    // 其他类型：直接展示 message.content（模板里兜底）
  } catch (error) {
    console.error('加载消息详情失败', error);
    
  } finally {
    loading.value = false;
  }
};
const loadCritical = async (id) => {
  const res = await getCriticalValueDetail(id);
  if (!res.data) {
    ElMessage.warning('未找到该危急值记录（可能已被作废或删除）');
    return;
  }
  criticalDetail.value = res.data;
};
/* ---------- 听班呼叫：取呼叫单详情 + 到岗应答闭环 ---------- */
const loadDutyCall = async (id) => {
  const res = await getDutyCallDetail(id);
  if (!res.data) {
    ElMessage.warning('未找到该听班呼叫单（可能已被删除）');
    return;
  }
  dutyCallDetail.value = res.data;
};
/** 呼叫状态配色：待到岗橙、已到岗绿、已超时升级红、已取消灰 */
const dutyCallStatusTagType = (status) => {
  if (status === 1) return 'success';
  if (status === 2) return 'danger';
  if (status === 3) return 'info';
  return 'warning';
};
const submitArrive = async () => {
  if (!dutyCallDetail.value)
    return;
  handling.value = true;
  try {
    await dutyCallArrive({id: dutyCallDetail.value.id, arriveNote: arriveNote.value.trim()});
    ElMessage.success('已登记到岗，呼叫单闭环');
    await loadDutyCall(dutyCallDetail.value.id);
    arriveNote.value = '';
    emit('processed');
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
  } finally {
    handling.value = false;
  }
};
/* ---------- 检查/检验报告结构化渲染（自 MessagesView 收口至此） ---------- */
const renderInspection = async (recordId) => {
  const res = await getInspectionDetail(recordId);
  const record = res.data?.record || res.data;
  if (!record) {
    ElMessage.warning('未找到检查记录');
    return '';
  }
  return `
    <div class="space-y-3 text-sm">
      <div class="grid grid-cols-2 gap-2">
        <p><strong>患者姓名：</strong>${record.patientName || '-'}</p>
        <p><strong>检查项目：</strong>${record.inspectionItemName || '-'}</p>
        <p><strong>检查部位：</strong>${record.bodyPart || '-'}</p>
        <p><strong>检查目的：</strong>${record.inspectionPurpose || '-'}</p>
        <p><strong>临床诊断：</strong>${record.clinicalDiagnosis || '-'}</p>
      </div>
      ${record.resultDescription ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">检查所见：</p>
        <p class="text-sm bg-slate-50 p-2 rounded whitespace-pre-wrap">${record.resultDescription}</p>
      </div>` : ''}
      ${record.resultConclusion ? `
      <div class="border-t pt-3">
        <p class="font-bold text-emerald-700 mb-2">影像诊断/印象：</p>
        <p class="text-sm bg-emerald-50 p-2 rounded whitespace-pre-wrap">${record.resultConclusion}</p>
      </div>` : ''}
      ${record.suggestions ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">建议：</p>
        <p class="text-sm bg-blue-50 p-2 rounded whitespace-pre-wrap">${record.suggestions}</p>
      </div>` : ''}
    </div>`;
};
const renderLaboratory = async (recordId) => {
  const res = await getLaboratoryDetail(recordId);
  const detail = res.data;
  const record = detail?.record || {};
  const results = detail?.results || [];
  let resultsHtml = '';
  if (results.length > 0) {
    resultsHtml = `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">检验结果明细（共 ${results.length} 项）</p>
        <table class="w-full text-sm border-collapse">
          <thead>
            <tr class="bg-slate-100">
              <th class="border border-slate-300 px-3 py-2 text-left">项目名称</th>
              <th class="border border-slate-300 px-3 py-2 text-left">结果</th>
              <th class="border border-slate-300 px-3 py-2 text-left">单位</th>
              <th class="border border-slate-300 px-3 py-2 text-left">参考范围</th>
              <th class="border border-slate-300 px-3 py-2 text-left">状态</th>
            </tr>
          </thead>
          <tbody>
            ${results.map((r) => `
              <tr>
                <td class="border border-slate-300 px-3 py-2">${r.itemName || '-'}</td>
                <td class="border border-slate-300 px-3 py-2 ${r.abnormalFlag === 1 ? 'text-red-600 font-bold' : ''}">${r.resultValue || '-'}</td>
                <td class="border border-slate-300 px-3 py-2">${r.resultUnit || '-'}</td>
                <td class="border border-slate-300 px-3 py-2">${r.referenceRange || '-'}</td>
                <td class="border border-slate-300 px-3 py-2">${r.abnormalFlagText || '—'}</td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  }
  return `
    <div class="space-y-3 text-sm">
      <div class="grid grid-cols-2 gap-2">
        <p><strong>患者姓名：</strong>${record.patientName || '-'}</p>
        <p><strong>检验项目：</strong>${record.laboratoryItemName || '-'}</p>
      </div>
      ${resultsHtml}
    </div>`;
};
/* ---------- 危急值闭环动作 ---------- */
const receiving = ref(false);
const handleMeasure = ref('');
const handling = ref(false);
/** 1 待接收 → 可确认接收；2 已接收 → 可填处置；3 已处置 / 4 已作废 → 只读 */
const canReceive = computed(() => criticalDetail.value?.status === 1);
const canHandle = computed(() => criticalDetail.value?.status === 2);
const submitReceive = async () => {
  if (!criticalDetail.value)
    return;
  receiving.value = true;
  try {
    await receiveCriticalValue(criticalDetail.value.id);
    ElMessage.success('已确认接收，请及时填写处置措施');
    await loadCritical(criticalDetail.value.id);
    emit('processed');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    receiving.value = false;
  }
};
const submitHandle = async () => {
  if (!criticalDetail.value)
    return;
  if (!handleMeasure.value.trim()) {
    ElMessage.warning('请填写处置措施');
    return;
  }
  handling.value = true;
  try {
    await handleCriticalValue(criticalDetail.value.id, handleMeasure.value.trim());
    ElMessage.success('处置已记录，危急值闭环完成');
    await loadCritical(criticalDetail.value.id);
    emit('processed');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    handling.value = false;
  }
};
watch(visible, (v) => {
  if (v) {
    handleMeasure.value = '';
    arriveNote.value = '';
    loadDetail();
  }
});
</script>
