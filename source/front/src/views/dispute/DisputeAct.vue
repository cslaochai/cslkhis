<template>
  <!-- 受理 / 处理跟踪 / 撤销（共用一张动作表单，actKey 决定标题与必填项） -->
  <el-dialog v-model="visible" :title="ACT_META[actKey].title" width="560px">
    <el-form label-width="100px" size="small">
      <el-form-item v-if="ACT_META[actKey].needAction" label="处理动作" required>
        <el-input v-model="actAction" placeholder="如：科室调查 / 医患协商 / 医调委调解"/>
      </el-form-item>
      <el-form-item v-if="actKey === 'follow'" label="推进状态">
        <el-select v-model="actToStatus" clearable placeholder="不改变状态" style="width: 100%">
          <el-option :value="2" label="调查中"/>
          <el-option :value="3" label="处理中"/>
        </el-select>
      </el-form-item>
      <el-form-item :label="ACT_META[actKey].needContent ? '说明' : '说明（可选）'"
                    :required="ACT_META[actKey].needContent">
        <el-input v-model="actContent" :placeholder="ACT_META[actKey].placeholder" :rows="4" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="actLoading" type="primary" @click="submitAct">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 动作弹窗（受理 / 处理跟踪 / 撤销 共用）
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {acceptDispute, followDispute, revokeDispute} from '@/api/dispute';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  actKey: {type: String, default: 'accept'},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const actAction = ref('');
const actContent = ref('');
const actToStatus = ref(null);
const actLoading = ref(false);

const ACT_META = {
  accept: {title: '受理', needAction: false, needContent: false, placeholder: '受理说明（可选）'},
  follow: {title: '登记处理跟踪', needAction: true, needContent: true, placeholder: '调查/协商/回复投诉人的具体情况'},
  revoke: {title: '撤销', needAction: false, needContent: true, placeholder: '撤销原因（必填，将留痕）'},
};

watch(visible, (open) => {
  if (!open) return;
  actAction.value = '';
  actContent.value = '';
  actToStatus.value = null;
});

const submitAct = async () => {
  if (!props.row) return;
  const meta = ACT_META[props.actKey];
  if (meta.needAction && !actAction.value.trim()) {
    ElMessage.warning('请填写处理动作');
    return;
  }
  if (meta.needContent && !actContent.value.trim()) {
    ElMessage.warning('请填写说明内容');
    return;
  }
  actLoading.value = true;
  try {
    let res;
    if (props.actKey === 'accept') {
      res = await acceptDispute({id: props.row.id, content: actContent.value.trim() || undefined});
    } else if (props.actKey === 'follow') {
      res = await followDispute({
        id: props.row.id,
        action: actAction.value.trim(),
        content: actContent.value.trim(),
        toStatus: actToStatus.value ?? undefined,
      });
    } else {
      res = await revokeDispute({id: props.row.id, content: actContent.value.trim()});
    }
    if (res.code === 200) {
      const vo = res.data;
      let tip = `${meta.title}成功`;
      if (props.actKey === 'accept' && vo?.sealStatus === 2) {
        tip += '（该患者暂无已归档病历，已标记待归档后封存，归档后请点「补封存」）';
      }
      if (props.actKey === 'accept' && vo?.sealStatus === 1) {
        tip += '（已联动封存病历）';
      }
      ElMessage.success(tip);
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || `${meta.title}失败`);
    }
  } catch (e) {
    console.error(e);
    ElMessage.error(`${meta.title}失败`);
  } finally {
    actLoading.value = false;
  }
};
</script>
