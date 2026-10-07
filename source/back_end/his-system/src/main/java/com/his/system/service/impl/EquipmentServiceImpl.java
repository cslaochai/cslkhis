package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.*;
import com.his.system.entity.BizEquipmentMaintain;
import com.his.system.entity.BizEquipmentMetering;
import com.his.system.entity.SysEquipment;
import com.his.system.enums.MaintainResultEnum;
import com.his.system.enums.MaintainTypeEnum;
import com.his.system.enums.MeteringResultEnum;
import com.his.system.enums.MeteringTypeEnum;
import com.his.system.mapper.BizEquipmentMaintainMapper;
import com.his.system.mapper.BizEquipmentMeteringMapper;
import com.his.system.mapper.SysEquipmentMapper;
import com.his.system.service.DictCacheService;
import com.his.system.service.EquipmentService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.EquipmentVO;
import com.his.system.vo.MaintainVO;
import com.his.system.vo.MeteringVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 设备档案/维保/计量服务。
 *
 * <p>台账口径：nextMaintainDate = lastMaintainDate + maintainCycleDays（现算不落库）；
 * 维保登记成功后回写医疗设备台账的最后维保日期（档案与记录双写一致性）；
 * 维保记录删除（录错）后按剩余记录重算最近维保日期，无剩余记录时保留原值不猜。
 *
 * <p>码值口径全部走 {@code com.his.system.equipment.enums}，码值合法性由DTO 上的 {@code @InEnum} 校验，
 * 本层不再手写containsKey 抛异常（AGENTS.md §13/§14）。
 */
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final SysEquipmentMapper sysEquipmentMapper;
    private final BizEquipmentMaintainMapper bizEquipmentMaintainMapper;
    private final BizEquipmentMeteringMapper bizEquipmentMeteringMapper;
    private DictCacheService dictCacheService;

    // 设备台账

    public IPage<EquipmentVO> listPage(EquipmentQueryPageDTO q) {
        String kw = TextUtil.trim(q.getKeyword());
        LambdaQueryWrapper<SysEquipment> w = new LambdaQueryWrapper<SysEquipment>()
                .eq(q.getCategory() != null, SysEquipment::getCategory, q.getCategory())
                .eq(q.getStatus() != null, SysEquipment::getStatus, q.getStatus())
                .and(StringUtils.hasText(kw), x -> x
                        .like(SysEquipment::getEquipmentCode, kw)
                        .or().like(SysEquipment::getEquipmentName, kw)
                        .or().like(SysEquipment::getModel, kw)
                        .or().like(SysEquipment::getDeptName, kw))
                .orderByDesc(SysEquipment::getId);
        IPage<SysEquipment> page = sysEquipmentMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        Map<Long, LocalDate> meteringMap = latestMeteringValidMap(
                page.getRecords().stream().map(SysEquipment::getId).toList());
        return page.convert(e -> toVo(e, meteringMap.get(e.getId())));
    }

    public EquipmentVO getDetailById(Long equipmentId) {
        SysEquipment e = requireEquipment(equipmentId);
        EquipmentVO vo = toVo(e, latestMeteringValidMap(List.of(equipmentId)).get(equipmentId));
        vo.setRecentMaintains(bizEquipmentMaintainMapper.selectList(new LambdaQueryWrapper<BizEquipmentMaintain>()
                        .eq(BizEquipmentMaintain::getEquipmentId, equipmentId)
                        .orderByDesc(BizEquipmentMaintain::getMaintainDate)
                        .orderByDesc(BizEquipmentMaintain::getId)
                        .last("LIMIT 10"))
                .stream().map(this::toMaintainVo).toList());
        vo.setRecentMeterings(bizEquipmentMeteringMapper.selectList(new LambdaQueryWrapper<BizEquipmentMetering>()
                        .eq(BizEquipmentMetering::getEquipmentId, equipmentId)
                        .orderByDesc(BizEquipmentMetering::getMeteringDate)
                        .orderByDesc(BizEquipmentMetering::getId)
                        .last("LIMIT 10"))
                .stream().map(this::toMeteringVo).toList());
        return vo;
    }

    public IPage<MaintainVO> maintainListPage(MaintainQueryPageDTO q) {
        requireEquipment(q.getEquipmentId());
        LambdaQueryWrapper<BizEquipmentMaintain> w = new LambdaQueryWrapper<BizEquipmentMaintain>()
                .eq(BizEquipmentMaintain::getEquipmentId, q.getEquipmentId())
                .eq(q.getMaintainType() != null, BizEquipmentMaintain::getMaintainType, q.getMaintainType())
                .orderByDesc(BizEquipmentMaintain::getMaintainDate)
                .orderByDesc(BizEquipmentMaintain::getId);
        return bizEquipmentMaintainMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w)
                .convert(this::toMaintainVo);
    }

    // 计量

    @Transactional(rollbackFor = Exception.class)
    public MaintainVO maintainCreate(MaintainCreateDTO dto) {
        // 码值合法性已由 DTO 的 @InEnum 把关，这里只管跨字段业务规则
        if (dto.getNextMaintainDate() != null && dto.getNextMaintainDate().isBefore(dto.getMaintainDate())) {
            throw new BusinessException("下次维保日期不能早于本次维保日期");
        }
        SysEquipment e = requireEquipment(dto.getEquipmentId());

        BizEquipmentMaintain m = new BizEquipmentMaintain();
        m.setEquipmentId(e.getId());
        m.setEquipmentCode(e.getEquipmentCode());
        m.setEquipmentName(e.getEquipmentName());
        m.setMaintainType(dto.getMaintainType());
        m.setMaintainDate(dto.getMaintainDate());
        m.setNextMaintainDate(dto.getNextMaintainDate());
        m.setCost(dto.getCost());
        m.setFaultDesc(TextUtil.trim(dto.getFaultDesc()));
        m.setHandleResult(TextUtil.trim(dto.getHandleResult()));
        m.setMaintainResult(dto.getMaintainResult() == null
                ? MaintainResultEnum.NORMAL.getCode() : dto.getMaintainResult());
        m.setHandlerName(StringUtils.hasText(dto.getHandlerName()) ? dto.getHandlerName().trim()
                : UserUtils.getCurrentUser().getRealName());
        m.setCreateBy(UserUtils.getCurrentUser().getRealName());
        bizEquipmentMaintainMapper.insert(m);

        // 回写档案最近维保日期（维保闭环的关键动作）；updateTime 由实体 @TableField 自动填充
        e.setLastMaintainDate(dto.getMaintainDate());
        e.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        sysEquipmentMapper.updateById(e);
        return toMaintainVo(m);
    }

    @Transactional(rollbackFor = Exception.class)
    public void maintainDelete(Long id) {
        BizEquipmentMaintain m = bizEquipmentMaintainMapper.selectById(id);
        if (m == null || Objects.equals(m.getDelFlag(), 1)) {
            throw new BusinessException("维保记录不存在或已删除");
        }
        bizEquipmentMaintainMapper.deleteById(id);
        // 按剩余记录重算最近维保日期；无剩余记录时保留原值（不猜史实）
        BizEquipmentMaintain latest = bizEquipmentMaintainMapper.selectList(new LambdaQueryWrapper<BizEquipmentMaintain>()
                        .eq(BizEquipmentMaintain::getEquipmentId, m.getEquipmentId())
                        .orderByDesc(BizEquipmentMaintain::getMaintainDate)
                        .orderByDesc(BizEquipmentMaintain::getId)
                        .last("LIMIT 1"))
                .stream().findFirst().orElse(null);
        if (latest != null) {
            SysEquipment e = sysEquipmentMapper.selectById(m.getEquipmentId());
            if (e != null && !Objects.equals(e.getDelFlag(), 1)
                    && !Objects.equals(e.getLastMaintainDate(), latest.getMaintainDate())) {
                e.setLastMaintainDate(latest.getMaintainDate());
                e.setUpdateBy(UserUtils.getCurrentUser().getRealName());
                sysEquipmentMapper.updateById(e);
            }
        }
    }

    public IPage<MeteringVO> meteringListPage(MeteringQueryPageDTO q) {
        requireEquipment(q.getEquipmentId());
        LambdaQueryWrapper<BizEquipmentMetering> w = new LambdaQueryWrapper<BizEquipmentMetering>()
                .eq(BizEquipmentMetering::getEquipmentId, q.getEquipmentId())
                .eq(q.getMeteringType() != null, BizEquipmentMetering::getMeteringType, q.getMeteringType())
                .orderByDesc(BizEquipmentMetering::getMeteringDate)
                .orderByDesc(BizEquipmentMetering::getId);
        return bizEquipmentMeteringMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w)
                .convert(this::toMeteringVo);
    }

    // 私有

    @Transactional(rollbackFor = Exception.class)
    public MeteringVO meteringCreate(MeteringCreateDTO dto) {
        if (dto.getValidUntil().isBefore(dto.getMeteringDate())) {
            throw new BusinessException("有效期至不能早于计量日期");
        }
        SysEquipment e = requireEquipment(dto.getEquipmentId());

        BizEquipmentMetering m = new BizEquipmentMetering();
        m.setEquipmentId(e.getId());
        m.setEquipmentCode(e.getEquipmentCode());
        m.setEquipmentName(e.getEquipmentName());
        m.setMeteringType(dto.getMeteringType());
        m.setMeteringDate(dto.getMeteringDate());
        m.setValidUntil(dto.getValidUntil());
        m.setMeteringResult(dto.getMeteringResult() == null
                ? MeteringResultEnum.QUALIFIED.getCode() : dto.getMeteringResult());
        m.setCertNo(TextUtil.trim(dto.getCertNo()));
        m.setAgency(TextUtil.trim(dto.getAgency()));
        m.setCreateBy(UserUtils.getCurrentUser().getRealName());
        bizEquipmentMeteringMapper.insert(m);
        return toMeteringVo(m);
    }

    @Transactional(rollbackFor = Exception.class)
    public void meteringDelete(Long id) {
        BizEquipmentMetering m = bizEquipmentMeteringMapper.selectById(id);
        if (m == null || Objects.equals(m.getDelFlag(), 1)) {
            throw new BusinessException("计量记录不存在或已删除");
        }
        bizEquipmentMeteringMapper.deleteById(id);
    }

    private SysEquipment requireEquipment(Long equipmentId) {
        SysEquipment e = sysEquipmentMapper.selectById(equipmentId);
        if (e == null || Objects.equals(e.getDelFlag(), 1)) {
            throw new BusinessException("设备档案不存在（id=" + equipmentId + "）");
        }
        return e;
    }

    /**
     * 设备 → 最近一次计量有效期至
     */
    private Map<Long, LocalDate> latestMeteringValidMap(List<Long> equipmentIds) {
        if (equipmentIds.isEmpty()) {
            return Map.of();
        }
        return bizEquipmentMeteringMapper.selectList(new LambdaQueryWrapper<BizEquipmentMetering>()
                        .in(BizEquipmentMetering::getEquipmentId, equipmentIds)
                        .orderByAsc(BizEquipmentMetering::getValidUntil))
                .stream()
                .collect(Collectors.toMap(BizEquipmentMetering::getEquipmentId,
                        BizEquipmentMetering::getValidUntil, (a, b) -> b.isAfter(a) ? b : a));
    }

    private EquipmentVO toVo(SysEquipment e, LocalDate meteringValidUntil) {
        EquipmentVO vo = new EquipmentVO();
        vo.setId(e.getId());
        vo.setEquipmentCode(e.getEquipmentCode());
        vo.setEquipmentName(e.getEquipmentName());
        vo.setCategory(e.getCategory());
        vo.setCategoryText(dictCacheService.getDicDataLabel("biz_system_equipCategoryEnum", e.getCategory()));
        vo.setDeptId(e.getDeptId());
        vo.setDeptName(e.getDeptName());
        vo.setBrand(e.getBrand());
        vo.setModel(e.getModel());
        vo.setPurchaseDate(e.getPurchaseDate());
        vo.setPurchasePrice(e.getPurchasePrice());
        vo.setStatus(e.getStatus());
        vo.setStatusText(dictCacheService.getDicDataLabel("biz_system_equipStatusEnum", e.getStatus()));
        vo.setMaintainCycleDays(e.getMaintainCycleDays());
        vo.setLastMaintainDate(e.getLastMaintainDate());
        if (e.getLastMaintainDate() != null && e.getMaintainCycleDays() != null && e.getMaintainCycleDays() > 0) {
            vo.setNextMaintainDate(e.getLastMaintainDate().plusDays(e.getMaintainCycleDays()));
        }
        vo.setMeteringValidUntil(meteringValidUntil);
        vo.setMeteringExpired(meteringValidUntil != null && meteringValidUntil.isBefore(LocalDate.now()));
        vo.setRemark(e.getRemark());
        return vo;
    }

    private MaintainVO toMaintainVo(BizEquipmentMaintain m) {
        MaintainVO vo = new MaintainVO();
        vo.setId(m.getId());
        vo.setEquipmentId(m.getEquipmentId());
        vo.setEquipmentCode(m.getEquipmentCode());
        vo.setEquipmentName(m.getEquipmentName());
        vo.setMaintainType(m.getMaintainType());
        vo.setMaintainTypeText(MaintainTypeEnum.getText(m.getMaintainType()));
        vo.setMaintainDate(m.getMaintainDate());
        vo.setNextMaintainDate(m.getNextMaintainDate());
        vo.setCost(m.getCost());
        vo.setFaultDesc(m.getFaultDesc());
        vo.setHandleResult(m.getHandleResult());
        vo.setMaintainResult(m.getMaintainResult());
        vo.setMaintainResultText(MaintainResultEnum.getText(m.getMaintainResult()));
        vo.setHandlerName(m.getHandlerName());
        vo.setCreateBy(m.getCreateBy());
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }

    private MeteringVO toMeteringVo(BizEquipmentMetering m) {
        MeteringVO vo = new MeteringVO();
        vo.setId(m.getId());
        vo.setEquipmentId(m.getEquipmentId());
        vo.setEquipmentCode(m.getEquipmentCode());
        vo.setEquipmentName(m.getEquipmentName());
        vo.setMeteringType(m.getMeteringType());
        vo.setMeteringTypeText(MeteringTypeEnum.getText(m.getMeteringType()));
        vo.setMeteringDate(m.getMeteringDate());
        vo.setValidUntil(m.getValidUntil());
        vo.setExpired(m.getValidUntil() != null && m.getValidUntil().isBefore(LocalDate.now()));
        vo.setMeteringResult(m.getMeteringResult());
        vo.setMeteringResultText(MeteringResultEnum.getText(m.getMeteringResult()));
        vo.setCertNo(m.getCertNo());
        vo.setAgency(m.getAgency());
        vo.setCreateBy(m.getCreateBy());
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }
}