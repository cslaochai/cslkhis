<template>
  <!-- 状态推进 / 退订 -->
  <el-dialog v-model="visible" :title="advanceTitle" width="500px" data-testid="advance-dialog">
    <el-form label-width="110px">
      <el-form-item label="处理条数">
        <el-input :value="`${advanceIds.length} 条 ${targetStatusText ? '→ ' + targetStatusText : ''}`" disabled/>
      </el-form-item>
      <el-form-item v-if="target === 1" label="当日食谱">
        <el-input v-model="advanceForm.dishContent" type="textarea" :rows="2" maxlength="500"
                  placeholder="如：糖尿病午餐 · 杂粮饭 + 清蒸鱼 + 炒青菜" data-testid="input-dish"/>
      </el-form-item>
      <el-form-item v-if="target === 3" label="签收人" required>
        <el-input v-model="advanceForm.signBy" maxlength="50" placeholder="患者 / 家属 / 护士姓名"
                  data-testid="input-sign-by"/>
      </el-form-item>
      <el-form-item v-if="target === 4" label="退订原因" required>
        <el-input v-model="advanceForm.cancelReason" type="textarea" :rows="2" maxlength="500"
                  placeholder="如：停餐 / 出院 / 拒餐 / 转科" data-testid="input-cancel-reason"/>
      </el-form-item>
      <div class="form-tip">
        批量是全成功或全不生效：一部分标成「已签收」另一部分报错，食堂就说不清今天到底送了几份。
        跨状态的勾选会被服务端按状态机拒掉，请一次勾选同一状态的餐。
      </div>
    </el-form>
    <template #footer>
      <el-button :type="target === 4 ? 'danger' : 'primary'" :loading="saving" data-testid="btn-do-advance"
                 @click="doAdvance">确认
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateMealStatus } from '@/api/diet'
import { MEAL_STATUS_TEXT } from '@/lib/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 本次要处理的订餐行（页面已完成状态一致性/可推进校验后才打开） */
  rows: { type: Array, default: () => [] },
  /** 目标状态：当前状态 +1，或退订固定 4 */
  target: { type: Number, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const advanceIds = computed(() => props.rows.map((r) => r.id))
const targetStatusText = computed(() => MEAL_STATUS_TEXT[props.target] || '')
const advanceTitle = computed(() => (props.target === 4 ? '退订餐单' : `推进到「${targetStatusText.value}」`))

const saving = ref(false)
const advanceForm = reactive({ dishContent: '', signBy: '', cancelReason: '' })

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    advanceForm.dishContent = props.rows[0]?.dishContent || ''
    advanceForm.signBy = ''
    advanceForm.cancelReason = ''
  },
)

async function doAdvance() {
  const payload = { ids: advanceIds.value, deliverStatus: props.target }
  if (props.target === 1) {
    payload.dishContent = advanceForm.dishContent.trim() || null
  } else if (props.target === 3) {
    if (!advanceForm.signBy.trim()) {
      ElMessage.warning('签收人不能为空（谁收到饭要落名）');
      return
    }
    payload.signBy = advanceForm.signBy.trim()
  } else if (props.target === 4) {
    if (!advanceForm.cancelReason.trim()) {
      ElMessage.warning('退订原因不能为空');
      return
    }
    payload.cancelReason = advanceForm.cancelReason.trim()
  }
  saving.value = true
  try {
    const res = await updateMealStatus(payload)
    ElMessage.success(`已更新 ${res?.data ?? 0} 条`)
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
