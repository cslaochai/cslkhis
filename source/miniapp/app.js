App({
  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('ai_sql_token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' })
    }
  },

  globalData: {
    baseUrl: 'http://localhost:8080'
  }
})
