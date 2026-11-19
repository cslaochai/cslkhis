<template>
  <!-- 统一待办深链提示条：从收件箱「前往…处理」跳进来时，明确告诉用户要看哪条单据。
       目标页能按 bizNo 定位（急诊/分诊/带搜索框的列表）会自己定位；定位不了的页面至少不再像「空降」。 -->
  <div v-if="link" :class="barClass" data-testid="msg-deeplink-bar">
    <el-icon class="flex-shrink-0">
      <Link/>
    </el-icon>
    <span class="font-medium">来自统一待办</span>
    <el-tag size="small" type="info">{{ label }}</el-tag>
    <span v-if="link.bizNo" class="font-mono text-xs">单号 {{ link.bizNo }}</span>
    <span class="text-xs opacity-80">{{ hint }}</span>
    <el-button class="ml-auto" link size="small" @click="dismiss">关闭</el-button>
  </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import {Link} from '@element-plus/icons-vue';
import {messageLabel} from '@/lib/messageCatalog';

const route = useRoute();
// 关闭只对「当前这条深链」生效：换个目标路由再跳进来要重新提示
const dismissedKey = ref('');
const key = computed(() => `${route.fullPath}`);
const link = computed(() => {
  if (!route.query.from || route.query.from !== 'msg')
    return null;
  if (dismissedKey.value === key.value)
    return null;
  return {bizType: String(route.query.bizType || ''), bizNo: String(route.query.bizNo || '')};
});
const label = computed(() => messageLabel(link.value?.bizType || ''));
const barClass = computed(() => 'mb-3 flex flex-wrap items-center gap-2 rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-sm text-sky-800');
const hint = computed(() => (link.value?.bizNo
    ? '已把单号带到本页，列表未自动筛到该条时可在搜索框按单号再查。'
    : '已打开该单据所在页面，请按业务时间/状态查找。'));
const dismiss = () => {
  dismissedKey.value = key.value;
};
// 页面内正常导航（query 变化）后旧提示自动失效
watch(() => route.fullPath, () => {
  if (dismissedKey.value && dismissedKey.value !== route.fullPath)
    dismissedKey.value = '';
});
</script>
