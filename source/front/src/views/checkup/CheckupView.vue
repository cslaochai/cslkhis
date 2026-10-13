<template>
  <div>
    <!-- 体检登记（「体检套餐」页签已拆为独立菜单 2930，sql/185） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="姓名 / 体检编号" style="width: 200px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.recordStatus" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 130px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'checkup:manage:add'" data-testid="checkup-create-btn" plain type="primary"
                     @click="openCreate">体检登记
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="checkup-table" stripe>
        <el-table-column label="体检编号" prop="recordNo" width="210"/>
        <el-table-column label="体检人" prop="patientName" width="100"/>
        <el-table-column label="性别" prop="gender" width="60">
          <template #default="{ row }">{{ ({1: '男', 2: '女', 9: '未知'})[row.gender] || '—' }}</template>
        </el-table-column>
        <el-table-column label="年龄" prop="age" width="60"/>
        <el-table-column label="对象" prop="personType" width="70">
          <template #default="{ row }">{{ personText(row.personType) }}</template>
        </el-table-column>
        <el-table-column label="套餐" min-width="150" prop="packageName" show-overflow-tooltip/>
        <el-table-column align="right" label="金额(元)" prop="totalAmount" width="90"/>
        <el-table-column label="体检日期" prop="checkupDate" width="105"/>
        <el-table-column label="状态" prop="recordStatus" width="95">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.recordStatus)">{{ statusText(row.recordStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="185">
          <template #default="{ row }">
            <el-button v-perm="'checkup:manage:edit'" link size="small" type="primary" @click="openDetail(row)">结果
            </el-button>
            <el-button v-if="row.recordStatus === 1" v-perm="'checkup:manage:edit'" link size="small" type="warning"
                       @click="startOne(row)">开始体检
            </el-button>
            <el-button v-if="row.recordStatus === 3" v-perm="'checkup:manage:edit'" link size="small" type="success"
                       @click="openConclude(row)">总检
            </el-button>
            <el-button v-if="row.recordStatus === 1" v-perm="'checkup:manage:delete'" link size="small" type="danger"
                       @click="deleteOne(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 登记弹窗 -->
    <CheckupAdd v-model="createVisible" @saved="loadList"/>

    <!-- 结果明细弹窗 -->
    <CheckupDetail v-model="detailVisible" :id="detailId" :row="detailRow" :status-dict="statusDict"
                   :flag-dict="flagDict" @conclude="openConclude"/>

    <!-- 总检弹窗 -->
    <CheckupConcludeEdit v-model="concludeVisible" :row="concludeRow" @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * 体检管理（G23，菜单 905，挂患者中心）
 *
 * 体检套餐已拆为独立菜单 2930「体检套餐」（sql/185，含套餐新增/编辑/停用维护）；
 * 本页登记弹窗的套餐下拉照旧走 checkupPackageListPage（仅启用）—— 数据流不断。
 * 登记状态机：1 已登记 → 2 检查中 → 3 已完成 → 4 已出报告（终态禁改禁删）。
 * 登记时按套餐项目预生成结果空行；出报告要求全部明细已录 + 总检结论必填。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  checkupRecordDelete,
  checkupRecordListPage,
  checkupRecordStart,
} from '@/api/checkup';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {checkupStatusTag as statusTag} from '@/lib/checkup';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import CheckupAdd from './CheckupAdd.vue';
import CheckupDetail from './CheckupDetail.vue';
import CheckupConcludeEdit from './CheckupConcludeEdit.vue';
// ---------------- 字典 ----------------
const statusDict = ref([]);
const personDict = ref([]);
const flagDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const personText = (v) => dictLabelText(personDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.CHECKUP_RECORD_STATUS},${DICT_TYPE.CHECKUP_PERSON_TYPE},${DICT_TYPE.CHECKUP_RESULT_FLAG}`);
    statusDict.value = res?.data?.[DICT_TYPE.CHECKUP_RECORD_STATUS] || [];
    personDict.value = res?.data?.[DICT_TYPE.CHECKUP_PERSON_TYPE] || [];
    flagDict.value = res?.data?.[DICT_TYPE.CHECKUP_RESULT_FLAG] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 登记列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', recordStatus: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await checkupRecordListPage({
      keyword: query.keyword.trim() || undefined,
      recordStatus: query.recordStatus ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    rows.value = res?.data?.records || [];
    total.value = Number(res?.data?.total || 0);
  } catch (e) {
    console.error('加载体检登记失败', e);
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  query.keyword = '';
  query.recordStatus = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 弹框开关（表单逻辑在子组件内） ----------------
const createVisible = ref(false);
const openCreate = () => {
  createVisible.value = true;
};
const detailVisible = ref(false);
const detailId = ref(null);
const detailRow = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailRow.value = row;
  detailVisible.value = true;
};
const concludeVisible = ref(false);
const concludeRow = ref(null);
const openConclude = (row) => {
  concludeRow.value = row;
  concludeVisible.value = true;
};
// ---------------- 行操作 ----------------
const startOne = async (row) => {
  try {
    await checkupRecordStart(row.id);
    ElMessage.success('已开始体检');
    loadList();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const deleteOne = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该体检登记？仅已登记状态可删。', '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    await checkupRecordDelete(row.id);
    ElMessage.success('已删除');
    loadList();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
