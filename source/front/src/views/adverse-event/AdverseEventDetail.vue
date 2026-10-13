<template>
  <el-dialog v-model="visible" :title="`不良事件 ${detail?.eventNo || ''}`" data-testid="ae-detail-dialog" width="680px">
    <template v-if="detail">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="事件类型">{{ typeText(detail.eventType) }}</el-descriptions-item>
        <el-descriptions-item label="事件等级">
          <el-tag :type="levelTagType(detail.eventLevel)">{{ levelText(detail.eventLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发生科室">{{ detail.occurDeptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="发生病区">{{ detail.occurWardName || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="Number(detail.eventType) === EVENT_TYPE_PRESSURE_INJURY" label="压疮来源">
          {{ acquiredText(detail.acquiredFlag) || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ (detail.occurTime || '').replace('T', ' ') }}</el-descriptions-item>
        <el-descriptions-item label="关联患者">{{ detail.patientName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上报人">{{ detail.reporterName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="上报时间">{{
            (detail.reportTime || '').replace('T', ' ')
          }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="事件摘要">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="详细经过">{{ detail.description }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="即时处置">{{ detail.immediateAction || '—' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="处理（D）">
          <template v-if="detail.handleTime">
            {{ detail.handlerName }} · {{ (detail.handleTime || '').replace('T', ' ') }}<br/>{{ detail.handleRemark }}
          </template>
          <template v-else>—</template>
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="整改（C）">
          <template v-if="detail.rectifyTime">
            {{ detail.rectifyByName }} · {{
              (detail.rectifyTime || '').replace('T', ' ')
            }}<br/>{{ detail.rectifyMeasures }}
          </template>
          <template v-else>—</template>
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="结案（A）">
          <template v-if="detail.closeTime">
            {{ detail.closeByName }} · {{ (detail.closeTime || '').replace('T', ' ') }}<br/>{{ detail.verifyRemark }}
          </template>
          <template v-else>—</template>
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 不良事件详情弹框（自 AdverseEventView 拆出）
 *
 * 按 id 自取详情（打开时拉 getAdverseEventDetail，PDCA 流转记录只有详情接口有，
 * 列表行数据不够展示）；字典由页面统一加载后传入，与列表列同一口径。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getAdverseEventDetail} from '@/api/adverseEvent';
import {dictLabelText} from '@/lib/utils';
import {EVENT_TYPE_PRESSURE_INJURY, levelTagType, statusTagType} from '@/lib/adverseEvent';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 事件ID（雪花ID，务必传字符串，避免精度丢失）
  id: {type: [String, Number], default: null},
  // 事件类型字典（页面已加载）
  typeDict: {type: Array, default: () => []},
  // 事件等级字典（页面已加载）
  levelDict: {type: Array, default: () => []},
  // 状态字典（页面已加载）
  statusDict: {type: Array, default: () => []},
  // 压疮来源字典（页面已加载）
  acquiredDict: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const typeText = (v) => dictLabelText(props.typeDict, v);
const levelText = (v) => dictLabelText(props.levelDict, v);
const statusText = (v) => dictLabelText(props.statusDict, v);
const acquiredText = (v) => dictLabelText(props.acquiredDict, v);

const detail = ref(null);

// 打开时按 id 自取详情（与拆分前 openDetail 的取数口径一致）
watch(() => props.modelValue, async (v) => {
  if (!v) return;
  detail.value = null;
  if (!props.id) return;
  try {
    const res = await getAdverseEventDetail(props.id);
    if (res.code === 200) {
      detail.value = res.data;
    } 
  } catch (e) {
    console.error(e);
    
  }
});
</script>
