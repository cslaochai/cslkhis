import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'
import App from './App.vue'
import router from './router'
import { ensurePerm, onPermsChange, permMatched } from './lib/perm'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// v-perm 按钮级权限指令：等权限集合到手后，无权限的节点直接移出文档。
// 用法 v-perm="'system:user:add'" 或 v-perm="['a:add','a:edit']"（任一）。
// 为什么留一个注释占位符而不是干脆删掉：权限集合是异步的，删掉的按钮要能恢复，
// 用占位符 + 订阅集合变化，删掉/恢复都能跟着新角色走。
app.directive('perm', {
  mounted(el, binding) {
    const state = { need: binding.value, stop: null }
    el.__vPerm = state
    if (!state.need) return // 没传码 = 不收敛（防误删）

    const marker = document.createComment('v-perm')
    const apply = (ok) => {
      if (!ok && el.parentNode) {
        el.parentNode.insertBefore(marker, el)
        el.parentNode.removeChild(el)
      } else if (ok && marker.parentNode) {
        marker.parentNode.insertBefore(el, marker)
        marker.parentNode.removeChild(marker)
      }
    }
    state.refresh = () => apply(permMatched(state.need))
    ensurePerm(state.need).then((ok) => {
      apply(ok)
      state.stop = onPermsChange(state.refresh)
    })
  },
  updated(el, binding) {
    const state = el.__vPerm
    if (!state?.need) return
    state.need = binding.value
    state.refresh?.()
  },
  unmounted(el) {
    el.__vPerm?.stop?.()
    el.__vPerm = null
  },
})

app.mount('#app')
