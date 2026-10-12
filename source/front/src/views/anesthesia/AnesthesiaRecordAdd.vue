<template>
  <el-dialog v-model="visible" data-testid="g15-create-dialog" title="开立麻醉记录单" width="680px">
    <el-form label-width="120px">
      <el-form-item label="手术申请单" required>
        <el-select v-model="createForm.applyId" class="!w-full" data-testid="g15-create-apply" filterable
                   placeholder="选择手术申请单">
          <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
        </el-select>
      </el-form-item>
      <div class="mb-3 rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
        <el-icon class="mr-1 align-middle">
          <Warning/>
        </el-icon>
        必须先有结论为「可施行麻醉」的<b>术前访视</b>才能开立；急诊手术允许先麻醉，
        但本单会一直标「待补访视」。访视结论明确为「暂缓 / 需会诊」的，连急诊也不能越过。
      </div>
      <el-form-item label="麻醉医师">
        <el-select v-model="createForm.anesthetistId" class="!w-full" clearable filterable
                   placeholder="默认取当前登录人">
          <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="麻醉方式">
        <el-select v-model="createForm.anesthesiaType" class="!w-full" clearable placeholder="不传则取申请单登记值">
          <el-option :value="1" label="全身麻醉"/>
          <el-option :value="2" label="椎管内麻醉"/>
          <el-option :value="3" label="神经阻滞麻醉"/>
          <el-option :value="4" label="局部麻醉"/>
          <el-option :value="5" label="其他"/>
        </el-select>
      </el-form-item>
      <el-form-item label="麻醉助手">
        <el-input v-model="createForm.assistantAnesthetistName" placeholder="多人用逗号分隔，可空"/>
      </el-form-item>
      <el-form-item label="入室时间">
        <el-date-picker v-model="createForm.enterRoomTime" class="!w-full" placeholder="不填则取当前时间"
                        type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="createForm.remark" :rows="2" placeholder="可空" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'ipd:anesthesia:add'" :loading="createSubmitting" data-testid="g15-create-submit"
                 type="primary" @click="submitCreate">开立
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Warning} from '@element-plus/icons-vue';
import {createAnesthesiaRecord} from '@/api/inpatientAnesthesia';
import {applyLabel} from '@/lib/anesthesia';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  applies: {type: Array, default: () => []},
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// ---- 开立 ----
const createSubmitting = ref(false);
const createForm = reactive({
  applyId: '',
  anesthetistId: '',
  anesthesiaType: undefined,
  assistantAnesthetistName: '',
  enterRoomTime: '',
  remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  createForm.applyId = '';
  createForm.anesthetistId = '';
  createForm.anesthesiaType = undefined;
  createForm.assistantAnesthetistName = '';
  createForm.enterRoomTime = '';
  createForm.remark = '';
});

const submitCreate = async () => {
  if (!createForm.applyId) {
    ElMessage.warning('请选择手术申请单');
    return;
  }
  createSubmitting.value = true;
  try {
    const res = await createAnesthesiaRecord({
      applyId: createForm.applyId,
      anesthetistId: createForm.anesthetistId || undefined,
      anesthesiaType: createForm.anesthesiaType,
      assistantAnesthetistName: createForm.assistantAnesthetistName.trim() || undefined,
      enterRoomTime: createForm.enterRoomTime || undefined,
      remark: createForm.remark.trim() || undefined,
    });
    ElMessage.success(`麻醉记录单已开立：${res.data || ''}（记录中）`);
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '开立麻醉记录单失败');
  } finally {
    createSubmitting.value = false;
  }
};
</script>
