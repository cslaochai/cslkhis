<template>
  <!-- 执行弹窗 -->
  <el-dialog v-model="visible" :title="`执行检查：${execRow?.recordNo || ''}`" width="520px">
    <el-form label-width="130px">
      <el-form-item label="内镜医师">
        <el-input v-model="execForm.endoscopist" placeholder="留空默认当前登录人"/>
      </el-form-item>
      <el-form-item label="部位/到达范围">
        <el-input v-model="execForm.bodyPart"/>
      </el-form-item>
      <el-form-item v-if="Number(execRow?.endoType) === 2" label="Boston 评分(0~9)">
        <el-input-number v-model="execForm.bowelPrepScore" :max="9" :min="0"/>
      </el-form-item>
      <el-form-item v-if="Number(execRow?.endoType) === 1" label="幽门螺杆菌">
        <el-radio-group v-model="execForm.hpResult">
          <el-radio :value="0">未查</el-radio>
          <el-radio :value="1">阴性</el-radio>
          <el-radio :value="2">阳性</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'medtech:endoscopy:edit'" :loading="actLoading" type="primary" @click="saveExecute">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 执行检查：肠镜录 Boston 评分、胃镜录 Hp，按内镜类型只开对应那一项
 * （类型口径来自列表行 endoType，入参也按它裁剪，不能让两种检查互串字段）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {endoscopyExecute} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const execRow = ref(null);
const execForm = reactive({endoscopist: '', bodyPart: '', bowelPrepScore: null, hpResult: null});
const actLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  execRow.value = props.row;
  Object.assign(execForm, {
    endoscopist: '',
    bodyPart: props.row?.bodyPart || '',
    bowelPrepScore: null,
    hpResult: null,
  });
});

const saveExecute = async () => {
  actLoading.value = true;
  try {
    const res = await endoscopyExecute({
      recordId: execRow.value.id,
      endoscopist: execForm.endoscopist.trim() || undefined,
      bodyPart: execForm.bodyPart.trim() || undefined,
      bowelPrepScore: Number(execRow.value.endoType) === 2 ? execForm.bowelPrepScore : undefined,
      hpResult: Number(execRow.value.endoType) === 1 ? execForm.hpResult : undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  } finally {
    actLoading.value = false;
  }
};
</script>
