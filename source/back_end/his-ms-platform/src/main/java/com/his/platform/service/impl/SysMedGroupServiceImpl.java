package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.platform.dto.MedGroupQueryDTO;
import com.his.platform.dto.MedGroupUpsertDTO;
import com.his.platform.entity.SysDepartment;
import com.his.platform.entity.SysEmployee;
import com.his.platform.entity.SysMedGroup;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.mapper.SysMedGroupMapper;
import com.his.platform.service.SysMedGroupService;
import com.his.platform.vo.MedGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 医疗组 / 主诊组服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysMedGroupServiceImpl extends ServiceImpl<SysMedGroupMapper, SysMedGroup>
        implements SysMedGroupService {

    private static final String GROUP_CODE_PREFIX = "MG";

    private final SysMedGroupMapper sysMedGroupMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final SysEmployeeMapper sysEmployeeMapper;

    @Override
    public PageResult<MedGroupVO> listPage(MedGroupQueryDTO queryDTO) {
        Page<SysMedGroup> page = sysMedGroupMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), buildWrapper(queryDTO));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toVOList(page.getRecords()));
    }

    @Override
    public List<MedGroupVO> list(MedGroupQueryDTO queryDTO) {
        return toVOList(sysMedGroupMapper.selectList(buildWrapper(queryDTO)));
    }

    @Override
    public List<MedGroupVO> selectList(Long deptId) {
        return toVOList(sysMedGroupMapper.selectList(new LambdaQueryWrapper<SysMedGroup>()
                .eq(SysMedGroup::getStatus, 1)
                .eq(deptId != null, SysMedGroup::getDeptId, deptId)
                .orderByAsc(SysMedGroup::getSortOrder)
                .orderByAsc(SysMedGroup::getGroupCode)));
    }

    @Override
    public MedGroupVO getInfo(Long id) {
        SysMedGroup group = sysMedGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("医疗组不存在");
        }
        return toVOList(Collections.singletonList(group)).get(0);
    }

    @Override
    public String upsert(MedGroupUpsertDTO upsertDTO) {
        if (sysDepartmentMapper.selectById(upsertDTO.getDeptId()) == null) {
            throw new BusinessException("所属科室不存在");
        }
        SysMedGroup group = new SysMedGroup();
        BeanUtils.copyProperties(upsertDTO, group);
        boolean isCreate = upsertDTO.getId() == null;

        if (isCreate) {
            if (!TextUtil.hasText(group.getGroupCode())) {
                group.setGroupCode(String.format("%s%03d",
                        GROUP_CODE_PREFIX, sysMedGroupMapper.selectMaxGroupSeq() + 1));
            } else if (codeExists(group.getGroupCode(), null)) {
                throw new BusinessException("医疗组编码已存在：" + group.getGroupCode());
            }
            if (group.getStatus() == null) {
                group.setStatus(1);
            }
            if (group.getSortOrder() == null) {
                group.setSortOrder(0);
            }
            sysMedGroupMapper.insert(group);
            return "新增成功";
        }

        if (sysMedGroupMapper.selectById(group.getId()) == null) {
            throw new BusinessException("医疗组不存在");
        }
        if (TextUtil.hasText(group.getGroupCode()) && codeExists(group.getGroupCode(), group.getId())) {
            throw new BusinessException("医疗组编码已存在：" + group.getGroupCode());
        }
        sysMedGroupMapper.updateById(group);
        return "修改成功";
    }

    @Override
    public void delete(Long id) {
        SysMedGroup group = sysMedGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("医疗组不存在");
        }
        // 前置校验：组内还有医师时不允许删，否则员工 group_id 会指向已删的组
        Long memberCount = sysEmployeeMapper.selectCount(
                new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getGroupId, id));
        if (memberCount != null && memberCount > 0) {
            throw new BusinessException("该医疗组还有 " + memberCount + " 名医师，请先调整医师分组再删除");
        }
        // 物理删除：group_code 上有唯一键，逻辑删除会让「删了再建同编码」撞 Duplicate entry。
        // 前置校验已挡住组内还有医师的情况，删掉不会留下悬空的 sys_employee.group_id。
        sysMedGroupMapper.deleteById(id);
    }

    private LambdaQueryWrapper<SysMedGroup> buildWrapper(MedGroupQueryDTO queryDTO) {
        return new LambdaQueryWrapper<SysMedGroup>()
                .like(TextUtil.hasText(queryDTO.getGroupName()), SysMedGroup::getGroupName, queryDTO.getGroupName())
                .like(TextUtil.hasText(queryDTO.getGroupCode()), SysMedGroup::getGroupCode, queryDTO.getGroupCode())
                .eq(queryDTO.getDeptId() != null, SysMedGroup::getDeptId, queryDTO.getDeptId())
                .eq(queryDTO.getStatus() != null, SysMedGroup::getStatus, queryDTO.getStatus())
                .orderByAsc(SysMedGroup::getSortOrder)
                .orderByAsc(SysMedGroup::getGroupCode);
    }

    private boolean codeExists(String groupCode, Long excludeId) {
        LambdaQueryWrapper<SysMedGroup> wrapper = new LambdaQueryWrapper<SysMedGroup>()
                .eq(SysMedGroup::getGroupCode, groupCode);
        if (excludeId != null) {
            wrapper.ne(SysMedGroup::getId, excludeId);
        }
        Long count = sysMedGroupMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private List<MedGroupVO> toVOList(List<SysMedGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> deptIds = groups.stream().map(SysMedGroup::getDeptId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> deptNameMap = deptIds.isEmpty() ? Collections.emptyMap()
                : sysDepartmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDeptName, (a, b) -> a));

        Set<Long> leaderIds = groups.stream().map(SysMedGroup::getLeaderId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> leaderNameMap = leaderIds.isEmpty() ? Collections.emptyMap()
                : sysEmployeeMapper.selectBatchIds(leaderIds).stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getEmpName, (a, b) -> a));

        // 组内人数：一次查出来再按 group_id 归并，避免 N+1
        Map<Long, Long> memberCountMap = countMembers(groups.stream().map(SysMedGroup::getId)
                .collect(Collectors.toSet()));

        return groups.stream().map(group -> {
            MedGroupVO vo = new MedGroupVO();
            BeanUtils.copyProperties(group, vo);
            if (group.getDeptId() != null) {
                vo.setDeptName(deptNameMap.get(group.getDeptId()));
            }
            if (group.getLeaderId() != null) {
                vo.setLeaderName(leaderNameMap.get(group.getLeaderId()));
            }
            vo.setMemberCount(memberCountMap.getOrDefault(group.getId(), 0L).intValue());
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, Long> countMembers(Set<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysEmployee> members = sysEmployeeMapper.selectList(new LambdaQueryWrapper<SysEmployee>()
                .select(SysEmployee::getId, SysEmployee::getGroupId)
                .in(SysEmployee::getGroupId, groupIds));
        return members.stream()
                .filter(e -> e.getGroupId() != null)
                .collect(Collectors.groupingBy(SysEmployee::getGroupId, Collectors.counting()));
    }
}
