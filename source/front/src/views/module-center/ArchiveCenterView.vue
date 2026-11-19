<template>
  <div v-if="visibleTabs.length === 0" class="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800"
       data-testid="module-no-perm">
    当前岗位未开通病案管理权限（病案归档 / 借阅复印 / 编码任务池）。
    请管理员在「系统管理 → 角色管理 → 菜单权限」里为本岗位勾选对应模块。
  </div>
  <el-tabs v-else v-model="active" type="border-card">
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
import {computed, onMounted, ref} from 'vue';
import {useRoute} from 'vue-router';
import {hasPermission, loadPermissions} from '@/lib/permission';
import MedicalRecordArchiveView from '@/views/medical-record/MedicalRecordArchiveView.vue';
import ArchiveBorrowView from '@/views/medical-record/ArchiveBorrowView.vue';
import CodingTaskView from '@/views/medical-record/CodingTaskView.vue';

// 原三个菜单：609 归档、611 借阅复印、612 编码任务池，是病案室一条串行岗位链。
const active = ref('archive');
const route = useRoute();

const order = ['archive', 'borrow', 'coding'];
const permOf = {
  archive: 'emr:archive:list',
  borrow: 'emr:archiveBorrow:list',
  coding: 'emr:codeTask:list',
};
const visibleTabs = computed(() => order.filter((k) => hasPermission(permOf[k])));
/** 待办深链：按消息类型直接落到对应 tab（归档超期→归档、借阅超期→借阅、编码退修→编码池） */
const MSG_TAB = {'emr-arch': 'archive', 'arch-borrow': 'borrow', 'code-task': 'coding'};

onMounted(async () => {
  await loadPermissions();
  const wanted = MSG_TAB[String(route.query.bizType || '')];
  if (wanted && hasPermission(permOf[wanted])) {
    active.value = wanted;
    return;
  }
  if (!hasPermission(permOf.archive)) {
    active.value = hasPermission(permOf.borrow) ? 'borrow'
        : (hasPermission(permOf.coding) ? 'coding' : 'archive');
  }
});
</script>
