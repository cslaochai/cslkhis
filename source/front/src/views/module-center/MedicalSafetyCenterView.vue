<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('emr:adverseEvent:list')" label="不良事件" lazy name="adverse">
      <AdverseEventView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('qc:dispute:list')" label="纠纷与投诉" lazy name="dispute">
      <DisputeView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import AdverseEventView from '@/views/adverse-event/AdverseEventView.vue';
import DisputeView from '@/views/dispute/DisputeView.vue';

// 原两个菜单：610 不良事件上报、615 医疗纠纷与投诉，同属医疗安全风险，归医务科。
const active = ref('adverse');

onMounted(async () => {
  await loadPermissions();
  if (!hasPermission('emr:adverseEvent:list') && hasPermission('qc:dispute:list')) {
    active.value = 'dispute';
  }
});
</script>
