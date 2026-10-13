<template>
  <div class="p-5 space-y-4">
    <!-- 统计 -->
    <div class="space-y-3">
      <div class="grid grid-cols-7 gap-3">
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">在库</div>
          <div class="text-xl font-semibold text-[#0E9488] mt-1">{{ stats.inStock }} <span
              class="text-xs font-normal text-gray-400">袋</span></div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">已预留</div>
          <div class="text-xl font-semibold text-[#1269B5] mt-1">{{ stats.reserved }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">已发血</div>
          <div class="text-xl font-semibold text-[#6B7280] mt-1">{{ stats.issued }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">7 天内到期</div>
          <div class="text-xl font-semibold text-[#B91C1C] mt-1">{{ stats.expireSoon }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">今日入库</div>
          <div class="text-xl font-semibold text-[#1269B5] mt-1">{{ stats.todayIn }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">今日发血</div>
          <div class="text-xl font-semibold text-[#B45309] mt-1">{{ stats.todayOut }}</div>
        </div>
        <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
          <div class="text-xs text-gray-500">待配血</div>
          <div class="text-xl font-semibold text-[#B45309] mt-1">{{ stats.pendingMatch }}</div>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm flex items-center gap-6">
        <span class="text-sm text-gray-500">分血型在库：</span>
        <span v-for="t in stats.byBloodType" :key="t.bloodType" class="text-sm">
          <span class="font-medium">{{ t.bloodTypeText }}</span>
          <span class="ml-1 text-[#1269B5] font-semibold">{{ t.bagCount }}</span> 袋 /
          <span>{{ t.volumeTotal }}</span> ml
        </span>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <el-tabs v-model="activeTab" @tab-change="refreshAll">
        <!-- 库存台账 -->
        <el-tab-pane label="库存台账" name="inventory">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="invQuery.bagNo" clearable placeholder="血袋号" style="width: 160px"
                      @keyup.enter="invQuery.pageNum = 1; loadInventory()"/>
            <el-select v-model="invQuery.bloodType" :fit-input-width="false" clearable placeholder="血型"
                       style="width: 100px">
              <el-option v-for="d in bloodTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="invQuery.componentType" :fit-input-width="false" clearable placeholder="成分"
                       style="width: 120px">
              <el-option v-for="d in componentDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="invQuery.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 110px">
              <el-option v-for="d in invStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="invQuery.expireWithinDays" :fit-input-width="false" clearable placeholder="效期预警"
                       style="width: 130px">
              <el-option :value="7" label="7 天内到期"/>
              <el-option :value="3" label="3 天内到期"/>
              <el-option :value="0" label="已过期"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="invQuery.pageNum = 1; loadInventory()">查询</el-button>
            <el-button :icon="Refresh" @click="resetInv">重置</el-button>
            <div class="flex-1"/>
            <el-button v-perm="'medtech:bloodBank:add'" type="primary" @click="openInbound">血袋入库</el-button>
          </div>
          <el-table v-loading="invLoading" :data="invRows" data-testid="blood-inventory-table" size="small">
            <el-table-column label="血袋号" prop="bagNo" width="140"/>
            <el-table-column label="血型" width="100">
              <template #default="{ row }">{{ bloodTypeText(row.bloodType) }} / {{ rhText(row.rhType) }}</template>
            </el-table-column>
            <el-table-column label="成分" width="110">
              <template #default="{ row }">{{ componentText(row.componentType) }}</template>
            </el-table-column>
            <el-table-column align="right" label="血量(ml)" prop="volume" width="90"/>
            <el-table-column label="失效日期" prop="expireDate" width="105"/>
            <el-table-column label="效期" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.expireDays != null" :type="expireTag(row)" size="small">
                  {{ Number(row.expireDays) < 0 ? '已过期' : `${row.expireDays}天` }}
                </el-tag>
                <span v-else class="text-gray-300">-</span>
              </template>
            </el-table-column>
            <el-table-column label="存放位置" prop="storageLoc" show-overflow-tooltip width="110"/>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ invStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="申请单号" prop="applyNo" show-overflow-tooltip width="110"/>
            <el-table-column fixed="right" label="操作" width="230">
              <template #default="{ row }">
                <el-button v-if="Number(row.status) === 1" v-perm="'medtech:bloodBank:edit'" link size="small"
                           type="primary" @click="reserve(row)">预留
                </el-button>
                <template v-if="Number(row.status) === 2">
                  <el-button v-perm="'medtech:bloodBank:edit'" link size="small" type="warning" @click="issue(row)">
                    发血
                  </el-button>
                  <el-button v-perm="'medtech:bloodBank:edit'" link size="small" type="primary"
                             @click="cancelReserve(row)">取消预留
                  </el-button>
                </template>
                <el-button v-if="[1, 2].includes(Number(row.status))" v-perm="'medtech:bloodBank:delete'" link
                           size="small" type="danger" @click="scrap(row)">报废
                </el-button>
                <el-button v-if="[1, 2].includes(Number(row.status))" v-perm="'medtech:bloodBank:delete'" link
                           size="small" type="info" @click="returnBag(row)">退回
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="invQuery.pageNum" v-model:page-size="invQuery.pageSize" :total="invTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadInventory"/>
          </div>
        </el-tab-pane>

        <!-- 交叉配血 -->
        <el-tab-pane label="交叉配血" name="crossmatch">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="cmQuery.matchNo" clearable placeholder="配血编号" style="width: 160px"
                      @keyup.enter="cmQuery.pageNum = 1; loadCrossmatch()"/>
            <el-input v-model="cmQuery.bagNo" clearable placeholder="血袋号" style="width: 140px"
                      @keyup.enter="cmQuery.pageNum = 1; loadCrossmatch()"/>
            <el-input v-model="cmQuery.patientName" clearable placeholder="患者姓名" style="width: 130px"
                      @keyup.enter="cmQuery.pageNum = 1; loadCrossmatch()"/>
            <el-select v-model="cmQuery.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 110px">
              <el-option v-for="d in cmStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="cmQuery.pageNum = 1; loadCrossmatch()">查询</el-button>
            <el-button :icon="Refresh" @click="resetCm">重置</el-button>
            <div class="flex-1"/>
            <el-button v-perm="'medtech:bloodBank:add'" type="primary" @click="openCmCreate">新建配血单</el-button>
          </div>
          <el-table v-loading="cmLoading" :data="cmRows" data-testid="crossmatch-table" size="small">
            <el-table-column label="配血编号" prop="matchNo" width="150"/>
            <el-table-column label="患者" prop="patientName" width="90"/>
            <el-table-column label="患者血型" width="110">
              <template #default="{ row }">{{ bloodTypeText(row.patientBloodType) }} / {{
                  rhText(row.patientRhType)
                }}
              </template>
            </el-table-column>
            <el-table-column label="血袋号" prop="bagNo" width="130"/>
            <el-table-column label="血袋血型" width="110">
              <template #default="{ row }">{{ bloodTypeText(row.bagBloodType) }} / {{
                  rhText(row.bagRhType)
                }}
              </template>
            </el-table-column>
            <el-table-column label="方法" width="110">
              <template #default="{ row }">{{ cmMethodText(row.method) }}</template>
            </el-table-column>
            <el-table-column label="结果" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.result != null" :type="Number(row.result) === 1 ? 'success' : 'danger'" size="small">
                  {{ cmResultText(row.result) }}
                </el-tag>
                <span v-else class="text-gray-300">-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ cmStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="配血人" prop="operator" width="90"/>
            <el-table-column label="复核人" prop="verifier" width="90"/>
            <el-table-column fixed="right" label="操作" width="140">
              <template #default="{ row }">
                <el-button v-if="Number(row.status) === 1" v-perm="'medtech:bloodBank:edit'" link size="small"
                           type="primary" @click="openCmExecute(row)">执行配血
                </el-button>
                <el-button v-if="Number(row.status) === 2" v-perm="'medtech:bloodBank:edit'" link size="small"
                           type="success" @click="doCmVerify(row)">复核
                </el-button>
                <el-button v-if="[1, 2].includes(Number(row.status))" v-perm="'medtech:bloodBank:delete'" link
                           size="small" type="danger" @click="doCmVoid(row)">作废
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="cmQuery.pageNum" v-model:page-size="cmQuery.pageSize" :total="cmTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadCrossmatch"/>
          </div>
        </el-tab-pane>

        <!-- 流水 -->
        <el-tab-pane label="出入库流水" name="log">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="logQuery.bagNo" clearable placeholder="血袋号" style="width: 160px"
                      @keyup.enter="logQuery.pageNum = 1; loadLogs()"/>
            <el-select v-model="logQuery.bizType" :fit-input-width="false" clearable placeholder="业务类型"
                       style="width: 120px">
              <el-option v-for="t in BIZ_TYPES" :key="t.value" :label="t.label" :value="t.value"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="logQuery.pageNum = 1; loadLogs()">查询</el-button>
            <el-button :icon="Refresh"
                       @click="logQuery.bagNo = ''; logQuery.bizType = null; logQuery.pageNum = 1; loadLogs()">重置
            </el-button>
          </div>
          <el-table v-loading="logLoading" :data="logRows" data-testid="blood-log-table" size="small">
            <el-table-column label="血袋号" prop="bagNo" width="140"/>
            <el-table-column label="业务" width="100">
              <template #default="{ row }">
                <el-tag :type="[2, 4].includes(Number(row.bizType)) ? 'warning' : 'primary'" size="small">
                  {{ row.bizTypeText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="申请单号" prop="applyNo" show-overflow-tooltip width="120"/>
            <el-table-column label="原因" min-width="160" prop="reason" show-overflow-tooltip/>
            <el-table-column label="操作人" prop="operator" width="100"/>
            <el-table-column label="操作时间" width="140">
              <template #default="{ row }">{{ fmtTime(row.operateTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="logQuery.pageNum" v-model:page-size="logQuery.pageSize" :total="logTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadLogs"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 入库弹窗（表单与提交已拆至 BloodBankInboundAdd.vue） -->
    <BloodBankInboundAdd v-model="inVisible" :blood-type-dict="bloodTypeDict" :component-dict="componentDict"
                         :rh-dict="rhDict" @saved="loadInventory(); loadStats()"/>

    <!-- 配血单弹窗（表单与提交已拆至 BloodBankCrossmatchAdd.vue） -->
    <BloodBankCrossmatchAdd v-model="cmVisible" :blood-type-dict="bloodTypeDict" :rh-dict="rhDict"
                            @saved="loadCrossmatch(); loadStats()"/>

    <!-- 执行配血弹窗（表单与提交已拆至 BloodBankCrossmatchExecuteEdit.vue） -->
    <BloodBankCrossmatchExecuteEdit v-model="cmExecVisible" :cm-method-dict="cmMethodDict"
                                    :cm-result-dict="cmResultDict" :row="cmExecRow"
                                    @saved="loadCrossmatch(); loadStats()"/>
  </div>
</template>

<script setup>
/**
 * 血库储血台账（G17，菜单 411）
 *
 * 四个 tab：库存台账（入库/预留/发血/报废/退回 + 效期预警）、交叉配血
 * （开单 → 执行 → 复核双人，相合自动预留血袋）、出入库流水（只增不改）、分血型统计。
 * 发血前置：配血复核相合 → 血袋已预留 → 带 apply_no 发血。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  bloodCancelReserve,
  bloodIssue,
  bloodReserve,
  bloodReturn,
  bloodScrap,
  crossmatchVerify,
  crossmatchVoid,
  getBloodInventoryPage,
  getBloodInventoryStats,
  getBloodLogPage,
  getCrossmatchPage,
} from '@/api/medicaltech';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import BloodBankInboundAdd from './BloodBankInboundAdd.vue';
import BloodBankCrossmatchAdd from './BloodBankCrossmatchAdd.vue';
import BloodBankCrossmatchExecuteEdit from './BloodBankCrossmatchExecuteEdit.vue';

const activeTab = ref('inventory');
// ---------------- 字典 ----------------
const bloodTypeDict = ref([]);
const rhDict = ref([]);
const componentDict = ref([]);
const invStatusDict = ref([]);
const cmStatusDict = ref([]);
const cmResultDict = ref([]);
const cmMethodDict = ref([]);
const bloodTypeText = (v) => dictLabelText(bloodTypeDict.value, v);
const rhText = (v) => dictLabelText(rhDict.value, v);
const componentText = (v) => dictLabelText(componentDict.value, v);
const invStatusText = (v) => dictLabelText(invStatusDict.value, v);
const cmStatusText = (v) => dictLabelText(cmStatusDict.value, v);
const cmResultText = (v) => dictLabelText(cmResultDict.value, v);
const cmMethodText = (v) => dictLabelText(cmMethodDict.value, v);
const loadDicts = async () => {
  try {
    // ⚠ 后端 getDictDataMapList 一次最多 5 个 type，超了直接报错 —— 分两批
    const res1 = await getDictDataMapList(`${DICT_TYPE.BLOOD_TYPE},${DICT_TYPE.BLOOD_RH},${DICT_TYPE.BLOOD_COMPONENT},${DICT_TYPE.BLOOD_INVENTORY_STATUS}`);
    const res2 = await getDictDataMapList(`${DICT_TYPE.CROSSMATCH_STATUS},${DICT_TYPE.CROSSMATCH_RESULT},${DICT_TYPE.CROSSMATCH_METHOD}`);
    bloodTypeDict.value = res1?.data?.[DICT_TYPE.BLOOD_TYPE] || [];
    rhDict.value = res1?.data?.[DICT_TYPE.BLOOD_RH] || [];
    componentDict.value = res1?.data?.[DICT_TYPE.BLOOD_COMPONENT] || [];
    invStatusDict.value = res1?.data?.[DICT_TYPE.BLOOD_INVENTORY_STATUS] || [];
    cmStatusDict.value = res2?.data?.[DICT_TYPE.CROSSMATCH_STATUS] || [];
    cmResultDict.value = res2?.data?.[DICT_TYPE.CROSSMATCH_RESULT] || [];
    cmMethodDict.value = res2?.data?.[DICT_TYPE.CROSSMATCH_METHOD] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 统计 ----------------
const stats = reactive({
  inStock: 0, reserved: 0, issued: 0, expireSoon: 0, todayIn: 0, todayOut: 0, pendingMatch: 0,
  byBloodType: [],
});
const loadStats = async () => {
  try {
    const res = await getBloodInventoryStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
// ---------------- 库存 ----------------
const invLoading = ref(false);
const invRows = ref([]);
const invTotal = ref(0);
const invQuery = reactive({
  bagNo: '', bloodType: null, componentType: null,
  status: null, expireWithinDays: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadInventory = async () => {
  invLoading.value = true;
  try {
    const res = await getBloodInventoryPage({
      bagNo: invQuery.bagNo.trim() || undefined,
      bloodType: invQuery.bloodType ?? undefined,
      componentType: invQuery.componentType ?? undefined,
      status: invQuery.status ?? undefined,
      expireWithinDays: invQuery.expireWithinDays ?? undefined,
      pageNum: invQuery.pageNum, pageSize: invQuery.pageSize,
    });
    if (res.code === 200) {
      invRows.value = res.data?.records || [];
      invTotal.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    invLoading.value = false;
  }
};
const resetInv = () => {
  Object.assign(invQuery, {
    bagNo: '',
    bloodType: null,
    componentType: null,
    status: null,
    expireWithinDays: null,
    pageNum: 1
  });
  loadInventory();
};
const expireTag = (row) => {
  const d = Number(row.expireDays);
  if (d < 0)
    return 'danger';
  if (d <= 7)
    return 'warning';
  return 'success';
};
// 入库（表单与提交已拆至 BloodBankInboundAdd.vue，这里只管开关）
const inVisible = ref(false);
const openInbound = () => {
  inVisible.value = true;
};
// 袋动作
const doBagAction = (fn, row, done) => {
  fn(row).then((res) => {
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      done();
    } 
  }).catch((e) => {
    console.error(e);
    
  });
};
const reserve = (row) => doBagAction((r) => bloodReserve({bagId: r.id}), row, () => {
  loadInventory();
  loadStats();
});
const cancelReserve = (row) => doBagAction((r) => bloodCancelReserve({bagId: r.id}), row, () => {
  loadInventory();
  loadStats();
});
const issue = (row) => {
  ElMessageBox.prompt('用血申请单号（必填）', `发血：${row.bagNo}`, {
    confirmButtonText: '发血', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '用血申请单号不能为空',
  }).then(({value}) => doBagAction(() => bloodIssue({bagId: row.id, applyNo: value.trim()}), row, () => {
    loadInventory();
    loadStats();
  })).catch(() => {
  });
};
const scrap = (row) => {
  ElMessageBox.prompt('报废原因（必填）', `报废：${row.bagNo}`, {
    confirmButtonText: '确定', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '报废原因不能为空',
  }).then(({value}) => doBagAction(() => bloodScrap({bagId: row.id, reason: value.trim()}), row, () => {
    loadInventory();
    loadStats();
  })).catch(() => {
  });
};
const returnBag = (row) => {
  ElMessageBox.prompt('退回原因（必填）', `退回：${row.bagNo}`, {
    confirmButtonText: '确定', cancelButtonText: '取消', inputPattern: /\S+/, inputErrorMessage: '退回原因不能为空',
  }).then(({value}) => doBagAction(() => bloodReturn({bagId: row.id, reason: value.trim()}), row, () => {
    loadInventory();
    loadStats();
  })).catch(() => {
  });
};
// ---------------- 交叉配血 ----------------
const cmLoading = ref(false);
const cmRows = ref([]);
const cmTotal = ref(0);
const cmQuery = reactive({
  matchNo: '',
  bagNo: '',
  patientName: '',
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
});
const loadCrossmatch = async () => {
  cmLoading.value = true;
  try {
    const res = await getCrossmatchPage({
      matchNo: cmQuery.matchNo.trim() || undefined,
      bagNo: cmQuery.bagNo.trim() || undefined,
      patientName: cmQuery.patientName.trim() || undefined,
      status: cmQuery.status ?? undefined,
      pageNum: cmQuery.pageNum, pageSize: cmQuery.pageSize,
    });
    if (res.code === 200) {
      cmRows.value = res.data?.records || [];
      cmTotal.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    cmLoading.value = false;
  }
};
const resetCm = () => {
  Object.assign(cmQuery, {matchNo: '', bagNo: '', patientName: '', status: null, pageNum: 1});
  loadCrossmatch();
};
// 新建配血单（表单与提交已拆至 BloodBankCrossmatchAdd.vue，这里只管开关）
const cmVisible = ref(false);
const openCmCreate = () => {
  cmVisible.value = true;
};
// 执行配血（表单与提交已拆至 BloodBankCrossmatchExecuteEdit.vue，这里只管开关与行快照）
const cmExecVisible = ref(false);
const cmExecRow = ref(null);
const openCmExecute = (row) => {
  cmExecRow.value = row;
  cmExecVisible.value = true;
};
const doCmVerify = (row) => {
  crossmatchVerify(row.id).then((res) => {
    if (res.code === 200) {
      ElMessage.success('复核通过' + (Number(row.result) === 1 ? '，血袋已自动预留' : ''));
      loadCrossmatch();
      loadStats();
    } 
  }).catch((e) => {
    console.error(e);
    
  });
};
const doCmVoid = (row) => {
  crossmatchVoid(row.id).then((res) => {
    if (res.code === 200) {
      ElMessage.success('已作废');
      loadCrossmatch();
      loadStats();
    } 
  }).catch((e) => {
    console.error(e);
    
  });
};
// ---------------- 流水 ----------------
const logLoading = ref(false);
const logRows = ref([]);
const logTotal = ref(0);
const logQuery = reactive({bagNo: '', bizType: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const BIZ_TYPES = [
  {value: 1, label: '入库'}, {value: 2, label: '发血'}, {value: 3, label: '退回'},
  {value: 4, label: '报废'}, {value: 5, label: '预留'}, {value: 6, label: '取消预留'},
];
const loadLogs = async () => {
  logLoading.value = true;
  try {
    const res = await getBloodLogPage({
      bagNo: logQuery.bagNo.trim() || undefined,
      bizType: logQuery.bizType ?? undefined,
      pageNum: logQuery.pageNum, pageSize: logQuery.pageSize,
    });
    if (res.code === 200) {
      logRows.value = res.data?.records || [];
      logTotal.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    logLoading.value = false;
  }
};
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-');
const refreshAll = () => {
  loadStats();
  if (activeTab.value === 'inventory')
    loadInventory();
};
onMounted(() => {
  loadDicts();
  loadStats();
  loadInventory();
  loadCrossmatch();
  loadLogs();
});
</script>
