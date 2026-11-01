<template>
  <!-- ---------------- 证书办理（优先 CA 在线申请，失败可转线下回填） ---------------- -->
  <el-dialog v-model="visible" title="办理签名证书" width="680px" @closed="onClosed">
    <el-steps :active="step" align-center class="mb-4" finish-status="success">
      <el-step title="生成密钥对"/>
      <el-step title="CA 签发"/>
      <el-step title="回填证书"/>
    </el-steps>

    <!-- 第一步：生成密钥对（在线模式会立即提交 CA 申请） -->
    <el-form v-show="step === 0" autocomplete="off" label-width="90px">
      <el-form-item label="员工" required>
        <el-select
            v-model="form.empId"
            :loading="empLoading"
            :remote-method="searchEmp"
            autocomplete="off"
            data-testid="p5-sign-issue-emp"
            filterable
            no-data-text="输入姓名搜索员工"
            placeholder="按姓名搜索员工"
            remote
            reserve-keyword
            style="width: 100%"
            @change="onEmpChange"
        >
          <el-option v-for="e in empOptions" :key="e.id" :label="`${e.empName}（${e.deptName || '未分配科室'}）`"
                     :value="e.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="签名PIN" required>
        <!-- 每次开框重新挂载：清掉密码框上残留的“明文显示”状态 -->
        <el-input :key="visible" v-model="form.pin" :show-password="pinMasked" autocomplete="off"
                  data-testid="p5-sign-issue-pin"
                  placeholder="4-32 位，签名时由本人输入" @focus="pinMasked = true"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" data-testid="p5-sign-issue-remark" placeholder="可空"/>
      </el-form-item>
    </el-form>

    <!-- 第二步：CA 申请单状态（在线轮询；未接入/失败时转线下） -->
    <div v-show="step === 1">
      <el-alert
          :closable="false"
          :title="caAlertTitle"
          :type="caAlertType"
          class="mb-3"
          show-icon>
        <template v-if="caAlertHint" #default>
          <span class="text-xs leading-relaxed">{{ caAlertHint }}</span>
        </template>
      </el-alert>
      <el-form label-width="90px">
        <el-form-item label="证书编号">
          <el-input :model-value="applyInfo?.certNo || ''" readonly/>
        </el-form-item>
        <el-form-item v-if="applyInfo?.online" label="申请状态">
          <el-tag :type="statusTagType(applyInfo?.applyStatus)" size="small">
            {{ applyInfo?.applyStatusText || applyInfo?.applyStatus || '—' }}
          </el-tag>
        </el-form-item>
        <el-form-item v-if="applyInfo?.online" label="状态说明">
          <span class="text-xs text-slate-600">{{ applyInfo?.applyMessage || '—' }}</span>
        </el-form-item>
        <el-form-item label="主体 DN">
          <el-input :model-value="applyInfo?.subjectDn || ''" data-testid="p5-sign-apply-dn" readonly/>
        </el-form-item>
        <el-form-item label="公钥 PEM">
          <el-input :model-value="applyInfo?.publicKeyPem || ''" :rows="5"
                    data-testid="p5-sign-apply-pubkey" readonly type="textarea"/>
        </el-form-item>
        <el-form-item label="公钥指纹">
          <el-input :model-value="applyInfo?.keyFingerprintGroups || ''" readonly/>
        </el-form-item>
      </el-form>
      <p class="pl-2 text-xs text-slate-400">{{ applyInfo?.guide || '' }}</p>
    </div>

    <!-- 第三步：线下回填 CA 签发的证书（兜底） -->
    <el-form v-show="step === 2" label-width="90px">
      <el-form-item label="证书 PEM" required>
        <el-input v-model="bindForm.certPem" :rows="8" data-testid="p5-sign-bind-pem" placeholder="粘贴 CA 签发返回的证书 PEM（含 -----BEGIN CERTIFICATE----- 段）"
                  type="textarea"/>
      </el-form-item>
      <p class="pl-2 text-xs text-slate-400">序列号、签发者、主体 DN、有效期均从证书文件解析，无需填写。</p>
    </el-form>

    <template #footer>
      <el-button v-if="step === 0" :loading="submitting" data-testid="p5-sign-issue-submit" type="primary"
                 @click="submitKeygen">
        生成密钥对
      </el-button>
      <template v-if="step === 1">
        <el-button v-if="canRetry" :loading="submitting" type="primary" @click="retrySubmit">重新提交</el-button>
        <el-button v-if="applyInfo?.online && applyInfo?.certStatus !== 1" :loading="refreshing" type="primary"
                   @click="refreshStatus">
          刷新状态
        </el-button>
        <el-button @click="step = 2">改为线下回填</el-button>
      </template>
      <el-button v-if="step === 2" :loading="submitting" data-testid="p5-sign-bind-submit" type="primary"
                 @click="submitBind">
        回填证书
      </el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, onBeforeUnmount, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {bindSignCert, getSignCaStatus, keygenSignCert, refreshSignCaApply, submitSignCaApply,} from '@/api/signature';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const refreshing = ref(false);
