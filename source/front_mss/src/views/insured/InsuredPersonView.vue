<template>
  <div class="space-y-3">
    <div class="his-card p-3">
      <div class="query-grid-wrap">
        <div class="query-grid">
          <div class="q-item">
            <el-input v-model="q.keyword" placeholder="姓名/身份证/医保卡号" clearable @keyup.enter="search" />
          </div>
          <div class="q-item">
            <el-select v-model="q.insuranceType" placeholder="险种" clearable style="width:100%">
              <el-option label="城镇职工医保" value="2" />
              <el-option label="城乡居民医保" value="3" />
            </el-select>
          </div>
        </div>
        <div class="query-grid-ops">
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="primary" plain @click="openAdd">新增参保人</el-button>
        </div>
      </div>
    </div>

    <div class="his-card p-3">
      <div class="font-semibold text-[15px] text-[#0B3B66] mb-2">参保人（医保侧参保库）</div>
      <el-table :data="list" size="small" v-loading="loading">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="insuranceNo" label="医保卡号" width="130" />
        <el-table-column prop="gender" label="性别" width="70" align="center" />
        <el-table-column label="险种" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.insuranceType === '2' ? 'primary' : 'success'">
              {{ row.insuranceType === '2' ? '城镇职工' : row.insuranceType === '3' ? '城乡居民' : row.insuranceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="参保状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.personStatus === 1 ? 'success' : 'info'">
              {{ row.personStatus === 1 ? '正常参保' : '停保' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="个人账户余额" width="130" align="right">
          <template #default="{ row }">{{ fmt(row.accountBalance) }}</template>
        </el-table-column>
        <el-table-column prop="region" label="参保地" width="110" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>
      <div class="flex justify-end mt-3">
        <el-pagination
          :page-sizes="PAGE_SIZES" :page-size="q.size" :current-page="q.page" :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="(p) => { q.page = p; load() }"
          @size-change="(s) => { q.size = s; q.page = 1; load() }"
        />
      </div>
    </div>

    <!-- 新增/同步参保人 -->
    <el-dialog v-model="addVisible" title="新增参保人" width="480px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="18 位身份证号" />
        </el-form-item>
        <el-form-item label="医保卡号">
          <el-input v-model="form.insuranceNo" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="险种">
          <el-select v-model="form.insuranceType" style="width:100%">
            <el-option label="城镇职工医保" value="2" />
            <el-option label="城乡居民医保" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="参保状态">
          <el-select v-model="form.personStatus" style="width:100%">
            <el-option label="正常参保" :value="1" />
            <el-option label="停保" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="个人账户余额">
          <el-input-number v-model="form.accountBalance" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="参保地">
          <el-input v-model="form.region" placeholder="默认长沙市" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { personListPage, personSave } from '@/api/insurance'
import { PAGE_SIZES } from '@/lib/pagination'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ keyword: '', insuranceType: '', page: 1, size: 10 })

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function load() {
  loading.value = true
  const params = { pageNum: q.page, pageSize: q.size, keyword: q.keyword || undefined, insuranceType: q.insuranceType || undefined }
  const res = await personListPage(params)
  loading.value = false
  list.value = res.data.records || []
  total.value = res.data.total || 0
}
function search() { q.page = 1; load() }
function reset() { q.keyword = ''; q.insuranceType = ''; q.page = 1; load() }

// 新增
const addVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  name: '', idCard: '', insuranceNo: '', gender: '男', insuranceType: '2',
  personStatus: 1, accountBalance: 0, region: '长沙市',
})
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
}
function openAdd() {
  Object.assign(form, { name: '', idCard: '', insuranceNo: '', gender: '男', insuranceType: '2', personStatus: 1, accountBalance: 0, region: '长沙市' })
  addVisible.value = true
}
async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await personSave({ ...form })
    ElMessage.success('保存成功')
    addVisible.value = false
    search()
  } catch (e) {
    // 后端 message 已全局提示
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
