<template>
  <!-- 无任何模态权限时整页会是空的（tab 全被 v-if 掉），必须给说明，不能留给用户一张白板 -->
  <div v-if="visibleTabs.length === 0" class="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800"
       data-testid="module-no-perm">
    当前岗位未开通检查工作站权限（检查 / 放射 / 超声 / 内镜 / 心电 / 胶片量方）。
    请管理员在「系统管理 → 角色管理 → 菜单权限」里为本岗位勾选对应模块；来自统一待办的检查报告，
    也可在待办弹框里直接查看详情。
  </div>
  <el-tabs v-else v-model="active" class="module-tabs">
    <el-tab-pane v-if="hasPermission('medtech:inspectionWorkstation:list')" label="检查" lazy name="inspection">
      <InspectionView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:radioDiagnosis:list')" label="放射" lazy name="radio">
      <RadioDiagnosisView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:ultrasound:list')" label="超声" lazy name="ultrasound">
      <UltrasoundView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:endoscopy:list')" label="内镜" lazy name="endoscopy">
      <EndoscopyView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:ecg:list')" label="心电" lazy name="ecg">
      <EcgWorkstationView/>
    </el-tab-pane>
    <el-tab-pane v-if="hasPermission('medtech:examFilm:list')" label="胶片量方" lazy name="film">
      <ExamFilmView/>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute} from 'vue-router';
import {hasPermission, loadPermissions} from '@/lib/permission';
import InspectionView from '@/views/lab/InspectionView.vue';
import RadioDiagnosisView from '@/views/lab/RadioDiagnosisView.vue';
import UltrasoundView from '@/views/ultrasound/UltrasoundView.vue';
import EndoscopyView from '@/views/endoscopy/EndoscopyView.vue';
import EcgWorkstationView from '@/views/lab/EcgWorkstationView.vue';
import ExamFilmView from '@/views/lab/ExamFilmView.vue';

// 原 6 个并列菜单（401 检查工作站 + 414 放射诊断 + 409 超声 + 408 内镜 + 417 心电 + 415 胶片量方）
// 合并为统一 PACS 入口，模态作为页内 tab。权限码沿用各模态原菜单，角色按所配模态看到对应 tab。
// 参考 311 合并先例：被合掉菜单只 is_visible=0，权限码/角色授权不动，这里 tab 显隐即由角色权限决定。
const active = ref('inspection');
const route = useRoute();

const order = ['inspection', 'radio', 'ultrasound', 'endoscopy', 'ecg', 'film'];
const permOf = {
  inspection: 'medtech:inspectionWorkstation:list',
  radio: 'medtech:radioDiagnosis:list',
  ultrasound: 'medtech:ultrasound:list',
  endoscopy: 'medtech:endoscopy:list',
  ecg: 'medtech:ecg:list',
  film: 'medtech:examFilm:list',
};
const visibleTabs = computed(() => order.filter((k) => hasPermission(permOf[k])));

/** 待办深链：检查报告消息落到「检查」tab，而不是用户上次停的 tab */
const MSG_TAB = {inspection: 'inspection', report: 'inspection'};

onMounted(async () => {
  await loadPermissions();
  const wanted = MSG_TAB[String(route.query.bizType || '')];
  if (wanted && hasPermission(permOf[wanted])) {
    active.value = wanted;
    return;
  }
  // 若当前角色没有「检查」权限（但有其他模态），自动跳到第一个可见 tab
  if (!hasPermission(permOf.inspection)) {
    const first = order.find(k => hasPermission(permOf[k]));
    if (first) active.value = first;
  }
});
</script>
