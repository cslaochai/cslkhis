<template>
  <!-- 建立清点单 -->
  <el-dialog v-model="visible" data-testid="g15-count-create-dialog" title="建立手术器械清点单"
             width="720px">
    <el-form label-width="120px">
      <el-form-item label="手术申请单" required>
        <el-select v-model="form.applyId" class="!w-full" data-testid="g15-count-create-apply" filterable
                   placeholder="选择手术申请单">
          <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="洗手护士">
        <el-select v-model="form.instrumentNurseId" class="!w-full" clearable filterable
                   placeholder="器械护士">
          <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="巡回护士">
        <el-select v-model="form.circulateNurseId" class="!w-full" clearable filterable
                   placeholder="巡回护士">
          <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="清点清单">
        <el-input v-model="form.itemsText" :rows="6" data-testid="g15-count-items"
                  placeholder="每行一条：名称,类别,数量（类别 1器械 2敷料 3缝针 4刀片 5其他）"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] text-amber-700">
      <el-icon class="mr-1 align-middle">
        <Warning/>
      </el-icon>
      清单在开始清点之前定：进入任何阶段后不能再加行（后补的基线没有比对意义）。
    </div>
    <template #footer>
      <el-button v-perm="'ipd:operationCount:add'" :loading="submitting" data-testid="g15-count-create-submit"
                 type="primary" @click="submit">建立
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Warning} from '@element-plus/icons-vue';
import {createOperationCount} from '@/api/operationCount';
import {applyLabel} from '@/lib/operationCount';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 申请单/员工候选由父页面统一加载（页面顶部筛选也在用同一份数据）
  applies: {type: Array, default: () => []},
  employees: {type: Array, default: () => []},
  // 父页面当前选中的申请单：打开时默认选它（与拆分前行为一致）
  presetApplyId: {type: String, default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const form = reactive({
  applyId: '',
  instrumentNurseId: '',
  circulateNurseId: '',
  itemsText: '止血钳,1,10\n纱布块,2,20\n圆针,3,5\n11号刀片,4,2',
});
watch(visible, (v) => {
  if (!v)
    return;
  // 清单草稿不重置：填到一半误关弹框不该清空（与拆分前一致）
  form.applyId = props.presetApplyId || '';
  form.instrumentNurseId = '';
  form.circulateNurseId = '';
});
/** 文本 → items：每行「名称,类别,数量」，类别 1器械 2敷料 3缝针 4刀片 5其他 */
const parseItems = (raw) => {
  const items = [];
  raw
      .split('\n')
      .map((l) => l.trim())
      .filter(Boolean)
      .forEach((line) => {
        const parts = line.split(/[,，]/).map((p) => p.trim());
        if (!parts[0])
          return;
        items.push({
          itemName: parts[0],
          itemCategory: Number(parts[1] || 1),
          beforeQty: Number(parts[2] || 0),
        });
      });
  return items;
};
const submit = async () => {
  if (!form.applyId) {
    ElMessage.warning('请选择手术申请单');
    return;
  }
  submitting.value = true;
  try {
    const res = await createOperationCount({
      applyId: form.applyId,
      instrumentNurseId: form.instrumentNurseId || undefined,
      circulateNurseId: form.circulateNurseId || undefined,
      items: parseItems(form.itemsText),
    });
    ElMessage.success(`清点单已建立：${res.data || ''}（请依次完成三次核对）`);
    visible.value = false;
    // 把新建的申请单 id 带回给父页面：切过去并加载清点单
    emit('saved', form.applyId);
  } catch (e) {
    ElMessage.error(e.message || '建立清点单失败');
  } finally {
    submitting.value = false;
  }
};
</script>
