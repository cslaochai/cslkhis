<template>
  <el-dialog v-model="visible" destroy-on-close title="换床" width="520px">
    <el-form label-width="100px">
      <el-form-item label="病区">
        <el-input :model-value="transferForm.wardName" disabled/>
      </el-form-item>
      <el-form-item label="目标床位" required>
        <el-select v-model="transferForm.newBedId" class="!w-full" placeholder="本区空闲床位">
          <el-option v-for="b in transferBeds" :key="b.bedId" :label="b.bedNo" :value="b.bedId"/>
        </el-select>
        <p v-if="!transferBeds.length" class="mt-1 text-xs text-rose-500">该病区没有其他空闲床位</p>
      </el-form-item>
      <el-form-item label="换床原因">
        <el-input v-model="transferForm.reason" placeholder="如 病情变化需调至监护床位"/>
      </el-form-item>
    </el-form>
    <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
      只能在同一科室内部换床；跨科室属于「转科」，走转科流程并需要重新确定主管医师。
    </div>
    <template #footer>
      <el-button v-perm="'ipd:inpatient:edit'" :loading="transferLoading" type="primary" @click="submitTransfer">
        确认换床
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getInpatientBedList, transferInpatientBed} from '@/api/inpatient';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前住院列表行：取 admissionId / wardId / wardName / bedId
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const transferLoading = ref(false);
const transferBeds = ref([]);
const transferForm = reactive({admissionId: '', newBedId: null, reason: '', wardName: ''});

watch(visible, async (open) => {
  if (!open)
    return;
  const row = props.row || {};
  transferForm.admissionId = row.admissionId;
  transferForm.newBedId = null;
  transferForm.reason = '';
  transferForm.wardName = row.wardName || '';
  transferBeds.value = [];
  try {
    const res = await getInpatientBedList({wardId: row.wardId, bedStatus: 1});
    // 后端规则：只能同科室换床，且不能换到原床位，这里先把原床位剔掉
    transferBeds.value = (res.data || []).filter((b) => String(b.bedId) !== String(row.bedId));
  } catch { /* 忽略 */
  }
});
const submitTransfer = async () => {
  if (!transferForm.newBedId)
    return ElMessage.warning('请选择目标床位');
  transferLoading.value = true;
  try {
    await transferInpatientBed({...transferForm});
    ElMessage.success('换床成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '换床失败');
  } finally {
    transferLoading.value = false;
  }
};
</script>
