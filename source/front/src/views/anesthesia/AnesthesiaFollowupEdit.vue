<template>
  <el-dialog
      v-model="visible"
      :title="followupForm.id ? '修改随访草稿' : '新建麻醉随访'"
      data-testid="p134-fu-dialog"
      width="720px"
  >
    <el-form label-width="120px">
      <el-form-item label="麻醉记录" required>
        <el-select
            v-model="followupForm.recordId"
            :disabled="!!followupForm.id"
            class="!w-full"
            data-testid="p134-fu-record"
            filterable
            placeholder="选择已提交/已审核的麻醉记录"
        >
          <el-option
              v-for="t in followupTargets"
              :key="t.id"
              :label="`${t.recordNo || t.id} · ${t.patientName || ''}`"
              :value="t.id"
          />
        </el-select>
        <div v-if="!followupForm.id" class="mt-1 text-[11px] text-slate-400">
          轮次由服务端按该麻醉记录现有轮次 +1（第1轮术后即刻 / 第2轮24h / 第3轮48h…），前端不填
        </div>
      </el-form-item>
      <el-form-item label="随访时间" required>
        <el-date-picker
            v-model="followupForm.followupTime"
            class="!w-full"
            data-testid="p134-fu-time"
            placeholder="不得早于麻醉结束时间"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
        <div v-if="followupTarget?.anesthesiaEndTime" class="mt-1 text-[11px] text-slate-400">
          该次麻醉结束：{{ fmt(followupTarget.anesthesiaEndTime) }}
        </div>
      </el-form-item>
      <el-form-item label="疼痛评分 NRS">
        <el-input-number v-model="followupForm.painScore" :max="10" :min="0" data-testid="p134-fu-pain"/>
        <span class="ml-2 text-[11px] text-slate-400">0~10（完成随访时必填；≥4 建议写明处理）</span>
      </el-form-item>
      <el-form-item label="恢复情况">
        <el-radio-group v-model="followupForm.recovery" data-testid="p134-fu-recovery">
          <el-radio v-for="o in A.followupRecoveryOptions" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="并发症">
        <el-checkbox-group v-model="followupForm.adverse" data-testid="p134-fu-adverse">
          <el-checkbox v-for="o in A.followupAdverseOptions" :key="o.value" :value="o.value">{{
              o.label
            }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <template v-if="hasAdverse">
        <el-form-item label="并发症经过" required>
          <el-input
              v-model="followupForm.adverseNote"
              :rows="2"
              data-testid="p134-fu-note"
              placeholder="发生了什么（勾了却写不出经过，比不勾更糟）"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="处理与转归" required>
          <el-input
              v-model="followupForm.handling"
              :rows="2"
              data-testid="p134-fu-handling"
              placeholder="怎么处理的、现在怎么样了"
              type="textarea"
          />
        </el-form-item>
      </template>
      <el-form-item label="备注">
        <el-input v-model="followupForm.remark" :rows="2" placeholder="可空" type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
      <el-icon class="mr-1 align-middle">
        <Warning/>
      </el-icon>
      保存的是<b>草稿</b>（可改可删）；「完成随访」后记录锁死。疼痛与恢复情况在完成时为必填，
      勾选任何并发症则经过与处理必须写全 —— 闸门在服务端。
    </div>
    <template #footer>
      <el-button v-perm="['ipd:anesthesia:add', 'ipd:anesthesia:edit']" :loading="followupSubmitting" data-testid="p134-fu-submit"
                 type="primary" @click="submitFollowup">
        保存草稿
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Warning} from '@element-plus/icons-vue';
import {getAnesthesiaRecordListPage, saveFollowup} from '@/api/inpatientAnesthesia';
import * as A from '@/lib/anesthesia';
import {fmt} from '@/lib/anesthesia';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // row = 编辑既有随访草稿；fromRecord = 从麻醉记录行发起新建时的锚点
  row: {type: Object, default: null},
  fromRecord: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const nowStr = () => {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const followupSubmitting = ref(false);
const followupTargets = ref([]);
const followupForm = reactive({
  id: '',
  recordId: '',
  followupTime: '',
  painScore: undefined,
  recovery: undefined,
  adverse: [],
  adverseNote: '',
  handling: '',
  remark: '',
});
const loadFollowupTargets = async () => {
  try {
    // 可选随访的锚点 = 已提交/已审核的麻醉记录（未定稿的过程没有"术后"可言）
    const res = await getAnesthesiaRecordListPage({pageNum: 1, pageSize: 200});
    followupTargets.value = (res.data?.records || [])
        .filter((r) => r.recordStatus === 1 || r.recordStatus === 2)
        .map((r) => ({
          id: String(r.id),
          recordNo: r.recordNo,
          patientName: r.patientName,
          anesthesiaEndTime: r.anesthesiaEndTime,
        }));
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};

watch(visible, async (open) => {
  if (!open) return;
  const row = props.row;
  if (row) {
    followupForm.id = row.id;
    followupForm.recordId = row.recordId || '';
    followupForm.followupTime = fmt(row.followupTime);
    followupForm.painScore = row.painScore ?? undefined;
    followupForm.recovery = row.recovery ?? undefined;
    followupForm.adverse = (row.adverseItems || '').split(',').filter(Boolean).map(Number);
    followupForm.adverseNote = row.adverseNote || '';
    followupForm.handling = row.handling || '';
    followupForm.remark = row.remark || '';
  } else {
    followupForm.id = '';
    followupForm.recordId = props.fromRecord?.id || '';
    followupForm.followupTime = nowStr();
    followupForm.painScore = undefined;
    followupForm.recovery = undefined;
    followupForm.adverse = [];
    followupForm.adverseNote = '';
    followupForm.handling = '';
    followupForm.remark = '';
  }
  if (!followupTargets.value.length)
    await loadFollowupTargets();
});

const followupTarget = computed(() => followupTargets.value.find((t) => String(t.id) === String(followupForm.recordId)));
const hasAdverse = computed(() => followupForm.adverse.length > 0);
const submitFollowup = async () => {
  if (!followupForm.recordId) {
    ElMessage.warning('请选择要随访的麻醉记录');
    return;
  }
  if (!followupForm.followupTime) {
    ElMessage.warning('随访时间不能为空（后端要求不早于麻醉结束时间）');
    return;
  }
  followupSubmitting.value = true;
  try {
    const res = await saveFollowup({
      id: followupForm.id || undefined,
      recordId: followupForm.recordId,
      followupTime: followupForm.followupTime,
      painScore: followupForm.painScore,
      recovery: followupForm.recovery,
      adverseItems: followupForm.adverse.join(',') || undefined,
      adverseNote: followupForm.adverseNote.trim() || undefined,
      handling: followupForm.handling.trim() || undefined,
      remark: followupForm.remark.trim() || undefined,
    });
    ElMessage.success(`${followupForm.id ? '随访草稿已更新' : '随访草稿已建'}：${res.data || ''}（轮次由服务端定）`);
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    followupSubmitting.value = false;
  }
};
</script>
