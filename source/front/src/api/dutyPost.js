import request from './request'

/**
 * 值守点位（sql/200，biz_duty_post）。
 *
 * 把「位」从「人」里剥出来：点位先定义存在，排班只是把人写进位里。
 * 以前 biz_duty_roster 的唯一键是 (日期, 班次, 角色) 不含人，「一个责任位一天一位」的约束
 * 和「一个人的班」混在一张表里，换人也看不出是换人还是换时间。
 */

/** 点位下拉（只要求登录：值班排班页要拿它选「哪个位」） */
export function getDutyPostSelectList(params) {
  return request.get('/system/dutyPost/selectList', { params })
}

/** 分页查询点位 */
export function getDutyPostListPage(data) {
  return request.post('/system/dutyPost/listPage', data)
}

/** 新增/修改点位（必须挂在启用班次上，编码全局唯一） */
export function dutyPostUpsert(data) {
  return request.post('/system/dutyPost/dutyPostUpsert', data)
}

/** 删除点位（物理删；已被排班占用的会被引擎拦下） */
export function deleteDutyPost(id) {
  return request.delete('/system/dutyPost/deleteById', { params: { id } })
}
