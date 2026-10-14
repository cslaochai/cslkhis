<template>
  <div class="space-y-3">
    <div class="grid grid-cols-2 md:grid-cols-5 gap-3">
      <div class="his-card p-3 text-center"><div class="text-[12px] text-gray-500">目录总数</div><div class="amount-strong">{{ stats.total }}</div></div>
      <div class="his-card p-3 text-center"><div class="text-[12px] text-gray-500">甲类</div><div class="amount-strong !text-[#16A34A]">{{ stats.byClass.甲 || 0 }}</div></div>
      <div class="his-card p-3 text-center"><div class="text-[12px] text-gray-500">乙类</div><div class="amount-strong !text-[#D97706]">{{ stats.byClass.乙 || 0 }}</div></div>
      <div class="his-card p-3 text-center"><div class="text-[12px] text-gray-500">丙类</div><div class="amount-strong !text-[#DC2626]">{{ stats.byClass.丙 || 0 }}</div></div>
      <div class="his-card p-3 text-center"><div class="text-[12px] text-gray-500">已停用</div><div class="amount-strong">{{ stats.disabled || 0 }}</div></div>
    </div>

    <el-tabs v-model="tab" class="his-card p-3">
      <!-- 三大目录 -->
      <el-tab-pane label="医保三大目录" name="catalog">
        <div class="query-grid-wrap mb-3">
          <div class="query-grid">
            <div class="q-item"><el-input v-model="cq.keyword" placeholder="名称/编码" clearable /></div>
            <div class="q-item">
              <el-select v-model="cq.category" placeholder="目录类别" clearable style="width:100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </div>
            <div class="q-item">
              <el-select v-model="cq.payClass" placeholder="支付类别" clearable style="width:100%">
                <el-option label="甲类" value="甲" /><el-option label="乙类" value="乙" /><el-option label="丙类" value="丙" />
              </el-select>
            </div>
          </div>
          <div class="query-grid-ops">
            <el-button type="primary" @click="searchCatalog">查询</el-button>
            <el-button @click="resetCatalog">重置</el-button>
          </div>
        </div>
        <el-table :data="catalogList" size="small" v-loading="catLoading" :max-height="440">
          <el-table-column prop="ybCode" label="医保编码" width="150" />
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column prop="spec" label="规格" width="110" />
          <el-table-column prop="unit" label="单位" width="70" />
          <el-table-column prop="payClass" label="支付类别" width="80" align="center">
            <template #default="{ row }"><el-tag size="small" :type="payClassType(row.payClass)">{{ row.payClass }}类</el-tag></template>
          </el-table-column>
          <el-table-column label="限价" align="right" width="90">
            <template #default="{ row }">{{ fmt(row.limitPrice) }}</template>
          </el-table-column>
          <el-table-column label="自付比例" align="right" width="90">
            <template #default="{ row }">{{ (row.selfRatio * 100).toFixed(0) }}%</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }"><el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
        </el-table>
        <div class="flex justify-end mt-2">
          <el-pagination :page-sizes="PAGE_SIZES" :page-size="cq.size" :current-page="cq.page" :total="catTotal"
            layout="total, sizes, prev, pager, next" @current-change="(p)=>{cq.page=p;loadCatalog()}" @size-change="(s)=>{cq.size=s;cq.page=1;loadCatalog()}" />
        </div>
      </el-tab-pane>

      <!-- 院内↔医保对照 -->
      <el-tab-pane label="院内项目对照" name="mapping">
        <div class="flex items-center justify-between mb-3">
          <div class="query-grid">
            <div class="q-item"><el-input v-model="mq.keyword" placeholder="院内项目名" clearable @keyup.enter="searchMapping" /></div>
            <div class="q-item">
              <el-select v-model="mq.mapped" placeholder="对照状态" clearable style="width:100%">
                <el-option label="已对照" :value="true" /><el-option label="未对照" :value="false" />
              </el-select>
            </div>
          </div>
          <el-button type="success" @click="runAutoMatch" :loading="matching">一键自动对照</el-button>
        </div>
        <el-table :data="mapList" size="small" v-loading="mapLoading" :max-height="440">
          <el-table-column prop="hospItemCode" label="院内编码" width="110" />
          <el-table-column prop="hospItemName" label="院内项目" min-width="150" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column label="对照状态" width="90">
            <template #default="{ row }"><el-tag size="small" :type="row.mapped ? 'success' : 'info'">{{ row.mapped ? '已对照' : '未对照' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="ybCode" label="医保编码" width="150" />
          <el-table-column prop="ybName" label="医保名称" min-width="140" />
          <el-table-column prop="payClass" label="支付类别" width="80" align="center">
            <template #default="{ row }"><span v-if="row.payClass"><el-tag size="small" :type="payClassType(row.payClass)">{{ row.payClass }}类</el-tag></span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openMap(row)">对照</el-button>
              <el-button link type="danger" :disabled="!row.mapped" @click="unmap(row)">解对照</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="flex justify-end mt-2">
          <el-pagination :page-sizes="PAGE_SIZES" :page-size="mq.size" :current-page="mq.page" :total="mapTotal"
            layout="total, sizes, prev, pager, next" @current-change="(p)=>{mq.page=p;loadMapping()}" @size-change="(s)=>{mq.size=s;mq.page=1;loadMapping()}" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 对照弹框 -->
    <el-dialog v-model="mapVisible" title="院内项目 ↔ 医保目录对照" width="520px">
      <el-form label-width="90px" v-if="mapForm.id">
        <el-form-item label="院内项目"><el-input :model-value="mapForm.hospItemName" disabled /></el-form-item>
        <el-form-item label="医保编码"><el-input v-model="mapForm.ybCode" placeholder="如 X0001..." /></el-form-item>
        <el-form-item label="医保名称"><el-input v-model="mapForm.ybName" placeholder="对应医保目录名称" /></el-form-item>
        <el-form-item label="支付类别">
          <el-select v-model="mapForm.payClass" style="width:100%">
            <el-option label="甲类" value="甲" /><el-option label="乙类" value="乙" /><el-option label="丙类" value="丙" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mapVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMap">保存对照</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listCatalog, catalogStats, listMappings, mapItem, unmapItem, autoMatch } from '@/api/insurance'
