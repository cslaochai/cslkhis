<template>
  <!-- 新建/编辑 -->
  <el-dialog v-model="visible" :title="editForm.id ? '编辑随访任务' : '新建随访任务'" width="560px">
    <el-form label-width="90px">
      <el-form-item label="患者" required>
        <PatientSelect v-model="editForm.patientId" :disabled="!!editForm.id" @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="随访类型" required>
        <el-select v-model="editForm.followupType" :fit-input-width="false" style="width: 220px">
          <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="计划时间" required>
        <el-date-picker v-model="editForm.followupTime" placeholder="计划随访时间" type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="随访内容" required>
        <el-input v-model="editForm.followupContent" :rows="3" type="textarea"/>
        <el-button :loading="composing" class="mt-1 self-start" link type="primary"
                   @click="composeAiContent">AI 拟话术
        </el-button>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="editForm.phone" placeholder="不填自动取患者档案电话" style="width: 220px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="['inpatient:followup:add','inpatient:followup:edit']" :loading="editLoading" type="primary"
                 @click="submitEdit">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 新建 / 编辑随访任务（后端是同一个 upsert，row 为空走新增、有值走修改）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {followupUpsert, getFollowupDetail} from '@/api/followup';
import {composeFollowup} from '@/api/ai';
import {fmtTime} from '@/lib/followup';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 列表行：有 id = 修改，为空 = 新增
  row: {type: Object, default: null},
  // 随访类型字典由页面加载一次传下来（列表列同一份）
  typeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const editLoading = ref(false);
const editForm = reactive({
  id: null, patientId: null, patientName: '',
  followupType: 1, followupTime: '', followupContent: '', phone: '', remark: '',
  diagnosis: '',
});

const fillBlank = () => {
  Object.assign(editForm, {
    id: null, patientId: null, patientName: '', followupType: 1,
    followupTime: '', followupContent: '', phone: '', remark: '', diagnosis: '',
  });
};

/**
 * 编辑必须走 getById 取明文：列表行里的 phone 是 null、只有 phoneMasked。
 * 用 `editForm.phone = row.phone` 会把电话栏清空，保存后随访员打电话没号可拨。
 */
const fillFromTask = async (id) => {
  try {
    const res = await getFollowupDetail(id);
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载任务失败');
      return false;
    }
    const t = res.data || {};
    Object.assign(editForm, {
      id: t.id, patientId: t.patientId, patientName: t.patientName, followupType: t.followupType,
      followupTime: fmtTime(t.followupTime),
      followupContent: t.followupContent || '', phone: t.phone || '', remark: t.remark || '',
      diagnosis: t.diagnosis || '',
    });
    return true;
  } catch (e) {
    console.error(e);
    ElMessage.error('加载任务失败');
    return false;
  }
};

watch(visible, async (open) => {
  if (!open) return;
  if (!props.row?.id) {
    fillBlank();
    return;
  }
  await fillFromTask(props.row.id);
});

const onPatientSelect = (p) => {
  editForm.patientName = p?.patientName || p?.name || '';
  if (p?.phone && !editForm.phone)
    editForm.phone = p.phone;
};
/**
 * G-06 AI 拟话术：按随访类型 + 患者慢病档案让模型草拟话术，医生核对改完再保存。
 * degraded=true 表示落到了类型模板（模型不可用或文案越界被闸），必须说人话告知，不许当模型产出展示。
 */
const composing = ref(false);
const composeAiContent = async () => {
  if (!editForm.patientId) {
    ElMessage.warning('请先选择患者');
    return;
  }
  composing.value = true;
  try {
    const res = await composeFollowup({
      patientId: editForm.patientId,
      followupType: editForm.followupType,
      diagnosis: editForm.diagnosis || undefined,
    });
    if (res.code === 200 && res.data?.content) {
      editForm.followupContent = res.data.content;
      if (res.data.degraded)
        ElMessage.warning(`AI 暂不可用（${res.data.degradeReason || '原因未知'}），已按随访类型模板生成`);
      else
        ElMessage.success('AI 话术已填入，请核对修改后保存');
    } else
      ElMessage.error(res.message || 'AI 未生成话术');
  } catch (e) {
    console.error(e);
    ElMessage.error('AI 拟话术失败');
  } finally {
    composing.value = false;
  }
};
const submitEdit = async () => {
  if (!editForm.patientId || !editForm.followupTime || !editForm.followupContent.trim()) {
    ElMessage.warning('患者、计划时间、随访内容为必填');
    return;
  }
  editLoading.value = true;
  try {
    const res = await followupUpsert({
      id: editForm.id ?? undefined,
      patientId: editForm.patientId,
      followupType: editForm.followupType,
      followupTime: editForm.followupTime,
      followupContent: editForm.followupContent.trim(),
      phone: editForm.phone || undefined,
      remark: editForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('随访任务已保存');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    editLoading.value = false;
  }
};
</script>
