<template>
  <div class="p-4 space-y-4">
    <el-card class="query-card" shadow="never">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="编码/名称/归属科室" style="width: 220px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="站点类型">
            <el-select v-model="query.stationType" :fit-input-width="false" clearable placeholder="不限"
                       style="width: 150px" @change="query.pageNum = 1; loadList()">
              <el-option v-for="t in stationTypes" :key="t.value" :label="t.label" :value="t.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.enabled" :fit-input-width="false" clearable placeholder="不限"
                       style="width: 110px" @change="query.pageNum = 1; loadList()">
              <el-option label="启用" :value="1"/>
              <el-option label="停用" :value="0"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button v-perm="'iot:robotStation:add'" type="primary" @click="openEdit(null)">新增站点</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" max-height="560" row-key="id">
        <el-table-column label="站点编码" prop="stationCode" width="130">
          <template #default="{row}"><span class="font-mono text-[12px]">{{ row.stationCode }}</span></template>
        </el-table-column>
        <el-table-column label="站点名称" prop="stationName" min-width="190" show-overflow-tooltip/>
        <el-table-column label="类型" width="110">
          <template #default="{row}">{{ row.stationTypeLabel || '—' }}</template>
        </el-table-column>
        <el-table-column label="位置" width="130">
          <template #default="{row}">{{ positionText(row) }}</template>
        </el-table-column>
        <el-table-column label="归属科室" min-width="120">
          <template #default="{row}">{{ row.deptName || '公共站点' }}</template>
        </el-table-column>
        <el-table-column label="取/收" width="90" align="center">
          <template #default="{row}">
            <el-tag v-if="row.pickupEnabled === 1" size="small" type="success" class="mr-1">取</el-tag>
            <el-tag v-if="row.receiveEnabled === 1" size="small" type="primary">收</el-tag>
            <span v-if="row.pickupEnabled !== 1 && row.receiveEnabled !== 1" class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="停靠点位" width="140">
          <template #default="{row}">
            <span v-if="row.dockCode" class="font-mono text-[12px]">{{ row.dockCode }}</span>
            <span v-else class="text-gray-400">未对码</span>
          </template>
        </el-table-column>
        <el-table-column label="服务时间窗" width="120">
          <template #default="{row}">{{ row.serviceWindow || '全天' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabledLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{row}">
            <el-button v-perm="'iot:robotStation:add'" link size="small" type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button v-perm="'iot:robotStation:delete'" link size="small" type="danger" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex items-center justify-end px-4 py-3">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <RobotStationEdit v-model="editVisible" :row="editRow" :station-types="stationTypes" @saved="loadList"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {robotStationDelete, robotStationListPage, robotStationOptions} from '@/api/robotStation'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import RobotStationEdit from './RobotStationEdit.vue'

/**
 * 机器人收发站点维护（设备管理下）：编码、名称、楼栋楼层、归属科室、取/收能力、停靠点位码。
 * 站点是物理收发点，不是组织节点 —— 一个科室可有多站点，充电桩/中转驿站不属任何科室。
 */

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const stationTypes = ref([])
const query = reactive({keyword: '', stationType: null, enabled: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE})

const editVisible = ref(false)
const editRow = ref(null)

/** 楼层文本：-1 → B1，1 → 1F；楼栋 + 楼层合成位置 */
const positionText = (row) => {
  const floor = row.floorNo == null ? '' : row.floorNo < 0 ? `B${Math.abs(row.floorNo)}` : `${row.floorNo}F`
  return [row.building, floor].filter(Boolean).join(' ') || '—'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await robotStationListPage({
      keyword: query.keyword?.trim() || '',
      stationType: query.stationType,
      enabled: query.enabled,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    rows.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载站点列表失败', e)
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const res = await robotStationOptions()
    stationTypes.value = res.data?.stationTypes || []
  } catch (e) {
    console.error('加载站点类型失败', e)
  }
}

const reset = () => {
  query.keyword = ''
  query.stationType = null
  query.enabled = null
  query.pageNum = 1
  loadList()
}

const openEdit = (row) => {
  editRow.value = row
  editVisible.value = true
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`删除站点「${row.stationName}」？站点编码会被释放，历史任务单的快照文本不受影响。`,
        '删除站点', {type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'})
  } catch {
    return
  }
  try {
    await robotStationDelete(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    console.error('删除站点失败', e)
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadOptions()
  loadList()
})
</script>
