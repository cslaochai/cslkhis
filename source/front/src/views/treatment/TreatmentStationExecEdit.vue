<template>
  <!-- 打卡确认 -->
  <el-dialog v-model="visible"
             :title="`打卡：${target?.patientName || ''} · ${target?.itemName || ''} 第 ${target?.execSeq || ''} 次`"
             width="560px">
    <div class="text-sm text-gray-600 mb-3">
      排期 {{ target?.planDate }} · 单价 {{ money(target?.price) }}，确认后按次计入本次就诊的治疗费单。
    </div>
    <el-form label-width="88px">
      <el-form-item label="患者反应">
        <el-radio-group v-model="form.recordStatus" data-testid="exec-record-status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">异常</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="治疗记录">
        <el-input v-model="form.result" :rows="2" data-testid="exec-result" maxlength="500"
                  placeholder="留空则记「第 N 次治疗完成，过程顺利」" type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" maxlength="200" placeholder="选填"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'opd:treatmentStation:edit'" :loading="saving" data-testid="exec-submit"
                 type="primary" @click="doExecute">确认打卡
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {treatmentExecExecute} from '@/api/treatment';
import {money} from '@/lib/treatment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 被打卡的那一行执行流水；「能不能打卡」的闸门在父页面（canExecute 由后端给），这里只管表单
  target: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({recordStatus: 1, result: '', remark: ''});

watch(visible, (open) => {
  if (open) Object.assign(form, {recordStatus: 1, result: '', remark: ''});
});

const doExecute = async () => {
  saving.value = true;
  try {
    const res = await treatmentExecExecute({
      recordId: props.target.recordId,
      recordStatus: form.recordStatus,
      result: form.result.trim() || undefined,
      remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已打卡');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    saving.value = false;
  }
};
</script>
