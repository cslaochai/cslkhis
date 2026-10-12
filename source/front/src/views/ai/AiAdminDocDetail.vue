<template>
  <el-dialog v-model="visible" :title="detail?.title || '文档详情'" width="640px">
    <el-form disabled label-width="80px">
      <el-form-item label="分类">{{ detail?.category || '—' }}</el-form-item>
      <el-form-item label="来源">{{ sourceTypeText(detail?.sourceType) }} · 切块 {{
          detail?.chunkCount ?? 0
        }}
      </el-form-item>
      <el-form-item label="原文">
        <div class="doc-content">{{ detail?.content }}</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 知识文档详情弹框（自 AiAdminView 拆出，只读）
 *
 * 打开时按 id 自取详情（与拆分前 showDetail 的取数口径一致）；
 * 来源文案走 lib/ai 的共用口径，与列表列同一个出口。
 */
import {computed, ref, watch} from 'vue';
import {getKnowledgeDocById} from '@/api/ai';
import {sourceTypeText} from '@/lib/ai';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 文档ID（雪花ID，务必传字符串，避免精度丢失）
  id: {type: [String, Number], default: null},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = ref(null);

// 打开时按 id 自取详情（与拆分前 showDetail 的取数口径一致）
watch(() => props.modelValue, async (v) => {
  if (!v) return;
  detail.value = null;
  if (!props.id) return;
  try {
    const res = await getKnowledgeDocById({id: props.id});
    detail.value = res?.data || null;
  } catch (e) {
    console.error('加载知识文档详情失败', e);
  }
});
</script>

<style scoped>
.doc-content {
  white-space: pre-wrap;
  font-size: 13px;
  max-height: 320px;
  overflow-y: auto;
}
</style>
