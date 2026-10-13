<template>
  <!-- 事件登记 -->
  <el-dialog v-model="visible" data-testid="event-dialog" title="登记 VTE 事件" width="520px">
    <el-form label-width="110px">
      <el-form-item label="住院患者" required>
        <div data-testid="sel-admission" style="width: 100%">
          <el-select v-model="eventForm.admissionId" filterable placeholder="选择住院患者" style="width: 100%">
            <el-option
                v-for="p in inpatients"
                :key="p.admissionId"
                :label="`${p.patientName || '未命名'} · ${p.admissionNo || ''}`"
                :value="p.admissionId"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="事件类型" required>
        <div data-testid="sel-event-type" style="width: 100%">
          <el-select v-model="eventForm.eventType" placeholder="选择事件类型" style="width: 100%">
            <el-option :value="1" label="深静脉血栓（DVT）"/>
            <el-option :value="2" label="肺栓塞（PE）"/>
            <el-option :value="3" label="预防相关出血"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="发生时机" required>
        <div data-testid="sel-onset-type" style="width: 100%">
          <el-radio-group v-model="eventForm.onsetType">
            <el-radio :value="1">院内发生（计入发生率）</el-radio>
            <el-radio :value="2">入院时已存在（不计入）</el-radio>
          </el-radio-group>
        </div>
      </el-form-item>
      <el-form-item label="确诊日期" required>
        <div data-testid="sel-diagnose-date" style="width: 100%">
          <el-date-picker v-model="eventForm.diagnoseDate" placeholder="确诊日期" style="width: 100%"
                          type="date" value-format="YYYY-MM-DD"/>
        </div>
      </el-form-item>
      <el-form-item label="诊断依据">
        <el-select v-model="eventForm.diagnosisBasis" clearable placeholder="选择诊断依据" style="width: 100%">
          <el-option :value="1" label="超声"/>
          <el-option :value="2" label="CT 肺动脉造影"/>
          <el-option :value="3" label="静脉造影"/>
          <el-option :value="4" label="临床诊断"/>
          <el-option :value="5" label="其他"/>
        </el-select>
      </el-form-item>
      <el-form-item label="血栓部位">
        <el-input v-model="eventForm.thrombusSite" maxlength="100" placeholder="如：左下肢股静脉"/>
      </el-form-item>
      <el-form-item label="转归">
        <el-select v-model="eventForm.outcome" clearable placeholder="选择转归" style="width: 100%">
          <el-option :value="1" label="好转"/>
          <el-option :value="2" label="未愈"/>
          <el-option :value="3" label="死亡"/>
          <el-option :value="4" label="未知"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="eventForm.remark" :rows="2" maxlength="500" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="savingEvent" data-testid="btn-save-event" type="primary" @click="submitEvent">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 登记 VTE 事件（只有新增，无编辑）：表单态、在院患者候选（打开时懒加载一次）
 * 与提交都在本组件；保存成功后 emit('saved') 让父页面重拉事件列表。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getInpatientListPage} from '@/api/inpatient';
import {upsertVteEvent} from '@/api/vte';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const savingEvent = ref(false);
const inpatients = ref([]);
const eventForm = reactive({
  admissionId: null,
  eventType: 1,
  onsetType: 1,
  diagnoseDate: null,
  diagnosisBasis: null,
  thrombusSite: '',
  outcome: null,
  remark: '',
});

watch(() => props.modelValue, async (v) => {
  if (!v)
    return;
  eventForm.admissionId = null;
  eventForm.eventType = 1;
  eventForm.onsetType = 1;
  eventForm.diagnoseDate = new Date().toISOString().slice(0, 10);
  eventForm.diagnosisBasis = null;
  eventForm.thrombusSite = '';
  eventForm.outcome = null;
  eventForm.remark = '';
  if (!inpatients.value.length) {
    try {
      const res = await getInpatientListPage({admitStatus: null, pageNum: 1, pageSize: 100});
      inpatients.value = res?.data?.records || res?.data?.list || [];
    } catch (e) {
      inpatients.value = [];
    }
  }
});

async function submitEvent() {
  if (!eventForm.admissionId) {
    ElMessage.warning('请选择住院患者');
    return
  }
  if (!eventForm.diagnoseDate) {
    ElMessage.warning('请选择确诊日期');
    return
  }
  savingEvent.value = true
  try {
    await upsertVteEvent({...eventForm})
    ElMessage.success('事件已登记')
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    savingEvent.value = false
  }
}
</script>
