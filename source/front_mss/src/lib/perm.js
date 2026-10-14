/**
 * 按钮级权限判断（前端显隐专用，后端接口鉴权才是真闸门）。
 *
 * 权限码来源：/auth/info 返回的 permissions —— 当前角色的权限码集合
 * （AuthServiceImpl 已按 token 里的当前角色收敛）。
 *
 * 用法：
 *   - 按钮（首选）：v-perm="'system:user:add'"     —— 等集合就绪，无权限直接移除节点
 *   - 多码任一：   v-perm="['a:add','a:edit']"      —— 同一个按钮既新增又修改（upsert）
 *   - 区块/tab:    v-if="hasPerm('system:user:add')" —— 响应式，集合一到就重渲染
 *
 * 降级口径：集合还没拉到时不删节点（先渲染出来，拉完再收敛），拉失败时完全不收敛。
 */
import { ref, watch } from 'vue';

/** 当前角色的权限集合（ref 包装，模板里读 hasPerm 才能被响应式追踪） */
const permSet = ref(new Set());
/** 是否已发起过拉取（未发起前指令不能删节点） */
let readyPromise = null;
/** 集合是否已知（登录后直接 setPermList 也算已知，不必等本模块发过请求） */
let loaded = false;
/** 拉取失败标记：失败即放弃收敛（fail-open） */
let loadFailed = false;

/** 用 /auth/info 的 permissions 填充集合（登录成功后也会调这里） */
export function setPermList(list) {
  permSet.value = new Set(Array.isArray(list) ? list : []);
  loaded = true;
  loadFailed = false;
  return permSet.value;
}

/** 拉取并缓存权限集合；多次调用共享同一个请求 */
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

/**
 * 判定口径（v-perm 指令内部用；数组=任一命中）
 */
export function permMatched(need) {
  const set = permSet.value;
  if (!loaded || loadFailed) return true;
  return Array.isArray(need) ? need.some((c) => set.has(c)) : set.has(need);
}

/** 单个码 */
export function hasPerm(code) {
  if (!code) return true;
  return permMatched(code);
}

/** 多码任一（同一按钮既新增又修改时用） */
export function hasAnyPerm(codes) {
  if (!Array.isArray(codes) || codes.length === 0) return true;
  return permMatched(codes);
}

/** 等权限集合到手后判断（v-perm 指令用）：集合没到之前不删按钮 */
export async function ensurePerm(need) {
  await ensurePerms();
  return permMatched(need);
}

/**
 * 订阅集合变化（v-perm 指令用）：权限是异步到达的，删掉的节点要能恢复
 * @returns {() => void} 退订，指令 unmounted 时必须调，否则泄漏
 */
export function onPermsChange(cb) {
  return watch(permSet, cb);
}
