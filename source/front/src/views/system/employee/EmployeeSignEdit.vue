<template>
  <!-- 签名笔迹：只维护该员工的手写签名图样（base64 PNG，处方/报告视觉落款），CA 数字证书另走只读弹框 -->
  <el-dialog
      v-model="visible"
      title="签名笔迹维护"
      width="640px"
      :destroy-on-close="true"
      @close="visible = false"
  >
    <div class="mb-2 flex items-center gap-3">
      <span class="text-xs text-slate-500">
        {{ signImageSaved ? '已维护签名笔迹，可直接在板上重签更新' : '尚未维护签名笔迹' }}
      </span>
      <el-button v-if="signImageSaved" link type="primary" @click="clearSignImage">清除笔迹</el-button>
    </div>
    <SignaturePad ref="signPadRef" :width="440" :height="150"/>
    <template #footer>
      <el-button v-perm="'org:employee:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, nextTick, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import SignaturePad from '@/components/his/SignaturePad.vue'
import {getEmployeeDetail, saveEmployeeSign} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 操作列传入当前行；需带 id
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const signPadRef = ref(null)
const signImageSaved = ref(false)
const submitLoading = ref(false)

const clearSignImage = () => {
  signPadRef.value?.clear()
  signImageSaved.value = false
}

watch(visible, async (open) => {
  if (!open) return
  signImageSaved.value = false
  await nextTick()
  signPadRef.value?.clear()
  if (!props.row?.id) return
  try {
    const res = await getEmployeeDetail(props.row.id)
    if (res.code === 200 && res.data && res.data.signImage) {
      signImageSaved.value = true
      signPadRef.value?.setImage(res.data.signImage)
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
})

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    // 非空 = 维护/更新笔迹；空 = 清除
    const signData = signPadRef.value?.confirm() || ''
    const res = await saveEmployeeSign({id: props.row.id, signImage: signData})
    if (res.code === 200) {
      ElMessage.success('签名保存成功')
      visible.value = false
      emit('saved')
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  } finally {
    submitLoading.value = false
  }
}
</script>
