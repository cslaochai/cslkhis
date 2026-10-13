<template>
  <el-dialog v-model="visible" title="生成月度监测快照" width="460px">
    <el-form label-width="110px">
      <el-form-item label="统计月份" required>
        <div data-testid="gen-month" style="width: 100%">
          <el-date-picker
            v-model="genMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            style="width: 100%" />
        </div>
      </el-form-item>
      <el-form-item label="统计范围" required>
        <el-radio-group v-model="genScope">
          <el-radio :value="1">全院一条</el-radio>
          <el-radio :value="2">按科室生成</el-radio>
        </el-radio-group>
      </el-form-item>
      <div class="form-tip">同月同范围重复生成会覆盖上一次结果 —— 指标是对外报数，只留最新一次复算。</div>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="saving" data-testid="btn-do-generate" @click="doGenerate">生成</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 生成月度监测快照（自 AntibioticMonitorView.vue 原样搬出）
 * 保存成功后把生成月份回传给父页面（父页面据此切换统计月份并刷新列表）。
 */
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateAntibioticStats } from '@/api/antibiotic'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const saving = ref(false)
const genMonth = ref(new Date().toISOString().slice(0, 7))
const genScope = ref(1)

async function doGenerate() {
  if (!genMonth.value) { ElMessage.warning('请选择统计月份'); return }
  saving.value = true
  try {
    const res = await generateAntibioticStats({ statMonth: genMonth.value, scopeType: genScope.value })
    const n = (res?.data || []).length
    ElMessage.success(`已生成 ${n} 条快照`)
    visible.value = false
    emit('saved', genMonth.value)
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
