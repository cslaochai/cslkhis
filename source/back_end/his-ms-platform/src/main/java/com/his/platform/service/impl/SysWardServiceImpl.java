package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.platform.dto.WardDeptQuotaDTO;
import com.his.platform.dto.WardQueryDTO;
import com.his.platform.dto.WardUpsertDTO;
import com.his.platform.entity.SysDepartment;
import com.his.platform.entity.SysEmployee;
import com.his.platform.entity.SysWard;
import com.his.platform.entity.SysWardDept;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.mapper.SysWardDeptMapper;
import com.his.platform.mapper.SysWardMapper;
import com.his.platform.service.SysWardService;
import com.his.platform.vo.WardDeptVO;
import com.his.platform.vo.WardVO;
import com.his.spi.service.InpatientFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 病区主数据服务实现。
 *
 * <p>核心是把「病区 ↔ 科室」从原来的一对一（sys_ward.dept_id）升级为多对多（sys_ward_dept），
 * 同时保留 dept_id 作为本位科室，避免砸掉 6 处依赖 ward_id → dept_id 单值折算的数据权限判定。
 */
@Service
@RequiredArgsConstructor
public class SysWardServiceImpl extends ServiceImpl<SysWardMapper, SysWard> implements SysWardService {

    /**
     * 病区编码前缀：W + 3 位序号
     */
    private static final String WARD_CODE_PREFIX = "W";

    private final SysWardMapper sysWardMapper;
    private final SysWardDeptMapper sysWardDeptMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final InpatientFacadeService inpatientFacadeService;

