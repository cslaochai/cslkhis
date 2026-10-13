<template>
  <el-dialog v-model="visible" data-testid="g15-pacu-enter-dialog" title="入 PACU 登记" width="620px">
    <el-form label-width="120px">
      <el-form-item label="麻醉记录单" required>
        <el-select v-model="enterForm.recordId" class="!w-full" data-testid="g15-pacu-record" filterable
                   placeholder="选择已提交的麻醉记录单">
          <el-option
              v-for="r in submittedRecords"
              :key="r.id"
              :label="`${r.recordNo} · ${r.patientName} · ${r.anesthesiaTypeText}`"
              :value="String(r.id)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="复苏护士">
        <el-select v-model="enterForm.nurseId" class="!w-full" clearable filterable placeholder="可空">
          <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="enterForm.remark" :rows="2" placeholder="可空" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'ipd:anesthesia:add'" :loading="enterSubmitting" data-testid="g15-pacu-enter-submit"
                 type="primary" @click="submitPacuEnter">确认入室
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {enterPacu, getAnesthesiaRecordListPage} from '@/api/inpatientAnesthesia';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const enterSubmitting = ref(false);
const enterForm = reactive({recordId: '', nurseId: '', remark: ''});
const submittedRecords = ref([]);

watch(visible, async (open) => {
  if (!open) return;
  submittedRecords.value = [];
  try {
    const r1 = await getAnesthesiaRecordListPage({recordStatus: 1, pageNum: 1, pageSize: 200});
    const r2 = await getAnesthesiaRecordListPage({recordStatus: 2, pageNum: 1, pageSize: 200});
    submittedRecords.value = (r1.data?.records || []).concat((r2.data?.records || []));
  } catch (e) {
    console.error('加载已提交麻醉记录失败:', e);
  }
  enterForm.recordId = '';
  enterForm.nurseId = '';
  enterForm.remark = '';
});

const submitPacuEnter = async () => {
  if (!enterForm.recordId) {
    ElMessage.warning('请选择已提交的麻醉记录单');
    return;
  }
  enterSubmitting.value = true;
  try {
    const res = await enterPacu({
      recordId: enterForm.recordId,
      nurseId: enterForm.nurseId || undefined,
      remark: enterForm.remark.trim() || undefined,
    });
    ElMessage.success(`已登记入 PACU：${res.data || ''}`);
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '入 PACU 登记失败');
  } finally {
    enterSubmitting.value = false;
  }
};
</script>
