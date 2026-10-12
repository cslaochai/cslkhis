<template>
  <el-dialog v-model="visible" :title="`设备记录 — ${detail?.equipmentName || ''}`" width="820px">
    <template v-if="detail">
      <div class="text-sm text-gray-500 mb-2">最近维保记录（{{ detail.recentMaintains?.length || 0 }} 条）</div>
      <el-table :data="detail.recentMaintains || []" border data-testid="maintain-records" size="small">
        <el-table-column label="类型" width="70">
          <template #default="{ row }">{{ maintainTypeText(row) }}</template>
        </el-table-column>
        <el-table-column label="维保日期" width="110">
          <template #default="{ row }">{{ fmtDate(row.maintainDate) }}</template>
        </el-table-column>
        <el-table-column label="故障描述" min-width="140" prop="faultDesc" show-overflow-tooltip>
          <template #default="{ row }">{{ row.faultDesc || '—' }}</template>
        </el-table-column>
        <el-table-column label="处理结果" min-width="140" prop="handleResult" show-overflow-tooltip>
          <template #default="{ row }">{{ row.handleResult || '—' }}</template>
        </el-table-column>
        <el-table-column label="结果" prop="maintainResultText" width="70"/>
        <el-table-column label="维保人" prop="handlerName" width="90"/>
        <el-table-column label="操作" width="70">
          <template #default="{ row }">
            <el-button v-perm="'asset:equipment:delete'" link size="small" type="danger" @click="delMaintain(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="text-sm text-gray-500 mt-4 mb-2">最近计量记录（{{ detail.recentMeterings?.length || 0 }} 条）</div>
      <el-table :data="detail.recentMeterings || []" border data-testid="metering-records" size="small">
        <el-table-column label="类型" width="70">
          <template #default="{ row }">{{ meteringTypeText(row) }}</template>
        </el-table-column>
        <el-table-column label="计量日期" width="110">
          <template #default="{ row }">{{ fmtDate(row.meteringDate) }}</template>
        </el-table-column>
        <el-table-column label="有效期至" width="110">
          <template #default="{ row }">{{ fmtDate(row.validUntil) }}</template>
        </el-table-column>
        <el-table-column label="结果" prop="meteringResultText" width="70"/>
        <el-table-column label="证书编号" prop="certNo" width="130">
          <template #default="{ row }">{{ row.certNo || '—' }}</template>
        </el-table-column>
        <el-table-column label="检定机构" min-width="130" prop="agency" show-overflow-tooltip>
          <template #default="{ row }">{{ row.agency || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ row }">
            <el-button v-perm="'asset:equipment:delete'" link size="small" type="danger" @click="delMetering(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getEquipmentDetail, maintainDelete, meteringDelete} from '@/api/equipment';
import {dictLabelText} from '@/lib/utils';
import {fmtDate} from '@/lib/equipment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 设备 id：打开时自拉档案与最近维保/计量记录
  id: {type: [Number, String], default: null},
  // 字典由父页一次加载后传入（与维保/计量登记弹框共用同一份）
  maintainTypeDict: {type: Array, default: () => []},
  meteringTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);

watch(visible, async (open) => {
  if (!open || props.id == null) return;
  detail.value = null;
  try {
    const res = await getEquipmentDetail(props.id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  }
});

const maintainTypeText = (row) => dictLabelText(props.maintainTypeDict, row.maintainType);
const meteringTypeText = (row) => dictLabelText(props.meteringTypeDict, row.meteringType);

const delMaintain = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该维保记录？删除后档案最近维保日期按剩余记录重算。', '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await maintainDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      const detailRes = await getEquipmentDetail(detail.value.id);
      if (detailRes.code === 200)
        detail.value = detailRes.data;
      emit('saved');
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
const delMetering = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该计量记录？', '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await meteringDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      const detailRes = await getEquipmentDetail(detail.value.id);
      if (detailRes.code === 200)
        detail.value = detailRes.data;
      emit('saved');
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
</script>
