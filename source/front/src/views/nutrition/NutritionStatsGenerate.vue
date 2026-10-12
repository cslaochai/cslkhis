<template>
  <!-- 生成快照 -->
  <el-dialog v-model="visible" title="生成营养膳食月度快照" width="470px" data-testid="gen-stats-dialog">
    <el-form label-width="110px">
      <el-form-item label="统计月份" required>
        <div data-testid="gen-month" style="width: 100%">
          <el-date-picker v-model="genMonth" type="month" placeholder="选择月份" value-format="YYYY-MM"
                          style="width: 100%"/>
        </div>
      </el-form-item>
      <el-form-item label="统计范围" required>
        <el-radio-group v-model="genScope">
          <el-radio :value="1">全院一条</el-radio>
          <el-radio :value="2">按科室生成</el-radio>
        </el-radio-group>
      </el-form-item>
      <div class="form-tip">
        按科室生成时，出院人数与膳食方案数都取该科室的口径（病区归属按入院科室）。同月同范围重复生成覆盖上一次结果。
      </div>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" data-testid="btn-do-generate" @click="doGenerate">生成</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateNutritionStats } from '@/api/nutrition'
import { currentMonthStr } from '@/lib/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

// 与原页面口径一致：默认当月，关掉弹框再开不重置（选择是有记忆的）
const genMonth = ref(currentMonthStr())
const genScope = ref(1)
const saving = ref(false)

async function doGenerate() {
  if (!genMonth.value) {
    ElMessage.warning('请选择统计月份');
    return
  }
  saving.value = true
  try {
    const res = await generateNutritionStats({statMonth: genMonth.value, scopeType: genScope.value})
    ElMessage.success(`已生成 ${(res?.data || []).length} 条快照`)
    visible.value = false
    emit('saved', genMonth.value)
  } catch (e) {
    ElMessage.error(e?.message || '生成失败')
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
