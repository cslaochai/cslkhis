<template>
  <el-dialog v-model="visible" title="安排会诊" width="600px">
    <el-form label-width="120px" size="small">
      <el-form-item label="计划会诊时间" required>
        <el-date-picker v-model="arrangeForm.planTime" placeholder="请选择" style="width: 100%"
                        type="datetime" value-format="YYYY-MM-DD HH:mm"/>
      </el-form-item>
      <el-form-item label="计划时长(分钟)">
        <el-input-number v-model="arrangeForm.durationMin" :max="480" :min="5"/>
      </el-form-item>
      <el-form-item label="对接平台">
        <el-input v-model="arrangeForm.platform" placeholder="预留字段：真实视频平台对接后回写"/>
      </el-form-item>
      <el-form-item label="接入号/会议室">
        <el-input v-model="arrangeForm.meetNo" placeholder="预留字段：平台开号后回写"/>
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="专家医院">
            <el-input v-model="arrangeForm.expertHospital"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="专家科室">
            <el-input v-model="arrangeForm.expertDept"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="专家姓名">
            <el-input v-model="arrangeForm.expertName"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="专家职称">
            <el-input v-model="arrangeForm.expertTitle"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button :icon="Connection" :loading="arrangeLoading" type="primary" @click="submitArrange">安排</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 安排会诊（待安排 → 已安排，定时间与接入方式）
 *
 * 平台 platform / 接入号 meet_no 是预留字段：真实视频平台对接时只换成「平台开号回写」，
 * 不改表结构、不改页面。所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Connection} from '@element-plus/icons-vue';
import {arrangeTeleConsult} from '@/api/teleconsult';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const arrangeForm = reactive({
  planTime: '', durationMin: null, platform: '', meetNo: '',
  expertHospital: '', expertDept: '', expertName: '', expertTitle: '',
});
const arrangeLoading = ref(false);

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  arrangeForm.planTime = '';
  arrangeForm.durationMin = 30;
  arrangeForm.platform = row.platform || '';
  arrangeForm.meetNo = row.meetNo || '';
  arrangeForm.expertHospital = row.expertHospital || '';
  arrangeForm.expertDept = row.expertDept || '';
  arrangeForm.expertName = row.expertName || '';
  arrangeForm.expertTitle = row.expertTitle || '';
});

const submitArrange = async () => {
  if (!arrangeForm.planTime) {
    ElMessage.warning('请选择计划会诊时间');
    return;
  }
  arrangeLoading.value = true;
  try {
    const res = await arrangeTeleConsult({
      id: props.row.id,
      planTime: arrangeForm.planTime.length === 16 ? `${arrangeForm.planTime}:00` : arrangeForm.planTime,
      durationMin: arrangeForm.durationMin ?? undefined,
      platform: arrangeForm.platform.trim() || undefined,
      meetNo: arrangeForm.meetNo.trim() || undefined,
      expertHospital: arrangeForm.expertHospital.trim() || undefined,
      expertDept: arrangeForm.expertDept.trim() || undefined,
      expertName: arrangeForm.expertName.trim() || undefined,
      expertTitle: arrangeForm.expertTitle.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('已安排');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    arrangeLoading.value = false;
  }
};
</script>
