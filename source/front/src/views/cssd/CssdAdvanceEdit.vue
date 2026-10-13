<template>
  <el-dialog v-model="visible" :title="`流转 — ${form.packNo}`" width="500px">
    <div class="mb-3 text-sm">
      下一节点：
      <el-tag type="primary">{{ form.nextNodeName }}</el-tag>
    </div>
    <el-form label-width="100px">
      <el-form-item label="操作人">
        <el-input v-model="form.operatorName" placeholder="不填默认当前登录人" style="width: 220px"/>
      </el-form-item>
      <template v-if="form.nextNodeName.startsWith('灭菌')">
        <el-form-item label="灭菌锅次" required>
          <el-input v-model="form.sterilizerNo" placeholder="如 1号锅 B03" style="width: 220px"/>
        </el-form-item>
        <el-form-item label="批次号" required>
          <el-input v-model="form.batchNo" style="width: 220px"/>
        </el-form-item>
      </template>
      <template v-if="form.nextNodeName.startsWith('储存')">
        <el-form-item label="节点结果" required>
          <el-radio-group v-model="form.result">
            <el-radio :value="1">合格（入储存待发放）</el-radio>
            <el-radio :value="2">不合格（自动退回清洗）</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:cssd:edit'" type="primary" @click="submitAdvance">确认流转</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {cssdAdvance} from '@/api/cssd';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 目标器械包行（列表「流转」按钮传入，状态决定下一节点）
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = reactive({
  packId: null, packNo: '', nextNodeName: '',
  operatorName: '', sterilizerNo: '', batchNo: '', result: 1, remark: '',
});
// 状态 → 下一节点名
const NEXT_NODE = {1: '清洗', 2: '打包', 3: '灭菌', 4: '储存（灭菌完成入储存）', 5: '发放'};

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  Object.assign(form, {
    packId: Number(row.id), packNo: row.packNo, nextNodeName: NEXT_NODE[row.status] || '—',
    operatorName: '', sterilizerNo: '', batchNo: '', result: 1, remark: '',
  });
});

const submitAdvance = async () => {
  if (form.nextNodeName.startsWith('灭菌') && (!form.sterilizerNo.trim() || !form.batchNo.trim())) {
    ElMessage.warning('推进到灭菌节点必须填灭菌锅次与批次号');
    return;
  }
  try {
    const res = await cssdAdvance({
      packId: form.packId,
      operatorName: form.operatorName || undefined,
      sterilizerNo: form.sterilizerNo.trim() || undefined,
      batchNo: form.batchNo.trim() || undefined,
      result: form.result,
      remark: form.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('流转成功');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  }
};
</script>
