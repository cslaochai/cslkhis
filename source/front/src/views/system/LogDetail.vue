<template>
  <!-- 详情：只读（审计记录不给改），编辑没有入口 -->
  <el-dialog
      v-model="visible"
      :title="type === 'audit' ? '审计日志详情' : '操作日志详情'"
      width="720px"
  >
    <el-descriptions :column="2" border size="small">
      <template v-if="type === 'audit'">
        <el-descriptions-item label="时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ detail.operation }}</el-descriptions-item>
        <el-descriptions-item label="对象类型">{{ detail.targetType }}</el-descriptions-item>
        <el-descriptions-item label="对象ID">{{ detail.targetId }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ip }}</el-descriptions-item>
        <el-descriptions-item label="结果">{{ detail.statusText }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="内容">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" :span="2" label="失败原因">
          {{ detail.errorMsg }}
        </el-descriptions-item>
      </template>
      <template v-else>
        <el-descriptions-item label="时间">{{ detail.operTime }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detail.businessTypeText }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.operIp }}（{{ detail.operLocation }}）</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costTime }} ms</el-descriptions-item>
        <el-descriptions-item :span="2" label="URL">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="请求参数">
          <pre class="log-pre">{{ detail.operParam || '-' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="失败原因">{{ detail.errorMsg || '-' }}</el-descriptions-item>
      </template>
    </el-descriptions>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getAuditLogDetail, getOperLogDetail} from '@/api/log';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 日志主键：详情按 id 自取数（请求参数与后端口径不变）
  id: {type: [String, Number], default: null},
  // audit-审计日志 / oper-操作日志（由页面按当前页签判定）
  type: {type: String, default: 'oper'},
  // 列表当前行：只用于「打开瞬间不闪白」，随后由详情接口覆盖
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const detail = ref({});

watch(visible, async (open) => {
  if (!open) return;
  detail.value = props.row || {};
  if (!props.id) return;
  try {
    const res = props.type === 'audit' ? await getAuditLogDetail(props.id) : await getOperLogDetail(props.id);
    detail.value = res?.data || {};
  } catch (e) {
    ElMessage.error(e?.message || '加载日志详情失败');
  }
});
</script>

<style scoped>
.log-pre {
  margin: 0;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}
</style>
