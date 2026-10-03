<script setup lang="ts">
// 页面底部固定动作条（门诊工作台的习惯：主操作永远在同一个位置、同一行，不随内容滚动）。
// 只管容器，按钮由调用方通过默认插槽给。
//
// align：按钮组在条内的水平位置。
//   center（默认）—— 居中，适合「全宽卡片、三两个按钮」的页面；
//   start —— 靠左，与上方正文左对齐，适合正文很宽、按钮缩在中间反而要来回找的作业台。
withDefaults(defineProps<{ align?: 'center' | 'start' }>(), {align: 'center'})
</script>

<template>
  <div
      class="flex h-[var(--his-actionbar-h)] shrink-0 items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 shadow-sm"
      :class="align === 'start' ? 'justify-start' : 'justify-center'">
    <slot/>
  </div>
</template>
