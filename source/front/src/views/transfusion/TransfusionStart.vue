<template>
  <el-dialog
      v-model="visible"
      data-testid="p4-tf-start-dialog"
      title="开始输注（输血前双人核对）"
      width="700px"
  >
    <div v-if="row" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">{{ text(row.patientName) }}</div>
        <div class="text-slate-700">
          {{ text(row.bloodTypeText) }} · {{ text(row.bloodComponentText) }} ·
          {{ text(row.bagProgressText) }}
        </div>
        <div class="text-[12px] text-slate-500">
          发血人 {{ text(row.issueDoctorName) }} · {{ fmt(row.issueTime) }}
        </div>
      </div>
      <div class="text-[12px] text-slate-500">
        标 <span class="text-red-500">*</span> 的是必核项，缺任何一项都无法提交 ——
        输血是唯一要求双人核对的护理操作，"已核对无误"这句话在飞检时回答不了"到底核了哪几项"。
      </div>
      <el-checkbox-group v-model="startForm.items" class="flex flex-col gap-2">
        <el-checkbox
            v-for="i in checkItemOptions"
            :key="i.code"
            :data-testid="`p4-tf-check-item-${i.code}`"
            :value="i.code"
        >
          <span class="text-slate-700">{{ i.label }}</span>
          <span v-if="i.required" class="ml-1 text-red-500">*</span>
        </el-checkbox>
      </el-checkbox-group>
      <el-form label-width="110px">
        <el-form-item label="核对护士 1" required>
          <el-select
              v-model="startForm.checkNurseId"
              class="!w-full"
              data-testid="p4-tf-check-nurse1"
              filterable
              placeholder="选择核对护士1"
          >
            <el-option
                v-for="e in employees"
                :key="e.id"
                :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                :value="String(e.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="核对护士 2" required>
          <el-select
              v-model="startForm.checkNurse2Id"
              class="!w-full"
              data-testid="p4-tf-check-nurse2"
              filterable
              placeholder="必须与护士1 不同人"
          >
            <el-option
                v-for="e in employees"
                :key="e.id"
                :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                :value="String(e.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="核对异常说明">
          <el-input
              v-model="startForm.checkNote"
              :rows="2"
              data-testid="p4-tf-check-note"
              placeholder="正常可空；有异常必须写（如：血袋外观有轻微气泡，已请血库复核）"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="输注开始时间" required>
          <el-date-picker
              v-model="startForm.infusionStartTime"
              class="!w-full"
              data-testid="p4-tf-start-time"
              placeholder="选择开始时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="执行护士">
          <el-select
              v-model="startForm.infusionNurseId"
              class="!w-full"
              clearable
              filterable
              placeholder="为空 = 由核对护士1 执行"
          >
            <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="滴速">
          <el-input v-model="startForm.infusionSpeed" maxlength="32" placeholder="如 60滴/分（前 15 分钟须慢速）"/>
        </el-form-item>
        <el-form-item label="输注观察">
          <el-input
              v-model="startForm.observation"
              :rows="2"
              placeholder="开始输注时的生命体征（也可在「登记完成」时补填）"
              type="textarea"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button
          v-perm="'medtech:transfusion:edit'"
          :loading="startSubmitting"
          data-testid="p4-tf-start-submit"
          type="primary"
          @click="submitStart"
      >
        确认已核对并开始输注
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 四、开始输注（双人核对） ----------------
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getTransfusionCheckItems, startTransfusion} from '@/api/inpatientTransfusion';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const employees = ref([]);
const checkItemOptions = ref([]);
const loadEmployees = async () => {
  try {
    const res = await getEmployeeList({});
    employees.value = (res.data || []);
  } catch (error) {
    console.error('加载员工失败:', error);
  }
};
const loadCheckItems = async () => {
  try {
    const res = await getTransfusionCheckItems();
    checkItemOptions.value = (res.data || []);
  } catch (error) {
    console.error('加载输血核对项失败:', error);
  }
};

const startSubmitting = ref(false);
const startForm = reactive({
  items: [],
  checkNote: '',
  checkNurseId: '',
  checkNurse2Id: '',
  infusionNurseId: '',
  infusionStartTime: '',
  infusionSpeed: '',
  observation: '',
});

watch(visible, async (open) => {
  if (!open) return;
  startForm.items = [];
  startForm.checkNote = '';
  startForm.checkNurseId = '';
  startForm.checkNurse2Id = '';
  startForm.infusionNurseId = '';
  startForm.infusionStartTime = '';
  startForm.infusionSpeed = '';
  startForm.observation = '';
  if (!employees.value.length) await loadEmployees();
  if (!checkItemOptions.value.length) await loadCheckItems();
});

const submitStart = async () => {
  if (!props.row)
    return;
  const missing = checkItemOptions.value
      .filter((i) => i.required && !startForm.items.includes(i.code))
      .map((i) => i.label);
  if (missing.length > 0) {
    ElMessage.warning(`输血前核对必核项未完成：${missing.join('；')}`);
    return;
  }
  if (!startForm.checkNurseId || !startForm.checkNurse2Id) {
    ElMessage.warning('请选择两名核对护士（输血必须双人核对）');
    return;
  }
  if (startForm.checkNurseId === startForm.checkNurse2Id) {
    ElMessage.warning('两名核对护士不能是同一个人（写同一个人等于没有双人核对）');
    return;
  }
  if (!startForm.infusionStartTime) {
    ElMessage.warning('请选择输注开始时间');
    return;
  }
  startSubmitting.value = true;
  try {
    await startTransfusion({
      applyId: props.row.id,
      checkItems: startForm.items.join(','),
      checkNote: startForm.checkNote.trim() || undefined,
      checkNurseId: startForm.checkNurseId,
      checkNurse2Id: startForm.checkNurse2Id,
      infusionNurseId: startForm.infusionNurseId || undefined,
      infusionStartTime: startForm.infusionStartTime,
      infusionSpeed: startForm.infusionSpeed.trim() || undefined,
      observation: startForm.observation.trim() || undefined,
    });
    ElMessage.success('已开始输注（双人核对完成）');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '开始输注失败');
  } finally {
    startSubmitting.value = false;
  }
};
</script>
