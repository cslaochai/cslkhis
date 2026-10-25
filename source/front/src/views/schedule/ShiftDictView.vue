<template>
  <!-- 排班字典管理：班次字典 + 公共工位字典，两个Tab合并维护 -->
  <div class="w-full">
    <el-card shadow="never">
      <el-tabs v-model="activeDictTab" @tab-change="handleDictTabChange">
        <!-- ===================== 班次字典 ===================== -->
        <el-tab-pane label="班次字典" name="shift">
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
        <el-input v-model="shiftKeyword" class="!w-56" clearable data-testid="shift-keyword"
                  placeholder="按班次名称搜索" @clear="handleShiftSearch" @keyup.enter="handleShiftSearch"/>
        <el-select v-model="shiftUseScope" :fit-input-width="false" class="!w-44" clearable
                   data-testid="shift-scope-filter"
                   placeholder="适用域" @change="handleShiftSearch">
          <el-option v-for="o in SHIFT_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
        <el-button :icon="Search" data-testid="shift-search-btn" type="primary" @click="handleShiftSearch">查询
        </el-button>
        <el-button v-perm="'shift:dict:add'" :icon="Plus" data-testid="shift-add-btn" type="success"
                   @click="handleShiftAdd">新增字典
        </el-button>
      </div>
      <el-table v-loading="shiftLoading" :data="shifts" border data-testid="shift-dict-table" style="width: 100%">
        <el-table-column label="班次名称" min-width="150" prop="shiftName">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span v-if="row.color" class="inline-block h-3 w-3 rounded-sm"
                    :style="{ backgroundColor: row.color }"/>
              <span>{{ row.shiftName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="缩写" width="70">
          <template #default="{ row }">
            <span v-if="row.shortName" class="font-medium text-slate-700">{{ row.shortName }}</span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="短码" width="80">
          <template #default="{ row }">
            <span v-if="row.shiftCode" class="font-mono text-xs text-slate-500">{{ row.shiftCode }}</span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="适用域" width="120">
          <template #default="{ row }">
            <el-tag :type="Number(row.useScope) === 2 ? 'success' : 'info'" data-testid="shift-scope-tag"
                    size="small">{{ SHIFT_SCOPE_TEXT[Number(row.useScope ?? 1)] || '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="起止时间" width="130">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-600">{{ row.startTime }} ~ {{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="时长" width="80">
          <template #default="{ row }">{{ row.durationMinutes }}分</template>
        </el-table-column>
        <el-table-column label="适用科室" min-width="120">
          <template #default="{ row }">{{ shiftDeptName(row.deptId) }}</template>
        </el-table-column>
        <el-table-column align="center" label="班次类型" width="90">
          <template #default="{ row }">{{
              row.scheduleType ? (shiftLabel[row.scheduleType] || '—') : '—'
            }}
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{
                row.status === 1 ? '启用' : '停用'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" fixed="right" label="操作" width="80">
          <template #default="{ row }">
            <el-button v-perm="'shift:dict:edit'" data-testid="shift-edit-btn" link type="primary"
                       @click="handleShiftEdit(row)">编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination :current-page="shiftPagination.pageNum" :page-size="shiftPagination.pageSize"
                     :page-sizes="PAGE_SIZES"
                     :total="shiftPagination.total" background
                     class="mt-3 justify-end" layout="total, prev, pager, next, sizes"
                     @current-change="handleShiftPageChange" @size-change="handleShiftSizeChange"/>
    </div>
        </el-tab-pane>

        <!-- ===================== 公共工位字典 ===================== -->
        <el-tab-pane label="公共工位字典" name="station">
          <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
              <el-select v-model="stationSearch.stationKind" class="!w-44" clearable placeholder="工位类别">
                <el-option v-for="o in STATION_KIND_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
              </el-select>
              <el-select v-model="stationSearch.status" class="!w-36" clearable placeholder="状态">
                <el-option :value="1" label="启用"/>
                <el-option :value="0" label="停用"/>
              </el-select>
              <el-input v-model="stationSearch.keyword" class="!w-56" clearable placeholder="编码/名称"
                        @keyup.enter="searchStation"/>
              <el-button :icon="Search" type="primary" @click="searchStation">查询</el-button>
              <el-button :icon="Refresh" @click="resetStation">重置</el-button>
              <el-button v-perm="'opd:station:add'" :icon="Plus" class="ml-auto" data-testid="station-add-btn"
                         type="success" @click="handleStationAdd">新增工位
              </el-button>
            </div>
            <el-table v-loading="stationLoading" :data="stationData" border data-testid="station-dict-table"
                      style="width: 100%">
              <el-table-column label="工位编码" prop="stationCode" width="150"/>
              <el-table-column label="工位名称" prop="stationName" min-width="150"/>
              <el-table-column align="center" label="类别" prop="stationKindText" width="110"/>
              <el-table-column align="center" label="应到岗位" prop="requiredStaffTypeName" width="100"/>
              <el-table-column align="center" label="在岗人数" prop="capacity" width="90"/>
              <el-table-column align="center" label="电话" prop="phone" width="120"/>
              <el-table-column align="center" label="排序" prop="sortNo" width="70"/>
              <el-table-column align="center" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{
                      row.status === 1 ? '启用' : '停用'
                    }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="备注" prop="remark" min-width="130" show-overflow-tooltip/>
              <el-table-column align="center" fixed="right" label="操作" width="150">
                <template #default="{ row }">
                  <el-button v-perm="'opd:station:add'" data-testid="station-edit-btn" link type="primary"
                             @click="handleStationEdit(row)">修改
                  </el-button>
                  <el-button v-perm="'opd:station:delete'" data-testid="station-delete-btn" link type="danger"
                             @click="handleStationDelete(row)">删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination :current-page="stationPage.pageNum" :page-size="stationPage.pageSize"
                           :page-sizes="PAGE_SIZES"
                           :total="stationPage.total" background
                           class="mt-3 justify-end" layout="total, prev, pager, next, sizes"
                           @current-change="handleStationPageChange" @size-change="handleStationSizeChange"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 工位编辑弹窗：公共工位字典新增/修改（row 空走新增），与门诊工位派岗页共用同一组件 -->
    <OpdStationEdit v-model="stationDialog" :row="stationEditRow" @saved="loadStation"/>

    <!-- 班次编辑弹窗：展示完整详情，仅名称可改（时间/科室/类型/状态只读） -->
    <ScheduleShiftEdit
        v-model="shiftEditVisible"
        :departments="departments"
        :row="shiftEditRow"
        @saved="loadShifts"
    />

    <!-- 新增班次弹窗：完整建条（防重/时间校验在后端 shiftUpsert） -->
    <ScheduleDictAdd
        v-model="shiftAddVisible"
        :departments="departments"
        @saved="handleShiftSearch"
    />
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {getShiftListPage} from '@/api/appoint';
import {deleteOpdStation, getOpdStationPage} from '@/api/opdStation';
import {getDepartmentSelectList} from '@/api/system';
import {SCHEDULE_TYPE_TEXT, SHIFT_SCOPE_OPTIONS, SHIFT_SCOPE_TEXT} from '@/lib/scheduleShift';
import {STATION_KIND_OPTIONS} from '@/lib/opdStation';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import OpdStationEdit from './OpdStationEdit.vue';
import ScheduleDictAdd from './ScheduleDictAdd.vue';
import ScheduleShiftEdit from './ScheduleShiftEdit.vue';

// ========== 班次字典维护（查询/分页/新增/编辑） ==========
// 已有班次的编辑只开放改名（时间/科室/类型是铺底口径，界面不可改）；
// 新增是完整建条（名称/起止时间/科室/类型），复用后端 shiftUpsert 的校验。
const departments = ref([]);
const shifts = ref([]);
const shiftLoading = ref(false);
const activeDictTab = ref('shift');
const shiftKeyword = ref('');
// 适用域筛选：不选就是四册混列；各排班场景按 SHIFT_SCOPE 各取所需，页面只做维护
const shiftUseScope = ref(null);
const shiftPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadShifts = async () => {
  shiftLoading.value = true;
  try {
    const res = await getShiftListPage({
      pageNum: shiftPagination.value.pageNum,
      pageSize: shiftPagination.value.pageSize,
      keyword: shiftKeyword.value || undefined,
      useScope: shiftUseScope.value ?? undefined,
    });
    shifts.value = res.data?.records || [];
    shiftPagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载班次字典失败:', error);
  } finally {
    shiftLoading.value = false;
  }
};
const handleShiftSearch = () => {
  shiftPagination.value.pageNum = 1;
  loadShifts();
};
const handleShiftPageChange = (val) => {
  shiftPagination.value.pageNum = val;
  loadShifts();
};
const handleShiftSizeChange = (val) => {
  shiftPagination.value.pageSize = val;
  shiftPagination.value.pageNum = 1;
  loadShifts();
};
const shiftDeptName = (deptId) => {
  if (!deptId)
    return '全院通用';
  const hit = departments.value.find((d) => String(d.id) === String(deptId));
  return hit?.deptName || `科室${deptId}`;
};
// ---------- 班次「编辑已有」与「新增建条」：表单与提交在 ScheduleShiftEdit / ScheduleDictAdd 组件里 ----------
const shiftEditVisible = ref(false);
const shiftEditRow = ref(null);
const handleShiftEdit = (row) => {
  shiftEditRow.value = row;
  shiftEditVisible.value = true;
};
const shiftAddVisible = ref(false);
const handleShiftAdd = () => {
  shiftAddVisible.value = true;
};
// 班次类型文案（1上午/2下午/3全天/4凌晨/5夜班）与列表列同一口径
const shiftLabel = SCHEDULE_TYPE_TEXT;

// ---------- 公共工位字典维护（20261025 从门诊工位派岗页搬回，与 OpdStationView 同 API 同数据） ----------
const stationLoading = ref(false);
const stationData = ref([]);
const stationPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const stationSearch = ref({stationKind: null, status: null, keyword: ''});
const stationDialog = ref(false);
const stationEditRow = ref(null);
const loadStation = async () => {
  stationLoading.value = true;
  try {
    const res = await getOpdStationPage({
      ...stationSearch.value,
      pageNum: stationPage.value.pageNum,
      pageSize: stationPage.value.pageSize,
    });
    stationData.value = res.data?.records || [];
    stationPage.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载工位字典失败:', error);
  } finally {
    stationLoading.value = false;
  }
};
const searchStation = () => {
  stationPage.value.pageNum = 1;
  loadStation();
};
const resetStation = () => {
  stationSearch.value = {stationKind: null, status: null, keyword: ''};
  searchStation();
};
const handleStationPageChange = (val) => {
  stationPage.value.pageNum = val;
  loadStation();
};
const handleStationSizeChange = (val) => {
  stationPage.value.pageSize = val;
  stationPage.value.pageNum = 1;
  loadStation();
};
const handleStationAdd = () => {
  stationEditRow.value = null;
  stationDialog.value = true;
};
const handleStationEdit = (row) => {
  stationEditRow.value = row;
  stationDialog.value = true;
};
const handleStationDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除工位「${row.stationName}」吗？（已有在岗派岗的删不掉）`, '删除工位', {type: 'warning'});
    await deleteOpdStation(row.id);
    ElMessage.success('删除成功');
    loadStation();
  } catch (error) /* 错误已由全局拦截器统一提示；confirm 取消同路静默 */ {
  }
};

const loadDepartments = async () => {
  try {
    const res = await getDepartmentSelectList({deptType: 1});
    departments.value = res.data || [];
  } catch (error) {
    console.error('加载科室下拉失败:', error);
  }
};

onMounted(() => {
  // 科室列表同时服务列表「适用科室」列与两个弹窗，必须同一份
  loadDepartments();
  loadShifts();
  // 工位字典默认 tab 是班次，切到工位 tab 时按需加载（handleDictTabChange）
});

// tab 切换：工位字典每次切入重拉，保证派岗台改动后本页也新鲜
const handleDictTabChange = (name) => {
  if (name === 'station') {
    loadStation();
  }
};
</script>

<style scoped>
/* 表格线条加深一档：默认 --el-table-border-color #ebeef5 在投影/低对比屏上几乎看不见。
   只作用于本页两个字典表（班次/工位），slate-300 = #cbd5e1 */
:deep(.el-table) {
  --el-table-border-color: #cbd5e1;
}

:deep(.el-table th.el-table__cell) {
  font-size: 15px;
}

:deep(.filter-row .el-button),
:deep(.filter-row .el-input__inner),
:deep(.filter-row .el-select__placeholder),
:deep(.filter-row .el-select__selected-item) {
  font-size: 15px;
}
</style>
