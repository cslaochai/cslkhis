package com.his.system.service.impl;

import com.his.common.exception.BusinessException;
import com.his.system.dto.DictDataQueryDTO;
import com.his.system.dto.SysDictDataUpsertDTO;
import com.his.system.entity.SysDictData;
import com.his.system.mapper.SysDictDataMapper;
import com.his.system.service.DictCacheService;
import com.his.system.service.SysDictDataService;
import com.his.system.vo.DictTypeGroupVO;
import com.his.system.vo.SysDictDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl implements SysDictDataService {

    private final SysDictDataMapper dictDataMapper;
    private final DictCacheService dictCacheService;

    @Override
    public List<SysDictDataVO> selectList(DictDataQueryDTO queryDTO) {
        return toVOList(dictCacheService.getDictDataByType(queryDTO.getDictTypes()));
    }

    @Override
    public List<DictTypeGroupVO> selectGroup(DictDataQueryDTO queryDTO) {
        List<DictTypeGroupVO> result = new ArrayList<>();
        String dictTypes = queryDTO.getDictTypes();
        if (dictTypes == null || dictTypes.isEmpty()) {
            return result;
        }

        String[] dictTypeList = dictTypes.split(",");
        if (dictTypeList.length > 8) {
            throw new BusinessException("数据字典每次最多只能查询8个！");
        }
        for (String type : dictTypeList) {
            String trimType = type.trim();
            if (!trimType.isEmpty()) {
                DictTypeGroupVO group = new DictTypeGroupVO();
                group.setDictType(trimType);
                group.setDataList(toVOList(dictCacheService.getDictDataByType(trimType)));
                result.add(group);
            }
        }
        return result;
    }

    @Override
    public SysDictDataVO getInfo(Long dataId) {
        SysDictData dictData = dictDataMapper.selectById(dataId);
        SysDictDataVO vo = new SysDictDataVO();
        BeanUtils.copyProperties(dictData, vo);
        return vo;
    }

    @Override
    public void upsert(SysDictDataUpsertDTO upsertDTO) {
        SysDictData dictData = new SysDictData();
        BeanUtils.copyProperties(upsertDTO, dictData);
        if (upsertDTO.getId() == null) {
            dictDataMapper.insert(dictData);
        } else {
            dictDataMapper.updateById(dictData);
        }
        dictCacheService.refreshDictCache(dictData.getDictType());
    }

    @Override
    public void delete(Long dataId) {
        SysDictData dictData = dictDataMapper.selectById(dataId);
        if (dictData != null) {
            dictDataMapper.deleteById(dataId);
            dictCacheService.refreshDictCache(dictData.getDictType());
        }
    }

    private List<SysDictDataVO> toVOList(List<SysDictData> dataList) {
        return dataList.stream().map(d -> {
            SysDictDataVO vo = new SysDictDataVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
