<template>
  <!-- ---------------- 证书办理（优先 CA 在线申请，失败可转线下回填） ---------------- -->
  <el-dialog v-model="visible" :title="bindCertId ? '回填证书（CA 已签发）' : '办理签名证书'" width="680px" @closed="onClosed">
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
      <p v-if="!applyInfo?.online && caStatus?.appId" class="mb-3 pl-2 text-xs text-slate-500">
        接入 App ID：<span class="font-mono">{{ caStatus.appId }}</span>
        <span v-if="caStatus.baseUrl"> · CA 地址：<span class="font-mono">{{ caStatus.baseUrl }}</span></span>
        · 接入密钥：{{ caStatus.appSecretConfigured ? '已配置' : '未配置' }}
      </p>
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

    <!-- 第三步：线下回填 CA 签发的证书（兜底 / 或回填已有证书） -->
    <div v-show="step === 2">
      <el-form label-width="90px">
        <el-form-item v-if="displayDn" label="主体 DN">
          <el-input :model-value="displayDn" data-testid="p5-sign-bind-dn" readonly/>
        </el-form-item>
        <el-form-item v-if="displayPubKey" label="本地公钥">
          <el-input :model-value="displayPubKey" :rows="5" data-testid="p5-sign-bind-pubkey" readonly type="textarea"/>
        </el-form-item>
      </el-form>
      <!-- 线下送 CA 一律走「上传 CA 导出的证书文件」：证书真值（序列号/签发者/主体 DN/
           生效与失效时间）全部由后端从文件里解析，不接受手工粘贴，避免抄错或抄入伪值。
           CA 侧产出形态不统一：多数是含证书链的 .pem，也可能是带密码的 .zip —— 后端按内容嗅探。 -->
      <el-form label-width="90px">
        <el-form-item label="证书文件" required>
          <el-upload
              :auto-upload="false"
              :limit="1"
              accept=".pem,.crt,.cer,.der,.zip"
              :on-change="onZipChange"
              :on-remove="onZipRemove"
              :file-list="zipFileList"
              data-testid="p5-sign-bind-zip">
            <el-button plain type="primary">选择证书文件</el-button>
            <template #tip>
              <span class="text-xs text-slate-400">CA 系统「下载证书」得到的 .pem（可含整条链），或其导出的带密码 .zip</span>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="isZipFile" label="解压密码" required>
          <el-input v-model="bindForm.zipPassword" data-testid="p5-sign-bind-zippwd" placeholder="压缩包由 CA 提供时填写其展示的解压密码"/>
        </el-form-item>
        <p class="pl-2 text-xs text-slate-400">
          序列号、签发者、主体 DN 与生效/失效时间均从证书文件（notBefore / notAfter）解析，无需填写；
          回填后列表展示的就是 CA 实际签发的有效期。若文件里同时有证书链，会自动挑公钥与本证书一致的那张。
        </p>
      </el-form>
    </div>

    <template #footer>
      <el-button v-if="step === 0" :loading="submitting" data-testid="p5-sign-issue-submit" type="primary"
                 @click="submitKeygen">
        生成密钥对
      </el-button>
      <template v-if="step === 1">
        <el-button v-if="canRetry" :loading="submitting" type="primary" @click="retrySubmit">重新提交</el-button>
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
import {bindSignCertFile, getSignCaStatus, getSignCertDetail, keygenSignCert, submitSignCaApply,} from '@/api/signature';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 回填模式：传入已存在且「待 CA 签发」的证书 id，跳过密钥生成，直接走线下回填
  bindCertId: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const empOptions = ref([]);
const empLoading = ref(false);
const step = ref(0);
const applyInfo = ref(null);
const caStatus = ref(null);
// 回填模式：加载已有证书的公钥 / 主体 DN 供用户核对（确认 CA 签的是本证书的公钥）
const bindCertDetail = ref(null);
// 回填模式下「上传证书压缩包」的文件列表（仅展示用，真实文件存在 bindForm.zipFile）
const zipFileList = ref([]);
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
  zipFile: null,
  zipPassword: ''
});

let pollTimer = null;

