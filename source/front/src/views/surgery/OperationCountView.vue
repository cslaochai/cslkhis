<template>
  <div class="operation-count-page" data-testid="operation-count-view">
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <el-select v-model="countApplyId" class="!w-96" clearable data-testid="g15-count-apply" filterable
                   placeholder="选择手术申请单" @change="loadCount">
          <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
        </el-select>
        <el-button v-perm="'ipd:operationCount:add'" :icon="Plus" data-testid="g15-count-create" type="primary"
                   @click="openCountCreate">建立清点单
        </el-button>
        <el-button
            v-if="countRow && (countRow.canCountBefore || countRow.canCountClosure || countRow.canCountFinal)"
            v-perm="'ipd:operationCount:edit'"
            data-testid="g15-count-phase"
            type="warning"
            @click="openPhase"
        >
          登记清点
        </el-button>
      </div>

      <div v-if="!countRow" class="py-8 text-center text-sm text-slate-400" data-testid="g15-count-empty">
        该手术尚未建立器械清点单（清点是可选登记的；一旦建了单，三轮对不上会锁死手术完成登记）
      </div>

      <div v-else class="space-y-4">
        <div v-if="countRow.warningText"
             class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] text-amber-700">
          <el-icon class="mr-1 align-middle">
            <Warning/>
          </el-icon>
          {{ countRow.warningText }}
        </div>
        <el-descriptions :column="4" border data-testid="g15-count-desc" size="small">
          <el-descriptions-item label="清点单号">{{ text(countRow.countNo) }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ text(countRow.patientName) }}</el-descriptions-item>
          <el-descriptions-item label="术式">{{ text(countRow.plannedOperationName) }}</el-descriptions-item>
          <el-descriptions-item label="手术间">{{ text(countRow.operationRoom) }}</el-descriptions-item>
          <el-descriptions-item label="当前阶段">{{ text(countRow.phaseText) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ text(countRow.statusText) }}</el-descriptions-item>
          <el-descriptions-item label="洗手护士">{{ text(countRow.instrumentNurseName) }}</el-descriptions-item>
          <el-descriptions-item label="巡回护士">{{ text(countRow.circulateNurseName) }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="countRow.items || []" data-testid="g15-count-item-table" style="width: 100%">
          <el-table-column label="#" prop="seqNo" width="50"/>
          <el-table-column label="类别" width="90">
            <template #default="{ row }">{{ text(row.itemCategoryText) }}</template>
          </el-table-column>
          <el-table-column label="名称 / 规格" min-width="180">
            <template #default="{ row }">
              <div class="text-slate-900">{{ text(row.itemName) }}</div>
              <div class="text-[11px] text-slate-400">{{ text(row.spec) }}</div>
            </template>
          </el-table-column>
          <el-table-column align="center" label="术前" width="90">
            <template #default="{ row }">{{
                row.beforeQty === null || row.beforeQty === undefined ? '—' : row.beforeQty
              }}
            </template>
          </el-table-column>
          <el-table-column align="center" label="关体前" width="90">
            <template #default="{ row }">
              {{ row.closureQty === null || row.closureQty === undefined ? '—' : row.closureQty }}
            </template>
          </el-table-column>
          <el-table-column align="center" label="关体后" width="90">
            <template #default="{ row }">{{
                row.finalQty === null || row.finalQty === undefined ? '—' : row.finalQty
              }}
            </template>
          </el-table-column>
          <el-table-column label="关体后 vs 术前" min-width="150">
            <template #default="{ row }">
              <span v-if="row.consistent === null || row.consistent === undefined" class="text-[11px] text-slate-400">尚未比对</span>
              <span v-else-if="row.consistent" class="text-[11px] text-green-600">一致</span>
              <span v-else class="text-[11px] font-medium text-red-600">差异 {{
                  row.diffQty > 0 ? '+' : ''
                }}{{ row.diffQty }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="text-[12px] text-slate-500">
          合计：术前 {{ countRow.totalBefore }} / 关体前 {{ countRow.totalClosure }} / 关体后 {{ countRow.totalFinal }}
        </div>
        <div v-if="countRow.diffNote"
             class="rounded border border-red-200 bg-red-50 px-3 py-2 text-[12px] text-red-700">
          差异说明：{{ countRow.diffNote }}
        </div>
      </div>
    </div>

    <OperationCountAdd
        v-model="countCreateVisible"
        :applies="applies"
        :employees="employees"
        :preset-apply-id="countApplyId"
        @saved="onCountCreated"
    />

    <OperationCountPhaseEdit
        v-model="phaseVisible"
        :employees="employees"
        :row="countRow"
        @saved="loadCount"
    />
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Plus, Warning} from '@element-plus/icons-vue';
import {getOperationCountByApply} from '@/api/operationCount';
import {getOperationApplyListPage} from '@/api/inpatientOperation';
import {getEmployeeList} from '@/api/system';
import {applyLabel, text} from '@/lib/operationCount';
import OperationCountAdd from './OperationCountAdd.vue';
import OperationCountPhaseEdit from './OperationCountPhaseEdit.vue';

// ==================== 手术申请单 / 员工候选 ====================
const applies = ref([]);
const employees = ref([]);
const loadBaseData = async () => {
  try {
    const res = await getOperationApplyListPage({pageNum: 1, pageSize: 200});
    applies.value = (res.data?.records || []);
  } catch (e) {
    console.error('加载手术申请失败:', e);
  }
  try {
    const res = await getEmployeeList({});
    employees.value = (res.data || []);
  } catch (e) {
    console.error('加载员工失败:', e);
  }
};
// ==================== 清点单 ====================
const countApplyId = ref('');
const countRow = ref(null);
const countLoading = ref(false);
const loadCount = async () => {
  if (!countApplyId.value) {
    countRow.value = null;
    return;
  }
  countLoading.value = true;
  try {
    const res = await getOperationCountByApply(countApplyId.value);
    countRow.value = (res.data || null);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    countLoading.value = false;
  }
};
const countCreateVisible = ref(false);
const openCountCreate = () => {
  countCreateVisible.value = true;
};
// 建单成功后切到新单所属的申请单并加载（与拆分前 openCountCreate→submitCountCreate 的行为一致）
const onCountCreated = async (applyId) => {
  countApplyId.value = applyId;
  await loadCount();
};
const phaseVisible = ref(false);
const openPhase = () => {
  phaseVisible.value = true;
};
onMounted(() => {
  loadBaseData();
});
</script>
