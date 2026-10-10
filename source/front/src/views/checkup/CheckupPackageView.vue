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

    <!-- 套餐编辑（明细整单替换） -->
    <el-dialog v-model="editVisible" :close-on-click-modal="false"
               :title="form.id ? `编辑套餐 — ${form.packageName}` : '新增体检套餐'" width="880px">
      <el-form label-width="100px">
        <div class="flex flex-wrap gap-x-6">
          <el-form-item label="套餐名称" required>
            <el-input v-model="form.packageName" style="width: 220px"/>
          </el-form-item>
          <el-form-item label="适用性别">
            <el-select v-model="form.genderLimit" style="width: 110px">
              <el-option :value="0" label="不限"/>
              <el-option :value="1" label="男"/>
              <el-option :value="2" label="女"/>
            </el-select>
          </el-form-item>
          <el-form-item label="价格(元)">
            <el-input-number v-model="form.price" :controls="false" :min="0" :precision="2" style="width: 130px"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item label="说明">
          <el-input v-model="form.description" style="width: 520px"/>
        </el-form-item>
        <el-form-item label="项目明细" required>
          <div class="w-full">
            <el-table :data="form.items" border max-height="320" size="small">
              <el-table-column label="项目名称" min-width="220">
                <template #default="{ row }">
                  <el-input v-model="row.itemName"/>
                </template>
              </el-table-column>
              <el-table-column label="类型" width="120">
                <template #default="{ row }">
                  <el-select v-model="row.itemType" style="width: 100%">
                    <el-option v-for="t in itemTypeOptions" :key="t" :label="itemTypeMap[t]" :value="t"/>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="参考范围" width="180">
                <template #default="{ row }">
                  <el-input v-model="row.refStandard"/>
                </template>
              </el-table-column>
              <el-table-column label="单项金额(元)" width="140">
                <template #default="{ row }">
                  <el-input-number v-model="row.amount" :controls="false" :min="0" :precision="2"
                                   style="width: 120px"/>
                </template>
              </el-table-column>
              <el-table-column align="center" label="" width="60">
                <template #default="{ $index }">
                  <el-button :disabled="form.items.length <= 1" :icon="Delete" link size="small" type="danger"
                             @click="removeItem($index)"/>
                </template>
              </el-table-column>
            </el-table>
            <div class="flex items-center gap-3 mt-1">
              <el-button :icon="Plus" link type="primary" @click="addItem">添加明细行</el-button>
              <span class="text-xs text-gray-500">保存时按整单替换明细；体检登记时按这些项目预生成结果空行</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
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
import {Delete, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {checkupPackageDisable, checkupPackageListPage, checkupPackageSave, getCheckupPackage,} from '@/api/checkup';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const itemTypeMap = {1: '检验', 2: '检查', 3: '一般'};
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
    } else {
      ElMessage.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
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
// ---------------- 新增 / 编辑（明细整单替换） ----------------
const editVisible = ref(false);
const saving = ref(false);
const form = reactive({
  id: null, packageName: '', genderLimit: 0, price: null, description: '', status: 1, remark: '',
  items: [],
});
const blankItem = () => ({itemName: '', itemType: 3, refStandard: '', amount: null});
const itemTypeOptions = [1, 2, 3];
const openCreate = () => {
  Object.assign(form, {
    id: null, packageName: '', genderLimit: 0, price: null, description: '', status: 1, remark: '',
    items: [blankItem()],
  });
  editVisible.value = true;
};
const openEdit = async (row) => {
  try {
    const res = await getCheckupPackage(row.id);
    if (res.code !== 200) {
      ElMessage.error(res.message || '查询失败');
      return;
    }
    const d = res.data;
    Object.assign(form, {
      id: d.id, packageName: d.packageName, genderLimit: Number(d.genderLimit ?? 0),
      price: d.price ?? null, description: d.description || '', status: Number(d.status ?? 1),
      remark: d.remark || '',
      items: (d.items || []).map((i) => ({
        itemName: i.itemName, itemType: Number(i.itemType ?? 3), refStandard: i.refStandard || '',
        amount: i.amount ?? null,
      })),
    });
    if (!form.items.length)
      form.items = [blankItem()];
    editVisible.value = true;
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  }
};
const addItem = () => form.items.push(blankItem());
const removeItem = (idx) => form.items.splice(idx, 1);
const save = async () => {
  if (!form.packageName.trim()) {
    ElMessage.warning('请填写套餐名称');
    return;
  }
  const items = form.items.filter((i) => i.itemName.trim());
  if (!items.length) {
    ElMessage.warning('项目明细至少一条');
    return;
  }
  saving.value = true;
  try {
    const res = await checkupPackageSave({
      id: form.id ?? undefined,
      packageName: form.packageName.trim(),
      genderLimit: form.genderLimit,
      price: form.price ?? undefined,
      description: form.description.trim() || undefined,
      status: form.status,
      remark: form.remark.trim() || undefined,
      items: items.map((i) => ({
        itemName: i.itemName.trim(),
        itemType: i.itemType,
        refStandard: i.refStandard?.trim() || undefined,
        amount: i.amount ?? undefined,
      })),
    });
    if (res.code === 200) {
      ElMessage.success(form.id ? '套餐已保存' : '套餐已创建');
      editVisible.value = false;
      loadList();
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};
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
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
onMounted(() => {
  loadList();
});
</script>
