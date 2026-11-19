<template>
  <!-- 分组模拟聚合页：DRG / DIP 两套分组器各自独立取数，页签切换；后端接口与表互不合并 -->
  <el-tabs v-model="activeTab">
    <el-tab-pane label="DRG 分组" name="drg" lazy>
      <DrgSimView />
    </el-tab-pane>
    <el-tab-pane label="DIP 分组" name="dip" lazy>
      <DipSimView />
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {ref} from 'vue';
import DrgSimView from './DrgSimView.vue';
import DipSimView from './DipSimView.vue';

const activeTab = ref('drg');
</script>
