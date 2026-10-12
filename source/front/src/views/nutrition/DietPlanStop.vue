<template>
  <!-- 停餐 -->
  <el-dialog v-model="visible" title="停止膳食方案" width="480px" data-testid="stop-dialog">
    <el-form label-width="90px">
      <el-form-item label="方案">
        <el-input :value="`${stopRow.dietName || ''}（${stopRow.dietNo || ''}）`" disabled />
      </el-form-item>
      <el-form-item label="停止时间">
        <div data-testid="sel-stop-time" style="width: 100%">
          <el-date-picker v-model="stopForm.stopTime" type="datetime" value-format="YYYY-MM-dd HH:mm:ss" placeholder="留空取当前时间" style="width: 100%" />
        </div>
      </el-form-item>
      <el-form-item label="停止原因" required>
        <el-input v-model="stopForm.reason" type="textarea" :rows="3" maxlength="500" placeholder="如：患者拒食 / 明日造影需禁食 / 转出病区" data-testid="input-stop-reason" />
      </el-form-item>
      <div class="form-tip">医嘱没停但临床上不吃了才用这里。停方案会把该时间之后还没配送的餐一并作废，已经送出去的不追。</div>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="warning" :loading="saving" data-testid="btn-do-stop" @click="doStop">确认停餐</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { stopDietPlan } from '@/api/diet'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 要停餐的方案行（主页面操作列按钮传入）
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
const stopRow = ref({})
const stopForm = reactive({ stopTime: null, reason: '' })

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    stopRow.value = props.row || {}
    stopForm.stopTime = null
    stopForm.reason = ''
  }
)

async function doStop() {
  if (!stopForm.reason.trim()) { ElMessage.warning('停止原因不能为空（停餐是一个临床决定）'); return }
  saving.value = true
  try {
    await stopDietPlan({ id: stopRow.value.id, stopTime: stopForm.stopTime, reason: stopForm.reason.trim() })
    ElMessage.success('已停餐')
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '停餐失败')
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