    @Override
    public PageResult<WardVO> listPage(WardQueryDTO queryDTO) {
        LambdaQueryWrapper<SysWard> wrapper = buildWrapper(queryDTO);
        if (wrapper == null) {
            return PageResult.of(0L, queryDTO.getPageNum(), queryDTO.getPageSize(), 0L, Collections.emptyList());
        }
        Page<SysWard> page = sysWardMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toVOList(page.getRecords()));
    }

    @Override
    public List<WardVO> list(WardQueryDTO queryDTO) {
        LambdaQueryWrapper<SysWard> wrapper = buildWrapper(queryDTO);
        if (wrapper == null) {
            return Collections.emptyList();
        }
        return toVOList(sysWardMapper.selectList(wrapper));
    }

    @Override
    public List<WardVO> selectList(Long deptId) {
        List<SysWard> wards;
        if (deptId == null) {
            wards = sysWardMapper.selectList(new LambdaQueryWrapper<SysWard>()
                    .eq(SysWard::getStatus, 1)
                    .orderByAsc(SysWard::getWardCode));
        } else {
            // 走多对多：该科室能收治的病区，含跨科收治的病区（原来按 sys_ward.dept_id 单值过滤会漏掉）
            List<Long> wardIds = sysWardDeptMapper.selectWardIdsByDeptId(deptId);
            if (wardIds.isEmpty()) {
                return Collections.emptyList();
            }
            wards = sysWardMapper.selectList(new LambdaQueryWrapper<SysWard>()
                    .in(SysWard::getWardId, wardIds)
                    .eq(SysWard::getStatus, 1)
                    .orderByAsc(SysWard::getWardCode));
        }
        return toVOList(wards);
    }

    @Override
    public WardVO getInfo(Long wardId) {
        SysWard ward = sysWardMapper.selectById(wardId);
        if (ward == null) {
            throw new BusinessException("病区不存在");
        }
        WardVO vo = toVO(ward, Collections.emptyMap());
        vo.setDepts(loadDepts(wardId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upsert(WardUpsertDTO upsertDTO) {
        // ---- 前置校验（全部在写库前）----
        if (upsertDTO.getDeptId() == null) {
            throw new BusinessException("本位科室不能为空");
        }
        SysDepartment dept = sysDepartmentMapper.selectById(upsertDTO.getDeptId());
        if (dept == null) {
            throw new BusinessException("本位科室不存在");
        }

        // 收治科室清单规范化：为空只按本位科室建一条；本位科室必须在内且只能有一条本位
        List<WardDeptQuotaDTO> quotas = normalizeDeptQuotas(upsertDTO);

        SysWard ward = new SysWard();
        BeanUtils.copyProperties(upsertDTO, ward);
        // 本位科室同步写回 sys_ward.dept_id：6 处数据权限折算依赖这一列，不能与新表脱节
        ward.setDeptId(upsertDTO.getDeptId());

        // 主键在 insert 后会被雪花填充，必须在写库前记下本次是新增还是修改
        boolean isCreate = upsertDTO.getWardId() == null;

        if (isCreate) {
            if (!TextUtil.hasText(ward.getWardCode())) {
                ward.setWardCode(nextWardCode());
            } else if (codeExists(ward.getWardCode(), null)) {
                throw new BusinessException("病区编码已存在：" + ward.getWardCode());
            }
            if (ward.getStatus() == null) {
                ward.setStatus(1);
            }
            if (ward.getTotalBeds() == null) {
                ward.setTotalBeds(0);
            }
            if (ward.getOccupiedBeds() == null) {
                ward.setOccupiedBeds(0);
            }
            sysWardMapper.insert(ward);
        } else {
            SysWard existing = sysWardMapper.selectById(ward.getWardId());
            if (existing == null) {
                throw new BusinessException("病区不存在");
            }
            if (TextUtil.hasText(ward.getWardCode()) && codeExists(ward.getWardCode(), ward.getWardId())) {
                throw new BusinessException("病区编码已存在：" + ward.getWardCode());
            }
            // 已占用床位数由业务侧维护，编辑表单不允许改写
            ward.setOccupiedBeds(null);
            sysWardMapper.updateById(ward);
        }

        saveWardDepts(ward.getWardId(), upsertDTO.getDeptId(), quotas);
        return isCreate ? "新增成功" : "修改成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long wardId) {
        SysWard ward = sysWardMapper.selectById(wardId);
        if (ward == null) {
            throw new BusinessException("病区不存在");
        }

        // 前置校验 1：还有员工挂在这个病区（本模块直接可查）
        Long empCount = sysEmployeeMapper.selectCount(
                new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getWardId, wardId));
        if (empCount != null && empCount > 0) {
            throw new BusinessException("该病区还有 " + empCount + " 名员工挂靠，请先调整人员归属再删除");
        }

        // 前置校验 2：病区已开床位（跨模块，走依赖倒置端口，不直接查住院域的表）
        List<com.his.spi.vo.BedVO> beds = inpatientFacadeService.listBeds(wardId, null, null);
        if (beds != null && !beds.isEmpty()) {
            throw new BusinessException("该病区已开 " + beds.size() + " 张床位，不能删除；不再使用请改为停用（状态设为停用）");
        }

        sysWardDeptMapper.delete(new LambdaQueryWrapper<SysWardDept>().eq(SysWardDept::getWardId, wardId));
        sysWardMapper.deleteById(wardId);
    }

    // ------------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------------

    /**
     * 构造查询条件。返回 null 表示「按科室过滤但该科室没有可收治的病区」，调用方直接返回空集，
     * 不要往下走查询（避免 in() 传空集合生成非法 SQL）。
     */
    private LambdaQueryWrapper<SysWard> buildWrapper(WardQueryDTO queryDTO) {
        LambdaQueryWrapper<SysWard> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getWardName()), SysWard::getWardName, queryDTO.getWardName())
                .like(TextUtil.hasText(queryDTO.getWardCode()), SysWard::getWardCode, queryDTO.getWardCode())
                .eq(queryDTO.getStatus() != null, SysWard::getStatus, queryDTO.getStatus())
                .orderByAsc(SysWard::getWardCode);
        if (queryDTO.getDeptId() != null) {
            List<Long> wardIds = sysWardDeptMapper.selectWardIdsByDeptId(queryDTO.getDeptId());
            if (wardIds.isEmpty()) {
                return null;
            }
            wrapper.in(SysWard::getWardId, wardIds);
        }
        return wrapper;
    }

    private boolean codeExists(String wardCode, Long excludeWardId) {
        LambdaQueryWrapper<SysWard> wrapper = new LambdaQueryWrapper<SysWard>()
                .eq(SysWard::getWardCode, wardCode);
        if (excludeWardId != null) {
            wrapper.ne(SysWard::getWardId, excludeWardId);
        }
        Long count = sysWardMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private String nextWardCode() {
        int seq = sysWardMapper.selectMaxWardSeq() + 1;
        return String.format("%s%03d", WARD_CODE_PREFIX, seq);
    }

    /**
     * 规范化收治科室清单：本位科室必须在列表里且只出现一次；不传清单就只留本位科室。
     */
    private List<WardDeptQuotaDTO> normalizeDeptQuotas(WardUpsertDTO upsertDTO) {
        Long primaryDeptId = upsertDTO.getDeptId();
        LinkedHashMap<Long, WardDeptQuotaDTO> map = new LinkedHashMap<>();
        if (upsertDTO.getDepts() != null) {
            for (WardDeptQuotaDTO item : upsertDTO.getDepts()) {
                if (item == null || item.getDeptId() == null) {
                    continue;
                }
                map.putIfAbsent(item.getDeptId(), item);
            }
        }
        map.putIfAbsent(primaryDeptId, buildPrimaryQuota(primaryDeptId));

        // 科室必须真实存在，否则多对多表会出现指向空气的关系
        Set<Long> deptIds = map.keySet();
        List<SysDepartment> depts = sysDepartmentMapper.selectBatchIds(deptIds);
        Set<Long> existing = depts.stream().map(SysDepartment::getId).collect(Collectors.toSet());
        List<Long> missing = deptIds.stream().filter(id -> !existing.contains(id)).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new BusinessException("收治科室不存在：" + missing);
        }
        return new ArrayList<>(map.values());
    }

    private WardDeptQuotaDTO buildPrimaryQuota(Long deptId) {
        WardDeptQuotaDTO quota = new WardDeptQuotaDTO();
        quota.setDeptId(deptId);
        return quota;
    }

    /**
     * 保存病区收治科室：先删后插（关系表无外部引用，全量替换最简单也最不容易漏）。
     * 本位科室固定 isPrimary=1，其余 0。
     */
    private void saveWardDepts(Long wardId, Long primaryDeptId, List<WardDeptQuotaDTO> quotas) {
        sysWardDeptMapper.delete(new LambdaQueryWrapper<SysWardDept>().eq(SysWardDept::getWardId, wardId));
        int sort = 0;
        // 本位科室排最前
        List<WardDeptQuotaDTO> ordered = new ArrayList<>(quotas);
        ordered.sort(Comparator.comparing(q -> q.getDeptId().equals(primaryDeptId) ? 0 : 1));
        for (WardDeptQuotaDTO quota : ordered) {
            SysWardDept rel = new SysWardDept();
            rel.setWardId(wardId);
            rel.setDeptId(quota.getDeptId());
            rel.setIsPrimary(quota.getDeptId().equals(primaryDeptId) ? 1 : 0);
            rel.setBedQuota(quota.getBedQuota());
            rel.setSortOrder(sort++);
            sysWardDeptMapper.insert(rel);
        }
    }

    private List<WardDeptVO> loadDepts(Long wardId) {
        List<SysWardDept> rels = sysWardDeptMapper.selectList(new LambdaQueryWrapper<SysWardDept>()
                .eq(SysWardDept::getWardId, wardId)
                .orderByDesc(SysWardDept::getIsPrimary)
                .orderByAsc(SysWardDept::getSortOrder));
        if (rels.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> nameMap = deptNameMap(rels.stream().map(SysWardDept::getDeptId).collect(Collectors.toSet()));
        return rels.stream().map(rel -> {
            WardDeptVO vo = new WardDeptVO();
            vo.setDeptId(rel.getDeptId());
            vo.setDeptName(nameMap.getOrDefault(rel.getDeptId(), ""));
            vo.setIsPrimary(rel.getIsPrimary());
            vo.setBedQuota(rel.getBedQuota());
            vo.setSortOrder(rel.getSortOrder());
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, String> deptNameMap(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysDepartmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDeptName, (a, b) -> a));
    }

    private List<WardVO> toVOList(List<SysWard> wards) {
        if (wards == null || wards.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> wardIds = wards.stream().map(SysWard::getWardId).collect(Collectors.toSet());
        // 一次查出这批病区的全部收治科室关系，避免按病区逐条查（列表页要显示收治科室列）
        List<SysWardDept> allRels = sysWardDeptMapper.selectList(new LambdaQueryWrapper<SysWardDept>()
                .in(SysWardDept::getWardId, wardIds)
                .orderByDesc(SysWardDept::getIsPrimary)
                .orderByAsc(SysWardDept::getSortOrder));
        Map<Long, List<SysWardDept>> relMap = allRels.stream()
                .collect(Collectors.groupingBy(SysWardDept::getWardId));

        Set<Long> deptIds = new HashSet<>();
        wards.stream().map(SysWard::getDeptId).filter(Objects::nonNull).forEach(deptIds::add);
        allRels.stream().map(SysWardDept::getDeptId).filter(Objects::nonNull).forEach(deptIds::add);
        Map<Long, String> nameMap = deptNameMap(deptIds);

        return wards.stream().map(w -> {
            WardVO vo = toVO(w, nameMap);
            List<SysWardDept> rels = relMap.getOrDefault(w.getWardId(), Collections.emptyList());
            vo.setDepts(rels.stream().map(rel -> {
                WardDeptVO item = new WardDeptVO();
                item.setDeptId(rel.getDeptId());
                item.setDeptName(nameMap.getOrDefault(rel.getDeptId(), ""));
                item.setIsPrimary(rel.getIsPrimary());
                item.setBedQuota(rel.getBedQuota());
                item.setSortOrder(rel.getSortOrder());
                return item;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    private WardVO toVO(SysWard ward, Map<Long, String> deptNameMap) {
        if (ward == null) {
            return null;
        }
        WardVO vo = new WardVO();
        BeanUtils.copyProperties(ward, vo);
        if (ward.getDeptId() != null) {
            vo.setDeptName(deptNameMap.get(ward.getDeptId()));
        }
        return vo;
    }
}
