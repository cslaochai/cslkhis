package com.mss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mss.common.base.PageResult;
import com.mss.common.exception.BusinessException;
import com.mss.config.LoginUser;
import com.mss.config.UserContext;
import com.mss.dto.DictDataUpsertDTO;
import com.mss.dto.DictTypeUpsertDTO;
import com.mss.entity.MssDictData;
import com.mss.entity.MssDictType;
import com.mss.mapper.MssDictDataMapper;
import com.mss.mapper.MssDictTypeMapper;
import com.mss.service.SystemDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
