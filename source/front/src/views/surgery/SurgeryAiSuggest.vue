<template>
  <el-dialog
      :model-value="modelValue"
      title="AI 智能排台建议"
      width="880px"
      append-to-body
      data-testid="p134-ai-dialog"
      @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="flex items-center gap-3 mb-3">
      <span class="text-sm text-slate-600">排台日期</span>
      <el-date-picker
          v-model="date"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          style="width: 160px;"
          data-testid="p134-ai-date"
      />
      <el-button type="primary" :loading="loading" data-testid="p134-ai-gen" @click="generate">
        {{ hasResult ? '重新生成' : '生成建议' }}
      </el-button>
      <el-button
          v-if="hasResult && suggestions.length"
          type="success"
          plain
          :disabled="applying"
          data-testid="p134-ai-accept-all"
          @click="acceptAll"
      >
        全部采纳
      </el-button>
      <span v-if="applying" class="text-xs text-slate-400">正在落库…</span>
    </div>

    <div v-if="hasResult" class="text-xs text-slate-500 mb-2">
      共 {{ suggestions.length }} 条建议、{{ blocked.length }} 条受阻；单间营业窗口 08:00~18:00（{{ result.capacityMinutesPerRoom }} 分钟），时长按手术级别预估。采纳后走既有排台校验落库。
    </div>

    <el-table
        v-if="hasResult"
        :data="suggestions"
        border
        stripe
        size="small"
        max-height="360"
        data-testid="p134-ai-table"
        style="width: 100%"
    >
      <el-table-column prop="applyNo" label="手术单号" width="120" />
      <el-table-column prop="patientName" label="患者" min-width="90" show-overflow-tooltip />
      <el-table-column prop="plannedOperationName" label="拟施术式" min-width="130" show-overflow-tooltip />
      <el-table-column label="级别" width="56" align="center">
        <template #default="{ row }">{{ row.operationLevel ? row.operationLevel + '级' : '—' }}</template>
      </el-table-column>
      <el-table-column label="时长" width="64" align="center">
        <template #default="{ row }">{{ row.durationMinutes }}min</template>
      </el-table-column>
      <el-table-column label="类型" width="64" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isEmergency === 1" type="danger" size="small">急诊</el-tag>
          <span v-else class="text-slate-400">常规</span>
        </template>
      </el-table-column>
      <el-table-column label="建议手术间 / 时段" min-width="180">
        <template #default="{ row }">
          <span class="font-medium">{{ row.suggestedRoomName }}</span>
          <span class="text-slate-500"> {{ hm(row.suggestedStart) }}~{{ hm(row.suggestedEnd) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="建议依据" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="76" align="center">
        <template #default="{ row }">
          <el-button
              v-perm="'ipd:surgery:edit'"
              link
              type="primary"
              size="small"
              :disabled="applying"
              data-testid="p134-ai-accept"
              @click="accept(row)"
          >
            采纳
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="blocked.length" class="mt-3">
      <div class="text-xs font-medium text-slate-500 mb-1">无法排入（{{ blocked.length }}）</div>
      <el-table :data="blocked" border size="small" max-height="180" style="width: 100%">
        <el-table-column prop="applyNo" label="手术单号" width="120" />
        <el-table-column prop="patientName" label="患者" min-width="90" />
        <el-table-column prop="plannedOperationName" label="拟施术式" min-width="140" show-overflow-tooltip />
        <el-table-column prop="reason" label="原因" min-width="300" show-overflow-tooltip />
      </el-table>
    </div>

    <template #footer>
      <span class="text-xs text-slate-400 mr-auto">采纳结果会直接写入排台总表，可在弹窗关闭后查看。</span>
      <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {aiSuggestOperationApply, scheduleOperation} from '@/api/inpatientOperation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  initialDate: {type: String, default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const today = () => {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
};
const date = ref(props.initialDate || today());
const result = ref(null);
const loading = ref(false);
const applying = ref(false);

const hasResult = computed(() => result.value !== null);
const suggestions = computed(() => result.value?.suggestions || []);
const blocked = computed(() => result.value?.blocked || []);
const hm = (t) => (t ? String(t).replace('T', ' ').slice(11, 16) : '—');

watch(() => props.modelValue, (v) => {
  if (v && !result.value)
    generate();
});

const generate = async () => {
  loading.value = true;
  try {
    const res = await aiSuggestOperationApply(date.value);
    result.value = res.data || {date: date.value, suggestions: [], blocked: []};
  } catch (error) /* 错误已由全局拦截器统一提示 */{
  } finally {
    loading.value = false;
  }
};

const accept = async (row) => {
  if (!row.surgeonId) {
    ElMessage.warning(`「${row.patientName}」主刀未定，无法直接采纳；请先在手术申请中指定主刀（排台核心是定人）`);
    return;
  }
  applying.value = true;
  try {
    await scheduleOperation({
      applyId: row.applyId,
      operationRoom: row.suggestedRoomName,
      plannedStartTime: String(row.suggestedStart).replace('T', ' '),
      plannedEndTime: String(row.suggestedEnd).replace('T', ' '),
      surgeonId: row.surgeonId,
      anesthetistId: row.anesthetistId,
      circulatingNurseId: row.circulatingNurseId,
      scrubNurseId: row.scrubNurseId,
      scheduleRemark: 'AI 排台采纳',
    });
    ElMessage.success(`已采纳：${row.patientName} → ${row.suggestedRoomName} ${hm(row.suggestedStart)}~${hm(row.suggestedEnd)}`);
    result.value.suggestions = result.value.suggestions.filter((s) => s.applyId !== row.applyId);
    emit('saved');
  } catch (error) /* 错误已由全局拦截器统一提示 */{
  } finally {
    applying.value = false;
  }
};

const acceptAll = async () => {
  const noSurgeon = suggestions.value.filter((s) => !s.surgeonId).length;
  if (noSurgeon > 0) {
    ElMessage.warning(`有 ${noSurgeon} 条未定主刀（无法自动采纳），已跳过；其余继续`);
  }
  applying.value = true;
  let ok = 0;
  try {
    for (const row of [...suggestions.value]) {
      if (!row.surgeonId)
        continue;
      try {
        await scheduleOperation({
          applyId: row.applyId,
          operationRoom: row.suggestedRoomName,
          plannedStartTime: String(row.suggestedStart).replace('T', ' '),
          plannedEndTime: String(row.suggestedEnd).replace('T', ' '),
          surgeonId: row.surgeonId,
          anesthetistId: row.anesthetistId,
          circulatingNurseId: row.circulatingNurseId,
          scrubNurseId: row.scrubNurseId,
          scheduleRemark: 'AI 排台采纳（批量）',
        });
        ok += 1;
        result.value.suggestions = result.value.suggestions.filter((s) => s.applyId !== row.applyId);
      } catch (error) /* 单条失败继续，全局拦截器已提示 */{
      }
    }
    if (ok > 0) {
      ElMessage.success(`已批量采纳 ${ok} 台`);
      emit('saved');
    }
  } finally {
    applying.value = false;
  }
};
</script>
