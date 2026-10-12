<template>
  <!-- 模板编辑 -->
  <el-dialog v-model="visible"
             :title="form.id ? `编辑模板 — ${form.templateCode}` : '新增模板'" width="880px">
    <el-form label-width="110px">
      <div class="flex flex-wrap gap-x-6">
        <el-form-item label="包编码" required>
          <el-input v-model="form.templateCode" placeholder="如 CSSDP008" style="width: 200px"/>
        </el-form-item>
        <el-form-item label="器械包名称" required>
          <el-input v-model="form.packName" style="width: 200px"/>
        </el-form-item>
        <el-form-item label="默认灭菌方式" required>
          <el-select v-model="form.sterilizeMethod" :fit-input-width="false" style="width: 160px">
            <el-option v-for="d in methodDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="组成明细" required>
        <div class="w-full">
          <el-table :data="form.items" border max-height="320" size="small">
            <el-table-column label="器械/耗材名称" min-width="260">
              <template #default="{ row }">
                <el-select v-model="row.itemName" :fit-input-width="false" allow-create class="w-full"
                           default-first-option filterable placeholder="从耗材字典选择或手输"
                           @change="(v: string) => onTplItemNameChange(row, v)">
                  <el-option v-for="(c, i) in tplItemOptions" :key="i" :label="c.name" :value="c.name"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="规格" width="170">
              <template #default="{ row }">
                <el-input v-model="row.spec"/>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="100">
              <template #default="{ row }">
                <el-input v-model="row.unit"/>
              </template>
            </el-table-column>
            <el-table-column label="基数" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" controls-position="right" size="small"
                                 style="width: 90px"/>
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
            <span class="text-xs text-gray-500">共 {{ form.items.length }} 条明细</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'asset:cssd:edit'" :loading="saving" type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 器械包模板弹框：row 为空走新增、有值走修改（后端同一个 upsert，明细按 id 回查）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus} from '@element-plus/icons-vue';
import {cssdTemplateItemSelectList, cssdTemplateUpsert, getCssdTemplateDetail} from '@/api/cssd';
import {getConsumableSelectList} from '@/api/supplies';

const props = defineProps({
  modelValue: Boolean,
  // 当前行：新增时为 null；修改时先用行数据兜「打开瞬间不闪白」，明细再按 id 回查
  row: {type: Object, default: null},
  // 页面级参照：灭菌方式字典由主页面加载一次后传入（列表列也用它翻译）
  methodDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// ---------------- 新增/编辑 ----------------
const saving = ref(false);
const form = reactive({
  id: null, templateCode: '', packName: '', sterilizeMethod: 1, status: 1, remark: '',
  items: [],
});
const blankItem = () => ({itemName: '', spec: '', unit: '件', quantity: 1});
const addItem = () => form.items.push(blankItem());
const removeItem = (idx) => form.items.splice(idx, 1);
// ---------------- 组成明细名称下拉数据源：启用模板明细去重汇总 + 耗材字典 ----------------
const tplItemOptions = ref([]);
const loadTplItemOptions = async () => {
  try {
    const [tplRes, conRes] = await Promise.all([cssdTemplateItemSelectList(), getConsumableSelectList()]);
    const fromTpl = (tplRes.code === 200 ? tplRes.data || [] : [])
        .map((x) => ({name: x.itemName, spec: x.spec || undefined, unit: x.unit || undefined}));
    const fromConsumable = (conRes.code === 200 ? conRes.data || [] : [])
        .map((x) => ({name: x.consumableName, spec: x.specification || undefined, unit: x.unit || undefined}));
    const seen = new Set();
    tplItemOptions.value = [...fromTpl, ...fromConsumable].filter((x) => x.name && !seen.has(x.name) && !!seen.add(x.name));
  } catch (e) {
    console.error('加载器械/耗材下拉失败', e);
  }
};
const onTplItemNameChange = (row, name) => {
  const c = tplItemOptions.value.find((x) => x.name === name);
  if (!c)
    return;
  row.spec = c.spec || row.spec;
  row.unit = c.unit || row.unit;
};
watch(visible, async (v) => {
  if (!v) return;
  if (!tplItemOptions.value.length) loadTplItemOptions();
  const row = props.row;
  if (!row?.id) {
    Object.assign(form, {
      id: null, templateCode: '', packName: '', sterilizeMethod: 1, status: 1, remark: '',
      items: [blankItem()],
    });
    return;
  }
  // 先用行数据兜住标题与基础字段，明细再按 id 回查
  Object.assign(form, {
    id: row.id, templateCode: row.templateCode ?? '', packName: row.packName ?? '',
    remark: row.remark ?? '', items: [],
  });
  try {
    const res = await getCssdTemplateDetail(row.id);
    if (res.code !== 200) {
      ElMessage.error(res.message || '查询失败');
      visible.value = false;
      return;
    }
    const d = res.data;
    Object.assign(form, {
      id: d.id, templateCode: d.templateCode, packName: d.packName,
      sterilizeMethod: Number(d.sterilizeMethod), status: Number(d.status), remark: d.remark || '',
      items: (d.items || []).map((i) => ({
        itemName: i.itemName, spec: i.spec || '', unit: i.unit || '件', quantity: Number(i.quantity),
      })),
    });
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
    visible.value = false;
  }
});
const save = async () => {
  if (!form.templateCode.trim()) {
    ElMessage.warning('请填写包编码');
    return;
  }
  if (!form.packName.trim()) {
    ElMessage.warning('请填写器械包名称');
    return;
  }
  const items = form.items.filter((i) => i.itemName.trim());
  if (!items.length) {
    ElMessage.warning('组成明细至少一条');
    return;
  }
  saving.value = true;
  try {
    const res = await cssdTemplateUpsert({
      id: form.id ?? undefined,
      templateCode: form.templateCode.trim(),
      packName: form.packName.trim(),
      sterilizeMethod: form.sterilizeMethod,
      status: form.status,
      remark: form.remark || undefined,
      items: items.map((i) => ({
        itemName: i.itemName.trim(), spec: i.spec?.trim() || undefined,
        unit: i.unit?.trim() || '件', quantity: Number(i.quantity) || 1,
      })),
    });
    if (res.code === 200) {
      ElMessage.success('模板已保存');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
