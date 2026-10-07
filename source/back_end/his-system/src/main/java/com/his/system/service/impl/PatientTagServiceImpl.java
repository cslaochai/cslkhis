package com.his.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.util.TextUtil;
import com.his.system.dto.SysPatientTagQueryDTO;
import com.his.system.dto.SysPatientTagUpsertDTO;
import com.his.system.entity.SysPatientTag;
import com.his.system.mapper.SysPatientTagMapper;
import com.his.system.service.PatientTagService;
import com.his.system.vo.SysPatientTagVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientTagServiceImpl extends ServiceImpl<SysPatientTagMapper, SysPatientTag> implements PatientTagService {

    @Override
    public PageResult<SysPatientTagVO> queryTagPage(SysPatientTagQueryDTO queryDTO) {
        IPage<SysPatientTag> page = new Page<SysPatientTag>(queryDTO.getPageNum(), queryDTO.getPageSize());
        this.page(page, nameLikeWrapper(queryDTO));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), toVoList(page.getRecords()));
    }

    @Override
    public List<SysPatientTagVO> queryTagList(SysPatientTagQueryDTO queryDTO) {
        return toVoList(this.list(nameLikeWrapper(queryDTO)));
    }

    @Override
    public SysPatientTagVO getTagInfo(Long tagId) {
        return toVo(this.getById(tagId));
    }

    @Override
    public String upsertTag(SysPatientTagUpsertDTO upsertDTO) {
        SysPatientTag tag = new SysPatientTag();
        BeanUtils.copyProperties(upsertDTO, tag);
        if (upsertDTO.getTagId() == null) {
            this.save(tag);
            return "新增成功";
        }
        this.updateById(tag);
        return "修改成功";
    }

    @Override
    public void removeTag(Long tagId) {
        this.removeById(tagId);
    }

    private LambdaQueryWrapper<SysPatientTag> nameLikeWrapper(SysPatientTagQueryDTO queryDTO) {
        LambdaQueryWrapper<SysPatientTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getTagName()), SysPatientTag::getTagName, queryDTO.getTagName())
                .orderByAsc(SysPatientTag::getTagId);
        return wrapper;
    }

    private List<SysPatientTagVO> toVoList(List<SysPatientTag> tags) {
        return tags.stream().map(this::toVo).collect(Collectors.toList());
    }

    private SysPatientTagVO toVo(SysPatientTag tag) {
        SysPatientTagVO vo = new SysPatientTagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }
}
