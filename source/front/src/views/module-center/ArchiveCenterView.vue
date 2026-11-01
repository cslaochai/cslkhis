<template>
  <el-tabs v-model="active" type="border-card">
    <el-tab-pane v-if="hasPermission('emr:archive:list')" label="病案归档" lazy name="archive">
      <MedicalRecordArchiveView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('emr:archiveBorrow:list')" label="借阅复印" lazy name="borrow">
      <ArchiveBorrowView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('emr:codeTask:list')" label="编码任务池" lazy name="coding">
      <CodingTaskView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {hasPermission, loadPermissions} from '@/lib/permission';
import MedicalRecordArchiveView from '@/views/medical-record/MedicalRecordArchiveView.vue';
import ArchiveBorrowView from '@/views/medical-record/ArchiveBorrowView.vue';
import CodingTaskView from '@/views/medical-record/CodingTaskView.vue';

// 原三个菜单：609 归档、611 借阅复印、612 编码任务池，是病案室一条串行岗位链。
const active = ref('archive');

onMounted(async () => {
  await loadPermissions();
  if (!hasPermission('emr:archive:list')) {
    active.value = hasPermission('emr:archiveBorrow:list') ? 'borrow'
        : (hasPermission('emr:codeTask:list') ? 'coding' : 'archive');
  }
});
</script>
