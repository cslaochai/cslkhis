<template>
  <div class="duty-log-view" data-testid="duty-log-view">
    <DutyLogPanel/>
  </div>
</template>

<script lang="js" setup>
/**
 * 总值班日志 / 交班本（菜单 2920 / 路由 /duty-log，sql/176）
 *
 * 为什么从「总值班排班」里拆出来单独挂菜单：
 * 排班（院办，一周编一次）与交班本（当班的人，每班都要记与签收）是两件不同频率、
 * 不同岗位的活。合在一页时，接班的人要先打开排班页再切到第二个页签才能签收 ——
 * 而「交出去的东西有没有人认领」本来就是值班期间最不能漏的一步。
 * 内容同 components/his/DutyLogPanel.vue，与拆之前的页签同源。
 */
import DutyLogPanel from '@/components/his/DutyLogPanel.vue'
</script>

<style scoped>
.duty-log-view {
  padding: 4px;
}
</style>
