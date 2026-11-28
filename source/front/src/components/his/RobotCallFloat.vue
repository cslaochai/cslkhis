<template>
  <!--
    全局叫车浮窗（右下角常驻）：任何有「发起配送」码的岗位都能一键叫车，不必先进调度台。
    厂商口径一致：站点端呼叫 → 后端派单算法自动选车；人工派车只是调度台的异常干预。
    浮窗只做发起 + 车队空闲提示，车队卡片、改派、取消留在 /iot-robot-dispatch 调度台。
  -->
  <div v-if="hasPerm('iot:robotDispatch:apply')" class="fixed right-6 bottom-6 z-50 flex flex-col items-end gap-2"
       data-testid="robot-call-float">
    <div v-if="fleetLoaded"
         class="rounded-full border border-slate-200 bg-white/95 px-3 py-1 text-[12px] text-gray-500 shadow-sm">
      <span class="mr-1 inline-block h-2 w-2 rounded-full align-middle"
            :class="idleCount ? 'bg-[#0E9488]' : 'bg-gray-400'"></span>
      空闲 {{ idleCount }}/{{ fleet.length }}
      <span v-if="!idleCount" class="text-orange-500">· 车队满负荷，急送可插队</span>
    </div>
    <el-button round style="height: 48px; padding: 0 20px; font-size: 15px; box-shadow: 0 6px 18px rgba(18,105,181,.35)"
               type="primary" @click="openDialog">
      <el-icon class="mr-1"><Van/></el-icon>
      叫机器人
    </el-button>

    <RobotDispatchAdd v-model="dialogVisible" :preset-dept-name="deptName" @saved="loadFleet"/>
  </div>
</template>

<script setup>
/**
 * 机器人叫车浮窗（全局，挂 DashboardLayout）。
 *
 * - 显隐按 `iot:robotDispatch:apply`：护士/护士长/科护士长/设备科/管理员都能发起；
 *   集合没拉到前 fail-open（先渲染，拉完再收敛），与 v-perm 同一口径。
 * - 预填按当前岗位：发起站点/取货点取登录时落到 localStorage 的 deptName，避免每次都手填本科室。
 * - 车队空闲数是提示，不是权限：拉不到就整条隐藏，不影响叫车。
 */
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {Van} from '@element-plus/icons-vue';
import {hasPerm, ensurePerm} from '@/lib/perm';
import {robotDispatchFleet} from '@/api/robotDispatch';
import RobotDispatchAdd from '@/components/his/RobotDispatchAdd.vue';

const dialogVisible = ref(false);
const fleet = ref([]);
const fleetLoaded = ref(false);
const idleCount = computed(() => fleet.value.filter((r) => !r.currentOrderNo && r.state !== 'OFFLINE').length);

// 岗位默认起点：护士站/医生站叫车时，发起站点一般就是本科室站点。
// 点击时现读 localStorage —— Header 拉到 /auth/info 才写入，computed 在挂载时算会拿到空值。
const deptName = ref('');
const openDialog = () => {
  deptName.value = localStorage.getItem('deptName') || '';
  dialogVisible.value = true;
};

const loadFleet = async () => {
  // 车队卡片是设备科的监控视角：没有「车队查看」码的岗位（医生、分诊）不发这个请求，
  // 否则每 30 秒一条 403，控制台噪音盖掉真错误。叫车本身不受影响。
  if (!hasPerm('iot:robotDispatch:list')) {
    fleetLoaded.value = false;
    return;
  }
  try {
    const res = await robotDispatchFleet();
    fleet.value = res?.data || [];
    fleetLoaded.value = true;
  } catch (e) {
    // 没有车队查看权限的岗位（如门诊分诊）仍旧能叫车，只是看不到空闲数
    console.error('加载机器人车队失败', e);
    fleet.value = [];
    fleetLoaded.value = false;
  }
};

let timer = null;
onMounted(async () => {
  // 等权限集合到手再拉车队：hasPerm 在集合未到时 fail-open，会先白打一次 403
  await ensurePerm('iot:robotDispatch:list').catch(() => false);
  loadFleet();
  timer = setInterval(() => {
    if (!document.hidden) loadFleet();
  }, 30_000);
});
onUnmounted(() => timer && clearInterval(timer));
</script>
