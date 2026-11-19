import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus, { ElMessage } from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import './style.css';
import App from './App.vue';
import router from './router';
import { installRouteGuard } from './router/guard';
import { ensurePerm, onPermsChange, permMatched } from './lib/perm';

const app = createApp(App);
app.use(createPinia());
app.use(router);

// 导航失败兜底：必须在 mount 之前注册。
// 起因：路由组件是 `() => import(...)` 动态导入，开发期改文件触发 HMR、依赖被重新预构建、
// 发版后旧的 chunk 失效，都会让 import 直接 reject。vue-router 对这类失败只打一行诊断
// （VUE_ROUTER_R0010 / R0011），不渲染任何东西 —— 现象就是整页白屏，且刷新前一直白着。
// 策略：10 秒窗口内只自动刷新一次（旧 chunk 换新即可恢复）；再次失败说明不是陈旧缓存，
// 改为明确提示，绝不无限刷新。
const CHUNK_RELOAD_KEY = 'router:chunkReloadAt';
router.onError((err, to) => {
    console.error('[router] 导航失败', to?.fullPath ?? '', err);
    const message = String(err?.message || err || '');
    const chunkFailed = /Failed to fetch dynamically imported module|error loading dynamically imported module|Importing a module script failed|Outdated Optimize Dep/i.test(message);
    if (!chunkFailed) return;
    const lastAt = Number(sessionStorage.getItem(CHUNK_RELOAD_KEY) || 0);
    if (Date.now() - lastAt > 10000) {
        sessionStorage.setItem(CHUNK_RELOAD_KEY, String(Date.now()));
        window.location.reload();
        return;
    }
    ElMessage.error('页面资源加载失败，请手动刷新浏览器');
});

app.use(ElementPlus, { locale: zhCn });
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);

}

// 全局路由守卫：按当前角色可见菜单收口「能进哪些页面」。
// 必须在 app.use(router) 之后注册 —— 守卫依赖 router.getRoutes() 已就绪（推导工作台要用 meta）。
// 提示回调由这里注入，避免 lib/router 层直接依赖 UI 库。
installRouteGuard(router, (msg) => ElMessage.warning(msg));

// v-perm 按钮级权限指令:等权限集合到手后,无权限的节点直接移出文档。
// 用法 v-perm="'opd:appointments:add'" 或 v-perm="['a:add','a:edit']"(任一)。
// 码必须取自 sys_menu 的 menu_type=3 按钮菜单(与「角色管理→菜单权限」同一份数据)。
//
// 为什么留一个注释占位符而不是干脆删掉:权限集合是**异步**的,而切角色时 Header 只
// router.replace 到落地页 —— 落地页与当前页相同(如医生切医生)时组件实例不重建,
// 真删掉的按钮就再也回不来了。用占位符 + 订阅集合变化,删掉/恢复都能跟着新角色走。
//
// tab/区块级别的显隐建议用 v-if="hasPerm(...)"(响应式,集合一到就重渲染;
// 指令移除节点对 el-tabs 子组件不友好)。
app.directive('perm', {
    mounted(el, binding) {
        const state = { need: binding.value, stop: null };
        el.__vPerm = state;
        if (!state.need) return; // 没传码 = 不收敛（防误删）

        const marker = document.createComment('v-perm');
        const apply = (ok) => {
            if (!ok && el.parentNode) {
                el.parentNode.insertBefore(marker, el);
                el.parentNode.removeChild(el);
            } else if (ok && marker.parentNode) {
                marker.parentNode.insertBefore(el, marker);
                marker.parentNode.removeChild(marker);
            }
        };
        state.refresh = () => apply(permMatched(state.need));
        ensurePerm(state.need).then((ok) => {
            apply(ok);
            state.stop = onPermsChange(state.refresh);
        });
    },
    updated(el, binding) {
        const state = el.__vPerm;
        if (!state?.need) return;
        state.need = binding.value;
        state.refresh?.();
    },
    unmounted(el) {
        el.__vPerm?.stop?.();
        el.__vPerm = null;
    },
});

app.mount('#app');
