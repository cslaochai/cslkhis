<template>
  <aside
      :class="sidebarCollapsed ? 'w-16' : 'w-64'"
      class="his-sidebar fixed left-0 top-16 z-40 flex flex-col border-r border-white/10 text-white transition-all duration-300"
  >
    <!-- Navigation -->
    <nav class="flex-1 overflow-y-auto px-2 py-3 custom-scrollbar">
      <!-- 加载中：不显示空态，避免「加载中 ≠ 没有」被误判成菜单丢失 -->
      <p v-if="loading" class="px-3 py-2 text-xs text-slate-300">
        <span v-if="!sidebarCollapsed">菜单加载中…</span>
        <span v-else>…</span>
      </p>

      <!-- 加载失败：给出原因与重试入口 -->
      <div v-else-if="loadError" class="px-3 py-2">
        <p class="mb-2 text-xs leading-5 text-amber-200">{{ loadError }}</p>
        <button
            class="w-full rounded-lg border border-white/30 px-2 py-1 text-xs text-white transition-colors hover:bg-white/10"
            @click="loadMenus(true)"
        >
          重新加载
        </button>
      </div>

      <div v-for="group in navGroups" v-else :key="group.title" class="mb-3">
        <div
            :class="{ 'text-white': isGroupActive(group) }"
            :style="{paddingLeft: sidebarCollapsed ? '17px' : '12px'}"
            class="group-title flex cursor-pointer items-center gap-2 overflow-hidden rounded-lg py-2 pr-3 text-sm font-semibold text-white/95 hover:bg-white/10 hover:text-white"
            @click="toggleGroup(group.title)"
        >
          <span
              :style="{maxWidth: sidebarCollapsed ? '14px' : '200px'}"
              class="group-title-label shrink-0 overflow-hidden whitespace-nowrap"
          >{{ group.title }}</span>
          <component
              :is="isGroupCollapsed(group.title) ? ArrowRight : ArrowDown"
              :class="sidebarCollapsed ? 'opacity-0' : 'opacity-100'"
              class="h-3 w-3 shrink-0 transition-[opacity,transform] duration-200"
          />
        </div>

        <div
            :style="{
              gridTemplateRows: isGroupCollapsed(group.title) ? '0fr' : '1fr',
              opacity: isGroupCollapsed(group.title) ? 0 : 1,
            }"
            class="grid transition-all duration-200"
        >
          <div class="min-h-0 space-y-0.5 overflow-hidden">
            <!-- 直接页面（menuType=2） -->
            <div
                v-for="item in group.items"
                :key="item.href"
                :class="
              isActive(item.href)
                ? 'sidebar-item-active bg-white/[0.16] text-white'
                : 'text-slate-200 hover:bg-white/10 hover:text-white'
            "
                :style="{
                  paddingLeft: sidebarCollapsed ? '15px' : '36px',
                  paddingRight: sidebarCollapsed ? '6px' : '12px',
                }"
                :title="sidebarCollapsed ? item.label : undefined"
                class="sidebar-item relative flex cursor-pointer items-center gap-3 overflow-hidden rounded-lg py-2 text-[13px] font-normal"
                @click="navigate(item.href)"
            >
              <component :is="item.icon" class="h-[18px] w-[18px] shrink-0"/>
              <!-- 文字淡出而不是 v-if 摘除：摘了会在动画起点就空出一截，看着像闪一下 -->
              <span
                  :class="sidebarCollapsed ? 'opacity-0' : 'opacity-100'"
                  class="shrink-0 whitespace-nowrap transition-opacity duration-200"
              >{{ item.label }}</span>
            </div>

            <!-- 子分组（嵌套目录 menuType=1）：分组 → 子分组 → 页面，支持三层（如药事管理/财务结算） -->
            <template v-for="sg in group.subgroups" :key="subKey(group.title, sg.title)">
              <div
                  :class="isSubgroupActive(group, sg) ? 'text-white' : 'text-white/80'"
                  :style="{paddingLeft: sidebarCollapsed ? '15px' : '30px'}"
                  class="flex cursor-pointer items-center gap-2 overflow-hidden rounded-lg py-1.5 pr-3 text-[12px] font-medium hover:bg-white/10"
                  @click="toggleGroup(subKey(group.title, sg.title))"
              >
                <component :is="sg.icon" class="h-3.5 w-3.5 shrink-0 opacity-80"/>
                <span :class="sidebarCollapsed ? 'opacity-0' : 'opacity-100'" class="shrink-0 whitespace-nowrap">{{ sg.title }}</span>
                <component
                    :is="isGroupCollapsed(subKey(group.title, sg.title)) ? ArrowRight : ArrowDown"
                    :class="sidebarCollapsed ? 'opacity-0' : 'opacity-100'"
                    class="h-3 w-3 shrink-0 transition-[opacity,transform] duration-200"/>
              </div>
              <div
                  :style="{
                    gridTemplateRows: isGroupCollapsed(subKey(group.title, sg.title)) ? '0fr' : '1fr',
                    opacity: isGroupCollapsed(subKey(group.title, sg.title)) ? 0 : 1,
                  }"
                  class="grid transition-all duration-200"
              >
                <div class="min-h-0 space-y-0.5 overflow-hidden">
                  <div
                      v-for="item in sg.items"
                      :key="item.href"
                      :class="isActive(item.href) ? 'sidebar-item-active bg-white/[0.16] text-white' : 'text-slate-200 hover:bg-white/10 hover:text-white'"
                      :style="{paddingLeft: sidebarCollapsed ? '15px' : '52px', paddingRight: sidebarCollapsed ? '6px' : '12px'}"
                      :title="sidebarCollapsed ? item.label : undefined"
                      class="sidebar-item relative flex cursor-pointer items-center gap-3 overflow-hidden rounded-lg py-2 text-[13px] font-normal"
                      @click="navigate(item.href)"
                  >
                    <component :is="item.icon" class="h-[18px] w-[18px] shrink-0"/>
                    <span :class="sidebarCollapsed ? 'opacity-0' : 'opacity-100'" class="shrink-0 whitespace-nowrap transition-opacity duration-200">{{ item.label }}</span>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <!-- Collapse Toggle -->
    <div class="shrink-0 border-t border-white/20 p-2">
      <button
          class="flex w-full items-center justify-center rounded-lg p-2 text-slate-300 transition-colors hover:bg-white/10 hover:text-white"
          @click="toggleSidebar()"
      >
        <ArrowRight v-if="sidebarCollapsed" class="h-5 w-5"/>
        <ArrowLeft v-else class="h-5 w-5"/>
      </button>
    </div>
  </aside>
