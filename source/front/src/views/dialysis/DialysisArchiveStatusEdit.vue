<template>
  <!-- ============ 档案状态弹框 ============ -->
  <el-dialog v-model="visible" data-testid="hd-status-dialog" title="档案状态变更" width="520px">
    <el-form label-width="90px">
      <el-form-item label="患者">{{ statusForm.patientName }}（{{ statusForm.dialysisNo }}）</el-form-item>
      <el-form-item label="当前状态">{{ dictText('archiveStatus', statusForm.currentStatus) }}</el-form-item>
      <el-form-item label="目标状态" required>
        <el-select v-model="statusForm.status" :fit-input-width="false" class="w-40" data-testid="hd-status-target"
                   placeholder="选择状态">
          <el-option v-for="d in dicts.archiveStatus" :key="d.dictValue" :disabled="Number(d.dictValue) === Number(statusForm.currentStatus)"
                     :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="statusForm.reason" :rows="2" data-testid="hd-status-reason"
                  placeholder="暂停/退出必填（转腹透、肾移植、死亡、失访等）" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="statusSaving" data-testid="hd-status-ok" type="primary" @click="saveStatus">确认变更
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 档案状态变更弹框（暂停/退出原因必填）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {changeDialysisArchiveStatus} from '@/api/dialysis';
import {dictLabelText} from '@/lib/utils';
import {ARCHIVE_STATUS} from '@/lib/dialysis';

const props = defineProps({
  modelValue: Boolean,
  // 当前行：打开瞬间先显示行上的基本信息，状态变更细节在本组件内处理
  row: {type: Object, default: null},
  // 页面级参照：档案状态字典由主页面加载一次后传入
  dicts: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictText = (key, value) => dictLabelText(props.dicts[key], value);

const statusSaving = ref(false);
const statusForm = reactive({id: null, dialysisNo: '', patientName: '', currentStatus: null, status: null, reason: ''});
const saveStatus = async () => {
  if (!statusForm.status) {
    ElMessage.warning('请选择目标状态');
    return;
  }
  if (statusForm.status !== ARCHIVE_STATUS.ON && !statusForm.reason.trim()) {
    ElMessage.warning('暂停/退出必须填写原因');
    return;
  }
  statusSaving.value = true;
  try {
    const res = await changeDialysisArchiveStatus({
      id: statusForm.id, status: statusForm.status, reason: statusForm.reason || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '状态已更新');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '状态更新失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '状态更新失败');
  } finally {
    statusSaving.value = false;
  }
};

watch(visible, (v) => {
  if (!v) return;
  const row = props.row;
  Object.assign(statusForm, {
    id: row?.id ?? null, dialysisNo: row?.dialysisNo ?? '', patientName: row?.patientName ?? '',
    currentStatus: row?.status ?? null, status: null, reason: '',
  });
});
</script>
