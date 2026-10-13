<template>
  <el-dialog v-model="visible" data-testid="g15-pacu-score-dialog" title="Aldrete 评分" width="720px">
    <div v-if="scoreTarget" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
        {{ text(scoreTarget.pacuNo) }} · {{ text(scoreTarget.patientName) }} · {{
          text(scoreTarget.anesthesiaTypeText)
        }}
      </div>
      <div class="text-[12px] text-slate-500">
        五项各 0~2 分，<b>总分由服务端逐项相加</b>（前端不传总分）；出室标准 ≥ {{ A.ALDRETE_DISCHARGE_MIN }} 分。
      </div>
      <el-form label-width="110px">
        <el-form-item v-for="(it, idx) in A.ALDRETE_ITEMS" :key="it.key" :label="it.label" required>
          <el-radio-group v-model="scoreValues[idx]" :data-testid="`g15-score-${it.key}`">
            <el-radio v-for="o in it.options" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="清醒程度">
          <el-select v-model="scoreForm.awareness" class="!w-48">
            <el-option v-for="o in A.awarenessOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="氧疗">
          <el-input v-model="scoreForm.oxygenTherapy" placeholder="如 鼻导管 3L/min"/>
        </el-form-item>
        <el-form-item label="镇痛">
          <el-input v-model="scoreForm.analgesia" placeholder="如 PCIA"/>
        </el-form-item>
        <el-form-item label="并发症">
          <el-radio-group v-model="scoreForm.complicationFlag">
            <el-radio :value="0">无</el-radio>
            <el-radio :value="1">有</el-radio>
          </el-radio-group>
          <el-input v-if="scoreForm.complicationFlag === 1" v-model="scoreForm.complicationNote"
                    class="!ml-3 !w-96" placeholder="经过与处理（必填）"/>
        </el-form-item>
      </el-form>
      <div
          :class="scoreLocalTotal >= A.ALDRETE_DISCHARGE_MIN ? 'border-green-200 bg-green-50 text-green-700' : 'border-amber-200 bg-amber-50 text-amber-700'"
          class="rounded border px-3 py-2 text-[13px]"
      >
        本地合计 {{ scoreLocalTotal }} 分（以服务端计算为准）·
        {{
          scoreLocalTotal >= A.ALDRETE_DISCHARGE_MIN ? '已达出室标准' : `未达出室标准（≥${A.ALDRETE_DISCHARGE_MIN}）`
        }}
      </div>
    </div>
    <template #footer>
      <el-button v-perm="'ipd:anesthesia:edit'" :loading="scoreSubmitting" data-testid="g15-pacu-score-submit"
                 type="primary" @click="submitScore">记录评分
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {scorePacu} from '@/api/inpatientAnesthesia';
import * as A from '@/lib/anesthesia';
import {text} from '@/lib/anesthesia';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const scoreTarget = computed(() => props.row);
const scoreSubmitting = ref(false);
/** Aldrete 五项取值（顺序与 lib/anesthesia.ALDRETE_ITEMS 一致） */
const scoreValues = reactive([2, 2, 2, 2, 2]);
const scoreForm = reactive({
  awareness: 1,
  oxygenTherapy: '',
  analgesia: '',
  complicationFlag: 0,
  complicationNote: '',
});

watch(visible, (open) => {
  if (!open) return;
  scoreValues[0] = 2;
  scoreValues[1] = 2;
  scoreValues[2] = 2;
  scoreValues[3] = 2;
  scoreValues[4] = 2;
  scoreForm.awareness = 1;
  scoreForm.oxygenTherapy = '';
  scoreForm.analgesia = '';
  scoreForm.complicationFlag = 0;
  scoreForm.complicationNote = '';
});

const scoreLocalTotal = computed(() => scoreValues.reduce((a, b) => a + b, 0));
const submitScore = async () => {
  if (!scoreTarget.value)
    return;
  if (scoreForm.complicationFlag === 1 && !scoreForm.complicationNote.trim()) {
    ElMessage.warning('已标记并发症，必须填写经过与处理');
    return;
  }
  scoreSubmitting.value = true;
  try {
    await scorePacu({
      pacuId: scoreTarget.value.id,
      scoreActivity: scoreValues[0],
      scoreRespiration: scoreValues[1],
      scoreCirculation: scoreValues[2],
      scoreConsciousness: scoreValues[3],
      scoreSpo2: scoreValues[4],
      awareness: scoreForm.awareness,
      oxygenTherapy: scoreForm.oxygenTherapy.trim() || undefined,
      analgesia: scoreForm.analgesia.trim() || undefined,
      complicationFlag: scoreForm.complicationFlag,
      complicationNote: scoreForm.complicationNote.trim() || undefined,
    });
    ElMessage.success(`Aldrete 评分已记录（服务端计算总分 ${scoreLocalTotal.value} 分）`);
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '评分失败');
  } finally {
    scoreSubmitting.value = false;
  }
};
</script>
