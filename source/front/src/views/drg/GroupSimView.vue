<template>
  <!-- 医保付费聚合页：DRG / DIP 分组器 + 医保结算结果，各自独立取数，页签切换；后端接口、表、权限码互不合并。
       入口由菜单 907 的 medpay:entry 单独控制，页签可见性只用各自业务码：
       授入口 ≠ 授分组模拟 —— 给医保结算员授入口时，不该顺带拿到 DRG/DIP 页签与模拟权 -->
  <el-tabs v-if="tabs.length" v-model="activeTab">
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
  <el-empty v-else description="当前角色未配置医保付费页签权限，请联系管理员分配"/>
</template>

<script setup>
import {computed, ref} from 'vue';
import {useRoute} from 'vue-router';
import {hasPerm, onPermsChange} from '@/lib/perm';
import DrgSimView from './DrgSimView.vue';
import DipSimView from './DipSimView.vue';
import MedpaySettleView from './MedpaySettleView.vue';

const route = useRoute();
// 页签与其后端接口鉴权码一一对应；壳入口码（菜单 907 = medpay:entry）只管"能不能进这一页"，
// 不参与页签判断 —— 否则授壳就等于授了 DRG 分组与模拟（436 之前的口径）
const TAB_PERM = {drg: 'report:drg:list', dip: 'report:dip:list', settle: 'medpay:settle:list'};
const tabs = computed(() => Object.keys(TAB_PERM).filter((t) => hasPerm(TAB_PERM[t])));
const init = route.query.tab;
const activeTab = ref(tabs.value.includes(init) ? init : tabs.value[0] || 'drg');
// 权限集合异步到达后收敛：若当前停留的页签被摘掉，切到第一个可见页签
onPermsChange(() => {
  if (tabs.value.length && !tabs.value.includes(activeTab.value)) activeTab.value = tabs.value[0];
});
</script>
