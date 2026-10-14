import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '经办工作台' },
      },
      {
        path: 'settlement/outpatient',
        name: 'OutpatientSettle',
        component: () => import('@/views/settlement/OutpatientSettleView.vue'),
        meta: { title: '门诊医保结算' },
      },
      {
        path: 'settlement/inpatient',
        name: 'InpatientSettle',
        component: () => import('@/views/settlement/InpatientSettleView.vue'),
        meta: { title: '住院医保结算' },
      },
      {
        path: 'settlement/bill',
        name: 'SettleBill',
        component: () => import('@/views/settlement/SettleBillView.vue'),
        meta: { title: '结算单管理' },
      },
      {
        path: 'settlement/gateway',
        name: 'GatewaySettle',
        component: () => import('@/views/settlement/GatewaySettleView.vue'),
        meta: { title: '机构报盘接收' },
      },
      {
        path: 'settlement/monthly',
        name: 'MonthlySettle',
        component: () => import('@/views/settlement/MonthlySettleView.vue'),
        meta: { title: '月度结算管理' },
      },
      {
        path: 'insured/person',
        name: 'InsuredPerson',
        component: () => import('@/views/insured/InsuredPersonView.vue'),
        meta: { title: '参保人管理' },
      },
      {
        path: 'audit/fee',
        name: 'FeeAudit',
        component: () => import('@/views/audit/FeeAuditView.vue'),
        meta: { title: '费用审核' },
      },
      {
        path: 'catalog',
        name: 'Catalog',
        component: () => import('@/views/catalog/CatalogView.vue'),
        meta: { title: '三大目录与对照' },
      },
      {
        path: 'filing/cross-region',
        name: 'CrossRegionFiling',
        component: () => import('@/views/filing/CrossRegionFilingView.vue'),
        meta: { title: '异地就医备案' },
      },
      {
        path: 'reconciliation',
        name: 'Reconciliation',
        component: () => import('@/views/reconciliation/ReconciliationView.vue'),
        meta: { title: '清算对账' },
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('@/views/stats/StatsView.vue'),
        meta: { title: '统计报表' },
      },
      // ---------------- 基金监管 ----------------
      {
        path: 'supervise/dashboard',
        name: 'SuperviseDashboard',
        component: () => import('@/views/supervise/SuperviseDashboard.vue'),
        meta: { title: '监管工作台' },
      },
      {
        path: 'supervise/rule',
        name: 'SuperviseRule',
        component: () => import('@/views/supervise/RuleView.vue'),
        meta: { title: '规则配置' },
      },
      {
        path: 'supervise/suspicion',
        name: 'SuperviseSuspicion',
        component: () => import('@/views/supervise/SuspicionView.vue'),
        meta: { title: '疑点稽核' },
      },
      {
        path: 'supervise/inspection',
        name: 'SuperviseInspection',
        component: () => import('@/views/supervise/InspectionView.vue'),
        meta: { title: '飞检任务' },
      },
      {
        path: 'supervise/recover',
        name: 'SuperviseRecover',
        component: () => import('@/views/supervise/RecoverView.vue'),
        meta: { title: '违规追回' },
      },
      // ---------------- 系统管理 ----------------
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/UserView.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/RoleView.vue'),
        meta: { title: '角色管理' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/MenuView.vue'),
        meta: { title: '菜单管理' },
      },
      {
        path: 'system/post',
        name: 'SystemPost',
        component: () => import('@/views/system/PostView.vue'),
        meta: { title: '岗位管理' },
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/DictView.vue'),
        meta: { title: '字典管理' },
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/DeptView.vue'),
        meta: { title: '科室管理' },
      },
      {
        path: 'system/employee',
        name: 'SystemEmployee',
        component: () => import('@/views/system/EmployeeView.vue'),
        meta: { title: '员工管理' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 登录守卫：无 token 一律去登录页；已登录访问登录页回首页
router.beforeEach((to) => {
  const auth = useAuthStore()
  const token = auth.token || localStorage.getItem('token')
  if (to.path === '/login') {
    return token ? '/' : true
  }
  if (!token) {
    return { path: '/login', replace: true }
  }
  return true
})

router.afterEach((to) => {
  const t = to.meta?.title
  if (t) document.title = `${t} - 医保结算经办平台`
})

export default router
