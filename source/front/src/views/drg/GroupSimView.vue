<template>
  <!-- 医保付费聚合页：DRG / DIP 分组器 + 医保结算结果，各自独立取数，页签切换；后端接口、表、权限码互不合并 -->
  <el-tabs v-model="activeTab">
    <el-tab-pane v-if="hasPerm('report:drg:list')" label="DRG 分组" name="drg" lazy>
      <DrgSimView />
    </el-tab-pane>
    <el-tab-pane v-if="hasPerm('report:dip:list')" label="DIP 分组" name="dip" lazy>
      <DipSimView />
    </el-tab-pane>
    <el-tab-pane v-if="hasPerm('medpay:settle:list')" label="医保结算" name="settle" lazy>
      <MedpaySettleView />
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {ref} from 'vue';
import {useRoute} from 'vue-router';
import {hasPerm, onPermsChange} from '@/lib/perm';
import DrgSimView from './DrgSimView.vue';
import DipSimView from './DipSimView.vue';
import MedpaySettleView from './MedpaySettleView.vue';

const route = useRoute();
// 页签与其后端接口鉴权码一一对应；菜单可见性与接口闸门分离，无权限的页签不在壳内出现
const TAB_PERM = {drg: 'report:drg:list', dip: 'report:dip:list', settle: 'medpay:settle:list'};
const visibleTabs = () => Object.keys(TAB_PERM).filter((t) => hasPerm(TAB_PERM[t]));
const init = route.query.tab;
const activeTab = ref(visibleTabs().includes(init) ? init : visibleTabs()[0] || 'drg');
// 权限集合异步到达后收敛：若当前停留的页签被摘掉，切到第一个可见页签
onPermsChange(() => {
  const tabs = visibleTabs();
  if (tabs.length && !tabs.includes(activeTab.value)) activeTab.value = tabs[0];
});
</script>
