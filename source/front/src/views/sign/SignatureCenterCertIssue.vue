<template>
  <!-- ---------------- 证书办理（CA 线下人工签发） ---------------- -->
  <el-dialog v-model="visible" title="办理签名证书" width="640px">
    <el-steps :active="step" align-center finish-status="success" class="mb-4">
      <el-step title="生成密钥对"/>
      <el-step title="线下送 CA 签发"/>
      <el-step title="回填证书"/>
    </el-steps>

    <!-- 第一步：生成密钥对 -->
    <el-form v-show="step === 0" label-width="90px" autocomplete="off">
      <el-form-item label="员工" required>
        <el-select
            v-model="form.empId"
            :loading="empLoading"
            :remote-method="searchEmp"
            autocomplete="off"
            data-testid="p5-sign-issue-emp"
            filterable
            name="empKeyword"
            placeholder="按姓名搜索员工"
            remote
            reserve-keyword
            style="width: 100%"
            @change="onEmpChange"
            @focus="e => e.target.removeAttribute('readonly')"
        >
          <el-option v-for="e in empOptions" :key="e.id" :label="`${e.empName}（${e.deptName || '未分配科室'}）`"
                     :value="e.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="签名PIN" required>
        <el-input v-model="form.pin" autocomplete="new-password" data-testid="p5-sign-issue-pin" name="certPin" placeholder="4-32 位，签名时由本人输入" show-password/>
        <div class="text-xs text-slate-500 mt-1">PIN 是软签的二次确认口令：私钥本地加密托管，签名时须本人输入 PIN 才能解密。只存哈希，明文不入库。</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" data-testid="p5-sign-issue-remark" placeholder="可空"/>
      </el-form-item>
    </el-form>

    <!-- 第二步：CA 签发申请单（线下送 CA） -->
    <div v-show="step === 1">
      <el-alert class="mb-3" :closable="false" show-icon type="info"
                title="把下方「主体 DN」与「公钥 PEM」线下送 CA 认证中心人工签发；CA 返回证书 PEM 后进入下一步回填。"/>
      <el-form label-width="90px">
        <el-form-item label="证书编号">
          <el-input :model-value="applyInfo?.certNo || ''" readonly/>
        </el-form-item>
        <el-form-item label="主体 DN">
          <el-input :model-value="applyInfo?.subjectDn || ''" data-testid="p5-sign-apply-dn" readonly/>
        </el-form-item>
        <el-form-item label="公钥 PEM">
          <el-input :model-value="applyInfo?.publicKeyPem || ''" data-testid="p5-sign-apply-pubkey"
                    :rows="6" readonly type="textarea"/>
        </el-form-item>
        <el-form-item label="公钥指纹">
          <el-input :model-value="applyInfo?.keyFingerprintGroups || ''" readonly/>
        </el-form-item>
      </el-form>
    </div>

    <!-- 第三步：回填 CA 签发的证书 -->
    <el-form v-show="step === 2" label-width="90px">
      <el-form-item label="证书 PEM" required>
        <el-input v-model="bindForm.certPem" data-testid="p5-sign-bind-pem" :rows="8" type="textarea"
                  placeholder="粘贴 CA 签发返回的证书 PEM（含 -----BEGIN CERTIFICATE----- 段）"/>
      </el-form-item>
      <p class="pl-2 text-xs text-slate-400">序列号、签发者、主体 DN、有效期均从证书文件解析，无需填写。</p>
    </el-form>

    <template #footer>
      <el-button v-if="step === 1" data-testid="p5-sign-apply-next" type="primary" @click="step = 2">
        CA 已签发，去回填
      </el-button>
      <el-button v-if="step === 0" :loading="submitting" data-testid="p5-sign-issue-submit" type="primary" @click="submitKeygen">
        生成密钥对
      </el-button>
      <el-button v-if="step === 2" :loading="submitting" data-testid="p5-sign-bind-submit" type="primary" @click="submitBind">
        回填证书
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, nextTick, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {bindSignCert, keygenSignCert} from '@/api/signature';
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
const empOptions = ref([]);
const empLoading = ref(false);
const step = ref(0);
const applyInfo = ref(null);
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

const searchEmp = async (keyword) => {
  empLoading.value = true;
  try {
    const res = await getEmployeeList({keyword: keyword || undefined});
    empOptions.value = res.data || [];
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    empLoading.value = false;
  }
};

// 打开即重置表单并拉一次员工候选（与原先 openIssue 的口径一致）
// 360 登录管家等密码管家无视 autocomplete：开框时把员工搜索框置只读（自动填充不碰只读框），聚焦才解锁
const unlockAutofill = (e) => {
  if (e.target.matches?.('input[name="empKeyword"]')) e.target.removeAttribute('readonly');
};
document.addEventListener('focusin', unlockAutofill, true);
watch(visible, (open) => {
  if (!open) return;
  nextTick(() => {
    const el = document.querySelector('input[name="empKeyword"]');
    if (el) el.readOnly = true;
  });
  step.value = 0;
  applyInfo.value = null;
  form.empId = undefined;
  form.empName = '';
  form.deptId = undefined;
  form.deptName = '';
  form.pin = '';
  form.remark = '';
  bindForm.certId = undefined;
  bindForm.certPem = '';
  searchEmp('');
});

const onEmpChange = (empId) => {
  const emp = empOptions.value.find((e) => String(e.id) === String(empId));
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
    applyInfo.value = res.data || {};
    bindForm.certId = applyInfo.value?.certId;
    step.value = 1;
    ElMessage.success('密钥对已生成，请把下方申请单线下送 CA 签发');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitting.value = false;
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
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitting.value = false;
  }
};
</script>
