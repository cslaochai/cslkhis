<template>
  <!-- 申请弹窗（仅新增：借阅/复印申请单，无修改语义） -->
  <el-dialog v-model="visible" data-testid="ab-apply-dialog" title="借阅/复印申请" width="560px">
    <el-form label-width="100px">
      <el-form-item label="选择病案" required>
        <el-select
            v-model="form.archiveId"
            :fit-input-width="false"
            :loading="archiveSearching"
            :remote-method="searchArchives"
            data-testid="ab-form-archive"
            filterable
            placeholder="输入病历号/患者姓名搜索（已归档/已封存）"
            remote
            style="width: 100%"
            @change="onArchiveChange"
        >
          <el-option
              v-for="a in archiveOptions"
              :key="a.id"
              :label="`${a.recordNo} / ${a.patientName}（${a.deptName || '—'}）`"
              :value="String(a.id)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" required>
        <el-radio-group v-model="form.borrowType" data-testid="ab-form-type">
          <el-radio :value="1">借阅（原件，需应还日期）</el-radio>
          <el-radio :value="2">复印</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.borrowType === 1" label="应归还日期" required>
        <el-date-picker
            v-model="form.expectReturnDate"
            :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
            :fit-input-width="false"
            placeholder="选择应归还日期"
            style="width: 100%"
            type="date"
            value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="用途" required>
        <el-input v-model="form.purpose" :rows="3" data-testid="ab-form-purpose" maxlength="500"
                  placeholder="病历讨论 / 医保核查 / 司法取证 / 科研等" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'emr:archiveBorrow:add'" :loading="saving" data-testid="ab-apply-save" type="primary"
                 @click="apply">提交申请
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {applyBorrow} from '@/api/archiveBorrow';
import {getArchiveList} from '@/api/archive';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  archiveId: null,
  archiveLabel: '',
  archiveStatus: null,
  borrowType: 1,
  purpose: '',
  expectReturnDate: '',
});
const archiveOptions = ref([]);
const archiveSearching = ref(false);
let archiveSearchTimer = null;
/** 远程搜病案（已归档/已封存；待归档借不了也复不了印） */
const searchArchives = (kw) => {
  if (archiveSearchTimer)
    clearTimeout(archiveSearchTimer);
  archiveSearchTimer = setTimeout(async () => {
    archiveSearching.value = true;
    try {
      const res = await getArchiveList({archiveStatus: null, keyword: kw || undefined, pageNum: 1, pageSize: 20});
      if (res.code === 200) {
        // 只放行可流通的：2 已归档 / 3 已封存
        archiveOptions.value = (res.data?.records || []).filter((a) => Number(a.archiveStatus) === 2 || Number(a.archiveStatus) === 3);
      }
    } catch (e) {
      console.error('搜索病案失败', e);
    } finally {
      archiveSearching.value = false;
    }
  }, 250);
};
const onArchiveChange = (id) => {
  const hit = archiveOptions.value.find(a => String(a.id) === String(id));
  form.archiveStatus = hit ? Number(hit.archiveStatus) : null;
};
watch(visible, (open) => {
  if (!open)
    return;
  form.archiveId = null;
  form.archiveLabel = '';
  form.archiveStatus = null;
  form.borrowType = 1;
  form.purpose = '';
  form.expectReturnDate = '';
  archiveOptions.value = [];
  searchArchives('');
});
const apply = async () => {
  if (!form.archiveId) {
    ElMessage.warning('请选择病案');
    return;
  }
  if (!form.purpose.trim()) {
    ElMessage.warning('请填写用途');
    return;
  }
  if (form.borrowType === 1 && !form.expectReturnDate) {
    ElMessage.warning('借阅必须选择应归还日期');
    return;
  }
  saving.value = true;
  try {
    const res = await applyBorrow({
      archiveId: form.archiveId,
      borrowType: form.borrowType,
      purpose: form.purpose.trim(),
      expectReturnDate: form.borrowType === 1 ? form.expectReturnDate : undefined,
    });
    if (res.code === 200) {
      ElMessage.success('申请成功，等待病案室审核');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '申请失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('申请失败');
  } finally {
    saving.value = false;
  }
};
</script>
