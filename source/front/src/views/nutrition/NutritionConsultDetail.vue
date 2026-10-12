<template>
  <!-- 详情（只读） -->
  <el-dialog v-model="visible" title="营养会诊明细" width="660px" data-testid="consult-detail-dialog">
    <el-form :disabled="true" label-width="110px">
      <el-form-item label="会诊号 / 类别">
        <el-input :value="`${detail.consultationNo || '—'} · ${detail.consultCategoryText || '—'}`"/>
      </el-form-item>
      <el-form-item label="患者">
        <el-input
            :value="`${detail.patientName || ''} · ${detail.admissionNo || ''}（${detail.fromDeptName || ''}${detail.bedNo ? ' ' + detail.bedNo + '床' : ''}）`"/>
      </el-form-item>
      <el-form-item label="申请 / 接诊">
        <el-input
            :value="`${detail.applyDoctorName || '—'} ${detail.applyTime || ''} → ${detail.acceptDoctorName || '未接诊'} ${detail.acceptTime || ''}`"/>
      </el-form-item>
      <el-form-item label="范围 / 紧急度">
        <el-input
            :value="`${detail.consultTypeText || '—'} · ${detail.isUrgentText || '—'} · 应答耗时 ${responseText(detail)}`"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-input
            :value="`${detail.consultStatusText || '—'}${detail.overdueText ? ' · ' + detail.overdueText : ''}`"/>
      </el-form-item>
      <el-form-item label="会诊理由">
        <el-input :value="detail.reason || '—'" type="textarea" :rows="3"/>
      </el-form-item>
      <el-form-item label="会诊结论">
        <el-input :value="detail.conclusion || '—'" type="textarea" :rows="4"/>
      </el-form-item>
      <el-form-item label="回写病历">
        <el-input :value="detail.recordNo ? `病历号 ${detail.recordNo} · ${detail.finishTime || ''}` : '尚未回写'"/>
      </el-form-item>
      <el-form-item label="取消原因">
        <el-input :value="detail.cancelReason || '—'"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getNutritionConsultDetail } from '@/api/nutrition'
import { responseText } from '@/lib/nutrition'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 会诊行：先展示行数据，详情接口取到后覆盖 */
  row: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const detail = ref({})

watch(
  () => props.modelValue,
  async (v) => {
    if (!v) return
    detail.value = props.row || {}
    try {
      const res = await getNutritionConsultDetail(detail.value.consultationId)
      if (res?.data) detail.value = res.data
    } catch (e) {
      // 列表行已有主字段，详情接口失败时保留行数据，不打断查看
    }
  },
)
</script>
