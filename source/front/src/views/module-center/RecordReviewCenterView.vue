<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('emr:medicalRecord:list')" label="病历查阅" lazy name="record">
      <MedicalRecordView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('emr:medicalReview:list')" label="病历审核" lazy name="review">
      <MedicalReviewView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import MedicalRecordView from '@/views/medical-record/MedicalRecordView.vue';
import MedicalReviewView from '@/views/medical-record/MedicalReviewView.vue';

// 原两个菜单：602 门诊病历（查阅）、603 病历审核。列表页同一套接口，审核只是多一个动作。
const active = ref('record');

onMounted(async () => {
  await loadPermissions();
  if (!hasPermission('emr:medicalRecord:list') && hasPermission('emr:medicalReview:list')) {
    active.value = 'review';
  }
});
</script>
