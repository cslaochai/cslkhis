<script setup lang="js">
import { ref, onMounted } from 'vue'
import { getCurrentDutyOfficer } from '@/api/dutyRoster'

const duty = ref(null)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await getCurrentDutyOfficer()
    if (res.code === 200) duty.value = res.data
  } catch (e) {
    // 失败不留空：宁可显示"未取到"，也不能让人以为系统里没有这一栏
    duty.value = { found: 0, emptyReason: '总值班信息获取失败，请刷新重试' }
  } finally {
    loading.value = false
  }
}

onMounted(load)
defineExpose({ load })
</script>

<template>
  <div v-loading="loading" class="duty-bar" data-testid="duty-officer-bar">
    <template v-if="duty && duty.found === 1">
      <el-alert type="success" :closable="false" show-icon>
        <template #title>
          <span class="duty-label">今日总值班</span>
          <span class="duty-name" data-testid="duty-name">{{ duty.employeeName }}</span>
          <el-tag size="small" effect="dark" type="primary">{{ duty.shiftTypeText }}·{{ duty.roleTypeText }}</el-tag>
          <span v-if="duty.phone" class="duty-phone">☎ {{ duty.phone }}</span>
          <span v-if="duty.deptName" class="duty-dept">（{{ duty.deptName }}）</span>
          <el-tag v-if="duty.substituted === 1" size="small" type="warning">
            临时换班顶替 {{ duty.originEmpName }}
          </el-tag>
        </template>
        <span class="duty-hint">
          急诊候诊/留观超时升级、床位跨科调配、双向转诊协调，本科室无人响应时由总值班兜底协调
          （{{ duty.dutyDate }} {{ duty.startTime }}~{{ duty.endTime }}）
        </span>
      </el-alert>
    </template>
    <template v-else>
      <el-alert type="error" :closable="false" show-icon>
        <template #title>
          <span class="duty-label">今日总值班</span>
          <span class="duty-name">无人值班</span>
        </template>
        <span class="duty-hint">
          {{ duty && duty.emptyReason ? duty.emptyReason : '当日总值班未排班，全院应急协调当前无人接手' }}
          —— 请在「组织与资源 → 总值班排班」登记
        </span>
      </el-alert>
    </template>
  </div>
</template>

<style scoped>
.duty-bar {
  margin-bottom: 12px;
}
.duty-label {
  font-weight: 600;
  margin-right: 8px;
}
.duty-name {
  font-size: 16px;
  font-weight: 700;
  margin-right: 8px;
}
.duty-phone {
  margin-left: 8px;
  font-variant-numeric: tabular-nums;
}
.duty-dept {
  margin-left: 4px;
  color: var(--el-text-color-secondary);
}
.duty-hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
