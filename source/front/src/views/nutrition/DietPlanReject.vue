<template>
  <!-- 批量退回 -->
  <el-dialog v-model="visible" title="批量退回膳食方案" width="480px" data-testid="reject-dialog">
    <el-form label-width="90px">
      <el-form-item label="退回原因" required>
        <el-input v-model="rejectReason" type="textarea" :rows="3" maxlength="500" placeholder="如：饮食类型与病情不符，需重评 NRS2002 后改嘱" data-testid="input-reject-reason" />
      </el-form-item>
      <div class="form-tip">退回去的是「让开嘱医师改医嘱」，方案会停在「已退回」，食堂不会为它配餐；改完由医师重新校对派生。</div>
    </el-form>
    <template #footer>
      <el-button type="danger" :loading="saving" data-testid="btn-do-reject" @click="doReject">确认退回 {{ ids.length }} 条</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { confirmDietPlan } from '@/api/diet'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 待退回的方案主键（来自主页面表格勾选）
  ids: { type: Array, default: () => [] },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
const rejectReason = ref('')

watch(
  () => props.modelValue,
  (v) => {
    if (v) rejectReason.value = ''
  }
)

async function doReject() {
  if (!props.ids.length) { ElMessage.warning('请先勾选膳食方案'); return }
  if (!rejectReason.value.trim()) { ElMessage.warning('退回必须填写原因'); return }
  saving.value = true
  try {
    const res = await confirmDietPlan({
      ids: props.ids,
      accept: false,
      rejectReason: rejectReason.value.trim(),
    })
    ElMessage.success(`已退回 ${res?.data ?? 0} 条`)
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
