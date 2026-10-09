<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import {sidebarCollapsed} from '@/lib/sidebarState'
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <!-- 顶部 Header（包含 logo、企业名、面包屑、搜索等） -->
    <Header />
    <!-- 下方区域 -->
    <!-- 高度取自 --his-header-h，与 style.css 里 --his-page-h 的扣减口径同源 -->
    <div class="flex" style="height: calc(100vh - var(--his-header-h));">
      <!-- 左侧导航栏（伸缩状态在 lib/sidebarState，Header 按钮与侧栏底部箭头共用） -->
      <Sidebar />
      <!-- 右侧主内容 -->
      <div
        class="flex-1 overflow-auto transition-all duration-300"
        :class="sidebarCollapsed ? 'ml-16' : 'ml-64'"
      >
        <main class="p-3">
          <router-view />
        </main>
      </div>
    </div>
  </div>
</template>
