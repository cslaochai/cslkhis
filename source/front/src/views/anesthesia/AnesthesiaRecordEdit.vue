<template>
  <el-dialog v-model="visible" data-testid="g15-update-dialog" title="更新麻醉记录（时间轴 / 出入量）"
             width="780px">
    <div v-if="updateTarget" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <span class="font-medium text-slate-900">{{ text(updateTarget.recordNo) }}</span>
        <span class="ml-3 text-slate-600">{{
            text(updateTarget.patientName)
          }} · {{ text(updateTarget.plannedOperationName) }}</span>
      </div>
      <el-form label-width="110px">
        <el-form-item label="麻醉方式">
          <el-select v-model="updateForm.anesthesiaType" class="!w-full" clearable>
            <el-option :value="1" label="全身麻醉"/>
            <el-option :value="2" label="椎管内麻醉"/>
            <el-option :value="3" label="神经阻滞麻醉"/>
            <el-option :value="4" label="局部麻醉"/>
            <el-option :value="5" label="其他"/>
          </el-select>
        </el-form-item>
        <el-form-item label="麻醉方法描述">
          <el-input v-model="updateForm.anesthesiaMethodDetail" placeholder="如：静吸复合全麻 + 气管插管"/>
        </el-form-item>
        <el-form-item label="气道管理">
          <el-select v-model="updateForm.airwayDevice" class="!w-52" clearable placeholder="选择">
            <el-option v-for="o in A.airwayDeviceOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
          <el-input v-model="updateForm.airwayDeviceSpec" class="!ml-2 !w-56" placeholder="规格（如 7.5# 加强型）"/>
        </el-form-item>
        <el-form-item label="通气方式">
          <el-select v-model="updateForm.ventilationMode" class="!w-52" clearable placeholder="选择">
            <el-option :value="1" label="自主呼吸"/>
            <el-option :value="2" label="辅助通气"/>
            <el-option :value="3" label="控制通气"/>
          </el-select>
        </el-form-item>
        <!-- ⚠ el-date-picker 上的 data-testid **不会落到 DOM**（与 el-input 落 <input>、el-select 落根不同），
             验证脚本定位不到 → 把锚点打在外层 el-form-item 上，用 [data-testid] input 取内部输入框 -->
        <el-form-item data-testid="g15-update-anestart" label="麻醉开始" required>
          <el-date-picker v-model="updateForm.anesthesiaStartTime" class="!w-full" placeholder="诱导开始"
                          type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item data-testid="g15-update-aneend" label="麻醉结束" required>
          <el-date-picker v-model="updateForm.anesthesiaEndTime" class="!w-full" data-testid="g15-update-aneend"
                          placeholder="停药" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="切皮 / 关腹">
          <el-date-picker v-model="updateForm.operationStartTime" class="!w-56" placeholder="切皮"
                          type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          <el-date-picker v-model="updateForm.operationEndTime" class="!ml-2 !w-56" placeholder="关腹/关胸"
                          type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="出入量（ml）">
          <el-input v-model.number="updateForm.crystalloid" class="!w-32" placeholder="晶体液"/>
          <el-input v-model.number="updateForm.colloid" class="!ml-2 !w-32" placeholder="胶体液"/>
          <el-input v-model.number="updateForm.bloodTransfusion" class="!ml-2 !w-32" placeholder="输血"/>
          <el-input v-model.number="updateForm.urineOutput" class="!ml-2 !w-32" placeholder="尿量"/>
          <el-input v-model.number="updateForm.bloodLoss" class="!ml-2 !w-32" placeholder="出血"/>
        </el-form-item>
        <el-form-item label="不良事件">
          <el-radio-group v-model="updateForm.adverseEventFlag">
            <el-radio :value="0">无</el-radio>
            <el-radio :value="1">有</el-radio>
          </el-radio-group>
          <el-input v-if="updateForm.adverseEventFlag === 1" v-model="updateForm.adverseEventNote"
                    class="!ml-3 !w-96" placeholder="经过与处理（必填）"/>
        </el-form-item>
        <el-form-item label="麻醉效果">
          <el-select v-model="updateForm.anesthesiaEffect" class="!w-full" clearable>
            <el-option v-for="o in A.effectOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="术后去向">
          <el-select v-model="updateForm.postopDisposition" class="!w-full" clearable>
            <el-option v-for="o in A.dispositionOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button v-perm="'ipd:anesthesia:edit'" :loading="updateSubmitting" data-testid="g15-update-submit"
                 type="primary" @click="submitUpdate">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getAnesthesiaRecordDetail, updateAnesthesiaRecord} from '@/api/inpatientAnesthesia';
