import { sm2 } from 'sm-crypto'
import { getPublicKey } from '@/api/system'

/**
 * 登录页口令加密：SM2 国密。
 *
 * 做法只有一条 —— 口令在离开浏览器之前就变成密文，网络上看不见明文。
 * 公钥本身是公开信息，能看到的只是"加密用的那把锁"，私钥从不露面，也不可能被传到前端。
 */

let cachedKey = null

async function fetchPublicKey(force = false) {
  if (cachedKey && !force) return cachedKey
  const res = await getPublicKey()
  if (res.code !== 200 || !res.data?.publicKey) {
    throw new Error('获取登录加密公钥失败')
  }
  cachedKey = { keyId: res.data.keyId, publicKey: res.data.publicKey }
  return cachedKey
}

/**
 * 口令加密 -> 十六进制密文（C1C3C2，不带 04 前缀，与后端 SM2Engine.Mode.C1C3C2 对齐）。
 * @param {string} plain 明文口令
 * @returns {Promise<string>}
 */
export async function encryptPassword(plain) {
  const { publicKey } = await fetchPublicKey()
  try {
    return sm2.doEncrypt(plain, publicKey, 1)
  } catch (e) {
    // 多半是后端换了密钥，前端还握着旧公钥：拉一次新的再加密一遍
    const { publicKey: freshKey } = await fetchPublicKey(true)
    return sm2.doEncrypt(plain, freshKey, 1)
  }
}

/** 退出登录后清掉，下次从头拉一次 */
export function resetPublicKeyCache() {
  cachedKey = null
}
