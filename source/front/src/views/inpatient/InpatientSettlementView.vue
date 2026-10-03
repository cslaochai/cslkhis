<script setup lang="ts">
/**
 * 住院账务（收费/住院管理侧）—— 直接挂载共用工作区，页面本身不写业务逻辑。
 *
 * 预交金、日清单、住院结算的口径只允许有一份实现，前端各写一套迟早对不上。
 */
import InpatientSettlementWorkspace from '@/components/his/InpatientSettlementWorkspace.vue'
</script>

<template>
  <InpatientSettlementWorkspace />
</template>
