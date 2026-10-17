package com.his.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.system.dto.SysDictTypeUpsertDTO;
import com.his.system.entity.SysDictType;
import com.his.system.mapper.SysDictTypeMapper;
import com.his.system.service.SysDictTypeService;
import com.his.system.vo.SysDictTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictTypeMapper sysDictTypeMapper;

    @Override
    public List<SysDictTypeVO> selectList() {
        return sysDictTypeMapper.selectList(null).stream().map(t -> {
            SysDictTypeVO vo = new SysDictTypeVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SysDictTypeVO getInfo(Long typeId) {
        SysDictType dictType = sysDictTypeMapper.selectById(typeId);
        SysDictTypeVO vo = new SysDictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }

    @Override
    public void upsert(SysDictTypeUpsertDTO upsertDTO) {
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(upsertDTO, dictType);
        if (upsertDTO.getId() == null) {
            sysDictTypeMapper.insert(dictType);
        } else {
            sysDictTypeMapper.updateById(dictType);
        }
    }

    @Override
    public void delete(Long typeId) {
        sysDictTypeMapper.deleteById(typeId);
    }
}