import { CATALOG_CATEGORIES } from '@/lib/dict'
import { PAGE_SIZES } from '@/lib/pagination'

const categories = CATALOG_CATEGORIES
const tab = ref('catalog')

// 目录
const stats = reactive({ total: 0, byClass: {}, disabled: 0 })
const catalogList = ref([])
const catTotal = ref(0)
const catLoading = ref(false)
const cq = reactive({ keyword: '', category: '', payClass: '', page: 1, size: 10 })

// 对照
const mapList = ref([])
const mapTotal = ref(0)
const mapLoading = ref(false)
const matching = ref(false)
const mq = reactive({ keyword: '', mapped: '', page: 1, size: 10 })
const mapVisible = ref(false)
const mapForm = reactive({})

function fmt(n) {
  return (n ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function payClassType(c) {
  return { 甲: 'success', 乙: 'warning', 丙: 'danger' }[c] || 'info'
}
async function loadCatalog() {
  catLoading.value = true
  const res = await listCatalog({ ...cq })
  catLoading.value = false
  catalogList.value = res.data.list
  catTotal.value = res.data.total
}
async function loadStats() {
  const res = await catalogStats()
  Object.assign(stats, res.data)
}
function searchCatalog() { cq.page = 1; loadCatalog() }
function resetCatalog() { cq.keyword = cq.category = cq.payClass = ''; cq.page = 1; loadCatalog() }

async function loadMapping() {
  mapLoading.value = true
  const res = await listMappings({ ...mq })
  mapLoading.value = false
  mapList.value = res.data.list
  mapTotal.value = res.data.total
}
function searchMapping() { mq.page = 1; loadMapping() }
function openMap(row) {
  Object.assign(mapForm, { id: row.id, hospItemName: row.hospItemName, ybCode: row.ybCode, ybName: row.ybName, payClass: row.payClass })
  mapVisible.value = true
}
async function submitMap() {
  await mapItem({ ...mapForm })
  ElMessage.success('对照已保存')
  mapVisible.value = false
  loadMapping()
}
async function unmap(row) {
  await unmapItem(row.id)
  ElMessage.success('已解对照')
  loadMapping()
}
async function runAutoMatch() {
  matching.value = true
  const res = await autoMatch()
  matching.value = false
  ElMessage.success(`自动对照完成，新增 ${res.data.matched} 条`)
  loadMapping()
}
onMounted(() => { loadStats(); loadCatalog(); loadMapping() })
</script>
