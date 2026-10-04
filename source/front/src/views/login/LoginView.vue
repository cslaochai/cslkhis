<script setup lang="js">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataLine, User, Lock, FirstAidKit, Tickets, CircleCheck, Service } from '@element-plus/icons-vue'
import { login } from '@/api/system'
import { encryptPassword } from '@/lib/password'
import { useCurrentPatientStore } from '@/stores/currentPatient'
import { clearSessionCaches } from '@/lib/session-cache'

const router = useRouter()
// 换个账号登录时清掉上一次会话的「当前患者」，否则顶部条会挂着上一个人的名字
const currentPatientStore = useCurrentPatientStore()

// 角色卡只是「快捷入口」：选中即按该角色登录（多角色账号要求卡片角色已授权）。
// 「其他」= 不指定角色，由后端取该账号角色列表中的第一个（sys_role.sort_order 最小者）。
// 默认选中「其他」——单角色账号不必选卡，多角色账号也能直接进（进系统后右上角可切换角色）。
const roles = [
  { key: 'other', label: '其他', icon: User, desc: '登录后进入账号首个角色，可在系统内切换', roleCode: '' },
  { key: 'doctor', label: '医生', icon: FirstAidKit, desc: '门诊/住院诊疗', roleCode: '10013' },
  { key: 'nurse', label: '护士', icon: DataLine, desc: '护理执行工作站', roleCode: '10014' },
  { key: 'pharmacist', label: '药剂师', icon: Tickets, desc: '审方调配发药', roleCode: '10016' },
  // 客服岗（sql/223）：接患者转人工工单 + 维护客服台语料，归客户服务中心
  { key: 'service', label: '客服', icon: Service, desc: '工单受理与患者服务', roleCode: '10034' },
  { key: 'admin', label: '系统管理员', icon: CircleCheck, desc: '系统配置管理', roleCode: '10012' },
]

const role = ref('other')
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
})

const AUTO_FILL_CAPTCHA = true
const captchaCanvas = ref(null)
let captchaCode = ''

const generateCaptcha = () => {
  // 去掉易混淆字符 I O 0 1
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  captchaCode = Array.from({ length: 4 }, () => chars.charAt(Math.floor(Math.random() * chars.length))).join('')
  if (AUTO_FILL_CAPTCHA) {
    loginForm.captcha = captchaCode
  }
  drawCaptcha()
}

const drawCaptcha = () => {
  const canvas = captchaCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const width = canvas.width
  const height = canvas.height

  // 背景
  ctx.fillStyle = '#EAF2F9'
  ctx.fillRect(0, 0, width, height)

  // 干扰线
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = `rgba(18, 105, 181, ${0.15 + Math.random() * 0.25})`
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(Math.random() * width, Math.random() * height)
    ctx.lineTo(Math.random() * width, Math.random() * height)
    ctx.stroke()
  }

  // 文字：随机颜色、轻微旋转错位
  const colors = ['#1269B5', '#0E9488', '#0B4F8A', '#5B8DB8']
  for (let i = 0; i < captchaCode.length; i++) {
    ctx.save()
    ctx.font = `bold ${20 + Math.floor(Math.random() * 5)}px Arial`
    ctx.fillStyle = colors[Math.floor(Math.random() * colors.length)]
    ctx.textBaseline = 'middle'
    ctx.translate(12 + i * 20, height / 2 + (Math.random() - 0.5) * 8)
    ctx.rotate((Math.random() - 0.5) * 0.5)
    ctx.fillText(captchaCode[i], 0, 0)
    ctx.restore()
  }

  // 噪点
  ctx.fillStyle = 'rgba(11, 79, 138, 0.35)'
  for (let i = 0; i < 30; i++) {
    ctx.fillRect(Math.random() * width, Math.random() * height, 1.5, 1.5)
  }
}

