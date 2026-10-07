package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.util.TextUtil;
import com.his.system.dto.SysDrugQueryPageDTO;
import com.his.system.dto.SysDrugSelectDTO;
import com.his.system.dto.SysDrugUpsertDTO;
import com.his.system.entity.SysDrug;
import com.his.system.mapper.SysDrugMapper;
import com.his.system.service.SysDrugService;
import com.his.system.vo.SysDrugSelectListVO;
import com.his.system.vo.SysDrugVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDrugServiceImpl extends ServiceImpl<SysDrugMapper, SysDrug> implements SysDrugService {

    private final SysDrugMapper sysDrugMapper;

    @Override
    public PageResult<SysDrugVO> listPage(SysDrugQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysDrug> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getDrugName()), SysDrug::getDrugName, queryDTO.getDrugName())
                .eq(queryDTO.getDrugType() != null, SysDrug::getDrugType, queryDTO.getDrugType())
                .eq(queryDTO.getSpecialFlag() != null, SysDrug::getSpecialFlag, queryDTO.getSpecialFlag())
                .orderByAsc(SysDrug::getDrugCode);

        Page<SysDrug> page = sysDrugMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<SysDrugVO> voList = page.getRecords().stream().map(d -> {
            SysDrugVO vo = new SysDrugVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<SysDrugSelectListVO> selectList(SysDrugSelectDTO queryDTO) {
        LambdaQueryWrapper<SysDrug> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getDrugType() != null, SysDrug::getDrugType, queryDTO.getDrugType())
                .eq(SysDrug::getStatus, 1)
                .orderByAsc(SysDrug::getDrugCode);
        return sysDrugMapper.selectList(wrapper).stream().map(d -> {
            SysDrugSelectListVO vo = new SysDrugSelectListVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SysDrugVO getInfo(Long drugId) {
        SysDrug drug = sysDrugMapper.selectById(drugId);
        SysDrugVO vo = new SysDrugVO();
        BeanUtils.copyProperties(drug, vo);
        return vo;
    }

    @Override
    public void upsert(SysDrugUpsertDTO upsertDTO) {
        SysDrug drug = new SysDrug();
        BeanUtils.copyProperties(upsertDTO, drug);
        if (upsertDTO.getId() == null) {
            sysDrugMapper.insert(drug);
        } else {
            sysDrugMapper.updateById(drug);
        }
    }

    @Override
    public void delete(Long drugId) {
        sysDrugMapper.deleteById(drugId);
    }
}
