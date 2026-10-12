<template>
  <el-dialog v-model="visible" :title="`危急值详情 - ${detail?.criticalNo || ''}`" destroy-on-close width="680px">
    <div v-if="detail" v-loading="detailLoading" class="space-y-4">
      <div
          :class="detail.overdue ? 'border-red-300 bg-red-50' : 'border-slate-200'"
          class="rounded-lg border p-4"
      >
        <div class="flex items-center gap-2">
          <el-icon class="text-red-600">
            <Warning/>
          </el-icon>
          <span class="text-sm font-semibold text-slate-800">{{ detail.criticalDesc }}</span>
        </div>
        <div v-if="detail.overdue" class="mt-1 text-xs text-red-600">
          已超过处置时限 {{ fmtTime(detail.deadlineTime) }}，请立即处置
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
        <div><p class="text-xs text-slate-400">患者</p>
          <p class="text-sm font-medium">{{ detail.patientName || '—' }} {{ detail.genderText || '' }}
            {{ detail.age ? detail.age + '岁' : '' }}</p></div>
        <div><p class="text-xs text-slate-400">来源</p>
          <p class="text-sm font-medium">
            {{ detail.source === 'RULE' ? '系统自动识别（硬规则）' : detail.source || '—' }}
          </p></div>
        <div><p class="text-xs text-slate-400">检验项目</p>
          <p class="text-sm font-medium">{{ detail.itemName || '—' }}</p></div>
        <div><p class="text-xs text-slate-400">参考区间</p>
          <p class="text-sm font-medium">{{ detail.referenceRange || '—' }}</p></div>
        <div><p class="text-xs text-slate-400">报告科室</p>
          <p class="text-sm font-medium">{{ detail.reportDeptName || '—' }}</p></div>
        <div><p class="text-xs text-slate-400">报告人</p>
          <p class="text-sm font-medium">{{ detail.reportBy || '—' }}</p></div>
        <div><p class="text-xs text-slate-400">上报时间</p>
          <p class="text-sm font-medium">{{ fmtTime(detail.reportTime) }}</p></div>
        <div><p class="text-xs text-slate-400">处置时限</p>
          <p class="text-sm font-medium">{{ fmtTime(detail.deadlineTime) }}</p></div>
      </div>

      <!-- 闭环轨迹 -->
      <div class="rounded-lg border border-slate-200 p-4">
        <p class="mb-3 text-sm font-medium text-slate-700">闭环轨迹</p>
        <div class="space-y-3">
          <div class="flex items-start gap-3">
            <el-icon class="mt-0.5 text-blue-500">
              <Bell/>
            </el-icon>
            <div>
              <p class="text-sm text-slate-700">已上报并通知开单医生</p>
              <p class="text-xs text-slate-400">{{ fmtTime(detail.reportTime) }} · {{ detail.reportBy || '—' }}</p>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <el-icon :class="detail.receiveBy ? 'text-emerald-500' : 'text-slate-300'" class="mt-0.5">
              <CircleCheck/>
            </el-icon>
            <div>
              <p :class="detail.receiveBy ? 'text-slate-700' : 'text-slate-400'" class="text-sm">
                {{ detail.receiveBy ? '已确认接收' : '待接收' }}
              </p>
              <p v-if="detail.receiveBy" class="text-xs text-slate-400">
                {{ fmtTime(detail.receiveTime) }} · {{ detail.receiveBy }}
              </p>
            </div>
          </div>
          <div class="flex items-start gap-3">
            <el-icon :class="detail.handleBy ? 'text-emerald-500' : 'text-slate-300'" class="mt-0.5">
              <CircleCheck/>
            </el-icon>
            <div>
              <p :class="detail.handleBy ? 'text-slate-700' : 'text-slate-400'" class="text-sm">
                {{ detail.handleBy ? '已记录处置' : '待处置' }}
              </p>
              <p v-if="detail.handleBy" class="text-xs text-slate-400">
                {{ fmtTime(detail.handleTime) }} · {{ detail.handleBy }}
              </p>
              <p v-if="detail.handleMeasure" class="mt-1 rounded bg-slate-50 px-2 py-1 text-xs text-slate-600">
                {{ detail.handleMeasure }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button
          v-if="detail?.status === 1"
          v-perm="'medtech:criticalValue:edit'"
          type="warning"
          @click="detail && handleReceive(detail)"
      >
        确认接收
      </el-button>
      <el-button
          v-if="detail && canOperate(detail)"
          v-perm="'medtech:criticalValue:edit'"
          type="primary"
          @click="detail && emit('handle', detail)"
      >
        记录处置
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 危急值详情：打开时按 id 取详情，接收闭环在弹框内完成，处置交由父页面开处置弹框。
 */
import {computed, ref, watch} from 'vue';
import {Bell, CircleCheck, Warning} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getCriticalValueDetail, receiveCriticalValue} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: null},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved', 'handle']);
const visible = computed({get: () => props.modelValue, set: (v) => emit('update:modelValue', v)});
const detailLoading = ref(false);
const detail = ref(null);
const fmtTime = (v) => (v ? String(v).replace('T', ' ').slice(0, 16) : '—');
/** 未处置且未作废才允许操作 */
const canOperate = (row) => row.status === 1 || row.status === 2;
watch(visible, async (open) => {
  if (!open || !props.id)
    return;
  detail.value = props.row || null;
  detailLoading.value = true;
  try {
    const res = await getCriticalValueDetail(props.id);
    if (res.data) {
      detail.value = res.data;
    }
  } catch (error) {
    ElMessage.error(error.message || '加载危急值详情失败');
  } finally {
    detailLoading.value = false;
  }
});
const handleReceive = async (row) => {
  try {
    await ElMessageBox.confirm(`确认接收 ${row.patientName || ''} 的危急值「${row.itemName || ''}」？`, '确认接收', {
      confirmButtonText: '确认接收',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await receiveCriticalValue(row.id);
    ElMessage.success('已确认接收');
    emit('saved');
    const res = await getCriticalValueDetail(row.id);
    detail.value = res.data || detail.value;
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '接收失败');
    }
  }
};
</script>
