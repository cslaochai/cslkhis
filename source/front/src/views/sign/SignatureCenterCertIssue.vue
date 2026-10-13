<template>
  <!-- ---------------- 签发证书 ---------------- -->
  <el-dialog v-model="visible" title="人工签发签名证书" width="520px">
    <el-form label-width="90px">
      <el-form-item label="员工" required>
        <el-select
            v-model="issueForm.empId"
            :loading="empLoading"
            :remote-method="searchEmp"
            data-testid="p5-sign-issue-emp"
            filterable
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
      <el-form-item label="有效期">
        <el-input-number v-model="issueForm.validDays" :max="3650" :min="1" data-testid="p5-sign-issue-days"
                         placeholder="留空取系统配置" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="issueForm.remark" data-testid="p5-sign-issue-remark" placeholder="可空"/>
      </el-form-item>
    </el-form>
    <p class="text-xs leading-5 text-slate-500">
      同一员工已有有效证书时会被拒绝（需先吊销旧证书）。签发后私钥加密入库，接口只回公钥与指纹。
    </p>
    <template #footer>
      <el-button :loading="issuing" data-testid="p5-sign-issue-submit" type="primary" @click="submitIssue">签发
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {issueSignCert} from '@/api/signature';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const issuing = ref(false);
const empOptions = ref([]);
const empLoading = ref(false);
const issueForm = reactive({
  empId: undefined,
  empName: '',
  deptId: undefined,
  deptName: '',
  validDays: undefined,
  remark: ''
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
watch(visible, (open) => {
  if (!open) return;
  issueForm.empId = undefined;
  issueForm.empName = '';
  issueForm.deptId = undefined;
  issueForm.deptName = '';
  issueForm.validDays = undefined;
  issueForm.remark = '';
  searchEmp('');
});

const onEmpChange = (empId) => {
  const emp = empOptions.value.find((e) => String(e.id) === String(empId));
  issueForm.empName = emp?.empName || '';
  issueForm.deptId = emp?.deptId || undefined;
  issueForm.deptName = emp?.deptName || '';
};

const submitIssue = async () => {
  if (!issueForm.empId) {
    ElMessage.warning('请选择员工');
    return;
  }
  issuing.value = true;
  try {
    const res = await issueSignCert({
      empId: issueForm.empId,
      empName: issueForm.empName,
      deptId: issueForm.deptId,
      deptName: issueForm.deptName,
      validDays: issueForm.validDays || undefined,
      remark: issueForm.remark || undefined,
    });
    // 私钥永不回显 —— 签发接口只回证书 VO（公钥 + 指纹），前端也没有地方能拿到私钥
    ElMessage.success(`已签发证书 ${res.data?.certNo || ''}（指纹 ${res.data?.keyFingerprintGroups || ''}）`);
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    issuing.value = false;
  }
};
</script>
