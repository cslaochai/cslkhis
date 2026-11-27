<template>
  <div class="operation-qa">
    <el-tabs v-model="activeTab">
      <!-- ============ AI 运营问数 ============ -->
      <el-tab-pane label="AI运营问数" name="ask">
        <el-card class="qa-ask-card" shadow="never">
          <div class="qa-input-row">
            <el-input
                v-model="question"
                clearable
                maxlength="200"
                placeholder="例如：最近7天每天的发药处方数量和金额"
                @keyup.enter="ask()"
            />
            <el-checkbox v-model="withSummary" class="qa-summary-check">生成结论</el-checkbox>
            <el-button v-perm="'ai:operationQa:ask'" :loading="loading" type="primary" @click="ask()">查询</el-button>
          </div>
          <div class="qa-chips">
            <el-tag
                v-for="q in quickQuestions"
                :key="q"
                class="qa-chip"
                effect="plain"
                type="info"
                @click="ask(q)"
            >{{ q }}
            </el-tag>
          </div>
        </el-card>

        <el-alert
            v-if="result?.degraded"
            :closable="false"
            :description="result.degradeReason"
            class="qa-degraded"
            show-icon
            title="本次未生成或未执行查询"
            type="warning"
        />

        <el-card v-if="result" class="qa-result-card" shadow="never">
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

          <el-table v-loading="loading" :data="tableRows" border class="qa-table" empty-text="没有查到符合条件的数据"
                    stripe>
            <el-table-column
                v-for="col in result.columns"
                :key="col.key"
                :label="col.label"
                :min-width="140"
                :prop="col.key"
            />
          </el-table>

          <el-collapse class="qa-sql-collapse">
            <el-collapse-item name="sql">
              <template #title>查看实际执行的 SQL</template>
              <pre class="qa-sql">{{ result.sql || '（未生成 SQL）' }}</pre>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {askOperationQa} from '@/api/ai';

const activeTab = ref('ask');
const question = ref('');
const withSummary = ref(true);
const loading = ref(false);
const result = ref(null);
const quickQuestions = [
  '最近7天每天的发药处方数量和金额',
  '本月各支付方式的净收款金额',
  '最近30天各科室的门诊就诊量',
  '本月检验记录数量按申请科室统计',
];
const tableRows = computed(() => {
  const res = result.value;
  if (!res || !res.rows?.length)
    return [];
  return res.rows.map((row) => {
    const item = {};
    row.cells.forEach((v, i) => {
      item[res.columns[i].key] = v;
    });
    return item;
  });
});
const ask = async (q) => {
  const text = (q ?? question.value).trim();
  if (!text) {
    ElMessage.warning('请输入要问的问题');
    return;
  }
  question.value = text;
  loading.value = true;
  result.value = null;
  try {
    const res = await askOperationQa({question: text, withSummary: withSummary.value});
    result.value = res?.data || null;
  } catch (e) {
    // 降级与业务拒绝都走 200 + degraded；走到这里的是网络或权限问题
    console.error('运营问数请求失败', e);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.operation-qa {
  padding: 16px;
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
</style>
