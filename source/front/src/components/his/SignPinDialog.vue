<template>
  <!-- 签名 PIN 输入（软签二次确认）；口令还是系统下发的初始值时，先换成自己的再签名 -->
  <el-dialog v-model="visible" :title="title" width="420px" @close="onClose">
    <el-form v-if="mustChange" @submit.prevent>
      <p class="mb-3 text-sm text-amber-600">
        这张证书用的还是系统下发的初始签名 PIN，不能直接用于签名，请先改成自己的 PIN。
      </p>
      <el-form-item label="当前PIN" required>
        <el-input v-model="oldPin" autocomplete="new-password" data-testid="sign-pin-old" maxlength="32"
                  placeholder="请输入当前签名 PIN" show-password type="password">
          <template #prefix>
            <el-icon><Lock/></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="新PIN" required>
        <el-input v-model="newPin" autocomplete="new-password" data-testid="sign-pin-new" maxlength="32"
                  placeholder="4-32 位，请设为您本人知道的口令" show-password type="password">
          <template #prefix>
            <el-icon><Lock/></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="确认新PIN" required>
        <el-input v-model="confirmPin" autocomplete="new-password" data-testid="sign-pin-confirm-input" maxlength="32"
                  placeholder="再次输入新签名 PIN" show-password type="password" @keyup.enter="confirmChange">
          <template #prefix>
            <el-icon><Lock/></el-icon>
          </template>
        </el-input>
      </el-form-item>
    </el-form>
    <el-form v-else @submit.prevent>
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
      <el-button :disabled="checking" :loading="loading" data-testid="sign-pin-confirm" type="primary"
                 @click="mustChange ? confirmChange() : confirm()">
        {{ mustChange ? '修改并签名' : '确认签名' }}
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
 *
 * 打开时先做一次本人签名预检：证书口令仍是建号下发的初始口令就换成「改 PIN」形态，
 * 改成功后把新口令交给调用方继续签名——所有接了本组件的签名动作因此共用同一个改口令入口。
 */
import {computed, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {changeSignPin, getSignPreCheck} from '@/api/signature'

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
const oldPin = ref('')
const newPin = ref('')
const confirmPin = ref('')
const mustChange = ref(false)
const checking = ref(false)
const loading = ref(false)

// 外部 await 的 promise：确认时 resolve PIN，取消时 resolve null
let resolver = null

/**
 * 打开 PIN 输入框并等待确认。
 * @returns {Promise<string|null>} 确认返回 PIN 文本；取消返回 null
 */
const askPin = () => {
  pin.value = ''
  oldPin.value = ''
  newPin.value = ''
  confirmPin.value = ''
  mustChange.value = false
  visible.value = true
  const waiting = new Promise((resolve) => {
    resolver = resolve
  })
  loadPinMustChange()
  return waiting
}

/**
 * 预检这张证书的口令是否还是初始值；预检失败就按普通 PIN 输入处理（服务端仍会拒签）。
 */
const loadPinMustChange = async () => {
  checking.value = true
  try {
    const res = await getSignPreCheck()
    mustChange.value = (res.data || {}).pinMustChange === true
  } catch (error) {
    console.error('签名预检失败，本次按普通签名 PIN 输入处理', error)
  } finally {
    checking.value = false
  }
}

const finish = (value) => {
  const done = resolver
  resolver = null
  visible.value = false
  loading.value = false
  emit('confirm', value)
  if (done) done(value)
}

const confirm = () => {
  if (!pin.value) {
    ElMessage.warning('请输入签名 PIN')
    return
  }
  loading.value = true
  finish(pin.value)
}

const confirmChange = async () => {
  if (!oldPin.value || !newPin.value) {
    ElMessage.warning('请输入当前签名 PIN 与新签名 PIN')
    return
  }
  if (newPin.value !== confirmPin.value) {
    ElMessage.warning('两次输入的新签名 PIN 不一致')
    return
  }
  loading.value = true
  try {
    await changeSignPin({oldPin: oldPin.value, newPin: newPin.value})
  } catch (error) {
    // 全局拦截器已提示原因，这里只把弹框留着让人改
    console.error('修改签名 PIN 失败', error)
    loading.value = false
    return
  }
  finish(newPin.value)
}

const onClose = () => {
  // 取消（点右上角或遮罩）：resolve null，调用方据此中止签名
  const done = resolver
  resolver = null
  if (done) done(null)
}

defineExpose({askPin})
</script>
