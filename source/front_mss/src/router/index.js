import { createRouter, createWebHistory } from 'vue-router'

const routes = [
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
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  const t = to.meta?.title
  if (t) document.title = `${t} - 医保结算经办平台`
})

export default router
