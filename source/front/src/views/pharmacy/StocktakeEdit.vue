<template>
  <!-- 建单 / 改范围 -->
  <el-dialog v-model="visible" :title="id ? '修改盘点单' : '新建盘点单'" data-testid="stocktake-form-dialog"
             width="620px">
    <el-form label-width="92px">
      <el-form-item label="盘点主题" required>
        <el-input v-model="form.stocktakeTitle" data-testid="stocktake-title-input" maxlength="100" placeholder="如：药房 9 月西药专项盘点"
                  show-word-limit/>
      </el-form-item>
      <el-form-item label="药品类型">
        <el-select v-model="form.scopeDrugType" :fit-input-width="false" clearable data-testid="stocktake-drugtype-select"
                   placeholder="全部类型">
          <el-option v-for="d in drugTypeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="form.scopeKeyword" clearable data-testid="stocktake-keyword-input" maxlength="50"
                  placeholder="药品名称/编码/批号，留空不限"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" placeholder="盘点依据、参与人等" type="textarea"/>
      </el-form-item>
      <el-alert :closable="false" class="!mt-1" show-icon type="info">
        <template #title>范围留空 = 全盘所有库存批次。建单即抓「药品×批号」的账面数快照，之后库存再变也不改快照。
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <el-button :loading="formSaving" data-testid="stocktake-form-save" type="primary" @click="saveForm">
        {{ id ? '保存' : '建单并抓快照' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 盘点单 建单/改范围 弹窗 —— 从 views/pharmacy/StocktakeView.vue 抽出
 *
 * 建单即抓「药品×批号」账面快照（之后库存再变不改快照）；改范围会重抓快照并清空已录实盘数，破坏性 → 二次确认。
 * 明细/成本全部服务端算，前端只提交范围条件。drugTypeDict 由父页加载后作 prop 传入（列表页共用一份字典请求）。
 */
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { upsertStocktake } from '@/api/pharmacy';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  id: { type: [Number, String], default: null },
  row: { type: Object, default: null },
  drugTypeDict: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formSaving = ref(false);
const form = reactive({
  stocktakeTitle: '',
  scopeDrugType: null,
  scopeKeyword: '',
  remark: '',
});

watch(() => props.modelValue, (v) => {
  if (!v) return;
  if (props.id && props.row) {
    form.stocktakeTitle = props.row.stocktakeTitle;
    form.scopeDrugType = props.row.scopeDrugType ?? null;
    form.scopeKeyword = props.row.scopeKeyword || '';
    form.remark = props.row.remark || '';
  } else {
    form.stocktakeTitle = `药房库存盘点 ${new Date().toISOString().slice(0, 10)}`;
    form.scopeDrugType = null;
    form.scopeKeyword = '';
    form.remark = '';
  }
});

const saveForm = async () => {
  if (!form.stocktakeTitle.trim()) {
    ElMessage.warning('请填写盘点主题');
    return;
  }
  // 改范围会重抓快照并清空已录入的实盘数 —— 破坏性，必须二次确认
  if (props.id && !!(form.scopeDrugType || form.scopeKeyword?.trim())) {
    try {
      await ElMessageBox.confirm('修改盘点范围会重新抓取账面快照，并清空本单已录入的实盘数。确认继续？', '重抓快照确认', {
        type: 'warning',
        confirmButtonText: '确认重抓'
      });
    } catch {
      return;
    }
  }
  formSaving.value = true;
  try {
    const res = await upsertStocktake({
      id: props.id || undefined,
      stocktakeTitle: form.stocktakeTitle.trim(),
      scopeDrugType: form.scopeDrugType ?? undefined,
      scopeKeyword: form.scopeKeyword?.trim() || undefined,
      remark: form.remark?.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已保存');
      visible.value = false;
      emit('saved', { id: res.data, isNew: !props.id });
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    formSaving.value = false;
  }
};
</script>
