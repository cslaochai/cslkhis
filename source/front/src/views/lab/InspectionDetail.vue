<template>
  <el-dialog v-model="visible" :title="`检查详情 - ${order?.itemName}`" destroy-on-close width="700px">
    <template v-if="order">
      <div class="space-y-4 py-2">
        <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
          <div><p class="text-xs text-slate-400">患者</p>
            <p class="text-sm font-medium">{{ order.patientName }}</p></div>
          <div><p class="text-xs text-slate-400">记录号</p>
            <p class="text-sm font-medium">{{ order.recordNo }}</p></div>
          <div><p class="text-xs text-slate-400">申请医生</p>
            <p class="text-sm font-medium">{{ order.doctorName }}</p></div>
          <div><p class="text-xs text-slate-400">申请科室</p>
            <p class="text-sm font-medium">{{ order.deptName }}</p></div>
          <div><p class="text-xs text-slate-400">检查项目</p>
            <p class="text-sm font-medium">{{ order.itemName }}</p></div>
          <div><p class="text-xs text-slate-400">检查部位</p>
            <p class="text-sm font-medium">{{ order.bodyPart }}</p></div>
          <div><p class="text-xs text-slate-400">费用</p>
            <p class="text-sm font-medium">¥{{ order.price }}</p></div>
          <div><p class="text-xs text-slate-400">临床诊断</p>
            <p class="text-sm font-medium">{{ order.clinicalDiagnosis }}</p></div>
        </div>

        <!-- 检查所见 -->
        <div v-if="order.resultDescription" class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 text-sm font-medium text-slate-700">检查所见</p>
          <p class="text-sm text-slate-600 whitespace-pre-wrap">{{ order.resultDescription }}</p>
        </div>

        <!-- 影像诊断 -->
        <div v-if="order.resultConclusion" class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 text-sm font-medium text-slate-700">影像诊断/印象</p>
          <p class="text-sm text-slate-600 whitespace-pre-wrap">{{ order.resultConclusion }}</p>
        </div>

        <!-- 建议 -->
        <div v-if="order.suggestions" class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 text-sm font-medium text-slate-700">建议</p>
          <p class="text-sm text-slate-600 whitespace-pre-wrap">{{ order.suggestions }}</p>
        </div>

        <!-- 影像展示（sql/137：真影像帧，挂在申请单上；详情弹框只读） -->
        <ExamImagePanel :apply-id="order.applyId" :biz-type="1" readonly/>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue';
import ExamImagePanel from '@/components/his/ExamImagePanel.vue';

const props = defineProps({
  // v-model 控制显隐
  modelValue: { type: Boolean, default: false },
  // 列表行数据（详情展示的就是这一行，无需再取数）
  order: { type: Object, default: null },
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
</script>
