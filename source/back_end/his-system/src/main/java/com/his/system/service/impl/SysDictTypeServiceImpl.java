package com.his.system.service.impl;

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
public class SysDictTypeServiceImpl implements SysDictTypeService {

    private final SysDictTypeMapper dictTypeMapper;

    @Override
    public List<SysDictTypeVO> selectList() {
        return dictTypeMapper.selectList(null).stream().map(t -> {
            SysDictTypeVO vo = new SysDictTypeVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SysDictTypeVO getInfo(Long typeId) {
        SysDictType dictType = dictTypeMapper.selectById(typeId);
        SysDictTypeVO vo = new SysDictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }

    @Override
    public void upsert(SysDictTypeUpsertDTO upsertDTO) {
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(upsertDTO, dictType);
        if (upsertDTO.getId() == null) {
            dictTypeMapper.insert(dictType);
        } else {
            dictTypeMapper.updateById(dictType);
        }
    }

    @Override
    public void delete(Long typeId) {
        dictTypeMapper.deleteById(typeId);
    }
}
