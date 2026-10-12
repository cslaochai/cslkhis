<template>
  <el-dialog v-model="visible" destroy-on-close title="标本详情" width="600px">
    <template v-if="specimen">
      <div class="space-y-4">
        <!-- 基本信息 -->
        <div class="rounded-lg bg-slate-50 p-4">
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div><span class="text-slate-500">标本条码：</span>
              <span class="font-mono font-medium">{{ specimen.specimenNo || '未分配' }}</span>
            </div>
            <div><span class="text-slate-500">标本类型：</span><span class="font-medium">{{
                specimen.specimenType
              }}</span></div>
            <div><span class="text-slate-500">患者：</span><span class="font-medium">{{
                specimen.patientName
              }} {{ patientGenderText(specimen.gender) }} {{ specimen.age }}岁</span></div>
            <div><span class="text-slate-500">患者号：</span><span class="font-medium">{{
                specimen.patientNo
              }}</span></div>
            <div><span class="text-slate-500">检验项目：</span><span class="font-medium">{{
                specimen.laboratoryItemName
              }}</span></div>
            <div><span class="text-slate-500">开单医生：</span><span class="font-medium">{{
                specimen.applyDoctorName
              }}</span></div>
            <div><span class="text-slate-500">申请科室：</span><span class="font-medium">{{
                specimen.applyDeptName || '-'
              }}</span></div>
            <div><span class="text-slate-500">记录号：</span>
              <span class="font-mono text-xs">{{ specimen.recordNo }}</span>
            </div>
          </div>
        </div>

        <!-- 流转时间线 -->
        <div>
          <h4 class="mb-3 text-sm font-semibold text-slate-700">流转记录</h4>
          <div class="space-y-3 pl-2">
            <div v-if="specimen.createTime" class="flex gap-3">
              <div class="flex flex-col items-center">
                <div class="h-2.5 w-2.5 rounded-full bg-blue-500"></div>
                <div class="w-px flex-1 bg-slate-200"></div>
              </div>
              <div class="pb-3">
                <p class="text-sm font-medium text-slate-700">医嘱开出</p>
                <p class="text-xs text-slate-400">{{ specimen.createTime?.replace('T', ' ') }}</p>
              </div>
            </div>
            <div v-if="specimen.sampleTime" class="flex gap-3">
              <div class="flex flex-col items-center">
                <div class="h-2.5 w-2.5 rounded-full bg-amber-500"></div>
                <div class="w-px flex-1 bg-slate-200"></div>
              </div>
              <div class="pb-3">
                <p class="text-sm font-medium text-slate-700">标本采集</p>
                <p class="text-xs text-slate-400">{{ specimen.sampleTime?.replace('T', ' ') }} · {{
                    specimen.sampleBy
                  }}</p>
              </div>
            </div>
            <div v-if="specimen.receiveTime" class="flex gap-3">
              <div class="flex flex-col items-center">
                <div class="h-2.5 w-2.5 rounded-full bg-purple-500"></div>
                <div class="w-px flex-1 bg-slate-200"></div>
              </div>
              <div class="pb-3">
                <p class="text-sm font-medium text-slate-700">标本接收</p>
                <p class="text-xs text-slate-400">{{ specimen.receiveTime?.replace('T', ' ') }} · {{
                    specimen.receiveBy
                  }}</p>
              </div>
            </div>
            <div v-if="specimen.executeTime" class="flex gap-3">
              <div class="flex flex-col items-center">
                <div class="h-2.5 w-2.5 rounded-full bg-emerald-500"></div>
                <div class="w-px flex-1 bg-slate-200"></div>
              </div>
              <div class="pb-3">
                <p class="text-sm font-medium text-slate-700">检验执行</p>
                <p class="text-xs text-slate-400">{{ specimen.executeTime?.replace('T', ' ') }} · {{
                    specimen.executeBy
                  }}</p>
              </div>
            </div>
            <div v-if="specimen.auditTime" class="flex gap-3">
              <div class="flex flex-col items-center">
                <div class="h-2.5 w-2.5 rounded-full bg-emerald-600"></div>
              </div>
              <div>
                <p class="text-sm font-medium text-slate-700">报告审核</p>
                <p class="text-xs text-slate-400">{{ specimen.auditTime?.replace('T', ' ') }} · {{
                    specimen.auditBy
                  }}</p>
              </div>
            </div>
            <div v-if="specimen.specimenStatus === 99" class="flex gap-3">
              <div class="flex flex-col items-center">
                <div class="h-2.5 w-2.5 rounded-full bg-red-500"></div>
              </div>
              <div>
                <p class="text-sm font-medium text-red-600">异常退回</p>
                <p v-if="specimen.suggestions" class="text-xs text-slate-400">原因：{{
                    specimen.suggestions
                  }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue';
import { patientGenderText } from '@/lib/patientGender';

const props = defineProps({
  // v-model 控制显隐
  modelValue: { type: Boolean, default: false },
  // 列表行数据（详情展示的就是这一行，无需再取数）
  specimen: { type: Object, default: null },
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
</script>
