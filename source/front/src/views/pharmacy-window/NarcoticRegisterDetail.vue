<template>
  <el-drawer v-model="visible" data-testid="narco-detail" size="46%" title="麻精专册登记详情">
    <div v-if="detail" class="space-y-3 text-sm">
      <div class="rounded border border-slate-200 p-3">
        <div class="mb-2 font-medium text-slate-700">登记信息</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="登记号">{{ detail.registerNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="发药时间">{{ detail.dispenseTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处方号">{{ detail.prescriptionNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="发药单号">{{ detail.dispensingNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="开方科室">{{ detail.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="临床诊断">{{ detail.diagnosis || '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="rounded border border-slate-200 p-3">
        <div class="mb-2 font-medium text-slate-700">患者（麻精需实名）</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="姓名">{{ detail.patientName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="患者号">{{ detail.patientNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ patientGenderText(detail.gender) }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ detail.age ?? '—' }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="身份证号">{{ detail.idCard || '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="rounded border border-slate-200 p-3">
        <div class="mb-2 font-medium text-slate-700">药品与限量</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="管制分类">
            <el-tag :type="flagTag(detail.specialFlag)" effect="dark" size="small">{{
                flagText(detail.specialFlag)
              }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="剂型">{{ detail.dosageForm || '—' }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="药品">{{ detail.drugName || '—' }} {{
              detail.specification || ''
            }}
          </el-descriptions-item>
          <el-descriptions-item label="数量">{{ detail.quantity ?? '—' }} {{
              detail.unit || ''
            }}
          </el-descriptions-item>
          <el-descriptions-item label="批号">{{ detail.batchNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="法定限量">{{
              detail.limitDays ? detail.limitDays + ' 日常用量' : '不限'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="处方天数">{{ detail.duration ?? '—' }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="核定日用量">{{ detail.dailyDosage ?? '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="rounded border border-slate-200 p-3">
        <div class="mb-2 font-medium text-slate-700">签名（双人复核）</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="开方医师">{{ detail.doctorName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审方药师">{{ detail.auditBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="发药人">{{ detail.dispenseBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="复核人">{{ detail.checkerName || '—' }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="复核时间">{{ detail.checkTime || '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="rounded border border-slate-200 p-3">
        <div class="mb-2 font-medium text-slate-700">空安瓿回收 / 剩余液销毁</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="回收状态">
            <el-tag :type="ampouleStatusTagType(detail.ampouleStatus)" size="small">
              {{ ampouleStatusText(detail.ampouleStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发出安瓿数">{{ detail.ampouleIssued ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="回收空安瓿数">{{ detail.ampouleReturned ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="剩余液销毁量">{{ detail.ampouleDestroyed ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="登记人">{{ detail.returnBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="登记时间">{{ detail.returnTime || '—' }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="说明">{{ detail.returnRemark || '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="detail.remark" class="rounded border border-amber-200 bg-amber-50 p-3 text-amber-700">
        {{ detail.remark }}
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
/**
 * 麻精专册登记详情（专册全字段留痕）—— 从 views/pharmacy-window/NarcoticRegisterView.vue 拆出
 *
 * 专册是证据：本页只摆列表已带出的那一笔登记全字段（后端没有按 id 单查专册的接口），
 * 一律只读，不提供任何编辑入口。分类文案与列表列同源（页面加载字典后按 prop 传下）。
 */
import {computed} from 'vue';
import {ampouleStatusTagType, ampouleStatusText} from '@/lib/drugSpecialFlag';
import {patientGenderText} from '@/lib/patientGender';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 专册登记行 */
  row: {type: Object, default: null},
  /** 管制分类文案/配色（列表列同源） */
  flagText: {type: Function, required: true},
  flagTag: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detail = computed(() => props.row);
</script>
