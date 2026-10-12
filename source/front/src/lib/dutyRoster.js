// 全院总值班排班（views/duty/DutyRosterView.vue）页面与其拆出子组件共用的口径
// —— 值班种类的 scope 与员工远程搜索主页面/两个弹框都要用，各写一份必然漂移。
import {ref} from 'vue';
import {getEmployeeList} from '@/api/system';

// 值班种类：全院总值班（行政位）vs 科室医师值班（sql/202 铺的临床位，39 个科室 × 一线/二线/三线）。
// 两者同居 biz_duty_roster 一张表，靠 post_id 挂的点位分开 —— 页面必须让排班员先选是哪一类，
// 否则「今日全院谁负责」那张表会被 234 个科室值班行冲垮，谁也看不懂。
export const DUTY_KINDS = [
  {value: 'admin', label: '全院总值班', scope: 1},
  {value: 'clinical', label: '科室医师值班', scope: 6},
]
export const dutyKindScope = (kind) => (DUTY_KINDS.find((k) => k.value === kind) || DUTY_KINDS[0]).scope

// 员工下拉（远程搜索：全院员工上千人，一次性拉全量会卡死下拉）
export const useEmployeeSearch = () => {
  const empOptions = ref([])
  const empLoading = ref(false)
  const searchEmployee = async (keyword) => {
    empLoading.value = true
    try {
      const res = await getEmployeeList({empName: keyword || '', pageSize: 50})
      const list = res?.data || []
      empOptions.value = list.map((e) => ({
        id: e.id,
        empName: e.empName,
        deptName: e.deptName,
        phone: e.phone,
      }))
    } catch (e) {
      empOptions.value = []
    } finally {
      empLoading.value = false
    }
  }
  return {empOptions, empLoading, searchEmployee}
}
