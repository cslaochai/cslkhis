<template>
  <el-dialog v-model="visible" title="日间手术登记详情" width="820px">
    <div v-if="detail">
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="登记单号">{{ detail.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="术式">{{ detail.itemName }}（{{ detail.itemCode }}）</el-descriptions-item>
        <el-descriptions-item label="最长滞留(h)">{{ detail.maxStayHours }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName }}（{{
            detail.patientNo || '—'
          }}）
        </el-descriptions-item>
        <el-descriptions-item label="手术科室">{{ detail.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划手术日">{{ detail.planSurgeryDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="术前评估">
          <el-tag v-if="detail.evalResult === 1" size="small" type="success">通过</el-tag>
          <el-tag v-else-if="detail.evalResult === 2" size="small" type="danger">不通过</el-tag>
          <span v-else>—</span>
        </el-descriptions-item>
        <el-descriptions-item label="麻醉方式">{{ anesText(detail.anesthesiaType) }}</el-descriptions-item>
        <el-descriptions-item :span="3" label="评估意见">{{ detail.evalRemark || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手术时间">{{ detail.surgeryTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手术间">{{ detail.operatingRoom || '—' }}</el-descriptions-item>
        <el-descriptions-item label="主刀">{{ detail.surgeon || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手术结束">{{ detail.surgeryEndTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="离院方式">{{
            detail.leaveType ? leaveText(detail.leaveType) : '—'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="离院时间">{{ detail.dischargeTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="随访时限">{{ detail.followDue || '—' }}</el-descriptions-item>
        <el-descriptions-item label="随访次数">{{ detail.followCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="超期">
          <el-tag v-if="detail.overdue" size="small" type="danger">滞留超期</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.transferAdmissionId" :span="2" label="转住院ID">
          {{ detail.transferAdmissionId }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.cancelReason" :span="3" label="取消原因">{{
            detail.cancelReason
          }}
        </el-descriptions-item>
        <el-descriptions-item :span="3" label="出院评估">{{ detail.dischargeRemark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div class="flow-title">随访台账</div>
      <el-timeline v-if="detail.follows && detail.follows.length">
        <el-timeline-item v-for="f in detail.follows" :key="f.id" :timestamp="f.followTime" placement="top">
          <div class="flow-item">
            <strong>{{ followTypeText(f.followType) }}</strong>
            <el-tag :type="Number(f.result) === 1 ? 'success' : 'warning'" size="small">{{
                followResultText(f.result)
              }}
            </el-tag>
            <span class="flow-op">{{ f.operator }}</span>
          </div>
          <div class="flow-content">{{ f.content || '—' }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else :image-size="60" description="暂无随访记录"/>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDaySurgeryDetail} from '@/api/daySurgery';
import {dictLabelText} from '@/lib/utils';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  statusDict: {type: Array, default: () => []},
  anesDict: {type: Array, default: () => []},
  leaveDict: {type: Array, default: () => []},
  followTypeDict: {type: Array, default: () => []},
  followResultDict: {type: Array, default: () => []},
  statusTag: {type: Function, required: true},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);
const statusText = (v) => dictLabelText(props.statusDict, v);
const anesText = (v) => dictLabelText(props.anesDict, v);
const leaveText = (v) => dictLabelText(props.leaveDict, v);
const followResultText = (v) => dictLabelText(props.followResultDict, v);
const followTypeText = (v) => dictLabelText(props.followTypeDict, v);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  detail.value = null;
  const res = await getDaySurgeryDetail(props.id);
  if (res.code === 200) {
    detail.value = res.data;
  } 
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
