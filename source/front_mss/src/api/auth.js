// 认证接口：登录 / 当前用户 / 菜单 / 登出 / 改密
import request from './request'

export function loginApi(data) {
  return request.post('/auth/login', data)
}

export function userInfoApi() {
  return request.get('/auth/info')
}

export function menuTreeApi() {
  return request.get('/auth/menus')
}

export function logoutApi() {
  return request.post('/auth/logout')
}

export function changePasswordApi(data) {
  return request.post('/auth/changePassword', data)
}
