<template>
  <el-tabs v-model="active" type="border-card">
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
import {onMounted, ref} from 'vue';
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

const order = ['inspection', 'radio', 'ultrasound', 'endoscopy', 'ecg', 'film'];
const permOf = {
  inspection: 'medtech:inspectionWorkstation:list',
  radio: 'medtech:radioDiagnosis:list',
  ultrasound: 'medtech:ultrasound:list',
  endoscopy: 'medtech:endoscopy:list',
  ecg: 'medtech:ecg:list',
  film: 'medtech:examFilm:list',
};

onMounted(async () => {
  await loadPermissions();
  // 若当前角色没有「检查」权限（但有其他模态），自动跳到第一个可见 tab
  if (!hasPermission(permOf.inspection)) {
    const first = order.find(k => hasPermission(permOf[k]));
    if (first) active.value = first;
  }
});
</script>
