<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑机位' : '新增机位'" data-testid="hd-machine-dialog"
             width="520px">
    <el-form label-width="90px">
      <el-form-item label="机位号" required>
        <el-input v-model="form.machineNo" data-testid="hd-mc-form-no" placeholder="如 HD-09"/>
      </el-form-item>
      <el-form-item label="分区">
        <el-input v-model="form.roomName" data-testid="hd-mc-form-room" placeholder="如 第二透析室"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" :fit-input-width="false" class="w-40" data-testid="hd-mc-form-status">
          <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" data-testid="hd-mc-form-remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" data-testid="hd-mc-ok" type="primary" @click="save">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 透析机位弹框：row 为空走新增、有值走修改（后端同一个 upsert，列表行已带全部字段）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertDialysisMachine} from '@/api/dialysis';

const props = defineProps({
  modelValue: Boolean,
  // 当前行：新增时为 null；修改时用它回显
  row: {type: Object, default: null},
  // 页面级参照：状态字典由主页面加载一次后传入（查询条与列表列也用它）
  statusDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// ---------------- 新增/编辑 ----------------
const saving = ref(false);
const form = reactive({id: null, machineNo: '', roomName: '', status: 1, remark: ''});
watch(visible, (v) => {
  if (!v) return;
  const row = props.row;
  Object.assign(form, {
    id: row?.id ?? null, machineNo: row?.machineNo ?? '', roomName: row?.roomName ?? '',
    status: row?.status ?? 1, remark: row?.remark ?? '',
  });
});
const save = async () => {
  if (!form.machineNo.trim()) {
    ElMessage.warning('机位号不能为空');
    return;
  }
  saving.value = true;
  try {
    const res = await upsertDialysisMachine({
      id: form.id ?? undefined,
      machineNo: form.machineNo.trim(),
      roomName: form.roomName || undefined,
      status: form.status,
      remark: form.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '保存成功');
      visible.value = false;
      emit('saved');
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saving.value = false;
  }
};
</script>
