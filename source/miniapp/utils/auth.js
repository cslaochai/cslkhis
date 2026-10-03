const TOKEN_KEY = 'ai_sql_token'
const USER_KEY = 'ai_sql_user'
const CURRENT_PATIENT_KEY = 'mini_current_patient'

export function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token)
}

export function removeToken() {
  wx.removeStorageSync(TOKEN_KEY)
}

export function getUser() {
  return wx.getStorageSync(USER_KEY) || null
}

export function setUser(user) {
  wx.setStorageSync(USER_KEY, user)
}

export function removeUser() {
  wx.removeStorageSync(USER_KEY)
}

export function isLoggedIn() {
  return !!getToken()
}

/**
 * 保存登录态：token + 用户信息（含 patientId / userType）
 * loginVO 来自后端 /auth/login：{ token, userId, username, realName, currentRole, patientId }
 */
export function saveLogin(loginVO) {
  // 换账号必须清掉上一个账号选的就诊人，否则新登录会带着别人的 patientId
  wx.removeStorageSync(CURRENT_PATIENT_KEY)
  setToken(loginVO.token)
  setUser({
    userId: loginVO.userId,
    username: loginVO.username,
    realName: loginVO.realName,
    currentRole: loginVO.currentRole,
    patientId: loginVO.patientId || null,
    userType: loginVO.userType != null ? loginVO.userType : 3 // 小程序端默认患者
  })
}

export function getPatientId() {
  const current = getCurrentPatient()
  if (current && current.patientId) return current.patientId
  const user = getUser()
  return user ? (user.patientId || null) : null
}

/** 当前就诊人（{patientId, patientName, relationText, self}），未手动选择时为 null */
export function getCurrentPatient() {
  return wx.getStorageSync(CURRENT_PATIENT_KEY) || null
}

/**
 * 设置当前就诊人并回传所有已注册的监听页（切页后 onShow 自行刷新）。
 * 只存展示需要的最小字段；解绑/失效由调用方在校验 myPatients 后决定。
 */
export function setCurrentPatient(p) {
  if (!p || !p.patientId) {
    wx.removeStorageSync(CURRENT_PATIENT_KEY)
    return
  }
  wx.setStorageSync(CURRENT_PATIENT_KEY, {
    patientId: p.patientId,
    patientName: p.patientName || '',
    relationText: p.relationText || '',
    self: !!p.self
  })
}

export function logout() {
  removeToken()
  removeUser()
  wx.removeStorageSync(CURRENT_PATIENT_KEY)
}
