<template>
  <!-- 排台 -->
  <el-dialog v-model="visible" data-testid="p4-op-schedule-dialog" title="手术室排台" width="620px">
    <div v-if="scheduleTarget" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">
          {{ text(scheduleTarget.patientName) }}（{{ text(scheduleTarget.admissionNo) }}）
          <el-tag v-if="scheduleTarget.isEmergency === 1" class="ml-2" size="small" type="danger">急诊</el-tag>
        </div>
        <div class="mt-1 text-slate-700">
          {{ text(scheduleTarget.plannedOperationName) }} ·
          {{ text(scheduleTarget.operationLevelText) }}手术 ·
          {{ text(scheduleTarget.anesthesiaTypeText) }}
        </div>
        <div class="text-[12px] text-slate-500">术前诊断：{{ text(scheduleTarget.preopDiagnosis) }}</div>
      </div>
      <el-form label-width="100px">
        <el-form-item label="手术间" required>
          <!-- 禁 allow-create：手术间是主数据（sys_operation_room），自由输入会造出
               「【P4】1号手术间」「OR-3」这类对不上主数据的脏值，排台总表只能把它们
               全塞进「未登记手术间」兜底桶。历史脏值在本下拉里仍会原样显示（EP 找不到
               label 就显示 value），改期时顺手改成规范名称即可。 -->
          <el-select
              v-model="scheduleForm.operationRoom"
              class="!w-full"
              data-testid="p4-op-schedule-room"
              default-first-option
              filterable
              placeholder="选择手术间"
          >
            <el-option v-for="r in rooms" :key="r" :label="r" :value="r"/>
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始" required>
          <el-date-picker
              v-model="scheduleForm.plannedStartTime"
              class="!w-full"
              data-testid="p4-op-schedule-start"
              placeholder="选择开始时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="计划结束" required>
          <el-date-picker
              v-model="scheduleForm.plannedEndTime"
              class="!w-full"
              data-testid="p4-op-schedule-end"
              placeholder="选择结束时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="主刀医师" required>
          <el-select
              v-model="scheduleForm.surgeonId"
              class="!w-full"
              data-testid="p4-op-schedule-surgeon"
              filterable
              placeholder="选择主刀医师"
          >
            <el-option
                v-for="e in employees"
                :key="e.id"
                :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                :value="String(e.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="助手">
          <el-input v-model="scheduleForm.assistantName" placeholder="多人用逗号分隔，可空"/>
        </el-form-item>
        <el-form-item label="麻醉医师">
          <el-select
              v-model="scheduleForm.anesthetistId"
              class="!w-full"
              clearable
              filterable
              placeholder="可空（未指定）"
          >
            <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="巡回护士">
          <el-select
              v-model="scheduleForm.circulatingNurseId"
              class="!w-full"
              clearable
              filterable
              placeholder="可空（未指定）"
          >
            <el-option
                v-for="e in employees"
                :key="e.id"
                :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                :value="String(e.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="洗手护士">
          <el-select
              v-model="scheduleForm.scrubNurseId"
              class="!w-full"
              clearable
              filterable
              placeholder="可空（未指定）"
          >
            <el-option
                v-for="e in employees"
                :key="e.id"
                :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                :value="String(e.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排台备注">
          <el-input v-model="scheduleForm.scheduleRemark" :rows="2" placeholder="可空" type="textarea"/>
        </el-form-item>
      </el-form>
      <el-alert :closable="false" show-icon type="info">
        同一手术间、时间区间重叠的在途手术会被后端拒绝；同一主刀/麻醉医师在同一时段
        已排其他手术间也会被拒绝（不能分身两台）。端点相接不算冲突：上一台 10:00 结束、
        下一台 10:00 开始是允许的。
      </el-alert>
    </div>
    <template #footer>
      <el-button v-perm="'ipd:surgery:edit'" :loading="scheduleSubmitting" data-testid="p4-op-schedule-submit"
                 type="primary" @click="submitSchedule">
        确认排台
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {scheduleOperation} from '@/api/inpatientOperation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前要排台/改期的那一条手术申请
  row: {type: Object, default: null},
  rooms: {type: Array, default: () => []},
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const scheduleSubmitting = ref(false);
const scheduleTarget = computed(() => props.row);
const scheduleForm = reactive({
  operationRoom: '',
  plannedStartTime: '',
  plannedEndTime: '',
  surgeonId: '',
  assistantName: '',
  anesthetistId: '',
  circulatingNurseId: '',
  scrubNurseId: '',
  scheduleRemark: '',
});

watch(visible, (open) => {
  if (!open || !props.row) return;
  const row = props.row;
  scheduleForm.operationRoom = row.operationRoom || '';
  scheduleForm.plannedStartTime = row.plannedStartTime || '';
  scheduleForm.plannedEndTime = row.plannedEndTime || '';
  scheduleForm.surgeonId = row.surgeonId || '';
  scheduleForm.assistantName = row.assistantName || '';
  // 四个排台人力字段必须**全部**回填：漏一个就是「改期时静默把那个人抹掉」——
  // 后端 schedule() 是整组覆盖写（setXxx(dto.getXxx())），不传就等于清空。
  scheduleForm.anesthetistId = row.anesthetistId || '';
  scheduleForm.circulatingNurseId = row.circulatingNurseId || '';
  scheduleForm.scrubNurseId = row.scrubNurseId || '';
  scheduleForm.scheduleRemark = row.scheduleRemark || '';
});

const submitSchedule = async () => {
  if (!scheduleTarget.value)
    return;
  if (!scheduleForm.operationRoom) {
    ElMessage.warning('请选择或输入手术间');
    return;
  }
  if (!scheduleForm.plannedStartTime || !scheduleForm.plannedEndTime) {
    ElMessage.warning('请选择计划开始与结束时间');
    return;
  }
  if (!scheduleForm.surgeonId) {
    ElMessage.warning('请选择主刀医师');
    return;
  }
  scheduleSubmitting.value = true;
  try {
    await scheduleOperation({
      applyId: scheduleTarget.value.id,
      operationRoom: scheduleForm.operationRoom,
      plannedStartTime: scheduleForm.plannedStartTime,
      plannedEndTime: scheduleForm.plannedEndTime,
      surgeonId: scheduleForm.surgeonId,
      assistantName: scheduleForm.assistantName.trim() || undefined,
      anesthetistId: scheduleForm.anesthetistId || undefined,
      circulatingNurseId: scheduleForm.circulatingNurseId || undefined,
      scrubNurseId: scheduleForm.scrubNurseId || undefined,
      scheduleRemark: scheduleForm.scheduleRemark.trim() || undefined,
    });
    ElMessage.success('已排台');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    scheduleSubmitting.value = false;
  }
};
</script>
