<template>
  <div class="space-y-3">
    <div class="his-card p-3 flex items-center justify-between">
      <div class="query-grid">
        <div class="q-item"><el-input v-model="q.keyword" placeholder="姓名/参保地/就医地" clearable @keyup.enter="search" /></div>
        <div class="q-item">
          <el-select v-model="q.status" placeholder="备案状态" clearable style="width:100%">
            <el-option label="已备案" value="已备案" /><el-option label="审核中" value="审核中" /><el-option label="已失效" value="已失效" />
          </el-select>
        </div>
      </div>
      <div class="flex gap-2">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" @click="openAdd">新增备案</el-button>
      </div>
    </div>

    <div class="his-card p-3">
      <el-table :data="list" size="small" v-loading="loading" :max-height="460">
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="idCardMask" label="身份证号" width="160" />
        <el-table-column prop="insuranceArea" label="参保地" width="110" />
        <el-table-column prop="hospital" label="就医医院" width="150" />
        <el-table-column prop="reason" label="备案原因" width="120" />
        <el-table-column prop="fromArea" label="就医地" width="110" />
        <el-table-column prop="validStart" label="生效" width="100" />
        <el-table-column prop="validEnd" label="失效" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status === '已失效'" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :disabled="row.status === '已失效'" @click="revoke(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-2">
        <el-pagination :page-sizes="PAGE_SIZES" :page-size="q.size" :current-page="q.page" :total="total"
          layout="total, sizes, prev, pager, next" @current-change="(p)=>{q.page=p;load()}" @size-change="(s)=>{q.size=s;q.page=1;load()}" />
      </div>
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑备案' : '新增异地就医备案'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.name" placeholder="患者姓名" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.idCardMask" placeholder="用于核验参保信息" /></el-form-item>
        <el-form-item label="参保地"><el-input v-model="form.insuranceArea" placeholder="如：长沙市" /></el-form-item>
        <el-form-item label="就医医院"><el-input v-model="form.hospital" placeholder="异地就医定点医院" /></el-form-item>
        <el-form-item label="就医地"><el-input v-model="form.fromArea" placeholder="如：北京市" /></el-form-item>
        <el-form-item label="备案原因">
          <el-select v-model="form.reason" style="width:100%">
            <el-option label="异地长期居住" value="异地长期居住" />
            <el-option label="异地转诊就医" value="异地转诊就医" />
            <el-option label="常驻异地工作" value="常驻异地工作" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="validRange" type="daterange" range-separator="至" start-placeholder="生效日" end-placeholder="失效日" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交备案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFilings, upsertFiling, revokeFiling } from '@/api/insurance'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ keyword: '', status: '', page: 1, size: 10 })

const visible = ref(false)
const validRange = ref([])
const form = reactive({ id: null, name: '', idCardMask: '', insuranceArea: '', hospital: '', fromArea: '', reason: '异地转诊就医', validStart: '', validEnd: '' })

function statusType(s) {
  return { 已备案: 'success', 审核中: 'warning', 已失效: 'info' }[s] || 'info'
}
async function load() {
  loading.value = true
  const res = await listFilings({ ...q })
  loading.value = false
  list.value = res.data.list
  total.value = res.data.total
}
function search() { q.page = 1; load() }
function reset() { q.keyword = q.status = ''; q.page = 1; load() }
function openAdd() {
  Object.assign(form, { id: null, name: '', idCardMask: '', insuranceArea: '', hospital: '', fromArea: '', reason: '异地转诊就医', validStart: '', validEnd: '' })
  validRange.value = []
  visible.value = true
}
function openEdit(row) {
  Object.assign(form, { ...row })
  validRange.value = [row.validStart, row.validEnd]
  visible.value = true
}
async function submit() {
  if (validRange.value?.length === 2) {
    form.validStart = validRange.value[0]
    form.validEnd = validRange.value[1]
  }
  await upsertFiling({ ...form })
  ElMessage.success(form.id ? '已更新备案' : '备案已提交，等待审核')
  visible.value = false
  load()
}
async function revoke(row) {
  await ElMessageBox.confirm(`确认撤销 ${row.name} 的异地就医备案？`, '撤销备案', { type: 'warning' })
  await revokeFiling(row.id)
  ElMessage.success('已撤销')
  load()
}
onMounted(load)
</script>
