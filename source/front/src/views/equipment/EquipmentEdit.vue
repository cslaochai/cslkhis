<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑设备' : '新增设备'" destroy-on-close width="620px">
    <el-form label-width="96px">
      <el-form-item label="设备编码" required>
        <el-input v-model="form.equipmentCode" :disabled="!!form.id" maxlength="32"
                  placeholder="资产编号，如 EQ092 / RBT-11"/>
        <div class="mt-1 text-xs text-slate-400">
          {{ isLogistics ? '物流设备的编码就是接入编号（MQTT 设备号），保存后同步进机器人车队' : '资产编号建档后不可修改' }}
        </div>
      </el-form-item>
      <el-form-item label="设备名称" required>
        <el-input v-model="form.equipmentName" maxlength="60" placeholder="如 便携式超声诊断仪"/>
      </el-form-item>
      <el-form-item label="设备类别" required>
        <el-select v-model="form.category" :fit-input-width="false" style="width: 100%">
          <el-option v-for="d in categoryDict" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="使用科室">
        <DeptTreeSelect v-model="form.deptId" mode="ORG" scope="ALL" width="100%" @change="onDeptChange"/>
        <div class="mt-1 text-xs text-slate-400">设备科替全院建档，科室下拉按全量取数（按当前人科室会只剩设备科自己）</div>
      </el-form-item>
      <el-form-item label="品牌">
        <el-input v-model="form.brand" maxlength="40"/>
      </el-form-item>
      <el-form-item label="型号">
        <el-input v-model="form.model" maxlength="40"/>
      </el-form-item>
      <el-form-item label="购置日期">
        <el-date-picker v-model="form.purchaseDate" style="width: 200px" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="购置价格">
        <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="1000" style="width: 200px"/>
        <span class="ml-2 text-xs text-slate-400">元</span>
      </el-form-item>
      <el-form-item label="状态" required>
        <el-select v-model="form.status" :fit-input-width="false" style="width: 160px">
          <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="维保周期">
        <el-input-number v-model="form.maintainCycleDays" :min="1" :max="3650" :step="30" style="width: 160px"/>
        <span class="ml-2 text-xs text-slate-400">天，下次维保自动按最近维保 + 周期算</span>
      </el-form-item>
      <el-form-item label="最近维保">
        <el-date-picker v-model="form.lastMaintainDate" style="width: 200px" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item v-if="!isLogistics" label="共享池">
        <el-switch v-model="form.rentable" :active-value="1" :inactive-value="0"
                   active-text="进入共享池（可被其他科室租借）" inactive-text="不进入共享池"/>
      </el-form-item>
      <el-form-item v-else label="共享池">
        <span class="text-xs text-slate-500">物流机器人不进共享池：由机器人调度按派单算法驱动</span>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="200" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:equipment:add'" type="primary" @click="submit">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 设备建档/修改弹框（设备管理页「新增设备 / 编辑」共用；row 为空走新增）。
 *
 * 设备编码 = 资产编号，建档后不可改：改编号等于换一台设备，会让维保/计量历史与物联网接入行对不上。
 * 类别选 8-物流设备时，后端把「设备编码」当接入编号同步进 biz_iot_device，并强制不进共享池。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import DeptTreeSelect from '@/components/his/DeptTreeSelect.vue';
import {equipmentUpsert} from '@/api/equipment';
import {loadDeptTreeOptions, flattenDeptTree} from '@/lib/deptOptions';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前行：为空 = 新增；有值 = 修改
  row: {type: Object, default: null},
  // 设备类别字典由父页统一加载后下传（查询卡与表单共用一份）
  categoryDict: {type: Array, default: () => []},
  statusDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const LOGISTICS_CATEGORY = 8;

const blankForm = () => ({
  id: null, equipmentCode: '', equipmentName: '', category: 5, deptId: null, deptName: '',
  brand: '', model: '', purchaseDate: '', purchasePrice: null, status: 1,
  maintainCycleDays: 180, lastMaintainDate: '', rentable: 0, remark: '',
});
const form = reactive(blankForm());
const isLogistics = computed(() => Number(form.category) === LOGISTICS_CATEGORY);

// 科室 id → 名称：档案存科室名快照，避免跨模块联查
const deptNames = ref({});
const loadDeptNames = async () => {
  try {
    const tree = await loadDeptTreeOptions({mode: 'ORG', scope: 'ALL'});
    const map = {};
    flattenDeptTree(tree).forEach((d) => { map[d.id] = d.deptName; });
    deptNames.value = map;
  } catch (e) {
    console.error('加载科室清单失败', e);
  }
};

watch(visible, async (open) => {
  if (!open) return;
  const row = props.row;
  Object.assign(form, row
      ? {
        id: row.id,
        equipmentCode: row.equipmentCode,
        equipmentName: row.equipmentName,
        category: row.category ?? 7,
        deptId: row.deptId != null ? Number(row.deptId) : null,
        deptName: row.deptName || '',
        brand: row.brand || '',
        model: row.model || '',
        purchaseDate: row.purchaseDate || '',
        purchasePrice: row.purchasePrice != null ? Number(row.purchasePrice) : null,
        status: row.status ?? 1,
        maintainCycleDays: row.maintainCycleDays ?? 180,
        lastMaintainDate: row.lastMaintainDate || '',
        rentable: row.rentable ?? 0,
        remark: row.remark || '',
      }
      : blankForm());
  await loadDeptNames();
});

const onDeptChange = (deptId) => {
  form.deptName = deptId == null ? '' : (deptNames.value[deptId] || '');
};

const submit = async () => {
  if (!form.equipmentCode.trim()) return ElMessage.warning('设备编码不能为空');
  if (!form.equipmentName.trim()) return ElMessage.warning('设备名称不能为空');
  if (form.category == null) return ElMessage.warning('请选择设备类别');
  try {
    await equipmentUpsert({
      ...form,
      id: form.id ?? undefined,
      equipmentCode: form.equipmentCode.trim(),
      equipmentName: form.equipmentName.trim(),
      // 「不限/未填」不发后端，DTO 侧按 null 处理
      deptId: form.deptId ?? undefined,
      deptName: form.deptName || undefined,
      purchaseDate: form.purchaseDate || undefined,
      purchasePrice: form.purchasePrice ?? undefined,
      lastMaintainDate: form.lastMaintainDate || undefined,
      maintainCycleDays: form.maintainCycleDays ?? undefined,
      remark: form.remark || undefined,
    });
    ElMessage.success(form.id ? '设备修改成功' : '设备建档成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存设备档案失败', e);
  }
};
</script>
