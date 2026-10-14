<template>
  <el-container class="h-screen">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="!bg-[#0B3B66] text-white flex flex-col">
      <div class="h-16 flex items-center gap-2 px-4 border-b border-white/10">
        <el-icon :size="26"><FirstAidKit /></el-icon>
        <div class="leading-tight">
          <div class="text-[15px] font-bold tracking-wide">医保结算经办</div>
          <div class="text-[11px] text-white/60">Medical Insurance Settlement</div>
        </div>
      </div>
      <el-scrollbar class="flex-1">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="transparent"
          text-color="rgba(255,255,255,0.75)"
          active-text-color="#ffffff"
          class="!border-none"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon><span>经办工作台</span>
          </el-menu-item>

          <el-sub-menu index="settlement">
            <template #title>
              <el-icon><Money /></el-icon><span>医保结算</span>
            </template>
            <el-menu-item index="/settlement/outpatient">门诊医保结算</el-menu-item>
            <el-menu-item index="/settlement/inpatient">住院医保结算</el-menu-item>
            <el-menu-item index="/settlement/bill">结算单管理</el-menu-item>
            <el-menu-item index="/settlement/gateway">机构报盘接收</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/audit/fee">
            <el-icon><Stamp /></el-icon><span>费用审核</span>
          </el-menu-item>

          <el-menu-item index="/catalog">
            <el-icon><Notebook /></el-icon><span>三大目录与对照</span>
          </el-menu-item>

          <el-menu-item index="/filing/cross-region">
            <el-icon><Promotion /></el-icon><span>异地就医备案</span>
          </el-menu-item>

          <el-menu-item index="/reconciliation">
            <el-icon><DataAnalysis /></el-icon><span>清算对账</span>
          </el-menu-item>

          <el-menu-item index="/stats">
            <el-icon><TrendCharts /></el-icon><span>统计报表</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
      <div class="px-4 py-3 text-[11px] text-white/50 border-t border-white/10">
        经办点：长沙市麓康医院<br />定点机构编码 P43010500001
      </div>
    </el-aside>

    <!-- 主区 -->
    <el-container>
      <el-header class="!h-16 !bg-white flex items-center justify-between px-6 shadow-sm">
        <div class="flex items-center gap-3">
          <span class="text-[16px] font-semibold text-[#0B3B66]">{{ pageTitle }}</span>
          <el-tag size="small" type="info" effect="plain">{{ today }}</el-tag>
        </div>
        <div class="flex items-center gap-4">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" type="danger">
            <el-icon :size="20" class="text-gray-500"><Bell /></el-icon>
          </el-badge>
          <el-divider direction="vertical" />
          <el-avatar :size="32" style="background:#1269B5">湘</el-avatar>
          <div class="leading-tight">
            <div class="text-[13px] font-medium">医保经办员</div>
            <div class="text-[11px] text-gray-400">湘医保 · 结算岗</div>
          </div>
        </div>
      </el-header>

      <el-main class="!bg-[#F2F7FB] !p-4">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '医保结算经办平台')

const now = new Date()
const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`

// 顶部待办角标：从工作台汇总接口读取
import { dashboardSummary } from '@/api/insurance'
import { ref, onMounted } from 'vue'
const pendingCount = ref(0)
onMounted(async () => {
  const res = await dashboardSummary()
  pendingCount.value = (res.data.todo?.total || 0)
})
</script>
