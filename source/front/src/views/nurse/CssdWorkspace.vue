<template>
  <div class="space-y-4" data-testid="cssd-workspace">
    <!-- 工具条：本科室器械包（按当前岗位科室收口） -->
    <div class="flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white p-3">
      <el-input v-model="keyword" clearable class="!w-52" placeholder="条码/包名" @keyup.enter="reload"/>
      <el-select v-model="status" :fit-input-width="false" class="!w-44" placeholder="状态">
        <el-option v-for="d in statusOptions" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
      </el-select>
      <el-button :icon="Search" type="primary" @click="reload">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
      <div class="ml-auto flex items-center gap-2">
        <el-button v-perm="'asset:waste:add'" plain @click="wasteVisible = true">医废登记</el-button>
        <el-button v-perm="'asset:cssd:apply'" type="primary" @click="applyVisible = true">申请回收</el-button>
      </div>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white p-3">
      <el-table v-loading="loading" :data="rows" data-testid="cssd-my-table" stripe>
        <el-table-column label="器械包条码" prop="packNo" width="170"/>
        <el-table-column label="器械包名称" min-width="140" prop="packName" show-overflow-tooltip/>
        <el-table-column label="灭菌方式" width="110">
          <template #default="{ row }">{{ methodText(row.sterilizeMethod) }}</template>
        </el-table-column>
        <el-table-column label="申请人" width="100">
          <template #default="{ row }">{{ row.applyBy || row.createBy || '' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.applyTime || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="最近流转" width="160">
          <template #default="{ row }">{{ fmtTime(row.lastNodeTime) }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="cssdStatusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.status === 6" v-perm="'asset:cssd:apply'" link size="small" type="primary"
                       @click="sign(row)">签收
            </el-button>
            <el-button link size="small" @click="openDetail(row)">追溯链</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">本科室暂未申领器械包，点右上「申请回收」发起</div>
        </template>
      </el-table>
      <div class="mt-3 flex justify-end">
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </div>

    <!-- 申请回收（临床发起，落 0-待回收） -->
    <CssdApplyEdit v-model="applyVisible" :method-dict="methodDict" @saved="reload"/>

    <!-- 医废登记（科室产生，供应室后续交接与处置） -->
    <WasteRegisterAdd v-model="wasteVisible" :type-dict="wasteTypeDict" @saved="onWasteSaved"/>

    <!-- 追溯链（与供应室页同一个详情弹框） -->
    <CssdTraceDetail v-model="detailVisible" :id="detailId" :method-dict="methodDict" :status-dict="statusDict"/>
  </div>
</template>

<script setup>
/**
 * 护士站「消毒供应」工作区：临床侧唯一入口。
 *
 * 只做三件事——申请回收器械包、收到无菌包后签收、登记本科室医废。
 * 清洗/打包/灭菌/发放的流转全在供应室「器械包追溯」页（护士看不到也不需要看锅次批次）。
 * 列表按当前岗位科室收口（后端 myListPage 用 CurrentUser.deptId 过滤），跨科室的包不出现。
 */
import {onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {cssdMyListPage, cssdSign} from '@/api/cssd';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {cssdStatusTag, fmtTime} from '@/lib/cssd';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import CssdApplyEdit from '@/views/cssd/CssdApplyEdit.vue';
import CssdTraceDetail from '@/views/cssd/CssdTraceDetail.vue';
import WasteRegisterAdd from '@/views/waste/WasteRegisterAdd.vue';

const statusDict = ref([]);
const methodDict = ref([]);
const wasteTypeDict = ref([]);
// 护士只关心这三档：刚申请的、等签收的、已签收的；中间的清洗灭菌过程对科室无意义
const statusOptions = ref([
  {dictValue: 0, dictLabel: '待回收'},
  {dictValue: 6, dictLabel: '已发放待签收'},
  {dictValue: 7, dictLabel: '已签收'},
]);
const statusText = (v) => dictLabelText(statusDict.value, v) || dictLabelText(statusOptions.value, v);
const methodText = (v) => dictLabelText(methodDict.value, v);

const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(
        `${DICT_TYPE.CSSD_PACK_STATUS},${DICT_TYPE.CSSD_STERIL_METHOD},${DICT_TYPE.WASTE_TYPE}`);
    statusDict.value = res?.data?.[DICT_TYPE.CSSD_PACK_STATUS] || [];
    methodDict.value = res?.data?.[DICT_TYPE.CSSD_STERIL_METHOD] || [];
    wasteTypeDict.value = res?.data?.[DICT_TYPE.WASTE_TYPE] || [];
  } catch (e) {
    console.error('加载消毒供应字典失败', e);
  }
};

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const keyword = ref('');
const status = ref(null);
const pageNum = ref(1);
const pageSize = ref(DEFAULT_PAGE_SIZE);

const loadList = async () => {
  loading.value = true;
  try {
    const res = await cssdMyListPage({
      keyword: keyword.value.trim() || undefined,
      status: status.value ?? undefined,
      pageNum: pageNum.value, pageSize: pageSize.value,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    }
  } catch (e) {
    console.error('加载本科室器械包失败', e);
  } finally {
    loading.value = false;
  }
};
const reload = () => {
  pageNum.value = 1;
  loadList();
};
const reset = () => {
  keyword.value = '';
  status.value = null;
  reload();
};

const applyVisible = ref(false);
const wasteVisible = ref(false);
const onWasteSaved = () => ElMessage.success('医废已登记，供应室会来交接');

const sign = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确认签收器械包「${row.packName}」（${row.packNo}）？签收后本次循环闭环。`,
        '科室签收', {type: 'info', confirmButtonText: '确认签收', cancelButtonText: '取消'});
  } catch {
    return; // 取消
  }
  try {
    const res = await cssdSign({packId: row.id});
    if (res.code === 200) {
      ElMessage.success('签收成功');
      loadList();
    }
  } catch (e) {
    console.error('签收失败', e);
  }
};

const detailVisible = ref(false);
const detailId = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};

onMounted(() => {
  loadDicts();
  loadList();
});
</script>
