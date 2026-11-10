<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('medtech:dialysis:list')" label="净化中心" lazy name="dialysis">
      <DialysisView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:dialysis:list')" label="机位管理" lazy name="machine">
      <DialysisMachineView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import DialysisView from '@/views/dialysis/DialysisView.vue';
import DialysisMachineView from '@/views/dialysis/DialysisMachineView.vue';

// 原 2 个并列菜单（413 血液净化中心 + 2931 血液净化机位管理）合并为一套血透系统，
// 机位作为「配置」tab。厂商口径：血透系统含机位/机组配置。两 tab 共用 medtech:dialysis:list 权限码。
const active = ref('dialysis');

onMounted(async () => {
  await loadPermissions();
});
</script>
