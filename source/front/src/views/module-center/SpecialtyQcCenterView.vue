<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('qc:clinicalPath:list')" label="临床路径" lazy name="path">
      <ClinicalPathView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('qc:singleDisease:list')" label="单病种质控" lazy name="single">
      <SingleDiseaseView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import ClinicalPathView from '@/views/clinical-path/ClinicalPathView.vue';
import SingleDiseaseView from '@/views/quality/SingleDiseaseView.vue';

// 原两个菜单：607 临床路径、2183 单病种质控，都是医务科的专项质量管理工具。
const active = ref('path');

onMounted(async () => {
  await loadPermissions();
  if (!hasPermission('qc:clinicalPath:list') && hasPermission('qc:singleDisease:list')) {
    active.value = 'single';
  }
});
</script>
