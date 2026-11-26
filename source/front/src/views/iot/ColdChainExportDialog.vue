<template>
  <el-dialog v-model="visible" title="导出冷链温湿度记录（GSP）" destroy-on-close width="620px">
    <div class="space-y-4">
      <div class="text-[13px] text-gray-500 leading-6">
        按 GSP 附录 5 口径导出：每 5 分钟一轮，含窗口平均温度、最低、最高与记录点数。
        单台设备一天约 288 行，可直接打印归档；跨天请分段导出。
      </div>

      <div class="flex items-center gap-3">
        <span class="w-[86px] text-[13px] text-gray-600 text-right">记录日期</span>
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
                        start-placeholder="开始日期" end-placeholder="结束日期"
                        :clearable="false" style="width: 320px"/>
      </div>

      <div class="flex items-center gap-3">
        <span class="w-[86px] text-[13px] text-gray-600 text-right">冷藏柜</span>
        <el-select v-model="deviceNo" :fit-input-width="false" style="width: 320px">
          <el-option label="全部冷藏柜" value="ALL"/>
          <el-option v-for="d in devices" :key="d.deviceNo"
                     :label="`${d.deviceNo} ${d.deviceName || ''}`" :value="d.deviceNo"/>
        </el-select>
        <span class="text-[12px] text-gray-400">共 {{ targetCount }} 台</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="exporting" @click="doExport">导出 CSV</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * GSP 冷链温湿度记录导出。
 *
 * 数据来自后端 /iot/telemetry/coldChainRecords（TDengine 5 分钟聚合），前端只负责拼 CSV。
 * 不在页面内做表格预览：GSP 检查要的是带签章的文件，预览反而容易让人以为"页面数据能替代记录"。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {iotColdChainRecords} from '@/api/iot';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  devices: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const pad = (n) => String(n).padStart(2, '0');
const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const dateRange = ref([today(), today()]);
const deviceNo = ref('ALL');
const exporting = ref(false);

const targetCount = computed(() => (deviceNo.value === 'ALL' ? props.devices.length : 1));

watch(visible, (v) => {
  if (v) {
    dateRange.value = [today(), today()];
    deviceNo.value = 'ALL';
  }
});

const fmtNum = (v, digits = 1) => (v == null ? '' : Number(v).toFixed(digits));
const escapeCsv = (v) => {
  const s = v == null ? '' : String(v);
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
};
const download = (filename, text) => {
  // BOM：Excel 直接双击打开中文表头不乱码
  const blob = new Blob(['\uFEFF' + text], {type: 'text/csv;charset=utf-8'});
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};

const doExport = async () => {
  if (!dateRange.value?.[0] || !dateRange.value?.[1]) {
    ElMessage.warning('请选择记录日期');
    return;
  }
  const [startDate, endDate] = dateRange.value;
  const targets = deviceNo.value === 'ALL'
      ? props.devices
      : props.devices.filter(d => d.deviceNo === deviceNo.value);
  if (!targets.length) {
    ElMessage.warning('没有可导出的冷藏柜');
    return;
  }

  exporting.value = true;
  try {
    const lines = ['设备编号,设备名称,归属科室,安装位置,记录时间,平均温度(℃),最低温度(℃),最高温度(℃),平均湿度(%),记录点数'];
    let total = 0;
    for (const dev of targets) {
      const res = await iotColdChainRecords({
        deviceNo: dev.deviceNo,
        startTime: `${startDate} 00:00:00`,
        endTime: `${endDate} 23:59:59`,
      });
      const rows = res.data || [];
      for (const r of rows) {
        lines.push([
          escapeCsv(dev.deviceNo),
          escapeCsv(dev.deviceName),
          escapeCsv(dev.deptName),
          escapeCsv(dev.location),
          escapeCsv(String(r.ts || '').replace('T', ' ')),
          fmtNum(r.temp),
          fmtNum(r.tempMin),
          fmtNum(r.tempMax),
          fmtNum(r.humidity, 0),
          r.pointCount,
        ].join(','));
      }
      total += rows.length;
    }
    if (!total) {
      ElMessage.warning('所选日期没有温湿度记录');
      return;
    }
    const suffix = deviceNo.value === 'ALL' ? '全部冷藏柜' : deviceNo.value;
    download(`冷链温湿度记录_${suffix}_${startDate}_${endDate}.csv`, lines.join('\n'));
    ElMessage.success(`已导出 ${total} 条记录`);
    visible.value = false;
  } catch (e) {
    console.error('导出冷链记录失败', e);
    ElMessage.error(e?.response?.data?.message || '导出失败');
  } finally {
    exporting.value = false;
  }
};
</script>
