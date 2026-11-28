package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.RobotDispatchPriorityEnum;
import com.his.common.enums.RobotGoodsTypeEnum;
import com.his.common.enums.RobotStationTypeEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.iot.dto.RobotStationPageQueryDTO;
import com.his.iot.dto.RobotStationUpsertDTO;
import com.his.iot.entity.RobotStation;
import com.his.iot.mapper.RobotStationMapper;
import com.his.iot.service.RobotStationService;
import com.his.iot.support.RobotDispatchConst;
import com.his.iot.vo.RobotGoodsTypeVO;
import com.his.iot.vo.RobotPriorityVO;
import com.his.iot.vo.RobotStationOptionsVO;
import com.his.iot.vo.RobotStationTypeVO;
import com.his.iot.vo.RobotStationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 机器人站点主数据实现。
 */
@Service
@RequiredArgsConstructor
public class RobotStationServiceImpl extends ServiceImpl<RobotStationMapper, RobotStation>
        implements RobotStationService {

    @Override
    public IPage<RobotStationVO> listPage(RobotStationPageQueryDTO query) {
        String kw = TextUtil.trim(query.getKeyword());
        LambdaQueryWrapper<RobotStation> w = new LambdaQueryWrapper<RobotStation>()
                .eq(query.getStationType() != null, RobotStation::getStationType, query.getStationType())
                .eq(query.getEnabled() != null, RobotStation::getEnabled, query.getEnabled())
                .and(TextUtil.hasText(kw), x -> x
                        .like(RobotStation::getStationCode, kw)
                        .or().like(RobotStation::getStationName, kw)
                        .or().like(RobotStation::getDeptName, kw))
                .orderByAsc(RobotStation::getBuilding)
                .orderByAsc(RobotStation::getFloorNo)
                .orderByAsc(RobotStation::getSortOrder);
        Page<RobotStation> page = baseMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), w);
        Page<RobotStationVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    @Override
    public RobotStationVO getDetail(Long id) {
        RobotStation station = baseMapper.selectById(id);
        if (station == null) {
            throw new BusinessException(400, "站点不存在：" + id);
        }
        return toVo(station);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(RobotStationUpsertDTO dto) {
        String code = TextUtil.trim(dto.getStationCode()).toUpperCase();
        long dup = baseMapper.selectCount(new LambdaQueryWrapper<RobotStation>()
                .eq(RobotStation::getStationCode, code)
                .ne(dto.getId() != null, RobotStation::getId, dto.getId()));
        if (dup > 0) {
            throw new BusinessException(400, "站点编码已存在：" + code);
        }

        RobotStation station = dto.getId() == null ? new RobotStation() : baseMapper.selectById(dto.getId());
        if (station == null) {
            throw new BusinessException(400, "站点不存在：" + dto.getId());
        }
        station.setStationCode(code);
        station.setStationName(TextUtil.trim(dto.getStationName()));
        station.setStationType(dto.getStationType());
        station.setBuilding(TextUtil.trim(dto.getBuilding()));
        station.setFloorNo(dto.getFloorNo());
        station.setDeptId(dto.getDeptId());
        station.setDeptName(TextUtil.trim(dto.getDeptName()));
        station.setPickupEnabled(dto.getPickupEnabled() == null ? 1 : dto.getPickupEnabled());
        station.setReceiveEnabled(dto.getReceiveEnabled() == null ? 1 : dto.getReceiveEnabled());
        station.setDockCode(TextUtil.trim(dto.getDockCode()));
        station.setServiceWindow(TextUtil.trim(dto.getServiceWindow()));
        station.setMapX(dto.getMapX());
        station.setMapY(dto.getMapY());
        station.setEnabled(dto.getEnabled() == null ? 1 : dto.getEnabled());
        station.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        station.setRemark(dto.getRemark());

        if (dto.getId() == null) {
            baseMapper.insert(station);
        } else {
            baseMapper.updateById(station);
        }
        return station.getId();
    }

    @Override
    public void delete(Long id) {
        if (id == null || baseMapper.selectById(id) == null) {
            throw new BusinessException(400, "站点不存在：" + id);
        }
        // 唯一键不含 del_flag，必须物理删；软删会让编码永久占位
        baseMapper.purgeById(id);
    }

    @Override
    public RobotStationOptionsVO options() {
        RobotStationOptionsVO vo = new RobotStationOptionsVO();
        List<RobotStation> stations = baseMapper.selectList(new LambdaQueryWrapper<RobotStation>()
                .eq(RobotStation::getEnabled, 1)
                .orderByAsc(RobotStation::getBuilding)
                .orderByAsc(RobotStation::getFloorNo)
                .orderByAsc(RobotStation::getSortOrder));
        vo.setStations(stations.stream().map(this::toVo).toList());

        List<RobotStationTypeVO> stationTypes = new ArrayList<>();
        for (RobotStationTypeEnum type : RobotStationTypeEnum.values()) {
            RobotStationTypeVO item = new RobotStationTypeVO();
            item.setValue(type.getCode());
            item.setLabel(type.getLabel());
            stationTypes.add(item);
        }
        vo.setStationTypes(stationTypes);

        List<RobotGoodsTypeVO> goods = new ArrayList<>();
        for (RobotGoodsTypeEnum type : RobotGoodsTypeEnum.values()) {
            RobotGoodsTypeVO g = new RobotGoodsTypeVO();
            g.setCode(type.getCode());
            g.setLabel(type.getLabel());
            g.setUnitWeightKg(type.getUnitWeightKg());
            g.setSpec(type.getSpec());
            g.setMaxBoxCount((int) Math.floor(RobotDispatchConst.NOMINAL_PAYLOAD_KG / type.getUnitWeightKg()));
            goods.add(g);
        }
        vo.setGoodsTypes(goods);

        List<RobotPriorityVO> priorities = new ArrayList<>();
        for (RobotDispatchPriorityEnum p : RobotDispatchPriorityEnum.values()) {
            RobotPriorityVO item = new RobotPriorityVO();
            item.setValue(p.getCode());
            item.setLabel(p.getLabel());
            priorities.add(item);
        }
        vo.setPriorities(priorities);
        return vo;
    }

    @Override
    public RobotStation requireStation(Long id, boolean pickup) {
        String usage = pickup ? "取货站点" : "收货站点";
        RobotStation station = requireEnabled(id, usage);
        Integer flag = pickup ? station.getPickupEnabled() : station.getReceiveEnabled();
        if (!Objects.equals(1, flag)) {
            throw new BusinessException(400, usage + "未开放" + (pickup ? "取货" : "收货") + "：" + station.getStationName());
        }
        assertServiceWindow(station);
        return station;
    }

    @Override
    public RobotStation requireEnabled(Long id) {
        return requireEnabled(id, "站点");
    }

    @Override
    public RobotStation findById(Long id) {
        return id == null ? null : baseMapper.selectById(id);
    }

    @Override
    public RobotStation findApplyStation(Long deptId, String deptName) {
        if (deptId != null) {
            RobotStation byDept = baseMapper.selectOne(baseQuery()
                    .eq(RobotStation::getDeptId, deptId).last("LIMIT 1"));
            if (byDept != null) {
                return byDept;
            }
        }
        if (TextUtil.hasText(deptName)) {
            return baseMapper.selectOne(baseQuery()
                    .eq(RobotStation::getDeptName, TextUtil.trim(deptName)).last("LIMIT 1"));
        }
        return null;
    }

    /** 发起站点候选：启用 + 可取货（叫车人站的地方必须能装货），排序取第一条 */
    private LambdaQueryWrapper<RobotStation> baseQuery() {
        return new LambdaQueryWrapper<RobotStation>()
                .eq(RobotStation::getEnabled, EnableStatusEnum.ENABLED.getCode())
                .eq(RobotStation::getPickupEnabled, YesOrNoEnum.YES.getCode())
                .orderByAsc(RobotStation::getSortOrder);
    }

    private RobotStation requireEnabled(Long id, String usage) {
        if (id == null) {
            throw new BusinessException(400, usage + "不能为空");
        }
        RobotStation station = baseMapper.selectById(id);
        if (station == null) {
            throw new BusinessException(400, usage + "不存在：" + id);
        }
        if (!Objects.equals(1, station.getEnabled())) {
            throw new BusinessException(400, usage + "已停用：" + station.getStationName());
        }
        return station;
    }

    /**
     * 服务时间窗校验：格式 hh:mm-hh:mm，支持跨零点（22:00-06:00）。空表示全天。
     * 时间窗在这里拦、不在前端拦 —— 同一份口径要给叫车弹窗、仿真建单、后端补派共用。
     */
    private void assertServiceWindow(RobotStation station) {
        String window = station.getServiceWindow();
        if (!TextUtil.hasText(window)) {
            return;
        }
        String[] parts = window.trim().split("-");
        if (parts.length != 2) {
            throw new BusinessException(400, "站点服务时间窗格式不正确：" + station.getStationName() + " " + window);
        }
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(parts[0].trim());
            end = LocalTime.parse(parts[1].trim());
        } catch (RuntimeException e) {
            throw new BusinessException(400, "站点服务时间窗格式不正确：" + station.getStationName() + " " + window);
        }
        LocalTime now = LocalTime.now();
        boolean inWindow = start.isBefore(end)
                ? (!now.isBefore(start) && !now.isAfter(end))
                : (!now.isBefore(start) || !now.isAfter(end));
        if (!inWindow) {
            throw new BusinessException(400, station.getStationName() + "当前不在服务时间窗（" + window + "）");
        }
    }

    private RobotStationVO toVo(RobotStation station) {
        RobotStationVO vo = new RobotStationVO();
        vo.setId(station.getId());
        vo.setStationCode(station.getStationCode());
        vo.setStationName(station.getStationName());
        vo.setStationType(station.getStationType());
        vo.setStationTypeLabel(RobotStationTypeEnum.getLabel(station.getStationType()));
        vo.setBuilding(station.getBuilding());
        vo.setFloorNo(station.getFloorNo());
        vo.setDeptId(station.getDeptId());
        vo.setDeptName(station.getDeptName());
        vo.setPickupEnabled(station.getPickupEnabled());
        vo.setReceiveEnabled(station.getReceiveEnabled());
        vo.setDockCode(station.getDockCode());
        vo.setServiceWindow(station.getServiceWindow());
        vo.setMapX(station.getMapX());
        vo.setMapY(station.getMapY());
        vo.setEnabled(station.getEnabled());
        vo.setEnabledLabel(EnableStatusEnum.getLabel(station.getEnabled()));
        vo.setSortOrder(station.getSortOrder());
        vo.setRemark(station.getRemark());
        vo.setCreateTime(station.getCreateTime());
        vo.setUpdateTime(station.getUpdateTime());
        return vo;
    }
}
