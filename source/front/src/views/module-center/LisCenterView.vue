<template>
  <div v-if="visibleTabs.length === 0" class="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800"
       data-testid="module-no-perm">
    当前岗位未开通检验工作站权限（检验 / 标本管理 / 检验质控 / 室间质评）。
    请管理员在「系统管理 → 角色管理 → 菜单权限」里为本岗位勾选对应模块；来自统一待办的检验报告，
    也可在待办弹框里直接查看详情。
  </div>
  <el-tabs v-else v-model="active" class="module-tabs">
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
import {computed, onMounted, ref} from 'vue';
import {useRoute} from 'vue-router';
import {hasPermission, loadPermissions} from '@/lib/permission';
import LaboratoryView from '@/views/lab/LaboratoryView.vue';
import SpecimenView from '@/views/specimen/SpecimenView.vue';
import LisQcView from '@/views/lisqc/LisQcView.vue';
import LisEqaView from '@/views/lisqc/LisEqaView.vue';

// 原 4 个并列菜单（402 检验工作站 + 403 标本管理 + 410 检验质控 + 416 室间质评）
// 合并为统一 LIS 入口，标本/质控/间质评作为页内 tab。权限码沿用原菜单，角色按所配项看到对应 tab。
const active = ref('lab');
const route = useRoute();

const order = ['lab', 'specimen', 'qc', 'eqa'];
const permOf = {
  lab: 'medtech:laboratoryWorkstation:list',
  specimen: 'medtech:specimen:list',
  qc: 'medtech:lisQc:list',
  eqa: 'medtech:lisEqa:list',
};
const visibleTabs = computed(() => order.filter((k) => hasPermission(permOf[k])));
/** 待办深链：检验报告落到「检验」tab */
const MSG_TAB = {report: 'lab', inspection: 'lab'};

onMounted(async () => {
  await loadPermissions();
  const wanted = MSG_TAB[String(route.query.bizType || '')];
  if (wanted && hasPermission(permOf[wanted])) {
    active.value = wanted;
    return;
  }
  if (!hasPermission(permOf.lab)) {
    const first = order.find(k => hasPermission(permOf[k]));
    if (first) active.value = first;
  }
});
</script>
