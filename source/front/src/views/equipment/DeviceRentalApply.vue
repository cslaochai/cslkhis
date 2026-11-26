<template>
  <el-dialog v-model="visible" title="发起设备借用申请" width="820px" append-to-body>
    <el-form label-width="92px" :model="form">
      <el-form-item label="患者/床位">
        <el-select v-model="form.patientKey" filterable clearable :fit-input-width="false" style="width: 100%"
                   placeholder="科室公用设备可不选患者">
          <el-option label="科室公用（不指定患者）" value="NONE"/>
          <el-option v-for="b in beds" :key="b.admissionId"
                     :label="`${b.bedNo} 床 · ${b.patientName} · ${b.wardName || ''}`"
                     :value="String(b.admissionId)"/>
        </el-select>
        <div class="mt-1 text-[12px] text-gray-400">
          患者级借用必须是在院患者；科室公用设备只登记到申请科室。
        </div>
      </el-form-item>

      <el-form-item label="借用设备" required>
        <div class="w-full space-y-2">
          <div v-for="(line, idx) in form.items" :key="idx"
               class="grid grid-cols-[1fr_120px_120px_34px] gap-2 items-start">
            <el-select v-model="line.equipmentId" filterable :fit-input-width="false"
                       placeholder="选择共享池设备" @change="onEquipmentChange(line)">
              <el-option v-for="e in catalog" :key="e.id" :value="String(e.id)"
                         :disabled="e.availableCount <= 0"
                         :label="`${e.equipmentName} · ${e.deptName || '设备科'} · 可借 ${e.availableCount}/${e.totalCount}`"/>
            </el-select>
            <el-input v-model="line.requestedBrand" placeholder="期望品牌（可空）" maxlength="50"/>
            <el-input v-model="line.requestedModel" placeholder="期望型号（可空）" maxlength="50"/>
            <el-button link type="danger" :disabled="form.items.length === 1" @click="removeLine(idx)">删除</el-button>
          </div>
          <button type="button" class="text-[#1269B5] text-[13px] disabled:text-gray-300"
                  :disabled="form.items.length >= 5" @click="addLine">＋ 再加一台</button>
          <div class="text-[12px] text-gray-400">
            共享池按设备台账维护；有物联网编号的设备按设备编号发放，普通设备按资产编号发放。
          </div>
        </div>
      </el-form-item>

      <el-form-item label="借用原因" required>
        <el-input v-model="form.purpose" maxlength="200" show-word-limit
                  placeholder="如：术后持续泵入、转运监护、床旁心电检查"/>
      </el-form-item>

      <el-form-item label="紧急程度">
        <el-radio-group v-model="form.urgency">
          <el-radio :value="1">普通</el-radio>
          <el-radio :value="2">紧急</el-radio>
          <el-radio :value="3">急救</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="预计归还">
        <el-date-picker v-model="form.expectReturnTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
                        placeholder="可空" style="width: 220px"/>
      </el-form-item>

      <el-form-item label="送达地点">
        <el-input v-model="form.deliveryPlace" maxlength="100" placeholder="如：呼吸内科 4 病区护士站"/>
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit/>
      </el-form-item>
    </el-form>

    <el-alert v-if="stockEmpty" type="warning" show-icon :closable="false"
              title="共享池暂无可借设备，请联系设备科补充或调整共享目录"/>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交申请</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 设备共享调配申请弹框（临床科室发起）。
 *
 * 一台设备一行；不指定具体设备号，设备科发放时按共享池库存分配具体资产/泵号。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getBedMap} from '@/api/inpatient';
import {deviceRentalCatalog, deviceRentalCreate} from '@/api/deviceRental';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  presetEquipmentId: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = ref(false);
const submitting = ref(false);
const catalog = ref([]);
const beds = ref([]);
const form = reactive({
  patientKey: 'NONE',
  purpose: '',
  urgency: 1,
  expectReturnTime: '',
  deliveryPlace: '',
  remark: '',
  items: [{equipmentId: '', requestedBrand: '', requestedModel: ''}],
});

const stockEmpty = computed(() => !catalog.value.some(e => e.availableCount > 0));
const selectedPatient = computed(() => beds.value.find(b => String(b.admissionId) === form.patientKey) || null);

const emptyLine = () => ({equipmentId: '', requestedBrand: '', requestedModel: ''});

watch(() => props.modelValue, async (v) => {
  visible.value = v;
  if (v) {
    form.patientKey = 'NONE';
    form.purpose = '';
    form.urgency = 1;
    form.expectReturnTime = '';
    form.deliveryPlace = '';
    form.remark = '';
    form.items = [emptyLine()];
    await Promise.all([loadCatalog(), loadBeds()]);
    if (props.presetEquipmentId) {
      form.items[0].equipmentId = String(props.presetEquipmentId);
    }
  }
});
watch(visible, (v) => {
  if (v !== props.modelValue) emit('update:modelValue', v);
});

const loadCatalog = async () => {
  try {
    const res = await deviceRentalCatalog('');
    catalog.value = res.data || [];
  } catch (e) {
    console.error('加载共享池失败', e);
    catalog.value = [];
    ElMessage.error('共享池加载失败');
  }
};

const loadBeds = async () => {
  try {
    const deptId = localStorage.getItem('deptId');
    const res = await getBedMap({deptId: deptId || undefined});
    beds.value = (res.data?.beds || []).filter(b => b.admissionId && b.patientName);
  } catch (e) {
    console.error('加载在院患者失败', e);
    beds.value = [];
  }
};

const addLine = () => form.items.push(emptyLine());
const removeLine = (idx) => form.items.splice(idx, 1);

const onEquipmentChange = (line) => {
  const e = catalog.value.find(x => String(x.id) === line.equipmentId);
  if (e) {
    line.requestedBrand = e.brand || '';
    line.requestedModel = e.model || '';
  }
};

const submit = async () => {
  if (!form.purpose.trim()) {
    ElMessage.warning('请填写借用原因');
    return;
  }
  const items = form.items.filter(i => i.equipmentId);
  if (!items.length) {
    ElMessage.warning('请至少选择一台设备');
    return;
  }
  const patient = selectedPatient.value;
  submitting.value = true;
  try {
    await deviceRentalCreate({
      admissionId: patient?.admissionId || null,
      patientName: patient?.patientName || null,
      bedNo: patient?.bedNo || null,
      wardId: patient?.wardId || null,
      wardName: patient?.wardName || null,
      purpose: form.purpose.trim(),
      urgency: form.urgency,
      expectReturnTime: form.expectReturnTime || null,
      deliveryPlace: form.deliveryPlace.trim(),
      remark: form.remark.trim(),
      items: items.map(i => ({
        equipmentId: i.equipmentId,
        requestedBrand: i.requestedBrand,
        requestedModel: i.requestedModel,
      })),
    });
    ElMessage.success('申请已提交，等待审批');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('提交设备借用申请失败', e);
    ElMessage.error(e?.response?.data?.message || e?.message || '提交失败');
  } finally {
    submitting.value = false;
  }
};
</script>
