<template>
  <!-- ============ 档案弹框 ============ -->
  <el-dialog v-model="visible" :title="archiveForm.id ? '编辑透析档案' : '新建透析档案'" data-testid="hd-archive-dialog"
             width="620px">
    <el-form label-width="100px">
      <el-form-item label="患者" required>
        <PatientSelect v-if="!archiveForm.id" v-model="archiveForm.patientId" data-testid="hd-archive-patient"
                       @select="onPatientPicked"/>
        <span v-else>{{ archiveForm.patientName }}（{{ archiveForm.dialysisNo }}）</span>
      </el-form-item>
      <el-form-item label="首透日期" required>
        <el-date-picker v-model="archiveForm.firstDialysisDate" :clearable="false" class="w-40"
                        data-testid="hd-archive-first-date" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="血管通路" required>
        <el-select v-model="archiveForm.accessType" :fit-input-width="false" class="w-44" data-testid="hd-archive-access"
                   placeholder="选择通路">
          <el-option v-for="d in dicts.access" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="通路部位">
        <el-input v-model="archiveForm.accessSite" data-testid="hd-archive-site"
                  placeholder="如 左前臂桡动脉-头静脉"/>
      </el-form-item>
      <el-form-item label="透析频次">
        <el-select v-model="archiveForm.dialysisFreq" :fit-input-width="false" class="w-40"
                   data-testid="hd-archive-freq">
          <el-option v-for="d in dicts.freq" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="原发病">
        <el-input v-model="archiveForm.cause" :rows="2" data-testid="hd-archive-cause" placeholder="如 慢性肾小球肾炎致慢性肾脏病5期"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="archiveForm.remark" data-testid="hd-archive-remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="archiveSaving" data-testid="hd-archive-ok" type="primary" @click="saveArchive">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 透析档案弹框：row 为空走新建、有值走修改（后端同一个 upsert）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDialysisArchive, upsertDialysisArchive} from '@/api/dialysis';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {localDateStr} from '@/lib/utils';

const props = defineProps({
  modelValue: Boolean,
  // 当前行：新建时为 null；修改时先用行数据兜「打开瞬间不闪白」，明细再按 id 回查
  row: {type: Object, default: null},
  // 页面级参照：通路/频次字典由主页面加载一次后传入
  dicts: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const archiveSaving = ref(false);
const archiveForm = reactive({
  id: null, patientId: null, patientName: '', dialysisNo: '',
  firstDialysisDate: localDateStr(), cause: '', accessType: null, accessSite: '',
  dialysisFreq: 3, remark: '',
});
const onPatientPicked = (p) => {
  archiveForm.patientId = p?.id ?? null;
  archiveForm.patientName = p?.patientName || p?.name || '';
};
const saveArchive = async () => {
  if (!archiveForm.patientId || !archiveForm.firstDialysisDate || !archiveForm.accessType) {
    ElMessage.warning('请选择患者、首次透析日期与血管通路');
    return;
  }
  archiveSaving.value = true;
  try {
    const res = await upsertDialysisArchive({
      id: archiveForm.id ?? undefined,
      patientId: archiveForm.patientId,
      firstDialysisDate: archiveForm.firstDialysisDate,
      cause: archiveForm.cause || undefined,
      accessType: archiveForm.accessType,
      accessSite: archiveForm.accessSite || undefined,
      dialysisFreq: archiveForm.dialysisFreq ?? undefined,
      remark: archiveForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '保存成功');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    archiveSaving.value = false;
  }
};

watch(visible, async (v) => {
  if (!v) return;
  const row = props.row;
  archiveForm.id = row?.id ?? null;
  archiveForm.patientId = row?.patientId ?? null;
  archiveForm.patientName = row?.patientName ?? '';
  archiveForm.dialysisNo = row?.dialysisNo ?? '';
  archiveForm.remark = row?.remark ?? '';
  if (row?.id) {
    // 编辑回显走 getById（明文电话），列表是脱敏出参
    try {
      const res = await getDialysisArchive(row.id);
      if (res.code === 200 && res.data) {
        Object.assign(archiveForm, {
          firstDialysisDate: res.data.firstDialysisDate,
          cause: res.data.cause || '',
          accessType: res.data.accessType,
          accessSite: res.data.accessSite || '',
          dialysisFreq: res.data.dialysisFreq,
          remark: res.data.remark || '',
        });
      }
    } catch (e) {
      console.error(e);
    }
  } else {
    Object.assign(archiveForm, {
      firstDialysisDate: localDateStr(), cause: '', accessType: null, accessSite: '', dialysisFreq: 3, remark: '',
    });
  }
});
</script>
