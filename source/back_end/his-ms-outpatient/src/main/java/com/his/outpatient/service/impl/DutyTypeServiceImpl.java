package com.his.outpatient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.DictTypeConst;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.outpatient.dto.DutyTypeUpsertDTO;
import com.his.outpatient.entity.BizDutyPost;
import com.his.outpatient.entity.BizDutyRoster;
import com.his.outpatient.entity.BizDutyType;
import com.his.outpatient.mapper.BizDutyPostMapper;
import com.his.outpatient.mapper.BizDutyRosterMapper;
import com.his.outpatient.mapper.BizDutyTypeMapper;
import com.his.outpatient.service.DutyTypeService;
import com.his.outpatient.vo.DutyTypeVO;
import com.his.spi.service.DictCacheService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.DeptBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 值班类型（值班线）维护服务实现。
 *
 * <p>值班线是配置不是枚举：院办自己增删，业务侧只认「这条线归口哪个科室」与
 * 「排什么人算合格」。两个判定都在这里收口，排班服务只调用不重复实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DutyTypeServiceImpl extends ServiceImpl<BizDutyTypeMapper, BizDutyType>
        implements DutyTypeService {

    /**
     * 系统管理员角色码：跨线维护（切岗位切换不影响它）
     */
    private static final String ADMIN_ROLE_CODE = "admin";

    private final BizDutyPostMapper bizDutyPostMapper;

    private final BizDutyRosterMapper bizDutyRosterMapper;

    private final SystemFacadeService systemFacadeService;

    private final DictCacheService dictCacheService;

    @Override
    public List<DutyTypeVO> listVO() {
        CurrentUser user = UserUtils.getCurrentUser();
        List<BizDutyType> rows = list(new LambdaQueryWrapper<BizDutyType>()
                .orderByAsc(BizDutyType::getSortNo)
                .orderByAsc(BizDutyType::getId));
        List<DutyTypeVO> out = new ArrayList<>(rows.size());
        for (BizDutyType row : rows) {
            DutyTypeVO vo = toVO(row);
            vo.setManageable(canManage(user, row));
            out.add(vo);
        }
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(DutyTypeUpsertDTO dto) {
        BizDutyType exists = getOne(new LambdaQueryWrapper<BizDutyType>()
                .eq(BizDutyType::getTypeCode, dto.getTypeCode()), false);
        if (null != exists && !Objects.equals(exists.getId(), dto.getId())) {
            throw new BusinessException("类型编码「" + dto.getTypeCode() + "」已被占用");
        }
        BizDutyType entity = null == dto.getId() ? new BizDutyType() : getById(dto.getId());
        if (null == entity) {
            throw new BusinessException("值班类型不存在，可能已被删除");
        }
        DeptBriefVO dept = systemFacadeService.findDepartment(dto.getManageDeptId());
        if (null == dept) {
            throw new BusinessException("归口科室不存在");
        }
        entity.setTypeCode(dto.getTypeCode().trim().toUpperCase());
        entity.setTypeName(dto.getTypeName().trim());
        entity.setManageDeptId(dto.getManageDeptId());
        entity.setManageDeptName(dept.getDeptName());
        entity.setRequiredStaffType(dto.getRequiredStaffType());
        entity.setRequiredPositions(normalizePositions(dto.getRequiredPositions()));
        entity.setLeaderLayer(null == dto.getLeaderLayer() ? 0 : dto.getLeaderLayer());
        entity.setSortNo(null == dto.getSortNo() ? 99 : dto.getSortNo());
        entity.setStatus(null == dto.getStatus() ? 1 : dto.getStatus());
        entity.setRemark(dto.getRemark());
        saveOrUpdate(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizDutyType type = getById(id);
        if (null == type) {
            return;
        }
        long postCount = bizDutyPostMapper.selectCount(new LambdaQueryWrapper<BizDutyPost>()
                .eq(BizDutyPost::getDutyTypeId, id));
        if (postCount > 0) {
            throw new BusinessException("这条值班线下还有 " + postCount + " 个点位在用，先停用或调整点位再删");
        }
        long rosterCount = bizDutyRosterMapper.selectCount(new LambdaQueryWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getDutyTypeId, id));
        if (rosterCount > 0) {
            throw new BusinessException("这条值班线已有 " + rosterCount + " 条值班记录，只可停用不可删");
        }
        removeById(id);
    }

    @Override
    public BizDutyType requireEnabled(Long dutyTypeId) {
        if (null == dutyTypeId) {
            throw new BusinessException("请选择值班类型");
        }
        BizDutyType type = getById(dutyTypeId);
        if (null == type) {
            throw new BusinessException("值班类型不存在，请先在「值班类型」页配置");
        }
        if (!Objects.equals(1, type.getStatus())) {
            throw new BusinessException("值班类型「" + type.getTypeName() + "」已停用");
        }
        return type;
    }

    @Override
    public Long idOfCode(String typeCode) {
        if (!TextUtil.hasText(typeCode)) {
            return null;
        }
        BizDutyType type = getOne(new LambdaQueryWrapper<BizDutyType>()
                .eq(BizDutyType::getTypeCode, typeCode.trim().toUpperCase()), false);
        return null == type ? null : type.getId();
    }

    /**
     * 当前登录人能否排这条线：系统管理员不限，其余按「当前岗位科室 = 类型归口科室」比对。
     *
     * <p>按线切权限而不另发权限码：同一个人换了岗位，能排的线随之变化，
     * 这正是岗位制要的效果；另发一套 dutyType:medical:edit 之类的码，切了岗还得有人去改授权。
     */
    private boolean canManage(CurrentUser user, BizDutyType type) {
        if (null == user) {
            return false;
        }
        if (null != user.getCurrentRole() && user.getRoles() != null
                && user.getRoles().contains(ADMIN_ROLE_CODE)) {
            return true;
        }
        return null != user.getDeptId() && Objects.equals(user.getDeptId(), type.getManageDeptId());
    }

    private DutyTypeVO toVO(BizDutyType row) {
        DutyTypeVO vo = new DutyTypeVO();
        vo.setId(row.getId());
        vo.setTypeCode(row.getTypeCode());
        vo.setTypeName(row.getTypeName());
        vo.setManageDeptId(row.getManageDeptId());
        vo.setManageDeptName(row.getManageDeptName());
        vo.setRequiredStaffType(row.getRequiredStaffType());
        vo.setRequiredStaffTypeText(StaffTypeEnum.getLabel(row.getRequiredStaffType()));
        vo.setRequiredPositions(row.getRequiredPositions());
        vo.setRequiredPositionsText(positionsText(row.getRequiredPositions()));
        vo.setLeaderLayer(row.getLeaderLayer());
        vo.setSortNo(row.getSortNo());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        return vo;
    }

    /**
     * 职务码值串归一：去空格、去空项、只允许纯数字码值（字典 hospital_position）
     */
    private String normalizePositions(String raw) {
        if (!TextUtil.hasText(raw)) {
            return null;
        }
        List<String> codes = new ArrayList<>();
        for (String part : raw.split(",")) {
            String code = part.trim();
            if (!TextUtil.hasText(code)) {
                continue;
            }
            if (!code.matches("\\d{1,3}")) {
                throw new BusinessException("职务码值只能是数字（见字典 hospital_position）");
            }
            if (!codes.contains(code)) {
                codes.add(code);
            }
        }
        return codes.isEmpty() ? null : String.join(",", codes);
    }

    /**
     * 职务码值 → 文案（字典 hospital_position）
     */
    private String positionsText(String codes) {
        if (!TextUtil.hasText(codes)) {
            return "不限";
        }
        List<String> labels = new ArrayList<>();
        for (String code : Arrays.asList(codes.split(","))) {
            String label = dictCacheService.getDicDataLabel(DictTypeConst.HOSPITAL_POSITION, code.trim());
            labels.add(!TextUtil.hasText(label) ? code.trim() : label);
        }
        return String.join(" / ", labels);
    }
}
