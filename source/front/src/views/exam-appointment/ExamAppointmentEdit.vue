<template>
  <el-dialog v-model="visible"
             :title="mode === 'book' ? '占号预约' : `改约：${target?.apptNo || ''}`"
             data-testid="exam-book-dialog" width="860px">
    <div class="mb-3 text-sm bg-gray-50 rounded p-3 leading-6">
      <span class="font-medium">{{ target?.patientName }}</span>
      <span class="text-gray-400"> / {{ target?.patientNo }}</span>
      　项目：<span class="font-medium">{{ target?.itemName }}</span>
      <span class="text-gray-400">{{ target?.bodyPart ? '（' + target.bodyPart + '）' : '' }}</span>
      　时长：{{ target?.examMinutes }}′
      <el-tag v-if="Number(target?.isEmergency) === 1" class="ml-1" size="small" type="danger">急诊</el-tag>
      <div class="text-xs text-gray-400 mt-1">
        冲突由服务端按「设备行 → 当日格子 → 患者当日预约」的固定加锁顺序判定，一次只暴露一个原因并点名占用者。
      </div>
    </div>
    <div class="flex flex-wrap items-center gap-2 mb-3">
      <el-select v-model="dlg.form.deviceId" :fit-input-width="false" data-testid="book-device" filterable
                 placeholder="选择设备"
                 style="width: 260px" @change="loadBoard">
        <el-option v-for="d in dlg.devices" :key="d.id" :disabled="Number(d.status) !== 1" :label="`${d.deviceName}（${d.deviceCode}）`"
                   :value="d.id"/>
      </el-select>
      <el-date-picker v-model="dlg.form.examDate" data-testid="book-date" placeholder="检查日期" style="width: 150px"
                      type="date" value-format="YYYY-MM-DD" @change="loadBoard"/>
      <el-button :loading="dlg.recLoading" @click="runRecommend">推荐可用时段</el-button>
      <span class="text-xs text-gray-400">开始时段：{{ dlg.form.startTime || '未选择' }}</span>
    </div>

    <div v-if="dlg.rec?.options?.length" class="mb-3 border border-[#0E9488]/30 rounded p-2">
      <div class="text-xs text-gray-500 mb-1">{{ dlg.rec.message || '最近可用时段：' }}</div>
      <div class="flex flex-wrap gap-2">
        <el-button v-for="(o, i) in dlg.rec.options" :key="i" size="small" @click="useOption(o)">
          {{ o.examDate }} {{ o.startTime }}-{{ o.endTime }} {{ o.deviceName }}
        </el-button>
      </div>
    </div>

    <div v-loading="dlg.boardLoading" class="grid grid-cols-8 gap-2 min-h-[120px]" data-testid="book-grid">
      <div v-for="c in boardCells" :key="c.slotId" :class="cellClass(c)" @click="pickCell(c)">
        <div class="text-xs font-medium">{{ c.startTime }}</div>
        <div class="text-xs">{{ slotCellState(c).label }}</div>
      </div>
    </div>
    <div v-if="!boardCells.length" class="text-xs text-gray-400">选择设备与日期后加载号源格子。</div>

    <el-form class="mt-3" label-width="90px">
      <el-form-item v-if="mode === 'reschedule'" label="改约原因" required>
        <el-input v-model="dlg.form.reason" :rows="2" placeholder="如：患者禁食准备未达标，改到次日上午"
                  type="textarea"/>
      </el-form-item>
      <el-form-item v-else label="备注">
        <el-input v-model="dlg.form.remark" :rows="2" placeholder="如：需家属陪同、增强前确认肾功能"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="['medtech:examAppoint:add', 'medtech:examAppoint:edit']" :loading="dlg.submitting"
                 data-testid="book-submit" type="primary"
                 @click="submitAppt">
        {{ mode === 'book' ? '确认占号' : '确认改约' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {
  examAppointBook,
  examAppointRecommend,
  examAppointReschedule,
  examSlotGenerate,
  getExamDeviceSelectList,
  getExamSlotBoard,
} from '@/api/examAppointment';
import {DEVICE_STATUS, slotCellState} from '@/lib/examAppointment';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 'book' 占号预约 / 'reschedule' 改约（同一弹窗两种模式）
  mode: {type: String, default: 'book'},
  // ApplyVO 或 ApptVO（父级传入的当前行）
  target: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const today = (() => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
})();

// ---------------- 占号弹窗（预约 / 改约共用） ----------------
const dlg = reactive({
  devices: [],
  form: {deviceId: null, examDate: '', startTime: '', remark: '', reason: ''},
  board: null,
  boardLoading: false,
  submitting: false,
  recLoading: false,
  rec: null,
});
const loadBookDevices = async (itemId) => {
  try {
    const res = await getExamDeviceSelectList({itemId: itemId ?? undefined});
    if (res.code === 200) {
      dlg.devices = res.data || [];
      if (!dlg.devices.length)
        ElMessage.warning('该项目还没有配置可开展设备，请到「设备与号源」页配置');
    } else
      ElMessage.error(res.message || '加载设备失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载设备失败');
  }
};

watch(visible, (v) => {
  if (!v) return;
  if (props.mode === 'reschedule') openReschedule();
  else openBook();
});

const openBook = async () => {
  Object.assign(dlg.form, {deviceId: null, examDate: today, startTime: '', remark: '', reason: ''});
  dlg.board = null;
  dlg.rec = null;
  await loadBookDevices(props.target?.itemId);
  if (dlg.devices.length === 1) {
    dlg.form.deviceId = dlg.devices[0].id;
    await loadBoard();
  }
};
const openReschedule = async () => {
  const row = props.target;
  Object.assign(dlg.form, {
    deviceId: row.deviceId,
    examDate: row.examDate,
    startTime: row.startTime,
    remark: '',
    reason: ''
  });
  dlg.board = null;
  dlg.rec = null;
  await loadBookDevices(row.itemId);
  if (!dlg.devices.some((d) => String(d.id) === String(row.deviceId))) {
    dlg.devices = [{
      id: row.deviceId,
      deviceCode: row.deviceCode,
      deviceName: row.deviceName,
      status: DEVICE_STATUS.OPEN
    }, ...dlg.devices];
  }
  await loadBoard();
};
const boardCells = computed(() => dlg.board?.slots || []);
const loadBoard = async () => {
  if (!dlg.form.deviceId || !dlg.form.examDate) {
    dlg.board = null;
    return;
  }
  dlg.boardLoading = true;
  dlg.form.startTime = '';
  try {
    // 先补格子再看板：设备当天可能还没生成号源（ahead_days 内允许直接生成）
    await examSlotGenerate({deviceId: dlg.form.deviceId, startDate: dlg.form.examDate, days: 1});
    const res = await getExamSlotBoard({deviceId: dlg.form.deviceId, slotDate: dlg.form.examDate});
    if (res.code === 200)
      dlg.board = res.data;
    else
      ElMessage.error(res.message || '加载号源失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载号源失败');
  } finally {
    dlg.boardLoading = false;
  }
};
const pickCell = (cell) => {
  const st = slotCellState(cell);
  if (!st.clickable)
    return;
  dlg.form.startTime = cell.startTime;
};
const cellClass = (cell) => {
  const st = slotCellState(cell);
  if (st.key === 'free')
    return dlg.form.startTime === cell.startTime ? 'cell cell-free cell-on' : 'cell cell-free';
  if (st.key === 'full')
    return 'cell cell-full';
  if (st.key === 'locked')
    return 'cell cell-locked';
  return 'cell cell-past';
};
const runRecommend = async () => {
  if (!dlg.form.deviceId && !props.target?.itemId) {
    ElMessage.warning('请先选设备');
    return;
  }
  dlg.recLoading = true;
  try {
    // 改约场景同样传 applyId：预约单快照里带着它，服务端按申请单项目找可承接设备
    const res = await examAppointRecommend({
      applyId: props.target?.applyId,
      deviceId: dlg.form.deviceId || undefined,
      examDate: dlg.form.examDate || undefined,
    });
    if (res.code === 200) {
      dlg.rec = res.data;
      if (!res.data?.options?.length)
        ElMessage.info(res.data?.message || '近期没有可用时段');
    } else
      ElMessage.error(res.message || '推荐失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('推荐失败');
  } finally {
    dlg.recLoading.value = false;
  }
};
const useOption = async (opt) => {
  dlg.form.deviceId = opt.deviceId;
  dlg.form.examDate = opt.examDate;
  await loadBoard();
  dlg.form.startTime = opt.startTime;
};
const submitAppt = async () => {
  const f = dlg.form;
  if (!f.deviceId) {
    ElMessage.warning('请选择设备');
    return;
  }
  if (!f.examDate) {
    ElMessage.warning('请选择检查日期');
    return;
  }
  if (!f.startTime) {
    ElMessage.warning('请点击一个可用时段');
    return;
  }
  if (props.mode === 'reschedule' && !f.reason.trim()) {
    ElMessage.warning('改约原因必填');
    return;
  }
  dlg.submitting = true;
  try {
    const isBook = props.mode === 'book';
    const payload = isBook
        ? {
          applyId: props.target.applyId,
          deviceId: f.deviceId,
          examDate: f.examDate,
          startTime: f.startTime,
          remark: f.remark.trim() || undefined
        }
        : {
          apptId: props.target.id,
          deviceId: f.deviceId,
          examDate: f.examDate,
          startTime: f.startTime,
          reason: f.reason.trim()
        };
    const res = isBook ? await examAppointBook(payload) : await examAppointReschedule(payload);
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '预约失败');
  } catch (e) {
    // 冲突由服务端判定，message 里点名占用者，必须原样给用户看
    ElMessage.error(e?.response?.data?.message || e?.message || '预约失败');
  } finally {
    dlg.submitting = false;
  }
};
</script>

<style scoped>
.cell {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 6px 8px;
  cursor: default;
  background: #fff;
  line-height: 1.4;
}

.cell-free {
  cursor: pointer;
  border-color: #bbe3de;
  background: #f2fbfa;
}

.cell-free:hover {
  border-color: #0e9488;
}

.cell-on {
  border-color: #1269b5;
  background: #e8f1fa;
  box-shadow: 0 0 0 2px rgba(18, 105, 181, .18);
}

.cell-full {
  background: #fdf1f1;
  border-color: #f2c3c3;
  color: #b91c1c;
}

.cell-locked {
  background: #f3f4f6;
  border-color: #d1d5db;
  color: #6b7280;
}

.cell-past {
  background: #fafafa;
  border-color: #ececec;
  color: #9ca3af;
}
</style>
