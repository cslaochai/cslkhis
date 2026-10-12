<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="设备编码/名称/型号/科室" style="width: 220px"
                    @keyup.enter="query.pageNum = 1; loadList()"/>
        </el-form-item>
        <el-form-item label="设备类别">
          <el-select v-model="query.category" :fit-input-width="false" clearable placeholder="设备类别"
                     style="width: 150px">
            <el-option v-for="d in categoryDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态" style="width: 110px">
            <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="equipment-table" stripe>
        <el-table-column label="资产编号" prop="equipmentCode" width="140"/>
        <el-table-column label="设备名称" min-width="150" prop="equipmentName" show-overflow-tooltip/>
        <el-table-column label="型号" prop="model" show-overflow-tooltip width="120"/>
        <el-table-column label="使用科室" prop="deptName" width="110"/>
        <el-table-column label="类别" prop="category" width="120">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column align="right" label="原值(元)" prop="purchasePrice" width="110">
          <template #default="{ row }">{{
              row.purchasePrice != null ? Number(row.purchasePrice).toLocaleString() : '—'
            }}
          </template>
        </el-table-column>
        <el-table-column label="最近维保" width="110">
          <template #default="{ row }">{{ fmtDate(row.lastMaintainDate) }}</template>
        </el-table-column>
        <el-table-column label="下次维保" width="110">
          <template #default="{ row }">
            <span :class="{ 'text-red-500 font-bold': row.nextMaintainDate && row.nextMaintainDate < today }">
              {{ fmtDate(row.nextMaintainDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="计量有效期至" width="120">
          <template #default="{ row }">
            <span v-if="row.meteringValidUntil" :class="{ 'text-red-500 font-bold': meteringExpired(row) }">
              {{ fmtDate(row.meteringValidUntil) }}{{ meteringExpired(row) ? '（过期）' : '' }}
            </span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button v-perm="'asset:equipment:add'" link size="small" type="primary" @click="openMaintain(row)">
              维保登记
            </el-button>
            <el-button v-perm="'asset:equipment:add'" link size="small" type="success" @click="openMetering(row)">
              计量登记
            </el-button>
            <el-button link size="small" @click="openDetail(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 维保登记 -->
    <EquipmentMaintainAdd v-model="maintainVisible" :maintain-type-dict="maintainTypeDict" :row="maintainRow"
                          @saved="loadList"/>

    <!-- 计量登记 -->
    <EquipmentMeteringAdd v-model="meteringVisible" :metering-type-dict="meteringTypeDict" :row="meteringRow"
                          @saved="loadList"/>

    <!-- 记录详情 -->
    <EquipmentRecordsDetail v-model="detailVisible" :id="detailId" :maintain-type-dict="maintainTypeDict"
                            :metering-type-dict="meteringTypeDict" @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * 设备管理（G22，菜单 902）
 *
 * 台账 = sys_equipment（49 号铺底 90 行）；本页聚焦维保/计量：
 * - 维保登记成功回写档案「最近维保日期」，下次维保日期 = 最近维保 + 维保周期（前端只展示，计算在后端 VO）
 * - 计量（强检/校准）有效期至过期即台账亮红
 * - 维保/计量记录录错可删（后端会按剩余记录重算最近维保日期）
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {equipmentListPage} from '@/api/equipment';
import EquipmentMaintainAdd from './EquipmentMaintainAdd.vue';
import EquipmentMeteringAdd from './EquipmentMeteringAdd.vue';
import EquipmentRecordsDetail from './EquipmentRecordsDetail.vue';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {fmtDate} from '@/lib/equipment';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---------------- 字典 ----------------
const statusDict = ref([]);
const categoryDict = ref([]);
const maintainTypeDict = ref([]);
const meteringTypeDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const categoryText = (v) => dictLabelText(categoryDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.EQUIP_MAINTAIN_TYPE},${DICT_TYPE.EQUIP_METERING_TYPE},${DICT_TYPE.EQUIP_METERING_RESULT}`);
    maintainTypeDict.value = res?.data?.[DICT_TYPE.EQUIP_MAINTAIN_TYPE] || [];
    meteringTypeDict.value = res?.data?.[DICT_TYPE.EQUIP_METERING_TYPE] || [];
    // 设备类别/状态是 49 号铺底的老字典，不在 G22 段，单独取
    const res2 = await getDictDataMapList('his_equipment_category,his_equipment_status');
    categoryDict.value = res2?.data?.['his_equipment_category'] || [];
    statusDict.value = res2?.data?.['his_equipment_status'] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 台账列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '', category: null, status: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await equipmentListPage({
      keyword: query.keyword.trim() || undefined,
      category: query.category ?? undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {keyword: '', category: null, status: null, pageNum: 1});
  loadList();
};
const statusTag = (v) => ({1: 'success', 2: 'info', 3: 'warning', 4: 'danger'}[v] || 'info');
// ---------------- 维保 / 计量登记 ----------------
const maintainVisible = ref(false);
const maintainRow = ref(null);
const openMaintain = (row) => {
  maintainRow.value = row;
  maintainVisible.value = true;
};
const meteringVisible = ref(false);
const meteringRow = ref(null);
const openMetering = (row) => {
  meteringRow.value = row;
  meteringVisible.value = true;
};
// ---------------- 详情（最近维保/计量） ----------------
const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
const meteringExpired = (row) => row.meteringExpired;
// 本地时区当日（勿用 toISOString —— UTC+8 零点前会取到昨天）
const today = (() => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
})();
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
