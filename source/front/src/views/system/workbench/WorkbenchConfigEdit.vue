<template>
  <!-- 新增与修改共用一张表单（后端同一个 widgetUpsert，row 为空走新增） -->
  <el-dialog v-model="visible" :title="form.id ? `修改卡片 ${form.widgetCode}` : '新增卡片'" width="640px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="卡片编码" required>
        <el-input v-model="form.widgetCode" :disabled="!!form.id"
                  placeholder="与后端 provider 的 widgetCode 一字不差，如 myTodo"/>
      </el-form-item>
      <el-form-item label="卡片名称" required>
        <el-input v-model="form.widgetName" placeholder="首页卡片标题，如 我的待办"/>
      </el-form-item>
      <el-form-item label="分区" required>
        <el-select v-model="form.area" :fit-input-width="false" style="width: 100%">
          <el-option v-for="a in areaOptions" :key="a.value" :label="a.label" :value="a.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="取数来源" required>
        <el-input v-model="form.apiKey" placeholder="<模块>.<summaryKey>，如 report.hospitalToday（仅对账排查用）"/>
      </el-form-item>
      <el-form-item label="权限码">
        <el-input v-model="form.permission"
                  placeholder="留空=登录即可见；否则必须是 sys_menu 里已有的码，如 report:stats:list"/>
      </el-form-item>
      <el-form-item label="默认宽度">
        <el-input-number v-model="form.defaultSpan" :max="24" :min="1" :step="6"/>
        <span class="ml-2 text-[14px] text-slate-400">24 列栅格，6=四分之一宽、24=整行</span>
      </el-form-item>
      <el-form-item label="顺序">
        <el-input-number v-model="form.sortOrder" :max="9999" :min="0" :step="10"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">已上线</el-radio>
          <el-radio :value="0">未上线</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" placeholder="这张卡的数据口径、归属模块、后续要拆到哪"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitting" type="primary" @click="submitWidget">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {widgetUpsert} from '@/api/workbench';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 修改传入当前行；为空表示新增
  row: {type: Object, default: null},
  // 卡片分区选项：查询条件与表格列也要用，由页面加载一次下传
  areaOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const emptyForm = () => ({
  id: null,
  widgetCode: '',
  widgetName: '',
  area: 'domain',
  apiKey: '',
  permission: '',
  defaultSpan: 6,
  sortOrder: 100,
  status: 0,
  remark: '',
});
const form = ref(emptyForm());

/** 编辑回显：VO 是 span、DTO 是 defaultSpan，这里一次性对齐 */
watch(visible, (open) => {
  if (!open) return;
  if (!props.row) {
    form.value = emptyForm();
    return;
  }
  form.value = {
    id: props.row.id,
    widgetCode: props.row.widgetCode,
    widgetName: props.row.widgetName,
    area: props.row.area,
    apiKey: props.row.apiKey,
    permission: props.row.permission || '',
    defaultSpan: Number(props.row.span ?? 6),
    sortOrder: Number(props.row.sortOrder ?? 100),
    status: Number(props.row.status ?? 0),
    remark: props.row.remark || '',
  };
});

async function submitWidget() {
  submitting.value = true;
  try {
    await widgetUpsert(form.value);
    ElMessage.success('保存成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
}
</script>
