<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="交接单号/科室" style="width: 180px"
                      @keyup.enter="query.pageNum = 1; loadList()"/>
          </el-form-item>
          <el-form-item label="医废类别">
            <el-select v-model="query.wasteType" :fit-input-width="false" clearable placeholder="医废类别"
                       style="width: 130px">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 110px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="收集日期起">
            <el-date-picker v-model="query.collectDateBegin" placeholder="收集日期起" style="width: 150px"
                            type="date" value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item label="收集日期止">
            <el-date-picker v-model="query.collectDateEnd" placeholder="收集日期止" style="width: 150px"
                            type="date" value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'asset:waste:add'" data-testid="waste-create-btn" plain type="primary" @click="openCreate">
            医废登记
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="waste-table" stripe>
        <el-table-column label="交接单号" prop="wasteNo" width="170"/>
        <el-table-column label="类别" width="110">
          <template #default="{ row }">{{ typeText(row.wasteType) }}</template>
        </el-table-column>
        <el-table-column align="right" label="重量(kg)" prop="weightKg" width="90">
          <template #default="{ row }">{{ row.weightKg ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="产生科室" prop="deptName" width="110">
          <template #default="{ row }">{{ row.deptName || '—' }}</template>
        </el-table-column>
        <el-table-column label="收集时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.collectTime) }}</template>
        </el-table-column>
        <el-table-column label="收集人" prop="collectorName" width="90">
          <template #default="{ row }">{{ row.collectorName || '—' }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="交接人" prop="handoverName" width="90">
          <template #default="{ row }">{{ row.handoverName || '—' }}</template>
        </el-table-column>
        <el-table-column label="处置公司" min-width="140" prop="disposalCompany" show-overflow-tooltip>
          <template #default="{ row }">{{ row.disposalCompany || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="160">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" v-perm="'asset:waste:edit'" link size="small" type="primary"
                       @click="openHandover(row)">交接
            </el-button>
            <el-button v-if="row.status === 2" v-perm="'asset:waste:edit'" link size="small" type="success"
                       @click="openDispose(row)">处置确认
            </el-button>
            <el-button v-if="row.status === 1" v-perm="'asset:waste:delete'" link size="small" type="danger"
                       @click="del(row)">删除
            </el-button>
            <span v-if="row.status === 3" class="text-gray-400">已闭环</span>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 登记 -->
    <el-dialog v-model="createVisible" :close-on-click-modal="false" title="医废登记" width="520px">
      <el-form label-width="100px">
        <el-form-item label="医废类别" required>
          <el-select v-model="createForm.wasteType" :fit-input-width="false" style="width: 220px">
            <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="重量(kg)">
          <el-input-number v-model="createForm.weightKg" :min="0" :precision="2" style="width: 180px"/>
        </el-form-item>
        <el-form-item label="产生科室" required>
          <el-select v-model="createForm.deptId" :fit-input-width="false" filterable
                     placeholder="请选择科室" style="width: 220px" @change="onCreateDeptChange">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="收集时间" required>
          <el-date-picker v-model="createForm.collectTime" format="YYYY-MM-DD HH:mm:ss" style="width: 220px"
                          type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="收集人">
          <el-select v-model="createForm.collectorName" :fit-input-width="false" allow-create clearable
                     default-first-option filterable placeholder="不填默认当前登录人" style="width: 220px">
            <el-option v-for="e in employeeOptions" :key="e.id" :label="e.empName || e.name"
                       :value="e.empName || e.name"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button v-perm="'asset:waste:add'" type="primary" @click="submitCreate">登记</el-button>
      </template>
    </el-dialog>

    <!-- 交接 -->
    <el-dialog v-model="handoverVisible" :close-on-click-modal="false" :title="`医废交接 — ${handoverForm.wasteNo}`"
               width="440px">
      <el-form label-width="90px">
        <el-form-item label="交接人" required>
          <el-input v-model="handoverForm.handoverName" placeholder="接收方签字人" style="width: 220px"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handoverVisible = false">取消</el-button>
        <el-button v-perm="'asset:waste:edit'" type="primary" @click="submitHandover">确认交接</el-button>
      </template>
    </el-dialog>

    <!-- 处置 -->
    <el-dialog v-model="disposeVisible" :close-on-click-modal="false" :title="`处置确认 — ${disposeForm.wasteNo}`"
               width="440px">
      <el-form label-width="90px">
        <el-form-item label="处置公司" required>
          <el-input v-model="disposeForm.disposalCompany" style="width: 260px"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disposeVisible = false">取消</el-button>
        <el-button v-perm="'asset:waste:edit'" type="primary" @click="submitDispose">确认处置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 医废管理（G22，菜单 904）
 *
 * 三态闭环：1已登记 → 2已交接 → 3已处置，不可逆。
 * 已交接/已处置禁删（交接单是与处置公司的对外凭证）；仅已登记可删。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {wasteCreate, wasteDelete, wasteDispose, wasteHandover, wasteListPage} from '@/api/waste';
