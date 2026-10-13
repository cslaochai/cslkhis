<template>
  <!-- 发起转科 -->
  <el-dialog v-model="visible" data-testid="p4-transfer-apply-dialog" title="发起转科" width="640px">
    <el-form label-width="96px">
      <el-form-item label="在院患者" required>
        <el-select
            v-model="applyForm.admissionId"
            class="!w-full"
            data-testid="p4-apply-admission"
            filterable
            placeholder="选择在院患者"
            @change="handleApplyAdmissionChange"
        >
          <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="currentAdmission" label="当前所在">
        <span class="text-slate-700">
          {{ text(currentAdmission.deptName) }} / {{ text(currentAdmission.wardName) }} /
          {{ text(currentAdmission.bedNo) }}床
        </span>
        <span class="ml-2 text-[11px] text-slate-400">转出科室以此为准，发起时不改动</span>
      </el-form-item>
      <el-form-item label="转入科室" required>
        <el-select
            v-model="applyForm.toDeptId"
            class="!w-full"
            data-testid="p4-apply-dept"
            filterable
            placeholder="选择转入科室（已排除原科室）"
            @change="handleApplyDeptChange"
        >
          <el-option v-for="d in applyDepts" :key="d.id" :label="d.deptName || d.id" :value="String(d.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="转入病区" required>
        <el-select
            v-model="applyForm.toWardId"
            :disabled="!applyForm.toDeptId"
            class="!w-full"
            data-testid="p4-apply-ward"
            filterable
            placeholder="选择转入病区"
            @change="handleApplyWardChange"
        >
          <el-option
              v-for="w in applyWards"
              :key="w.wardId"
              :label="`${w.wardName || w.wardId}（空闲 ${w.freeBeds ?? 0}）`"
              :value="String(w.wardId)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="转入床位" required>
        <el-select
            v-model="applyForm.toBedId"
            :disabled="!applyForm.toWardId"
            class="!w-full"
            data-testid="p4-apply-bed"
            filterable
            placeholder="选择空闲床位"
        >
          <el-option
              v-for="b in targetBeds"
              :key="b.bedId"
              :label="`${b.bedNo || b.bedId}床`"
              :value="String(b.bedId)"
          />
        </el-select>
        <div v-if="applyForm.toWardId && targetBeds.length === 0" class="mt-1 text-[11px] text-red-500">
          该病区当前没有空闲床位
        </div>
      </el-form-item>
      <el-form-item label="转科类型">
        <el-radio-group v-model="applyForm.transferType" data-testid="p4-apply-type">
          <el-radio :value="1">普通转科</el-radio>
          <el-radio :value="2">急诊转科</el-radio>
          <el-radio :value="3">转入ICU</el-radio>
          <el-radio :value="4">ICU转出</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="转科原因" required>
        <el-input
            v-model="applyForm.transferReason"
            :rows="3"
            data-testid="p4-apply-reason"
            placeholder="请写明要解决的问题（如：骨折需手术治疗、病情稳定转回普通病房）"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="applyForm.remark" :rows="2" placeholder="可空" type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
      <el-icon class="mr-1 align-middle">
        <Warning/>
      </el-icon>
      发起后转科<b>尚未生效</b>：床位不占、科室不改、医嘱不停，需转入科室在列表里点「接收」。
      接收时系统会自动停掉原科室「已校对 / 执行中」的长期医嘱，并回写一份转科记录病历。
    </div>
    <template #footer>
      <el-button v-perm="'ipd:transfer:add'" :loading="applySubmitting" data-testid="p4-apply-submit" type="primary"
                 @click="submitApply">
        提交转科申请
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 发起转科：只新增（无修改语义），表单态与提交都在本组件。
 * 在院患者名册与科室名册列表页也在用，由页面加载一次后以 prop 传进来；
 * 病区名册只有本表单用，打开时自加载。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Warning} from '@element-plus/icons-vue';
