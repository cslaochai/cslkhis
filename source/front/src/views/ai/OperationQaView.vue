<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { askOperationQa, getOperationSchema } from '@/api/ai'

const question = ref('')
const withSummary = ref(true)
const loading = ref(false)
const result = ref<any>(null)
const schemaList = ref<any[]>([])

const quickQuestions = [
  '最近7天每天的发药处方数量和金额',
  '本月各支付方式的净收款金额',
  '最近30天各科室的门诊就诊量',
  '本月检验记录数量按申请科室统计',
]

const tableRows = computed(() => {
  const res = result.value
  if (!res || !res.rows?.length) return []
  return res.rows.map((row: any) => {
    const item: Record<string, any> = {}
    row.cells.forEach((v: any, i: number) => {
      item[res.columns[i].key] = v
    })
    return item
  })
})

const ask = async (q?: string) => {
  const text = (q ?? question.value).trim()
  if (!text) {
    ElMessage.warning('请输入要问的问题')
    return
  }
  question.value = text
  loading.value = true
  result.value = null
  try {
    const res: any = await askOperationQa({ question: text, withSummary: withSummary.value })
    result.value = res?.data || null
  } catch (e: any) {
    // 降级与业务拒绝都走 200 + degraded；走到这里的是网络或权限问题
    console.error('运营问数请求失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const res: any = await getOperationSchema()
    schemaList.value = res?.data || []
  } catch (e) {
    console.error('加载数据域失败', e)
  }
})
</script>

<template>
  <div class="operation-qa">
    <div class="qa-header">
      <div>
        <h2 class="qa-title">AI 运营问数</h2>
        <p class="qa-sub">用自然语言问经营数据：只读查询，结果由受控 SQL 从白名单经营表算出，模型不接触患者临床信息。</p>
      </div>
    </div>

    <el-card shadow="never" class="qa-ask-card">
      <div class="qa-input-row">
        <el-input
          v-model="question"
          placeholder="例如：最近7天每天的发药处方数量和金额"
          maxlength="200"
          clearable
          @keyup.enter="ask()"
        />
        <el-checkbox v-model="withSummary" class="qa-summary-check">生成结论</el-checkbox>
        <el-button type="primary" :loading="loading" v-perm="'ai:operationQa:ask'" @click="ask()">查询</el-button>
      </div>
      <div class="qa-chips">
        <el-tag
          v-for="q in quickQuestions"
          :key="q"
          class="qa-chip"
          type="info"
          effect="plain"
          @click="ask(q)"
        >{{ q }}</el-tag>
      </div>
    </el-card>

    <el-alert
      v-if="result?.degraded"
      type="warning"
      :closable="false"
      show-icon
      class="qa-degraded"
      title="本次未生成或未执行查询"
      :description="result.degradeReason"
    />

    <el-card v-if="result" shadow="never" class="qa-result-card">
      <template #header>
        <div class="qa-result-head">
          <span class="qa-result-title">{{ result.title || result.question }}</span>
          <span class="qa-result-meta">
            {{ result.rowCount }} 行<template v-if="result.truncated">（超过 100 行已截断）</template>
            · {{ result.elapsedMs }} ms
          </span>
        </div>
      </template>

      <p v-if="result.summary" class="qa-summary">{{ result.summary }}</p>

      <el-table :data="tableRows" v-loading="loading" border stripe class="qa-table" empty-text="没有查到符合条件的数据">
        <el-table-column
          v-for="col in result.columns"
          :key="col.key"
          :prop="col.key"
          :label="col.label"
          :min-width="140"
        />
      </el-table>

      <el-collapse class="qa-sql-collapse">
        <el-collapse-item name="sql">
          <template #title>查看实际执行的 SQL（透明可查）</template>
          <pre class="qa-sql">{{ result.sql || '（未生成 SQL）' }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card shadow="never" class="qa-schema-card">
      <template #header>可查询的数据域</template>
      <div class="qa-schema-list">
        <div v-for="item in schemaList" :key="item.tableName" class="qa-schema-item">
          <span class="qa-schema-usage">{{ item.description }}</span>
          <span class="qa-schema-table">{{ item.tableName }}</span>
        </div>
      </div>
      <p class="qa-schema-note">仅支持只读统计查询；个体患者的临床信息不在可查询范围内。</p>
    </el-card>
  </div>
</template>

<style scoped>
.operation-qa {
  padding: 16px;
}
.qa-header {
  margin-bottom: 12px;
}
.qa-title {
  margin: 0;
  font-size: 18px;
}
.qa-sub {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}
.qa-ask-card {
  margin-bottom: 12px;
}
.qa-input-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.qa-input-row .el-input {
  flex: 1;
}
.qa-summary-check {
  white-space: nowrap;
}
.qa-chips {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.qa-chip {
  cursor: pointer;
}
.qa-degraded {
  margin-bottom: 12px;
}
.qa-result-card {
  margin-bottom: 12px;
}
.qa-result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.qa-result-title {
  font-weight: 600;
}
.qa-result-meta {
  color: #6b7280;
  font-size: 12px;
  white-space: nowrap;
}
.qa-summary {
  margin: 0 0 12px;
  padding: 10px 12px;
  background: #f0f7ff;
  border-left: 3px solid #1269B5;
  border-radius: 4px;
  font-size: 14px;
}
.qa-table {
  width: 100%;
}
.qa-sql-collapse {
  margin-top: 12px;
  border-top: none;
}
.qa-sql {
  margin: 0;
  padding: 10px;
  background: #f6f8fa;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
.qa-schema-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 8px 20px;
}
.qa-schema-item {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  padding: 4px 0;
  border-bottom: 1px dashed #e5e7eb;
}
.qa-schema-usage {
  color: #374151;
}
.qa-schema-table {
  color: #9ca3af;
  font-family: monospace;
  font-size: 12px;
}
.qa-schema-note {
  margin: 10px 0 0;
  color: #9ca3af;
  font-size: 12px;
}
</style>
