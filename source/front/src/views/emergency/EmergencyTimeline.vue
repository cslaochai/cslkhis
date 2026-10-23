<template>
  <div class="rounded-lg border border-slate-200 bg-slate-50 p-3" data-testid="emg-timeline">
    <div class="mb-2 text-xs font-bold text-slate-500">就诊时间轴</div>
    <div class="flex items-start">
      <div v-for="(node, idx) in nodes" :key="node.key" class="flex flex-1 items-start"
           :class="idx === nodes.length - 1 ? 'flex-none' : ''">
        <div class="flex flex-col items-center">
          <span class="flex h-6 w-6 items-center justify-center rounded-full text-xs font-bold"
                :class="node.done ? node.color : 'bg-slate-200 text-slate-400'">
            {{ idx + 1 }}
          </span>
          <div class="mt-1 text-center">
            <div class="text-xs font-medium text-slate-700">{{ node.label }}</div>
            <div class="text-[11px]" :class="node.done ? 'text-slate-500' : 'text-slate-300'">
              {{ node.time || '未记录' }}
            </div>
          </div>
        </div>
        <div v-if="idx !== nodes.length - 1" class="mt-3 h-0.5 flex-1"
             :class="node.done ? 'bg-emerald-300' : 'bg-slate-200'"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue';

const props = defineProps({
  row: {type: Object, required: true},
});

const fmt = (v) => {
  if (!v) return '';
  const s = String(v).replace('T', ' ');
  return s.slice(0, 16);
};

const nodes = computed(() => {
  const r = props.row || {};
  const ended = r.emergencyStatus != null && r.emergencyStatus >= 4;
  return [
    {key: 'register', label: '登记', time: fmt(r.createTime), done: !!r.createTime, color: 'bg-blue-500 text-white'},
    {key: 'admit', label: '入抢救室', time: fmt(r.admissionTime), done: !!r.admissionTime, color: 'bg-red-500 text-white'},
    {key: 'treat', label: '接诊', time: fmt(r.diagnosisTime), done: !!r.diagnosisTime, color: 'bg-purple-500 text-white'},
    {
      key: 'obs', label: '留观',
      time: r.observationEndTime ? `${fmt(r.observationStartTime)}~${fmt(r.observationEndTime)}`
        : (r.observationStartTime ? `${fmt(r.observationStartTime)}起` : ''),
      done: !!r.observationStartTime, color: 'bg-amber-500 text-white',
    },
    {key: 'finish', label: '转归', time: fmt(r.finishTime), done: ended, color: 'bg-slate-500 text-white'},
  ];
});
</script>
