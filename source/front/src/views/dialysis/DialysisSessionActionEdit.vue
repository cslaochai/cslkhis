<template>
  <!-- ============ 动作弹框（上机/下机/反应/取消/改期）============
       modelValue 声明为 Boolean，Vue 会把 '' 强转成 true，所以不能直接 v-model 绑字符串 kind，
       否则进页面就自带弹开一个空弹框（遮罩挡住整页）。 -->
  <el-dialog v-model="visible"
             :title="actionTitle"
             data-testid="hd-action-dialog" width="560px">
    <div v-if="row" class="text-sm text-slate-500 mb-3">
      {{ row.patientName }} · {{ row.dialysisDate }} {{ dictText('slot', row.timeSlot) }} ·
      机位 {{ row.machineNo }} · {{ row.sessionNo }}
    </div>
    <el-form label-width="110px">
      <template v-if="kind === 'start'">
        <el-form-item label="透前体重(kg)" required>
          <el-input-number v-model="actionForm.beforeWeight" :controls="false" :max="999.99" :min="0" :precision="2"
                           class="w-32" data-testid="hd-action-before-weight"/>
        </el-form-item>
        <el-form-item label="通路评估" required>
          <el-input v-model="actionForm.accessCheck" :rows="2" data-testid="hd-action-access" placeholder="如 内瘘血流量良好、无渗血"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="上机时间">
          <el-date-picker v-model="actionForm.onTime" class="w-52" data-testid="hd-action-on-time"
                          placeholder="默认当前时间" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
      </template>
      <template v-else-if="kind === 'finish'">
        <el-form-item label="透后体重(kg)" required>
          <el-input-number v-model="actionForm.afterWeight" :controls="false" :max="999.99" :min="0" :precision="2"
                           class="w-32" data-testid="hd-action-after-weight"/>
        </el-form-item>
        <el-form-item label="下机时间">
          <el-date-picker v-model="actionForm.offTime" class="w-52" data-testid="hd-action-off-time"
                          placeholder="默认当前时间" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <div class="text-xs text-slate-500 pl-[110px]">超滤量
          =（透前-透后）×1000，实际时长按下机-上机回算，均由服务端计算。
        </div>
      </template>
      <template v-else-if="kind === 'adverse'">
        <el-form-item label="不良反应" required>
          <el-select v-model="actionForm.adverseType" :fit-input-width="false" class="w-48" data-testid="hd-action-adverse-type"
                     placeholder="选择类型">
            <el-option v-for="d in dicts.adverse" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="处置描述">
          <el-input v-model="actionForm.adverseDesc" :rows="3" data-testid="hd-action-adverse-desc"
                    placeholder="如 透中血压下降至 90/55，降低血流速并补生理盐水 200ml"
                    type="textarea"/>
        </el-form-item>
      </template>
      <template v-else-if="kind === 'cancel'">
        <el-form-item label="取消原因" required>
          <el-input v-model="actionForm.reason" :rows="2" data-testid="hd-action-reason" placeholder="如 患者临时发热，本次取消"
                    type="textarea"/>
        </el-form-item>
      </template>
      <template v-else-if="kind === 'reschedule'">
        <el-form-item label="日期">
          <el-date-picker v-model="actionForm.dialysisDate" :clearable="false" class="w-40" data-testid="hd-action-date"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="actionForm.timeSlot" :fit-input-width="false" class="w-32" data-testid="hd-action-slot">
            <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="机位">
          <el-select v-model="actionForm.machineId" :fit-input-width="false" class="w-40" data-testid="hd-action-machine"
                     filterable>
            <el-option v-for="m in machineOptions" :key="m.id" :label="`${m.machineNo} ${m.roomName || ''}`"
                       :value="m.id"/>
          </el-select>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="actionSaving" data-testid="hd-action-ok" type="primary" @click="doAction">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 透析单动作弹框：上机/下机/不良反应/取消/改期共用一个壳，kind 决定表单与提交的接口
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {
  cancelDialysisSession,
  finishDialysisSession,
  recordDialysisAdverse,
  rescheduleDialysisSession,
  startDialysisSession,
} from '@/api/dialysis';
import {dictLabelText} from '@/lib/utils';

const props = defineProps({
  modelValue: Boolean,
  // start / finish / adverse / cancel / reschedule，由父页面按钮决定
  kind: {type: String, default: ''},
  // 当前透析单行（打开瞬间不闪白 + 头部信息展示）
  row: {type: Object, default: null},
  // 页面级参照：时段/不良反应字典由主页面加载一次后传入
  dicts: {type: Object, required: true},
  machineOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictText = (key, value) => dictLabelText(props.dicts[key], value);

const actionSaving = ref(false);
const actionForm = reactive({
  beforeWeight: null, accessCheck: '', onTime: '', afterWeight: null, offTime: '',
  adverseType: null, adverseDesc: '', reason: '', dialysisDate: '', timeSlot: null, machineId: null,
});
const actionTitle = computed(() => ({
  start: '上机登记', finish: '下机登记', adverse: '登记不良反应', cancel: '取消治疗单', reschedule: '改期/改机位',
}[props.kind] || ''));
const doAction = async () => {
  const row = props.row;
  if (!row)
    return;
  const kind = props.kind;
  let payload = {id: row.id};
  let call = null;
  if (kind === 'start') {
    if (actionForm.beforeWeight == null || !actionForm.accessCheck.trim()) {
      ElMessage.warning('透前体重与通路评估都必填');
      return;
    }
    payload = {
      ...payload,
      beforeWeight: actionForm.beforeWeight,
      accessCheck: actionForm.accessCheck,
      onTime: actionForm.onTime || undefined
    };
    call = startDialysisSession;
  } else if (kind === 'finish') {
    if (actionForm.afterWeight == null) {
      ElMessage.warning('透后体重必填');
      return;
    }
    payload = {...payload, afterWeight: actionForm.afterWeight, offTime: actionForm.offTime || undefined};
    call = finishDialysisSession;
  } else if (kind === 'adverse') {
    if (!actionForm.adverseType) {
      ElMessage.warning('请选择不良反应类型');
      return;
    }
    payload = {...payload, adverseType: actionForm.adverseType, adverseDesc: actionForm.adverseDesc || undefined};
    call = recordDialysisAdverse;
  } else if (kind === 'cancel') {
    if (!actionForm.reason.trim()) {
      ElMessage.warning('取消原因必填');
      return;
    }
    payload = {...payload, reason: actionForm.reason};
    call = cancelDialysisSession;
  } else if (kind === 'reschedule') {
    if (!actionForm.dialysisDate || !actionForm.machineId || !actionForm.timeSlot) {
      ElMessage.warning('日期、时段、机位都必选');
      return;
    }
    payload = {
      ...payload,
      dialysisDate: actionForm.dialysisDate,
      timeSlot: actionForm.timeSlot,
      machineId: actionForm.machineId
    };
    call = rescheduleDialysisSession;
  }
  actionSaving.value = true;
  try {
    const res = await call(payload);
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败');
  } finally {
    actionSaving.value = false;
  }
};

watch(visible, (v) => {
  if (!v) return;
  const row = props.row;
  Object.assign(actionForm, {
    beforeWeight: null, accessCheck: '', onTime: '', afterWeight: null, offTime: '',
    adverseType: null, adverseDesc: '', reason: '',
    dialysisDate: row?.dialysisDate, timeSlot: row?.timeSlot, machineId: row?.machineId,
  });
});
</script>
