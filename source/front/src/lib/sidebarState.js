import {ref} from 'vue'

/**
 * 侧边栏伸缩状态：Header 的顶栏按钮与 Sidebar 共用同一份开关。
 * 放在模块级而不是 DashboardLayout 的 ref，是因为 Header 不接收 props，
 * 再往下钻一层 prop 不如两个组件直接读写同一个 ref。
 */
export const sidebarCollapsed = ref(false)

export const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
