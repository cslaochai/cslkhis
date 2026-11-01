<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('emr:infectiousReport:list')" label="传染病报告卡" lazy name="infect">
      <InfectiousReportView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('emr:publicHealth:list')" label="公卫上报" lazy name="health">
      <PublicHealthReportView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import InfectiousReportView from '@/views/infectious-report/InfectiousReportView.vue';
import PublicHealthReportView from '@/views/public-health/PublicHealthReportView.vue';

// 原两个菜单：613 传染病报告卡、618 公卫上报。报卡本身就是公卫上报的一种。
const active = ref('infect');

onMounted(async () => {
  await loadPermissions();
  if (!hasPermission('emr:infectiousReport:list') && hasPermission('emr:publicHealth:list')) {
    active.value = 'health';
  }
});
</script>
