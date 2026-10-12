<template>
  <!-- 详情 -->
  <el-dialog v-model="visible" title="纠纷/投诉详情" width="820px">
    <div v-if="detail">
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="单号">{{ detail.caseNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ caseTypeText(detail.caseType) }}</el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="levelTagType(detail.level)" size="small">{{ levelText(detail.level) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || '—' }}（{{
            detail.patientNo || '—'
          }}）
        </el-descriptions-item>
        <el-descriptions-item label="被投诉科室">{{ detail.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="涉及人员">{{ detail.involvedStaff || '—' }}</el-descriptions-item>
        <el-descriptions-item label="投诉人">{{ detail.complainant || '—' }}（{{
            relationText(detail.complainantRel)
          }}）
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.complainantTel || '—' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ sourceText(detail.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ detail.occurTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="发生地点">{{ detail.occurPlace || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="病历封存">
          <el-tag v-if="Number(detail.needSeal) === 1" :type="sealTagType(detail.sealStatus)" size="small">
            {{ sealText(detail.sealStatus) }}
          </el-tag>
          <span v-else>未申请</span>
        </el-descriptions-item>
        <el-descriptions-item label="受理天数">{{ detail.openDays ?? 0 }} 天</el-descriptions-item>
        <el-descriptions-item label="登记人">{{ detail.registerBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="受理人">{{ detail.acceptBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="结案人">{{ detail.closeBy || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="1" label="处理途径">{{
            detail.dealType ? dealTypeText(detail.dealType) : '—'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="责任认定">{{
            detail.dutyType ? dutyText(detail.dutyType) : '—'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="赔偿(元)">{{ detail.compensation ?? '—' }}</el-descriptions-item>
        <el-descriptions-item :span="3" label="内容">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item :span="3" label="诉求">{{ detail.demand || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="3" label="结论">{{ detail.conclusion || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.revokeReason" :span="3" label="撤销原因">{{
            detail.revokeReason
          }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="flow-title">处理跟踪台账</div>
      <el-timeline v-if="detail.flows && detail.flows.length">
        <el-timeline-item v-for="f in detail.flows" :key="f.id" :timestamp="f.operateTime" placement="top">
          <div class="flow-item">
            <strong>{{ f.action }}</strong>
            <span class="flow-status">{{ statusText(f.fromStatus) }} → {{ statusText(f.toStatus) }}</span>
            <span class="flow-op">{{ f.operator }}</span>
          </div>
          <div class="flow-content">{{ f.content || '—' }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else :image-size="60" description="暂无处理跟踪记录"/>
    </div>
  </el-dialog>
</template>

<script setup>
// 详情组件自己按 id 取数（主单 + 处理跟踪台账）
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDisputeDetail} from '@/api/dispute';
import {dictLabelText} from '@/lib/utils';
import {levelTagType, sealTagType, statusTagType} from '@/lib/dispute';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  dicts: {
    type: Object,
    default: () => ({caseType: [], source: [], status: [], level: [], dealType: [], duty: [], relation: [], seal: []}),
  },
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const caseTypeText = (v) => dictLabelText(props.dicts.caseType, v);
const sourceText = (v) => dictLabelText(props.dicts.source, v);
const statusText = (v) => dictLabelText(props.dicts.status, v);
const levelText = (v) => dictLabelText(props.dicts.level, v);
const dealTypeText = (v) => dictLabelText(props.dicts.dealType, v);
const dutyText = (v) => dictLabelText(props.dicts.duty, v);
const relationText = (v) => dictLabelText(props.dicts.relation, v);
const sealText = (v) => dictLabelText(props.dicts.seal, v);

const detail = ref(null);

const loadDetail = async () => {
  try {
    const res = await getDisputeDetail(props.id);
    if (res.code === 200) {
      detail.value = res.data;
    } else {
      ElMessage.error(res.message || '加载详情失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('加载详情失败');
  }
};

watch(visible, (open) => {
  if (open && props.id) loadDetail();
});
</script>

<style scoped>
.flow-title {
  margin: 16px 0 10px;
  font-weight: 600;
  color: #303133;
}

.flow-item {
  display: flex;
  gap: 10px;
  align-items: baseline;
}

.flow-status {
  font-size: 12px;
  color: #909399;
}

.flow-op {
  font-size: 12px;
  color: #409eff;
}

.flow-content {
  margin-top: 4px;
  color: #606266;
  white-space: pre-wrap;
}
</style>
