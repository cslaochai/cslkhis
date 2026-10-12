<template>
  <!-- ============ 检查单详情（只读：行点击进来，改单走列表上的编辑按钮） ============ -->
  <el-dialog v-model="visible" data-testid="qc-detail-dialog" title="检查单详情" width="1080px">
    <el-descriptions v-if="detail" :column="4" border size="small">
      <el-descriptions-item label="单号">{{ detail.check.checkNo }}</el-descriptions-item>
      <el-descriptions-item label="病区">{{ detail.check.wardName }}</el-descriptions-item>
      <el-descriptions-item label="月份 / 日期">{{ detail.check.checkMonth }} · {{
          detail.check.checkDate
        }}
      </el-descriptions-item>
      <el-descriptions-item label="类别">{{
          detail.check.categoryName || categoryText(detail.check.category)
        }}
      </el-descriptions-item>
      <el-descriptions-item label="检查人">{{ detail.check.inspectorName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="抽查/合格">{{ detail.check.qualifiedCount }} / {{
          detail.check.sampleCount
        }}
      </el-descriptions-item>
      <el-descriptions-item label="合格率">{{ fmt(detail.check.qualifiedRate) }}%</el-descriptions-item>
      <el-descriptions-item label="得分">{{ fmt(detail.check.totalScore, 1) }} / {{
          fmt(detail.check.fullScore, 1)
        }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">{{
          detail.check.statusText || statusText(detail.check.status)
        }}
      </el-descriptions-item>
      <el-descriptions-item label="录入时间">{{ detail.check.createTime }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="小结">{{ detail.check.summary || '—' }}</el-descriptions-item>
    </el-descriptions>
    <p v-if="detail && Number(detail.missingItemCount) > 0" class="mt-2 text-sm text-amber-700"
       data-testid="qc-missing">
      本类别共 {{ detail.catalog?.length }} 项，本轮还有 {{ detail.missingItemCount }} 项未录入，合格率只按已查项统计
    </p>
    <el-table v-if="detail" :data="detail.items" border class="mt-2" data-testid="qc-detail-items" max-height="360"
              size="small">
      <el-table-column label="检查项目" prop="itemName" show-overflow-tooltip width="200"/>
      <el-table-column align="center" label="抽查" prop="checkedNum" width="70"/>
      <el-table-column align="center" label="合格" prop="qualifiedNum" width="70"/>
      <el-table-column align="right" label="单项合格率" width="110">
        <template #default="{ row }">{{ fmt(row.qualifiedRate) }}%</template>
      </el-table-column>
      <el-table-column align="right" label="得分" width="100">
        <template #default="{ row }">{{ fmt(row.score, 1) }} / {{ fmt(row.fullScore, 1) }}</template>
      </el-table-column>
      <el-table-column label="存在问题" min-width="160" prop="problem" show-overflow-tooltip/>
      <el-table-column label="原因分析" min-width="160" prop="causeAnalysis" show-overflow-tooltip/>
      <el-table-column label="整改措施" min-width="160" prop="rectifyMeasure" show-overflow-tooltip/>
    </el-table>
    <template #footer>
      <el-button type="primary" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 检查单详情（只读）：明细 + 漏查项提示。
 * 打开时按 id 自拉 getQcCheckDetailById；类别/状态字典列表也在用，由父页面加载后传入。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getQcCheckDetailById} from '@/api/nursingQc';
import {dictLabelText} from '@/lib/utils';
import {fmt} from '@/lib/nursingQc';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 检查单 id（雪花 id，字符串）
  id: {type: [String, Number], default: null},
  // 字典：列表列与详情共用，父页面加载一次传入
  categoryDict: {type: Array, default: () => []},
  statusDict: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const categoryText = (v) => dictLabelText(props.categoryDict, v);
const statusText = (v) => dictLabelText(props.statusDict, v);

const detail = ref(null);
const load = async () => {
  detail.value = null;
  try {
    const res = await getQcCheckDetailById(props.id);
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载详情失败');
      return;
    }
    detail.value = res.data;
  } catch (e) {
    ElMessage.error(e.message || '加载详情失败');
  }
};
watch(() => props.modelValue, (v) => {
  if (v)
    load();
});
</script>
