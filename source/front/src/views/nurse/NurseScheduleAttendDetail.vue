<template>
  <!-- 某一天的出勤对照：这里有「补登工时」「确认缺勤」两个动作。
       为什么必须给人这两个按钮：全院没有考勤机对接，查不到打卡数据的日子只能靠登记。
       与其让护士长去别的菜单找入口，不如把登记动作放在她看到问题的那一行旁边。 -->
  <el-drawer v-model="visible" :title="`${date} 出勤对照（计划 vs 实际）`"
             data-testid="ns-attend-detail"
             size="70%">
    <el-table v-loading="loading" :data="rows" border size="small" style="width: 100%">
      <el-table-column fixed label="护士" prop="employeeName" width="110"/>
      <el-table-column label="班次" prop="shiftName" width="130"/>
      <el-table-column align="right" label="计划工时" width="90">
        <template #default="{ row }">{{ row.plannedMinutes ?? 0 }}</template>
      </el-table-column>
      <el-table-column align="right" label="实际工时" width="90">
        <template #default="{ row }">{{ row.actualMinutes ?? '—' }}</template>
      </el-table-column>
      <el-table-column label="打卡" width="130">
        <template #default="{ row }">
          <span v-if="row.checkIn">
            {{ String(row.checkIn).slice(11, 16) }} → {{
              row.checkOut ? String(row.checkOut).slice(11, 16) : '未签退'
            }}
          </span>
          <span v-else class="text-slate-400">无打卡</span>
        </template>
      </el-table-column>
      <el-table-column label="对照结论" min-width="190">
        <template #default="{ row }">
          <el-tag :type="diffTagType(row.diffType)" effect="plain" size="small">{{ row.diffReason }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="{ row }">
          <div class="flex items-center gap-2">
            <el-button v-perm="PERM_EDIT" data-testid="ns-attend-adjust" link size="small" type="primary"
                       @click="doAdjust(row)">补登工时
            </el-button>
            <el-button v-perm="PERM_EDIT" data-testid="ns-attend-absent" link size="small" type="danger"
                       @click="doMarkAbsent(row)">确认缺勤
            </el-button>
            <el-button v-if="row.attendId" v-perm="PERM_EDIT" link size="small" type="info"
                       @click="doRemoveAttend(row)">撤销
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <template v-if="!rows.length && !loading" #footer>
      <span class="text-xs text-slate-400">这一天该单元没有「上班」排班，没有可对照的行。</span>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 出勤对照抽屉（计划 vs 实际）：按 date/orgType/orgId 自拉数据，
 * 补登工时、确认缺勤、撤销登记三个动作都在本组件完成；
 * 每次动作后自刷新并 emit('changed')，让父页面重拉周汇总条。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  adjustAttendance,
  getAttendanceComparison,
  markAttendanceAbsent,
  removeAttendance,
} from '@/api/staffAttendance';
import {PERM_EDIT} from '@/lib/nurseSchedule';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 对照的日期（YYYY-MM-DD）
  date: {type: String, default: ''},
  // 组织口径与 id（病区 / 门诊科室由父页面换算后传入）
  orgType: {type: Number, default: null},
  orgId: {type: [String, Number], default: null},
});

const emit = defineEmits(['update:modelValue', 'changed']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const rows = ref([]);
const load = async () => {
  if (!props.orgId)
    return;
  loading.value = true;
  try {
    const res = await getAttendanceComparison({
      startDate: props.date, endDate: props.date, orgType: props.orgType, orgId: props.orgId, staffType: 2,
    });
    rows.value = res.data || [];
  } catch {
    rows.value = [];
  } finally {
    loading.value = false;
  }
};
watch(() => props.modelValue, (v) => {
  if (v)
    load();
});
const refresh = async () => {
  await load();
  emit('changed');
};
const doAdjust = async (row) => {
  let minutes;
  try {
    minutes = await ElMessageBox.prompt(`登记 ${row.employeeName} ${props.date}「${row.shiftName || '未排班次'}」的实际工时（分钟）。`
        + `计划 ${row.plannedMinutes ?? 0} 分钟。没有打卡数据时由你补登，补了就是确认过的事实。`, '补登工时', {
      inputPattern: /^\d{1,4}$/,
      inputErrorMessage: '请填 0~9999 的整数分钟'
    });
  } catch {
    return;
  }
  try {
    const res = await adjustAttendance({
      employeeId: row.employeeId, workDate: props.date, shiftId: row.shiftId,
      actualMinutes: Number(minutes.value), remark: '护理排班页补登',
    });
    ElMessage.success(res.message || '工时已登记');
    await refresh();
  } catch (e) {
    ElMessage.error(e.message || '登记失败');
  }
};
const doMarkAbsent = async (row) => {
  try {
    await ElMessageBox.confirm(`确认 ${row.employeeName} ${props.date}「${row.shiftName || '未排班次'}」缺勤？`
        + '系统只在人确认时才记缺勤 —— 查不到打卡记录的行一律算「未回填」，不会自动扣到个人头上。', '确认缺勤', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await markAttendanceAbsent({
      employeeId: row.employeeId, workDate: props.date, shiftId: row.shiftId, remark: '护理排班页确认',
    });
    ElMessage.success(res.message || '已确认为缺勤');
    await refresh();
  } catch (e) {
    ElMessage.error(e.message || '确认失败');
  }
};
const doRemoveAttend = async (row) => {
  if (!row.attendId)
    return;
  try {
    await ElMessageBox.confirm('撤销这条出勤登记？（登记错了就该彻底抹掉，好让这天能重新签一次）', '撤销登记', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await removeAttendance({id: row.attendId});
    ElMessage.success(res.message || '已撤销');
    await refresh();
  } catch (e) {
    ElMessage.error(e.message || '撤销失败');
  }
};
/** 差异类型 → 标签配色（结论与文案都由后端给，前端不做二次判断） */
const diffTagType = (t) => ({
  1: 'success',
  2: 'warning',
  3: 'warning',
  4: 'warning',
  5: 'info',
  6: 'danger',
  7: 'info',
  8: 'info'
}[Number(t)] || 'info');
</script>
