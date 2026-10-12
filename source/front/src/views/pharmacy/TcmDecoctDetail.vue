<template>
  <!-- 详情：只读。行点击进来的必须是只读弹框（AGENTS §2），编辑走列表上的按钮入口 -->
  <el-dialog v-model="visible" data-testid="tcm-decoct-detail-dialog" title="代煎单详情" width="760px">
    <div v-loading="loading">
      <el-form v-if="detail" class="!text-sm" disabled label-width="90px">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="代煎单号">
            <el-input :model-value="detail.decoctNo"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-input :model-value="detail.decoctStatusLabel"/>
          </el-form-item>
          <el-form-item label="患者">
            <el-input :model-value="detail.patientName"/>
          </el-form-item>
          <el-form-item label="处方号">
            <el-input :model-value="detail.prescriptionNo"/>
          </el-form-item>
          <el-form-item label="剂数">
            <el-input :model-value="`${detail.doseCount} 剂`"/>
          </el-form-item>
          <el-form-item label="味数">
            <el-input :model-value="`${detail.herbCount} 味`"/>
          </el-form-item>
          <el-form-item label="总克数">
            <el-input :model-value="`${Number(detail.totalGrams || 0).toFixed(2)} g`"/>
          </el-form-item>
          <el-form-item label="每剂约">
            <el-input :model-value="`${Number(detail.gramsPerDose || 0).toFixed(1)} g`"/>
          </el-form-item>
          <el-form-item label="开方医师">
            <el-input :model-value="detail.doctorName"/>
          </el-form-item>
          <el-form-item label="代煎药房">
            <el-input :model-value="detail.pharmacyName"/>
          </el-form-item>
          <el-form-item label="建单时间">
            <el-input :model-value="detail.createTime"/>
          </el-form-item>
          <el-form-item label="煎药时间">
            <el-input :model-value="detail.decoctTime || '—'"/>
          </el-form-item>
          <el-form-item label="取走时间">
            <el-input :model-value="detail.pickupTime || '—'"/>
          </el-form-item>
          <el-form-item label="经办人">
            <el-input :model-value="detail.operatorName || '—'"/>
          </el-form-item>
        </div>
        <el-form-item label="煎法脚注">
          <el-input :model-value="detail.methodSummary || '常规水煎'" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item v-if="detail.decoctStatus === 9" label="作废原因">
          <el-input :model-value="detail.cancelReason" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>

      <div class="mt-2 text-xs font-medium text-slate-500">逐味明细（{{ herbs.length }} 味）</div>
      <el-table :data="herbs" border class="mt-1" data-testid="tcm-decoct-herbs" max-height="260" size="small">
        <el-table-column align="center" label="#" type="index" width="46"/>
        <el-table-column label="药名" min-width="120" prop="drugName"/>
        <el-table-column label="规格" prop="specification" width="90"/>
        <el-table-column align="right" label="每剂" width="90">
          <template #default="{ row }">{{ row.perDoseText }} g</template>
        </el-table-column>
        <el-table-column align="right" label="实发" width="100">
          <template #default="{ row }">{{ Number(row.grams || 0).toFixed(2) }} g</template>
        </el-table-column>
        <el-table-column label="煎法" prop="method" width="100"/>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 代煎单详情（只读）—— 从 views/pharmacy/TcmDecoctView.vue 抽出
 *
 * 状态只进不退：1-待煎 →（煎好封装）2-已煎 →（患者取走）3-已取，9-已作废。
 * 这里没有任何写动作：推进/作废/打印都留在列表页的行按钮上，弹框只是把后端给的数据摊开看。
 */
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { tcmDecoctGetDetailById } from '@/api/tcmDecoct';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  id: { type: [Number, String], default: null },
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const detail = ref(null);
const herbs = computed(() => detail.value?.herbs || []);

const load = async () => {
  if (!props.id) return;
  loading.value = true;
  detail.value = null;
  try {
    const res = await tcmDecoctGetDetailById(props.id);
    if (res.code === 200)
      detail.value = res.data;
    else
      ElMessage.error(res.message || '加载代煎单详情失败');
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

watch(() => props.modelValue, (v) => {
  if (v) load();
});

defineExpose({ reload: load });
</script>
