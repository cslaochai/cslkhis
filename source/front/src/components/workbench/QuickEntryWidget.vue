<template>
  <div>
    <div v-if="items.length === 0" class="py-8 text-center text-[15px] text-slate-400">
      当前角色没有可跳转的菜单页
    </div>

    <div v-else class="grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-4">
      <button
          v-for="it in visible"
          :key="it.path"
          class="flex items-center gap-2.5 rounded-lg border border-slate-200 bg-white px-3.5 py-3 text-left transition-colors hover:border-[#1269B5] hover:bg-blue-50/50"
          type="button"
          @click="router.push(it.path)"
      >
        <component :is="it.icon" class="h-5 w-5 shrink-0 text-[#1269B5]"/>
        <span class="min-w-0">
          <span :title="it.label" class="block truncate text-[15px] font-medium text-slate-800">{{ it.label }}</span>
          <span :title="it.group" class="block truncate text-[13px] text-slate-400">{{ it.group }}</span>
        </span>
      </button>
    </div>

    <button
        v-if="items.length > COLLAPSED_COUNT"
        class="mt-3 text-[15px] text-[#1269B5] hover:underline"
        type="button"
        @click="expanded = !expanded"
    >
      {{ expanded ? '收起' : `展开全部 ${items.length} 个入口` }}
    </button>
  </div>
</template>

<script setup>
import {computed, markRaw, onMounted, ref, watch} from 'vue';
import {useRouter} from 'vue-router';
import * as ElIcons from '@element-plus/icons-vue';
import {loadMenuTree, menuCacheEpoch} from '@/lib/menu-cache';

const router = useRouter();
/** 折叠态下最多展示几个入口，其余点「更多」就地展开（不跳页，弹层展开太容易被误关） */
const COLLAPSED_COUNT = 12;
const FallbackIcon = ElIcons.Document;
const ICON_MAP = ElIcons;
const items = ref([]);
const expanded = ref(false);
const visible = computed(() => (expanded.value ? items.value : items.value.slice(0, COLLAPSED_COUNT)));

async function load() {
  expanded.value = false;
  try {
    const tree = await loadMenuTree();
    items.value = (tree || [])
        .filter((d) => d.menuType === 1)
        .flatMap((d) => (d.children || [])
            // path='/' 是工作台自己，摆进「常用入口」等于原地踏步，跳过
            .filter((m) => m.menuType === 2 && m.isVisible === 1 && m.status === 1 && m.path && m.path !== '/')
            .map((m) => ({
              path: m.path,
              label: m.menuName,
              group: d.menuName,
              // 组件对象进了响应式数组就会被 Proxy 包住，Vue 每次渲染都警告
              icon: markRaw(ICON_MAP[m.icon] || FallbackIcon),
            })));
  } catch (e) {
    // 菜单拉不到时留空态而不是报错页：侧边栏同一份数据，那里也会给出失败提示
    console.error('常用入口加载失败：', e);
    items.value = [];
  }
}

onMounted(load);
// 切角色后菜单代次推进，常驻的工作台不会重建，必须自己重拉
watch(menuCacheEpoch, load);
</script>
