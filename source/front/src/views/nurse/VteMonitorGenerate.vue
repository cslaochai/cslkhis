<template>
  <!-- 生成快照 -->
  <el-dialog v-model="visible" title="生成月度防控指标快照" width="460px">
    <el-form label-width="110px">
      <el-form-item label="统计月份" required>
        <div data-testid="gen-month" style="width: 100%">
          <el-date-picker v-model="genMonth" placeholder="选择月份" style="width: 100%" type="month"
                          value-format="YYYY-MM"/>
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
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" data-testid="btn-do-generate" type="primary" @click="doGenerate">生成</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 生成月度防控指标快照：月份/范围与提交都在本组件；
 * 生成成功后把所用月份回传给父页面（statsQuery.statMonth 跟着走，方便立刻查到刚生成的行）。
 */
import {computed, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {generateVteStats} from '@/api/vte';
import {currentMonth} from '@/lib/vte';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const genMonth = ref(currentMonth());
const genScope = ref(1);
const saving = ref(false);

async function doGenerate() {
  if (!genMonth.value) {
    ElMessage.warning('请选择统计月份');
    return
  }
  saving.value = true
  try {
    const res = await generateVteStats({statMonth: genMonth.value, scopeType: genScope.value})
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
