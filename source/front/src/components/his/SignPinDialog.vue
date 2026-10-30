<template>
  <!-- 签名 PIN 输入（软签二次确认） -->
  <el-dialog v-model="visible" :title="title" width="420px" @close="onClose">
    <el-alert class="mb-3" :closable="false" show-icon type="info"
              title="软签确认：每次签名都要由本人输入 PIN，校验通过才会解密私钥完成签名。"/>
    <el-form @submit.prevent>
      <el-form-item label="签名PIN" required>
        <el-input v-model="pin" autocomplete="new-password" data-testid="sign-pin-input" maxlength="32" placeholder="请输入签名 PIN"
                  show-password type="password" @keyup.enter="confirm">
          <template #prefix>
            <el-icon><Lock/></el-icon>
          </template>
        </el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="loading" data-testid="sign-pin-confirm" type="primary" @click="confirm">确认签名
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 签名 PIN 输入框（软签二次确认）。
 *
 * 没有 UKey 硬件时，用「签名人本人输入的 PIN」代替芯片内的 PIN 保护：
 * 服务端只存 PIN 的 PBKDF2 哈希，校验通过才解密私钥。
 *
 * 用法：业务页面在真正发起签名前，先 await askPin() 拿到 PIN，再随请求提交；
 * PIN 为空或错误时服务端会拒绝签名（不会静默降级为免密签名）。
 */
import {computed, ref} from 'vue'
import {ElMessage} from 'element-plus'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  title: {type: String, default: '签名确认'},
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const pin = ref('')
const loading = ref(false)

// 外部 await 的 promise：确认时 resolve PIN，取消时 resolve null
let resolver = null

/**
 * 打开 PIN 输入框并等待确认。
 * @returns {Promise<string|null>} 确认返回 PIN 文本；取消返回 null
 */
const askPin = () => {
  pin.value = ''
  visible.value = true
  return new Promise((resolve) => {
    resolver = resolve
  })
}

const confirm = () => {
  if (!pin.value) {
    ElMessage.warning('请输入签名 PIN')
    return
  }
  loading.value = true
  const value = pin.value
  const done = resolver
  resolver = null
  visible.value = false
  loading.value = false
  emit('confirm', value)
  if (done) done(value)
}

const onClose = () => {
  // 取消（点右上角或遮罩）：resolve null，调用方据此中止签名
  const done = resolver
  resolver = null
  if (done) done(null)
}

defineExpose({askPin})
</script>
