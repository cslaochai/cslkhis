<script setup lang="ts">
import type {Component} from 'vue'
import {ref, shallowRef, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import * as ElIcons from '@element-plus/icons-vue'
import {loadMenuTree, menuCacheEpoch} from '@/lib/menu-cache'
import {sidebarCollapsed, toggleSidebar} from '@/lib/sidebarState'

/** 侧边栏自身的 UI 图标（折叠箭头、图标兜底），与菜单数据无关 */
const ArrowDown = ElIcons.ArrowDown
const ArrowLeft = ElIcons.ArrowLeft
const ArrowRight = ElIcons.ArrowRight
const FallbackIcon = ElIcons.Document

/**
 * 侧边栏菜单项（由 sys_menu 中 menu_type=2 的记录映射而来）
 */
interface NavItem {
  /** 目标路由地址，取 sys_menu.path（如 /system/user） */
  href: string
  /** 菜单显示名，取 sys_menu.menu_name */
  label: string
  /** Element Plus 图标组件，按 sys_menu.icon 的组件名解析 */
  icon: Component
}

/**
 * 侧边栏菜单分组（由 sys_menu 中 menu_type=1 的记录映射而来）
 */
interface NavGroup {
  /** 分组标题，取 sys_menu.menu_name */
  title: string
  /** 分组内的菜单项，按 sort_order 升序 */
  items: NavItem[]
}

/**
 * 后端 /system/menu/tree 返回的菜单节点（MenuVO 的前端投影，只声明用到的字段）
 */
interface MenuNode {
  id: string
  parentId: string
  menuName: string
  sortOrder: number | null
  /** 1-目录 2-菜单 3-按钮 */
  menuType: number
  path: string
  icon: string
  /** 0-隐藏 1-显示 */
  isVisible: number
  /** 0-停用 1-启用 */
  status: number
  children?: MenuNode[] | null
}

const route = useRoute()
const router = useRouter()

const navGroups = shallowRef<NavGroup[]>([])

const loading = ref(true)
const loadError = ref('')

const ICON_MAP = ElIcons as unknown as Record<string, Component>

function resolveIcon(name?: string): Component {
  if (!name) return FallbackIcon
  return ICON_MAP[name] || FallbackIcon
}

function bySortOrder(a: MenuNode, b: MenuNode) {
  // sort_order 缺失要沉到最后：按 0 参与比较会让新菜单静默置顶
  return (a.sortOrder ?? Number.MAX_SAFE_INTEGER) - (b.sortOrder ?? Number.MAX_SAFE_INTEGER)
}

/** 把后端菜单树映射成侧边栏分组结构 */
function toNavGroups(nodes: MenuNode[]): NavGroup[] {
  return nodes
      .filter(node => node.menuType === 1 && node.isVisible === 1 && node.status === 1)
      .slice()
      .sort(bySortOrder)
      .map(directory => ({
        title: directory.menuName,
        items: (directory.children || [])
            .filter(child => child.menuType === 2 && child.isVisible === 1 && child.status === 1)
            .slice()
            .sort(bySortOrder)
            .map(child => ({
              href: child.path,
              label: child.menuName,
              icon: resolveIcon(child.icon),
            })),
      }))
      // 目录下一个可见菜单都没有时，不渲染空标题
      .filter(group => group.items.length > 0)
}

async function loadMenus(force = false) {
  loading.value = true
  loadError.value = ''
  try {
    const tree = await loadMenuTree(force)
    navGroups.value = toNavGroups((tree || []) as MenuNode[])
    if (navGroups.value.length === 0) {
      loadError.value = '菜单未配置，请联系管理员在「系统管理 → 菜单管理」中维护'
    }
  } catch (e: any) {
    // 绝不静默：失败要看得见，并给出重试
    console.error('菜单加载失败：', e)
    loadError.value = e?.message ? `菜单加载失败：${e.message}` : '菜单加载失败'
  } finally {
    loading.value = false
    initGroupState()
  }
}

// 分组折叠状态（key 用分组标题）
const collapsedGroups = ref<Set<string>>(new Set())

// 初始化：根据当前路由找到所在分组，展开该分组，折叠其他分组
const initGroupState = () => {
  const currentPath = route.path
  let activeGroupTitle = ''

  for (const group of navGroups.value) {
    if (group.items.some(item => isPathMatch(currentPath, item.href))) {
      activeGroupTitle = group.title
      break
    }
  }

  // 折叠所有分组，除了当前活跃的
  collapsedGroups.value = new Set(
      navGroups.value.filter(g => g.title !== activeGroupTitle).map(g => g.title)
  )
}

const toggleGroup = (title: string) => {
  if (collapsedGroups.value.has(title)) {
    collapsedGroups.value.delete(title)
  } else {
    collapsedGroups.value.add(title)
  }
}

const isGroupCollapsed = (title: string) => collapsedGroups.value.has(title)

// 检查分组内是否有当前路由的子项（用于高亮分组标题）
const isGroupActive = (group: NavGroup) => {
  const currentPath = route.path
  return group.items.some(item => isPathMatch(currentPath, item.href))
}

// 路由匹配：必须整段相等或以后续 / 为边界，避免 /pharmacy-window 误命中 /pharmacy
function isPathMatch(path: string, href: string): boolean {
  if (href === '/') return path === '/'
  return path === href || path.startsWith(href + '/')
}

// 路由变化时自动展开对应分组
watch(() => route.path, () => {
  initGroupState()
}, {immediate: true})

function isActive(href: string): boolean {
  return isPathMatch(route.path, href)
}

function navigate(href: string) {
  router.push(href)
}

loadMenus()

/**
 * 菜单缓存被清空时立即重跑（切换角色 / 切换科室 / 菜单管理改完菜单）
 */
watch(menuCacheEpoch, () => {
  loadMenus()
})
</script>

<template>
  <aside
      class="fixed left-0 top-16 z-40 flex flex-col border-r border-white/10 text-white transition-all duration-300"
      :class="sidebarCollapsed ? 'w-16' : 'w-64'"
      style="background: linear-gradient(180deg, #0A3D6B 0%, #0B4F8A 60%, #0E5F8F 100%); height: calc(100vh - 64px); box-shadow: 2px 0 10px rgba(9, 47, 84, 0.18);"
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
            class="group-title flex cursor-pointer items-center gap-2 overflow-hidden rounded-lg py-2 pr-3 text-sm font-semibold text-white/95 hover:bg-white/10 hover:text-white"
            :class="{ 'text-white': isGroupActive(group) }"
            :style="{paddingLeft: sidebarCollapsed ? '17px' : '12px'}"
            @click="toggleGroup(group.title)"
        >
          <span
              class="group-title-label shrink-0 overflow-hidden whitespace-nowrap"
              :style="{maxWidth: sidebarCollapsed ? '14px' : '200px'}"
          >{{ group.title }}</span>
          <component
              :is="isGroupCollapsed(group.title) ? ArrowRight : ArrowDown"
              class="h-3 w-3 shrink-0 transition-[opacity,transform] duration-200"
              :class="sidebarCollapsed ? 'opacity-0' : 'opacity-100'"
          />
        </div>

        <div
            class="grid transition-all duration-200"
            :style="{
              gridTemplateRows: isGroupCollapsed(group.title) ? '0fr' : '1fr',
              opacity: isGroupCollapsed(group.title) ? 0 : 1,
            }"
        >
          <div class="min-h-0 space-y-0.5 overflow-hidden">
            <div
                v-for="item in group.items"
                :key="item.href"
                :title="sidebarCollapsed ? item.label : undefined"
                class="sidebar-item relative flex cursor-pointer items-center gap-3 overflow-hidden rounded-lg py-2 text-[13px] font-normal"
                :class="
              isActive(item.href)
                ? 'sidebar-item-active bg-white/[0.16] text-white'
                : 'text-slate-200 hover:bg-white/10 hover:text-white'
            "
                :style="{
                  paddingLeft: sidebarCollapsed ? '15px' : '36px',
                  paddingRight: sidebarCollapsed ? '6px' : '12px',
                }"
                @click="navigate(item.href)"
            >
              <component :is="item.icon" class="h-[18px] w-[18px] shrink-0"/>
              <!-- 文字淡出而不是 v-if 摘除：摘了会在动画起点就空出一截，看着像闪一下 -->
              <span
                  class="shrink-0 whitespace-nowrap transition-opacity duration-200"
                  :class="sidebarCollapsed ? 'opacity-0' : 'opacity-100'"
              >{{ item.label }}</span>
            </div>
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

/* 激活项左侧青绿指示条 */
.sidebar-item-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, #2DD4BF, #0E9488);
}

.sidebar-item-active {
  background-image: linear-gradient(90deg, rgba(14, 148, 136, 0.25), rgba(255, 255, 255, 0.08));
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
