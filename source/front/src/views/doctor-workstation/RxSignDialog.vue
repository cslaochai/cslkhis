<template>
  <!-- 结诊开方医师手写签名弹框：电子证书签名负责验签留痕，本板只采视觉笔迹落到处方笺。
       打开时自动引用当前登录医师在员工档案预维护的签名笔迹，无需每次手写；也可清空重签。 -->
  <el-dialog
      v-model="visible"
      data-testid="rx-sign-dialog"
      :title="`开方医师签名（${doctorName}）`"
      width="520px"
      destroy-on-close
      @closed="onClosed"
  >
    <p class="mb-3 text-xs text-slate-500">
      本次 {{ prescriptionCount }} 张处方共用本次签名笔迹；电子签名由系统在结诊时自动完成并可验签，本签名仅作处方笺视觉落款。
    </p>
    <div v-if="archiveSignLoaded" class="mb-2 flex items-center gap-2">
      <el-tag data-testid="rx-sign-archive-tag" effect="plain" size="small" type="success">
        {{ archiveSignImage ? '已引用员工档案签名笔迹，可直接确认或重签' : '档案未维护签名笔迹，请手写签名' }}
      </el-tag>
    </div>
    <SignaturePad ref="signPad" :width="440" :height="150"/>
    <template #footer>
      <el-button data-testid="rx-sign-cancel" @click="onCancel">取消</el-button>
      <el-button data-testid="rx-sign-confirm" type="primary" :loading="saving" @click="onConfirm">签署并完成结诊</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, nextTick, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import SignaturePad from '@/components/his/SignaturePad.vue';
import {getEmployeeCurrent} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  doctorName: {type: String, default: ''},
  prescriptionCount: {type: Number, default: 0},
});

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const signPad = ref(null);
const saving = ref(false);
// 员工档案预维护的签名笔迹（base64 PNG）：打开弹框时拉取，有则预填画板
const archiveSignImage = ref('');
const archiveSignLoaded = ref(false);

// 打开弹框时引用当前登录医师档案笔迹（employeeId 后端从 token 取）
watch(visible, async (open) => {
  if (!open) return
  archiveSignLoaded.value = false
  archiveSignImage.value = ''
  try {
    const res = await getEmployeeCurrent()
    if (res.code === 200 && res.data?.signImage) {
      archiveSignImage.value = res.data.signImage
    }
  } catch (error) /* 错误已由全局拦截器统一提示，签名仍可手写 */ {
  }
  archiveSignLoaded.value = true
  nextTick(() => {
    if (archiveSignImage.value) {
      signPad.value?.setImage(archiveSignImage.value)
    }
  })
});

const onConfirm = async () => {
  if (!signPad.value) return
  // 画板有内容（手写或预填的档案笔迹）→ 正常确认
  if (!signPad.value.isEmpty()) {
    saving.value = true;
    emit('confirm', signPad.value.confirm());
    return
  }
  // 画板为空：档案笔迹还在加载（毫秒级窗口）→ 提示稍候；已加载完成仍为空 → 未签名
  if (archiveSignImage.value && !archiveSignLoaded.value) {
    ElMessage.warning('签名笔迹加载中，请稍候再点');
    return
  }
  ElMessage.warning('请医师在签名板上签名');
};

const onCancel = () => {
  emit('cancel');
  visible.value = false;
};

const onClosed = () => {
  signPad.value?.clear();
  saving.value = false;
  archiveSignImage.value = '';
  archiveSignLoaded.value = false;
};
</script>
