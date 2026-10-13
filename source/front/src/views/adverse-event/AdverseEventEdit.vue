<template>
  <el-dialog v-model="visible" :title="form.id ? '修改不良事件' : '上报不良事件'" data-testid="ae-dialog" width="640px">
    <el-form label-width="90px">
      <el-form-item label="事件类型" required>
        <el-select v-model="form.eventType" :fit-input-width="false" data-testid="ae-form-type" placeholder="请选择"
                   style="width: 100%">
          <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="事件等级" required>
        <el-select v-model="form.eventLevel" :fit-input-width="false" data-testid="ae-form-level" placeholder="请选择"
                   style="width: 100%">
          <el-option v-for="d in levelDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.eventType === EVENT_TYPE_PRESSURE_INJURY" label="压疮来源" required>
        <el-select v-model="form.acquiredFlag" :fit-input-width="false" data-testid="ae-form-acquired"
                   placeholder="院内获得 / 入院带入" style="width: 100%">
          <el-option v-for="d in acquiredDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="发生科室" required>
        <el-select v-model="form.occurDeptId" :fit-input-width="false" data-testid="ae-form-dept" filterable
                   placeholder="请选择" style="width: 100%">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="发生时间" required>
        <el-date-picker
            v-model="form.occurTime"
            :fit-input-width="false"
            format="YYYY-MM-DD HH:mm"
            placeholder="选择发生时间"
            style="width: 100%"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm"
        />
      </el-form-item>
      <el-form-item label="关联患者">
        <PatientSelect
            v-model="form.patientId"
            placeholder="搜索患者（可空：非患者事件）"
            width="100%"
            @clear="form.patientId = null; form.patientName = ''"
            @select="onPatientSelect"
        />
      </el-form-item>
      <el-form-item label="事件摘要" required>
        <el-input v-model="form.title" data-testid="ae-form-title" maxlength="100" placeholder="一句话概括事件"
                  show-word-limit/>
      </el-form-item>
      <el-form-item label="事件经过" required>
        <el-input v-model="form.description" :rows="4" data-testid="ae-form-desc"
                  placeholder="详细经过（时间、地点、人员、过程、后果）" type="textarea"/>
      </el-form-item>
      <el-form-item label="即时处置">
        <el-input v-model="form.immediateAction" :rows="2" placeholder="已采取的即时处置措施（可空）" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="['emr:adverseEvent:add','emr:adverseEvent:edit']" :loading="saving" data-testid="ae-save-btn"
                 type="primary" @click="save">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 不良事件「上报 / 修改」弹框（自 AdverseEventView 拆出，同一表单布局按 row 有无区分新增与修改）
 *
 * 字典与科室列表由页面统一加载后传入（筛选/列表列同一份数据，见拆分规范第 6 条）；
 * 保存成功后 emit('saved')，由页面刷新列表与统计卡。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertAdverseEvent} from '@/api/adverseEvent';
import {EVENT_TYPE_PRESSURE_INJURY} from '@/lib/adverseEvent';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 修改时传入的行快照（null=上报新事件）
  row: {type: Object, default: null},
  // 事件类型字典（页面已加载）
  typeDict: {type: Array, default: () => []},
  // 事件等级字典（页面已加载）
  levelDict: {type: Array, default: () => []},
  // 压疮来源字典（页面已加载）
  acquiredDict: {type: Array, default: () => []},
  // 科室下拉数据（页面已加载）
  deptOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null,
  eventType: null,
  eventLevel: null,
  occurDeptId: null,
  acquiredFlag: null,
  occurTime: '',
  patientId: null,
  patientName: '',
  title: '',
  description: '',
  immediateAction: '',
});

// 每次打开按 row 快照初始化：有 row=修改（口径与拆分前 openEdit 一致），空=上报
watch(() => props.modelValue, (v) => {
  if (!v) return;
  const r = props.row;
  if (r) {
    form.id = r.id;
    form.eventType = Number(r.eventType);
    form.eventLevel = Number(r.eventLevel);
    form.occurDeptId = r.occurDeptId;
    form.acquiredFlag = r.acquiredFlag == null ? null : Number(r.acquiredFlag);
    form.occurTime = (r.occurTime || '').slice(0, 16);
    form.patientId = r.patientId;
    form.patientName = r.patientName || '';
    form.title = r.title || '';
    form.description = r.description || '';
    form.immediateAction = r.immediateAction || '';
  } else {
    form.id = null;
    form.eventType = null;
    form.eventLevel = null;
    form.occurDeptId = null;
    form.acquiredFlag = null;
    form.occurTime = '';
    form.patientId = null;
    form.patientName = '';
    form.title = '';
    form.description = '';
    form.immediateAction = '';
  }
});

const onPatientSelect = (p) => {
  form.patientId = p?.id ?? null;
  form.patientName = p?.name || p?.patientName || '';
};

const save = async () => {
  if (!form.eventType || !form.eventLevel || !form.occurDeptId) {
    ElMessage.warning('请选择事件类型、等级与发生科室');
    return;
  }
  if (!form.occurTime) {
    ElMessage.warning('请选择发生时间');
    return;
  }
  if (!form.title.trim() || !form.description.trim()) {
    ElMessage.warning('请填写事件摘要与详细经过');
    return;
  }
  // 压疮必须显式选来源：入院带入的皮肤问题不算本院的发生率（护理质控千床日率口径），
  // 让服务端兜默认值等于把「没区分」洗成「院内获得」，评审一查就对不上
  if (form.eventType === EVENT_TYPE_PRESSURE_INJURY && !form.acquiredFlag) {
    ElMessage.warning('压力性损伤请选择来源：院内获得还是入院带入');
    return;
  }
  saving.value = true;
  try {
    const res = await upsertAdverseEvent({
      id: form.id || undefined,
      eventType: form.eventType,
      eventLevel: form.eventLevel,
      occurDeptId: form.occurDeptId,
      acquiredFlag: form.eventType === EVENT_TYPE_PRESSURE_INJURY ? form.acquiredFlag : undefined,
      occurTime: form.occurTime.length === 16 ? `${form.occurTime}:00` : form.occurTime,
      patientId: form.patientId || undefined,
      title: form.title.trim(),
      description: form.description.trim(),
      immediateAction: form.immediateAction.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(form.id ? '修改成功' : '上报成功');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