const empOptions = ref([]);
const empLoading = ref(false);
const step = ref(0);
const applyInfo = ref(null);
const caStatus = ref(null);
const form = reactive({
  empId: undefined,
  empName: '',
  deptId: undefined,
  deptName: '',
  pin: '',
  remark: ''
});
const bindForm = reactive({
  certId: undefined,
  certPem: ''
});

let pollTimer = null;

const canRetry = computed(() => ['FAILED', 'REJECTED', 'CANCELLED'].includes(applyInfo.value?.applyStatus));

// CA 通道真实状态：只认后端 caStatus 的 caMode（offline=未启用，online=已启用），
// 不再用「有没有提交过申请」冒充「是否接入」。凭证缺失时由 hint 逐条列出缺了哪些参数。
const caAlertTitle = computed(() => {
  if (applyInfo.value?.online) return `CA 申请单：${applyInfo.value?.applyNo || '待生成'}`;
  if (caStatus.value?.caMode === 'offline') return '未接入 CA 在线申请';
  if (caStatus.value?.caMode === 'online' && caStatus.value?.available === false)
    return 'CA 在线申请已启用，但接入凭证未配置';
  return 'CA 在线申请（已启用）';
});

const caAlertType = computed(() => {
  if (applyInfo.value?.certStatus === 1) return 'success';
  if (applyInfo.value?.online) return 'info';
  if (caStatus.value?.caMode === 'offline') return 'warning';
  if (caStatus.value?.caMode === 'online' && caStatus.value?.available === false) return 'warning';
  return 'info';
});

const caAlertHint = computed(() => {
  if (applyInfo.value?.online) return applyInfo.value?.guide || '';
  return caStatus.value?.hint || '';
});

const loadCaStatus = async () => {
  try {
    const res = await getSignCaStatus();
    caStatus.value = res.data || null;
  } catch (e) {
    console.error('加载 CA 通道状态失败', e);
  }
};

const statusTagType = (status) => {
  if (status === 'ISSUED' || status === 'APPROVED') return 'success';
  if (status === 'PENDING') return 'warning';
  if (status === 'FAILED' || status === 'REJECTED') return 'danger';
  return 'info';
};

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
};

const applyRefresh = (data) => {
  applyInfo.value = data || {};
  bindForm.certId = data?.certId;
};

const finishIfActive = () => {
  if (applyInfo.value?.certStatus === 1) {
    stopPolling();
    ElMessage.success(`证书已生效 ${applyInfo.value?.certNo || ''}`);
    emit('saved');
    visible.value = false;
    return true;
  }
  return false;
};

const startPolling = () => {
  stopPolling();
  if (!applyInfo.value?.online || applyInfo.value?.certStatus === 1) return;
  pollTimer = setInterval(async () => {
    if (!visible.value || step.value !== 1) {
      stopPolling();
      return;
    }
    try {
      const res = await refreshSignCaApply(applyInfo.value.certId);
      applyRefresh(res.data);
      if (finishIfActive()) return;
      if (['FAILED', 'REJECTED', 'CANCELLED'].includes(res.data?.applyStatus)) stopPolling();
    } catch (e) {
      console.error('刷新 CA 申请状态失败', e);
      stopPolling();
    }
  }, 5000);
};

const onClosed = () => {
  stopPolling();
};

// 员工远程搜索：selectList 只认 empName（传 keyword 会被忽略，等于把全院名册整表返回），
// 所以按姓名收口；并做输入防抖 —— 停止输入 400ms 才请求，清空/未输入一律不请求。
const EMP_SEARCH_DEBOUNCE = 400;
let empSearchTimer = null;
let empSearchSeq = 0;
const selectedEmp = ref(null);

// 已选员工要留在候选里：清空关键字时候选被清空，否则下拉会把 id 当标签显示出来
const keepSelected = (list) => {
  const chosen = selectedEmp.value;
  if (!chosen) return list;
  return [chosen, ...list.filter((e) => String(e.id) !== String(chosen.id))];
};

const resetEmpOptions = () => {
  empOptions.value = selectedEmp.value ? [selectedEmp.value] : [];
};

const fetchEmpOptions = async (keyword) => {
  const seq = ++empSearchSeq;
  try {
    const res = await getEmployeeList({empName: keyword});
    if (seq !== empSearchSeq) return;
    empOptions.value = keepSelected(res.data || []);
  } catch (e) {
    if (seq !== empSearchSeq) return;
    console.error('加载员工候选失败', e);
    resetEmpOptions();
  } finally {
    if (seq === empSearchSeq) empLoading.value = false;
  }
};

