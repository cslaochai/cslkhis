<template>
  <!-- 详情弹窗（只读；组件自己按 id 取数，类型/状态文案走页面传入的列表字典） -->
  <el-dialog v-model="visible" :title="`借阅/复印单 ${detail?.borrowNo || ''}`" data-testid="ab-detail-dialog"
             width="640px">
    <template v-if="detail">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="类型">
          <el-tag :type="typeTagType(detail.borrowType)">{{ typeText(detail.borrowType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          <el-tag v-if="isOverdue(detail)" class="ml-1" size="small" type="danger">超期</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="病历号">{{ detail.recordNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detail.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="应还日期">{{ detail.expectReturnDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicantName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{
            (detail.createTime || '').replace('T', ' ').slice(0, 19)
          }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="用途">{{ detail.purpose }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="审核">
          <template v-if="detail.auditTime">
            {{ detail.auditByName }} · {{
              (detail.auditTime || '').replace('T', ' ').slice(0, 19)
            }}<br/>{{ detail.auditRemark || '（无意见）' }}
          </template>
          <template v-else>—</template>
        </el-descriptions-item>
        <el-descriptions-item label="借出时间">
          {{ detail.lendTime ? (detail.lendTime || '').replace('T', ' ').slice(0, 19) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="归还时间">
          {{ detail.returnTime ? (detail.returnTime || '').replace('T', ' ').slice(0, 19) : '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getBorrowDetail} from '@/api/archiveBorrow';
import {dictLabelText} from '@/lib/utils';
import {isOverdue, statusTagType, typeTagType} from '@/lib/archiveBorrow';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 类型/状态字典由主页面加载一次（列表列共用同一份），子组件不许再拉一遍
  typeDict: {type: Array, default: () => []},
  statusDict: {type: Array, default: () => []},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const typeText = (v) => dictLabelText(props.typeDict, v);
const statusText = (v) => dictLabelText(props.statusDict, v);

const detail = ref(null);
const loadDetail = async () => {
  try {
    const res = await getBorrowDetail(props.id);
    if (res.code === 200) {
      detail.value = res.data;
    } else {
      ElMessage.error(res.message || '查询详情失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  }
};
watch(visible, (open) => {
  if (!open)
    return;
  detail.value = null;
  loadDetail();
});
</script>
