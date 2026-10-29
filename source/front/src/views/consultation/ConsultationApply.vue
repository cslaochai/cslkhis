<template>
  <!-- 申请会诊 -->
  <el-dialog v-model="visible" destroy-on-close title="申请会诊" width="560px">
    <el-form label-width="96px">
      <el-form-item label="住院患者" required>
        <el-select v-if="!admission" v-model="pickedAdmissionId" :loading="admSearching" :remote-method="searchAdmissions"
                   class="w-full" data-testid="p4-apply-admission" filterable placeholder="输入姓名/住院号搜索在院患者"
                   remote>
          <el-option v-for="a in admissionOptions" :key="a.admissionId" :label="admissionLabel(a)"
                     :value="String(a.admissionId)"/>
        </el-select>
        <el-input v-else :model-value="admissionLabel(admission)" disabled/>
      </el-form-item>
      <el-form-item label="会诊科室" required>
        <el-select v-model="applyForm.toDeptId" class="w-full" data-testid="p4-apply-dept" filterable
                   placeholder="请选择会诊科室">
          <el-option v-for="d in depts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)"/>
        </el-select>
        <p class="mt-1 text-[11px] text-slate-400">
          申请科室 = {{ text(effectiveAdmission?.deptName) }}（由入院记录决定，不由前端填写）
        </p>
      </el-form-item>
      <el-form-item label="会诊范围" required>
        <el-radio-group v-model="applyForm.consultType" data-testid="p4-apply-type">
          <el-radio :value="1">科内会诊</el-radio>
          <el-radio :value="2">科间会诊</el-radio>
          <el-radio :value="3">全院会诊</el-radio>
        </el-radio-group>
        <p class="mt-1 text-[11px] text-slate-400">科内会诊必须与申请科室相同；科间 /
          全院必须请到别的科室（后端强制）</p>
      </el-form-item>
      <el-form-item label="急会诊">
        <el-switch v-model="applyForm.isUrgent" :active-value="1" :inactive-value="0" data-testid="p4-apply-urgent"/>
        <span class="ml-2 text-[11px] text-slate-400">急会诊时限 10 分钟，超时在列表标红（不阻断）</span>
      </el-form-item>
      <el-form-item label="会诊理由" required>
        <el-input
            v-model="applyForm.reason"
            :rows="3"
            data-testid="p4-apply-reason"
            maxlength="500"
            placeholder="请写明要解决的问题（会诊方据此准备）"
            show-word-limit
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="applyForm.remark" :rows="2" maxlength="500" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'ipd:consultation:add'" :loading="saveLoading" data-testid="p4-apply-submit" type="primary"
                 @click="submitApply">提交申请
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 申请会诊弹框：会诊必须挂在一次住院上。页面已选患者时直接带出，未选时弹框内搜索在院患者。
 * 申请科室由入院记录决定、不由前端填写（口径见列表页注释）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {saveConsultation} from '@/api/inpatientConsultation';
import {getInpatientListPage} from '@/api/inpatient';
import {admissionLabel, text} from '@/lib/consultation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 科室下拉候选（页面已加载，prop 传入避免重复请求）
  depts: {type: Array, default: () => []},
  // 选定的入院记录（展示患者/申请科室用）；为空时弹框自己提供在院患者搜索
  admission: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saveLoading = ref(false);
const emptyApplyForm = () => ({
  toDeptId: '',
  consultType: 2,
  isUrgent: 0,
  reason: '',
  remark: '',
});
const applyForm = ref(emptyApplyForm());
// 页面没预选患者时，弹框自己提供在院患者远程搜索（会诊必须挂在一次住院上）
const admissionOptions = ref([]);
const pickedAdmissionId = ref('');
const admSearching = ref(false);
const effectiveAdmission = computed(() => props.admission
    || admissionOptions.value.find((a) => String(a.admissionId) === String(pickedAdmissionId.value))
    || null);
const searchAdmissions = async (keyword) => {
  admSearching.value = true;
  try {
    const res = await getInpatientListPage({
      admitStatus: 1,
      ...(keyword ? {patientName: String(keyword).trim()} : {}),
      pageNum: 1,
      pageSize: 50,
    });
    admissionOptions.value = res.data?.records || [];
  } catch (error) {
    console.error('搜索在院患者失败:', error);
  } finally {
    admSearching.value = false;
  }
};
watch(visible, (open) => {
  if (!open) return;
  applyForm.value = emptyApplyForm();
  pickedAdmissionId.value = '';
  admissionOptions.value = [];
  if (!props.admission) searchAdmissions('');
});

const submitApply = async () => {
  if (!effectiveAdmission.value) {
    ElMessage.warning('请选择住院患者（会诊必须挂在一次住院上）');
    return;
  }
  if (!applyForm.value.toDeptId) {
    ElMessage.warning('请选择会诊科室');
    return;
  }
  if (!applyForm.value.reason || !String(applyForm.value.reason).trim()) {
    ElMessage.warning('请填写会诊理由（会诊方需要知道要解决什么问题）');
    return;
  }
  saveLoading.value = true;
  try {
    const res = await saveConsultation({
      ...applyForm.value,
      admissionId: String(effectiveAdmission.value.admissionId),
    });
    ElMessage.success(`会诊申请已提交：${res.data}`);
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saveLoading.value = false;
  }
};
</script>
