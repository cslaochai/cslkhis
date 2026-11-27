<template>
  <!-- 签名资格复核：人事异动把「需要签名」的岗位换成「不需要签名」的岗位时，
       由医务科决定证书保留还是吊销；医生换科室/医生转护士（仍要签名）不会出现在这里 -->
  <el-dialog v-model="visible" title="签名资格复核" width="900px">
    <el-alert :closable="false" class="mb-3" show-icon type="info">
      <template #default>
        <span class="text-xs leading-relaxed">
          只处理「新岗位不再需要电子签名」的人。保留 = 证书继续有效；吊销 = 走业务吊销，系统自动通知信息科按需重新签发。
        </span>
      </template>
    </el-alert>
    <el-table v-loading="loading" :data="rows" border empty-text="暂无待复核" size="small">
      <el-table-column label="员工" width="140">
        <template #default="{ row }">
          <div class="text-xs font-medium text-slate-700">{{ row.empName || row.employeeId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="岗位变化" min-width="320">
        <template #default="{ row }">
          <div class="text-xs text-slate-500">原：{{ row.fromPosts }}</div>
          <div class="text-xs text-slate-700">现：{{ row.toPosts }}</div>
        </template>
      </el-table-column>
      <el-table-column label="当前有效证书" width="180">
        <template #default="{ row }">
          <span class="font-mono text-xs text-slate-600">{{ row.certNo || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="150">
        <template #default="{ row }">
          <span class="text-xs text-slate-500">{{ row.createTime || '' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button :loading="actingId === String(row.id)" link size="small" type="primary"
                     @click="keep(row)">保留证书
          </el-button>
          <el-button :loading="actingId === String(row.id)" link size="small" type="danger"
                     @click="revoke(row)">吊销证书
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getSignQualReviewList, handleSignQualReview} from '@/api/signature';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'done']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const rows = ref([]);
const loading = ref(false);
const actingId = ref(null);

const load = async () => {
  loading.value = true;
  try {
    const res = await getSignQualReviewList();
    rows.value = res.data || [];
  } catch (e) {
    // 全局拦截器已提示，这里只留痕
    console.error('签名资格复核列表加载失败', e);
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

watch(() => props.modelValue, (open) => {
  if (open) load();
});

const submit = async (row, action, remark) => {
  actingId.value = String(row.id);
  try {
    await handleSignQualReview({id: row.id, action, remark});
    ElMessage.success(action === 1 ? '已保留证书' : '已吊销，已通知信息科按需重新签发');
    await load();
    emit('done');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {
    console.error('签名资格复核处理失败', e);
  } finally {
    actingId.value = null;
  }
};

const keep = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确认保留 ${row.empName || '该员工'} 的签名证书？保留后该证书继续有效。`,
        '保留证书', {type: 'info', confirmButtonText: '确认保留', cancelButtonText: '取消'});
  } catch (e) {
    return;
  }
  await submit(row, 1, '岗位调整后经医务科复核，证书继续有效');
};

const revoke = async (row) => {
  let reason = '';
  try {
    const {value} = await ElMessageBox.prompt(
        `吊销 ${row.empName || '该员工'} 的证书 ${row.certNo || ''}，请写吊销依据（如：已转行政岗、不再书写病历）`,
        '吊销证书', {
          type: 'warning',
          confirmButtonText: '确认吊销',
          cancelButtonText: '取消',
          inputValidator: (v) => (v && v.trim() ? true : '吊销必须写明理由'),
        });
    reason = value.trim();
  } catch (e) {
    return;
  }
  await submit(row, 2, reason);
};
</script>
