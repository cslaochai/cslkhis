<template>
  <!-- ==================== 床位卡详情（图上点床） ==================== -->
  <el-dialog v-model="visible" :title="`床位 ${bed.bedNo || ''} · ${bed.bedStatusText || ''}`" width="520px">
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="所属">{{ bed.deptName || '—' }} · {{ bed.wardName || '—' }}
      </el-descriptions-item>
      <el-descriptions-item label="床型">{{ bed.bedTypeText || bed.bedType || '—' }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="bedStatusTag(bed.bedStatus)" size="small">{{ bed.bedStatusText }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="占用患者">
        {{ bed.patientName || '—' }}
        <span v-if="bed.admitTime" class="text-xs text-slate-400">（{{ bed.admitTime }} 入院）</span>
      </el-descriptions-item>
      <el-descriptions-item label="预留去向">
        <span v-if="bed.reservedPatientName">
          {{ bed.reservedPatientName }}
          <span class="text-xs text-slate-400">
            · {{ bed.reservedWaitNo || '无等待号' }} · {{ bed.reservedPriorityText || '' }}
          </span>
        </span>
        <span v-else class="text-xs text-slate-400">—</span>
      </el-descriptions-item>
      <el-descriptions-item label="调配方式">
        {{ bed.allocTypeText || '—' }}
        <el-tag v-if="bed.crossDept" class="ml-1" size="small" type="warning">跨科</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="预留时间">{{ bed.reserveTime || '—' }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ bed.remark || '—' }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button v-if="bed.canReserve" v-perm="'ipd:bedCenter:assign'" data-testid="bc-bed-reserve"
                 type="primary" @click="emit('reserve')">预留给等床患者
      </el-button>
      <el-button v-if="bed.canRelease" v-perm="'ipd:bedCenter:release'" data-testid="bc-bed-release"
                 type="warning" @click="emit('release')">释放该床
      </el-button>
      <el-button v-if="bed.canAdmit" v-perm="'ipd:bedCenter:admit'" data-testid="bc-bed-admit"
                 type="primary" @click="emit('admit')">办理入院
      </el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 床位一张图点床后的只读详情。床位数据来自图上床卡本身（无独立详情接口），
 * 预留 / 释放 / 办理入院三个后续动作由父页面接手（emit 回传），
 * 父页面点击时已把同一 bed 存进 bedDetail，预留弹框继续用它。
 */
import {computed} from 'vue';
import {bedStatusTag} from '@/lib/bedCenter';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 被点击的床位对象（床位一张图行数据）
  bed: {type: Object, default: () => ({})},
});

const emit = defineEmits(['update:modelValue', 'reserve', 'release', 'admit']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
</script>
