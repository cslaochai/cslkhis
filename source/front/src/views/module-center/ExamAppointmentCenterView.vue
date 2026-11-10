<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('medtech:examAppoint:list')" label="检查预约" lazy name="appointment">
      <ExamAppointmentView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:examAppoint:list')" label="设备与号源" lazy name="slot">
      <ExamDeviceSlotView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import ExamAppointmentView from '@/views/exam-appointment/ExamAppointmentView.vue';
import ExamDeviceSlotView from '@/views/exam-appointment/ExamDeviceSlotView.vue';

// 原 2 个并列菜单（412 检查预约中心 + 2925 检查设备与号源）合并为一套预约入口，
// 设备与号源作为 tab。厂商 PACS 内「预约登记工作站 + 站点/设备管理」同属一件事。
// 两 tab 共用 medtech:examAppoint:list 权限码。
const active = ref('appointment');

onMounted(async () => {
  await loadPermissions();
});
</script>
