<template>
  <!-- 调度派车（原 RobotDispatchView 内联弹框）：选车 + 下发，主视图只控显隐 -->
  <el-dialog v-model="visible" title="调度派车" width="480px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="单号">{{ row?.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="物品">
        {{ row?.goodsName }}（{{ row?.boxCount }} 箱 {{ row?.loadKg }}kg）
      </el-descriptions-item>
      <el-descriptions-item label="路线">{{ row?.fromStation }} → {{ floorText(row?.toFloor) }} {{ row?.toStation }}</el-descriptions-item>
    </el-descriptions>
    <el-form class="mt-4" label-width="90px">
      <el-form-item label="机器人">
        <el-select v-model="robotNo" clearable placeholder="自动选择空闲车辆" style="width: 100%">
          <el-option label="自动选车（空闲 + 电量最高）" value=""/>
          <el-option v-for="r in freeFleet" :key="r.robotNo"
                     :label="`${r.deviceName || r.robotNo}（电量 ${r.battery ?? '—'}%）`" :value="r.robotNo"/>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="confirm">确认派车</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {robotDispatchAssign} from '@/api/robotDispatch';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  freeFleet: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({get: () => props.modelValue, set: (v) => emit('update:modelValue', v)});

const submitting = ref(false);
/** 楼层文本：-1 → B1，12 → 12F */
const floorText = (n) => (n == null ? '—' : n < 0 ? `B${Math.abs(n)}` : `${n}F`);
// 空串 = 后端派单算法自动选车（不能绑 null，el-option 的 value 禁 null）
const robotNo = ref('');

const confirm = async () => {
  submitting.value = true;
  try {
    await robotDispatchAssign({id: props.row.id, robotNo: robotNo.value || null});
    ElMessage.success('已派车，指令下发至机器人');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('派车失败', e);
    ElMessage.error(e?.response?.data?.message || '派车失败');
  } finally {
    submitting.value = false;
  }
};
</script>
