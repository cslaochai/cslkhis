<template>
  <!-- 详情弹窗 -->
  <el-dialog v-model="visible" title="病理单详情" width="760px">
    <div v-loading="detailLoading">
      <template v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="病理号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item label="检查类型">{{ detail.examTypeText }}</el-descriptions-item>
          <el-descriptions-item label="标本">{{ detail.specimenType || '-' }} / {{
              detail.specimenPart || '-'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="冰冻结果">{{ detail.frozenResult || '-' }}</el-descriptions-item>
          <el-descriptions-item :span="3" label="诊断">{{ detail.diagnosis || '未出' }}</el-descriptions-item>
          <el-descriptions-item :span="3" label="建议">{{ detail.suggestion || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt-3 font-medium text-sm text-gray-600">蜡块明细</div>
        <el-table :data="detail.blocks || []" class="mt-1" size="small">
          <el-table-column label="蜡块号" prop="blockNo" width="90"/>
          <el-table-column label="部位" min-width="120" prop="partDesc" show-overflow-tooltip/>
          <el-table-column label="状态" width="90">
            <template #default="{ row: b }">
              <el-tag size="small">{{ b.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="切片号" prop="sliceNo" width="90"/>
          <el-table-column label="流转" width="220">
            <template #default="{ row: b }">
              <el-button v-if="Number(b.status) === 1" v-perm="'medtech:pathology:edit'" link size="small"
                         type="primary" @click="blockAction(b, 1)">取材
              </el-button>
              <el-button v-if="Number(b.status) === 2" v-perm="'medtech:pathology:edit'" link size="small"
                         type="primary" @click="blockAction(b, 2)">包埋
              </el-button>
              <el-button v-if="Number(b.status) === 3" v-perm="'medtech:pathology:edit'" link size="small"
                         type="primary" @click="blockAction(b, 3)">切片
              </el-button>
              <span v-if="Number(b.status) === 4" class="text-gray-400 text-xs">已完成</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * 病理单详情（含蜡块明细）：蜡块的取材/包埋/切片在弹框内推进，成功后重取本详情
 * 并通知页面刷新列表；列表行不带这些扩展字段，所以组件按 id 自取。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getPathologyDetail, pathologyBlockAction} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detailLoading = ref(false);
const detail = ref(null);

const loadDetail = async () => {
  detailLoading.value = true;
  try {
    const res = await getPathologyDetail(props.id);
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

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detail.value = null;
  await loadDetail();
});

const blockAction = async (block, action) => {
  try {
    const res = await pathologyBlockAction({blockId: block.id, action});
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      await loadDetail();
      emit('saved');
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
</script>
