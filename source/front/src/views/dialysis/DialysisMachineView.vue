<script setup lang="ts">
/**
 * 血液净化机位管理（菜单 2931，sql/186）
 *
 * 原为 413「血液净化中心」的「机位管理」页签，2026-09-29 拆出独立挂菜单。
 * 透析室专有资源（不复用 sys_bed），机位号唯一；维修/停用机位不可排班（排班侧收口）。
 * 机位是**建档**（装机/报废低频），排班与上下机是**每日动作**，岗位与频率不同，故拆开。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { listDialysisMachinePage, upsertDialysisMachine } from '@/api/dialysis'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { machineStatusTag } from '@/lib/dialysis'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// ---------------- 字典 ----------------
const statusDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(DICT_TYPE.DIALYSIS_MACHINE_STATUS)
    statusDict.value = res?.data?.[DICT_TYPE.DIALYSIS_MACHINE_STATUS] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({ machineNo: '', roomName: '', status: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await listDialysisMachinePage({
      machineNo: query.machineNo.trim() || undefined,
      roomName: query.roomName.trim() || undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else {
      ElMessage.error(res.message || '查询机位失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '查询机位失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  Object.assign(query, { machineNo: '', roomName: '', status: null, pageNum: 1 })
  loadList()
}

// ---------------- 新增/编辑 ----------------
const editVisible = ref(false)
const saving = ref(false)
const form = reactive<any>({ id: null, machineNo: '', roomName: '', status: 1, remark: '' })

const openEdit = (row?: any) => {
  Object.assign(form, {
    id: row?.id ?? null, machineNo: row?.machineNo ?? '', roomName: row?.roomName ?? '',
    status: row?.status ?? 1, remark: row?.remark ?? '',
  })
  editVisible.value = true
}

const save = async () => {
  if (!form.machineNo.trim()) {
    ElMessage.warning('机位号不能为空')
    return
  }
  saving.value = true
  try {
    const res: any = await upsertDialysisMachine({
      id: form.id ?? undefined,
      machineNo: form.machineNo.trim(),
      roomName: form.roomName || undefined,
      status: form.status,
      remark: form.remark || undefined,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '保存成功')
      editVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadDicts()
  loadList()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="机位号">
            <el-input v-model="query.machineNo" placeholder="机位号" clearable class="w-36" data-testid="hd-mc-no" />
          </el-form-item>
          <el-form-item label="分区">
            <el-input v-model="query.roomName" placeholder="分区" clearable class="w-36" data-testid="hd-mc-room" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="状态" clearable class="w-28" :fit-input-width="false">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="hd-mc-search" @click="() => { query.pageNum = 1; loadList() }">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'medtech:dialysis:add'" type="primary" :icon="Plus" data-testid="hd-mc-add-btn"
                     @click="openEdit()">新增机位</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" stripe :max-height="tableMaxHeight" data-testid="hd-machine-table">
        <el-table-column prop="machineNo" label="机位号" width="140" />
        <el-table-column prop="roomName" label="分区" min-width="160">
          <template #default="{ row }">{{ row.roomName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="machineStatusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sessionTotal" label="累计例次" width="110" align="right" />
        <el-table-column prop="remark" label="备注" min-width="180">
          <template #default="{ row }">{{ row.remark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'medtech:dialysis:add'" link type="primary" size="small"
                       data-testid="hd-mc-edit-btn" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :total="total" :page-sizes="PAGE_SIZES" layout="total, sizes, prev, pager, next"
                       @current-change="loadList" @size-change="() => { query.pageNum = 1; loadList() }" />
      </div>
    </el-card>

    <el-dialog v-model="editVisible" :title="form.id ? '编辑机位' : '新增机位'" width="520px"
               data-testid="hd-machine-dialog">
      <el-form label-width="90px">
        <el-form-item label="机位号" required>
          <el-input v-model="form.machineNo" placeholder="如 HD-09" data-testid="hd-mc-form-no" />
        </el-form-item>
        <el-form-item label="分区">
          <el-input v-model="form.roomName" placeholder="如 第二透析室" data-testid="hd-mc-form-room" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" class="w-40" :fit-input-width="false" data-testid="hd-mc-form-status">
            <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" data-testid="hd-mc-form-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="hd-mc-ok" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