const canRetry = computed(() => ['FAILED', 'REJECTED', 'CANCELLED'].includes(applyInfo.value?.applyStatus));

// 回填模式下展示的「主体 DN / 本地公钥」：优先取密钥生成流程的临时申请单，否则取已有证书详情
const displayDn = computed(() => applyInfo.value?.subjectDn || bindCertDetail.value?.caSubjectDn || '');
const displayPubKey = computed(() => applyInfo.value?.publicKeyPem || bindCertDetail.value?.publicKey || '');

// 加载已有证书详情（回填模式下核对用）
const loadBindCertDetail = async (certId) => {
  try {
    const res = await getSignCertDetail(certId);
    bindCertDetail.value = res.data || null;
  } catch (e) {
    console.error('加载证书详情失败', e);
  }
};

// 选择压缩包：el-upload 非自动上传，这里只把原生 File 暂存到 bindForm.zipFile
const onZipChange = (file) => {
  bindForm.zipFile = file.raw || null;
  if (!bindForm.zipFile) {
    ElMessage.warning('未读取到文件，请重新选择');
  }
};

const onZipRemove = () => {
  bindForm.zipFile = null;
  bindForm.zipPassword = '';
};

// 只有 .zip 才需要解压密码（CA 侧多数直接给 .pem，无密码）
const isZipFile = computed(() => {
  const n = (bindForm.zipFile?.name || '').toLowerCase();
  return n.endsWith('.zip');
});

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
      // HIS 已撤销 CA 查询权限：状态只能由 CA 推送，这里只读本地证书记录，
      // 收到推送后本地的 certStatus / 申请状态才会变
      const res = await getSignCertDetail(applyInfo.value.certId);
      const d = res.data || {};
      applyInfo.value = {
        ...(applyInfo.value || {}),
        certStatus: d.certStatus,
        certStatusText: d.certStatusText,
        applyNo: d.caApplyNo,
        applyStatus: d.caApplyStatus,
        applyStatusText: d.caApplyStatusText,
        applyMessage: d.caLastMessage
      };
      if (finishIfActive()) return;
      if (['FAILED', 'REJECTED', 'CANCELLED', 'ISSUED'].includes(d.caApplyStatus)) stopPolling();
    } catch (e) {
      console.error('读取证书本地状态失败', e);
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
  bindForm.zipFile = null;
  bindForm.zipPassword = '';
  zipFileList.value = [];
  pinMasked.value = false;
  selectedEmp.value = null;
  bindCertDetail.value = null;
  empSearchSeq += 1;
  // 回填模式：传入了已有证书 id → 跳过密钥生成，直接进回填步骤并加载公钥/DN 供核对
  if (props.bindCertId) {
    step.value = 2;
    bindForm.certId = props.bindCertId;
    loadBindCertDetail(props.bindCertId);
    return;
  }
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

const submitBind = async () => {
  // 线下送 CA 只认「CA 导出的证书文件」：序列号/签发者/主体 DN/生效与失效时间
  // 全部由后端从文件里解析，不接受手工粘贴，避免抄错或抄入伪值。
  if (!bindForm.zipFile) {
    ElMessage.warning('请选择 CA 系统下载的证书文件(.pem/.crt/.cer/.der/.zip)');
    return;
  }
  if (isZipFile.value && !bindForm.zipPassword) {
    ElMessage.warning('上传的是压缩包，请输入解压密码');
    return;
  }
  submitting.value = true;
  try {
    const fd = new FormData();
    fd.append('certId', String(bindForm.certId));
    if (isZipFile.value) fd.append('password', bindForm.zipPassword);
    fd.append('file', bindForm.zipFile);
    const res = await bindSignCertFile(fd);
    // 有效期由证书文件的 notBefore/notAfter 解析，回填即覆盖申请时的预期值 —— 回显出来供核对
    const d = res.data || {};
    ElMessage.success(`证书已回填生效 ${d.certNo || ''}，有效期 ${d.validFrom || '—'} ~ ${d.validTo || '—'}`);
    emit('saved');
    visible.value = false;
  } catch (e) {
    console.error('上传压缩包回填证书失败', e);
  } finally {
    submitting.value = false;
  }
};
</script>
