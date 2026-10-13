<template>
  <el-dialog v-model="visible" title="长款 / 短款处理" width="480px">
    <el-form label-width="100px">
      <el-form-item label="渠道流水">
        <span>{{ row ? row.channelTradeNo : '' }}（¥{{ row ? money(row.amount) : '' }}）</span>
      </el-form-item>
      <el-form-item label="处理结论" required>
        <el-radio-group v-model="diffForm.handleType" data-testid="payc-diff-type">
          <el-radio :value="2">长款（渠道有、本地无）</el-radio>
          <el-radio :value="3">短款（金额不符待核）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="处理说明" required>
        <el-input v-model="diffForm.handleRemark" type="textarea" :rows="3" maxlength="490"
                  placeholder="定性依据必填（如：重复推送的渠道流水，已联系渠道冲正）" data-testid="payc-diff-remark" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" data-testid="payc-diff-ok" @click="doDiff">确认处理</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { money } from '@/lib/payChannel'
import * as api from '@/api/payChannel'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 待定性处理的渠道账单行（列表行原样传入，取 id / channelTradeNo / amount）
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const diffForm = reactive({ handleType: 2, handleRemark: '' })

watch(visible, (open) => {
  if (!open) return
  diffForm.handleType = 2
  diffForm.handleRemark = ''
})

async function doDiff() {
  if (!diffForm.handleRemark.trim()) {
    ElMessage.warning('长款/短款定性必须写明处理说明')
    return
  }
  await api.handleDiff({ id: props.row.id, handleType: diffForm.handleType, handleRemark: diffForm.handleRemark })
  ElMessage.success('已登记处理结论')
  visible.value = false
  emit('saved')
}
</script>
