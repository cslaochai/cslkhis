<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑设备档位' : '新增设备档位'" data-testid="exam-device-dialog"
             width="720px">
    <el-form label-width="110px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="设备编码" required>
          <el-input v-model="form.deviceCode" placeholder="如 EX-CT03"/>
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model="form.deviceName"/>
        </el-form-item>
        <el-form-item label="设备类别" required>
          <el-select v-model="form.deviceType" style="width: 100%">
            <el-option v-for="d in deviceTypeDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="所属科室">
          <el-select v-model="form.deptId" :fit-input-width="false" clearable filterable placeholder="可空"
                     style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="设备台账">
          <el-select v-model="form.equipmentId" :fit-input-width="false" clearable filterable
                     placeholder="可空（台账没建档也能先排号源）" style="width: 100%">
            <el-option v-for="e in equipOptions" :key="e.id" :label="`${e.equipmentName}（${e.equipmentCode}）`"
                       :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="机房">
          <el-input v-model="form.roomName"/>
        </el-form-item>
        <el-form-item label="上午开始" required>
          <el-time-picker v-model="form.amStart" format="HH:mm" style="width: 100%" value-format="HH:mm"/>
        </el-form-item>
        <el-form-item label="上午结束" required>
          <el-time-picker v-model="form.amEnd" format="HH:mm" style="width: 100%" value-format="HH:mm"/>
        </el-form-item>
        <el-form-item label="下午开始">
          <el-time-picker v-model="form.pmStart" format="HH:mm" style="width: 100%" value-format="HH:mm"/>
        </el-form-item>
        <el-form-item label="下午结束">
          <el-time-picker v-model="form.pmEnd" format="HH:mm" style="width: 100%" value-format="HH:mm"/>
        </el-form-item>
        <el-form-item label="号源粒度(分)">
          <el-input-number v-model="form.slotMinutes" :max="240" :min="5" :step="5" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="并行检查台数">
          <el-input-number v-model="form.parallelCount" :max="20" :min="1" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="开放提前天数">
          <el-input-number v-model="form.aheadDays" :max="90" :min="1" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="单次可占上限(分)">
          <el-input-number v-model="form.maxSlotMinutes" :max="1440" :min="5" :step="5" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="开放状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="d in deviceStatusDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark"/>
        </el-form-item>
      </div>
    </el-form>
    <div class="text-xs text-gray-400 px-2">
      改开放时间或粒度不会自动重排历史格子：号源一旦被占用就不允许被静默挪走，保存后请到号源台按新口径生成后续日期并做一次对账。
    </div>
    <template #footer>
      <el-button v-perm="['medtech:examAppoint:add', 'medtech:examAppoint:edit']" :loading="saving"
                 type="primary" @click="saveDevice">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {examDeviceUpsert, getExamEquipmentOptions} from '@/api/examAppointment';
import {DEVICE_STATUS} from '@/lib/examAppointment';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 编辑时传当前设备行；新增时传空
  row: {type: Object, default: null},
  // 字典与科室同时用于列表列展示，由父级加载一次后传入
  deviceTypeDict: {type: Array, default: () => []},
  deviceStatusDict: {type: Array, default: () => []},
  deptOptions: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
// 设备台账是表单专用下拉，本组件自行懒加载
const equipOptions = ref([]);
const form = reactive({
  id: null, deviceCode: '', deviceName: '', deviceType: 1, equipmentId: null,
  deptId: null, roomName: '', amStart: '08:00', amEnd: '12:00', pmStart: '14:30', pmEnd: '17:30',
  slotMinutes: 30, parallelCount: 1, aheadDays: 7, maxSlotMinutes: 240, status: DEVICE_STATUS.OPEN, remark: '',
});

watch(visible, (v) => {
  if (v) openDevice();
});

const openDevice = async () => {
  if (!equipOptions.value.length) {
    try {
      const res = await getExamEquipmentOptions();
      equipOptions.value = res?.data || [];
    } catch (e) {
      console.error(e);
    }
  }
  const row = props.row;
  Object.assign(form, {
    id: row?.id || null, deviceCode: row?.deviceCode || '', deviceName: row?.deviceName || '',
    deviceType: row?.deviceType || 1, equipmentId: row?.equipmentId || null, deptId: row?.deptId || null,
    roomName: row?.roomName || '', amStart: row?.amStart || '08:00', amEnd: row?.amEnd || '12:00',
    pmStart: row?.pmStart || '14:30', pmEnd: row?.pmEnd || '17:30',
    slotMinutes: row?.slotMinutes ?? 30, parallelCount: row?.parallelCount ?? 1,
    aheadDays: row?.aheadDays ?? 7, maxSlotMinutes: row?.maxSlotMinutes ?? 240,
    status: row?.status ?? DEVICE_STATUS.OPEN, remark: row?.remark || '',
  });
};

const saveDevice = async () => {
  if (!form.deviceCode.trim() || !form.deviceName.trim()) {
    ElMessage.warning('设备编码与名称必填');
    return;
  }
  if (!form.deviceType) {
    ElMessage.warning('请选择设备类别');
    return;
  }
  saving.value = true;
  try {
    const deptName = form.deptId ? (props.deptOptions.find((d) => String(d.id) === String(form.deptId))?.deptName || '') : '';
    const res = await examDeviceUpsert({
      id: form.id || undefined, deviceCode: form.deviceCode.trim(), deviceName: form.deviceName.trim(),
      deviceType: form.deviceType, equipmentId: form.equipmentId || undefined,
      deptId: form.deptId || undefined, deptName: deptName || undefined, roomName: form.roomName.trim() || undefined,
      amStart: form.amStart, amEnd: form.amEnd, pmStart: form.pmStart || undefined, pmEnd: form.pmEnd || undefined,
      slotMinutes: form.slotMinutes, parallelCount: form.parallelCount, aheadDays: form.aheadDays,
      maxSlotMinutes: form.maxSlotMinutes, status: form.status, remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage({type: res.data?.warning ? 'warning' : 'success', message: res.message || '已保存', duration: 6000});
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
