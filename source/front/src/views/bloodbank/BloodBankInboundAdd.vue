<template>
  <el-dialog v-model="visible" title="血袋入库登记" width="680px">
    <el-form label-width="100px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="血袋号" required>
          <el-input v-model="form.bagNo" placeholder="全局唯一"/>
        </el-form-item>
        <el-form-item label="献血码">
          <el-input v-model="form.donorNo"/>
        </el-form-item>
        <el-form-item label="血型" required>
          <el-select v-model="form.bloodType" style="width: 100%">
            <el-option v-for="d in bloodTypeDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="Rh">
          <el-radio-group v-model="form.rhType">
            <el-radio v-for="d in rhDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="血液成分" required>
          <el-select v-model="form.componentType" style="width: 100%">
            <el-option v-for="d in componentDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="血量(ml)">
          <el-input-number v-model="form.volume" :min="0" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="单位(U)">
          <el-input-number v-model="form.unitAmount" :min="0" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="存放位置">
          <el-input v-model="form.storageLoc" placeholder="储血冰箱A/2层"/>
        </el-form-item>
        <el-form-item label="采集日期">
          <el-date-picker v-model="form.collectDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="失效日期">
          <el-date-picker v-model="form.expireDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="血液来源">
          <el-select v-model="form.sourceType" style="width: 100%">
            <el-option v-for="d in sourceDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="来源单位/人">
          <el-input v-model="form.sourceName"/>
        </el-form-item>
      </div>
      <el-form-item label="血型复核">
        <el-radio-group v-model="form.aboVerify">
          <el-radio :value="0">未复核</el-radio>
          <el-radio :value="1">已复核</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'medtech:bloodBank:add'" :loading="loading" type="primary" @click="saveInbound">入库
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 血袋入库登记弹框（自 BloodBankView 拆出，只有新增语义）
 *
 * 血型 / Rh / 血液成分字典由页面加载后传入（列表列也用同一份，见拆分规范第 6 条）；
 * 血液来源只有本表单在用，随表单在弹框打开时拉一次。
 * 入库成功后 emit('saved')，由页面刷新库存列表与统计卡。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {bloodInbound} from '@/api/medicaltech';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 血型字典（页面已加载）
  bloodTypeDict: {type: Array, default: () => []},
  // Rh 字典（页面已加载）
  rhDict: {type: Array, default: () => []},
  // 血液成分字典（页面已加载）
  componentDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// 只有本表单用的参照数据（血液来源）随表单在弹框打开时拉一次
const sourceDict = ref([]);
let sourceDictLoaded = false;
const loadSourceDict = async () => {
  if (sourceDictLoaded) return;
  sourceDictLoaded = true;
  const map = await loadDictDataMap(DICT_TYPE.BLOOD_SOURCE_TYPE);
  sourceDict.value = map[DICT_TYPE.BLOOD_SOURCE_TYPE] || [];
};

const loading = ref(false);
const form = reactive({
  bagNo: '', bloodType: null, rhType: 1, componentType: null,
  volume: null, unitAmount: null, collectDate: '', expireDate: '',
  sourceType: 1, sourceName: '', donorNo: '', aboVerify: 0, storageLoc: '',
});
const resetForm = () => {
  Object.assign(form, {
    bagNo: '', bloodType: null, rhType: 1, componentType: null, volume: null, unitAmount: null,
    collectDate: '', expireDate: '', sourceType: 1, sourceName: '', donorNo: '', aboVerify: 0, storageLoc: '',
  });
};

watch(visible, (open) => {
  if (!open) return;
  resetForm();
  loadSourceDict();
});

const saveInbound = async () => {
  if (!form.bagNo.trim()) {
    ElMessage.warning('血袋号必填');
    return;
  }
  if (form.bloodType == null || form.componentType == null) {
    ElMessage.warning('血型与血液成分必填');
    return;
  }
  loading.value = true;
  try {
    const res = await bloodInbound({
      ...form, bagNo: form.bagNo.trim(),
      volume: form.volume ?? undefined, unitAmount: form.unitAmount ?? undefined,
      collectDate: form.collectDate || undefined, expireDate: form.expireDate || undefined,
      sourceName: form.sourceName || undefined, donorNo: form.donorNo || undefined,
      storageLoc: form.storageLoc || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('血袋已入库');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false;
  }
};
</script>
