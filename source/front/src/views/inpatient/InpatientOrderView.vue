<script setup lang="ts">
/**
 * 住院医生站（菜单 302，路径沿用 /inpatient-order）
 *
 * 只负责挂载工作区组件：医嘱的界面只允许有一份实现（见 InpatientOrderWorkspace 的注释），
 * 医生站与护士站的区别仅仅是「先看哪个页签」。
 *
 * 菜单名从「住院医嘱」改成「住院医生站」：改名前这一页从名称上看只是住院医嘱的一个子功能，
 * 实际上它是在院患者的医嘱工作台（开立/校对/执行/执行记录 4 个页签），
 * 医生侧的工作入口只有它和 304 住院病历，名字看不出来是给医生用的。
 * 只是改名，menu_key / path / component / 角色授权都不动 —— 不新增空壳菜单。
 */
import InpatientOrderWorkspace from '@/components/his/InpatientOrderWorkspace.vue'
</script>

<template>
  <InpatientOrderWorkspace mode="doctor" />
</template>
