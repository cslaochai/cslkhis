<template>
  <div>
    <!-- 统计卡片（真实全库计数） -->
    <div class="mb-3 grid grid-cols-3 gap-4">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5">
            <Clock class="h-5 w-5 text-amber-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-amber-600">{{ statusCounts.pending }}</p>
            <p class="text-xs text-slate-500">待发药（全部）</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-blue-50 p-2.5">
            <Check class="h-5 w-5 text-blue-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-blue-600">{{ statusCounts.dispensed }}</p>
            <p class="text-xs text-slate-500">已发药（全部）</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-red-50 p-2.5">
            <RefreshLeft class="h-5 w-5 text-red-500"/>
          </div>
          <div>
            <p class="text-lg font-bold text-red-500">{{ statusCounts.returned }}</p>
            <p class="text-xs text-slate-500">已退药（全部）</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 查询卡（真实传参） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="{ patientName, prescriptionNo, statusFilter }" inline @submit.prevent>
        <el-form-item label="患者姓名">
          <el-input v-model="patientName" :prefix-icon="Search" class="!w-52" clearable placeholder="患者姓名"
                    @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="处方号">
          <el-input v-model="prescriptionNo" :prefix-icon="Search" class="!w-56" clearable placeholder="处方号"
                    @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="发药状态">
          <el-select v-model="statusFilter" class="!w-36" clearable placeholder="全部" @change="handleSearch">
            <el-option :value="1" label="待发药"/>
            <el-option :value="2" label="已发药"/>
            <el-option :value="3" label="已退药"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡：发药明细列表（每行 = 药品 × 处方） -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="dispensingList" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column label="患者" prop="patientName" width="110"/>
        <el-table-column class-name="font-mono" label="处方号" prop="prescriptionNo" width="180"/>
        <el-table-column label="药品" min-width="170" prop="drugName" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
                v-if="isControlledFlag(row.specialFlag)"
                :type="rowFlagTag(row.specialFlag)"
                class="mr-1"
                data-testid="pharm-flag-tag"
                effect="dark"
                size="small"
            >{{ rowFlagText(row.specialFlag) }}
            </el-tag>
            <span>{{ row.drugName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="规格" prop="specification" show-overflow-tooltip width="110"/>
        <el-table-column align="center" label="数量" width="90">
          <template #default="{ row }">{{ row.quantity }}{{ row.unit || '' }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="90">
          <template #default="{ row }">
            <span class="font-medium text-red-600">¥{{ row.amount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <span :class="['inline-block rounded px-2 py-0.5 font-medium', statusColor(row.dispensingStatus)]">
              {{ statusLabel(row.dispensingStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="发药药师" prop="pharmacistName" width="100"/>
        <el-table-column label="发药时间" prop="dispensingTime" width="160"/>
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="viewDetail(row)">详情</el-button>
            <template v-if="row.dispensingStatus === 1">
              <el-button v-perm="'pharmacy:dispensing:edit'" :icon="Check" data-testid="pharm-dispense-btn" link
                         type="primary" @click.stop="startDispense(row, false)">发药
              </el-button>
              <el-button v-perm="'pharmacy:dispensing:edit'" data-testid="pharm-dispense-all-btn" link plain
                         type="primary" @click.stop="startDispense(row, true)">整单发药
              </el-button>
            </template>
            <el-button v-else-if="row.dispensingStatus === 2" v-perm="'pharmacy:dispensing:edit'" link type="warning"
                       @click.stop="handleReturn(row)">退药
            </el-button>
            <span v-else class="text-slate-400">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="dispensingList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无发药记录
      </div>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 发药复核弹窗（单行 / 整单共用；麻精限量 + 双人复核 + 超量理由） -->
    <PharmacyWindowReview v-model="reviewVisible" :pending-action="pendingAction" :row-flag-tag="rowFlagTag"
                          :row-flag-text="rowFlagText" @saved="refreshAll"/>

    <!-- 发药详情弹窗 -->
    <PharmacyWindowDetail v-model="detailVisible" :id="detailRow?.id" :row-flag-tag="rowFlagTag"
                          :row-flag-text="rowFlagText"/>
  </div>
</template>

<script setup>
/**
 * 药房发药窗口
 *
 * G10 增量（麻精药品特殊管理，评审一票否决项）：
 * 1. **列表带管制分类标签**（`specialFlag` 由后端按药品主数据补齐）——
 *    药师在点「发药」之前就能看出这行是不是麻精，而不是发完才知道。
 * 2. **发药前预检 + 双人复核弹窗**：麻醉药品 / 第一类精神药品必须指定复核药师，
 *    且不得与发药人同一人（《医疗机构麻醉药品、第一类精神药品管理规定》第17条）。
 *    预检（`/narcotic/precheck`）会把「能不能发、为什么不能、每个管制品种限几日」一次说清，
 *    避免"点一次、被拒一次、再猜哪里错"。
 * 3. **超量理由**：第二类精神药品超 7 日须由医师注明理由后才放行（《处方管理办法》第24条），
 *    理由随发药写进专册留痕。
 *
 * ⚠ 这三条在前端都只是**提前告知**，真正的闸门在 his-emr 的 NarcoticControlService：
 * 前端算错只是提示不准，后端漏判才是合规事故。所以这里不"筛掉"任何超限行，
 * 一律把后端的结论原样摆出来。
 */
import {onMounted, ref} from 'vue';
import {Check, Clock, Refresh, RefreshLeft, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import request from '@/api/request';
import {getDispensingList, returnDrug} from '@/api/dispensing';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {isControlledFlag, specialFlagTagType, specialFlagText} from '@/lib/drugSpecialFlag';
import {dispensingStatusColor as statusColor, dispensingStatusLabel as statusLabel} from '@/lib/pharmacyWindow';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import PharmacyWindowReview from './PharmacyWindowReview.vue';
import PharmacyWindowDetail from './PharmacyWindowDetail.vue';

const loading = ref(false);
const dispensingList = ref([]);
const detailVisible = ref(false);
const detailRow = ref(null);
const patientName = ref('');
const prescriptionNo = ref('');
const statusFilter = ref(null);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const statusCounts = ref({pending: 0, dispensed: 0, returned: 0});
// ---------------- 麻精相关 ----------------
/** 管制分类文案唯一来源：字典 his_drug_special_flag */
const specialDict = ref([]);
const loadNarcoticSupport = async () => {
  try {
    // ⚠ 取值必须是 `res.data[dictType]`：request 拦截器已把响应剥成 `{code,message,data}`，
    //    而 getDictDataMapList 又包了一层 `{...res, data: {dictType: [...]}}`。
    //    少写这层 `.data` 不报错，但 `specialDict` 恒空 → 列表里的管制分类标签全渲染「未知(n)」。
    //    "麻精药品不能和维生素C长得一样"就靠这个标签，静默失效等于把管制提示抹掉了。
    const res = await getDictDataMapList(DICT_TYPE.DRUG_SPECIAL_FLAG);
    specialDict.value = res?.data?.[DICT_TYPE.DRUG_SPECIAL_FLAG] || [];
  } catch (e) {
    // 拿不到字典 → 分类渲染「未知(n)」，不回落成"普通药品"（普通药品不受管制，回落会洗掉告警）
    console.error('加载药品特殊管理分类字典失败', e);
  }
};
/** 管制分类标签（空值/未维护 → 不显示标签，而不是显示「普通」） */
const rowFlagText = (flag) => specialFlagText(flag, specialDict.value);
const rowFlagTag = (flag) => specialFlagTagType(flag) || 'info';
const loadStatusCount = async () => {
  try {
    const res = await request.get('/charge/dispensing/statusCount');
    if (res.data)
      statusCounts.value = res.data;
  } catch (error) {
    console.error('加载发药统计失败:', error);
  }
};
// ---------------- 发药复核弹窗（单行 / 整单共用） ----------------
const reviewVisible = ref(false);
/** 待执行的发药动作：整单会带 row.prescriptionId，单行会用 row.id */
const pendingAction = ref(null);
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    if (patientName.value.trim())
      params.patientName = patientName.value.trim();
    if (prescriptionNo.value.trim())
      params.prescriptionNo = prescriptionNo.value.trim();
    if (statusFilter.value != null)
      params.dispensingStatus = statusFilter.value;
    const res = await getDispensingList(params);
    dispensingList.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载发药列表失败:', error);
    
  } finally {
    loading.value = false;
  }
};
const refreshAll = () => {
  loadData();
  loadStatusCount();
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  patientName.value = '';
  prescriptionNo.value = '';
  statusFilter.value = null;
  handleSearch();
};
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.pageNum = 1;
  loadData();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadData();
};
const viewDetail = (row) => {
  detailRow.value = row;
  detailVisible.value = true;
};
/**
 * 发药统一入口（单行 / 整单）：只把「要做什么」交给复核弹框，
 *   预检 → 摆结论 → （麻精）选复核药师 /（二类精神超限）填超量理由 → 提交
 *   全在 PharmacyWindowReview 里走。
 *
 * 单行与整单共用一套弹窗，因为两者的判定完全一致（都按处方整体判限量），
 * 差别只在最后调哪个接口。分成两套弹窗只会让口径跑偏。
 */
const startDispense = (row, whole) => {
  if (!row.prescriptionId) {
    ElMessage.warning('该发药记录未关联处方，无法校验');
    return;
  }
  pendingAction.value = {type: whole ? 'whole' : 'one', row};
  reviewVisible.value = true;
};
// 退药（回库存 + 处方状态联动）
const handleReturn = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt('请输入退药原因', `退药 - ${row.drugName}`, {
      confirmButtonText: '确认退药',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：患者过敏 / 开错药',
      inputValidator: (v) => (v && v.trim() ? true : '退药原因不能为空'),
    });
    await returnDrug(row.id, value.trim());
    ElMessage.success('退药成功，库存已回加');
    refreshAll();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
onMounted(() => {
  loadNarcoticSupport();
  refreshAll();
});
</script>
