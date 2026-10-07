package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.util.TextUtil;
import com.his.system.dto.*;
import com.his.system.entity.SysInspectionItem;
import com.his.system.entity.SysLaboratoryItem;
import com.his.system.entity.SysLaboratoryItemDetail;
import com.his.system.mapper.SysInspectionItemMapper;
import com.his.system.mapper.SysLaboratoryItemDetailMapper;
import com.his.system.mapper.SysLaboratoryItemMapper;
import com.his.system.service.MedicalItemService;
import com.his.system.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalItemServiceImpl implements MedicalItemService {

    private final SysInspectionItemMapper sysInspectionItemMapper;
    private final SysLaboratoryItemMapper sysLaboratoryItemMapper;
    private final SysLaboratoryItemDetailMapper sysLaboratoryItemDetailMapper;

    @Override
    public PageResult<SysInspectionItemVO> inspectionListPage(SysInspectionItemQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysInspectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getKeyword()), SysInspectionItem::getItemName, queryDTO.getKeyword())
                .eq(queryDTO.getItemType() != null, SysInspectionItem::getItemType, queryDTO.getItemType())
                .orderByAsc(SysInspectionItem::getItemCode);
        Page<SysInspectionItem> page = sysInspectionItemMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<SysInspectionItemVO> voList = page.getRecords().stream().map(i -> {
            SysInspectionItemVO vo = new SysInspectionItemVO();
            BeanUtils.copyProperties(i, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<SysInspectionItemSelectListVO> inspectionSelectList(String keyword, Integer limit) {
        LambdaQueryWrapper<SysInspectionItem> wrapper = new LambdaQueryWrapper<>();
        if (TextUtil.hasText(keyword)) {
            wrapper.and(w -> w.like(SysInspectionItem::getItemName, keyword)
                    .or().like(SysInspectionItem::getItemCode, keyword));
        }
        wrapper.eq(SysInspectionItem::getStatus, 1).orderByAsc(SysInspectionItem::getItemCode);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + Math.min(limit, 200));
        }
        return sysInspectionItemMapper.selectList(wrapper).stream().map(i -> {
            SysInspectionItemSelectListVO vo = new SysInspectionItemSelectListVO();
            BeanUtils.copyProperties(i, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SysInspectionItemVO getInspectionItem(Long id) {
        SysInspectionItem item = sysInspectionItemMapper.selectById(id);
        SysInspectionItemVO vo = new SysInspectionItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    @Override
    public void inspectionUpsert(SysInspectionItemUpsertDTO upsertDTO) {
        SysInspectionItem item = new SysInspectionItem();
        BeanUtils.copyProperties(upsertDTO, item);
        if (upsertDTO.getId() == null) {
            sysInspectionItemMapper.insert(item);
        } else {
            sysInspectionItemMapper.updateById(item);
        }
    }

    @Override
    public void deleteInspectionItem(Long id) {
        sysInspectionItemMapper.deleteById(id);
    }

    @Override
    public PageResult<SysLaboratoryItemVO> laboratoryListPage(SysLaboratoryItemQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysLaboratoryItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getKeyword()), SysLaboratoryItem::getItemName, queryDTO.getKeyword())
                .eq(queryDTO.getItemType() != null, SysLaboratoryItem::getItemType, queryDTO.getItemType())
                .orderByAsc(SysLaboratoryItem::getItemCode);
        Page<SysLaboratoryItem> page = sysLaboratoryItemMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<SysLaboratoryItemVO> voList = page.getRecords().stream().map(i -> {
            SysLaboratoryItemVO vo = new SysLaboratoryItemVO();
            BeanUtils.copyProperties(i, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<SysLaboratoryItemSelectListVO> laboratorySelectList(String keyword, Integer limit) {
        LambdaQueryWrapper<SysLaboratoryItem> wrapper = new LambdaQueryWrapper<>();
        if (TextUtil.hasText(keyword)) {
            wrapper.and(w -> w.like(SysLaboratoryItem::getItemName, keyword)
                    .or().like(SysLaboratoryItem::getItemCode, keyword));
        }
        wrapper.eq(SysLaboratoryItem::getStatus, 1).orderByAsc(SysLaboratoryItem::getItemCode);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + Math.min(limit, 200));
        }
        return sysLaboratoryItemMapper.selectList(wrapper).stream().map(i -> {
            SysLaboratoryItemSelectListVO vo = new SysLaboratoryItemSelectListVO();
            BeanUtils.copyProperties(i, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SysLaboratoryItemVO getLaboratoryItem(Long id) {
        SysLaboratoryItem item = sysLaboratoryItemMapper.selectById(id);
        SysLaboratoryItemVO vo = new SysLaboratoryItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    @Override
    public void laboratoryUpsert(SysLaboratoryItemUpsertDTO upsertDTO) {
        SysLaboratoryItem item = new SysLaboratoryItem();
        BeanUtils.copyProperties(upsertDTO, item);
        if (upsertDTO.getId() == null) {
            sysLaboratoryItemMapper.insert(item);
        } else {
            sysLaboratoryItemMapper.updateById(item);
        }
    }

    @Override
    public void deleteLaboratoryItem(Long id) {
        sysLaboratoryItemMapper.deleteById(id);
    }

    @Override
    public List<SysLaboratoryItemDetailVO> laboratoryDetailList(Long laboratoryItemId) {
        LambdaQueryWrapper<SysLaboratoryItemDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLaboratoryItemDetail::getLaboratoryItemId, laboratoryItemId)
                .eq(SysLaboratoryItemDetail::getStatus, 1)
                .orderByAsc(SysLaboratoryItemDetail::getSortOrder);
        return sysLaboratoryItemDetailMapper.selectList(wrapper).stream().map(d -> {
            SysLaboratoryItemDetailVO vo = new SysLaboratoryItemDetailVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void laboratoryDetailUpsert(SysLaboratoryItemDetailUpsertDTO upsertDTO) {
        SysLaboratoryItemDetail detail = new SysLaboratoryItemDetail();
        BeanUtils.copyProperties(upsertDTO, detail);
        if (upsertDTO.getId() == null) {
            sysLaboratoryItemDetailMapper.insert(detail);
        } else {
            sysLaboratoryItemDetailMapper.updateById(detail);
        }
    }

    @Override
    public void deleteLaboratoryDetail(Long id) {
        sysLaboratoryItemDetailMapper.deleteById(id);
    }
}
