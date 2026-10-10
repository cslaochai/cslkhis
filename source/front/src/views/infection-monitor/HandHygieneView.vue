<template>
  <div class="hand-hygiene-view" data-testid="hand-hygiene-view">
    <HandHygienePanel/>
  </div>
</template>

<script setup>
/**
 * 手卫生依从性（菜单 617 / 路由 /hand-hygiene，sql/165）
 *
 * 为什么把院感监测里的一个页签单独拎出来成一个菜单：
 * 手卫生观察是感控专职**按月、按科室**做的活 —— 每月要去各科室暗访登记的时机数/执行数，
 * 汇总出依从率交给院感委员会。放在「院感监测」第三个页签里要先进院感、再切页签，
 * 而且院感监测主页面关注的是病例核查（急诊且高频），两个使用频次完全不同的动作挤在一起。
 * 内容同 components/his/HandHygienePanel.vue，与院感监测页签同源。
 */
import HandHygienePanel from '@/components/his/HandHygienePanel.vue'
</script>

<style scoped>
.hand-hygiene-view {
  padding: 4px;
}
</style>
