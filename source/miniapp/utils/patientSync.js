import { guardianApi } from './api'
import { getUser, getCurrentPatient, setCurrentPatient } from './auth'

const USER_TYPE_PATIENT = 3

/**
 * 按后端的默认绑定关系回填「当前就诊人」。
 *
 * 绑定关系存在 biz_patient_guardian 里（登录接口回来就能看到），但登录时 saveLogin
 * 必须清掉本地指针——换账号不清就带着上一个账号的 patientId 去挂号了。清完没人回填，
 * 用户看到的就是"我绑的就诊人没了"。指针指向的档案在别处被解绑时同样靠这里自愈。
 *
 * 只对患者账号（userType=3）回填：医生/护士/管理员登录小程序是为了处理工作，
 * 给他们塞一个就诊人，页面就会拿着别人的 patientId 去查病历、排队和费用。
 */
export async function restoreCurrentPatient() {
  const user = getUser()
  if (!user || user.userType !== USER_TYPE_PATIENT) {
    setCurrentPatient(null)
    return null
  }
  try {
    const res = await guardianApi.myPatients()
    if (res.code !== 200) return getCurrentPatient()
    const list = res.data || []
    const target = list.find(p => p.isDefault === 1) || list[0]
    if (!target) {
      // 账号名下确实没有任何可就诊人：清掉指针，别让页面拿着已解绑的 id 继续查
      setCurrentPatient(null)
      return null
    }
    setCurrentPatient(target)
    return getCurrentPatient()
  } catch (e) {
    console.error('回填当前就诊人失败', e)
    return getCurrentPatient()
  }
}

/** 指针有值直接用，为空（刚登录被清、深链直接进 tab）才回源，省掉每次 onShow 一次请求 */
export async function ensurePatient() {
  return getCurrentPatient() || restoreCurrentPatient()
}
