<template>
  <div class="er-duty-view">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="page-title">急诊值班表</span>
          <span class="page-subtitle">急诊内科 / 外科 / 儿科 · 已发布医生值班（全院公开）</span>
        </div>
        <div class="toolbar-right">
          <el-date-picker
            v-model="queryDate"
            type="date"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            placeholder="选择日期"
            :clearable="false"
            style="width: 160px"
            @change="load"
          />
          <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
        </div>
      </div>

      <el-alert
        title="值班表全院公开是《医疗质量安全核心制度》要求：本页为只读视图，排班/审核请在「岗位出勤」「排班审批中心」操作。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
      />

      <el-table :data="rows" v-loading="loading" border stripe empty-text="当日暂无已发布的急诊值班排班">
        <el-table-column label="科室" width="140">
          <template #default="{ row }">{{ row.deptName || row.orgName || '-' }}</template>
        </el-table-column>
        <el-table-column label="班次" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="shiftTagType(row)">{{ row.shiftName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="起止时间" width="150">
          <template #default="{ row }">{{ row.startTime }} – {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column label="值班医生" min-width="140">
          <template #default="{ row }">{{ row.employeeName || '-' }}</template>
        </el-table-column>
        <el-table-column label="响应形态" width="110">
          <template #default="{ row }">{{ row.attendModeText || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {Refresh} from '@element-plus/icons-vue'
import {getErDutyPublic} from '@/api/staffSchedule'

const queryDate = ref(new Date().toISOString().slice(0, 10))
const rows = ref([])
const loading = ref(false)

// 班次标签色：白班=success，前夜=warning，后夜=danger；其余按班次名兜底
const shiftTagType = (row) => {
  const name = row.shiftName || ''
  if (name.includes('白班')) return 'success'
  if (name.includes('前夜') || name.includes('小夜')) return 'warning'
  if (name.includes('后夜') || name.includes('大夜')) return 'danger'
  return 'info'
}

const load = async () => {
  loading.value = true
  try {
    const res = await getErDutyPublic(queryDate.value)
    rows.value = res?.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.er-duty-view {
  padding: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.toolbar-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.page-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}
.page-subtitle {
  font-size: 12.5px;
  color: #6b7280;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
