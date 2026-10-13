<template>
  <!-- 结果明细弹窗 -->
  <el-dialog v-model="visible" :title="`结果明细 - ${detail?.patientName || ''}（${detail?.recordNo || ''}）`"
             width="860px">
    <div v-if="detail" data-testid="checkup-result-panel">
      <p class="text-sm text-gray-500 mb-2">
        状态：
        <el-tag :type="statusTag(detail.recordStatus)" size="small">{{ statusText(detail.recordStatus) }}</el-tag>
        <span class="ml-3">套餐：{{ detail.packageName }}（¥{{ detail.totalAmount }}）</span>
      </p>
      <el-table :data="detail.results || []" border size="small">
        <el-table-column label="项目" prop="itemName" width="150"/>
        <el-table-column label="参考范围" prop="refStandard" show-overflow-tooltip width="160"/>
        <el-table-column label="结果值" min-width="170">
          <template #default="{ row }">
            <el-input v-model="row.resultValue" :data-testid="`checkup-result-input-${row.id}`" :disabled="detail.recordStatus === 4"
                      size="small"/>
          </template>
        </el-table-column>
        <el-table-column label="标志" width="100">
          <template #default="{ row }">
            <el-select v-model="row.abnormalFlag" :disabled="detail.recordStatus === 4" :fit-input-width="false"
                       size="small">
              <el-option v-for="d in flagDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="小结" min-width="140">
          <template #default="{ row }">
            <el-input v-model="row.summaryText" :disabled="detail.recordStatus === 4" size="small"/>
          </template>
        </el-table-column>
        <el-table-column label="医师" width="90">
          <template #default="{ row }">
            <el-input v-model="row.checkerName" :disabled="detail.recordStatus === 4" size="small"/>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="80">
          <template #default="{ row }">
            <el-button v-if="detail.recordStatus !== 4" v-perm="'checkup:manage:edit'" link size="small"
                       type="primary"
                       @click="saveOne(row)">保存
            </el-button>
            <el-tag v-else :type="flagTag(row.abnormalFlag)" size="small">{{ flagText(row.abnormalFlag) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button v-if="detail?.recordStatus === 3" v-perm="'checkup:manage:edit'" data-testid="checkup-conclude-btn"
                 type="primary"
                 @click="emit('conclude', detail)">总检出报告
      </el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 体检结果明细（按 id 自取数；结果录入逐行保存后原地刷新明细）
 * 行数据由父页面传入仅用于「打开瞬间不闪白」，正式数据以 getCheckupRecord 为准。
 * 「总检出报告」按钮把当前明细抛给父页面去开总检弹框（与行操作列的总检共用一个入口）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {checkupResultSave, getCheckupRecord} from '@/api/checkup';
import {dictLabelText} from '@/lib/utils';
import {checkupFlagTag as flagTag, checkupStatusTag as statusTag} from '@/lib/checkup';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [Number, String], default: null},
  row: {type: Object, default: null},
  statusDict: {type: Array, default: () => []},
  flagDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'conclude']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const statusText = (v) => dictLabelText(props.statusDict, v);
const flagText = (v) => dictLabelText(props.flagDict, v);

const detail = ref(null);

watch(
  () => props.modelValue,
  async (v) => {
    if (!v) return;
    detail.value = props.row || null;
    try {
      const res = await getCheckupRecord(props.id);
      detail.value = res?.data || null;
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '加载详情失败');
      visible.value = false;
    }
  }
);

const saveOne = async (row) => {
  if (!row.resultValue || !String(row.resultValue).trim())
    return ElMessage.warning('请填写结果值');
  try {
    await checkupResultSave({
      resultId: row.id, resultValue: row.resultValue, abnormalFlag: row.abnormalFlag ?? 0,
      summaryText: row.summaryText, checkerName: row.checkerName,
    });
    ElMessage.success('结果已保存');
    const res = await getCheckupRecord(detail.value.id);
    detail.value = res?.data;
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败');
  }
};
</script>
