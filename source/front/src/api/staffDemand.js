import request from './request'

// 人力需求与缺口（sql/212，闭环第①步：排班的分母）
// 后端控制器：/system/staffDemand（his-system）
//
// 为什么排班页要读它：只看到「今天排了 5 个人」判断不出够不够 ——
// 需求 6 人时那叫缺 1 人，需求 4 人时那叫富余 1 人。需求是算出来的，
// 住院侧来自「在院患者 × 护理等级工时」，门诊侧来自「出诊医生数」，
// 且一律与该单元核定的最低配置取 MAX（病区再空的白班/前夜/后夜三班也得有人顶）。

// 缺口清单：给定日期区间（+ 单元 + 岗位类别），返回 需求/在岗/缺口 三个数
// orgType 必填（1-科室 2-病区）：两种单元的 id 不在同一空间，只给 orgId 后端直接拒
export function getStaffDemandGapList(params) {
  return request.get('/system/staffDemand/gapList', { params })
}

// 重算某段日期的需求：只覆盖派生行，护士长手工调过的（source=3）原样保留
export function recalcStaffDemand(params) {
  return request.post('/system/staffDemand/recalc', null, { params })
}

// 护士长手工调整需求人数：写为手工来源，后续重算不再覆盖
export function adjustStaffDemand(params) {
  return request.post('/system/staffDemand/adjust', null, { params })
}
