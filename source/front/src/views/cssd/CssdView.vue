<template>
  <div>
    <!-- 器械包模板页签已拆为独立菜单 2932（sql/187）；本页只剩追溯链，拆掉 el-tabs 外壳 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="条码/包名/科室" style="width: 200px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'asset:cssd:edit'" data-testid="cssd-receive-btn" plain type="primary"
                     @click="openReceive">器械回收登记
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="cssd-table" stripe>
        <el-table-column label="器械包条码" prop="packNo" width="180"/>
        <el-table-column label="器械包名称" min-width="130" prop="packName" show-overflow-tooltip/>
        <el-table-column label="申领科室" prop="deptName" width="110">
          <template #default="{ row }">{{ row.deptName || '—' }}</template>
        </el-table-column>
        <el-table-column label="灭菌方式" width="100">
          <template #default="{ row }">{{ methodText(row.sterilizeMethod) }}</template>
        </el-table-column>
        <el-table-column label="灭菌锅次" prop="sterilizerNo" width="100">
          <template #default="{ row }">{{ row.sterilizerNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="批次号" prop="batchNo" width="100">
          <template #default="{ row }">{{ row.batchNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="最近流转" width="160">
          <template #default="{ row }">{{ fmtTime(row.lastNodeTime) }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.status < 6" v-perm="'asset:cssd:edit'" link size="small" type="primary"
                       @click="openAdvance(row)">流转
            </el-button>
            <el-button link size="small" @click="openDetail(row)">追溯链</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 回收登记 -->
    <el-dialog v-model="receiveVisible" :close-on-click-modal="false" title="器械回收登记" width="680px">
      <el-form label-width="100px">
        <el-form-item label="器械包条码">
          <el-input v-model="receiveForm.packNo" placeholder="不填自动生成（CSSD+日期+序号）" style="width: 280px"/>
        </el-form-item>
        <el-form-item label="器械包名称" required>
          <el-select v-model="receiveForm.packName" :fit-input-width="false" allow-create default-first-option
                     filterable placeholder="从模板目录选择" style="width: 280px"
                     @change="onPackNameChange">
            <el-option v-for="t in templateOptions" :key="t.id" :label="t.packName" :value="t.packName"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="packItemsPreview.length" label="组成清单">
          <el-table :data="packItemsPreview" border max-height="260" size="small" style="width: 420px">
            <el-table-column label="名称" min-width="150" prop="itemName"/>
            <el-table-column label="规格" prop="spec" width="120">
              <template #default="{ row }">{{ row.spec || '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="基数" width="90">
              <template #default="{ row }">{{ row.quantity }}{{ row.unit }}</template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="申领科室">
          <el-select v-model="receiveForm.deptId" :fit-input-width="false" clearable filterable
                     placeholder="请选择科室" style="width: 280px" @change="onReceiveDeptChange">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="灭菌方式" required>
          <el-select v-model="receiveForm.sterilizeMethod" :fit-input-width="false" style="width: 280px">
            <el-option v-for="d in methodDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="receiveForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button v-perm="'asset:cssd:edit'" type="primary" @click="submitReceive">登记</el-button>
      </template>
    </el-dialog>

    <!-- 流转 -->
    <el-dialog v-model="advanceVisible" :close-on-click-modal="false" :title="`流转 — ${advanceForm.packNo}`"
               width="500px">
      <div class="mb-3 text-sm">
        下一节点：
        <el-tag type="primary">{{ advanceForm.nextNodeName }}</el-tag>
      </div>
      <el-form label-width="100px">
        <el-form-item label="操作人">
          <el-input v-model="advanceForm.operatorName" placeholder="不填默认当前登录人" style="width: 220px"/>
        </el-form-item>
        <template v-if="advanceForm.nextNodeName.startsWith('灭菌')">
          <el-form-item label="灭菌锅次" required>
            <el-input v-model="advanceForm.sterilizerNo" placeholder="如 1号锅 B03" style="width: 220px"/>
          </el-form-item>
          <el-form-item label="批次号" required>
            <el-input v-model="advanceForm.batchNo" style="width: 220px"/>
          </el-form-item>
        </template>
        <template v-if="advanceForm.nextNodeName.startsWith('储存')">
          <el-form-item label="节点结果" required>
            <el-radio-group v-model="advanceForm.result">
              <el-radio :value="1">合格（入储存待发放）</el-radio>
              <el-radio :value="2">不合格（自动退回清洗）</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input v-model="advanceForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="advanceVisible = false">取消</el-button>
        <el-button v-perm="'asset:cssd:edit'" type="primary" @click="submitAdvance">确认流转</el-button>
      </template>
    </el-dialog>

    <!-- 模板编辑弹窗已随 2932 拆分移除（sql/187） -->

    <!-- 追溯链 -->
    <el-dialog v-model="detailVisible" :title="`追溯链 — ${detail?.packNo || ''}`" width="640px">
      <template v-if="detail">
        <div class="text-sm text-gray-500 mb-3">
          {{ detail.packName }} · {{ methodText(detail.sterilizeMethod) }} ·
          当前状态
          <el-tag :type="statusTag(detail.status)">{{ statusText(detail.status) }}</el-tag>
        </div>
        <el-timeline data-testid="cssd-trace-timeline">
          <el-timeline-item v-for="t in detail.traces || []" :key="t.id"
                            :timestamp="`${fmtTime(t.nodeTime)} · ${t.operatorName || '—'}`"
                            :type="t.result === 2 ? 'danger' : 'primary'">
            <span class="font-bold">{{ t.nodeTypeText }}</span>
            <el-tag v-if="t.result === 2" class="ml-1" size="small" type="danger">不合格</el-tag>
            <span v-if="t.sterilizerNo" class="text-xs text-gray-500 ml-2">锅次 {{ t.sterilizerNo }} / 批次 {{
                t.batchNo
              }}</span>
            <div v-if="t.remark" class="text-xs text-gray-500">{{ t.remark }}</div>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 消毒供应 CSSD（G22，菜单 903）
 *
 * 追溯链：回收登记（建包）→ 清洗 → 打包 → 灭菌（必填锅次/批次）→ 储存（不合格自动退回清洗）→ 发放。
 * 追溯节点只增不改；详情弹窗展示全链路时间线。
 * 器械包模板目录已拆为独立菜单 2932「器械包模板」（sql/187）；
 * 回收登记的包名下拉照旧走 cssdTemplateSelectList（仅启用模板），选包带出默认灭菌方式与组成清单。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  cssdAdvance,
  cssdListPage,
  cssdReceive,
  cssdTemplateSelectList,
  getCssdDetail,
  getCssdTemplateDetail,
} from '@/api/cssd';
import {getDepartmentSelectList, getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---------------- 字典 ----------------
const statusDict = ref([]);
const methodDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const methodText = (v) => dictLabelText(methodDict.value, v);
const statusTag = (v) => ({
  1: 'warning',
  2: 'warning',
  3: 'primary',
  4: 'primary',
  5: 'success',
  6: 'info'
}[v] || 'info');
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.CSSD_PACK_STATUS},${DICT_TYPE.CSSD_STERIL_METHOD}`);
    statusDict.value = res?.data?.[DICT_TYPE.CSSD_PACK_STATUS] || [];
    methodDict.value = res?.data?.[DICT_TYPE.CSSD_STERIL_METHOD] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 科室下拉 ----------------
const deptOptions = ref([]);
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    if (res.code === 200)
      deptOptions.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
// ---------------- 追溯列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await cssdListPage({
      keyword: query.keyword.trim() || undefined,
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
  Object.assign(query, {keyword: '', status: null, pageNum: 1});
  loadList();
};
// ---------------- 回收登记 ----------------
const templateOptions = ref([]);
const loadTemplateOptions = async () => {
  try {
    const res = await cssdTemplateSelectList();
    if (res.code === 200)
      templateOptions.value = res.data || [];
  } catch (e) {
    console.error('加载器械包模板失败', e);
  }
};
const receiveVisible = ref(false);
const receiveForm = reactive({
  packNo: '', packName: '', deptId: null, deptName: '', sterilizeMethod: 1, remark: '',
});
const packItemsPreview = ref([]);
const openReceive = () => {
  Object.assign(receiveForm, {packNo: '', packName: '', deptId: null, deptName: '', sterilizeMethod: 1, remark: ''});
  packItemsPreview.value = [];
  receiveVisible.value = true;
};
const onReceiveDeptChange = (id) => {
  const d = deptOptions.value.find((x) => String(x.id) === String(id));
  receiveForm.deptName = d?.deptName || '';
};
/** 选包软带出：默认灭菌方式与组成清单（仍可手改包名走自定义） */
const onPackNameChange = async (name) => {
  const tpl = templateOptions.value.find((x) => x.packName === name);
  if (!tpl) {
    packItemsPreview.value = [];
    return;
  }
  receiveForm.sterilizeMethod = Number(tpl.sterilizeMethod) || receiveForm.sterilizeMethod;
  try {
    const res = await getCssdTemplateDetail(tpl.id);
    if (res.code === 200)
      packItemsPreview.value = res.data?.items || [];
  } catch (e) {
    console.error(e);
    packItemsPreview.value = [];
  }
};
const submitReceive = async () => {
  if (!String(receiveForm.packName || '').trim()) {
    ElMessage.warning('请选择器械包');
    return;
  }
  try {
    const res = await cssdReceive({
      packNo: receiveForm.packNo.trim() || undefined,
      packName: String(receiveForm.packName).trim(),
      deptId: receiveForm.deptId ?? undefined,
      deptName: receiveForm.deptName.trim() || undefined,
      sterilizeMethod: receiveForm.sterilizeMethod,
      remark: receiveForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(`回收登记成功，条码 ${res.data?.packNo}`);
      receiveVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  }
};
// ---------------- 流转 ----------------
const advanceVisible = ref(false);
const advanceForm = reactive({
  packId: null, packNo: '', nextNodeName: '',
  operatorName: '', sterilizerNo: '', batchNo: '', result: 1, remark: '',
});
// 状态 → 下一节点名
const NEXT_NODE = {1: '清洗', 2: '打包', 3: '灭菌', 4: '储存（灭菌完成入储存）', 5: '发放'};
const openAdvance = (row) => {
  Object.assign(advanceForm, {
    packId: Number(row.id), packNo: row.packNo, nextNodeName: NEXT_NODE[row.status] || '—',
    operatorName: '', sterilizerNo: '', batchNo: '', result: 1, remark: '',
  });
  advanceVisible.value = true;
};
const submitAdvance = async () => {
  if (advanceForm.nextNodeName.startsWith('灭菌') && (!advanceForm.sterilizerNo.trim() || !advanceForm.batchNo.trim())) {
    ElMessage.warning('推进到灭菌节点必须填灭菌锅次与批次号');
    return;
  }
  try {
    const res = await cssdAdvance({
      packId: advanceForm.packId,
      operatorName: advanceForm.operatorName || undefined,
      sterilizerNo: advanceForm.sterilizerNo.trim() || undefined,
      batchNo: advanceForm.batchNo.trim() || undefined,
      result: advanceForm.result,
      remark: advanceForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('流转成功');
      advanceVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '流转失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('流转失败');
  }
};
// ---------------- 详情（追溯链） ----------------
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  try {
    const res = await getCssdDetail(row.id);
    if (res.code === 200) {
      detail.value = res.data;
      detailVisible.value = true;
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  }
};
const fmtTime = (v) => (v ? String(v).slice(0, 19).replace('T', ' ') : '—');
// 器械包模板维护页签已拆为独立菜单 2932（sql/187）；loadTemplateOptions 与
// getCssdTemplateDetail 保留供回收登记选包带出默认灭菌方式与组成清单。
onMounted(() => {
  loadDicts();
  loadDepts();
  loadList();
  loadTemplateOptions();
});
</script>
