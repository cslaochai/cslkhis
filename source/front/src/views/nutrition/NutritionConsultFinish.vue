<template>
  <!-- 出结论 -->
  <el-dialog v-model="visible" title="填写营养会诊结论" width="560px" data-testid="finish-dialog">
    <el-form label-width="110px">
      <el-form-item label="患者">
        <el-input :value="`${finishRow.patientName || ''}（${finishRow.admissionNo || ''}）`" disabled/>
      </el-form-item>
      <el-form-item label="会诊时间">
        <div data-testid="sel-consult-time" style="width: 100%">
          <el-date-picker v-model="finishForm.consultTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                          placeholder="留空取当前时间" style="width: 100%"/>
        </div>
      </el-form-item>
      <el-form-item label="会诊结论" required>
        <el-input v-model="finishForm.conclusion" type="textarea" :rows="4" maxlength="2000"
                  placeholder="营养评定结论 + 膳食/管饲/肠外方案 + 目标热量蛋白 + 复评时间"
                  data-testid="input-conclusion"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="finishForm.remark" type="textarea" :rows="2" maxlength="500"/>
      </el-form-item>
      <div class="form-tip">结论会回写进住院病历，没有结论的「已完成」在病历上等于什么都没发生。</div>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="saving" data-testid="btn-do-finish" @click="doFinish">完成并回写病历
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { finishNutritionConsult } from '@/api/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 要出结论的会诊行 */
  row: { type: Object, default: () => ({}) },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
const finishRow = ref({})
const finishForm = reactive({ consultTime: null, conclusion: '', remark: '' })

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    finishRow.value = props.row || {}
    finishForm.consultTime = null
    finishForm.conclusion = ''
    finishForm.remark = ''
  },
)

async function doFinish() {
  if (!finishForm.conclusion.trim()) {
    ElMessage.warning('会诊结论不能为空');
    return
  }
  saving.value = true
  try {
    const res = await finishNutritionConsult({
      consultationId: finishRow.value.consultationId,
      consultTime: finishForm.consultTime,
      conclusion: finishForm.conclusion.trim(),
      remark: finishForm.remark,
    })
    ElMessage.success(res?.data ? `已完成，病历号 ${res.data}` : '已完成并回写病历')
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '完成失败')
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
