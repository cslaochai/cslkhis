<template>
  <el-dialog v-model="visible" :title="`列配置 · ${tableName}`" width="860px">
    <el-alert
        :closable="false"
        class="qa-column-manage-hint"
        show-icon
        title="只启用经营统计需要的列：患者个人信息列（姓名/手机号/身份证等）与审计列请停用，停用后不会出现在模型提示词中。「可过滤」标记决定模型能否用该列做 WHERE 条件。敏感类型在此统一维护：1-脱敏（出参自动打码）2-禁止出库（查询直接拦截）。"
        type="info"
    />
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-select v-model="colQuery.isEnabled" clearable placeholder="是否喂给模型" style="width: 150px">
          <el-option :value="1" label="启用"/>
          <el-option :value="0" label="停用"/>
        </el-select>
        <el-button type="primary" @click="searchColumns">查询</el-button>
        <div class="flex-1"></div>
        <el-button plain type="primary" @click="openColumnEdit()">新增列</el-button>
      </div>
    </el-card>

    <el-table v-loading="colLoading" :data="colRows" border stripe>
      <el-table-column label="列名" prop="columnName" show-overflow-tooltip width="170"/>
      <el-table-column label="列含义" prop="columnDesc" show-overflow-tooltip min-width="220"/>
      <el-table-column align="center" label="可过滤" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isFilterable ? 'success' : 'info'" effect="plain" size="small">
            {{ row.isFilterable ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="可分组" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isGroupable ? 'success' : 'info'" effect="plain" size="small">
            {{ row.isGroupable ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="喂给模型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isEnabled ? 'success' : 'warning'" effect="plain" size="small">
            {{ row.isEnabled ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="敏感类型" width="110">
        <template #default="{ row }">
          <el-tag
              v-if="row.sensitiveType === 2"
              type="danger"
              effect="dark"
              size="small"
          >禁止出库</el-tag>
          <el-tag
              v-else-if="row.sensitiveType === 1"
              type="warning"
              effect="plain"
              size="small"
          >脱敏{{ row.maskType ? '·' + row.maskType : '' }}</el-tag>
          <span v-else class="qa-sens-none">—</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="排序" prop="sortOrder" width="70"/>
      <el-table-column align="center" label="操作" width="120">
        <template #default="{ row }">
          <el-button link size="small" type="primary" @click.stop="openColumnEdit(row)">编辑</el-button>
          <el-button link size="small" type="danger" @click.stop="removeColumn(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="colQuery.pageNum"
        v-model:page-size="colQuery.pageSize"
        :page-sizes="PAGE_SIZES"
        :total="colTotal"
        class="pager"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onColSizeChange"
        @current-change="loadColumns"
    />

    <!-- 列新增/编辑弹框 -->
    <AiAdminQaColumnEdit v-model="colEditVisible" :row="colEditRow" :table-name="tableName" @saved="onColSaved"/>
  </el-dialog>
</template>

<script setup>
/**
 * 运营问数数据列配置「管理」弹框（自 AiAdminView 拆出）
 *
 * 容器弹框：内嵌列列表 + 分页 + 列新增/编辑（AiAdminQaColumnEdit 已拆独立组件）。
 * 表单 state / 校验 / 提交都在各自子组件内，本组件只控制显隐与刷新。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {deleteOperationQaColumn, getOperationQaColumnPage} from '@/api/ai';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import AiAdminQaColumnEdit from './AiAdminQaColumnEdit.vue';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 所属表名
  tableName: {type: String, required: true},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const colQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  isEnabled: undefined,
});
const colLoading = ref(false);
const colRows = ref([]);
const colTotal = ref(0);

const loadColumns = async () => {
  colLoading.value = true;
  try {
    const res = await getOperationQaColumnPage({
      pageNum: colQuery.pageNum,
      pageSize: colQuery.pageSize,
      tableName: props.tableName,
      isEnabled: colQuery.isEnabled,
    });
    colRows.value = res?.data?.records || [];
    colTotal.value = res?.data?.total || 0;
  } catch (e) {
    console.error('加载列配置失败', e);
  } finally {
    colLoading.value = false;
  }
};
const searchColumns = () => {
  colQuery.pageNum = 1;
  loadColumns();
};
const onColSizeChange = () => {
  colQuery.pageNum = 1;
  loadColumns();
};

// 列新增/编辑弹框（逻辑在子组件内）
const colEditVisible = ref(false);
const colEditRow = ref(null);
const openColumnEdit = (row) => {
  colEditRow.value = row || null;
  colEditVisible.value = true;
};
const onColSaved = () => {
  colQuery.pageNum = 1;
  loadColumns();
};

const removeColumn = async (row) => {
  await ElMessageBox.confirm(`确认删除列 ${row.tableName}.${row.columnName}？`, '删除确认', {type: 'warning'});
  try {
    await deleteOperationQaColumn(row.id);
    ElMessage.success('已删除');
    loadColumns();
  } catch (e) {
    console.error('删除列配置失败', e);
  }
};

watch(() => props.modelValue, (v) => {
  if (!v) {
    return;
  }
  colQuery.pageNum = 1;
  colQuery.isEnabled = undefined;
  loadColumns();
});
</script>

<style scoped>
.qa-column-manage-hint {
  margin-bottom: 12px;
}

.filter-card {
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.flex-1 {
  flex: 1;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.qa-sens-none {
  color: #c0c4cc;
}
</style>
