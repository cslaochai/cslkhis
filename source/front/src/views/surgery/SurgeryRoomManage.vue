<template>
  <!-- 手术间主数据管理（P134.1） -->
  <el-dialog v-model="visible" data-testid="p134-room-dialog" title="手术间管理（排台总表的台）" width="760px">
    <div class="mb-3 flex items-center justify-between">
      <div class="text-[12px] leading-5 text-slate-500">
        总表列 = 启用中的手术间，按排序号排列序。<b>删除是物理删</b>（编码会释放）；只是暂用不到请「停用」。
      </div>
      <el-button v-perm="'ipd:surgery:add'" :icon="Plus" data-testid="p134-room-new" size="small" type="primary"
                 @click="newRoom">
        新增手术间
      </el-button>
    </div>
    <el-table v-loading="roomLoading" :data="roomRows" data-testid="p134-room-table" size="small">
      <el-table-column label="编码" prop="roomCode" width="100"/>
      <el-table-column label="名称" min-width="140" prop="roomName"/>
      <el-table-column label="位置" min-width="140" prop="location">
        <template #default="{ row }">{{ text(row.location) }}</template>
      </el-table-column>
      <el-table-column align="center" label="排序" prop="sortOrder" width="70"/>
      <el-table-column align="center" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{
              row.status === 1 ? '启用' : '停用'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="200">
        <template #default="{ row }">
          <el-button v-perm="'ipd:surgery:add'" :data-testid="`p134-room-edit-${row.id}`" link size="small"
                     type="primary" @click="editRoom(row)">编辑
          </el-button>
          <el-button
              v-perm="'ipd:surgery:add'"
              :data-testid="`p134-room-toggle-${row.id}`"
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="toggleRoomStatus(row, row.status === 1 ? 0 : 1)"
          >
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button v-perm="'ipd:surgery:delete'" :data-testid="`p134-room-del-${row.id}`" link size="small"
                     type="danger" @click="removeRoom(row)">删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="py-4 text-[12px] text-slate-400">暂无手术间主数据（点「新增手术间」建第一间）</div>
      </template>
    </el-table>

    <div v-if="roomFormVisible" class="mt-4 rounded-lg border border-slate-200 p-4" data-testid="p134-room-form">
      <div class="mb-2 text-sm font-semibold text-slate-900">{{ roomForm.id ? '修改手术间' : '新增手术间' }}</div>
      <el-form label-width="90px">
        <el-form-item label="编码" required>
          <el-input v-model="roomForm.roomCode" data-testid="p134-room-form-code" maxlength="32"
                    placeholder="如 OR07（唯一）"/>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="roomForm.roomName" data-testid="p134-room-form-name" maxlength="64"
                    placeholder="如 7号手术间（排台快照按名称对齐）"/>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="roomForm.location" maxlength="100" placeholder="楼层/区域，如 外科楼3层东区"/>
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model.number="roomForm.sortOrder" placeholder="总表列序，小的排前" type="number"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roomForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div class="flex justify-end gap-2">
        <el-button @click="roomFormVisible = false">取消</el-button>
        <el-button v-perm="'ipd:surgery:add'" :loading="roomSubmitting" data-testid="p134-room-submit" type="primary"
                   @click="submitRoom">保存
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import {deleteOperationRoom, getOperationRoomAll, saveOperationRoom} from '@/api/inpatientOperation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
// saved = 新增/修改手术间（申请列表的手术间列要跟着刷）；changed = 停用/启用或物理删（只动总表列，不动申请数据）
const emit = defineEmits(['update:modelValue', 'saved', 'changed']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const roomRows = ref([]);
const roomLoading = ref(false);
const roomFormVisible = ref(false);
const roomSubmitting = ref(false);
const roomForm = reactive({
  id: '',
  roomCode: '',
  roomName: '',
  location: '',
  sortOrder: 1,
  status: 1,
});
const loadRooms = async () => {
  roomLoading.value = true;
  try {
    const res = await getOperationRoomAll();
    roomRows.value = (res.data || []);
  } catch (error) {
    ElMessage.error(error.message || '加载手术间失败');
  } finally {
    roomLoading.value = false;
  }
};

watch(visible, (open) => {
  if (!open) return;
  roomFormVisible.value = false;
  loadRooms();
});

const editRoom = (row) => {
  roomForm.id = row.id;
  roomForm.roomCode = row.roomCode;
  roomForm.roomName = row.roomName;
  roomForm.location = row.location || '';
  roomForm.sortOrder = row.sortOrder ?? 1;
  roomForm.status = row.status ?? 1;
  roomFormVisible.value = true;
};
const newRoom = () => {
  roomForm.id = '';
  roomForm.roomCode = '';
  roomForm.roomName = '';
  roomForm.location = '';
  roomForm.sortOrder = (roomRows.value.length + 1) || 1;
  roomForm.status = 1;
  roomFormVisible.value = true;
};
const submitRoom = async () => {
  if (!roomForm.roomCode.trim() || !roomForm.roomName.trim()) {
    ElMessage.warning('手术间编码与名称都不能为空');
    return;
  }
  roomSubmitting.value = true;
  try {
    await saveOperationRoom({
      id: roomForm.id || undefined,
      roomCode: roomForm.roomCode.trim(),
      roomName: roomForm.roomName.trim(),
      location: roomForm.location.trim() || undefined,
      sortOrder: roomForm.sortOrder,
      status: roomForm.status,
    });
    ElMessage.success('手术间已保存');
    roomFormVisible.value = false;
    emit('saved');
    await loadRooms();
  } catch (error) {
    ElMessage.error(error.message || '保存手术间失败');
  } finally {
    roomSubmitting.value = false;
  }
};
const removeRoom = async (row) => {
  try {
    await ElMessageBox.confirm(`手术间「${row.roomName}」将被物理删除（编码 ${row.roomCode} 会释放，可重建同码）。`
        + '只是暂时不用请改「停用」，不要删。历史手术单不受影响。', '删除手术间', {
      confirmButtonText: '确认删除',
      cancelButtonText: '改用停用',
      type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await deleteOperationRoom(row.id);
    ElMessage.success('手术间已删除');
    emit('changed');
    await loadRooms();
  } catch (error) {
    ElMessage.error(error.message || '删除手术间失败');
  }
};
const toggleRoomStatus = async (row, status) => {
  try {
    await saveOperationRoom({
      id: row.id,
      roomCode: row.roomCode,
      roomName: row.roomName,
      location: row.location,
      sortOrder: row.sortOrder,
      status,
    });
    ElMessage.success(status === 1 ? '手术间已启用' : '手术间已停用（总表不再出列，历史手术不受影响）');
    emit('changed');
    await loadRooms();
  } catch (error) {
    ElMessage.error(error.message || '状态更新失败');
  }
};
</script>
