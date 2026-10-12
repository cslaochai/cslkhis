<template>
  <el-dialog
      v-model="visible"
      :title="form.supplierId ? '编辑供应商' : '新增供应商'"
      data-testid="supplier-dialog"
      width="560px"
  >
    <el-form label-width="100px">
      <el-form-item label="编码">
        <el-input v-model="form.supplierCode" placeholder="留空由后端生成"/>
      </el-form-item>
      <el-form-item label="名称" required>
        <el-input v-model="form.supplierName" placeholder="供应商名称"/>
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="form.contactPerson"/>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone"/>
      </el-form-item>
      <el-form-item label="经营许可证">
        <el-input v-model="form.businessLicense"/>
      </el-form-item>
      <el-form-item label="证照有效期">
        <el-date-picker v-model="form.licenseExpiry" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="评级">
        <el-select v-model="form.rating" :fit-input-width="false" style="width: 100%">
          <el-option v-for="o in ratingOptions" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'pharmacy:supplier:add'" :loading="saving" data-testid="supplier-save-btn" type="primary"
                 @click="save">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 供应商新增/编辑（合并：row 为空=新增，row.supplierId 存在=编辑）
 * 逻辑自 SupplierView.vue 原样搬出，字段名、校验、接口、提示文案均未改动。
 * ratingOptions 由父页面按字典加载（筛选栏与列表评级列同名），此处只作下拉数据源。
 */
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { upsertSupplier } from '@/api/purchase';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
  ratingOptions: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) });

const saving = ref(false);
const form = reactive({
  supplierId: null,
  supplierCode: '',
  supplierName: '',
  contactPerson: '',
  phone: '',
  address: '',
  licenseNo: '',
  licenseExpiry: '',
  rating: 3,
  status: 1,
  remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  if (row && row.supplierId) {
    form.supplierId = row.supplierId;
    form.supplierCode = row.supplierCode || '';
    form.supplierName = row.supplierName || '';
    form.contactPerson = row.contactPerson || '';
    form.phone = row.phone || '';
    form.address = row.address || '';
    form.licenseNo = row.licenseNo || '';
    form.licenseExpiry = row.licenseExpiry || '';
    form.rating = Number(row.rating ?? 3);
    form.status = Number(row.status ?? 1);
    form.remark = row.remark || '';
  } else {
    form.supplierId = null;
    form.supplierCode = '';
    form.supplierName = '';
    form.contactPerson = '';
    form.phone = '';
    form.address = '';
    form.licenseNo = '';
    form.licenseExpiry = '';
    form.rating = 3;
    form.status = 1;
    form.remark = '';
  }
});

const save = async () => {
  if (!form.supplierName.trim()) {
    ElMessage.warning('请填写供应商名称');
    return;
  }
  saving.value = true;
  try {
    const res = await upsertSupplier({
      supplierId: form.supplierId || undefined,
      supplierCode: form.supplierCode.trim() || undefined,
      supplierName: form.supplierName.trim(),
      contactPerson: form.contactPerson.trim() || undefined,
      phone: form.phone.trim() || undefined,
      address: form.address.trim() || undefined,
      licenseNo: form.licenseNo.trim() || undefined,
      licenseExpiry: form.licenseExpiry || undefined,
      rating: form.rating,
      status: form.status,
      remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(form.supplierId ? '供应商已更新' : '供应商已创建');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};
</script>
