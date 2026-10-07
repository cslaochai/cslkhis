package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.util.TextUtil;
import com.his.system.dto.ClinicRoomQueryDTO;
import com.his.system.dto.ClinicRoomUpsertDTO;
import com.his.system.entity.SysClinicRoom;
import com.his.system.mapper.SysClinicRoomMapper;
import com.his.system.service.SysClinicRoomService;
import com.his.system.vo.ClinicRoomVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysClinicRoomServiceImpl extends ServiceImpl<SysClinicRoomMapper, SysClinicRoom> implements SysClinicRoomService {

    @Override
    public PageResult<ClinicRoomVO> listPage(ClinicRoomQueryDTO queryDTO) {
        Page<SysClinicRoom> page = baseMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                buildWrapper(queryDTO));
        List<ClinicRoomVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<ClinicRoomVO> listAll(ClinicRoomQueryDTO queryDTO) {
        return baseMapper.selectList(buildWrapper(queryDTO)).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ClinicRoomVO getInfo(Long roomId) {
        return toVO(baseMapper.selectById(roomId));
    }

    @Override
    public String upsert(ClinicRoomUpsertDTO upsertDTO) {
        SysClinicRoom room = new SysClinicRoom();
        BeanUtils.copyProperties(upsertDTO, room);
        if (room.getId() == null) {
            baseMapper.insert(room);
            return "新增成功";
        }
        baseMapper.updateById(room);
        return "修改成功";
    }

    @Override
    public void delete(Long roomId) {
        baseMapper.deleteById(roomId);
    }

    private LambdaQueryWrapper<SysClinicRoom> buildWrapper(ClinicRoomQueryDTO queryDTO) {
        LambdaQueryWrapper<SysClinicRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getName()), SysClinicRoom::getName, queryDTO.getName())
                .eq(queryDTO.getDeptId() != null, SysClinicRoom::getDeptId, queryDTO.getDeptId())
                .eq(queryDTO.getStatus() != null, SysClinicRoom::getStatus, queryDTO.getStatus())
                .orderByAsc(SysClinicRoom::getCode);
        return wrapper;
    }

    private ClinicRoomVO toVO(SysClinicRoom room) {
        if (room == null) {
            return null;
        }
        ClinicRoomVO vo = new ClinicRoomVO();
        BeanUtils.copyProperties(room, vo);
        return vo;
    }
}
