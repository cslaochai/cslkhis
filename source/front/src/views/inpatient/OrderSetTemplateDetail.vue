<template>
  <el-dialog v-model="visible" :title="`组套明细：${viewRow?.templateName || ''}`" top="8vh" width="900px">
    <div v-loading="viewLoading">
      <div v-if="viewRow" class="mb-2 text-xs text-slate-500">
        共享范围 {{ viewRow.scopeText }} · {{ viewRow.orderTypeText }} · 共 {{ viewRow.itemCount }} 条
        <span v-if="viewRow.remark"> · {{ viewRow.remark }}</span>
      </div>
      <el-table v-if="viewRow" :data="viewRow.items || []" border data-testid="orderset-view-table" size="small">
        <el-table-column align="center" label="#" type="index" width="50"/>
        <el-table-column align="center" label="类别" width="80">
          <template #default="{ row }">{{ row.orderClassText || orderClassText(row.orderClass) }}</template>
        </el-table-column>
        <el-table-column label="项目名称" min-width="160" prop="itemName" show-overflow-tooltip/>
        <el-table-column label="规格" prop="spec" width="100"/>
        <el-table-column label="剂量" width="120">
          <template #default="{ row }">
            <span v-if="row.dosage != null">{{ row.dosage }} {{ row.dosageUnit || '' }}</span>
            <span v-else class="text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="途径" prop="route" width="90"/>
        <el-table-column label="频次" prop="frequency" width="80"/>
        <el-table-column align="center" label="数量" prop="quantity" width="70"/>
        <el-table-column label="单位" prop="unit" width="70"/>
        <el-table-column align="right" label="参考单价" prop="price" width="90"/>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getOrderSetDetailById} from '@/api/inpatientOrder';
import {orderClassText} from '@/lib/orderItemDict';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 组套主键（雪花ID，务必传字符串，避免精度丢失）：明细自己按 id 取数
  id: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const viewRow = ref(null);
const viewLoading = ref(false);
watch(visible, async (open) => {
  if (!open) return;
  viewRow.value = null;
  viewLoading.value = true;
  try {
    const res = await getOrderSetDetailById(props.id);
    viewRow.value = res?.data || null;
  } catch (e) {
    ElMessage.error(e?.message || '加载组套明细失败');
  } finally {
    viewLoading.value = false;
  }
});
</script>