</template>

<script setup>
import {ref, shallowRef, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import * as ElIcons from '@element-plus/icons-vue';
import {loadMenuTree, menuCacheEpoch} from '@/lib/menu-cache';
import {sidebarCollapsed, toggleSidebar} from '@/lib/sidebarState';

/** 侧边栏自身的 UI 图标（折叠箭头、图标兜底），与菜单数据无关 */
const ArrowDown = ElIcons.ArrowDown;
const ArrowLeft = ElIcons.ArrowLeft;
const ArrowRight = ElIcons.ArrowRight;
const FallbackIcon = ElIcons.Document;
const route = useRoute();
const router = useRouter();
const navGroups = shallowRef([]);
const loading = ref(true);
const loadError = ref('');
const ICON_MAP = ElIcons;

function resolveIcon(name) {
  if (!name)
    return FallbackIcon;
  return ICON_MAP[name] || FallbackIcon;
}

function bySortOrder(a, b) {
  // sort_order 缺失要沉到最后：按 0 参与比较会让新菜单静默置顶
  return (a.sortOrder ?? Number.MAX_SAFE_INTEGER) - (b.sortOrder ?? Number.MAX_SAFE_INTEGER);
}

/**
 * 把后端菜单树映射成侧边栏分组结构。
 * 支持三层：分组(menuType=1) → 子分组(menuType=1) → 页面(menuType=2)。
 * 普通两层结构（分组下直接挂页面）保持原行为；嵌套目录（如药事管理/财务结算）
 * 会被拆成「分组 → 子分组 → 页面」，否则子目录会被旧的两层过滤整组隐藏。
 */
function toNavGroups(nodes) {
  return nodes
      .filter(node => node.menuType === 1 && node.isVisible === 1 && node.status === 1)
      .slice()
      .sort(bySortOrder)
      .map(directory => {
        const kids = (directory.children || [])
            .filter(c => c.isVisible === 1 && c.status === 1)
            .sort(bySortOrder);
        // 直接页面
        const pages = kids
            .filter(c => c.menuType === 2)
            .map(c => ({href: c.path, label: c.menuName, icon: resolveIcon(c.icon)}));
        // 子分组（嵌套目录）：只保留「下有可见页面」的子分组，避免空标题
        const subgroups = kids
            .filter(c => c.menuType === 1)
            .map(sub => ({
              title: sub.menuName,
              icon: resolveIcon(sub.icon),
              items: (sub.children || [])
                  .filter(c => c.menuType === 2 && c.isVisible === 1 && c.status === 1)
                  .sort(bySortOrder)
                  .map(c => ({href: c.path, label: c.menuName, icon: resolveIcon(c.icon)})),
            }))
            .filter(sg => sg.items.length > 0);
        return {title: directory.menuName, icon: resolveIcon(directory.icon), items: pages, subgroups};
      })
      // 分组下一个可见菜单都没有时，不渲染空标题
      .filter(group => group.items.length > 0 || group.subgroups.length > 0);
}

async function loadMenus(force = false) {
  loading.value = true;
  loadError.value = '';
  try {
    const tree = await loadMenuTree(force);
    navGroups.value = toNavGroups((tree || []));
    if (navGroups.value.length === 0) {
      loadError.value = '菜单未配置，请联系管理员在「系统管理 → 菜单管理」中维护';
    }
  } catch (e) {
    // 绝不静默：失败要看得见，并给出重试
    console.error('菜单加载失败：', e);
    loadError.value = e?.message ? `菜单加载失败：${e.message}` : '菜单加载失败';
  } finally {
    loading.value = false;
    initGroupState();
  }
}

// 分组折叠状态（key：顶层分组用标题；子分组用「分组 / 子分组」复合 key）
const collapsedGroups = ref(new Set());
const subKey = (g, s) => `${g} / ${s}`;
// 初始化：根据当前路由找到所在分组/子分组，展开它们，折叠其余
const initGroupState = () => {
  const currentPath = route.path;
  const active = new Set();
  for (const group of navGroups.value) {
    const groupHit = group.items.some(i => isPathMatch(currentPath, i.href));
    const hitSubs = group.subgroups.filter(sg => sg.items.some(i => isPathMatch(currentPath, i.href)));
    if (groupHit || hitSubs.length) {
      active.add(group.title);
      hitSubs.forEach(sg => active.add(subKey(group.title, sg.title)));
    }
  }
  const allKeys = [];
  for (const group of navGroups.value) {
    allKeys.push(group.title);
    group.subgroups.forEach(sg => allKeys.push(subKey(group.title, sg.title)));
  }
  // 折叠所有分组/子分组，除了当前活跃的
  collapsedGroups.value = new Set(allKeys.filter(k => !active.has(k)));
};
const toggleGroup = (key) => {
  if (collapsedGroups.value.has(key)) {
    collapsedGroups.value.delete(key);
  } else {
    collapsedGroups.value.add(key);
  }
};
const isGroupCollapsed = (key) => collapsedGroups.value.has(key);
// 高亮：分组本身或任一子分组命中当前路由
const isGroupActive = (group) => {
  const currentPath = route.path;
  return group.items.some(item => isPathMatch(currentPath, item.href))
      || group.subgroups.some(sg => sg.items.some(item => isPathMatch(currentPath, item.href)));
};
// 子分组命中当前路由时高亮子分组标题
const isSubgroupActive = (group, sg) => {
  const currentPath = route.path;
  return sg.items.some(item => isPathMatch(currentPath, item.href));
};

// 路由匹配：必须整段相等或以后续 / 为边界，避免 /pharmacy-window 误命中 /pharmacy
function isPathMatch(path, href) {
  if (href === '/')
    return path === '/';
  return path === href || path.startsWith(href + '/');
}

// 路由变化时自动展开对应分组
watch(() => route.path, () => {
  initGroupState();
}, {immediate: true});

function isActive(href) {
  return isPathMatch(route.path, href);
}

function navigate(href) {
  router.push(href);
}

loadMenus();
/**
 * 菜单缓存被清空时立即重跑（切换角色 / 切换科室 / 菜单管理改完菜单）
 */
watch(menuCacheEpoch, () => {
  loadMenus();
});
</script>

<style scoped>
.group-title,
.sidebar-item {
  transition: padding-left 0.3s ease, padding-right 0.3s ease,
  background-color 0.15s ease, color 0.15s ease;
}

/* 分组标题限宽也要跟着过渡，否则首字是「啪」一下切出来的 */
.group-title-label {
  transition: max-width 0.3s ease;
}

/* 激活项左侧白色指示条：底色是纯色 chrome，选中态只靠「亮条 + 半透明白底」两级表达，
   不再用青绿——--his-teal 是护理/健康语义色，占在选中态上会让「这一项被选中」和「护理类」混成一件事。
   半透明白底由元素上的 bg-white/[0.16] 提供，这里不叠第二层背景。 */
.sidebar-item-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  border-radius: 0 3px 3px 0;
  background: #fff;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
