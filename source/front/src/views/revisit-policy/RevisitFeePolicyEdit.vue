<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑复诊收费策略' : '新增复诊收费策略'" destroy-on-close
             width="560px">
    <el-form label-width="120px">
      <el-form-item label="策略名称" required>
        <el-input v-model="form.policyName" placeholder="如：同医生 7 日内复诊免门诊诊查费"/>
      </el-form-item>
      <el-form-item label="适用来源" required>
        <el-select v-model="form.revisitSource" class="w-full" placeholder="请选择">
          <el-option v-for="o in sourceOptions" :key="o.dictValue" :label="o.dictLabel"
                     :value="Number(o.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="与原就诊医生">
        <el-radio-group v-model="form.sameDoctor">
          <el-radio v-for="o in REVISIT_MATCH_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="与原就诊科室">
        <el-radio-group v-model="form.sameDept">
          <el-radio v-for="o in REVISIT_MATCH_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="间隔天数">
        <el-input-number v-model="form.withinDays" :max="3650" :min="0" class="!w-40"/>
        <span class="ml-2 text-xs text-slate-400">留空=不限；与原病历就诊日相差超过该天数则本条不命中</span>
      </el-form-item>
      <el-form-item label="收费方式" required>
        <el-select v-model="form.chargeMode" class="w-full" placeholder="请选择">
          <el-option v-for="o in chargeModeOptions" :key="o.dictValue" :label="o.dictLabel"
                     :value="Number(o.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-input-number v-model="form.priority" :max="9999" :min="1" class="!w-40"/>
        <span class="ml-2 text-xs text-slate-400">数值小者优先命中；铺底策略占用 10 的间隔便于插队</span>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio v-for="o in statusOptions" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" placeholder="免钱的依据，会写进收费单备注供医保/审计追溯"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'opd:revisitPolicy:add'" :loading="formSubmitting" type="primary" @click="submitForm">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {revisitFeePolicyUpsert} from '@/api/revisitPolicy';
import {REVISIT_MATCH_ANY, REVISIT_MATCH_OPTIONS} from '@/lib/revisitPolicy';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 空 = 新增，有值 = 编辑（后端同一个 upsert）
  row: {type: Object, default: null},
  // 字典由页面一次性加载（列表列也在用），这里只读取
  sourceOptions: {type: Array, default: () => []},
  chargeModeOptions: {type: Array, default: () => []},
  statusOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formSubmitting = ref(false);
const form = reactive({
  id: null,
  policyName: '',
  revisitSource: null,
  sameDoctor: REVISIT_MATCH_ANY,
  sameDept: REVISIT_MATCH_ANY,
  withinDays: null,
  chargeMode: null,
  priority: 100,
  status: 1,
  remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  if (!row) {
    form.id = null;
    form.policyName = '';
    form.revisitSource = null;
    form.sameDoctor = REVISIT_MATCH_ANY;
    form.sameDept = REVISIT_MATCH_ANY;
    form.withinDays = null;
    form.chargeMode = null;
    form.priority = 100;
    form.status = 1;
    form.remark = '';
    return;
  }
  form.id = row.id;
  form.policyName = row.policyName;
  form.revisitSource = row.revisitSource;
  form.sameDoctor = Number(row.sameDoctor ?? 0);
  form.sameDept = Number(row.sameDept ?? 0);
  form.withinDays = row.withinDays === null || row.withinDays === undefined ? null : Number(row.withinDays);
  form.chargeMode = row.chargeMode;
  form.priority = Number(row.priority ?? 100);
  form.status = row.status;
  form.remark = row.remark || '';
});

async function submitForm() {
  if (!form.policyName.trim()) {
    ElMessage.warning('请填写策略名称');
    return;
  }
  if (form.revisitSource === null) {
    ElMessage.warning('请选择适用的复诊来源');
    return;
  }
  if (form.chargeMode === null) {
    ElMessage.warning('请选择收费方式');
    return;
  }
  formSubmitting.value = true;
  try {
    await revisitFeePolicyUpsert({
      id: form.id,
      policyName: form.policyName.trim(),
      revisitSource: form.revisitSource,
      sameDoctor: form.sameDoctor,
      sameDept: form.sameDept,
      // 留空 = 不限间隔；不能兜 0 —— 0 是「只允许同一天」，两者差得很远
      withinDays: form.withinDays,
      chargeMode: form.chargeMode,
      priority: form.priority,
      status: form.status,
      remark: form.remark,
    });
    ElMessage.success(form.id ? '修改成功' : '新增成功');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    formSubmitting.value = false;
  }
}
</script>
