<template>
  <el-dialog v-model="visible" title="拉取渠道账单" width="420px">
    <el-form label-width="90px">
      <el-form-item label="渠道">
        <el-select v-model="importForm.channel" style="width: 100%" data-testid="payc-import-channel">
          <el-option v-for="c in CHANNELS" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="账单日期">
        <el-date-picker v-model="importForm.billDate" type="date" value-format="YYYY-MM-DD"
                        style="width: 100%" data-testid="payc-import-date" />
      </el-form-item>
      <el-alert type="info" :closable="false"
                title="当前未接真实渠道：后端打印会在服务端控制台打印拉取动作，并按本地当日支付流水生成模拟账单（幂等，可重复拉取）" />
    </el-form>
    <template #footer>
      <el-button type="primary" data-testid="payc-import-ok" @click="doImport">拉取</el-button>
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
  // 页面查询区的账单日期：打开时带过来，未选则默认当天（口径同拆分前的 openImport）
  billDate: { type: String, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const importForm = reactive({ channel: 2, billDate: null })

watch(visible, (open) => {
  if (!open) return
  importForm.billDate = props.billDate || localDateStr()
})

async function doImport() {
  const { data } = await api.importBill({ ...importForm })
  ElMessage.success(`拉取完成，新落台账 ${data ?? 0} 笔（服务端控制台可见 [M7支付渠道口子] 打印）`)
  visible.value = false
  emit('saved')
}
</script>
