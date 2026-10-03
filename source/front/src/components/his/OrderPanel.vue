<script setup lang="ts">
// 医嘱面板折叠区容器（处方 / 检查申请 / 检验申请 各一节）。
// 只做「标题 + 计数 + 内容」的壳，不做业务判断；业务内容由调用方插槽给。
// 存在的意义：让「病历」与「医嘱」同屏，而不是像原来那样靠 Tab 二选一。
//
// 形态（2026-09-21 老王定）：它是**「医嘱」那张大卡里面的一个子卡**，与左栏「病历」里的
// 病史/体格检查/诊断处置三卡同构 —— 大卡给标题栏，子卡各自带边框、圆角、灰标题条，
// 间距由调用方的内容区（p-3 + space-y-3）给。别把它做成无边框的分节：
// 那会和「病历」那边长得不一样，同一个页面两套卡片语言。
withDefaults(defineProps<{
  title: string
  /** 右上角计数文案，例如 "2 张" / "3 项" */
  countText?: string
  /** 折叠区默认是否展开 */
  defaultOpen?: boolean
}>(), {
  countText: '',
  defaultOpen: true,
})
</script>

<template>
  <el-collapse class="order-panel" :model-value="defaultOpen ? ['1'] : []">
    <el-collapse-item name="1">
      <template #title>
        <div class="flex w-full items-center justify-between pr-2">
          <span class="text-sm font-bold text-slate-700">{{ title }}</span>
          <span v-if="countText" class="text-xs font-normal text-slate-400">{{ countText }}</span>
        </div>
      </template>
      <div class="pt-1">
        <slot/>
      </div>
    </el-collapse-item>
  </el-collapse>
</template>

<style scoped>
/* 形态对齐左栏「病历」里的三个子卡：border + rounded-lg + 灰标题条 + 12px 内容内边距。
   ⚠ 这里**不能**写 `:deep(.el-collapse)` —— 那会编译成 `[data-v-x] .el-collapse`（后代选择器），
   而 el-collapse 就是本组件的根元素、祖先上不带这个属性，规则永远不生效（假绿：查源码有、渲染没变）。
   根元素自己带 scope 属性，直接写类名即可。原文件里那句 `:deep(.el-collapse){border:none}` 就是这么变成死代码的。 */
.order-panel {
  /* 边框色与「病历」子卡的 border-slate-200 取同一个值 —— 同页两处子卡，颜色别各说各话 */
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

:deep(.el-collapse-item__header) {
  height: auto;
  min-height: 37px;   /* 与「病历」子卡标题条同高（px-3 py-2 + text-sm + 1px 下边框） */
  padding: 0 12px;
  border-bottom: 1px solid var(--his-card-border, #e3ecf5);
  background: #f8fafc;
}

:deep(.el-collapse-item__wrap) {
  border-bottom: none;
}

:deep(.el-collapse-item__content) {
  padding: 8px 12px 12px;
}
</style>