import * as A from '@/lib/anesthesia';
import {text} from '@/lib/anesthesia';

// ---- 更新（时间轴 / 出入量） ----
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const updateSubmitting = ref(false);
const updateTarget = ref(null);
const updateForm = reactive({
  anesthesiaType: undefined,
  anesthesiaMethodDetail: '',
  airwayDevice: undefined,
  airwayDeviceSpec: '',
  ventilationMode: undefined,
  enterRoomTime: '',
  anesthesiaStartTime: '',
  operationStartTime: '',
  operationEndTime: '',
  anesthesiaEndTime: '',
  leaveRoomTime: '',
  crystalloid: undefined,
  colloid: undefined,
  bloodTransfusion: undefined,
  urineOutput: undefined,
  bloodLoss: undefined,
  adverseEventFlag: 0,
  adverseEventNote: '',
  anesthesiaEffect: undefined,
  postopDisposition: undefined,
});

watch(visible, async (open) => {
  if (!open) return;
  let d = props.row;
  try {
    const res = await getAnesthesiaRecordDetail(props.row.id);
    d = (res.data || props.row);
  } catch (e) {
    /* 详情拉取失败就用列表行 */
  }
  updateTarget.value = d;
  updateForm.anesthesiaType = d.anesthesiaType;
  updateForm.anesthesiaStartTime = d.anesthesiaStartTime || '';
  updateForm.anesthesiaEndTime = d.anesthesiaEndTime || '';
  updateForm.enterRoomTime = '';
  updateForm.operationStartTime = '';
  updateForm.operationEndTime = '';
  updateForm.leaveRoomTime = '';
  updateForm.crystalloid = undefined;
  updateForm.colloid = undefined;
  updateForm.bloodTransfusion = undefined;
  updateForm.urineOutput = undefined;
  updateForm.bloodLoss = undefined;
  updateForm.adverseEventFlag = 0;
  updateForm.adverseEventNote = '';
  updateForm.anesthesiaEffect = undefined;
  updateForm.postopDisposition = undefined;
  updateForm.anesthesiaMethodDetail = '';
  updateForm.airwayDevice = undefined;
  updateForm.airwayDeviceSpec = '';
  updateForm.ventilationMode = undefined;
});

const submitUpdate = async () => {
  if (!updateTarget.value)
    return;
  if (updateForm.adverseEventFlag === 1 && !updateForm.adverseEventNote.trim()) {
    ElMessage.warning('已标记不良事件，必须填写经过与处理');
    return;
  }
  updateSubmitting.value = true;
  try {
    await updateAnesthesiaRecord({
      recordId: updateTarget.value.id,
      anesthesiaType: updateForm.anesthesiaType,
      anesthesiaMethodDetail: updateForm.anesthesiaMethodDetail.trim() || undefined,
      airwayDevice: updateForm.airwayDevice,
      airwayDeviceSpec: updateForm.airwayDeviceSpec.trim() || undefined,
      ventilationMode: updateForm.ventilationMode,
      enterRoomTime: updateForm.enterRoomTime || undefined,
      anesthesiaStartTime: updateForm.anesthesiaStartTime || undefined,
      anesthesiaEndTime: updateForm.anesthesiaEndTime || undefined,
      operationStartTime: updateForm.operationStartTime || undefined,
      operationEndTime: updateForm.operationEndTime || undefined,
      leaveRoomTime: updateForm.leaveRoomTime || undefined,
      crystalloid: updateForm.crystalloid,
      colloid: updateForm.colloid,
      bloodTransfusion: updateForm.bloodTransfusion,
      urineOutput: updateForm.urineOutput,
      bloodLoss: updateForm.bloodLoss,
      adverseEventFlag: updateForm.adverseEventFlag,
      adverseEventNote: updateForm.adverseEventNote.trim() || undefined,
      anesthesiaEffect: updateForm.anesthesiaEffect,
      postopDisposition: updateForm.postopDisposition,
    });
    ElMessage.success('麻醉记录已更新');
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    updateSubmitting.value = false;
  }
};
</script>
