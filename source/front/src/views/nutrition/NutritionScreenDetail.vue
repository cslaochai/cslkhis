<template>
  <!-- 只读详情：行点击打开，编辑走列表按钮 -->
  <el-dialog v-model="visible" data-testid="detail-dialog" title="筛查记录详情" width="560px">
    <el-form disabled label-width="120px">
      <el-form-item label="筛查编号">
        <el-input :value="detail.screenNo"/>
      </el-form-item>
      <el-form-item label="患者">
        <el-input :value="`${detail.patientName || ''}（${detail.patientNo || ''}）`"/>
      </el-form-item>
      <el-form-item label="量表">
        <el-input :value="detail.screenTypeText"/>
      </el-form-item>
      <el-form-item label="总分 / 判定">
        <el-input :value="`${detail.totalScore} 分 · ${detail.riskFlagText}`"/>
      </el-form-item>
      <el-form-item label="BMI">
        <el-input :value="detail.bmi ?? '—'"/>
      </el-form-item>
      <el-form-item label="下次筛查">
        <el-input :value="detail.nextScreenDate || '—'"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input :rows="2" :value="detail.remark || '—'" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="btn-plan-for-risk" type="primary" @click="goDietPlan">查看膳食方案</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 筛查行（列表行自带全部展示字段，无需二次取数） */
  row: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue', 'saved'])

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
  { immediate: true },
)

function goDietPlan() {
  visible.value = false
  router.push({path: '/diet-plan', query: {admissionId: detail.value.admissionId}})
}
</script>
