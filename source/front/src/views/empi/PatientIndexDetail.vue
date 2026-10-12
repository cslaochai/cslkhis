<template>
  <el-drawer v-model="visible" data-testid="p5-empi-detail" size="560px" title="患者主索引详情">
    <div v-if="detail" class="space-y-4">
      <div class="rounded-lg border border-slate-200 p-4">
        <p class="text-base font-semibold text-slate-900">
          {{ detail.patientNo }} {{ detail.patientName }}
          <span class="ml-2 text-sm font-normal text-slate-500">{{ detail.genderText }} / {{
              detail.age ?? '—'
            }}岁</span>
        </p>
        <p class="mt-2 text-sm text-slate-600">身份证号：{{ detail.idCard || '（空）' }}</p>
        <p class="text-sm text-slate-600">手机号：{{ detail.phone || '（空）' }}</p>
        <p class="text-sm text-slate-600">地址：{{ detail.address || '（空）' }}</p>
        <p class="mt-2 text-sm">
          主索引状态：
          <el-tag v-if="Number(detail.mergeStatus) === 1" size="small" type="warning">
            已并入 {{ detail.masterNo }} {{ detail.masterName }}
          </el-tag>
          <el-tag v-else size="small" type="success">主档</el-tag>
        </p>
      </div>

      <div class="rounded-lg border border-slate-200 p-4">
        <p class="mb-2 text-sm font-medium text-slate-700">
          档案完整度 {{ detail.completeRate }}%（{{ detail.completeCount }}/{{ detail.totalFieldCount }}）
        </p>
        <div class="flex flex-wrap gap-1">
          <el-tag
              v-for="f in detail.missingFields || []"
              :key="f"
              size="small"
              type="warning"
          >
            缺 {{ f }}
          </el-tag>
          <span v-if="!(detail.missingFields || []).length" class="text-xs text-emerald-600">关键字段已填全</span>
        </div>
      </div>

      <div class="rounded-lg border border-slate-200 p-4">
        <p class="mb-2 text-sm font-medium text-slate-700">关联业务数据（{{ detail.totalDataCount }} 条）</p>
        <div class="grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
          <div v-for="d in detail.dataCounts || []" :key="d.key" class="flex justify-between">
            <span class="text-slate-500">{{ d.label }}</span>
            <span class="font-medium text-slate-800">{{ d.count }}</span>
          </div>
        </div>
      </div>

      <div v-if="(detail.siblingPatients || []).length" class="rounded-lg border border-slate-200 p-4">
        <p class="mb-2 text-sm font-medium text-slate-700">
          同主档下的其他档案（{{ detail.siblingPatients.length }} 份）
        </p>
        <div v-for="s in detail.siblingPatients" :key="s.id" class="mb-2 text-sm text-slate-600">
          {{ s.patientNo }} {{ s.patientName }}（{{ s.genderText }}）｜
          证件 {{ s.idCard || '空' }}｜并入时间 {{ s.mergeTime || '—' }}
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
/**
 * 患者主索引详情抽屉（自 PatientIndexView 拆出）
 *
 * 按 id 自取 getPatientIndexDetail（雪花ID，务必传字符串，避免精度丢失）；
 * 展示字段（genderText / mergeStatus 等）全部来自后端出参，前端不翻译。
 */
import {computed, ref, watch} from 'vue';
import {getPatientIndexDetail} from '@/api/patientIndex';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 患者档案ID（雪花ID，务必传字符串）
  id: {type: [String, Number], default: null},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);

// 打开时按 id 自取详情（与拆分前 openDetail 的取数口径一致）
watch(() => props.modelValue, async (v) => {
  if (!v) return;
  detail.value = null;
  if (!props.id) return;
  try {
    const res = await getPatientIndexDetail(String(props.id));
    detail.value = res.data;
  } catch (e) {
    console.error(e);
  }
});
</script>
