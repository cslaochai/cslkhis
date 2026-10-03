/**
 * 按钮级权限判断(前端显隐专用,后端 @PreAuthorize 才是真闸门)。
 *
 * 权限码来源:/auth/info 返回的 permissions —— 当前角色的权限码集合
 * (JwtAuthenticationFilter 已按 token 里的 currentRole 收敛,切角色后自动变)。
 *
 * 用法:
 *   - 按钮(首选): v-perm="'opd:appointments:add'"     —— 等集合就绪,无权限**直接移除节点**
 *   - 多码任一:   v-perm="['a:add','a:edit']"          —— 同一个按钮既新增又修改(upsert)
 *   - 区块/tab:   v-if="hasPerm('pharmacy:stock:add')" —— 响应式,集合一到就重渲染
 *
 * ⚠ 权限码必须与 sys_menu 里 menu_type=3 的按钮菜单 permission 一字不差 ——
 * 那 161 条码就是「角色管理 → 菜单权限」勾选的同一份数据,前端自造码等于永远不匹配(按钮常隐)。
 * 页面上有按钮但**没有对应码**时不要挂:宁可不收敛,也别把业务锁死。
 *
 * 降级口径(与 router/guard.js、lib/permission.js 一致):
 * 集合**还没拉到**时不删节点(先渲染出来,拉完再收敛),**拉失败**时完全不收敛 ——
 * /auth/info 抖动是基础设施问题,此时把医生的「开立医嘱」也藏掉等于医疗事故。
 */
import { ref, watch } from 'vue';

/** 当前角色的权限集合（ref 包装，模板里读 hasPerm 才能被响应式追踪） */
const permSet = ref(new Set());
/** 是否已发起过拉取（未发起前指令不能删节点） */
let readyPromise = null;
/** 集合是否已知（Header.loadUserInfo 直接 setPermList 也算已知，不必等本模块发过请求） */
let loaded = false;
/** 拉取失败标记:失败即放弃收敛(fail-open),见文件头 */
let loadFailed = false;

/** 用 /auth/info 的 permissions 填充集合(Header.loadUserInfo 成功后也会调这里) */
export function setPermList(list) {
  permSet.value = new Set(Array.isArray(list) ? list : []);
  loaded = true;
  loadFailed = false;
  return permSet.value;
}

/** 拉取并缓存权限集合;多次调用共享同一个请求 */
export function ensurePerms() {
  if (!readyPromise) {
    // 动态引入避免与 request.js 循环依赖
    readyPromise = import('@/api/system')
      .then(({ getUserInfo }) => getUserInfo())
      .then((res) => {
        if (res.code === 200 && res.data) setPermList(res.data.permissions || []);
        return permSet.value;
      })
      .catch(() => {
        loadFailed = true;
        return permSet.value;
      });
  }
  return readyPromise;
}

/** 切角色后重置:下一次 ensurePerms 会重新拉取新角色的集合 */
export function resetPerms() {
  permSet.value = new Set();
  readyPromise = null;
  loaded = false;
  loadFailed = false;
}

/**
 * 指令与判定用的核心口径。**开头必须读一次 permSet.value**：模板里的
 * `v-if="hasPerm(...)"` 只有碰到响应式 ref 才会订阅变化，少了这次读取，
 * 集合稍后才到时按钮会永远停在「没权限」。
 * @param {string|string[]} need 单个权限码,或数组(任一命中即通过)
 */
function match(need) {
  const set = permSet.value;
  // 集合还不知道（还没拉到 / 刚切角色重置）或拉失败 → 放行不收敛，见文件头
  if (!loaded || loadFailed) return true;
  return Array.isArray(need) ? need.some((c) => set.has(c)) : set.has(need);
}

/** 判定口径（v-perm 指令内部用；数组=任一命中） */
export function permMatched(need) {
  return match(need);
}

/** 单个码 */
export function hasPerm(code) {
  if (!code) return true;
  return match(code);
}

/** 多码任一（同一按钮既新增又修改时用） */
export function hasAnyPerm(codes) {
  if (!Array.isArray(codes) || codes.length === 0) return true;
  return match(codes);
}

/** 等权限集合到手后判断（v-perm 指令用）：集合没到之前不删按钮 */
export async function ensurePerm(need) {
  await ensurePerms();
  return match(need);
}

/**
 * 订阅集合变化（v-perm 指令用）：权限是异步到达的，删掉的节点要能恢复，
 * 否则「切角色后原地复用」的常驻组件会永久缺按钮。
 * @returns {() => void} 退订，指令 unmounted 时必须调，否则泄漏
 */
export function onPermsChange(cb) {
  return watch(permSet, cb);
}

