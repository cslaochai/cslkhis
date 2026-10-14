<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon"><el-icon :size="30"><FirstAidKit /></el-icon></div>
        <div class="brand-title">医保结算经办平台</div>
        <div class="brand-sub">Medical Insurance Settlement Platform</div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>

      <div class="login-tip">
        演示账号：admin / admin123（经办管理员）<br />
        yb001 / 123456（经办员）· yb002 / 123456（审核员）
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    ElMessage.success('登录成功')
    router.replace('/')
  } catch (e) {
    // 登录失败提示由后端 message 决定（登录接口不弹全局提示，这里内联展示）
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0b3b66 0%, #1269b5 55%, #0e9488 120%);
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 14px;
  padding: 40px 36px 28px;
  box-shadow: 0 16px 48px rgba(11, 59, 102, 0.35);
}
.login-brand {
  text-align: center;
  margin-bottom: 30px;
}
.brand-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 12px;
  border-radius: 16px;
  background: linear-gradient(135deg, #1269b5, #0e9488);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: #0b3b66;
  letter-spacing: 1px;
}
.brand-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #8aa0b8;
  letter-spacing: 0.4px;
}
.login-btn {
  width: 100%;
  margin-top: 4px;
}
.login-tip {
  margin-top: 22px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f2f7fb;
  color: #5c7a99;
  font-size: 12px;
  line-height: 1.8;
  text-align: center;
}
</style>
