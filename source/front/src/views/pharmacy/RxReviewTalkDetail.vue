<template>
  <!-- ============ 约谈详情（只读） ============ -->
  <el-dialog v-model="visible" title="约谈记录详情" width="640px">
    <template v-if="row">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="约谈编号">{{ row.talkNo }}</el-descriptions-item>
        <el-descriptions-item label="约谈类型">{{ TALK_TYPE[row.talkType] }}</el-descriptions-item>
        <el-descriptions-item label="被约谈医师">{{ row.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ row.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="约谈时间">{{ row.talkTime }}</el-descriptions-item>
        <el-descriptions-item label="约谈人">{{ row.talkerName }}（{{ row.talkerOrg || '—' }}）</el-descriptions-item>
        <el-descriptions-item label="问题摘要" :span="2">{{ row.problemSummary || '—' }}</el-descriptions-item>
        <el-descriptions-item label="约谈内容" :span="2">{{ row.talkContent || '—' }}</el-descriptions-item>
        <el-descriptions-item label="整改要求" :span="2">{{ row.rectifyRequire || '—' }}</el-descriptions-item>
        <el-descriptions-item label="整改状态" :span="2">
          {{ RECTIFY_STATUS[row.rectifyStatus] }}
          <span v-if="row.rectifyRemark">：{{ row.rectifyRemark }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="关联处方" :span="2">
          <template v-if="(row.relatedItems || []).length">
            <div v-for="it in row.relatedItems" :key="it.id" class="drug-line">
              {{ it.prescriptionNo }}（{{ resultText(it.reviewResult) }}，{{ it.visitDate }}）
            </div>
          </template>
          <span v-else>无（独立约谈）</span>
        </el-descriptions-item>
        <el-descriptions-item label="医师确认" :span="2">
          <template v-if="row.doctorConfirm === 1">
            {{ row.doctorConfirmBy }} 于 {{ row.doctorConfirmTime }} 签字确认
          </template>
          <span v-else>未确认</span>
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { RECTIFY_STATUS, TALK_TYPE, resultText } from '@/lib/rxReview'

// 约谈没有按 id 取详情的接口，列表行 talkListPage 已带回全部明细字段（含 relatedItems），故直接渲染当前行
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})
</script>

<style scoped>
.drug-line {
  font-size: 13px;
  line-height: 1.8;
}
</style>