const refreshCaptcha = () => {
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

const handleLogin = async () => {
  if (!loginForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!loginForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!loginForm.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (loginForm.captcha.trim().toUpperCase() !== captchaCode) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return
  }

  // 获取选择的角色编码
  const selectedRole = roles.find(r => r.key === role.value)
  const roleCode = selectedRole ? selectedRole.roleCode : ''

  loading.value = true
  try {
    // 口令在提交前先 SM2 加密：HTTP 链路上只能看到密文。
    // 加密失败（多半是后端换了密钥）直接抛出去，不要退化成明文重试 —— 那等于白做。
    const password = await encryptPassword(loginForm.password)
    const res = await login({
      username: loginForm.username,
      password: password,
      roleCode: roleCode,
    })

    if (res.code === 200) {
      // 保存token和用户信息
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.userId)
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('realName', res.data.realName)
      localStorage.setItem('currentRole', res.data.currentRole)
      // 清掉上一个会话残留的「当前患者」（sessionStorage），避免串患者
      currentPatientStore.clearSession()
      // 菜单/权限/工作台配置是模块级进程内缓存：上一会话若没整页刷新（退出走的是 SPA 路由），
      // 不清就会把 A 角色的菜单原样画给 B（loadMenuTree 命中旧缓存，连接口都不再请求）
      clearSessionCaches()

      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message || '登录失败')
      refreshCaptcha()
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 院徽 + 院名 -->
      <div class="login-header">
        <img src="../../assets/main_logo.png" alt="长沙市麓康医院" class="hospital-logo" />
        <h1 class="hospital-name">长沙市麓康医院</h1>
        <p class="hospital-sub">CHANGSHA LUKANG HOSPITAL</p>
      </div>

      <div class="login-divider"></div>

      <!-- 角色选择 -->
      <div class="role-selector">
        <div
          v-for="r in roles"
          :key="r.key"
          class="role-item"
          :class="{ active: role === r.key }"
          @click="role = r.key"
        >
          <component :is="r.icon" class="role-icon" />
          <span class="role-label">{{ r.label }}</span>
        </div>
      </div>

      <!-- 登录表单 -->
      <el-form :model="loginForm" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名/工号"
            :prefix-icon="User"
            size="large"
            class="login-input"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            class="login-input"
          />
        </el-form-item>
        <el-form-item>
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              :prefix-icon="CircleCheck"
              size="large"
              maxlength="4"
              class="captcha-input"
            />
            <canvas
              ref="captchaCanvas"
              width="100"
              height="40"
              class="captcha-image"
              title="点击刷新验证码"
              @click="refreshCaptcha"
            ></canvas>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <p class="role-desc">{{ roles.find((r) => r.key === role)?.desc }}</p>
    </div>

    <p class="login-footer">长沙市麓康医院信息管理平台 · 仅限授权人员使用</p>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #EAF2F9 0%, #DDEAF4 100%);
  padding: 24px;
}

.login-card {
  width: 440px;
  background: #fff;
  border: 1px solid #E3ECF5;
  border-radius: 14px;
  box-shadow: 0 6px 24px rgba(11, 79, 138, 0.08);
  padding: 40px 40px 28px;
}

.login-header {
  text-align: center;
}

.hospital-logo {
  width: 88px;
  height: 88px;
  object-contain: contain;
  margin: 0 auto 12px;
  display: block;
}

.hospital-name {
  font-size: 24px;
  font-weight: 700;
  color: #0B4F8A;
  letter-spacing: 4px;
  margin: 0;
}

.hospital-sub {
  font-size: 12px;
  color: #8CA6BC;
  letter-spacing: 2px;
  margin: 6px 0 0;
}

.login-divider {
  height: 1px;
  background: #E9F0F7;
  margin: 24px 0 20px;
}

.role-selector {
  display: flex;
  gap: 6px;
  margin-bottom: 20px;
}

.role-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 10px 2px;
  border: 1px solid #E3ECF5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
  background: #FAFCFE;
  white-space: nowrap;
}

.role-item:hover {
  border-color: #7FB2DF;
  background: #F0F7FD;
}

.role-item.active {
  border-color: var(--his-primary, #1269B5);
  background: #E8F1FA;
  color: #1269B5;
}

.role-icon {
  width: 20px;
  height: 20px;
}

.role-label {
  font-size: 12px;
  font-weight: 500;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 100px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #E3ECF5;
  cursor: pointer;
  flex-shrink: 0;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 6px;
  border-radius: 8px;
}

.role-desc {
  text-align: center;
  font-size: 12px;
  color: #9CAFBF;
  margin: 4px 0 0;
}

.login-footer {
  margin-top: 24px;
  font-size: 12px;
  color: #9AAFC2;
  letter-spacing: 1px;
}
</style>
