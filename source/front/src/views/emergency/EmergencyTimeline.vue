<template>
  <div class="rounded-lg border border-slate-200 bg-slate-50 p-3" data-testid="emg-timeline">
    <div class="mb-2 text-xs font-bold text-slate-500">就诊时间轴</div>
    <div class="flex items-start">
      <!-- 2026-10-28 修复横线：连接线改为绝对定位（本圆圆心→下一圆圆心），不再靠 flex 分剩余宽度——
           5 个节点挤 420px 右栏时 flex-1 只剩几 px，线视觉上消失；末节点同样等分，圆 z-10 压住线头 -->
      <div v-for="(node, idx) in nodes" :key="node.key"
           class="relative flex flex-1 flex-col items-center">
        <div v-if="idx !== nodes.length - 1" class="absolute h-0.5"
             :class="node.done ? 'bg-emerald-400' : 'bg-slate-300'"
             style="top: 11px; left: calc(50% + 13px); width: calc(100% - 26px);"></div>
        <span class="relative z-10 flex h-6 w-6 items-center justify-center rounded-full text-xs font-bold"
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
