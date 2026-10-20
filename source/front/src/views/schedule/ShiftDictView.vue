<template>
  <!-- 班次字典（biz_shift）独立维护页：全院 4 册班次的唯一维护入口。
       门诊排班/护理排班/值班点位配置/人力配置标准等场景按「适用域」各取所需。
       20261019 从 ScheduleView.vue 的「班次字典」签抽出，随排班中心独立菜单一起归位。 -->
  <div class="w-full">
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
        <el-table-column label="班次名称" min-width="150" prop="shiftName"/>
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
import {Plus, Search} from '@element-plus/icons-vue';
import {getShiftListPage} from '@/api/appoint';
import {getDepartmentSelectList} from '@/api/system';
import {SCHEDULE_TYPE_TEXT, SHIFT_SCOPE_OPTIONS, SHIFT_SCOPE_TEXT} from '@/lib/scheduleShift';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import ScheduleDictAdd from './ScheduleDictAdd.vue';
import ScheduleShiftEdit from './ScheduleShiftEdit.vue';

// ========== 班次字典维护（查询/分页/新增/编辑） ==========
// 已有班次的编辑只开放改名（时间/科室/类型是铺底口径，界面不可改）；
// 新增是完整建条（名称/起止时间/科室/类型），复用后端 shiftUpsert 的校验。
const departments = ref([]);
const shifts = ref([]);
const shiftLoading = ref(false);
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
});
</script>

<style scoped>
/* 表格线条加深一档：默认 --el-table-border-color #ebeef5 在投影/低对比屏上几乎看不见。
   只作用于本页班次字典表，slate-300 = #cbd5e1 */
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
