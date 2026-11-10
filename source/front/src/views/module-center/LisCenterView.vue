<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('medtech:laboratoryWorkstation:list')" label="检验" lazy name="lab">
      <LaboratoryView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:specimen:list')" label="标本管理" lazy name="specimen">
      <SpecimenView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:lisQc:list')" label="检验质控" lazy name="qc">
      <LisQcView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:lisEqa:list')" label="室间质评" lazy name="eqa">
      <LisEqaView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import LaboratoryView from '@/views/lab/LaboratoryView.vue';
import SpecimenView from '@/views/specimen/SpecimenView.vue';
import LisQcView from '@/views/lisqc/LisQcView.vue';
import LisEqaView from '@/views/lisqc/LisEqaView.vue';

// 原 4 个并列菜单（402 检验工作站 + 403 标本管理 + 410 检验质控 + 416 室间质评）
// 合并为统一 LIS 入口，标本/质控/间质评作为页内 tab。权限码沿用原菜单，角色按所配项看到对应 tab。
const active = ref('lab');

const order = ['lab', 'specimen', 'qc', 'eqa'];
const permOf = {
  lab: 'medtech:laboratoryWorkstation:list',
  specimen: 'medtech:specimen:list',
  qc: 'medtech:lisQc:list',
  eqa: 'medtech:lisEqa:list',
};

onMounted(async () => {
  await loadPermissions();
  if (!hasPermission(permOf.lab)) {
    const first = order.find(k => hasPermission(permOf[k]));
    if (first) active.value = first;
  }
});
</script>
