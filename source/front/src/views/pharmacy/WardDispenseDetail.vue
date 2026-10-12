<template>
  <!-- 明细弹窗 -->
  <el-dialog v-model="visible" :title="`摆药单 ${detail?.dispenseNo || ''}`" data-testid="wd-detail-dialog"
             width="920px">
    <div v-if="detail" v-loading="detailLoading" class="space-y-3">
      <div class="flex gap-6 text-sm text-slate-600 flex-wrap">
        <span>摆药日期：<b class="text-slate-800">{{ detail.dispenseDate }}</b></span>
        <span>患者：<b class="text-slate-800">{{ detail.patientName || '—' }}</b>（{{ detail.patientNo || '—' }}）</span>
        <span>病区：<b class="text-slate-800">{{ detail.wardName || '—' }}</b></span>
        <span>
          状态：<el-tag :type="orderStatusTag(detail.status)" size="small">{{
            orderStatusText(detail.status)
          }}</el-tag>
        </span>
        <span>生成人：{{ detail.generateBy || '—' }}</span>
      </div>
      <el-table :data="detail.items || []" border data-testid="wd-item-table" size="small">
        <el-table-column label="药品" min-width="150" prop="drugName"/>
        <el-table-column label="规格" prop="spec" width="100"/>
        <el-table-column align="right" label="数量" width="90">
          <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="90">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="itemStatusTag(row.status)" size="small">{{ itemStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="库存(配前→配后)" width="140">
          <template #default="{ row }">
            <span v-if="row.stockBefore != null" class="text-xs">{{ row.stockBefore }} → {{ row.stockAfter }}</span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column label="配药人" prop="dispenserName" width="90">
          <template #default="{ row }">{{ row.dispenserName || '—' }}</template>
        </el-table-column>
        <el-table-column label="核对人" prop="checkerName" width="90">
          <template #default="{ row }">{{ row.checkerName || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <el-button v-if="canDispense(row.status)" v-perm="'pharmacy:wardDispense:edit'" :icon="Van" :loading="actLoading"
                       data-testid="wd-dispense-btn" plain
                       size="small" type="primary" @click="onDispense(row)">配药
            </el-button>
            <el-button v-if="canCheck(row.status)" v-perm="'pharmacy:wardDispense:edit'" :icon="CircleCheck"
                       :loading="actLoading" data-testid="wd-check-btn" plain
                       size="small" type="success" @click="onCheck(row)">核对
            </el-button>
            <el-button v-if="canReturn(row.status)" v-perm="'pharmacy:wardDispense:edit'" :icon="RefreshLeft"
                       :loading="actLoading" data-testid="wd-return-btn" plain
                       size="small" type="danger" @click="onReturn(row)">退药
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-4 text-sm text-slate-400">本单暂无明细</div>
        </template>
      </el-table>
      <div v-if="(detail.items || []).some((i: any) => i.returnReason)" class="text-xs text-slate-500">
        <template v-for="i in detail.items.filter((x: any) => x.returnReason)" :key="i.id">
          <div>退药留痕：{{ i.drugName }} × {{ i.quantity }} —— {{ i.returnReason }}（{{ i.returnBy }}）</div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * 病区摆药明细弹窗 —— 从 views/pharmacy/WardDispenseView.vue 抽出
 *
 * 明细级三段动作（配药→核对→退药）只在本弹窗里出现，故一并搬入；每次动作成功后 emit('changed')
 * 让父页刷新列表与统计。口径不变：配药扣库存并计费、退药回库负冲账、操作不可逆。
 */
import { computed, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CircleCheck, RefreshLeft, Van } from '@element-plus/icons-vue';
import { checkItem, dispenseItem, getWardDispenseDetail, returnItem } from '@/api/wardDispense';
import { dictLabelText, formatMoney } from '@/lib/utils';
import { canCheck, canDispense, canReturn, itemStatusTag, orderStatusTag } from '@/lib/wardDispense';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  id: { type: [Number, String], default: null },
  orderStatusDict: { type: Array, default: () => [] },
  itemStatusDict: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'changed']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const orderStatusText = (v) => dictLabelText(props.orderStatusDict, v);
const itemStatusText = (v) => dictLabelText(props.itemStatusDict, v);

const detail = ref(null);
const detailLoading = ref(false);
const actLoading = ref(false);

const load = async (id) => {
  detailLoading.value = true;
  try {
    const res = await getWardDispenseDetail(id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '查询详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  } finally {
    detailLoading.value = false;
  }
};

const refreshDetail = async () => {
  if (detail.value?.id)
    await load(detail.value.id);
  emit('changed');
};

const onDispense = async (item) => {
  actLoading.value = true;
  try {
    const res = await dispenseItem({ itemId: item.id });
    if (res.code === 200) {
      ElMessage.success(`明细已配药（${item.drugName} × ${item.quantity}），库存已扣减并计费`);
      await refreshDetail();
    } else {
      ElMessage.error(res.message || '配药失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('配药失败');
  } finally {
    actLoading.value = false;
  }
};
const onCheck = async (item) => {
  actLoading.value = true;
  try {
    const res = await checkItem({ itemId: item.id });
    if (res.code === 200) {
      ElMessage.success('核对通过');
      await refreshDetail();
    } else {
      ElMessage.error(res.message || '核对失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('核对失败');
  } finally {
    actLoading.value = false;
  }
};
const onReturn = async (item) => {
  try {
    const { value } = await ElMessageBox.prompt(`确认为「${item.drugName} × ${item.quantity}」办理退药？回库后金额将负冲账，操作不可逆。`, '退药原因（必填）', {
      confirmButtonText: '确认退药',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '退药原因必填'
    });
    actLoading.value = true;
    const res = await returnItem({ itemId: item.id, reason: value.trim() });
    if (res.code === 200) {
      ElMessage.success('退药完成（已回库并负冲账）');
      await refreshDetail();
    } else {
      ElMessage.error(res.message || '退药失败');
    }
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel')
      console.error(e);
  } finally {
    actLoading.value = false;
  }
};

watch(() => props.modelValue, (v) => {
  if (v && props.id) load(props.id);
});

defineExpose({ reload: () => props.id && load(props.id) });
</script>
