<template>
  <!-- 座位维护：row 为空走新增，有值走修改（后端同一个 upsert） -->
  <el-dialog v-model="visible" title="座位维护" width="420px">
    <el-form label-width="80px">
      <el-form-item label="座位号">
        <el-input v-model="form.seatNo" data-testid="ir-seat-no"/>
      </el-form-item>
      <el-form-item label="区域">
        <el-input v-model="form.area" data-testid="ir-seat-area"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.seatStatus" data-testid="ir-seat-status">
          <el-option :value="1" label="空闲"/>
          <el-option :value="3" label="停用"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="ir-seat-cancel" @click="visible = false">取消</el-button>
      <el-button v-perm="'medtech:infusion:add'" data-testid="ir-seat-save" type="primary" @click="saveSeat">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {upsertInfusionSeat} from '@/api/infusionRoom';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 传入座位走修改；为空走新增
  seat: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = ref({});

watch(visible, (open) => {
  if (!open) return;
  const seat = props.seat;
  form.value = seat
      ? {id: seat.id, seatNo: seat.seatNo, area: seat.area, seatStatus: seat.seatStatus, remark: seat.remark}
      : {id: null, seatNo: '', area: '普通区', seatStatus: 1, remark: ''};
});

const saveSeat = async () => {
  const res = await upsertInfusionSeat(form.value);
  if (res?.code === 200) {
    ElMessage.success('座位已保存');
    visible.value = false;
    emit('saved');
  }
};
</script>
