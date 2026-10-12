<template>
  <!-- 模板详情（只读） -->
  <el-dialog v-model="visible" :title="`模板 ${tplView?.pathwayName || ''} ${tplView?.version || ''}`"
             width="900px">
    <div v-if="tplView" class="space-y-3">
      <div class="flex items-center gap-6 text-sm text-slate-600 flex-wrap">
        <span>编码：<b class="text-slate-800">{{ tplView.pathwayCode }}</b></span>
        <span>科室：<b class="text-slate-800">{{ tplView.deptName || '—' }}</b></span>
        <span>总日数：<b class="text-slate-800">{{ tplView.totalDays }}</b></span>
        <span>状态：<el-tag :type="pathwayStatusTag(tplView.status)" size="small">{{
            statusText(tplView.status)
          }}</el-tag></span>
        <span>适用病种：{{ tplView.diagnosis || '—' }}</span>
      </div>
      <el-table :data="tplView.steps || []" border max-height="420" size="small">
        <el-table-column align="center" label="路径日" prop="dayNo" width="80"/>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ itemTypeText(row.itemType) }}</template>
        </el-table-column>
        <el-table-column label="项目" min-width="170" prop="itemName"/>
        <el-table-column label="路径要求" min-width="260" prop="content" show-overflow-tooltip/>
      </el-table>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * 路径模板详情（只读）：步骤清单不在列表行里，组件按 id 自取 getPathwayDetail。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getPathwayDetail} from '@/api/pathway';
import {dictLabelText} from '@/lib/utils';
import {pathwayStatusTag} from '@/lib/pathway';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 状态/项目类型文案与列表同一份字典，由页面加载一次传下来
  statusDict: {type: Array, default: () => []},
  itemTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const statusText = (v) => dictLabelText(props.statusDict, v);
const itemTypeText = (v) => dictLabelText(props.itemTypeDict, v);

const tplView = ref(null);

const loadDetail = async () => {
  try {
    const res = await getPathwayDetail(props.id);
    if (res.code === 200)
      tplView.value = res.data;
    else
      ElMessage.error(res.message || '加载模板失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载模板失败');
  }
};

watch(visible, async (open) => {
  if (!open || !props.id) return;
  tplView.value = null;
  await loadDetail();
});
</script>