import {getDepartmentSelectList, getDictDataMapList, getEmployeeList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---------------- 字典 ----------------
const typeDict = ref([]);
const statusDict = ref([]);
const deptOptions = ref([]);
const employeeOptions = ref([]);
const typeText = (v) => dictLabelText(typeDict.value, v);
const statusText = (v) => dictLabelText(statusDict.value, v);
const statusTag = (v) => ({1: 'warning', 2: 'primary', 3: 'success'}[v] || 'info');
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.WASTE_TYPE},${DICT_TYPE.WASTE_STATUS}`);
    typeDict.value = res?.data?.[DICT_TYPE.WASTE_TYPE] || [];
    statusDict.value = res?.data?.[DICT_TYPE.WASTE_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadDeptsAndEmployees = async () => {
  try {
    const [d, e] = await Promise.all([getDepartmentSelectList({}), getEmployeeList({})]);
    deptOptions.value = d?.data || [];
    employeeOptions.value = e?.data || [];
  } catch (e) {
    console.error('加载科室/员工失败', e);
  }
};
// ---------------- 列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '', wasteType: null, status: null,
  collectDateBegin: '', collectDateEnd: '',
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await wasteListPage({
      keyword: query.keyword.trim() || undefined,
      wasteType: query.wasteType ?? undefined,
      status: query.status ?? undefined,
      collectDateBegin: query.collectDateBegin || undefined,
      collectDateEnd: query.collectDateEnd || undefined,
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
  Object.assign(query, {
    keyword: '', wasteType: null, status: null, collectDateBegin: '', collectDateEnd: '', pageNum: 1,
  });
  loadList();
};
// ---------------- 登记 ----------------
const createVisible = ref(false);
const createForm = reactive({
  wasteType: 1, weightKg: '', deptId: null, deptName: '',
  collectTime: '', collectorName: '',
});
const nowLocal = () => {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const openCreate = () => {
  Object.assign(createForm, {
    wasteType: 1, weightKg: '', deptId: null, deptName: '', collectTime: nowLocal(), collectorName: '',
  });
  createVisible.value = true;
};
const onCreateDeptChange = (id) => {
  const d = deptOptions.value.find((x) => String(x.id) === String(id));
  createForm.deptName = d?.deptName || '';
};
const submitCreate = async () => {
  if (!createForm.deptId) {
    ElMessage.warning('请选择产生科室');
    return;
  }
  if (!createForm.collectTime) {
    ElMessage.warning('请选择收集时间');
    return;
  }
  try {
    const res = await wasteCreate({
      wasteType: createForm.wasteType,
      weightKg: createForm.weightKg || undefined,
      deptId: createForm.deptId ?? undefined,
      deptName: createForm.deptName.trim(),
      collectTime: createForm.collectTime,
      collectorName: createForm.collectorName || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(`登记成功，单号 ${res.data?.wasteNo}`);
      createVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  }
};
// ---------------- 交接 / 处置 / 删除 ----------------
const handoverVisible = ref(false);
const handoverForm = reactive({id: null, wasteNo: '', handoverName: ''});
const openHandover = (row) => {
  Object.assign(handoverForm, {id: Number(row.id), wasteNo: row.wasteNo, handoverName: ''});
  handoverVisible.value = true;
};
const submitHandover = async () => {
  if (!handoverForm.handoverName.trim()) {
    ElMessage.warning('请填写交接人');
    return;
  }
  try {
    const res = await wasteHandover({id: handoverForm.id, handoverName: handoverForm.handoverName.trim()});
    if (res.code === 200) {
      ElMessage.success('交接成功');
      handoverVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
const disposeVisible = ref(false);
const disposeForm = reactive({id: null, wasteNo: '', disposalCompany: ''});
const openDispose = (row) => {
  Object.assign(disposeForm, {id: Number(row.id), wasteNo: row.wasteNo, disposalCompany: ''});
  disposeVisible.value = true;
};
const submitDispose = async () => {
  if (!disposeForm.disposalCompany.trim()) {
    ElMessage.warning('请填写处置公司');
    return;
  }
  try {
    const res = await wasteDispose({id: disposeForm.id, disposalCompany: disposeForm.disposalCompany.trim()});
    if (res.code === 200) {
      ElMessage.success('处置确认成功');
      disposeVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
const del = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该登记？已交接/已处置的记录不可删除。', '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await wasteDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      loadList();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
const fmtTime = (v) => (v ? String(v).slice(0, 19).replace('T', ' ') : '—');
onMounted(() => {
  loadDicts();
  loadDeptsAndEmployees();
  loadList();
});
</script>
