<template>
  <!-- ============ 排班弹框 ============ -->
  <el-dialog v-model="visible" data-testid="hd-schedule-dialog" title="透析排班" width="560px">
    <el-form label-width="90px">
      <el-form-item label="患者">
        <el-select v-model="scheduleForm.archiveId" :fit-input-width="false" class="w-full" data-testid="hd-schedule-archive"
                   filterable placeholder="选择在透患者">
          <el-option v-for="a in archiveOptions" :key="a.id" :label="`${a.patientName}（${a.patientNo}）${a.dialysisNo}`"
                     :value="a.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="scheduleForm.dialysisDate" :clearable="false" class="w-40"
                        data-testid="hd-schedule-date" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="时段">
        <el-select v-model="scheduleForm.timeSlot" :fit-input-width="false" class="w-32"
                   data-testid="hd-schedule-slot">
          <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="机位">
        <el-select v-model="scheduleForm.machineId" :fit-input-width="false" class="w-40" data-testid="hd-schedule-machine"
                   filterable>
          <el-option v-for="m in machineOptions" :key="m.id" :label="`${m.machineNo} ${m.roomName || ''}`"
                     :value="m.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="scheduleForm.remark" data-testid="hd-schedule-remark" placeholder="可空"/>
      </el-form-item>
      <div class="text-xs text-slate-500 pl-[90px]">
        排班会把该患者当前有效处方（干体重/时长/血流速/透析器/抗凝）整套快照进透析单；同一机位同一时段只能排一人。
      </div>
    </el-form>
    <template #footer>
      <el-button :loading="scheduling" data-testid="hd-schedule-ok" type="primary" @click="doSchedule">确认排班
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 排班弹框（只做新增：空格子点一下排一次班，改期走动作弹框，没有修改语义）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {listDialysisArchivePage, scheduleDialysisSession} from '@/api/dialysis';
import {ARCHIVE_STATUS} from '@/lib/dialysis';

const props = defineProps({
  modelValue: Boolean,
  // 页面级参照：时段字典与机位下拉由主页面加载一次后传入
  dicts: {type: Object, required: true},
  machineOptions: {type: Array, default: () => []},
  // 点格子带过来的初始值（日期/时段/机位）
  seed: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const scheduling = ref(false);
const archiveOptions = ref([]);
const scheduleForm = reactive({
  dialysisDate: '', timeSlot: null, machineId: null, archiveId: null, remark: '',
});
const loadArchiveOptions = async () => {
  try {
    const res = await listDialysisArchivePage({status: ARCHIVE_STATUS.ON, pageNum: 1, pageSize: 100});
    archiveOptions.value = res?.data?.records || [];
  } catch (e) {
    console.error(e);
  }
};
const doSchedule = async () => {
  if (!scheduleForm.archiveId || !scheduleForm.machineId || !scheduleForm.timeSlot || !scheduleForm.dialysisDate) {
    ElMessage.warning('请选择患者、日期、时段与机位');
    return;
  }
  scheduling.value = true;
  try {
    const res = await scheduleDialysisSession({
      archiveId: scheduleForm.archiveId,
      dialysisDate: scheduleForm.dialysisDate,
      timeSlot: scheduleForm.timeSlot,
      machineId: scheduleForm.machineId,
      remark: scheduleForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已排班');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '排班失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '排班失败');
  } finally {
    scheduling.value = false;
  }
};

watch(visible, (v) => {
  if (!v) return;
  // 每次打开都重拉在透档案（新建档案后要能立刻排到）
  loadArchiveOptions();
  scheduleForm.dialysisDate = props.seed.dialysisDate ?? '';
  scheduleForm.timeSlot = props.seed.timeSlot ?? null;
  scheduleForm.machineId = props.seed.machineId ?? null;
  scheduleForm.archiveId = null;
  scheduleForm.remark = '';
});
</script>
