package com.his.platform.service;

import com.his.spi.vo.DeptBriefVO;

/**
 * 科室摘要 Redis 读缓存：按科室ID懒加载，未命中回库并回填；科室变更 / 删除后主动失效。
 *
 * <p>只服务 {@code SystemFacadeService} 的科室只读取数，缓存载荷为窄摘要 {@link DeptBriefVO}，
 * 不把科室主档实体透出模块。
 */
public interface DeptCacheService {

    /**
     * 按科室ID取摘要；缓存未命中则回库加载并回填，Redis 异常时直接降级查库。
     *
     * @param deptId 科室ID（可空，为空返回 {@code null}）
     * @return 科室不存在 / 已删除时返回 {@code null}
     */
    DeptBriefVO findBrief(Long deptId);

    /**
     * 失效单个科室的缓存条目（只删键，不回源重建），科室新增 / 修改 / 删除后调用。
     */
    void evict(Long deptId);
}
