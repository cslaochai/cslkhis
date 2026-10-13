<template>
  <el-dialog v-model="visible" title="新建配血单" width="560px">
    <el-form label-width="100px">
      <el-form-item label="患者" required>
        <patient-select v-model="form.patientId" @select="onPatient"/>
      </el-form-item>
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="患者血型" required>
          <el-select v-model="form.patientBloodType" style="width: 100%">
            <el-option v-for="d in bloodTypeDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="患者 Rh">
          <el-radio-group v-model="form.patientRhType">
            <el-radio v-for="d in rhDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="血袋号" required>
        <el-input v-model="form.bagNo" placeholder="从库存台账复制在库血袋号"/>
      </el-form-item>
      <el-form-item label="用血申请单号">
        <el-input v-model="form.applyNo"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'medtech:bloodBank:add'" :loading="loading" type="primary" @click="saveCm">创建
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 新建配血单弹框（自 BloodBankView 拆出，只有新增语义）
 *
 * 血型 / Rh 字典由页面加载后传入（列表列也用同一份，见拆分规范第 6 条）；
 * 创建成功后 emit('saved')，由页面刷新配血列表与统计卡。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {crossmatchCreate} from '@/api/medicaltech';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 血型字典（页面已加载）
  bloodTypeDict: {type: Array, default: () => []},
  // Rh 字典（页面已加载）
  rhDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const form = reactive({
  applyNo: '', patientId: null, patientNo: '', patientName: '',
  patientBloodType: null, patientRhType: 1, bagNo: '',
});
const resetForm = () => {
  Object.assign(form, {
    applyNo: '',
    patientId: null,
    patientNo: '',
    patientName: '',
    patientBloodType: null,
    patientRhType: 1,
    bagNo: ''
  });
};

watch(visible, (open) => {
  if (!open) return;
  resetForm();
});

const onPatient = (p) => {
  form.patientId = p?.id;
  form.patientName = p?.name || p?.patientName || '';
  form.patientNo = p?.patientNo || '';
};

const saveCm = async () => {
  if (!form.patientId || !form.patientName.trim()) {
    ElMessage.warning('请选择患者');
    return;
  }
  if (form.patientBloodType == null) {
    ElMessage.warning('患者血型必填');
    return;
  }
  if (!form.bagNo.trim()) {
    ElMessage.warning('血袋号必填');
    return;
  }
  loading.value = true;
  try {
    const res = await crossmatchCreate({
      applyNo: form.applyNo.trim() || undefined,
      patientId: form.patientId, patientNo: form.patientNo || undefined,
      patientName: form.patientName.trim(),
      patientBloodType: form.patientBloodType, patientRhType: form.patientRhType,
      bagNo: form.bagNo.trim(),
    });
    if (res.code === 200) {
      ElMessage.success(`配血单 ${res.data?.matchNo || ''} 已创建`);
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '创建失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('创建失败');
  } finally {
    loading.value = false;
  }
};
</script>
