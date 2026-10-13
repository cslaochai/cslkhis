<template>
  <!-- 入座：点空闲座位打开，患者与输注内容在本组件内录入 -->
  <el-dialog v-model="visible" :title="`入座 ${form?.seatNo || ''}`" width="480px">
    <el-form label-width="100px">
      <el-form-item label="患者">
        <PatientSelect v-model="form.patientId" data-testid="ir-admit-patient" @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="输注内容">
        <el-input v-model="form.drugSummary" :rows="2" data-testid="ir-admit-drug" placeholder="药名/组数摘要"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="需要皮试">
        <el-switch v-model="form.needSkinTest" :active-value="1" :inactive-value="0"
                   data-testid="ir-admit-needtest"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'medtech:infusion:edit'" data-testid="ir-admit-save" type="primary" @click="saveAdmit">
        确认入座
      </el-button>
      <el-button data-testid="ir-admit-cancel" @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {admitInfusion} from '@/api/infusionRoom';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 要入座的空闲座位
  seat: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = ref(null);

watch(visible, (open) => {
  if (!open || !props.seat) return;
  const seat = props.seat;
  form.value = {
    seatId: seat.id, seatNo: seat.seatNo, patientId: null, patientName: '',
    treatmentRecordId: null, drugSummary: '', needSkinTest: 0, remark: ''
  };
});

const onPatientSelect = (p) => {
  if (form.value)
    form.value.patientName = p?.patientName || '';
};

const saveAdmit = async () => {
  const f = form.value;
  if (!f.patientId) {
    ElMessage.warning('请先选患者');
    return;
  }
  if (!f.drugSummary.trim()) {
    ElMessage.warning('请填写输注内容摘要');
    return;
  }
  const res = await admitInfusion(f);
  if (res?.code === 200) {
    ElMessage.success(`已入座 ${f.seatNo}（单号 ${res.data?.infusionNo}）`);
    visible.value = false;
    emit('saved');
  }
};
</script>
