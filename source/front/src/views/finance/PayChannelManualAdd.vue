<template>
  <el-dialog v-model="visible" title="手工登记渠道流水" width="460px">
    <el-form label-width="100px">
      <el-form-item label="渠道" required>
        <el-select v-model="manualForm.channel" style="width: 100%" data-testid="payc-manual-channel">
          <el-option v-for="c in CHANNELS" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="账单日期" required>
        <el-date-picker v-model="manualForm.billDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
      </el-form-item>
      <el-form-item label="渠道流水号" required>
        <el-input v-model="manualForm.channelTradeNo" data-testid="payc-manual-trade-no" />
      </el-form-item>
      <el-form-item label="金额" required>
        <!-- 退款账单行是负数，min 不能卡 0.01，否则渠道退的那笔钱根本登记不进去 -->
        <el-input-number v-model="manualForm.amount" :min="-999999" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="交易时间">
        <el-date-picker v-model="manualForm.tradeTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="manualForm.remark" type="textarea" :rows="2" maxlength="490" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" data-testid="payc-manual-ok" @click="doManual">登记</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { localDateStr } from '@/lib/utils'
import { CHANNELS } from '@/lib/payChannel'
import * as api from '@/api/payChannel'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 页面查询区的账单日期：打开时带过来，未选则默认当天（口径同拆分前的 openManual）
  billDate: { type: String, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const manualForm = reactive({ channel: 2, billDate: null, channelTradeNo: '', amount: null, tradeTime: null, remark: '' })

watch(visible, (open) => {
  if (!open) return
  // 拆分前只重置账单日期、其余字段沿用上次输入，这里保持一致
  manualForm.billDate = props.billDate || localDateStr()
})

async function doManual() {
  await api.manualRegister({ ...manualForm })
  ElMessage.success('已登记渠道流水')
  visible.value = false
  emit('saved')
}
</script>
