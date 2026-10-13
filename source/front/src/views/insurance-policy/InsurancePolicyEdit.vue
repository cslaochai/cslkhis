<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑医保政策' : '新增医保政策'" destroy-on-close width="520px">
    <el-form label-width="100px">
      <el-form-item label="政策名称" required>
        <el-input v-model="form.policyName" placeholder="如：城镇职工医保-在职"/>
      </el-form-item>
      <el-form-item label="医保类型" required>
        <el-input v-model="form.insuranceType" placeholder="如：在职职工 / 退休职工 / 老年居民"/>
      </el-form-item>
      <el-form-item label="结算方式" required>
        <el-select v-model="form.settlementType" class="w-full" placeholder="请选择">
          <el-option
              v-for="o in settlementTypeOptions"
              :key="o.dictValue"
              :label="o.dictLabel"
              :value="Number(o.dictValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="统筹比例" required>
        <el-input-number v-model="form.coverageRatio" :max="100" :min="0" :precision="2" :step="1" class="!w-full"/>
        <span class="ml-2 text-xs text-slate-400">%，如 85 表示报销 85%</span>
      </el-form-item>
      <el-form-item label="乙类自付">
        <el-input-number v-model="form.selfPayRatio" :max="100" :min="0" :precision="2" :step="1" class="!w-full"/>
        <span class="ml-2 text-xs text-slate-400">%，乙类药品个人先自付比例</span>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'finance:insurancePolicy:add'" :loading="formSubmitting" type="primary" @click="submitForm">
        保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 医保政策 新增/编辑弹窗（从 InsurancePolicyView 拆出，row 为空＝新增，有值＝编辑）。
 * 结算方式候选是页面级字典（列表列与筛选也在用），由父页传入。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {insurancePolicyUpsert} from '@/api/insurancePolicy';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 编辑时传入的行数据；为空表示新增 */
  row: {type: Object, default: null},
  settlementTypeOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formSubmitting = ref(false);
const form = reactive({
  id: null,
  policyName: '',
  insuranceType: '',
  settlementType: null,
  coverageRatio: 0,
  selfPayRatio: 0,
  status: 1,
  remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  // 打开时按父页传入的行复位表单（原 openCreate/openEdit 的赋值口径逐条照搬）
  if (props.row) {
    form.id = props.row.id;
    form.policyName = props.row.policyName;
    form.insuranceType = props.row.insuranceType;
    form.settlementType = props.row.settlementType;
    form.coverageRatio = Number(props.row.coverageRatio ?? 0);
    form.selfPayRatio = Number(props.row.selfPayRatio ?? 0);
    form.status = props.row.status;
    form.remark = props.row.remark || '';
  } else {
    form.id = null;
    form.policyName = '';
    form.insuranceType = '';
    form.settlementType = null;
    form.coverageRatio = 0;
    form.selfPayRatio = 0;
    form.status = 1;
    form.remark = '';
  }
});

async function submitForm() {
  if (!form.policyName.trim()) {
    ElMessage.warning('请填写政策名称');
    return;
  }
  if (!form.insuranceType.trim()) {
    ElMessage.warning('请填写医保类型');
    return;
  }
  if (form.settlementType === null) {
    ElMessage.warning('请选择结算方式');
    return;
  }
  const coverage = Number(form.coverageRatio);
  if (Number.isNaN(coverage) || coverage < 0 || coverage > 100) {
    ElMessage.warning('统筹比例需在 0 ~ 100 之间');
    return;
  }
  formSubmitting.value = true;
  try {
    await insurancePolicyUpsert({
      id: form.id,
      policyName: form.policyName.trim(),
      insuranceType: form.insuranceType.trim(),
      settlementType: form.settlementType,
      coverageRatio: coverage,
      selfPayRatio: Number(form.selfPayRatio),
      status: form.status,
      remark: form.remark,
    });
    ElMessage.success(form.id ? '修改成功' : '新增成功');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    formSubmitting.value = false;
  }
}
</script>
