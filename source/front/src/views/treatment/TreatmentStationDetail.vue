<template>
  <!-- 疗程详情 -->
  <el-dialog v-model="visible" title="疗程详情" width="900px">
    <div v-loading="loading">
      <template v-if="data?.apply">
        <div class="grid grid-cols-4 gap-2 text-sm mb-3">
          <div>疗程单号：<b>{{ data.apply.applyNo }}</b></div>
          <div>患者：{{ data.apply.patientName }} / {{ data.apply.patientNo }}</div>
          <div>就诊：{{ data.apply.registNo }}</div>
          <div>状态：{{ data.apply.applyStatusText }}（{{ data.apply.progressText }}）</div>
          <div>项目：{{ data.apply.itemName }}（{{ data.apply.itemTypeText }}）</div>
          <div>开单：{{ data.apply.deptName || '未指定' }} {{ data.apply.doctorName }}</div>
          <div>执行科室：{{ data.apply.execDeptName || '未指定' }}</div>
          <div>单价/计划：{{ money(data.apply.price) }} / {{ money(data.apply.planAmount) }}</div>
        </div>
        <el-table :data="data.execList" data-testid="detail-exec-table" max-height="420" size="small">
          <el-table-column label="流水号" prop="recordNo" width="190"/>
          <el-table-column label="排期" prop="planDate" width="105"/>
          <el-table-column label="执行" width="90">
            <template #default="{ row }">
              <el-tag :type="execTag(row.execStatus)" size="small">{{ row.execStatusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="计费" width="100">
            <template #default="{ row }">
              <el-tag :type="chargeTag(row.chargeStatus)" size="small">{{ row.chargeStatusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column align="right" label="金额" width="80">
            <template #default="{ row }">{{ money(row.chargeAmount) }}</template>
          </el-table-column>
          <el-table-column label="执行人/时间" width="170">
            <template #default="{ row }">{{ row.executorName || '—' }} {{ fmtTime(row.executeTime) }}</template>
          </el-table-column>
          <el-table-column label="治疗记录" min-width="200" prop="result" show-overflow-tooltip/>
        </el-table>
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getTreatmentApplyDetail} from '@/api/treatment';
import {chargeTag, execTag, fmtTime, money} from '@/lib/treatment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 疗程主键（字符串雪花 ID，禁止 Number()），打开时组件自己按 id 取数
  id: {type: [String, Number], default: ''},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const data = ref(null);

watch(visible, async (open) => {
  if (!open) return;
  loading.value = true;
  data.value = null;
  try {
    const res = await getTreatmentApplyDetail(props.id);
    if (res.code === 200)
      data.value = res.data;
    else
      ElMessage.error(res.message || '加载详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载详情失败');
  } finally {
    loading.value = false;
  }
});
</script>
