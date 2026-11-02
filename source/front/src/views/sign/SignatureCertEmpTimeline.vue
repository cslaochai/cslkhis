<template>
  <el-dialog v-model="visible" :title="`证书操作履历 · ${empName || ''}`" width="740px" @open="load">
    <div v-loading="loading" class="max-h-[60vh] overflow-auto pr-1">
      <el-empty v-if="!loading && items.length === 0" description="该员工暂无证书申请、回填或吊销记录"/>
      <el-timeline v-else>
        <el-timeline-item v-for="(it, i) in items" :key="i" :timestamp="it.eventTime || ''" placement="top"
                          :hollow="it.eventType !== 'REVOKE'"
                          :type="it.eventType === 'REVOKE' ? 'danger' : 'primary'">
          <div class="space-y-1">
            <div class="flex flex-wrap items-center gap-2">
              <el-tag size="small"
                      :type="it.eventType === 'REVOKE' ? 'danger' : caApplyTagType(it.applyStatus)">
                {{ it.eventType === 'REVOKE' ? '吊销' : (it.applyStatusText || it.applyStatus) }}
              </el-tag>
              <span class="font-mono text-xs text-slate-600">{{ it.certNo }}</span>
              <span v-if="it.eventType === 'CA_APPLY'" class="text-xs text-slate-400">第 {{ it.applySeq }} 次申请</span>
              <span v-if="it.applyNo" class="text-xs text-slate-400">申请单 {{ it.applyNo }}</span>
              <span v-if="it.operatorName" class="text-xs text-slate-400">提交人：{{ it.operatorName }}</span>
              <span v-if="it.revokeByName" class="text-xs text-red-500">吊销人：{{ it.revokeByName || '（历史数据未留痕）' }}</span>
            </div>
            <div v-if="it.revokeReason" class="text-xs text-red-600">吊销原因：{{ it.revokeReason }}</div>
            <div v-if="it.caMessage" class="text-xs text-slate-500">{{ it.caMessage }}</div>
            <div v-if="it.eventType === 'CA_APPLY' && (it.reviewedAt || it.issuedAt || it.boundAt || it.caIssuer)"
                 class="text-xs text-slate-400">
              <template v-if="it.reviewedAt">CA审核 {{ it.reviewedAt }}</template>
              <template v-if="it.issuedAt">{{ it.reviewedAt ? ' · ' : '' }}CA签发 {{ it.issuedAt }}</template>
              <template v-if="it.boundAt">{{ it.reviewedAt || it.issuedAt ? ' · ' : '' }}回填生效 {{ it.boundAt }}</template>
              <template v-if="it.caIssuer">{{ it.reviewedAt || it.issuedAt || it.boundAt ? ' · ' : '' }}签发CA {{ it.caIssuer }}</template>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref} from 'vue';
import {getSignCertEmpTimeline} from '@/api/signature';
import {caApplyTagType} from '@/lib/sign';

const props = defineProps({
  modelValue: Boolean,
  empId: {type: [Number, String], default: null},
  empName: {type: String, default: ''},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const items = ref([]);

const load = async () => {
  if (!props.empId) return;
  loading.value = true;
  items.value = [];
  try {
    const res = await getSignCertEmpTimeline(props.empId);
    items.value = res.data || [];
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    loading.value = false;
  }
};
</script>
