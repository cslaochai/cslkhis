import {onMounted, onUnmounted, ref} from 'vue'

/**
 * 标准两卡式列表页（参照 views/system/user/UserView.vue）的表格限高。
 *
 * 表格只设最大高度：不足时随内容收缩，超过最大值表格内部滚动；
 * 分页在流内紧跟表格底，不钉面板底。
 *
 * 最大值 = 视口高 - 查询卡顶偏移 - 查询卡实高 - 底部总留白 - 两卡间距 - 分页行实高 - 表格卡边框
 *
 * 用法：页面模板里给查询卡 el-card 绑 ref="queryCardRef"、给分页所在 div 绑 ref="footerRef"，
 * 表格绑 :max-height="tableMaxHeight"。
 */
export function useTableMaxHeight() {
  const queryCardRef = ref(null)
  const footerRef = ref(null)
  const tableMaxHeight = ref(480)

  const calcTableMaxHeight = () => {
    const queryEl = queryCardRef.value?.$el
    if (!queryEl) return
    const queryRect = queryEl.getBoundingClientRect()
    const bottomTotal = 12 /* 外层布局下内边距（main p-3） */
    const chrome = queryRect.height
        + 12 /* 两卡间距 mb-3 */ + (footerRef.value?.offsetHeight || 0) + 2 /* 表格卡上下边框 */
    tableMaxHeight.value = Math.max(240, window.innerHeight - queryRect.top - bottomTotal - chrome)
  }

  onMounted(() => {
    calcTableMaxHeight()
    window.addEventListener('resize', calcTableMaxHeight)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', calcTableMaxHeight)
  })

  return {queryCardRef, footerRef, tableMaxHeight}
}
