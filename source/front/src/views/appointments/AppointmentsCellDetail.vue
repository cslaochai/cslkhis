<template>
  <el-dialog v-model="visible" :title="cell
               ? `${cell.date} · ${cell.doctorName} 号源明细`
               : '医生号源明细'" destroy-on-close
             width="900px">
    <div v-if="cell" data-testid="desk-cell-detail">
      <el-table :data="schedules" border max-height="520" size="small">
        <el-table-column label="班次" width="110">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ deskShiftLabel(Number(row.scheduleType)) || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时段" width="180">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-600">{{ row.startTime }}-{{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="诊室" width="140">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ row.roomName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="余号 / 总数" width="140">
          <template #default="{ row }">
            <span :class="['text-sm font-medium', row.availableSource > 0 ? 'text-emerald-600' : 'text-slate-400']">
              {{ row.availableSource }}
            </span>
            <span class="text-xs text-slate-400"> / {{ row.totalSource }}</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="门诊诊查费" width="120">
          <template #default="{ row }">
            <span class="text-sm text-slate-700">¥{{ row.diagnosisFee || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="专家" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isExpert === 1" effect="plain" size="small" type="warning">专家</el-tag>
            <span v-else class="text-xs text-slate-400">普通</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-perm="'opd:appointments:add'" :disabled="row.availableSource <= 0" link size="small"
                       type="primary"
                       @click="goRegister(row)">
              去挂号
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 开关藏掉的班次要给总账：弹框里"只有 3 位医生"和"实际排了 5 位"必须说清 -->
      <p v-if="hiddenCount > 0"
         class="mt-2 text-xs text-amber-600">
        另有 {{ hiddenCount }} 个班次已满/停诊，
        当前被「隐藏已满/停诊」开关隐藏
      </p>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed} from 'vue';
import {deskShiftLabel} from '@/lib/appointments';

// 周视图格子的「医生号源明细」弹框（从 AppointmentsView 拆出）。
// 格子数据（某医生×某天的排班、被开关藏掉的班次）是看板事实，由主页面算好传进来；
// 「去挂号」要带着号源快照开新增挂号弹框，那也是主页面的事 —— 这里只管展示与回抛。
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 选中的格子：{date, doctorId, doctorName}；为空 = 关闭态（原来由 selectedCell 驱动） */
  cell: {type: Object, default: null},
  /** 该格应用「隐藏已满/停诊」后的可见排班 */
  schedules: {type: Array, default: () => []},
  /** 该格被开关藏掉的班次数（提示总账用） */
  hiddenCount: {type: Number, default: 0},
});
const emit = defineEmits(['update:modelValue', 'register']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
const goRegister = (row) => emit('register', row);
</script>
