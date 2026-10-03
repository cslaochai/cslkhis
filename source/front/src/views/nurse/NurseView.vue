<script setup lang="ts">
/**
 * 护士工作站（住院医嘱执行站 + 护理文书）
 *
 * 这一页原来是个硬编码空壳：36 个在管患者、18 条待执行医嘱全是写死的演示数据，
 * 「执行医嘱」按下去什么也不会发生。P1 把它换成真实的医嘱执行站，P2 再加护理文书。
 *
 * 三个视图共用一个页面：护士的实际工作就是「先看病区全貌、再核对医嘱、最后做护理记录」，
 * 拆成三个菜单反而要在页面之间来回切患者。床位图是那个「病区全貌」。
 *
 * 重要：切换用 el-radio-button **不是 el-tabs** —— 医嘱工作区本身是 el-tabs，
 * 外层再用 el-tabs 就会有两层 tab 项，查询「当前激活的页签」会取到外层的那个，
 * 自动化验证与"默认落在待校对页签"这个口径都会被外层抢走。
 */
import { ref } from 'vue'
import { FirstAidKit, EditPen, Grid } from '@element-plus/icons-vue'
import InpatientOrderWorkspace from '@/components/his/InpatientOrderWorkspace.vue'
import NursingRecordWorkspace from '@/components/his/NursingRecordWorkspace.vue'
import BedMapWorkspace from '@/components/his/BedMapWorkspace.vue'

const view = ref<'order' | 'nursing' | 'bedmap'>('order')
</script>

<template>
  <div class="space-y-4">
    <el-radio-group v-model="view" class="shrink-0">
      <el-radio-button value="order">
        <el-icon class="mr-1"><FirstAidKit /></el-icon>医嘱执行
      </el-radio-button>
      <el-radio-button value="nursing">
        <el-icon class="mr-1"><EditPen /></el-icon>护理文书
      </el-radio-button>
      <el-radio-button value="bedmap">
        <el-icon class="mr-1"><Grid /></el-icon>床位图
      </el-radio-button>
    </el-radio-group>
    <InpatientOrderWorkspace v-if="view === 'order'" mode="nurse" />
    <NursingRecordWorkspace v-else-if="view === 'nursing'" />
    <BedMapWorkspace v-else />
  </div>
</template>
