<template>
  <el-dialog v-model="visible" title="人工勾对" width="560px">
    <el-form label-width="100px">
      <el-form-item label="渠道流水">
        <span>{{ row ? row.channelTradeNo : '' }}</span>
      </el-form-item>
      <el-form-item label="渠道金额">
        <span class="strong">¥{{ row ? money(row.amount) : '' }}</span>
      </el-form-item>
      <el-form-item label="本地支付流水" required>
        <el-select v-model="matchForm.localTxnNo" filterable placeholder="选择当日该渠道的成功支付流水"
                   style="width: 100%" data-testid="payc-match-txn" :fit-input-width="false">
          <el-option v-for="c in candidates" :key="c.txnNo" :value="c.txnNo"
                     :label="`${c.txnNo}  ${c.patientName}  ¥${money(c.amount)}  ${c.directionText}  ${c.txnTime}`" />
        </el-select>
      </el-form-item>
      <el-alert type="warning" :closable="false"
                title="两侧金额（收正退负）不一致会被拒绝；差额需要定性的请改用「差额处理」登记长款/短款" />
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" data-testid="payc-match-ok" @click="doMatch">确认勾对</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { money } from '@/lib/payChannel'
import * as api from '@/api/payChannel'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 待勾对的渠道账单行（列表行原样传入，取 id / channelTradeNo / amount）
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const candidates = ref([])
const matchForm = reactive({ localTxnNo: '' })

watch(visible, async (open) => {
  if (!open || !props.row) return
  matchForm.localTxnNo = ''
  const { data } = await api.matchCandidates(props.row.id)
  candidates.value = data || []
})

async function doMatch() {
  await api.match({ id: props.row.id, localTxnNo: matchForm.localTxnNo })
  ElMessage.success('勾对成功')
  visible.value = false
  emit('saved')
}
</script>

<style scoped>
.strong {
  font-weight: 600;
}
</style>
