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
          <template v-for="menu in auth.menus" :key="menu.id">
            <el-sub-menu v-if="menu.menuType === 1" :index="String(menu.id)">
              <template #title>
                <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
                <span>{{ menu.menuName }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children || []"
                :key="child.id"
                :index="child.path || '/'"
              >
                <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
                <span>{{ child.menuName }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path || '/'">
              <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
              <span>{{ menu.menuName }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
      <div class="px-4 py-3 text-[11px] text-white/50 border-t border-white/10">
        经办点：长沙市麓康医院<br />定点机构编码 H4301000001
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
          <el-dropdown trigger="click" @command="onCommand">
            <div class="flex items-center gap-2 cursor-pointer select-none">
              <el-avatar :size="32" style="background:#1269B5">{{ avatarChar }}</el-avatar>
              <div class="leading-tight">
                <div class="text-[13px] font-medium">{{ auth.realName }}</div>
                <div class="text-[11px] text-gray-400">{{ auth.deptName }} · {{ auth.roleName }}</div>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="!bg-[#F2F7FB] !p-4">
        <router-view />
      </el-main>
    </el-container>

    <!-- 修改密码 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px" append-to-body>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="onChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { changePasswordApi } from '@/api/auth'
import { dashboardSummary } from '@/api/insurance'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '医保结算经办平台')
const avatarChar = computed(() => (auth.realName || '用').slice(0, 1))

const now = new Date()
const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`

// 顶部待办角标：从工作台汇总接口读取
const pendingCount = ref(0)
onMounted(async () => {
  try {
    const res = await dashboardSummary()
    pendingCount.value = res.data.todo?.total || 0
  } catch (e) {
    // 工作台接口失败不阻塞布局
  }
})

async function onCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    await auth.logout()
    router.replace('/login')
  } else if (cmd === 'password') {
    pwdForm.oldPassword = pwdForm.newPassword = pwdForm.confirmPassword = ''
    pwdVisible.value = true
  }
}

// 修改密码
const pwdVisible = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref()
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) =>
        value === pwdForm.value.newPassword ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur',
    },
  ],
}
async function onChangePassword() {
  await pwdFormRef.value.validate()
  pwdSaving.value = true
  try {
    await changePasswordApi({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword,
    })
    ElMessage.success('密码修改成功')
    pwdVisible.value = false
  } catch (e) {
    // 后端 message 已全局提示
  } finally {
    pwdSaving.value = false
  }
}
</script>
