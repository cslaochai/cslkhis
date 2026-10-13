<template>
  <el-dialog v-model="visible" title="整周复制排班" width="560px">
    <el-alert
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
        title="只按星期对齐：来源区间与目标区间长度必须相同（整周=7 天），否则周一的班会错位到别的星期。"
        type="info"
    />
    <el-form ref="copyFormRef" :model="copyForm" :rules="copyRules" label-width="150px">
      <el-form-item label="排班单元类型">
        <el-radio-group v-model="copyForm.orgType" @change="copyForm.orgId = null">
          <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="来源区间开始日" prop="fromStartDate">
        <el-date-picker v-model="copyForm.fromStartDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="来源区间结束日">
        <el-date-picker v-model="copyForm.fromEndDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="目标区间开始日" prop="toStartDate">
        <el-date-picker v-model="copyForm.toStartDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item>
        <el-button size="small" @click="fillLastWeekToNext">填入「上上周 → 上周」</el-button>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" data-testid="ss-copy-submit" type="primary" @click="handleCopy">开始复制
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {staffScheduleCopyRange} from '@/api/staffSchedule'
import {ORG_UNIT_TYPE_OPTIONS} from '@/lib/staffSchedule'

/**
 * 整周复制排班：把来源区间的班按「星期对齐」搬到目标区间（一次请求，后端整批处理）。
 * 页面只负责开关弹框与刷新列表。
 */
const props = defineProps({
  modelValue: {type: Boolean, default: false},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const copyFormRef = ref(null)
const copyForm = reactive({orgType: 1, orgId: null, fromStartDate: null, fromEndDate: null, toStartDate: null})
const copyRules = {
  fromStartDate: [{required: true, message: '请选择来源区间开始日', trigger: 'change'}],
  toStartDate: [{required: true, message: '请选择目标区间开始日', trigger: 'change'}],
}

/** 一键把「上一周」整周搬到「下一周」：源/目标长度必须相同，否则周一的班会错位到别的星期 */
const fillLastWeekToNext = () => {
  const dayOffset = (d, n) => {
    const x = new Date(d)
    x.setDate(x.getDate() + n)
    return x.toISOString().slice(0, 10)
  }
  const today = new Date()
  copyForm.fromStartDate = dayOffset(today, -14)
  copyForm.fromEndDate = dayOffset(today, -8)
  copyForm.toStartDate = dayOffset(today, -7)
}

const handleCopy = async () => {
  if (!copyFormRef.value) return
  try {
    await copyFormRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const res = await staffScheduleCopyRange({...copyForm})
    if (res.code === 200) {
      ElMessage.success(res.message || '复制完成')
      visible.value = false
      emit('saved')
    } else {
      ElMessage.error(res.message || '复制失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '复制失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