import {saveTransfer} from '@/api/inpatientTransfer';
import {getInpatientBedList, getInpatientWardList} from '@/api/inpatient';
import {admissionLabel, text} from '@/lib/transfer';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 在院患者名册（列表页顶部过滤共用同一份）
  admissions: {type: Array, default: () => []},
  // 科室名册（列表页筛选共用同一份）
  depts: {type: Array, default: () => []},
  // 页面顶部「按在院患者过滤」已选的患者：点发起时带进表单
  presetAdmissionId: {type: String, default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const applySubmitting = ref(false);
const applyForm = reactive({
  admissionId: '',
  toDeptId: '',
  toWardId: '',
  toBedId: '',
  transferType: 1,
  transferReason: '',
  remark: '',
});
const currentAdmission = computed(() => props.admissions.find((a) => String(a.admissionId) === String(applyForm.admissionId)));
const wards = ref([]);
const wardsLoaded = ref(false);
const loadWards = async () => {
  try {
    const res = await getInpatientWardList();
    wards.value = (res.data || []);
  } catch (error) {
    console.error('加载病区失败:', error);
  }
};
/** 目标病区：按目标科室过滤（后端也会校验"病区属于科室"，前端只是少让人点错） */
const applyWards = computed(() => wards.value.filter((w) => String(w.deptId) === String(applyForm.toDeptId)));
/** 目标科室：排除患者当前科室（同科室挪床请走换床） */
const applyDepts = computed(() => props.depts.filter((d) => String(d.id) !== String(currentAdmission.value?.deptId || '')));
/** 目标床位：只取空闲（bedStatus=1）—— "能不能用"以 sys_bed 为准，不看病区的演示计数 */
const targetBeds = ref([]);
const loadTargetBeds = async (wardId) => {
  targetBeds.value = [];
  if (!wardId)
    return;
  try {
    const res = await getInpatientBedList({wardId, bedStatus: 1});
    targetBeds.value = (res.data || []);
  } catch (error) {
    console.error('加载床位失败:', error);
  }
};
watch(visible, async (open) => {
  if (!open)
    return;
  applyForm.admissionId = props.presetAdmissionId || '';
  applyForm.toDeptId = '';
  applyForm.toWardId = '';
  applyForm.toBedId = '';
  applyForm.transferType = 1;
  applyForm.transferReason = '';
  applyForm.remark = '';
  targetBeds.value = [];
  if (!wardsLoaded.value) {
    wardsLoaded.value = true;
    await loadWards();
  }
});
const handleApplyAdmissionChange = () => {
  // 换了患者就重选目标：科室 → 病区 → 床位三者联动，留着上一轮的会填到错误科室
  applyForm.toDeptId = '';
  applyForm.toWardId = '';
  applyForm.toBedId = '';
  targetBeds.value = [];
};
const handleApplyDeptChange = () => {
  applyForm.toWardId = '';
  applyForm.toBedId = '';
  targetBeds.value = [];
};
const handleApplyWardChange = () => {
  applyForm.toBedId = '';
  loadTargetBeds(applyForm.toWardId);
};
const submitApply = async () => {
  if (!applyForm.admissionId) {
    ElMessage.warning('请选择要转科的在院患者');
    return;
  }
  if (!applyForm.toDeptId || !applyForm.toWardId || !applyForm.toBedId) {
    ElMessage.warning('请完整选择转入科室、病区与床位');
    return;
  }
  if (!applyForm.transferReason.trim()) {
    ElMessage.warning('请填写转科原因（转科是一个医疗决定，必须写清理由）');
    return;
  }
  applySubmitting.value = true;
  try {
    const res = await saveTransfer({
      admissionId: applyForm.admissionId,
      toDeptId: applyForm.toDeptId,
      toWardId: applyForm.toWardId,
      toBedId: applyForm.toBedId,
      transferType: applyForm.transferType,
      transferReason: applyForm.transferReason.trim(),
      remark: applyForm.remark.trim() || undefined,
    });
    ElMessage.success(`转科申请已提交：${res.data || ''}（等待转入科室接收）`);
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '转科申请提交失败');
  } finally {
    applySubmitting.value = false;
  }
};
</script>
