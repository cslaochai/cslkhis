<template>
  <!-- 详情（只读） -->
  <el-dialog v-model="visible" title="膳食方案明细" width="640px" data-testid="plan-detail-dialog">
    <el-form :disabled="true" label-width="110px">
      <el-form-item label="患者">
        <el-input :value="`${detail.patientName || ''} · ${detail.admissionNo || ''}（${detail.wardName || detail.deptName || ''}${detail.bedNo ? ' ' + detail.bedNo + '床' : ''}）`" />
      </el-form-item>
      <el-form-item label="方案号 / 医嘱号">
        <el-input :value="`${detail.dietNo || '—'} / ${detail.orderNo || '—'}`" />
      </el-form-item>
      <el-form-item label="来源">
        <el-input :value="`${detail.sourceText || '—'} · ${detail.dietCode || ''} ${detail.dietName || ''}`" />
      </el-form-item>
      <el-form-item label="类别 / 途径">
        <el-input :value="`${detail.dietCategoryText || '—'} · ${detail.routeText || '—'} · ${Number(detail.needsMeal) === 1 ? '食堂订餐' : '不订餐'}`" />
      </el-form-item>
      <el-form-item label="供应餐次">
        <el-input :value="detail.mealTypesText || '—'" />
      </el-form-item>
      <el-form-item label="管饲/输注">
        <el-input :value="detail.feedWay || '—'" />
      </el-form-item>
      <el-form-item label="目标量">
        <el-input :value="`热量 ${detail.calorieTarget ?? '—'} kcal · 蛋白 ${detail.proteinTarget ?? '—'} g · 液体 ${detail.fluidTarget ?? '—'} ml`" />
      </el-form-item>
      <el-form-item label="起止时间">
        <el-input :value="`${detail.startTime || '—'} → ${detail.stopTime || '长期'}`" />
      </el-form-item>
      <el-form-item label="接收">
        <el-input :value="`${detail.confirmStatusText || '—'}${detail.confirmerName ? ' · ' + detail.confirmerName : ''}${detail.confirmTime ? ' · ' + detail.confirmTime : ''}`" />
      </el-form-item>
      <el-form-item label="退回原因">
        <el-input :value="detail.rejectReason || '—'" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input :value="detail.remark || '—'" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" data-testid="btn-detail-meal" @click="goMeal">查看订餐</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  // 列表行即全量展示字段（本接口无按 id 取详情的入口），打开瞬间不闪白
  row: { type: Object, default: () => ({}) },
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const router = useRouter()
const detail = ref({})

watch(
  () => props.modelValue,
  (v) => {
    if (v) detail.value = props.row || {}
  },
  { immediate: true }
)

function goMeal() {
  visible.value = false
  router.push({ path: '/meal-order', query: { dietPlanId: detail.value.id } })
}
</script>
