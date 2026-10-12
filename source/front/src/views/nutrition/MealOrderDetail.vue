<template>
  <!-- 详情（只读） -->
  <el-dialog v-model="visible" title="订餐明细" width="600px" data-testid="meal-detail-dialog">
    <el-form :disabled="true" label-width="110px">
      <el-form-item label="患者">
        <el-input
            :value="`${detail.patientName || ''} · ${detail.admissionNo || ''}（${detail.wardName || ''}${detail.bedNo ? ' ' + detail.bedNo + '床' : ''}）`"/>
      </el-form-item>
      <el-form-item label="订餐单号">
        <el-input :value="detail.mealNo || '—'"/>
      </el-form-item>
      <el-form-item label="饮食 / 餐次">
        <el-input
            :value="`${detail.dietName || '—'}（${detail.dietCode || ''}）· ${detail.mealDate || ''} ${detail.mealTypeText || ''} × ${detail.quantity ?? 1}`"/>
      </el-form-item>
      <el-form-item label="配餐内容">
        <el-input :value="detail.dishContent || '—'" type="textarea" :rows="2"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-input
            :value="`${detail.deliverStatusText || '—'}${detail.nextStatusText ? ' · 下一步 ' + detail.nextStatusText : ' · 终态'}`"/>
      </el-form-item>
      <el-form-item label="配餐时间">
        <el-input :value="detail.prepareTime || '—'"/>
      </el-form-item>
      <el-form-item label="配送">
        <el-input :value="`${detail.deliverTime || '—'}${detail.deliverByName ? ' · ' + detail.deliverByName : ''}`"/>
      </el-form-item>
      <el-form-item label="签收">
        <el-input :value="`${detail.signTime || '—'}${detail.signBy ? ' · ' + detail.signBy : ''}`"/>
      </el-form-item>
      <el-form-item label="退订">
        <el-input :value="`${detail.cancelTime || '—'}${detail.cancelReason ? ' · ' + detail.cancelReason : ''}`"/>
      </el-form-item>
      <el-form-item label="来源 / 备注">
        <el-input :value="`${detail.sourceText || '—'}${detail.remark ? ' · ' + detail.remark : ''}`" type="textarea"
                  :rows="2"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 订餐行（列表行自带全部展示字段，无需二次取数） */
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
  (v) => {
    if (v) detail.value = props.row || {}
  },
  { immediate: true },
)
</script>
