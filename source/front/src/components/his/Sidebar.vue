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

/**
 * 伸缩状态不再由父层透传：Header 顶栏的按钮和这里底部的箭头读写同一份
 * lib/sidebarState，两个入口永远不会各说各话。
 */

const route = useRoute()
const router = useRouter()

/**
 * 菜单数据源 = 后端 sys_menu 表（`/system/menu/tree`）。
 *
 * 这里**不再写死任何菜单结构**：「系统管理 → 菜单管理」里改完立即生效。
 * 表结构与初始化数据见 `source/back_end/sql/52-菜单表重建.sql`。
 *
 * 约定：
 *  1. 一级目录（menu_type=1）→ 一个可折叠分组；二级菜单（menu_type=2）→ 一个真实页面。
 *  2. 只渲染 is_visible=1 且 status=1 的记录；is_visible=0 用于「路由存在但不上菜单」的页面。
 *  3. 排序沿用 sort_order（目录 10/20/… 步长，便于中间插入）。
 *  4. 菜单项的 path 必须在 `router/index.js` 注册过，否则点进去是白屏 —— 由
 *     `workspace/verify-menu-rebuild.mjs` 的「契约层」断言兜住。
 *
 * 【已下线的假壳菜单 —— 原样保留，不删】
 * 以下假壳页已全部真实化或删除，仓库不再保留演示壳组件，
 * 但条目留成注释、字段完整，等页面接了真实接口后，照着在 sys_menu 里补一行即可挂回菜单：
 *   // {href: '/emr',          label: '电子病历 EMR',   icon: DocumentCopy}  // 唯一是假壳
 *   // {href: '/records',      label: '门诊病历查询',   icon: Document}      // 非假壳，但 2026-09-23 已摘除菜单：
 *   //                                                                       // 与 602 同表同接口同检索、信息量更少，改用 /medical-record（见 sql/74）
 *   // {href: '/anesthesia',   label: '麻醉信息系统',   icon: Odometer}      // 住院业务
 *   // {href: '/icu',          label: 'ICU 重症监护',   icon: TrendCharts}   // 住院业务
 *   // {href: '/infusion',     label: '输液 / 治疗室',  icon: Pouring}       // 门诊业务
 *   // {href: '/infection',    label: '院感监控',       icon: DataBoard}     // 病历与质量安全
 *   // {href: '/adverse-event', label: '不良事件',       icon: Flag}          // 病历与质量安全
 *   // {href: '/equipment',    label: '设备管理',       icon: Tools}         // 物资设备
 *   // {href: '/cssd',         label: '消毒供应 CSSD',  icon: RefreshRight}  // 物资设备
 *   // {href: '/audit',        label: '审计日志',       icon: FolderOpened}  // 报表统计（原「报表与审计」，sql/188）
 *   // {href: '/billing',      label: '收费管理',       icon: Money}         // 财务结算（非假壳，见下）
 * 另有 4 个假壳页 /pacs /hr /performance /dictionary 原本就没挂菜单，同样保留。
 * （原本这个列表里还有 /health-record —— 它已重做成真页面并挂上菜单，
 *   见 sys_menu.menu_key='patient.profile' 与 sql/61-健康档案菜单.sql。）
 * （/finance「财务日结」同理：已重做成真实页面 FinanceView.vue（班结/日结/三级对账），
 *   菜单见 sql/76-财务班结日结与科室锚点.sql 的 menu_key='finance.settlement'。）
 *
 * 【/billing「收费管理」是另一种情况】它与上面的假壳不同 —— 页面是真的（BillingView.vue，
 * 调 /charge/listPage + /charge/getById），但作为「收费查询」它是 /cashier 的坏子集：
 *   ① 把 chargeNo 当 chargeId 传给 /charge/getById → 详情必然 400，且 catch 静默降级，
 *      弹窗里只剩 4 个字段、一条明细都没有（用户会以为这单本来就没明细）
 *   ② 统计卡是前端 reduce 且数据源写死 pageSize:100 → 394 条里只有前 100 条参与，数字是错的
 *   ③ filtered 对当前页 list.filter（前端切片）→ 违反「筛选下推后端」，且无分页
 * 所以一并从菜单与路由摘掉。页面文件保留未删，待重做成 /cashier 的查询 tab 后再放开。
 */
// 组件对象禁止进深层 ref：会被 reactive 代理，触发「Component made a reactive object」警告。
// navGroups 只有整体赋值（loadMenus），从不原地改，shallowRef 正合适。
const navGroups = shallowRef<NavGroup[]>([])

/** 加载状态：区分「还在加载」与「真的没有」——加载期间不能显示空态文案 */
const loading = ref(true)
/** 加载失败提示（后端不可达 / 非 200），失败时给重试入口，不静默 */
const loadError = ref('')

/** 图标名 → 组件。sys_menu.icon 存组件名字符串，取不到时用 FallbackIcon 兜底 */
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
 *
 * 为什么必须有这个 watch：侧边栏是**常驻布局**的一部分，`setup()` 只跑一次。
 * 切角色时 Header 会 `clearMenuTreeCache()`，但那只是清了模块里的缓存变量 ——
 * Sidebar 对此一无所知，`navGroups` 还挂着旧角色那一套。
 * 路由变化（router.replace）也不会让它重建。
 * 结果就是「切了角色菜单没变，得手工刷新页面」。
 *
 * `menuCacheEpoch` 是缓存失效的响应式信号，自增一次这里就重跑一次。
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
        <!--
          分组标题（可点击折叠）：比子菜单大一号、更亮，与子项拉开层级。
          收起态**不换 DOM 分支**（原来是 `pt-2 pb-1` 的单字 <p>，比展开态矮 4px，
          切换瞬间整列先跳高度、再随宽度重新排 → 就是「先加高后平铺」那个观感）。
          现在同一行同高度，靠三件事过渡：
            · 左内边距 12→17（配合 14px 的字宽，把首字推到 64px 图标条的中轴上）；
            · 标题 max-width 收到 14px —— 注意 overflow 是在 padding box 裁的，
              光靠行宽会露出两个字（半个字被切很难看），必须自己限宽；
            · 箭头淡出（opacity，不动布局，所以不会引起回流）。
        -->
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
            <!--
              菜单项：同理不换 DOM。左内边距 36→15 随宽度同速过渡
              （图标中心 = 8 + 15 + 9 = 32，正好落在 64px 图标条的中轴上，
              与顶栏 Logo、底部箭头同一条竖线）；文字用淡出，不用 v-if 摘除。
            -->
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
/* 收起 / 展开动画：padding 与 aside 的宽度同速（300ms）过渡，图标才会「滑」到中轴，
   而不是先跳过去再等宽度追上来；颜色单独给 150ms，免得 hover 高亮拖泥带水。 */
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
