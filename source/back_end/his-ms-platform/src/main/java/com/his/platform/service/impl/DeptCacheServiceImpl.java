package com.his.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.platform.entity.SysDepartment;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.service.DeptCacheService;
import com.his.spi.vo.DeptBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 科室摘要 Redis 缓存实现：懒加载模式，启动时不预热；缓存未命中回库，读异常降级直查库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptCacheServiceImpl implements DeptCacheService {

    private static final String DEPT_CACHE_PREFIX = "sys:dept:brief:";

    private static final long CACHE_EXPIRE_HOURS = 24;

    private final StringRedisTemplate stringRedisTemplate;

    private final SysDepartmentMapper sysDepartmentMapper;

    private final ObjectMapper objectMapper;

    @Override
    public DeptBriefVO findBrief(Long deptId) {
        if (deptId == null) {
            return null;
        }
        try {
            String key = DEPT_CACHE_PREFIX + deptId;
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<DeptBriefVO>() {
                });
            }

            DeptBriefVO brief = toBrief(sysDepartmentMapper.selectById(deptId));
            if (brief != null) {
                json = objectMapper.writeValueAsString(brief);
                stringRedisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }
            return brief;
        } catch (Exception e) {
            log.error("获取科室摘要缓存失败: deptId={}", deptId, e);
            // 降级：直接查库，不让缓存故障挡住业务读数
            return toBrief(sysDepartmentMapper.selectById(deptId));
        }
    }

    @Override
    public void evict(Long deptId) {
        if (deptId == null) {
            return;
        }
        stringRedisTemplate.delete(DEPT_CACHE_PREFIX + deptId);
    }

    private DeptBriefVO toBrief(SysDepartment department) {
        if (department == null) {
            return null;
        }
        DeptBriefVO vo = new DeptBriefVO();
        vo.setId(department.getId());
        vo.setDeptCode(department.getDeptCode());
        vo.setParentId(department.getParentId());
        vo.setDeptName(department.getDeptName());
        vo.setLocation(department.getLocation());
        vo.setContactPhone(department.getContactPhone());
        vo.setDeptType(department.getDeptType());
        vo.setNodeType(department.getNodeType());
        vo.setStatus(department.getStatus());
        vo.setOutpatientAuditorId(department.getOutpatientAuditorId());
        vo.setNursingAuditorId(department.getNursingAuditorId());
        vo.setScheduleAuditorId(department.getScheduleAuditorId());
        return vo;
    }
}
