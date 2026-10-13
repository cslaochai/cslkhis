<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="套餐名称">
            <el-input v-model="query.keyword" clearable placeholder="套餐名称" style="width: 200px"
                      @keyup.enter="loadList"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'checkup:manage:add'" :icon="Plus" type="primary" @click="openCreate">新增套餐</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe>
        <el-table-column label="套餐名称" min-width="170" prop="packageName" show-overflow-tooltip/>
        <el-table-column align="center" label="适用性别" width="90">
          <template #default="{ row }">{{ genderText(row.genderLimit) }}</template>
        </el-table-column>
        <el-table-column align="right" label="价格(元)" prop="price" width="100"/>
        <el-table-column label="说明" min-width="180" prop="description" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description || '—' }}</template>
        </el-table-column>
        <el-table-column align="center" label="项目数" width="80">
          <template #default="{ row }">{{ (row.items || []).length }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small">
              {{ Number(row.status) === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140" prop="remark" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="130">
          <template #default="{ row }">
            <el-button v-perm="'checkup:manage:add'" link size="small" type="primary" @click="openEdit(row)">编辑
            </el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'checkup:manage:edit'" link size="small" type="warning"
                       @click="disable(row)">停用
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无体检套餐"/>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 套餐新增/编辑（明细整单替换，表单逻辑在子组件内） -->
    <CheckupPackageEdit v-model="editVisible" :row="editRow" @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * 体检套餐（菜单 2930，sql/185；原为 905 体检管理的第二个页签）
 *
 * 套餐是体检登记的**主数据**：登记时按套餐项目预生成结果空行，套餐价格决定登记金额。
 *
 * 拆分口径：套餐（配置，随体检中心开展项目调整，低频）与体检登记（单据，逐人体检经办，高频）
 * 主键维度不同、写接口不同（checkupPackageSave/checkupPackageDisable ↔ checkupRecordCreate/Start/
 * ResultSave/Conclude/Delete）。拆开后 905 登记弹窗的套餐下拉照旧走 checkupPackageListPage —— 数据流不断。
 *
 * 口径：
 * 1. 套餐保存是**明细整单替换**（Save DTO items 整删重插），编辑前必须回填全量明细。
 * 2. 项目类型 itemType：1 检验 / 2 检查 / 3 一般 —— 决定结果行的参考范围展示口径。
 * 3. 停用走 checkupPackageDisable（后端只此一个状态接口）；重新启用走编辑把状态改回启用
 *    （套餐已停用不影响历史登记单，只是不再出现在登记下拉）。
 * 4. 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {checkupPackageDisable, checkupPackageListPage} from '@/api/checkup';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import CheckupPackageEdit from './CheckupPackageEdit.vue';

const genderText = (v) => ({0: '不限', 1: '男', 2: '女'}[Number(v)] || '不限');
// ---------------- 套餐列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({keyword: '', status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await checkupPackageListPage({
      keyword: query.keyword.trim() || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.keyword = '';
  query.status = null;
  query.pageNum = 1;
  loadList();
};
// ---------------- 新增 / 编辑（子组件 CheckupPackageEdit，row 为空走新增） ----------------
const editVisible = ref(false);
const editRow = ref(null);
const openCreate = () => {
  editRow.value = null;
  editVisible.value = true;
};
const openEdit = (row) => {
  editRow.value = row;
  editVisible.value = true;
};
// ---------------- 停用 ----------------
const disable = async (row) => {
  try {
    await ElMessageBox.confirm(`停用后套餐不再出现在体检登记下拉（历史登记单不受影响），确认停用「${row.packageName}」？`, '停用确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await checkupPackageDisable(row.id);
    if (res.code === 200) {
      ElMessage.success('已停用');
      loadList();
    } 
  } catch (e) {
    console.error(e);
    
  }
};
onMounted(() => {
  loadList();
});
</script>
