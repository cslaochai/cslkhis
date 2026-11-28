package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.DeptNodeTypeEnum;
import com.his.common.enums.DeptSelectModeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import org.apache.commons.lang3.StringUtils;
import com.his.platform.dto.DepartmentQueryDTO;
import com.his.platform.dto.DepartmentSelectDTO;
import com.his.platform.dto.DepartmentUpsertDTO;
import com.his.platform.entity.SysDepartment;
import com.his.platform.entity.SysEmployee;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.service.DeptCacheService;
import com.his.platform.service.DeptScopeService;
import com.his.platform.service.SysDepartmentService;
import com.his.platform.vo.DepartmentTreeSelectVO;
import com.his.platform.vo.DepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements SysDepartmentService {
    /**
     * 顶级部门的固定编码：它既是树根也是「不许删、不许改父节点」的判据。
     */
    private static final String ROOT_DEPT_CODE = "1001";
    private final DeptScopeService deptScopeService;
    private final DeptCacheService deptCacheService;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final SysEmployeeMapper sysEmployeeMapper;

    @Override
    public List<DepartmentVO> tree() {
        List<SysDepartment> allDepts = sysDepartmentMapper.selectList(null);
        List<DepartmentVO> voList = allDepts.stream().map(this::toVO).collect(Collectors.toList());
        return buildDeptTree(voList, 0L);
    }

    @Override
    public PageResult<DepartmentVO> listPage(DepartmentQueryDTO queryDTO) {
        Page<SysDepartment> page = sysDepartmentMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), buildWrapper(queryDTO));
        List<DepartmentVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<DepartmentVO> list(DepartmentQueryDTO queryDTO) {
        return sysDepartmentMapper.selectList(buildWrapper(queryDTO)).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DepartmentTreeSelectVO> selectTree(DepartmentSelectDTO selectDTO) {
        DeptSelectModeEnum mode = DeptSelectModeEnum.fromCode(selectDTO.getMode());
        List<SysDepartment> all = sysDepartmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getDelFlag, 0));
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, SysDepartment> byId = all.stream()
                .collect(Collectors.toMap(SysDepartment::getId, d -> d, (a, b) -> a));

        // 叶子判定必须基于全量父子关系，不受本次类型/权限过滤影响（与 retainLeaf 同一口径）
        Set<Long> parentIds = all.stream().map(SysDepartment::getParentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        // 类型过滤命中的业务候选；祖先链补全时不受这层过滤剔除
        Set<Long> seedIds = all.stream()
                .filter(d -> matchDeptType(d, selectDTO.getDeptType()))
                // 排除类型：未传时不过滤（matchDeptType 对空条件恒返回 true，直接取反会把全院科室滤空）
                .filter(d -> !TextUtil.hasText(selectDTO.getExcludeDeptType())
                        || !matchDeptType(d, selectDTO.getExcludeDeptType()))
                .filter(d -> matchNodeType(d, selectDTO.getNodeType()))
                .filter(d -> !Boolean.TRUE.equals(selectDTO.getOpenOnly()) || isOpenDept(d))
                .map(SysDepartment::getId)
                .collect(Collectors.toSet());

        if (!isAllScope(selectDTO.getScope())) {
            Set<Long> allowed = deptScopeService.allowedDeptIds();
            if (allowed != null) {
                if (allowed.isEmpty()) {
                    return new ArrayList<>();
                }
                seedIds.retainAll(allowed);
            }
        }
        if (seedIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 回溯补全授权/命中节点的祖先链：祖先可见、仅作分组，不因此放开可选
        Set<Long> visibleIds = new LinkedHashSet<>();
        for (Long id : seedIds) {
            collectSelfAndAncestors(id, byId, visibleIds);
        }

        Map<Long, List<SysDepartment>> childrenByParent = all.stream()
                .filter(d -> visibleIds.contains(d.getId()))
                .collect(Collectors.groupingBy(d -> d.getParentId() == null ? 0L : d.getParentId()));
        childrenByParent.values().forEach(this::sortDepts);

        List<SysDepartment> rootDepts = all.stream()
                .filter(d -> visibleIds.contains(d.getId()))
                .filter(d -> {
                    Long pid = d.getParentId();
                    return pid == null || pid == 0L || !visibleIds.contains(pid);
                })
                .collect(Collectors.toList());
        sortDepts(rootDepts);
        return rootDepts.stream()
                .map(d -> toTreeVO(d, mode, parentIds, childrenByParent, ""))
                .collect(Collectors.toList());
    }

    private DepartmentTreeSelectVO toTreeVO(SysDepartment dept, DeptSelectModeEnum mode, Set<Long> parentIds,
                                            Map<Long, List<SysDepartment>> childrenByParent, String parentPath) {
        DepartmentTreeSelectVO vo = new DepartmentTreeSelectVO();
        vo.setId(dept.getId());
        vo.setDeptCode(dept.getDeptCode());
        vo.setDeptName(dept.getDeptName());
        vo.setDeptType(dept.getDeptType());
        vo.setNodeType(dept.getNodeType());
        vo.setNodeTypeText(DeptNodeTypeEnum.getLabel(dept.getNodeType()));
        vo.setParentId(dept.getParentId());
        String fullPath = TextUtil.hasText(parentPath) ? parentPath + " / " + dept.getDeptName() : dept.getDeptName();
        vo.setFullPath(fullPath);

        String disabledReason = resolveDisabledReason(dept, mode, parentIds);
        vo.setDisabled(disabledReason != null);
        vo.setDisabledReason(disabledReason);

        List<DepartmentTreeSelectVO> children = childrenByParent.getOrDefault(dept.getId(), List.of()).stream()
                .map(c -> toTreeVO(c, mode, parentIds, childrenByParent, fullPath))
                .collect(Collectors.toList());
        vo.setChildren(children);
        return vo;
    }

    /**
     * 返回不可选原因；返回 {@code null} 表示可选。判定顺序即提示优先级：根 → 停用 → 片区/行政 → 非业务 → 模式收窄。
     */
    private String resolveDisabledReason(SysDepartment dept, DeptSelectModeEnum mode, Set<Long> parentIds) {
        if (dept.getParentId() == null || dept.getParentId() == 0L) {
            return "医疗机构根节点不可选";
        }
        if (dept.getStatus() != null && dept.getStatus() == 0) {
            return "科室已停用";
        }
        if (mode != DeptSelectModeEnum.ORG) {
            if (DeptNodeTypeEnum.isArea(dept.getNodeType())) {
                return "管理片区不是服务科室";
            }
            if (DeptNodeTypeEnum.ADMIN.getCode().equals(dept.getNodeType())) {
                return "行政后勤科室不可选";
            }
            if (!DeptNodeTypeEnum.isBusinessDept(dept.getNodeType())) {
                return "该节点不可作为业务科室";
            }
            if (mode == DeptSelectModeEnum.LEAF && parentIds.contains(dept.getId())) {
                return "请选择末级科室";
            }
        }
        return null;
    }

    private void collectSelfAndAncestors(Long id, Map<Long, SysDepartment> byId, Set<Long> acc) {
        Long current = id;
        int guard = 0;
        while (current != null && current > 0 && acc.add(current) && guard++ < 32) {
            SysDepartment dept = byId.get(current);
            if (dept == null) {
                break;
            }
            current = dept.getParentId();
        }
    }

    private boolean matchDeptType(SysDepartment dept, String deptType) {
        if (!TextUtil.hasText(deptType)) {
            return true;
        }
        Set<String> own = Arrays.stream(StringUtils.defaultString(dept.getDeptType()).split(","))
                .map(String::trim).filter(TextUtil::hasText).collect(Collectors.toSet());
        return splitCsv(deptType).stream().anyMatch(own::contains);
    }

    private boolean matchNodeType(SysDepartment dept, String nodeType) {
        if (!TextUtil.hasText(nodeType)) {
            return true;
        }
        return splitCsv(nodeType).contains(StringUtils.trimToEmpty(dept.getNodeType()));
    }

    /** 对外开放挂号：is_open=1 且未停用；停用口径与 resolveDisabledReason 一致。 */
    private boolean isOpenDept(SysDepartment dept) {
        boolean enabled = dept.getStatus() == null || dept.getStatus() == 1;
        return enabled && Integer.valueOf(1).equals(dept.getIsOpen());
    }

    private List<String> splitCsv(String csv) {
        return Arrays.stream(csv.split(",")).map(String::trim)
                .filter(TextUtil::hasText).distinct().collect(Collectors.toList());
    }

    private void sortDepts(List<SysDepartment> depts) {
        depts.sort(Comparator.comparing(SysDepartment::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SysDepartment::getId, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    @Override
    public DepartmentVO getInfo(Long deptId) {
        return toVO(sysDepartmentMapper.selectById(deptId));
    }

    @Override
    public String upsert(DepartmentUpsertDTO upsertDTO) {
        // 片区只是组织归集与审核权落点，不是服务科室：给它挂门诊/住院类型，它就会再次混进开诊与排班下拉
        if (DeptNodeTypeEnum.isArea(upsertDTO.getNodeType()) && TextUtil.hasText(upsertDTO.getDeptType())) {
            throw new BusinessException(400, "管理片区不是服务科室，科室类型必须为空");
        }
        SysDepartment dept = new SysDepartment();
        BeanUtils.copyProperties(upsertDTO, dept);
        if (dept.getId() == null) {
            dept.setDeptCode(generateDeptCode(dept.getParentId()));
            sysDepartmentMapper.insert(dept);
            deptCacheService.evict(dept.getId());
            return "新增成功";
        }
        if (dept.getId().equals(dept.getParentId())) {
            throw new BusinessException("上级部门不能是自己");
        }

        SysDepartment existingDept = sysDepartmentMapper.selectById(dept.getId());
        if (existingDept != null && ROOT_DEPT_CODE.equals(existingDept.getDeptCode())) {
            dept.setParentId(existingDept.getParentId());
        }

        sysDepartmentMapper.updateById(dept);
        deptCacheService.evict(dept.getId());
        return "修改成功";
    }

    @Override
    public void delete(Long deptId) {
        SysDepartment dept = sysDepartmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        if (dept.getDeptCode() != null && ROOT_DEPT_CODE.equals(dept.getDeptCode())) {
            throw new BusinessException("顶级部门不允许删除");
        }

        Long childCount = sysDepartmentMapper.selectCount(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getParentId, deptId));
        if (childCount > 0) {
            throw new BusinessException("该部门下有子部门，不能删除");
        }

        sysDepartmentMapper.deleteById(deptId);
        deptCacheService.evict(deptId);
    }

    private LambdaQueryWrapper<SysDepartment> buildWrapper(DepartmentQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getDeptName()), SysDepartment::getDeptName, queryDTO.getDeptName())
                .apply(TextUtil.hasText(queryDTO.getDeptType()),
                        "FIND_IN_SET({0}, dept_type) > 0", queryDTO.getDeptType())
                .eq(TextUtil.hasText(queryDTO.getNodeType()), SysDepartment::getNodeType, queryDTO.getNodeType())
                .orderByAsc(SysDepartment::getSortOrder);
        return wrapper;
    }

    /**
     * scope 是否为「显式索取全部」
     */
    private boolean isAllScope(String scope) {
        return "ALL".equalsIgnoreCase(scope);
    }

    private DepartmentVO toVO(SysDepartment dept) {
        if (dept == null) {
            return null;
        }
        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(dept, vo);
        vo.setNodeTypeText(DeptNodeTypeEnum.getLabel(dept.getNodeType()));
        // 三个人员字段（负责人/门诊审核人/护理审核人）批量一次查名，避免 N+1
        Set<Long> empIds = new HashSet<>();
        if (dept.getDeptLeaderId() != null) empIds.add(dept.getDeptLeaderId());
        if (dept.getOutpatientAuditorId() != null) empIds.add(dept.getOutpatientAuditorId());
        if (dept.getNursingAuditorId() != null) empIds.add(dept.getNursingAuditorId());
        if (dept.getScheduleAuditorId() != null) empIds.add(dept.getScheduleAuditorId());
        if (!empIds.isEmpty()) {
            Map<Long, String> nameMap = sysEmployeeMapper.selectBatchIds(empIds).stream()
                    .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getEmpName, (a, b) -> a));
            if (dept.getDeptLeaderId() != null) vo.setDeptLeaderName(nameMap.get(dept.getDeptLeaderId()));
            if (dept.getOutpatientAuditorId() != null) vo.setOutpatientAuditorName(nameMap.get(dept.getOutpatientAuditorId()));
            if (dept.getNursingAuditorId() != null) vo.setNursingAuditorName(nameMap.get(dept.getNursingAuditorId()));
            if (dept.getScheduleAuditorId() != null) vo.setScheduleAuditorName(nameMap.get(dept.getScheduleAuditorId()));
        }
        return vo;
    }

    private List<DepartmentVO> buildDeptTree(List<DepartmentVO> depts, Long parentId) {
        return depts.stream()
                .filter(dept -> parentId.equals(dept.getParentId()))
                .peek(dept -> dept.setChildren(buildDeptTree(depts, dept.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 自动生成科室编码：根据父节点编号往后加3位递增
     * 例如：父编码 100001 -> 子编码 100001001, 100001002, ...
     * 顶级部门（parentId=0）使用 1001 为前缀
     */
    private String generateDeptCode(Long parentId) {
        String prefix;
        if (parentId == null || parentId == 0L) {
            prefix = ROOT_DEPT_CODE;
        } else {
            SysDepartment parent = sysDepartmentMapper.selectById(parentId);
            if (parent == null || parent.getDeptCode() == null) {
                throw new RuntimeException("上级部门不存在");
            }
            prefix = parent.getDeptCode();
        }

        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysDepartment::getDeptCode, prefix)
                .orderByDesc(SysDepartment::getDeptCode)
                .last("LIMIT 1");
        SysDepartment maxDept = sysDepartmentMapper.selectOne(wrapper);

        int nextNum = 1;
        if (maxDept != null && maxDept.getDeptCode() != null && maxDept.getDeptCode().length() > prefix.length()) {
            String suffix = maxDept.getDeptCode().substring(prefix.length());
            try {
                nextNum = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException e) {
                nextNum = 1;
            }
        }

        return prefix + String.format("%03d", nextNum);
    }
}
