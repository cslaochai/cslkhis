<script setup lang="ts">
/**
 * 住院病历（医生站）—— 直接挂载共用工作区，页面本身不写业务逻辑。
 *
 * 与护士站共用组件是刻意的：病历的状态机（草稿→已提交→已归档）与文案口径
 * 只允许有一份实现，前端各写一套迟早对不上。
 */
import InpatientRecordWorkspace from '@/components/his/InpatientRecordWorkspace.vue'
</script>

<template>
  <InpatientRecordWorkspace />
</template>