const searchEmp = (keyword) => {
  const kw = (keyword || '').trim();
  if (empSearchTimer) {
    clearTimeout(empSearchTimer);
    empSearchTimer = null;
  }
  if (!kw) {
    empSearchSeq += 1; // 丢弃在途请求的结果
    empLoading.value = false;
    resetEmpOptions();
    return;
  }
  empLoading.value = true;
  empSearchTimer = setTimeout(() => {
    empSearchTimer = null;
    fetchEmpOptions(kw);
  }, EMP_SEARCH_DEBOUNCE);
};

// PIN 输入框初始以 text 渲染、聚焦后才变成密码框：浏览器/360 登录管家会把「前面的文本框 + 密码框」
// 当成登录表单自动填充（员工搜索框被灌入账号、PIN 被灌入密码）。扫描时没有密码字段，就不会填。
const pinMasked = ref(false);

// 打开即重置表单；员工候选不预拉（未输入不请求）
watch(visible, (open) => {
  if (!open) {
    stopPolling();
    if (empSearchTimer) {
      clearTimeout(empSearchTimer);
      empSearchTimer = null;
    }
    // 关框即清空 PIN 并回到未聚焦形态，避免关闭动画期间以明文回显
    form.pin = '';
    pinMasked.value = false;
    return;
  }
  step.value = 0;
  applyInfo.value = null;
  caStatus.value = null;
  form.empId = undefined;
  form.empName = '';
  form.deptId = undefined;
  form.deptName = '';
  form.pin = '';
  form.remark = '';
  bindForm.certId = undefined;
  bindForm.certPem = '';
  pinMasked.value = false;
  selectedEmp.value = null;
  empSearchSeq += 1;
  loadCaStatus();
  empLoading.value = false;
  resetEmpOptions();
});

onBeforeUnmount(() => {
  if (empSearchTimer) {
    clearTimeout(empSearchTimer);
    empSearchTimer = null;
  }
});

const onEmpChange = (empId) => {
  const emp = empOptions.value.find((e) => String(e.id) === String(empId));
  if (emp) selectedEmp.value = emp;
  form.empName = emp?.empName || '';
  form.deptId = emp?.deptId || undefined;
  form.deptName = emp?.deptName || '';
};

const submitKeygen = async () => {
  if (!form.empId) {
    ElMessage.warning('请选择员工');
    return;
  }
  if (!form.pin || form.pin.length < 4) {
    ElMessage.warning('请设置 4-32 位签名 PIN');
    return;
  }
  submitting.value = true;
  try {
    const res = await keygenSignCert({
      empId: form.empId,
      empName: form.empName,
      deptId: form.deptId,
      deptName: form.deptName,
      pin: form.pin,
      remark: form.remark || undefined,
    });
    applyRefresh(res.data);
    step.value = 1;
    if (finishIfActive()) return;
    if (applyInfo.value?.online) {
      ElMessage.success('密钥对已生成，CA 申请已提交');
      startPolling();
    } else {
      ElMessage.success('密钥对已生成，可按线下流程送 CA 签发后回填');
    }
  } catch (e) {
    console.error('生成密钥对失败', e);
  } finally {
    submitting.value = false;
  }
};

const retrySubmit = async () => {
  submitting.value = true;
  try {
    const res = await submitSignCaApply(applyInfo.value.certId);
    applyRefresh(res.data);
    if (finishIfActive()) return;
    ElMessage.success('已重新提交 CA 申请');
    startPolling();
  } catch (e) {
    console.error('重新提交 CA 申请失败', e);
  } finally {
    submitting.value = false;
  }
};

const refreshStatus = async () => {
  refreshing.value = true;
  try {
    const res = await refreshSignCaApply(applyInfo.value.certId);
    applyRefresh(res.data);
    if (finishIfActive()) return;
    ElMessage.success('已刷新 CA 申请状态');
  } catch (e) {
    console.error('刷新 CA 申请状态失败', e);
  } finally {
    refreshing.value = false;
  }
};

const submitBind = async () => {
  if (!bindForm.certPem || !bindForm.certPem.includes('BEGIN CERTIFICATE')) {
    ElMessage.warning('请粘贴 CA 返回的完整证书 PEM');
    return;
  }
  submitting.value = true;
  try {
    const res = await bindSignCert({
      certId: bindForm.certId,
      certPem: bindForm.certPem,
    });
    ElMessage.success(`证书已回填生效 ${res.data?.certNo || ''}`);
    emit('saved');
    visible.value = false;
  } catch (e) {
    console.error('回填证书失败', e);
  } finally {
    submitting.value = false;
  }
};
</script>
