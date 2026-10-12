<template>
  <!-- 转入留观：必须选一张急诊病区的空闲床（占 sys_bed，住院分床不会再发这张床） -->
  <el-dialog v-model="visible" destroy-on-close title="转入留观" width="460px">
    <el-form :model="obsForm" label-position="top">
      <el-form-item label="患者">
        <el-input :model-value="obsForm.patientName" disabled/>
      </el-form-item>
      <el-form-item label="留观病区" required>
        <el-select v-model="obsForm.wardId" class="!w-full" placeholder="选择急诊病区" @change="onObsWardChange">
          <el-option v-for="w in emergencyWards" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)">
            <span>{{ w.wardName }}</span>
            <span :class="w.freeBeds > 0 ? 'text-emerald-500' : 'text-rose-500'" class="float-right text-xs">
              空闲 {{ w.freeBeds }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="留观床位" required>
        <el-select v-model="obsForm.bedId" :disabled="!obsForm.wardId" class="!w-full" placeholder="先选病区">
          <el-option v-for="b in obsBeds" :key="b.bedId" :label="`${b.bedNo}（${b.bedType}）`"
                     :value="String(b.bedId)"/>
        </el-select>
        <p v-if="obsForm.wardId && !obsBeds.length" class="mt-1 text-xs text-rose-500">该病区暂无空闲床位</p>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="obsSubmitting" type="warning" @click="submitObs">确认占床并转留观</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 留观必须落到一张真实的 sys_bed 床位上：不占床的话「这张床有没有人躺着」
// 就只存在于急诊表一处，住院分床会把同一张床再发出去。
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getEmergencyBedSelectList, updateEmergencyStatus} from '@/api/emergency';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 病区名册由主页面加载一次（转住院弹框共用同一份），子组件不许再拉一遍
  wards: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const obsSubmitting = ref(false);
const obsBeds = ref([]);
const obsForm = ref({
  id: '',
  patientName: '',
  wardId: null,
  bedId: null,
});
/** 留观只在急诊自己的病区（急诊内科/外科/儿科病区），不是全院住院病区 */
const emergencyWards = computed(() => props.wards.filter((w) => (w.wardName || '').startsWith('急诊')));

watch(visible, (open) => {
  if (!open)
    return;
  obsForm.value = {
    id: props.row?.id,
    patientName: props.row?.patientName,
    wardId: null,
    bedId: null,
  };
  obsBeds.value = [];
});

const onObsWardChange = async (wardId) => {
  obsForm.value.bedId = null;
  obsBeds.value = [];
  if (!wardId)
    return;
  try {
    const res = await getEmergencyBedSelectList({wardId, bedStatus: 1});
    obsBeds.value = res.data || [];
  } catch {
    obsBeds.value = [];
  }
};

const submitObs = async () => {
  if (!obsForm.value.wardId)
    return ElMessage.warning('请选择留观病区');
  if (!obsForm.value.bedId)
    return ElMessage.warning('请选择留观床位');
  obsSubmitting.value = true;
  try {
    await updateEmergencyStatus(obsForm.value.id, 3, {
      observationWardId: obsForm.value.wardId,
      observationBedId: obsForm.value.bedId,
    });
    ElMessage.success('已转入留观并占床');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '转留观失败');
  } finally {
    obsSubmitting.value = false;
  }
};
</script>
