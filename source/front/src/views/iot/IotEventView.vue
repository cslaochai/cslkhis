<template>
  <div>
    <!-- 查询卡：事件筛选条件 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="设备类型">
          <el-select v-model="query.deviceType" :fit-input-width="false" clearable placeholder="全部类型"
                     style="width: 160px">
            <el-option v-for="t in deviceTypes" :key="t.type" :label="t.label" :value="t.type"/>
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="query.handledFlag" :fit-input-width="false" clearable placeholder="全部状态"
                     style="width: 140px">
            <el-option label="未处理" :value="0"/>
            <el-option label="已处理" :value="1"/>
            <el-option label="已忽略" :value="2"/>
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="query.minLevel" :fit-input-width="false" clearable placeholder="全部级别"
                     style="width: 140px">
            <el-option label="提示" :value="1"/>
            <el-option label="警告" :value="2"/>
            <el-option label="严重" :value="3"/>
          </el-select>
        </el-form-item>
        <el-form-item label="事件时间">
          <el-date-picker v-model="query.dateRange" end-placeholder="截止日期" start-placeholder="起始日期"
                          range-separator="至" style="width: 250px" type="daterange"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="onSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 事件列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe data-testid="iot-event-table">
        <el-table-column label="事件时间" width="165">
          <template #default="{ row }">{{ fmtTs(row.ts) }}</template>
        </el-table-column>
        <el-table-column label="设备编号" width="130" prop="deviceNo"/>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ typeLabel(row.deviceType) }}</template>
        </el-table-column>
        <el-table-column label="事件内容" min-width="230">
          <template #default="{ row }">
            <div class="text-[13px] text-gray-800">{{ row.eventCodeText || row.eventCode }}</div>
            <div v-if="row.detail" class="text-[12px] text-gray-400">{{ row.detail }}</div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="级别" width="90">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ row.levelText || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.handledFlag)" size="small">{{ row.handledFlagText || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理人" width="90">
          <template #default="{ row }">{{ row.handleBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="处理时间" width="165">
          <template #default="{ row }">{{ row.handleTime ? fmtTs(row.handleTime) : '—' }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <template v-if="row.handledFlag === 0">
              <el-button link size="small" type="primary" @click="openHandle(row, 1)">已处理</el-button>
              <el-button link size="small" type="warning" @click="openHandle(row, 2)">忽略</el-button>
            </template>
            <span v-else class="text-[12px] text-gray-400">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="loading ? '加载中…' : '暂无设备告警事件'"/>
        </template>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            :page-sizes="PAGE_SIZES"
            layout="total, prev, pager, next, jumper"
            @current-change="loadList"
            @size-change="loadList"
        />
      </div>
    </el-card>

    <IotEventHandleDialog v-model="dialogVisible" :event="currentEvent" :action-type="currentAction"
                          @saved="loadList"/>
  </div>
</template>

<script setup>
/**
 * 告警中心（设备事件闭环）。
 *
 * 数据源：biz_iot_event（MQTT 事件落库），分页查询 + 处理/忽略闭环。
 * 文案一律以后端 VO 的 levelText / handledFlagText / eventCodeText 为准，前端不维护第二份字典；
 * 前端只按码值做标签配色。处理动作的并发兜底与操作人落库都在服务端。
 */
import {onMounted, reactive, ref} from 'vue';
import {Refresh, Search} from '@element-plus/icons-vue';
import {iotEventListPage, iotSelectDeviceTypes} from '@/api/iot';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import IotEventHandleDialog from './IotEventHandleDialog.vue';

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const deviceTypes = ref([]);
const query = reactive({
  deviceType: '',
  handledFlag: null,
  minLevel: null,
  dateRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});

const typeLabel = (type) => deviceTypes.value.find(t => t.type === type)?.label || type || '—';
const levelTagType = (level) => level === 3 ? 'danger' : level === 2 ? 'warning' : 'info';
const statusTagType = (flag) => flag === 1 ? 'success' : flag === 2 ? 'info' : 'warning';
const fmtTs = (ts) => (ts ? String(ts).replace('T', ' ').slice(0, 19) : '—');

const loadList = async () => {
  loading.value = true;
  try {
    const res = await iotEventListPage({
      deviceType: query.deviceType || undefined,
      handledFlag: query.handledFlag ?? undefined,
      minLevel: query.minLevel ?? undefined,
      startTime: query.dateRange?.[0] ? `${query.dateRange[0]} 00:00:00` : undefined,
      endTime: query.dateRange?.[1] ? `${query.dateRange[1]} 23:59:59` : undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const loadTypes = async () => {
  try {
    const res = await iotSelectDeviceTypes();
    if (res.code === 200) deviceTypes.value = res.data || [];
  } catch (e) {
    console.error('加载设备类型失败', e);
  }
};

const onSearch = () => {
  query.pageNum = 1;
  loadList();
};

const resetQuery = () => {
  Object.assign(query, {deviceType: '', handledFlag: null, minLevel: null, dateRange: null, pageNum: 1});
  loadList();
};

// ------------------------------------------------------------------
// 处理 / 忽略弹框
// ------------------------------------------------------------------
const dialogVisible = ref(false);
const currentEvent = ref(null);
const currentAction = ref(1);

const openHandle = (row, actionType) => {
  currentEvent.value = row;
  currentAction.value = actionType;
  dialogVisible.value = true;
};

onMounted(() => {
  loadTypes();
  loadList();
});
</script>
