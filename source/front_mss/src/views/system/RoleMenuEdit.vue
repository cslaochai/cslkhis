<template>
  <!-- 菜单权限配置弹框：菜单树只有这里用，随本组件懒加载 -->
  <el-dialog
      v-model="visible"
      :title="`配置菜单权限 - ${props.role?.roleName || ''}`"
      width="640px"
      destroy-on-close
  >
    <div v-loading="menuLoading" class="menu-tree-wrap">
      <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          :props="{ label: 'menuName', children: 'children' }"
          default-expand-all
          node-key="id"
          show-checkbox
      />
    </div>
    <template #footer>
      <el-button :loading="menuSubmitting" type="primary" @click="handleSaveRoleMenu">
        保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, nextTick, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getMenuTree, getRoleMenuIds, saveRoleMenu} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  role: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const menuTree = ref([])
const menuTreeRef = ref(null)
const menuLoading = ref(false)
const menuSubmitting = ref(false)

watch(visible, async (open) => {
  if (!open || !props.role) return
  menuLoading.value = true
  try {
    // 菜单树的 id 由后端按字符串下发（MenuVO 用 ToStringSerializer），
    // 因此 tree 的 key 与回显值都必须用 String，否则 setCheckedKeys 匹配不上、权限树全空
    const [treeRes, idsRes] = await Promise.all([
      menuTree.value.length ? Promise.resolve(null) : getMenuTree(),
      getRoleMenuIds(props.role.id),
    ])
    if (treeRes?.code === 200) menuTree.value = treeRes.data || []
    await nextTick()
    const ids = idsRes.code === 200 ? (idsRes.data || []) : []
    // 回显只设置叶子节点。el-tree 的 setCheckedKeys(keys, leafOnly) 在父目录上不可靠：
    // 目录一旦被当作可勾选节点选中，父子联动会把它下面**未授权**的子菜单一并勾上，
    // 保存后权限被放大（实测医生角色 23 条被写成 32 条）。
    // 这里显式剔除「有子节点的目录」，父节点的全选/半选交给 el-tree 自己推导。
    const parentIds = new Set()
    const collectParents = (nodes) => {
      for (const node of nodes || []) {
        if (node.children && node.children.length) {
          parentIds.add(String(node.id))
          collectParents(node.children)
        }
      }
    }
    collectParents(menuTree.value)
    menuTreeRef.value?.setCheckedKeys(ids.map(String).filter(id => !parentIds.has(id)))
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    menuLoading.value = false
  }
})

const handleSaveRoleMenu = async () => {
  if (!menuTreeRef.value || !props.role) return
  // 半选状态的父目录也要提交，否则后端拿到的菜单可能缺目录节点
  const checked = menuTreeRef.value.getCheckedKeys()
  const halfChecked = menuTreeRef.value.getHalfCheckedKeys()
  menuSubmitting.value = true
  try {
    const res = await saveRoleMenu({
      roleId: props.role.id,
      menuIds: [...checked, ...halfChecked],
    })
    if (res.code === 200) {
      ElMessage.success('菜单权限保存成功')
      visible.value = false
      emit('saved')
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    menuSubmitting.value = false
  }
}
</script>

<style scoped>
/* 菜单树节点很多（100+），弹框内限高内部滚动，避免把弹框撑出视口 */
.menu-tree-wrap {
  max-height: 60vh;
  overflow-y: auto;
}
</style>
