<template>
  <div data-testid="antibiotic-auth-view">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="抗菌药物处方权"
      description="按《抗菌药物临床应用管理办法》，医师只有取得对应级别的处方权才能开该级别抗菌药：医师授权级别必须 ≥ 药品分级（非限制使用级 / 限制使用级 / 特殊使用级）才放行。授权有有效期，到期需复审；暂停/取消必须写明原因 —— 这张表是事后追溯「当时他到底有没有权限开」的唯一依据，只能另立记录，不能覆盖。" />

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="authQuery" inline @submit.prevent>
          <el-form-item label="关键字">
            <div data-testid="auth-keyword">
              <el-input
                v-model="authQuery.keyword"
                placeholder="医师姓名 / 科室"
                clearable
                style="width: 200px"
                @keyup.enter="loadAuth"
                @clear="loadAuth" />
            </div>
          </el-form-item>
          <el-form-item label="授权级别">
            <div data-testid="auth-level">
              <el-select v-model="authQuery.authLevel" placeholder="全部" clearable style="width: 150px" @change="loadAuth">
                <el-option v-for="code in [1, 2, 3]" :key="code" :label="ANTIBIOTIC_LEVEL[code]" :value="code" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="状态">
            <div data-testid="auth-status">
              <el-select v-model="authQuery.status" placeholder="全部" clearable style="width: 120px" @change="loadAuth">
                <el-option v-for="(text, code) in ANTIBIOTIC_AUTH_STATUS" :key="code" :label="text" :value="Number(code)" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="authQuery.onlyEffective" @change="loadAuth">只看当前可用</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-auth-query" @click="loadAuth">查询</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button
            v-perm="'pharmacy:antibiotic:authEdit'"
            type="primary"
            data-testid="btn-new-auth"
            @click="openAuthDialog()">授予处方权</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="authRows" stripe v-loading="authLoading" :max-height="tableMaxHeight" data-testid="auth-table">
      <el-table-column prop="authNo" label="授权编号" width="150" />
      <el-table-column prop="doctorName" label="医师" width="100" />
      <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column label="授权级别" width="130">
        <template #default="{ row }">
          <el-tag :type="levelTagType(row.authLevel)" data-testid="cell-auth-level">{{ antibioticLevelText(row.authLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="authBasis" label="授权依据" min-width="220" show-overflow-tooltip />
      <el-table-column label="有效期" width="190">
        <template #default="{ row }">{{ row.authDate }} ~ {{ row.expireDate }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="ANTIBIOTIC_STATUS_TAG[row.status]" data-testid="cell-auth-status">{{ authStatusText(row.status) }}</el-tag>
          <span v-if="row.status === 1 && !row.effective" class="expired">已过期</span>
        </template>
      </el-table-column>
      <el-table-column prop="authorizeOrg" label="授权部门" width="120" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button v-perm="'pharmacy:antibiotic:authEdit'" link type="primary" @click="openAuthDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          layout="total, sizes, prev, pager, next"
          :total="authTotal"
          :page-sizes="PAGE_SIZES"
          v-model:current-page="authQuery.pageNum"
          v-model:page-size="authQuery.pageSize"
          @current-change="loadAuth"
          @size-change="onAuthSizeChange" />
      </div>
    </el-card>

    <!-- ============ 授予/编辑处方权 ============ -->
    <AntibioticAuthEdit v-model="authDialogVisible" :row="authRow" @saved="loadAuth" />
  </div>
</template>

<script setup>
/**
 * 抗菌药物处方权（菜单 2922 / 路由 /antibiotic-auth，sql/178）
 *
 * 为什么从「抗菌药物分级目录」里拆出来单独挂菜单：
 * 分级目录管的实体是**药品**（一个药属哪一级），处方权管的实体是**医师**（一个医师能开到哪一级），
 * 两张表的主键维度不同、维护岗位也不同（目录由临床药师/药事委员会定，处方权由医务科按培训考核授予），
 * 而且授权有有效期、有暂停/取消（必须写原因）—— 这是**资质台账**，不是一个目录页的附属页签。
 * 内容与拆之前的「医师处方权授权」页签同源，接口与权限码（pharmacy:antibiotic:authEdit）均未变。
 */
import { onMounted, reactive, ref } from 'vue'
import {
  ANTIBIOTIC_AUTH_STATUS,
  ANTIBIOTIC_LEVEL,
  ANTIBIOTIC_STATUS_TAG,
  antibioticLevelText,
  authStatusText
} from '@/lib/antibiotic'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import { listAntibioticAuthPage } from '@/api/antibiotic'
import AntibioticAuthEdit from './AntibioticAuthEdit.vue'

const levelTagType = (level) => (level === 3 ? 'danger' : level === 2 ? 'warning' : level === 1 ? 'success' : 'info')

const authQuery = reactive({ keyword: '', authLevel: null, status: null, onlyEffective: false, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const authRows = ref([])
const authTotal = ref(0)
const authLoading = ref(false)

async function loadAuth() {
  authLoading.value = true
  try {
    const res = await listAntibioticAuthPage({ ...authQuery })
    authRows.value = res?.data?.records || []
    authTotal.value = Number(res?.data?.total || 0)
  } finally {
    authLoading.value = false
  }
}

function onAuthSizeChange() {
  authQuery.pageNum = 1
  loadAuth()
}

const authDialogVisible = ref(false)
const authRow = ref(null)

function openAuthDialog(row) {
  authRow.value = row || null
  authDialogVisible.value = true
}

onMounted(() => {
  loadAuth()
})
</script>

<style scoped>
.expired {
  margin-left: 6px;
  color: #e6a23c;
  font-size: 12px;
}
</style>
