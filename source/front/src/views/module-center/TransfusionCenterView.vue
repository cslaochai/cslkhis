<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('medtech:transfusion:list')" label="输血管理" lazy name="transfusion">
      <TransfusionView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:bloodBank:list')" label="血库管理" lazy name="bloodBank">
      <BloodBankView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import TransfusionView from '@/views/transfusion/TransfusionView.vue';
import BloodBankView from '@/views/bloodbank/BloodBankView.vue';

// 原 2 个并列菜单（406 输血管理 + 411 血库管理）合并为一套输血系统，血库作为「库存」tab。
// 厂商口径：输血系统含血库（入出库/发血/报废/配血）。权限码沿用原菜单。
const active = ref('transfusion');

const order = ['transfusion', 'bloodBank'];
const permOf = {
  transfusion: 'medtech:transfusion:list',
  bloodBank: 'medtech:bloodBank:list',
};

onMounted(async () => {
  await loadPermissions();
  if (!hasPermission(permOf.transfusion)) {
    const first = order.find(k => hasPermission(permOf[k]));
    if (first) active.value = first;
  }
});
</script>
