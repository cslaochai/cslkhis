package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageResult;
import com.mis.common.exception.BusinessException;
import com.mis.config.LoginUser;
import com.mis.config.UserContext;
import com.mis.dto.DictDataUpsertDTO;
import com.mis.dto.DictTypeUpsertDTO;
import com.mis.entity.MssDictData;
import com.mis.entity.MssDictType;
import com.mis.mapper.MssDictDataMapper;
import com.mis.mapper.MssDictTypeMapper;
import com.mis.service.SystemDictService;
import com.mis.vo.DictGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典管理实现
 */
@Service
@RequiredArgsConstructor
public class SystemDictServiceImpl implements SystemDictService {

    private final MssDictTypeMapper dictTypeMapper;
    private final MssDictDataMapper dictDataMapper;

    @Override
    public PageResult<MssDictType> typePage(String keyword, long pageNum, long pageSize) {
        Page<MssDictType> page = dictTypeMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MssDictType>()
                        .and(keyword != null && !keyword.isEmpty(), w -> w
                                .like(MssDictType::getDictName, keyword)
                                .or()
                                .like(MssDictType::getDictType, keyword))
                        .orderByAsc(MssDictType::getId));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<MssDictType> typeSelectList() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<MssDictType>()
                .eq(MssDictType::getStatus, 1)
                .orderByAsc(MssDictType::getId));
    }

    @Override
    public MssDictType typeGetById(Long id) {
        MssDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException("字典类型不存在");
        }
        return type;
    }

    @Override
    public MssDictType typeUpsert(DictTypeUpsertDTO dto) {
        MssDictType type = new MssDictType();
        BeanUtils.copyProperties(dto, type);
        String operator = operatorName();
        if (dto.getId() == null) {
            Long exist = dictTypeMapper.selectCount(new LambdaQueryWrapper<MssDictType>()
                    .eq(MssDictType::getDictType, dto.getDictType()));
            if (exist != null && exist > 0) {
                throw new BusinessException("字典类型已存在");
            }
            type.setDictSource(2);
            type.setCreateBy(operator);
            dictTypeMapper.insert(type);
        } else {
            if (dictTypeMapper.selectById(dto.getId()) == null) {
                throw new BusinessException("字典类型不存在");
            }
            type.setUpdateBy(operator);
            dictTypeMapper.updateById(type);
        }
        return type;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void typeDelete(Long id) {
        MssDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException("字典类型不存在");
        }
        dictTypeMapper.deleteById(id);
        // 连带删除该类型下的数据
        dictDataMapper.delete(new LambdaQueryWrapper<MssDictData>().eq(MssDictData::getDictType, type.getDictType()));
    }

    @Override
    public PageResult<MssDictData> dataPage(String dictType, String keyword, long pageNum, long pageSize) {
        Page<MssDictData> page = dictDataMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MssDictData>()
                        .eq(dictType != null && !dictType.isEmpty(), MssDictData::getDictType, dictType)
                        .and(keyword != null && !keyword.isEmpty(), w -> w
                                .like(MssDictData::getDictLabel, keyword)
                                .or()
                                .like(MssDictData::getDictValue, keyword))
                        .orderByAsc(MssDictData::getDictSort)
                        .orderByAsc(MssDictData::getId));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public MssDictData dataGetById(Long id) {
        MssDictData data = dictDataMapper.selectById(id);
        if (data == null) {
            throw new BusinessException("字典数据不存在");
        }
        return data;
    }

    @Override
    public List<MssDictData> dataSelectList(String dictTypes) {
        List<String> types = splitTypes(dictTypes);
        if (types.isEmpty()) {
            return new ArrayList<>();
        }
        return dictDataMapper.selectList(new LambdaQueryWrapper<MssDictData>()
                .in(MssDictData::getDictType, types)
                .eq(MssDictData::getStatus, 1)
                .orderByAsc(MssDictData::getDictType)
                .orderByAsc(MssDictData::getDictSort)
                .orderByAsc(MssDictData::getId));
    }

    @Override
    public List<DictGroupVO> dataSelectGroup(String dictTypes) {
        List<String> types = splitTypes(dictTypes);
        if (types.isEmpty()) {
            return new ArrayList<>();
        }
        List<MssDictData> all = dictDataMapper.selectList(new LambdaQueryWrapper<MssDictData>()
                .in(MssDictData::getDictType, types)
                .eq(MssDictData::getStatus, 1)
                .orderByAsc(MssDictData::getDictType)
                .orderByAsc(MssDictData::getDictSort)
                .orderByAsc(MssDictData::getId));
        Map<String, List<MssDictData>> grouped = all.stream()
                .collect(Collectors.groupingBy(MssDictData::getDictType, LinkedHashMap::new, Collectors.toList()));
        List<DictGroupVO> out = new ArrayList<>();
        grouped.forEach((type, list) -> out.add(new DictGroupVO(type, list)));
        return out;
    }

    private List<String> splitTypes(String dictTypes) {
        if (dictTypes == null || dictTypes.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(dictTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public MssDictData dataUpsert(DictDataUpsertDTO dto) {
        MssDictData data = new MssDictData();
        BeanUtils.copyProperties(dto, data);
        String operator = operatorName();
        if (dto.getId() == null) {
            data.setDictSource(2);
            data.setCreateBy(operator);
            dictDataMapper.insert(data);
        } else {
            if (dictDataMapper.selectById(dto.getId()) == null) {
                throw new BusinessException("字典数据不存在");
            }
            data.setUpdateBy(operator);
            dictDataMapper.updateById(data);
        }
        return data;
    }

    @Override
    public void dataDelete(Long id) {
        if (dictDataMapper.selectById(id) == null) {
            throw new BusinessException("字典数据不存在");
        }
        dictDataMapper.deleteById(id);
    }

    @Override
    public List<MssDictData> dataList(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<MssDictData>()
                .eq(MssDictData::getDictType, dictType)
                .eq(MssDictData::getStatus, 1)
                .orderByAsc(MssDictData::getDictSort)
                .orderByAsc(MssDictData::getId));
    }

    private String operatorName() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }
}
