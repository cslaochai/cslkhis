<template>
  <el-dialog v-model="visible" title="草稿移交代补" data-testid="handover-dialog" width="640px">
    <el-alert
        class="mb-3"
        type="info"
        :closable="false"
        title="移交后由承接人补写并提交；病历经治医生仍为原接诊医生，移交与代补过程全程留痕。"
    />
    <el-form label-width="90px">
      <el-form-item label="待移交病历">
        <div class="w-full space-y-1 text-xs text-slate-600">
          <div v-for="r in records" :key="r.id" class="flex items-center gap-2">
            <span class="font-mono">{{ r.recordNo }}</span>
            <span>{{ r.patientName }}</span>
            <span class="text-slate-400">原经治：{{ r.doctorName || '' }}</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="承接人" required>
        <el-select
            v-model="form.receiverId"
            :fit-input-width="false"
            filterable
            data-testid="handover-receiver"
            placeholder="搜索上级医师/质控岗"
            style="width: 100%"
            @change="onReceiverChange"
        >
          <el-option
              v-for="emp in receiverOptions"
              :key="emp.id"
              :label="`${emp.empName}（${emp.deptName || '未配科室'}）`"
              :value="emp.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="移交原因" required>
        <el-select v-model="form.reason" :fit-input-width="false" data-testid="handover-reason"
                   placeholder="请选择" style="width: 100%">
          <el-option v-for="opt in REASON_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="移交说明">
        <el-input v-model="form.remark" :rows="3" data-testid="handover-remark" maxlength="500"
                  placeholder="补充说明（可空）" show-word-limit type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" type="primary" @click="save">确认移交</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {assignHandover} from '@/api/emr';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 选中的草稿行（只含草稿态）
  records: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const REASON_OPTIONS = [
  {value: 1, label: '离职'},
  {value: 2, label: '长期休假'},
  {value: 3, label: '调离临床岗位'},
  {value: 4, label: '其他'},
];

const saving = ref(false);
const receiverOptions = ref([]);
const form = reactive({
  receiverId: null,
  reason: null,
  remark: '',
});

const loadReceivers = async () => {
  try {
    const res = await getEmployeeList({status: 1, pageNum: 1, pageSize: 200});
    receiverOptions.value = res.data?.records || res.data || [];
  } catch (error) {
    console.error('加载承接人列表失败:', error);
  }
};

const onReceiverChange = () => {
  // 占位：保留变更钩子，便于后续按科室/角色收敛候选
};

watch(() => props.modelValue, (v) => {
  if (!v) return;
  form.receiverId = null;
  form.reason = null;
  form.remark = '';
  loadReceivers();
});

const save = async () => {
  if (!form.receiverId) {
    ElMessage.warning('请选择承接人');
    return;
  }
  if (form.reason == null) {
    ElMessage.warning('请选择移交原因');
    return;
  }
  saving.value = true;
  try {
    await assignHandover({
      recordIds: props.records.map((r) => r.id),
      receiverId: form.receiverId,
      reason: form.reason,
      remark: form.remark?.trim() || undefined,
    });
    ElMessage.success('移交成功');
    visible.value = false;
    emit('saved');
  } catch (error) {
    console.error('移交失败:', error);
  } finally {
    saving.value = false;
  }
};
</script>
